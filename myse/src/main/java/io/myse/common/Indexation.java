package io.myse.common;

import io.myse.access.Source;
import static io.myse.common.LOG.LOG;
import io.myse.db.model.DBDescSource;
import io.myse.embeddedes.ElasticSearch;
import io.myse.exploration.FileAnalyser;

public class Indexation {

	public static void start(DBDescSource dbSource) {
		if (dbSource.getId() == 0) {
			LOG.error("You started an invalid dbSource (id==0)");
			return;
		}
		ElasticSearch.prepare(dbSource);
		Source source = Source.get(dbSource);
		
		// Launch exploration
		Tasks.schedule(source.getExplorer());

		// Launch indexation
		if (source.launchAnalyser()) {
			Tasks.schedule(new FileAnalyser(dbSource.getId()));
		}
	}
}
