package project.data.consumer;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import project.wallet.consumer.WalletConsumeServer;
import project.wallet.consumer.WalletExtendConsumeServer;

public class ConsumerStateHandle implements Runnable {

	private static final Logger logger = LoggerFactory.getLogger(ConsumerStateHandle.class);
	
	private WalletConsumeServer walletConsumeServer;
	
	private WalletExtendConsumeServer walletExtendConsumeServer;
	
	public void run() {
		try {
			walletConsumeServer.start();
			walletExtendConsumeServer.start();
		} catch (Throwable e) {
			logger.error("ConsumerStateHandle taskExecutor.execute() fail", e);
		}
	}

	public void start() {
		new Thread(this, "ConsumerStateHandle").start();
		if (logger.isInfoEnabled()) {
			logger.info("启动消费者！");
		}

	}

	public void setWalletConsumeServer(WalletConsumeServer walletConsumeServer) {
		this.walletConsumeServer = walletConsumeServer;
	}

	public void setWalletExtendConsumeServer(WalletExtendConsumeServer walletExtendConsumeServer) {
		this.walletExtendConsumeServer = walletExtendConsumeServer;
	}
	
}
