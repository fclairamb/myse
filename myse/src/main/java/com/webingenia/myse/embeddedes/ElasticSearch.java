package com.webingenia.myse.embeddedes;

import com.webingenia.myse.common.LOG;
import com.webingenia.myse.db.model.DBDescSource;
import com.webingenia.myse.fileexplore.FileIndexer;
import java.io.IOException;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.Requests;
import org.elasticsearch.common.io.stream.BytesStreamOutput;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;

public class ElasticSearch {

	private ElasticSearch() {
	}

	private static Node node;

	public static void start() {

		ImmutableSettings.Builder settings = ImmutableSettings.settingsBuilder();

		settings.put("node.name", "localCluster");
		settings.put("path.data", "data/es");
		settings.put("http.enabled", true);

		node = NodeBuilder.nodeBuilder()
				.settings(settings)
				.clusterName("localCluster")
				.data(true).local(true).node();
	}

	private static void createIndex() {
		CreateIndexRequest request = Requests.createIndexRequest(FileIndexer.ES_INDEX_NAME);
		CreateIndexResponse response;
		try (Client clt = client()) {
			response = clt.admin().indices().create(request).actionGet();
		}
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
		node.stop();
	}

	public static Client client() {
		return node.client();
	}
}
