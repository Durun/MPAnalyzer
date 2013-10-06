package jp.ac.osaka_u.ist.sdl.mpanalyzer.db;

import java.sql.PreparedStatement;
import java.util.Collection;

import jp.ac.osaka_u.ist.sdl.mpanalyzer.data.Modification;

public class ModificationDAO extends DAO {

	final private PreparedStatement codefragmentPS;
	final private PreparedStatement modificationPS;
	private int numberOfCodefragmentPS;
	private int numberOfModificationPS;

	public ModificationDAO() throws Exception {
		super(false, true, true, false, false);

		this.codefragmentPS = this.connector
				.prepareStatement("insert into codefragment values (?, ?)");
		this.modificationPS = this.connector
				.prepareStatement("insert into modification (filepath, beforeText, beforeHash, afterText, afterHash, revision, type) values (?, ?, ?, ?, ?, ?, ?)");

		this.numberOfCodefragmentPS = 0;
		this.numberOfModificationPS = 0;
	}

	public void addModification(final Modification modification)
			throws Exception {

		this.codefragmentPS.setString(1, modification.before.text);
		this.codefragmentPS.setInt(2, modification.before.hash);
		this.codefragmentPS.addBatch();
		this.numberOfCodefragmentPS++;

		this.codefragmentPS.setString(1, modification.after.text);
		this.codefragmentPS.setInt(2, modification.after.hash);
		this.codefragmentPS.addBatch();
		this.numberOfCodefragmentPS++;

		this.modificationPS.setString(1, modification.filepath);
		this.modificationPS.setString(2, modification.before.text);
		this.modificationPS.setInt(3, modification.before.hash);
		this.modificationPS.setString(4, modification.after.text);
		this.modificationPS.setInt(5, modification.after.hash);
		this.modificationPS.setInt(6, (int) modification.revision.number);
		this.modificationPS.setInt(7, modification.changeType.getValue());
		this.modificationPS.addBatch();
		this.numberOfModificationPS++;

		if (20000 < this.numberOfCodefragmentPS) {
			System.out.println("writing \'codefragment\' table ...");
			this.codefragmentPS.executeBatch();
			this.numberOfCodefragmentPS = 0;
		}

		if (20000 < this.numberOfModificationPS) {
			System.out.println("writing \'modification\' table ...");
			this.modificationPS.executeBatch();
			this.numberOfModificationPS = 0;
		}
	}

	public void addModifications(Collection<Modification> modifications)
			throws Exception {
		for (final Modification modification : modifications) {
			this.addModification(modification);
		}
	}

	public void flush() throws Exception {
		if (0 < this.numberOfCodefragmentPS) {
			System.out.println("writing \'codefragment\' table ...");
			this.codefragmentPS.executeBatch();
			this.numberOfCodefragmentPS = 0;
		}

		if (0 < this.numberOfModificationPS) {
			System.out.println("writing \'modification\' table ...");
			this.modificationPS.executeBatch();
			this.numberOfModificationPS = 0;
		}
	}

	@Override
	public void close() throws Exception {
		this.codefragmentPS.close();
		this.modificationPS.close();
		super.close();
	}
}
