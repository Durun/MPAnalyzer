package yoshikihigo.cpanalyzer.binding.nitron

import yoshikihigo.cpanalyzer.data.Statement
import yoshikihigo.cpanalyzer.lexer.token.GenericToken
import yoshikihigo.cpanalyzer.lexer.token.Token
import java.security.MessageDigest

object NitronBinder {
    private val digester = MessageDigest.getInstance("MD5")

    fun bindToken(
            value: String,
            line: Int,
            index: Int
    ): Token {
        return GenericToken(value, line, index)
    }

    fun bindStatement(
            tokens: List<Token>,
            rText: String,
            nText: String,
            nestLevel: Int = -1,        // TODO
            isTarget: Boolean = true    // TODO
    ): Statement {
        val fromLine = tokens.first().line
        val toLine = tokens.last().line
        val hash = digester.digest(nText.toByteArray())
        return Statement(fromLine, toLine, nestLevel, isTarget, tokens, rText, nText, hash)
    }
}