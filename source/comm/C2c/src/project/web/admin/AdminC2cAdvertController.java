package project.web.admin;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.providers.encoding.PasswordEncoder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import kernel.exception.BusinessException;
import kernel.util.Arith;
import kernel.util.JsonUtils;
import kernel.util.StringUtils;
import kernel.web.ApplicationUtil;
import kernel.web.Page;
import kernel.web.PageActionSupport;
import project.Constants;
import project.c2c.AdminC2cAdvertService;
import project.c2c.C2cAdvert;
import project.c2c.C2cAdvertService;
import project.c2c.C2cAppealService;
import project.c2c.C2cOrderService;
import project.c2c.C2cPaymentMethod;
import project.c2c.C2cPaymentMethodConfig;
import project.c2c.C2cPaymentMethodConfigService;
import project.c2c.C2cPaymentMethodService;
import project.c2c.C2cUser;
import project.c2c.C2cUserService;
import project.log.LogService;
import project.syspara.SysparaService;
import security.Role;
import security.SecUser;
import security.internal.SecUserService;

/**
 * C2C广告管理
 */
@RestController
public class AdminC2cAdvertController extends PageActionSupport {

	private Logger logger = LoggerFactory.getLogger(AdminC2cAdvertController.class);

	@Autowired
	private AdminC2cAdvertService adminC2cAdvertService;
	@Autowired
	private C2cAdvertService c2cAdvertService;
	@Autowired
	private C2cPaymentMethodService c2cPaymentMethodService;
	@Autowired
	private C2cPaymentMethodConfigService c2cPaymentMethodConfigService;
	@Autowired
	private SecUserService secUserService;
	@Autowired
	private PasswordEncoder passwordEncoder;
	@Autowired
	private LogService logService;
	@Autowired
	private C2cUserService c2cUserService;
	@Autowired
	private C2cOrderService c2cOrderService;
	@Autowired
	private C2cAppealService c2cAppealService;
	@Autowired
	private SysparaService sysparaService;
	
	private final String action = "normal/adminC2cAdvertAction!";
	
	/**
	 * 获取 C2C广告 列表
	 */
	@RequestMapping(action + "list.action")
	public ModelAndView list(HttpServletRequest request) {
		String pageNoStr = request.getParameter("pageNo");
		String message = request.getParameter("message");
		String error = request.getParameter("error");
		String c2c_user_code_para = request.getParameter("c2c_user_code_para");
		String c2c_user_type_para = request.getParameter("c2c_user_type_para");
		String user_code_para = request.getParameter("user_code_para");
		String direction_para = request.getParameter("direction_para");
		String currency_para = request.getParameter("currency_para");
		String symbol_para = request.getParameter("symbol_para");	
		
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("c2c_advert_list");

		int pageNo=1;
		Page page = null;
		int pageSize = 20;
		try {
			pageNo=checkAndSetPageNo(pageNoStr);
			Map<String, String> currencyMap = this.c2cAdvertService.getCurrencyMap();
			Map<String, String> symbolMap = this.c2cAdvertService.getSymbolMap();
			
			Integer c2c_user_type_int = null;
			if (!StringUtils.isEmptyString(c2c_user_type_para)) {
				c2c_user_type_int = Integer.valueOf(c2c_user_type_para);
			}
			
			String secUuid = "";
			String userNameLogin = this.getUsername_login();
			if (null != userNameLogin) {
				SecUser sec = this.secUserService.findUserByLoginName(userNameLogin);
				Set<Role> roles = sec.getRoles();
				Iterator<Role> it = roles.iterator();
				while (it.hasNext()) {
					Role role = (Role) it.next();
					if (role.getRoleName().equals("C2C")) {
						secUuid = sec.getId().toString();
						break;
					}
				}
			}
			
			page = this.adminC2cAdvertService.pagedQuery(pageNo, pageSize, c2c_user_code_para, c2c_user_type_int, user_code_para, 
					direction_para, currency_para, symbol_para, secUuid);
						
			for (Map<String, Object> data : (List<Map<String, Object>>) page.getElements()) {
				if (data.get("pay_type") != null && StringUtils.isNotEmpty(data.get("pay_type").toString())) {
					List<String> pay_type = new LinkedList<String>();
					String[] types = data.get("pay_type").toString().split(",");
					for (String type : types) {
						C2cPaymentMethodConfig method = this.c2cPaymentMethodConfigService.get(type.trim());
						if (null != method) {
							pay_type.add(method.getMethodName());							
						}
					}
					data.put("pay_type", String.join("<br />/", pay_type));
				}				
			}

			modelAndView.addObject("currencyMap", currencyMap);
			modelAndView.addObject("symbolMap", symbolMap);

		} catch (BusinessException e) {
			modelAndView.addObject("error", e.getMessage());
			return modelAndView;
		} catch (Throwable t) {
			logger.error(" error ", t);
			modelAndView.addObject("error", "[ERROR] " + t.getMessage());
			return modelAndView;
		}

		modelAndView.addObject("pageNo", pageNo);
		modelAndView.addObject("pageSize", pageSize);
		modelAndView.addObject("page", page);
		modelAndView.addObject("message", message);
		modelAndView.addObject("error", error);		
		modelAndView.addObject("c2c_user_code_para", c2c_user_code_para);
		modelAndView.addObject("c2c_user_type_para",c2c_user_type_para);
		modelAndView.addObject("user_code_para",user_code_para);
		modelAndView.addObject("direction_para", direction_para);
		modelAndView.addObject("currency_para", currency_para);
		modelAndView.addObject("symbol_para", symbol_para);
		return modelAndView;
	}
	
	/**
	 * 新增 C2C广告 页面
	 */
	@RequestMapping(action + "toAdd.action")
	public ModelAndView toAdd(HttpServletRequest request) {
		ModelAndView modelAndView = new ModelAndView();
		try {
			Map<String, String> currencyMap = this.c2cAdvertService.getCurrencyMap();
			Map<String, String> symbolMap = this.c2cAdvertService.getSymbolMap();

			modelAndView.addObject("currencyMap", currencyMap);
			modelAndView.addObject("symbolMap", symbolMap);
			modelAndView.addObject("expireTimeMap", this.c2cAdvertService.getC2cSyspara("c2c_advert_expire_time"));
			
			modelAndView.addObject("c2c_user_code", "");
			modelAndView.addObject("all_deposit", 0);
			modelAndView.addObject("deposit_open", 0);
			modelAndView.addObject("deposit_total", 0);
			modelAndView.addObject("symbol_close", 0);
			modelAndView.addObject("coin_amount", 0);
			modelAndView.addObject("coin_amount_max", 0);
			modelAndView.addObject("symbol_value", 0);
			modelAndView.addObject("pay_rate", 100);
			modelAndView.addObject("investment_min", 0);
			modelAndView.addObject("investment_max", 0);
			modelAndView.addObject("investment_min_limit", 0);
			modelAndView.addObject("investment_max_limit", 0);
			modelAndView.addObject("price", 0);
			
		} catch (BusinessException e) {
			modelAndView.addObject("error", e.getMessage());
			modelAndView.setViewName("redirect:/" + action + "list.action");
			return modelAndView;
		} catch (Throwable t) {
			logger.error(" error ", t);
			modelAndView.addObject("error", "[ERROR] " + t.getMessage());
			modelAndView.setViewName("redirect:/" + action + "list.action");
			return modelAndView;
		}

		modelAndView.setViewName("c2c_advert_add");
		return modelAndView;
	}
	
	/**
	 * 新增 C2C广告
	 */
	@RequestMapping(action + "add.action")
	public ModelAndView add(HttpServletRequest request) {
		String c2c_user_code = request.getParameter("c2c_user_code");
		String direction = request.getParameter("direction");
		String payment_method1 = request.getParameter("payment_method1");
		String payment_method2 = request.getParameter("payment_method2");
		String payment_method3 = request.getParameter("payment_method3");	
		String currency = request.getParameter("currency");
		String symbol = request.getParameter("symbol");		
		String coin_amount = request.getParameter("coin_amount");
		String symbol_value = request.getParameter("symbol_value");
		String investment_min = request.getParameter("investment_min");	
		String investment_max = request.getParameter("investment_max");	
		String on_sale = request.getParameter("on_sale");
		String sort_index = request.getParameter("sort_index");
		String expire_time = request.getParameter("expire_time");
		String transaction_terms = request.getParameter("transaction_terms");
		String order_msg = request.getParameter("order_msg");
		String remark = request.getParameter("remark");	

		String all_deposit = request.getParameter("all_deposit");
		String deposit_open = request.getParameter("deposit_open");
		String deposit_total = request.getParameter("deposit_total");
		String symbol_close = request.getParameter("symbol_close");
		String coin_amount_max = request.getParameter("coin_amount_max");
		String pay_rate = request.getParameter("pay_rate");
		String investment_min_limit = request.getParameter("investment_min_limit");
		String investment_max_limit = request.getParameter("investment_max_limit");
		String price = request.getParameter("price");
		
		String login_safeword = request.getParameter("login_safeword");		
				
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.addObject("c2c_user_code", c2c_user_code);
		modelAndView.addObject("direction", direction);
		modelAndView.addObject("payment_method1", payment_method1);
		modelAndView.addObject("payment_method2", payment_method2);
		modelAndView.addObject("payment_method3", payment_method3);
		modelAndView.addObject("currency", currency);
		modelAndView.addObject("symbol", symbol);
		modelAndView.addObject("coin_amount", coin_amount);
		modelAndView.addObject("symbol_value", symbol_value);
		modelAndView.addObject("investment_min", investment_min);
		modelAndView.addObject("investment_max", investment_max);
		modelAndView.addObject("on_sale", on_sale);
		modelAndView.addObject("sort_index", sort_index);
		modelAndView.addObject("expire_time", expire_time);
		modelAndView.addObject("transaction_terms", transaction_terms);
		modelAndView.addObject("order_msg", order_msg);
		modelAndView.addObject("remark", remark);		

		modelAndView.addObject("all_deposit", all_deposit);
		modelAndView.addObject("deposit_open", deposit_open);
		modelAndView.addObject("deposit_total", deposit_total);
		modelAndView.addObject("symbol_close", symbol_close);
		modelAndView.addObject("coin_amount_max", coin_amount_max);
		modelAndView.addObject("pay_rate", pay_rate);
		modelAndView.addObject("investment_min_limit", investment_min_limit);
		modelAndView.addObject("investment_max_limit", investment_max_limit);
		modelAndView.addObject("price", price);
		
		try {
			
			Map<String, String> currencyMap = this.c2cAdvertService.getCurrencyMap();
			Map<String, String> symbolMap = this.c2cAdvertService.getSymbolMap();
			
			modelAndView.addObject("currencyMap", currencyMap);
			modelAndView.addObject("symbolMap", symbolMap);
			modelAndView.addObject("expireTimeMap", this.c2cAdvertService.getC2cSyspara("c2c_advert_expire_time"));
						
			// 支付方式拼接
			String pay_type = "";
			if (StringUtils.isNotEmpty(payment_method1)) {
				if ("".equals(pay_type)) {
					pay_type += payment_method1;
				} else {
					pay_type = pay_type + "," + payment_method1;
				}
			}
			if (StringUtils.isNotEmpty(payment_method2) && !pay_type.contains(payment_method2)) {
				if ("".equals(pay_type)) {
					pay_type += payment_method2;
				} else {
					pay_type = pay_type + "," + payment_method2;
				}
			}
			if (StringUtils.isNotEmpty(payment_method3) && !pay_type.contains(payment_method3)) {
				if ("".equals(pay_type)) {
					pay_type += payment_method3;
				} else {
					pay_type = pay_type + "," + payment_method3;
				}
			}

			String error = this.verif(c2c_user_code, direction, pay_type, currency, symbol, coin_amount, symbol_value, 
					investment_min, investment_max, on_sale, sort_index, expire_time, login_safeword);
			if (!StringUtils.isNullOrEmpty(error)) {
				throw new BusinessException(error);
			}
			
			if (StringUtils.isEmptyString(coin_amount) || !StringUtils.isDouble(coin_amount) || Double.valueOf(coin_amount).doubleValue() <= 0) {
				throw new BusinessException("交易币种数量未填或格式不正确");
			}
			
			if (StringUtils.isEmptyString(symbol_value) || !StringUtils.isDouble(symbol_value) || Double.valueOf(symbol_value).doubleValue() <= 0) {
				throw new BusinessException("币种单价未填或格式不正确");
			}
			
			DecimalFormat df = new DecimalFormat("#.########");
			
			double coin_amount_double = Double.valueOf(df.format(Double.valueOf(coin_amount))).doubleValue();
			double symbol_value_double = Double.valueOf(df.format(Double.valueOf(symbol_value))).doubleValue();
			double investment_min_double = Double.valueOf(df.format(Double.valueOf(investment_min))).doubleValue();
			double investment_max_double = Double.valueOf(df.format(Double.valueOf(investment_max))).doubleValue();
			
			C2cUser c2cUser = this.c2cUserService.findByUsercode(c2c_user_code);
			if (null == c2cUser) {
				throw new BusinessException("承兑商不存在");
			}
			
			// C2C承兑商添加广告最大数量
			List<C2cAdvert> advertList = this.c2cAdvertService.getByC2cUserId(c2cUser.getId().toString());
			Object obj = this.sysparaService.find("c2c_advert_count_max");
			if (null != obj) {
				if (advertList.size() >= Integer.valueOf(this.sysparaService.find("c2c_advert_count_max").getValue()).intValue()) {
					throw new BusinessException("广告数量已达上限");
				}
			}
			
			String userNameLogin = this.getUsername_login();
			
			// 验证资金密码
			SecUser sec = this.secUserService.findUserByLoginName(userNameLogin);
			this.checkLoginSafeword(sec, userNameLogin, login_safeword);
			
			// 计算广告参数
			Map<String, Object> computeValue = this.c2cAdvertService.getComputeValue(c2cUser.getDeposit(), currency, symbol, coin_amount_double, symbol_value_double);
			
			if (coin_amount_double > Double.valueOf(computeValue.get("coin_amount_max").toString()).doubleValue()) {
				throw new BusinessException("交易币种数量不能大于最大可交易数量");
			}
			
			if (investment_min_double <= 0 || investment_min_double < Double.valueOf(computeValue.get("investment_min_limit").toString()).doubleValue()) {
				throw new BusinessException("单笔订单支付金额下限错误");
			}
			
			if (investment_max_double > Double.valueOf(computeValue.get("investment_max_limit").toString()).doubleValue()) {
				throw new BusinessException("单笔订单支付金额上限错误");
			}
			
				
			double pay_rate_double = Double.valueOf(computeValue.get("pay_rate").toString()).doubleValue();
			double symbol_close_double = Double.valueOf(computeValue.get("symbol_close").toString()).doubleValue();
			
			double deposit_open_double = Double.valueOf(computeValue.get("deposit_open").toString()).doubleValue();
							
			if (deposit_open_double < 0) {
				throw new BusinessException("广告保证金不能小于0");
			}
			
			C2cAdvert c2cAdvert = new C2cAdvert();
			c2cAdvert.setId(ApplicationUtil.getCurrentTimeUUID());
			c2cAdvert.setC2cUserId(c2cUser.getId().toString());
			c2cAdvert.setDirection(direction);
			c2cAdvert.setCurrency(currency);
			c2cAdvert.setSymbol(symbol);
			c2cAdvert.setSymbolClose(symbol_close_double);
			c2cAdvert.setPayRate((int) pay_rate_double);
			c2cAdvert.setPayType(pay_type);
			c2cAdvert.setSymbolValue(symbol_value_double);
			c2cAdvert.setCoinAmount(coin_amount_double);
			c2cAdvert.setInvestmentMin(investment_min_double);
			c2cAdvert.setInvestmentMax(investment_max_double);
			c2cAdvert.setDeposit(deposit_open_double);
			c2cAdvert.setDepositOpen(deposit_open_double);
			c2cAdvert.setOnSale(Integer.valueOf(on_sale).intValue());
			c2cAdvert.setClosed(0);
			c2cAdvert.setSortIndex(StringUtils.isNotEmpty(sort_index) ? Integer.valueOf(sort_index).intValue() : 0);
			c2cAdvert.setExpireTime(Integer.valueOf(expire_time).intValue());
			c2cAdvert.setTransactionTerms(transaction_terms);
			c2cAdvert.setOrderMsg(order_msg);
			c2cAdvert.setRemark(remark);
			c2cAdvert.setCreateTime(new Date());
			c2cAdvert.setUpdateTime(new Date());
			
			this.c2cAdvertService.save(c2cAdvert);
			
			double oldC2cUserDeposit = c2cUser.getDeposit();
			
			c2cUser.setDeposit(Arith.sub(c2cUser.getDeposit(), deposit_open_double));
			
			this.c2cUserService.update(c2cUser);
			
			String log = MessageFormat.format("ip:" + this.getIp() 
				+ ",管理员新增承兑商广告,id:{0},承兑商ID:{1},买卖方式:{2},支付币种:{3},上架币种:{4},上架币种实时行情价:{5},支付比率:{6},支付方式:{7},币种单价:{8},"
				+ "币种数量:{9},单笔订单最低限额:{10},单笔订单最高限额:{11},剩余派单金额:{12},派单金额:{13},是否上架:{14},是否关闭:{15},"
				+ "排序索引:{16},支付时效:{17},交易条款:{18},订单自动消息:{19},备注:{20},创建时间:{21},更新时间:{22}#####原承兑商剩余派单金额:{23},新承兑商剩余派单金额:{24}", 
				c2cAdvert.getId(), c2cAdvert.getC2cUserId(), c2cAdvert.getDirection(), c2cAdvert.getCurrency(), c2cAdvert.getSymbol(), c2cAdvert.getSymbolClose(), c2cAdvert.getPayRate(), c2cAdvert.getPayType(), c2cAdvert.getSymbolValue(), 
				c2cAdvert.getCoinAmount(), c2cAdvert.getInvestmentMin(), c2cAdvert.getInvestmentMax(), c2cAdvert.getDeposit(), c2cAdvert.getDepositOpen(), c2cAdvert.getOnSale(), c2cAdvert.getClosed(), 
				c2cAdvert.getSortIndex(), c2cAdvert.getExpireTime(), c2cAdvert.getTransactionTerms(), c2cAdvert.getOrderMsg(), c2cAdvert.getRemark(), c2cAdvert.getCreateTime(), c2cAdvert.getUpdateTime(), 
				oldC2cUserDeposit, c2cUser.getDeposit());
			
			this.saveLog(sec, userNameLogin, log, Constants.LOG_CATEGORY_C2C);
				

		} catch (BusinessException e) {	
			modelAndView.addObject("error", e.getMessage());
			modelAndView.setViewName("c2c_advert_add");
			return modelAndView;			
		} catch (Throwable t) {
			logger.error(" error ", t);
			modelAndView.addObject("error", "[ERROR] " + t.getMessage());
			modelAndView.setViewName("c2c_advert_add");
			return modelAndView;			
		}
		
		modelAndView.addObject("message", "操作成功");
		modelAndView.setViewName("redirect:/" + action + "list.action");
		return modelAndView;
	}
	
	/**
	 * 修改 C2C广告 页面
	 */
	@RequestMapping(action + "toUpdate.action")
	public ModelAndView toUpdate(HttpServletRequest request) {
		String id = request.getParameter("id");
		
		ModelAndView modelAndView = new ModelAndView();
		
		try {
			
			Map<String, String> currencyMap = this.c2cAdvertService.getCurrencyMap();
			Map<String, String> symbolMap = this.c2cAdvertService.getSymbolMap();			
			
			C2cAdvert c2cAdvert = this.c2cAdvertService.get(id);
			if (null == c2cAdvert) {
				throw new BusinessException("广告不存在");
			}
			
			C2cUser c2cUser = this.c2cUserService.get(c2cAdvert.getC2cUserId());
			if (null == c2cUser) {
				throw new BusinessException("承兑商不存在");
			}
			
			modelAndView.addObject("id", id);
			modelAndView.addObject("currencyMap", currencyMap);
			modelAndView.addObject("symbolMap", symbolMap);
			modelAndView.addObject("expireTimeMap", this.c2cAdvertService.getC2cSyspara("c2c_advert_expire_time"));
			
			if (StringUtils.isNotEmpty(c2cAdvert.getPayType())) {				
				String[] types = c2cAdvert.getPayType().split(",");
				modelAndView.addObject("payment_method1", types.length >= 1 ? types[0] : "");
				modelAndView.addObject("payment_method2", types.length >= 2 ? types[1] : "");
				modelAndView.addObject("payment_method3", types.length >= 3 ? types[2] : "");
			} else {
				modelAndView.addObject("payment_method1", "");
				modelAndView.addObject("payment_method2", "");
				modelAndView.addObject("payment_method3", "");
			}			
			modelAndView.addObject("c2c_user_code", c2cUser.getC2cUserCode());
//			modelAndView.addObject("deposit_open", c2cAdvert.getDepositOpen());
			modelAndView.addObject("deposit",new BigDecimal(c2cAdvert.getDeposit()).toPlainString());
//			modelAndView.addObject("symbol_close", c2cAdvert.getSymbolClose());
			modelAndView.addObject("direction", c2cAdvert.getDirection());
			modelAndView.addObject("coin_amount",new BigDecimal(c2cAdvert.getCoinAmount()).toPlainString());
			modelAndView.addObject("symbol_value", c2cAdvert.getSymbolValue());
			modelAndView.addObject("currency", c2cAdvert.getCurrency());
			modelAndView.addObject("symbol", c2cAdvert.getSymbol());
//			modelAndView.addObject("pay_rate", c2cAdvert.getPayRate());
			modelAndView.addObject("investment_min",new BigDecimal(c2cAdvert.getInvestmentMin()).toPlainString());
			modelAndView.addObject("investment_max",new BigDecimal(c2cAdvert.getInvestmentMax()).toPlainString());
			modelAndView.addObject("on_sale", c2cAdvert.getOnSale());
//			modelAndView.addObject("closed", c2cAdvert.getClosed());
			modelAndView.addObject("sort_index", c2cAdvert.getSortIndex());
			modelAndView.addObject("expire_time", c2cAdvert.getExpireTime());
			modelAndView.addObject("transaction_terms", c2cAdvert.getTransactionTerms());
			modelAndView.addObject("order_msg", c2cAdvert.getOrderMsg());
			modelAndView.addObject("remark", c2cAdvert.getRemark());
			
			// 计算广告参数
			Map<String, Object> computeValue = this.c2cAdvertService.getComputeValue(Arith.add(c2cUser.getDeposit(), c2cAdvert.getDeposit()), c2cAdvert.getCurrency(), c2cAdvert.getSymbol(), 
					c2cAdvert.getCoinAmount(), c2cAdvert.getSymbolValue());
			
			modelAndView.addObject("all_deposit", c2cUser.getDeposit());
			modelAndView.addObject("pay_rate", computeValue.get("pay_rate"));
			modelAndView.addObject("symbol_close", computeValue.get("symbol_close"));
			modelAndView.addObject("price", computeValue.get("price"));
			modelAndView.addObject("coin_amount_max", computeValue.get("coin_amount_max"));
			modelAndView.addObject("investment_min_limit", computeValue.get("investment_min_limit"));
			modelAndView.addObject("investment_max_limit", computeValue.get("investment_max_limit"));
						
//			modelAndView.addObject("deposit_open_old", c2cAdvert.getDepositOpen());
			modelAndView.addObject("deposit_old", c2cAdvert.getDeposit());
						
			DecimalFormat df = new DecimalFormat("#.########");
			
			modelAndView.addObject("deposit_total", df.format(Arith.add(c2cUser.getDeposit(), c2cAdvert.getDeposit())));

		} catch (BusinessException e) {
			modelAndView.addObject("error", e.getMessage());
			modelAndView.setViewName("redirect:/" + action + "list.action");
			return modelAndView;
		} catch (Throwable t) {
			logger.error(" error ", t);
			modelAndView.addObject("error", "[ERROR] " + t.getMessage());
			modelAndView.setViewName("redirect:/" + action + "list.action");
			return modelAndView;
		}
		
		modelAndView.setViewName("c2c_advert_update");
		return modelAndView;
	}
	
	/**
	 * 修改 C2C广告
	 */
	@RequestMapping(action + "update.action")
	public ModelAndView update(HttpServletRequest request) {
		String id = request.getParameter("id");
		String payment_method1 = request.getParameter("payment_method1");
		String payment_method2 = request.getParameter("payment_method2");
		String payment_method3 = request.getParameter("payment_method3");
		String c2c_user_code = request.getParameter("c2c_user_code");
//		String deposit_open = request.getParameter("deposit_open");
		String deposit = request.getParameter("deposit");
		String direction = request.getParameter("direction");	
		String coin_amount = request.getParameter("coin_amount");		
		String symbol_value = request.getParameter("symbol_value");
		String currency = request.getParameter("currency");
		String symbol = request.getParameter("symbol");	
		String investment_min = request.getParameter("investment_min");	
		String investment_max = request.getParameter("investment_max");	
		String on_sale = request.getParameter("on_sale");
//		String closed = request.getParameter("closed");
		String sort_index = request.getParameter("sort_index");
		String expire_time = request.getParameter("expire_time");
		String transaction_terms = request.getParameter("transaction_terms");
		String order_msg = request.getParameter("order_msg");
		String remark = request.getParameter("remark");

		String all_deposit = request.getParameter("all_deposit");	
		String pay_rate = request.getParameter("pay_rate");	
		String symbol_close = request.getParameter("symbol_close");	
		String price = request.getParameter("price");
		String coin_amount_max = request.getParameter("coin_amount_max");	
		String investment_min_limit = request.getParameter("investment_min_limit");	
		String investment_max_limit = request.getParameter("investment_max_limit");

//		String deposit_open_old = request.getParameter("deposit_open_old");
		String deposit_old = request.getParameter("deposit_old");
		String deposit_total = request.getParameter("deposit_total");
		
		String login_safeword = request.getParameter("login_safeword");	
				
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.addObject("id", id);
		modelAndView.addObject("payment_method1", payment_method1);
		modelAndView.addObject("payment_method2", payment_method2);
		modelAndView.addObject("payment_method3", payment_method3);
		modelAndView.addObject("c2c_user_code", c2c_user_code);
//		modelAndView.addObject("deposit_open", deposit_open);
		modelAndView.addObject("deposit", deposit);
		modelAndView.addObject("direction", direction);
		modelAndView.addObject("coin_amount", coin_amount);
		modelAndView.addObject("symbol_value", symbol_value);
		modelAndView.addObject("currency", currency);
		modelAndView.addObject("symbol", symbol);
		modelAndView.addObject("investment_min", investment_min);
		modelAndView.addObject("investment_max", investment_max);
		modelAndView.addObject("on_sale", on_sale);
//		modelAndView.addObject("closed", closed);
		modelAndView.addObject("sort_index", sort_index);
		modelAndView.addObject("expire_time", expire_time);
		modelAndView.addObject("transaction_terms", transaction_terms);
		modelAndView.addObject("order_msg", order_msg);
		modelAndView.addObject("remark", remark);

		modelAndView.addObject("all_deposit", all_deposit);
		modelAndView.addObject("pay_rate", pay_rate);
		modelAndView.addObject("symbol_close", symbol_close);
		modelAndView.addObject("price", price);
		modelAndView.addObject("coin_amount_max", coin_amount_max);
		modelAndView.addObject("investment_min_limit", investment_min_limit);
		modelAndView.addObject("investment_max_limit", investment_max_limit);

//		modelAndView.addObject("deposit_open_old", deposit_open_old);
		modelAndView.addObject("deposit_old", deposit_old);
		modelAndView.addObject("deposit_total", deposit_total);
		
		try {
			
			Map<String, String> currencyMap = this.c2cAdvertService.getCurrencyMap();
			Map<String, String> symbolMap = this.c2cAdvertService.getSymbolMap();
			
			modelAndView.addObject("currencyMap", currencyMap);
			modelAndView.addObject("symbolMap", symbolMap);
			modelAndView.addObject("expireTimeMap", this.c2cAdvertService.getC2cSyspara("c2c_advert_expire_time"));
			
			// 支付方式拼接
			String pay_type = "";
			if (StringUtils.isNotEmpty(payment_method1)) {
				if ("".equals(pay_type)) {
					pay_type += payment_method1;
				} else {
					pay_type = pay_type + "," + payment_method1;
				}
			}
			if (StringUtils.isNotEmpty(payment_method2) && !pay_type.contains(payment_method2)) {
				if ("".equals(pay_type)) {
					pay_type += payment_method2;
				} else {
					pay_type = pay_type + "," + payment_method2;
				}
			}
			if (StringUtils.isNotEmpty(payment_method3) && !pay_type.contains(payment_method3)) {
				if ("".equals(pay_type)) {
					pay_type += payment_method3;
				} else {
					pay_type = pay_type + "," + payment_method3;
				}
			}

			String error = this.verif(c2c_user_code, direction, pay_type, currency, symbol, coin_amount, symbol_value, 
					investment_min, investment_max, on_sale, sort_index, expire_time, login_safeword);
			if (!StringUtils.isNullOrEmpty(error)) {
				throw new BusinessException(error);
			}
			
			if (StringUtils.isEmptyString(coin_amount) || !StringUtils.isDouble(coin_amount) || Double.valueOf(coin_amount).doubleValue() <= 0) {
				throw new BusinessException("交易币种数量未填或格式不正确");
			}
			
			if (StringUtils.isEmptyString(symbol_value) || !StringUtils.isDouble(symbol_value) || Double.valueOf(symbol_value).doubleValue() <= 0) {
				throw new BusinessException("币种单价未填或格式不正确");
			}
			
			DecimalFormat df = new DecimalFormat("#.########");
			
			double coin_amount_double = Double.valueOf(df.format(Double.valueOf(coin_amount))).doubleValue();
			double symbol_value_double = Double.valueOf(df.format(Double.valueOf(symbol_value))).doubleValue();
			double investment_min_double = Double.valueOf(df.format(Double.valueOf(investment_min))).doubleValue();
			double investment_max_double = Double.valueOf(df.format(Double.valueOf(investment_max))).doubleValue();
			
			C2cUser c2cUser = this.c2cUserService.findByUsercode(c2c_user_code);
			if (null == c2cUser) {
				throw new BusinessException("承兑商不存在");
			}
			
			C2cAdvert c2cAdvert = this.c2cAdvertService.get(id);
			if (null == c2cAdvert || !c2cAdvert.getC2cUserId().equals(c2cUser.getId().toString())) {
				throw new BusinessException("广告不存在");
			}
			
			String userNameLogin = this.getUsername_login();
			
			// 验证资金密码
			SecUser sec = this.secUserService.findUserByLoginName(userNameLogin);
			this.checkLoginSafeword(sec, userNameLogin, login_safeword);
						
			Long orderCount = this.c2cOrderService.findNoEndingOrdersCountByAdvertId(id);
			if (orderCount > 0) {
				throw new BusinessException("广告还有未完结订单，不能修改");
			}
			
			Long appealCount = this.c2cAppealService.findNoHandleAppealsCountByAdvertId(id);
			if (null == appealCount) {
				throw new BusinessException("广告还有未处理的订单申诉，不能修改");
			}
			
			// 计算广告参数
			Map<String, Object> computeValue = this.c2cAdvertService.getComputeValue(Arith.add(c2cUser.getDeposit(), c2cAdvert.getDeposit()), currency, symbol, 
					coin_amount_double, symbol_value_double);
			
			if (coin_amount_double > Double.valueOf(computeValue.get("coin_amount_max").toString()).doubleValue()) {
				throw new BusinessException("交易币种数量不能大于最大可交易数量");
			}
			
			if (investment_min_double <= 0 || investment_min_double < Double.valueOf(computeValue.get("investment_min_limit").toString()).doubleValue()) {
				throw new BusinessException("单笔订单支付金额下限错误");
			}
			
			if (investment_max_double > Double.valueOf(computeValue.get("investment_max_limit").toString()).doubleValue()) {
				throw new BusinessException("单笔订单支付金额上限错误");
			}
			
//			modelAndView.addObject("all_deposit", c2cUser.getDeposit());
			modelAndView.addObject("pay_rate", computeValue.get("pay_rate"));
			modelAndView.addObject("symbol_close", computeValue.get("symbol_close"));
			modelAndView.addObject("price", computeValue.get("price"));
			modelAndView.addObject("coin_amount_max", computeValue.get("coin_amount_max"));
			modelAndView.addObject("investment_min_limit", computeValue.get("investment_min_limit"));
			modelAndView.addObject("investment_max_limit", computeValue.get("investment_max_limit"));
			
//			modelAndView.addObject("deposit_open_old", c2cAdvert.getDepositOpen());
			modelAndView.addObject("deposit_old", c2cAdvert.getDeposit());
				
			double pay_rate_double = Double.valueOf(computeValue.get("pay_rate").toString()).doubleValue();
			double symbol_close_double = Double.valueOf(computeValue.get("symbol_close").toString()).doubleValue();
			
			double deposit_old_double = c2cAdvert.getDeposit();
			double deposit_double = Double.valueOf(computeValue.get("deposit_open").toString()).doubleValue();
			double change = deposit_double - deposit_old_double;
			
			modelAndView.addObject("all_deposit", df.format(Arith.sub(c2cUser.getDeposit(), change)));
			modelAndView.addObject("deposit", deposit_double);
			modelAndView.addObject("deposit_total", Arith.add(c2cUser.getDeposit(), c2cAdvert.getDeposit()));
			
			if (deposit_double < 0) {
				throw new BusinessException("广告保证金不能小于0");
			}
			
			if (change > 0) {
				if (Double.valueOf(change).doubleValue() > c2cUser.getDeposit()) {
					throw new BusinessException("广告保证金增加差值不能大于承兑商剩余总保证金");
				}				
			}
			
			String log = MessageFormat.format("ip:" + this.getIp() 
				+ ",管理员修改承兑商广告,id:{0},原承兑商ID:{1},原买卖方式:{2},原支付币种:{3},原上架币种:{4},原上架币种实时行情价:{5},原支付比率:{6},原支付方式:{7},原币种单价:{8},"
				+ "原币种数量:{9},原单笔订单最低限额:{10},原单笔订单最高限额:{11},原剩余保证金:{12},原保证金:{13},原是否上架:{14},原是否关闭:{15},"
				+ "原排序索引:{16},原支付时效:{17},原交易条款:{18},原订单自动消息:{19},原备注:{20},原创建时间:{21},原更新时间:{22}", 
				c2cAdvert.getId(), c2cAdvert.getC2cUserId(), c2cAdvert.getDirection(), c2cAdvert.getCurrency(), c2cAdvert.getSymbol(), c2cAdvert.getSymbolClose(), c2cAdvert.getPayRate(), c2cAdvert.getPayType(), c2cAdvert.getSymbolValue(), 
				c2cAdvert.getCoinAmount(), c2cAdvert.getInvestmentMin(), c2cAdvert.getInvestmentMax(), c2cAdvert.getDeposit(), c2cAdvert.getDepositOpen(), c2cAdvert.getOnSale(), c2cAdvert.getClosed(), 
				c2cAdvert.getSortIndex(), c2cAdvert.getExpireTime(), c2cAdvert.getTransactionTerms(), c2cAdvert.getOrderMsg(), c2cAdvert.getRemark(), c2cAdvert.getCreateTime(), c2cAdvert.getUpdateTime());
			
			c2cAdvert.setC2cUserId(c2cUser.getId().toString());
			c2cAdvert.setDirection(direction);
			c2cAdvert.setCurrency(currency);
			c2cAdvert.setSymbol(symbol);
			c2cAdvert.setSymbolClose(symbol_close_double);
			c2cAdvert.setPayRate((int) pay_rate_double);
			c2cAdvert.setPayType(pay_type);
			c2cAdvert.setSymbolValue(symbol_value_double);
			c2cAdvert.setCoinAmount(coin_amount_double);
			c2cAdvert.setInvestmentMin(investment_min_double);
			c2cAdvert.setInvestmentMax(investment_max_double);
			c2cAdvert.setDeposit(deposit_double);
//				c2cAdvert.setDepositOpen(deposit_open_double);
			c2cAdvert.setOnSale(Integer.valueOf(on_sale).intValue());
//				c2cAdvert.setClosed(Integer.valueOf(closed).intValue());
			c2cAdvert.setSortIndex(StringUtils.isNotEmpty(sort_index) ? Integer.valueOf(sort_index).intValue() : 0);
			c2cAdvert.setExpireTime(Integer.valueOf(expire_time).intValue());
			c2cAdvert.setTransactionTerms(transaction_terms);
			c2cAdvert.setOrderMsg(order_msg);
			c2cAdvert.setRemark(remark);
			c2cAdvert.setUpdateTime(new Date());
			
			this.c2cAdvertService.update(c2cAdvert);
			
			double oldC2cUserDeposit = c2cUser.getDeposit();
			
			c2cUser.setDeposit(Arith.sub(c2cUser.getDeposit(), change));
			
			this.c2cUserService.update(c2cUser);
			
			log += MessageFormat.format(",id:{0},新承兑商ID:{1},新买卖方式:{2},新支付币种:{3},新上架币种:{4},新上架币种实时行情价:{5},新支付比率:{6},新支付方式:{7},新币种单价:{8},"
				+ "新币种数量:{9},新单笔订单最低限额:{10},新单笔订单最高限额:{11},新剩余保证金:{12},新保证金:{13},新是否上架:{14},新是否关闭:{15},"
				+ "新排序索引:{16},新支付时效:{17},新交易条款:{18},新订单自动消息:{19},新备注:{20},新创建时间:{21},新更新时间:{22}#####原承兑商剩余保证金:{23},新承兑商剩余保证金:{24}",
				c2cAdvert.getId(), c2cAdvert.getC2cUserId(), c2cAdvert.getDirection(), c2cAdvert.getCurrency(), c2cAdvert.getSymbol(), c2cAdvert.getSymbolClose(), c2cAdvert.getPayRate(), c2cAdvert.getPayType(), c2cAdvert.getSymbolValue(), 
				c2cAdvert.getCoinAmount(), c2cAdvert.getInvestmentMin(), c2cAdvert.getInvestmentMax(), c2cAdvert.getDeposit(), c2cAdvert.getDepositOpen(), c2cAdvert.getOnSale(), c2cAdvert.getClosed(), 
				c2cAdvert.getSortIndex(), c2cAdvert.getExpireTime(), c2cAdvert.getTransactionTerms(), c2cAdvert.getOrderMsg(), c2cAdvert.getRemark(), c2cAdvert.getCreateTime(), c2cAdvert.getUpdateTime(), 
				oldC2cUserDeposit, c2cUser.getDeposit());
							
			this.saveLog(sec, userNameLogin, log, Constants.LOG_CATEGORY_C2C);
			
		} catch (BusinessException e) {
			modelAndView.addObject("error", e.getMessage());
			modelAndView.setViewName("c2c_advert_update");
			return modelAndView;
		} catch (Throwable t) {
			logger.error(" error ", t);
			modelAndView.addObject("error", "[ERROR] " + t.getMessage());
			modelAndView.setViewName("c2c_advert_update");
			return modelAndView;
		}
		
		modelAndView.addObject("message", "操作成功");
		modelAndView.setViewName("redirect:/" + action + "list.action");
		return modelAndView;
	}

	/**
	 * 关闭 C2C广告
	 */
	@RequestMapping(action + "close.action")
	public ModelAndView close(HttpServletRequest request) {
		String id = request.getParameter("id");
		String login_safeword = request.getParameter("login_safeword");

		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("redirect:/" + action + "list.action");
		
		try {
			
			if (StringUtils.isEmptyString(id)) {
				throw new BusinessException("广告id不正确");
			}

			if (StringUtils.isEmptyString(login_safeword)) {
				throw new BusinessException("资金密码错误");
			}
						
			String userNameLogin = this.getUsername_login();
			
			SecUser sec = this.secUserService.findUserByLoginName(userNameLogin);
			this.checkLoginSafeword(sec, userNameLogin, login_safeword);			
			
			C2cAdvert c2cAdvert = this.c2cAdvertService.get(id);
			if (null == c2cAdvert) {
				throw new BusinessException("广告不存在");
			}
			
			C2cUser c2cUser = this.c2cUserService.get(c2cAdvert.getC2cUserId());
			if (null == c2cUser) {
				throw new BusinessException("承兑商不存在");
			}
			
			Long orderCount = this.c2cOrderService.findNoEndingOrdersCountByAdvertId(id);
			if (orderCount > 0) {
				throw new BusinessException("广告还有未完结订单，不能关闭");
			}
			
			Long appealCount = this.c2cAppealService.findNoHandleAppealsCountByAdvertId(id);
			if (null == appealCount) {
				throw new BusinessException("广告还有未处理的订单申诉，不能关闭");
			}
			
			double oldC2cUserDeposit = c2cUser.getDeposit();
			if (c2cAdvert.getDeposit() > 0) {
				// 退还保证金
				c2cUser.setDeposit(Arith.add(c2cUser.getDeposit(), c2cAdvert.getDeposit()));
				this.c2cUserService.update(c2cUser);	
				c2cAdvert.setDeposit(0);							
			}

			c2cAdvert.setOnSale(0);
			c2cAdvert.setClosed(1);
			this.c2cAdvertService.update(c2cAdvert);
			
			String log = MessageFormat.format("ip:" + this.getIp() 
				+ ",管理员关闭承兑商广告,id:{0},承兑商ID:{1},买卖方式:{2},支付币种:{3},上架币种:{4},上架币种实时行情价:{5},支付比率:{6},支付方式:{7},币种单价:{8},"
				+ "币种数量:{9},单笔订单最低限额:{10},单笔订单最高限额:{11},剩余保证金:{12},保证金:{13},是否上架:{14},是否关闭:{15},"
				+ "排序索引:{16},支付时效:{17},交易条款:{18},订单自动消息:{19},备注:{20},创建时间:{21},更新时间:{22}#####原承兑商剩余保证金:{23},新承兑商剩余保证金:{24}",
				c2cAdvert.getId(), c2cAdvert.getC2cUserId(), c2cAdvert.getDirection(), c2cAdvert.getCurrency(), c2cAdvert.getSymbol(), c2cAdvert.getSymbolClose(), c2cAdvert.getPayRate(), c2cAdvert.getPayType(), c2cAdvert.getSymbolValue(), 
				c2cAdvert.getCoinAmount(), c2cAdvert.getInvestmentMin(), c2cAdvert.getInvestmentMax(), c2cAdvert.getDeposit(), c2cAdvert.getDepositOpen(), c2cAdvert.getOnSale(), c2cAdvert.getClosed(), 
				c2cAdvert.getSortIndex(), c2cAdvert.getExpireTime(), c2cAdvert.getTransactionTerms(), c2cAdvert.getOrderMsg(), c2cAdvert.getRemark(), c2cAdvert.getCreateTime(), c2cAdvert.getUpdateTime(), 
				oldC2cUserDeposit, c2cUser.getDeposit());
			
			this.saveLog(sec, userNameLogin, log, Constants.LOG_CATEGORY_C2C);
			
		} catch (BusinessException e) {
			modelAndView.addObject("error", e.getMessage());
			return modelAndView;
		} catch (Throwable t) {
			logger.error("delete error ", t);
			modelAndView.addObject("error", "程序错误");
			return modelAndView;
		}
		
		modelAndView.addObject("message", "操作成功");
		return modelAndView;
	}

	/**
	 * 退还 C2C广告 所有保证金
	 */
	@RequestMapping(action + "backAllDeposit.action")
	public ModelAndView backAllDeposit(HttpServletRequest request) {
		String id = request.getParameter("id");
		String login_safeword = request.getParameter("login_safeword");

		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("redirect:/" + action + "list.action");
		
		try {
			
			if (StringUtils.isEmptyString(id)) {
				throw new BusinessException("广告id不正确");
			}
			
			if (StringUtils.isEmptyString(login_safeword)) {
				throw new BusinessException("资金密码错误");
			}
			
			String userNameLogin = this.getUsername_login();
			
			SecUser sec = this.secUserService.findUserByLoginName(userNameLogin);
			this.checkLoginSafeword(sec, userNameLogin, login_safeword);			
			
			C2cAdvert c2cAdvert = this.c2cAdvertService.get(id);
			if (null == c2cAdvert) {
				throw new BusinessException("广告不存在");
			}
			
			C2cUser c2cUser = this.c2cUserService.get(c2cAdvert.getC2cUserId());
			if (null == c2cUser) {
				throw new BusinessException("承兑商不存在");
			}
			
			Long orderCount = this.c2cOrderService.findNoEndingOrdersCountByAdvertId(id);
			if (orderCount > 0) {
				throw new BusinessException("广告还有未完结订单，不能退还");
			}
			
			Long appealCount = this.c2cAppealService.findNoHandleAppealsCountByAdvertId(id);
			if (null == appealCount) {
				throw new BusinessException("广告还有未处理的订单申诉，不能退还");
			}
			
			DecimalFormat df = new DecimalFormat("#.########");
			
			double oldC2cUserDeposit = c2cUser.getDeposit();
			if (c2cAdvert.getDeposit() > 0) {
				// 退还保证金
				double depositTotal = Arith.add(c2cUser.getDeposit(), c2cAdvert.getDeposit());
				c2cUser.setDeposit(Double.valueOf(df.format(depositTotal)).doubleValue());
				this.c2cUserService.update(c2cUser);
				
				c2cAdvert.setCoinAmount(0);
				c2cAdvert.setDeposit(0);
			}
			
			// 广告下架
			c2cAdvert.setOnSale(0);
			this.c2cAdvertService.update(c2cAdvert);
			
			String log = MessageFormat.format("ip:" + this.getIp() 
				+ ",管理员退还承兑商广告所有保证金,id:{0},承兑商ID:{1},买卖方式:{2},支付币种:{3},上架币种:{4},上架币种实时行情价:{5},支付比率:{6},支付方式:{7},币种单价:{8},"
				+ "币种数量:{9},单笔订单最低限额:{10},单笔订单最高限额:{11},剩余保证金:{12},保证金:{13},是否上架:{14},是否关闭:{15},"
				+ "排序索引:{16},支付时效:{17},交易条款:{18},订单自动消息:{19},备注:{20},创建时间:{21},更新时间:{22}#####原承兑商剩余保证金:{23},新承兑商剩余保证金:{24}",
				c2cAdvert.getId(), c2cAdvert.getC2cUserId(), c2cAdvert.getDirection(), c2cAdvert.getCurrency(), c2cAdvert.getSymbol(), c2cAdvert.getSymbolClose(), c2cAdvert.getPayRate(), c2cAdvert.getPayType(), c2cAdvert.getSymbolValue(), 
				c2cAdvert.getCoinAmount(), c2cAdvert.getInvestmentMin(), c2cAdvert.getInvestmentMax(), c2cAdvert.getDeposit(), c2cAdvert.getDepositOpen(), c2cAdvert.getOnSale(), c2cAdvert.getClosed(), 
				c2cAdvert.getSortIndex(), c2cAdvert.getExpireTime(), c2cAdvert.getTransactionTerms(), c2cAdvert.getOrderMsg(), c2cAdvert.getRemark(), c2cAdvert.getCreateTime(), c2cAdvert.getUpdateTime(), 
				oldC2cUserDeposit, c2cUser.getDeposit());
			
			this.saveLog(sec, userNameLogin, log, Constants.LOG_CATEGORY_C2C);
			
		} catch (BusinessException e) {
			modelAndView.addObject("error", e.getMessage());
			return modelAndView;
		} catch (Throwable t) {
			logger.error("delete error ", t);
			modelAndView.addObject("error", "程序错误");
			return modelAndView;
		}
		
		modelAndView.addObject("message", "操作成功");
		return modelAndView;
	}
	
	/**
	 * 获取 承兑商 剩余保证金
	 */
	@RequestMapping(action + "getC2cUserDeposit.action")
	public String getC2cUserDeposit(HttpServletRequest request) {
		String c2c_user_code = request.getParameter("c2c_user_code");		
		String advert_id = request.getParameter("advert_id");
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		try {
			
			C2cUser c2cUser = this.c2cUserService.findByUsercode(c2c_user_code);
			if (null == c2cUser) {
				throw new BusinessException("承兑商不存在");
			}
			
			double advertDeposit = 0;
			if (StringUtils.isNotEmpty(advert_id)) {
				C2cAdvert c2cAdvert = this.c2cAdvertService.get(advert_id);
				if (null != c2cAdvert) {
					advertDeposit = c2cAdvert.getDeposit();
				}
			}
			
			List<C2cPaymentMethod> methodList = this.c2cPaymentMethodService.getMethodConfigListByPartyId(c2cUser.getC2cUserPartyId());
			
			resultMap.put("code", 200);
			resultMap.put("all_deposit", c2cUser.getDeposit());
			resultMap.put("deposit_total", Arith.add(c2cUser.getDeposit(), advertDeposit));
			resultMap.put("paymentMethodList", methodList);
			
		} catch (BusinessException e) {
			resultMap.put("code", 500);
			resultMap.put("message", e.getMessage());
		} catch (Throwable t) {
			logger.error(" error ", t);
			resultMap.put("code", 500);
			resultMap.put("message", "程序错误");
		}		

		return JsonUtils.getJsonString(resultMap);
	}
	
	/**
	 * 获取 交易币种数量，币种单价
	 */
	@RequestMapping(action + "compute.action")
	public String compute(HttpServletRequest request) {
		String deposit_total = request.getParameter("deposit_total");	
		String currency = request.getParameter("currency");	
		String symbol = request.getParameter("symbol");	
		String coin_amount = request.getParameter("coin_amount");
		String symbol_value = request.getParameter("symbol_value");	
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		try {
			
			Map<String, Object> computeValue = new HashMap<String, Object>();
			
			String error = this.verifCompute(currency, symbol, coin_amount, symbol_value);
			if (!StringUtils.isNullOrEmpty(error)) {
				throw new BusinessException(error);
			}
			
			if (StringUtils.isEmptyString(deposit_total) || !StringUtils.isDouble(deposit_total) || Double.valueOf(deposit_total).doubleValue() <= 0) {

				computeValue.put("pay_rate", 100);
				computeValue.put("deposit_open", 0);
				computeValue.put("all_deposit", deposit_total);
				computeValue.put("symbol_close", 0);
				computeValue.put("price", 0);
				computeValue.put("coin_amount_max", 0);
				computeValue.put("investment_min_limit", 0);
				computeValue.put("investment_max_limit", 0);
				
				resultMap.put("code", 200);
				resultMap.putAll(computeValue);
				return JsonUtils.getJsonString(resultMap);
			}

			double deposit_total_double = Double.valueOf(deposit_total).doubleValue();
			
			if (StringUtils.isEmptyString(coin_amount) || !StringUtils.isDouble(coin_amount) || Double.valueOf(coin_amount).doubleValue() <= 0) {
				
				computeValue = this.c2cAdvertService.getComputeValue(deposit_total_double, currency, symbol, 0, 1);
				computeValue.put("pay_rate", 100);
				computeValue.put("deposit_open", 0);
				computeValue.put("all_deposit", deposit_total);
				computeValue.put("investment_min_limit", 0);
				computeValue.put("investment_max_limit", 0);
				
				resultMap.put("code", 200);
				resultMap.putAll(computeValue);
				return JsonUtils.getJsonString(resultMap);
			}
			double coin_amount_double = Double.valueOf(coin_amount).doubleValue();
			
			if (StringUtils.isEmptyString(symbol_value) || !StringUtils.isDouble(symbol_value) || Double.valueOf(symbol_value).doubleValue() <= 0) {
				
				computeValue = this.c2cAdvertService.getComputeValue(deposit_total_double, currency, symbol, coin_amount_double, 1);
				computeValue.put("pay_rate", 100);
				computeValue.put("deposit_open", 0);
				computeValue.put("all_deposit", deposit_total);
				computeValue.put("investment_min_limit", 0);
				computeValue.put("investment_max_limit", 0);
				
				resultMap.put("code", 200);
				resultMap.putAll(computeValue);
				return JsonUtils.getJsonString(resultMap);
			}
			
			double symbol_value_double = Double.valueOf(symbol_value).doubleValue();
			
			computeValue = this.c2cAdvertService.getComputeValue(deposit_total_double, currency, symbol, coin_amount_double, symbol_value_double);
			
			resultMap.put("code", 200);
			resultMap.putAll(computeValue);
			
		} catch (BusinessException e) {
			resultMap.put("code", 500);
			resultMap.put("message", e.getMessage());
		} catch (Throwable t) {
			logger.error(" error ", t);
			resultMap.put("code", 500);
			resultMap.put("message", "程序错误");
		}		

		return JsonUtils.getJsonString(resultMap);
	}

	private String verifCompute(String currency, String symbol, String coin_amount, String symbol_value) {		
		Map<String, String> currencyMap = this.c2cAdvertService.getCurrencyMap();
		Map<String, String> symbolMap = this.c2cAdvertService.getSymbolMap();		
		if (null == currencyMap || !currencyMap.containsKey(currency)) {
			return "支付币种不正确";
		}		
		if (null == symbolMap || !symbolMap.containsKey(symbol)) {
			return "上架币种不正确";
		}
//		if (StringUtils.isEmptyString(coin_amount) || !StringUtils.isDouble(coin_amount) || Double.valueOf(coin_amount).doubleValue() <= 0) {
//			return "交易币种数量未填或格式不正确";
//		}
//		if (StringUtils.isEmptyString(symbol_value) || !StringUtils.isDouble(symbol_value) || Double.valueOf(symbol_value).doubleValue() <= 0) {
//			return "币种单价未填或格式不正确";
//		}		
		return null;
	}
	
	private String verif(String c2c_user_code, String direction, String pay_type, String currency, String symbol, String coin_amount, String symbol_value, 
			String investment_min, String investment_max, String on_sale, String sort_index, String expire_time, String login_safeword) {
		this.verifCompute(currency, symbol, coin_amount, symbol_value);
		if (StringUtils.isEmptyString(c2c_user_code)) {
			return "承兑商UID为空";
		}		
		if (StringUtils.isEmptyString(direction) || !Arrays.asList("buy", "sell").contains(direction)) {
			return "买卖方式不正确";
		}		
		if (StringUtils.isEmptyString(pay_type)) {
			return "请选择支付方式";
		}		
		if (StringUtils.isEmptyString(investment_min) || !StringUtils.isDouble(investment_min) || Double.valueOf(investment_min).doubleValue() < 0) {
			return "单笔订单最低限额未填或格式不正确";
		}		
		if (StringUtils.isEmptyString(investment_max) || !StringUtils.isDouble(investment_max) || Double.valueOf(investment_max).doubleValue() < 0) {
			return "单笔订单最高限额未填或格式不正确";
		}
		if (Double.valueOf(investment_max).doubleValue() < Double.valueOf(investment_min).doubleValue()) {
			return "单笔订单上限金额不能小于下限金额";
		}
		if (StringUtils.isEmptyString(on_sale) || !Arrays.asList("0", "1").contains(on_sale)) {
			return "是否上架未填或格式不正确";
		}
//		if (StringUtils.isEmptyString(sort_index) || !StringUtils.isInteger(sort_index) || Integer.valueOf(sort_index).intValue() < 0) {
//			return "排序索引未填或格式不正确";
//		}
		if (StringUtils.isEmptyString(expire_time) || !StringUtils.isInteger(expire_time) || Integer.valueOf(expire_time).intValue() < 0) {
			return "支付时效未填或格式不正确";
		}		
		if (StringUtils.isEmptyString(login_safeword)) {
			return "资金密码错误";
		}
		return null;
	}

	/**
	 * 验证登录人资金密码
	 */
	protected void checkLoginSafeword(SecUser secUser, String operatorUsername, String loginSafeword) {
//		SecUser sec = this.secUserService.findUserByLoginName(operatorUsername);
		String sysSafeword = secUser.getSafeword();
		String safeword_md5 = this.passwordEncoder.encodePassword(loginSafeword, operatorUsername);
		if (!safeword_md5.equals(sysSafeword)) {
			throw new BusinessException("登录人资金密码错误");
		}		
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
