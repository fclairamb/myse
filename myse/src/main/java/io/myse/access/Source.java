package io.myse.access;

import io.myse.access.dbox.SourceDBox;
import io.myse.access.disk.SourceDisk;
import io.myse.access.drive.SourceDrive;
import io.myse.access.ftps.SourceFTPS;
import io.myse.access.smb.SourceSMB;
import io.myse.access.vfs.SourceVFS;
import io.myse.access.web.SourceWeb;
import io.myse.db.model.DBDescSource;
import io.myse.exploration.DirExplorer;
import io.myse.exploration.SourceExplorer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.persistence.EntityManager;
import org.elasticsearch.common.base.Strings;

/**
 * Source handling. This object
 */
public abstract class Source {

	public static class PropertyDescription {

		public PropertyDescription(String name, Type type, String description) {
			this(name, type, description, null);
		}

		public PropertyDescription(String name, Type type, String description, String defaultValue) {
			this(name, type, description, defaultValue, null);
		}

		public PropertyDescription(String name, Type type, String description, String defaultValue, String sampleValue) {
			this.name = name;
			this.type = type;
			this.description = description;
			this.defaultValue = defaultValue;
			this.sampleValue = sampleValue;
		}

		public enum Type {

			TEXT,
			PASSWORD,
			BOOLEAN
		}
		public final String name;
		public final Type type;
		public final String description;
		public final String defaultValue;
		public final String sampleValue;
	}

	protected final DBDescSource desc;

	public Source(DBDescSource desc) {
		this.desc = desc;
	}

	public final DBDescSource getDesc() {
		return desc;
	}

	public static Source get(DBDescSource source) {
		switch (source.getType()) {
			case SourceSMB.TYPE:
				return new SourceSMB(source);
			case SourceDisk.TYPE:
				return new SourceDisk(source);
			case SourceVFS.TYPE:
				return new SourceVFS(source);
			case SourceFTPS.TYPE_EXPLICIT:
			case SourceFTPS.TYPE_IMPLICIT:
				return new SourceFTPS(source);
			case SourceDrive.TYPE:
				return new SourceDrive(source);
			case SourceDBox.TYPE:
				return new SourceDBox(source);
			case SourceWeb.TYPE:
				return new SourceWeb(source);
			default:
				return null;
		}
	}

	public static Source getTemporary(String type) {
		DBDescSource temp = new DBDescSource();
		temp.setType(type);
		return get(temp);
	}

	public static List<Source> all(EntityManager em) {
		List<Source> list = new ArrayList<>();
		for (DBDescSource s : DBDescSource.allExisting(em)) {
			list.add(get(s));
		}
		return list;
	}

	public abstract File getRootDir() throws AccessException;

	public abstract File getFile(String path) throws AccessException;

	@Override
	public String toString() {
		DBDescSource d = getDesc();
		return "[" + d.getShortName() + "]";
	}

	protected List<PropertyDescription> getSharedProperties() {
		return Arrays.asList(
				new PropertyDescription(PROP_FILENAME_INCLUDE, PropertyDescription.Type.TEXT, "Files to include", "*.doc,*.docx,*.xls,*.xlsx,*.ppt,*.pptx,*.pdf,*.odt,*.ods,*.odp"),
				new PropertyDescription(PROP_FILENAME_EXCLUDE, PropertyDescription.Type.TEXT, "Files to exclude", ".*,~$*"),
				new PropertyDescription(PROP_INDEX, PropertyDescription.Type.BOOLEAN, "Do index", "true")
		);
	}

	public static String PROP_FILENAME_INCLUDE = "filename_include",
			PROP_FILENAME_EXCLUDE = "filename_exclude",
			PROP_INDEX = "index";

	public abstract List<PropertyDescription> getProperties();

	private void preSaveChangePath() {
		String path = desc.getProperties().get("path");
		if (!Strings.isNullOrEmpty(path) && !path.endsWith("/")) {
			path += "/";
			desc.getProperties().put("path", path);
		}
	}

	public void preSave() {
		preSaveChangePath();
	}

	/**
	 * Post-saving method. This method is called after the source has been
	 * saved. It allows to do processing and even redirect to the user to a new
	 * URL.
	 *
	 * @param context The source editing context
	 */
	public void postSave(SourceEditingContext context) {

	}

	/**
	 * Get an explorer for this source.
	 *
	 * The DirExplorer is the standard explorer. Some sources might want to
	 * override this method to offer an explorer more adapted to them.
	 *
	 * @return Explorer working on this source
	 */
	public SourceExplorer getExplorer() {
		return new DirExplorer(getDesc().getId());
	}
	
	/**
	 * If an indexer task should be launch. This only returns true if an indexer
	 * task should be launched.
	 * @return If an indexer task should be launched
	 */
	public boolean launchAnalyser() {
		return true;
	}
}
