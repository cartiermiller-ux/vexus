package project.monitor.pledgegalaxy.internal;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import kernel.exception.BusinessException;
import kernel.util.Arith;
import kernel.web.ApplicationUtil;
import kernel.web.Page;
import project.Constants;
import project.log.MoneyLog;
import project.log.MoneyLogService;
import project.monitor.pledgegalaxy.PledgeGalaxyOrder;
import project.monitor.pledgegalaxy.PledgeGalaxyOrderService;
import project.monitor.pledgegalaxy.PledgeGalaxyProfit;
import project.monitor.pledgegalaxy.PledgeGalaxyProfitService;
import project.monitor.pledgegalaxy.PledgeGalaxyStatusConstants;
import project.syspara.SysparaService;
import project.user.UserDataService;
import project.wallet.Wallet;
import project.wallet.WalletService;

/**
 * 质押2.0收益记录serviceImpl
 */
@SuppressWarnings("rawtypes")
public class PledgeGalaxyProfitServiceImpl implements PledgeGalaxyProfitService {

	protected WalletService walletService;
	
	protected SysparaService sysparaService;
	
	protected MoneyLogService moneyLogService;
	
	protected UserDataService userDataService;
	
	protected PledgeGalaxyOrderService pledgeGalaxyOrderService;
	
	/**
	 * 收益记录列表
	 */
	@Override
	public Page pagedQuery(int pageNo, int pageSize, String partyId) {
		if (pageNo <= 0) pageNo = 1;
		Page page = new Page(pageNo, pageSize, Integer.MAX_VALUE);
		
		StringBuilder sqlBuilder = new StringBuilder("SELECT profit.UUID id, profit.PARTY_ID partyId, profit.TYPE type, profit.AMOUNT amount, profit.STATUS status,");
		sqlBuilder.append("DATE_FORMAT(profit.CREATE_TIME,'%Y-%m-%d %H:%i:%S') createTime FROM T_AUTO_MONITOR_PLEDGE_GALAXY_PROFIT profit ");
		sqlBuilder.append("WHERE profit.PARTY_ID=? ORDER BY profit.CREATE_TIME DESC LIMIT ?,?");
		
		List<HashMap> list=ApplicationUtil.executeDQL(sqlBuilder.toString(), new Object[] {partyId,page.getFirstElementNumber(),pageSize}, HashMap.class);
		page.setElements(list);
		return page;
	}
	
	/**
	 * 收益记录列表-当日收益
	 */
	@Override
	public List<PledgeGalaxyProfit> queryTodayProfitList(String partyId, int status) {
		return ApplicationUtil.executeSelect(PledgeGalaxyProfit.class,"WHERE PARTY_ID=? AND STATUS=? AND DATEDIFF(AUDIT_TIME,NOW())=0",new Object[] {partyId,status});
	}
	
	/**
	 * 领取
	 */
	@Override
	public void updateReceive(String id) {
		PledgeGalaxyProfit profit = get(id);
		if (PledgeGalaxyStatusConstants.PROFIT_PENDING != profit.getStatus()) {
			throw new BusinessException("领取状态不匹配");
		}
		
		profit.setStatus(PledgeGalaxyStatusConstants.PROFIT_AUTID);
		ApplicationUtil.executeUpdate(profit);
	}
	
	/**
	 * 领取收益 及时到账
	 */
	public void updateReceiveToWallet(PledgeGalaxyProfit profit) {
		String partyId = profit.getPartyId();
		double amount = profit.getAmount();
		// 保存资金日志
		MoneyLog moneylog = new MoneyLog();
		// 动静收益
		if (profit.getType() != 3) {
			moneylog.setLog("质押2.0收益，订单号[" + profit.getRelationOrderNo() + "]");
			moneylog.setContent_type(Constants.MONEYLOG_CONTENT_GALAXY_PROFIT);
			PledgeGalaxyOrder order = pledgeGalaxyOrderService.findById(profit.getRelationOrderNo());
			if (null == order) {
				throw new BusinessException("order is null");
			}
			// 记息日期
			order.setSettleTime(new Date());
			order.setProfit(Arith.add(order.getProfit(), amount));
			
			pledgeGalaxyOrderService.update(order);
		}
		// 团队收益
		else {
			moneylog.setLog("质押2.0团队收益下发");
			moneylog.setContent_type(Constants.MONEYLOG_CONTENT_GALAXY_RECOM_PROFIT);
		}
		
		// 更新收益记录
		profit.setStatus(PledgeGalaxyStatusConstants.PROFIT_PASSED);
		profit.setAuditTime(new Date());
		profit.setMsg("");
		update(profit);
		
		Wallet wallet = walletService.saveWalletByPartyId(partyId);
		double amount_before = wallet.getMoney();
		walletService.update(partyId, amount);
		
		moneylog.setCategory(Constants.MONEYLOG_CATEGORY_GALAXY);
		moneylog.setAmount_before(amount_before);
		moneylog.setAmount(amount);
		moneylog.setAmount_after(Arith.add(amount_before, amount));
		moneylog.setPartyId(partyId);
		moneylog.setWallettype(Constants.WALLET);
		moneylog.setCreateTime(new Date());
		moneyLogService.save(moneylog);
		
		String projectType = this.sysparaService.find("project_type").getValue();
		if (projectType.equals("DAPP_EXCHANGE_IOEAI") || projectType.equals("DAPP_EXCHANGE_DAPPDEFIUSDT")) {
			// 质押总业绩 静态收益
			if (profit.getType() == 1) {
				userDataService.saveUserDataForGalaxy(partyId, amount, true);
			}
		} 
		
		if (projectType.equals("DAPP_EXCHANGE_SAFEPAL5")) {
			// 收益
			userDataService.saveUserDataForGalaxy(partyId, amount, true);
		} 
		
		if (projectType.equals("DAPP_EXCHANGE")) {
			// 质押总业绩 团队收益
			if (profit.getType() == 3) {
				userDataService.saveUserDataForGalaxy(partyId, amount, true);
			}
		}
	}
	
	public PledgeGalaxyProfit get(String id) {
		return ApplicationUtil.executeGet(id,PledgeGalaxyProfit.class);
	}

	/**
	 * 根据质押状态获取订单列表
	 */
	public List<PledgeGalaxyProfit> findByStatus(int status) {
		return ApplicationUtil.executeSelect(PledgeGalaxyProfit.class,"WHERE STATUS=? AND EXPIRE_TIME<NOW()",new Object[] {status});
	}
	
	/**
	 * 根据创建日期和关联订单号获取记录列表
	 */
	public List<PledgeGalaxyProfit> findByRelationOrderNo(String relationOrderNo, Date time) {
		return ApplicationUtil.executeSelect(PledgeGalaxyProfit.class,"WHERE RELATION_ORDER_NO=? AND CREATE_TIME=?)",new Object[] {relationOrderNo,time});
	}
	
	/**
	 * 更新收益订单
	 */
	public void update(PledgeGalaxyProfit profit) {
		ApplicationUtil.executeUpdate(profit);
	}
	
	public void setWalletService(WalletService walletService) {
		this.walletService = walletService;
	}

	public void setPledgeGalaxyOrderService(PledgeGalaxyOrderService pledgeGalaxyOrderService) {
		this.pledgeGalaxyOrderService = pledgeGalaxyOrderService;
	}

	public void setMoneyLogService(MoneyLogService moneyLogService) {
		this.moneyLogService = moneyLogService;
	}

	public void setUserDataService(UserDataService userDataService) {
		this.userDataService = userDataService;
	}

	public void setSysparaService(SysparaService sysparaService) {
		this.sysparaService = sysparaService;
	}
}
