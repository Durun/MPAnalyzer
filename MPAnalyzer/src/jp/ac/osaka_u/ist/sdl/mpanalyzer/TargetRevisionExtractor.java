package jp.ac.osaka_u.ist.sdl.mpanalyzer;

import java.io.File;
import java.io.IOException;

import jp.ac.osaka_u.ist.sdl.mpanalyzer.data.Revision;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.db.RevisionDAO;

import org.tmatesoft.svn.core.ISVNLogEntryHandler;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.io.SVNRepository;

public class TargetRevisionExtractor {

	public static void main(String[] args) throws SVNException, IOException {

		try {

			final long startTime = System.nanoTime();

			final String PATH_TO_REPOSITORY = Config.getPATH_TO_REPOSITORY();
			final String TARGET = "/" + Config.getTARGET();
			final String LANGUAGE = Config.getLanguage();

			final SVNURL url = SVNURL.fromFile(new File(PATH_TO_REPOSITORY));
			FSRepositoryFactory.setup();
			final SVNRepository repository = FSRepositoryFactory.create(url);

			final long startRevision = Config.getStartRevision();
			final long endRevision = Config.getEndtRevision();

			final RevisionDAO dao = new RevisionDAO();

			repository.log(null, startRevision, endRevision, true, true,
					new ISVNLogEntryHandler() {
						public void handleLogEntry(SVNLogEntry logEntry)
								throws SVNException {
							for (final Object key : logEntry.getChangedPaths()
									.keySet()) {
								final String path = (String) key;
								final int number = (int) logEntry.getRevision();
								final String date = StringUtility
										.getDateString(logEntry.getDate());
								final String message = logEntry.getMessage();
								final Revision revision = new Revision(number,
										date, message);
								if (LANGUAGE.equalsIgnoreCase("JAVA")
										&& path.startsWith(TARGET)
										&& StringUtility.isJavaFile(path)) {
									System.out.print(Integer.toString(number));
									System.out.println(" is beging checked.");
									dao.addRevision(revision);
									break;
								} else if (LANGUAGE.equalsIgnoreCase("C")
										&& path.startsWith(TARGET)
										&& StringUtility.isCFile(path)) {
									System.out.print(Integer.toString(number));
									System.out.println(" is being checked.");
									dao.addRevision(revision);
									break;
								}
							}
						}
					});

			dao.close();

			final long endTime = System.nanoTime();
			System.out.print("execution time: ");
			System.out.println(TimingUtility.getExecutionTime(startTime,
					endTime));

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}
}
