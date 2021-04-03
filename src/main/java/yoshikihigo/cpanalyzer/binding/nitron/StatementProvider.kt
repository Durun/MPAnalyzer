package yoshikihigo.cpanalyzer.binding.nitron

import io.github.durun.nitron.app.preparse.Extractor
import io.github.durun.nitron.binding.cpanalyzer.CodeProcessor
import io.github.durun.nitron.core.MD5
import io.github.durun.nitron.core.ast.node.AstNode
import io.github.durun.nitron.core.ast.node.AstRuleNode
import io.github.durun.nitron.core.ast.node.AstTerminalNode
import io.github.durun.nitron.core.ast.visitor.AstVisitor
import io.github.durun.nitron.core.config.NitronConfig
import io.github.durun.nitron.core.config.loader.NitronConfigLoader
import yoshikihigo.cpanalyzer.CPAConfig
import yoshikihigo.cpanalyzer.LANGUAGE
import yoshikihigo.cpanalyzer.data.Statement
import yoshikihigo.cpanalyzer.lexer.token.Token
import java.io.File
import java.io.FileNotFoundException
import java.nio.file.Paths

object StatementProvider {
    private val config: NitronConfig = NitronConfigLoader.load(NitronBindConfig.configFile)

    // cache
    private val cacheDB: File? = CPAConfig.getInstance().cacheDB
    private val extractor: Extractor? = run {
        if (cacheDB?.exists() == true) Extractor.open(config, cacheDB.toPath())
        else null
    }

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
                ?: throw NoSuchElementException(lang)
    }

    fun readStatements(fileText: String, lang: String): List<Statement> {
        val processor = getProcessor(lang)
        val fileHash = MD5.digest(fileText).toString()
        val tree = extractor?.getAst(fileHash, lang, processor.nodeTypePool) // get AST from cache
            ?: processor.parse(fileText)
                .also { println("cache missed: $lang, file=$fileHash") }

        val astList = processor.split(tree)

        return astList.mapNotNull { ast ->
            val tokens = ast.toTokens()
            val rText = ast.getText()
            val normAst = processor.proceess(ast)
            if (normAst != null) bindStatement(tokens, rText, normAst)
            else null
        }
    }

    private fun recordStructure(astList: List<AstNode>, lang: String) {
        val processor = getProcessor(lang)
        processor.write(astList)
    }

    @JvmStatic
    fun recordStatementStructure(statementList: List<Statement>, lang: String) {
        val statements = statementList.filterIsInstance<StatementWithAst>()
        val asts = statements.mapNotNull { it.ast }
        recordStructure(asts, lang)
    }

    @JvmStatic
    fun recordStatementStructure(statementList: List<Statement>, language: LANGUAGE) {
        recordStatementStructure(statementList, lang = language.name().toLowerCase())
    }

    private fun AstNode.toTokens(): List<Token> {
        return accept(FastAstFlattenVisitor)
                .mapIndexed { index, it ->
                    NitronBinder.bindToken(
                            value = it.token,
                            line = it.line,
                            index = index
                    )
                }.toList()
    }

    private fun bindStatement(tokens: List<Token>, rText: String, normalized: AstNode?): StatementWithAst {
        return NitronBinder.bindStatement(
                tokens = tokens,
                rText = rText,
                nText = normalized?.getText().orEmpty(),
                ast = normalized
        )
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

object FastAstFlattenVisitor : AstVisitor<List<AstTerminalNode>> {
    override fun visit(node: AstNode): List<AstTerminalNode> {
        return node.children?.flatMap {
            it.accept(this)
        } ?: emptyList()
    }

    override fun visitRule(node: AstRuleNode): List<AstTerminalNode> {
        return visit(node)
    }

    override fun visitTerminal(node: AstTerminalNode): List<AstTerminalNode> {
        return listOf(node)
    }
}