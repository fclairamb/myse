package com.webingenia.cifstest.db;

import java.util.ArrayList;
import java.util.List;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

public class SmbHelper {

	public static List<SmbFile> listAll(SmbFile src) throws SmbException {
		List<SmbFile> list = new ArrayList<>();
		for (SmbFile file : src.listFiles()) {
			list.add(file);
			if (file.isDirectory()) {
				list.addAll(listAll(file));
			}
		}
		return list;
	}
}
