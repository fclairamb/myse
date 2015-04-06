package com.webingenia.myse.webserver.servlets;

import com.webingenia.myse.access.AccessException;
import com.webingenia.myse.access.File;
import com.webingenia.myse.access.Source;
import static com.webingenia.myse.common.LOG.LOG;
import com.webingenia.myse.db.DBMgmt;
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
		String sPath = req.getParameter("path");

		EntityManager em = DBMgmt.getEntityManager();
		try {
			DBDescSource dbSource = DBDescSource.get(sSource, em);
			Source source = Source.get(dbSource);
			File file = source.getFile(sPath);
			resp.addHeader("Content-Disposition", "inline; filename=\"" + file.getName() + "\"");
			try (InputStream is = file.getInputStream()) {
				try (OutputStream os = resp.getOutputStream()) {
					Streams.copy(is, os);
				}
			}
		} catch (AccessException ex) {
			LOG.error("Could not access " + sSource + ":" + sPath, ex);
		} finally {
			em.close();
		}
	}

}
