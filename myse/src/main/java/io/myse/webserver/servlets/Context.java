/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.myse.webserver.servlets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.io.Reader;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

class Context {

	private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

	public final HttpServletRequest req;
	public final HttpServletResponse resp;

	public Context(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		this.req = req;
		this.resp = resp;
		try (Reader reader = req.getReader()) {
			this.input = gson.fromJson(reader, Map.class);
		}
	}

	EntityManager em;
	Map<String, Object> input;

	public Session getSession() {
		return Session.get(req);
	}
}
