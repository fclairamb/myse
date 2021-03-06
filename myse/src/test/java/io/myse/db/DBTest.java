package io.myse.db;

import io.myse.db.model.Config;
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
		Config.del("unknown");
		Config.del("param1");
		Assert.assertEquals(null, Config.get("unknown", null, true));
		Config.set("param1", "value1");
		Assert.assertEquals("value1", Config.get("param1", "value3", true));
		Config.set("param1", "value2");
		Assert.assertEquals("value2", Config.get("param1", "value3", true));
	}
	
}
