package project.web.api.filter;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.Enumeration;
import java.util.HashSet;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;

import email.Config;
import kernel.web.ApplicationUtil;
import kernel.web.PageActionSupport;
import kernel.web.ResultObject;
import project.syspara.Syspara;
import project.syspara.SysparaService;
import project.user.token.TokenService;
import util.IpUtil;

/**
 * @author 15308
 * @description SERVLET请求过滤器(不含静态页面文件)
 */
public class AllRequestFilter extends PageActionSupport implements Filter  {
	/**
	 * 白名单URL
	 */
	private static final HashSet<String> WHITE_URLS = new HashSet<String>();
	
	/**
	 * 白名单URL
	 */
	private static final HashSet<String> WHITE_URLS2 = new HashSet<String>();
	
    /**
     * 日志工具
     */
    private static final Logger logger = LoggerFactory.getLogger(AllRequestFilter.class);
	
	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain filterChain) throws IOException, ServletException {
		//白名单接口地址直接(断网用户也可以访问白名单)
	
		
		//SERVLET请求响应对象
		HttpServletRequest request = (HttpServletRequest)req;
		HttpServletResponse response = (HttpServletResponse)res;		
		response.setHeader("Access-Control-Allow-Origin", "*");
		response.setHeader("Access-Control-Allow-Headers", "Content-Type,X-Requested-With,sign,tissuePaper");
		String servletPath = request.getServletPath();
		if(WHITE_URLS2.contains(servletPath)) {
			filterChain.doFilter(request, response);
			return;
		}
		
		if(ObjectUtils.isNotEmpty(Config.VERSION_NUMBER)) {
			//验证时间戳签名
			if(checkSign(request,response)) {
				return;
			}
		}
		//校验IP是否合法(如果为null则直接返回)
		String requestIP = this.getIp(request);
		if (!IpUtil.isCorrectIpRegular(requestIP)) {
			logger.error("校验IP不合法,参数: {}", requestIP);
			return;
		}
		
		//校验IP是否存在于黑名单中(如果存在则直接返回)
		SysparaService sysparaService = ApplicationUtil.getBean(SysparaService.class);
		Syspara blackListSyspara = sysparaService.find("blacklist_ip");
		if(null!=blackListSyspara) {
			String blackIPS=blackListSyspara.getValue();
			if(null!=blackIPS && !(blackIPS=blackIPS.trim()).isEmpty() && blackIPS.contains(requestIP)) {
				logger.error("黑名单IP,参数: {}", requestIP);
				return;
			}
		}
		
		if(WHITE_URLS.contains(servletPath)) {
			filterChain.doFilter(request, response);
			return;
		}
		
		//校验用户是否登录(通过判断是否提交Token来辨别)
		TokenService tokenService = ApplicationUtil.getBean(TokenService.class);
		String token = request.getParameter("token");
		if(null==token || (token=token.trim()).isEmpty()) {
			logger.error("浏览器端未提交token值,用户必须先登录才能有token,当前请求接口地址:{}",servletPath);
			return;
		}
		
		
		String userName = tokenService.getUserName(token,servletPath);		
		if (StringUtils.isBlank(userName)) {
			logger.error("token已失效路径:token值{},访问路径{}",token,servletPath);
			ResultObject resultObject = new ResultObject();
			resultObject.setCode("403");
			resultObject.setMsg("请重新登录");
			response.getWriter().print(JSON.toJSONString(resultObject));
			return;
		}
		
		//被设置为断网的用户无法访问
		Syspara stopUserInternets = sysparaService.findByDB("stop_user_internet");
		if(StringUtils.isNotBlank(userName)) {	
			if(null!=stopUserInternets) {
				String value = stopUserInternets.getValue();
				logger.error("当前配置的断网用户:{},当前访问的用户名:{},用户访问的路径:{}", value,userName,servletPath);
				if(null!=value && value.contains(userName)) {
					logger.error("当前配置的断网用户:{},不能访问的用户名:{},用户访问的路径:{}", value,userName,servletPath);
					((HttpServletResponse)response).sendError(405, "请求超时");
					return;
				}
			}
		}
		
		//检查提交数据包中是否存在静态脚本
		if (checkParameter(request)) return;	
		//放行到后端的SERVLET
		filterChain.doFilter(request, response);
	}
	
	/**
	 * 请求参数中包含"script"的过滤
	 * @param request 请求对象
	 * @return 检查是否通过(返回true表示不通过)
	 */
	private boolean checkParameter(HttpServletRequest request) {
		Enumeration<String> enu = request.getParameterNames();
		while (enu.hasMoreElements()) {
			String paraName = enu.nextElement();
			if(null==paraName || (paraName=paraName.trim()).isEmpty()) continue;
			String value = request.getParameter(paraName).trim().toLowerCase();
			if (-1!=value.indexOf("script")) {
				logger.error("请求参数中包含script的过滤,参数: " + request.getParameter(paraName) + "请求地址: " + request.getServletPath());
				return true;
			}
		}
		
		Enumeration<String> heads = request.getHeaderNames();
		while (heads.hasMoreElements()) {
			String headName = (String) heads.nextElement();
			if(null==headName || (headName=headName.trim()).isEmpty()) continue;
			String value = request.getHeader(headName).trim().toLowerCase();
			if(-1!=value.indexOf("<")) {
				logger.error("head参数中包含<的过滤,参数: " + request.getHeader(headName) + "请求地址: " + request.getServletPath());
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * 初始化白名单
	 */
	@Override
	public void init(FilterConfig arg0) throws ServletException {
		// 访问图片
		WHITE_URLS2.add("/public/showimg!showImg.action");	
		// 访问充值地址
		WHITE_URLS2.add("/api/channelBlockchain!getBlockchainName.action");
		//H5注册
		WHITE_URLS.add("/api/localuser!registerNoVerifcode.action");	
		
		WHITE_URLS.add("/api/newOnlinechat!send.action");
		
		WHITE_URLS.add("/api/onlinechat!send.action");
		
		WHITE_URLS.add("/api/newOnlinechat!list.action");
		
		WHITE_URLS.add("/api/onlinechat!list.action");
		
		WHITE_URLS.add("/api/loan!getLoanParamList.action");			
		//H5注册
		WHITE_URLS.add("/api/localuser!register.action");
		//试用注册
		WHITE_URLS.add("/api/localuser!registerTest.action");
		//平仓参数接口
		WHITE_URLS.add("/api/contractApplyOrder!closeview.action");		
		//开仓参数接口
		WHITE_URLS.add("/api/contractApplyOrder!openview.action");	
		
		//平仓参数接口
		WHITE_URLS.add("/api/futuresOrder!closeview.action");		
		//开仓参数接口
		WHITE_URLS.add("/api/futuresOrder!openview.action");		
		
		//PC注册
		WHITE_URLS.add("/api/localuser!getImageCode.action");	
		//PC发送邮箱功能
		WHITE_URLS.add("/api/idcode!execute.action");
		
		WHITE_URLS.add("/api/localuser!register_username.action");
				
		WHITE_URLS.add("/api/exchangerateuserconfig!get.action");
		// 登录
		WHITE_URLS.add("/api/dapp!login.action");
		// 热门币种
		WHITE_URLS.add("/api/item!list.action");
		// 股票行情
		WHITE_URLS.add("/api/stock!list.action");
		WHITE_URLS.add("/api/stock!kline.action");
		WHITE_URLS.add("/api/alpaca!snapshots.action");
		WHITE_URLS.add("/api/alpaca!stream.action");
		// AI 客服
		WHITE_URLS.add("/api/ai!chat.action");
		// 
		WHITE_URLS.add("/api/dapp!pooldata.action");
		// 轮播日志
		WHITE_URLS.add("/api/dapp!get_notice_logs.action");
		// 上传文件
		WHITE_URLS.add("/public/uploadimg!execute.action");
		WHITE_URLS.add("/public/showimg!showImg.action");
		WHITE_URLS.add("/api/monitor!getAutoMonitorPoolData.action");
		
		WHITE_URLS.add("/api/user!getUserNameVerifTarget.action");
		// 实时数据
		WHITE_URLS.add("/api/hobi!getRealtime.action");
		// Kline
		WHITE_URLS.add("/api/hobi!getKline.action");
		// 分时图
		WHITE_URLS.add("/api/hobi!getTrend.action");
		
		// onlineChat
		WHITE_URLS.add("/api/onlinechat!list.action");
		WHITE_URLS.add("/api/onlinechat!send.action");
		WHITE_URLS.add("/api/cms!list.action");
		WHITE_URLS.add("/api/news!list.action");
		WHITE_URLS.add("/api/news!get.action");
		WHITE_URLS.add("/api/exchangerate!list.action");
		WHITE_URLS.add("/api/user!login.action");
		WHITE_URLS.add("/api/syspara!getSyspara.action");
		WHITE_URLS.add("/api/news!list_v2_popup.action");
		WHITE_URLS.add("/api/banner!list.action");		
		WHITE_URLS.add("/api/cms!get.action");	
		WHITE_URLS.add("/api/user!resetpsw.action");
		WHITE_URLS.add("/api/miner!list.action");
		WHITE_URLS.add("/api/miner!get.action");
		WHITE_URLS.add("/api/finance!list.action");
		WHITE_URLS.add("/api/finance!get.action");
		
				 
	}
	
	@Override
	public void destroy() {}
	
	
	  /**
     * 普通请求处理处理
	 * @throws IOException 
     */
    public boolean checkSign(HttpServletRequest request, ServletResponse response) throws IOException { 	
    	String servletPath2 = request.getServletPath();
        // 响应请求前参数校验
        // 获取请求头中的时间戳参数
        String timestamp = request.getHeader("tissuePaper");
		Enumeration<String> heads = request.getHeaderNames();
        if (timestamp == null) {
            // 没有时间戳参数返回验签失败
        	logger.error("时间戳为空"+servletPath2);
			((HttpServletResponse)response).sendError(201, "时间戳为空");
			return true;
        }

        try {
            // 3分钟内有效
            long timestampDate = Long.parseLong(timestamp) + (60 * 1);
            // 10位时间戳
            long currDate = System.currentTimeMillis() / 1000L;
            if (timestampDate < currDate) {
                // 请求过期
            	logger.error("请求过期"+servletPath2);
    			((HttpServletResponse)response).sendError(202, "请求过期");
    			return true;
            }
			/*
			 * if (timestampDate-60 > currDate) { // 请求过期 logger.info("时间超前");
			 * System.out.println("时间超前"+servletPath2+"时间:"+timestampDate);
			 * ((HttpServletResponse)response).sendError(203, "时间超前"); return true; }
			 */
        } catch (NumberFormatException e) {
            assert response != null;
            logger.error("请求异常"+servletPath2);
			((HttpServletResponse)response).sendError(204, "请求异常");
			return true;
        }

        String sign = request.getHeader("sign");
        if (sign == null || "".equals(sign.trim())) {
            // 没有签名返回验签失败
            assert response != null;
            logger.error("签名为空"+servletPath2);
        	((HttpServletResponse)response).sendError(205, "签名为空");
			//((HttpServletResponse)response).sendError(201, "验签失败");
			return true;
        }
        
        // 验签， 根据时间戳生成签名加盐值反复加密两次， 对比是否一致
        // 第一个参数为加密内容， 第二个参数为加密时的盐值
        // 获取后台管理MD5盐值
        String waitSign = Config.ENCRYPTION_KEY+timestamp;
        String md5_result = md5Hex(waitSign).toUpperCase();
        if (!md5_result.equals(sign)) {
            // 验签失败
            logger.error("签名失败"+servletPath2);
            ((HttpServletResponse)response).sendError(206, "签名失败");
            return true;
        }
        
        return false;
    }

	/**
	 * 计算字符串 MD5（十六进制小写），替代已删除的 org.apache.poi.assistant.MD5
	 */
	private static String md5Hex(String text) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] bytes = md.digest(text.getBytes("UTF-8"));
			StringBuilder sb = new StringBuilder();
			for (byte b : bytes) {
				String hex = Integer.toHexString(b & 0xff);
				if (hex.length() == 1) {
					sb.append('0');
				}
				sb.append(hex);
			}
			return sb.toString();
		} catch (Exception e) {
			throw new RuntimeException("MD5 not available", e);
		}
	}
}
