package project.web.admin;

import java.util.Date;

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
import project.ddos.AdminIpMenuService;
import project.ddos.IpMenuService;
import project.ddos.model.IpMenu;

/**
 * IP名单管理
 */
@RestController
public class AdminIpMenuController extends PageActionSupport {

	private Logger logger = LoggerFactory.getLogger(AdminIpMenuController.class);

	@Autowired
	private AdminIpMenuService adminIpMenuService;
	@Autowired
	private IpMenuService ipMenuService;
	
	private final String action = "normal/adminIpMenuAction!";

	/**
	 * 获取 IP名单 列表
	 */
	@RequestMapping(action + "list.action")
	public ModelAndView list(HttpServletRequest request) {
		String message = request.getParameter("message");
		String error = request.getParameter("error");
		String type_para = request.getParameter("type_para");
		String ip_para = request.getParameter("ip_para");

		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("ip_menu_list");

		int pageNo = 1;
		int pageSize = 50;
		Page page = null;
		try {
			pageNo = checkAndSetPageNo(request.getParameter("pageNo"));
			
			if (StringUtils.isEmptyString(type_para)) {
				type_para = "white";
			}
			
			page = this.adminIpMenuService.pagedQuery(pageNo, pageSize, ip_para, type_para);

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
		modelAndView.addObject("type_para", type_para);
		modelAndView.addObject("ip_para", ip_para);
		return modelAndView;
	}

	/**
	 * 新增 IP名单 页面
	 */
	@RequestMapping(action + "toAdd.action")
	public ModelAndView toAdd(HttpServletRequest request) {
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("ip_menu_add");
		return modelAndView;
	}

	/**
	 * 新增 IP名单
	 */
	@RequestMapping(action + "add.action")
	public ModelAndView add(HttpServletRequest request) {
		String menu_ip = request.getParameter("menu_ip");	
		String menu_type = request.getParameter("menu_type");
		String login_safeword = request.getParameter("login_safeword");	

		ModelAndView modelAndView = new ModelAndView();
		
		try {
			
			this.check();
			
			String error = this.verif_add(menu_ip, menu_type);
			if (!StringUtils.isNullOrEmpty(error)) {
				throw new BusinessException(error);
			}
			
			if (StringUtils.isNullOrEmpty(login_safeword)) {
				throw new BusinessException("请输入登录人资金密码");
			}

			IpMenu ipMenu = new IpMenu();
			ipMenu.setIp(menu_ip);
			ipMenu.setLast_opera_time(new Date());
			ipMenu.setCreate_time(new Date());
			ipMenu.setDelete_status(0);
			ipMenu.setType(menu_type);
			
			// 补充设值
			this.adminIpMenuService.save(ipMenu, this.getUsername_login(), login_safeword, this.getIp());
			
		} catch (BusinessException e) {
			modelAndView.addObject("error", e.getMessage());
			modelAndView.addObject("menu_ip", menu_ip);
			modelAndView.addObject("menu_type", menu_type);
			modelAndView.setViewName("ip_menu_add");
			return modelAndView;
		} catch (Throwable t) {
			logger.error(" error ", t);
			modelAndView.addObject("error", "[ERROR] " + t.getMessage());
			modelAndView.addObject("menu_ip", menu_ip);
			modelAndView.addObject("menu_type", menu_type);
			modelAndView.setViewName("ip_menu_add");
			return modelAndView;
		}

		modelAndView.addObject("message", "操作成功");
		modelAndView.setViewName("redirect:/" + action + "list.action");
		return modelAndView;
	}

	/**
	 * 修改 IP名单 页面
	 */
	@RequestMapping(action + "toUpdate.action")
	public ModelAndView toUpdate(HttpServletRequest request) {
		String menu_ip = request.getParameter("menu_ip");

		ModelAndView modelAndView = new ModelAndView();

		try {
			
			this.check();
			
			IpMenu cacheByIp = this.ipMenuService.cacheByIp(menu_ip);

			modelAndView.addObject("menu_ip", cacheByIp.getIp());
			modelAndView.addObject("menu_type", cacheByIp.getType());
			
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
		
		modelAndView.setViewName("ip_menu_update");
		return modelAndView;
	}

	/**
	 * 修改 IP名单
	 */
	@RequestMapping(action + "update.action")
	public ModelAndView update(HttpServletRequest request) {
		String menu_ip = request.getParameter("menu_ip");		
		String menu_type = request.getParameter("menu_type");		
		String login_safeword = request.getParameter("login_safeword");

		ModelAndView modelAndView = new ModelAndView();
		
		try {
			
			this.check();
			
			String error = this.verif_add(menu_ip, menu_type);
			if (!StringUtils.isNullOrEmpty(error)) {
				throw new BusinessException(error);
			}
			
			IpMenu ipMenu = new IpMenu();
			ipMenu.setIp(menu_ip);
			ipMenu.setLast_opera_time(new Date());
			ipMenu.setDelete_status(0);
			ipMenu.setType(menu_type);

			this.adminIpMenuService.update(ipMenu, this.getUsername_login(), login_safeword, this.getIp());
						
		} catch (BusinessException e) {
			modelAndView.addObject("error", e.getMessage());
			modelAndView.addObject("menu_ip", menu_ip);
			modelAndView.addObject("menu_type", menu_type);
			modelAndView.setViewName("ip_menu_update");
			return modelAndView;
		} catch (Throwable t) {
			logger.error(" error ", t);
			modelAndView.addObject("error", "[ERROR] " + t.getMessage());
			modelAndView.addObject("menu_ip", menu_ip);
			modelAndView.addObject("menu_type", menu_type);
			modelAndView.setViewName("ip_menu_update");
			return modelAndView;
		}
		
		modelAndView.addObject("message", "操作成功");
		modelAndView.setViewName("redirect:/" + action + "list.action");
		return modelAndView;
	}

	/**
	 * 删除 IP名单 页面
	 */
	@RequestMapping(action + "toDelete.action")
	public ModelAndView toDelete(HttpServletRequest request) {
		String menu_ip = request.getParameter("menu_ip");
		String login_safeword = request.getParameter("login_safeword");

		ModelAndView modelAndView = new ModelAndView();

		try {
			
			this.check();
			
			this.adminIpMenuService.delete(menu_ip, this.getUsername_login(), login_safeword, this.getIp());
						
		} catch (BusinessException e) {
			modelAndView.addObject("error", e.getMessage());
			modelAndView.setViewName("redirect:/" + action + "list.action");
			return modelAndView;
		} catch (Throwable t) {
			logger.error("update error ", t);
			modelAndView.addObject("error", "程序错误");
			modelAndView.setViewName("redirect:/" + action + "list.action");
			return modelAndView;
		}
		
		modelAndView.addObject("message", "操作成功");
		modelAndView.setViewName("redirect:/" + action + "list.action");
		return modelAndView;
	}

	private void check() {
		String loginUserName = this.getUsername_login();
		if (!("root".equals(loginUserName))) {
			throw new BusinessException("权限不足，无法操作");
		}
	}

	private String verif_add(String menu_ip, String menu_type) {
		if (StringUtils.isEmptyString(menu_ip)) {
			return "请输入[IP]";
		}
		if (StringUtils.isEmptyString(menu_type)) {
			return "请选择[名单]";
		}
		return null;
	}

}
