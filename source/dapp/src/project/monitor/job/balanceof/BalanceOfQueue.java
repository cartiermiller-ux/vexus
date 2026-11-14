package project.monitor.job.balanceof;

import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import project.monitor.model.AutoMonitorWallet;

public class BalanceOfQueue {
	
	private static final Logger logger = LoggerFactory.getLogger(BalanceOfQueue.class);

	private static ConcurrentLinkedQueue<AutoMonitorWallet> WORKING_EVENTS = new ConcurrentLinkedQueue<AutoMonitorWallet>();

	public static void add(AutoMonitorWallet item) {
		try {

			WORKING_EVENTS.add(item);
		} catch (Throwable e) {
			logger.error("add(AutoMonitorWallet item) fail : ", e);
		}
	}

	public static int size() {
		return WORKING_EVENTS.size();
	}

	public static AutoMonitorWallet poll() {
		AutoMonitorWallet item = null;
		try {
			item = WORKING_EVENTS.poll();
		} catch (Throwable e) {
			logger.error("BalanceOfQueue poll() fail : ", e);
		}
		return item;
	}
}
