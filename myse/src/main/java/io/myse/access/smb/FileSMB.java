package io.myse.access.smb;

import io.myse.access.AccessException;
import io.myse.access.File;
import io.myse.access.Source;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

public class FileSMB extends File {

	private final SmbFile file;
	private final SourceSMB source;

	FileSMB(SmbFile file, SourceSMB source) {
		this.file = file;
		this.source = source;
	}

	private AccessException convertException(SmbException ex) {
		switch (ex.getNtStatus()) {
			case SmbException.ERROR_ACCESS_DENIED:
				return new AccessException(AccessException.AccessState.DENIED, ex);
			default:
				return new AccessException(AccessException.AccessState.UNKNOWN, ex);
		}
	}

	private AccessException convertException(IOException ex) {
		return new AccessException(AccessException.AccessState.ERROR, ex);
	}

	@Override
	public boolean isDirectory() throws AccessException {
		try {
			return file.isDirectory();
		} catch (SmbException ex) {
			throw convertException(ex);
		}
	}

	@Override
	public Date getLastModified() throws AccessException {
		try {
			return new Date(file.lastModified());
		} catch (SmbException ex) {
			throw convertException(ex);
		}
	}

	@Override
	public List<File> listFiles() throws AccessException {
		try {
			List<File> list = new ArrayList<>();

			for (SmbFile f : file.listFiles()) {
				list.add(new FileSMB(f, source));
			}
			return list;
		} catch (SmbException ex) {
			throw convertException(ex);
		}
	}

	public SmbFile getFile() {
		return file;
	}

	@Override
	public Source getSource() {
		return source;
	}

	@Override
	public String getPath() {
		return file.getPath().substring(source.getPathOffset());
	}

	@Override
	public InputStream getInputStream() throws AccessException {
		try {
			return file.getInputStream();
		} catch (IOException ex) {
			throw convertException(ex);
		}
	}

	@Override
	public long getSize() throws AccessException {
		try {
			return file.length();
		} catch (SmbException ex) {
			throw convertException(ex);
		}
	}

}
