package project.web.admin.monitor.user;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import kernel.exception.BusinessException;
import kernel.web.ApplicationUtil;
import kernel.web.Page;
import kernel.web.PageActionSupport;
import project.Constants;
import project.log.AdminLogService;
import project.log.MoneyLog;
import project.log.MoneyLogService;

/**
 * 账变记录
 */
@Controller
public class AdminMoneyLogController extends PageActionSupport {

	private Logger logger = LoggerFactory.getLogger(AdminMoneyLogController.class);
	
	@Autowired
	private AdminLogService adminLogService;
	
	private final String action = "normal/adminMoneyLogAction!";

	/**
	 * 获取 账变记录 列表
	 */
	@RequestMapping(action + "list.action")
	public ModelAndView list(HttpServletRequest request) {
		String log_para = request.getParameter("log_para");
		String rolename_para = request.getParameter("rolename_para");
		String start_time = request.getParameter("start_time");
		String end_time = request.getParameter("end_time");
		String name_para = request.getParameter("name_para");

		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("moneylog_list");
		
		int pageNo = 1;
		int pageSize = 30;
		Page page = null;
		try {
			pageNo = this.checkAndSetPageNo(request.getParameter("pageNo"));

			String loginPartyId = this.getLoginPartyId();
			page = this.adminLogService.pagedQueryMoneyLog(pageNo, pageSize, log_para, name_para,
					loginPartyId, rolename_para, start_time, end_time);

			List<Map<String, Object>> list = (List<Map<String, Object>>) page.getElements();
			for (int i = 0; i < list.size(); i++) {
				Map<String, Object> map = list.get(i);
				map.put("amount", new BigDecimal(map.get("amount").toString()).toPlainString());
				map.put("amount_before", new BigDecimal(map.get("amount_before").toString()).toPlainString());
				map.put("amount_after", new BigDecimal(map.get("amount_after").toString()).toPlainString());
				if("0".equals(map.get("showStatus"))) {
					map.put("showStatus", "否");
				}else if("1".equals(map.get("showStatus"))) {
					map.put("showStatus", "是");
				}
				if (null == map.get("rolename")) {
					map.put("roleNameDesc", "");
				} else {
					String roleName = map.get("rolename").toString();
					map.put("roleNameDesc", Constants.ROLE_MAP.containsKey(roleName) ? Constants.ROLE_MAP.get(roleName) : roleName);
				}
			}
			
		} catch (BusinessException e) {
			this.error = e.getMessage();
			modelAndView.addObject("error", this.error);
			return modelAndView;
		} catch (Throwable t) {
			logger.error(" error ", t);
			this.error = ("[ERROR] " + t.getMessage());
			modelAndView.addObject("error", this.error);
			return modelAndView;
		}
		
		modelAndView.addObject("pageNo", pageNo);
		modelAndView.addObject("pageSize", pageSize);
		modelAndView.addObject("page", page);
		modelAndView.addObject("log_para", log_para);
		modelAndView.addObject("rolename_para", rolename_para);
		modelAndView.addObject("start_time", start_time);
		modelAndView.addObject("end_time", end_time);
		modelAndView.addObject("name_para", name_para);
		return modelAndView;
	}	
	
	/**
	 * 更新账变状态
	 * @return 账变列表
	 */
	@RequestMapping(action+"updateStatus.action")
	public ModelAndView updateStatus(HttpServletRequest request) {
		ModelAndView modelAndView = new ModelAndView();	
		String orderId = request.getParameter("orderId");
		String statusStr = request.getParameter("statusStr");
		
		try {
			MoneyLogService moneyLogService =ApplicationUtil.getBean(MoneyLogService.class);
			MoneyLog moneyLog = new MoneyLog();
			moneyLog.setId(orderId);
			moneyLog.setShowStatus(statusStr);
			moneyLogService.updateStatus(moneyLog);
			modelAndView.addObject("message", "操作成功");
		}catch(BusinessException e) {
			modelAndView.addObject("message", e.getMessage());
			logger.error("change loan order state error:", e);
		}catch(Throwable e) {
			modelAndView.addObject("message", "操作失败");
			logger.error("change loan order state error:", e);
		}
		
		modelAndView.setViewName("redirect:/" + action + "list.action");
		return modelAndView;
	}

}
