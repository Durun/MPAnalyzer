package yoshikihigo.cpanalyzer;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.wc.ISVNDiffStatusHandler;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNDiffClient;
import org.tmatesoft.svn.core.wc.SVNDiffStatus;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNStatusType;
import org.tmatesoft.svn.core.wc.SVNWCClient;
import yoshikihigo.cpanalyzer.data.Change;
import yoshikihigo.cpanalyzer.data.Revision;
import yoshikihigo.cpanalyzer.data.Statement;
import yoshikihigo.cpanalyzer.db.ChangeDAO;

public class SVNChangeExtractionThread extends Thread {

  final private static Object LOCK = new Object();

  final CPAConfig config;
  final public Revision beforeRevision;
  final public Revision afterRevision;

  public SVNChangeExtractionThread(final CPAConfig config, final Revision beforeRevision,
      final Revision afterRevision) {
    this.config = config;
    this.beforeRevision = beforeRevision;
    this.afterRevision = afterRevision;
  }

  @Override
  public void run() {

    final long id = Thread.currentThread()
        .getId();
    final String repository = config.getSVNREPOSITORY_FOR_MINING();
    final Set<LANGUAGE> languages = config.getLANGUAGE();
    final String repoPath = repository;
    final boolean onlyCondition = config.isONLY_CONDITION();
    final boolean ignoreImport = config.isIGNORE_IMPORT();
    final boolean isVerbose = config.isVERBOSE();

    SVNURL url;
    SVNDiffClient diffClient;
    SVNWCClient wcClient;
    try {
      synchronized (LOCK) {
        FSRepositoryFactory.setup();
        url = SVNURL.fromFile(new File(repository));
        diffClient = SVNClientManager.newInstance()
            .getDiffClient();
        wcClient = SVNClientManager.newInstance()
            .getWCClient();
      }
    } catch (final SVNException e) {
      e.printStackTrace();
      return;
    }

    final String author = this.afterRevision.author;

    if (isVerbose) {
      final StringBuilder progress = new StringBuilder();
      progress.append(id);
      progress.append(": checking revisions ");
      progress.append(beforeRevision.id);
      progress.append(" and ");
      progress.append(this.afterRevision.id);
      System.out.println(progress.toString());
    }

    final List<String> changedPaths = new ArrayList<>();
    try {
      synchronized (LOCK) {
        final long beforeRevNumber = Long.valueOf(this.beforeRevision.id);
        final long afterRevNumber = Long.valueOf(this.afterRevision.id);
        diffClient.doDiffStatus(url, SVNRevision.create(beforeRevNumber), url,
            SVNRevision.create(afterRevNumber), SVNDepth.INFINITY, true,
            new ISVNDiffStatusHandler() {

              @Override
              public void handleDiffStatus(final SVNDiffStatus diffStatus) {

                final String path = diffStatus.getPath();
                final SVNStatusType type = diffStatus.getModificationType();

                if (!type.equals(SVNStatusType.STATUS_MODIFIED)) {
                  return;
                }

                if (languages.stream()
                    .anyMatch(lang -> lang.isTarget(path))) {
                  changedPaths.add(path);
                }
              }
            });
      }
    } catch (final SVNException | NullPointerException e) {
      e.printStackTrace();
      return;
    }

    final LCS lcs = new LCS(config, repoPath, this.afterRevision);

    FILE: for (final String path : changedPaths) {

      if (isVerbose) {
        final StringBuilder progress = new StringBuilder();
        progress.append(" ");
        progress.append(id);
        progress.append(": extracting changes from ");
        progress.append(path);
        System.out.println(progress.toString());
      }

      SVNURL fileurl;
      try {
        synchronized (LOCK) {
          fileurl =
              SVNURL.fromFile(new File(repository + System.getProperty("file.separator") + path));
        }
      } catch (final SVNException e) {
        e.printStackTrace();
        continue FILE;
      }

      final StringBuilder beforeText = new StringBuilder();
      try {
        synchronized (LOCK) {
          final long beforeRevNumber = Long.valueOf(this.beforeRevision.id);
          wcClient.doGetFileContents(fileurl, SVNRevision.create(beforeRevNumber),
              SVNRevision.create(beforeRevNumber), false, new OutputStream() {

                @Override
                public void write(int b) throws IOException {
                  beforeText.append((char) b);
                }
              });
        }
      } catch (final SVNException | NullPointerException e) {
        e.printStackTrace();
        continue FILE;
      }

      final StringBuilder afterText = new StringBuilder();
      try {
        synchronized (LOCK) {
          final long afterRevNumber = Long.valueOf(this.afterRevision.id);
          wcClient.doGetFileContents(fileurl, SVNRevision.create(afterRevNumber),
              SVNRevision.create(afterRevNumber), false, new OutputStream() {

                @Override
                public void write(int b) throws IOException {
                  afterText.append((char) b);
                }
              });
        }
      } catch (final SVNException | NullPointerException e) {
        e.printStackTrace();
        continue FILE;
      }

      final LANGUAGE language = FileUtility.getLANGUAGE(path);
      final StringUtility stringUtil = new StringUtility(config);
      final List<Statement> beforeStatements =
          stringUtil.splitToStatements(beforeText.toString(), language);
      final List<Statement> afterStatements =
          stringUtil.splitToStatements(afterText.toString(), language);

      final List<Change> changes = lcs.getChanges(beforeStatements, afterStatements, path);

      if (onlyCondition) {
        ChangeDAO.SINGLETON.addChanges(changes.stream()
            .filter(change -> change.isCondition())
            .collect(Collectors.toList()));
      }

      else if (ignoreImport) {
        ChangeDAO.SINGLETON.addChanges(changes.stream()
            .filter(change -> !change.isImport())
            .collect(Collectors.toList()));
      }

      else {
        ChangeDAO.SINGLETON.addChanges(changes);
      }
    }
  }
}
