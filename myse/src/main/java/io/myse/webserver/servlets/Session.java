package io.myse.webserver.servlets;

import io.myse.db.model.Config;
import io.myse.db.model.DBUser;
import java.io.IOException;
import java.util.Date;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class Session {

	private final Date creation = new Date();

	private Long userId;

	public static int RIGHT_GUEST = 0,
			RIGHT_USER = 1,
			RIGHT_ADMIN = 2;

	private int right;

	public Session() {
		right = getDefaultRight();
	}

	private int getDefaultRight() {
		if (Config.get(Config.PAR_ALLOW_GUEST_LOGIN, true, true)) {
			if (Config.get(Config.PAR_ALLOW_GUEST_ADMIN, true, true)) {
				return RIGHT_ADMIN;
			} else {
				return RIGHT_USER;
			}
		} else {
			return RIGHT_GUEST;
		}
	}

	public void setUser(DBUser user) {
		if (user != null) {
			userId = user.getId();
			if (user.isAdmin()) {
				right = RIGHT_ADMIN;
			} else {
				right = RIGHT_USER;
			}
		} else {
			right = getDefaultRight();
		}
	}

	public boolean getConnected() {
		return userId != null;
	}

	public boolean getIsAdmin() {
		return right >= RIGHT_ADMIN;
	}

	public boolean getIsUser() {
		return right >= RIGHT_USER;
	}

	public boolean getIsOnlyGuest() {
		return right == RIGHT_GUEST;
	}

	public boolean checkIsAdmin(HttpServletResponse response) throws IOException {
		if (!getIsAdmin()) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "You need to be an admin !");
			return false;
		}
		return true;
	}

	public boolean checkIsUser(HttpServletResponse response) throws IOException {
		if (!getIsUser()) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "You need to be a user !");
			return false;
		}
		return true;
	}

	private static final String ATTR_NAME = "myse";

	public static Session get(HttpSession httpSession) {
		Session data = (Session) httpSession.getAttribute(ATTR_NAME);

		if (data == null) {
			data = new Session();
			httpSession.setAttribute(ATTR_NAME, data);
		}
		return data;
	}

	public static Session get(HttpServletRequest httpRequest) {
		return get(httpRequest.getSession(true));
	}

	void delete(HttpServletRequest req) {
		HttpSession session = req.getSession();
		if (session != null) {
			session.removeAttribute(ATTR_NAME);
		}
	}

}
