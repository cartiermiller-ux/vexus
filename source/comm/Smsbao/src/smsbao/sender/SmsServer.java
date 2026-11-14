package smsbao.sender;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.task.TaskExecutor;

import kernel.util.ThreadUtils;
import smsbao.internal.InternalSmsSenderService;

/**
 * 短信服务类，负责从短信消息队列取出短信消息并发送
 */
public class SmsServer implements InitializingBean, Runnable {

	private static final Logger logger = LoggerFactory.getLogger(SmsServer.class);

	private TaskExecutor taskExecutor;

	private InternalSmsSenderService internalSmsSenderService;

	/**
	 * 服务运行： 1. 从消息队列获取message 2.调用currentProvider发送短信
	 */
	public void run() {
		while (true) {

			try {
				SmsMessage item = SmsMessageQueue.poll();

				if (item != null) {

					taskExecutor.execute(new HandleRunner(item));
				} else {
					/*
					 * 限速，最多1秒2个
					 */
					ThreadUtils.sleep(500);
				}

			} catch (Throwable e) {
				logger.error("SmsServer taskExecutor.execute() fail", e);

			}
		}
	}

	public class HandleRunner implements Runnable {
		private SmsMessage item;

		public HandleRunner(SmsMessage item) {
			this.item = item;
		}

		public void run() {
			try {
				internalSmsSenderService.send(item);
			} catch (Throwable t) {
				logger.error("SmsServer taskExecutor.execute() fail", t);
			}

		}

	}

	public void afterPropertiesSet() throws Exception {

		new Thread(this, "SmsbaoServer").start();
		if (logger.isInfoEnabled()) {
			logger.info("启动短信(Smsbao)服务！");
		}

	}

	public void setTaskExecutor(TaskExecutor taskExecutor) {
		this.taskExecutor = taskExecutor;
	}

	public void setInternalSmsSenderService(InternalSmsSenderService internalSmsSenderService) {
		this.internalSmsSenderService = internalSmsSenderService;
	}

}
