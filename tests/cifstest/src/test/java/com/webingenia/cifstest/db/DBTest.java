package com.webingenia.cifstest.db;

import com.webingenia.cifstest.model.Config;
import java.sql.SQLException;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.FlushModeType;
import org.junit.Before;
import org.junit.Test;

public class DBTest {

	public DBTest() {
	}

	@Before
	public void setUp() {
	}

	@Test
	public void insert() throws Exception {
		DBMgmt.start();
		Config c = new Config();
		c.setName("config1");
		c.setValue("value1");

		EntityManager em = DBMgmt.getEntityManager();
		EntityTransaction tr = em.getTransaction();
		tr.begin();
		em.persist(c);
		tr.commit();
	}
}
