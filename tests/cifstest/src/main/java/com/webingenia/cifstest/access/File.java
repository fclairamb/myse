package com.webingenia.cifstest.access;

import java.util.Date;
import java.util.List;

public abstract class File {

	public abstract boolean isDirectory() throws Exception;

	public abstract Date getModifiedDate() throws Exception;

	public abstract List<File> listFiles() throws Exception;

	public abstract String getPath();

	public abstract Source getSource();
}
