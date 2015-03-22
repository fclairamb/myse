package com.webingenia.cifstest;

import static com.webingenia.cifstest.common.LOG.LOG;
import com.webingenia.cifstest.db.DBMgmt;
import java.util.Date;
import jcifs.smb.SmbFile;

public class Main {

	public static void main(String[] args) throws Exception {
		DBMgmt.start();

		//NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication("WORKGROUP", "User", "aze");
		SmbFile file = new SmbFile("smb://User:aze@192.168.1.109/Desktop/");
		long time = System.currentTimeMillis();
		for (SmbFile f : file.listFiles()) {
			long m = f.lastModified();
			LOG.info("File - " + f + " - " + (time - m) / 1000 + " - " + new Date(m));
		}
	}
}
