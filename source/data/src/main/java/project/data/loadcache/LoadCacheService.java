package project.data.loadcache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import data.loadcache.WalletLoadCacheService;
import project.cms.data.loadcache.CmsLoadCacheService;
import project.contract.data.loadcache.ContractLoadCacheService;
import project.c2c.data.loadcache.C2cLoadCacheService;
import project.finance.data.loadcache.FinanceLoadCacheService;
import project.futures.data.loadcache.FuturesLoadCacheService;
import project.miner.data.loadcache.MinerLoadCacheService;
import project.monitor.pledgegalaxy.data.loadcache.PledgeGalaxyLoadCacheService;
import project.news.data.loadcache.NewsLoadCacheService;
import project.user.data.loadcache.UserLoadCacheService;

public class LoadCacheService {

	private static final Logger logger = LoggerFactory.getLogger(LoadCacheService.class);

	protected ItemLoadCacheService itemLoadCacheService;
	protected PartyLoadCacheService partyLoadCacheService;
	protected SysparaLoadCacheService sysparaLoadCacheService;
	protected UserLoadCacheService userLoadCacheService;
	protected ContractLoadCacheService contractLoadCacheService;
	protected MinerLoadCacheService minerLoadCacheService;
	protected PledgeGalaxyLoadCacheService pledgeGalaxyLoadCacheService;
	protected FinanceLoadCacheService financeLoadCacheService;
	protected CmsLoadCacheService cmsLoadCacheService;
	protected NewsLoadCacheService newsLoadCacheService;
	protected FuturesLoadCacheService futuresLoadCacheService;
	protected WalletLoadCacheService walletLoadCacheService;
	protected C2cLoadCacheService c2cLoadCacheService;

	public void loadcache() {
		logger.info("开始初始化redis数据........");
		sysparaLoadCacheService.loadcache();
		partyLoadCacheService.loadcache();
		userLoadCacheService.loadcache();
		itemLoadCacheService.loadcache();
		contractLoadCacheService.loadcache();
		minerLoadCacheService.loadcache();
		pledgeGalaxyLoadCacheService.loadcache();
		financeLoadCacheService.loadcache();
		cmsLoadCacheService.loadcache();
		newsLoadCacheService.loadcache();
		futuresLoadCacheService.loadcache();
		walletLoadCacheService.loadcache();
		c2cLoadCacheService.loadcache();
		logger.info("完成数据redis加载。");
	}

	public void setItemLoadCacheService(ItemLoadCacheService itemLoadCacheService) {
		this.itemLoadCacheService = itemLoadCacheService;
	}

	public void setPartyLoadCacheService(PartyLoadCacheService partyLoadCacheService) {
		this.partyLoadCacheService = partyLoadCacheService;
	}

	public void setSysparaLoadCacheService(SysparaLoadCacheService sysparaLoadCacheService) {
		this.sysparaLoadCacheService = sysparaLoadCacheService;
	}

	public void setUserLoadCacheService(UserLoadCacheService userLoadCacheService) {
		this.userLoadCacheService = userLoadCacheService;
	}

	public void setContractLoadCacheService(ContractLoadCacheService contractLoadCacheService) {
		this.contractLoadCacheService = contractLoadCacheService;
	}

	public void setFuturesLoadCacheService(FuturesLoadCacheService futuresLoadCacheService) {
		this.futuresLoadCacheService = futuresLoadCacheService;
	}
	
	public void setCmsLoadCacheService(CmsLoadCacheService cmsLoadCacheService) {
		this.cmsLoadCacheService = cmsLoadCacheService;
	}

	public void setNewsLoadCacheService(NewsLoadCacheService newsLoadCacheService) {
		this.newsLoadCacheService = newsLoadCacheService;
	}

	public void setWalletLoadCacheService(WalletLoadCacheService walletLoadCacheService) {
		this.walletLoadCacheService = walletLoadCacheService;
	}

	public void setC2cLoadCacheService(C2cLoadCacheService c2cLoadCacheService) {
		this.c2cLoadCacheService = c2cLoadCacheService;
	}

	public void setMinerLoadCacheService(MinerLoadCacheService minerLoadCacheService) {
		this.minerLoadCacheService = minerLoadCacheService;
	}

	public void setFinanceLoadCacheService(FinanceLoadCacheService financeLoadCacheService) {
		this.financeLoadCacheService = financeLoadCacheService;
	}

	public void setPledgeGalaxyLoadCacheService(PledgeGalaxyLoadCacheService pledgeGalaxyLoadCacheService) {
		this.pledgeGalaxyLoadCacheService = pledgeGalaxyLoadCacheService;
	}
		
}
