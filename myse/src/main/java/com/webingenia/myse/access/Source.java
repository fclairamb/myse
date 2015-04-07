package com.webingenia.myse.access;

import com.webingenia.myse.access.disk.SourceDisk;
import com.webingenia.myse.access.ftps.SourceFTPS;
import com.webingenia.myse.access.smb.SourceSMB;
import com.webingenia.myse.access.vfs.SourceVFS;
import com.webingenia.myse.db.model.DBDescSource;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;

public abstract class Source {

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
			default:
				return null;
		}
	}

	public static List<Source> all(EntityManager em) {
		List<Source> list = new ArrayList<>();
		for (DBDescSource s : DBDescSource.all(em)) {
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
}
