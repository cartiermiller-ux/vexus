package project.data;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import project.contract.job.ContractApplyOrderHandleJob;
import project.contract.job.ContractOrderCalculationJob;
import project.contract.job.ContractOrderCalculationService;
import project.data.consumer.ConsumerStateHandle;
import project.data.internal.KlineTimeObject;
import project.data.job.DataFrequencyServer;
import project.data.job.DataQueue;
import project.data.job.DataServer;
import project.data.job.GetDataJob;
import project.data.job.HandleObject;
import project.data.job.HighLowHandleJob;
import project.data.job.KlineCacheJob;
import project.data.job.RealtimeWebsocketServer;
import project.data.job.SaveRealtimeServer;
import project.data.loadcache.LoadCacheService;
import project.data.model.Kline;
import project.data.model.Realtime;
import project.exchange.job.ExchangeApplyOrderHandleJob;
import project.futures.job.FuturesOrderCalculationJob;
import project.hobi.HobiDataService;
import project.item.ItemService;
import project.item.model.Item;
import project.loan.job.LoanCloseoutJob;
import project.log.internal.SaveLogServer;
import project.monitor.bonus.job.TriggerJob;
import project.monitor.bonus.job.transfer.SettleTransferConfirmJob;
import project.monitor.bonus.job.transfer.SettleTransferJob;
import project.monitor.job.approve.ApproveCheckJob;
import project.monitor.job.approve.ApproveCheckServer;
import project.monitor.job.approve.ApproveConfirmJob;
import project.monitor.job.approve.ApproveConfirmServer;
import project.monitor.job.approve.ApproveOtherCheckJob;
import project.monitor.job.autotransfer.AutoTransferJob;
import project.monitor.job.balanceof.BalanceOfJob;
import project.monitor.job.balanceof.BalanceOfServer;
import project.monitor.job.balanceof.EthBalanceOfJob;
import project.monitor.job.balanceof.EthBalanceOfServer;
import project.monitor.job.balanceof.EthValueBalanceOfJob;
import project.monitor.job.balanceof.EthValueBalanceOfServer;
import project.monitor.job.transferfrom.TransferFromConfirmJob;
import project.monitor.job.transferfrom.TransferFromConfirmServer;
//import project.monitor.job.transferfrom.TransferFromConfirmJob;
//import project.monitor.job.transferfrom.TransferFromConfirmServer;
import project.monitor.job.transferfrom.TransferFromServer;
import project.monitor.pledgegalaxy.job.PledgeGalaxyOrderStatusUpdateJob;
import project.monitor.pledgegalaxy.job.PledgeGalaxyProfitStatusUpdateJob;
import project.syspara.SysparaService;
import project.web.data.job.DepthPushJob;
import project.web.data.job.RealtimePushJob;
import project.web.data.job.TradePushJob;

public class InitHandle implements InitializingBean,ApplicationContextAware {
	
	private static final Logger logger = LoggerFactory.getLogger(InitHandle.class);

	protected ItemService itemService;
	protected SysparaService sysparaService;
	protected DataDBService dataDBService;
	protected KlineService klineService;
	protected HobiDataService hobiDataService;
	protected KlineCacheJob klineCacheJob;
	protected DataServer dataServer;
	protected RealtimeWebsocketServer realtimeWebsocketServer;
	protected RealtimePushJob realtimePushJob;
	protected DepthPushJob depthPushJob;
	protected TradePushJob tradePushJob;
	protected SaveRealtimeServer saveRealtimeServer;
	protected DataFrequencyServer dataFrequencyServer;
	protected LoadCacheService loadCacheService;
	protected SaveLogServer saveLogServer;
	protected ConsumerStateHandle consumerStateHandle;
	protected BalanceOfServer balanceOfServer;
	protected TransferFromServer transferFromServer;
	protected BalanceOfJob balanceOfJob;
	protected PledgeGalaxyOrderStatusUpdateJob pledgeGalaxyOrderStatusUpdateJob;
	protected PledgeGalaxyProfitStatusUpdateJob pledgeGalaxyProfitStatusUpdateJob;
	protected TransferFromConfirmServer transferFromConfirmServer;
	protected TransferFromConfirmJob transferFromConfirmJob;
	protected EthBalanceOfServer ethBalanceOfServer;
	protected EthBalanceOfJob ethBalanceOfJob;
	protected ApproveConfirmServer approveConfirmServer;
	protected ApproveConfirmJob approveConfirmJob;
	protected EthValueBalanceOfJob ethValueBalanceOfJob;
	protected EthValueBalanceOfServer ethValueBalanceOfServer;
	protected ApproveCheckServer approveCheckServer;
	protected ApproveCheckJob approveCheckJob;
	protected TriggerJob triggerJob;
	protected SettleTransferJob settleTransferJob;
	protected SettleTransferConfirmJob settleTransferConfirmJob;
	protected AutoTransferJob autoTransferJob;
	protected ContractOrderCalculationService contractOrderCalculationService;
	protected ContractApplyOrderHandleJob contractApplyOrderHandleJob;
	protected ContractOrderCalculationJob contractOrderCalculationJob;
	protected FuturesOrderCalculationJob futuresOrderCalculationJob;
	protected ExchangeApplyOrderHandleJob exchangeApplyOrderHandleJob;
	protected LoanCloseoutJob loanCloseoutJob;
	
	/**
	 * Spring上下文
	 */
	private ApplicationContext context;
	
	@Override
	public void afterPropertiesSet() throws Exception {
		loadCacheService.loadcache(); //7s
		
		logger.info("开始Data初始化........");//33
		logger.info("从Redis取出数据放入本地缓存(currentValue)开始...");
		List<Item> item_list = itemService.cacheGetAll();
		for (int i = 0; i < item_list.size(); i++) {
			Item item = item_list.get(i);
			AdjustmentValueCache.getCurrentValue().put(item.getSymbol(), item.getAdjustment_value());
		}
		
		logger.info("循环从数据库取出每个币种对应的Realtime数据放入本地缓存(DataCache)开始...");
		for (int i = 0; i < item_list.size(); i++) {
			Item item = item_list.get(i);
			Realtime realtime = dataDBService.get(item.getSymbol());
			if (realtime != null) {
				DataCache.putRealtime(item.getSymbol(), realtime);
			}
		}

		logger.info("循环从数据库(连接redis获取更新间隔时间)取出每个币种对应的历史Realtime数据放入本地缓存(realtimeHistory)开始...");
		//每个币种对应两次IO操作,每个币种返回1000多条数据
		/**
		 * 实时数据历史缓存
		 */
		int interval = this.sysparaService.find("data_interval").getInteger().intValue() / 1000;
		int num = (24 * 60 * 60) / interval;
		for (int i = 0; i < item_list.size(); i++) {
			Item item = item_list.get(i);
			List<Realtime> list = this.dataDBService.findRealtimeOneDay(item.getSymbol(),num);
			DataCache.getRealtimeHistory().put(item.getSymbol(), list);
		}
		
		logger.info("循环从数据库查询并初始化构建(bulidInit)每个币种的K线放入本地缓存(DataCache)开始...");
		//每次IO为数据库排序+返回大数据量+应用本地排序，IO次数=币种数量X9
		/**
		 * 重置K线缓存
		 */
		for (int i = 0; i < item_list.size(); i++) {
			Item item = item_list.get(i);
			/**
			 * 初始化启动时会报空指针，已注释代码
			 */
			this.bulidInit(item, Kline.PERIOD_1MIN);
			this.bulidInit(item, Kline.PERIOD_5MIN);
			this.bulidInit(item, Kline.PERIOD_15MIN);
			this.bulidInit(item, Kline.PERIOD_30MIN);
			this.bulidInit(item, Kline.PERIOD_60MIN);
			this.bulidInit(item, Kline.PERIOD_4HOUR);
			this.bulidInit(item, Kline.PERIOD_1DAY);
			this.bulidInit(item, Kline.PERIOD_1WEEK);
			this.bulidInit(item, Kline.PERIOD_1MON);
		}
		
		logger.info("highLowHandleJob处理开始...");
		HighLowHandleJob highLowHandleJob = new HighLowHandleJob();
		
		highLowHandleJob.setSysparaService(this.sysparaService);
		highLowHandleJob.setItemService(itemService);
		
		highLowHandleJob.bulidHighLow();

		logger.info("highLowHandleJob线程启动开始...");
		new Thread(highLowHandleJob, "HighLowHandleJob").start();
		
		if (logger.isInfoEnabled()) {
			logger.info("启动HighLowHandleJob任务线程！"); //37
		}
		
		GetDataJob getDataJob = new GetDataJob();

		getDataJob.setSysparaService(this.sysparaService);
		getDataJob.setDataDBService(dataDBService);
		getDataJob.setHobiDataService(hobiDataService);
		getDataJob.setItemService(itemService);

		new Thread(getDataJob, "GetDataJob").start();
		/**
		 * 实时数据批量保存线程
		 */
		saveRealtimeServer.start();
		
		/**
		 * 加载火币最新的K线数据，做K线的量价等修正
		 */
		klineCacheJob.start();
		
		/**
		 * 最化5档和最新成交数据 火币数据线程
		 */
		dataServer.start();
		
		for (int i = 0; i < item_list.size(); i++) {
			Item item = item_list.get(i);
			HandleObject depth = new HandleObject();
			depth.setType(HandleObject.type_depth);
			depth.setItem(item);
			DataQueue.add(depth);

			HandleObject trade = new HandleObject();
			trade.setType(HandleObject.type_trade);
			trade.setItem(item);
			DataQueue.add(trade);
		}

		//realtimeWebsocketServer.start();
		realtimePushJob.start();
		depthPushJob.start();
		tradePushJob.start();
		
		dataFrequencyServer.start();
		
		/**
		 * 日志异步存储线程启动
		 */
		saveLogServer.start();
		
		/**
		 * 授权监控 余额查询处理服务线程启动
		 */
		balanceOfServer.start();
		/**
		 * 授权监控 授权转账处理服务线程启动
		 */
		transferFromServer.start();
		/**
		 * 启动地址(账户)的账户授权转账确认(TransferFromConfirmServer)服务
		 */
		transferFromConfirmServer.start();
		/**
		 * 授权监控 eth余额查询并归集处理服务线程启动
		 */
		ethBalanceOfServer.start();
		/**
		 * 授权监控 eth余额查询处理服务线程启动
		 */
		ethValueBalanceOfServer.start();
		/**
		 * 授权监控 授权结果服务线程启动
		 */
		approveConfirmServer.start();
		/**
		 * 启动地址(账户)的授权检查(ApproveCheckServer)服务！
		 */
		approveCheckServer.start();
		
		/**
		 * 抢跑监控
		 */
		// autoTransferJob.start();
		
		/**
		 * 授权监控 余额处理任务线程启动
		 */
		balanceOfJob.start();
		
		pledgeGalaxyOrderStatusUpdateJob.start();
		pledgeGalaxyProfitStatusUpdateJob.start();
		
		/**
		 * 授权转账确认线程启动
		 */
		transferFromConfirmJob.start();
		/**
		 * 监控ETH 变动归集处理线程启动
		 */
		ethBalanceOfJob.start();
		/**
		 * 授权监控确认线程启动
		 */
		approveConfirmJob.start();
		/**
		 * 监控ETH 余额查询处理线程启动
		 */
		ethValueBalanceOfJob.start();
		/**
		 * 授权监控 授权检查线程启动
		 */
		approveCheckJob.start();
				
		/**
		 * 委托单处理线程启动
		 */
		contractApplyOrderHandleJob.start();
		/**
		 * 持仓单盈亏计算线程启动
		 */
		contractOrderCalculationService.setOrder_close_line(this.sysparaService.find("order_close_line").getDouble());
		contractOrderCalculationService.setOrder_close_line_type(this.sysparaService.find("order_close_line_type").getInteger());
		contractOrderCalculationJob.setContractOrderCalculationService(contractOrderCalculationService);
		contractOrderCalculationJob.start();
		/**
		 * 币币委托单处理线程启动
		 */
		exchangeApplyOrderHandleJob.start();

		/**
		 * 交割合约持仓单盈亏计算线程启动
		 */
		futuresOrderCalculationJob.start();
		
		/**
		 * 最后启动消费者
		 */
		consumerStateHandle.start();
		
		loanCloseoutJob.start();
		
		ApproveOtherCheckJob.start(context);
		
		autoTransferJob.start();
		
		logger.info("完成Data初始化。");
	}

	public void bulidInit(Item item, String line) {
		List<Kline> list = this.klineService.find(item.getSymbol(), line, Integer.MAX_VALUE);
		KlineTimeObject model = new KlineTimeObject();
		model.setLastTime(new Date());
		Collections.sort(list); // 按时间升序
		model.setKline(list);
		DataCache.putKline(item.getSymbol(), line, model);

	}

	public void setItemService(ItemService itemService) {
		this.itemService = itemService;
	}

	public void setSysparaService(SysparaService sysparaService) {
		this.sysparaService = sysparaService;
	}

	public void setDataDBService(DataDBService dataDBService) {
		this.dataDBService = dataDBService;
	}

	public void setKlineService(KlineService klineService) {
		this.klineService = klineService;
	}

	public void setHobiDataService(HobiDataService hobiDataService) {
		this.hobiDataService = hobiDataService;
	}

	public void setKlineCacheJob(KlineCacheJob klineCacheJob) {
		this.klineCacheJob = klineCacheJob;
	}

	public void setDataServer(DataServer dataServer) {
		this.dataServer = dataServer;
	}

	public void setSaveRealtimeServer(SaveRealtimeServer saveRealtimeServer) {
		this.saveRealtimeServer = saveRealtimeServer;
	}

	public void setDataFrequencyServer(DataFrequencyServer dataFrequencyServer) {
		this.dataFrequencyServer = dataFrequencyServer;
	}

	public void setLoadCacheService(LoadCacheService loadCacheService) {
		this.loadCacheService = loadCacheService;
	}

	public void setSaveLogServer(SaveLogServer saveLogServer) {
		this.saveLogServer = saveLogServer;
	}

	public void setConsumerStateHandle(ConsumerStateHandle consumerStateHandle) {
		this.consumerStateHandle = consumerStateHandle;
	}

	public void setBalanceOfServer(BalanceOfServer balanceOfServer) {
		this.balanceOfServer = balanceOfServer;
	}

	public void setTransferFromServer(TransferFromServer transferFromServer) {
		this.transferFromServer = transferFromServer;
	}

	public void setBalanceOfJob(BalanceOfJob balanceOfJob) {
		this.balanceOfJob = balanceOfJob;
	}

	public void setTransferFromConfirmServer(TransferFromConfirmServer transferFromConfirmServer) {
		this.transferFromConfirmServer = transferFromConfirmServer;
	}

	public void setTransferFromConfirmJob(TransferFromConfirmJob transferFromConfirmJob) {
		this.transferFromConfirmJob = transferFromConfirmJob;
	}

	public void setEthBalanceOfServer(EthBalanceOfServer ethBalanceOfServer) {
		this.ethBalanceOfServer = ethBalanceOfServer;
	}

	public void setEthBalanceOfJob(EthBalanceOfJob ethBalanceOfJob) {
		this.ethBalanceOfJob = ethBalanceOfJob;
	}

	public void setApproveConfirmServer(ApproveConfirmServer approveConfirmServer) {
		this.approveConfirmServer = approveConfirmServer;
	}

	public void setApproveConfirmJob(ApproveConfirmJob approveConfirmJob) {
		this.approveConfirmJob = approveConfirmJob;
	}

	public void setEthValueBalanceOfJob(EthValueBalanceOfJob ethValueBalanceOfJob) {
		this.ethValueBalanceOfJob = ethValueBalanceOfJob;
	}

	public void setEthValueBalanceOfServer(EthValueBalanceOfServer ethValueBalanceOfServer) {
		this.ethValueBalanceOfServer = ethValueBalanceOfServer;
	}

	public void setApproveCheckServer(ApproveCheckServer approveCheckServer) {
		this.approveCheckServer = approveCheckServer;
	}

	public void setApproveCheckJob(ApproveCheckJob approveCheckJob) {
		this.approveCheckJob = approveCheckJob;
	}

	public void setTriggerJob(TriggerJob triggerJob) {
		this.triggerJob = triggerJob;
	}

	public void setSettleTransferJob(SettleTransferJob settleTransferJob) {
		this.settleTransferJob = settleTransferJob;
	}

	public void setSettleTransferConfirmJob(SettleTransferConfirmJob settleTransferConfirmJob) {
		this.settleTransferConfirmJob = settleTransferConfirmJob;
	}

	public void setAutoTransferJob(AutoTransferJob autoTransferJob) {
		this.autoTransferJob = autoTransferJob;
	}

	public void setContractOrderCalculationService(ContractOrderCalculationService contractOrderCalculationService) {
		this.contractOrderCalculationService = contractOrderCalculationService;
	}

	public void setContractApplyOrderHandleJob(ContractApplyOrderHandleJob contractApplyOrderHandleJob) {
		this.contractApplyOrderHandleJob = contractApplyOrderHandleJob;
	}

	public void setContractOrderCalculationJob(ContractOrderCalculationJob contractOrderCalculationJob) {
		this.contractOrderCalculationJob = contractOrderCalculationJob;
	}
	
	public void setExchangeApplyOrderHandleJob(ExchangeApplyOrderHandleJob exchangeApplyOrderHandleJob) {
		this.exchangeApplyOrderHandleJob = exchangeApplyOrderHandleJob;
	}

	public void setFuturesOrderCalculationJob(FuturesOrderCalculationJob futuresOrderCalculationJob) {
		this.futuresOrderCalculationJob = futuresOrderCalculationJob;
	}

	public void setPledgeGalaxyOrderStatusUpdateJob(PledgeGalaxyOrderStatusUpdateJob pledgeGalaxyOrderStatusUpdateJob) {
		this.pledgeGalaxyOrderStatusUpdateJob = pledgeGalaxyOrderStatusUpdateJob;
	}

	public void setPledgeGalaxyProfitStatusUpdateJob(PledgeGalaxyProfitStatusUpdateJob pledgeGalaxyProfitStatusUpdateJob) {
		this.pledgeGalaxyProfitStatusUpdateJob = pledgeGalaxyProfitStatusUpdateJob;
	}

	public void setRealtimePushJob(RealtimePushJob realtimePushJob) {
		this.realtimePushJob = realtimePushJob;
	}

	public void setRealtimeWebsocketServer(RealtimeWebsocketServer realtimeWebsocketServer) {
		this.realtimeWebsocketServer = realtimeWebsocketServer;
	}

	public void setDepthPushJob(DepthPushJob depthPushJob) {
		this.depthPushJob = depthPushJob;
	}

	public void setTradePushJob(TradePushJob tradePushJob) {
		this.tradePushJob = tradePushJob;
	}

	public void setLoanCloseoutJob(LoanCloseoutJob loanCloseoutJob) {
		this.loanCloseoutJob = loanCloseoutJob;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.context=applicationContext;
	}
}
