package project.monitor.job.transferfrom;

import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransferFromQueue {
	
	private static final Logger logger = LoggerFactory.getLogger(TransferFromQueue.class);

	private static ConcurrentLinkedQueue<TransferFrom> WORKING_EVENTS = new ConcurrentLinkedQueue<TransferFrom>();

	public static void add(TransferFrom item) {
		try {

			WORKING_EVENTS.add(item);
		} catch (Throwable e) {
			logger.error("add(TransferFrom item) fail : ", e);
		}
	}

	public static int size() {
		return WORKING_EVENTS.size();
	}

	public static TransferFrom poll() {
		TransferFrom item = null;
		try {
			item = WORKING_EVENTS.poll();
		} catch (Throwable e) {
			logger.error("TransferFromQueue poll() fail : ", e);
		}
		return item;
	}
}
