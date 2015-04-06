package com.webingenia.myse.db.model;

import com.webingenia.myse.access.AccessException;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.CollectionTable;
import javax.persistence.ElementCollection;
import javax.persistence.EntityManager;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.MapKeyColumn;
import javax.persistence.Table;

@Entity
@Table(
		name = "source",
		indexes = {
			@Index(name = "shortName", columnList = "shortName", unique = true)
		}
)
public class DBDescSource implements Serializable {

	@Id
	@Column(name = "source_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@Column(name = "name")
	private String name;

	@Column(name = "short_name")
	private String shortName;

	@Column(name = "type")
	private String type;

	@Column(name = "scan_period")
	private int scanPeriod;

	@Column(name = "state")
	@Enumerated(EnumType.STRING)
	private AccessException.AccessState state;

	@ElementCollection
	@CollectionTable(name = "source_property")
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
		if (this.shortName == null) {
			this.shortName = this.name.replaceAll("[\\s]", "_").replaceAll("[^\\w]", "");
		}
	}

	public String getShortName() {
		return this.shortName;
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

	public static DBDescSource get(long id, EntityManager em) {
		for (DBDescSource s : em.createQuery("SELECT s FROM DBDescSource s WHERE s.id = :id", DBDescSource.class).setParameter("id", id).getResultList()) {
			return s;
		}
		return null;
	}

	public static DBDescSource get(String shortName, EntityManager em) {
		for (DBDescSource s : em.createQuery("SELECT s FROM DBDescSource s WHERE s.shortName = :short", DBDescSource.class).setParameter("short", shortName).getResultList()) {
			return s;
		}
		return null;
	}

	public Map<String, String> asMap() {
		Map<String, String> map = new HashMap<>();
		for (Map.Entry<String, String> me : getProperties().entrySet()) {
			map.put(me.getKey(), me.getValue());
		}
		map.put("_id", Long.toString(getId()));
		map.put("_type", getType());
		map.put("_name", getName());
		map.put("_short", getShortName());
		return map;
	}

	public void fromMap(Map<String, String> map) {
		for (Map.Entry<String, String> me : map.entrySet()) {
			String value = me.getValue();
			switch (me.getKey()) {
				case "_id":
					id = Long.parseLong(value);
					break;
				case "_type":
					type = value;
					break;
				case "_name":
					name = value;
					break;
				case "_short":
					shortName = value;
					break;
				default:
					properties.put(name, value);
			}
		}
	}

//	public String getEsIndexName() {
//		return "source_" + getId();
//	}
}
