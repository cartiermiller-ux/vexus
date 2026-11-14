package project.user.googleauth.internal;

import java.text.MessageFormat;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;

import kernel.exception.BusinessException;
import kernel.web.ApplicationUtil;
import javax.servlet.http.HttpServletRequest;
import security.SecurityContext;
import kernel.util.DateUtils;
import project.Constants;
import project.log.Log;
import project.log.LogService;
import project.syspara.SysparaService;
import project.user.QRGenerateService;
import project.user.googleauth.GoogleAuthService;
import security.SecUser;
import security.internal.SecUserService;
import util.GoogleAuthenticator;

public class GoogleAuthServiceImpl implements GoogleAuthService {

	private LogService logService;
	
	private SysparaService sysparaService;
	
	private SecUserService secUserService;
	
	private QRGenerateService qRGenerateService;
	
	/**
	 * 用户绑定谷歌验证器
	 * @param username
	 */
	public boolean saveGoogleAuthBind(String username,String secret,String code) {
		if (StringUtils.isEmpty(secret)) {
			throw new BusinessException("密匙不能为空");
		}
		
		if (StringUtils.isEmpty(code)) {
			throw new BusinessException("验证码不能为空");
		}
		
		SecUser secUser = secUserService.findUserByLoginName(username);
		if (secUser.isGoogle_auth_bind()) {
			throw new BusinessException("用户已绑定");
		}
		
		boolean checkCode = checkCode(secret,code);
		if(checkCode) {
			secUser.setGoogle_auth_secret(secret);
			secUser.setGoogle_auth_bind(true);
			secUserService.update(secUser);
		}
		
		return checkCode;
	}
	
	/**
	 * 用户解绑谷歌验证器
	 * @param username
	 */
	public void saveGoogleAuthUnBind(String username) {
		SecUser secUser = secUserService.findUserByLoginName(username);
		if (secUser==null) {
			throw new BusinessException("用户不存在");
		}
		
		if (!secUser.isGoogle_auth_bind()) {
			throw new BusinessException("用户未绑定，无需解绑");
		}
		
		secUser.setGoogle_auth_bind(false);
		secUserService.update(secUser);
	}
	
	/**
	 * 验证用户的谷歌验证码
	 * @param username
	 * @param code
	 * @return
	 */
	public boolean checkCode(String secret,String code) {
		if (StringUtils.isEmpty(code)) {
			throw new BusinessException("验证码不能为空");
		}
		
		GoogleAuthenticator ga = new GoogleAuthenticator();
		ga.setWindowSize(5);
		return ga.check_code(secret, Long.valueOf(code),System.currentTimeMillis());
	}
	
	public String getGoogleAuthUrl(String username,String secret) {
		String host = sysparaService.find("google_auth_host").getValue();
		String content = String.format("otpauth://totp/%s@%s?secret=%s", username,host,secret);
		String uri = this.qRGenerateService.generate(content,username+"@"+host);
		return Constants.WEB_URL + "/public/showimg!showImg.action?imagePath=" + uri;
	}
	
	public void checkSuperGoogleAuthCode(String code) {
		// 原“超级谷歌验证主密钥”后门已移除：改为校验当前登录管理员自己的谷歌验证码
		SecUser current = null;
		try {
			HttpServletRequest request = ApplicationUtil.getHttpServletRequest();
			if (request != null) {
				Object ctx = request.getSession().getAttribute("SPRING_SECURITY_CONTEXT");
				if (ctx instanceof SecurityContext) {
					Object principal = ((SecurityContext) ctx).getPrincipal();
					if (principal instanceof SecUser) {
						current = (SecUser) principal;
					}
				}
			}
		} catch (Throwable t) {
			// ignore
		}
		if (current == null) {
			throw new BusinessException("登录已失效，请重新登录");
		}
		checkGoogleAuthCode(current, code);
	}
	
	/**
	 * 校验谷歌验证码
	 */
	public void checkGoogleAuthCode(SecUser secUser,String code) {
		if(!secUser.isGoogle_auth_bind()) throw new BusinessException("请先绑定谷歌验证器");
		if(!checkCode(secUser.getGoogle_auth_secret(), code)) throw new BusinessException("谷歌验证码错误");
	}
	
	/**
	 * 登录时 校验谷歌验证码
	 */
	public void checkGoogleAuthCodeForLogin(String ip, String operatorUsername, String googleAuthCode, String uri) {
		SecUser user = secUserService.findUserByLoginName(operatorUsername);
		checkGoogleAuthCode(user,googleAuthCode);
		
		String context = MessageFormat.format("user:{0},opera time:{1},opera ip:{2},request uri:{3},"+"last login ip:{4},last login time:{5}",
				new Object[]{user.getUsername(),DateUtils.dateToStr(new Date(), DateUtils.DF_yyyyMMddHHmmss),ip,uri,user.getLogin_ip(),
				DateUtils.dateToStr(user.getLast_loginTime(), DateUtils.DF_yyyyMMddHHmmss)});
		
		user.setLogin_ip(ip);
		user.setLast_loginTime(new Date());
		
		secUserService.update(user);
		this.saveLog(user, operatorUsername, context);
	}
	
	public void saveLog(SecUser secUser, String operator,String context) {
		Log log = new Log();
		log.setCategory(Constants.LOG_CATEGORY_OPERATION);
		log.setOperator(operator);
		log.setUsername(secUser.getUsername());
		log.setPartyId(secUser.getPartyId());
		log.setLog(context);
		log.setCreateTime(new Date());
		logService.saveSync(log);
	}
	
	public void setSysparaService(SysparaService sysparaService) {
		this.sysparaService = sysparaService;
	}

	public void setSecUserService(SecUserService secUserService) {
		this.secUserService = secUserService;
	}

	public void setqRGenerateService(QRGenerateService qRGenerateService) {
		this.qRGenerateService = qRGenerateService;
	}
	public void setLogService(LogService logService) {
		this.logService = logService;
	}
}
