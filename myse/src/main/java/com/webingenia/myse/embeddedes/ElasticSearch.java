package com.webingenia.myse.embeddedes;

import com.webingenia.myse.common.LOG;
import com.webingenia.myse.common.Paths;
import com.webingenia.myse.db.model.DBDescSource;
import com.webingenia.myse.exploration.FileIndexer;
import com.webingenia.myse.webserver.servlets.RestSearch;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.delete.DeleteRequestBuilder;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.IndicesAdminClient;
import org.elasticsearch.client.Requests;
import org.elasticsearch.common.io.stream.BytesStreamOutput;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.elasticsearch.search.SearchHit;

public class ElasticSearch {

	private ElasticSearch() {
	}

	private static Node node;

	public static void start() {

		ImmutableSettings.Builder settings = ImmutableSettings.settingsBuilder();

		settings.put("node.name", "localCluster");
		settings.put("path.data", Paths.getESDir().getAbsolutePath());
		settings.put("http.enabled", true);

		node = NodeBuilder.nodeBuilder()
				.settings(settings)
				.clusterName("localCluster")
				.data(true).local(true).node();
		createIndex();
	}

	private static void createIndex() {
		try {
			try (Client clt = client()) {
				IndicesAdminClient indices = clt.admin().indices();
				boolean exists = indices.prepareExists(FileIndexer.ES_INDEX_NAME).execute().actionGet().isExists();
				if (!exists) {
					indices.create(Requests.createIndexRequest(FileIndexer.ES_INDEX_NAME)).actionGet();
				}
			}
		} catch (Exception ex) {
			LOG.LOG.error("createIndex", ex);
		}
	}

	public static int deleteDocsForSource(DBDescSource source) {
		int count = 0;
		try (Client clt = client()) {
			SearchRequestBuilder esRequest = clt.prepareSearch(FileIndexer.ES_INDEX_NAME).setTypes(FileIndexer.ES_DOC_TYPE)
					.setSearchType(SearchType.DFS_QUERY_AND_FETCH)
					//.setQuery(QueryBuilders.termQuery("multi", q)) // Query
					.setQuery(QueryBuilders.matchQuery("source_id", source.getId()))
					.setFrom(0)
					.setSize(100);

			SearchResponse esResponse = esRequest
					.execute()
					.actionGet();

			for (SearchHit hit : esResponse.getHits().getHits()) {
				count++;
				RestSearch.SearchResult r = new RestSearch.SearchResult();
				try {
					// TODO: Execute it
					LOG.LOG.info("Deleting " + hit.getId());
					new DeleteRequestBuilder(clt).setIndex(hit.getIndex()).setId(hit.getId()).execute();
				} catch (Exception ex) {
					r.error = ex.toString();
				}
			}
		}
		return count;
	}

	/**
	 * Prepare elasticsearch for this source.
	 *
	 * @param source Source to prepare it for.
	 * @deprecated We will use only one indice for now.
	 */
	@Deprecated
	public static void prepare(DBDescSource source) {
		try {
			CreateIndexRequest request = Requests.createIndexRequest(FileIndexer.ES_INDEX_NAME);
			CreateIndexResponse response;
			try (Client clt = client()) {
				response = clt.admin().indices().create(request).actionGet();
			}
			// See http://docs.webingenia.com/elasticsearch/doxygen/classorg_1_1elasticsearch_1_1common_1_1io_1_1stream_1_1StreamOutput.html
			try (BytesStreamOutput bso = new BytesStreamOutput()) {
				response.writeTo(bso);
				LOG.LOG.info("Index response: " + new String(bso.bytes().array()));
			}
		} catch (Exception ex) {
			LOG.LOG.error("Index preparation failed", ex);
		}
	}

	public static void stop() {
		if (node != null) {
			node.stop();
			node = null;
		}
	}

	public static Client client() {
		return node.client();
	}
}
