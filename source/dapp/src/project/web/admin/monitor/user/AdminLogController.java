package project.web.admin.monitor.user;

import java.util.Date;
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
import kernel.util.StringUtils;
import kernel.web.Page;
import kernel.web.PageActionSupport;
import project.Constants;
import project.log.AdminLogService;

/**
 * 操作日志
 */
@Controller
public class AdminLogController extends PageActionSupport {
	
	private Logger logger = LoggerFactory.getLogger(AdminLogController.class);
	
	@Autowired
	private AdminLogService adminLogService;
	
	private final String action = "normal/adminLogAction!";

	/**
	 * 获取 操作日志 列表
	 */
	@RequestMapping(action + "list.action")
	public ModelAndView list(HttpServletRequest request) {
		String log_para = request.getParameter("log_para");
		String date_para = request.getParameter("date_para");
		String name_para = request.getParameter("name_para");
		String category = request.getParameter("category");
		String operator = request.getParameter("operator");
		String rolename_para = request.getParameter("rolename_para");

		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("log_list");
		
		int pageNo = 1;
		int pageSize = 30;
		Page page = null;
		try {
			pageNo = this.checkAndSetPageNo(request.getParameter("pageNo"));
			
			String loginPartyId = this.getLoginPartyId();
			if (!StringUtils.isNullOrEmpty(loginPartyId)) {
				category = Constants.LOG_CATEGORY_OPERATION;
			}

			List<Date> date_range = toRangeDate(date_para);
			page = this.adminLogService.pagedQueryLog(pageNo, pageSize, log_para, name_para, category,
					operator, date_range.get(0), date_range.get(1), loginPartyId, this.getUsername_login(), rolename_para);

			List<Map<String, Object>> list = (List<Map<String, Object>>) page.getElements();
			for (int i = 0; i < list.size(); i++) {
				Map<String, Object> map = list.get(i);
				
				if (null == map.get("rolename")) {
					map.put("roleNameDesc", "");
				} else {
					String roleName = map.get("rolename").toString();
					map.put("roleNameDesc", Constants.ROLE_MAP.containsKey(roleName) ? Constants.ROLE_MAP.get(roleName) : roleName);
				}
			}

			modelAndView.addObject("category_map", Constants.LOG_CATEGORY);
			
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
		modelAndView.addObject("date_para", date_para);
		modelAndView.addObject("name_para", name_para);
		modelAndView.addObject("category", category);
		modelAndView.addObject("operator", operator);
		modelAndView.addObject("rolename_para", rolename_para);
		return modelAndView;
	}

}
