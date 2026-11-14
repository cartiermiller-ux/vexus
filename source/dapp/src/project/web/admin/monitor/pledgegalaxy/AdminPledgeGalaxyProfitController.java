package project.web.admin.monitor.pledgegalaxy;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import kernel.exception.BusinessException;
import kernel.util.StringUtils;
import kernel.web.Page;
import kernel.web.PageActionSupport;
import project.Constants;
import project.log.LogService;
import project.monitor.AdminPledgeGalaxyProfitService;
import project.party.PartyService;

/**
 * 质押2.0收益单
 */
@RestController
public class AdminPledgeGalaxyProfitController extends PageActionSupport {

	private Logger logger = LoggerFactory.getLogger(AdminPledgeGalaxyProfitController.class);

	@Autowired
	protected AdminPledgeGalaxyProfitService adminPledgeGalaxyProfitService;
	@Autowired
	protected PartyService partyService;
	@Autowired
	protected LogService logService;
	
	private final String action = "normal/adminPledgeGalaxyProfitAction!";

	/**
	 * 获取 质押2.0收益单 列表
	 */
	@RequestMapping(value = action + "list.action") 
	public ModelAndView list(HttpServletRequest request) {
		String message = request.getParameter("message");
		String error = request.getParameter("error");
		String order_no_para = request.getParameter("order_no_para");
		String relation_order_no_para = request.getParameter("relation_order_no_para");
		String start_time = request.getParameter("start_time");
		String end_time = request.getParameter("end_time");
		String name_para = request.getParameter("name_para");
		String rolename_para = request.getParameter("rolename_para");
		String status_para = request.getParameter("status_para");

		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("auto_monitor_pledge_galaxy_profit_list");

		int pageNo = 1;
		int pageSize = 30;
		Page page = null;
		try {
			pageNo = this.checkAndSetPageNo(request.getParameter("pageNo"));
			Integer status_para_int = null;
			
			if (StringUtils.isNullOrEmpty(status_para)) {
				status_para_int = null;
			} else {
				status_para_int = Integer.valueOf(status_para);
			}
			
			page = this.adminPledgeGalaxyProfitService.pagedQuery(pageNo, pageSize, order_no_para, relation_order_no_para, start_time, end_time, name_para, rolename_para, 
					status_para_int, this.getLoginPartyId());
			
			for(Map map : (List<Map>) page.getElements()) {
				
				if (null == map.get("rolename")) {
					map.put("roleNameDesc", "");
				} else {
					String roleName = map.get("rolename").toString();
					map.put("roleNameDesc", Constants.ROLE_MAP.containsKey(roleName) ? Constants.ROLE_MAP.get(roleName) : roleName);
				}
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
		modelAndView.addObject("message", message);
		modelAndView.addObject("error", error);
		modelAndView.addObject("order_no_para", order_no_para);
		modelAndView.addObject("relation_order_no_para", relation_order_no_para);
		modelAndView.addObject("start_time", start_time);
		modelAndView.addObject("end_time", end_time);
		modelAndView.addObject("name_para", name_para);
		modelAndView.addObject("rolename_para", rolename_para);
		return modelAndView;
	}

	/**
	 * 审核通过
	 */
	@RequestMapping(action + "savePassed.action")
	public ModelAndView savePassed(HttpServletRequest request) {
		String partyId = request.getParameter("partyId");

		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("redirect:/" + action + "list.action");
		
		try {
			
			this.adminPledgeGalaxyProfitService.saveReceiveApply(partyId, "", true);
			
		} catch (BusinessException e) {
			modelAndView.addObject("error", e.getMessage());
			return modelAndView;
		} catch (Throwable t) {
			logger.error("update error ", t);
			modelAndView.addObject("error", "程序错误");
			return modelAndView;
		}
		
		modelAndView.addObject("message", "操作成功");
		return modelAndView;
	}

	/**
	 * 驳回
	 */
	@RequestMapping(action + "saveFailed.action")
	public ModelAndView saveFailed(HttpServletRequest request) {
		String partyId = request.getParameter("partyId");
		String msg = request.getParameter("msg");

		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("redirect:/" + action + "list.action");
		
		try {
			
			this.adminPledgeGalaxyProfitService.saveReceiveApply(partyId, msg, false);
			
		} catch (BusinessException e) {
			modelAndView.addObject("error", e.getMessage());
			return modelAndView;
		} catch (Throwable t) {
			logger.error("update error ", t);
			modelAndView.addObject("error", "程序错误");
			return modelAndView;
		}
		
		modelAndView.addObject("message", "操作成功");
		return modelAndView;
	}
	
	/**
	 * 人工补收益
	 */
	@RequestMapping(action + "addProfit.action")
	public ModelAndView addProfit(HttpServletRequest request) {
		String message = "";
		String error = "";
		try {
			if(!"root".equals(this.getUsername_login())) {
				throw new BusinessException("权限不足");
			}
			String time = request.getParameter("time");
			if(StringUtils.isEmptyString(time)) {
				throw new BusinessException("请填入系统时间");
			}
			String type = request.getParameter("type");
			if (type.equals("")) {
				// 静态及动态收益
				adminPledgeGalaxyProfitService.saveProfit(time);
			} else if (type.equals("")) {
				// 团队收益
				adminPledgeGalaxyProfitService.saveTeamProfit(time);
			}
			
			message = "操作成功";
		} catch (BusinessException e) {
			error = e.getMessage();
		} catch (Exception e) {
			logger.error("error ", e);
			error = "程序错误";
		}
		ModelAndView model = new ModelAndView();
		model.addObject("message", message);
		model.addObject("error", error);
		return model;
	}

}
