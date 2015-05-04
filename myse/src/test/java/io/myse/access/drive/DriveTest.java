package io.myse.access.drive;

import io.myse.access.File;
import io.myse.access.Source;
import io.myse.access.SourceEditingContext;
import io.myse.db.DBMgmt;
import io.myse.db.model.DBDescSource;
import io.myse.desktop.Browser;
import io.myse.webserver.JettyServer;
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
