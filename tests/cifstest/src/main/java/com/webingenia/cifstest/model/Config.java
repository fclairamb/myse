package com.webingenia.cifstest.model;

import com.webingenia.cifstest.db.DBMgmt;
import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
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
		Config c = getConfig(name);
		if (c == null) {
			c = new Config();
			c.setName(name);
		}

		c.setValue(value);
		DBMgmt.getEntityManager().persist(c);
	}

	public static String get(String name, String defaultValue) {
		Config c = getConfig(name);
		return c != null ? c.getValue() : null;
	}

	private static Config getConfig(String name) {
		for (Config c : DBMgmt.getEntityManager().createQuery("SELECT c FROM Config c WHERE c.name = :name", Config.class).setParameter("name", name).getResultList()) {
			return c;
		}
		return null;
	}
}
