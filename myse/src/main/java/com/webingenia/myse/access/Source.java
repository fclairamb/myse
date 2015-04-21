package com.webingenia.myse.access;

import com.webingenia.myse.access.disk.SourceDisk;
import com.webingenia.myse.access.drive.SourceDrive;
import com.webingenia.myse.access.ftps.SourceFTPS;
import com.webingenia.myse.access.smb.SourceSMB;
import com.webingenia.myse.access.vfs.SourceVFS;
import com.webingenia.myse.db.model.DBDescSource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.persistence.EntityManager;
import org.elasticsearch.common.base.Strings;

public abstract class Source {

	public static class PropertyDescription {

		public PropertyDescription(String name, Type type, String description) {
			this(name, type, description, null);
		}

		public PropertyDescription(String name, Type type, String description, String defaultValue) {
			this.name = name;
			this.type = type;
			this.description = description;
			this.defaultValue = defaultValue;
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
				new PropertyDescription(PROP_FILENAME_EXCLUDE, PropertyDescription.Type.TEXT, "Files to exclude", "*")
		);
	}

	public static String PROP_FILENAME_INCLUDE = "filename_include",
			PROP_FILENAME_EXCLUDE = "filename_exclude";

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

	public void postSave() {

	}
}
