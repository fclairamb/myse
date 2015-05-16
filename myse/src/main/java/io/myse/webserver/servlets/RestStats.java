package io.myse.webserver.servlets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.myse.common.Files;
import static io.myse.common.LOG.LOG;
import io.myse.common.Paths;
import io.myse.db.DBMgmt;
import io.myse.db.model.DBDescSource;
import io.myse.embeddedes.ElasticSearch;
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
		public long docsNbIndexed;
		public long docsNbDeleted;
		public int docsNbToAnalyse;
		private int docsNbTotal;
	}

	public static class Stats {

		List<SourceStats> sources;
		public long totalSize;
		public long esSize;
		public long logsSize;
		public long h2Size;
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		if (!Session.get(req).checkIsUser(resp)) {
			return;
		}
		Stats stats = new Stats();
		stats.sources = new ArrayList<>();

		EntityManager em = DBMgmt.getEntityManager();
		try (Client esClt = ElasticSearch.client()) {
			for (String indexName : ElasticSearch.listIndexes()) {
//				try {
				DBDescSource dbSource = DBDescSource.get(indexName, em);
				SourceStats indexStats = new SourceStats();
				stats.sources.add(indexStats);
				IndicesStatsResponse isr = esClt.admin().indices().prepareStats()
						.clear()
						.setIndices(indexName)
						.setStore(true)
						.setDocs(true)
						.execute().actionGet();
				long size = isr.getTotal().getStore().getSize().bytes();
				if (dbSource != null) {
					indexStats.name = dbSource.getName();
//						indexStats.docsNbTotal = dbSource.getTotalNbDocs(em);
					indexStats.docsNbToAnalyse = dbSource.getNbDocsToAnalyse(em);
				}
				indexStats.shortName = indexName;
				indexStats.size = size;
				indexStats.docsNbIndexed = isr.getTotal().getDocs().getCount();
				indexStats.docsNbDeleted = isr.getTotal().getDocs().getDeleted();
//				} catch (Exception ex) {
//					LOG.warn("Stats", ex);
//				}
			}

			long total = 0;
			for (SourceStats ss : stats.sources) {
				total += ss.size;
			}
			for (SourceStats ss : stats.sources) {
				ss.sizePercentage = total != 0 ? (int) (ss.size * 100 / total) : 0;
			}

			stats.h2Size = Files.sizeR(Paths.getH2Dir());
			stats.esSize = Files.sizeR(Paths.getESDir());
			stats.logsSize = Files.sizeR(Paths.getLogsDir());
			stats.totalSize = Files.sizeR(Paths.getAppDir());
		} catch (Exception ex) {
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal error");
			try (PrintWriter out = resp.getWriter()) {
				ex.printStackTrace(out);
			}
			LOG.error("RestStats", ex);
			return;
		} finally {
			em.close();
		}

		{ // The response
			resp.setContentType("application/json; charset=utf8");
			try (PrintWriter out = resp.getWriter()) {
				out.write(gson.toJson(stats));
			}
		}
	}
}
