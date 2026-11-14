package project.c2c.internal;

import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import email.EmailSendService;
import kernel.bo.RecordObjectMapper;
import kernel.exception.BusinessException;
import kernel.util.Arith;
import kernel.util.DateUtils;
import kernel.util.StringUtils;
import kernel.util.ThreadUtils;
import kernel.web.ApplicationUtil;
import kernel.web.Page;
import kernel.web.PagedQueryDao;
import project.Constants;
import project.RedisKeys;
import project.c2c.C2cAdvert;
import project.c2c.C2cAdvertLock;
import project.c2c.C2cAdvertService;
import project.c2c.C2cLock;
import project.c2c.C2cOrder;
import project.c2c.C2cOrderService;
import project.c2c.C2cPaymentMethod;
import project.c2c.C2cPaymentMethodService;
import project.c2c.C2cUser;
import project.c2c.C2cUserService;
import project.data.DataService;
import project.log.MoneyLog;
import project.log.MoneyLogService;
import project.party.PartyService;
import project.party.model.Party;
import project.redis.RedisHandler;
import project.syspara.SysparaService;
import project.tip.TipConstants;
import project.tip.TipService;
import project.user.UserDataService;
import project.user.kyc.Kyc;
import project.user.kyc.KycHighLevel;
import project.user.kyc.KycHighLevelService;
import project.user.kyc.KycService;
import project.wallet.Wallet;
import project.wallet.WalletExtend;
import project.wallet.WalletService;
import project.wallet.rate.ExchangeRateService;
import smsbao.SmsSendService;

public class C2cOrderServiceImpl implements C2cOrderService {
	private static final Logger logger=LoggerFactory.getLogger(C2cOrderServiceImpl.class);
	private PagedQueryDao pagedDao;
	private RedisHandler redisHandler;
	private JdbcTemplate jdbcTemplate;
	private C2cAdvertService c2cAdvertService;
	private C2cUserService c2cUserService;
	private DataService dataService;
	private ExchangeRateService exchangeRateService;
	private WalletService walletService;
	private MoneyLogService moneyLogService;
	private C2cPaymentMethodService c2cPaymentMethodService;
	private UserDataService userDataService;
	private TipService tipService;
	private SysparaService sysparaService;
	private KycService kycService;
	private KycHighLevelService kycHighLevelService;
	private PartyService partyService;
	private SmsSendService smsSendService;
	private EmailSendService emailSendService;
	
	public C2cOrder get(String order_no) {
		C2cOrder order = (C2cOrder) this.redisHandler.get(RedisKeys.C2C_ORDER_NO + order_no);
		if (null != order) {
			return order;
		} else {
			List<C2cOrder> list = jdbcTemplate.query("SELECT * FROM T_C2C_ORDER WHERE ORDER_NO=?", RecordObjectMapper.newInstance(C2cOrder.class), order_no);
			if (list.size() > 0) {
				return list.get(0);
			}
			return null;
		}
	}
	
	public List<C2cOrder> getByPayId(String payId) {				
		return jdbcTemplate.query("SELECT * FROM T_C2C_ORDER WHERE PAYMENT_METHOD_ID=? and STATE IN('0','1')", RecordObjectMapper.newInstance(C2cOrder.class), payId);
	}


	public void save(C2cOrder entity) {
		String insertSql = "INSERT INTO T_C2C_ORDER(UUID,PARTY_ID,C2C_USER_ID,C2C_ADVERT_ID,PAYMENT_METHOD_ID,ORDER_TYPE,ORDER_NO,STATE,C2C_USER_TYPE,C2C_USER_CODE,"
				+ "C2C_USER_NICK_NAME,C2C_USER_HEAD_IMG,C2C_USER_PARTY_ID,C2C_USER_PARTY_CODE,C2C_USER_PARTY_NAME,DIRECTION,CURRENCY,SYMBOL,PAY_RATE,SYMBOL_VALUE,COIN_AMOUNT,"
				+ "EXPIRE_TIME,AMOUNT,AMOUNT_USDT,METHOD_TYPE,METHOD_NAME,METHOD_IMG,REAL_NAME,PARAM_NAME1,PARAM_VALUE1,PARAM_NAME2,PARAM_VALUE2,PARAM_NAME3,PARAM_VALUE3,PARAM_NAME4,"
				+ "PARAM_VALUE4,PARAM_NAME5,PARAM_VALUE5,PARAM_NAME6,PARAM_VALUE6,PARAM_NAME7,PARAM_VALUE7,PARAM_NAME8,PARAM_VALUE8,PARAM_NAME9,PARAM_VALUE9,PARAM_NAME10,PARAM_VALUE10,"
				+ "PARAM_NAME11,PARAM_VALUE11,PARAM_NAME12,PARAM_VALUE12,PARAM_NAME13,PARAM_VALUE13,PARAM_NAME14,PARAM_VALUE14,PARAM_NAME15,PARAM_VALUE15,QRCODE,REMARK,CREATE_TIME,HANDLE_TIME,CLOSE_TIME,PAY_TIME,CANCEL_TIME) "
				+ "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		
		jdbcTemplate.update(insertSql,entity.getId(),entity.getPartyId(),entity.getC2cUserId(),entity.getC2cAdvertId(),entity.getPaymentMethodId(),
				entity.getOrderType(),entity.getOrderNo(),entity.getState(),entity.getC2cUserType(),entity.getC2cUserCode(),
				entity.getC2cUserNickName(),entity.getC2cUserHeadImg(),entity.getC2cUserPartyId(),entity.getC2cUserPartyCode(),entity.getC2cUserPartyName(),entity.getDirection(),
				entity.getCurrency(),entity.getSymbol(),entity.getPayRate(),entity.getSymbolValue(),entity.getCoinAmount(),
				entity.getExpireTime(),entity.getAmount(),entity.getAmountUsdt(),entity.getMethodType(),entity.getMethodName(),entity.getMethodImg(),entity.getRealName(),
				entity.getParamName1(),entity.getParamValue1(),entity.getParamName2(),entity.getParamValue2(),entity.getParamName3(),entity.getParamValue3(),
				entity.getParamName4(),entity.getParamValue4(),entity.getParamName5(),entity.getParamValue5(),entity.getParamName6(),entity.getParamValue6(),
				entity.getParamName7(),entity.getParamValue7(),entity.getParamName8(),entity.getParamValue8(),entity.getParamName9(),entity.getParamValue9(),
				entity.getParamName10(),entity.getParamValue10(),entity.getParamName11(),entity.getParamValue11(),entity.getParamName12(),entity.getParamValue12(),
				entity.getParamName13(),entity.getParamValue13(),entity.getParamName14(),entity.getParamValue14(),entity.getParamName15(),entity.getParamValue15(),
				entity.getQrcode(),entity.getRemark(),entity.getCreateTime(),entity.getHandleTime(),entity.getCloseTime(),entity.getPayTime(),entity.getCancelTime());
		
		this.redisHandler.setSync(RedisKeys.C2C_ORDER_NO + entity.getOrderNo(), entity);

		// 订单自动取消时间和确认收款超时时间（0未付款/1已付款）
		Map<String, Long> map = (Map<String, Long>) this.redisHandler.get(RedisKeys.C2C_ORDER_NO_EXPIRE_TIME);
		if (null == map) {
			map = new ConcurrentHashMap<String, Long>();
		}
		map.put(entity.getOrderNo() + "_auto_cancel", entity.getCreateTime().getTime() + entity.getExpireTime() * 60 * 1000);
		map.put(entity.getOrderNo() + "_expire", entity.getCreateTime().getTime() + entity.getExpireTime() * 60 * 1000 * 2);
		this.redisHandler.setSync(RedisKeys.C2C_ORDER_NO_EXPIRE_TIME, map);
		
		// 用户未结束订单数量（0未付款/1已付款/2申诉中/5已超时）
		Map<String, Long> ocMap = (Map<String, Long>) this.redisHandler.get(RedisKeys.C2C_NOFINISH_ORDER_COUNT);
		if (null == ocMap) {
			ocMap = new ConcurrentHashMap<String, Long>();
		}
		Long count = ocMap.get(entity.getPartyId());
		if (null == count) {
			ocMap.put(entity.getPartyId(), 1L);
		} else {
			ocMap.put(entity.getPartyId(), count + 1);
		}
		this.redisHandler.setSync(RedisKeys.C2C_NOFINISH_ORDER_COUNT, ocMap);
	}

	public synchronized void update(C2cOrder entity) {
		if (null == entity) {
			return;
		}
		String updateSql="UPDATE T_C2C_ORDER SET STATE=?,DIRECTION=?,CURRENCY=?,SYMBOL=?,PAY_RATE=?,SYMBOL_VALUE=?,COIN_AMOUNT=?,EXPIRE_TIME=?,"
				+ "AMOUNT=?,AMOUNT_USDT=?,METHOD_TYPE=?,METHOD_NAME=?,METHOD_IMG=?,REAL_NAME=?,PARAM_NAME1=?,PARAM_VALUE1=?,PARAM_NAME2=?,"
				+ "PARAM_VALUE2=?,PARAM_NAME3=?,PARAM_VALUE3=?,PARAM_NAME4=?,PARAM_VALUE4=?,PARAM_NAME5=?,PARAM_VALUE5=?,PARAM_NAME6=?,"
				+ "PARAM_VALUE6=?,PARAM_NAME7=?,PARAM_VALUE7=?,PARAM_NAME8=?,PARAM_VALUE8=?,PARAM_NAME9=?,PARAM_VALUE9=?,PARAM_NAME10=?,"
				+ "PARAM_VALUE10=?,PARAM_NAME11=?,PARAM_VALUE11=?,PARAM_NAME12=?,PARAM_VALUE12=?,PARAM_NAME13=?,PARAM_VALUE13=?,PARAM_NAME14=?,"
				+ "PARAM_VALUE14=?,PARAM_NAME15=?,PARAM_VALUE15=?,QRCODE=?,REMARK=?,HANDLE_TIME=?,CLOSE_TIME=?,PAY_TIME=?,CANCEL_TIME=? WHERE UUID=?";
		jdbcTemplate.update(updateSql,entity.getState(),entity.getDirection(),
				entity.getCurrency(),entity.getSymbol(),entity.getPayRate(),entity.getSymbolValue(),entity.getCoinAmount(),
				entity.getExpireTime(),entity.getAmount(),entity.getAmountUsdt(),entity.getMethodType(),entity.getMethodName(),entity.getMethodImg(),entity.getRealName(),
				entity.getParamName1(),entity.getParamValue1(),entity.getParamName2(),entity.getParamValue2(),entity.getParamName3(),entity.getParamValue3(),
				entity.getParamName4(),entity.getParamValue4(),entity.getParamName5(),entity.getParamValue5(),entity.getParamName6(),entity.getParamValue6(),
				entity.getParamName7(),entity.getParamValue7(),entity.getParamName8(),entity.getParamValue8(),entity.getParamName9(),entity.getParamValue9(),
				entity.getParamName10(),entity.getParamValue10(),entity.getParamName11(),entity.getParamValue11(),entity.getParamName12(),entity.getParamValue12(),
				entity.getParamName13(),entity.getParamValue13(),entity.getParamName14(),entity.getParamValue14(),entity.getParamName15(),entity.getParamValue15(),
				entity.getQrcode(),entity.getRemark(),entity.getHandleTime(),entity.getCloseTime(),entity.getPayTime(),entity.getCancelTime(),entity.getId());

		if (null != entity && Arrays.asList("0", "1").contains(entity.getState())) {
			// 只有未付款和已付款的订单存入redis
			this.redisHandler.setSync(RedisKeys.C2C_ORDER_NO + entity.getOrderNo(), entity);
		} else {
			this.redisHandler.remove(RedisKeys.C2C_ORDER_NO + entity.getOrderNo());
		}
		
		// 订单自动取消时间和确认收款超时时间（0未付款/1已付款）
		Map<String, Long> map = (Map<String, Long>) this.redisHandler.get(RedisKeys.C2C_ORDER_NO_EXPIRE_TIME);
		if (null == map) {
			map = new ConcurrentHashMap<String, Long>();
		}		
		if (!Arrays.asList("0", "1").contains(entity.getState())) {
			map.remove(entity.getOrderNo() + "_auto_cancel");
			map.remove(entity.getOrderNo() + "_expire");
		} else {
			if (Arrays.asList("0").contains(entity.getState())) {
				map.put(entity.getOrderNo() + "_auto_cancel", entity.getCreateTime().getTime() + entity.getExpireTime() * 60 * 1000);
				map.put(entity.getOrderNo() + "_expire", entity.getCreateTime().getTime() + entity.getExpireTime() * 60 * 1000 * 2);
			} else if (Arrays.asList("1").contains(entity.getState())) {
				map.put(entity.getOrderNo() + "_auto_cancel", entity.getCreateTime().getTime() + entity.getExpireTime() * 60 * 1000);
				map.put(entity.getOrderNo() + "_expire", entity.getPayTime().getTime() + entity.getExpireTime() * 60 * 1000);
			}
		}		
		this.redisHandler.setSync(RedisKeys.C2C_ORDER_NO_EXPIRE_TIME, map);
	}
	
	/*
	 * 用户未结束订单数量（0未付款/1已付款/2申诉中/5已超时）减1
	 */
	public void updateNofinishOrderCount(C2cOrder entity) {		
		Map<String, Long> ocMap = (Map<String, Long>) this.redisHandler.get(RedisKeys.C2C_NOFINISH_ORDER_COUNT);
		if (null == ocMap) {
			ocMap = new ConcurrentHashMap<String, Long>();
		}
		Long count = ocMap.get(entity.getPartyId());
		if (null == count) {
			ocMap.put(entity.getPartyId(), 0L);
		} else {
			ocMap.put(entity.getPartyId(), count - 1 <= 0 ? 0 : count - 1);
		}
		this.redisHandler.setSync(RedisKeys.C2C_NOFINISH_ORDER_COUNT, ocMap);
	}
	
	/*
	 * 获取 用户未结束订单数量
	 */
	public Long getNofinishOrderCount(String partyId,String direction) {
		List<C2cOrder> c2cOrderList = jdbcTemplate.query("SELECT * FROM T_C2C_ORDER where STATE = '0' AND PARTY_ID =? AND DIRECTION =?", 
				RecordObjectMapper.newInstance(C2cOrder.class), partyId,direction);
		if(ObjectUtils.isNotEmpty(c2cOrderList)) {
			return (long)c2cOrderList.size();
		}
		return 0l;
	}

	public Page pagedQuery(int pageNo, int pageSize, String direction, String state, String loginPartyId, boolean isC2cUser,String startTime, String endTime) {

		StringBuffer queryString = new StringBuffer();
		
		queryString.append(" SELECT ");
		queryString.append(" ord.UUID id, ord.PARTY_ID party_id, ord.ORDER_NO order_no, ord.STATE state, ord.C2C_USER_NICK_NAME c2c_user_nick_name, ord.C2C_USER_HEAD_IMG c2c_user_head_img, ord.DIRECTION direction, ord.CURRENCY currency, ord.SYMBOL symbol, ord.PAY_RATE pay_rate, ");
		queryString.append(" ord.SYMBOL_VALUE symbol_value, ord.COIN_AMOUNT coin_amount, ord.AMOUNT amount, DATE_FORMAT(ord.CREATE_TIME, '%Y-%m-%d %H:%i:%S') create_time ");

		queryString.append(" FROM T_C2C_ORDER ord ");
		queryString.append(" WHERE 1=1 ");
		if (!StringUtils.isNullOrEmpty(startTime)) {
			queryString.append("AND DATE(CREATE_TIME)>=DATE('"+startTime+" 00:00:00') ");
		}
		
		if (!StringUtils.isNullOrEmpty(endTime)) {
			queryString.append("AND DATE(CREATE_TIME)<=DATE('"+endTime+" 23:59:59') ");
		}
		Map<String, Object> parameters = new HashMap<String, Object>();
		
		if (StringUtils.isNotEmpty(direction)) {
			queryString.append(" AND ord.DIRECTION = :direction ");
			parameters.put("direction", direction);
		}
		
		if (StringUtils.isNotEmpty(state)) {
			queryString.append(" AND ord.STATE = :state ");
			parameters.put("state", Integer.valueOf(state).intValue());
		}

		if (isC2cUser) {
			if (StringUtils.isNotEmpty(loginPartyId)) {
				queryString.append(" AND ord.C2C_USER_PARTY_ID = :loginPartyId ");
				parameters.put("loginPartyId", loginPartyId);
			}
		} else {
			if (StringUtils.isNotEmpty(loginPartyId)) {
				queryString.append(" AND ord.PARTY_ID = :loginPartyId ");
				parameters.put("loginPartyId", loginPartyId);
			}
		}		

		queryString.append(" order by ord.CREATE_TIME desc ");
		
		Page page = this.pagedDao.pagedQuerySQL(pageNo, pageSize, queryString.toString(), parameters);
		
		// 金额默认保留2位
		DecimalFormat df = new DecimalFormat("#.##");		
		// 币种默认保留8位
		DecimalFormat dfCoin = new DecimalFormat("#.########");
		
		for (Map<String, Object> data : (List<Map<String, Object>>) page.getElements()) {
			data.put("symbol_value", df.format(data.get("symbol_value")));
			data.put("coin_amount", dfCoin.format(data.get("coin_amount")));
			data.put("symbol", data.get("symbol").toString().toUpperCase());
		}
		
		return page;
	}

	public List<C2cOrder> findByPartyIdAndToday(String partyId, String direction, String state) {
		if (StringUtils.isEmptyString(state)) {
			List<C2cOrder> list = jdbcTemplate.query("SELECT * FROM T_C2C_ORDER WHERE PARTY_ID = ? AND DIRECTION = ? and DateDiff(CREATE_TIME, NOW()) = 0 ",
					RecordObjectMapper.newInstance(C2cOrder.class), partyId, direction);
			return list;
		}
		List<C2cOrder> list = jdbcTemplate.query("SELECT * FROM T_C2C_ORDER WHERE PARTY_ID = ? AND DIRECTION = ? and DateDiff(CREATE_TIME, NOW()) = 0 AND STATE = ?",
				RecordObjectMapper.newInstance(C2cOrder.class), partyId, direction, state);
		return list;
	}
	
	public List<C2cOrder> findNoEndingSellOrders(String loginPartyId) {
		List<C2cOrder> list = jdbcTemplate.query("SELECT * FROM T_C2C_ORDER WHERE STATE IN ('0','1','2','5') AND DIRECTION = 'sell' AND PARTY_ID = ?",
				RecordObjectMapper.newInstance(C2cOrder.class), loginPartyId);
		return list;
	}
	
	/*
	 * 查询未完结订单数量，根据广告ID
	 */
	public Long findNoEndingOrdersCountByAdvertId(String c2cAdvertId) {
		List find = jdbcTemplate.queryForList(" SELECT COUNT(*) FROM T_C2C_ORDER WHERE C2C_ADVERT_ID = ? AND STATE IN ('0','1','2','5') ", String.class, c2cAdvertId);
		return CollectionUtils.isEmpty(find) ? 0L : (null == find.get(0) ? 0L : Long.valueOf(find.get(0).toString()));
	}
	
	public void saveOpen(C2cOrder c2cOrder,String remark) {
		logger.error("saveOpen start:"+remark);
		C2cAdvert c2cAdvert = this.c2cAdvertService.findById(c2cOrder.getC2cAdvertId());
		if (null == c2cAdvert) {
			throw new BusinessException("广告不存在");
		}
		
		if (1 != c2cAdvert.getOnSale()) {
			throw new BusinessException("广告已下架");
		}
		
		Party party = this.partyService.cachePartyBy(c2cOrder.getPartyId(), false);
		if (null == party) {
			throw new BusinessException("用户信息不存在");
		}
		
		C2cUser c2cUser = this.c2cUserService.get(c2cAdvert.getC2cUserId());
		if (null == c2cUser) {
			throw new BusinessException("承兑商不存在");
		}
		
		Party c2cParty = this.partyService.cachePartyBy(c2cUser.getC2cUserPartyId(), false);
		if (null == c2cParty) {
			throw new BusinessException("承兑商的用户信息不存在");				
		}
		
		C2cPaymentMethod method = this.c2cPaymentMethodService.get(c2cOrder.getPaymentMethodId());
		if (null == method) {
			throw new BusinessException("支付方式不存在");
		}
		
		if (C2cAdvert.DIRECTION_SELL.equals(c2cAdvert.getDirection())) {
			
			if (!party.getWithdraw_authority()) {
				throw new BusinessException( "无权限");
			}
			
			this.checkSellAuth(c2cOrder.getPartyId());
			this.checkSellNum(c2cOrder.getPartyId());
			
			if (!method.getPartyId().equals(c2cOrder.getPartyId())) {
				throw new BusinessException("支付方式不匹配该用户");
			}
		} else {
			
			if (!method.getPartyId().equals(c2cUser.getC2cUserPartyId())) {
				throw new BusinessException("支付方式不匹配该承兑商");
			}
		}

		c2cOrder.setC2cUserType(c2cUser.getC2cUserType());
		c2cOrder.setC2cUserCode(c2cUser.getC2cUserCode());
		c2cOrder.setC2cUserNickName(c2cUser.getNickName());
		c2cOrder.setC2cUserHeadImg(c2cUser.getHeadImg());
		c2cOrder.setC2cUserPartyId(c2cUser.getC2cUserPartyId());
		c2cOrder.setC2cUserPartyCode(c2cParty.getUsercode());
		c2cOrder.setC2cUserPartyName(c2cParty.getUsername());
		c2cOrder.setDirection(c2cAdvert.getDirection());
		c2cOrder.setCurrency(c2cAdvert.getCurrency());
		c2cOrder.setSymbol(c2cAdvert.getSymbol());
		c2cOrder.setPayRate(c2cAdvert.getPayRate());
		c2cOrder.setSymbolValue(c2cAdvert.getSymbolValue());
		c2cOrder.setExpireTime(c2cAdvert.getExpireTime());
		c2cOrder.setMethodType(method.getMethodType());
		c2cOrder.setMethodName(method.getMethodName());
		c2cOrder.setMethodImg(method.getMethodImg());
		c2cOrder.setRealName(method.getRealName());
		c2cOrder.setParamName1(method.getParamName1());
		c2cOrder.setParamValue1(method.getParamValue1());
		c2cOrder.setParamName2(method.getParamName2());
		c2cOrder.setParamValue2(method.getParamValue2());
		c2cOrder.setParamName3(method.getParamName3());
		c2cOrder.setParamValue3(method.getParamValue3());
		c2cOrder.setParamName4(method.getParamName4());
		c2cOrder.setParamValue4(method.getParamValue4());
		c2cOrder.setParamName5(method.getParamName5());
		c2cOrder.setParamValue5(method.getParamValue5());
		c2cOrder.setParamName6(method.getParamName6());
		c2cOrder.setParamValue6(method.getParamValue6());
		c2cOrder.setParamName7(method.getParamName7());
		c2cOrder.setParamValue7(method.getParamValue7());
		c2cOrder.setParamName8(method.getParamName8());
		c2cOrder.setParamValue8(method.getParamValue8());
		c2cOrder.setParamName9(method.getParamName9());
		c2cOrder.setParamValue9(method.getParamValue9());
		c2cOrder.setParamName10(method.getParamName10());
		c2cOrder.setParamValue10(method.getParamValue10());
		c2cOrder.setParamName11(method.getParamName11());
		c2cOrder.setParamValue11(method.getParamValue11());
		c2cOrder.setParamName12(method.getParamName12());
		c2cOrder.setParamValue12(method.getParamValue12());
		c2cOrder.setParamName13(method.getParamName13());
		c2cOrder.setParamValue13(method.getParamValue13());
		c2cOrder.setParamName14(method.getParamName14());
		c2cOrder.setParamValue14(method.getParamValue14());
		c2cOrder.setParamName15(method.getParamName15());
		c2cOrder.setParamValue15(method.getParamValue15());
		c2cOrder.setQrcode(method.getQrcode());
		c2cOrder.setCreateTime(new Date());
		c2cOrder.setHandleTime(null);
		c2cOrder.setCloseTime(DateUtils.addMinute(c2cOrder.getCreateTime(), c2cOrder.getExpireTime()));
		c2cOrder.setPayTime(null);
		c2cOrder.setCancelTime(null);
		
		// 币种单价
		double symbolValue = c2cAdvert.getSymbolValue();
		if (C2cAdvert.DIRECTION_BUY.equals(c2cAdvert.getDirection())) {
			// 买币
			this.saveBuy(c2cOrder, c2cAdvert, symbolValue,remark);
		} else if (C2cAdvert.DIRECTION_SELL.equals(c2cAdvert.getDirection())) {
			// 卖币
			this.saveSell(c2cOrder, c2cAdvert, symbolValue,remark);
		} else {
			throw new BusinessException("买卖方式不正确");
		}
		
		this.tipService.saveNewTip(c2cOrder.getId().toString(), TipConstants.C2C_ORDER,remark);
		this.c2cSendMessageByState(c2cOrder, "0");
		logger.error("saveOpen end:"+remark);
	}
	
	private void saveBuy(C2cOrder c2cOrder, C2cAdvert c2cAdvert, double symbol_value,String remark) {
		logger.error("saveBuy start:"+remark);
		DecimalFormat df = new DecimalFormat("#.########");
		
		if (C2cOrder.ORDER_TYPE_BY_AMOUNT.equals(c2cOrder.getOrderType())) {
			// 按支付金额支付
			
			// 币种数量 = 支付金额/币种单价
			double coin_amount = Double.valueOf(df.format(Arith.div(c2cOrder.getAmount(), symbol_value))).doubleValue();

			if (coin_amount > c2cAdvert.getCoinAmount()) {
				throw new BusinessException("该广告剩余数量不足");
			}
			
			c2cOrder.setCoinAmount(coin_amount);
			c2cOrder.setAmountUsdt(Double.valueOf(df.format(Arith.mul(coin_amount, c2cAdvert.getSymbolClose()))).doubleValue());
			c2cAdvert.setCoinAmount(Double.valueOf(df.format(Arith.sub(c2cAdvert.getCoinAmount(), coin_amount))).doubleValue());
			c2cAdvert.setDeposit(Double.valueOf(df.format(Arith.sub(c2cAdvert.getDeposit(), c2cOrder.getAmountUsdt()))).doubleValue());
		} else {
			// 按币种数量支付
			
			if (c2cOrder.getCoinAmount() > c2cAdvert.getCoinAmount()) {
				throw new BusinessException("该广告剩余数量不足");
			}
			
			c2cOrder.setAmount(Double.valueOf(df.format(Arith.mul(c2cOrder.getCoinAmount(), symbol_value))).doubleValue());
			c2cOrder.setAmountUsdt(Double.valueOf(df.format(Arith.mul(c2cOrder.getCoinAmount(), c2cAdvert.getSymbolClose()))).doubleValue());
			c2cAdvert.setCoinAmount(Double.valueOf(df.format(Arith.sub(c2cAdvert.getCoinAmount(), c2cOrder.getCoinAmount()))).doubleValue());
			c2cAdvert.setDeposit(Double.valueOf(df.format(Arith.sub(c2cAdvert.getDeposit(), c2cOrder.getAmountUsdt()))).doubleValue());
		}
		logger.error("buy save start:"+remark);
		this.save(c2cOrder);
		logger.error("buy save end:"+remark);
		// 买入金额需要在区间内
		if (c2cOrder.getAmount() > c2cAdvert.getInvestmentMax() || c2cOrder.getAmount() < c2cAdvert.getInvestmentMin()) {
			throw new BusinessException("金额不在购买区间");
		}
		
		c2cAdvert.setSortIndex(0);
		this.c2cAdvertService.update(c2cAdvert);
		logger.error("saveBuy end:"+remark);
	}

	private void saveSell(C2cOrder c2cOrder, C2cAdvert c2cAdvert, double symbol_value,String remark) {
		logger.error("saveSell start::"+remark);
		DecimalFormat df = new DecimalFormat("#.########");
		
		if (C2cOrder.ORDER_TYPE_BY_AMOUNT.equals(c2cOrder.getOrderType())) {
			// 按支付金额支付
			
			// 币种数量 = 支付金额/币种单价
			double coin_amount = Double.valueOf(df.format(Arith.div(c2cOrder.getAmount(), symbol_value)));
			c2cOrder.setCoinAmount(coin_amount);
			c2cOrder.setAmountUsdt(Double.valueOf(df.format(Arith.mul(coin_amount, c2cAdvert.getSymbolClose()))).doubleValue());
		} else {
			// 按币种数量支付			
			c2cOrder.setAmount(Double.valueOf(df.format(Arith.mul(c2cOrder.getCoinAmount(), symbol_value))));
			c2cOrder.setAmountUsdt(Double.valueOf(df.format(Arith.mul(c2cOrder.getCoinAmount(), c2cAdvert.getSymbolClose()))).doubleValue());
		}
		this.save(c2cOrder);
		// 买入金额需要在区间内
		if (c2cOrder.getAmount() > c2cAdvert.getInvestmentMax() || c2cOrder.getAmount() < c2cAdvert.getInvestmentMin()) {
			throw new BusinessException("金额不在购买区间");
		}
		
		double amountBefore = 0d;
		double amountAfter = 0d;
		
		if ("usdt".equalsIgnoreCase(c2cAdvert.getSymbol())) {
			
			Wallet wallet = this.walletService.saveWalletByPartyId(c2cOrder.getPartyId());
			if (c2cOrder.getCoinAmount() > wallet.getMoney()) {
				throw new BusinessException("用户剩余数量不足");
			}
			
			amountBefore = wallet.getMoney();
			amountAfter = Double.valueOf(df.format(Arith.sub(wallet.getMoney(), c2cOrder.getCoinAmount()))).doubleValue();
			this.walletService.update(c2cOrder.getPartyId(), Arith.sub(0, c2cOrder.getCoinAmount()));
		} else {
			
			WalletExtend walletExtend = this.walletService.saveExtendByPara(c2cOrder.getPartyId(), c2cOrder.getSymbol());
			
			if (c2cOrder.getCoinAmount() > walletExtend.getAmount()) {
				throw new BusinessException("用户剩余数量不足");
			}
			
			amountBefore = walletExtend.getAmount();
			amountAfter = Double.valueOf(df.format(Arith.sub(walletExtend.getAmount(), c2cOrder.getCoinAmount()))).doubleValue();
			this.walletService.updateExtend(c2cOrder.getPartyId(), c2cOrder.getSymbol(), Arith.sub(0, c2cOrder.getCoinAmount()));
		}

		// 保存资金日志
		MoneyLog moneylog = new MoneyLog();
		moneylog.setCategory(Constants.MONEYLOG_CATEGORY_C2C);
		moneylog.setAmount_before(amountBefore);
		moneylog.setAmount(Arith.sub(0, c2cOrder.getCoinAmount()));
		moneylog.setAmount_after(amountAfter);
		moneylog.setLog("c2c卖币，币种[" + c2cOrder.getSymbol() + "]，订单号[" + c2cOrder.getOrderNo() + "]");
		moneylog.setPartyId(c2cOrder.getPartyId());
		moneylog.setWallettype(c2cOrder.getSymbol());
		moneylog.setContent_type(Constants.MONEYLOG_CONTENT_C2C_SELL);
		moneyLogService.save(moneylog);

		c2cAdvert.setSortIndex(0);
		this.c2cAdvertService.update(c2cAdvert);
	}

	/**
	 * 检测用户可卖币次数
	 */
	public void checkSellNum(String partyId) {
		// 当日提现次数是否超过
		Object obj = this.sysparaService.find("c2c_sell_limit_num");
		if (null != obj) {
			double c2c_sell_limit_num = Double.valueOf(this.sysparaService.find("c2c_sell_limit_num").getValue());
			List<C2cOrder> c2cOrders = this.findByPartyIdAndToday(partyId, C2cAdvert.DIRECTION_SELL, null);

			if (c2c_sell_limit_num > 0 && c2cOrders != null) {
				if (c2cOrders.size() >= c2c_sell_limit_num) {
					throw new BusinessException(1, "当日可提现次数不足");
				}
			}
		}
	}
	
	/**
	 * 检测用户可卖币权限
	 */
	public void checkSellAuth(String partyId) {
		Kyc party_kyc = this.kycService.get(partyId);
		Object objKyc = this.sysparaService.find("c2c_sell_by_kyc");
		if (null != objKyc) {
			if (!(party_kyc.getStatus() == 2) && "true".equals(this.sysparaService.find("c2c_sell_by_kyc").getValue())) {
				throw new BusinessException(401, "无权限");
			}
		}
		KycHighLevel party_kycHighLevel = this.kycHighLevelService.get(partyId);
		Object objKycHigh = this.sysparaService.find("c2c_sell_by_high_kyc");
		if (null != objKycHigh) {
			if (!(party_kycHighLevel.getStatus() == 2) && "true".equals(this.sysparaService.find("c2c_sell_by_high_kyc").getValue())) {
				throw new BusinessException(1, "请先通过高级认证");
			}
		}
	}
	
//	public void saveOpenQuick(C2cOrder c2cOrder) {
//				
//		C2cPaymentMethod method = this.c2cPaymentMethodService.get(c2cOrder.getPaymentMethodId());
//		if (null == method) {
//			throw new BusinessException("支付方式不存在");
//		}
//		
//		Page page = null;		
//		if (C2cOrder.ORDER_TYPE_BY_AMOUNT.equals(c2cOrder.getOrderType())) {
//			// 按支付金额支付
//			page = this.c2cAdvertService.pagedQueryQuick(1, 1, c2cOrder.getCurrency(), c2cOrder.getSymbol(), c2cOrder.getDirection(), c2cOrder.getAmount(), -1, method.getMethodConfigId());
//		} else {
//			// 按币种数量支付
//			page = this.c2cAdvertService.pagedQueryQuick(1, 1, c2cOrder.getCurrency(), c2cOrder.getSymbol(), c2cOrder.getDirection(), -1, c2cOrder.getCoinAmount(), method.getMethodConfigId());
//		}
//		
//		if (CollectionUtils.isNotEmpty(page.getElements())) {
//			
//			Map<String, Object> advertMap = (Map<String, Object>) page.getElements().get(0);
//			
//			String c2cAdvertId = advertMap.get("id").toString();
//			c2cOrder.setC2cAdvertId(c2cAdvertId);
//			String c2cUserId = advertMap.get("c2c_user_id").toString();
//			c2cOrder.setC2cUserId(c2cUserId);
//			
//			if (C2cAdvert.DIRECTION_BUY.equals(advertMap.get("direction").toString())) {
//				// 买币 订单存承兑商支付方式
//			} else if (C2cAdvert.DIRECTION_SELL.equals(advertMap.get("direction").toString())) {
//				// 卖币 订单存用户支付方式
//				
//				C2cUser c2cUser = this.c2cUserService.get(c2cUserId);
//				if (null == c2cUser) {
//					throw new BusinessException("承兑商不存在");
//				}
//				
//				Map<String, C2cPaymentMethod> methodMap = this.c2cPaymentMethodService.getByPartyId(c2cUser.getC2cUserPartyId());
//				if (null == methodMap) {
//					throw new BusinessException("未找到符合条件的承兑商广告");
//				}
//				
//				boolean found = false;
//				for (String id : methodMap.keySet()) {
//					C2cPaymentMethod cpm = methodMap.get(id);
//					if (null != cpm && cpm.getMethodConfigId().equals(method.getMethodConfigId())) {
//						c2cOrder.setPaymentMethodId(cpm.getId().toString());
//						found = true;
//						break;
//					}
//				}
//				if (!found) {
//					throw new BusinessException("未找到符合条件的承兑商广告");
//				}
//				
//			} else {
//				throw new BusinessException("买卖方式不正确");
//			}
//			
//			boolean lock = false;
//			
//			try {
//				
//				int i = 0;
//				while (true) {
//					
//					if (C2cAdvertLock.add(c2cAdvertId)) {
//						lock = true;
//						this.saveOpen(c2cOrder);
//						// 处理完退出
//						break;
//					}
//					i++;
//					if (i == 100) {
//						// 尝试100次没获取到，则先退出
//						throw new BusinessException(1, "请稍后再试");
//					}
//					
//				}
//				
//			} catch (BusinessException e) {
//				throw e;
//			} catch (Throwable t) {
//				throw t;
//			} finally {
//				if (lock) {
//					ThreadUtils.sleep(200);
//					// 事务提交
//					C2cAdvertLock.remove(c2cAdvertId);
//				}
//			}
//
//		} else {
//			throw new BusinessException("未找到符合条件的承兑商广告");
//		}
//	}
	
	public void saveOpenQuickApply(C2cOrder c2cOrder) {
		
		C2cPaymentMethod method = this.c2cPaymentMethodService.get(c2cOrder.getPaymentMethodId());
		if (null == method) {
			throw new BusinessException("支付方式不存在");
		}
		
		C2cAdvert c2cAdvert = this.c2cAdvertService.get(c2cOrder.getC2cAdvertId());
		if (null == c2cAdvert) {
			throw new BusinessException("广告不存在");
		}
		
		c2cOrder.setC2cUserId(c2cAdvert.getC2cUserId());
		
		if (C2cAdvert.DIRECTION_BUY.equals(c2cAdvert.getDirection())) {
			// 买币 订单存承兑商支付方式
			
			C2cUser c2cUser = this.c2cUserService.get(c2cAdvert.getC2cUserId());
			if (null == c2cUser) {
				throw new BusinessException("承兑商不存在");
			}
			
			Map<String, C2cPaymentMethod> methodMap = this.c2cPaymentMethodService.getByPartyId(c2cUser.getC2cUserPartyId());
			if (null == methodMap) {
				throw new BusinessException("承兑商支付方式不存在");
			}
			
			boolean found = false;
			for (String id : methodMap.keySet()) {
				C2cPaymentMethod cpm = methodMap.get(id);
				if (null != cpm && cpm.getMethodConfigId().equals(method.getMethodConfigId())) {
					c2cOrder.setPaymentMethodId(cpm.getId().toString());
					found = true;
					break;
				}
			}
			if (!found) {
				throw new BusinessException("承兑商支付方式不存在");
			}
			
		} else if (C2cAdvert.DIRECTION_SELL.equals(c2cAdvert.getDirection())) {
			// 卖币 订单存用户支付方式			
		} else {
			throw new BusinessException("买卖方式不正确");
		}
		
		boolean lock = false;
		String partyId = c2cOrder.getPartyId();
		try {
			
			if (!C2cLock.add(partyId)) {
				throw new BusinessException("Please try again later");
			}
			lock = true;
				
			this.saveOpen(c2cOrder,"");
			
		} catch (BusinessException e) {
			throw e;
		} catch (Throwable t) {
			throw t;
		} finally {
			if (lock) {
				C2cLock.remove(partyId);
			}
		}
	}

	/**
	 * 订单取消 
	 * @param role: user/用户；c2c_user/承兑商；manager/承兑商管理员、root和admin；timer/订单自动取消定时器；
	 */
	public synchronized void saveOrderCancel(C2cOrder c2cOrder, String role) {
		
		DecimalFormat df = new DecimalFormat("#.########");
		
		if ("4".equals(c2cOrder.getState())) {
			throw new BusinessException("该订单已取消");
		}

		if ("manager".equals(role)) {
			if (!("0".equals(c2cOrder.getState()) || "1".equals(c2cOrder.getState()) || "2".equals(c2cOrder.getState()) || "5".equals(c2cOrder.getState()))) {
				throw new BusinessException("当前订单不为待付款、已付款、申诉中或已超时状态，无法取消");
			}
		} else {
			if (!"0".equals(c2cOrder.getState())) {
				throw new BusinessException("当前订单不为待付款状态，无法取消");
			}
		}
		
		if (C2cOrder.DIRECTION_BUY.equals(c2cOrder.getDirection())) {
			// 买币
			
			boolean lock = false;
			
			try {
				
				if (C2cAdvertLock.add(c2cOrder.getC2cAdvertId())) {						
					lock = true;
					C2cAdvert c2cAdvert = this.c2cAdvertService.findById(c2cOrder.getC2cAdvertId());
					c2cAdvert.setCoinAmount(Double.valueOf(df.format(Arith.add(c2cAdvert.getCoinAmount(), c2cOrder.getCoinAmount()))).doubleValue());
					c2cAdvert.setDeposit(Double.valueOf(df.format(Arith.add(c2cAdvert.getDeposit(), c2cOrder.getAmountUsdt()))).doubleValue());
					this.c2cAdvertService.update(c2cAdvert);
				}
				
			} finally {
				if (lock) {
					ThreadUtils.sleep(100);
					// 事务提交
					C2cAdvertLock.remove(c2cOrder.getC2cAdvertId());
				}
			}

		} else if (C2cOrder.DIRECTION_SELL.equals(c2cOrder.getDirection())) {
			// 卖币
			
			// 用户钱包退还
			double amountBefore = 0d;
			double amountAfter = 0d;
			
			if ("usdt".equalsIgnoreCase(c2cOrder.getSymbol())) {
				Wallet wallet = this.walletService.saveWalletByPartyId(c2cOrder.getPartyId());
				amountBefore = wallet.getMoney();
				amountAfter = Double.valueOf(df.format(Arith.add(wallet.getMoney(), c2cOrder.getCoinAmount()))).doubleValue();
				this.walletService.update(c2cOrder.getPartyId().toString(), c2cOrder.getCoinAmount());
			} else {
				WalletExtend walletExtend = this.walletService.saveExtendByPara(c2cOrder.getPartyId(), c2cOrder.getSymbol());
				amountBefore = walletExtend.getAmount();
				amountAfter = walletExtend.getAmount();
				this.walletService.updateExtend(c2cOrder.getPartyId().toString(), c2cOrder.getSymbol(), c2cOrder.getCoinAmount());
			}

			// 保存资金日志
			MoneyLog moneyLog = new MoneyLog();
			moneyLog.setCategory(Constants.MONEYLOG_CATEGORY_C2C);
			moneyLog.setAmount_before(amountBefore);
			moneyLog.setAmount(c2cOrder.getCoinAmount());
			moneyLog.setAmount_after(amountAfter);
			moneyLog.setLog("c2c订单取消，描述[" + c2cOrder.getRemark() + "]");
			moneyLog.setPartyId(c2cOrder.getPartyId());
			moneyLog.setWallettype(c2cOrder.getSymbol());
			moneyLog.setContent_type(Constants.MONEYLOG_CONTENT_C2C_CANCEL);

			this.moneyLogService.save(moneyLog);
		}
		
		c2cOrder.setState("4");
		c2cOrder.setCancelTime(new Date());
		this.update(c2cOrder);
		this.updateNofinishOrderCount(c2cOrder);
		
		this.tipService.deleteTip(c2cOrder.getId().toString());
		
		this.c2cSendMessageByState(c2cOrder, "4");
		
		// 取消次数加1
		String partyIdCancel = "";
		if ("user".equals(role)) {
			partyIdCancel = c2cOrder.getPartyId();
		} else if ("c2c_user".equals(role)) {
			partyIdCancel = c2cOrder.getC2cUserPartyId();
		}
		if (!"".equals(partyIdCancel)) {
			Map<String, Integer> map = (Map<String, Integer>) this.redisHandler.get(RedisKeys.C2C_ORDER_CANCEL_DAY_TIMES);
			if (null == map) {
				map = new ConcurrentHashMap<String, Integer>();
			}
			if (null == map.get(partyIdCancel)) {
				map.put(partyIdCancel, 1);
			} else {
				map.put(partyIdCancel, map.get(partyIdCancel) + 1);
			}
			this.redisHandler.setSync(RedisKeys.C2C_ORDER_CANCEL_DAY_TIMES, map);
		}
	}

	/**
	 * 订单放行
	 */
	public void saveOrderPass(C2cOrder c2cOrder) {
		
		DecimalFormat df = new DecimalFormat("#.########");
				
		C2cAdvert c2cAdvert = this.c2cAdvertService.get(c2cOrder.getC2cAdvertId());
		if (null == c2cAdvert) {
			throw new BusinessException("广告不存在");
		}

//		double symbolClose = symbolClose(c2cOrder.getSymbol());
		double symbolClose = c2cAdvert.getSymbolClose();
		
		if (C2cOrder.DIRECTION_BUY.equals(c2cOrder.getDirection())) {
			// 买币
			
			// 给用户账户添加相应的币种数量
			double amountBefore = 0d;
			double amountAfter = 0d;
			
			if ("usdt".equalsIgnoreCase(c2cOrder.getSymbol())) {
				Wallet wallet = this.walletService.saveWalletByPartyId(c2cOrder.getPartyId());
				amountBefore = wallet.getMoney();
				amountAfter = Double.valueOf(df.format(Arith.add(wallet.getMoney(), c2cOrder.getCoinAmount()))).doubleValue();
				this.walletService.update(c2cOrder.getPartyId().toString(), c2cOrder.getCoinAmount());
			} else {
				WalletExtend walletExtend = this.walletService.saveExtendByPara(c2cOrder.getPartyId(), c2cOrder.getSymbol());
				amountBefore = walletExtend.getAmount();
				amountAfter = Double.valueOf(df.format(Arith.add(walletExtend.getAmount(), c2cOrder.getCoinAmount()))).doubleValue();
				this.walletService.updateExtend(c2cOrder.getPartyId().toString(), c2cOrder.getSymbol(), c2cOrder.getCoinAmount());
			}

			// 保存资金日志
			MoneyLog moneyLog = new MoneyLog();
			moneyLog.setCategory(Constants.MONEYLOG_CATEGORY_C2C);
			moneyLog.setAmount_before(amountBefore);
			moneyLog.setAmount(c2cOrder.getCoinAmount());
			moneyLog.setAmount_after(amountAfter);
			moneyLog.setLog("c2c订单购买放行，订单号[" + c2cOrder.getOrderNo() + "]");
			moneyLog.setPartyId(c2cOrder.getPartyId());
			moneyLog.setWallettype(c2cOrder.getSymbol());
			moneyLog.setContent_type(Constants.MONEYLOG_CONTENT_OTC_BUY);

			moneyLogService.save(moneyLog);

			// usdt价值=币种数量*行情价
			if ("usdt".equalsIgnoreCase(c2cOrder.getSymbol())) {
				this.userDataService.saveRechargeHandle(c2cOrder.getPartyId(), c2cOrder.getCoinAmount(), c2cOrder.getSymbol());
			} else {
				this.userDataService.saveRechargeHandle(c2cOrder.getPartyId(), c2cOrder.getCoinAmount(), c2cOrder.getSymbol());
			}
			
		} else if (C2cOrder.DIRECTION_SELL.equals(c2cOrder.getDirection())) {
			// 卖币
				
			if (C2cAdvertLock.add(c2cOrder.getC2cAdvertId())) {
				c2cAdvert.setCoinAmount(Double.valueOf(df.format(Arith.add(c2cAdvert.getCoinAmount(), c2cOrder.getCoinAmount()))).doubleValue());
				c2cAdvert.setDeposit(Double.valueOf(df.format(Arith.add(c2cAdvert.getDeposit(), c2cOrder.getAmountUsdt()))).doubleValue());
				this.c2cAdvertService.update(c2cAdvert);
			}

			// usdt价值=币种数量*行情价
			if ("usdt".equalsIgnoreCase(c2cOrder.getSymbol())) {
				this.userDataService.saveWithdrawHandle(c2cOrder.getPartyId(), c2cOrder.getCoinAmount(), 0d, "usdt");
			} else {
				this.userDataService.saveWithdrawHandle(c2cOrder.getPartyId(), Double.valueOf(df.format(Arith.mul(c2cOrder.getCoinAmount(), symbolClose))).doubleValue(), 0d, c2cOrder.getSymbol());
			}
		}
		
		// 订单完成
		c2cOrder.setState("3");
		c2cOrder.setHandleTime(new Date());
		this.update(c2cOrder);
		this.updateNofinishOrderCount(c2cOrder);
		
		C2cUser c2cUser = this.c2cUserService.get(c2cOrder.getC2cUserId());
		if (null == c2cUser) {
			throw new BusinessException("承兑商不存在");
		}
		if (C2cOrder.DIRECTION_BUY.equals(c2cOrder.getDirection())) {
			// 买币
			c2cUser.setBuyAmount(c2cUser.getBuyAmount() + c2cOrder.getAmountUsdt());
			c2cUser.setBuySuccessOrders(c2cUser.getBuySuccessOrders() + 1);
		} else if (C2cOrder.DIRECTION_SELL.equals(c2cOrder.getDirection())) {
			// 卖币
			c2cUser.setSellAmount(c2cUser.getSellAmount() + c2cOrder.getAmountUsdt());
			c2cUser.setSellSuccessOrders(c2cUser.getSellSuccessOrders() + 1);
		}
		c2cUser.setTotalAmount(c2cUser.getTotalAmount() + c2cOrder.getAmountUsdt());
		c2cUser.setTotalSuccessOrders(c2cUser.getTotalSuccessOrders() + 1);

		if (C2cOrder.DIRECTION_SELL.equals(c2cOrder.getDirection())) {
			// 卖币			
			// 用户出售承兑商付款 相当于给承兑商账户充值了保证金
			c2cUser.setDepositOpen(Double.valueOf(df.format(Arith.add(c2cUser.getDepositOpen(), c2cOrder.getAmountUsdt()))).doubleValue());
		}
		
		this.c2cUserService.update(c2cUser);
		
		this.tipService.deleteTip(c2cOrder.getId().toString());
		
		this.c2cSendMessageByState(c2cOrder, "3");
	}

//	private double symbolClose(String symbol) {
//		double close = 0d;
//		if (symbol.equals("usdt")) {
//			close = 1;
//		} else {
//			List<Realtime> list = this.dataService.realtime(symbol);
//			if (list.size() == 0) {
//				return 0;
//			}
//			Realtime realtime = list.get(0);
//			close = realtime.getClose();
//		}
//		return close;
//	}

	/*
	 * C2C发送短信和邮件通知
	 */
	public void c2cSendMessageByState(C2cOrder order, String state) {
		return;
		
//		Party party = this.partyService.cachePartyBy(order.getPartyId(), false);
//		if (null == party) {
//			throw new BusinessException("用户不存在");
//		}
//
//		Party c2cParty = this.partyService.cachePartyBy(order.getC2cUserPartyId(), false);
//		if (null == c2cParty) {
//			throw new BusinessException("承兑商不存在");
//		}
//		
//		C2cUser c2cUser = this.c2cUserService.getByPartyId(order.getC2cUserPartyId());
//		if (null == c2cUser) {
//			throw new BusinessException("承兑商不存在");				
//		}
//		
//		// C2C短信和邮件通知内容
//		String c2c_send_msg_content = this.sysparaService.find("c2c_send_msg_content").getValue();
//		if (StringUtils.isNullOrEmpty(c2c_send_msg_content)) {
//			log.error("System parameter c2c_send_msg_content have not configured.");
//			return;
//		}
//
//		// C2C短信和邮件通知内容: 
//		// BUY_CREAT_ORDER##您在交易所的订单{0}已提交,请向承兑商付款{1}{2},这是一条自动消息请不要回复
//		// &&SELL_CREAT_ORDER##您在交易所的有新的订单{0},请向用户付款{1}{2},这是一条自动消息请不要回复
//		// &&PAY_FINISH##您在交易所的订单{0}已成功付款{1}{2},请确定收款放币,这是一条自动消息请不要回复
//		// &&APPEAL##您在交易所的订单{0}已提交申诉,请耐心等待处理,这是一条自动消息请不要回复
//		// &&SUCCEED##您在交易所的订单{0}已放币{1}{2},请确认查收,这是一条自动消息请不要回复
//		// &&CANCELLED##您在交易所的订单{0}已取消,这是一条自动消息请不要回复
//		// &&EXPIRED##您在交易所的订单{0}已超时,这是一条自动消息请不要回复
//
//		Map<String, String> msgMap = new HashMap<String, String>();
//		String[] msgArray = c2c_send_msg_content.split("&&");
//		for (int i = 0; i < msgArray.length; i++) {
//			String[] msg = msgArray[i].split("##");
//			msgMap.put(msg[0], msg[1]);
//		}
//
//		String userPhone = party.getPhone();
//		String userEmail = party.getEmail();
//		String c2cUserPhone = c2cParty.getPhone();
//		String c2cUserEmail = c2cParty.getEmail();
//		
//		String userContent = "";
//		String c2cUserContent = "";
//		
//		// 订单状态：0未付款/1已付款/2申诉中/3已完成/4已取消/5已超时
//		switch (state) {
//			case "0":
//				if ("buy".equals(order.getDirection())) {
//					userContent = MessageFormat.format(msgMap.get("BUY_CREAT_ORDER"), new Object[] { order.getOrderNo(), order.getAmount(), order.getCurrency() });
//					if (1 == c2cUser.getOrderSmsNoticeOpen()) {
//						this.c2cSendMessage(userPhone, userContent);
//					}
//					if (1 == c2cUser.getOrderMailNoticeOpen()) {
//						this.c2cSendMessage(userEmail, userContent);
//					}
//				} else {
//					c2cUserContent = MessageFormat.format(msgMap.get("SELL_CREAT_ORDER"), new Object[] { order.getOrderNo(), order.getAmount(), order.getCurrency() });
//					if (1 == c2cUser.getOrderSmsNoticeOpen()) {
//						this.c2cSendMessage(c2cUserPhone, c2cUserContent);
//					}
//					if (1 == c2cUser.getOrderMailNoticeOpen()) {
//						this.c2cSendMessage(c2cUserEmail, c2cUserContent);
//					}
//				}
//				break;
//			case "1":
//				if ("buy".equals(order.getDirection())) {
//					c2cUserContent = MessageFormat.format(msgMap.get("PAY_FINISH"), new Object[] { order.getOrderNo(), order.getAmount(), order.getCurrency() });
//					if (1 == c2cUser.getOrderSmsNoticeOpen()) {
//						this.c2cSendMessage(c2cUserPhone, c2cUserContent);
//					}
//					if (1 == c2cUser.getOrderMailNoticeOpen()) {
//						this.c2cSendMessage(c2cUserEmail, c2cUserContent);
//					}
//				} else {
//					userContent = MessageFormat.format(msgMap.get("PAY_FINISH"), new Object[] { order.getOrderNo(), order.getAmount(), order.getCurrency() });
//					if (1 == c2cUser.getOrderSmsNoticeOpen()) {
//						this.c2cSendMessage(userPhone, userContent);
//					}
//					if (1 == c2cUser.getOrderMailNoticeOpen()) {
//						this.c2cSendMessage(userEmail, userContent);
//					}
//				}
//				break;
//			case "2":
//				if ("buy".equals(order.getDirection())) {
//					userContent = MessageFormat.format(msgMap.get("APPEAL"), new Object[] { order.getOrderNo() });
//					if (1 == c2cUser.getOrderSmsNoticeOpen()) {
//						this.c2cSendMessage(userPhone, userContent);
//					}
//					if (1 == c2cUser.getOrderMailNoticeOpen()) {
//						this.c2cSendMessage(userEmail, userContent);
//					}
//				} else {
//					c2cUserContent = MessageFormat.format(msgMap.get("APPEAL"), new Object[] { order.getOrderNo() });
//					if (1 == c2cUser.getOrderSmsNoticeOpen()) {
//						this.c2cSendMessage(c2cUserPhone, c2cUserContent);
//					}
//					if (1 == c2cUser.getOrderMailNoticeOpen()) {
//						this.c2cSendMessage(c2cUserEmail, c2cUserContent);
//					}
//				}
//				break;
//			case "3":
//				if ("buy".equals(order.getDirection())) {
//					userContent = MessageFormat.format(msgMap.get("SUCCEED"), new Object[] { order.getOrderNo(), order.getCoinAmount(), order.getSymbol() });
//					if (1 == c2cUser.getOrderSmsNoticeOpen()) {
//						this.c2cSendMessage(userPhone, userContent);
//					}
//					if (1 == c2cUser.getOrderMailNoticeOpen()) {
//						this.c2cSendMessage(userEmail, userContent);
//					}
//				} else {
//					c2cUserContent = MessageFormat.format(msgMap.get("SUCCEED"), new Object[] { order.getOrderNo(), order.getCoinAmount(), order.getSymbol() });
//					if (1 == c2cUser.getOrderSmsNoticeOpen()) {
//						this.c2cSendMessage(c2cUserPhone, c2cUserContent);
//					}
//					if (1 == c2cUser.getOrderMailNoticeOpen()) {
//						this.c2cSendMessage(c2cUserEmail, c2cUserContent);
//					}
//				}
//				break;
//			case "4":
//				break;
//			case "5":
//				userContent = MessageFormat.format(msgMap.get("EXPIRED"), new Object[] { order.getOrderNo() });
//				if (1 == c2cUser.getOrderSmsNoticeOpen()) {
//					this.c2cSendMessage(userPhone, userContent);
//				}
//				if (1 == c2cUser.getOrderMailNoticeOpen()) {
//					this.c2cSendMessage(userEmail, userContent);
//				}
//				c2cUserContent = MessageFormat.format(msgMap.get("EXPIRED"), new Object[] { order.getOrderNo() });
//				if (1 == c2cUser.getOrderSmsNoticeOpen()) {
//					this.c2cSendMessage(c2cUserPhone, c2cUserContent);
//				}
//				if (1 == c2cUser.getOrderMailNoticeOpen()) {
//					this.c2cSendMessage(c2cUserEmail, c2cUserContent);
//				}
//				break;
//			default:
//				break;
//		}
	}

	public void c2cSendMessage(String target, String content) {
		
		if (target.indexOf("@") == -1) {
			// 短信
			
			// 发送的短信接口类型
			String send_code_type = this.sysparaService.find("send_code_type").getValue();
			if (StringUtils.isNullOrEmpty(send_code_type)) {
				logger.error("System parameter send_code_type have not configured.");
				return;
			}
			
			if ("tiantian".equals(send_code_type)) {
				this.smsSendService.send(target, content);
				logger.info(MessageFormat.format("tiantian--target:{0},content:{1}", target, content));
			} else if ("smsbao".equals(send_code_type)) {
				this.smsSendService.send(target, content);
				logger.info(MessageFormat.format("smsbao--target:{0},content:{1}", target, content));
			}

		} else {
			// 邮件
			
			this.emailSendService.sendEmail(target, content, content);
			logger.info(MessageFormat.format("email--target:{0},content:{1}", target, content));
		}
	}

	public void setPagedDao(PagedQueryDao pagedDao) {
		this.pagedDao = pagedDao;
	}

	public void setRedisHandler(RedisHandler redisHandler) {
		this.redisHandler = redisHandler;
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public void setC2cAdvertService(C2cAdvertService c2cAdvertService) {
		this.c2cAdvertService = c2cAdvertService;
	}

	public void setC2cUserService(C2cUserService c2cUserService) {
		this.c2cUserService = c2cUserService;
	}

	public void setDataService(DataService dataService) {
		this.dataService = dataService;
	}

	public void setExchangeRateService(ExchangeRateService exchangeRateService) {
		this.exchangeRateService = exchangeRateService;
	}

	public void setWalletService(WalletService walletService) {
		this.walletService = walletService;
	}

	public void setMoneyLogService(MoneyLogService moneyLogService) {
		this.moneyLogService = moneyLogService;
	}

	public void setC2cPaymentMethodService(C2cPaymentMethodService c2cPaymentMethodService) {
		this.c2cPaymentMethodService = c2cPaymentMethodService;
	}

	public void setUserDataService(UserDataService userDataService) {
		this.userDataService = userDataService;
	}

	public void setTipService(TipService tipService) {
		this.tipService = tipService;
	}

	public void setSysparaService(SysparaService sysparaService) {
		this.sysparaService = sysparaService;
	}

	public void setKycService(KycService kycService) {
		this.kycService = kycService;
	}

	public void setKycHighLevelService(KycHighLevelService kycHighLevelService) {
		this.kycHighLevelService = kycHighLevelService;
	}

	public void setPartyService(PartyService partyService) {
		this.partyService = partyService;
	}

	public void setSmsSendService(SmsSendService smsSendService) {
		this.smsSendService = smsSendService;
	}

	public void setEmailSendService(EmailSendService emailSendService) {
		this.emailSendService = emailSendService;
	}

}
