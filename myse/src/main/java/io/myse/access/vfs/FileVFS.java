package io.myse.access.vfs;

import io.myse.access.AccessException;
import io.myse.access.File;
import io.myse.access.Source;
import static io.myse.common.LOG.LOG;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;

public class FileVFS extends File {

	final FileObject file;
	private final SourceVFS source;

	FileVFS(FileObject file, SourceVFS source) {
		this.file = file;
		this.source = source;
	}

	@Override
	public boolean isDirectory() throws AccessException {
		try {
			return file.getType() == FileType.FOLDER;
		} catch (FileSystemException ex) {
			throw source.convertException(ex);
		}
	}

	@Override
	public Date getLastModified() throws AccessException {
		try {
			return new Date(file.getContent().getLastModifiedTime());
		} catch (FileSystemException ex) {
			throw source.convertException(ex);
		}
	}

	/**
	 * VFS file listing. It doesn't work for many implementations. See
	 * http://commons.apache.org/proper/commons-vfs/filesystems.html
	 *
	 * @return
	 * @throws AccessException
	 */
	@Override
	public List<File> listFiles() throws AccessException {
		try {
			List<File> list = new ArrayList<>();

			for (FileObject f : file.getChildren()) {
				list.add(new FileVFS(f, source));
			}
			return list;
		} catch (FileSystemException ex) {
			throw source.convertException(ex);
		}
	}

	@Override
	public String getPath() {
		try {
			return file.getURL().toString().substring(source.getPathOffset());
		} catch (FileSystemException ex) {
			LOG.error(this + ".getPath", ex);
			return null;
		}
	}

	@Override
	public Source getSource() {
		return source;
	}

	@Override
	public InputStream getInputStream() throws AccessException {
		try {
			return file.getContent().getInputStream();
		} catch (FileSystemException ex) {
			throw source.convertException(ex);
		}
	}

	@Override
	public long getSize() throws AccessException {
		try {
			return file.getContent().getSize();
		} catch (FileSystemException ex) {
			throw source.convertException(ex);
		}
	}

}
