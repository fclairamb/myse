package com.webingenia.myse;

import com.webingenia.myse.access.Source;
import com.webingenia.myse.direxplore.DirExplorer;
import com.webingenia.myse.fileexplore.FileIndexer;
import com.webingenia.myse.tasks.Tasks;
import java.util.concurrent.TimeUnit;

public class Indexation {

	public static void start(Source source) {
		Tasks.getService().scheduleWithFixedDelay(new DirExplorer(source), 0, 3, TimeUnit.MINUTES);
		Tasks.getService().scheduleWithFixedDelay(new FileIndexer(source), 5, 30, TimeUnit.SECONDS);
	}
}
