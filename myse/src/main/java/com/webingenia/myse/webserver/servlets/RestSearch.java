package com.webingenia.myse.webserver.servlets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import static com.webingenia.myse.common.LOG.LOG;
import com.webingenia.myse.embeddedes.ElasticSearch;
import com.webingenia.myse.fileexplore.FileIndexer;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;

public class RestSearch extends HttpServlet {

	public static class MyseSearchResponse {

		public List<SearchResult> results = new ArrayList<>();
		public String error;
		public long time;
	}

	public static class SearchResult {

		public String title;
		public String path;
		public String source;
		public String description;
		public String image;
		public String error;
		public long dateMod;
	}

	private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
		String q = null;

		int size = 50;

		if (req.getMethod().equals("POST")) {
			try (Reader reader = req.getReader()) {
				Map<String, Object> map = gson.fromJson(reader, Map.class);
				if (map.containsKey("q")) {
					q = (String) map.get("q");
				}
			}
		} else {
			q = req.getParameter("q");
			{
				String sSize = req.getParameter("size");
				if (sSize != null) {
					size = Integer.parseInt(sSize);
				}
			}
		}

		MyseSearchResponse response = new MyseSearchResponse();
		try {
			try (Client client = ElasticSearch.client()) {
				long before = System.currentTimeMillis();
				SearchRequestBuilder esRequest = client.prepareSearch(FileIndexer.ES_INDEX_NAME).setTypes(FileIndexer.ES_DOC_TYPE)
						.setSearchType(SearchType.DFS_QUERY_AND_FETCH)
						//.setQuery(QueryBuilders.termQuery("multi", q)) // Query
						.setQuery(QueryBuilders.queryString(q))
						.setFrom(0)
						.setSize(size);

				SearchResponse esResponse = esRequest
						.execute()
						.actionGet();

				int count = 0;
				for (SearchHit hit : esResponse.getHits().getHits()) {
					if (count++ > size) {
						break;
					}
					SearchResult r = new SearchResult();
					try {
						Map<String, Object> source = hit.getSource();
						r.title = (String) source.get("title");
						r.path = (String) source.get("path");
						r.source = (String) source.get("source_short");
						r.description = (String) source.get("content");

						if (r.description != null && r.description.length() > 400) {
							r.description = r.description.substring(0, 400) + "...";
						}
						
						{
							String sDate = (String) source.get("date_mod");
							Date dateMod = sdf.parse(sDate);
							r.dateMod = dateMod.getTime();
						}

					} catch (Exception ex) {
						r.error = ex.toString();
					}
					response.results.add(r);
				}
				response.time = System.currentTimeMillis() - before;
			}

		} catch (Exception ex) {
			response.error = ex.toString();
		}
		{ // The response
			resp.setContentType("application/json; charset=utf8");
			try (PrintWriter out = resp.getWriter()) {
				out.write(gson.toJson(response));
			}
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doGet(req, resp);
	}
}
