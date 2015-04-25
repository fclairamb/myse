package com.webingenia.myse.access.drive;

import com.webingenia.myse.access.AccessException;
import com.webingenia.myse.access.File;
import com.webingenia.myse.access.Source;
import com.webingenia.myse.common.LOG;
import com.webingenia.myse.db.DBMgmt;
import com.webingenia.myse.db.model.DBDescSource;
import java.sql.SQLException;
import java.util.List;
import javax.persistence.EntityManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DriveTest {

	@Before
	public void before() throws SQLException {
		DBMgmt.start();
	}

	@After
	public void after() {
		DBMgmt.stop();
	}

	@Test
	public void testAccess() throws AccessException {
		EntityManager em = DBMgmt.getEntityManager();
		try {
			DBDescSource dbSrcDrive = null;
			for (DBDescSource src : DBDescSource.allExisting(em)) {
				if (src.getType().equals(SourceDrive.TYPE)) {
					dbSrcDrive = src;
				}
			}
			if (dbSrcDrive == null) {
				return;
			}
			Source srcDrive = Source.get(dbSrcDrive);
			File rootDir = srcDrive.getRootDir();
		} catch (Exception ex) {
			LOG.LOG.error("testAccess", ex);
		} finally {
			em.close();
		}
	}
}
