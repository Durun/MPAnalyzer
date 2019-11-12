package yoshikihigo.cpanalyzer.binding.nitron

import io.github.durun.nitron.binding.cpanalyzer.CodeProcessor
import io.github.durun.nitron.core.ast.visitor.AstFlattenVisitor
import io.github.durun.nitron.core.config.NitronConfig
import io.github.durun.nitron.core.config.loader.NitronConfigLoader
import io.github.durun.nitron.inout.model.table.Codes.nText
import yoshikihigo.cpanalyzer.data.Statement
import java.util.*

object StatementProvider {
    private val config: NitronConfig = NitronConfigLoader.load(NitronBindConfig.configFile)

    private val processors: Map<String, Lazy<CodeProcessor>> = config.langConfig
            .mapValues { lazy { CodeProcessor(it.value) } }

    private fun getProcessor(lang: String): CodeProcessor {
        return processors[lang]?.value
                ?: throw NoSuchElementException("$lang")
    }

    fun parse(fileText: String, lang: String): List<Statement> {
        val processor = getProcessor(lang)
        val astList = processor.split(fileText)
        val result = processor.proceessWithOriginal(astList)
        return result.map { (ast, normAst) ->
            val tokens = ast.accept(AstFlattenVisitor)
                    .mapIndexed { index, it ->
                        NitronBinder.bindToken(
                                value = it.token,
                                line = it.range.line.start,
                                index = index
                        )
                    }
            val nText = normAst?.getText().orEmpty()
            NitronBinder.bindStatement(
                    tokens = tokens,
                    rText = ast.getText() ?: throw Exception(),
                    nText = nText
            )
        }
    }

}