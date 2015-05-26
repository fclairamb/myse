package io.myse.webserver.servlets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import static io.myse.common.LOG.LOG;
import io.myse.db.DBMgmt;
import io.myse.db.SettingsExporter;
import io.myse.db.model.DBUser;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RestSetupUser extends HttpServlet {

	private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

	public static class UserInfo {

		public UserInfo() {

		}

		public UserInfo(DBUser user) {
			this.id = user.getId();
			this.name = user.getName();
			this.admin = user.isAdmin();
		}

		public Long id;
		public String name;
		public String password;
		public boolean admin;
	}

	public static class UsersInfo {

		List<UserInfo> users = new ArrayList<>();
	}

	private Object doList(Context context) {
		UsersInfo info = new UsersInfo();
		for (DBUser user : DBUser.all(context.em)) {
			info.users.add(new UserInfo(user));
		}
		return info;
	}

	private Object doGetUser(Context context) {
		long userId = Integer.parseInt(context.req.getParameter("id"));
		return new UserInfo(DBUser.get(userId, context.em));
	}

	private Object doDelete(Context context) {
		long userId = Integer.parseInt(context.req.getParameter("id"));
		context.em.getTransaction().begin();
		try {
			DBUser user = DBUser.get(userId, context.em);
			context.em.remove(user);
			return true;
		} finally {
			context.em.getTransaction().commit();
		}
	}

	private Object doEdit(Context context) {
		context.em.getTransaction().begin();
		try {
			DBUser user;
			if (context.input.containsKey("id")) {
				Object oId = context.input.get("id");
				long userId;
				if (oId instanceof Double) {
					userId = ((Double) oId).longValue();
				} else {
					throw new IllegalArgumentException("Unhandled user.id type !");
				}
				user = DBUser.get(userId, context.em);
			} else {
				user = new DBUser();
			}
//			if ( context.inputst.containsKey("name") ) {
			user.setName((String) context.input.get("name"));
//			}
			String password = (String) context.input.get("password");
			if (password != null && !password.isEmpty()) {
				user.setPlainPassword(password);
			}

			Boolean admin = (Boolean) context.input.get("admin");
			user.setAdmin(admin);
			context.em.persist(user);
			return new UserInfo(user);
		} finally {
			context.em.getTransaction().commit();
			SettingsExporter.autoSave();
		}
	}

	protected void doRequest(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		if (!Session.get(req).checkIsAdmin(resp)) {
			return;
		}
		String path = req.getPathInfo();
		Object output = null;

		Context context = new Context(req, resp);
		context.em = DBMgmt.getEntityManager();

		try {
			switch (path) {
				case "/list":
					output = doList(context);
					break;
				case "/get":
					output = doGetUser(context);
					break;
				case "/edit":
					output = doEdit(context);
					break;
				case "/delete":
					output = doDelete(context);
					break;
			}

		} catch (Exception ex) {
			LOG.error("RestSetupUser", ex);
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal error");
			try (PrintWriter out = resp.getWriter()) {
				ex.printStackTrace(out);
			}
			return;
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
		doRequest(req, resp); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doRequest(req, resp); //To change body of generated methods, choose Tools | Templates.
	}
}
