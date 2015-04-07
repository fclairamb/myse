package com.webingenia.myse.access;

import java.io.InputStream;
import java.util.Date;
import java.util.List;

/**
 * Generic file access object.
 */
public abstract class File {

	/**
	 * If it is a directory.
	 *
	 * @return true for a directory
	 * @throws AccessException
	 */
	public abstract boolean isDirectory() throws AccessException;

	/**
	 * Get the last modified date.
	 *
	 * @return Date of the last modification
	 * @throws AccessException
	 */
	public abstract Date getLastModified() throws AccessException;

	/**
	 * List all the files of a directory. This can only be applied to a
	 * directory.
	 *
	 * @return A list of all the files in a directory.
	 * @throws AccessException
	 */
	public abstract List<File> listFiles() throws AccessException;

	/**
	 * Get the file size.
	 *
	 * @return File's size
	 * @throws AccessException
	 */
	public abstract long getSize() throws AccessException;

	/**
	 * Get the RELATIVE path of the file. The path must absolutely be relative
	 * to the source.
	 *
	 * @return Relative path of the file
	 */
	public abstract String getPath();

	/**
	 * Get the source of the file.
	 *
	 * @return Source of the file
	 */
	public abstract Source getSource();

	/**
	 * Get the input stream of the file
	 *
	 * @return InputStream of the file
	 * @throws com.webingenia.myse.access.AccessException
	 */
	public abstract InputStream getInputStream() throws AccessException;

	@Override
	public String toString() {
		return "File{" + getSource() + ":" + getPath() + "}";
	}

	public String getName() {
		String path = getPath();
		int p = path.lastIndexOf("/");
		return path.substring(p + 1);
	}
}
