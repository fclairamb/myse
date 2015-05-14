package io.myse.access.dbox;

import com.dropbox.core.DbxEntry;
import com.dropbox.core.DbxException;
import io.myse.access.AccessException;
import io.myse.access.File;
import io.myse.access.Link;
import io.myse.access.LinkContext;
import io.myse.access.Source;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileDBox extends File {

	private final SourceDBox source;
	private String path;
	private DbxEntry entry;
	private boolean notFound;

	FileDBox(String path, SourceDBox source) {
		// Dropbox paths must be lowercase because they are set
		// to lowercase for deletion.
		this.path = path.toLowerCase();
		this.source = source;
	}

	FileDBox(DbxEntry entry, SourceDBox source) {
		this(entry.isFile() ? entry.asFile().path : entry.asFolder().path, source);
		this.entry = entry;
	}

	private DbxEntry getEntry() throws AccessException {
		if (entry == null && !notFound) {
			try {
				entry = source.getClient().getMetadata(path);
				if (entry == null) {
					notFound = true;
				}
			} catch (DbxException ex) {
				throw new AccessException(AccessException.AccessState.ERROR, ex);
			}
		}
		return entry;
	}

	@Override
	public boolean exists() throws AccessException {
		return getEntry() != null;
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

	//private static final String URL_PREVIEW = "https://www.dropbox.com/home%s?preview=%s";
	private String shareableLink;

	@Override
	public Link getLink(LinkContext context) throws AccessException {
		if (shareableLink == null) {
			try {
				shareableLink = source.getClient().createShareableUrl(path);
			} catch (DbxException ex) {
				throw new AccessException(AccessException.AccessState.ERROR, ex);
			}
		}
		return new Link(shareableLink);
	}

	@Override
	public String toString() {
		return String.format("FileDBox{%s}", path);
	}
}
