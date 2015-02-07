package jp.ac.osaka_u.ist.sdl.mpanalyzer.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;

import jp.ac.osaka_u.ist.sdl.mpanalyzer.Config;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.data.Change;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.data.Revision;

public class ChangeDAO {

	static public final String REVISIONS_SCHEMA = "software string, number integer, date string, message string, primary key(software, number)";
	static public final String CODES_SCHEMA = "software string, id integer, text string, hash integer, start int, end int, primary key(software, id)";
	static public final String CHANGES_SCHEMA = "software string, id integer, filepath string, beforeID integer, beforeHash integer, afterID integer, afterHash integer, revision integer, type integer, primary key(software, id)";

	private Connection connector;
	private PreparedStatement codePS;
	private PreparedStatement changePS;
	private int numberOfCodePS;
	private int numberOfChangePS;

	public ChangeDAO() {

		try {
			Class.forName("org.sqlite.JDBC");
			final String database = Config.getInstance().getDATABASE();
			this.connector = DriverManager.getConnection("jdbc:sqlite:"
					+ database);

			final Statement statement = this.connector.createStatement();
			statement.executeUpdate("create table if not exists revisions ("
					+ REVISIONS_SCHEMA + ")");
			statement.executeUpdate("create table if not exists codes ("
					+ CODES_SCHEMA + ")");
			statement.executeUpdate("create table if not exists changes ("
					+ CHANGES_SCHEMA + ")");
			statement.close();

			this.codePS = this.connector
					.prepareStatement("insert into codes values (?, ?, ?, ?, ?, ?)");
			this.changePS = this.connector
					.prepareStatement("insert into changes values(?, ?, ?, ?, ?, ?, ?, ?, ?)");

			this.numberOfCodePS = 0;
			this.numberOfChangePS = 0;

		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
			System.exit(0);
		}
	}

	public void addRevisions(final Revision[] revisions) {

		try {
			final PreparedStatement statement = this.connector
					.prepareStatement("insert into revisions values (?, ?, ?, ?)");
			for (final Revision revision : revisions) {
				statement.setString(1, revision.software);
				statement.setLong(2, revision.number);
				statement.setString(3, revision.date);
				statement.setString(4, revision.message);
				statement.addBatch();
			}
			statement.executeBatch();
			statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
			System.exit(0);
		}
	}

	public void addChange(final Change change) {

		try {
			this.codePS.setString(1, change.before.software);
			this.codePS.setInt(2, change.before.getID());
			this.codePS.setString(3, change.before.text);
			this.codePS.setInt(4, change.before.hash);
			final int beforeStart = change.before.statements.isEmpty() ? 0
					: change.before.statements.get(0).getStartLine();
			final int beforeEnd = change.before.statements.isEmpty() ? 0
					: change.before.statements.get(
							change.before.statements.size() - 1).getEndLine();
			this.codePS.setInt(5, beforeStart);
			this.codePS.setInt(6, beforeEnd);
			this.codePS.addBatch();
			this.numberOfCodePS++;

			this.codePS.setString(1, change.after.software);
			this.codePS.setInt(2, change.after.getID());
			this.codePS.setString(3, change.after.text);
			this.codePS.setInt(4, change.after.hash);
			final int afterStart = change.after.statements.isEmpty() ? 0
					: change.after.statements.get(0).getStartLine();
			final int afterEnd = change.after.statements.isEmpty() ? 0
					: change.after.statements.get(
							change.after.statements.size() - 1).getEndLine();
			this.codePS.setInt(5, afterStart);
			this.codePS.setInt(6, afterEnd);
			this.codePS.addBatch();
			this.numberOfCodePS++;

			this.changePS.setString(1, change.software);
			this.changePS.setInt(2, change.id);
			this.changePS.setString(3, change.filepath);
			this.changePS.setInt(4, change.before.getID());
			this.changePS.setInt(5, change.before.hash);
			this.changePS.setInt(6, change.after.getID());
			this.changePS.setInt(7, change.after.hash);
			this.changePS.setInt(8, (int) change.revision.number);
			this.changePS.setInt(9, change.changeType.getValue());
			this.changePS.addBatch();
			this.numberOfChangePS++;

			if (10000 < this.numberOfCodePS) {
				System.out.println("writing \'codes\' table ...");
				this.codePS.executeBatch();
				this.numberOfCodePS = 0;
			}

			if (10000 < this.numberOfChangePS) {
				System.out.println("writing \'changes\' table ...");
				this.changePS.executeBatch();
				this.numberOfChangePS = 0;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			System.exit(0);
		}
	}

	public void addChanges(Collection<Change> changes) throws Exception {
		for (final Change change : changes) {
			this.addChange(change);
		}
	}

	public void flush() {
		try {
			if (0 < this.numberOfCodePS) {
				System.out.println("writing \'codes\' table ...");
				this.codePS.executeBatch();
				this.numberOfCodePS = 0;
			}
			if (0 < this.numberOfChangePS) {
				System.out.println("writing \'changes\' table ...");
				this.changePS.executeBatch();
				this.numberOfChangePS = 0;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			System.exit(0);
		}
	}

	public void close() {
		try {
			this.codePS.close();
			this.changePS.close();
			this.connector.close();
		} catch (SQLException e) {
			e.printStackTrace();
			System.exit(0);
		}
	}
}
