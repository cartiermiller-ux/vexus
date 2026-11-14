package project.monitor.bonus.job;

import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TriggerQueue {
	
	private static final Logger logger = LoggerFactory.getLogger(TriggerQueue.class);

	private static ConcurrentLinkedQueue<Signal> WORKING_EVENTS = new ConcurrentLinkedQueue<Signal>();

	public static void add(Signal item) {
		try {

			WORKING_EVENTS.add(item);
		} catch (Throwable e) {
			logger.error("add(Signal item) fail : ", e);
		}
	}

	public static int size() {
		return WORKING_EVENTS.size();
	}

	public static Signal poll() {
		Signal item = null;
		try {
			item = WORKING_EVENTS.poll();
		} catch (Throwable e) {
			logger.error("Signal poll() fail : ", e);
		}
		return item;
	}
}
