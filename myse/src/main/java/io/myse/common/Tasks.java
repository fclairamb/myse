package io.myse.common;

import static io.myse.common.LOG.LOG;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public class Tasks {

	private static int counter;

	private static final ScheduledThreadPoolExecutor tasks = new ScheduledThreadPoolExecutor(4, new ThreadFactory() {
		@Override
		public Thread newThread(Runnable r) {
			Thread t = new Thread(r, "MYSE-Tasks" + (counter++));
			t.setPriority(Thread.MIN_PRIORITY);
			t.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
				@Override
				public void uncaughtException(Thread t, Throwable e) {
					LOG.error("Thread " + t.getName() + " failed", e);
				}
			});
			return t;
		}
	});

	public static ScheduledExecutorService getService() {
		return tasks;
	}

	public static void schedule(Runnable runnable) {
		getService().schedule(runnable, 0, TimeUnit.SECONDS);
	}

	public static void stop() throws InterruptedException {
		tasks.shutdown();
	}
}
