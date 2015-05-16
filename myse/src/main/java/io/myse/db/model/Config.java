package io.myse.db.model;

import static io.myse.common.LOG.LOG;
import io.myse.db.DBMgmt;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "config")
public class Config implements Serializable {

	@Column(name = "NAME", unique = true)
	@Id
	private String name;

	@Column(name = "VALUE")
	private String value;

	public static final String PAR_UPDATE_CHANNEL = "update.channel",
			PAR_HOSTNAME = "hostname",
			PAR_ALLOW_DOWNLOAD = "allow_download",
			PAR_ALLOW_LINK = "allow_link",
			PAR_ALLOW_GUEST_LOGIN = "guests.authorized",
			PAR_ALLOW_GUEST_ADMIN = "guests.admin";

	private static final Map<String, String> cache = Collections.synchronizedMap(new TreeMap());

	public static String get(String name, String defaultValue, boolean save) {

		String value = cache.get(name);
		if (value != null) {
			return value;
		}

		EntityManager em = DBMgmt.getEntityManager();
		try {
			Config c = getConfig(name, em);
			if (c == null) {
				if (defaultValue != null && save) {
					set(name, defaultValue);
				}
				value = defaultValue;
			} else {
				value = c.value;
			}
			cache.put(name, value);
		} finally {
			em.close();
		}

		return value;
	}

	public static int get(String name, int defaultValue, boolean save) {
		return Integer.parseInt(get(name, Integer.toString(defaultValue), save));
	}

	public static boolean get(String name, boolean defaultValue, boolean save) {
		return Boolean.parseBoolean(get(name, Boolean.toString(defaultValue), save));
	}

	public static void set(String name, String value) {
		LOG.warn("CONFIG: {} = {}", name, value);

		EntityManager em = DBMgmt.getEntityManager();
		try {
			EntityTransaction tr = em.getTransaction();
			tr.begin();
			Config c = getConfig(name, em);
			if (c == null) {
				c = new Config();
				c.name = name;
				em.persist(c);
			}
			c.value = value;
			tr.commit();
		} finally {
			em.close();
			cache.remove(name);
		}
	}

	private static Config getConfig(String name, EntityManager em) {
		for (Config c : em.createQuery("SELECT c FROM Config c WHERE c.name = :name", Config.class).setParameter("name", name).getResultList()) {
			return c;
		}
		return null;
	}

	public static List<Config> all(EntityManager em) {
		return em.createQuery("SELECT c FROM Config c", Config.class).getResultList();
	}

	public static void del(String name) {
		EntityManager em = DBMgmt.getEntityManager();
		em.getTransaction().begin();
		try {
			em.createQuery("DELETE FROM Config c WHERE c.name = :name").setParameter("name", name).executeUpdate();
		} finally {
			em.getTransaction().commit();
			em.close();
		}
	}

	public String getName() {
		return name;
	}

	public String getValue() {
		return value;
	}

	@Override
	public String toString() {
		return name + "=" + value;
	}
}
