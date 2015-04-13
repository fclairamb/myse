package com.webingenia.myse.db.model;

import static com.webingenia.myse.common.LOG.LOG;
import com.webingenia.myse.db.DBMgmt;
import java.io.Serializable;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "config")
public class Config implements Serializable {

	@Column(name = "NAME")
	@Id
	private String name;

	@Column(name = "VALUE")
	private String value;

	public static final String PAR_UPDATE_CHANNEL = "update.channel";

	public static String get(String name, String defaultValue, boolean save) {

		EntityManager em = DBMgmt.getEntityManager();
		try {
			Config c = getConfig(name, em);
			if (c == null) {
				if (defaultValue != null && save) {
					set(name, defaultValue);
				}
				return defaultValue;
			}
			return c.value;
		} finally {
			em.close();
		}
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
			Config c = getConfig(name, em);
			if (c == null) {
				c = new Config();
				c.name = name;
			}

			c.value = value;
			EntityTransaction tr = em.getTransaction();
			tr.begin();
			em.persist(c);
			tr.commit();
		} finally {
			em.close();
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

	@Override
	public String toString() {
		return name + "=" + value;
	}
}
