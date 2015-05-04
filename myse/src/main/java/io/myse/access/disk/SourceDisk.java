package io.myse.access.disk;

import io.myse.access.AccessException;
import io.myse.access.Source;
import io.myse.db.model.DBDescSource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SourceDisk extends Source {

	public static final String PROP_PATH = "path";

	public static final String TYPE = "disk";

	public SourceDisk(DBDescSource desc) {
		super(desc);
	}

	// DONE: Make sure the root path is finishing with a "/": Done using the
	// Source class preSave method.
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

	@Override
	public List<PropertyDescription> getProperties() {
		ArrayList<PropertyDescription> list = new ArrayList<>();
		list.addAll(Arrays.asList(
				new PropertyDescription(PROP_PATH, PropertyDescription.Type.TEXT, "Path of the directory to index")
		));
		list.addAll(getSharedProperties());
		return list;
	}
}
