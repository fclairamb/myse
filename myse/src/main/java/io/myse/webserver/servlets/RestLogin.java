package io.myse.webserver.servlets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.myse.db.DBMgmt;
import io.myse.db.model.DBUser;
import java.io.IOException;
import java.io.PrintWriter;
import javax.persistence.EntityManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RestLogin extends HttpServlet {

	private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

	static class LoginResult {

		public boolean ok;
		public boolean connected;
		public boolean identified;
		public boolean admin;
		public String error;
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		Context ctx = new Context(req, resp);
		Object response = checkLogin(ctx);

		{ // The response
			resp.setContentType("application/json; charset=utf8");
			try (PrintWriter pw = resp.getWriter()) {
				pw.write(gson.toJson(response));
			}
		}
	}

	private Object checkLogin(Context ctx) {
		Session session = ctx.getSession();

		LoginResult result = new LoginResult();

		if (ctx.input != null) {
			String user = (String) ctx.input.get("name");
			String pass = (String) ctx.input.get("pass");
			if (user != null && pass != null) {
				EntityManager em = DBMgmt.getEntityManager();
				try {
					DBUser dbUser = DBUser.get(user, em);
					if (dbUser != null) {
						if (dbUser.checkPlainPassword(pass)) {
							session.setUser(dbUser);
							result.ok = true;
						} else {
							result.error = "Wrong password.";
						}
					} else {
						result.error = "Unknown user.";
					}
				} finally {
					em.close();
				}
			}
		}

		result.connected = session.getIsUser();
		result.identified = session.getIdentified();
		result.admin = session.getIsAdmin();

		return result;
	}

}
