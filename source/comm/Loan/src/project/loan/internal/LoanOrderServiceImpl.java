package project.loan.internal;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;

import kernel.bo.RecordObjectMapper;
import kernel.exception.BusinessException;
import kernel.util.Arith;
import kernel.util.DateUtils;
import kernel.util.StringUtils;
import kernel.web.ApplicationUtil;
import kernel.web.Page;
import kernel.web.PagedQueryDao;
import project.Constants;
import project.data.DataService;
import project.data.model.Realtime;
import project.loan.LoanConstants;
import project.loan.LoanOrder;
import project.loan.LoanOrderService;
import project.loan.LoanRelationOrder;
import project.loan.LoanRelationOrderService;
import project.log.MoneyLog;
import project.log.MoneyLogService;
import project.syspara.SysparaService;
import project.wallet.Wallet;
import project.wallet.WalletExtend;
import project.wallet.WalletService;
import util.DateUtil;
import util.RandomUtil;

/**
 * 质押借币service实现类
 */
public class LoanOrderServiceImpl implements LoanOrderService {
	
	private DataService dataService;
	
	private JdbcTemplate jdbcTemplate;
	
	private PagedQueryDao pagedQueryDao;
	
	private WalletService walletService;
	
	private SysparaService sysparaService;
	
	private MoneyLogService moneyLogService;
	
	private LoanRelationOrderService loanRelationOrderService;
	
	private Logger logger = LoggerFactory.getLogger(LoanOrderServiceImpl.class);
	
	protected Map<String, LoanOrder> cache = new ConcurrentHashMap<String, LoanOrder>();
	
	public void init() {
		List<LoanOrder> list = queryOrdersByState();
		for (LoanOrder order : list) {
			cache.put(order.getId(), order);
		}
	}
	
	/**
	 * 新增借币订单
	 */
	public void saveLoanOrder(LoanOrder order) {
		
		WalletExtend walletExtend = walletService.saveExtendByPara(order.getPartyId(), order.getPledgeCurrency());
		if (walletExtend.getAmount() < order.getPledgeAmount()) {
			throw new BusinessException(1, "Insufficient balance");
		}
		
		double amountBeforeExtend = walletExtend.getAmount();
		double pledgeAmount = order.getPledgeAmount();
		String partyId = walletExtend.getPartyId().toString();
		// 修改拓展钱包 余额 及冻结余额
		walletService.updateExtend(partyId, walletExtend.getWallettype(), Arith.sub(0, pledgeAmount), pledgeAmount);

		String orderNo = DateUtil.getToday("yyMMddHHmmss") + RandomUtil.getRandomNum(8);
		
		// 冻结资金日志
		MoneyLog logExtend = new MoneyLog();
		logExtend.setCategory(Constants.MONEYLOG_CATEGORY_LOAN);
		logExtend.setAmount_before(amountBeforeExtend);
		logExtend.setAmount(pledgeAmount);
		logExtend.setAmount_after(Arith.add(amountBeforeExtend, Arith.sub(0, pledgeAmount)));
		logExtend.setLog("质押借币，订单号[" + orderNo + "]，" + "冻结：" + pledgeAmount);
		logExtend.setPartyId(partyId);
		logExtend.setWallettype(order.getPledgeCurrency());
		logExtend.setContent_type(Constants.MONEYLOG_CONTENT_LOAN_FROZEN);
		logExtend.setCreateTime(new Date());
		moneyLogService.save(logExtend);
		
		Wallet wallet = walletService.saveWalletByPartyId(partyId);
		double amountBefore = wallet.getMoney();
		double loanAmount = order.getLoanAmount();
		// 修改主钱包余额
		walletService.update(partyId, loanAmount);
		
		// 借款资金日志
		MoneyLog moneylog = new MoneyLog();
		moneylog.setCategory(Constants.MONEYLOG_CATEGORY_LOAN);
		moneylog.setAmount_before(amountBefore);
		moneylog.setAmount(loanAmount);
		moneylog.setAmount_after(Arith.add(amountBefore, loanAmount));
		moneylog.setLog("质押借币，订单号[" + orderNo + "]，" + "借款：" + loanAmount);
		moneylog.setPartyId(partyId);
		moneylog.setWallettype(Constants.WALLET);
		moneylog.setContent_type(Constants.MONEYLOG_CONTENT_LOAN_ADD);
		moneylog.setCreateTime(new Date());
		moneyLogService.save(moneylog);
		
		// 质押借币记录
		order.setOrderNo(orderNo);
		order.setOrderType(LoanConstants.PLEDGE_ORDER_TYPE_LOAN);
		order.setState(LoanConstants.PLEDGE_ORDER_STATE_CALCULATE);
		order.setHourlyRate(Double.valueOf(getLoanConfig().get("hourlyRate").toString()));
		order.setDebtAmount(loanAmount);
		order.setPledgeType(LoanConstants.PLEDGE_TYPE);
		order.setLoanCurrency("usdt");
		order.setOverdueRate(Double.valueOf(getLoanConfig().get("overdueRate").toString()));
		
		Date date = new Date();
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(date);
		calendar.add(Calendar.DATE, order.getLoanCycle());
		Date expireTime = calendar.getTime();
		order.setCreateTime(date);
		order.setExpireTime(expireTime);
		
		String insertSql = "INSERT INTO T_LOAN_ORDER(UUID,ORDER_NO,PARTY_ID,ORDER_TYPE,LOAN_AMOUNT,STATE,LOAN_CURRENCY,PLEDGE_CURRENCY,"
				+ "PLEDGE_AMOUNT,PLEDGE_RATE,PLEDGE_TYPE,DEBT_AMOUNT,INTEREST_AMOUNT,OVERDUE_AMOUNT,OVERDUE_RATE,HOURLY_RATE,LOAN_CYCLE,"
				+ "CREATE_TIME,EXPIRE_TIME) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		jdbcTemplate.update(insertSql,order.getId(),order.getOrderNo(),order.getPartyId(),order.getOrderType(),order.getLoanAmount(),
				order.getState(),order.getLoanCurrency(),order.getPledgeCurrency(),order.getPledgeAmount(),order.getPledgeRate(),order.getPledgeType(),
				order.getDebtAmount(),order.getInterestAmount(),order.getOverdueAmount(),order.getOverdueRate(),order.getHourlyRate(),
				order.getLoanCycle(),order.getCreateTime(),order.getExpireTime());
		// 放入缓存
		cache.put(order.getId(), order);
	}
	
	/**
	 * 修改质押借币订单
	 */
	public void updateLoanOrder(LoanOrder order) {
		String updateSql = "UPDATE T_LOAN_ORDER SET LOAN_AMOUNT=?,STATE=?,LOAN_CURRENCY=?,PLEDGE_CURRENCY=?,PLEDGE_AMOUNT=?,PLEDGE_RATE=?,"
				+ "PLEDGE_TYPE=?,DEBT_AMOUNT=?,INTEREST_AMOUNT=?,OVERDUE_AMOUNT=?,OVERDUE_RATE=?,HOURLY_RATE=?,LOAN_CYCLE=?,EXPIRE_TIME=?"
				+ " WHERE UUID=?";
		jdbcTemplate.update(updateSql,order.getLoanAmount(),order.getState(),order.getLoanCurrency(),order.getPledgeCurrency(),
				order.getPledgeAmount(),order.getPledgeRate(),order.getPledgeType(),order.getDebtAmount(),order.getInterestAmount(),order.getOverdueAmount(),
				order.getOverdueRate(),order.getHourlyRate(),order.getLoanCycle(),order.getExpireTime(),order.getId());
	}
	
	/**
	 * 质押借币订单
	 */
	public List<Map<String, Object>> pagedQuery(int pageNo, int pageSize, String partyId) {
		if (pageNo <= 0) pageNo = 1;
		Page page = new Page(pageNo, pageSize, Integer.MAX_VALUE);
		List<LoanOrder> orders = ApplicationUtil.executeSelect(LoanOrder.class,"WHERE PARTY_ID=? ORDER BY CREATE_TIME DESC LIMIT ?,?",new Object[] {partyId,page.getFirstElementNumber(),pageSize});
		
		// 借款记录
		List<Map<String, Object>> list = new ArrayList<>();
		for (LoanOrder order : orders) {
			Map<String, Object> map = new HashMap<>();
			map.put("id", order.getId());
			map.put("orderType", order.getOrderType());
			map.put("loanAmount", order.getLoanAmount());
			map.put("state", order.getState());
			map.put("loanCurrency", order.getLoanCurrency());
			if (LoanConstants.PLEDGE_ORDER_STATE_CALCULATE == order.getState() || LoanConstants.PLEDGE_ORDER_STATE_OVERDUE == order.getState()) {
				List<LoanRelationOrder> relations = loanRelationOrderService.queryOrders(order.getId(), LoanConstants.PLEDGE_ORDER_TYPE_REPLENISH);
				double pledgeAmount = order.getPledgeAmount();
				for (LoanRelationOrder relationOrder : relations) {
					pledgeAmount = Arith.add(pledgeAmount, relationOrder.getPledgeAmount());
				}
				
				Map<String, Double> calculateMap = calculatePledgeRate(order.getPledgeCurrency(), order.getDebtAmount(), pledgeAmount);
				map.put("pledgeRate", calculateMap.get("pledgeRate"));
			}else {
				map.put("pledgeRate", order.getPledgeRate());
			}
			map.put("debtAmount", order.getDebtAmount());
			list.add(map);		
		}
		return list;
	}
	
	/**
	 * 根据订单关联ID获取订单列表
	 */
	public List<LoanOrder> queryOrdersByState() {
		List<LoanOrder> list = jdbcTemplate.query("SELECT * FROM T_LOAN_ORDER WHERE STATE = 1 OR STATE = 4", 
				RecordObjectMapper.newInstance(LoanOrder.class));
		return list;
	}
	
	/**
	 * 根据订单关联ID获取订单列表
	 */
	public List<String> queryOrdersNoticeList(String partyId) {
		List<LoanOrder> list = jdbcTemplate.query("SELECT * FROM T_LOAN_ORDER WHERE (STATE = 1 OR STATE = 4) AND PARTY_ID =?", 
				RecordObjectMapper.newInstance(LoanOrder.class), partyId);
		List<String> noticeList = new ArrayList<>();
		for (LoanOrder order : list) {
			Map<String, Double> calculateMap = calculatePledgeRate(order.getPledgeCurrency(), order.getDebtAmount(), order.getPledgeAmount());
			if (Double.valueOf(calculateMap.get("pledgeRate").toString()) >= LoanConstants.PLEDGE_RATE_NOTICE) {
				noticeList.add(order.getId());
			}
		}	
		return noticeList;
	}
	
	/**
	 * 从缓存中获取订单
	 */
	public List<LoanOrder> cacheOrders() {
		return new ArrayList<LoanOrder>(cache.values());
	}
	
	/**
	 * 存入缓存
	 */
	public void addCacheOrder(LoanOrder order) {
		cache.put(order.getId(), order);
	}
	
	/**
	 * 从缓存中移除订单
	 */
	public void removeCacheOrder(String orderId) {
		cache.remove(orderId);
	}
	
	/**
	 * 从缓存中移除订单
	 */
	public void cacheRemoveOrders(List<String> orderIds) {
		for (String orderId : orderIds) {
			cache.remove(orderId);
		}
	}
	
	/**
	 * 订单详情
	 */
	public Map<String, Object> getLoanOrder(String id) {
		LoanOrder order = getLoanOrderParam(id);
		if (null == order) {
			throw new BusinessException(1, "order is unknown");
		}
		Map<String, Object> map = new HashMap<>();
		map.put("id", order.getId());
		map.put("state", order.getState());
		map.put("debtAmount", order.getDebtAmount());
		map.put("loanCurrency", order.getLoanCurrency());
		map.put("pledgeCurrency", order.getPledgeCurrency());
		map.put("loanAmount", order.getLoanAmount());
		map.put("interestAmount", order.getInterestAmount());
		map.put("hourlyRate", order.getHourlyRate());
		map.put("orderNo", order.getOrderNo());
		map.put("createTime", DateUtils.format(order.getCreateTime(), DateUtils.DF_yyyyMMddHHmmss));
		map.put("expireTime", DateUtils.format(order.getExpireTime(), DateUtils.DF_yyyyMMddHHmmss));
		
		// 计息中、已逾期
		if (LoanConstants.PLEDGE_ORDER_STATE_CALCULATE == order.getState() 
				|| LoanConstants.PLEDGE_ORDER_STATE_OVERDUE == order.getState()) {
			
			List<LoanRelationOrder> relations = loanRelationOrderService.queryOrders(order.getId(), LoanConstants.PLEDGE_ORDER_TYPE_REPLENISH);
			double pledgeAmount = order.getPledgeAmount();
			for (LoanRelationOrder relationOrder : relations) {
				pledgeAmount = Arith.add(pledgeAmount, relationOrder.getPledgeAmount());
			}
			
			Map<String, Double> calculateMap = calculatePledgeRate(order.getPledgeCurrency(), order.getDebtAmount(), pledgeAmount);
			map.put("pledgeRate", calculateMap.get("pledgeRate"));
			map.put("closeOut", calculateMap.get("closeOut"));
			map.put("overdueAmount", order.getOverdueAmount());
			map.put("overdueRate", order.getOverdueRate());
		} 
		return map;
	}
	
	/**
	 * 订单
	 */
	public LoanOrder getLoanOrderParam(String id) {
		List<LoanOrder> list = jdbcTemplate.query("SELECT * FROM T_LOAN_ORDER WHERE UUID=?", RecordObjectMapper.newInstance(LoanOrder.class), id);
		if (null != list && list.size() > 0) {
			return list.get(0);
        }
		return null;
	}
	
	/**
	 * 获取配置
	 */
	public Map<String, Object> getLoanConfig() {
		String config = sysparaService.find("exchange_loan").getValue();
		Map<String, Object> map = new HashMap<>();
		String[] configSplit = config.split("\\|");
		map.put("hourlyRate", configSplit[0]);
		map.put("loanCycle", configSplit[1].split(":"));
		map.put("loanAmountMin", configSplit[3]);
		map.put("overdueRate", configSplit[2]);
		return map;
	}
	
	/**
	 * 分页查询 计息中、已逾期的订单
	 */
	public Page pagedQueryInterestOrder(int pageNo, int pageSize, Date date) {
		if (pageNo <= 0) pageNo = 1;
		Page page = new Page(pageNo, pageSize, Integer.MAX_VALUE);
		List<LoanOrder> list=ApplicationUtil.executeSelect(LoanOrder.class,"WHERE STATE=1 OR STATE=4 AND CREATE_TIME<=? ORDER BY CREATE_TIME DESC LIMIT ?,?",new Object[] {date,page.getFirstElementNumber(),pageSize});
		page.setElements(list);
		return page;
	}
	
	/**
	 * 定时计算利息
	 */
	public void updateInterest(List<LoanOrder> orders) {
		List<LoanOrder> updateOrders = new ArrayList<>();
		Date date = new Date();
		for (LoanOrder order : orders) {
			// 还款记录
			List<LoanRelationOrder> relationList = loanRelationOrderService.queryOrders(order.getId(), LoanConstants.PLEDGE_ORDER_TYPE_REPAY);
			double repayAmount = 0;
			for (LoanRelationOrder relation : relationList ) {
				repayAmount += relation.getLoanAmount();
			}
			double loanAmountSum = Arith.sub(order.getLoanAmount(), repayAmount);
			// 借款利息
			double interestAmount = Arith.mul(loanAmountSum, order.getHourlyRate());
			// 总利息
			order.setInterestAmount(Arith.add(order.getInterestAmount(), interestAmount));
			// 总负债
			order.setDebtAmount(Arith.add(order.getDebtAmount(), interestAmount));
			// 到期
			if (LoanConstants.PLEDGE_ORDER_STATE_CALCULATE == order.getState() && date.after(order.getExpireTime())) {
				order.setState(LoanConstants.PLEDGE_ORDER_STATE_OVERDUE);
			}
			// 已逾期
			if (LoanConstants.PLEDGE_ORDER_STATE_OVERDUE == order.getState()) {
				// 逾期扣款
				double overdueAmount = Arith.mul(loanAmountSum, order.getOverdueRate());
				// 总逾期
				order.setOverdueAmount(Arith.add(order.getOverdueAmount(), overdueAmount));
				// 总负债
				order.setDebtAmount(Arith.add(order.getDebtAmount(), overdueAmount));
			}
			updateOrders.add(order);
		}
		
		// 批量更新
		updateBatchOrder(updateOrders);
	}
	
	/**
	 * 批量新增收益记录
	 */
	protected void updateBatchOrder(final List<LoanOrder> orderList) {
		String sql = "UPDATE T_LOAN_ORDER SET INTEREST_AMOUNT = ?, DEBT_AMOUNT = ?, STATE = ?, OVERDUE_AMOUNT = ? WHERE UUID=?";
		int[] batchUpdate = jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {
				ps.setDouble(1, orderList.get(i).getInterestAmount());
				ps.setDouble(2, orderList.get(i).getDebtAmount());
				ps.setInt(3, orderList.get(i).getState());
				ps.setDouble(4, orderList.get(i).getOverdueAmount());
				ps.setString(5, orderList.get(i).getId());
			}
			@Override
			public int getBatchSize() {
				return orderList.size();
			}
		});
		logger.info("end loan batch update attr:{}", batchUpdate);
	}
	
	
	/**
	 * 后台质押借币订单
	 */
	public Page pagedQueryAdmin(int pageNo, int pageSize, String userParam, String orderNo, String roleName, String state) {
		Map<String, Object> parameters = new HashMap<>();
		StringBuffer queryString = new StringBuffer(
				" SELECT loan.UUID id, loan.ORDER_NO orderNo, loan.PARTY_ID partyId, "
				+ "loan.ORDER_TYPE orderType, loan.LOAN_AMOUNT loanAmount, loan.STATE state, loan.LOAN_CURRENCY loanCurrency, "
				+ "loan.PLEDGE_CURRENCY pledgeCurrency, loan.PLEDGE_AMOUNT pledgeAmount, loan.PLEDGE_TYPE pledgeType, loan.PLEDGE_RATE pledgeRate, "
				+ "loan.DEBT_AMOUNT debtAmount, loan.INTEREST_AMOUNT interestAmount, loan.HOURLY_RATE hourlyRate, loan.LOAN_CYCLE loanCycle, "
				+ "loan.CREATE_TIME createTime, loan.EXPIRE_TIME expireTime, ");
		queryString.append(" party.USERNAME username,party.USERCODE usercode,party.ROLENAME rolename ");
		queryString.append(" FROM T_LOAN_ORDER loan ");
		queryString.append(" LEFT JOIN PAT_PARTY party ON loan.PARTY_ID = party.UUID ");
		queryString.append(" WHERE 1 = 1 ");
		
		if (!StringUtils.isNullOrEmpty(userParam)) {
			queryString.append("AND (party.USERNAME like:userParam OR party.USERCODE like:userParam ) ");
			parameters.put("userParam", "%" + userParam + "%");
		}
		
		if (!StringUtils.isNullOrEmpty(orderNo)) {
			queryString.append(" and loan.ORDER_NO =:orderNo ");
			parameters.put("orderNo", orderNo);
		}
		
		if (!StringUtils.isNullOrEmpty(state)) {
			queryString.append(" and loan.STATE =:state ");
			parameters.put("state", state);
		}

		if (!StringUtils.isNullOrEmpty(roleName)) {
			queryString.append(" and party.ROLENAME =:rolename");
			parameters.put("rolename", roleName);
		}
		
		queryString.append(" order by loan.CREATE_TIME desc ");

		Page page = this.pagedQueryDao.pagedQuerySQL(pageNo, pageSize, queryString.toString(), parameters);
		return page;
	}
	
	/**
	 * 根据订单总负债 计算质押率
	 */
	public Map<String, Double> calculatePledgeRate(String pledgeCurrency, double debtAmount, double pledgeAmount) {
		List<Realtime> realtimes = dataService.realtime(pledgeCurrency);
//		Map<String, Object> config = getLoanConfig();
//		double hourlyRate = Double.valueOf(config.get("hourlyRate").toString());
//		// 质押率 = (借款金额 + (借款金额 * 时利率 * 24 * 借款周期)) / 质押币的价格  / 质押数量
//		// 质押率 = (借款金额 + (借款金额 * 时利率 * 24 * 借款周期)) / 质押币的价格  / 质押数量11
//		double interestAmount = Arith.mul(loanAmount, hourlyRate * 24 * loanCycle);
//		double debtAmount = Arith.add(loanAmount, interestAmount);
		Map<String, Double> map = new HashMap<>();
		map.put("pledgeRate", Arith.div(Arith.div(debtAmount, realtimes.get(0).getClose()), pledgeAmount, 2));
		map.put("closeOut", Arith.div(Arith.div(debtAmount, LoanConstants.PLEDGE_RATE_CLOSEOUT), pledgeAmount, 2));
		return map;
	}
	
	/**
	 * 系统强平
	 */
	public void updateCloseout(LoanOrder order, double pledgeRate) {
		WalletExtend walletExtend = walletService.saveExtendByPara(order.getPartyId(), order.getPledgeCurrency());
		double pledgeAmount = order.getPledgeAmount();
		List<LoanRelationOrder> relationOrderList = loanRelationOrderService.queryOrders(order.getId(), LoanConstants.PLEDGE_ORDER_TYPE_REPLENISH);
		for (LoanRelationOrder relation : relationOrderList) {
			// 补增质押
			pledgeAmount = Arith.add(relation.getPledgeAmount(), pledgeAmount);
		}
		
		double amountBeforeExtend = walletExtend.getAmount();
		String partyId = walletExtend.getPartyId().toString();
		// 修改拓展钱包 冻结余额
		walletService.updateExtend(partyId, walletExtend.getWallettype(), 0, Arith.sub(0, pledgeAmount));

		// 强平资金日志
		MoneyLog logExtend = new MoneyLog();
		logExtend.setCategory(Constants.MONEYLOG_CATEGORY_LOAN);
		logExtend.setAmount_before(amountBeforeExtend);
		logExtend.setAmount(pledgeAmount);
		logExtend.setAmount_after(Arith.add(amountBeforeExtend, Arith.sub(0, pledgeAmount)));
		logExtend.setLog("质押借币，订单号[" + order.getOrderNo() + "]，" + "强平：" + pledgeAmount);
		logExtend.setPartyId(partyId);
		logExtend.setWallettype(order.getPledgeCurrency());
		logExtend.setContent_type(Constants.MONEYLOG_CONTENT_LOAN_CLOSEOUT);
		logExtend.setCreateTime(new Date());
		moneyLogService.save(logExtend);
		
		order.setPledgeRate(pledgeRate);
		order.setState(LoanConstants.PLEDGE_ORDER_STATE_CLOSEOUT);
		order.setOrderType(LoanConstants.PLEDGE_ORDER_TYPE_CLOSEOUT);
		updateLoanOrder(order);
	}
	
	public void setPagedQueryDao(PagedQueryDao pagedQueryDao) {
		this.pagedQueryDao = pagedQueryDao;
	}

	public void setWalletService(WalletService walletService) {
		this.walletService = walletService;
	}

	public void setSysparaService(SysparaService sysparaService) {
		this.sysparaService = sysparaService;
	}

	public void setMoneyLogService(MoneyLogService moneyLogService) {
		this.moneyLogService = moneyLogService;
	}

	public void setDataService(DataService dataService) {
		this.dataService = dataService;
	}

	public void setLoanRelationOrderService(LoanRelationOrderService loanRelationOrderService) {
		this.loanRelationOrderService = loanRelationOrderService;
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}
	
}
