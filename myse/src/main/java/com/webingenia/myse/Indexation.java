package com.webingenia.myse;

import com.webingenia.myse.access.Source;
import com.webingenia.myse.direxplore.DirExplorer;
import com.webingenia.myse.fileexplore.FileIndexer;
import com.webingenia.myse.tasks.Tasks;
import java.util.concurrent.TimeUnit;

public class Indexation {

	public static void start(Source source) {
		Tasks.scheduleWithFixedDelay(new DirExplorer(source), 5, 30, TimeUnit.SECONDS);
		Tasks.scheduleWithFixedDelay(new FileIndexer(source), 5, 30, TimeUnit.SECONDS);
	}
}
