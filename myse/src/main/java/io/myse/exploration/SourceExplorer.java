package io.myse.exploration;

import io.myse.access.AccessException;
import io.myse.access.File;
import io.myse.access.Source;
import io.myse.common.EventsNotifier;
import static io.myse.common.LOG.LOG;
import io.myse.common.ReschedulingRunnable;
import io.myse.db.DBMgmt;
import io.myse.db.model.DBDescFile;
import io.myse.db.model.DBDescSource;
import static io.myse.exploration.DirExplorer.compileWildcardRule;
import java.util.Map;
import java.util.regex.Pattern;
import javax.persistence.EntityManager;
import org.elasticsearch.common.base.Strings;

public abstract class SourceExplorer extends ReschedulingRunnable {

	private final long sourceId;

	public SourceExplorer(long sourceId) {
		this.sourceId = sourceId;
	}

	protected Pattern patternInclude, patternExclude;

	protected DBDescSource dbSource;

	protected Source source;

	protected void init(EntityManager em) {
		dbSource = DBDescSource.get(sourceId, em);
		if (dbSource == null) {
			cancel();
			return;
		}
		source = Source.get(dbSource);
	}

	public static Pattern compileWildcardRule(String wildCard) {
		StringBuilder sb = new StringBuilder();

		int count = 0;
		for (String s : wildCard.split(",")) {
			s = s.trim(); // We remove space
			if (count++ > 0) {
				sb.append("|");
			}
			sb.append(s.replace(".", "\\.").replace("*", ".*"));
		}

		return Pattern.compile(sb.toString(), Pattern.CASE_INSENSITIVE);
	}

	protected void fetchSettings() {
		Map<String, String> properties = source.getDesc().getProperties();
		String strPatternInclude = properties.get(Source.PROP_FILENAME_INCLUDE);
		String strPatternExclude = properties.get(Source.PROP_FILENAME_EXCLUDE);
		patternInclude = Strings.isNullOrEmpty(strPatternInclude) ? null : compileWildcardRule(strPatternInclude);
		patternExclude = Strings.isNullOrEmpty(strPatternExclude) ? null : compileWildcardRule(strPatternExclude);
	}

	protected abstract void explorerRun(EntityManager em) throws Exception;

	protected boolean mustSkip(File file) throws AccessException {
		// File name checking doesn't apply to directories
		if (!file.isDirectory()) {
			String name = file.getName();
			boolean index = true;
			// We don't care about hidden files and lock files by default
			if (name.startsWith("~$") || name.startsWith(".")) {
				index = false;
			}
			if (index && patternExclude != null) {
				index = !patternExclude.matcher(name).matches();
			}
			if (!index && patternInclude != null) {
				index = patternInclude.matcher(name).matches();
			}
			if (!index) {
				//LOG.info("This filename was excluded: " + name);
				return true;
			}
		}
		return false;
	}

	protected boolean indexFile(DBDescFile desc, File file, EntityManager em) throws AccessException {
		boolean changed;
		if (!file.exists()) {
			changed = desc.setDeleted(true);
		} else {

			if (mustSkip(file)) {
				return false;
			}

			changed = desc.setLastModified(file.getLastModified());
			desc.setDirectory(file.isDirectory());
			if (!file.isDirectory()) {
				desc.setSize(file.getSize());
			}
		}

		desc.updateNextAnalysis();

		if (changed) {
			if (file.exists()) {
				EventsNotifier.eventPreparingIndexation(file);
			} else {
				EventsNotifier.eventPreparingDeletion(file);
			}
		}

		return changed;
	}

	protected boolean indexFile(File file, EntityManager em) throws AccessException {
		return indexFile(DBDescFile.getOrCreate(file, em), file, em);
	}

	protected DBDescFile getDbFile(File file, EntityManager em) throws AccessException {
		return DBDescFile.getOrCreate(file, em);
	}

	protected File getSourceFile(DBDescFile file) throws AccessException {
		return source.getFile(file.getPath());
	}

	@Override
	public void actualRun() {
		EntityManager em = DBMgmt.getEntityManager();
		em.getTransaction().begin();
		try {
			init(em);
			fetchSettings();
			LOG.info("{}: Starting exploration...", this);
			explorerRun(em);
			LOG.info("{}: Exploration ended... / delay = {}", this, delay);
		} catch (Exception ex) {
			LOG.error("{}: Issues running explorerRun", ex);
		} finally {
			em.getTransaction().commit();
			em.close();
		}
	}
}
