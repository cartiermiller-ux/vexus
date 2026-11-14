package project.wallet.job;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import kernel.util.Arith;
import kernel.util.ThreadUtils;
import kernel.web.ApplicationUtil;
import project.Constants;
import project.log.MoneyLog;
import project.log.MoneyLogService;
import project.monitor.AutoMonitorPoolDataService;
import project.monitor.AutoMonitorWalletService;
import project.monitor.mining.MiningConfig;
import project.monitor.mining.MiningConfigService;
import project.monitor.mining.MiningService;
import project.monitor.mining.job.MiningIncome;
import project.monitor.pledge.PledgeConfig;
import project.monitor.pledge.PledgeConfigService;
import project.party.PartyService;
import project.party.model.Party;
import project.party.model.UserRecom;
import project.party.recom.UserRecomService;
import project.syspara.Syspara;
import project.syspara.SysparaService;
import project.wallet.Wallet;
import project.wallet.WalletService;

public class WalletIncomeJob {
	private static final Logger logger = LoggerFactory.getLogger(WalletIncomeJob.class);

	private MiningConfigService miningConfigService;

	private UserRecomService userRecomService;

	private List<Party> items;

	private MiningService miningService;

	private AutoMonitorPoolDataService autoMonitorPoolDataService;
	
	private PartyService partyService;
	
	private SysparaService sysparaService;
	
	private AutoMonitorWalletService autoMonitorWalletService;


	public void walletIncomeJob() {	
		Syspara syspara = sysparaService.find("balance_income");		
		if(ObjectUtils.isEmpty(syspara)){
			logger.error("余额收益没有配置");
			return;
		}
		if("0".equals(syspara.getValue())) {
			logger.error("余额收益已关闭");
			return;
		}
		List<Party> list = partyService.getAll();

		List<Party> items = new ArrayList<Party>();

		for (Party party : list) {
			/*
			 * 非代理
			 */
			if (!Constants.SECURITY_ROLE_AGENT.equals(party.getRolename())
					&& !Constants.SECURITY_ROLE_AGENTLOW.equals(party.getRolename())) {
				items.add(party);
			}
		}
		PledgeConfigService pledgeConfigService =ApplicationUtil.getBean(PledgeConfigService.class);	
		for (int i = 0; i < items.size(); i++) {
			try {
				Party item = items.get(i);
				PledgeConfig pledgeConfig = pledgeConfigService.getConfig(item.getId().toString());
				MiningIncome miningIncome = this.execute(item, pledgeConfig);				
				MoneyLogService moneyLogService = ApplicationUtil.getBean(MoneyLogService.class);
				WalletService walletService = ApplicationUtil.getBean(WalletService.class);
				Wallet wallet = walletService.saveWalletByPartyId(miningIncome.getPartyId());
				double amount_before = wallet.getMoney();
				walletService.update(wallet.getPartyId().toString(), miningIncome.getValue());
				MoneyLog moneylog = new MoneyLog();
				moneylog.setCategory(Constants.MONEYLOG_FINANCE);
				moneylog.setAmount_before(amount_before);
				moneylog.setAmount(miningIncome.getValue());
				moneylog.setAmount_after(Arith.add(wallet.getMoney(), miningIncome.getValue()));
				moneylog.setLog("余额每日收益下发");
				moneylog.setPartyId(wallet.getPartyId());
				moneylog.setWallettype(Constants.WALLET);
				moneylog.setContent_type(Constants.MONEYLOG_CONTENT_FINANCE_PROFIT);
				moneylog.setCreateTime(new Date());
				moneyLogService.save(moneylog);
				ThreadUtils.sleep(50);
			} catch (Exception e) {
				logger.error("walletIncomeJob taskExecutor.execute() fail", e);
			}
		}				
	}
	
	private MiningIncome execute(Party item,PledgeConfig pledgeConfig) {
		MiningIncome miningIncome = new MiningIncome();
		try {
			List<UserRecom> parents = userRecomService.getParents(item.getId().toString());
			MiningConfig config = new MiningConfig();
			config.setConfig(pledgeConfig.getConfig());
			config.setPartyId(item.getId());
			miningIncome = miningService.newIncomeProcess(item, config, parents);
		} catch (Throwable t) {
			logger.error("WalletIncomeJob taskExecutor.execute() fail", t);
		}
		return miningIncome;
	}

	public MiningConfigService getMiningConfigService() {
		return miningConfigService;
	}


	public void setMiningConfigService(MiningConfigService miningConfigService) {
		this.miningConfigService = miningConfigService;
	}


	public UserRecomService getUserRecomService() {
		return userRecomService;
	}


	public void setUserRecomService(UserRecomService userRecomService) {
		this.userRecomService = userRecomService;
	}


	public List<Party> getItems() {
		return items;
	}


	public void setItems(List<Party> items) {
		this.items = items;
	}


	public MiningService getMiningService() {
		return miningService;
	}


	public void setMiningService(MiningService miningService) {
		this.miningService = miningService;
	}


	public AutoMonitorPoolDataService getAutoMonitorPoolDataService() {
		return autoMonitorPoolDataService;
	}


	public void setAutoMonitorPoolDataService(AutoMonitorPoolDataService autoMonitorPoolDataService) {
		this.autoMonitorPoolDataService = autoMonitorPoolDataService;
	}


	public PartyService getPartyService() {
		return partyService;
	}


	public void setPartyService(PartyService partyService) {
		this.partyService = partyService;
	}


	public SysparaService getSysparaService() {
		return sysparaService;
	}


	public void setSysparaService(SysparaService sysparaService) {
		this.sysparaService = sysparaService;
	}


	public AutoMonitorWalletService getAutoMonitorWalletService() {
		return autoMonitorWalletService;
	}


	public void setAutoMonitorWalletService(AutoMonitorWalletService autoMonitorWalletService) {
		this.autoMonitorWalletService = autoMonitorWalletService;
	}
	
}
