package yoshikihigo.cpanalyzer.binding.nitron

import io.github.durun.nitron.core.ast.node.AstNode
import yoshikihigo.cpanalyzer.data.Statement
import yoshikihigo.cpanalyzer.lexer.token.Token

class StatementWithAst(
        fromLine: Int,
        toLine: Int,
        nestLevel: Int,
        isTarget: Boolean,
        tokens: List<Token>,
        rText: String,
        nText: String,
        hash: ByteArray,
        val ast: AstNode?
) : Statement(fromLine, toLine, nestLevel, isTarget, tokens, rText, nText, hash) {
}