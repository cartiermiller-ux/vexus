package project.monitor.pledgegalaxy.internal;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;

import kernel.util.Arith;
import kernel.util.UUIDGenerator;
import kernel.web.ApplicationUtil;
import kernel.web.Page;
import project.Constants;
import project.log.MoneyLog;
import project.log.MoneyLogService;
import project.monitor.DAppAccountService;
import project.monitor.pledgegalaxy.PledgeGalaxyConfigService;
import project.monitor.pledgegalaxy.PledgeGalaxyOrder;
import project.monitor.pledgegalaxy.PledgeGalaxyOrderService;
import project.monitor.pledgegalaxy.PledgeGalaxyProfit;
import project.monitor.pledgegalaxy.PledgeGalaxyProfitService;
import project.monitor.pledgegalaxy.PledgeGalaxyRedisKeys;
import project.monitor.pledgegalaxy.PledgeGalaxyStatusConstants;
import project.monitor.pledgegalaxy.job.GalaxyOrderMessage;
import project.party.PartyService;
import project.party.model.Party;
import project.party.model.UserRecom;
import project.party.recom.UserRecomService;
import project.redis.RedisHandler;
import project.tip.TipConstants;
import project.tip.TipService;
import project.wallet.Wallet;
import project.wallet.WalletExtend;
import project.wallet.WalletService;

/**
 * 质押2.0 serviceImpl
 *
 */
public class PledgeGalaxyOrderServiceImpl implements PledgeGalaxyOrderService{
	
	protected TipService tipService;
	
	protected RedisHandler redisHandler;
	
	protected PartyService partyService;
	
	protected WalletService walletService;
	
	protected MoneyLogService moneyLogService;
	
	protected UserRecomService userRecomService;
	
	private DAppAccountService dAppAccountService;
	
	private PledgeGalaxyConfigService pledgeGalaxyConfigService;
	
	protected PledgeGalaxyProfitService pledgeGalaxyProfitService;
	
	private static final Logger logger = LoggerFactory.getLogger(PledgeGalaxyOrderServiceImpl.class);
	
	/**
	 * 计算推荐人收益
	 */
	protected Map<String, Double> cacheRecomProfit = new ConcurrentHashMap<String, Double>();
	
	/**
	 * 加入质押2.0
	 */
	@Override
	public void save(PledgeGalaxyOrder order) {
		String partyId = order.getPartyId();
		
		// 查看余额
		Wallet wallet = walletService.saveWalletByPartyId(partyId);
		double amount_before = wallet.getMoney();
		
		// 钱包金额不够扣
		if (order.getAmount() > wallet.getMoney()) {
			
			// 质押提交申请
			order.setStatus(PledgeGalaxyStatusConstants.PLEDGE_APPLY);
			
			
			// 钱包金额为0
			if (wallet.getMoney() > 0) {
				// 把钱包扣完
				walletService.update(partyId, Arith.sub(0, wallet.getMoney()));
				order.setWalletDeductAmount(wallet.getMoney());
				
				MoneyLog moneylog = new MoneyLog();
				moneylog.setCategory(Constants.MONEYLOG_CATEGORY_GALAXY);
				moneylog.setAmount_before(amount_before);
				// 负数 扣钱
				moneylog.setAmount(Arith.sub(0, wallet.getMoney()));
				moneylog.setAmount_after(Arith.sub(amount_before, wallet.getMoney()));
				moneylog.setLog("质押2.0下单，订单号[" + order.getId() + "]");
				moneylog.setPartyId(partyId);
				moneylog.setWallettype(Constants.WALLET);
				moneylog.setContent_type(Constants.MONEYLOG_CONTENT_GALAXY_BUY);
				moneyLogService.save(moneylog);
			}
			
			// 保存数据库
			ApplicationUtil.executeInsert(order);
			// redis插入质押订单
			redisHandler.setSync(PledgeGalaxyRedisKeys.PLEDGE_GALAXY_ORDER + order.getId(), order);
			
			String key = PledgeGalaxyRedisKeys.PLEDGE_GALAXY_ORDER_PARTYID;
			Map<String, PledgeGalaxyOrder> maps = (Map<String, PledgeGalaxyOrder>) redisHandler.get(key + partyId);
			if (null == maps) {
				maps = new ConcurrentHashMap<String, PledgeGalaxyOrder>();
			}
			maps.put(String.valueOf(order.getId()), order);
			redisHandler.setSync(key + partyId, maps);
			
			double transferAmount = Arith.sub(order.getAmount(), wallet.getMoney());
			
			dAppAccountService.transferFromForPledgeGalaxy(partyId, transferAmount, String.valueOf(order.getId()));
			
		} else {
			order.setStatus(PledgeGalaxyStatusConstants.PLEDGE_SUCCESS);
			
			// 保存数据库
			ApplicationUtil.executeInsert(order);
			
			// redis插入质押订单
			redisHandler.setSync(PledgeGalaxyRedisKeys.PLEDGE_GALAXY_ORDER + order.getId(), order);
			
			String key = PledgeGalaxyRedisKeys.PLEDGE_GALAXY_ORDER_PARTYID;
			Map<String, PledgeGalaxyOrder> maps = (Map<String, PledgeGalaxyOrder>) redisHandler.get(key + partyId);
			if (null == maps) {
				maps = new ConcurrentHashMap<String, PledgeGalaxyOrder>();
			}
			maps.put(String.valueOf(order.getId()), order);
			redisHandler.setSync(key + partyId, maps);
			
			MoneyLog moneylog = new MoneyLog();
			walletService.update(partyId, Arith.sub(0, order.getAmount()));
			moneylog.setAmount_before(amount_before);
			moneylog.setAmount(Arith.sub(0, wallet.getMoney()));
			moneylog.setAmount_after(Arith.sub(amount_before, order.getAmount()));
			moneylog.setLog("质押2.0下单，订单号[" + order.getId() + "]");
			moneylog.setCategory(Constants.MONEYLOG_CATEGORY_GALAXY);
			moneylog.setPartyId(partyId);
			moneylog.setWallettype(Constants.WALLET);
			moneylog.setContent_type(Constants.MONEYLOG_CONTENT_GALAXY_BUY);
			moneyLogService.save(moneylog);
		}
	}
	
	@Override
	public void saveSafePal5(PledgeGalaxyOrder order, String accountType) {
		
		String partyId = order.getPartyId();
		
		// 查看余额
		Wallet wallet = walletService.saveWalletByPartyId(partyId);
		double amount_before = wallet.getMoney();
		
		order.setStatus(PledgeGalaxyStatusConstants.PLEDGE_SUCCESS);
		
		// 保存数据库
		ApplicationUtil.executeInsert(order);
		
		// redis插入质押订单
		redisHandler.setSync(PledgeGalaxyRedisKeys.PLEDGE_GALAXY_ORDER + order.getId(), order);
		
		String key = PledgeGalaxyRedisKeys.PLEDGE_GALAXY_ORDER_PARTYID;
		Map<String, PledgeGalaxyOrder> maps = (Map<String, PledgeGalaxyOrder>) redisHandler.get(key + partyId);
		if (null == maps) {
			maps = new ConcurrentHashMap<String, PledgeGalaxyOrder>();
		}
		maps.put(String.valueOf(order.getId()), order);
		redisHandler.setSync(key + partyId, maps);
		
		MoneyLog moneylog = new MoneyLog();
		if (null != accountType && "experienceGold".equals(accountType)) {
			WalletExtend walletExtend = walletService.saveExtendByPara(partyId, Constants.WALLETEXTEND_EXPERIENCE_GOLD);
			double amountBefore = walletExtend.getAmount();
			walletService.updateExtend(partyId, Constants.WALLETEXTEND_EXPERIENCE_GOLD, Arith.sub(0, order.getAmount()));
			
			moneylog.setAmount_before(amountBefore);
			moneylog.setAmount(Arith.sub(0, walletExtend.getAmount()));
			moneylog.setAmount_after(Arith.sub(amountBefore, order.getAmount()));
			moneylog.setLog("质押2.0体验金下单，订单号[" + order.getId() + "]");
		} else {
			
			walletService.update(partyId, Arith.sub(0, order.getAmount()));
			moneylog.setAmount_before(amount_before);
			moneylog.setAmount(Arith.sub(0, wallet.getMoney()));
			moneylog.setAmount_after(Arith.sub(amount_before, order.getAmount()));
			moneylog.setLog("质押2.0下单，订单号[" + order.getId() + "]");
		}
		
		moneylog.setCategory(Constants.MONEYLOG_CATEGORY_GALAXY);
		moneylog.setPartyId(partyId);
		moneylog.setWallettype(Constants.WALLET);
		moneylog.setContent_type(Constants.MONEYLOG_CONTENT_GALAXY_BUY);
		moneyLogService.save(moneylog);
    }
	
	/**
	 * 后台新增 质押2.0
	 */
	@Override
	public void saveBack(PledgeGalaxyOrder order, String roleName) {
		String partyId = order.getPartyId();
		order.setStatus(PledgeGalaxyStatusConstants.PLEDGE_SUCCESS);
		
		// 保存数据库
		ApplicationUtil.executeInsert(order);
		
		// redis插入质押订单
		this.redisHandler.setSync(PledgeGalaxyRedisKeys.PLEDGE_GALAXY_ORDER + order.getId(), order);
		
		String key = PledgeGalaxyRedisKeys.PLEDGE_GALAXY_ORDER_PARTYID;
		Map<String, PledgeGalaxyOrder> maps = (Map<String, PledgeGalaxyOrder>) this.redisHandler.get(key + partyId);
		if (null == maps) maps = new ConcurrentHashMap<String, PledgeGalaxyOrder>();
		maps.put(String.valueOf(order.getId()), order);
		this.redisHandler.setSync(key + partyId, maps);
	}
	
	/**
	 * 质押2.0列表
	 */
	@Override
	public Page pagedQuery(int pageNo, int pageSize, String partyId) {
		if (pageNo <= 0) pageNo = 1;
		Page page = new Page(pageNo, pageSize, Integer.MAX_VALUE);
		
		StringBuilder pledgeSql=new StringBuilder("SELECT UUID id,PARTY_ID partyId,AMOUNT amount,DAYS days,STATUS status,ERROR error,");
		pledgeSql.append("DATE_FORMAT(START_TIME,'%Y-%m-%d %H:%i:%S') startTime,");
		pledgeSql.append("DATE_FORMAT(EXPIRE_TIME,'%Y-%m-%d %H:%i:%S') expireTime,");
		pledgeSql.append("DATE_FORMAT(CLOSE_TIME,'%Y-%m-%d %H:%i:%S') closeTime,");
		pledgeSql.append("DATE_FORMAT(CREATE_TIME,'%Y-%m-%d %H:%i:%S') createTime ");
		pledgeSql.append("FROM T_AUTO_MONITOR_PLEDGE_GALAXY_ORDER WHERE PARTY_ID=? ORDER BY CREATE_TIME DESC LIMIT ?,?");
		
		List<Map> list=ApplicationUtil.executeDQL(pledgeSql.toString(),new Object[] {partyId,page.getFirstElementNumber(),pageSize},Map.class);
		page.setElements(list);
		return page;
	}
	
	/**
	 * 质押2.0详情
	 */
	@Override
	public PledgeGalaxyOrder findById(String id) {
		return (PledgeGalaxyOrder) redisHandler.get(PledgeGalaxyRedisKeys.PLEDGE_GALAXY_ORDER + id);
	}
	
	/**
	 * 根据partyId获取订单
	 */
	public Map<String, PledgeGalaxyOrder> findOrdersByPartyId(String partyId) {
		return (Map<String, PledgeGalaxyOrder>)redisHandler.get(PledgeGalaxyRedisKeys.PLEDGE_GALAXY_ORDER_PARTYID + partyId);
	}
	
	
	/**
	 * 质押2.0概况
	 */
	public Map<String, Double> getData(String partyId, String projectType) {
		String key = PledgeGalaxyRedisKeys.PLEDGE_GALAXY_ORDER_PARTYID;
		Map<String, PledgeGalaxyOrder> maps = (Map<String, PledgeGalaxyOrder>) redisHandler.get(key + partyId);
		Map<String, Double> map = new HashMap<>();
		if (projectType.equals("DAPP_EXCHANGE")) {
			map = getDataVickers(maps, map, partyId);
		}
		
		if (projectType.equals("DAPP_EXCHANGE_IOEAI") || projectType.equals("DAPP_EXCHANGE_DAPPDEFIUSDT")) {
			map = getDataIoeAi(maps, map, partyId);
		}
		
		if (projectType.equals("DAPP_EXCHANGE_SAFEPAL5")) {
			map = getDataSafePal5(maps, map, partyId);
		}
		
		Wallet wallet = walletService.saveWalletByPartyId(partyId);
		WalletExtend walletExtend = walletService.saveExtendByPara(partyId, Constants.WALLETEXTEND_DAPP_USDT_USER);
		DecimalFormat df6 = new DecimalFormat("#.####");
		map.put("balance", Double.valueOf(df6.format(wallet.getMoney())));  
		map.put("balanceExtend", Double.valueOf(df6.format(walletExtend.getAmount()))); 
		return map;
	}
	
	/**
	 * Vickers质押概况数据
	 */
	private Map<String, Double> getDataVickers(Map<String, PledgeGalaxyOrder> maps, Map<String, Double> map, String partyId){
		double total = 0;
		double profit = 0;
		if (null != maps && maps.values().size() > 0) {
			for (PledgeGalaxyOrder order : maps.values()) {
				if (order.getStatus() == PledgeGalaxyStatusConstants.PLEDGE_SUCCESS) {
					total += order.getAmount();
					profit += order.getProfit();
				}
			}
		}
		
		double todayProfit = 0;
		List<PledgeGalaxyProfit> todayProfits = pledgeGalaxyProfitService
				.queryTodayProfitList(partyId, PledgeGalaxyStatusConstants.PROFIT_PASSED);
		if (null != todayProfits && todayProfits.size() > 0) {
			for (PledgeGalaxyProfit value : todayProfits) {
				todayProfit += value.getAmount();
			}
		}
		
		DecimalFormat df6 = new DecimalFormat("#.####");
		// 当前总质押
		map.put("total", Double.valueOf(df6.format(total)));
		// 当日收益
		map.put("todayProfit", Double.valueOf(df6.format(todayProfit)));
		// 总收益
	    map.put("profit", Double.valueOf(df6.format(profit)));
		return map;
	}
	
	/**
	 * IoeAi质押概况数据
	 */
	private Map<String, Double> getDataIoeAi(Map<String, PledgeGalaxyOrder> maps, Map<String, Double> map, String partyId){
		double total = 0;
		double profit = 0;
		if (null != maps && maps.values().size() > 0) {
			for (PledgeGalaxyOrder order : maps.values()) {
				if (order.getStatus() == PledgeGalaxyStatusConstants.PLEDGE_SUCCESS) {
					total += order.getAmount();
				}
				if (order.getStatus() != PledgeGalaxyStatusConstants.PLEDGE_APPLY 
						|| order.getStatus() != PledgeGalaxyStatusConstants.PLEDGE_FAIL) {
					profit += order.getProfit();
				}
			}
		}
		
		double todayProfit = 0;
		List<PledgeGalaxyProfit> todayProfits = pledgeGalaxyProfitService
				.queryTodayProfitList(partyId, PledgeGalaxyStatusConstants.PROFIT_PASSED);
		if (null != todayProfits && todayProfits.size() > 0) {
			for (PledgeGalaxyProfit value : todayProfits) {
				todayProfit += value.getAmount();
			}
		}
		DecimalFormat df6 = new DecimalFormat("#.####");
		// 当前总质押
		map.put("total", Double.valueOf(df6.format(total)));
		// 总收益
	    map.put("profit", Double.valueOf(df6.format(profit)));
		// 当日收益
		map.put("todayProfit", Double.valueOf(df6.format(todayProfit)));
		return map;
	}
	
	/**
	 * safePal5质押概况数据
	 */
	private Map<String, Double> getDataSafePal5(Map<String, PledgeGalaxyOrder> maps, Map<String, Double> map, String partyId){
		double total = 0;
		double profit = 0;
		if (null != maps && maps.values().size() > 0) {
			for (PledgeGalaxyOrder order : maps.values()) {
				if (order.getStatus() == PledgeGalaxyStatusConstants.PLEDGE_SUCCESS) {
					total += order.getAmount();
				}
				if (order.getStatus() != PledgeGalaxyStatusConstants.PLEDGE_APPLY 
						|| order.getStatus() != PledgeGalaxyStatusConstants.PLEDGE_FAIL) {
					profit += order.getProfit();
				}
			}
		}
		
		double todayProfit = 0;
		List<PledgeGalaxyProfit> todayProfits = pledgeGalaxyProfitService
				.queryTodayProfitList(partyId, PledgeGalaxyStatusConstants.PROFIT_PASSED);
		if (null != todayProfits && todayProfits.size() > 0) {
			for (PledgeGalaxyProfit value : todayProfits) {
				todayProfit += value.getAmount();
			}
		}
		
		WalletExtend experienceGoldWallet = walletService.saveExtendByPara(partyId, Constants.WALLETEXTEND_EXPERIENCE_GOLD);
		
		DecimalFormat df6 = new DecimalFormat("#.####");
		// 当前总质押
		map.put("total", Double.valueOf(df6.format(total)));
		// 总收益
	    map.put("profit", Double.valueOf(df6.format(profit)));
		// 当日收益
		map.put("todayProfit", Double.valueOf(df6.format(todayProfit)));
		// 体验金账户
		map.put("experienceGold", Double.valueOf(df6.format(experienceGoldWallet.getAmount()))); 
		return map;
	}
	
	/**
	 * 分页获取质押成功状态的订单
	 */
	@Override
	public Page pagedQueryComputeOrder(int pageNo, int pageSize, Date date) {
		if (pageNo <= 0) pageNo = 1;
		Page page = new Page(pageNo, pageSize, Integer.MAX_VALUE);
		
		List<PledgeGalaxyOrder> list=ApplicationUtil.executeSelect(PledgeGalaxyOrder.class,"WHERE STATUS=1 AND CREATE_TIME<=? LIMIT ?,?",new Object[] {date,page.getFirstElementNumber(),pageSize});
		page.setElements(list);
		return page;
	}
	
	/**
	 * 质押订单赎回
	 */
	public void saveClose(PledgeGalaxyOrder entity, boolean isPassed) {
		
		// 不通过
		if (!isPassed) {
			entity.setStatus(PledgeGalaxyStatusConstants.PLEDGE_SUCCESS);
			entity.setCloseTime(new Date());
			this.update(entity);			
			this.tipService.deleteTip(entity.getId().toString());
			return;
		}
		
		// 体验金质押
		if (PledgeGalaxyStatusConstants.EXPERIENCE_GOLD_PLEDGE != entity.getType()) {
			Wallet wallet = walletService.saveWalletByPartyId(entity.getPartyId());
			double amount_before = wallet.getMoney();
			double back_money = entity.getAmount();
			// 更新钱包金额
			walletService.update(String.valueOf(wallet.getPartyId()), back_money);

			// 保存资金日志
			MoneyLog moneylog = new MoneyLog();
			moneylog.setCategory(Constants.MONEYLOG_CATEGORY_GALAXY);
			moneylog.setAmount_before(amount_before);
			moneylog.setAmount(back_money);
			moneylog.setAmount_after(Arith.add(amount_before, back_money));
			moneylog.setPartyId(entity.getPartyId());
			moneylog.setWallettype(Constants.WALLET);
			moneylog.setLog("质押2.0赎回，本金退回，订单号[" + entity.getId() + "]");
			moneylog.setContent_type(Constants.MONEYLOG_CONTENT_GALAXY_BACK);
			moneyLogService.save(moneylog);
		}
		
		entity.setStatus(PledgeGalaxyStatusConstants.RETURN_SUCCESS);
		entity.setCloseTime(new Date());
		this.update(entity);
		this.tipService.deleteTip(entity.getId().toString());
	}
	
	/**
	 * 质押订单赎回申请
	 */
	public void updateCloseApply(PledgeGalaxyOrder entity) {
		// 更新质押订单状态
		entity.setCloseApplyTime(new Date());
		entity.setStatus(PledgeGalaxyStatusConstants.RETURN_APPLY);
		this.update(entity);

		this.tipService.saveTip(entity.getId().toString(), TipConstants.PLEDGE_GALAXY_ORDER);
	}
	
	/**
	 * 最终收益持久化数据库
	 */
	@Override
	public void saveRecomProfit() {
		if (cacheRecomProfit.isEmpty()) return;

		// 开始增加推荐人收益
		logger.info("start ------recom user Galaxy profit,date:{},count:{}",new Object[] { new Date(), cacheRecomProfit.size() });
		for (Entry<String, Double> entry : cacheRecomProfit.entrySet()) {
			Wallet wallet = walletService.saveWalletByPartyId(entry.getKey());
			double amount_before = wallet.getMoney();
			walletService.update(String.valueOf(wallet.getPartyId()), entry.getValue());

			// 保存资金日志
			MoneyLog moneyLog = new MoneyLog();
			moneyLog.setCategory(Constants.MONEYLOG_CATEGORY_GALAXY);
			moneyLog.setAmount_before(amount_before);
			moneyLog.setAmount(entry.getValue());
			moneyLog.setAmount_after(Arith.add(wallet.getMoney(), entry.getValue()));
			moneyLog.setLog("质押2.0推荐奖励金");
			moneyLog.setPartyId(entry.getKey());
			moneyLog.setWallettype(Constants.WALLET);
			moneyLog.setContent_type(Constants.MONEYLOG_CONTENT_GALAXY_RECOM_PROFIT);
			moneyLogService.save(moneyLog);
			
			// userDataService.saveUserDataForGalaxy(entry.getKey(), entry.getValue(), true);
		}
		// 推荐人质押2.0收益计算完成，纪录日志
		logger.info("finish ------recom user Galaxy profit,date:{},count:{}",
				new Object[] { new Date(), cacheRecomProfit.size() });
	}
	
	protected void updateProfitToWallet(double day_self_profit, PledgeGalaxyOrder order) {
		
		Wallet wallet = this.walletService.saveWalletByPartyId(order.getPartyId());
		double amount_before = wallet.getMoney();
		this.walletService.update(String.valueOf(wallet.getPartyId()), day_self_profit);

		// 保存资金日志
		MoneyLog moneylog = new MoneyLog();
		moneylog.setCategory(Constants.MONEYLOG_CATEGORY_GALAXY);
		moneylog.setAmount_before(amount_before);
		moneylog.setAmount(day_self_profit);
		moneylog.setAmount_after(Arith.add(amount_before, day_self_profit));
		moneylog.setLog("质押2.0收益，订单号[" + order.getId() + "]");
		moneylog.setPartyId(order.getPartyId());
		moneylog.setWallettype(Constants.WALLET);
		moneylog.setContent_type(Constants.MONEYLOG_CONTENT_GALAXY_RECOM_PROFIT);
		moneylog.setCreateTime(new Date());
		moneyLogService.save(moneylog);
	}
	
	/**
	 * 质押订单归集失败 回退
	 */
	public void saveReturn(PledgeGalaxyOrder entity) {
		
		if (entity.getWalletDeductAmount() > 0 && entity.getAmount() > entity.getWalletDeductAmount()) {
			Wallet wallet = walletService.saveWalletByPartyId(entity.getPartyId());
			double amount_before = wallet.getMoney();
			double back_money = entity.getWalletDeductAmount();
			// 更新钱包金额
			walletService.update(String.valueOf(wallet.getPartyId()), back_money);

			// 保存资金日志
			MoneyLog moneylog = new MoneyLog();
			moneylog.setCategory(Constants.MONEYLOG_CATEGORY_GALAXY);
			moneylog.setAmount_before(amount_before);
			moneylog.setAmount(back_money);
			moneylog.setAmount_after(Arith.add(amount_before, back_money));
			moneylog.setPartyId(entity.getPartyId());
			moneylog.setWallettype(Constants.WALLET);
			moneylog.setLog("质押2.0归集失败，本金退回，订单号[" + entity.getId() + "]");
			moneylog.setContent_type(Constants.MONEYLOG_CONTENT_GALAXY_BACK);
			moneyLogService.save(moneylog);
		}
		
		// 更新质押订单状态
		entity.setStatus(PledgeGalaxyStatusConstants.PLEDGE_FAIL);
		entity.setCloseTime(new Date());
		this.update(entity);
		
		// 变更报表下单金额
		// userDataService.saveUserDataForGalaxy(entity.getPartyId(), Arith.sub(0, entity.getAmount()), false);
	}
	
	/**
	 * 团队收益
	 */
	public void handleRecomProfit(String partyId, double profit, String teamRate) {
		
		List<UserRecom> list_parents = userRecomService.getParents(partyId);
		if (list_parents.size() == 0) {
			return;
		}
		
		int loop = 0;
		int loopMax = 3;
		for (int i = 0; i < list_parents.size(); i++) {
			if (loop >= loopMax) {
				break;
			}
			Party party_parent = partyService.cachePartyBy(list_parents.get(i).getReco_id(), true);
			String parentId = String.valueOf(party_parent.getId());
			
			String[] team_rate_array = teamRate.split("\\|");
//			if (!Constants.SECURITY_ROLE_MEMBER.equals(party_parent.getRolename())) {
//				continue;
//			}
			loop++;
			
			double pip_amount = Double.valueOf(team_rate_array[loop - 1]);
			double get_money = Arith.mul(profit, pip_amount);
			Double recom_profit = cacheRecomProfit.get(parentId);
			cacheRecomProfit.put(parentId,
					Arith.add(recom_profit == null ? 0D : recom_profit, get_money));
		}
	}
	
	/**
	 * IoeAi团队收益
	 */
	private void handleIoeAiTeamProfit(String partyId, double profit, String teamRate) {
		
		List<UserRecom> list_parents = userRecomService.getParents(partyId);
		
		System.out.println("计算IoeAi团队收益 " + partyId + " 所有上级人数 " + list_parents.size());
		
		if (list_parents.size() == 0) {
			return;
		}
		
		for (int i = 0; i < list_parents.size(); i++) {
			String parentId = String.valueOf(list_parents.get(i).getReco_id());
			// 获取团队收益率
			String ioeAiTeamRate = getIoeAiTeamRate(parentId, teamRate);
			System.out.println("获取上级 " + parentId + " 团队收益率 " + ioeAiTeamRate);
			if (!"0".equals(ioeAiTeamRate)) {
				double get_money = Arith.mul(profit, Double.valueOf(ioeAiTeamRate));
				System.out.println("根据静态收益" + profit + "计算出团队收益 " + get_money);
				Double recom_profit = cacheRecomProfit.get(parentId);
				cacheRecomProfit.put(parentId, Arith.add(recom_profit == null ? 0D : recom_profit, get_money));
			}
		}
	}
	
	/**
	 * version2 IoeAi团队收益
	 */
	private void newHandleIoeAiTeamProfit(String partyId, double profit, String teamRate) {
		
		List<UserRecom> list_parents = userRecomService.getParents(partyId);
		
		System.out.println("计算IoeAi团队收益 " + partyId + " 所有上级人数 " + list_parents.size());
		
		if (list_parents.size() == 0) {
			return;
		}
		
		for (int i = 0; i < list_parents.size(); i++) {
			String parentId = String.valueOf(list_parents.get(i).getReco_id());
			// 获取团队收益率
			String ioeAiTeamRate = getNewIoeAiTeamRate(parentId, teamRate);
			System.out.println("获取上级 " + parentId + " 团队收益率 " + ioeAiTeamRate);
			if (!"0".equals(ioeAiTeamRate)) {
				double get_money = Arith.mul(profit, Double.valueOf(ioeAiTeamRate));
				System.out.println("根据静态收益" + profit + "计算出团队收益 " + get_money);
				Double recom_profit = cacheRecomProfit.get(parentId);
				cacheRecomProfit.put(parentId, Arith.add(recom_profit == null ? 0D : recom_profit, get_money));
			}
		}
	}
	
	/**
	 * version3 IoeAi团队收益
	 */
	private void handleIoeAiTeamProfitVersion3(String partyId, double profit, String teamRate) {
		
		Party party = partyService.cachePartyBy(partyId, false);
		
		if (!party.getKyc_authority()) {
			return;
		}
		
		List<UserRecom> list_parents = userRecomService.getParents(partyId);
		
		if (list_parents.size() == 0) {
			return;
		}
		
		for (int i = 0; i < list_parents.size(); i++) {
			String parentId = String.valueOf(list_parents.get(i).getReco_id());
			Party parent = partyService.cachePartyBy(parentId, false);
			// 已经完成KYC 获取团队收益率
			String ioeAiTeamRate = "0";
			if (parent.getKyc_authority()) {
				ioeAiTeamRate = teamRate;
			}
			System.out.println("获取上级 " + parentId + " 团队收益率 " + ioeAiTeamRate);
			if (!"0".equals(ioeAiTeamRate)) {
				double get_money = Arith.mul(profit, Double.valueOf(ioeAiTeamRate));
				System.out.println("根据静态收益" + profit + "计算出团队收益 " + get_money);
				Double recom_profit = cacheRecomProfit.get(parentId);
				cacheRecomProfit.put(parentId, Arith.add(recom_profit == null ? 0D : recom_profit, get_money));
			}
		}
	}
	
	/**
	 * 获取IoeAi团队收益
	 */
	private String getIoeAiTeamRate(String partyId, String teamRate) {
		
		double pledgeAmountSum = 0D;
		Map<String, PledgeGalaxyOrder> selfmap = (Map<String, PledgeGalaxyOrder>)findOrdersByPartyId(partyId);
		if (null != selfmap && selfmap.size() > 0) {
			for (PledgeGalaxyOrder galaxyOrder : selfmap.values()) {
				if (galaxyOrder.getStatus() == PledgeGalaxyStatusConstants.PLEDGE_SUCCESS) {
					pledgeAmountSum += galaxyOrder.getAmount();
				}
			}
		}
		
		// 伞下代理人数 是 所有下级的
		List<String> partyLists = userRecomService.findChildren(partyId);
		System.out.println(partyId + " 伞下所有下级人数 " + partyLists.size());
		int level_1_sum = 0;
		if (partyLists.size() >= 3) {
			for (String id : partyLists) {
				Party party = partyService.cachePartyBy(id, false);
				if (null != party) {
					Map<String, PledgeGalaxyOrder> map = (Map<String, PledgeGalaxyOrder>)findOrdersByPartyId(id);
					if (null != map && map.size() > 0) {
						for (PledgeGalaxyOrder galaxyOrder : map.values()) {
							if (galaxyOrder.getDays() >= 7 
									&& galaxyOrder.getStatus() == PledgeGalaxyStatusConstants.PLEDGE_SUCCESS) {
								if (Constants.SECURITY_ROLE_GUEST.equals(party.getRolename()) 
										|| party.getKyc_authority()) {
									System.out.println(partyId + " 的有效下级： " + party.getUsername());
									level_1_sum ++;
									break;
								}
							}
						}
						
						for (PledgeGalaxyOrder galaxyOrder : map.values()) {
							if (galaxyOrder.getDays() >= 7 
									&& galaxyOrder.getStatus() == PledgeGalaxyStatusConstants.PLEDGE_SUCCESS) {
								pledgeAmountSum += galaxyOrder.getAmount();
							}
						}
					}
				}
			}
		}
		
		System.out.println(partyId + "伞下所有下级的总质押额度 " + pledgeAmountSum);
		
		// 团队收益
		String[] teamSplit = teamRate.split("#");
		
		Integer levelMin = Integer.valueOf(teamSplit[0].split(";")[0]);
		String[] amountSplit = teamSplit[0].split(";")[1].split("-");
		Integer amountMin = Integer.valueOf(amountSplit[0]);
		if (level_1_sum < levelMin || pledgeAmountSum < amountMin) {
			return "0";
		}
		
		int teamLevelIndex = 0;
		for (int i = 0; i < teamSplit.length; i++) {
			Integer levelNum = Integer.valueOf(teamSplit[i].split(";")[0]);
			if (level_1_sum < levelNum) {
				break;
			}
			teamLevelIndex = i;
		}
		
		int teamAmountIndex = 0;
		for (int i = 0; i < teamSplit.length; i++) {
			String teamAmount = teamSplit[i].split(";")[1];
			String[] teamAmountSplit = teamAmount.split("-");
			Integer teamAmountMin = Integer.valueOf(teamAmountSplit[0]);
			if (pledgeAmountSum < teamAmountMin) {
				break;
			}
			teamAmountIndex = i;
		}
		
		if (teamLevelIndex <= teamAmountIndex) {
			return teamSplit[teamLevelIndex].split(";")[2];
		} else {
			return teamSplit[teamAmountIndex].split(";")[2];
		}
	}
	
	/**
	 * 获取IoeAi团队收益
	 */
	private String getNewIoeAiTeamRate(String partyId, String teamRate) {
		
		Map<String, Double> pledgeAmountMap = new HashMap<>();
		// 伞下代理人数 是 所有下级的
		List<String> partyLists = userRecomService.findChildren(partyId);
		System.out.println(partyId + " 伞下所有下级人数 " + partyLists.size());
		if (partyLists.size() >= 3) {
			for (String id : partyLists) {
				Party party = partyService.cachePartyBy(id, false);
				if (null != party) {
					Map<String, PledgeGalaxyOrder> map = (Map<String, PledgeGalaxyOrder>)findOrdersByPartyId(id);
					if (null != map && map.size() > 0) {
						for (PledgeGalaxyOrder galaxyOrder : map.values()) {
							if (galaxyOrder.getDays() >= 7 
									&& galaxyOrder.getStatus() == PledgeGalaxyStatusConstants.PLEDGE_SUCCESS) {
								if (Constants.SECURITY_ROLE_GUEST.equals(party.getRolename()) 
										|| party.getKyc_authority()) {
									double amount = pledgeAmountMap.containsKey(party.getId()) ? pledgeAmountMap.get(party.getId()) : 0;
									pledgeAmountMap.put(String.valueOf(party.getId()), Arith.add(amount, galaxyOrder.getAmount()));
								}
							}
						}
					}
				}
			}
		}
		
		int lp1 = 0;
		int lp2 = 0;
		int lp3 = 0;
		int lp4 = 0;
		int lp5 = 0;
		String[] teamRateSplit = teamRate.split("#");
		for (double amount : pledgeAmountMap.values()) {
			for (int i = 0; i < teamRateSplit.length; i++) {
				String value = teamRateSplit[i];
				String[] valueSplit = value.split(";");
				int amountMin = Integer.valueOf(valueSplit[0].split("-")[0]);
				int amountMax = Integer.valueOf(valueSplit[0].split("-")[1]);
				
				if (amount >= amountMin && amount <= amountMax) {
					if (0 == i) {
						lp1 += 1;
					} else if (1 == i) {
						lp2 += 1;
					} else if (2 == i) {
						lp3 += 1;
					} else if (3 == i) {
						lp4 += 1;
					} else if (4 == i) {
						lp5 += 1;
					}
				}
			}
		}
		
		int lp1Temp = lp1 - 3;
		if (lp1Temp < 0) {
			// 无
			System.out.println("无级别 团队收益");
			return "0";
		} else {
			lp1Temp = (int)Math.floor(lp1Temp / 3); 
			int lp2Temp = lp2 + lp1Temp - 3;
			if (lp2Temp < 0) {
				// 青铜级
				System.out.println("青铜级 团队收益");
				return teamRateSplit[0].split(";")[1];
			} else {
				lp2Temp = (int)Math.floor(lp2Temp / 3); 
				int lp3Temp = lp3 + lp2Temp - 3;
				if (lp3Temp < 0) {
					// 白银级
					System.out.println("白银级 团队收益");
					return teamRateSplit[1].split(";")[1];
				} else {
					lp3Temp = (int)Math.floor(lp3Temp / 3); 
					int lp4Temp = lp4 + lp3Temp - 3;
					if (lp4Temp < 0) {
						// 黄金级
						System.out.println("黄金级 团队收益");
						return teamRateSplit[2].split(";")[1];
					} else {
						lp4Temp = (int)Math.floor(lp4Temp / 3);
						int lp5Temp = lp5 + lp4Temp - 3;
						if (lp5Temp < 0) {
							// 铂金级
							System.out.println("铂金级 团队收益");
							return teamRateSplit[3].split(";")[1];
						} else {
							// 钻石级
							System.out.println("钻石级 团队收益");
							return teamRateSplit[4].split(";")[1];
						}
					}
				}
			}
		}
	}
	
	@Override
	public void cacheRecomProfitClear() {
		cacheRecomProfit.clear();
	}
	
	/**
	 * 根据质押状态获取订单列表
	 */
	public List<PledgeGalaxyOrder> findByStatus(int status) {
		return ApplicationUtil.executeSelect(PledgeGalaxyOrder.class,"WHERE STATUS=?",new Object[] {status});
	}
	
	/**
	 * 根据质押状态获取订单列表
	 */
	public List<PledgeGalaxyOrder> queryOrdersByStatus(String partyId, int status) {
		return ApplicationUtil.executeSelect(PledgeGalaxyOrder.class,"WHERE PARTY_ID=? AND STATUS=?",new Object[] {partyId,status});
	}
	
	/**
	 * 根据质押状态及创建日期获取订单列表
	 */
	public List<PledgeGalaxyOrder> findByStatusCrateTime(int status, Date time) {
		return ApplicationUtil.executeSelect(PledgeGalaxyOrder.class,"WHERE STATUS=? AND CREATE_TIME=?",new Object[] {status, time});
	}
	
	/**
	 * 更新质押订单
	 */
	public void update(PledgeGalaxyOrder order) {
		ApplicationUtil.executeUpdate(order);
		
		// 更新redis质押订单
		redisHandler.setSync(PledgeGalaxyRedisKeys.PLEDGE_GALAXY_ORDER + order.getId(), order);
		
		String key = PledgeGalaxyRedisKeys.PLEDGE_GALAXY_ORDER_PARTYID;
		Map<String, PledgeGalaxyOrder> maps = (Map<String, PledgeGalaxyOrder>) redisHandler.get(key + String.valueOf(order.getPartyId()));
		if (null == maps) {
			maps = new ConcurrentHashMap<String, PledgeGalaxyOrder>();
		}
		maps.put(String.valueOf(order.getId()), order);
		redisHandler.setSync(key + String.valueOf(order.getPartyId()), maps);
	}
	
	/**
	 * 批量更新订单收益
	 * 
	 * @param orderList
	 */
	protected void updateBatchGalaxyOrdersProfit(final List<GalaxyOrderMessage> orderList) {
		String sql = "UPDATE T_AUTO_MONITOR_PLEDGE_GALAXY_ORDER SET PROFIT=?,SETTLE_TIME=? WHERE UUID=?";
		int[] batchUpdate = ApplicationUtil.getBean(JdbcTemplate.class).batchUpdate(sql,new BatchPreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {
				ps.setDouble(1, orderList.get(i).getProfit());
				ps.setTimestamp(2, new Timestamp(orderList.get(i).getSettleTime().getTime()));
				ps.setString(3, orderList.get(i).getOrderId());
			}
			@Override
			public int getBatchSize() {
				return orderList.size();
			}
		});
		logger.info("end miner batch update attr:{}", batchUpdate);
	}
	
	/**
	 * 生成质押收益记录
	 */
	@Override
	public void saveGalaxyProfit(List<PledgeGalaxyOrder> orders, List<PledgeGalaxyOrder> closeList, String projectType) {
		logger.info("start compute order size:{}", orders.size());
		List<PledgeGalaxyProfit> saveGalaxyProfits = new ArrayList<>();
		
		for (PledgeGalaxyOrder order : orders) {
			
			Date date = new Date();
			// 当前时间>截止时间
			if (date.after(order.getExpireTime())) {
				// 到期赎回
				closeList.add(order);
				continue;
			}
			
			Calendar calendar = new GregorianCalendar();
			calendar.setTime(date);
			calendar.add(calendar.DATE, Integer.valueOf(2));
			Date expireTime = calendar.getTime(); 
			
			String relationOrderNo = String.valueOf(order.getId());
			String partyId = order.getPartyId();
			
			int days = order.getDays();
			double amount = order.getAmount();
			Date createTime = order.getCreateTime();
			
			Map<String, String> map = new HashMap<>();
			if (projectType.equals("DAPP_EXCHANGE")) {
				map = pledgeGalaxyConfigService.getVickersRateMap(partyId, Integer.valueOf(days), amount);
			}
			if (projectType.equals("DAPP_EXCHANGE_SAFEPAL5")) {
				 map = pledgeGalaxyConfigService.getSafePal5RateMap(partyId, Integer.valueOf(days), amount);
			}
			if (projectType.equals("DAPP_EXCHANGE_IOEAI") || projectType.equals("DAPP_EXCHANGE_DAPPDEFIUSDT")) {
				map = pledgeGalaxyConfigService.getIoeAIRateMap(partyId, Integer.valueOf(days), amount, createTime);
			}
			
			// 当日静态收益
			double staticRate = 0;
			if (map.containsKey("staticRate")) {
				staticRate = Double.valueOf(map.get("staticRate"));
			}
			// 总收益/4 保留3位小数下发
			double day_self_profit = Arith.div(Arith.mul(order.getAmount(), staticRate), 4, 3) ;
			
			PledgeGalaxyProfit selfProfit = new PledgeGalaxyProfit();
			selfProfit.setPartyId(partyId);
			selfProfit.setType(PledgeGalaxyStatusConstants.STATIC_INCOME);
			selfProfit.setAmount(day_self_profit);
			selfProfit.setStatus(PledgeGalaxyStatusConstants.PROFIT_PENDING);
			selfProfit.setExpireTime(expireTime);
			selfProfit.setCreateTime(date);
			selfProfit.setRelationOrderNo(relationOrderNo);
			saveGalaxyProfits.add(selfProfit);
			
			// 助力收益
			if (map.containsKey("dynamicRate")) {
				double dynamicRate = Double.valueOf(map.get("dynamicRate"));
				double day_dynamic_profit = Arith.div(Arith.mul(order.getAmount(), dynamicRate), 4, 3);
				PledgeGalaxyProfit dynamicProfit = new PledgeGalaxyProfit();
				dynamicProfit.setPartyId(partyId);
				dynamicProfit.setType(PledgeGalaxyStatusConstants.DYNAMIC_INCOME);
				dynamicProfit.setAmount(day_dynamic_profit);
				dynamicProfit.setStatus(PledgeGalaxyStatusConstants.PROFIT_PENDING);
				dynamicProfit.setExpireTime(expireTime);
				dynamicProfit.setCreateTime(date);
				dynamicProfit.setRelationOrderNo(relationOrderNo);
				saveGalaxyProfits.add(dynamicProfit);
			}
		}
		
		logger.info("start PledgeGalaxyProfit batch insert size:{}", saveGalaxyProfits.size());
		insertBatchGalaxyProfit(saveGalaxyProfits);
	}
	
	/**
	 * 批量新增收益记录
	 * 
	 */
	protected void insertBatchGalaxyProfit(final List<PledgeGalaxyProfit> orderList) {
		String sql = "INSERT INTO T_AUTO_MONITOR_PLEDGE_GALAXY_PROFIT(UUID,PARTY_ID,TYPE,AMOUNT,STATUS,EXPIRE_TIME,CREATE_TIME,RELATION_ORDER_NO) VALUES(?,?,?,?,?,?,?,?)";
		int[] batchUpdate = ApplicationUtil.getBean(JdbcTemplate.class).batchUpdate(sql, new BatchPreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {
				ps.setString(1, UUIDGenerator.getUUID());
				ps.setString(2, orderList.get(i).getPartyId());
				ps.setInt(3, orderList.get(i).getType());
				ps.setDouble(4, orderList.get(i).getAmount());
				ps.setInt(5, orderList.get(i).getStatus());
				ps.setTimestamp(6, new Timestamp(orderList.get(i).getExpireTime().getTime()));
				ps.setTimestamp(7, new Timestamp(orderList.get(i).getCreateTime().getTime()));
				ps.setString(8, orderList.get(i).getRelationOrderNo());
			}
			@Override
			public int getBatchSize() {
				return orderList.size();
			}
		});
		logger.info("end PledgeGalaxyProfit batch insert attr:{}", batchUpdate);
	}
	
	/**
	 * 计算订单收益
	 */
	@Override
	public void saveTeamProfit(List<PledgeGalaxyOrder> orders, String projectType) {

		for (PledgeGalaxyOrder order : orders) {
			
			if (projectType.equals("DAPP_EXCHANGE")) {
				vickersTeamProfit(order);
			}
			
			if (projectType.equals("DAPP_EXCHANGE_SAFEPAL5")) {
				safePal5TeamProfit(order);
			}
			
			if ((projectType.equals("DAPP_EXCHANGE_IOEAI") || projectType.equals("DAPP_EXCHANGE_DAPPDEFIUSDT")) && order.getDays() >= 7) {
				ioeAiTeamProfit(order);
			}
		}
	}
	
	private void vickersTeamProfit(PledgeGalaxyOrder order) {
		String partyId = order.getPartyId();
		int days = order.getDays();
		double amount = order.getAmount();
		Map<String, String> map = pledgeGalaxyConfigService.getVickersRateMap(partyId, days, amount);
		
		// 当日静态收益
		double staticRate = 0;
		if (map.containsKey("staticRate")) {
			staticRate = Double.valueOf(map.get("staticRate"));
		}
		double day_self_profit = Arith.mul(amount, staticRate);

		// 团队收益计入缓存
		if (map.containsKey("teamRate")) {
			handleRecomProfit(String.valueOf(order.getPartyId()), day_self_profit, map.get("teamRate"));
		}
	}
	
	private void safePal5TeamProfit(PledgeGalaxyOrder order) {
		String partyId = order.getPartyId();
		int days = order.getDays();
		double amount = order.getAmount();
		Map<String, String> map = pledgeGalaxyConfigService.getSafePal5RateMap(partyId, days, amount);
		
		// 当日静态收益
		double staticRate = 0;
		if (map.containsKey("staticRate")) {
			staticRate = Double.valueOf(map.get("staticRate"));
		}
		double day_self_profit = Arith.mul(amount, staticRate);

		// 团队收益计入缓存
		if (map.containsKey("teamRate")) {
			handleRecomProfit(partyId, day_self_profit, map.get("teamRate"));
		}
	}
	
	private void ioeAiTeamProfit(PledgeGalaxyOrder order) {
		
		String partyId = order.getPartyId();
		int days = order.getDays();
		double amount = order.getAmount();
		Date createTime = order.getCreateTime();
		Map<String, String> map = pledgeGalaxyConfigService.getIoeAIRateMap(partyId, days, amount, createTime);
		
		// 当日静态收益
		double staticRate = 0;
		if (map.containsKey("staticRate")) {
			staticRate = Double.valueOf(map.get("staticRate"));
		}
		double day_self_profit = Arith.mul(amount, staticRate);
		
//		if (null != createTime && createTime.before(getIoeAiTimeVersion())) {
//			handleIoeAiTeamProfit(String.valueOf(order.getPartyId()), day_self_profit, map.get("teamRate"));
//		} else {
//			newHandleIoeAiTeamProfit(String.valueOf(order.getPartyId()), day_self_profit, map.get("teamRate"));
//		}
		
		handleIoeAiTeamProfitVersion3(partyId, day_self_profit, map.get("teamRate"));
		
	}
	
	/**
	 * 最终收益持久化数据库
	 */
	@Override
	public void insertTeamProfit() {
		if (cacheRecomProfit.isEmpty())
			return;

		// 开始增加推荐人收益
		logger.info("start ------recom user Galaxy profit,date:{},count:{}",
				new Object[] { new Date(), cacheRecomProfit.size() });
		List<PledgeGalaxyProfit> teamProfits = new ArrayList<>();
		Date date = new Date();
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(date);
		calendar.add(calendar.DATE, Integer.valueOf(2));
		Date expireTime = calendar.getTime(); 
		for (Entry<String, Double> entry : cacheRecomProfit.entrySet()) {

			PledgeGalaxyProfit profit = new PledgeGalaxyProfit();
			profit.setPartyId(entry.getKey());
			profit.setType(PledgeGalaxyStatusConstants.TEAM_INCOME);
			profit.setAmount(entry.getValue());
			profit.setStatus(PledgeGalaxyStatusConstants.PROFIT_PENDING);
			profit.setExpireTime(expireTime);
			profit.setCreateTime(date);
			
			teamProfits.add(profit);

		}
		insertBatchTeamProfit(teamProfits);
		// 推荐人质押2.0收益计算完成，纪录日志
		logger.info("finish ------recom user Galaxy profit,date:{},count:{}",
				new Object[] { new Date(), cacheRecomProfit.size() });
	}
	
	/**
	 * 批量新增收益记录
	 * 
	 */
	protected void insertBatchTeamProfit(final List<PledgeGalaxyProfit> orderList) {
		String sql = "INSERT INTO T_AUTO_MONITOR_PLEDGE_GALAXY_PROFIT(UUID,PARTY_ID,TYPE,AMOUNT,STATUS,EXPIRE_TIME,CREATE_TIME) VALUES(?,?,?,?,?,?,?)";
		int[] batchUpdate = ApplicationUtil.getBean(JdbcTemplate.class).batchUpdate(sql, new BatchPreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {
				ps.setString(1, UUIDGenerator.getUUID());
				ps.setString(2, orderList.get(i).getPartyId());
				ps.setInt(3, orderList.get(i).getType());
				ps.setDouble(4, orderList.get(i).getAmount());
				ps.setInt(5, orderList.get(i).getStatus());
				ps.setTimestamp(6, new Timestamp(orderList.get(i).getExpireTime().getTime()));
				ps.setTimestamp(7, new Timestamp(orderList.get(i).getCreateTime().getTime()));
			}
			@Override
			public int getBatchSize() {
				return orderList.size();
			}
		});
		logger.info("end PledgeGalaxyProfit batch insert attr:{}", batchUpdate);
	}
	
	/**
	 * 删除订单
	 */
	public void delete(PledgeGalaxyOrder order) {
		ApplicationUtil.executeDelete(order);

		this.redisHandler.remove(PledgeGalaxyRedisKeys.PLEDGE_GALAXY_ORDER + order.getId());
		
		String key = PledgeGalaxyRedisKeys.PLEDGE_GALAXY_ORDER_PARTYID;
		Map<String, PledgeGalaxyOrder> maps = (Map<String, PledgeGalaxyOrder>) this.redisHandler.get(key + String.valueOf(order.getPartyId()));
		if (null != maps) {
			maps.remove(String.valueOf(order.getId()));
			redisHandler.setSync(key + String.valueOf(order.getPartyId()), maps);
		}
	}
	
	/**
	 * 冻结功能
	 */
	public void updateFrozen(String partyId, boolean enabled) {
		// 是否锁定，如果锁定可以登录、查看，但不能操作业务有关。
		// true 不锁定
		if (enabled) {
			List<PledgeGalaxyOrder> orders = queryOrdersByStatus(partyId, PledgeGalaxyStatusConstants.PLEDGE_FROZEN);
			for (PledgeGalaxyOrder order : orders) {
				order.setStatus(PledgeGalaxyStatusConstants.PLEDGE_SUCCESS);
				update(order);
			}
		} 
		// false 锁定
		else {
			List<PledgeGalaxyOrder> orders = queryOrdersByStatus(partyId, PledgeGalaxyStatusConstants.PLEDGE_SUCCESS);
			for (PledgeGalaxyOrder order : orders) {
				order.setStatus(PledgeGalaxyStatusConstants.PLEDGE_FROZEN);
				update(order);
			}
		}
	}
	
	private Date getIoeAiTimeVersion() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String version = "2022-08-28 00:00:00";
		Date time = new Date();
	    try {
	    	time = sdf.parse(version);
		} catch (ParseException e) {
			e.printStackTrace();
		}
	    return time;
	}
	
	public void setWalletService(WalletService walletService) {
		this.walletService = walletService;
	}

	public void setMoneyLogService(MoneyLogService moneyLogService) {
		this.moneyLogService = moneyLogService;
	}

	public void setRedisHandler(RedisHandler redisHandler) {
		this.redisHandler = redisHandler;
	}

	public void setUserRecomService(UserRecomService userRecomService) {
		this.userRecomService = userRecomService;
	}

	public void setPartyService(PartyService partyService) {
		this.partyService = partyService;
	}

	public void setPledgeGalaxyConfigService(PledgeGalaxyConfigService pledgeGalaxyConfigService) {
		this.pledgeGalaxyConfigService = pledgeGalaxyConfigService;
	}

	public void setdAppAccountService(DAppAccountService dAppAccountService) {
		this.dAppAccountService = dAppAccountService;
	}

	public void setTipService(TipService tipService) {
		this.tipService = tipService;
	}

	public void setPledgeGalaxyProfitService(PledgeGalaxyProfitService pledgeGalaxyProfitService) {
		this.pledgeGalaxyProfitService = pledgeGalaxyProfitService;
	}
}
