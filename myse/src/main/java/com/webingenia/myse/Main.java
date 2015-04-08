package com.webingenia.myse;

import com.webingenia.myse.access.Source;
import com.webingenia.myse.access.disk.SourceDisk;
import com.webingenia.myse.common.BuildInfo;
import static com.webingenia.myse.common.LOG.LOG;
import com.webingenia.myse.db.DBMgmt;
import com.webingenia.myse.db.model.Config;
import com.webingenia.myse.db.model.DBDescSource;
import com.webingenia.myse.embeddedes.ElasticSearch;
import com.webingenia.myse.webserver.JettyServer;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.persistence.EntityManager;

public class Main {

	public static void main(String[] args) throws Exception {
		DBMgmt.start();
		ElasticSearch.start();
		JettyServer.start();

		EntityManager em = DBMgmt.getEntityManager();

		List<DBDescSource> allSources = DBDescSource.all(em);
		if (allSources.isEmpty()) {
			LOG.warn("NO SOURCE! Creating one !");
			em.getTransaction().begin();

			// We will create a new source
			if (new File("private/nas.properties").exists()) {
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
			}
			{
				String home = System.getenv("user.home");
				if (home == null) { // Java 8
					home = System.getProperty("user.home");
				}
				if (home != null) {
					String[] paths = {"My Documents", "Documents", "Mes Documents"};
					for (String path : paths) {
						File file = new File(new File(home), path);
						if (file.exists()) {
							LOG.warn("Adding your documents dir !");
							DBDescSource docs = new DBDescSource();
							docs.setName("Local documents");
							docs.setType(SourceDisk.TYPE);
							docs.getProperties().put("path", file.getAbsolutePath());
							em.persist(docs);
							break;
						}
					}
				} else {
					LOG.warn("Could not find $HOME env var.");
				}
			}
			em.getTransaction().commit();
			allSources = DBDescSource.all(em);
		}

		{
			File sampleDocsDir = new File("sample_docs");
			if (sampleDocsDir.exists() || DBDescSource.get("Sample_docs", em) == null) {
				DBDescSource sampleDocs = new DBDescSource();
				sampleDocs.setName("Sample docs");
				sampleDocs.setType(SourceDisk.TYPE);
				sampleDocs.getProperties().put("path", sampleDocsDir.getAbsolutePath());
				em.persist(sampleDocs);
			}
		}

		{ // We check the version
			String currentVersion = BuildInfo.getBuildInfo();
			String version = Config.get("version", currentVersion);
			if (!currentVersion.equals(version)) {
				LOG.info("VERSION Change: {} --> {}", version, currentVersion);
				Config.set("version", currentVersion);
			} else {
				LOG.info("Version: " + currentVersion);
			}
		}

		for (DBDescSource dbSource : allSources) {
			Indexation.start(Source.get(dbSource));
		}
	}
}
