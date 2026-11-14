package project.web.admin.controller.report;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import kernel.exception.BusinessException;
import kernel.util.DateUtils;
import kernel.util.StringUtils;
import kernel.web.Page;
import kernel.web.PageActionSupport;
import project.party.PartyService;
import project.user.UserDataService;
import project.web.admin.service.report.AdminUserAllStatisticsService;
import project.web.admin.service.user.AdminAgentService;

/**
 * 交易所_用户收益报表
 */
@RestController
public class AdminUserAllStatisticsController extends PageActionSupport {

	private Logger logger = LoggerFactory.getLogger(AdminUserAllStatisticsController.class);

	@Autowired
	protected AdminUserAllStatisticsService adminUserAllStatisticsService;
	@Autowired
	protected AdminAgentService adminAgentService;
	@Autowired
	protected UserDataService userDataService;
	@Autowired
	protected PartyService partyService;

	private final String action = "normal/adminUserAllStatisticsAction!";

	/**
	 * 获取 用户收益报表 列表
	 */
	@RequestMapping(action + "list.action")
	public ModelAndView list(HttpServletRequest request) {
		boolean no_agent_recom = Boolean.valueOf(request.getParameter("no_agent_recom")).booleanValue();
		String para_rolename = request.getParameter("para_rolename");
		boolean para_agent_view = Boolean.valueOf(request.getParameter("para_agent_view")).booleanValue();
		String start_time = request.getParameter("start_time");
		String end_time = request.getParameter("end_time");
		String para_username = request.getParameter("para_username");
		String para_party_id = request.getParameter("para_party_id");
		String sort_column = request.getParameter("sort_column");
		String sort_type = request.getParameter("sort_type");

		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("statistics_user_all_list");
		
		int pageNo = 1;
		int pageSize = 30;
		Page page = null;
		try {
			
			pageNo = this.checkAndSetPageNo(request.getParameter("pageNo"));
			
			if (StringUtils.isEmptyString(start_time) && StringUtils.isEmptyString(end_time)) {
				end_time = DateUtils.format(new Date(), DateUtils.DF_yyyyMMdd);
				start_time = DateUtils.format(DateUtils.getMonthStart(new Date()), DateUtils.DF_yyyyMMdd);
			}
			
			para_rolename = null;
			para_agent_view = false;

			if (no_agent_recom) {
				page = this.adminUserAllStatisticsService.pagedQueryNoAgentParent(pageNo, pageSize, start_time, end_time,
						this.getLoginPartyId(), para_username, para_rolename, para_party_id, para_agent_view, sort_column, sort_type);
			} else {				
				page = this.adminUserAllStatisticsService.pagedQuery(pageNo, pageSize, start_time, end_time,
						this.getLoginPartyId(), para_username, para_rolename, para_party_id, para_agent_view, sort_column, sort_type);				
			}

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
		modelAndView.addObject("no_agent_recom", no_agent_recom);
		modelAndView.addObject("para_rolename", para_rolename);
		modelAndView.addObject("para_agent_view", para_agent_view);
		modelAndView.addObject("start_time", start_time);
		modelAndView.addObject("end_time", end_time);
		modelAndView.addObject("para_username", para_username);
		modelAndView.addObject("para_party_id", para_party_id);
		modelAndView.addObject("sort_column", sort_column);
		modelAndView.addObject("sort_type", sort_type);
		return modelAndView;
	}

	/**
	 * 获取 用户收益报表 列表
	 */
	@RequestMapping(action + "exchangeList.action")
	public ModelAndView exchangeList(HttpServletRequest request) {
		String para_rolename = request.getParameter("para_rolename");
		boolean para_agent_view = Boolean.valueOf(request.getParameter("para_agent_view")).booleanValue();
		String start_time = request.getParameter("start_time");
		String end_time = request.getParameter("end_time");
		String para_username = request.getParameter("para_username");
		String para_party_id = request.getParameter("para_party_id");
		String sort_column = request.getParameter("sort_column");
		String sort_type = request.getParameter("sort_type");

		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("statistics_user_all_list");
		
		int pageNo = 1;
		int pageSize = 30;
		Page page = null;
		try {
			pageNo = this.checkAndSetPageNo(request.getParameter("pageNo"));
			
			if (StringUtils.isEmptyString(start_time) && StringUtils.isEmptyString(end_time)) {
				end_time = DateUtils.format(new Date(), DateUtils.DF_yyyyMMdd);
				start_time = DateUtils.format(DateUtils.getMonthStart(new Date()), DateUtils.DF_yyyyMMdd);
			}
			
			para_rolename = null;
			para_agent_view = false;

			page = adminUserAllStatisticsService.exchangePagedQuery(pageNo, pageSize, start_time, end_time, this.getLoginPartyId(),
					para_username, para_rolename, para_party_id, para_agent_view, sort_column, sort_type);

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
		modelAndView.addObject("para_rolename", para_rolename);
		modelAndView.addObject("para_agent_view", para_agent_view);
		modelAndView.addObject("start_time", start_time);
		modelAndView.addObject("end_time", end_time);
		modelAndView.addObject("para_username", para_username);
		modelAndView.addObject("para_party_id", para_party_id);
		modelAndView.addObject("sort_column", sort_column);
		modelAndView.addObject("sort_type", sort_type);
		return modelAndView;
	}
	
	
	/**
	 * 导出数据到文件
	 */
	@RequestMapping(action + "exportData.action")
	public ModelAndView exportData(HttpServletRequest request) {
		String pageNo = request.getParameter("pageNo");
		String para_rolename = request.getParameter("para_rolename");
		boolean para_agent_view = Boolean.valueOf(request.getParameter("para_agent_view")).booleanValue();
		String start_time = request.getParameter("start_time");
		String end_time = request.getParameter("end_time");
		String para_username = request.getParameter("para_username");
		String para_party_id = request.getParameter("para_party_id");
		String sort_column = request.getParameter("sort_column");
		String sort_type = request.getParameter("sort_type");

		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("statistics_user_all_list");

		try {
			this.checkAndSetPageNo(pageNo);

			if (StringUtils.isEmptyString(start_time) && StringUtils.isEmptyString(end_time)) {
				end_time = DateUtils.format(new Date(), DateUtils.DF_yyyyMMdd);
				start_time = DateUtils.format(DateUtils.getMonthStart(new Date()), DateUtils.DF_yyyyMMdd);
			}
			
			String error = this.adminUserAllStatisticsService.loadExportData(this.getResponse(), 30, start_time,
					end_time, this.getLoginPartyId(), para_username, para_rolename, para_party_id, para_agent_view,
					sort_column, sort_type);
			if (!StringUtils.isNullOrEmpty(error)) {
				throw new BusinessException(error);
			}

		} catch (BusinessException e) {
			modelAndView.addObject("error", e.getMessage());
			return modelAndView;
		} catch (IOException e) {
			logger.error("export fail:{}", e);
			modelAndView.addObject("error", "程序错误,导出异常");
			return modelAndView;
		} catch (Throwable t) {
			logger.error(" error ", t);
			modelAndView.addObject("error", "[ERROR] " + t.getMessage());
			return modelAndView;
		}

		return modelAndView;
	}

	/**
	 * 用户钱包 登录者只能看自己下面的用户钱包
	 */
	@RequestMapping(action + "walletExtendsAll.action")
	public ModelAndView walletExtendsAll(HttpServletRequest request) {
		String para_wallet_party_id = request.getParameter("para_wallet_party_id");

		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("statistics_user_all_money");

		try {
			
			List<Map<String, Object>> wallet_data = this.adminUserAllStatisticsService.getWalletExtends(this.getLoginPartyId(), para_wallet_party_id);
			List<String> types = new ArrayList<String>();
			modelAndView.addObject("wallet_data", wallet_data);
			if(ObjectUtils.isNotEmpty(wallet_data)) {
				for(Map<String, Object> walletMap :wallet_data) {
					types.add(walletMap.get("wallettype").toString());
				}
			}
			String wallettype = String.join(",", types);
			modelAndView.addObject("wallet_type_arr", wallettype);
			
		} catch (BusinessException e) {
			modelAndView.addObject("error", e.getMessage());
			return modelAndView;
		} catch (Throwable t) {
			logger.error(" error ", t);
			modelAndView.addObject("error", "[ERROR] " + t.getMessage());
			return modelAndView;
		}

		return modelAndView;
	}

	/**
	 * 用户资产 登录者只能看自己下面的用户资产
	 */
	@RequestMapping(action + "assetsAll.action")
	public ModelAndView assetsAll(HttpServletRequest request) {
		String para_wallet_party_id = request.getParameter("para_wallet_party_id");

		ModelAndView modelAndView = new ModelAndView();

		try {
			
			List<Map<String, Object>> asset_data = adminUserAllStatisticsService.getAssetsAll(this.getLoginPartyId(), para_wallet_party_id);

			modelAndView.addObject("asset_data", asset_data);
			
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

		modelAndView.setViewName("statistics_user_all_asset");
		return modelAndView;
	}

//	protected void initTime() {
//		if (null == start_time && null == end_time) {
//			this.end_time = DateUtils.format(new Date(), DateUtils.DF_yyyyMMdd);
//			this.start_time = DateUtils.format(DateUtils.getMonthStart(new Date()), DateUtils.DF_yyyyMMdd);
//		}
//	}

//	protected void roleMap() {
//		role_map.put(Constants.SECURITY_ROLE_MEMBER, "正式用户");
//		role_map.put(Constants.SECURITY_ROLE_AGENT, "代理商");
//		role_map.put(Constants.SECURITY_ROLE_AGENTLOW, "代理商");
//	}

}
