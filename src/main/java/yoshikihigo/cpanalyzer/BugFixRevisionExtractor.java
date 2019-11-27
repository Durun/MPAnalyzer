package yoshikihigo.cpanalyzer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import com.google.common.base.Splitter;

public class BugFixRevisionExtractor {

  public static void main(final String[] args) {
    CPAConfig.initialize(args);
    final BugFixRevisionExtractor main = new BugFixRevisionExtractor();
    main.make();
  }

  private void make() {

    final String BUGFIXREVISIONS_SCHEMA =
        "repo string, " + "id string, " + "date string, " + "message string, " + "author string, "
            + "bugfix integer, " + "info string, " + "primary key(repo, id)";
    final CPAConfig config = CPAConfig.getInstance();
    final String database = config.getDATABASE();
    final Map<String, String> bugIDs = this.getBugIDs();

    try {
      Class.forName("org.sqlite.JDBC");
      final Connection connector = DriverManager.getConnection("jdbc:sqlite:" + database);

      final Statement statementA = connector.createStatement();
      statementA.executeUpdate("update revisions set bugfix = 0");
      statementA.close();

      final Statement statementB = connector.createStatement();
      final ResultSet resultB = statementB.executeQuery("select repo, id, message from revisions");
      while (resultB.next()) {
        final String repo = resultB.getString(1);
        final String id = resultB.getString(2);
        String message = resultB.getString(3);
        if (message.contains("git-svn-id")) {
          message = message.substring(0, message.indexOf("git-svn-id"));
        }

        int bugfix = 0;
        final StringBuilder urls = new StringBuilder();
        for (final Entry<String, String> entry : bugIDs.entrySet()) {
          if (message.contains("Merged revisions")) {
            continue;
          }
          final String bugId = entry.getKey();
          if (/* message.contains("CAMEL-")&&!(message.contains("/branches/")) */message
              .endsWith(bugId) || message.contains(bugId + " ") || message.contains(bugId + "\t")
              || message.contains(bugId + '\r') || message.contains(bugId + '\n')
              || message.contains(bugId + ":") || message.contains(bugId + ";")
              || message.contains(bugId + ".")) {
            bugfix++;
            final String url = entry.getValue();
            urls.append(url);
            urls.append(System.lineSeparator());
          }
        }

        if (0 < bugfix) {
          final StringBuilder updateText = new StringBuilder();
          updateText.append("update revisions set bugfix = ")
              .append(bugfix)
              .append(" where repo = \'")
              .append(repo)
              .append("\' and id = \'")
              .append(id)
              .append("\'");
          final Statement statementC = connector.createStatement();
          statementC.executeUpdate(updateText.toString());
          statementC.close();
        }
      }
      statementB.close();


      final Statement statement1 = connector.createStatement();
      statement1.executeUpdate("drop index if exists index_id_bugfixrevisions");
      statement1.executeUpdate("drop index if exists index_bugfix_bugfixrevisions");
      statement1.executeUpdate("drop table if exists bugfixrevisions");
      statement1.executeUpdate("create table bugfixrevisions (" + BUGFIXREVISIONS_SCHEMA + ")");
      statement1.executeUpdate("create index index_id_bugfixrevisions on bugfixrevisions(id)");
      statement1
          .executeUpdate("create index index_bugfix_bugfixrevisions on bugfixrevisions(bugfix)");
      statement1.close();

      final Statement statement2 = connector.createStatement();
      final ResultSet results2 =
          statement2.executeQuery("select repo, id, date, message, author from revisions");
      final PreparedStatement statement3 =
          connector.prepareStatement("insert into bugfixrevisions values (?, ?, ?, ?, ?, ?, ?)");
      while (results2.next()) {
        final String repo = results2.getString(1);
        final String id = results2.getString(2);
        final String date = results2.getString(3);
        String message = results2.getString(4);
        if (message.contains("git-svn-id")) {
          message = message.substring(0, message.indexOf("git-svn-id"));
        }
        final String author = results2.getString(5);

        int bugfix = 0;
        final StringBuilder urls = new StringBuilder();
        for (final Entry<String, String> entry : bugIDs.entrySet()) {
          if (message.contains("Merged revisions")) {
            continue;
          }
          final String bugId = entry.getKey();
          if (/* message.contains("CAMEL-")&&!(message.contains("/branches/")) */message
              .endsWith(bugId) || message.contains(bugId + " ") || message.contains(bugId + "\t")
              || message.contains(bugId + '\r') || message.contains(bugId + '\n')
              || message.contains(bugId + ":") || message.contains(bugId + ";")
              || message.contains(bugId + ".")) {
            bugfix++;
            final String url = entry.getValue();
            urls.append(url);
            urls.append(System.lineSeparator());
          }
        }

        statement3.setString(1, repo);
        statement3.setString(2, id);
        statement3.setString(3, date);
        statement3.setString(4, message);
        statement3.setString(5, author);
        statement3.setInt(6, bugfix);
        statement3.setString(7, urls.toString());
        statement3.executeUpdate();
      }
      statement2.close();
      statement3.close();

    } catch (SQLException | ClassNotFoundException e) {
      e.printStackTrace();
    }
  }

  private Map<String, String> getBugIDs() {
    final CPAConfig config = CPAConfig.getInstance();
    final Path bugFilePath = config.getBUG();
    Map<String, String> map = Collections.emptyMap();

    try {
      map = Files.readAllLines(bugFilePath)
          .stream()
          .collect(
              Collectors.toMap(BugFixRevisionExtractor::getID, BugFixRevisionExtractor::getURL));
    } catch (final IOException e) {
      e.printStackTrace();
      System.exit(0);
    }

    return map;
  }

  static private String getID(final String line) {
    return Splitter.on(',')
        .omitEmptyStrings()
        .trimResults()
        .splitToList(line)
        .get(0);
  }

  static private String getURL(final String line) {
    return Splitter.on(',')
        .omitEmptyStrings()
        .trimResults()
        .splitToList(line)
        .get(1);
  }
}
