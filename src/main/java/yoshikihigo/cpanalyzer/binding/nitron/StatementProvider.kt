package yoshikihigo.cpanalyzer.binding.nitron

import io.github.durun.nitron.binding.cpanalyzer.CodeProcessor
import io.github.durun.nitron.core.ast.AstNode
import io.github.durun.nitron.core.ast.visitor.AstFlattenVisitor
import io.github.durun.nitron.core.config.NitronConfig
import io.github.durun.nitron.core.config.loader.NitronConfigLoader
import yoshikihigo.cpanalyzer.data.Statement
import yoshikihigo.cpanalyzer.lexer.token.Token
import java.util.*

object StatementProvider {
    private val config: NitronConfig = NitronConfigLoader.load(NitronBindConfig.configFile)

    private val processors: Map<String, Lazy<CodeProcessor>> = config.langConfig
            .mapValues { lazy { CodeProcessor(it.value) } }

    private fun getProcessor(lang: String): CodeProcessor {
        return processors[lang]?.value
                ?: throw NoSuchElementException("$lang")
    }

    fun readStatements(fileText: String, lang: String): List<Statement> {
        val processor = getProcessor(lang)
        val astList = processor.parseSplitting(fileText)
        return processor
                .proceessWithOriginal(astList)
                .filter { it.second != null }
                .map { (ast, normAst) -> bindStatement(ast, normAst) }
    }

    private fun CodeProcessor.parseSplitting(fileText: String): List<AstNode> {
        return this.split(fileText)
    }


    private fun AstNode.toTokens(): List<Token> {
        return accept(AstFlattenVisitor)
                .mapIndexed { index, it ->
                    NitronBinder.bindToken(
                            value = it.token,
                            line = it.range.line.start,
                            index = index
                    )
                }
    }

    private fun bindStatement(original: AstNode, normalized: AstNode?): Statement {
        return NitronBinder.bindStatement(
                tokens = original.toTokens(),
                rText = original.getText() ?: throw Exception(),
                nText = normalized?.getText().orEmpty().trim()
        )
    }
}