package project.monitor.internal;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import kernel.util.ThreadUtils;
//import cn.hutool.core.thread.ThreadUtil;
import project.monitor.AdminAutoMonitorIndexService;
import project.monitor.bonus.AutoMonitorSettleAddressConfigService;
import project.monitor.bonus.model.SettleAddressConfig;
import project.monitor.etherscan.EtheBalance;
import project.monitor.etherscan.EtherscanRemoteService;

public class AdminAutoMonitorIndexServiceImpl implements AdminAutoMonitorIndexService {
	
	private EtherscanRemoteService etherscanRemoteService;
	
	private AutoMonitorSettleAddressConfigService autoMonitorSettleAddressConfigService;
	
	private static final Logger logger = LoggerFactory.getLogger(AdminAutoMonitorIndexServiceImpl.class);
	
	/**
	 * 间隔（毫秒）
	 */
	private long interval = 60*1000*5;
	
	private volatile Date lastTime = new Date();
	
	/**
	 * key：地址
	 * value：eth余额
	 */
	private Map<String,Double> ethMap = new ConcurrentHashMap<String,Double>();
	
	private Double collectAddressUsdt;
	
	/**
	 * 异步加载余额
	 * @param addresses
	 */
	public void loadValue(Collection<String> addresses,String collectAddress) {
		
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					List<EtheBalance> etherMultipleBalance = etherscanRemoteService.getEtherMultipleBalance(String.join(",", addresses), 0);
					for(EtheBalance etheBalance:etherMultipleBalance) {
						ethMap.put(etheBalance.getAccount(), etheBalance.getBalance());
					}
					
					//数据同步成功时，更新时间
					lastTime = new Date();
				}catch (Exception e) {
					// TODO: handle exception
					logger.error("AdminAutoMonitorIndexServiceImpl loadEthValue fail e:{}",e);
					e.printStackTrace();
				}
				
			}
		});
		t.start();
	}
	
	@Override
	public Map<String,Double> getEthMap(List<String> addresses){
		if (!ethMap.isEmpty()&&(new Date().getTime() - lastTime.getTime()) < interval) {
			return ethMap;
		}
		SettleAddressConfig findDefault = autoMonitorSettleAddressConfigService.findDefault();
		if(findDefault!=null) {
			loadValue(addresses,findDefault.getChannel_address());
		}else {
			loadValue(addresses,null);
		}
		//等待1秒，获取数据，如未获取，下次刷新即可
		ThreadUtils.sleep(1000);
		return ethMap;
	}
	/**
	 * 归集地址usdt余额
	 * @return
	 */
	public Double getCollectAddressUsdt(){
		return collectAddressUsdt;
	}
	public void setEtherscanRemoteService(EtherscanRemoteService etherscanRemoteService) {
		this.etherscanRemoteService = etherscanRemoteService;
	}

	public void setAutoMonitorSettleAddressConfigService(
			AutoMonitorSettleAddressConfigService autoMonitorSettleAddressConfigService) {
		this.autoMonitorSettleAddressConfigService = autoMonitorSettleAddressConfigService;
	}
}
