package yoshikihigo.cpanalyzer.binding.nitron

import io.github.durun.nitron.core.MD5
import io.github.durun.nitron.core.ast.node.AstNode
import yoshikihigo.cpanalyzer.data.Statement
import yoshikihigo.cpanalyzer.lexer.token.GenericToken
import yoshikihigo.cpanalyzer.lexer.token.Token

object NitronBinder {
    fun bindToken(
            value: String,
            line: Int,
            index: Int
    ): Token {
        return GenericToken(value, line, index)
    }

    @Deprecated("returns Statement with no AST information.")
    fun bindStatement(
            tokens: List<Token>,
            rText: String,
            nText: String,
            nestLevel: Int = -1,        // TODO
            isTarget: Boolean = true    // TODO
    ): Statement {
        val fromLine = tokens.first().line
        val toLine = tokens.last().line
        val hash = MD5.digest(nText).bytes
        return Statement(fromLine, toLine, nestLevel, isTarget, tokens, rText, nText, hash)
    }

    fun bindStatement(
            tokens: List<Token>,
            rText: String,
            nText: String,
            nestLevel: Int = -1,        // TODO
            isTarget: Boolean = true,   // TODO
            ast: AstNode?
    ): StatementWithAst {
        val fromLine = tokens.first().line
        val toLine = tokens.last().line
        val hash = MD5.digest(nText).bytes
        return StatementWithAst(fromLine, toLine, nestLevel, isTarget, tokens, rText, nText, hash,
                ast)
    }
}