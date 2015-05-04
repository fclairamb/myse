package io.myse.access.disk;

import io.myse.access.AccessException;
import io.myse.access.File;
import io.myse.access.Source;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FileDisk extends File {

	final java.io.File file;
	private final SourceDisk source;

	FileDisk(java.io.File file, SourceDisk source) {
		this.file = file;
		this.source = source;
	}

	@Override
	public boolean isDirectory() throws AccessException {
		return file.isDirectory();
	}

	@Override
	public Date getLastModified() throws AccessException {
		return new Date(file.lastModified());
	}

	@Override
	public List<File> listFiles() throws AccessException {
		ArrayList<File> files = new ArrayList<>();
		for (java.io.File f : file.listFiles()) {
			files.add(new FileDisk(f, source));
		}
		return files;
	}

	@Override
	public String getPath() {
		return file.getAbsolutePath().substring(source.getPathOffset());
	}

	@Override
	public Source getSource() {
		return source;
	}

	@Override
	public InputStream getInputStream() throws AccessException {
		try {
			return new FileInputStream(file);
		} catch (FileNotFoundException ex) {
			throw new AccessException(AccessException.AccessState.ERROR, ex);
		}
	}

	@Override
	public long getSize() throws AccessException {
		return file.length();
	}

}
