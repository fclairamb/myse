package com.webingenia.cifstest.access.disk;

import com.webingenia.cifstest.access.AccessException;
import com.webingenia.cifstest.access.File;
import com.webingenia.cifstest.access.Source;
import com.webingenia.cifstest.db.model.DBDescSource;

public class SourceDisk extends Source {

	public static final String PROP_PATH = "path";

	public static final String TYPE = "disk";

	public SourceDisk(DBDescSource desc) {
		super(desc);
	}

	private String getRootPath() {
		return desc.getProperties().get(PROP_PATH);
	}

	@Override
	public FileDisk getRootDir() {
		return new FileDisk(new java.io.File(getRootPath()), this);
	}

	@Override
	public FileDisk getFile(String path) throws AccessException {
		return new FileDisk(new java.io.File(getRootPath(), path), this);
	}

	private int pathOffset;

	int getPathOffset() {
		if (pathOffset == 0) {
			pathOffset = getRootDir().file.getAbsolutePath().length();
		}
		return pathOffset;
	}
}
