package yoshikihigo.cpanalyzer.lexer;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import yoshikihigo.cpanalyzer.lexer.token.ABSTRACT;
import yoshikihigo.cpanalyzer.lexer.token.AND;
import yoshikihigo.cpanalyzer.lexer.token.ANNOTATION;
import yoshikihigo.cpanalyzer.lexer.token.ASSERT;
import yoshikihigo.cpanalyzer.lexer.token.ASSIGN;
import yoshikihigo.cpanalyzer.lexer.token.BOOLEAN;
import yoshikihigo.cpanalyzer.lexer.token.BREAK;
import yoshikihigo.cpanalyzer.lexer.token.BYTE;
import yoshikihigo.cpanalyzer.lexer.token.CASE;
import yoshikihigo.cpanalyzer.lexer.token.CATCH;
import yoshikihigo.cpanalyzer.lexer.token.CHAR;
import yoshikihigo.cpanalyzer.lexer.token.CHARLITERAL;
import yoshikihigo.cpanalyzer.lexer.token.CLASS;
import yoshikihigo.cpanalyzer.lexer.token.COLON;
import yoshikihigo.cpanalyzer.lexer.token.COMMA;
import yoshikihigo.cpanalyzer.lexer.token.CONST;
import yoshikihigo.cpanalyzer.lexer.token.CONTINUE;
import yoshikihigo.cpanalyzer.lexer.token.DECREMENT;
import yoshikihigo.cpanalyzer.lexer.token.DEFAULT;
import yoshikihigo.cpanalyzer.lexer.token.DIVIDE;
import yoshikihigo.cpanalyzer.lexer.token.DIVIDEEQUAL;
import yoshikihigo.cpanalyzer.lexer.token.DO;
import yoshikihigo.cpanalyzer.lexer.token.DOT;
import yoshikihigo.cpanalyzer.lexer.token.DOUBLE;
import yoshikihigo.cpanalyzer.lexer.token.ELSE;
import yoshikihigo.cpanalyzer.lexer.token.ENUM;
import yoshikihigo.cpanalyzer.lexer.token.EQUAL;
import yoshikihigo.cpanalyzer.lexer.token.EXTENDS;
import yoshikihigo.cpanalyzer.lexer.token.FALSE;
import yoshikihigo.cpanalyzer.lexer.token.FINAL;
import yoshikihigo.cpanalyzer.lexer.token.FINALLY;
import yoshikihigo.cpanalyzer.lexer.token.FLOAT;
import yoshikihigo.cpanalyzer.lexer.token.FOR;
import yoshikihigo.cpanalyzer.lexer.token.GOTO;
import yoshikihigo.cpanalyzer.lexer.token.GREAT;
import yoshikihigo.cpanalyzer.lexer.token.GREATEQUAL;
import yoshikihigo.cpanalyzer.lexer.token.IDENTIFIER;
import yoshikihigo.cpanalyzer.lexer.token.IF;
import yoshikihigo.cpanalyzer.lexer.token.IMPLEMENTS;
import yoshikihigo.cpanalyzer.lexer.token.IMPORT;
import yoshikihigo.cpanalyzer.lexer.token.INCREMENT;
import yoshikihigo.cpanalyzer.lexer.token.INSTANCEOF;
import yoshikihigo.cpanalyzer.lexer.token.INT;
import yoshikihigo.cpanalyzer.lexer.token.INTERFACE;
import yoshikihigo.cpanalyzer.lexer.token.LEFTBRACKET;
import yoshikihigo.cpanalyzer.lexer.token.LEFTPAREN;
import yoshikihigo.cpanalyzer.lexer.token.LEFTSQUAREBRACKET;
import yoshikihigo.cpanalyzer.lexer.token.LESS;
import yoshikihigo.cpanalyzer.lexer.token.LESSEQUAL;
import yoshikihigo.cpanalyzer.lexer.token.LONG;
import yoshikihigo.cpanalyzer.lexer.token.MINUS;
import yoshikihigo.cpanalyzer.lexer.token.MINUSEQUAL;
import yoshikihigo.cpanalyzer.lexer.token.MOD;
import yoshikihigo.cpanalyzer.lexer.token.MODEQUAL;
import yoshikihigo.cpanalyzer.lexer.token.NATIVE;
import yoshikihigo.cpanalyzer.lexer.token.NEW;
import yoshikihigo.cpanalyzer.lexer.token.NOT;
import yoshikihigo.cpanalyzer.lexer.token.NULL;
import yoshikihigo.cpanalyzer.lexer.token.NUMBERLITERAL;
import yoshikihigo.cpanalyzer.lexer.token.OR;
import yoshikihigo.cpanalyzer.lexer.token.PACKAGE;
import yoshikihigo.cpanalyzer.lexer.token.PLUS;
import yoshikihigo.cpanalyzer.lexer.token.PLUSEQUAL;
import yoshikihigo.cpanalyzer.lexer.token.PRIVATE;
import yoshikihigo.cpanalyzer.lexer.token.PROTECTED;
import yoshikihigo.cpanalyzer.lexer.token.PUBLIC;
import yoshikihigo.cpanalyzer.lexer.token.QUESTION;
import yoshikihigo.cpanalyzer.lexer.token.RETURN;
import yoshikihigo.cpanalyzer.lexer.token.RIGHTBRACKET;
import yoshikihigo.cpanalyzer.lexer.token.RIGHTPAREN;
import yoshikihigo.cpanalyzer.lexer.token.RIGHTSQUAREBRACKET;
import yoshikihigo.cpanalyzer.lexer.token.SEMICOLON;
import yoshikihigo.cpanalyzer.lexer.token.SHORT;
import yoshikihigo.cpanalyzer.lexer.token.STAR;
import yoshikihigo.cpanalyzer.lexer.token.STAREQUAL;
import yoshikihigo.cpanalyzer.lexer.token.STATIC;
import yoshikihigo.cpanalyzer.lexer.token.STRICTFP;
import yoshikihigo.cpanalyzer.lexer.token.STRINGLITERAL;
import yoshikihigo.cpanalyzer.lexer.token.SUPER;
import yoshikihigo.cpanalyzer.lexer.token.SWITCH;
import yoshikihigo.cpanalyzer.lexer.token.SYNCHRONIZED;
import yoshikihigo.cpanalyzer.lexer.token.THIS;
import yoshikihigo.cpanalyzer.lexer.token.THROW;
import yoshikihigo.cpanalyzer.lexer.token.THROWS;
import yoshikihigo.cpanalyzer.lexer.token.TRANSIENT;
import yoshikihigo.cpanalyzer.lexer.token.TRUE;
import yoshikihigo.cpanalyzer.lexer.token.TRY;
import yoshikihigo.cpanalyzer.lexer.token.Token;
import yoshikihigo.cpanalyzer.lexer.token.VOID;
import yoshikihigo.cpanalyzer.lexer.token.VOLATILE;
import yoshikihigo.cpanalyzer.lexer.token.WHILE;

public class JavaLineLexer implements LineLexer {

	@Override
	public List<Token> lexFile(final String text) {
		try {
			final BufferedReader reader = new BufferedReader(new StringReader(
					text));
			final JavaLineLexer lexer = new JavaLineLexer();
			final List<Token> tokens = new ArrayList<Token>();
			String line;
			int lineNumber = 1;
			while (null != (line = reader.readLine())) {
				for (final Token t : lexer.lexLine(line)) {
					t.line = lineNumber;
					tokens.add(t);
				}
				lineNumber++;
			}

			return tokens;

		} catch (final Exception e) {
			e.printStackTrace();
			System.exit(0);
			return null;
		}
	}

	public List<Token> lexLine(final String line) {
		final List<Token> tokenList = new ArrayList<Token>();
		this.lex(new StringBuilder(line), tokenList);
		return tokenList;
	}

	private void lex(final StringBuilder text, final List<Token> tokenList) {

		if (0 == text.length()) {
			return;
		}

		final String string = text.toString();
		if (string.startsWith("-=")) {
			text.delete(0, 2);
			tokenList.add(new MINUSEQUAL());
		} else if (string.startsWith("+=")) {
			text.delete(0, 2);
			tokenList.add(new PLUSEQUAL());
		} else if (string.startsWith("/=")) {
			text.delete(0, 2);
			tokenList.add(new DIVIDEEQUAL());
		} else if (string.startsWith("*=")) {
			text.delete(0, 2);
			tokenList.add(new STAREQUAL());
		} else if (string.startsWith("%=")) {
			text.delete(0, 2);
			tokenList.add(new MODEQUAL());
		} else if (string.startsWith("++")) {
			text.delete(0, 2);
			tokenList.add(new INCREMENT());
		} else if (string.startsWith("--")) {
			text.delete(0, 2);
			tokenList.add(new DECREMENT());
		} else if (string.startsWith("<=")) {
			text.delete(0, 2);
			tokenList.add(new LESSEQUAL());
		} else if (string.startsWith(">=")) {
			text.delete(0, 2);
			tokenList.add(new GREATEQUAL());
		} else if (string.startsWith("==")) {
			text.delete(0, 2);
			tokenList.add(new EQUAL());
		} else if (string.startsWith("!")) {
			text.delete(0, 1);
			tokenList.add(new NOT());
		}

		else if (string.startsWith(":")) {
			text.delete(0, 1);
			tokenList.add(new COLON());
		} else if (string.startsWith(";")) {
			text.delete(0, 1);
			tokenList.add(new SEMICOLON());
		} else if (string.startsWith("=")) {
			text.delete(0, 1);
			tokenList.add(new ASSIGN());
		} else if (string.startsWith("-")) {
			text.delete(0, 1);
			tokenList.add(new MINUS());
		} else if (string.startsWith("+")) {
			text.delete(0, 1);
			tokenList.add(new PLUS());
		} else if (string.startsWith("/")) {
			text.delete(0, 1);
			tokenList.add(new DIVIDE());
		} else if (string.startsWith("*")) {
			text.delete(0, 1);
			tokenList.add(new STAR());
		} else if (string.startsWith("%")) {
			text.delete(0, 1);
			tokenList.add(new MOD());
		} else if (string.startsWith("?")) {
			text.delete(0, 1);
			tokenList.add(new QUESTION());
		} else if (string.startsWith("<")) {
			text.delete(0, 1);
			tokenList.add(new LESS());
		} else if (string.startsWith(">")) {
			text.delete(0, 1);
			tokenList.add(new GREAT());
		} else if (string.startsWith("&")) {
			text.delete(0, 1);
			tokenList.add(new AND());
		} else if (string.startsWith("|")) {
			text.delete(0, 1);
			tokenList.add(new OR());
		} else if (string.startsWith("(")) {
			text.delete(0, 1);
			tokenList.add(new LEFTPAREN());
		} else if (string.startsWith(")")) {
			text.delete(0, 1);
			tokenList.add(new RIGHTPAREN());
		} else if (string.startsWith("{")) {
			text.delete(0, 1);
			tokenList.add(new LEFTBRACKET());
		} else if (string.startsWith("}")) {
			text.delete(0, 1);
			tokenList.add(new RIGHTBRACKET());
		} else if (string.startsWith("[")) {
			text.delete(0, 1);
			tokenList.add(new LEFTSQUAREBRACKET());
		} else if (string.startsWith("]")) {
			text.delete(0, 1);
			tokenList.add(new RIGHTSQUAREBRACKET());
		} else if (string.startsWith(",")) {
			text.delete(0, 1);
			tokenList.add(new COMMA());
		} else if (string.startsWith(".")) {
			text.delete(0, 1);
			tokenList.add(new DOT());
		}

		else if ('\"' == string.charAt(0)) {
			int index = 1;
			while (index < string.length()) {
				if ('\"' == string.charAt(index)) {
					break;
				}
				index++;
			}
			final String value = text.substring(1, index);
			text.delete(0, index + 1);
			tokenList.add(new STRINGLITERAL(value));
		}

		else if ('\'' == string.charAt(0)) {
			int index = 1;
			while (index < string.length()) {
				if ('\'' == string.charAt(index)) {
					break;
				}
				index++;
			}
			final String value = text.substring(1, index);
			text.delete(0, index + 1);
			tokenList.add(new CHARLITERAL(value));
		}

		else if ('/' == string.charAt(0)) {

			if ((2 <= string.length()) && ('/' == string.charAt(1))) {
				return;
			}
		}

		else if (isDigit(string.charAt(0))) {
			int index = 1;
			while (index < string.length()) {
				if (!isDigit(string.charAt(index))) {
					break;
				}
				index++;
			}
			text.delete(0, index);
			final String sconstant = string.substring(0, index);
			tokenList.add(new NUMBERLITERAL(sconstant));
		}

		else if (isAlphabet(string.charAt(0))) {
			int index = 1;
			while (index < string.length()) {
				if (!isAlphabet(string.charAt(index))
						&& !isDigit(string.charAt(index))
						&& '_' != string.charAt(index)
						&& '$' != string.charAt(index)) {
					break;
				}
				index++;
			}
			text.delete(0, index);
			final String identifier = string.substring(0, index);

			if (identifier.equals("abstract")) {
				tokenList.add(new ABSTRACT());
			} else if (identifier.equals("assert")) {
				tokenList.add(new ASSERT());
			} else if (identifier.equals("boolean")) {
				tokenList.add(new BOOLEAN());
			} else if (identifier.equals("break")) {
				tokenList.add(new BREAK());
			} else if (identifier.equals("byte")) {
				tokenList.add(new BYTE());
			} else if (identifier.equals("case")) {
				tokenList.add(new CASE());
			} else if (identifier.equals("catch")) {
				tokenList.add(new CATCH());
			} else if (identifier.equals("char")) {
				tokenList.add(new CHAR());
			} else if (identifier.equals("class")) {
				tokenList.add(new CLASS());
			} else if (identifier.equals("const")) {
				tokenList.add(new CONST());
			} else if (identifier.equals("continue")) {
				tokenList.add(new CONTINUE());
			} else if (identifier.equals("default")) {
				tokenList.add(new DEFAULT());
			} else if (identifier.equals("do")) {
				tokenList.add(new DO());
			} else if (identifier.equals("double")) {
				tokenList.add(new DOUBLE());
			} else if (identifier.equals("else")) {
				tokenList.add(new ELSE());
			} else if (identifier.equals("enum")) {
				tokenList.add(new ENUM());
			} else if (identifier.equals("extends")) {
				tokenList.add(new EXTENDS());
			} else if (identifier.equals("false")) {
				tokenList.add(new FALSE());
			} else if (identifier.equals("final")) {
				tokenList.add(new FINAL());
			} else if (identifier.equals("finally")) {
				tokenList.add(new FINALLY());
			} else if (identifier.equals("float")) {
				tokenList.add(new FLOAT());
			} else if (identifier.equals("for")) {
				tokenList.add(new FOR());
			} else if (identifier.equals("goto")) {
				tokenList.add(new GOTO());
			} else if (identifier.equals("if")) {
				tokenList.add(new IF());
			} else if (identifier.equals("implements")) {
				tokenList.add(new IMPLEMENTS());
			} else if (identifier.equals("import")) {
				tokenList.add(new IMPORT());
			} else if (identifier.equals("instanceof")) {
				tokenList.add(new INSTANCEOF());
			} else if (identifier.equals("int")) {
				tokenList.add(new INT());
			} else if (identifier.equals("interface")) {
				tokenList.add(new INTERFACE());
			} else if (identifier.equals("long")) {
				tokenList.add(new LONG());
			} else if (identifier.equals("native")) {
				tokenList.add(new NATIVE());
			} else if (identifier.equals("new")) {
				tokenList.add(new NEW());
			} else if (identifier.equals("null")) {
				tokenList.add(new NULL());
			} else if (identifier.equals("package")) {
				tokenList.add(new PACKAGE());
			} else if (identifier.equals("private")) {
				tokenList.add(new PRIVATE());
			} else if (identifier.equals("protected")) {
				tokenList.add(new PROTECTED());
			} else if (identifier.equals("public")) {
				tokenList.add(new PUBLIC());
			} else if (identifier.equals("return")) {
				tokenList.add(new RETURN());
			} else if (identifier.equals("short")) {
				tokenList.add(new SHORT());
			} else if (identifier.equals("static")) {
				tokenList.add(new STATIC());
			} else if (identifier.equals("strictfp")) {
				tokenList.add(new STRICTFP());
			} else if (identifier.equals("super")) {
				tokenList.add(new SUPER());
			} else if (identifier.equals("switch")) {
				tokenList.add(new SWITCH());
			} else if (identifier.equals("synchronized")) {
				tokenList.add(new SYNCHRONIZED());
			} else if (identifier.equals("this")) {
				tokenList.add(new THIS());
			} else if (identifier.equals("throw")) {
				tokenList.add(new THROW());
			} else if (identifier.equals("throws")) {
				tokenList.add(new THROWS());
			} else if (identifier.equals("transient")) {
				tokenList.add(new TRANSIENT());
			} else if (identifier.equals("true")) {
				tokenList.add(new TRUE());
			} else if (identifier.equals("try")) {
				tokenList.add(new TRY());
			} else if (identifier.equals("void")) {
				tokenList.add(new VOID());
			} else if (identifier.equals("volatile")) {
				tokenList.add(new VOLATILE());
			} else if (identifier.equals("while")) {
				tokenList.add(new WHILE());
			} else {
				tokenList.add(new IDENTIFIER(identifier));
			}
		}

		else if ('@' == string.charAt(0)) {

			int index = 1;
			while (index < string.length()) {
				if (' ' == string.charAt(index) || ' ' == string.charAt(index)) {

				}
				index++;
			}
			text.delete(0, index);
			final String value = string.substring(0, index);
			tokenList.add(new ANNOTATION(value));
		}

		else if (' ' == string.charAt(0) || '\t' == string.charAt(0)) {
			text.deleteCharAt(0);
		}

		else {
			// assert false : "unexpected situation: " + string;
			text.delete(0, 1);
		}

		this.lex(text, tokenList);
	}

	private static boolean isAlphabet(final char c) {
		return Character.isLowerCase(c) || Character.isUpperCase(c);
	}

	private static boolean isDigit(final char c) {
		return '0' == c || '1' == c || '2' == c || '3' == c || '4' == c
				|| '5' == c || '6' == c || '7' == c || '8' == c || '9' == c;
	}
}