package project.web.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.providers.encoding.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import kernel.exception.BusinessException;
import kernel.util.Arith;
import kernel.util.StringUtils;
import kernel.web.BaseAction;
import kernel.web.ResultObject;
import project.Constants;
import project.ddos.IpMenuService;
import project.hobi.http.HttpHelper;
import project.hobi.http.HttpMethodType;
import project.log.Log;
import project.log.LogService;
import project.party.PartyService;
import project.party.model.Party;
import project.syspara.SysparaService;
import project.user.UserSafewordApply;
import project.user.UserSafewordApplyService;
import project.user.UserService;
import project.user.googleauth.GoogleAuthService;
import project.user.idcode.IdentifyingCodeTimeWindowService;
import project.user.token.TokenService;
import security.SaltSigureUtils;
import security.SecUser;
import security.internal.SecUserService;
import util.IpUtil;
import util.Strings;

/**
 * API用户相关
 *
 */
@RestController
@CrossOrigin
public class UserController extends BaseAction {
	
	private Logger logger=LoggerFactory.getLogger(UserController.class);
	
	@Autowired
	private SecUserService secUserService;
	@Autowired
	private UserService userService;
	@Autowired
	private PartyService partyService;
	@Autowired
	private IdentifyingCodeTimeWindowService identifyingCodeTimeWindowService;
	@Autowired
	private LogService logService;
	@Autowired
	private UserSafewordApplyService userSafewordApplyService;
	@Autowired
	private GoogleAuthService googleAuthService;
	@Autowired
	private IpMenuService ipMenuService;
	@Autowired
	private SysparaService sysparaService;
	@Autowired
	private PasswordEncoder passwordEncoder;
	@Autowired
	private TokenService tokenService;

	private final String action = "/api/user!";

	
	/**
	 * 用户名登录接口
	 * 
	 */
	@RequestMapping(action + "login.action")
	public Object login(HttpServletRequest request) {
		ResultObject resultObject = new ResultObject();
		try {

			String username = request.getParameter("username");
			String password = request.getParameter("password");
			
			if (StringUtils.isEmptyString(username)) {
				throw new BusinessException("用户名不能为空");
			}
			
			if (StringUtils.isEmptyString(password)) {
				throw new BusinessException("登录密码不能为空");
			}
			
			if (password.length() < 6 || password.length() > 12) {
				throw new BusinessException("登录密码必须6-12位");
			}
			
			String ip = this.getIp();
			if (!IpUtil.isCorrectIpRegular(ip)) {
				logger.error("校验IP不合法,参数{}", ip);
				throw new BusinessException("校验IP不合法");
			}
			
			SecUser secUser = this.userService.login(username, password);

			String token = tokenService.savePut(secUser.getPartyId(),secUser.getUsername());
			userService.online(secUser.getPartyId());
			ipMenuService.saveIpMenuWhite(this.getIp());
			Party party = this.partyService.cachePartyBy(secUser.getPartyId(), false);
			Map<String, Object> data = new HashMap<String, Object>();
			data.put("token", token);
			data.put("username", secUser.getUsername());
			data.put("usercode", party.getUsercode());

			Log log = new Log();

			log.setCategory(Constants.LOG_CATEGORY_SECURITY);
			log.setLog("用户登录,ip[" + this.getIp(getRequest()) + "]");
			log.setPartyId(secUser.getPartyId());
			log.setUsername(username);
			logService.saveAsyn(log);

			party.setLogin_ip(this.getIp(getRequest()));
			this.partyService.update(party);

			resultObject.setData(data);

		} catch (BusinessException e) {
			resultObject.setCode("1");
			resultObject.setMsg(e.getMessage());
		} catch (Throwable t) {
			resultObject.setCode("1");
			resultObject.setMsg("程序错误");
			logger.error("error:", t);
		}
		return resultObject;
	}

	public Object online() {

		ResultObject resultObject = new ResultObject();
		try {
			userService.online(this.getLoginPartyId());
		} catch (BusinessException e) {
			resultObject.setCode("1");
			resultObject.setMsg(e.getMessage());
		} catch (Throwable t) {
			resultObject.setCode("1");
			resultObject.setMsg("程序错误");
			logger.error("error:", t);
		}

		return resultObject;
	}

	/**
	 * 退出登录
	 */
	@RequestMapping(action + "logout.action")
	public Object logout(HttpServletRequest request) {
		ResultObject resultObject = new ResultObject();
		String token = request.getParameter("token");
		try {
			this.userService.logout(this.getLoginPartyId());
			this.tokenService.delete(token);

		} catch (BusinessException e) {
			resultObject.setCode("1");
			resultObject.setMsg(e.getMessage());
		} catch (Throwable t) {
			resultObject.setCode("1");
			resultObject.setMsg("程序错误");
			logger.error("error:", t);
		}
		return resultObject;
	}
	
	/**
	 * 验证资金密码
	 */
	@RequestMapping(action + "verifySafeword.action")
	public Object verifySafeword(HttpServletRequest request) {
		ResultObject resultObject = new ResultObject();
		String safeWord = request.getParameter("safeWord");
		try {
			String partyId = this.getLoginPartyId();
			Party party = this.partyService.cachePartyBy(partyId, false);
			String md5 = passwordEncoder.encodePassword(safeWord, SaltSigureUtils.saltfigure);
			if (!md5.equals(party.getSafeword())) {
				throw new BusinessException("资金密码错误");
			}
		} catch (BusinessException e) {
			resultObject.setCode("1");
			resultObject.setMsg(e.getMessage());
		} catch (Throwable t) {
			resultObject.setCode("1");
			resultObject.setMsg("程序错误");
			logger.error("error:", t);
		}
		return resultObject;
	}

	/**
	 * 验证码登录 - 暂时不用
	 */
	@RequestMapping(action + "login_idcode.action")
	public Object login_idcode(HttpServletRequest request) {
		ResultObject resultObject = new ResultObject();
		String username = request.getParameter("username");
		String verifcode = request.getParameter("verifcode");
		try {
			SecUser secUser = this.userService.login_idcode(username, verifcode);
			String token = tokenService.savePut(secUser.getPartyId(),secUser.getUsername());
			userService.online(secUser.getPartyId());
			ipMenuService.saveIpMenuWhite(this.getIp());
			Map<String, Object> data = new HashMap<String, Object>();
			data.put("token", token);
			resultObject.setData(data);

		} catch (BusinessException e) {
			resultObject.setCode("1");
			resultObject.setMsg(e.getMessage());
		} catch (Throwable t) {
			resultObject.setCode("1");
			resultObject.setMsg("程序错误");
			logger.error("error:", t);
		}
		return resultObject;
	}

	/**
	 * 修改登录密码 用验证码
	 */
	@RequestMapping(action + "updatepsw.action")
	public Object updatepsw(HttpServletRequest request) {
		String password = request.getParameter("password");
		String verifcode_type = request.getParameter("verifcode_type");
		String verifcode = request.getParameter("verifcode");

		ResultObject resultObject = new ResultObject();
		resultObject = this.readSecurityContextFromSession(resultObject);
		if (!"0".equals(resultObject.getCode())) {
			return resultObject;
		}

		try {

			if (StringUtils.isEmptyString(password)) {
				throw new BusinessException("密码不能为空");
			}
			if (password.length() < 6 || password.length() > 12) {
				throw new BusinessException("密码必须6-12位");
			}
			if (StringUtils.isEmptyString(verifcode_type)) {
				throw new BusinessException("验证类型不能为空");
			}
			if (StringUtils.isEmptyString(verifcode)) {
				throw new BusinessException("验证码不能为空");
			}

			String loginPartyId = this.getLoginPartyId();
			Party party = this.partyService.cachePartyBy(loginPartyId, false);
			SecUser secUser = this.secUserService.findUserByPartyId(loginPartyId);

			// 根据验证类型获取验证key verifcode_type: 1/手机;2/邮箱;3/谷歌验证器;
			String key = "";
			String errMsg = "";
			if ("1".equals(verifcode_type)) {
				key = StringUtils.isEmptyString(party.getPhone()) || false == party.getPhone_authority() ? "" : party.getPhone();
				errMsg = "未绑定手机号";
			} else if ("2".equals(verifcode_type)) {
				key = StringUtils.isEmptyString(party.getEmail()) || false == party.getEmail_authority() ? "" : party.getEmail();
				errMsg = "未绑定邮箱";
			} else if ("3".equals(verifcode_type)) {
				key = StringUtils.isEmptyString(secUser.getGoogle_auth_secret()) || false == secUser.isGoogle_auth_bind() ? "" : secUser.getGoogle_auth_secret();
				errMsg = "未绑定谷歌验证器";
			}
			if (StringUtils.isEmptyString(key)) {
				throw new BusinessException(errMsg);
			}

			// 验证
			boolean passed = false;
			if ("1".equals(verifcode_type) || "2".equals(verifcode_type)) {
				String authcode = this.identifyingCodeTimeWindowService.getAuthCode(key);
				if ((null != authcode) && (authcode.equals(verifcode))) {
					passed = true;
					this.identifyingCodeTimeWindowService.delAuthCode(key);
				}
			} else if ("3".equals(verifcode_type)) {
				if (this.googleAuthService.checkCode(key, verifcode)) {
					passed = true;
				}
			}

			// 如果是演示用户，则不判断验证码
			if (!"GUEST".contentEquals(party.getRolename())) {
				if (!passed) {
					throw new BusinessException("验证码不正确");
				}
			}

			// 更新密码
			this.secUserService.updatePassword(secUser.getUsername(), password);			
			Log log = new Log();
			log.setCategory(Constants.LOG_CATEGORY_SECURITY);
			log.setLog("修改登录密码,ip[" + this.getIp(getRequest()) + "]");
			log.setPartyId(party.getId());
			log.setUsername(party.getUsername());
			logService.saveAsyn(log);

		} catch (BusinessException e) {
			resultObject.setCode("1");
			resultObject.setMsg(e.getMessage());
		} catch (Throwable t) {
			resultObject.setCode("1");
			resultObject.setMsg("程序错误");
			logger.error("error:", t);
		}

		return resultObject;
	}

	/**
	 * 修改登录密码 用旧密码
	 */
	@RequestMapping(action + "updateOldAndNewPsw.action")
	public Object updateOldAndNewPsw(HttpServletRequest request) {
		String old_password = request.getParameter("old_password");
		String password = request.getParameter("password");
		String re_password = request.getParameter("re_password");

		ResultObject resultObject = new ResultObject();
		resultObject = this.readSecurityContextFromSession(resultObject);
		if (!"0".equals(resultObject.getCode())) {
			return resultObject;
		}

		try {

			if (StringUtils.isEmptyString(old_password)) {
				throw new BusinessException("旧密码不能为空");
			}
			if (StringUtils.isEmptyString(password)) {
				throw new BusinessException("新密码不能为空");
			}
			if (StringUtils.isEmptyString(re_password)) {
				throw new BusinessException("新密码确认不能为空");
			}

			if (old_password.length() < 6 || old_password.length() > 12 || password.length() < 6 || password.length() > 12) {
				throw new BusinessException("密码必须6-12位");
			}

			SecUser secUser = this.secUserService.findUserByPartyId(this.getLoginPartyId());

			if (!this.passwordEncoder.encodePassword(old_password, secUser.getUsername()).equals(secUser.getPassword())) {
				throw new BusinessException("旧密码不正确");
			}

			if (!password.equals(re_password)) {
				throw new BusinessException("新密码不一致");
			}

			this.secUserService.updatePassword(secUser.getUsername(), password);
			Log log = new Log();

			log.setCategory(Constants.LOG_CATEGORY_SECURITY);
			log.setLog("用户修改登录密码,ip[" + this.getIp(getRequest()) + "]");
			log.setPartyId(secUser.getPartyId());
			log.setUsername(secUser.getName());
			logService.saveAsyn(log);
		} catch (BusinessException e) {
			resultObject.setCode("1");
			resultObject.setMsg(e.getMessage());
		} catch (Throwable t) {
			resultObject.setCode("1");
			resultObject.setMsg("程序错误");
			logger.error("error:", t);
		}

		return resultObject;
	}

	/**
	 * 重置登录密码
	 */
	@RequestMapping(action + "resetpsw.action")
	public Object resetpsw(HttpServletRequest request) {
		String username = request.getParameter("username");
		String password = request.getParameter("password");
		String verifcode_type = request.getParameter("verifcode_type");
		String verifcode = request.getParameter("verifcode");

		ResultObject resultObject = new ResultObject();

		try {

			if (StringUtils.isEmptyString(username)) {
				throw new BusinessException("用户名不能为空");
			}
			if (StringUtils.isEmptyString(password)) {
				throw new BusinessException("密码不能为空");
			}
			if (password.length() < 6 || password.length() > 12) {
				throw new BusinessException("密码必须6-12位");
			}
			if (StringUtils.isEmptyString(verifcode_type)) {
				throw new BusinessException("验证类型不能为空");
			}
			if (StringUtils.isEmptyString(verifcode)) {
				throw new BusinessException("验证码不能为空");
			}

			Party party = this.partyService.findPartyByUsername(username);
			if (null == party) {
				throw new BusinessException("用户名不存在");
			}
			SecUser secUser = this.secUserService.findUserByPartyId(party.getId().toString());

			// 根据验证类型获取验证key verifcode_type: 1/手机;2/邮箱;3/谷歌验证器;
			String key = "";
			String errMsg = "";
			if ("1".equals(verifcode_type)) {
				key = StringUtils.isEmptyString(party.getPhone()) || false == party.getPhone_authority() ? "" : party.getPhone();
				errMsg = "未绑定手机号";
			} else if ("2".equals(verifcode_type)) {
				key = StringUtils.isEmptyString(party.getEmail()) || false == party.getEmail_authority() ? "" : party.getEmail();
				errMsg = "未绑定邮箱";
			} else if ("3".equals(verifcode_type)) {
				key = StringUtils.isEmptyString(secUser.getGoogle_auth_secret()) || false == secUser.isGoogle_auth_bind() ? "" : secUser.getGoogle_auth_secret();
				errMsg = "未绑定谷歌验证器";
			}
			if (StringUtils.isEmptyString(key)) {
				throw new BusinessException(errMsg);
			}

			// 验证
			boolean passed = false;
			if ("1".equals(verifcode_type) || "2".equals(verifcode_type)) {
				String authcode = this.identifyingCodeTimeWindowService.getAuthCode(key);
				if ((null != authcode) && (authcode.equals(verifcode))) {
					passed = true;
					this.identifyingCodeTimeWindowService.delAuthCode(key);
				}
			} else if ("3".equals(verifcode_type)) {
				if (this.googleAuthService.checkCode(key, verifcode)) {
					passed = true;
				}
			}

			// 如果是演示用户，则不判断验证码
			if (!"GUEST".contentEquals(party.getRolename())) {
				if (!passed) {
					throw new BusinessException("验证码不正确");
				}
			}

			// 更新密码
			this.secUserService.updatePassword(username, password);

		} catch (BusinessException e) {
			resultObject.setCode("1");
			resultObject.setMsg(e.getMessage());
		} catch (Throwable t) {
			resultObject.setCode("1");
			resultObject.setMsg("程序错误");
			logger.error("error:", t);
		}

		return resultObject;
	}
	
//	/**
//	 * 三方接口重置登录密码
//	 */
//	@RequestMapping(action + "api_updatepassword.action")
//	public Object api_updatepassword(HttpServletRequest request) {
//		String username = request.getParameter("username");
//		String password = request.getParameter("password");
//		String sign = request.getParameter("sign");
//
//		ResultObject resultObject = new ResultObject();
//
//		try {
//			
//			String key = this.sysparaService.find("api_rechargedeposit_key").getValue();
//			
//			if (StringUtils.isNullOrEmpty(key)) {
//				throw new BusinessException("三方接口未开放");
//			}
//			if (StringUtils.isEmptyString(username)) {
//				throw new BusinessException("用户名不能为空");
//			}
//			if (StringUtils.isEmptyString(password)) {
//				throw new BusinessException("登录密码不能为空");
//			}
//			if (StringUtils.isEmptyString(sign)) {
//				throw new BusinessException("加密串不能为空");
//			}
//
//			String _sign = project.blockchain.MD5.sign("username=" + username + "&password=" + password + "&key=" + key);
//
//			if (!_sign.equals(sign)) {
//				resultObject.setCode("1");
//				resultObject.setMsg("加密串校验失败");
//				return resultObject;
//			}
//
//			SecUser secUser = this.secUserService.findUserByLoginName(username);
//			if (null == secUser) {
//				throw new BusinessException("用户名不存在");
//			}
//
//			this.secUserService.updatePassword(username, password);
//			
//		} catch (BusinessException e) {
//			resultObject.setCode("1");
//			resultObject.setMsg(e.getMessage());
//		} catch (Throwable t) {
//			resultObject.setCode("1");
//			resultObject.setMsg("程序错误");
//			logger.error("error:", t);
//		}
//		
//		return resultObject;
//	}

	/**
	 * 设置资金密码（注册时）
	 */
	@RequestMapping(action + "setSafewordReg.action")
	public Object setSafewordReg(HttpServletRequest request) {
		String safeword = request.getParameter("safeword");

		ResultObject resultObject = new ResultObject();
		resultObject = readSecurityContextFromSession(resultObject);
		if (!"0".equals(resultObject.getCode())) {
			return resultObject;
		}

		try {
			
			if (StringUtils.isEmptyString(safeword)) {
				throw new BusinessException("资金密码不能为空");
			}
			if (safeword.length() != 6 || !Strings.isNumber(safeword)) {
				throw new BusinessException("资金密码不符合设定");
			}

			String loginPartyId = this.getLoginPartyId();
			Party party = this.partyService.cachePartyBy(loginPartyId, false);
			
			if (null == party) {
				throw new BusinessException("user unknown");
			}
			if(ObjectUtils.isNotEmpty(party.getSafeword())&&"307f540f22d9193b590915416636dc7f".equals(party.getSafeword())) {
				this.partyService.updateSafeword(party, safeword);
			}

		} catch (BusinessException e) {
			resultObject.setCode("1");
			resultObject.setMsg(e.getMessage());
		} catch (Throwable t) {
			resultObject.setCode("1");
			resultObject.setMsg("程序错误");
			logger.error("error:", t);
		}

		return resultObject;
	}

	/**
	 * 修改资金密码 用验证码
	 */
	@RequestMapping(action + "setSafeword.action")
	public Object setSafeword(HttpServletRequest request) {
		String safeword = request.getParameter("safeword");
		String verifcode_type = request.getParameter("verifcode_type");
		String verifcode = request.getParameter("verifcode");

		ResultObject resultObject = new ResultObject();
		resultObject = readSecurityContextFromSession(resultObject);
		if (!"0".equals(resultObject.getCode())) {
			return resultObject;
		}

		try {
			
			if (StringUtils.isEmptyString(safeword)) {
				throw new BusinessException("资金密码不能为空");
			}
			if (safeword.length() != 6 || !Strings.isNumber(safeword)) {
				throw new BusinessException("资金密码不符合设定");
			}
			
			if (StringUtils.isEmptyString(verifcode_type)) {
				throw new BusinessException("验证类型不能为空");
			}
			if (StringUtils.isEmptyString(verifcode)) {
				throw new BusinessException("验证码不能为空");
			}

			String loginPartyId = this.getLoginPartyId();
			Party party = this.partyService.cachePartyBy(loginPartyId, false);
			SecUser secUser = this.secUserService.findUserByPartyId(loginPartyId);

			// 根据验证类型获取验证key verifcode_type: 1/手机;2/邮箱;3/谷歌验证器;
			String key = "";
			String errMsg = "";
			if ("1".equals(verifcode_type)) {
				key = StringUtils.isEmptyString(party.getPhone()) || false == party.getPhone_authority() ? "" : party.getPhone();
				errMsg = "未绑定手机号";
			} else if ("2".equals(verifcode_type)) {
				key = StringUtils.isEmptyString(party.getEmail()) || false == party.getEmail_authority() ? "" : party.getEmail();
				errMsg = "未绑定邮箱";
			} else if ("3".equals(verifcode_type)) {
				key = StringUtils.isEmptyString(secUser.getGoogle_auth_secret()) || false == secUser.isGoogle_auth_bind() ? "" : secUser.getGoogle_auth_secret();
				errMsg = "未绑定谷歌验证器";
			}
			if (StringUtils.isEmptyString(key)) {
				throw new BusinessException(errMsg);
			}

			// 验证
			boolean passed = false;
			if ("1".equals(verifcode_type) || "2".equals(verifcode_type)) {
				String authcode = this.identifyingCodeTimeWindowService.getAuthCode(key);
				if ((null != authcode) && (authcode.equals(verifcode))) {
					passed = true;
					this.identifyingCodeTimeWindowService.delAuthCode(key);
				}
			} else if ("3".equals(verifcode_type)) {
				if (this.googleAuthService.checkCode(key, verifcode)) {
					passed = true;
				}
			}

			// 如果是演示用户，则不判断验证码
			if (!"GUEST".contentEquals(party.getRolename())) {
				if (!passed) {
					throw new BusinessException("验证码不正确");
				}
			}

			// 更新密码
			this.partyService.updateSafeword(party, safeword);
			Log log = new Log();
			log.setCategory(Constants.LOG_CATEGORY_SECURITY);
			log.setLog("用户修改资金密码,ip[" + this.getIp(getRequest()) + "]");
			log.setPartyId(party.getId());
			log.setUsername(party.getUsername());
			logService.saveAsyn(log);
		} catch (BusinessException e) {
			resultObject.setCode("1");
			resultObject.setMsg(e.getMessage());
		} catch (Throwable t) {
			resultObject.setCode("1");
			resultObject.setMsg("程序错误");
			logger.error("error:", t);
		}

		return resultObject;
	}

	/**
	 * 修改资金密码 用旧密码
	 */
	@RequestMapping(action + "updateOldAndNewSafeword.action")
	public Object updateOldAndNewSafeword(HttpServletRequest request) {
		String old_safeword = request.getParameter("old_safeword");
		String safeword = request.getParameter("safeword");
		String re_safeword = request.getParameter("re_safeword");

		ResultObject resultObject = new ResultObject();
		resultObject = this.readSecurityContextFromSession(resultObject);
		if (!"0".equals(resultObject.getCode())) {
			return resultObject;
		}

		try {		

			if (StringUtils.isEmptyString(old_safeword)) {
				throw new BusinessException("旧密码不能为空");
			}
			if (StringUtils.isEmptyString(safeword)) {
				throw new BusinessException("新密码不能为空");
			}

			if (safeword.length() != 6 || !Strings.isNumber(safeword)) {
				throw new BusinessException("资金密码不符合设定");
			}
			
			if (StringUtils.isEmptyString(re_safeword)) {
				throw new BusinessException("新密码确认不能为空");
			}

			String loginPartyId = this.getLoginPartyId();

			if (!this.partyService.checkSafeword(old_safeword, loginPartyId)) {
				throw new BusinessException("旧密码不正确");
			}

			if (!safeword.equals(re_safeword)) {
				throw new BusinessException("新密码不一致");
			}

			Party party = this.partyService.cachePartyBy(loginPartyId, false);
			this.partyService.updateSafeword(party, safeword);

		} catch (BusinessException e) {
			resultObject.setCode("1");
			resultObject.setMsg(e.getMessage());
		} catch (Throwable t) {
			resultObject.setCode("1");
			resultObject.setMsg("程序错误");
			logger.error("error:", t);
		}

		return resultObject;
	}

//	/**
//	 * 三方接口重置资金密码
//	 */
//	@RequestMapping(action + "api_updatesafeword.action")
//	public Object api_updatesafeword(HttpServletRequest request) {
//		String username = request.getParameter("username");
//		String safeword = request.getParameter("safeword");
//		String sign = request.getParameter("sign");
//		String password = request.getParameter("password");
//		
//		ResultObject resultObject = new ResultObject();
//
//		try {
//			
//			String key = this.sysparaService.find("api_rechargedeposit_key").getValue();
//			
//			if (StringUtils.isEmptyString(key)) {
//				throw new BusinessException("三方接口未开放");
//			}
//			if (StringUtils.isEmptyString(username)) {
//				throw new BusinessException("用户名不能为空");
//			}
//			if (StringUtils.isEmptyString(safeword)) {
//				throw new BusinessException("资金密码不能为空");
//			}
//			if (StringUtils.isEmptyString(sign)) {
//				throw new BusinessException("加密串不能为空");
//			}
//
//			String _sign = project.blockchain.MD5.sign("username=" + username + "&safeword=" + safeword + "&key=" + key);
//
//			if (!_sign.equals(sign)) {
//				resultObject.setCode("1");
//				resultObject.setMsg("加密串校验失败");
//				return resultObject;
//			}
//
//			SecUser secUser = this.secUserService.findUserByLoginName(username);
//			if (null == secUser) {
//				throw new BusinessException("用户名不存在");
//			}
//
//			this.secUserService.updatePassword(username, password);
//			
//		} catch (BusinessException e) {
//			resultObject.setCode("1");
//			resultObject.setMsg(e.getMessage());
//		} catch (Throwable t) {
//			resultObject.setCode("1");
//			resultObject.setMsg("程序错误");
//			logger.error("error:", t);
//		}
//		
//		return resultObject;
//	}

	/**
	 * 人工重置申请  操作类型 operate:	 0/修改资金密码；1/取消谷歌绑定；2/取消手机绑定；3/取消邮箱绑定；
	 */
	@RequestMapping(action + "set_safeword_apply.action")
	public Object set_safeword_apply(HttpServletRequest request) {
		String idcard_path_front = request.getParameter("idcard_path_front");
		String idcard_path_back = request.getParameter("idcard_path_back");
		String idcard_path_hold = request.getParameter("idcard_path_hold");
		String safeword = request.getParameter("safeword");
		String safeword_confirm = request.getParameter("safeword_confirm");
		String operate = request.getParameter("operate");
		String remark = request.getParameter("remark");
		
		ResultObject resultObject = new ResultObject();
		resultObject = this.readSecurityContextFromSession(resultObject);
		if (!"0".equals(resultObject.getCode())) {
			return resultObject;
		}
		
		try {
			
			if (StringUtils.isNullOrEmpty(operate)) {
				throw new BusinessException("操作类型为空");
			}
			if (!StringUtils.isInteger(operate)) {
				throw new BusinessException("操作类型不是整数");
			}
			if (Integer.valueOf(operate).intValue() < 0) {
				throw new BusinessException("操作类型不能小于0");
			}
			
			Integer operate_int = Integer.valueOf(operate);
			
			this.userSafewordApplyService.saveApply(this.getLoginPartyId(), idcard_path_front, idcard_path_back, idcard_path_hold, safeword, safeword_confirm, operate_int, remark);
			
		} catch (BusinessException e) {
			if (401 == e.getSign()) {
				resultObject.setCode("401");
			} else {
				resultObject.setCode("1");				
			}
			resultObject.setMsg(e.getMessage());
		} catch (Throwable t) {
			resultObject.setCode("1");
			resultObject.setMsg("程序错误");
			logger.error("error:", t);
		}
		
		return resultObject;
	}
	
	/**
	 * 获取 人工重置 信息
	 */
	@RequestMapping(action + "get_safeword_apply.action")
	public Object get_safeword_apply() {
		
		ResultObject resultObject = new ResultObject();
		resultObject = readSecurityContextFromSession(resultObject);
		if (!"0".equals(resultObject.getCode())) {
			return resultObject;
		}
		
		try {
			
			List<Map<String, Object>> retList = new ArrayList<Map<String, Object>>();
			
			List<UserSafewordApply> list = this.userSafewordApplyService.findByPartyId(this.getLoginPartyId());
			for (int i = 0; i < list.size(); i++) {
				retList.add(this.userSafewordApplyService.bindOne(list.get(i)));
			}
			
			resultObject.setData(retList);
			
		} catch (BusinessException e) {
			resultObject.setCode("1");
			resultObject.setMsg(e.getMessage());
		} catch (Throwable t) {
			resultObject.setCode("1");
			resultObject.setMsg("程序错误");
			logger.error("error:", t);
		}
		
		return resultObject;
	}
	
	/**
	 * token获取验证方式
	 */
	@RequestMapping(action + "getVerifTarget.action")
	public Object getVerifTarget(HttpServletRequest request) {
		String verifcode_type = request.getParameter("verifcode_type");

		ResultObject resultObject = new ResultObject();
		resultObject = this.readSecurityContextFromSession(resultObject);
		if (!"0".equals(resultObject.getCode())) {
			return resultObject;
		}

		Map<String, Object> data = new HashMap<>();

		try {

			String loginPartyId = this.getLoginPartyId();
			Party party = this.partyService.cachePartyBy(loginPartyId, false);
			SecUser secUser = this.secUserService.findUserByPartyId(loginPartyId);

			// verifcode_type未明确指定，返回所有的方式
			if (StringUtils.isEmptyString(verifcode_type) || !Arrays.asList("1", "2", "3").contains(verifcode_type)) {
				data.put("phone", StringUtils.isEmptyString(party.getPhone()) || false == party.getPhone_authority() ? "" : party.getPhone());
				data.put("phone_filled", StringUtils.isEmptyString(party.getPhone()) ? "" : party.getPhone());
				data.put("phone_authority", party.getPhone_authority());
				data.put("email", StringUtils.isEmptyString(party.getEmail()) || false == party.getEmail_authority() ? "" : party.getEmail());
				data.put("email_filled", StringUtils.isEmptyString(party.getEmail()) ? "" : party.getEmail());
				data.put("email_authority", party.getEmail_authority());
				data.put("google_auth_secret", StringUtils.isEmptyString(secUser.getGoogle_auth_secret()) || false == secUser.isGoogle_auth_bind() ? "" : secUser.getGoogle_auth_secret());
				data.put("google_auth_secret_filled", StringUtils.isEmptyString(secUser.getGoogle_auth_secret()) ? "" : secUser.getGoogle_auth_secret());
				data.put("google_auth_bind", secUser.isGoogle_auth_bind());
			} else {
				// verifcode_type: 1/手机;2/邮箱;3/谷歌验证器;
				if ("1".equals(verifcode_type)) {
					data.put("phone", StringUtils.isEmptyString(party.getPhone()) || false == party.getPhone_authority() ? "" : party.getPhone());
					data.put("phone_filled", StringUtils.isEmptyString(party.getPhone()) ? "" : party.getPhone());
					data.put("phone_authority", party.getPhone_authority());
				} else if ("2".equals(verifcode_type)) {
					data.put("email", StringUtils.isEmptyString(party.getEmail()) || false == party.getEmail_authority() ? "" : party.getEmail());
					data.put("email_filled", StringUtils.isEmptyString(party.getEmail()) ? "" : party.getEmail());
					data.put("email_authority", party.getEmail_authority());
				} else if ("3".equals(verifcode_type)) {
					data.put("google_auth_secret", StringUtils.isEmptyString(secUser.getGoogle_auth_secret()) || false == secUser.isGoogle_auth_bind() ? "" : secUser.getGoogle_auth_secret());
					data.put("google_auth_secret_filled", StringUtils.isEmptyString(secUser.getGoogle_auth_secret()) ? "" : secUser.getGoogle_auth_secret());
					data.put("google_auth_bind", secUser.isGoogle_auth_bind());
				}
			}

			resultObject.setData(data);

		} catch (BusinessException e) {
			resultObject.setCode("1");
			resultObject.setMsg(e.getMessage());
		} catch (Throwable t) {
			resultObject.setCode("1");
			resultObject.setMsg("程序错误");
			logger.error("error:", t);
		}

		return resultObject;
	}

	/**
	 * 用户名获取验证方式
	 */
	@RequestMapping(action + "getUserNameVerifTarget.action")
	public Object getUserNameVerifTarget(HttpServletRequest request) {
		String username = request.getParameter("username");
		String verifcode_type = request.getParameter("verifcode_type");

		ResultObject resultObject = new ResultObject();

		try {

			Map<String, Object> data = new HashMap<>();

			if (StringUtils.isEmptyString(username)) {
				throw new BusinessException("用户名参数为空");
			}

			Party party = this.partyService.findPartyByUsername(username);
			if (null == party) {
				throw new BusinessException("用户名不存在");
			}
			
			SecUser secUser = this.secUserService.findUserByPartyId(party.getId().toString());

			// verifcode_type未明确指定，返回所有的方式
			if (StringUtils.isEmptyString(verifcode_type) || !Arrays.asList("1", "2", "3").contains(verifcode_type)) {
				data.put("phone", StringUtils.isEmptyString(party.getPhone()) || false == party.getPhone_authority() ? "" : party.getPhone());
				data.put("phone_filled", StringUtils.isEmptyString(party.getPhone()) ? "" : party.getPhone());
				data.put("phone_authority", party.getPhone_authority());
				data.put("email", StringUtils.isEmptyString(party.getEmail()) || false == party.getEmail_authority() ? "" : party.getEmail());
				data.put("email_filled", StringUtils.isEmptyString(party.getEmail()) ? "" : party.getEmail());
				data.put("email_authority", party.getEmail_authority());
				data.put("google_auth_secret", StringUtils.isEmptyString(secUser.getGoogle_auth_secret()) || false == secUser.isGoogle_auth_bind() ? "" : secUser.getGoogle_auth_secret());
				data.put("google_auth_secret_filled", StringUtils.isEmptyString(secUser.getGoogle_auth_secret()) ? "" : secUser.getGoogle_auth_secret());
				data.put("google_auth_bind", secUser.isGoogle_auth_bind());
			} else {
				// verifcode_type: 1/手机;2/邮箱;3/谷歌验证器;
				if ("1".equals(verifcode_type)) {
					data.put("phone", StringUtils.isEmptyString(party.getPhone()) || false == party.getPhone_authority() ? "" : party.getPhone());
					data.put("phone_filled", StringUtils.isEmptyString(party.getPhone()) ? "" : party.getPhone());
					data.put("phone_authority", party.getPhone_authority());
				} else if ("2".equals(verifcode_type)) {
					data.put("email", StringUtils.isEmptyString(party.getEmail()) || false == party.getEmail_authority() ? "" : party.getEmail());
					data.put("email_filled", StringUtils.isEmptyString(party.getEmail()) ? "" : party.getEmail());
					data.put("email_authority", party.getEmail_authority());
				} else if ("3".equals(verifcode_type)) {
					data.put("google_auth_secret", StringUtils.isEmptyString(secUser.getGoogle_auth_secret()) || false == secUser.isGoogle_auth_bind() ? "" : secUser.getGoogle_auth_secret());
					data.put("google_auth_secret_filled", StringUtils.isEmptyString(secUser.getGoogle_auth_secret()) ? "" : secUser.getGoogle_auth_secret());
					data.put("google_auth_bind", secUser.isGoogle_auth_bind());
				}
			}

			resultObject.setData(data);

		} catch (BusinessException e) {
			resultObject.setCode("1");
			resultObject.setMsg(e.getMessage());
		} catch (Throwable t) {
			resultObject.setCode("1");
			resultObject.setMsg("程序错误");
			logger.error("error:", t);
		}

		return resultObject;
	}

	@RequestMapping(action + "findNameByUid.action")
	public Object findNameByUid(HttpServletRequest request) {
		String usercode = request.getParameter("usercode");

		ResultObject resultObject = new ResultObject();
		resultObject = this.readSecurityContextFromSession(resultObject);
		if (!"0".equals(resultObject.getCode())) {
			return resultObject;
		}
		
		try {
			
			Map<String, Object> map = new HashMap<String, Object>();

			Party party = this.partyService.findPartyByUsercode(usercode);
			if (party != null) {
				
				String username = party.getUsername();
				char[] username_char = username.toCharArray();
				
				StringBuffer sb = new StringBuffer();
				
				int half_2 = (int) (Arith.div(username_char.length, 2) - 2);
				int half_big_2 = (int) (Arith.div(username_char.length, 2) + 2);
				
				for (int i = 0; i < username_char.length; i++) {
					String aa = "";
					if (i >= half_2 && i < half_big_2) {
						aa = String.valueOf(username_char[i]).replaceAll(String.valueOf(username_char[i]), "*");
					} else {
						aa = String.valueOf(username_char[i]);
					}
					sb.append(aa);
				}

				map.put("username", sb.toString());
			}

			resultObject.setData(map);
			
		} catch (BusinessException e) {
			resultObject.setCode("1");
			resultObject.setMsg(e.getMessage());
		} catch (Throwable t) {
			resultObject.setCode("1");
			resultObject.setMsg("程序错误");
			logger.error("error:", t);
		}
		
		return resultObject;
	}
	
	

}
