package com.webingenia.cifstest;

import static com.webingenia.cifstest.common.LOG.LOG;
import com.webingenia.cifstest.db.DBMgmt;
import com.webingenia.cifstest.common.SmbHelper;
import java.util.Date;
import jcifs.smb.SmbFile;

public class Main {

	public static void main(String[] args) throws Exception {
		DBMgmt.start();

		//NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication("WORKGROUP", "User", "aze");
		SmbFile file = new SmbFile("smb://User:aze@192.168.1.109/Desktop/");
		int cut = file.getPath().length();
		long time = System.currentTimeMillis();
		for (SmbFile f : SmbHelper.listAll(file)) {
			long m = f.lastModified();
			LOG.info("File - " + f.getPath().substring(cut) + " - " + (time - m) / 1000 + " - " + new Date(m));
		}
	}
}
