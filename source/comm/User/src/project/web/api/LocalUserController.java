package project.web.api;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import kernel.exception.BusinessException;
import kernel.util.DateUtils;
import kernel.util.ImageVerificationCodeUtil;
import kernel.util.ImageVerificationEndecrypt;
import kernel.util.StringUtils;
import kernel.util.ThreadUtils;
import kernel.util.UUIDGenerator;
import kernel.web.BaseAction;
import kernel.web.ResultObject;
import project.Constants;
import project.ddos.IpMenuService;
import project.log.Log;
import project.log.LogService;
import project.party.PartyService;
import project.party.model.Party;
import project.party.model.UserRecom;
import project.party.recom.UserRecomService;
import project.syspara.Syspara;
import project.syspara.SysparaService;
import project.user.LocalNormalReg;
import project.user.LocalUserService;
import project.user.QRGenerateService;
import project.user.UserService;
import project.user.captcha.GeetestService;
import project.user.idcode.IdentifyingCodeTimeWindowService;
import project.user.kyc.Kyc;
import project.user.kyc.KycHighLevel;
import project.user.kyc.KycHighLevelService;
import project.user.kyc.KycService;
import project.user.token.TokenService;
import security.SecUser;
import security.internal.SecUserService;
import util.IpUtil;
import util.LockFilter;
import util.RegexUtil;
import util.Strings;

@RestController
@CrossOrigin
public class LocalUserController extends BaseAction {
	
	private Logger logger = LoggerFactory.getLogger(LocalUserController.class);
	
	@Autowired
	private PartyService partyService;
	@Autowired
	private QRGenerateService qRGenerateService;
	@Autowired
	private KycService kycService;
	@Autowired
	private KycHighLevelService kycHighLevelService;
	@Autowired
	private SysparaService sysparaService;
	@Autowired
	private UserService userService;
	@Autowired
	private LocalUserService localUserService;
	@Autowired
	private SecUserService secUserService;
	@Autowired
	private LogService logService;
	@Autowired
	protected TokenService tokenService;
	@Autowired
	private IpMenuService ipMenuService;
	@Autowired
	private IdentifyingCodeTimeWindowService identifyingCodeTimeWindowService;
	@Autowired
	private GeetestService geetestService;
	@Autowired
	private UserRecomService userRecomService;
	@Autowired
	protected JdbcTemplate jdbcTemplate;
	
	private final String action = "api/localuser!";
	
	/**
	 * 交易所用户注册（Geetest滑动图片验证方式）
	 * 备注：可以用，但是目前没用到
	 */
	@RequestMapping(action + "register_username_geetest.action")
	public Object register_username_geetest(HttpServletRequest request) {
		
		String username = request.getParameter("username").replace(" ", "");
		String password = request.getParameter("password").replace(" ", "");
		String safeword = request.getParameter("safeword").replace(" ", "");
		String usercode = request.getParameter("usercode");
		
		String challenge = request.getParameter("geetest_challenge");
		String validate = request.getParameter("geetest_validate");
		String seccode = request.getParameter("geetest_seccode");
		String gt_server_status = request.getParameter("gt_server_status");

		ResultObject resultObject = new ResultObject();
		
		boolean lock = false;
		
		try {			
			
			if (!LockFilter.add(username)) {
				throw new BusinessException("重复提交");
			}
			
			lock = true;
			
			String error = this.validateParamUsername(username, password);
			if (!StringUtils.isNullOrEmpty(error)) {
				throw new BusinessException(error);
			}
			
			if (StringUtils.isEmptyString(safeword)) {
				throw new BusinessException("资金密码不能为空");
			}
			if (safeword.length() != 6 || !Strings.isNumber(safeword)) {
				throw new BusinessException("资金密码不符合设定");
			}
			
			Syspara geetest_open = this.sysparaService.find("geetest_open");
			if (null != geetest_open && true == geetest_open.getBoolean()) {
				
				HashMap<String, String> retGeetest = this.geetestVerif(challenge, validate, seccode, gt_server_status);
				if ("fail".equals(retGeetest.get("status"))) {
					throw new BusinessException("图形验证错误，请重新再试");
				}
			}
			
			LocalNormalReg reg = new LocalNormalReg();
			reg.setUsername(username);
			reg.setPassword(password);
			reg.setReco_usercode(usercode);
			reg.setSafeword(safeword);
			this.localUserService.saveRegisterUsername(reg);

			SecUser secUser = this.secUserService.findUserByLoginName(username);
			
			Log log = new Log();
			log.setCategory(Constants.LOG_CATEGORY_SECURITY);
			log.setLog("用户无验证码注册,ip[" + this.getIp(getRequest()) + "]");
			log.setPartyId(secUser.getPartyId());
			log.setUsername(username);
			this.logService.saveAsyn(log);

			// 注册完直接登录返回token
			String token = this.tokenService.savePut(secUser.getPartyId(),secUser.getUsername());
			this.userService.online(secUser.getPartyId());
			this.ipMenuService.saveIpMenuWhite(this.getIp());
			
			Party party = this.partyService.cachePartyBy(secUser.getPartyId(), true);
			
			Map<String, Object> data = new HashMap<String, Object>();
			data.put("token", token);
			data.put("username", secUser.getUsername());
			data.put("usercode", party.getUsercode());
			
			party.setLogin_ip(this.getIp(getRequest()));
			this.partyService.update(party);
			
			resultObject.setData(data);
			
		} catch (BusinessException e) {
			resultObject.setCode("1");
			resultObject.setMsg(e.getMessage());
		} catch (Throwable t) {
			logger.error("UserAction.register error ", t);
			this.error = ("[ERROR] " + t.getMessage());
			resultObject.setCode("1");
			resultObject.setMsg(this.error);
		} finally {
			if (lock) {
				LockFilter.remove(username);
			}
		}

		return resultObject;
	}
	
	/**
	 * 交易所用户注册（验证码方式）
	 * 备注：可以用，但是目前没用到
	 */
	@RequestMapping(action + "register_username.action")
	public Object register_username(HttpServletRequest request) {
		
		String username = request.getParameter("username").replace(" ", "");
		String password = request.getParameter("password").replace(" ", "");
		String safeword = request.getParameter("safeword").replace(" ", "");
		String usercode = request.getParameter("usercode");
		String code = request.getParameter("code");
		String key = request.getParameter("key");

		ResultObject resultObject = new ResultObject();
		String error = "";
		boolean lock = false;
		try {
			
			if (!LockFilter.add(username)) {
				resultObject.setCode("1");
				resultObject.setMsg("重复提交");
				return resultObject;
			}
			lock = true;
			error = validateParamUsername(username, password);
			if (!StringUtils.isNullOrEmpty(error)) {
				resultObject.setCode("1");
				resultObject.setMsg(error);
				return resultObject;
			}
			
			if (StringUtils.isEmptyString(safeword)) {
				throw new BusinessException("资金密码不能为空");
			}
			if (safeword.length() != 6 || !Strings.isNumber(safeword)) {
				throw new BusinessException("资金密码不符合设定");
			}
			
			boolean register_image_code_button = sysparaService.find("register_image_code_button").getBoolean();
			if (register_image_code_button) {
				if (StringUtils.isEmptyString(code) || StringUtils.isEmptyString(key)) {
					throw new BusinessException("验证码不能为空");
				} else {
					String decryptCode = ImageVerificationEndecrypt.decryptDES(code, key + "key");
					if (!decryptCode.equalsIgnoreCase(userService.cacheImageCode(key))) {
						logger.info("ip:{" + this.getIp() + "},图片验证码不正确,paramcode:{" + decryptCode + "},truecode:{"
								+ userService.cacheImageCode(key) + "}");
						throw new BusinessException("验证码错误");
					}
				}
			}
			LocalNormalReg reg = new LocalNormalReg();
			reg.setUsername(username);
			reg.setPassword(password);
			reg.setReco_usercode(usercode);
			reg.setSafeword(safeword);

			this.localUserService.saveRegisterUsername(reg);

			SecUser secUser = secUserService.findUserByLoginName(username);
			Log log = new Log();

			log.setCategory(Constants.LOG_CATEGORY_SECURITY);
			log.setLog("用户无验证码注册,ip[" + this.getIp(getRequest()) + "]");
			log.setPartyId(secUser.getPartyId());
			log.setUsername(username);
			logService.saveAsyn(log);

			// 注册完直接登录返回token
			String token = tokenService.savePut(secUser.getPartyId(),secUser.getUsername());
			userService.online(secUser.getPartyId());
			ipMenuService.saveIpMenuWhite(this.getIp());
			Party party = this.partyService.cachePartyBy(secUser.getPartyId(), true);
			Map<String, Object> data = new HashMap<String, Object>();
			data.put("token", token);
			data.put("username", secUser.getUsername());
			data.put("usercode", party.getUsercode());
			party.setLogin_ip(this.getIp(getRequest()));
			this.partyService.update(party);
			resultObject.setData(data);
		} catch (BusinessException e) {
			resultObject.setCode("1");
			resultObject.setMsg(e.getMessage());
		} catch (Throwable t) {
			logger.error("UserAction.register error ", t);
			this.error = ("[ERROR] " + t.getMessage());
			resultObject.setCode("1");
			resultObject.setMsg(this.error);
		} finally {
			if (lock) {
				LockFilter.remove(username);
			}
		}

		return resultObject;
	}

	/**
	 * 手机/邮箱注册接口
	 */
	@RequestMapping(action + "register.action")
	public Object register(HttpServletRequest request) {
		String username = request.getParameter("username").replace(" ", "");
		String password = request.getParameter("password").replace(" ", "");
		String safeword = request.getParameter("safeword").replace(" ", "");
		String verifcode = request.getParameter("verifcode");
		String usercode = request.getParameter("usercode");
		// 注册类型：1/手机；2/邮箱；
		String type = request.getParameter("type");
		
		ResultObject resultObject = new ResultObject();
		
		boolean lock = false;
		
		try {

			if (!LockFilter.add(username)) {
				throw new BusinessException("重复提交");
			}
			
			lock = true;
			
			String error = this.validateParam(username, verifcode, password, type);
			if (!StringUtils.isNullOrEmpty(error)) {
				throw new BusinessException(error);
			}
			
			if (StringUtils.isEmptyString(safeword)) {
				throw new BusinessException("资金密码不能为空");
			}
			if (safeword.length() != 6 || !Strings.isNumber(safeword)) {
				throw new BusinessException("资金密码不符合设定");
			}
			
			LocalNormalReg reg = new LocalNormalReg();
			reg.setUsername(username);
			reg.setPassword(password);
			reg.setSafeword(safeword);
			reg.setReco_usercode(usercode);
			reg.setIdentifying_code(verifcode);

			this.localUserService.saveRegister(reg, type);

			SecUser secUser = this.secUserService.findUserByLoginName(username);
			
			project.log.Log log = new project.log.Log();
			log.setCategory(Constants.LOG_CATEGORY_SECURITY);
			log.setLog("用户注册,ip[" + this.getIp(getRequest()) + "]");
			log.setPartyId(secUser.getPartyId());
			log.setUsername(username);
			this.logService.saveAsyn(log);

			// 注册完直接登录返回token
			String token = this.tokenService.savePut(secUser.getPartyId(),secUser.getUsername());
			
			this.userService.online(secUser.getPartyId());
			this.ipMenuService.saveIpMenuWhite(this.getIp());
			
			Party party = this.partyService.cachePartyBy(secUser.getPartyId(), true);
			
			Map<String, Object> data = new HashMap<String, Object>();
			data.put("token", token);
			data.put("username", secUser.getUsername());
			data.put("usercode", party.getUsercode());
			
			party.setLogin_ip(this.getIp(getRequest()));
			this.partyService.update(party);
			
			ThreadUtils.sleep(500);

			resultObject.setData(data);

		} catch (BusinessException e) {
			resultObject.setCode("1");
			resultObject.setMsg(e.getMessage());
		} catch (Throwable t) {
			logger.error("UserAction.register error ", t);
			resultObject.setCode("1");
			resultObject.setMsg("[ERROR] " + t.getMessage());
		} finally {
			if (lock) {
				LockFilter.remove(username);
			}
		}
		
		return resultObject;
	}

	/**
	 * 手机/邮箱/用户名注册（无验证码）
	 */
	@RequestMapping(action + "registerNoVerifcode.action")
	public Object registerNoVerifcode(HttpServletRequest request) {
		String username = request.getParameter("username").replace(" ", "");
		String password = request.getParameter("password").replace(" ", "");
		String re_password = request.getParameter("re_password").replace(" ", "");
		String usercode = request.getParameter("usercode");
		// 注册类型：1/手机；2/邮箱；3/用户名；
		String type = request.getParameter("type");
		
		ResultObject resultObject = new ResultObject();
		
		boolean lock = false;
		
		try {

			if (!LockFilter.add(username)) {
				throw new BusinessException("重复提交");
			}
			
			lock = true;
			
			if (StringUtils.isEmptyString(username)) {
				throw new BusinessException("用户名不能为空");
			}

			if (StringUtils.isEmptyString(password)) {
				throw new BusinessException("登录密码不能为空");
			}

			if (StringUtils.isEmptyString(re_password)) {
				throw new BusinessException("密码确认不能为空");
			}

			if (password.length() < 6 || password.length() > 12 || re_password.length() < 6 || re_password.length() > 12) {
				throw new BusinessException("密码必须6-12位");
			}

			if (!password.equals(re_password)) {
				throw new BusinessException("两次输入的密码不相同");
			}
			
			if (StringUtils.isEmptyString(type) || !Arrays.asList("1", "2", "3").contains(type)) {
				throw new BusinessException("类型不能为空");
			}
			
			// 手机
			if ("1".equals(type)) {
				if(!isValidPhone(username)) {
					throw new BusinessException("not a valid Phone!");
				}
			}
			
			// 邮箱
			if ("2".equals(type)) {
				if(!isValidEmail(username)) {
					throw new BusinessException("not a valid Email!");
				}
			}
			
			// 用户名
			if ("3".equals(type)) {
				if(!isValidUsername(username)) {
					throw new BusinessException("not a valid username!");
				}
			}
			
			String ip = this.getIp();
			if (!IpUtil.isCorrectIpRegular(ip)) {
				logger.error("校验IP不合法,参数{}", ip);
				throw new BusinessException("校验IP不合法");
			}

			LocalNormalReg reg = new LocalNormalReg();
			reg.setUsername(username);
			reg.setPassword(password);
			reg.setSafeword("000000");
			reg.setReco_usercode(usercode);

			this.localUserService.saveRegisterNoVerifcode(reg, type);

			SecUser secUser = this.secUserService.findUserByLoginName(username);
			
			project.log.Log log = new project.log.Log();
			log.setCategory(Constants.LOG_CATEGORY_SECURITY);
			log.setLog("用户注册,ip[" + this.getIp(getRequest()) + "]");
			log.setPartyId(secUser.getPartyId());
			log.setUsername(username);
			this.logService.saveAsyn(log);

			// 注册完直接登录返回token
			String token = this.tokenService.savePut(secUser.getPartyId(),secUser.getUsername());
			
			this.userService.online(secUser.getPartyId());
			this.ipMenuService.saveIpMenuWhite(this.getIp());
			
			Party party = this.partyService.cachePartyBy(secUser.getPartyId(), true);
			
			Map<String, Object> data = new HashMap<String, Object>();
			data.put("token", token);
			data.put("username", secUser.getUsername());
			data.put("usercode", party.getUsercode());
			
			party.setLogin_ip(this.getIp(getRequest()));
			this.partyService.update(party);
			
			ThreadUtils.sleep(500);

			resultObject.setData(data);

		} catch (BusinessException e) {
			resultObject.setCode("1");
			resultObject.setMsg(e.getMessage());
		} catch (Throwable t) {
			logger.error("UserAction.register error ", t);
			resultObject.setCode("1");
			resultObject.setMsg("[ERROR] " + t.getMessage());
		} finally {
			if (lock) {
				LockFilter.remove(username);
			}
		}
		
		return resultObject;
	}
	
	/**
	 * 体验用户注册
	 */
	@RequestMapping(action + "registerTest.action")
	public Object registerTest(HttpServletRequest request) {
		String username = request.getParameter("username").replace(" ", "");
		String password = request.getParameter("password").replace(" ", "");
		String re_password = request.getParameter("re_password").replace(" ", "");
		String usercode = request.getParameter("usercode");
		//体验用户默认是账号类型
		String type = "3";
		
		ResultObject resultObject = new ResultObject();
		
		boolean lock = false;
		
		try {

			if (!LockFilter.add(username)) {
				throw new BusinessException("重复提交");
			}
			
			lock = true;
			
			if (StringUtils.isEmptyString(username)) {
				throw new BusinessException("用户名不能为空");
			}

			if (StringUtils.isEmptyString(password)) {
				throw new BusinessException("登录密码不能为空");
			}

			if (StringUtils.isEmptyString(re_password)) {
				throw new BusinessException("密码确认不能为空");
			}			

			if (password.length() < 6 || password.length() > 12 || re_password.length() < 6 || re_password.length() > 12) {
				throw new BusinessException("密码必须6-12位");
			}

			if (!password.equals(re_password)) {
				throw new BusinessException("两次输入的密码不相同");
			}			
			
			// 用户名
			if ("3".equals(type)) {
				if(!isValidUsername(username)) {
					throw new BusinessException("not a valid username!");
				}
			}
			
			String ip = this.getIp();
			if (!IpUtil.isCorrectIpRegular(ip)) {
				logger.error("校验IP不合法,参数{}", ip);
				throw new BusinessException("校验IP不合法");
			}

			LocalNormalReg reg = new LocalNormalReg();
			reg.setUsername(username);
			reg.setPassword(password);
			reg.setSafeword("000000");
			reg.setReco_usercode(usercode);

			this.localUserService.saveRegisterTest(reg, type);

			SecUser secUser = this.secUserService.findUserByLoginName(username);
			
			project.log.Log log = new project.log.Log();
			log.setCategory(Constants.LOG_CATEGORY_SECURITY);
			log.setLog("用户注册,ip[" + this.getIp(getRequest()) + "]");
			log.setPartyId(secUser.getPartyId());
			log.setUsername(username);
			this.logService.saveAsyn(log);

			// 注册完直接登录返回token
			String token = this.tokenService.savePut(secUser.getPartyId(),secUser.getUsername());
			
			this.userService.online(secUser.getPartyId());
			this.ipMenuService.saveIpMenuWhite(this.getIp());
			
			Party party = this.partyService.cachePartyBy(secUser.getPartyId(), true);
			
			Map<String, Object> data = new HashMap<String, Object>();
			data.put("token", token);
			data.put("username", secUser.getUsername());
			data.put("usercode", party.getUsercode());
			
			party.setLogin_ip(this.getIp(getRequest()));
			this.partyService.update(party);
			
			ThreadUtils.sleep(500);

			resultObject.setData(data);

		} catch (BusinessException e) {
			resultObject.setCode("1");
			resultObject.setMsg(e.getMessage());
		} catch (Throwable t) {
			logger.error("UserAction.register error ", t);
			resultObject.setCode("1");
			resultObject.setMsg("[ERROR] " + t.getMessage());
		} finally {
			if (lock) {
				LockFilter.remove(username);
			}
		}
		
		return resultObject;
	}


	/**
	 * 手机/邮箱/用户名注册（无验证码，填资金密码）
	 */
	@RequestMapping(action + "registerNoVerifcodeSafeword.action")
	public Object registerNoVerifcodeSafeword(HttpServletRequest request) {
		String username = request.getParameter("username").replace(" ", "");
		String password = request.getParameter("password").replace(" ", "");
		String re_password = request.getParameter("re_password").replace(" ", "");
		String usercode = request.getParameter("usercode");
		// 注册类型：1/手机；2/邮箱；3/用户名；
		String type = request.getParameter("type");
		// 资金密码选填，不填默认 000000
		String safeword = request.getParameter("safeword").replace(" ", "");
		String re_safeword = request.getParameter("re_safeword").replace(" ", "");
		
		ResultObject resultObject = new ResultObject();
		
		boolean lock = false;
		
		try {

			if (!LockFilter.add(username)) {
				throw new BusinessException("重复提交");
			}
			
			lock = true;
			
			if (StringUtils.isEmptyString(username)) {
				throw new BusinessException("用户名不能为空");
			}

			if (StringUtils.isEmptyString(password)) {
				throw new BusinessException("登录密码不能为空");
			}

			if (StringUtils.isEmptyString(re_password)) {
				throw new BusinessException("密码确认不能为空");
			}

			if (password.length() < 6 || password.length() > 12 || re_password.length() < 6 || re_password.length() > 12) {
				throw new BusinessException("密码必须6-12位");
			}

			if (!password.equals(re_password)) {
				throw new BusinessException("两次输入的密码不相同");
			}
			
			if (StringUtils.isEmptyString(safeword)) {
				safeword = "000000";
			}
			if (StringUtils.isEmptyString(re_safeword)) {
				re_safeword = "000000";
			}
			if (safeword.length() != 6 || !Strings.isNumber(safeword)) {
				throw new BusinessException("资金密码不符合设定");
			}

			if (!safeword.equals(re_safeword)) {
				throw new BusinessException("两次输入的资金密码不相同");
			}
			
			if (StringUtils.isEmptyString(type) || !Arrays.asList("1", "2", "3").contains(type)) {
				throw new BusinessException("类型不能为空");
			}
			
			// 手机
			if ("1".equals(type)) {
				if(!isValidPhone(username)) {
					throw new BusinessException("not a valid Phone!");
				}
			}
			
			// 邮箱
			if ("2".equals(type)) {
				if(!isValidEmail(username)) {
					throw new BusinessException("not a valid Email!");
				}
			}
			
			// 用户名
			if ("3".equals(type)) {
				if(!isValidUsername(username)) {
					throw new BusinessException("not a valid username!");
				}
			}
			
			String ip = this.getIp();
			if (!IpUtil.isCorrectIpRegular(ip)) {
				logger.error("校验IP不合法,参数{}", ip);
				throw new BusinessException("校验IP不合法");
			}

			LocalNormalReg reg = new LocalNormalReg();
			reg.setUsername(username);
			reg.setPassword(password);			
			reg.setSafeword(safeword);
			reg.setReco_usercode(usercode);

			this.localUserService.saveRegisterNoVerifcode(reg, type);

			SecUser secUser = this.secUserService.findUserByLoginName(username);
			
			project.log.Log log = new project.log.Log();
			log.setCategory(Constants.LOG_CATEGORY_SECURITY);
			log.setLog("用户注册,ip[" + this.getIp(getRequest()) + "]");
			log.setPartyId(secUser.getPartyId());
			log.setUsername(username);
			this.logService.saveAsyn(log);

			// 注册完直接登录返回token
			String token = this.tokenService.savePut(secUser.getPartyId(),secUser.getUsername());
			
			this.userService.online(secUser.getPartyId());
			this.ipMenuService.saveIpMenuWhite(this.getIp());
			
			Party party = this.partyService.cachePartyBy(secUser.getPartyId(), true);
			
			Map<String, Object> data = new HashMap<String, Object>();
			data.put("token", token);
			data.put("username", secUser.getUsername());
			data.put("usercode", party.getUsercode());
			
			party.setLogin_ip(this.getIp(getRequest()));
			this.partyService.update(party);
			
			ThreadUtils.sleep(500);

			resultObject.setData(data);

		} catch (BusinessException e) {
			resultObject.setCode("1");
			resultObject.setMsg(e.getMessage());
		} catch (Throwable t) {
			logger.error("UserAction.register error ", t);
			resultObject.setCode("1");
			resultObject.setMsg("[ERROR] " + t.getMessage());
		} finally {
			if (lock) {
				LockFilter.remove(username);
			}
		}
		
		return resultObject;
	}
	
	/**
	 * 绑定邮箱or手机-确认
	 */
	@RequestMapping(action + "bindEmailOrPhone.action")
	public Object bindEmailOrPhone(HttpServletRequest request) {
		String target = request.getParameter("target");
		String verifcode = request.getParameter("verifcode");
		String phone = request.getParameter("phone");
		String email = request.getParameter("email");
		
		ResultObject resultObject = new ResultObject();
		resultObject = this.readSecurityContextFromSession(resultObject);
		if (!"0".equals(resultObject.getCode())) {
			return resultObject;
		}
		
		try {
			
			// 验证码
			Party party = this.partyService.cachePartyBy(this.getLoginPartyId(), true);
			String key = target;
			String authcode = this.identifyingCodeTimeWindowService.getAuthCode(key);
			
			String bind_phone_email_ver = this.sysparaService.find("bind_phone_email_ver").getValue();

			// 不是演示用户 && 绑定手机邮箱号是否需要验证：1需要，2不需要
			if (!"GUEST".contentEquals(party.getRolename()) && "1".equals(bind_phone_email_ver)) {
				if ((null == authcode) || (!authcode.equals(verifcode))) {
					resultObject.setCode("1");
					resultObject.setMsg("验证码不正确");
					return resultObject;
				}
			}
			
			if (target.indexOf("@") == -1) {
				// 绑定手机				
//				this.localUserService.savePhone(target, String.valueOf(party.getId()));

	            // 电话绑定成功
	            party.setPhone(phone);
	            party.setPhone_authority(true);
				
				// 获取用户系统等级：1/新注册；2/邮箱谷歌手机其中有一个已验证；3/用户实名认证； 4/用户高级认证；
				int userLevelSystem = this.partyService.getUserLevelByAuth(party);

				// 十进制个位表示系统级别：1/新注册；2/邮箱谷歌手机其中有一个已验证；3/用户实名认证；4/用户高级认证；
				// 十进制十位表示自定义级别：对应在前端显示为如VIP1 VIP2等级、黄金 白银等级；
				// 如：级别11表示：新注册的前端显示为VIP1；
				int userLevel = party.getUser_level();
				party.setUser_level(((int) Math.floor(userLevel / 10)) * 10 + userLevelSystem);
				
				this.partyService.update(party);
				
			} else {
				// 绑定邮箱
//				this.localUserService.saveEmail(target, String.valueOf(party.getId()));
				
	            // 邮箱绑定成功
	            party.setEmail(email);
	            party.setEmail_authority(true);

	            // 获取用户等级 1/新注册；2/邮箱谷歌手机其中有一个已验证；3/用户实名认证； 4/用户高级认证；
	            party.setUser_level(this.partyService.getUserLevelByAuth(party));

	            this.partyService.update(party);
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
	 * 电话绑定
	 */
	@RequestMapping(action + "save_phone.action")
	public Object save_phone(HttpServletRequest request) {
		String phone = request.getParameter("phone");
		String verifcode = request.getParameter("verifcode");
		String usercode = request.getParameter("usercode");

		ResultObject resultObject = new ResultObject();
		resultObject = this.readSecurityContextFromSession(resultObject);
		if (!"0".equals(resultObject.getCode())) {
			return resultObject;
		}

		try {
			
//			if (StringUtils.isEmptyString(phone) || !Strings.isNumber(phone) || phone.length() > 15) {
			if (StringUtils.isEmptyString(phone) || phone.length() > 20) {
				throw new BusinessException("请填写正确的电话号码");
			}

			String loginPartyId = this.getLoginPartyId();
			Party party = this.partyService.cachePartyBy(loginPartyId, false);
			
			if (null != party.getPhone() && party.getPhone().equals(phone) && true == party.getPhone_authority()) {
				throw new BusinessException("电话号码已绑定");
			}
			
			Party partyPhone = this.partyService.findPartyByVerifiedPhone(phone);
			if (null != partyPhone && !partyPhone.getId().toString().equals(loginPartyId)) {
				throw new BusinessException("电话号码已绑定其他用户");
			}			
			
			String authcode = this.identifyingCodeTimeWindowService.getAuthCode(phone);
			
			String bind_phone_email_ver = this.sysparaService.find("bind_phone_email_ver").getValue();
			String bind_usercode = this.sysparaService.find("bind_usercode").getValue();

			// 如果是演示用户，则不判断验证码
			if (!"GUEST".contentEquals(party.getRolename())) {
				
				if ("1".contentEquals(bind_phone_email_ver)) {
										
					if (StringUtils.isEmptyString(verifcode)) {
						throw new BusinessException("请填写正确的验证码");
					}
										
					if ((null == authcode) || (!authcode.equals(verifcode))) {
						throw new BusinessException("验证码不正确");
					}
				}
				
				if ("1".contentEquals(bind_usercode)) {
										
					if (StringUtils.isEmptyString(usercode)) {
						throw new BusinessException("推荐码不正确");
					}
					
					Party party_reco = this.partyService.findPartyByUsercode(usercode);
					if (null == party_reco || !party_reco.getEnabled()) {
						throw new BusinessException("推荐人无权限推荐");
					}
					
					UserRecom userRecom = this.userRecomService.findByPartyId(party.getId());
					if (null == userRecom) {
						userRecom = new UserRecom();
						userRecom.setPartyId(party.getId());
						userRecom.setReco_id(party_reco.getId());
						this.userRecomService.save(userRecom);
					} else {
//						this.userRecomService.update(party.getId(), party_reco.getId());
					}
				}
			}

			// 电话绑定成功
			party.setPhone(phone);
			party.setPhone_authority(true);
			
			// 获取用户系统等级：1/新注册；2/邮箱谷歌手机其中有一个已验证；3/用户实名认证； 4/用户高级认证；
			int userLevelSystem = this.partyService.getUserLevelByAuth(party);

			// 十进制个位表示系统级别：1/新注册；2/邮箱谷歌手机其中有一个已验证；3/用户实名认证；4/用户高级认证；
			// 十进制十位表示自定义级别：对应在前端显示为如VIP1 VIP2等级、黄金 白银等级；
			// 如：级别11表示：新注册的前端显示为VIP1；
			int userLevel = party.getUser_level();
			party.setUser_level(((int) Math.floor(userLevel / 10)) * 10 + userLevelSystem);
			
			this.partyService.update(party);
			
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
	 * 邮箱绑定
	 */
	@RequestMapping(action + "save_email.action")
	public Object save_email(HttpServletRequest request) {
		String email = request.getParameter("email");
		String verifcode = request.getParameter("verifcode");

		ResultObject resultObject = new ResultObject();
		resultObject = this.readSecurityContextFromSession(resultObject);
		if (!"0".equals(resultObject.getCode())) {
			return resultObject;
		}

		try {
			
			if (StringUtils.isEmptyString(email) || !Strings.isEmail(email)) {
				throw new BusinessException("请填写正确的邮箱地址");
			}

			String loginPartyId = this.getLoginPartyId();
			Party party = this.partyService.cachePartyBy(loginPartyId, false);
			
			if (null != party.getEmail() && party.getEmail().equals(email) && true == party.getEmail_authority()) {
				throw new BusinessException("邮箱已绑定");
			}
			
			Party partyEmail = this.partyService.findPartyByVerifiedEmail(email);
			if (null != partyEmail && !partyEmail.getId().toString().equals(loginPartyId)) {
				throw new BusinessException("邮箱已绑定其他用户");
			}
			
			String authcode = this.identifyingCodeTimeWindowService.getAuthCode(email);
			
			String bind_phone_email_ver = sysparaService.find("bind_phone_email_ver").getValue();

			// 如果是演示用户，则不判断验证码
			if (!"GUEST".contentEquals(party.getRolename())) {
				if ("1".contentEquals(bind_phone_email_ver)) {
					
					if (StringUtils.isEmptyString(verifcode)) {
						throw new BusinessException("请填写正确的验证码");
					}
					
					if ((null == authcode) || (!authcode.equals(verifcode))) {
						resultObject.setCode("1");
						resultObject.setMsg("验证码不正确");
						return resultObject;
					}
				}
			}

			// 邮箱绑定成功
			party.setEmail(email);
			party.setEmail_authority(true);
			
			// 获取用户系统等级：1/新注册；2/邮箱谷歌手机其中有一个已验证；3/用户实名认证； 4/用户高级认证；
			int userLevelSystem = this.partyService.getUserLevelByAuth(party);

			// 十进制个位表示系统级别：1/新注册；2/邮箱谷歌手机其中有一个已验证；3/用户实名认证；4/用户高级认证；
			// 十进制十位表示自定义级别：对应在前端显示为如VIP1 VIP2等级、黄金 白银等级；
			// 如：级别11表示：新注册的前端显示为VIP1；
			int userLevel = party.getUser_level();
			party.setUser_level(((int) Math.floor(userLevel / 10)) * 10 + userLevelSystem);
			
			this.partyService.update(party);
			Log log = new Log();
			log.setCategory(Constants.LOG_CATEGORY_SECURITY);
			log.setLog("用户邮箱绑定,ip[" + this.getIp(getRequest()) + "]");
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
	 * 获取我的分享信息
	 * 
	 */
	@RequestMapping(action + "getShare.action")
	public Object getShare() {

		ResultObject resultObject = new ResultObject();
		resultObject = readSecurityContextFromSession(resultObject);
		
		if (!"0".equals(resultObject.getCode())) {
			return resultObject;
		}
		try {
			Party party = this.partyService.cachePartyBy(getLoginPartyId(), false);
			// 关闭后，正式用户进入推广页面的时候，接口就不返回内容
			boolean member_promote_button = sysparaService.find("member_promote_button").getBoolean();

			Kyc kyc = kycService.get(party.getId().toString());
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("username", party.getUsername());
			map.put("userrole", party.getRolename());
			map.put("usercode", party.getUsercode());

			// 十进制个位表示系统级别：1/新注册；2/邮箱谷歌手机其中有一个已验证；3/用户实名认证；4/用户高级认证；
			// 十进制十位表示自定义级别：对应在前端显示为如VIP1 VIP2等级、黄金 白银等级；
			// 如：级别11表示：新注册的前端显示为VIP1；
			map.put("user_level", (int) (party.getUser_level() % 10));
			map.put("user_level_custom", (int) Math.floor(party.getUser_level() / 10));
			map.put("user_level_custom_display", "VIP");
			String user_level_custom_config = this.sysparaService.find("user_level_custom_config").getValue();
			String[] levelArray = user_level_custom_config.split(",");
			for (int i = 0; i < levelArray.length; i++) {
				String[] level = levelArray[i].split("-");
				if (level[0].equals(map.get("user_level_custom").toString())) {
					map.put("user_level_custom_display", level[1]);
					break;
				}
			}

			String shareUrl = sysparaService.find("share_url").getValue();
			if (party.getKyc_authority())
				map.put("name", kyc.getName());
			if (Constants.SECURITY_ROLE_TEST.equals(party.getRolename())) {
				map.put("test", true);

			} else {
				if (Constants.SECURITY_ROLE_MEMBER.equals(party.getRolename()) && !member_promote_button) {
					map.put("url", "");
					map.put("usercode_qr", "");
				} else {
					
					map.put("url", shareUrl + "/?usercode=" + party.getUsercode());
					/**
					 * 生成二维码图片
					 */
					qRGenerateService.generate(party.getUsercode());

					map.put("usercode_qr", shareUrl + "/public/showimg!showImg.action?imagePath=/qr/"
							+ party.getUsercode() + ".png");
				}
			}

			resultObject.setData(map);
		} catch (Exception e) {
			resultObject.setCode("1");
			resultObject.setMsg("程序错误");
		}
		return resultObject;
	}
	
	@RequestMapping(action + "getImageCode.action")
	public Object getImageCode() {
		ResultObject resultObject = new ResultObject();
		try {
			Map<String, Object> data = new HashMap<String, Object>();
			String key = UUIDGenerator.getUUID();
			ImageVerificationCodeUtil iv = new ImageVerificationCodeUtil();
			data.put("code", iv.getBase64());
			data.put("key", key);
			resultObject.setData(data);
			userService.putImageCode(key, iv.getText());
		} catch (BusinessException e) {
			resultObject.setCode("1");
			resultObject.setMsg(e.getMessage());
		} catch (Throwable t) {
			logger.error("UserAction.register error ", t);
			resultObject.setCode("1");
			resultObject.setMsg("[ERROR] " + t.getMessage());
		}

		return resultObject;
	}

	/**
	 * 获取个人信息
	 */
	@RequestMapping(action + "get.action")
	public Object get(HttpServletRequest request) {

		ResultObject resultObject = new ResultObject();
		resultObject = this.readSecurityContextFromSession(resultObject);
		if (!"0".equals(resultObject.getCode())) {
			return resultObject;
		}

		try {

			String loginPartyId = this.getLoginPartyId();
			Party party = this.partyService.cachePartyBy(loginPartyId, false);
			SecUser secUser = this.secUserService.findUserByPartyId(loginPartyId);
			Kyc kyc = this.kycService.get(party.getId().toString());
			KycHighLevel kycHighLevel = this.kycHighLevelService.get(party.getId().toString());

			Map<String, Object> map = new HashMap<String, Object>();

			// 十进制个位表示系统级别：1/新注册；2/邮箱谷歌手机其中有一个已验证；3/用户实名认证；4/用户高级认证；
			// 十进制十位表示自定义级别：对应在前端显示为如VIP1 VIP2等级、黄金 白银等级；
			// 如：级别11表示：新注册的前端显示为VIP1；
			map.put("user_level", (int) (party.getUser_level() % 10));
			map.put("user_level_custom", (int) Math.floor(party.getUser_level() / 10));	
			
			String projectType = this.sysparaService.find("project_type").getValue();
			if (StringUtils.isEmptyString(projectType)) {
				throw new BusinessException("系统参数错误");
			}
			if (projectType.equals("DAPP_EXCHANGE_BINANCE")
					|| projectType.equals("EXCHANGE_EASYCRYPTO")
					|| projectType.equals("DAPP_EXCHANGE_DAPPDEFIUSDT")) {
				map.put("user_level_custom_display", map.get("user_level_custom"));
			} else {
				map.put("user_level_custom_display", "VIP");
				String user_level_custom_config = this.sysparaService.find("user_level_custom_config").getValue();
				String[] levelArray = user_level_custom_config.split(",");
				for (int i = 0; i < levelArray.length; i++) {
					String[] level = levelArray[i].split("-");
					if (level[0].equals(map.get("user_level_custom").toString())) {
						map.put("user_level_custom_display", level[1]);	
						break;
					}
				}
			}

			map.put("username", party.getUsername());
			map.put("userrole", party.getRolename());
			map.put("usercode", party.getUsercode());
			map.put("phone", party.getPhone());
			map.put("phoneverif", party.getPhone_authority());
			map.put("email", party.getEmail());
			map.put("emailverif", party.getEmail_authority());
			map.put("google_auth_secret", secUser.getGoogle_auth_secret());
			map.put("googleverif", secUser.isGoogle_auth_bind());
			map.put("identityverif", party.getKyc_authority());
			map.put("advancedverif", party.isKyc_highlevel_authority());
			map.put("lastlogintime", party.getLast_loginTime());
			map.put("lastloginip", party.getLogin_ip());

			// 实名认证通过返回真实姓名
			if (party.getKyc_authority()) {
				map.put("name", kyc.getName());
			}
			
			if (null != kyc) {
				map.put("nationality", kyc.getNationality());
				map.put("kyc_status", kyc.getStatus());
			}
			
			if (null != kycHighLevel) {
				map.put("kyc_high_level_status", kycHighLevel.getStatus());
			}

			if (Constants.SECURITY_ROLE_TEST.equals(party.getRolename())) {
				map.put("test", true);
			} else {

				// 关闭后，正式用户进入推广页面的时候，接口就不返回内容
				boolean member_promote_button = this.sysparaService.find("member_promote_button").getBoolean();

				if (Constants.SECURITY_ROLE_MEMBER.equals(party.getRolename()) && !member_promote_button) {
					map.put("url", "");
					map.put("usercode_qr", "");
				} else {
					map.put("url", Constants.WEB_URL + "/register.html?usercode=" + party.getUsercode());
					// 生成二维码图片
					qRGenerateService.generate(party.getUsercode());
					map.put("usercode_qr", Constants.WEB_URL + "/public/showimg!showImg.action?imagePath=/qr/" + party.getUsercode() + ".png");
				}
			}
			
			UserRecom userRecom = this.userRecomService.findByPartyId(party.getId());
			if (null == userRecom || null == userRecom.getReco_id()) {
				map.put("usercode_parent", "");
			} else {
				Party party_reco = this.partyService.cachePartyBy(userRecom.getReco_id(), false);
				if (null == party_reco || null == party_reco.getUsercode() || StringUtils.isEmptyString(party_reco.getUsercode().toString())) {
					map.put("usercode_parent", "");
				} else {
					map.put("usercode_parent", party_reco.getUsercode());
				}
			}

			// 时间分隔点：新用户需要注册后填手机号和邀请码
			map.put("register_need_phone_usercode", false);
			String register_need_phone_usercode_time = this.sysparaService.find("register_need_phone_usercode_time").getValue();
			if (!StringUtils.isEmptyString(register_need_phone_usercode_time)) {
				
				// 结合盘：只有新用户需要注册后填手机号和邀请码
				Date dateFixed = DateUtils.toDate(register_need_phone_usercode_time, DateUtils.NORMAL_DATE_FORMAT);
				if (party.getCreateTime().getTime() > dateFixed.getTime()) {
					map.put("register_need_phone_usercode", true);
				} else {
					map.put("register_need_phone_usercode", false);
				}
			}
			
			// 承兑商类型：0不是承兑商/1后台承兑商/2用户承兑商
			map.put("c2c_user_type", party.getC2cUserType());
			
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
	
	/**
	 * Geetest二次验证：返回验证结果, request表单中必须包含challenge, validate, seccode
	 */
	public HashMap<String, String> geetestVerif(String challenge, String validate, String seccode, String gt_server_status) throws UnsupportedEncodingException {
			
		if (StringUtils.isEmptyString(challenge) || StringUtils.isEmptyString(validate) 
				|| StringUtils.isEmptyString(seccode) || StringUtils.isEmptyString(gt_server_status)) {
			logger.info("ip:{" + this.getIp() + "},图形验证错误: "
					+ "challenge:{" + challenge + "},validate:{" + validate + "},seccode:{" + seccode + "},gt_server_status:{" + gt_server_status + "}");
			throw new BusinessException("图形验证错误，请重新再试");
		}
		
		// 自定义参数，可选择添加
		HashMap<String, String> param = new HashMap<String, String>();
		
		// 网站用户id
		param.put("user_id", "test");
		
		param.put("challenge", challenge);
		param.put("validate", validate);
		param.put("seccode", seccode);
		
		String geetest_id = this.sysparaService.find("geetest_id").getValue();
		String geetest_key = this.sysparaService.find("geetest_key").getValue();
		String new_failback = this.sysparaService.find("geetest_new_failback").getValue();
		if(StringUtils.isEmptyString(geetest_id) || StringUtils.isEmptyString(geetest_key) || StringUtils.isEmptyString(new_failback)) {
			throw new BusinessException("系统参数错误");
		}
		
		param.put("geetest_id", geetest_id);
		param.put("geetest_key", geetest_key);
		param.put("new_failback", new_failback);
		
		int gtResult = 0;
		if ("1".equals(gt_server_status)) {
			// gt-server正常，向gt-server进行二次验证	
			gtResult = this.geetestService.enhencedValidateRequest(param);
			System.out.println(gtResult);
		} else {
			// gt-server非正常情况下，进行failback模式验证					
			System.out.println("failback:use your own server captcha validate");
			gtResult = this.geetestService.failbackValidateRequest(param);
			System.out.println(gtResult);
		}
		
		HashMap<String, String> retMap = this.geetestService.preProcess(param);

		if (1 == gtResult) {
			// 验证成功
			retMap.put("status", "success");
			retMap.put("version", this.geetestService.getVersionInfo());
		} else {
			// 验证失败
			retMap.put("status", "fail");
			retMap.put("version", this.geetestService.getVersionInfo());
		}
		
		return retMap;
	}
	
	private String validateParamUsername(String username, String password) {

		if (StringUtils.isNullOrEmpty(username)) {
			return "用户名不能为空";
		}

		if (StringUtils.isNullOrEmpty(password)) {
			return "登录密码不能为空";
		}
		if (!RegexUtil.isUSername(username)) {
			return "用户名必须由数字和英文字母组成";
		}

		int min = 6;
		int max = 12;
		int max_name = 24;
		if (!RegexUtil.length(username, min, max_name)) {
			return "用户名不符合设定";
		}
		if (!RegexUtil.length(password, min, max)) {
			return "登陆密码长度不符合设定";
		}
//		if (!RegexUtil.isDigits(this.password)) {
//			// 只能输入数字
//			return "登陆密码不符合设定";
//		}
//		if (StringUtils.isEmptyString(this.safeword)) {
//			return "资金密码不能为空";
//		}
//		if (!StringUtils.isEmptyString(this.safeword) && !RegexUtil.length(this.safeword, min, max)) {
//			// return "资金密码长度限制" + min + "-" + max + "个字符";
//			return "资金密码长度不符合设定";
//		}
//		if (StringUtils.isEmptyString(this.safeword) && !RegexUtil.isDigits(this.safeword)) {
//			// 只能输入数字
//			return "资金密码不符合设定";
//		}

		return null;
	}
	
	// 手机号校验
	private boolean isValidPhone(String username) {
        Pattern p = Pattern.compile("[0-9]*");
        return p.matcher(username).matches();
	}
	
	// 邮箱校验
	private boolean isValidEmail(String username) {
        String regexPattern = "^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@"
                + "[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$";
        return Pattern.compile(regexPattern)
                .matcher(username)
                .matches();
	}
	
	// 用户名校验
	private boolean isValidUsername(String username) {
		String regex = "^[A-Za-z]\\w{5,29}";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(username);
        return m.matches();
	}
	
	private String validateParam(String username, String verifcode, String password, String type) {

		if (StringUtils.isEmptyString(username)) {
			return "用户名不能为空";
		}

//		if (StringUtils.isEmptyString(verifcode)) {
//			return "验证码不能为空";
//		}
		if (StringUtils.isEmptyString(password)) {
			return "登录密码不能为空";
		}
		int min = 6;
		int max = 12;
		if (!RegexUtil.length(password, min, max)) {
			return "登陆密码长度不符合设定";
		}
//		if (!RegexUtil.isDigits(this.password)) {
//			// 只能输入数字
//			return "登陆密码不符合设定";
//		}

//		if (StringUtils.isEmptyString(this.usercode)) {
//			return "推荐码不能为空";
//		}
		
		if (StringUtils.isEmptyString(type) || !Arrays.asList("1", "2").contains(type)) {
			return "类型不能为空";
		}
		
		return null;
	}
	
}
