package io.myse.common;

import java.util.concurrent.TimeUnit;
import static io.myse.common.LOG.LOG;

public abstract class ReschedulingRunnable implements Runnable {

	protected long delay = 30000;
	protected boolean cancelled;

	@Override
	public void run() {
		try {
			if (!cancelled) {
				actualRun();
			}
		} catch (Exception ex) {
			LOG.error(String.format("%s.run", this), ex);
		} finally {
			after();
		}
	}

	protected void before() {

	}

	public abstract void actualRun() throws Exception;

	protected void after() {
		if (!cancelled) {
			Tasks.getService().schedule(this, delay, TimeUnit.MILLISECONDS);
		}
	}

	public void cancel() {
		cancelled = true;
	}

}
