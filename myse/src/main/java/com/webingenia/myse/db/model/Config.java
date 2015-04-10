package com.webingenia.myse.db.model;

import static com.webingenia.myse.common.LOG.LOG;
import com.webingenia.myse.db.DBMgmt;
import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public static void set(String name, String value) {
		LOG.warn("CONFIG: {} = {}", name, value);
		Config c = getConfig(name);
		if (c == null) {
			c = new Config();
			c.setName(name);
		}

		c.setValue(value);
		DBMgmt.getDefaultEntityManager().persist(c);
	}

	public static String get(String name, String defaultValue) {
		Config c = getConfig(name);
		if (c == null) {
			set(name, defaultValue);
		}
		return defaultValue;
	}

	public static int get(String name, int defaultValue) {
		return Integer.parseInt(get(name, Integer.toString(defaultValue)));
	}

	public static boolean get(String name, boolean defaultValue) {
		return Boolean.parseBoolean(get(name, Boolean.toString(defaultValue)));
	}

	private static Config getConfig(String name) {
		for (Config c : DBMgmt.getDefaultEntityManager().createQuery("SELECT c FROM Config c WHERE c.name = :name", Config.class).setParameter("name", name).getResultList()) {
			return c;
		}
		return null;
	}

	public static void del(String name) {
		DBMgmt.getDefaultEntityManager().createQuery("DELETE FROM Config c WHERE c.name = :name").setParameter("name", name).executeUpdate();
	}
}
