package com.webingenia.cifstest.access.smb;

import com.webingenia.cifstest.access.File;
import com.webingenia.cifstest.access.Source;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

public class FileSMB extends File {

	private final SmbFile file;
	private final SourceSMB source;

	public FileSMB(SmbFile file, SourceSMB source) {
		this.file = file;
		this.source = source;
	}

	@Override
	public boolean isDirectory() throws SmbException {
		return file.isDirectory();
	}

	@Override
	public Date getModifiedDate() throws SmbException {
		return new Date(file.lastModified());
	}

	@Override
	public List<File> listFiles() throws SmbException {
		List<File> list = new ArrayList<>();
		for (SmbFile f : file.listFiles()) {
			list.add(new FileSMB(f, source));
		}
		return list;
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

}
