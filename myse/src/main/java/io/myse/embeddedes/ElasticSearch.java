package io.myse.embeddedes;

import io.myse.common.Paths;
import io.myse.db.model.DBDescSource;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.IndicesAdminClient;
import org.elasticsearch.client.Requests;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;

import static io.myse.common.LOG.LOG;
import java.util.ArrayList;
import java.util.List;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.elasticsearch.common.hppc.cursors.ObjectCursor;

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
			LOG.error("createIndex", ex);
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

	public static List<String> listIndexes() {
		List<String> list = new ArrayList<>();
		try (Client esClt = ElasticSearch.client()) {
			ImmutableOpenMap<String, IndexMetaData> indices = esClt.admin().cluster().prepareState().execute().actionGet().getState().getMetaData().indices();
			for (ObjectCursor<IndexMetaData> next : indices.values()) {
				list.add(next.value.index());
			}
		}
		return list;
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
			LOG.error("Index preparation failed", ex);
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
