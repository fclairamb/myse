package com.webingenia.myse.webserver.servlets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.webingenia.myse.common.LOG;
import com.webingenia.myse.db.DBMgmt;
import com.webingenia.myse.db.model.DBDescSource;
import com.webingenia.myse.embeddedes.ElasticSearch;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.elasticsearch.action.admin.indices.stats.IndicesStatsResponse;
import org.elasticsearch.client.Client;

public class RestStats extends HttpServlet {

	private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

	public static class SourceStats {

		public String shortName;
		public String name;
		public long size;
		public int sizePercentage;
		public long docsCount;
		public long docsDeletedCount;
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		Object output;
		List<SourceStats> list = new ArrayList<>();
		output = list;

		EntityManager em = DBMgmt.getEntityManager();
		try (Client esClt = ElasticSearch.client()) {
			for (String indexName : ElasticSearch.listIndexes()) {
				try {
					DBDescSource dbSource = DBDescSource.get(indexName, em);
					SourceStats indexStats = new SourceStats();
					list.add(indexStats);
					IndicesStatsResponse stats = esClt.admin().indices().prepareStats()
							.clear()
							.setIndices(indexName)
							.setStore(true)
							.setDocs(true)
							.execute().actionGet();
					long size = stats.getTotal().getStore().getSize().bytes();
					if (dbSource != null) {
						indexStats.name = dbSource.getName();
					}
					indexStats.shortName = indexName;
					indexStats.size = size;
					indexStats.docsCount = stats.getTotal().getDocs().getCount();
					indexStats.docsDeletedCount = stats.getTotal().getDocs().getDeleted();
				} catch (Exception ex) {
					LOG.LOG.warn("Stats", ex);
				}
			}

			long total = 0;
			for (SourceStats stats : list) {
				total += stats.size;
			}
			for (SourceStats stats : list) {
				stats.sizePercentage = total != 0 ? (int) (stats.size * 100 / total) : 0;
			}
		} finally {
			em.close();
		}

		{ // The response
			resp.setContentType("application/json; charset=utf8");
			try (PrintWriter out = resp.getWriter()) {
				out.write(gson.toJson(output));
			}
		}
	}
}
