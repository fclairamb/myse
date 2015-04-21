package com.webingenia.myse;

import com.webingenia.myse.access.Source;
import com.webingenia.myse.embeddedes.ElasticSearch;
import com.webingenia.myse.exploration.DirExplorer;
import com.webingenia.myse.exploration.FileIndexer;
import com.webingenia.myse.tasks.Tasks;
import java.util.concurrent.TimeUnit;

public class Indexation {

	public static void start(Source source) {
		ElasticSearch.prepare(source.getDesc());
		Tasks.scheduleWithFixedDelay(new DirExplorer(source), 0, 30, TimeUnit.SECONDS);
		Tasks.scheduleWithFixedDelay(new FileIndexer(source), 5, 30, TimeUnit.SECONDS);
	}
}
