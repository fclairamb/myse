package com.webingenia.myse.embeddedes;

import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;

public class EmbeddedElasticSearch {

	private static Node node;

	public static void start() {

		ImmutableSettings.Builder settings = ImmutableSettings.settingsBuilder();

		settings.put("node.name", "localCluster");
		settings.put("path.data", "esData");
		settings.put("http.enabled", true);

		node = NodeBuilder.nodeBuilder()
				.settings(settings)
				.clusterName("localCluster")
				.data(true).local(true).node();

	}

	public static void stop() {
		node.stop();
	}
}
