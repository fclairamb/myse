package com.webingenia.cifstest.access.smb;

import com.webingenia.cifstest.access.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

public class FileSMB extends File {

	private final SmbFile file;
	private final int cut;

	public FileSMB(SmbFile file, int cut) {
		this.file = file;
		this.cut = cut;
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
			list.add(new FileSMB(f, cut));
		}
		return list;
	}

	@Override
	public String getPath() {
		return file.getPath().substring(cut);
	}

}
