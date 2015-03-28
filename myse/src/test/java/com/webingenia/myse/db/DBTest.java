package com.webingenia.myse.db;

import com.webingenia.myse.db.model.Config;
import java.sql.SQLException;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

public class DBTest {

	public DBTest() {
	}

	@Before
	public void setUp() throws SQLException {
		DBMgmt.start();
	}

	@Test
	public void config() throws Exception {
		Assert.assertEquals(null, Config.get("unknown", null));
		Config.set("param1", "value1");
		Assert.assertEquals("value1", Config.get("param1", "value3"));
		Config.set("param1", "value2");
		Assert.assertEquals("value2", Config.get("param1", "value3"));
	}
}
