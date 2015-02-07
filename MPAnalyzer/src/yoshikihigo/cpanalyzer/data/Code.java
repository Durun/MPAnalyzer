package yoshikihigo.cpanalyzer.data;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import yoshikihigo.cpanalyzer.StringUtility;
import yoshikihigo.cpanalyzer.lexer.token.Token;

public class Code implements Comparable<Code> {

	private final static AtomicInteger ID_GENERATOR = new AtomicInteger();

	public final List<Statement> statements;
	public final String software;
	private int id;
	public final String text;
	public final String position;
	public final int hash;

	public Code(final String software, final List<Statement> statements) {
		this.software = software;
		this.id = ID_GENERATOR.getAndIncrement();
		this.statements = statements;
		{
			final StringBuilder tmp = new StringBuilder();
			for (final Statement statement : this.statements) {
				tmp.append(statement);
				tmp.append(System.getProperty("line.separator"));
			}
			this.text = tmp.toString();
		}
		{

			if (!statements.isEmpty()) {
				final StringBuilder tmp2 = new StringBuilder();
				tmp2.append(statements.get(0).getStartLine());
				tmp2.append(" --- ");
				tmp2.append(statements.get(statements.size() - 1).getEndLine());
				this.position = tmp2.toString();
			} else {
				this.position = "not exist.";
			}

		}
		this.hash = this.text.hashCode();
	}

	public Code(final String software, final int id, final String text,
			final int startLine, final int endLine) {
		this(software, StringUtility
				.splitToStatements(text, startLine, endLine));
		this.id = id;
	}

	public int getID() {
		return this.id;
	}

	@Override
	public boolean equals(final Object o) {

		if (!(o instanceof Code)) {
			return false;
		}

		final Code target = (Code) o;
		if (this.statements.size() != target.statements.size()) {
			return false;
		}

		for (int i = 0; i < this.statements.size(); i++) {
			if (this.statements.get(i).hash != target.statements.get(i).hash) {
				return false;
			}
		}

		return true;
	}

	@Override
	public int hashCode() {
		return this.hash;
	}

	@Override
	public String toString() {
		return this.text;
	}

	@Override
	public int compareTo(final Code o) {
		return this.text.compareTo(o.text);
	}

	public int getStartLine() {
		if (this.statements.isEmpty()) {
			return 0;
		} else {
			return this.statements.get(0).tokens.get(0).line;
		}
	}

	public int getEndLine() {
		if (this.statements.isEmpty()) {
			return 0;
		} else {
			return this.statements.get(this.statements.size() - 1).tokens
					.get(this.statements.get(this.statements.size() - 1).tokens
							.size() - 1).line;
		}
	}

	public List<Token> getTokens() {
		final List<Token> tokens = new ArrayList<Token>();
		for (final Statement statement : this.statements) {
			tokens.addAll(statement.tokens);
		}
		return tokens;
	}
}
