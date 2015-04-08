package com.webingenia.myse.webserver.servlets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.webingenia.myse.access.Source;
import com.webingenia.myse.access.disk.SourceDisk;
import com.webingenia.myse.access.smb.SourceSMB;
import com.webingenia.myse.access.vfs.SourceVFS;
import com.webingenia.myse.db.DBMgmt;
import com.webingenia.myse.db.model.DBDescSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RestSetupSource extends HttpServlet {

	private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

	static class Context {

		EntityManager em;
		Map<String, Object> input;
		private HttpServletRequest req;
	}

	private Object doProcessList(Context context) {
		List<DBDescSource> dbList = DBDescSource.all(context.em);
		List<Map<String, String>> list = new ArrayList<>(dbList.size());
		for (DBDescSource s : dbList) {
			list.add(s.asMap());
		}
		return list;
	}

	private Object doProcessGet(Context context) {
		String sId = context.req.getParameter("id");
		DBDescSource dbSource = null;
		if (sId != null) {
			long id = Long.parseLong(sId);
			dbSource = DBDescSource.get(id, context.em);
		}
		if (dbSource != null) {
			return dbSource.asMap();
		} else {
			return null;
		}
	}

	private Map<String, String> mapStrObjToMapStrStr(Map<String, Object> map) {
		final Map<String, String> newMap = new HashMap<>();
		for (Map.Entry<String, Object> me : map.entrySet()) {
			Object value = me.getValue();
			if (value instanceof String) {
				newMap.put(me.getKey(), (String) value);
			}
		}
		return newMap;
	}

	private Object doProcessEdit(Context context) {
		context.em.getTransaction().begin();
		try {
			DBDescSource dbSource = new DBDescSource();
			dbSource.fromMap(mapStrObjToMapStrStr(context.input));
			context.em.persist(dbSource);
			return dbSource.asMap();
		} finally {
			context.em.getTransaction().commit();
		}
	}

	private Object doProcessDesc(Context context) {
		context.em.getTransaction().begin();
		try {
			DBDescSource dbSource = new DBDescSource();
			dbSource.setType(context.req.getParameter("type"));
			Source s = Source.get(dbSource);
			return s.getProperties();
		} finally {
			context.em.getTransaction().commit();
		}
	}

	static class SourceType {

		public SourceType(String type, String name) {
			this.type = type;
			this.name = name;
		}
		public String type;
		public String name;
	}

	private Object doProcessTypes(Context context) {
		return new SourceType[]{
			new SourceType(SourceSMB.TYPE, "Samba"),
			new SourceType(SourceVFS.TYPE, "VFS"),
			new SourceType(SourceDisk.TYPE, "Disk")
		};
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String path = req.getPathInfo();
		Context context = new Context();
		context.req = req;
		Object output = null;
		try (Reader reader = req.getReader()) {
			context.input = gson.fromJson(reader, Map.class);
		}
		try {

			context.em = DBMgmt.getEntityManager();

			switch (path) {
				case "/list":
					output = doProcessList(context);
					break;
				case "/edit":
					output = doProcessEdit(context);
					break;
				case "/get":
					output = doProcessGet(context);
					break;
				case "/desc":
					output = doProcessDesc(context);
					break;
				case "/types":
					output = doProcessTypes(context);
					break;
				default:
					output = "NOT HANDLED";
					break;
			}
		} finally {
			context.em.close();
		}

		{ // The response
			resp.setContentType("application/json; charset=utf8");
			try (PrintWriter out = resp.getWriter()) {
				out.write(gson.toJson(output));
			}
		}
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doPost(req, resp);
	}

}
