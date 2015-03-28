package com.webingenia.myse;

import com.webingenia.myse.access.File;
import com.webingenia.myse.access.Source;
import static com.webingenia.myse.common.LOG.LOG;
import com.webingenia.myse.db.DBMgmt;
import com.webingenia.myse.db.model.DBDescSource;
import com.webingenia.myse.embeddedes.EmbeddedElasticSearch;
import com.webingenia.myse.explore.DirExplorer;
import com.webingenia.myse.explore.FileIndexer;
import com.webingenia.myse.tasks.Tasks;
import com.webingenia.myse.webserver.JettyServer;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import javax.persistence.EntityManager;

public class Main {

	public static void main(String[] args) throws Exception {
		DBMgmt.start();
		EmbeddedElasticSearch.start();
		JettyServer.start();

		EntityManager em = DBMgmt.getEntityManager();

		List<Source> allSources = Source.all(em);
		if (allSources.isEmpty()) {
			em.getTransaction().begin();

			// We will create a new source
			DBDescSource smb = new DBDescSource();
			Map<String, String> props = smb.getProperties();

			// All this source's data will come from the private directory
			try (InputStream is = new FileInputStream("private/nas.properties")) {
				Properties fileprops = new Properties();
				fileprops.load(is);
				smb.setName(fileprops.getProperty("_name"));
				smb.setType(fileprops.getProperty("_type"));
				for (Map.Entry<Object, Object> me : fileprops.entrySet()) {
					props.put((String) me.getKey(), (String) me.getValue());
				}
			}

			em.persist(smb);
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
			File rootDir = source.getRootDir();
			LOG.info("files.size: " + rootDir.listFiles().size());
			Tasks.getService().scheduleWithFixedDelay(new DirExplorer(source), 0, 10, TimeUnit.SECONDS);
			Tasks.getService().scheduleWithFixedDelay(new FileIndexer(source), 30, 10, TimeUnit.SECONDS);
		}
	}
}
