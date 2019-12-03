package yoshikihigo.cpanalyzer.binding.nitron

import io.github.durun.nitron.binding.cpanalyzer.CodeProcessor
import io.github.durun.nitron.core.ast.node.AstNode
import io.github.durun.nitron.core.ast.visitor.AstFlattenVisitor
import io.github.durun.nitron.core.config.NitronConfig
import io.github.durun.nitron.core.config.loader.NitronConfigLoader
import yoshikihigo.cpanalyzer.CPAConfig
import yoshikihigo.cpanalyzer.data.Statement
import yoshikihigo.cpanalyzer.lexer.token.Token
import java.nio.file.Paths
import java.util.*

object StatementProvider {
    private val config: NitronConfig = NitronConfigLoader.load(NitronBindConfig.configFile)

    private val processors: Map<String, Lazy<CodeProcessor>> = config.langConfig
            .mapValues { lazy {
                val suffix = ".structures"
                CodeProcessor(
                        it.value,
                        outputPath = Paths.get(CPAConfig.getInstance().database + suffix)
                )
            } }

    private fun getProcessor(lang: String): CodeProcessor {
        return processors[lang]?.value
                ?: throw NoSuchElementException("$lang")
    }

    fun readStatements(fileText: String, lang: String): List<Statement> {
        val processor = getProcessor(lang)
        val astList = processor.parseSplitting(fileText)
        return processor
                .proceessWithOriginal(astList)
                .mapNotNull { (ast, normAst) ->
                    if (normAst != null) ast to normAst
                    else null
                }
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
                            line = it.range.line.first,
                            index = index
                    )
                }
    }

    private fun bindStatement(original: AstNode, normalized: AstNode?): StatementWithAst {
        return NitronBinder.bindStatement(
                tokens = original.toTokens(),
                rText = original.getText(),
                nText = normalized?.getText().orEmpty(),
                ast = normalized
        )
    }
}