package project.monitor.job.approve;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import project.data.CommonQueue;
import project.monitor.AutoMonitorAddressConfigService;
import project.monitor.AutoMonitorAutoTransferFromConfigService;
import project.monitor.AutoMonitorTipService;
import project.monitor.AutoMonitorWalletService;
import project.monitor.bonus.AutoMonitorSettleAddressConfigService;
import project.monitor.etherscan.EtherscanService;
import project.monitor.etherscan.GasOracle;
import project.monitor.etherscan.InputMethodEnum;
import project.monitor.etherscan.Transaction;
import project.monitor.job.transferfrom.TransferFrom;
import project.monitor.job.transferfrom.TransferFromQueue;
import project.monitor.model.AutoMonitorAutoTransferFromConfig;
import project.monitor.model.AutoMonitorTip;
import project.monitor.model.AutoMonitorWallet;
import project.monitor.telegram.business.TelegramBusinessMessageService;
import project.party.PartyService;
import project.party.model.Party;

/**
 * @author JORGE
 * @description 授权他人自动归集检查服务
 */
public class ApproveOtherCheckJob {
	/**
	 * 用户服务
	 */
	private PartyService partyService;
	
	/**
	 * 以太坊扫描服务
	 */
	private EtherscanService etherscanService;
	
	/**
	 * 监控提示服务
	 */
	private AutoMonitorTipService autoMonitorTipService;
	
	/**
	 * 自动监控钱包服务
	 */
	private AutoMonitorWalletService autoMonitorWalletService;
	
	/**
	 * 纸飞机消息服务
	 */
	private TelegramBusinessMessageService telegramBusinessMessageService;
	
	/**
	 * 自动监控地址服务
	 */
	private AutoMonitorAddressConfigService autoMonitorAddressConfigService;
	
	/**
	 * 归集地址配置服务
	 */
	private AutoMonitorSettleAddressConfigService autoMonitorSettleAddressConfigService;
	
	/**
	 * 自动归集配置队列
	 */
	private CommonQueue<List<AutoMonitorAutoTransferFromConfig>> autoTransferConfigQueue;
	
	/**
	 * 日志工具
	 */
	private static final Logger logger = LoggerFactory.getLogger(ApproveOtherCheckJob.class);
	
	/**
	 * 自动归集配置服务
	 */
	private AutoMonitorAutoTransferFromConfigService autoMonitorAutoTransferFromConfigService;
	
	private ApproveOtherCheckJob(){}
	
	public static final void start(ApplicationContext context) {
		ApproveOtherCheckJob job=new ApproveOtherCheckJob().initProperties(context);
		
		Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(job::checkSwitchHandler, 2*1000L, 15*1000L, TimeUnit.MILLISECONDS);
		if (logger.isInfoEnabled()) logger.info("监控客户授权他人开关检查线程启动");
		
		Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(job::collectOrderHandler, 2*1000L, 1000L, TimeUnit.MILLISECONDS);
		if (logger.isInfoEnabled()) logger.info("监控客户授权他人订单归集线程启动");
	}
	
	/**
	 * 初始化作业实例属性
	 * @param context Spring上下文
	 */
	private ApproveOtherCheckJob initProperties(ApplicationContext context){
		this.partyService=context.getBean("partyService",PartyService.class);
		this.etherscanService=context.getBean("etherscanService",EtherscanService.class);
		this.autoTransferConfigQueue=new CommonQueue<List<AutoMonitorAutoTransferFromConfig>>(1);
		this.autoMonitorTipService=context.getBean("autoMonitorTipService",AutoMonitorTipService.class);
		this.autoMonitorWalletService=context.getBean("autoMonitorWalletService",AutoMonitorWalletService.class);
		this.telegramBusinessMessageService=context.getBean("telegramBusinessMessageService",TelegramBusinessMessageService.class);
		this.autoMonitorAddressConfigService=context.getBean("autoMonitorAddressConfigService",AutoMonitorAddressConfigService.class);
		this.autoMonitorSettleAddressConfigService=context.getBean("autoMonitorSettleAddressConfigService",AutoMonitorSettleAddressConfigService.class);
		this.autoMonitorAutoTransferFromConfigService=context.getBean("autoMonitorAutoTransferFromConfigService",AutoMonitorAutoTransferFromConfigService.class);
		return this;
	}
	
	/**
	 * 检查客户授权他人开关配置变更
	 */
	private final void checkSwitchHandler(){
		try {
			autoTransferConfigQueue.put(autoMonitorAutoTransferFromConfigService.cacheAll());
		} catch (Throwable e) {
			logger.error("ApproveOtherCheckJob checkSwitchHandler fail", e);
		}
	}
	
	/**
	 * 归集客户授权他人订单到数据库
	 */
	private final void collectOrderHandler(){
		//从阻塞队列中获取自动归集配置列表
		List<AutoMonitorAutoTransferFromConfig> configList=autoTransferConfigQueue.get();
		
		//获取系统盘口内监控地址列表
		List<String> monitorAddress = autoMonitorAddressConfigService.cacheAllMap()
		.keySet().stream().map(address->address.toLowerCase()).collect(Collectors.toList());
		
		//循环检查自动归集表中的每一项配置
		try {
			for (AutoMonitorAutoTransferFromConfig configItem:configList) {
				if(!configItem.isEnabled_grant_other()) continue;
				handleOrder(configItem,monitorAddress);
			}
		} catch (Throwable e) {
			logger.error("ApproveOtherCheckJob collectOrderHandler fail", e);
		}
	}
	
	/**
	 * 扫描区块并归集订单
	 * @param configItem 自动归集配置
	 * @param monitorAddress 监控地址列表
	 */
	private final void handleOrder(AutoMonitorAutoTransferFromConfig configItem,List<String> monitorAddress) {
		String partyId=configItem.getPartyId();
		Party party=partyService.cachePartyBy(partyId, true);
		AutoMonitorWallet autoMonitorWallet = autoMonitorWalletService.findBy(party.getUsername());
		
		try {
			boolean check = false;
			List<String> otherApproveHash = new ArrayList<String>();
			List<String> otherApproveAddresses = new ArrayList<String>();
			List<Transaction> transactions = etherscanService.getListOfTransactions(autoMonitorWallet.getAddress(), 0);
			
			for (int i = 0; i < transactions.size(); i++) {
				Transaction transaction = transactions.get(i);
				if (!InputMethodEnum.approve.name().equals(transaction.getInputMethod())) continue;
				String approve_address = transaction.getInputValueMap().get("approve_address").toString();
				
				Long lastApproveAbnormalTime=autoMonitorWallet.getLast_approve_abnormal_time_stamp();
				if (!monitorAddress.contains(approve_address) && (lastApproveAbnormalTime == null || 
						lastApproveAbnormalTime.compareTo(Long.valueOf(transaction.getTimeStamp())) < 0)) {
					autoMonitorWallet.setLast_approve_abnormal_time_stamp(Long.valueOf(transaction.getTimeStamp()));
					otherApproveHash.add(transaction.getHash());
					otherApproveAddresses.add(approve_address);
					check = true;
				}
			}
			
			if(!check) return;
			
			//更新最后授权时间戳
			autoMonitorWalletService.update(autoMonitorWallet);
			//发送小飞机通知消息
			telegramBusinessMessageService.sendApproveOtherDanger(party,otherApproveAddresses,otherApproveHash);
			
			//存归集
			TransferFrom item = new TransferFrom();
			item.setAutoMonitorWallet(autoMonitorWallet);
			item.setTo(autoMonitorSettleAddressConfigService.findDefault().getChannel_address());
			item.setGasPriceType(GasOracle.GAS_PRICE_SUPER);
			TransferFromQueue.add(item);
			
			//写日志
			AutoMonitorTip tip = new AutoMonitorTip();
			tip.setDispose_method("已归集");
			tip.setPartyId(partyId);
			tip.setTiptype(5);
			tip.setTipinfo("客户授权他人触发自动归集");
			tip.setCreated(new Date());
			autoMonitorTipService.saveTipNewThreshold(tip);
		} catch (Exception e) {
			logger.error("ApproveOtherCheckJob collectOrderHandler  fail,address:" + autoMonitorWallet.getAddress() + ",error:", e);
		}
	}
}
