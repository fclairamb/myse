package io.myse.webserver.servlets;

import io.myse.Main;
import io.myse.common.Tasks;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RestQuit extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		Tasks.getService().schedule(new Runnable() {

			@Override
			public void run() {
				Main.quit();
			}
		}, 2, TimeUnit.SECONDS);
	}

}
