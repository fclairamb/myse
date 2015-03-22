package com.webingenia.cifstest;

import com.webingenia.cifstest.access.File;
import com.webingenia.cifstest.access.Source;
import com.webingenia.cifstest.access.smb.SourceSMB;
import com.webingenia.cifstest.common.AccessHelper;
import static com.webingenia.cifstest.common.LOG.LOG;
import com.webingenia.cifstest.db.DBMgmt;
import com.webingenia.cifstest.db.model.DBDescFile;
import com.webingenia.cifstest.db.model.DBDescSource;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;

public class Main {

	public static void main(String[] args) throws Exception {
		DBMgmt.start();

		EntityManager em = DBMgmt.getEntityManager();

		List<Source> allSources = Source.all(em);
		if (allSources.isEmpty()) {
			em.getTransaction().begin();
			DBDescSource smb = new DBDescSource();
			smb.setName("Sample SMB share");
			smb.setType(SourceSMB.TYPE);
			Map<String, String> props = smb.getProperties();
			props.put(SourceSMB.PROP_USER, "User");
			props.put(SourceSMB.PROP_PASS, "aze");
			props.put(SourceSMB.PROP_HOST, "192.168.1.109");
			props.put(SourceSMB.PROP_DIR, "Desktop");
			em.persist(smb);
			em.getTransaction().commit();
			allSources = Source.all(em);
		}

		for (Source source : allSources) {
			File rootDir = source.getRootDir();
			em.getTransaction().begin();
			for (File f : AccessHelper.listAll(rootDir)) {
				DBDescFile df = DBDescFile.getOrCreate(source.getDesc(), f.getPath(), em);
				LOG.info("File " + f.getPath() + " / " + f.getModifiedDate());
				df.setLastModification(f.getModifiedDate());
				df.setDirectory(f.isDirectory());
				em.persist(df);
			}
			em.getTransaction().commit();
		}
	}
}
