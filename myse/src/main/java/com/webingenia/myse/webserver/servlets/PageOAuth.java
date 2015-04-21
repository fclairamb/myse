package com.webingenia.myse.webserver.servlets;

import static com.webingenia.myse.common.LOG.LOG;
import com.webingenia.myse.db.DBMgmt;
import com.webingenia.myse.db.model.DBDescSource;
import java.io.IOException;
import javax.persistence.EntityManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class PageOAuth extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {
			String sourceId = req.getParameter("source_id");
			String code = req.getParameter("code");
			if (sourceId != null && code != null) {
				EntityManager em = DBMgmt.getEntityManager();
				try {
					em.getTransaction().begin();
					DBDescSource dbSource = DBDescSource.get(Integer.parseInt(sourceId), em);
					dbSource.getProperties().put("code", code);
					em.persist(dbSource);
					em.getTransaction().commit();
					resp.sendRedirect("/static/index.html#/setup/source/edit/" + sourceId);
				} finally {
					em.close();
				}
			}
		} catch (Exception ex) {
			LOG.error("OAuth", ex);
		}
	}

}
