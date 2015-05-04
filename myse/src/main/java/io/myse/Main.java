package io.myse;

import io.myse.common.Indexation;
import io.myse.desktop.Browser;
import io.myse.desktop.TrayIconMgmt;
import io.myse.access.Source;
import io.myse.access.disk.SourceDisk;
import io.myse.access.vfs.SourceVFS;
import io.myse.common.BuildInfo;
import static io.myse.common.LOG.LOG;
import io.myse.db.DBMgmt;
import io.myse.db.model.Config;
import io.myse.db.model.DBDescSource;
import io.myse.embeddedes.ElasticSearch;
import io.myse.common.Tasks;
import io.myse.updater.Updater;
import io.myse.updater.Upgrader;
import io.myse.updater.VersionComparator;
import io.myse.webserver.JettyServer;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import javax.persistence.EntityManager;
import sun.misc.Signal;
import sun.misc.SignalHandler;

public class Main {

	public static void main(String[] args) throws Exception {
		try {
			start(args);
		} catch (Exception ex) {
			LOG.error("Problem starting !", ex);
		}
	}

	private void signalHandling() {
		try {
			Signal.handle(new Signal("TERM"),
					new SignalHandler() {
						@Override
						public void handle(Signal signal) {
							LOG.warn("Received a TERM signal !");
							Main.quit();
						}
					});
		} catch (Throwable ex) {
			// Not a big deal
			LOG.info("sun.* Proprietary call failure", ex);
		}
	}

	public static void start(String[] args) throws SQLException, IOException, Exception {
		TrayIconMgmt.start();

		Upgrader.main(args); // Upgrading code

		DBMgmt.start(); // RDB code

		JettyServer.start(); // Web server

		ElasticSearch.start();  // DDB code

		Browser.showMyse();

		versionCheck();

		EntityManager em = DBMgmt.getEntityManager();
		startIndexation(em);

		deletePreviousSources(em);

		//EventsNotifier.eventTextNotification("Finished loading", "MySE finished loading.");
	}

	private static boolean stopped = false;

	public static void stop() {
		try {
			stopped = true;
			LOG.info("Stopping tasks...");
			Tasks.stop();
			LOG.info("Stopping H2...");
			DBMgmt.stop();
			LOG.info("Stopping ES...");
			ElasticSearch.stop();
			LOG.info("Stopping Jetty...");
			JettyServer.stop();
		} catch (Exception ex) {
			LOG.error("Main.stop", ex);
		}
	}

	public static void quit() {
		stop();
		LOG.info("Good bye !");
		System.exit(0);
	}

	public static boolean running() {
		return !stopped;
	}

	private static void deletePreviousSources(EntityManager em) throws IOException {
		for (DBDescSource deleted : DBDescSource.allDeleted(em)) {
			em.getTransaction().begin();
			try {
				int nb = -1;
				while (nb != 0) {
					nb = deleted.deleteDocs(em);
					LOG.info("Deleting {}: Deleted {} DB files.", deleted.getShortName(), nb);
				}
				if (ElasticSearch.deleteIndex(deleted.getShortName())) {
					LOG.info("Deleting {}: Deleted index.", deleted.getShortName());
				}
				em.remove(deleted);
			} finally {
				em.getTransaction().commit();
			}
		}
		for (String shortName : ElasticSearch.listIndexes()) {
			DBDescSource dbSource = DBDescSource.get(shortName, em);
			if (dbSource == null) {
				LOG.warn("Deleting index \"{}\".", shortName);
				ElasticSearch.deleteIndex(shortName);
			}
		}
	}

	private static void startIndexation(EntityManager em) throws IOException {
		List<DBDescSource> allSources = DBDescSource.allExisting(em);
		if (allSources.isEmpty()) {
			LOG.warn("NO SOURCE! Creating one !");
			em.getTransaction().begin();

			if (new File("private/nas.properties").exists()) { // Private NAS source
				DBDescSource smb = new DBDescSource();
				Map<String, String> props = smb.getProperties();

				// All this source's data will come from the private directory
				try (InputStream is = new FileInputStream("private/nas.properties")) {
					Properties fileprops = new Properties();
					fileprops.load(is);
					smb.setName(fileprops.getProperty("_name"), em);
					smb.setType(fileprops.getProperty("_type"));
					for (Map.Entry<Object, Object> me : fileprops.entrySet()) {
						props.put((String) me.getKey(), (String) me.getValue());
					}
				}
				em.persist(smb);
			}

			{ // Private documents source
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
							docs.setName("Local documents", em);
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

			{ // FTP Free source
				DBDescSource docs = new DBDescSource();
				docs.setName("assistance free", em);
				docs.setType(SourceVFS.TYPE);
				Map<String, String> props = docs.getProperties();
				props.put(SourceVFS.PROP_SCHEME, "ftp");
				props.put(SourceVFS.PROP_HOST, "ftp.free.fr");
				props.put(SourceVFS.PROP_PATH, "/pub/assistance/");
				props.put(Source.PROP_FILENAME_INCLUDE, "*.pdf");
				props.put(Source.PROP_FILENAME_EXCLUDE, "*");
				em.persist(docs);
			}

			{ // Some sample docs
				File sampleDocsDir = new File("sample_docs");
				if (sampleDocsDir.exists() || DBDescSource.get("Sample_docs", em) == null) {
					DBDescSource sampleDocs = new DBDescSource();
					sampleDocs.setName("Sample docs", em);
					sampleDocs.setType(SourceDisk.TYPE);
					sampleDocs.getProperties().put("path", sampleDocsDir.getAbsolutePath());
					em.persist(sampleDocs);
				}
			}

			em.getTransaction().commit();
			allSources = DBDescSource.allExisting(em);
		}

		for (DBDescSource dbSource : allSources) {
			Indexation.start(dbSource);
		}

		//DONE: Delete all the non-existing sources and their respective documents
		//DONE: Show tray icon if possible : Double click opens the interface
	}

	private static void versionCheck() {
		Tasks.getService().scheduleWithFixedDelay(new Updater(), 0, 3, TimeUnit.MINUTES);

		{ // We check the version
			String currentVersion = BuildInfo.VERSION;
			String previousVersion = Config.get(VERSION, "0.0", false);
			if (!currentVersion.equals(previousVersion)) {
				LOG.info("VERSION Change: {} --> {}", previousVersion, currentVersion);
				upgradeVersion(previousVersion);
				Config.set(VERSION, currentVersion);
			} else {
				LOG.info("Version: " + currentVersion);
			}
		}
	}

	public static final String VERSION = "version";

	private static void upgradeVersion(String previousVersion) {
		EntityManager em = DBMgmt.getEntityManager();
		em.getTransaction().begin();
		try {
//			try (Client esClt = ElasticSearch.client()) {
			if (VersionComparator.compareVersions(previousVersion, "1.0.132") < 0) {
				LOG.info("Upgrading from one index to multiple indexes");

				for (DBDescSource source : DBDescSource.allExisting(em)) {
					String shortName = source.getShortName();
					source.resetShortName(em);

					if (!source.getShortName().equals(shortName)) {
						ElasticSearch.deleteIndex(shortName);

						// It's easier to just reindex everything
						source.deleteDocs(em);
					}
					//em.persist(source);
				}
				ElasticSearch.deleteIndex("all");

			}

			// Unique install ID
			Config.get("install_id", UUID.randomUUID().toString(), true);
//			}
		} finally {
			em.getTransaction().commit();
			em.close();
		}
	}
}
