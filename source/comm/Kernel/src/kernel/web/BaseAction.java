package kernel.web;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import kernel.util.DateUtils;
import kernel.util.StringUtils;
import project.user.token.TokenService;

public class BaseAction {
	
	private Logger logger = LoggerFactory.getLogger(BaseAction.class);
	
	public static final String SPRING_SECURITY_CONTEXT_KEY = "SPRING_SECURITY_CONTEXT";

	protected String error;

	protected String message;

	protected String username_login;

	protected String result;

	protected String callback;

	WebApplicationContext wac = ContextLoader.getCurrentWebApplicationContext();

	public ResultObject readSecurityContextFromSession(ResultObject resultObject) {
		HttpServletRequest request = this.getRequest();
		String token = request.getParameter("token");
		
		if (StringUtils.isNullOrEmpty(token)) {
			resultObject.setCode("403");
			resultObject.setMsg("请重新登录");
			return resultObject;
		}
		
		TokenService tokenService = (TokenService)wac.getBean("tokenService");
		String partyId = tokenService.cacheGet(token);
		if (StringUtils.isNullOrEmpty(partyId)) {
			resultObject.setCode("403");
			resultObject.setMsg("请重新登录");
			return resultObject;
		}

		return resultObject;
	}

	/**
	 * 从Request对象中获得客户端IP，处理了HTTP代理服务器和Nginx的反向代理截取了ip
	 * @param request
	 * @return ip
	 */
	public String getIp(HttpServletRequest request) {
		String ip = request.getHeader("X-Forwarded-For");
		if (StringUtils.isNotEmpty(ip) && !"unKnown".equalsIgnoreCase(ip)) {
			int index = ip.indexOf(",");
			return -1==index?ip.trim():ip.substring(0, index).trim();
		}
		
		ip = request.getHeader("X-Real-IP");
		if (StringUtils.isNotEmpty(ip) && !"unKnown".equalsIgnoreCase(ip)) {
			return ip.trim();
		}
		
		String remoteAddr=request.getRemoteAddr();
		if(null!=remoteAddr && !(remoteAddr=remoteAddr.trim()).isEmpty()) return remoteAddr;
		
		String remoteHost=request.getRemoteHost();
		if(null!=remoteHost && !(remoteHost=remoteHost.trim()).isEmpty()) return remoteHost;
		
		return null;
	}

	public String getLoginPartyId() {
		HttpServletRequest request = this.getRequest();
		String token = request.getParameter("token");
		
		if (StringUtils.isNullOrEmpty(token)) {
			logger.error("token is null");
			return null;
		}
		TokenService tokenService = (TokenService)wac.getBean("tokenService");
		return tokenService.cacheGet(token);
	}

	protected HttpServletRequest getRequest() {
		return ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
	}

	protected HttpServletResponse getResponse() {
		return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
	}

	public String getError() {
		return this.error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public String getMessage() {
		return this.message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Date toDate(String dateStr, String format) {
		Date date = null;
		if (!StringUtils.isNullOrEmpty(dateStr)) {
			try {
				date = DateUtils.toDate(dateStr, format);
			} catch (Throwable e) {
				date = null;
				System.out.println(e.getStackTrace());
			}
		}
		return date;
	}

	public List<Date> toRangeDate(String dateStr) {
		List<Date> list = new ArrayList<Date>();
		try {
			if (!StringUtils.isNullOrEmpty(dateStr)) {
				String begin_str = dateStr.split("-")[0].trim();
				String end_str = dateStr.split("-")[1].trim();

				Date begin = DateUtils.toDate(begin_str, DateUtils.DF_MMddyyyy);
				Date end = DateUtils.toDate(end_str, DateUtils.DF_MMddyyyy);

				list.add(begin);
				list.add(end);
			}
		} catch (Exception e) {
			System.out.println(e.getStackTrace());
			list = new ArrayList<Date>();
			list.add(null);
			list.add(null);
		} finally {
			if (list.size() != 2) {
				list = new ArrayList<Date>();
				list.add(null);
				list.add(null);
			}

		}

		return list;
	}

	public List<Date> toRangeSecondsDate(String dateStr) {
		List<Date> list = new ArrayList<Date>();
		try {
			if (!StringUtils.isNullOrEmpty(dateStr)) {
				String begin_str = dateStr.split("-")[0].trim();
				String end_str = dateStr.split("-")[1].trim();

				Date begin = DateUtils.toDate(begin_str, "MM/dd/yyyy HH:mm:ss");
				Date end = DateUtils.toDate(end_str, "MM/dd/yyyy HH:mm:ss");
				list.add(begin);
				list.add(end);
			}
		} catch (Exception e) {
			System.out.println(e.getStackTrace());
			list = new ArrayList<Date>();
			list.add(null);
			list.add(null);
		} finally {
			if (list.size() != 2) {
				list = new ArrayList<Date>();
				list.add(null);
				list.add(null);
			}

		}

		return list;
	}

	public List<String> toRangeThirdsDate(String dateStr) {
		List<String> list = new ArrayList<String>();
		try {
			if (!StringUtils.isNullOrEmpty(dateStr)) {
				String begin_str = dateStr.split(" - ")[0].trim();
				String end_str = dateStr.split(" - ")[1].trim();

				Date begin = DateUtils.toDate(begin_str, "yyyy-MM-dd HH:mm:ss");
				Date end = DateUtils.toDate(end_str, "yyyy-MM-dd HH:mm:ss");
				list.add(begin_str);
				list.add(end_str);
			}
		} catch (Exception e) {
			System.out.println(e.getStackTrace());
			list = new ArrayList<String>();
			list.add(null);
			list.add(null);
		} finally {
			if (list.size() != 2) {
				list = new ArrayList<String>();
				list.add(null);
				list.add(null);
			}

		}

		return list;
	}

	public Date toDate(String dateStr) {
		Date date = null;
		if (!StringUtils.isNullOrEmpty(dateStr)) {
			try {
				date = DateUtils.toDate(dateStr, "yyyy-MM-dd");
			} catch (Throwable e) {
				System.out.println(e.getStackTrace());
			}
		}
		return date;
	}

	public String dateToStr(Date date, String format) {
		String dateStr = null;
		if (date != null) {
			try {
				dateStr = DateUtils.dateToStr(date, format);
			} catch (Throwable e) {
				System.out.println(e.getStackTrace());
			}
		}

		return dateStr;
	}

	public String dateToStr(Date date) {
		String dateStr = null;
		if (date != null) {
			try {
				dateStr = DateUtils.dateToStr(date, "yyyy-MM-dd");
			} catch (Throwable e) {
				System.out.println(e.getStackTrace());
			}
		}

		return dateStr;
	}

	public void addCookie(String name, String value) {
		Cookie cookie = new Cookie(name, value);
		cookie.setMaxAge(31536000);
		this.getResponse().addCookie(cookie);
	}

	public String getCookie(String name) {
		HttpServletRequest request = this.getRequest();
		Cookie[] cookies = request.getCookies();
		for (Cookie cookie : cookies) {
			if (cookie.getName().equals(name)) {
				return cookie.getValue();
			}
		}
		return null;
	}

	public String getResult() {
		return this.result;
	}

//	public String getLocalText(String localkey, String log) {
//		String[] args = log.split(",");
//		return this.messageSource.getMessage(localkey, args, "Required", null);
//	}

	/**
	 * 从Request对象中获得客户端IP，处理了HTTP代理服务器和Nginx的反向代理截取了ip
	 * 
	 * @param request
	 * @return ip
	 */
	public String getIp() {
		HttpServletRequest request = this.getRequest();
		String ip = request.getHeader("X-Forwarded-For");
		if (StringUtils.isNotEmpty(ip) && !"unKnown".equalsIgnoreCase(ip)) {
			// 多次反向代理后会有多个ip值，第一个ip才是真实ip
			int index = ip.indexOf(",");
			if (index != -1) {
				return ip.substring(0, index);
			} else {
				return ip;
			}
		}
		ip = request.getHeader("X-Real-IP");
		if (StringUtils.isNotEmpty(ip) && !"unKnown".equalsIgnoreCase(ip)) {
			return ip;
		}
		return request.getRemoteAddr();
	}

	public String getCallback() {
		return callback;
	}

	public void setCallback(String callback) {
		this.callback = callback;
	}

}
