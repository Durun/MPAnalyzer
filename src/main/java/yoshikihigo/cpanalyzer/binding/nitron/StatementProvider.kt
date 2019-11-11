package yoshikihigo.cpanalyzer.binding.nitron

import io.github.durun.nitron.binding.cpanalyzer.CodeProcessor
import io.github.durun.nitron.core.ast.visitor.AstFlattenVisitor
import io.github.durun.nitron.core.config.NitronConfig
import io.github.durun.nitron.core.config.loader.NitronConfigLoader
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
        val result = processor.process(fileText)
        return result.map { (statement, nText) ->
            val tokens = statement.accept(AstFlattenVisitor)
                    .mapIndexed { index, it ->
                        NitronBinder.bindToken(
                                value = it.token,
                                line = it.range.line.start,
                                index = index
                        )
                    }
            NitronBinder.bindStatement(
                    tokens = tokens,
                    rText = statement.getText() ?: throw Exception(),
                    nText = nText
            )
        }
    }

}