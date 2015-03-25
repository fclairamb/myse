package com.webingenia.myse;

import com.webingenia.myse.access.Source;
import com.webingenia.myse.access.disk.SourceDisk;
import com.webingenia.myse.access.smb.SourceSMB;
import com.webingenia.myse.db.DBMgmt;
import com.webingenia.myse.db.model.DBDescSource;
import com.webingenia.myse.embeddedes.EmbeddedElasticSearch;
import com.webingenia.myse.explore.DirExplorer;
import com.webingenia.myse.explore.FileIndexer;
import com.webingenia.myse.tasks.Tasks;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.persistence.EntityManager;

public class Main {

	public static void main(String[] args) throws Exception {
		DBMgmt.start();
		EmbeddedElasticSearch.start();

		EntityManager em = DBMgmt.getEntityManager();

		List<Source> allSources = Source.all(em);
		if (allSources.isEmpty()) {
			em.getTransaction().begin();
			{
				DBDescSource disk = new DBDescSource();
				disk.setName("Disk access");
				disk.setType(SourceDisk.TYPE);
				Map<String, String> props = disk.getProperties();
				props.put(SourceDisk.PROP_PATH, "/home/florent");
				em.persist(disk);
			}
			if (false) {
				DBDescSource smb = new DBDescSource();
				smb.setName("Sample SMB share");
				smb.setType(SourceSMB.TYPE);
				Map<String, String> props = smb.getProperties();
				props.put(SourceSMB.PROP_USER, "User");
				props.put(SourceSMB.PROP_PASS, "aze");
				props.put(SourceSMB.PROP_HOST, "192.168.1.109");
				props.put(SourceSMB.PROP_DIR, "c/Users");
				em.persist(smb);
			}
			em.getTransaction().commit();
			allSources = Source.all(em);
		}

		for (Source source : allSources) {
//			File rootDir = source.getRootDir();
//			em.getTransaction().begin();
//			for (File f : AccessHelper.listAll(rootDir)) {
//				DBDescFile df = DBDescFile.getOrCreate(f, em);
//				LOG.info("File " + f.getPath() + " / " + f.getModifiedDate());
//				df.setLastModification(f.getModifiedDate());
//				df.setDirectory(f.isDirectory());
//				em.persist(df);
//			}
//			em.getTransaction().commit();

			Tasks.getService().scheduleWithFixedDelay(new DirExplorer(source), 5, 60, TimeUnit.SECONDS);
			Tasks.getService().scheduleWithFixedDelay(new FileIndexer(source), 10, 10, TimeUnit.SECONDS);
		}
	}
}
