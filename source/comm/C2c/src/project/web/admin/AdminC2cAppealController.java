package project.web.admin;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import project.c2c.AdminC2cAppealService;
import project.c2c.C2cAppeal;
import project.c2c.C2cOrder;
import project.c2c.C2cOrderService;
import security.Role;
import security.SecUser;
import security.internal.SecUserService;

/**
 * C2C申诉
 */
@RestController
public class AdminC2cAppealController extends PageActionSupport {

	private Logger logger = LoggerFactory.getLogger(AdminC2cAppealController.class);

	@Autowired
	protected AdminC2cAppealService adminC2cAppealService;
	@Autowired
	protected C2cOrderService c2cOrderService;
	@Autowired
	protected SecUserService secUserService;
	
	private final String action = "normal/adminC2cAppealAction!";

	/**
	 * 获取 C2C申诉 列表
	 */
	@RequestMapping(action + "list.action")
	public ModelAndView list(HttpServletRequest request) {
		String pageNoStr = request.getParameter("pageNo");
		String message = request.getParameter("message");
		String error = request.getParameter("error");
		String status_para = request.getParameter("status_para");
		String order_no_para = request.getParameter("order_no_para");
		String user_code_para = request.getParameter("user_code_para");		
		String rolename_para = request.getParameter("rolename_para");		
		String c2c_user_code_para = request.getParameter("c2c_user_code_para");		
		String c2c_user_type_para = request.getParameter("c2c_user_type_para");		
		String c2c_user_party_code_para = request.getParameter("c2c_user_party_code_para");	

		ModelAndView modelAndView = new ModelAndView();	
		modelAndView.setViewName("c2c_appeal_list");	

		int pageNo=1;
		Page page = null;
		int pageSize = 20;
		try {
			pageNo=checkAndSetPageNo(pageNoStr);
			
			Integer status_int = null;
			if (!StringUtils.isEmptyString(status_para)) {
				status_int = Integer.valueOf(status_para);
			}
			
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

			page = this.adminC2cAppealService.pagedQuery(pageNo, pageSize, status_int, order_no_para, user_code_para,
					rolename_para, c2c_user_code_para, c2c_user_type_int, c2c_user_party_code_para, secUuid);

			for (Map<String, Object> map : (List<Map<String, Object>>) page.getElements()) {
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
		modelAndView.addObject("status_para", status_para);
		modelAndView.addObject("order_no_para", order_no_para);
		modelAndView.addObject("user_code_para", user_code_para);
		modelAndView.addObject("rolename_para", rolename_para);
		modelAndView.addObject("c2c_user_code_para", c2c_user_code_para);
		modelAndView.addObject("c2c_user_type_para", c2c_user_type_para);
		modelAndView.addObject("c2c_user_party_code_para", c2c_user_party_code_para);
		return modelAndView;
	}
	
	/**
	 * 已处理
	 */
	@RequestMapping(action + "handled.action")
	public ModelAndView handled(HttpServletRequest request) {			
		String order_no = request.getParameter("order_no");
		
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.addObject("order_no", order_no);
		modelAndView.setViewName("redirect:/" + action + "list.action");
		
		try {
				
			C2cOrder order = this.c2cOrderService.get(order_no);
			if (null == order) {
				throw new BusinessException("订单不存在");
			}
			
			C2cAppeal appeal = this.adminC2cAppealService.get(order_no);
			if (null == appeal) {
				throw new BusinessException("申诉不存在");
			}
			
			this.adminC2cAppealService.handled(appeal, this.getUsername_login(), order.getPartyId());
				
		} catch (BusinessException e) {	
			modelAndView.addObject("error", e.getMessage());
			return modelAndView;
		} catch (Throwable t) {
			logger.error(" error ", t);
			modelAndView.addObject("error", "[ERROR] " + t.getMessage());
			return modelAndView;
		}
		
		modelAndView.addObject("message", "操作成功");
		return modelAndView;
	}
	
}
