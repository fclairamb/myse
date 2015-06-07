package io.myse.common;

import java.util.concurrent.TimeUnit;
import static io.myse.common.LOG.LOG;

/**
 * A task that reschedules itself.
 */
public abstract class ReschedulingRunnable implements Runnable {

	/**
	 * Period between calls.
	 */
	protected long delay = 30000;

	/**
	 * If the task is cancelled.
	 */
	protected boolean cancelled;

	/**
	 * Reschedule itself.
	 */
	protected boolean reschedule = true;

	@Override
	public void run() {
		try {
			before();
			if (!cancelled) {
				actualRun();
			}
		} catch (Exception ex) {
			LOG.error(String.format("%s.run", this), ex);
			delay += 1000; // Throwing an exception should delay next execution
		} finally {
			after();
		}
	}

	/**
	 * Called before the actualRun() method.
	 */
	protected void before() {

	}

	/**
	 * Actual running method of the class.
	 *
	 * @throws Exception Exception thrown during the execution.
	 */
	public abstract void actualRun() throws Exception;

	/**
	 * Called after the actualRun() method.
	 */
	protected void after() {
		if (reschedule) {
			// We reschedule ourself
			Tasks.getService().schedule(this, delay, TimeUnit.MILLISECONDS);
		}
	}

	/**
	 * Cancel future executions on the tasks.
	 */
	public void cancel() {
		cancelled = true;
		reschedule = false;
	}

	public void setReschedule(boolean reschedule) {
		this.reschedule = reschedule;
	}

}
