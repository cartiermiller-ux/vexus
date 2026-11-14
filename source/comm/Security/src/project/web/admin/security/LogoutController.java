package project.web.admin.security;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import kernel.web.BaseAction;

/**
 * 后台管理系统退出登录
 */
@RestController
public class LogoutController extends BaseAction {

	@RequestMapping(value = "public/logout.action") 
	public ModelAndView Logout(HttpServletRequest request) {
		request.getSession().setAttribute("SPRING_SECURITY_CONTEXT", null);
		ModelAndView model = new ModelAndView();
		model.setViewName("login");
		return model;
	}
}
