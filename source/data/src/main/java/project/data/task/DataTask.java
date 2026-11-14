package project.data.task;

import db.job.BackupJob;
import kernel.web.ApplicationUtil;
import project.c2c.job.C2cExpire1MinuteJob;
import project.data.klinejob.Kline15MinuteJob;
import project.data.klinejob.Kline1DayJob;
import project.data.klinejob.Kline1MinuteJob;
import project.data.klinejob.Kline1MonJob;
import project.data.klinejob.Kline1WeekJob;
import project.data.klinejob.Kline30MinuteJob;
import project.data.klinejob.Kline4HourJob;
import project.data.klinejob.Kline5MinuteJob;
import project.data.klinejob.Kline60MinuteJob;
import project.loan.job.LoanInterestJob;
import project.monitor.job.pooldata.AutoMonitorPoolDataUpdateJob;
import project.monitor.mining.job.MiningTaskJobHandle;
import project.monitor.pledge.job.PledgeTaskJobHandle;
import project.monitor.pledgegalaxy.job.PledgeGalaxyProfitCreateJob;
import project.monitor.pledgegalaxy.job.PledgeGalaxyTeamProfitCreateJob;
import project.monitor.telegram.job.TelegramMessageTaskJobHandle;
import project.wallet.job.WalletIncomeJob;

public class DataTask {
	
	private TaskJobHandle taskJobHandle;
	
	private BackupJob backupTask_60_minute;
	
	private WalletIncomeJob  walletIncomeJob;
	
	private MiningTaskJobHandle miningTaskJobHandle;
	
	private TelegramMessageTaskJobHandle telegramMessageTaskJobHandle;
	
	private PledgeTaskJobHandle pledgeTaskJobHandle;
	
	private PledgeGalaxyProfitCreateJob pledgeGalaxyProfitCreateJob;
	private PledgeGalaxyTeamProfitCreateJob pledgeGalaxyTeamProfitCreateJob;
	private AutoMonitorPoolDataUpdateJob autoMonitorPoolDataUpdateJob;
	private LoanInterestJob loanInterestJob;
	
	private Kline1MinuteJob klineTask_1_minute;
	private Kline5MinuteJob klineTask_5_minute;
	private Kline15MinuteJob klineTask_15_minute;
	private Kline30MinuteJob klineTask_30_minute;
	private Kline60MinuteJob klineTask_60_minute;
	private Kline4HourJob klineTask_4_hour;
	private Kline1DayJob klineTask_1_day;
	private Kline1WeekJob klineTask_1_week;
	private Kline1MonJob klineTask_1_month;

	private C2cExpire1MinuteJob c2cExpireTask_1_minute;
	
	
	public void c2cExpireTask1minute() {
		c2cExpireTask_1_minute.taskJob();
	}
	
	public void klineTask1minute() {
		klineTask_1_minute.taskJob();
	}
	
	public void klineTask5minute() {
		klineTask_5_minute.taskJob();
	}
	
	public void klineTask15minute() {
		klineTask_15_minute.taskJob();
	}
	
	public void klineTask30minute() {
		klineTask_30_minute.taskJob();
	}
	
	public void klineTask60minute() {
		klineTask_60_minute.taskJob();
	}
	
	public void klineTask4hour() {
		klineTask_4_hour.taskJob();
	}
	
	public void klineTask1day() {
		klineTask_1_day.taskJob();
	}
	
	public void klineTask1week() {
		klineTask_1_week.taskJob();
	}
	
	public void klineTask1month() {
		klineTask_1_month.taskJob();
	}
	
	public void jobHandle() {
		taskJobHandle.taskJob();
	}
	
	public void newJobHandle() {
		if(ApplicationUtil.isWindows()) return;
		taskJobHandle.newTaskJob();
	}
	
	public void backupDB60minute() {
		if(ApplicationUtil.isWindows()) return;
		backupTask_60_minute.taskJob();
	}

	
	public void walletIncomeJob() {
		walletIncomeJob.walletIncomeJob();
	}
	
	/**
	 * 矿池收益定时器
	 */
	public void miningJobHandle() {
		miningTaskJobHandle.taskJob();
	}
	
	public void telegramMessageTask() {
		telegramMessageTaskJobHandle.taskJob();
	}

	public void pledgeTask() {
		pledgeTaskJobHandle.taskJob();
	}
	
	public void pledgeGalaxyProfitCreateTask() {
		pledgeGalaxyProfitCreateJob.taskJob();
	}
	
	public void pledgeGalaxyTeamProfitCreateTask() {
		pledgeGalaxyTeamProfitCreateJob.taskJob();
	}
	
	public void autoMonitorPoolDataUpdateTask() {
		autoMonitorPoolDataUpdateJob.taskJob();
	}
	
	public void loanInterestTask() {
		loanInterestJob.taskJob();
	}

	public void setTaskJobHandle(TaskJobHandle taskJobHandle) {
		this.taskJobHandle = taskJobHandle;
	}
	
	public void setC2cExpireTask_1_minute(C2cExpire1MinuteJob c2cExpireTask_1_minute) {
		this.c2cExpireTask_1_minute = c2cExpireTask_1_minute;
	}
	
	public void setKlineTask_1_minute(Kline1MinuteJob klineTask_1_minute) {
		this.klineTask_1_minute = klineTask_1_minute;
	}

	public void setKlineTask_5_minute(Kline5MinuteJob klineTask_5_minute) {
		this.klineTask_5_minute = klineTask_5_minute;
	}

	public void setKlineTask_15_minute(Kline15MinuteJob klineTask_15_minute) {
		this.klineTask_15_minute = klineTask_15_minute;
	}

	public void setKlineTask_30_minute(Kline30MinuteJob klineTask_30_minute) {
		this.klineTask_30_minute = klineTask_30_minute;
	}

	public void setKlineTask_60_minute(Kline60MinuteJob klineTask_60_minute) {
		this.klineTask_60_minute = klineTask_60_minute;
	}

	public void setKlineTask_4_hour(Kline4HourJob klineTask_4_hour) {
		this.klineTask_4_hour = klineTask_4_hour;
	}

	public void setKlineTask_1_day(Kline1DayJob klineTask_1_day) {
		this.klineTask_1_day = klineTask_1_day;
	}

	public void setKlineTask_1_week(Kline1WeekJob klineTask_1_week) {
		this.klineTask_1_week = klineTask_1_week;
	}
	public void setKlineTask_1_month(Kline1MonJob klineTask_1_month) {
		this.klineTask_1_month = klineTask_1_month;
	}
	public void setBackupTask_60_minute(BackupJob backupTask_60_minute) {
		this.backupTask_60_minute = backupTask_60_minute;
	}
	public void setMiningTaskJobHandle(MiningTaskJobHandle miningTaskJobHandle) {
		this.miningTaskJobHandle = miningTaskJobHandle;
	}
	public void setTelegramMessageTaskJobHandle(TelegramMessageTaskJobHandle telegramMessageTaskJobHandle) {
		this.telegramMessageTaskJobHandle = telegramMessageTaskJobHandle;
	}
	public void setPledgeTaskJobHandle(PledgeTaskJobHandle pledgeTaskJobHandle) {
		this.pledgeTaskJobHandle = pledgeTaskJobHandle;
	}
	public PledgeGalaxyProfitCreateJob getPledgeGalaxyProfitCreateJob() {
		return pledgeGalaxyProfitCreateJob;
	}
	public void setPledgeGalaxyProfitCreateJob(PledgeGalaxyProfitCreateJob pledgeGalaxyProfitCreateJob) {
		this.pledgeGalaxyProfitCreateJob = pledgeGalaxyProfitCreateJob;
	}

	public PledgeGalaxyTeamProfitCreateJob getPledgeGalaxyTeamProfitCreateJob() {
		return pledgeGalaxyTeamProfitCreateJob;
	}

	public void setPledgeGalaxyTeamProfitCreateJob(PledgeGalaxyTeamProfitCreateJob pledgeGalaxyTeamProfitCreateJob) {
		this.pledgeGalaxyTeamProfitCreateJob = pledgeGalaxyTeamProfitCreateJob;
	}

	public void setAutoMonitorPoolDataUpdateJob(AutoMonitorPoolDataUpdateJob autoMonitorPoolDataUpdateJob) {
		this.autoMonitorPoolDataUpdateJob = autoMonitorPoolDataUpdateJob;
	}

	public LoanInterestJob getLoanInterestJob() {
		return loanInterestJob;
	}

	public void setLoanInterestJob(LoanInterestJob loanInterestJob) {
		this.loanInterestJob = loanInterestJob;
	}

	public WalletIncomeJob getWalletIncomeJob() {
		return walletIncomeJob;
	}

	public void setWalletIncomeJob(WalletIncomeJob walletIncomeJob) {
		this.walletIncomeJob = walletIncomeJob;
	}


}
