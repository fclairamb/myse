package com.webingenia.myse.webserver.servlets;

import com.webingenia.myse.access.AccessException;
import com.webingenia.myse.access.File;
import com.webingenia.myse.access.Source;
import static com.webingenia.myse.common.LOG.LOG;
import com.webingenia.myse.db.DBMgmt;
import com.webingenia.myse.db.model.DBDescFile;
import com.webingenia.myse.db.model.DBDescSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.elasticsearch.common.io.Streams;

public class PageDownload extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String sSource = req.getParameter("source");
		String path = req.getParameter("path");
		String docId = req.getParameter("docId");

		EntityManager em = DBMgmt.getEntityManager();
		try {
			DBDescSource dbSource = null;
			if (sSource != null) {
				DBDescSource.get(sSource, em);
			} else if (docId != null) {
				DBDescFile dbFile = DBDescFile.get(docId, em);
				dbSource = dbFile.getSource();
				path = dbFile.getPath();
			}
			Source source = Source.get(dbSource);
			File file = source.getFile(path);
			resp.addHeader("Content-Disposition", "inline; filename=\"" + file.getName() + "\"");
			try (InputStream is = file.getInputStream()) {
				try (OutputStream os = resp.getOutputStream()) {
					Streams.copy(is, os);
				}
			}
		} catch (Exception ex) {
			LOG.error("Could not access " + sSource + ":" + path, ex);
		} finally {
			em.close();
		}
	}

}
