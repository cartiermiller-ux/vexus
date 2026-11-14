package project.monitor.telegram.sender;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import kernel.util.ThreadUtils;
import project.monitor.telegram.TelegramMessageService;

/**
 * 短信服务类，负责从短信消息队列取出短信消息并发送
 */
public class TelegramServer implements InitializingBean, Runnable {

	private static Logger logger = LoggerFactory.getLogger(TelegramServer.class);
	
	private TelegramMessageService telegramMessageService;

	/**
	 */
	public void run() {
		while (true) {

			try {
				TelegramMessage item = TelegramMessageQueue.poll();

				if (item != null) {

					telegramMessageService.send(item.getText(),item.getParse_mode());
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


	public void afterPropertiesSet() throws Exception {

		new Thread(this, "SmsbaoServer").start();
		if (logger.isInfoEnabled()) {
			logger.info("启动短信(Smsbao)服务！");
		}

	}



}
