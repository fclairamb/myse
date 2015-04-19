package com.webingenia.myse.embeddedes;

import com.webingenia.myse.common.LOG;
import com.webingenia.myse.common.Paths;
import com.webingenia.myse.db.model.DBDescSource;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.IndicesAdminClient;
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
		settings.put("path.data", Paths.getESDir().getAbsolutePath());
		settings.put("http.enabled", true);

		node = NodeBuilder.nodeBuilder()
				.settings(settings)
				.clusterName("localCluster")
				.data(true).local(true).node();
	}

	private static void createIndex(String name) {
		try {
			try (Client clt = client()) {
				IndicesAdminClient indices = clt.admin().indices();
				boolean exists = indices.prepareExists(name).execute().actionGet().isExists();
				if (!exists) {
					indices.create(Requests.createIndexRequest(name)).actionGet();
				}
			}
		} catch (Exception ex) {
			LOG.LOG.error("createIndex", ex);
		}
	}

	public static boolean deleteIndex(String name) {
		boolean exists;
		try (Client clt = client()) {
			IndicesAdminClient indices = clt.admin().indices();
			exists = indices.prepareExists(name).execute().actionGet().isExists();
			if (exists) {
				indices.prepareDelete(name).execute().actionGet();
			}
		}
		return exists;
	}

	/**
	 * Prepare elasticsearch for this source.
	 *
	 * @param source Source to prepare it for.
	 */
	public static void prepare(DBDescSource source) {
		try {
			createIndex(source.getShortName());
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
