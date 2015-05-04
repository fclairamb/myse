package io.myse.access.dbox;

import com.dropbox.core.DbxEntry;
import com.dropbox.core.DbxException;
import io.myse.access.AccessException;
import io.myse.access.File;
import io.myse.access.Source;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FileDBox extends File {

	private final SourceDBox source;
	private String path;
	private DbxEntry entry;

	FileDBox(String path, SourceDBox source) {
		this.path = path;
		this.source = source;
	}

	FileDBox(DbxEntry entry, SourceDBox source) {
		this(entry.isFile() ? entry.asFile().path : entry.asFolder().path, source);
		this.entry = entry;
	}

	private DbxEntry getEntry() throws AccessException {
		if (entry == null) {
			try {
				entry = source.getClient().getMetadata(path);
			} catch (DbxException ex) {
				throw new AccessException(AccessException.AccessState.ERROR, ex);
			}
		}
		return entry;
	}

	@Override
	public boolean isDirectory() throws AccessException {
		return getEntry().isFolder();
	}

	@Override
	public Date getLastModified() throws AccessException {
		if (getEntry().isFile()) {
			return getEntry().asFile().lastModified;
		} else {
			return new Date();
		}
	}

	@Override
	public List<File> listFiles() throws AccessException {
		try {
			List<File> list = new ArrayList<>();
			DbxEntry.WithChildren metadataWithChildren = source.getClient().getMetadataWithChildren(path);
			for (DbxEntry e : metadataWithChildren.children) {
				list.add(new FileDBox(e, source));
			}
			return list;
		} catch (DbxException ex) {
			throw new AccessException(AccessException.AccessState.ERROR, ex);
		}
	}

	@Override
	public long getSize() throws AccessException {
		return getEntry().asFile().numBytes;
	}

	@Override
	public String getPath() {
		return path;
	}

	@Override
	public Source getSource() {
		return source;
	}

	@Override
	public InputStream getInputStream() throws AccessException {
		try {
			java.io.File tmpFile = java.io.File.createTempFile("myse", "dbox");
			tmpFile.deleteOnExit();
			try (FileOutputStream fos = new FileOutputStream(tmpFile)) {
				source.getClient().getFile(path, null, fos);
			}
			return new FileInputStream(tmpFile);
		} catch (IOException | DbxException ex) {
			throw new AccessException(AccessException.AccessState.ERROR, ex);
		}
	}

	@Override
	public String toString() {
		return String.format("FileDBox{%s}", path);
	}
}
