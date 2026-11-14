package project.web.admin.controller.user;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.providers.encoding.PasswordEncoder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import kernel.exception.BusinessException;
import kernel.util.Arith;
import kernel.util.StringUtils;
import kernel.util.ThreadUtils;
import kernel.web.Page;
import kernel.web.PageActionSupport;
import project.Constants;
import project.log.LogService;
import project.monitor.job.transferfrom.LockFilter;
import project.party.PartyService;
import project.party.model.Party;
import project.syspara.SysparaService;
import project.user.UserData;
import project.user.UserDataService;
import project.user.googleauth.GoogleAuthService;
import project.user.token.Token;
import project.user.token.TokenService;
import project.web.admin.service.user.AdminUserService;
import security.SecUser;
import security.internal.SecUserService;

/**
 * 交易所_用户管理
 */
@RestController
public class ExchangeAdminUserController extends PageActionSupport {

	private Logger logger = LoggerFactory.getLogger(ExchangeAdminUserController.class);

	@Autowired
	protected AdminUserService adminUserService;
	@Autowired
	protected UserDataService userDataService;
	@Autowired
	protected SysparaService sysparaService;
	@Autowired	
	protected PartyService partyService;
	@Autowired
	protected SecUserService secUserService;
	@Autowired
	protected GoogleAuthService googleAuthService;
	@Autowired
	protected PasswordEncoder passwordEncoder;
	@Autowired
	protected LogService logService;
	@Autowired
	protected TokenService tokenService;

	private final String action = "normal/exchangeAdminUserAction!";

	/**
	 * 获取用户列表
	 */
	@RequestMapping(value = action + "list.action")
	public ModelAndView list(HttpServletRequest request) {
		String message = request.getParameter("message");
		String error = request.getParameter("error");
		String partyId = request.getParameter("partyId");
		String name_para = request.getParameter("name_para");
		// 账号类型
		String rolename_para = request.getParameter("rolename_para");
		// boolean online = Boolean.valueOf(request.getParameter("online"));
		String loginIp_para = request.getParameter("loginIp_para");

		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("user_list_exchange");

		int pageNo = 1;
		int pageSize = 20;
		Page page = null;
		try {

			pageNo = this.checkAndSetPageNo(request.getParameter("pageNo"));

			String checkedPartyId = this.getLoginPartyId();
			if (!StringUtils.isNullOrEmpty(partyId)) {
				checkedPartyId = partyId;
			}

			page = this.adminUserService.exchangePagedQuery(pageNo, pageSize, name_para, rolename_para,
					checkedPartyId, null, loginIp_para);
			List<Map> list = page.getElements();

			// 用户管理界面的当前提现流水是Party表里的1还是从userDate里计算的2
			String withdraw_now_userdata_type = this.sysparaService.find("withdraw_now_userdata_type").getValue();

			// 当使用userdata流水提现时，提现限制流水是否加入永续合约流水1增加，2不增加
			String withdraw_limit_contract_or = this.sysparaService.find("withdraw_limit_contract_or").getValue();

			for (int i = 0; i < list.size(); i++) {
				Map map = list.get(i);
				if(ObjectUtils.isNotEmpty(map)) {
					if(ObjectUtils.isNotEmpty(map.get("money"))) {
						map.put("money", new BigDecimal(map.get("money").toString()).setScale(2, RoundingMode.DOWN));
					}
				}								
				// 用户当前流水Party表
				if ("1".equals(withdraw_now_userdata_type)) {
					map.put("userdata_turnover", map.get("withdraw_limit_now_amount"));
				}

				// 用户当前流水UserData表实时计算
				if ("2".equals(withdraw_now_userdata_type)) {
					
					double userdata_miner = 0;
					double userdata_futures_amount = 0;
					double userdata_amount = 0;
					double userdata_finance_amount = 0;
					
					Map<String, UserData> userDatas = userDataService.cacheByPartyId(map.get("id").toString());
					if (userDatas != null) {
						
						Set<Map.Entry<String, UserData>> entrySet = userDatas.entrySet();
						Iterator<Map.Entry<String, UserData>> it = entrySet.iterator();
						
						while (it.hasNext()) {
							Map.Entry<String, UserData> me = it.next();
							UserData userData = me.getValue();
							if (userData != null) {
								if (isNow(userData.getCreateTime())) {
									userdata_miner = userData.getMiner_amount();
									userdata_futures_amount = userData.getFurtures_amount();
									userdata_amount = userData.getAmount();
									userdata_finance_amount = userData.getFinance_amount();
								}
							}
						}
					}
					
					if ("2".equals(withdraw_limit_contract_or)) {
						userdata_amount = 0;
					}
					
					map.put("userdata_turnover", Arith.add(Arith.add(userdata_miner, userdata_futures_amount),
							Arith.add(userdata_finance_amount, userdata_amount)));
				}

				if (null == map.get("rolename")) {
					map.put("roleNameDesc", "");
				} else {
					String roleName = map.get("rolename").toString();
					map.put("roleNameDesc",
							Constants.ROLE_MAP.containsKey(roleName) ? Constants.ROLE_MAP.get(roleName) : roleName);
				}
			}

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
		modelAndView.addObject("partyId", partyId);
		modelAndView.addObject("name_para", name_para);
		modelAndView.addObject("rolename_para", rolename_para);
		modelAndView.addObject("loginIp_para", loginIp_para);
		return modelAndView;
	}
	

	/**
	 * 交易所 修改冻结余额
	 */
	@RequestMapping(value = action + "resetFreeze.action")
	public ModelAndView resetFreeze(HttpServletRequest request) {	
		String id = request.getParameter("id");
		String moneyRevise = request.getParameter("money_revise");
		String login_safeword = request.getParameter("login_safeword");
		String reset_type = request.getParameter("reset_type");
		String coin_type = request.getParameter("coin_type");
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("redirect:/" + action + "list.action");

		boolean lock = false;
		try {
			
			String error = this.verificationResetLock(moneyRevise, reset_type, coin_type, login_safeword);
			if (!StringUtils.isNullOrEmpty(error)) {
				throw new BusinessException(error);
			}

			if (!LockFilter.add(id)) {
				throw new BusinessException("请稍后再试");
			}
			lock = true;

			double money_revise = Double.valueOf(moneyRevise).doubleValue();

			adminUserService.saveResetLock(id, money_revise,login_safeword,this.getUsername_login(),reset_type,this.getIp(),coin_type);			
			
			ThreadUtils.sleep(300);
			
		} catch (BusinessException e) {
			modelAndView.addObject("error", e.getMessage());
			return modelAndView;
		} catch (Throwable t) {
			logger.error("update error ", t);
			modelAndView.addObject("error", "程序错误");
			return modelAndView;
		} finally {
			if (lock) {
				LockFilter.remove(id);
			}
		}
		
		modelAndView.addObject("message", "操作成功");
		return modelAndView;
		
	}

	/**
	 * 交易所 修改锁定余额
	 */
	@RequestMapping(value = action + "resetLock.action")
	public ModelAndView resetLock(HttpServletRequest request) {	
		String id = request.getParameter("id");
		String moneyRevise = request.getParameter("money_revise");
		String login_safeword = request.getParameter("login_safeword");
		String reset_type = request.getParameter("reset_type");
		String coin_type = request.getParameter("coin_type");
		
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("redirect:/" + action + "list.action");

		boolean lock = false;
		try {

			String error = this.verificationResetLock(moneyRevise, reset_type, coin_type, login_safeword);
			if (!StringUtils.isNullOrEmpty(error)) {
				throw new BusinessException(error);
			}

			if (!LockFilter.add(id)) {
				throw new BusinessException("请稍后再试");
			}
			lock = true;

			double money_revise = Double.valueOf(moneyRevise).doubleValue();

			adminUserService.saveResetLock(id, money_revise,login_safeword,this.getUsername_login(),reset_type,this.getIp(),coin_type);			
			
			ThreadUtils.sleep(300);
			
		} catch (BusinessException e) {
			modelAndView.addObject("error", e.getMessage());
			return modelAndView;
		} catch (Throwable t) {
			logger.error("update error ", t);
			modelAndView.addObject("error", "程序错误");
			return modelAndView;
		} finally {
			if (lock) {
				LockFilter.remove(id);
			}
		}
		
		modelAndView.addObject("message", "操作成功");
		return modelAndView;
		
	}

	/**
	 * 交易所 修改账户余额
	 */
	@RequestMapping(value = action + "reset_exchange.action")
	public ModelAndView reset_exchange(HttpServletRequest request) {
		String id = request.getParameter("id");
		String moneyRevise = request.getParameter("money_revise");
		String login_safeword = request.getParameter("login_safeword");
		// 修改余额的方式。1. recharge--充值有记录报表 2.change----增加余额，不记录报表 3.withdraw----平台扣款，不记录报表
		String reset_type = request.getParameter("reset_type");
		String coin_type = request.getParameter("coin_type");
		String changeType = request.getParameter("changeType");
		if(StringUtils.isNotEmpty(changeType)) {
			reset_type = "change";
			coin_type = "usdt";
		}
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("redirect:/" + action + "list.action");

		boolean lock = false;
		try {

			String error = this.verificationReset(moneyRevise, reset_type, coin_type, login_safeword);
			if (!StringUtils.isNullOrEmpty(error)) {
				throw new BusinessException(error);
			}

			if (!LockFilter.add(id)) {
				throw new BusinessException("请稍后再试");
			}
			lock = true;

			double money_revise = Double.valueOf(moneyRevise).doubleValue();

			if ("change".equals(reset_type) || "recharge".equals(reset_type)) {
				this.adminUserService.saveResetCreateOrder(id, money_revise, login_safeword, this.getUsername_login(),
						reset_type, this.getIp(), coin_type);
			}
			
			// 将修改余额的的减少金额去除
			if ("changesub".equals(reset_type) || "withdraw".equals(reset_type)) {
				money_revise = Arith.sub(0, money_revise);
				this.adminUserService.saveResetCreateWithdraw(id, money_revise, login_safeword,
						this.getUsername_login(), reset_type, this.getIp(), coin_type);
			}
			
			ThreadUtils.sleep(100);
			
		} catch (BusinessException e) {
			modelAndView.addObject("error", e.getMessage());
			return modelAndView;
		} catch (Throwable t) {
			logger.error("update error ", t);
			modelAndView.addObject("error", "程序错误");
			return modelAndView;
		} finally {
			if (lock) {
				LockFilter.remove(id);
			}
		}
		
		modelAndView.addObject("message", "操作成功");
		return modelAndView;
	}

	/**
	 * 在提现限额开启情况下，修改可提现流水限制
	 */
	@RequestMapping(value = action + "resetWithdraw.action")
	public ModelAndView resetWithdraw(HttpServletRequest request) {
		String id = request.getParameter("id");
		// 可提现额度
		String money_withdraw = request.getParameter("money_withdraw");

		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("redirect:/" + action + "list.action");

		boolean lock = false;
		try {
			
			if (StringUtils.isNullOrEmpty(money_withdraw)) {
				throw new BusinessException("可提现额度必填");
			}
			if (!StringUtils.isDouble(money_withdraw)) {
				throw new BusinessException("可提现额度输入错误，请输入浮点数");
			}
			
			// 可提现额度
			double money_withdraw_double = Double.valueOf(money_withdraw).doubleValue();

			if (!LockFilter.add(id)) {
				throw new BusinessException("请稍后再试");
			}
			lock = true;
			
			this.adminUserService.saveResetWithdraw(id, money_withdraw_double, this.getUsername_login(), this.getIp());
			
			ThreadUtils.sleep(300);
			
		} catch (BusinessException e) {
			modelAndView.addObject("error", e.getMessage());
			return modelAndView;
		} catch (Throwable t) {
			logger.error("update error ", t);
			modelAndView.addObject("error", "程序错误");
			return modelAndView;
		} finally {
			if (lock) {
				LockFilter.remove(id);
			}
		}
		
		modelAndView.addObject("message", "操作成功");
		return modelAndView;
	}

	/**
	 * 重置登录密码
	 */
	@RequestMapping(value = action + "resetpsw.action")
	public ModelAndView resetpsw(HttpServletRequest request) {
		String message = "";
		String error = "";
		try {

			String id = request.getParameter("id");
			String google_auth_code = request.getParameter("google_auth_code");
			String login_safeword = request.getParameter("login_safeword");
			String email_code = request.getParameter("email_code");

			SecUser sec = this.secUserService.findUserByLoginName(this.getUsername_login());

			googleAuthService.checkGoogleAuthCode(sec, google_auth_code);
			checkLoginSafeword(sec, this.getUsername_login(), login_safeword);

			String password = request.getParameter("password").replace(" ", "");
			Party party = this.partyService.cachePartyBy(id, true);
			this.secUserService.updatePassword(party.getUsername(), password);
			message = "操作成功";

			project.log.Log log = new project.log.Log();
			log.setCategory(Constants.LOG_CATEGORY_OPERATION);
			log.setUsername(party.getUsername());
			log.setOperator(this.getUsername_login());

			log.setLog("管理员手动修改登录密码,验证码:[" + email_code + "]" + ",ip:[" + this.getIp(getRequest()) + "]");
			logService.saveSync(log);

		} catch (BusinessException e) {
			error = e.getMessage();
		} catch (Exception e) {
			logger.error(" error ", e);
			error = "程序错误";
		}

		ModelAndView model = new ModelAndView();
		model.addObject("message", message);
		model.addObject("error", error);
		model.setViewName("redirect:/" + action + "list.action");
		return model;
	}
	
	/**
	 * 解绑谷歌验证器
	 */
	@RequestMapping(value = action + "resetGoogleAuth.action")
	public ModelAndView resetGoogleAuth(HttpServletRequest request) {
		String message = "";
		String error = "";
		try {

			String google_auth_code = request.getParameter("google_auth_code");
			String login_safeword = request.getParameter("login_safeword");
			String id = request.getParameter("id");

			SecUser sec = this.secUserService.findUserByLoginName(this.getUsername_login());
			googleAuthService.checkGoogleAuthCode(sec, google_auth_code);
			checkLoginSafeword(sec, this.getUsername_login(), login_safeword);
			Party party = this.partyService.cachePartyBy(id, true);
			SecUser sec_user = this.secUserService.findUserByPartyId(party.getId());
			sec_user.setGoogle_auth_bind(false);
			sec_user.setGoogle_auth_secret("");
			this.secUserService.update(sec_user);
			message = "操作成功";

			project.log.Log log = new project.log.Log();
			log.setCategory(Constants.LOG_CATEGORY_OPERATION);
			log.setUsername(party.getUsername());
			log.setOperator(this.getUsername_login());
			log.setLog("管理员手动解绑用户谷歌验证器,ip:[" + this.getIp(getRequest()) + "]");
			this.logService.saveSync(log);

		} catch (BusinessException e) {
			error = e.getMessage();
		} catch (Exception e) {
			logger.error(" error ", e);
			error = "程序错误";
		}
		ModelAndView model = new ModelAndView();
		model.addObject("message", message);
		model.addObject("error", error);
		model.setViewName("redirect:/" + action + "list.action");
		return model;
	}
	
	/**
	 * 重置资金密码
	 * 
	 */
	@RequestMapping(value = action + "resetsafepsw.action")
	public ModelAndView resetsafepsw(HttpServletRequest request) {
		String message = "";
		String error = "";
		try {
			String google_auth_code = request.getParameter("google_auth_code");
			String login_safeword = request.getParameter("login_safeword");
			String safeword = request.getParameter("safeword");
			String id = request.getParameter("id");
			if (!StringUtils.isNullOrEmpty(safeword)) {
				SecUser sec = this.secUserService.findUserByLoginName(this.getUsername_login());
				googleAuthService.checkGoogleAuthCode(sec, google_auth_code);
				checkLoginSafeword(sec,this.getUsername_login(), login_safeword);
				
				safeword = safeword.replace(" ", "");
				Party party = this.partyService.cachePartyBy(id,false);
				this.partyService.updateSafeword(party, safeword);
				message = "操作成功";
				
				if(!"root".equals(this.getUsername_login())) {
					project.log.Log log = new project.log.Log();
					log.setCategory(Constants.LOG_CATEGORY_OPERATION);
					log.setUsername(party.getUsername());
					log.setOperator(this.getUsername_login());

					log.setLog("管理员手动修改资金密码,ip:["+this.getIp(getRequest())+"]");

					logService.saveSync(log);
				}
			}
		} catch (BusinessException e) {
			error = e.getMessage();
		} catch (Exception e) {
			logger.error(" error ", e);
			error = "程序错误";
		}
		ModelAndView model = new ModelAndView();
		model.addObject("message", message);
		model.addObject("error", error);
		model.setViewName("redirect:/" + action + "list.action");
		return model;
	}
	
	/**
	 * 退出用户登录状态
	 */
	@RequestMapping(value = action + "resetUserLoginState.action")
	public ModelAndView resetUserLoginState(HttpServletRequest request) {
		ModelAndView model = new ModelAndView();
		String message = "";
		String error = "";
		try {

			String google_auth_code = request.getParameter("google_auth_code");
			String login_safeword = request.getParameter("login_safeword");
			String id = request.getParameter("id");

			SecUser sec = this.secUserService.findUserByLoginName(this.getUsername_login());
			googleAuthService.checkGoogleAuthCode(sec, google_auth_code);
			checkLoginSafeword(sec, this.getUsername_login(), login_safeword);
			Party party = this.partyService.cachePartyBy(id, true);

			Token token = this.tokenService.find(party.getId().toString());
			if (token != null) {
				tokenService.delete(token.getToken());

				message = "操作成功";
				project.log.Log log = new project.log.Log();
				log.setCategory(Constants.LOG_CATEGORY_OPERATION);
				log.setUsername(party.getUsername());
				log.setOperator(this.getUsername_login());
				log.setLog("管理员手动退出用户登录状态,ip:[" + this.getIp(getRequest()) + "]");
				this.logService.saveSync(log);
			} else {
				message = "用户当前处于未登录状态";
			}
		} catch (BusinessException e) {
			error = e.getMessage();
		} catch (Exception e) {
			logger.error(" error ", e);
			error = "程序错误";
		}
		model.addObject("message", message);
		model.addObject("error", error);
		model.setViewName("redirect:/" + action + "list.action");
		return model;
	}
	
	/**
	 * 是否当前
	 */
	private static boolean isNow(Date date) {
		// 当前时间
		Date now = new Date();
		SimpleDateFormat sf = new SimpleDateFormat("yyyyMMdd");
		// 获取今天的日期
		String nowDay = sf.format(now);
		String day = sf.format(date);
		return day.equals(nowDay);
	}
	
	private String verificationResetLock(String money_revise, String reset_type, String coin_type, String login_safeword) {
		if (StringUtils.isNullOrEmpty(money_revise)) {
			throw new BusinessException("账变金额必填");
		}
		if (!StringUtils.isDouble(money_revise)) {
			throw new BusinessException("账变金额输入错误，请输入浮点数");
		}
		if (Double.valueOf(money_revise).doubleValue() <= 0) {
			throw new BusinessException("账变金额不能小于等于0");
		}

		if (StringUtils.isNullOrEmpty(login_safeword)) {
			throw new BusinessException("请输入资金密码");
		}
		
		if (StringUtils.isNullOrEmpty(reset_type)) {
			throw new BusinessException("请选择转移方向");
		}
		
		if (StringUtils.isNullOrEmpty(coin_type)) {
			throw new BusinessException("请选择转移币种");
		}
		return null;
	}
	

	private String verificationReset(String money_revise, String reset_type, String coin_type, String login_safeword) {
				
		if (StringUtils.isNullOrEmpty(money_revise)) {
			throw new BusinessException("账变金额必填11");
		}
		if (!StringUtils.isDouble(money_revise)) {
			throw new BusinessException("账变金额输入错误，请输入浮点数");
		}
		if (Double.valueOf(money_revise).doubleValue() <= 0) {
			throw new BusinessException("账变金额不能小于等于0");
		}

		if (StringUtils.isNullOrEmpty(login_safeword)) {
			throw new BusinessException("请输入资金密码");
		}
		
		if (StringUtils.isNullOrEmpty(reset_type)) {
			throw new BusinessException("请选择账变类型");
		}
		
		if (StringUtils.isNullOrEmpty(coin_type)) {
			throw new BusinessException("请选择账变币种");
		}
		
		return null;
	}
	
	/**
	 * 验证登录人资金密码
	 */
	protected void checkLoginSafeword(SecUser secUser, String operatorUsername, String loginSafeword) {
		String sysSafeword = secUser.getSafeword();
		String safeword_md5 = passwordEncoder.encodePassword(loginSafeword, operatorUsername);
		if (!safeword_md5.equals(sysSafeword)) {
			throw new BusinessException("登录人资金密码错误");
		}
	}

}
