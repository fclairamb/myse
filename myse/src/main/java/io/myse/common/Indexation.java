package io.myse.common;

import io.myse.access.Source;
import static io.myse.common.LOG.LOG;
import io.myse.db.model.DBDescSource;
import io.myse.embeddedes.ElasticSearch;
import io.myse.exploration.FileIndexer;
import java.util.concurrent.TimeUnit;

public class Indexation {

	public static void start(DBDescSource dbSource) {
		if (dbSource.getId() == 0) {
			LOG.error("You started an invalid dbSource (id==0)");
			return;
		}
		ElasticSearch.prepare(dbSource);
		Source source = Source.get(dbSource);
		Tasks.scheduleWithFixedDelay(source.getExplorer(), 0, 10, TimeUnit.SECONDS);
		Tasks.scheduleWithFixedDelay(new FileIndexer(dbSource.getId()), 5, 30, TimeUnit.SECONDS);
	}
}
