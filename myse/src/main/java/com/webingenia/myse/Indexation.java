package com.webingenia.myse;

import com.webingenia.myse.access.Source;
import com.webingenia.myse.db.model.DBDescSource;
import com.webingenia.myse.embeddedes.ElasticSearch;
import com.webingenia.myse.exploration.FileIndexer;
import com.webingenia.myse.tasks.Tasks;
import java.util.concurrent.TimeUnit;

public class Indexation {

	public static void start(DBDescSource dbSource) {
		ElasticSearch.prepare(dbSource);
		Source source = Source.get(dbSource);
		Tasks.scheduleWithFixedDelay(source.getExplorer(), 0, 10, TimeUnit.SECONDS);
		Tasks.scheduleWithFixedDelay(new FileIndexer(dbSource.getId()), 5, 30, TimeUnit.SECONDS);
	}
}
