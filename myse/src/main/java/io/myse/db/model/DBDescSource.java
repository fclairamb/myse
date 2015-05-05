package io.myse.db.model;

import io.myse.access.AccessException;
import io.myse.access.Source;
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
import org.eclipse.persistence.annotations.CascadeOnDelete;

@Entity
@Table(
		name = "source",
		indexes = {
			@Index(name = "shortName", columnList = "shortName", unique = true)
		}
)
@CascadeOnDelete
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

	@Column(name = "deleted")
	private boolean deleted;

	@ElementCollection
	@CollectionTable(name = "source_property")
	@JoinTable(name = "source_property", joinColumns = @JoinColumn(name = "source_id"))
	@MapKeyColumn(name = "name", nullable = false)
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

	public void setName(String name, EntityManager em) {
		this.name = name;
		createShortName(em);
	}

	private void createShortName(EntityManager em) {
		if (this.shortName == null) {
			String osn = this.name.replaceAll("[\\s]", "_").replaceAll("[^\\w]", "").toLowerCase();

			for (int i = 0; i < 100; i++) {
				String sn = osn + (i == 0 ? "" : "_" + i);
				List<DBDescSource> list = em.createQuery("SELECT s FROM DBDescSource s WHERE s.deleted = false AND s.shortName = :shortName", DBDescSource.class).setParameter("shortName", sn).getResultList();
				if (list.isEmpty() || (list.size() == 1 && list.get(0).id == id)) {
					this.shortName = sn;
					break;
				}
			}
		}
	}

	public String getShortName() {
		return this.shortName;
	}

	public void resetShortName(EntityManager em) {
		shortName = null;
		createShortName(em);
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

	private static List<DBDescSource> all(boolean deleted, EntityManager em) {
		return em.createQuery("SELECT s FROM DBDescSource s WHERE s.deleted = :deleted", DBDescSource.class).setParameter("deleted", deleted).getResultList();
	}

	public static List<DBDescSource> allExisting(EntityManager em) {
		return all(false, em);
	}

	public static List<DBDescSource> allDeleted(EntityManager em) {
		return all(true, em);
	}

	public boolean deleted() {
		return this.deleted;
	}

	public static void delete(long id, EntityManager em) {
		DBDescSource dbs = get(id, em);
		dbs.deleted = true;
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

	public void fromMap(Map<String, String> map, EntityManager em) {
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
					properties.put(me.getKey(), value);
			}
		}
		createShortName(em);
	}

	public boolean doIndex() {
		String value = getProperties().get(Source.PROP_INDEX);
		return !deleted && (value == null || value.equals("true"));
	}

	public int deleteDocs(EntityManager em) {
//		em.getTransaction().begin();
//		try {
		return em.createQuery("DELETE FROM DBDescFile f WHERE f.source = :source").setParameter("source", this).executeUpdate();
//		} finally {
//			em.getTransaction().commit();
//		}
	}
	
	public int getNbDocsToAnalyse(EntityManager em) {
		return (int) (long) em.createQuery("SELECT COUNT(f) FROM DBDescFile f WHERE f.source = :source AND f.directory = FALSE AND f.toAnalyse = TRUE").setParameter("source", this).getSingleResult();
	}
	
	public int getTotalNbDocs(EntityManager em) {
		return (int) (long) em.createQuery("SELECT COUNT(f) FROM DBDescFile f WHERE f.source = :source AND f.directory = FALSE").setParameter("source", this).getSingleResult();
	}
}
