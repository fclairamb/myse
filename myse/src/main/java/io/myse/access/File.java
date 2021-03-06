package io.myse.access;

import java.io.InputStream;
import java.util.Date;
import java.util.List;

/**
 * Generic file access object.
 */
public abstract class File {

	/**
	 * If the file exists.
	 *
	 * @return ture if it exists
	 * @throws AccessException
	 */
	public abstract boolean exists() throws AccessException;

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
	 * Get an external link.
	 * @param context Context
	 * @return Return a link
	 * @throws io.myse.access.AccessException
	 */
	public abstract Link getLink(LinkContext context) throws AccessException;

	/**
	 * Get the input stream of the file
	 *
	 * @return InputStream of the file
	 * @throws io.myse.access.AccessException
	 */
	public abstract InputStream getInputStream() throws AccessException;

	@Override
	public String toString() {
		return String.format("File{%s (%s)}", getPath(), getSource());
	}

	/**
	 * Get the name of the file. It is not necessarly the end of the path. Some
	 * source implementations can have ID in the path, this function must still
	 * return the name of the original file.
	 *
	 * @return Name of the file
	 * @throws AccessException
	 */
	public String getName() throws AccessException {
		String path = getPath();
		int p = path.lastIndexOf("/");
		return path.substring(p + 1);
	}
}
