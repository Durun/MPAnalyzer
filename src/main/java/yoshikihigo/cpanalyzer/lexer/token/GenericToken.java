package yoshikihigo.cpanalyzer.lexer.token;

/**
 * for binding nitron
 */
public class GenericToken extends Token {
    public GenericToken(String value, int line, int index) {
        super(value);
        this.line = line;
        this.index = index;
    }
}
