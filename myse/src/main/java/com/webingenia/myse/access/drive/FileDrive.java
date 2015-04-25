package com.webingenia.myse.access.drive;

import com.google.api.services.drive.model.ChildList;
import com.google.api.services.drive.model.ChildReference;
import com.webingenia.myse.access.AccessException;
import com.webingenia.myse.access.File;
import com.webingenia.myse.access.Source;
import com.webingenia.myse.common.LOG;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FileDrive extends File {

	private final String fileId;
	private final boolean directory;
	private final SourceDrive source;

	public FileDrive(String path, SourceDrive source) {
		this.directory = path.startsWith("D:");
		this.fileId = path.substring(2);
		this.source = source;
	}

	@Override
	public boolean isDirectory() {
		return directory;
	}

	@Override
	public Date getLastModified() throws AccessException {
		if (isDirectory()) {
			// We will have to find a better way to do this
			return new Date();
		}
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public List<File> listFiles() throws AccessException {
		ArrayList<File> files = new ArrayList<>();
		try {
			ChildList list = source.getDrive().children().list(fileId).execute();
			for (ChildReference f : list.getItems()) {
				files.add(new FileDrive(f.getId() + "/", source));
			}
		} catch (IOException ex) {
			throw new AccessException(AccessException.AccessState.ERROR, ex);
		}
		return files;
	}

	private com.google.api.services.drive.model.File getFile() throws IOException {
		return source.getDrive().files().get(fileId).execute();
	}

	@Override
	public long getSize() throws AccessException {
		try {
			com.google.api.services.drive.model.File file = source.getDrive().files().get(fileId).execute();
			return file.getFileSize();
		} catch (IOException ex) {
			throw new AccessException(AccessException.AccessState.ERROR, ex);
		}
	}

	@Override
	public String getPath() {
		return (isDirectory() ? "D:" : "F:") + fileId;
	}

	@Override
	public Source getSource() {
		return source;
	}

	@Override
	public InputStream getInputStream() throws AccessException {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public String getName() {
		try {
			return getFile().getOriginalFilename();
		} catch (IOException ex) {
			LOG.LOG.error("Could not get name", ex);
		}
		return null;
	}

}
