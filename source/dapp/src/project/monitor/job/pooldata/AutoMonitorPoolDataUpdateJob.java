package project.monitor.job.pooldata;

import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import kernel.util.Arith;
import kernel.util.JsonUtils;
import project.monitor.AutoMonitorPoolDataService;
import project.monitor.model.AutoMonitorPoolData;
import project.syspara.Syspara;
import project.syspara.SysparaService;
import util.RandomUtil;

/**
 * 矿池产出数据更新定时器
 *
 */
public class AutoMonitorPoolDataUpdateJob {
	
	private static final Logger logger = LoggerFactory.getLogger(AutoMonitorPoolDataUpdateJob.class);
	
	private AutoMonitorPoolDataService autoMonitorPoolDataService;
	
	private SysparaService sysparaService;
	
	public void taskJob() {	
		Syspara syspara = sysparaService.find("project_type");	
		logger.info("矿池产出收益开始执行");
		if(ObjectUtils.isEmpty(syspara)) {
			return;
		}
		if (!syspara.getValue().contains("DAPP_EXCHANGE")) {
			return;
		}
		logger.info("矿池产出收益开始执行项目类型"+syspara.getValue());
		int randomVerifier = RandomUtil.random(35, 65); 
		int randomAmount = RandomUtil.random(1500, 2500);		
		int randomNode = RandomUtil.random(10, 50);  
		AutoMonitorPoolData data = autoMonitorPoolDataService.findDefault();
		Syspara mining_pool_config = sysparaService.find("mining_pool_config");
		if(ObjectUtils.isNotEmpty(mining_pool_config)) {
			try {
				String miningPoolConfig = mining_pool_config.getValue();
				if(!"0".equals(miningPoolConfig)) {
					String[] arr1 = miningPoolConfig.split(",");
					String random[] = arr1[0].split("-");
					randomVerifier = RandomUtil.random(Integer.parseInt(random[0]), Integer.parseInt(random[1]));			
					String random2[] = arr1[1].split("-");
					int user_revenue = RandomUtil.random(Integer.parseInt(random2[0]), Integer.parseInt(random2[1]));
					data.setUser_revenue(Arith.add(data.getUser_revenue(), user_revenue));
					String random3[] = arr1[2].split("-");
					randomAmount = RandomUtil.random(Integer.parseInt(random3[0]), Integer.parseInt(random3[1]));
					String random4[] = arr1[3].split("-");
					randomNode =  RandomUtil.random(Integer.parseInt(random4[0]), Integer.parseInt(random4[1]));
				}
			} catch (Exception e) {
				logger.error("taskJob解析异常",e.getMessage());
			}			
		}		
		data.setTradingSum(Arith.add(data.getTradingSum(), randomAmount));
		data.setVerifier(Arith.add(data.getVerifier(), randomVerifier));
		data.setNode_num(Arith.add(data.getNode_num(), randomNode));
		data.setMining_total(Arith.add(data.getMining_total(), randomAmount));
		
		String noticeRate = sysparaService.find("notice_rate").getValue();
		data.setNotice_logs(JsonUtils.bean2Json(autoMonitorPoolDataService.createNotice(noticeRate)));
		autoMonitorPoolDataService.update(data);
	}

	public AutoMonitorPoolDataService getAutoMonitorPoolDataService() {
		return autoMonitorPoolDataService;
	}

	public void setAutoMonitorPoolDataService(AutoMonitorPoolDataService autoMonitorPoolDataService) {
		this.autoMonitorPoolDataService = autoMonitorPoolDataService;
	}

	public void setSysparaService(SysparaService sysparaService) {
		this.sysparaService = sysparaService;
	}
	
}
