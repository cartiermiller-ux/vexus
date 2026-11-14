package project.web.admin.filter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

import kernel.util.DateUtils;
import kernel.util.StringUtils;
import kernel.web.PageActionSupport;
import project.syspara.Syspara;
import project.syspara.SysparaService;
import security.SecUser;
import security.SecurityAppUserHolder;
import security.SecurityContext;
import security.internal.SecUserService;
import util.IpUtil;

public class AllRequestFilter extends PageActionSupport implements Filter  {

	private Logger logger = LoggerFactory.getLogger(AllRequestFilter.class);
	
	/**
	 * url 白名单
	 */
	private List<String> urls = new ArrayList<String>();
	/**
	 * 操作不打日志url
	 */
	private List<String> opNoLogUrls = new ArrayList<>();
	
	/**
	 * 不校验IP变化--临时。等改版后IP此处校验可有可无
	 */
	private List<String> noCheckIPUrls = new ArrayList<>();
	
	@Override
	public void destroy() {
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
			throws IOException, ServletException {
		
		ServletRequest oldRequest = request;
		ServletResponse oldResponse = response;
		HttpServletRequest httpServletRequest = (HttpServletRequest)request;
		
		WebApplicationContext wac = ContextLoader.getCurrentWebApplicationContext();
		SysparaService sysparaService =(SysparaService) wac.getBean("sysparaService");
		
		// 检查IP黑名单
		Syspara blackListSyspara = sysparaService.find("blacklist_ip");
		String requestIP = this.getIp(httpServletRequest);
		if(blackListSyspara != null && !StringUtils.isEmptyString(blackListSyspara.getValue())) {
			if (!checkBlackListIP(blackListSyspara.getValue(), requestIP)) {
				logger.error("黑名单IP,参数{}", requestIP);
				return;
			}
		}
		
		String ip = this.getIp(httpServletRequest);
		if (!IpUtil.isCorrectIpRegular(ip)) {
			logger.error("校验IP不合法,参数{}", ip);
			return;
		}
		
		// 白名单直接过滤，非action请求直接过滤
		String servletPath = httpServletRequest.getServletPath();
		if(urls.contains(servletPath) || !".action".equals(servletPath.substring(servletPath.length()-7))) {
			filterChain.doFilter(oldRequest, oldResponse);
			return;
		}
		
		SecUserService secUserService =(SecUserService) wac.getBean("secUserService");
		Syspara syspara = sysparaService.find("filter_ip");
		
		String usernameLogin = getUsername_login(httpServletRequest);
		if(StringUtils.isEmptyString(usernameLogin)) {//未登录时不操作
			filterChain.doFilter(oldRequest, oldResponse);
			return;
		}
		SecUser secUser = secUserService.findUserByLoginName(usernameLogin);
		if(!StringUtils.isEmptyString(secUser.getPartyId())) {//代理商不验证
			filterChain.doFilter(oldRequest, oldResponse);
			return;
		}
		
		if(syspara != null && !StringUtils.isEmptyString(syspara.getValue())) {
			if(!noCheckIPUrls.contains(httpServletRequest.getServletPath())) {
				checkIP(syspara,request);
			}
		}
		
		Syspara para = sysparaService.find("open_google_auth_code");
		if (null == para || para.getValue().equals("true")) {
			if(checkOperaIp(httpServletRequest, response, secUser)) {
				// 不记录日志直接返回
				if(opNoLogUrls.contains(httpServletRequest.getServletPath())) {
					return ;
				}
				RequestDispatcher requestDispatcher = request.getRequestDispatcher("/include/google_auth_code.jsp"); 
				request.setAttribute("check_opera_ip", "ture");
				request.setAttribute("username", getUsername_login(httpServletRequest));
				requestDispatcher.forward(request, response); 
				return;
			}
		}
		
// 加了加密签名，这个可以不用了
//		if (checkParameter(httpServletRequest)) {
//			return;
//		}
		
		filterChain.doFilter(oldRequest, oldResponse);
	}
//	
//	/**
//	 * 请求参数中包含"script"的过滤
//	 */
//	private boolean checkParameter(HttpServletRequest request) {
//		
//		Enumeration<String> enu = request.getParameterNames();
//		while (enu.hasMoreElements()) {
//			String paraName = (String) enu.nextElement();
//			String value = request.getParameter(paraName).toLowerCase();
//			if (value.indexOf("script") != -1) {
//				System.out.println("请求参数中包含script的过滤,参数：" + request.getParameter(paraName) + "请求地址：" + request.getServletPath());
//				return true;
//			}
//		}
//		
//		Enumeration heads = request.getHeaderNames();
//		while (heads.hasMoreElements()) {
//			String headName = String.valueOf(heads.nextElement());
//			String value = request.getHeader(headName).toLowerCase();
//			if (value.indexOf("<script") != -1) {
//				System.out.println("head参数中包含script的过滤,参数：" + request.getHeader(headName) + "请求地址：" + request.getServletPath());
//				return true;
//			}
//		}
//		
//		return false;
//	}
	
	/**
	 * 验证是否是白名单
	 */
	private void checkIP(Syspara syspara,ServletRequest request) {
		HttpServletRequest httpServletRequest = (HttpServletRequest)request;
		String loginIp = this.getIp(httpServletRequest);
		String[] loginIpParts = loginIp.split("\\.");
		String ips = syspara.getValue();
		String[] ipsArrs = ips.split(",");
		//[192.188.1.*,192.188.2.*]
		int index=0;
		for(String ip:ipsArrs) {
			String[] ipParts = ip.split("\\.");
			for (int i = 0; i < ipParts.length; i++) {
				if(ipParts[i].equals(loginIpParts[i])||"*".equals(ipParts[i])) {//匹配
					index++;
				}else {//不匹配
					break;
				}
			}
			if(index==4) {//存在完全匹配的ip地址池
				break;
			}else {//每次和新的地址匹配都重置
				index=0;
			}
		}
		if(index!=4) {//全部地址池匹配完，没有与登录ip相符的
			logger.info("filter fail,time:{},ip:{},request uri:{}",
					new Object[]{DateUtils.dateToStr(new Date(), DateUtils.DF_yyyyMMddHHmmss),loginIp,httpServletRequest.getRequestURI()});
            throw new RuntimeException();
		}
	}
	
	/**
	 * 验证是否是黑名单
	 */
	private boolean checkBlackListIP(String ips, String requestIP) {
		String[] ipsArrs = ips.split(",");
		for(String ip : ipsArrs) {
			if(requestIP.equals(ip)) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * 验证操作的ip和登录的是否相同
	 */
	private boolean checkOperaIp(HttpServletRequest httpServletRequest, ServletResponse response,SecUser secUser) throws ServletException, IOException {
		
		String operaIp = this.getIp(httpServletRequest);
		
		if(!operaIp.equals(secUser.getLogin_ip())) {
			if(opNoLogUrls.contains(httpServletRequest.getServletPath())) {//不记录日志直接返回
				return true;
			}
			logger.info("last login ip different with opera ip ,login user:{},opera time:{},opera ip:{},request uri:{},"
					+ "last login ip:{},last login time:{}",
					new Object[]{secUser.getUsername(),DateUtils.dateToStr(new Date(), DateUtils.DF_yyyyMMddHHmmss),operaIp,httpServletRequest.getRequestURI(),
							secUser.getLogin_ip(),DateUtils.dateToStr(secUser.getLast_loginTime(), DateUtils.DF_yyyyMMddHHmmss)});
			return true;
		}
		return false;
	}
	
	@Override
	public void init(FilterConfig arg0) throws ServletException {
		urls.add("/normal/adminGoogleAuthAction!checkGoogleAuthCodeForLogin.action");
		urls.add("/js/jquery.min.js");
		
		//登录界面所需
		urls.add("/login.jsp");
		urls.add("/www/css/local.css");
		urls.add("/www/css/styles.css");
		urls.add("/css/font-awesome.min.css");
		
		opNoLogUrls.add("/normal/adminTipAction!getTips.action");
		opNoLogUrls.add("/normal/adminTipAction!getNewTips.action");
		opNoLogUrls.add("/public/newAdminOnlineChatAction!userlist.action");
		opNoLogUrls.add("/public/newAdminOnlineChatAction!list.action");
		opNoLogUrls.add("/public/newAdminOnlineChatAction!unread.action");
		opNoLogUrls.add("/public/newAdminOnlineChatAction!getUserInfo.action");
		opNoLogUrls.add("/public/newAdminOnlineChatAction!getOnlineChatMessage.action");
		
		noCheckIPUrls.add("/public/newAdminOnlineChatAction!userlist.action");
		noCheckIPUrls.add("/public/newAdminOnlineChatAction!list.action");
		noCheckIPUrls.add("/public/newAdminOnlineChatAction!send.action");
		noCheckIPUrls.add("/public/newAdminOnlineChatAction!create.action");
		noCheckIPUrls.add("/public/newAdminOnlineChatAction!del.action");
		noCheckIPUrls.add("/public/newAdminOnlineChatAction!getChatToken.action");
		noCheckIPUrls.add("/public/newAdminOnlineChatAction!getOnlineChatMessage.action");
		noCheckIPUrls.add("/public/newAdminOnlineChatAction!getOnlineChatMessage.action");
		noCheckIPUrls.add("/public/newAdminOnlineChatAction!deleteOnlineChatMessage.action");
		noCheckIPUrls.add("/public/newAdminOnlineChatAction!unread.action");
		noCheckIPUrls.add("/public/newAdminOnlineChatAction!getUserInfo.action");
	}
	
	public String getUsername_login(HttpServletRequest httpServletRequest) {

		HttpSession session = httpServletRequest.getSession();
		Object object = session.getAttribute("SPRING_SECURITY_CONTEXT");
		if (object != null) {
			return ((SecurityContext) object).getUsername();
		}
		return SecurityAppUserHolder.gettUsername();
	}
}