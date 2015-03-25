package com.webingenia.cifstest.db.model;

import com.webingenia.cifstest.access.AccessException;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.ElementCollection;
import javax.persistence.EntityManager;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.MapKeyColumn;
import javax.persistence.Table;

@Entity
@Table(name = "source")
public class DBDescSource implements Serializable {

	@Id
	@Column(name = "source_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@Column(name = "name")
	private String name;

	@Column(name = "type")
	private String type;

	@Column(name = "scan_period")
	private int scanPeriod;

	@Column(name = "state")
	@Enumerated(EnumType.STRING)
	private AccessException.AccessState state;

	@ElementCollection
	@JoinTable(name = "source_property", joinColumns = @JoinColumn(name = "source_id"))
	@MapKeyColumn(name = "name")
	@Column(name = "value")
	private Map<String, String> properties = new HashMap<>();

	public long getId() {
		return id;
	}

	public void setId(long sourceId) {
		this.id = sourceId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Map<String, String> getProperties() {
		return properties;
	}

	public void setProperties(Map<String, String> properties) {
		this.properties = properties;
	}

	public int getScanPeriod() {
		return scanPeriod;
	}

	public void setScanPeriod(int scanPeriod) {
		this.scanPeriod = scanPeriod;
	}

	public AccessException.AccessState getState() {
		return state;
	}

	public void setState(AccessException.AccessState state) {
		this.state = state;
	}

	public static List<DBDescSource> all(EntityManager em) {
		return em.createQuery("SELECT s FROM DBDescSource s", DBDescSource.class).getResultList();
	}
}
