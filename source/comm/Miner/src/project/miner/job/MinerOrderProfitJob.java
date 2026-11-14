package project.miner.job;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import kernel.util.ThreadUtils;
import kernel.web.Page;
import project.data.DataService;
import project.log.SysLog;
import project.log.SysLogService;
import project.miner.model.MinerOrder;
import project.syspara.SysparaService;

public class MinerOrderProfitJob {
	
	private static final Logger logger = LoggerFactory.getLogger(MinerOrderProfitJob.class);
	
	protected MinerOrderProfitService minerOrderProfitService;
	protected SysparaService sysparaService;
	protected DataService dataService;
	protected SysLogService sysLogService;

	public void taskJob() {

		try {
			for(int i = 0; i < 2;i++) {
				int pageNo = 1;
				int pageSize = 300;
				minerOrderProfitService.cacheRecomProfitClear();
				String miner_bonus_parameters = sysparaService.find("miner_bonus_parameters").getValue();
				while (true) {
					Page page = minerOrderProfitService.pagedQueryComputeOrder(pageNo, pageSize);
					List<MinerOrder> minerOrders = page.getElements();
					if (CollectionUtils.isEmpty(minerOrders)) {
						break;
					}
					try {
						this.minerOrderProfitService.saveComputeOrderProfit(minerOrders, miner_bonus_parameters);
					} catch (Throwable e) {
						logger.error("error:", e);
					}
					logger.info("miner profit finished ,count:" + minerOrders.size());
					pageNo++;
				}
				// 用户收益计算完，计算推荐人收益
				minerOrderProfitService.saveRecomProfit();
				
				//处理完后等待10秒
				ThreadUtils.sleep(20000);
			}
		} catch (Throwable e) {
			logger.error("miner profit run fail", e);
		}

	}

	public void handleData(Date systemTime) {
		try {
			int pageNo = 1;
			int pageSize = 300;
			minerOrderProfitService.cacheRecomProfitClear();
			String miner_bonus_parameters = sysparaService.find("miner_bonus_parameters").getValue();
			while (true) {
				Page page = minerOrderProfitService.pagedQueryComputeOrder(pageNo, pageSize);
				List<MinerOrder> minerOrders = page.getElements();
				if (CollectionUtils.isEmpty(minerOrders)) {// 分页没数据时表示已经计算结束
					break;
				}
				try {
					this.minerOrderProfitService.saveComputeOrderProfit(minerOrders, miner_bonus_parameters, systemTime);
				} catch (Throwable e) {
					logger.error("error:", e);
				}
				logger.info("miner profit finished ,count:" + minerOrders.size());
				pageNo++;
			}
			// 用户收益计算完，计算推荐人收益
			minerOrderProfitService.saveRecomProfit(systemTime);
		} catch (Throwable e) {
			logger.error("MinerOrderProfitJob run fail e:", e);
			SysLog entity = new SysLog();
			entity.setLevel(SysLog.level_error);
			entity.setCreateTime(new Date());
			entity.setLog("MinerOrderProfitJob 矿机任务 执行失败  e:"+e);
			sysLogService.saveAsyn(entity);
		}
	}
	
	public void setMinerOrderProfitService(MinerOrderProfitService minerOrderProfitService) {
		this.minerOrderProfitService = minerOrderProfitService;
	}

	public void setSysparaService(SysparaService sysparaService) {
		this.sysparaService = sysparaService;
	}

	public void setDataService(DataService dataService) {
		this.dataService = dataService;
	}

	public void setSysLogService(SysLogService sysLogService) {
		this.sysLogService = sysLogService;
	}

}
