package com.webingenia.cifstest.db.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import javax.persistence.ElementCollection;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.MapKeyColumn;
import javax.persistence.Table;

@Entity
@Table(name = "source")
public class DBDescSource implements Serializable {

	@Id
	@Column(name = "source_id")
	private long sourceId;

	@Column
	private String name;

	@ElementCollection
	@JoinTable(name = "SOURCE_PROPERTY", joinColumns = @JoinColumn(name = "source_id"))
	@MapKeyColumn(name = "id")
	@Column(name = "VALUE")
	private Map<String, String> properties = new HashMap<>();

	public long getSourceId() {
		return sourceId;
	}

	public void setSourceId(long sourceId) {
		this.sourceId = sourceId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Map<String, String> getProperties() {
		return properties;
	}

	public void setProperties(Map<String, String> properties) {
		this.properties = properties;
	}
}
