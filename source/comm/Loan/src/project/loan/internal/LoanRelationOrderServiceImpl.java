package project.loan.internal;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.jdbc.core.JdbcTemplate;

import kernel.bo.RecordObjectMapper;
import kernel.exception.BusinessException;
import kernel.util.Arith;
import kernel.util.DateUtils;
import kernel.util.UUIDGenerator;
import kernel.web.ApplicationUtil;
import kernel.web.Page;
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
import project.wallet.Wallet;
import project.wallet.WalletExtend;
import project.wallet.WalletService;

public class LoanRelationOrderServiceImpl implements LoanRelationOrderService {
	
	private DataService dataService;
	
	private JdbcTemplate jdbcTemplate;
	
	private WalletService walletService;
	
	private MoneyLogService moneyLogService;
	
	private LoanOrderService loanOrderService;
	
	/**
	 * 新增质押借币关联订单
	 */
	public void saveLoanRelationOrder(LoanRelationOrder relationOrder) {
		relationOrder.setId(UUIDGenerator.getUUID());
		relationOrder.setPledgeType(LoanConstants.PLEDGE_TYPE);
		relationOrder.setOrderType(LoanConstants.PLEDGE_ORDER_TYPE_LOAN);
		relationOrder.setLoanCurrency("usdt");
		relationOrder.setCreateTime(new Date());
		this.insertLoanRelationOrder(relationOrder);
	}
	
	/**
	 * 质押记录
	 */
	public List<Map<String, Object>> pagedQueryPledge(int pageNo, int pageSize, String loanRelationOrderId) {
		if (pageNo <= 0) pageNo = 1;
		Page page = new Page(pageNo, pageSize, Integer.MAX_VALUE);
		
		List<LoanRelationOrder> orders = ApplicationUtil.executeSelect(LoanRelationOrder.class,"WHERE LOAN_RELATION_ORDER_ID=? ORDER BY CREATE_TIME DESC LIMIT ?,?",new Object[] {loanRelationOrderId,page.getFirstElementNumber(),pageSize});
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		for (LoanRelationOrder order : orders) {
			Map<String, Object> map = new HashMap<>();
			// 借款
			if (LoanConstants.PLEDGE_ORDER_TYPE_LOAN == order.getOrderType()) {
				map.put("orderType", order.getOrderType());
				map.put("loanAmount", order.getLoanAmount());
				map.put("pledgeType", order.getPledgeType());
				map.put("pledgeAmount", order.getPledgeAmount());
				map.put("pledgeCurrency", order.getPledgeCurrency());
				map.put("createTime", DateUtils.format(order.getCreateTime(), DateUtils.DF_yyyyMMddHHmmss));
			}
			// 续借、强平、还款
			if (LoanConstants.PLEDGE_ORDER_TYPE_REFURBISH == order.getOrderType() 
					|| LoanConstants.PLEDGE_ORDER_TYPE_CLOSEOUT == order.getOrderType()
					|| LoanConstants.PLEDGE_ORDER_TYPE_REPAY == order.getOrderType()) {
				map.put("orderType", order.getOrderType());
				map.put("loanAmount", order.getLoanAmount());
				map.put("createTime", DateUtils.format(order.getCreateTime(), DateUtils.DF_yyyyMMddHHmmss));
			}
			// 补增质押
			if (LoanConstants.PLEDGE_ORDER_TYPE_REPLENISH == order.getOrderType()) {
				map.put("orderType", order.getOrderType());
				map.put("pledgeType", order.getPledgeType());
				map.put("pledgeAmount", order.getPledgeAmount());
				map.put("pledgeCurrency", order.getPledgeCurrency());
				map.put("createTime", DateUtils.format(order.getCreateTime(), DateUtils.DF_yyyyMMddHHmmss));
			}
			if(ObjectUtils.isNotEmpty(map)) {
				list.add(map);
			}		
		}
		return list;
	}
	
	/**
	 * 补增质押
	 */
	public void saveReplenishOrder(String id, double pledgeAmount, String partyId) {
		LoanOrder order = loanOrderService.getLoanOrderParam(id);
		if (null == order) {
			throw new BusinessException(1, "order is unknown");
		}
		
		if (LoanConstants.PLEDGE_ORDER_STATE_CALCULATE != order.getState()) {
			throw new BusinessException(1, "order state error");
		}
		
		WalletExtend walletExtend = walletService.saveExtendByPara(partyId, order.getPledgeCurrency());
		if (walletExtend.getAmount() < pledgeAmount) {
			throw new BusinessException(1, "Insufficient balance");
		}
		
		double amountBeforeExtend = walletExtend.getAmount();
		// 修改拓展钱包 余额 及冻结余额
		walletService.updateExtend(partyId, walletExtend.getWallettype(), Arith.sub(0, pledgeAmount), pledgeAmount);

		// 保存资金日志
		MoneyLog logExtend = new MoneyLog();
		logExtend.setCategory(Constants.MONEYLOG_CATEGORY_LOAN);
		logExtend.setAmount_before(amountBeforeExtend);
		logExtend.setAmount(pledgeAmount);
		logExtend.setAmount_after(Arith.add(amountBeforeExtend, Arith.sub(0, pledgeAmount)));
		logExtend.setLog("补增质押，订单号[" + order.getOrderNo() + "]，" + "冻结：" + pledgeAmount);
		logExtend.setPartyId(partyId);
		logExtend.setWallettype(order.getPledgeCurrency());
		logExtend.setContent_type(Constants.MONEYLOG_CONTENT_LOAN_FROZEN);
		logExtend.setCreateTime(new Date());
		moneyLogService.save(logExtend);
		
		// 保存质押记录
		LoanRelationOrder replenish = new LoanRelationOrder();
		replenish.setId(UUIDGenerator.getUUID());
		replenish.setLoanRelationOrderId(id);
		replenish.setPartyId(partyId);
		replenish.setOrderType(LoanConstants.PLEDGE_ORDER_TYPE_REPLENISH);
		replenish.setPledgeType(LoanConstants.PLEDGE_TYPE);
		replenish.setPledgeCurrency(order.getPledgeCurrency());
		replenish.setPledgeAmount(pledgeAmount);
		replenish.setCreateTime(new Date());
		this.insertLoanRelationOrder(replenish);
	}
	
	/**
	 * 续借
	 */
	public void saveRefurbishOrder(String id, double loanAmountRq, String partyId) {
		LoanOrder order = loanOrderService.getLoanOrderParam(id);
		if (null == order) {
			throw new BusinessException(1, "order is unknown");
		}
		
		if (LoanConstants.PLEDGE_ORDER_STATE_CALCULATE != order.getState()) {
			throw new BusinessException(1, "order state error");
		}
		
		double pledgeAmount = order.getPledgeAmount();
		String pledgeCurrency = order.getPledgeCurrency();
		int loanCycle = order.getLoanCycle();
		
		// 质押率 = (借款金额 + (借款金额 * 时利率 * 24 * 借款周期)) / 质押币的价格  / 质押数量
		List<Realtime> realtimes = dataService.realtime(pledgeCurrency);
		double debtAmount = Arith.mul(realtimes.get(0).getClose(), pledgeAmount) * LoanConstants.PLEDGE_RATE_INITIAL;
		double rate = Arith.add(Arith.mul(order.getHourlyRate() * 24, loanCycle), 1);
		double loanAmountMax = Arith.div(debtAmount, rate, 2);
		double loanAmountSub = Arith.sub(loanAmountMax, order.getLoanAmount());
		if (loanAmountSub < loanAmountRq) {
			throw new BusinessException(1, "Maximum Borrow: " + new BigDecimal(loanAmountSub).setScale(2, RoundingMode.DOWN));
		}
		
		Wallet wallet = walletService.saveWalletByPartyId(partyId);
		double amountBefore = wallet.getMoney();
		// 修改主钱包余额
		walletService.update(partyId, loanAmountRq);
		
		// 保存资金日志
		MoneyLog moneylog = new MoneyLog();
		moneylog.setCategory(Constants.MONEYLOG_CATEGORY_LOAN);
		moneylog.setAmount_before(amountBefore);
		moneylog.setAmount(loanAmountRq);
		moneylog.setAmount_after(Arith.add(amountBefore, loanAmountRq));
		moneylog.setLog("质押借币，订单号[" + order.getOrderNo() + "]，" + "借款：" + loanAmountRq);
		moneylog.setPartyId(partyId);
		moneylog.setWallettype(Constants.WALLET);
		moneylog.setContent_type(Constants.MONEYLOG_CONTENT_LOAN_ADD);
		moneylog.setCreateTime(new Date());
		moneyLogService.save(moneylog);

		LoanRelationOrder refurbish = new LoanRelationOrder();
		refurbish.setId(UUIDGenerator.getUUID());
		refurbish.setLoanRelationOrderId(id);
		refurbish.setPartyId(partyId);
		refurbish.setOrderType(LoanConstants.PLEDGE_ORDER_TYPE_REFURBISH);
		refurbish.setLoanAmount(loanAmountRq);
		refurbish.setCreateTime(new Date());
		this.insertLoanRelationOrder(refurbish);
		
		// 修改质押借币单
		order.setDebtAmount(Arith.add(order.getDebtAmount(), loanAmountRq));
		order.setLoanAmount(Arith.add(order.getLoanAmount(), loanAmountRq));
		loanOrderService.updateLoanOrder(order);
		
		// 更新缓存
		loanOrderService.addCacheOrder(order);
	}
	
	/**
	 * 还款
	 */
	public void saveRepayOrder(String id, double repayAmount, String partyId) {
		LoanOrder order = loanOrderService.getLoanOrderParam(id);
		if (null == order) {
			throw new BusinessException(1, "order is unknown");
		}
		
		if (LoanConstants.PLEDGE_ORDER_STATE_SETTLE == order.getState() 
				|| LoanConstants.PLEDGE_ORDER_STATE_CLOSEOUT == order.getState()) {
			throw new BusinessException(1, "order state error");
		}
		
		if (repayAmount > order.getDebtAmount()) {
			throw new BusinessException(1, "wrong repayment amount");
		}
		
		// 解冻
		if (repayAmount >= order.getDebtAmount()) {
			WalletExtend walletExtend = walletService.saveExtendByPara(order.getPartyId(), order.getPledgeCurrency());
			
			List<LoanRelationOrder> list = queryOrders(id, LoanConstants.PLEDGE_ORDER_TYPE_REPLENISH);
			double pledgeAmount = order.getPledgeAmount();
			for (LoanRelationOrder relationOrder : list) {
				pledgeAmount = Arith.add(pledgeAmount, relationOrder.getPledgeAmount());
			}
			
			double amountBeforeExtend = walletExtend.getAmount();
			// 修改拓展钱包 余额 及冻结余额
			walletService.updateExtend(partyId, walletExtend.getWallettype(), pledgeAmount, Arith.sub(0, pledgeAmount));

			// 保存资金日志
			MoneyLog logExtend = new MoneyLog();
			logExtend.setCategory(Constants.MONEYLOG_CATEGORY_LOAN);
			logExtend.setAmount_before(amountBeforeExtend);
			logExtend.setAmount(pledgeAmount);
			logExtend.setAmount_after(Arith.add(amountBeforeExtend, pledgeAmount));
			logExtend.setLog("质押借币，订单号[" + order.getOrderNo() + "]，" + "解冻：" + pledgeAmount);
			logExtend.setPartyId(partyId);
			logExtend.setWallettype(order.getPledgeCurrency());
			logExtend.setContent_type(Constants.MONEYLOG_CONTENT_LOAN_THAW);
			logExtend.setCreateTime(new Date());
			moneyLogService.save(logExtend);
			
			// 主动结清
			order.setState(LoanConstants.PLEDGE_ORDER_STATE_SETTLE);
			
		}
		
		Wallet wallet = walletService.saveWalletByPartyId(partyId);
		double amountBefore = wallet.getMoney();
		// 修改主钱包余额
		walletService.update(partyId, Arith.sub(0, repayAmount));
		
		// 保存资金日志
		MoneyLog moneylog = new MoneyLog();
		moneylog.setCategory(Constants.MONEYLOG_CATEGORY_LOAN);
		moneylog.setAmount_before(amountBefore);
		moneylog.setAmount(repayAmount);
		moneylog.setAmount_after(Arith.sub(amountBefore, repayAmount));
		moneylog.setLog("借币还款，订单号[" + order.getOrderNo() + "]，" + "还款：" + repayAmount);
		moneylog.setPartyId(partyId);
		moneylog.setWallettype(Constants.WALLET);
		moneylog.setContent_type(Constants.MONEYLOG_CONTENT_LOAN_REPAY);
		moneylog.setCreateTime(new Date());
		moneyLogService.save(moneylog);
		
		LoanRelationOrder repay = new LoanRelationOrder();
		repay.setId(UUIDGenerator.getUUID());
		repay.setLoanRelationOrderId(id);
		repay.setPartyId(partyId);
		repay.setOrderType(LoanConstants.PLEDGE_ORDER_TYPE_REPAY);
		repay.setLoanAmount(repayAmount);
		repay.setCreateTime(new Date());
		this.insertLoanRelationOrder(repay);
		
		// 修改质押借币单
		order.setDebtAmount(Arith.sub(order.getDebtAmount(), repayAmount));
		// order.setLoanAmount(Arith.sub(order.getLoanAmount(), repayAmount));
		loanOrderService.updateLoanOrder(order);
		
		// 更新缓存
		if (LoanConstants.PLEDGE_ORDER_STATE_SETTLE == order.getState()) {
			// 主动结清
			loanOrderService.removeCacheOrder(order.getId());
		} else {
			loanOrderService.addCacheOrder(order);
		}
	}
	
	/**
	 * 后台质押记录
	 */
	public List<Map<String,Object>> queryLoanRelation(String loanRelationOrderId) {
		List<Map<String,Object>> result = new ArrayList<Map<String,Object>>();
		List<LoanRelationOrder> list = jdbcTemplate.query("SELECT * FROM T_LOAN_RELATION_ORDER WHERE LOAN_RELATION_ORDER_ID=? ORDER BY CREATE_TIME DESC", 
				RecordObjectMapper.newInstance(LoanRelationOrder.class), loanRelationOrderId);
		for (LoanRelationOrder order : list) {
			Map<String,Object> data = new HashMap<String, Object>();
			data.put("orderType", order.getOrderType());
			data.put("loanAmount", order.getLoanAmount());
			data.put("pledgeType", order.getPledgeType());
			data.put("pledgeAmount", order.getPledgeAmount());
			data.put("pledgeCurrency", order.getPledgeCurrency());
			data.put("createTime", DateUtils.format(order.getCreateTime(), DateUtils.DF_yyyyMMddHHmmss));
			result.add(data);
		}
		return result;
	}
	
	/**
	 * 根据订单关联ID获取订单列表
	 */
	public List<LoanRelationOrder> queryOrders(String loanRelationOrderId, int orderType) {
		List<LoanRelationOrder> list = jdbcTemplate.query("SELECT * FROM T_LOAN_RELATION_ORDER WHERE LOAN_RELATION_ORDER_ID=? AND ORDER_TYPE=?", 
				RecordObjectMapper.newInstance(LoanRelationOrder.class), loanRelationOrderId, orderType);
		return list;
	}
	
	public void insertLoanRelationOrder(LoanRelationOrder order) {
		String insertSql = "INSERT INTO T_LOAN_RELATION_ORDER(UUID,LOAN_RELATION_ORDER_ID,PARTY_ID,ORDER_TYPE,LOAN_AMOUNT,LOAN_CURRENCY,"
				+ "PLEDGE_AMOUNT,PLEDGE_CURRENCY,PLEDGE_TYPE,CREATE_TIME) VALUES (?,?,?,?,?,?,?,?,?,?)";
		jdbcTemplate.update(insertSql,order.getId(),order.getLoanRelationOrderId(),order.getPartyId(),order.getOrderType(),
				order.getLoanAmount(),order.getLoanCurrency(),order.getPledgeAmount(),order.getPledgeCurrency(),
				order.getPledgeType(),order.getCreateTime());
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public void setWalletService(WalletService walletService) {
		this.walletService = walletService;
	}

	public void setMoneyLogService(MoneyLogService moneyLogService) {
		this.moneyLogService = moneyLogService;
	}

	public void setDataService(DataService dataService) {
		this.dataService = dataService;
	}

	public void setLoanOrderService(LoanOrderService loanOrderService) {
		this.loanOrderService = loanOrderService;
	}
	
}
