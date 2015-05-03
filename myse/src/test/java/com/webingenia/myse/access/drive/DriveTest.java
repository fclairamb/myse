package com.webingenia.myse.access.drive;

import com.webingenia.myse.access.File;
import com.webingenia.myse.access.Source;
import com.webingenia.myse.access.SourceEditingContext;
import com.webingenia.myse.db.DBMgmt;
import com.webingenia.myse.db.model.DBDescSource;
import com.webingenia.myse.desktop.Browser;
import com.webingenia.myse.webserver.JettyServer;
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
	public void after() throws Exception {
		DBMgmt.stop();
		JettyServer.stop();
	}

	@Test
	public void testAccess() throws Exception {
		EntityManager em = DBMgmt.getEntityManager();
		em.getTransaction().begin();
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
			SourceEditingContext sec = new SourceEditingContext();
			srcDrive.postSave(sec);
			if (sec.nextUrl != null) {
				JettyServer.start();

				Browser.show(sec.nextUrl);
			}
			SourceDrive drive = (SourceDrive) srcDrive;
			File rootDir = srcDrive.getRootDir();
			List<File> listFiles = rootDir.listFiles();
			System.out.println("files: " + listFiles.size());
		} finally {
			em.getTransaction().commit();
			em.close();
		}
	}
}
