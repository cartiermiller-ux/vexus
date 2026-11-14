package project.web.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import kernel.exception.BusinessException;
import kernel.sessiontoken.SessionTokenService;
import kernel.util.DateUtils;
import kernel.util.StringUtils;
import kernel.web.BaseAction;
import kernel.web.Page;
import kernel.web.ResultObject;
import project.Constants;
import project.c2c.C2cAdvert;
import project.c2c.C2cAdvertService;
import project.c2c.C2cAppealService;
import project.c2c.C2cOrderService;
import project.c2c.C2cPaymentMethodConfig;
import project.c2c.C2cPaymentMethodConfigService;
import project.c2c.C2cPaymentMethodService;
import project.c2c.C2cTranslate;
import project.c2c.C2cTranslateService;
import project.c2c.C2cUser;
import project.c2c.C2cUserService;
import project.item.ItemService;
import project.item.model.Item;
import project.log.LogService;
import project.party.PartyService;
import project.party.model.Party;
import project.syspara.SysparaService;
import project.wallet.rate.ExchangeRate;
import project.wallet.rate.ExchangeRateService;
import security.SecUser;
import security.internal.SecUserService;

/**
 * C2C广告
 */
@RestController
@CrossOrigin
public class C2cAdvertController extends BaseAction {

	private Logger logger = LoggerFactory.getLogger(C2cAdvertController.class);

	@Autowired
	private C2cAdvertService c2cAdvertService;
	@Autowired
	private C2cUserService c2cUserService;
	@Autowired
	private PartyService partyService;
	@Autowired
	private ExchangeRateService exchangeRateService;
	@Autowired
	private ItemService itemService;
	@Autowired
	private C2cPaymentMethodConfigService c2cPaymentMethodConfigService;
	@Autowired
	private C2cPaymentMethodService c2cPaymentMethodService;
	@Autowired
	private SessionTokenService sessionTokenService;
	@Autowired
	private SecUserService secUserService;
	@Autowired
	private LogService logService;
	@Autowired
	private SysparaService sysparaService;
	@Autowired
	private C2cOrderService c2cOrderService;
	@Autowired
	private C2cAppealService c2cAppealService;
	@Autowired
	private C2cTranslateService c2cTranslateService;
    
	private final String action = "/api/c2cAdvert!";
	
//	/**
//	 * 首次进入新增或修改广告页面，传递session_token
//	 */
//	@RequestMapping(action + "advert_open.action")
//	public Object advert_open(HttpServletRequest request) throws IOException {
//		String currency = request.getParameter("currency");
//		String symbol = request.getParameter("symbol");
//		String language = request.getParameter("language");
//
//		ResultObject resultObject = new ResultObject();
//		resultObject = this.readSecurityContextFromSession(resultObject);
//		if (!"0".equals(resultObject.getCode())) {
//			return resultObject;
//		}
//		
//		try {
//
//			Map<String, Object> data = new HashMap<String, Object>();
//			
//			String partyId = this.getLoginPartyId();
//			String session_token = this.sessionTokenService.savePut(partyId);
//			data.put("session_token", session_token);
//			
//			Map<String, String> methodConfigMap = this.c2cPaymentMethodService.getMethodConfigMapByPartyId(partyId);
//			if (null == methodConfigMap || 0 == methodConfigMap.size()) {				
//				data.put("method_config_map", new HashMap<String, String>());
//			} else {
//								
//				// 多语言
//				for (String id : methodConfigMap.keySet()) {
//					String name = methodConfigMap.get(id);
//					if (null != name) {
//						C2cTranslate trans = this.c2cTranslateService.get(name, language);
//						if (null != trans) {
//							methodConfigMap.put(id, trans.getTranslate());
//						}
//					}
//				}
//				
//				data.put("method_config_map", methodConfigMap);
//			}
//			
//			// 计算广告参数
//			Map<String, Object> computeValue = this.c2cAdvertService.getComputeValue(1, currency, symbol, 1, 1);
//			
//			data.put("price", computeValue.get("price"));
//			
//			data.put("expire_time_map", this.c2cAdvertService.getC2cSyspara("c2c_advert_expire_time"));
//
//			resultObject.setData(data);
//
//		} catch (BusinessException e) {
//			resultObject.setCode("1");
//			resultObject.setMsg(e.getMessage());
//		} catch (Throwable t) {
//			resultObject.setCode("1");
//			resultObject.setMsg("程序错误");
//			logger.error("error:", t);
//		}
//		
//		return resultObject;
//	}
	
//	/**
//	 * 计算广告参数
//	 * 
//	 * currency 支付币种
//	 * symbol 上架币种
//	 * coin_amount 交易数量
//	 * symbol_value 币种单价
//	 */
//	@RequestMapping(action + "getComputeValue.action")
//	public Object getComputeValue(HttpServletRequest request) throws IOException {
//		String currency = request.getParameter("currency");
//		String symbol = request.getParameter("symbol");
//		String coin_amount = request.getParameter("coin_amount");
//		String symbol_value = request.getParameter("symbol_value");
//
//		ResultObject resultObject = new ResultObject();
//		resultObject = this.readSecurityContextFromSession(resultObject);
//		if (!"0".equals(resultObject.getCode())) {
//			return resultObject;
//		}
//		
//		try {
//			
//			String partyId = this.getLoginPartyId();
//			if (null == partyId) {
//				throw new BusinessException("请重新登录");
//			}
//			
//			C2cUser c2cUser = this.c2cUserService.getByPartyId(partyId);
//			if (null == c2cUser) {
//				throw new BusinessException("承兑商不存在");
//			}
//			
//			Map<String, String> currencyMap = this.c2cAdvertService.getCurrencyMap();
//			Map<String, String> symbolMap = this.c2cAdvertService.getSymbolMap();
//			
//			if (StringUtils.isEmptyString(currency) || null == currencyMap || (null != currencyMap && !currencyMap.containsKey(currency))) {
//				throw new BusinessException("支付币种不正确");
//			}		
//			if (StringUtils.isEmptyString(symbol) || null == symbolMap || (null != symbolMap && !symbolMap.containsKey(symbol))) {
//				throw new BusinessException("上架币种不正确");
//			}
//			
//			double coin_amount_double = 0;
//			if (!StringUtils.isEmptyString(coin_amount)) {
//				coin_amount_double = Double.valueOf(coin_amount).doubleValue();
//			}
//			
//			double symbol_value_double = 1;
//			if (!StringUtils.isEmptyString(symbol_value)) {
//				symbol_value_double = Double.valueOf(symbol_value).doubleValue();
//			}
//			
//			// 计算广告参数
//			Map<String, Object> computeValue = this.c2cAdvertService.getComputeValue(c2cUser.getDeposit(), currency, symbol, coin_amount_double, symbol_value_double);
//
//			resultObject.setData(computeValue);
//
//		} catch (BusinessException e) {
//			resultObject.setCode("1");
//			resultObject.setMsg(e.getMessage());
//		} catch (Throwable t) {
//			resultObject.setCode("1");
//			resultObject.setMsg("程序错误");
//			logger.error("error:", t);
//		}
//		
//		return resultObject;
//	}

//	/**
//	 * 发布广告
//	 * 
//	 * safeword 资金密码
//	 * payment_method1 支付方式模板ID1
//	 * payment_method2 支付方式模板ID2
//	 * payment_method3 支付方式模板ID3
//	 * direction 买卖方式：buy买/sell卖
//	 * currency 支付币种
//	 * symbol 上架币种
//	 * coin_amount 交易数量
//	 * symbol_value 币种单价
//	 * investment_min 单笔订单最低限额
//	 * investment_max 单笔订单最高限额
//	 * on_sale 是否上架：0下架/1上架
//	 * expire_time 支付时效
//	 * transaction_terms 交易条款
//	 * order_msg 订单自动消息
//	 * remark 备注
//	 */
//	@RequestMapping(action + "add.action")
//	public Object add(HttpServletRequest request) throws IOException {
//		String session_token = request.getParameter("session_token");
//		String safeword = request.getParameter("safeword");
//		String payment_method1 = request.getParameter("payment_method1");
//		String payment_method2 = request.getParameter("payment_method2");
//		String payment_method3 = request.getParameter("payment_method3");
//		String coin_amount = request.getParameter("coin_amount");
//		String direction = request.getParameter("direction");
//		String currency = request.getParameter("currency");
//		String symbol = request.getParameter("symbol");
//		String symbol_value = request.getParameter("symbol_value");
//		String investment_min = request.getParameter("investment_min");	
//		String investment_max = request.getParameter("investment_max");	
//		String on_sale = request.getParameter("on_sale");
//		String expire_time = request.getParameter("expire_time");
//		String transaction_terms = request.getParameter("transaction_terms");
//		String order_msg = request.getParameter("order_msg");
//		String remark = request.getParameter("remark");
//		
//		ResultObject resultObject = new ResultObject();
//		resultObject = this.readSecurityContextFromSession(resultObject);
//		if (!"0".equals(resultObject.getCode())) {
//			return resultObject;
//		}
//
//		// 支付方式拼接
//		String pay_type = "";
//		if (StringUtils.isNotEmpty(payment_method1)) {
//			if ("".equals(pay_type)) {
//				pay_type += payment_method1;
//			} else {
//				pay_type = pay_type + "," + payment_method1;
//			}
//		}
//		if (StringUtils.isNotEmpty(payment_method2) && !pay_type.contains(payment_method2)) {
//			if ("".equals(pay_type)) {
//				pay_type += payment_method2;
//			} else {
//				pay_type = pay_type + "," + payment_method2;
//			}
//		}
//		if (StringUtils.isNotEmpty(payment_method3) && !pay_type.contains(payment_method3)) {
//			if ("".equals(pay_type)) {
//				pay_type += payment_method3;
//			} else {
//				pay_type = pay_type + "," + payment_method3;
//			}
//		}
//		
//		return this.add_advert(session_token, safeword, coin_amount, direction, pay_type, currency, symbol, 
//				symbol_value, investment_min, investment_max, on_sale, expire_time, transaction_terms, order_msg, remark);
//	}

//	/**
//	 * 已关闭广告重新上架
//	 * 
//	 * id 广告id
//	 */
//	@RequestMapping(action + "add_closed.action")
//	public Object add_closed(HttpServletRequest request) throws IOException {
//		String id = request.getParameter("id");
//		String safeword = request.getParameter("safeword");
//		
//		ResultObject resultObject = new ResultObject();
//		resultObject = this.readSecurityContextFromSession(resultObject);
//		if (!"0".equals(resultObject.getCode())) {
//			return resultObject;
//		}
//		
//		try {
//			
//			String partyId = this.getLoginPartyId();
//			if (null == partyId) {
//				throw new BusinessException("请重新登录");
//			}
//			String session_token = this.sessionTokenService.savePut(partyId);
//			
//			C2cUser c2cUser = this.c2cUserService.getByPartyId(partyId);
//			if (null == c2cUser) {
//				throw new BusinessException("承兑商不存在");
//			}
//			
//			C2cAdvert c2cAdvert = this.c2cAdvertService.get(id);
//			if (null == c2cAdvert || !c2cAdvert.getC2cUserId().equals(c2cUser.getId().toString())) {
//				throw new BusinessException("广告不存在");
//			}
//			
//			// 计算广告参数
//			Map<String, Object> computeValue = this.c2cAdvertService.getComputeValue(c2cAdvert.getDepositOpen(), c2cAdvert.getCurrency(), c2cAdvert.getSymbol(), 1, 1);
//			
//			String coin_amount = computeValue.get("coin_amount_max").toString();
//			
//			return this.add_advert(session_token, safeword, coin_amount, c2cAdvert.getDirection(), c2cAdvert.getPayType(), 
//					c2cAdvert.getCurrency(), c2cAdvert.getSymbol(), String.valueOf(c2cAdvert.getSymbolValue()), String.valueOf(c2cAdvert.getInvestmentMin()), 
//					String.valueOf(c2cAdvert.getInvestmentMax()), "1", String.valueOf(c2cAdvert.getExpireTime()), c2cAdvert.getTransactionTerms(), 
//					c2cAdvert.getOrderMsg(), c2cAdvert.getRemark());
//
//		} catch (BusinessException e) {
//			resultObject.setCode("1");
//			resultObject.setMsg(e.getMessage());
//		} catch (Throwable t) {
//			resultObject.setCode("1");
//			resultObject.setMsg("程序错误");
//			logger.error("error:", t);
//		}
//		
//		return resultObject;
//	}

//	/**
//	 * 发布广告
//	 */
//	public Object add_advert(String session_token, String safeword, String coin_amount, String direction, String pay_type, String currency, String symbol, String symbol_value, String investment_min, 
//			String investment_max, String on_sale, String expire_time, String transaction_terms, String order_msg, String remark) throws IOException {
//
//		ResultObject resultObject = new ResultObject();
//		
//		String partyId = this.getLoginPartyId();
//		if (null == partyId) {
//			throw new BusinessException("请重新登录");
//		}
//		
//		boolean lock = false;
//		
//		try {
//
//			String error = this.verif_add(coin_amount, direction, pay_type, currency, symbol, symbol_value, 
//					investment_min, investment_max, on_sale, expire_time, safeword);
//			if (StringUtils.isNotEmpty(error)) {
//				throw new BusinessException(error);
//			}
//			
//			DecimalFormat df = new DecimalFormat("#.########");
//			
//			double coin_amount_double = Double.valueOf(df.format(Double.valueOf(coin_amount))).doubleValue();
//			double symbol_value_double = Double.valueOf(df.format(Double.valueOf(symbol_value))).doubleValue();
//			double investment_min_double = Double.valueOf(df.format(Double.valueOf(investment_min))).doubleValue();
//			double investment_max_double = Double.valueOf(df.format(Double.valueOf(investment_max))).doubleValue();
//			
//			Party party = this.partyService.cachePartyBy(partyId, false);
//			if (Constants.SECURITY_ROLE_TEST.equals(party.getRolename())) {
//				throw new BusinessException("无权限");
//			}
//			
//			C2cUser c2cUser = this.c2cUserService.getByPartyId(partyId);
//			if (null == c2cUser) {
//				throw new BusinessException("承兑商不存在");
//			}
//			
//			// C2C承兑商添加广告最大数量
//			List<C2cAdvert> advertList = this.c2cAdvertService.getByC2cUserId(c2cUser.getId().toString());
//			Object obj = this.sysparaService.find("c2c_advert_count_max");
//			if (null != obj) {
//				if (advertList.size() >= Integer.valueOf(this.sysparaService.find("c2c_advert_count_max").getValue()).intValue()) {
//					throw new BusinessException("广告数量已达上限");
//				}
//			}
//			
//			if (!C2cAdvertLock.add(partyId)) {
//				throw new BusinessException("请稍后再试");
//			}
//			
//			lock = true;
//			
//			Object object = this.sessionTokenService.cacheGet(session_token);
//			this.sessionTokenService.del(session_token);
//			if (null == object || !this.getLoginPartyId().equals((String) object)) {
//				throw new BusinessException("请稍后再试");
//			}
//			
//			if (!this.partyService.checkSafeword(safeword, partyId)) {
//				throw new BusinessException("资金密码错误");
//			}
//	
//			// C2C承兑商发布广告是否需要基础认证（true:是，false:否）
//			Object obj1 = this.sysparaService.find("c2c_advert_need_kyc");
//			if (null != obj1) {
//				if (!party.getKyc_authority() && "true".equals(this.sysparaService.find("c2c_advert_need_kyc").getValue())) {
//					resultObject.setCode("401");
//					resultObject.setMsg("未实名认证，是否认证？");
//					return resultObject;
//				}
//			}
//			
//			// 计算广告参数
//			Map<String, Object> computeValue = this.c2cAdvertService.getComputeValue(c2cUser.getDeposit(), currency, symbol, coin_amount_double, symbol_value_double);
//			
//			if (coin_amount_double > Double.valueOf(computeValue.get("coin_amount_max").toString()).doubleValue()) {
//				throw new BusinessException("交易币种数量不能大于最大可交易数量");
//			}
//			
//			if (investment_min_double <= 0 || investment_min_double < Double.valueOf(computeValue.get("investment_min_limit").toString()).doubleValue()) {
//				throw new BusinessException("单笔订单支付金额下限错误");
//			}
//			
//			if (investment_max_double > Double.valueOf(computeValue.get("investment_max_limit").toString()).doubleValue()) {
//				throw new BusinessException("单笔订单支付金额上限错误");
//			}
//			
//			double pay_rate_double = Double.valueOf(computeValue.get("pay_rate").toString()).doubleValue();
//			double symbol_close_double = Double.valueOf(computeValue.get("symbol_close").toString()).doubleValue();
//			
//			double deposit_open_double = Double.valueOf(computeValue.get("deposit_open").toString()).doubleValue();
//							
//			if (deposit_open_double < 0) {
//				throw new BusinessException("广告总保证金和剩余保证金不能小于0");
//			}
//			
//			C2cAdvert c2cAdvert = new C2cAdvert();
//			c2cAdvert.setC2cUserId(c2cUser.getId().toString());
//			c2cAdvert.setDirection(direction);
//			c2cAdvert.setCurrency(currency);
//			c2cAdvert.setSymbol(symbol);
//			c2cAdvert.setSymbolClose(symbol_close_double);
//			c2cAdvert.setPayRate((int) pay_rate_double);
//			c2cAdvert.setPayType(pay_type);
//			c2cAdvert.setSymbolValue(symbol_value_double);
//			c2cAdvert.setCoinAmount(coin_amount_double);
//			c2cAdvert.setInvestmentMin(investment_min_double);
//			c2cAdvert.setInvestmentMax(investment_max_double);
//			c2cAdvert.setDeposit(deposit_open_double);
//			c2cAdvert.setDepositOpen(deposit_open_double);
//			c2cAdvert.setOnSale(Integer.valueOf(on_sale).intValue());
//			c2cAdvert.setClosed(0);
//			c2cAdvert.setSortIndex(0);
//			c2cAdvert.setExpireTime(Integer.valueOf(expire_time).intValue());
//			c2cAdvert.setTransactionTerms(transaction_terms);
//			c2cAdvert.setOrderMsg(order_msg);
//			c2cAdvert.setRemark(remark);
//			c2cAdvert.setCreateTime(new Date());
//			c2cAdvert.setUpdateTime(new Date());
//			
//			this.c2cAdvertService.save(c2cAdvert);
//			
//			double oldC2cUserDeposit = c2cUser.getDeposit();
//			
//			c2cUser.setDeposit(Arith.sub(c2cUser.getDeposit(), deposit_open_double));
//			
//			this.c2cUserService.update(c2cUser);
//						
//			String log = MessageFormat.format("ip:" + this.getIp(getRequest())
//			+ ",承兑商新增广告,id:{0},承兑商ID:{1},买卖方式:{2},支付币种:{3},上架币种:{4},上架币种实时行情价:{5},支付比率:{6},支付方式:{7},币种单价:{8},"
//			+ "币种数量:{9},单笔订单最低限额:{10},单笔订单最高限额:{11},剩余派单金额:{12},派单金额:{13},是否上架:{14},是否关闭:{15},"
//			+ "排序索引:{16},支付时效:{17},交易条款:{18},订单自动消息:{19},备注:{20},创建时间:{21},更新时间:{22}#####原承兑商剩余派单金额:{23},新承兑商剩余派单金额:{24}", 
//			c2cAdvert.getId(), c2cAdvert.getC2cUserId(), c2cAdvert.getDirection(), c2cAdvert.getCurrency(), c2cAdvert.getSymbol(), c2cAdvert.getSymbolClose(), c2cAdvert.getPayRate(), c2cAdvert.getPayType(), c2cAdvert.getSymbolValue(), 
//			c2cAdvert.getCoinAmount(), c2cAdvert.getInvestmentMin(), c2cAdvert.getInvestmentMax(), c2cAdvert.getDeposit(), c2cAdvert.getDepositOpen(), c2cAdvert.getOnSale(), c2cAdvert.getClosed(), 
//			c2cAdvert.getSortIndex(), c2cAdvert.getExpireTime(), c2cAdvert.getTransactionTerms(), c2cAdvert.getOrderMsg(), c2cAdvert.getRemark(), c2cAdvert.getCreateTime(), c2cAdvert.getUpdateTime(), 
//			oldC2cUserDeposit, c2cUser.getDeposit());
//
//			SecUser sec = this.secUserService.findUserByPartyId(partyId);
//			
//			this.saveLog(sec, sec.getUsername(), log, Constants.LOG_CATEGORY_C2C);
//			
//			ThreadUtils.sleep(100);
//
//		} catch (BusinessException e) {
//			resultObject.setCode("1");
//			resultObject.setMsg(e.getMessage());
//		} catch (Throwable t) {
//			resultObject.setCode("1");
//			resultObject.setMsg("程序错误");
//			logger.error("error:", t);
//		} finally {
//			if (lock) {
//				C2cAdvertLock.remove(partyId);
//			}
//		}
//		
//		return resultObject;
//	}

//	/**
//	 * 修改广告
//	 * 
//	 * safeword 资金密码
//	 * payment_method1 支付方式模板ID1
//	 * payment_method2 支付方式模板ID2
//	 * payment_method3 支付方式模板ID3
//	 * coin_amount 交易数量
//	 * direction 买卖方式：buy买/sell卖
//	 * currency 支付币种
//	 * symbol 上架币种
//	 * symbol_value 币种单价
//	 * investment_min 单笔订单最低限额
//	 * investment_max 单笔订单最高限额
//	 * on_sale 是否上架：0下架/1上架
//	 * closed 是否关闭：0否/1是
//	 * expire_time 支付时效
//	 * transaction_terms 交易条款
//	 * order_msg 订单自动消息
//	 * remark 备注
//	 */
//	@RequestMapping(action + "update.action")
//	public Object update(HttpServletRequest request) throws IOException {
//		String id = request.getParameter("id");
//		String session_token = request.getParameter("session_token");
//		String payment_method1 = request.getParameter("payment_method1");
//		String payment_method2 = request.getParameter("payment_method2");
//		String payment_method3 = request.getParameter("payment_method3");
//		String direction = request.getParameter("direction");	
//		String coin_amount = request.getParameter("coin_amount");		
//		String symbol_value = request.getParameter("symbol_value");
//		String currency = request.getParameter("currency");
//		String symbol = request.getParameter("symbol");	
//		String investment_min = request.getParameter("investment_min");	
//		String investment_max = request.getParameter("investment_max");	
//		String on_sale = request.getParameter("on_sale");
//		String closed = request.getParameter("closed");
////		String sort_index = request.getParameter("sort_index");
//		String expire_time = request.getParameter("expire_time");
//		String transaction_terms = request.getParameter("transaction_terms");
//		String order_msg = request.getParameter("order_msg");
//		String remark = request.getParameter("remark");
//		String safeword = request.getParameter("safeword");
//
//		ResultObject resultObject = new ResultObject();
//		resultObject = this.readSecurityContextFromSession(resultObject);
//		if (!"0".equals(resultObject.getCode())) {
//			return resultObject;
//		}
//		
//		String partyId = this.getLoginPartyId();
//		if (null == partyId) {
//			throw new BusinessException("请重新登录");
//		}
//		
//		boolean lock = false;
//		
//		try {
//			
//			// 支付方式拼接
//			String pay_type = "";
//			if (StringUtils.isNotEmpty(payment_method1)) {
//				if ("".equals(pay_type)) {
//					pay_type += payment_method1;
//				} else {
//					pay_type = pay_type + "," + payment_method1;
//				}
//			}
//			if (StringUtils.isNotEmpty(payment_method2) && !pay_type.contains(payment_method2)) {
//				if ("".equals(pay_type)) {
//					pay_type += payment_method2;
//				} else {
//					pay_type = pay_type + "," + payment_method2;
//				}
//			}
//			if (StringUtils.isNotEmpty(payment_method3) && !pay_type.contains(payment_method3)) {
//				if ("".equals(pay_type)) {
//					pay_type += payment_method3;
//				} else {
//					pay_type = pay_type + "," + payment_method3;
//				}
//			}
//
//			String error = this.verif_add(coin_amount, direction, pay_type, currency, symbol, symbol_value, 
//					investment_min, investment_max, on_sale, expire_time, safeword);
//			if (StringUtils.isNotEmpty(error)) {
//				throw new BusinessException(error);
//			}
//			
//			DecimalFormat df = new DecimalFormat("#.########");
//			
//			double coin_amount_double = Double.valueOf(df.format(Double.valueOf(coin_amount))).doubleValue();
//			double symbol_value_double = Double.valueOf(df.format(Double.valueOf(symbol_value))).doubleValue();
//			double investment_min_double = Double.valueOf(df.format(Double.valueOf(investment_min))).doubleValue();
//			double investment_max_double = Double.valueOf(df.format(Double.valueOf(investment_max))).doubleValue();
//			
//			Party party = this.partyService.cachePartyBy(partyId, false);
//			if (Constants.SECURITY_ROLE_TEST.equals(party.getRolename())) {
//				throw new BusinessException("无权限");
//			}
//
//			if (!C2cAdvertLock.add(partyId)) {
//				throw new BusinessException("请稍后再试");
//			}
//			
//			lock = true;
//						
//			C2cUser c2cUser = this.c2cUserService.getByPartyId(party.getId().toString());
//			if (null == c2cUser) {
//				throw new BusinessException("承兑商不存在");
//			}
//
//			C2cAdvert c2cAdvert = this.c2cAdvertService.get(id);
//			if (null == c2cAdvert || !c2cAdvert.getC2cUserId().equals(c2cUser.getId().toString())) {
//				throw new BusinessException("广告不存在");
//			}
//			
//			Object object = this.sessionTokenService.cacheGet(session_token);
//			this.sessionTokenService.del(session_token);
//			if (null == object || !this.getLoginPartyId().equals((String) object)) {
//				throw new BusinessException("请稍后再试");
//			}
//			
//			if (!this.partyService.checkSafeword(safeword, partyId)) {
//				throw new BusinessException("资金密码错误");
//			}
//	
//			// C2C承兑商发布广告是否需要基础认证（true:是，false:否）
//			Object obj = this.sysparaService.find("c2c_advert_need_kyc");
//			if (null != obj) {
//				if (!party.getKyc_authority() && "true".equals(this.sysparaService.find("c2c_advert_need_kyc").getValue())) {
//					resultObject.setCode("401");
//					resultObject.setMsg("未实名认证，是否认证？");
//					return resultObject;
//				}
//			}
//			
//			// 计算广告参数
//			Map<String, Object> computeValue = this.c2cAdvertService.getComputeValue(Arith.add(c2cUser.getDeposit(), c2cAdvert.getDeposit()), currency, symbol, 
//					coin_amount_double, symbol_value_double);
//			
//			if (coin_amount_double > Double.valueOf(computeValue.get("coin_amount_max").toString()).doubleValue()) {
//				throw new BusinessException("交易币种数量不能大于最大可交易数量");
//			}
//			
//			if (investment_min_double <= 0 || investment_min_double < Double.valueOf(computeValue.get("investment_min_limit").toString()).doubleValue()) {
//				throw new BusinessException("单笔订单支付金额下限错误");
//			}
//			
//			if (investment_max_double > Double.valueOf(computeValue.get("investment_max_limit").toString()).doubleValue()) {
//				throw new BusinessException("单笔订单支付金额上限错误");
//			}
//			
//			double pay_rate_double = Double.valueOf(computeValue.get("pay_rate").toString()).doubleValue();
//			double symbol_close_double = Double.valueOf(computeValue.get("symbol_close").toString()).doubleValue();
//			
//			double deposit_open_old_double = c2cAdvert.getDepositOpen();
//			double deposit_old_double = c2cAdvert.getDeposit();
//			double deposit_open_double = Double.valueOf(computeValue.get("deposit_open").toString()).doubleValue();
//			double change = deposit_open_double - deposit_open_old_double;
//			double deposit_double = deposit_old_double + change;
//			
//			if (deposit_double < 0 || deposit_open_double < 0) {
//			throw new BusinessException("广告总保证金和剩余保证金不能小于0");
//			}
//			
//			if (deposit_double > deposit_open_double) {
//			throw new BusinessException("广告剩余保证金不能大于广告总保证金");
//			}
//			
//			if (change > 0) {				
//				if (Double.valueOf(change).doubleValue() > c2cUser.getDeposit()) {
//					throw new BusinessException("广告总保证金增加差值不能大于承兑商剩余总保证金");
//				}				
//			}
//			
//			String log = MessageFormat.format("ip:" + this.getIp(getRequest())
//				+ ",承兑商修改广告,id:{0},原承兑商ID:{1},原买卖方式:{2},原支付币种:{3},原上架币种:{4},原上架币种实时行情价:{5},原支付比率:{6},原支付方式:{7},原币种单价:{8},"
//				+ "原币种数量:{9},原单笔订单最低限额:{10},原单笔订单最高限额:{11},原剩余派单金额:{12},原派单金额:{13},原是否上架:{14},原是否关闭:{15},"
//				+ "原排序索引:{16},原支付时效:{17},原交易条款:{18},原订单自动消息:{19},原备注:{20},原创建时间:{21},原更新时间:{22}", 
//				c2cAdvert.getId(), c2cAdvert.getC2cUserId(), c2cAdvert.getDirection(), c2cAdvert.getCurrency(), c2cAdvert.getSymbol(), c2cAdvert.getSymbolClose(), c2cAdvert.getPayRate(), c2cAdvert.getPayType(), c2cAdvert.getSymbolValue(), 
//				c2cAdvert.getCoinAmount(), c2cAdvert.getInvestmentMin(), c2cAdvert.getInvestmentMax(), c2cAdvert.getDeposit(), c2cAdvert.getDepositOpen(), c2cAdvert.getOnSale(), c2cAdvert.getClosed(), 
//				c2cAdvert.getSortIndex(), c2cAdvert.getExpireTime(), c2cAdvert.getTransactionTerms(), c2cAdvert.getOrderMsg(), c2cAdvert.getRemark(), c2cAdvert.getCreateTime(), c2cAdvert.getUpdateTime());
//		
//			c2cAdvert.setDirection(direction);
//			c2cAdvert.setCurrency(currency);
//			c2cAdvert.setSymbol(symbol);
//			c2cAdvert.setSymbolClose(symbol_close_double);
//			c2cAdvert.setPayRate((int) pay_rate_double);
//			c2cAdvert.setPayType(pay_type);
//			c2cAdvert.setSymbolValue(symbol_value_double);
//			c2cAdvert.setCoinAmount(coin_amount_double);
//			c2cAdvert.setInvestmentMin(investment_min_double);
//			c2cAdvert.setInvestmentMax(investment_max_double);
//			c2cAdvert.setDeposit(deposit_double);
//			c2cAdvert.setDepositOpen(deposit_open_double);
//			c2cAdvert.setOnSale(Integer.valueOf(on_sale).intValue());
//			c2cAdvert.setClosed(StringUtils.isNotEmpty(closed) ? Integer.valueOf(closed).intValue() : 0);
//			c2cAdvert.setExpireTime(Integer.valueOf(expire_time).intValue());
//			c2cAdvert.setTransactionTerms(transaction_terms);
//			c2cAdvert.setOrderMsg(order_msg);
//			c2cAdvert.setRemark(remark);
//			c2cAdvert.setUpdateTime(new Date());
//			
//			this.c2cAdvertService.update(c2cAdvert);
//			
//			double oldC2cUserDeposit = c2cUser.getDeposit();
//			
//			c2cUser.setDeposit(Arith.sub(c2cUser.getDeposit(), change));
//			
//			this.c2cUserService.update(c2cUser);
//			
//			log += MessageFormat.format(",id:{0},新承兑商ID:{1},新买卖方式:{2},新支付币种:{3},新上架币种:{4},新上架币种实时行情价:{5},新支付比率:{6},新支付方式:{7},新币种单价:{8},"
//				+ "新币种数量:{9},新单笔订单最低限额:{10},新单笔订单最高限额:{11},新剩余派单金额:{12},新派单金额:{13},新是否上架:{14},新是否关闭:{15},"
//				+ "新排序索引:{16},新支付时效:{17},新交易条款:{18},新订单自动消息:{19},新备注:{20},新创建时间:{21},新更新时间:{22}#####原承兑商剩余派单金额:{23},新承兑商剩余派单金额:{24}",
//				c2cAdvert.getId(), c2cAdvert.getC2cUserId(), c2cAdvert.getDirection(), c2cAdvert.getCurrency(), c2cAdvert.getSymbol(), c2cAdvert.getSymbolClose(), c2cAdvert.getPayRate(), c2cAdvert.getPayType(), c2cAdvert.getSymbolValue(), 
//				c2cAdvert.getCoinAmount(), c2cAdvert.getInvestmentMin(), c2cAdvert.getInvestmentMax(), c2cAdvert.getDeposit(), c2cAdvert.getDepositOpen(), c2cAdvert.getOnSale(), c2cAdvert.getClosed(), 
//				c2cAdvert.getSortIndex(), c2cAdvert.getExpireTime(), c2cAdvert.getTransactionTerms(), c2cAdvert.getOrderMsg(), c2cAdvert.getRemark(), c2cAdvert.getCreateTime(), c2cAdvert.getUpdateTime(), 
//				oldC2cUserDeposit, c2cUser.getDeposit());
//			
//			SecUser sec = this.secUserService.findUserByPartyId(partyId);
//			
//			this.saveLog(sec, sec.getUsername(), log, Constants.LOG_CATEGORY_C2C);
//			
//			ThreadUtils.sleep(100);
//
//		} catch (BusinessException e) {
//			resultObject.setCode("1");
//			resultObject.setMsg(e.getMessage());
//		} catch (Throwable t) {
//			resultObject.setCode("1");
//			resultObject.setMsg("程序错误");
//			logger.error("error:", t);
//		} finally {
//			if (lock) {
//				C2cAdvertLock.remove(partyId);
//			}
//		}
//		
//		return resultObject;
//	}

//	/**
//	 * 关闭广告
//	 */
//	@RequestMapping(action + "close.action")
//	public Object close(HttpServletRequest request) throws IOException {
//		String id = request.getParameter("id");
//		String safeword = request.getParameter("safeword");
//
//		ResultObject resultObject = new ResultObject();
//		resultObject = this.readSecurityContextFromSession(resultObject);
//		if (!"0".equals(resultObject.getCode())) {
//			return resultObject;
//		}
//		
//		String partyId = this.getLoginPartyId();
//		if (null == partyId) {
//			throw new BusinessException("请重新登录");
//		}
//		
//		boolean lock = false;
//		
//		try {
//
//			if (StringUtils.isEmptyString(id)) {
//				throw new BusinessException("广告id不正确");
//			}
//
//			if (StringUtils.isEmptyString(safeword)) {
//				throw new BusinessException("资金密码错误");
//			}
//
//			if (!C2cAdvertLock.add(partyId)) {
//				throw new BusinessException("请稍后再试");
//			}
//			
//			lock = true;
//			
//			C2cUser c2cUser = this.c2cUserService.getByPartyId(partyId);
//			if (null == c2cUser) {
//				throw new BusinessException("承兑商不存在");
//			}
//			
//			if (!this.partyService.checkSafeword(safeword, partyId)) {
//				throw new BusinessException("资金密码错误");
//			}
//			
//			C2cAdvert c2cAdvert = this.c2cAdvertService.get(id);
//			if (null == c2cAdvert || !c2cAdvert.getC2cUserId().equals(c2cUser.getId().toString())) {
//				throw new BusinessException("广告不存在");
//			}
//			
//			Long orderCount = this.c2cOrderService.findNoEndingOrdersCountByAdvertId(id);
//			if (orderCount > 0) {
//				throw new BusinessException("广告还有未完结订单，不能关闭");
//			}
//			
//			Long appealCount = this.c2cAppealService.findNoHandleAppealsCountByAdvertId(id);
//			if (null == appealCount) {
//				throw new BusinessException("广告还有未处理的订单申诉，不能关闭");
//			}
//
//			double oldC2cUserDeposit = c2cUser.getDeposit();
//			if (c2cAdvert.getDeposit() > 0) {
//				// 退还保证金
//				c2cUser.setDeposit(Arith.add(c2cUser.getDeposit(), c2cAdvert.getDeposit()));
//				this.c2cUserService.update(c2cUser);	
//				c2cAdvert.setDeposit(0);			
//			}
//			
//			c2cAdvert.setOnSale(0);
//			c2cAdvert.setClosed(1);
//			this.c2cAdvertService.update(c2cAdvert);
//			
//			String log = MessageFormat.format("ip:" + this.getIp(getRequest())
//				+ ",承兑商关闭广告,id:{0},承兑商ID:{1},买卖方式:{2},支付币种:{3},上架币种:{4},上架币种实时行情价:{5},支付比率:{6},支付方式:{7},币种单价:{8},"
//				+ "币种数量:{9},单笔订单最低限额:{10},单笔订单最高限额:{11},剩余派单金额:{12},派单金额:{13},是否上架:{14},是否关闭:{15},"
//				+ "排序索引:{16},支付时效:{17},交易条款:{18},订单自动消息:{19},备注:{20},创建时间:{21},更新时间:{22}#####原承兑商剩余派单金额:{23},新承兑商剩余派单金额:{24}", 
//				c2cAdvert.getId(), c2cAdvert.getC2cUserId(), c2cAdvert.getDirection(), c2cAdvert.getCurrency(), c2cAdvert.getSymbol(), c2cAdvert.getSymbolClose(), c2cAdvert.getPayRate(), c2cAdvert.getPayType(), c2cAdvert.getSymbolValue(), 
//				c2cAdvert.getCoinAmount(), c2cAdvert.getInvestmentMin(), c2cAdvert.getInvestmentMax(), c2cAdvert.getDeposit(), c2cAdvert.getDepositOpen(), c2cAdvert.getOnSale(), c2cAdvert.getClosed(), 
//				c2cAdvert.getSortIndex(), c2cAdvert.getExpireTime(), c2cAdvert.getTransactionTerms(), c2cAdvert.getOrderMsg(), c2cAdvert.getRemark(), c2cAdvert.getCreateTime(), c2cAdvert.getUpdateTime(), 
//				oldC2cUserDeposit, c2cUser.getDeposit());
//
//			SecUser sec = this.secUserService.findUserByPartyId(partyId);
//			
//			this.saveLog(sec, sec.getUsername(), log, Constants.LOG_CATEGORY_C2C);
//			
//			ThreadUtils.sleep(100);
//			
//		} catch (BusinessException e) {
//			resultObject.setCode("1");
//			resultObject.setMsg(e.getMessage());
//		} catch (Throwable t) {
//			resultObject.setCode("1");
//			resultObject.setMsg("程序错误");
//			logger.error("error:", t);
//		} finally {
//			if (lock) {
//				C2cAdvertLock.remove(partyId);
//			}
//		}
//		
//		return resultObject;
//	}
	
	/**
	 * 获取 支付币种（法币） 列表
	 */
	@RequestMapping(action + "currency.action")
	public Object currency(HttpServletRequest request) {
		
		ResultObject resultObject = new ResultObject();
		
		try {
			
			Map<String, String> pmtMap = this.c2cAdvertService.getC2cSyspara("c2c_advert_currency");
			
			List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
			
			List<ExchangeRate> exchangeRateList = this.exchangeRateService.findBy(Constants.OUT_OR_IN_DEFAULT);
			for (ExchangeRate er : exchangeRateList) {
				if (pmtMap.keySet().contains(er.getCurrency())) {
					Map<String, Object> erMap = new HashMap<String, Object>();
					erMap.put("out_or_in", er.getOut_or_in());
					erMap.put("rate", er.getRata());
					erMap.put("currency", er.getCurrency());
					erMap.put("name", er.getName());
					erMap.put("currency_symbol", er.getCurrency_symbol());
					data.add(erMap);
				}
			}
			
			resultObject.setData(data);		
			
		} catch (BusinessException e) {
			resultObject.setCode("1");
			resultObject.setMsg(e.getMessage());
		} catch (Throwable t) {
			resultObject.setCode("1");
			resultObject.setMsg("程序错误");
			logger.error("error:", t);
		}

		return resultObject;
	}
	
	/**
	 * 获取 上架币种 列表
	 */
	@RequestMapping(action + "symbol.action")
	public Object symbol(HttpServletRequest request) {
		
		ResultObject resultObject = new ResultObject();
		
		try {
			
			Map<String, String> asMap = this.c2cAdvertService.getC2cSyspara("c2c_advert_symbol");
			
			Map<String, String> data = new HashMap<String, String>();

			List<Item> itemList = this.itemService.cacheGetAll();
			data.put("usdt", "USDT");
			for (Item item : itemList) {
				if (asMap.keySet().contains(item.getSymbol().toUpperCase())) {
					data.put(item.getSymbol(), item.getSymbol().toUpperCase());
				}
			}
			
			resultObject.setData(data);
			
		} catch (BusinessException e) {
			resultObject.setCode("1");
			resultObject.setMsg(e.getMessage());
		} catch (Throwable t) {
			resultObject.setCode("1");
			resultObject.setMsg("程序错误");
			logger.error("error:", t);
		}

		return resultObject;		
	}
	
//	/**
//	 * 获取 广告支付时效 列表
//	 */
//	@RequestMapping(action + "expire_time.action")
//	public Object expire_time(HttpServletRequest request) {
//		
//		ResultObject resultObject = new ResultObject();
//		
//		try {
//			
//			Map<String, String> data = this.c2cAdvertService.getC2cSyspara("c2c_advert_expire_time");
//			
//			resultObject.setData(data);
//			
//		} catch (BusinessException e) {
//			resultObject.setCode("1");
//			resultObject.setMsg(e.getMessage());
//		} catch (Throwable t) {
//			resultObject.setCode("1");
//			resultObject.setMsg("程序错误");
//			logger.error("error:", t);
//		}
//
//		return resultObject;		
//	}
	
	/**
	 * 获取 广告 列表
	 * 
	 * direction 买卖方式：buy买/sell卖
	 * currency 支付币种（大写CNY、USD、CAD等）
	 * symbol 上架币种（大写USDT、BTC、ETH）
	 * method_type 支付方式类型：0其它/1银行卡/2虚拟货币/3微信/4支付宝/5PayPal/6西联汇款/7SWIFT国际汇款
	 * amount 支付金额：必须小于剩余派单金额
	 */
	@RequestMapping(action + "list.action")
	public Object list(HttpServletRequest request) {
		String page_no = request.getParameter("page_no");
		String direction = request.getParameter("direction");
		String currency = request.getParameter("currency");
		String symbol = request.getParameter("symbol");
		String method_type = request.getParameter("method_type");
		String amount = request.getParameter("amount");
		String language = request.getParameter("language");
		
		ResultObject resultObject = new ResultObject();
		resultObject = this.readSecurityContextFromSession(resultObject);
		if (!"0".equals(resultObject.getCode())) {
			return resultObject;
		}
		
		try {

			String error = this.verif(direction, currency, symbol, method_type, amount);
			if (!StringUtils.isNullOrEmpty(error)) {
				throw new BusinessException(error);
			}
			
			if (StringUtils.isNullOrEmpty(page_no)) {
				page_no = "1";
			}
			if (!StringUtils.isInteger(page_no)) {
				throw new BusinessException("页码不是整数");
			}
			if (Integer.valueOf(page_no).intValue() <= 0) {
				throw new BusinessException("页码不能小于等于0");
			}

			int page_no_int = Integer.valueOf(page_no).intValue();	
			
			String partyId = this.getLoginPartyId();
			if (null == partyId) {
				throw new BusinessException("请重新登录");
			}
			
			Party party = this.partyService.cachePartyBy(partyId, false);
			if (null == party) {
				throw new BusinessException("用户不存在");
			}
			
			String c2cUserId = "";
			if (Arrays.asList(1, 2).contains(party.getC2cUserType())) {
				C2cUser c2cUser = this.c2cUserService.getByPartyId(partyId);
				if (null != c2cUser) {
					c2cUserId = c2cUser.getId().toString();
				}
			}			

			Page page = this.c2cAdvertService.pagedQuery(page_no_int, 20, c2cUserId, direction, currency, symbol, amount, 1, 0, false);
			if (null == page) {
				resultObject.setData(new ArrayList<Map<String, Object>>());
			} else {
				
				List<Map<String, Object>> dataResult = new ArrayList<Map<String, Object>>();
				
				if (!StringUtils.isEmptyString(method_type)) {
					
					List<Map<String, Object>> data = (List<Map<String, Object>>) page.getElements();
					
					for (int i = 0; i < data.size(); i++) {					
						Map<String, Object> map = data.get(i);
						
						Object pay_type = map.get("pay_type");
						if (null == pay_type) {
							continue;
						}
						
						// 获取 Map<支付方式模板ID，支付方式模板类型>
						Map<String, String> methodConfigIdTypeMap = this.c2cPaymentMethodConfigService.getMethodConfigIdTypeMap();
						
						String[] payTypes = pay_type.toString().split(",");
						for (String type : payTypes) {
							
							if (method_type.equals(methodConfigIdTypeMap.get(type))) {
								dataResult.add(map);
								break;
							}
						}
					}
				} else {
					dataResult = (List<Map<String, Object>>) page.getElements();
				}
				
				for (int i = 0; i < dataResult.size(); i++) {
					Map<String, Object> mso = dataResult.get(i);
					if (null != mso) {
						
						int thirtyDaysOrder = 0;
						int thirtyDaysOrderBase = 0;
						double thirtyDaysOrderRatio = 0D;
						double thirtyDaysOrderRatioBase = 0D;
						
						if (null != mso.get("thirty_days_order") && StringUtils.isNotEmpty(mso.get("thirty_days_order").toString())) {
							thirtyDaysOrder = Integer.valueOf(mso.get("thirty_days_order").toString()).intValue();
						}
						if (null != mso.get("thirty_days_order_base") && StringUtils.isNotEmpty(mso.get("thirty_days_order_base").toString())) {
							thirtyDaysOrderBase = Integer.valueOf(mso.get("thirty_days_order_base").toString()).intValue();
						}
						if (null != mso.get("thirty_days_order_ratio") && StringUtils.isNotEmpty(mso.get("thirty_days_order_ratio").toString())) {
							thirtyDaysOrderRatio = Double.valueOf(mso.get("thirty_days_order_ratio").toString()).doubleValue();
						}
						if (null != mso.get("thirty_days_order_ratio_base") && StringUtils.isNotEmpty(mso.get("thirty_days_order_ratio_base").toString())) {
							thirtyDaysOrderRatioBase = Double.valueOf(mso.get("thirty_days_order_ratio_base").toString()).doubleValue();
						}
						mso.put("thirty_days_order", thirtyDaysOrderBase + thirtyDaysOrder);
						mso.put("thirty_days_order_ratio", 0 != thirtyDaysOrderRatioBase ? thirtyDaysOrderRatioBase : thirtyDaysOrderRatio);
						
						if (null != mso.get("head_img") && StringUtils.isNotEmpty(mso.get("head_img").toString())) {
							String path = Constants.WEB_URL + "/public/showimg!showImg.action?imagePath=" + mso.get("head_img").toString();
							mso.put("head_img", path);
						}
						
						if (mso.get("pay_type") != null && StringUtils.isNotEmpty(mso.get("pay_type").toString())) {							
							List<String> pay_type = new LinkedList<String>();							
							String[] types = mso.get("pay_type").toString().split(",");
							for (String type : types) {								
								C2cPaymentMethodConfig method = this.c2cPaymentMethodConfigService.get(type.trim());
								if (null != method) {
									C2cTranslate trans = this.c2cTranslateService.get(method.getMethodName(), language);
									if (null != trans) {
										pay_type.add(trans.getTranslate());
									} else {
										pay_type.add(method.getMethodName());
									}
								}
							}							
							mso.put("pay_type_name", String.join(",", pay_type));
						}
					}
				}
				
				resultObject.setData(dataResult);
			}
			
		} catch (BusinessException e) {
			resultObject.setCode("1");
			resultObject.setMsg(e.getMessage());
		} catch (Throwable t) {
			resultObject.setCode("1");
			resultObject.setMsg("程序错误");
			logger.error("error:", t);
		}

		return resultObject;
	}
	
//	/**
//	 * 获取 承兑商广告 列表
//	 * 
//	 * direction 买卖方式：buy买/sell卖
//	 * currency 支付币种（大写CNY、USD、CAD等）
//	 * symbol 上架币种（大写USDT、BTC、ETH）
//	 * on_sale 是否上架：0下架/1上架
//	 */
//	@RequestMapping(action + "list_c2c_user.action")
//	public Object list_c2c_user(HttpServletRequest request) {
//		String page_no = request.getParameter("page_no");
//		String direction = request.getParameter("direction");
//		String currency = request.getParameter("currency");
//		String symbol = request.getParameter("symbol");
//		String on_sale = request.getParameter("on_sale");
//		String language = request.getParameter("language");
//		
//		ResultObject resultObject = new ResultObject();
//		resultObject = this.readSecurityContextFromSession(resultObject);
//		if (!"0".equals(resultObject.getCode())) {
//			return resultObject;
//		}
//		
//		try {
//
//			String error = this.verif_c2c_user(direction, currency, symbol, on_sale);
//			if (!StringUtils.isNullOrEmpty(error)) {
//				throw new BusinessException(error);
//			}
//			
//			if (StringUtils.isNullOrEmpty(page_no)) {
//				page_no = "1";
//			}
//			if (!StringUtils.isInteger(page_no)) {
//				throw new BusinessException("页码不是整数");
//			}
//			if (Integer.valueOf(page_no).intValue() <= 0) {
//				throw new BusinessException("页码不能小于等于0");
//			}
//
//			int page_no_int = Integer.valueOf(page_no).intValue();
//			
//			String partyId = this.getLoginPartyId();
//			if (null == partyId) {
//				throw new BusinessException("请重新登录");
//			}
//			
//			C2cUser c2cUser = this.c2cUserService.getByPartyId(partyId);
//			if (null == c2cUser) {
//				throw new BusinessException("承兑商不存在");
//			}
//			
//			Integer on_sale_int = null;
//			if (StringUtils.isNotEmpty(on_sale)) {
//				on_sale_int = Integer.valueOf(on_sale);
//			}
//
//			Page page = this.c2cAdvertService.pagedQuery(page_no_int, 20, c2cUser.getId().toString(), direction, currency, symbol, "", on_sale_int, 0, true);
//			if (null == page) {
//				resultObject.setData(new ArrayList<Map<String, Object>>());
//			} else {
//				List<Map<String, Object>> dataResult = (List<Map<String, Object>>) page.getElements();
//				
//				for (int i = 0; i < dataResult.size(); i++) {
//					Map<String, Object> mso = dataResult.get(i);
//					if (null != mso) {
//						
//						if (mso.get("pay_type") != null && StringUtils.isNotEmpty(mso.get("pay_type").toString())) {							
//							List<String> pay_type = new LinkedList<String>();							
//							String[] types = mso.get("pay_type").toString().split(",");
//							for (String type : types) {								
//								C2cPaymentMethodConfig method = this.c2cPaymentMethodConfigService.get(type.trim());
//								if (null != method) {
//									C2cTranslate trans = this.c2cTranslateService.get(method.getMethodName(), language);
//									if (null != trans) {
//										pay_type.add(trans.getTranslate());
//									} else {
//										pay_type.add(method.getMethodName());
//									}
//								}
//							}							
//							mso.put("pay_type_name", String.join(",", pay_type));
//						}
//					}
//				}
//				
//				resultObject.setData(dataResult);
//			}
//			
//		} catch (BusinessException e) {
//			resultObject.setCode("1");
//			resultObject.setMsg(e.getMessage());
//		} catch (Throwable t) {
//			resultObject.setCode("1");
//			resultObject.setMsg("程序错误");
//			logger.error("error:", t);
//		}
//
//		return resultObject;
//	}
	
//	/**
//	 * 获取 承兑商广告 历史列表
//	 * 
//	 * direction 买卖方式：buy买/sell卖
//	 */
//	@RequestMapping(action + "list_history.action")
//	public Object list_history(HttpServletRequest request) {
//		String page_no = request.getParameter("page_no");
//		String direction = request.getParameter("direction");
//		String language = request.getParameter("language");
//		
//		ResultObject resultObject = new ResultObject();
//		resultObject = this.readSecurityContextFromSession(resultObject);
//		if (!"0".equals(resultObject.getCode())) {
//			return resultObject;
//		}
//		
//		try {
//			
//			if (!StringUtils.isEmptyString(direction) && !Arrays.asList("buy", "sell").contains(direction)) {
//				throw new BusinessException("买卖方式不正确");
//			}
//			
//			if (StringUtils.isNullOrEmpty(page_no)) {
//				page_no = "1";
//			}
//			if (!StringUtils.isInteger(page_no)) {
//				throw new BusinessException("页码不是整数");
//			}
//			if (Integer.valueOf(page_no).intValue() <= 0) {
//				throw new BusinessException("页码不能小于等于0");
//			}
//
//			int page_no_int = Integer.valueOf(page_no).intValue();
//			
//			String partyId = this.getLoginPartyId();
//			if (null == partyId) {
//				throw new BusinessException("请重新登录");
//			}
//			
//			C2cUser c2cUser = this.c2cUserService.getByPartyId(partyId);
//			if (null == c2cUser) {
//				throw new BusinessException("承兑商不存在");
//			}
//			
//			Page page = this.c2cAdvertService.pagedQuery(page_no_int, 20, c2cUser.getId().toString(), direction, "", "", "", null, 1, true);
//			if (null == page) {
//				resultObject.setData(new ArrayList<Map<String, Object>>());
//			} else {
//				List<Map<String, Object>> dataResult = (List<Map<String, Object>>) page.getElements();
//				
//				for (int i = 0; i < dataResult.size(); i++) {
//					Map<String, Object> mso = dataResult.get(i);
//					if (null != mso) {
//						
//						if (mso.get("pay_type") != null && StringUtils.isNotEmpty(mso.get("pay_type").toString())) {							
//							List<String> pay_type = new LinkedList<String>();							
//							String[] types = mso.get("pay_type").toString().split(",");
//							for (String type : types) {								
//								C2cPaymentMethodConfig method = this.c2cPaymentMethodConfigService.get(type.trim());
//								if (null != method) {
//									C2cTranslate trans = this.c2cTranslateService.get(method.getMethodName(), language);
//									if (null != trans) {
//										pay_type.add(trans.getTranslate());
//									} else {
//										pay_type.add(method.getMethodName());
//									}						
//								}
//							}							
//							mso.put("pay_type_name", String.join(",", pay_type));
//						}
//					}
//				}
//				
//				resultObject.setData(dataResult);
//			}
//			
//		} catch (BusinessException e) {
//			resultObject.setCode("1");
//			resultObject.setMsg(e.getMessage());
//		} catch (Throwable t) {
//			resultObject.setCode("1");
//			resultObject.setMsg("程序错误");
//			logger.error("error:", t);
//		}
//
//		return resultObject;
//	}
	
	/**
	 * 获取 广告 详情
	 * 
	 * id C2C广告ID
	 */
	@RequestMapping(action + "get.action")
	public Object get(HttpServletRequest request) {
		String id = request.getParameter("id");
		String language = request.getParameter("language");
		
		ResultObject resultObject = new ResultObject();
		resultObject = this.readSecurityContextFromSession(resultObject);
		if (!"0".equals(resultObject.getCode())) {
			return resultObject;
		}
		
		try {
			
			if (StringUtils.isEmptyString(id)) {
				throw new BusinessException("C2C广告id不正确");
			}
			
			C2cAdvert c2cAdvert = this.c2cAdvertService.get(id);
			if (null == c2cAdvert) {
				throw new BusinessException("广告不存在");
			}
			
			C2cUser c2cUser = this.c2cUserService.get(c2cAdvert.getC2cUserId());
			if (null == c2cUser) {
				throw new BusinessException("承兑商不存在");
			}
			
			Party c2cParty = this.partyService.cachePartyBy(c2cUser.getC2cUserPartyId(), false);
			if (null == c2cParty) {
				throw new BusinessException("承兑商的用户信息不存在");				
			}
			
			Map<String, String> data = new HashMap<String, String>();
			data.put("id", c2cAdvert.getId().toString());
			data.put("direction", c2cAdvert.getDirection());
			data.put("currency", c2cAdvert.getCurrency());
			data.put("symbol", c2cAdvert.getSymbol());
			data.put("pay_rate", String.valueOf(c2cAdvert.getPayRate()));
			data.put("pay_type", c2cAdvert.getPayType());
			data.put("symbol_value", String.valueOf(c2cAdvert.getSymbolValue()));
			data.put("coin_amount", String.valueOf(c2cAdvert.getCoinAmount()));
			data.put("investment_min", String.valueOf(c2cAdvert.getInvestmentMin()));
			data.put("investment_max", String.valueOf(c2cAdvert.getInvestmentMax()));
			data.put("deposit", String.valueOf(c2cAdvert.getDeposit()));
			data.put("deposit_open", String.valueOf(c2cAdvert.getDepositOpen()));
			data.put("on_sale", String.valueOf(c2cAdvert.getOnSale()));
			data.put("sort_index", String.valueOf(c2cAdvert.getSortIndex()));
			data.put("expire_time", String.valueOf(c2cAdvert.getExpireTime()));
			data.put("transaction_terms", c2cAdvert.getTransactionTerms());
			data.put("order_msg", c2cAdvert.getOrderMsg());
			data.put("remark", c2cAdvert.getRemark());
			data.put("create_time", DateUtils.format(c2cAdvert.getCreateTime(), DateUtils.DF_yyyyMMddHHmmss));
			data.put("update_time", DateUtils.format(c2cAdvert.getUpdateTime(), DateUtils.DF_yyyyMMddHHmmss));
			data.put("c2c_user_id", c2cUser.getId().toString());
			data.put("nick_name", c2cUser.getNickName());
			data.put("c2c_user_type", String.valueOf(c2cUser.getC2cUserType()));
			data.put("c2c_user_code", c2cUser.getC2cUserCode());
			data.put("party_id", c2cParty.getId().toString());
			data.put("user_code", c2cParty.getUsercode());
			data.put("user_name", c2cParty.getUsername());	

			if (StringUtils.isNotEmpty(c2cUser.getHeadImg())) {
				String path = Constants.WEB_URL + "/public/showimg!showImg.action?imagePath=" + c2cUser.getHeadImg();
				data.put("head_img", path);
			} else {
				data.put("head_img", "");
			}

			if (data.get("pay_type") != null && StringUtils.isNotEmpty(data.get("pay_type").toString())) {							
				List<String> pay_type = new LinkedList<String>();							
				String[] types = data.get("pay_type").toString().split(",");
				for (String type : types) {								
					C2cPaymentMethodConfig method = this.c2cPaymentMethodConfigService.get(type.trim());
					if (null != method) {
						C2cTranslate trans = this.c2cTranslateService.get(method.getMethodName(), language);
						if (null != trans) {
							pay_type.add(trans.getTranslate());
						} else {
							pay_type.add(method.getMethodName());
						}						
					}
				}							
				data.put("pay_type_name", String.join(",", pay_type));
			}
			
			resultObject.setData(data);
			
		} catch (BusinessException e) {
			resultObject.setCode("1");
			resultObject.setMsg(e.getMessage());
		} catch (Throwable t) {
			resultObject.setCode("1");
			resultObject.setMsg("程序错误");
			logger.error("error:", t);
		}

		return resultObject;
	}
	
	private String verif_add(String coin_amount, String direction, String pay_type, String currency, 
			String symbol, String symbol_value, String investment_min, String investment_max, String on_sale, String expire_time, String safeword) {
		if (StringUtils.isEmptyString(coin_amount) || !StringUtils.isDouble(coin_amount) || Double.valueOf(coin_amount).doubleValue() <= 0) {
			return "交易数量不正确";
		}		
		if (StringUtils.isEmptyString(direction) || !Arrays.asList("buy", "sell").contains(direction)) {
			return "买卖方式不正确";
		}		
		if (StringUtils.isEmptyString(pay_type)) {
			return "支付方式不正确";
		}		
		Map<String, String> currencyMap = this.c2cAdvertService.getCurrencyMap();
		Map<String, String> symbolMap = this.c2cAdvertService.getSymbolMap();
		if (StringUtils.isEmptyString(currency) || null == currencyMap || (null != currencyMap && !currencyMap.containsKey(currency))) {
			return "支付币种不正确";
		}		
		if (StringUtils.isEmptyString(symbol) || null == symbolMap || (null != symbolMap && !symbolMap.containsKey(symbol))) {
			return "上架币种不正确";
		}		
		if (StringUtils.isEmptyString(symbol_value) || !StringUtils.isDouble(symbol_value) || Double.valueOf(symbol_value).doubleValue() <= 0) {
			return "币种单价不正确";
		}
		if (StringUtils.isEmptyString(investment_min) || !StringUtils.isDouble(investment_min) || Double.valueOf(investment_min).doubleValue() <= 0) {
			return "单笔订单最低限额未填或格式不正确";
		}		
		if (StringUtils.isEmptyString(investment_max) || !StringUtils.isDouble(investment_max) || Double.valueOf(investment_max).doubleValue() <= 0) {
			return "单笔订单最高限额未填或格式不正确";
		}
		if (StringUtils.isEmptyString(on_sale) || !Arrays.asList("0", "1").contains(on_sale)) {
			return "是否上架未填或格式不正确";
		}
		Map<String, String> aetMap = this.c2cAdvertService.getC2cSyspara("c2c_advert_expire_time");
		if (StringUtils.isEmptyString(expire_time) || !aetMap.keySet().contains(expire_time)) {
			return "支付时效未填或格式不正确";
		}		
		if (StringUtils.isEmptyString(safeword)) {
			return "资金密码错误";
		}		
		return null;
	}
	
	private String verif(String direction, String currency, String symbol, String method_type, String amount) {
		
		if (!StringUtils.isEmptyString(direction) && !Arrays.asList("buy", "sell").contains(direction)) {
			return "买卖方式不正确";
		}
		
		Map<String, String> currencyMap = this.c2cAdvertService.getCurrencyMap();
		Map<String, String> symbolMap = this.c2cAdvertService.getSymbolMap();
		if (!StringUtils.isEmptyString(currency) && null != currencyMap && !currencyMap.containsKey(currency)) {
			return "支付币种不正确";
		}		
		if (!StringUtils.isEmptyString(symbol) && null != symbolMap && !symbolMap.containsKey(symbol)) {
			return "上架币种不正确";
		}
		
		Map<String, String> pmtMap = this.c2cAdvertService.getC2cSyspara("c2c_payment_method_type");
		if (!StringUtils.isEmptyString(method_type) && !pmtMap.keySet().contains(method_type)) {
			return "支付方式类型不正确";
		}
		
		if (!StringUtils.isEmptyString(amount) && (!StringUtils.isDouble(amount) || Double.valueOf(amount).doubleValue() < 0)) {
			return "支付金额不正确";
		}
		
		return null;
	}
	
	private String verif_c2c_user(String direction, String currency, String symbol, String on_sale) {
		
		if (!StringUtils.isEmptyString(direction) && !Arrays.asList("buy", "sell").contains(direction)) {
			return "买卖方式不正确";
		}
		
		Map<String, String> currencyMap = this.c2cAdvertService.getCurrencyMap();
		Map<String, String> symbolMap = this.c2cAdvertService.getSymbolMap();
		if (!StringUtils.isEmptyString(currency) && null != currencyMap && !currencyMap.containsKey(currency)) {
			return "支付币种不正确";
		}		
		if (!StringUtils.isEmptyString(symbol) && null != symbolMap && !symbolMap.containsKey(symbol)) {
			return "上架币种不正确";
		}

		if (!StringUtils.isEmptyString(on_sale) && !Arrays.asList("0", "1").contains(on_sale)) {
			return "是否上架不正确";
		}
		
		return null;
	}
	
	public void saveLog(SecUser secUser, String operator, String context, String category) {
		project.log.Log log = new project.log.Log();
		log.setCategory(category);
		log.setOperator(operator);
		log.setUsername(secUser.getUsername());
		log.setPartyId(secUser.getPartyId());
		log.setLog(context);
		log.setCreateTime(new Date());
		this.logService.saveSync(log);
	}
	
}
