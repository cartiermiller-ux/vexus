package project.data.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import db.DBBackupLock;
import db.util.BackupUtil;
import project.bonus.job.RecharegeBonus1DayJob;
import project.c2c.job.C2cStatistics1DayJob;
import project.data.WithdrawLimitAmountr1DayJob;
import project.data.job.CleanDataJob;
import project.finance.job.FinanceOrder1DayJob;
import project.finance.job.FinanceOrderCreateRecomJob;
import project.futures.job.FuturesOrderCreateRecomJob;
import project.log.SysLogService;
import project.miner.job.MinerOrderProfitJob;
import project.monitor.activity.job.ActivityOrderTaskJobHandle;
import project.syspara.Syspara;
import project.syspara.SysparaService;

public class TaskJobHandle {

	private static final Logger logger = LoggerFactory.getLogger(TaskJobHandle.class);

	protected CleanDataJob cleanDataJob;
	
	protected MinerOrderProfitJob minerOrderProfitJob;
	protected FinanceOrder1DayJob financeOrder1DayJob;
	protected RecharegeBonus1DayJob recharegeBonus1DayJob;
	
//	protected UserDataHoldingMoneyJob userDataHoldingMoneyJob;
	protected SysLogService sysLogService;
	protected SysparaService sysparaService;
	protected FinanceOrderCreateRecomJob financeOrderCreateRecomJob;
	protected FuturesOrderCreateRecomJob futuresOrderCreateRecomJob;
	protected ActivityOrderTaskJobHandle activityOrderTaskJobHandle;

	protected WithdrawLimitAmountr1DayJob withdrawLimitAmountr1DayJob;
	
	protected C2cStatistics1DayJob c2cStatistics1DayJob;
	
	public void newTaskJob() {
		/**
		 * 16点定时任务
		 */
		try {
			logger.error("newTaskJobHandle backup 发起调用 start...........");
			// 每日备份数据库
			BackupUtil.backup(sysLogService,sysparaService);
			logger.error("newTaskJobHandle backup 调用结束 end...........");
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			logger.error("TaskJobHandle fail e:",e);
		}
	}
	
	public void taskJob() {
		/**
		 * 凌晨4点主定时任务
		 */
		try {
			DBBackupLock.add(DBBackupLock.ALL_DB_LOCK);
			
			activityOrderTaskJobHandle.taskJob();
			
			// 理财每日计息
			financeOrder1DayJob.taskJob();
			
			// 矿机每日计息
			minerOrderProfitJob.taskJob();
			
			// 每日定时返佣前一天的 充值,为空则 上级不 返佣
			// 是否开启每日定时任务返佣，为空则不开启 0.5% 0.3% 0.2% = 0.005,0.003,0.002
			Syspara dailyRechargeRecom = this.sysparaService.find("daily_recharge_recom");
			if(null != dailyRechargeRecom && !"".equals(dailyRechargeRecom.getValue())) {
				recharegeBonus1DayJob.taskJob();
			}
			
			/**
			 * 每日定时任务是否重置提现限制流水，1重置，2不重置
			 */
			String withdraw_reset_daily = this.sysparaService.find("withdraw_reset_daily").getValue();
			/**
			 * 用户提现限制流水重置时间(1为凌晨4点，2为凌晨0点)
			 */
			String withdraw_reset_daily_time = this.sysparaService.find("withdraw_reset_daily_time").getValue();
			/**
			 *	是否重置当日提现限制流水
			 */
			if("1".equals(withdraw_reset_daily) && "1".equals(withdraw_reset_daily_time)) {
				withdrawLimitAmountr1DayJob.taskJob();
			}
			
			// 理财计算前一日购买奖励金额
			financeOrderCreateRecomJob.taskJob();

			// 交割计算前一日购买推荐奖励
			futuresOrderCreateRecomJob.taskJob();
			
			// 删除和重置分时和K线数据
			cleanDataJob.taskJob();
			
			// C2C统计数据
			c2cStatistics1DayJob.taskJob();
			
			logger.error("TaskJobHandle backup 发起调用 start...........");
			// 每日备份数据库
			BackupUtil.backup(sysLogService,sysparaService);
			logger.error("TaskJobHandle backup 调用结束 end...........");
			
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			logger.error("TaskJobHandle fail e:",e);
		}finally {
			DBBackupLock.remove(DBBackupLock.ALL_DB_LOCK);
		}
	}

	public void setCleanDataJob(CleanDataJob cleanDataJob) {
		this.cleanDataJob = cleanDataJob;
	}

//	public void setUserDataHoldingMoneyJob(UserDataHoldingMoneyJob userDataHoldingMoneyJob) {
//		this.userDataHoldingMoneyJob = userDataHoldingMoneyJob;
//	}

	public void setSysLogService(SysLogService sysLogService) {
		this.sysLogService = sysLogService;
	}

	public void setSysparaService(SysparaService sysparaService) {
		this.sysparaService = sysparaService;
	}


	public void setActivityOrderTaskJobHandle(ActivityOrderTaskJobHandle activityOrderTaskJobHandle) {
		this.activityOrderTaskJobHandle = activityOrderTaskJobHandle;
	}

	public void setMinerOrderProfitJob(MinerOrderProfitJob minerOrderProfitJob) {
		this.minerOrderProfitJob = minerOrderProfitJob;
	}

	public void setFinanceOrder1DayJob(FinanceOrder1DayJob financeOrder1DayJob) {
		this.financeOrder1DayJob = financeOrder1DayJob;
	}

	public void setFinanceOrderCreateRecomJob(FinanceOrderCreateRecomJob financeOrderCreateRecomJob) {
		this.financeOrderCreateRecomJob = financeOrderCreateRecomJob;
	}

	public void setFuturesOrderCreateRecomJob(FuturesOrderCreateRecomJob futuresOrderCreateRecomJob) {
		this.futuresOrderCreateRecomJob = futuresOrderCreateRecomJob;
	}

	public void setRecharegeBonus1DayJob(RecharegeBonus1DayJob recharegeBonus1DayJob) {
		this.recharegeBonus1DayJob = recharegeBonus1DayJob;
	}

	public void setWithdrawLimitAmountr1DayJob(WithdrawLimitAmountr1DayJob withdrawLimitAmountr1DayJob) {
		this.withdrawLimitAmountr1DayJob = withdrawLimitAmountr1DayJob;
	}

	public void setC2cStatistics1DayJob(C2cStatistics1DayJob c2cStatistics1DayJob) {
		this.c2cStatistics1DayJob = c2cStatistics1DayJob;
	}
	
}
