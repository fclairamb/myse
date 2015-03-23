package com.webingenia.cifstest.access;

import java.util.Date;
import java.util.List;

public abstract class File {

	public abstract boolean isDirectory() throws AccessException;

	public abstract Date getLastModified() throws AccessException;

	public abstract List<File> listFiles() throws AccessException;

	public abstract String getPath();

	public abstract Source getSource();
}
