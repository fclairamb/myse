package com.webingenia.myse;

import com.webingenia.myse.access.Source;
import static com.webingenia.myse.common.LOG.LOG;
import com.webingenia.myse.db.model.DBDescSource;
import com.webingenia.myse.embeddedes.ElasticSearch;
import com.webingenia.myse.exploration.FileIndexer;
import com.webingenia.myse.tasks.Tasks;
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
