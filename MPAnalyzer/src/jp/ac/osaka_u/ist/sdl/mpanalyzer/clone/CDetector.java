package jp.ac.osaka_u.ist.sdl.mpanalyzer.clone;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import jp.ac.osaka_u.ist.sdl.mpanalyzer.Config;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.StringUtility;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.TimingUtility;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.data.Statement;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.data.Token;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.db.CloneDAO;

import org.tmatesoft.svn.core.ISVNDirEntryHandler;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNDirEntry;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNLogClient;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNWCClient;

public class CDetector {

	private static final String REPOSITORY = Config.getPATH_TO_REPOSITORY();
	private static final String TARGET = Config.getTARGET();
	private static final String LANGUAGE = Config.getLanguage();
	private static final long REVISION = Config.getCloneDetectionRevision();
	private static final int THREADS = Config.getThreadsValue();

	private static final String DATABASELOCATION = Config.getDATABASELOCATION();
	private static final String DATABASENAME = Config.getDATABASENAME();

	public static void main(final String[] args) {

		final CDetector detector = new CDetector();

		final long startTime = System.nanoTime();

		System.out.print("identifying source files in revision ");
		System.out.print(Long.toString(REVISION));
		System.out.print(" ... ");
		final SortedSet<String> paths = detector.identifyFiles();
		System.out.print(Integer.toString(paths.size()));
		System.out.println(" files: done.");

		System.out.print("detecting clones ... ");
		final AtomicInteger index = new AtomicInteger();
		final String[] pathArray = paths.toArray(new String[0]);
		final ConcurrentMap<String, List<Statement>> contents = detector
				.getFileContent(paths);
		final ConcurrentMap<List<Integer>, Set<Clone>> clones = new ConcurrentHashMap<List<Integer>, Set<Clone>>();
		try {
			final CloneDetectionThread[] threads = new CloneDetectionThread[THREADS];
			for (int i = 0; i < threads.length; i++) {
				threads[i] = new CloneDetectionThread(index, pathArray,
						contents, clones);
				threads[i].start();
			}
			for (final CloneDetectionThread thread : threads) {
				thread.join();
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
		System.out.println(": done.");

		System.out.print("writing result to clone table ... ");
		try {
			final CloneDAO cloneDAO = new CloneDAO(true);
			for (final Set<Clone> cloneset : clones.values()) {
				cloneDAO.addClone(cloneset);
			}
			cloneDAO.close();
		} catch (final Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
		System.out.println(": done.");

		final long endTime = System.nanoTime();
		System.out.print("execution time: ");
		System.out.println(TimingUtility.getExecutionTime(startTime, endTime));
	}

	protected SortedSet<String> identifyFiles() {

		final SortedSet<String> files = new TreeSet<String>();

		try {

			final SVNURL url = SVNURL.fromFile(new File(REPOSITORY));
			FSRepositoryFactory.setup();
			final SVNLogClient logClient = SVNClientManager.newInstance()
					.getLogClient();

			logClient.doList(url, SVNRevision.create(REVISION),
					SVNRevision.create(REVISION), true, SVNDepth.INFINITY,
					SVNDirEntry.DIRENT_ALL, new ISVNDirEntryHandler() {

						@Override
						public void handleDirEntry(final SVNDirEntry entry)
								throws SVNException {

							if (entry.getKind() == SVNNodeKind.FILE) {
								final String path = entry.getRelativePath();

								if (StringUtility.isJavaFile(path)) {
									if (TARGET.isEmpty()
											|| path.startsWith(TARGET)) {
										files.add(path);
									}
								}
							}
						}
					});

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}

		return files;
	}

	protected ConcurrentMap<String, List<Statement>> getFileContent(
			final SortedSet<String> paths) {

		final ConcurrentMap<String, List<Statement>> contents = new ConcurrentHashMap<String, List<Statement>>();
		try {

			final SVNWCClient wcClient = SVNClientManager.newInstance()
					.getWCClient();

			for (final String path : paths) {

				final SVNURL fileurl = SVNURL.fromFile(new File(REPOSITORY
						+ System.getProperty("file.separator") + path));

				final StringBuilder text = new StringBuilder();
				wcClient.doGetFileContents(fileurl,
						SVNRevision.create(REVISION),
						SVNRevision.create(REVISION), false,
						new OutputStream() {
							@Override
							public void write(int b) throws IOException {
								text.append((char) b);
							}
						});

				final List<Token> tokens = Token.getTokens(text.toString());
				final List<Statement> statements = Statement
						.getStatements(tokens);
				contents.put(path, statements);
			}

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}

		return contents;
	}
}
