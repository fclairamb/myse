package io.myse.db.model;

import io.myse.common.Passwords;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.CollectionTable;
import javax.persistence.ElementCollection;
import javax.persistence.EntityManager;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.MapKeyColumn;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

@Entity
@Table(name = "user", indexes = {
	@Index(name = "userName", columnList = "userName")
})
public class DBUser {

	@Id
	@Column(name = "user_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	public Long getId() {
		return id;
	}

	@Column(name = "name", unique = true)
	private String name;

	@Column(name = "password")
	private String password;

	@ElementCollection(fetch = FetchType.LAZY)
	@CollectionTable(name = "user_property")
	@JoinTable(name = "user_property", joinColumns = @JoinColumn(name = "user_id"))
	@MapKeyColumn(name = "name", nullable = false)
	private Map<String, String> properties = new HashMap<>();

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setPlainPassword(String password) {
		if (password.equals(" ")) {
			setPassword(null);
		} else {
			setPassword(Passwords.hash(password));
		}
	}

	public boolean checkPlainPassword(String pass) {
		return Passwords.hash(pass).equals(this.password);
	}

	private static final String PROP_RIGHT_ADMIN = "right_admin";

	public boolean isAdmin() {
		return "true".equals(properties.get(PROP_RIGHT_ADMIN));
	}

	public void setAdmin(boolean admin) {
		properties.put(PROP_RIGHT_ADMIN, admin ? "true" : "false");
	}

	public static List<DBUser> all(EntityManager em) {
		return em.createQuery("SELECT u FROM DBUser u", DBUser.class).getResultList();
	}

	public static DBUser get(String name, EntityManager em) {
		for (DBUser u : em.createQuery("SELECT u FROM DBUser u WHERE u.name = :name", DBUser.class).setParameter("name", name).getResultList()) {
			return u;
		}
		return null;
	}

	public static DBUser get(long id, EntityManager em) {
		for (DBUser u : em.createQuery("SELECT u FROM DBUser u WHERE u.id = :id", DBUser.class).setParameter("id", id).getResultList()) {
			return u;
		}
		return null;
	}
}
