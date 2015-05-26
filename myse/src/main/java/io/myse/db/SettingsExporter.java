package io.myse.db;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import static io.myse.common.LOG.LOG;
import io.myse.common.Paths;
import io.myse.db.model.Config;
import io.myse.db.model.DBDescSource;
import io.myse.db.model.DBUser;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

public class SettingsExporter {

	private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

	public static class Settings {

		public Map<String, String> parameters = new HashMap<>();
		public Map<String, Map<String, String>> sources = new HashMap<>();
		public Map<String, Map<String, String>> users = new HashMap<>();
	}

	public static Settings getSettings() {
		EntityManager em = DBMgmt.getEntityManager();
		try {
			Settings settings = new Settings();
			settings.parameters = Config.allAsMap(em);
			for (DBDescSource src : DBDescSource.allExisting(em)) {
				settings.sources.put(src.getShortName(), src.asMap());
			}
			for (DBUser usr : DBUser.all(em)) {
				settings.users.put(usr.getName(), usr.asMap());
			}
			return settings;
		} finally {
			em.close();
		}
	}

	public static void exportSettings(File file) throws FileNotFoundException {
		try (PrintWriter writer = new PrintWriter(file)) {
			writer.print(gson.toJson(getSettings()));
		}
	}

	public static void importSettings(File file) throws IOException {
		Settings settings;
		try (FileReader fr = new FileReader(file)) {
			try (JsonReader jr = new JsonReader(fr)) {
				settings = gson.fromJson(jr, Settings.class);
			}
		}

		EntityManager em = DBMgmt.getEntityManager();
		try {
			// We import the parameters
			EntityTransaction tr = em.getTransaction();
			tr.begin();
			for (Map.Entry<String, String> conf : settings.parameters.entrySet()) {
				Config.set(conf.getKey(), conf.getValue());
			}

			// And then the sources
			for (Map.Entry<String, Map<String, String>> me : settings.sources.entrySet()) {
				DBDescSource db = DBDescSource.get(me.getKey(), em);
				if (db == null) {
					db = new DBDescSource();
					em.persist(db);
				}
				db.fromMap(me.getValue(), em);
			}
			tr.commit();
		} finally {
			em.close();
		}
	}

	public static void autoSave() {
		File settingsFile = new File(Paths.getAppDir(), "settings.json");
		try {
			exportSettings(settingsFile);
		} catch (FileNotFoundException ex) {
			LOG.error("This is impossible !", ex);
		}
	}
}
