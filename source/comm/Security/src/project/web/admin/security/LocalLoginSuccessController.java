package project.web.admin.security;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import kernel.util.DateUtils;
import kernel.util.StringUtils;
import project.Constants;
import project.log.Log;
import project.log.LogService;
import security.web.LoginSuccessAction;

@RestController
public class LocalLoginSuccessController extends LoginSuccessAction {

	@Autowired
	private LogService logService;

	@RequestMapping(value = "normal/LoginSuccessAction!view.action") 
	public ModelAndView loginSuccess() {
		ModelAndView model = new ModelAndView();

		String cookie_username = this.getCookie(COOKIE_USERNAME_NAME);
		if (!StringUtils.isNullOrEmpty(cookie_username) && cookie_username.length() >= 4000) {
			cookie_username = cookie_username.substring(0, 3999);
		}
		
		String partyId = this.getLoginPartyId();
		
		Log log = new Log();
		log.setCategory(Constants.LOG_CATEGORY_SECURITY);
		log.setLog("登录系统，ip[" + this.getIp(getRequest()) + "]");
		log.setPartyId(partyId);
		log.setUsername(this.getUsername_login());
		logService.saveAsyn(log);
	
		model.addObject("username_login", this.getUsername_login());
		model.setViewName("auto_monitor_iframe");
		return model;
	}
}
