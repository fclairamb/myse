package com.webingenia.cifstest.access;

import com.webingenia.cifstest.access.smb.SourceSMB;
import com.webingenia.cifstest.db.model.DBDescSource;
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

	public abstract File getRootDir();
}
