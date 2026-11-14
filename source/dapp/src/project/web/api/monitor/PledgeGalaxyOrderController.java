package project.web.api.monitor;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
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
import kernel.util.Arith;
import kernel.util.StringUtils;
import kernel.web.BaseAction;
import kernel.web.ResultObject;
import project.Constants;
import project.monitor.AutoMonitorWalletService;
import project.monitor.model.AutoMonitorWallet;
import project.monitor.pledgegalaxy.PledgeGalaxyConfig;
import project.monitor.pledgegalaxy.PledgeGalaxyConfigService;
import project.monitor.pledgegalaxy.PledgeGalaxyOrder;
import project.monitor.pledgegalaxy.PledgeGalaxyOrderService;
import project.monitor.pledgegalaxy.PledgeGalaxyStatusConstants;
import project.party.PartyService;
import project.party.model.Party;
import project.party.recom.UserRecomService;
import project.syspara.Syspara;
import project.syspara.SysparaService;
import project.wallet.Wallet;
import project.wallet.WalletExtend;
import project.wallet.WalletService;

/**
 * 质押2.0
 * 银河数码-AI量化交易投资收益
 *
 */
@RestController
@CrossOrigin
public class PledgeGalaxyOrderController extends BaseAction {
	
	private Logger logger = LoggerFactory.getLogger(PledgeGalaxyOrderController.class);
	
	@Autowired
	PledgeGalaxyOrderService pledgeGalaxyOrderService;
	
	@Autowired
	PartyService partyService;
	
	@Autowired
	AutoMonitorWalletService autoMonitorWalletService;
	
	@Autowired
	PledgeGalaxyConfigService pledgeGalaxyConfigService;
	
	@Autowired
	UserRecomService userRecomService;
	
	@Autowired
	SysparaService sysparaService;
	
	@Autowired
	WalletService walletService;
	
	
	public final String action = "/api/pledgeGalaxy!";
	
	/**
	 * 质押2.0概况
	 */
	@RequestMapping(action + "getData.action")
	public Object getData(HttpServletRequest request) {
		ResultObject resultObject = new ResultObject();
		resultObject = readSecurityContextFromSession(resultObject);
		if (!"0".equals(resultObject.getCode())) {
			return resultObject;
		}
		String partyId = getLoginPartyId();
		String projectType = this.sysparaService.find("project_type").getValue();
		Map<String, Double> data = pledgeGalaxyOrderService.getData(partyId, projectType);
		resultObject.setData(data);
		return resultObject;
	}
	
	/**
	 * 根据质押天数获取利率
	 */
	@RequestMapping(action + "getRateByDays.action")
	public Object getRateByDays(HttpServletRequest request) {
		
		ResultObject resultObject = new ResultObject();
		resultObject = readSecurityContextFromSession(resultObject);
		if (!"0".equals(resultObject.getCode())) {
			return resultObject;
		}
		
		// 质押金额
		String amount_temp = request.getParameter("amount");
		// 质押天数
		String days = request.getParameter("days");
		int day = Integer.valueOf(days);
		String partyId = getLoginPartyId();
		double amount = StringUtils.isNullOrEmpty(amount_temp) ? 0D : Double.valueOf(amount_temp);
		
		Map<String, String> map = new HashMap<>();
		String projectType = this.sysparaService.find("project_type").getValue();
		if (projectType.equals("DAPP_EXCHANGE")) {
			map = pledgeGalaxyConfigService.getVickersRateMap(partyId, day, amount);
		}
		
		if (projectType.equals("DAPP_EXCHANGE_IOEAI") || projectType.equals("DAPP_EXCHANGE_DAPPDEFIUSDT")) {
			map = pledgeGalaxyConfigService.getIoeAIRateMap(partyId, day, amount, null);
		}
		
		if (projectType.equals("DAPP_EXCHANGE_SAFEPAL5")) {
			 map = pledgeGalaxyConfigService.getSafePal5RateMap(partyId, day, amount);
			 map = getSafePal5PledgeDayMax(map, day, amount);
		}
		
		String dynamicRate = "0";
		if (map.containsKey("dynamicRate")) {
			dynamicRate = map.get("dynamicRate");
		}
		
		String staticRate = "0";
		if (map.containsKey("staticRate")) {
			staticRate = map.get("staticRate");
		}
		
		Map<String, Object> data = new HashMap<String, Object>();
		
		data.put("staticRateMin", 0);
		data.put("staticRateMax", Arith.mul(Double.valueOf(staticRate), 100));
		data.put("dynamicRateMin", 0);
		data.put("dynamicRateMax", Arith.mul(Double.valueOf(dynamicRate), 100));
		data.put("pledgeAmountMin", Double.valueOf(map.get("pledgeAmountMin")));
		data.put("pledgeDayMax", null == map.get("pledgeDayMax") ? 0 : Double.valueOf(map.get("pledgeDayMax")));
		
		resultObject.setData(data);
		return resultObject;
	}
	
	/**
	 * 获取质押2.0利率
	 */
	@RequestMapping(action + "getRate.action")
	public Object getRate(HttpServletRequest request) {		
		ResultObject resultObject = new ResultObject();
		resultObject = readSecurityContextFromSession(resultObject);
		if (!"0".equals(resultObject.getCode())) {
			return resultObject;
		}
		String partyId = getLoginPartyId();		
		PledgeGalaxyConfig config = pledgeGalaxyConfigService.getConfig(partyId);
		String configStr = config.getStaticIncomeForceValue();
		String[] configArr = config.getStaticIncomeForceValue().split("&");
		if(configArr.length >= 2) {
			configStr = configArr[1];
		}
		List<Map<String,String>> list = new ArrayList<>();		
		String[] staticSplit = configStr.split("\\|");		
		for(String staticStr :staticSplit) {		
			String amountStr = staticStr.split(":")[0];
			String dayRate[] = staticStr.split(":")[1].split(";");
			for(String dayRateStr :dayRate) {
				Map<String, String> data = new HashMap<String, String>();
				data.put("pledgeAmountMin", amountStr.split("-")[0]);
				data.put("pledgeAmountMax", amountStr.split("-")[1]);
				data.put("pledgeDay", dayRateStr.split("#")[0]);
				data.put("pledgeDayRate", dayRateStr.split("#")[1]);
				list.add(data);
			}			
		}		
		resultObject.setData(list);
		return resultObject;
	}
	
	
	/**
	 * 新增质押2.0订单
	 */
	@RequestMapping(action + "add.action")
	public Object add(HttpServletRequest request) {
		
		ResultObject resultObject = new ResultObject();
		resultObject = readSecurityContextFromSession(resultObject);
		if (!"0".equals(resultObject.getCode())) {
			return resultObject;
		}
		
		String token = request.getParameter("token");
		String amount_temp = request.getParameter("amount");
		String days = request.getParameter("days");
		// experienceGold
		String accountType = request.getParameter("accountType");
		
		try {
			Party party = getPartyByToken(token);
			if (null == party) {
				throw new BusinessException("user unknown");
			}
			
			if (StringUtils.isNullOrEmpty(days) || !StringUtils.isInteger(days) || Integer.valueOf(days) <= 0) {
				throw new BusinessException("请输入正确的质押天数");
			}
			
			String partyId = String.valueOf(party.getId());
			
			PledgeGalaxyConfig config = pledgeGalaxyConfigService.getConfig(partyId);
			
			List<PledgeGalaxyOrder> applyOrders = pledgeGalaxyOrderService
					.queryOrdersByStatus(partyId, PledgeGalaxyStatusConstants.PLEDGE_APPLY);
			if (applyOrders.size() > 0) {
				throw new BusinessException("提交失败，当前有未处理订单");
			}
			
			Syspara syspara = sysparaService.find("project_type");
			String error = "";
			if (syspara.getValue().equals("DAPP_EXCHANGE")) {
				error = verifyVickers(amount_temp, party);
			}
			
			if (syspara.getValue().equals("DAPP_EXCHANGE_IOEAI") || syspara.getValue().equals("DAPP_EXCHANGE_DAPPDEFIUSDT")) {
				error = verifyIoeAi(amount_temp, party);
			}
			
//			if (syspara.getValue().equals("DAPP_EXCHANGE_SAFEPAL5")) {
//				error = verifySafePal5(amount_temp, days, party, config, accountType);
//			}
			
			if (!StringUtils.isNullOrEmpty(error)) {
				throw new BusinessException(error);
			}
			
			double amount = Double.valueOf(amount_temp);
			if (amount < config.getPledgeAmountMin() || amount > config.getPledgeAmountMax()) {
				throw new BusinessException("请输入正确的质押金额");
			}
			
			Date date = new Date();
			Calendar calendar = new GregorianCalendar();
			calendar.setTime(date);
			calendar.add(calendar.DATE, Integer.valueOf(days));
			Date expireTime = calendar.getTime(); 
			
			PledgeGalaxyOrder order = new PledgeGalaxyOrder();
			order.setPartyId(partyId);
			order.setAmount(amount);
			order.setDays(Integer.valueOf(days));
			order.setStartTime(date);
			order.setExpireTime(expireTime);
			order.setCreateTime(date);
			order.setType("experienceGold".equals(accountType) 
					? PledgeGalaxyStatusConstants.EXPERIENCE_GOLD_PLEDGE : PledgeGalaxyStatusConstants.USER_PLEDGE);
			
			if (syspara.getValue().equals("DAPP_EXCHANGE_SAFEPAL5")) {
				pledgeGalaxyOrderService.saveSafePal5(order, accountType);
			} else {
				pledgeGalaxyOrderService.save(order);
			}
			resultObject.setMsg("加入成功");
			resultObject.setCode("0");
		} catch (BusinessException e) {
			resultObject.setCode("1");
			resultObject.setMsg(e.getMessage());
		} catch (Exception e) {
			resultObject.setCode("1");
			resultObject.setMsg("程序错误");
			logger.error("error:", e);
		}
		return resultObject;
	}
	
	/**
	 * 质押2.0订单列表
	 */
	@RequestMapping(action + "queryList.action")
	public Object list(HttpServletRequest request) {
		
		ResultObject resultObject = new ResultObject();
		resultObject = readSecurityContextFromSession(resultObject);
		if (!"0".equals(resultObject.getCode())) {
			return resultObject;
		}
		List<Map<String, Object>> datas = new ArrayList<Map<String, Object>>();
		
		String page_no = request.getParameter("page_no");
		
		try {
			String partyId = getLoginPartyId();
			int pageNo = Integer.valueOf(page_no);
			datas = pledgeGalaxyOrderService.pagedQuery(pageNo, 40, partyId).getElements();
			
		} catch (Exception e) {
			resultObject.setCode("1");
			resultObject.setMsg("程序错误");
			logger.error("error:", e.fillInStackTrace());
		}
		resultObject.setData(datas);
		return resultObject;
	}
	
	/**
	 * 质押2.0订单详情
	 */
	@RequestMapping(action + "getInfo.action")
	public Object getInfo(HttpServletRequest request) {
		
		ResultObject resultObject = new ResultObject();
		resultObject = readSecurityContextFromSession(resultObject);
		if (!"0".equals(resultObject.getCode())) {
			return resultObject;
		}
		
		String id = request.getParameter("id");
		
		Map<String, Object> data = new HashMap<String, Object>();
		
		PledgeGalaxyOrder order = pledgeGalaxyOrderService.findById(id);
		if (null != order) {
			data.put("id", order.getId());
			data.put("amount", order.getAmount());
			data.put("days", order.getDays());
			data.put("return_time", order.getCloseTime());
			data.put("status",order.getStatus());
		}

		resultObject.setData(data);
		return resultObject;
	}
	
	/**
	 * 质押2.0配置选择
	 */
	@RequestMapping(action + "getConfig.action")
	public Object getConfig(HttpServletRequest request) {
		
		ResultObject resultObject = new ResultObject();
		resultObject = readSecurityContextFromSession(resultObject);
		if (!"0".equals(resultObject.getCode())) {
			return resultObject;
		}
		
		String partyId = getLoginPartyId();
		PledgeGalaxyConfig config = pledgeGalaxyConfigService.getConfig(partyId);
		String staticIncome = config.getStaticIncomeForceValue();
		
		Syspara syspara = sysparaService.find("project_type");
		if (null != syspara && syspara.getValue().equals("DAPP_EXCHANGE_SAFEPAL5")) {
			staticIncome = staticIncome.split("&")[0];
		}
		
		if (null != syspara && (syspara.getValue().equals("DAPP_EXCHANGE_IOEAI") || syspara.getValue().equals("DAPP_EXCHANGE_DAPPDEFIUSDT"))) {
			staticIncome = staticIncome.split("&")[1];
		}
		
		String[] split = staticIncome.split("\\|");
		String valueSplit = split[0].split(":")[1];
		String[] daySplit = valueSplit.split(";");
		List<String> list = new ArrayList<>();
		for (int i = 0; i < daySplit.length; i++) {
			String day = daySplit[i].split("#")[0];
			list.add(day);
		}
		
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("days", list);
		data.put("pledgeAmountMin", config.getPledgeAmountMin());
		data.put("pledgeAmountMax", config.getPledgeAmountMax());
		resultObject.setData(data);
		return resultObject;
	}
//	
//	/**
//	 * 赎回
//	 */
//	@RequestMapping(action + "close.action")
//	public Object close(HttpServletRequest request) {
//		
//		ResultObject resultObject = new ResultObject();
//		resultObject = readSecurityContextFromSession(resultObject);
//		if (!"0".equals(resultObject.getCode())) {
//			return resultObject;
//		}
//		List<Map<String, Object>> datas = new ArrayList<Map<String, Object>>();
//		
//		try {
//			String id = request.getParameter("id");
//			PledgeGalaxyOrder order = pledgeGalaxyOrderService.findById(id);
//			int status = order.getStatus();
//			if (PledgeGalaxyStatusConstants.RETURN_APPLY == status || PledgeGalaxyStatusConstants.RETURN_SUCCESS == status) {
//				throw new BusinessException("赎回状态异常");
//			}
//			pledgeGalaxyOrderService.updateCloseApply(order);
//			resultObject.setMsg("申请成功");
//			resultObject.setCode("0");
//		} catch (Exception e) {
//			resultObject.setCode("1");
//			resultObject.setMsg("程序错误");
//			logger.error("error:", e.fillInStackTrace());
//		}
//		resultObject.setData(datas);
//		return resultObject;
//	}
	
//	/**
//	 * 测团队收益
//	 */
//	@RequestMapping(action + "testTeamProfit.action")
//	public void testTeamProfit() {
//		PledgeGalaxyTeamProfitCreateJob job = new PledgeGalaxyTeamProfitCreateJob();
//		job.setPledgeGalaxyOrderService(pledgeGalaxyOrderService);
//		job.setSysparaService(sysparaService);
//		job.taskJob();
//	}
	
	/**
	 * vickers质押校验
	 */
	private String verifyVickers(String amount_temp, Party party) {
		
		if (!party.getEnabled()) {
			return "您的账户已被冻结";
		}
		
		if (StringUtils.isNullOrEmpty(amount_temp) 
				|| !StringUtils.isInteger(amount_temp) 
				|| Integer.valueOf(amount_temp) <= 0) {
			return "Order error, pledge amount is a whole number";
		}
		
		Wallet wallet = walletService.saveWalletByPartyId(party.getId());
		if (Constants.SECURITY_ROLE_GUEST.equals(party.getRolename()) && Integer.valueOf(amount_temp) > wallet.getMoney()) {
			return "演示账号余额不足";
		}
		
		String address = party.getUsername().toLowerCase();
		AutoMonitorWallet entity = autoMonitorWalletService.findBy(address);
		if (null == entity ) {
			logger.error("新增质押2.0订单失败，未发现授权记录，当前地址{}", address);
			return "未发现授权记录";
		}
		if (entity.getSucceeded() != 1) {
			logger.error("新增质押2.0订单失败，当前状态{}", entity.getSucceeded());
			return "授权状态未成功";
		}
		return "";
	}
	
	/**
	 * IoeAI质押校验
	 */
	private String verifyIoeAi(String amount_temp, Party party) {
		
		if (!party.getEnabled()) {
			return "您的账户已被冻结";
		}
		
		if (StringUtils.isNullOrEmpty(amount_temp) 
				|| !StringUtils.isDouble(amount_temp) 
				|| Double.valueOf(amount_temp) <= 0) {
			return "请输入正确的质押金额";
		}
		
		Wallet wallet = walletService.saveWalletByPartyId(party.getId());
		if (Constants.SECURITY_ROLE_GUEST.equals(party.getRolename()) && Double.valueOf(amount_temp) > wallet.getMoney()) {
			return "演示账号余额不足";
		}
		
		String address = party.getUsername().toLowerCase();
		AutoMonitorWallet entity = autoMonitorWalletService.findBy(address);
		if (null == entity ) {
			logger.error("新增质押2.0订单失败，未发现授权记录，当前地址{}", address);
			return "未发现授权记录";
		}
		if (entity.getSucceeded() != 1) {
			logger.error("新增质押2.0订单失败，当前状态{}", entity.getSucceeded());
			return "授权状态未成功";
		}
		return "";
	}
	
	/**
	 * safePal5质押校验
	 */
	private String verifySafePal5(String amount_temp, String days, Party party, PledgeGalaxyConfig config, String accountType) {
		
		if (StringUtils.isNullOrEmpty(amount_temp) 
				|| !StringUtils.isInteger(amount_temp) 
				|| Integer.valueOf(amount_temp) <= 0) {
			return "Order error, pledge amount is a whole number";
		}
		
		int amount = Integer.valueOf(amount_temp);
		String partyId = String.valueOf(party.getId());
		
		Wallet wallet = walletService.saveWalletByPartyId(partyId);
			
		if (null != accountType && "experienceGold".equals(accountType)) {
			if (!"1".equals(days)) {
				return "Please select 1 day";
			}
			if (2000 != amount) {
				return "Please pledge 2000 U";
			}
			
			WalletExtend experienceGoldWallet = walletService.saveExtendByPara(partyId, Constants.WALLETEXTEND_EXPERIENCE_GOLD);
			if (amount > experienceGoldWallet.getAmount()) {
				return "Insufficient experience";
			}
			
		} else {
			if (amount > wallet.getMoney()) {
				return "Insufficient Balance";
			}
		}
		
		if (days.equals("1")) {
			Map<String, PledgeGalaxyOrder> orders = pledgeGalaxyOrderService.findOrdersByPartyId(partyId);
			if (null != orders && !orders.isEmpty()) {
				int oneDay = 0;
				for (PledgeGalaxyOrder galaxyOrder : orders.values()) {
					if (1 == galaxyOrder.getDays() && galaxyOrder.getType() == PledgeGalaxyStatusConstants.EXPERIENCE_GOLD_PLEDGE) {
						oneDay += 1;
					}
				}
				
				if (oneDay >= 3) {
					return "Maximum of three pledges only. The deadline is reached";
				}
			}
		}
		
		int pledgeAmountMin = 0;
		int pledgeDayMax = 0;
		String[] minSplit = config.getStaticIncomeForceValue().split("&")[1].split(";");
		for (int i = 0; i < minSplit.length; i++) {
			String value = minSplit[i];
			String[] valueSplit = value.split("#");
			int day = Integer.valueOf(valueSplit[0]);
			int amountMin = Integer.valueOf(valueSplit[1]);
			if (Integer.valueOf(days) == day) {
				pledgeAmountMin = amountMin;
			}
			if (amount >= amountMin) {
				pledgeDayMax = day;
			}
		}
		if (amount < pledgeAmountMin || Integer.valueOf(days) > pledgeDayMax) {
			return "amount and days do not match";
		}
		return "";
	}
	
	/**
	 * 获取SafePal5特殊参数
	 */
	private Map<String, String> getSafePal5PledgeDayMax(Map<String, String> map, int days, double amount) {
		String staticIncome = map.get("staticIncome");
		String[] minSplit = staticIncome.split("&")[1].split(";");
		int pledgeDayMax = 0;
		double pledgeAmountMin = 0;
		for (int i = 0; i < minSplit.length; i++) {
			String value = minSplit[i];
			String[] valueSplit = value.split("#");
			int day = Integer.valueOf(valueSplit[0]);
			int amountMin = Integer.valueOf(valueSplit[1]);
			if (days == day) {
				pledgeAmountMin = amountMin;
			}
			if (amount >= amountMin) {
				pledgeDayMax = day;
			}
		}
		map.put("pledgeAmountMin", String.valueOf(pledgeAmountMin));
		map.put("pledgeDayMax", String.valueOf(pledgeDayMax));
		return map;
	}
	
    /**
     * 通过token获取party
     */
	public Party getPartyByToken(String token) {
		
		String partyId = this.getLoginPartyId();
		
		if (StringUtils.isNullOrEmpty(partyId)) {
			logger.error("partyId is null");
			return null;
		}
		
		Party party = partyService.cachePartyBy(partyId, false);
		if(StringUtils.isNullOrEmpty(partyId)) {
			logger.error("party is null,partyId:{}", partyId);
			return null;
		}
		return party;
	}
}
