package io.myse.webserver.servlets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.myse.access.File;
import io.myse.access.Link;
import io.myse.access.LinkContext;
import io.myse.access.Source;
import static io.myse.common.LOG.LOG;
import io.myse.db.DBMgmt;
import io.myse.db.model.Config;
import io.myse.db.model.DBDescFile;
import io.myse.db.model.DBDescSource;
import java.io.IOException;
import java.io.PrintWriter;
import javax.persistence.EntityManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RestLink extends HttpServlet {

	private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

	private class ThisLink {

		private String address;
		private Link.LinkType type;
		public String name;

		public ThisLink(Link link) {
			this.address = link.address;
			this.type = link.type;
		}
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		if (!Config.get(Config.PAR_ALLOW_LINK, true, true)) {
			resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Linking is forbidden !");
			try (PrintWriter out = resp.getWriter()) {
				out.println("Linking is forbidden !");
			}
			return;
		}
		
		LinkContext context = new LinkContext();
		{
			String agent = req.getHeader("user-agent");
			if ( agent.contains("Windows") ) {
				context.os = LinkContext.OSType.WINDOWS;
			} else if ( agent.contains("Mac")) {
				context.os = LinkContext.OSType.MAC_OS_X;
			} else if ( agent.contains("Linux")) {
				context.os = LinkContext.OSType.LINUX;
			}
		}
		
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

			ThisLink link = null;

			File file = source.getFile(path);
			{
				Link fileLink = file.getLink(context);
				link = new ThisLink(fileLink);
				link.name = file.getName();
			}

			{ // The response
				resp.setContentType("application/json; charset=utf8");
				try (PrintWriter out = resp.getWriter()) {
					out.write(gson.toJson(link));
				}
			}

		} catch (Exception ex) {
			try (PrintWriter out = resp.getWriter()) {
				ex.printStackTrace(out);
			}
			LOG.error(String.format("Could not access %s", docId), ex);
		} finally {
			em.close();
		}
	}

}
