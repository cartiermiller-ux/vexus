package project.web.admin.security;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.providers.encoding.PasswordEncoder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.ModelAndView;

import kernel.exception.BusinessException;
import kernel.util.StringUtils;
import kernel.web.BaseAction;
import project.Constants;
import project.syspara.Syspara;
import project.syspara.SysparaService;
import project.user.googleauth.GoogleAuthService;
import project.user.token.Token;
import project.user.token.TokenService;
import security.Resource;
import security.Role;
import security.RoleService;
import security.SecUser;
import security.SecurityContext;
import security.internal.SecUserService;

/**
 * 登录相关接口
 */
@RestController
public class LoginController extends BaseAction {
	
	@Autowired
    private RoleService roleService;
	
	@Autowired
	private TokenService tokenService;
	
	@Autowired
	private SecUserService secUserService;
	
	@Autowired
	private SysparaService sysparaService;
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@Autowired
	private GoogleAuthService googleAuthService;
	
	private static final Logger logger=LoggerFactory.getLogger(LoginController.class);
	
	@RequestMapping(value = "public/login.action") 
	public ModelAndView login(HttpServletRequest request) {
		ModelAndView modelAndView = new ModelAndView();
		
		//谷歌验证码为空则返回登录页面
	    String googleAuthCode = request.getParameter("googleAuthCode");
	    if (StringUtils.isNullOrEmpty(googleAuthCode)) {
			modelAndView.setViewName("login");
		    return modelAndView;
	    }
	    
	    //用户名为空则返回登录页面
	    String username = request.getParameter("j_username");
	    if (null==username || (username=username.replaceAll("\\s*", "").trim()).isEmpty()) {
	    	modelAndView.addObject("error","用户名不能为空!");
			modelAndView.setViewName("login");
		    return modelAndView;
		}
	    
	    //密码为空则返回登录页面
	    String password = request.getParameter("j_password");
	    if (null==password || (password=password.replaceAll("\\s*", "").trim()).isEmpty()) {
	    	modelAndView.addObject("error","密码不能为空!");
			modelAndView.setViewName("login");
		    return modelAndView;
		}
	    
	    //谷歌验证码不正确则返回登录页面
	    Syspara para = sysparaService.find("open_google_auth_code");
		if (null == para || "true".equals(para.getValue())) {
		    try {
		    	googleAuthService.checkGoogleAuthCodeForLogin(this.getIp(), username, googleAuthCode, getRequest().getRequestURI());
		    } catch (BusinessException e) {
		    	modelAndView.addObject("error", e.getMessage());
				modelAndView.setViewName("login");
			    return modelAndView;
			} catch (Throwable e) {
				modelAndView.addObject("error", e.getMessage());
				modelAndView.setViewName("login");
			    return modelAndView;
			}
		}
		
		//用户名错误(找不到用户)或用户无权限则返回登录页面
		SecUser user = this.secUserService.findValidUserByLoginName(username,loginRoles());
		if (null==user) {
			modelAndView.addObject("error", "未找到用户或用户无权限!");
			modelAndView.setViewName("login");
		    return modelAndView;
		}
		
		//密码校验错误返回登录页面
		String md5 = passwordEncoder.encodePassword(password, user.getUsername());
		if (!user.getPassword().equals(md5)) {
			modelAndView.addObject("error", "登录密码错误!");
			modelAndView.setViewName("login");
		    return modelAndView;
		}
		
		//保存客户端会话数据
		SecurityContext securityContext = new SecurityContext();
		securityContext.setPartyId(user.getPartyId());
		securityContext.setPrincipal(user);
		securityContext.setUsername(user.getUsername());
		Iterator<Role> it = user.getRoles().iterator();
		while (it.hasNext()) securityContext.getRoles().add("ROLE_"+it.next().getRoleName());
		request.getSession().setAttribute("SPRING_SECURITY_CONTEXT", securityContext);

		//更新Token存储
	    onlineChatToken(user);
	    
	    //修改用户登录状态
		loginIpRecord(user);
		
		//重定向到主页
		modelAndView.setViewName("redirect:/normal/LoginSuccessAction!view.action");
	    return modelAndView;
	} 
	
	/**
	 * 获取角色集
	 * @return 角色数组
	 */
	private String[] loginRoles() {
		List<Role> roleList=roleService.getAll();
		
		if(null==roleList) return null;
		if(0==roleList.size()) return new String[0];
		
		List<String> roleNames=roleList.stream()
				.filter(role->{String roleName=role.getRoleName();return !Constants.SECURITY_ROLE_MEMBER.equals(roleName) && !Constants.SECURITY_ROLE_GUEST.equals(roleName);})
				.map(role->role.getRoleName()).collect(Collectors.toList());
		
		return null==roleNames?null:roleNames.toArray(new String[roleNames.size()]);
	}
	
	/**
	 * 更新用户Token值
	 * @param user 用户对象
	 */
	private void onlineChatToken(SecUser user) {
		try {
			Set<Role> roles = user.getRoles();
			for (Role role : roles) {
				for (Resource resource : role.getResources()) {
					if ("OP_ADMIN_ONLINECHAT".equals(resource.getId().toString())) {
						tokenService.savePut(user.getUsername(),user.getUsername());
						return;
					}
				}
			}
			
			Token token = tokenService.find(user.getUsername());
			if (token != null) tokenService.delete(token.getToken());
		} catch (Exception e) {
			logger.error("online chat token fail ,username:" + user.getUsername() + ",e:", e);
		}
	}

	/**
	 * 更新用户登录状态
	 * @param user 用户对象
	 */
	private void loginIpRecord(SecUser user) {
		String ip = getIp(((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest());
		
		//代理商、root用户、普通用户登录IP等于实际IP
		if (!StringUtils.isEmptyString(user.getPartyId()) || ip.equals(user.getLogin_ip())) {
			user.setLogin_ip(ip);
			user.setLast_loginTime(new Date());
			this.secUserService.update(user);
		}
	}
}
