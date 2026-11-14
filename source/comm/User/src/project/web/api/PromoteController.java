package project.web.api;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
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
import kernel.util.DateUtils;
import kernel.util.StringUtils;
import kernel.web.BaseAction;
import kernel.web.ResultObject;
import project.monitor.pledgegalaxy.PledgeGalaxyConfigService;
import project.party.PartyService;
import project.party.recom.UserRecomService;
import project.syspara.Syspara;
import project.syspara.SysparaService;
import project.user.ChildrenLever;
import project.user.UserData;
import project.user.UserDataService;
import util.DateUtil;

/**
 * 我的推广
 *
 */
@RestController
@CrossOrigin
public class PromoteController extends BaseAction {
	
	private Logger logger=LoggerFactory.getLogger(PromoteController.class);
	
	@Autowired
	protected UserRecomService userRecomService;
	
	@Autowired
	protected UserDataService userDataService;
	
	@Autowired
	protected PartyService partyService;
	
	@Autowired
	protected SysparaService sysparaService;
	
	@Autowired
	protected PledgeGalaxyConfigService pledgeGalaxyConfigService;
	
	private final String action = "api/promote!";

	@RequestMapping(action + "getPromote.action")
	public Object getPromote(HttpServletRequest request) {
		ResultObject resultObject = new ResultObject();
		resultObject = readSecurityContextFromSession(resultObject);
		if (!"0".equals(resultObject.getCode())) {
			return resultObject;
		}
		
		// 层级 1为第一级 1,2,3,4总共4级代理
		String level_temp = request.getParameter("level");
		
		if (StringUtils.isNullOrEmpty(level_temp) 
				|| !StringUtils.isInteger(level_temp) || Integer.valueOf(level_temp) <= 0) {
			throw new BusinessException("代理层级错误");
		}
		
		int level = Integer.valueOf(level_temp);
		String page_no = request.getParameter("page_no");
		if (StringUtils.isNullOrEmpty(page_no) 
				|| !StringUtils.isInteger(page_no) || Integer.valueOf(page_no) <= 0) {
			page_no = "1";
		}
		
		int pageNo = Integer.valueOf(page_no);
		
		String partyId = getLoginPartyId();
		try {

			Map<String, Object> data = new HashMap<String, Object>();
			Map<String, Object> data_total = new HashMap<String, Object>();
			List<Map<String, Object>> dataChilds = new ArrayList<Map<String, Object>>();

			ChildrenLever childrenLever = userDataService.cacheChildrenLever4(partyId);

			data.put("children", childrenLever.getLever1().size() 
					+ childrenLever.getLever2().size()
					+ childrenLever.getLever3().size());

			data.put("level_1", childrenLever.getLever1().size());
			data.put("level_2", childrenLever.getLever2().size());
			data.put("level_3", childrenLever.getLever3().size());
//			data.put("level_4", childrenLever.getLever4().size());
			data_total.put("total", data);

			// 资金盘 定制化需求，后面盘口下架可以删
			dataChilds = this.userDataService.getChildrenLevelPagedForGalaxy(pageNo, 10, partyId, level);
			Map<String, UserData> map = userDataService.cacheByPartyId(partyId);
			Syspara projectType = sysparaService.find("project_type");
			if (null != projectType && projectType.getValue().equals("DAPP_EXCHANGE")) {
				double sum = 0;
				if (null != map && map.size() > 0) {
					for (UserData userData : map.values()) {
						sum += userData.getGalaxy_income();
					}
				}
				// 总绩效
				data_total.put("profit_sum", sum);
			}
			
			// safePal5
			
			if (null != projectType && projectType.getValue().equals("DAPP_EXCHANGE_SAFEPAL5")) {
				double sum = 0;
				// 自己
				if (null != map && map.size() > 0) {
					for (UserData userData : map.values()) {
						sum += userData.getGalaxy_income();
					}
				}
				// 总绩效
				DecimalFormat df2 = new DecimalFormat("#.##");
				data_total.put("profit_sum", df2.format(sum));
			}
			
			
			// 交易所
			if (null == projectType || !projectType.getValue().contains("DAPP_EXCHANGE")) {
				double sum = 0;
				if (null != map && map.size() > 0) {
					for (UserData userData : map.values()) {
						sum += userData.getRecharge();
					}
				}
				// 总充值
				data_total.put("recharge_sum", sum);
			}
			
			// 加密用户名
			handleChilds(dataChilds);
			data_total.put("list", dataChilds);
			resultObject.setData(data_total);
		} catch (BusinessException e) {
			resultObject.setCode("402");
			resultObject.setMsg(e.getMessage());
		} catch (Throwable e) {
			resultObject.setCode("500");
			resultObject.setMsg("程序错误");
			logger.error("error:", e);
		}

		return resultObject;
	}
	
	/**
	 * 推广页面-IoeAI资金盘 定制
	 */
	@RequestMapping(action + "getPromoteIoeAi.action")
	public Object getPromoteIoeAi(HttpServletRequest request) {
		ResultObject resultObject = new ResultObject();
		resultObject = readSecurityContextFromSession(resultObject);
		if (!"0".equals(resultObject.getCode())) {
			return resultObject;
		}
		
		String page_no = request.getParameter("page_no");
		if (StringUtils.isNullOrEmpty(page_no) 
				|| !StringUtils.isInteger(page_no) || Integer.valueOf(page_no) <= 0) {
			page_no = "1";
		}
		
		int pageNo = Integer.valueOf(page_no);
		
		String partyId = getLoginPartyId();
		try {

			Map<String, Object> data = new HashMap<String, Object>();
			List<Map<String, Object>> dataChilds = new ArrayList<Map<String, Object>>();

			// 所有下级（1-N级）
			List<String> partyLists = userRecomService.findChildren(partyId);

			// 推广总人数
			data.put("total", partyLists.size());

			dataChilds = this.userDataService.getAllChildrenPaged(pageNo, 30, partyId);
			
			// 总业绩 - 所有下级的 收益之和
			double sum = 0;
			for (String id : partyLists) {
				Map<String, UserData> map = userDataService.cacheByPartyId(id);
				if (null != map && map.size() > 0) {
					for (UserData userData : map.values()) {
						sum += userData.getGalaxy_income();
					}
				}
			}
			
			// 总绩效
			data.put("profit_sum", sum);
			
			int ioeAiLevel = pledgeGalaxyConfigService.getIoeAiLevel(partyId);
			// -1 0 青铜级 1 白银级 2 黄金级 3 铂金级 4 钻石级
			data.put("ioeAiLevel", ioeAiLevel);
			
			// 加密用户名
			handleChilds(dataChilds);
			
			// 下级列表
			data.put("list", dataChilds);
			data.put("page_no", pageNo);
			resultObject.setData(data);
		} catch (BusinessException e) {
			resultObject.setCode("402");
			resultObject.setMsg(e.getMessage());
		} catch (Throwable e) {
			resultObject.setCode("500");
			resultObject.setMsg("程序错误");
			logger.error("error:", e);
		}

		return resultObject;
	}
	
	/**
	 * 交易所-数据总览-PC端
	 */
	@RequestMapping(action + "getPromoteData.action")
	public Object getPromoteData(HttpServletRequest request) {
		ResultObject resultObject = new ResultObject();
		resultObject = readSecurityContextFromSession(resultObject);
		if (!"0".equals(resultObject.getCode())) {
			return resultObject;
		}
		
		String partyId = getLoginPartyId();
		Map<String, String> dataMap = new HashMap<>();
		try {
			Date date = new Date();
			Date startTime = null;
			Date endTime = null;
			String type = request.getParameter("type");
			if (type.equals("day")) {
				startTime = DateUtils.getDayStart(DateUtils.addDate(date, 1));
				endTime = DateUtils.getDayEnd(DateUtils.addDate(date, 1));
			} else if (type.equals("week")) {
				startTime = DateUtil.getFirstDateOfWeek(date);
				endTime = DateUtil.getLastDateOfWeek(date);
			} else if (type.equals("month")) {
				startTime = DateUtil.getFirstDateOfMonth(date);
				endTime = DateUtil.getLastDateOfMonth(date);
			}
			System.out.println("推广数据总览 开始时间" + startTime);
			System.out.println("推广数据总览 结束时间" + endTime);
			dataMap = userDataService.getPromoteData(partyId, dataMap, startTime, endTime);
			
			Map<String, UserData> map = userDataService.cacheByPartyId(partyId);
			double sum = 0;
			if (null != map && map.size() > 0) {
				for (UserData userData : map.values()) {
					sum += userData.getRechargeRecom();
				}
			}
			
			dataMap.put("rechargeRecom", String.valueOf(sum));

			resultObject.setData(dataMap);
		} catch (BusinessException e) {
			resultObject.setCode("402");
			resultObject.setMsg(e.getMessage());
		} catch (Throwable e) {
			resultObject.setCode("500");
			resultObject.setMsg("程序错误");
			logger.error("error:", e);
		}
		
		return resultObject;
	}

	/**
	 * 加密用户名
	 */
	protected void handleChilds(List<Map<String, Object>> dataChilds) {
		for (Map<String, Object> data : dataChilds) {
			String username = data.get("username").toString();
			int length = username.length();
			if (username.length() > 2) {
				data.put("username", username.substring(0, 3) + "***" + username.substring(length - 3));
//				data.put("username", String.format("%s%s%s", username.substring(0, 1), securityLength(length - 2),
//						username.substring(length - 1)));
			}
		}
	}

	private String securityLength(int length) {
		if (length <= 0)
			return "";
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < length; i++) {
			sb.append("*");
		}
		return sb.toString();
	}
	
}
