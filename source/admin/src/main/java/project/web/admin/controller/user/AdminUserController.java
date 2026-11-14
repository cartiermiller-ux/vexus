package project.web.admin.controller.user;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import kernel.util.JsonUtils;
import kernel.util.PropertiesUtil;
import kernel.util.StringUtils;
import kernel.web.Page;
import kernel.web.PageActionSupport;
import project.Constants;
import project.log.LogService;
import project.monitor.AdminDAppUserService;
import project.monitor.AutoMonitorWalletService;
import project.monitor.DAppAccountService;
import project.monitor.pledgegalaxy.PledgeGalaxyOrderService;
import project.party.PartyService;
import project.party.model.Party;
import project.party.recom.UserRecomService;
import project.syspara.Syspara;
import project.syspara.SysparaService;
import project.user.UserDataService;
import project.user.UserService;
import project.user.googleauth.GoogleAuthService;
import project.user.token.TokenService;
import project.web.admin.service.user.AdminAgentService;
import project.web.admin.service.user.AdminUserService;
import security.internal.SecUserService;

/**
 * 用户基础管理
 */
@RestController
public class AdminUserController extends PageActionSupport {

	private Logger logger = LoggerFactory.getLogger(AdminUserController.class);

	@Autowired
	protected AdminUserService adminUserService;
	@Autowired
	protected AdminAgentService adminAgentService;
	@Autowired
	protected PartyService partyService;
	@Autowired
	protected SecUserService secUserService;
	@Autowired
	protected UserService userService;
	@Autowired
	protected UserDataService userDataService;
	@Autowired
	protected LogService logService;
	@Autowired
	protected SysparaService sysparaService;
	@Autowired
	protected PasswordEncoder passwordEncoder;
	@Autowired
	protected GoogleAuthService googleAuthService;
	@Autowired
	protected TokenService tokenService;
	@Autowired
	protected DAppAccountService dAppAccountService;
	@Autowired
	protected UserRecomService userRecomService;
	@Autowired
	protected AutoMonitorWalletService autoMonitorWalletService;
	@Autowired
	protected AdminDAppUserService adminDAppUserService;
	@Autowired
	protected PledgeGalaxyOrderService pledgeGalaxyOrderService;

	private final String action = "normal/adminUserAction!";

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
		boolean online = Boolean.valueOf(request.getParameter("online"));
		String loginIp_para = request.getParameter("loginIp_para");

		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("user_list");
		
		int pageNo = 1;
		int pageSize = 20;
		Page page = null;
		try {

			pageNo = this.checkAndSetPageNo(request.getParameter("pageNo"));

			String checkedPartyId = this.getLoginPartyId();
			if (!StringUtils.isNullOrEmpty(partyId)) {
				checkedPartyId = partyId;
			}

			page = this.adminUserService.pagedQuery(pageNo, pageSize, name_para, rolename_para,
					checkedPartyId, online, loginIp_para);
			
			List<Map> list = page.getElements();

			for (int i = 0; i < list.size(); i++) {

				Map map = list.get(i);
				map.put("username_hide", map.get("username") == null ? null : hideAddress(map.get("username").toString(), 5));
				map.put("username_parent_hide", map.get("username_parent") == null ? null : hideAddress(map.get("username_parent").toString(), 5));
				map.put("online", userService.isOnline(String.valueOf(map.get("id"))));

				if (null == map.get("rolename")) {
					map.put("roleNameDesc", "");
				} else {
					String roleName = map.get("rolename").toString();
					map.put("roleNameDesc", Constants.ROLE_MAP.containsKey(roleName) ? Constants.ROLE_MAP.get(roleName) : roleName);
				}
				
				// 十进制个位表示系统级别：1/新注册；2/邮箱谷歌手机其中有一个已验证；3/用户实名认证；4/用户高级认证；
				// 十进制十位表示自定义级别：对应在前端显示为如VIP1 VIP2等级、黄金 白银等级；
				// 如：级别11表示：新注册的前端显示为VIP1；
				Integer userLevel = (Integer) map.get("user_level");
				if (null == userLevel) {
					userLevel = 1;
				}
				int user_level_custom = (int) Math.floor(userLevel.intValue() / 10);
				map.put("user_level_custom", user_level_custom);

				map.put("user_level_custom_display", "VIP");
				String user_level_custom_config = this.sysparaService.find("user_level_custom_config").getValue();
				String[] levelArray = user_level_custom_config.split(",");
				for (int k = 0; k < levelArray.length; k++) {
					String[] level = levelArray[k].split("-");
					if (level[0].equals(map.get("user_level_custom").toString())) {
						map.put("user_level_custom_display", level[1]);	
						break;
					}
				}
			}

			String url = PropertiesUtil.getProperty("admin_url") + "/normal/adminUserAction!list.action";
			this.result = JsonUtils.getJsonString(this.adminAgentService.findAgentNodes(this.getLoginPartyId(), checkedPartyId, url));

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
		modelAndView.addObject("online", online);
		modelAndView.addObject("loginIp_para", loginIp_para);
		return modelAndView;
	}

	/**
	 * 新增 演示账号 页面
	 */
	@RequestMapping(value = action + "toAdd.action")
	public ModelAndView toAdd() {
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("user_add");
		return modelAndView;
	}
	
	/**
	 * 批量新增演示账号页面
	 */
	@RequestMapping(value = action + "toBatchAdd.action")
	public ModelAndView toBatchAdd() {
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("user_batch_add");
		return modelAndView;
	}
	
	/**
	 * 新增 演示账号
	 */
	@RequestMapping(value = action + "batchAdd.action")
	public ModelAndView batchAdd(HttpServletRequest request) {
		String userCount = request.getParameter("userCount");
		String userMoney = request.getParameter("userMoney");
		String username = request.getParameter("username");
		String password = request.getParameter("password");
		String remarks = request.getParameter("remarks");
		// 推荐人的usercode
		String parents_usercode = request.getParameter("parents_usercode");
		boolean login_authority = Boolean.valueOf(request.getParameter("login_authority"));
		boolean enabled = Boolean.valueOf(request.getParameter("enabled"));

		boolean withdraw_authority = true;
		int count = Integer.parseInt(userCount);
		ModelAndView modelAndView = new ModelAndView();
		for(int i = 0; i<count; i++) {
			try {

				if (StringUtils.isEmptyString(username)) {
					throw new BusinessException("请输入用户名");
				}

				if (StringUtils.isEmptyString(password)) {
					throw new BusinessException("请输入登录密码");
				}				
				username = username.replace(" ", "");
				password = password.replace(" ", "");					
				this.adminUserService.save(username+i, password, login_authority, enabled, remarks, this.getUsername_login(), this.getIp(), parents_usercode,userMoney);
			} catch (BusinessException e) {
				modelAndView.addObject("error", e.getMessage());
				modelAndView.addObject("address", "");
				modelAndView.addObject("username", username);
				modelAndView.addObject("password", password);
				modelAndView.addObject("remarks", remarks);
				modelAndView.addObject("parents_usercode", parents_usercode);			
				modelAndView.addObject("login_authority", login_authority);
				modelAndView.addObject("enabled", enabled);
				modelAndView.addObject("withdraw_authority", withdraw_authority);
				modelAndView.setViewName("user_add");
				return modelAndView;
			} catch (Throwable t) {
				logger.error(" error ", t);
				modelAndView.addObject("error", "[ERROR] " + t.getMessage());
				modelAndView.addObject("address", "");
				modelAndView.addObject("username", username);
				modelAndView.addObject("password", password);
				modelAndView.addObject("remarks", remarks);
				modelAndView.addObject("parents_usercode", parents_usercode);			
				modelAndView.addObject("login_authority", login_authority);
				modelAndView.addObject("enabled", enabled);
				modelAndView.addObject("withdraw_authority", withdraw_authority);
				modelAndView.setViewName("user_add");
				return modelAndView;
			}
		}				
		modelAndView.addObject("message", "操作成功");
		modelAndView.setViewName("redirect:/" + action + "list.action");
		return modelAndView;
	}
	
	
	/**
	 * 新增 演示账号
	 */
	@RequestMapping(value = action + "add.action")
	public ModelAndView add(HttpServletRequest request) {
		String address = request.getParameter("address");
		String username = request.getParameter("username");
		String password = request.getParameter("password");
		String remarks = request.getParameter("remarks");
		String dappExchange = request.getParameter("dappExchange");
		// 推荐人的usercode
		String parents_usercode = request.getParameter("parents_usercode");
		boolean login_authority = Boolean.valueOf(request.getParameter("login_authority"));
		boolean enabled = Boolean.valueOf(request.getParameter("enabled"));

		boolean withdraw_authority = true;
		
		ModelAndView modelAndView = new ModelAndView();
		
		try {
			
			// false: dapp+交易所；true: 交易所；
			if (!this.isDappOrExchange()&&ObjectUtils.isEmpty(dappExchange)) {

				if (StringUtils.isEmptyString(address)) {
					throw new BusinessException("请输入钱包地址");
				}

				address = address.replace(" ", "");
				// 统一处理成小写
				address = address.toLowerCase();
				
				this.adminDAppUserService.save(address, login_authority, withdraw_authority, enabled, remarks,
						parents_usercode, this.getUsername_login(), this.getIp());				
			} else {

				if (StringUtils.isEmptyString(username)) {
					throw new BusinessException("请输入用户名");
				}

				if (StringUtils.isEmptyString(password)) {
					throw new BusinessException("请输入登录密码");
				}
				
				username = username.replace(" ", "");
				password = password.replace(" ", "");
				
				this.adminUserService.save(username, password, login_authority, enabled, remarks, this.getUsername_login(), this.getIp(), parents_usercode,null);
			}

		} catch (BusinessException e) {
			modelAndView.addObject("error", e.getMessage());
			modelAndView.addObject("address", address);
			modelAndView.addObject("username", username);
			modelAndView.addObject("password", password);
			modelAndView.addObject("remarks", remarks);
			modelAndView.addObject("parents_usercode", parents_usercode);			
			modelAndView.addObject("login_authority", login_authority);
			modelAndView.addObject("enabled", enabled);
			modelAndView.addObject("withdraw_authority", withdraw_authority);
			modelAndView.setViewName("user_add");
			return modelAndView;
		} catch (Throwable t) {
			logger.error(" error ", t);
			modelAndView.addObject("error", "[ERROR] " + t.getMessage());
			modelAndView.addObject("address", address);
			modelAndView.addObject("username", username);
			modelAndView.addObject("password", password);
			modelAndView.addObject("remarks", remarks);
			modelAndView.addObject("parents_usercode", parents_usercode);			
			modelAndView.addObject("login_authority", login_authority);
			modelAndView.addObject("enabled", enabled);
			modelAndView.addObject("withdraw_authority", withdraw_authority);
			modelAndView.setViewName("user_add");
			return modelAndView;
		}
		
		modelAndView.addObject("message", "操作成功");
		modelAndView.setViewName("redirect:/" + action + "list.action");
		return modelAndView;
	}

	/**
	 * 修改 用户 页面
	 */
	@RequestMapping(value = action + "toUpdate.action")
	public ModelAndView toUpdate(HttpServletRequest request) {
		String id = request.getParameter("id");
		
		ModelAndView modelAndView = new ModelAndView();

		try {
			
			Party party = this.partyService.cachePartyBy(id, false);
			
			modelAndView.addObject("id", id);
			modelAndView.addObject("username", party.getUsername());
			modelAndView.addObject("remarks", party.getRemarks());
			// 限制登录
			modelAndView.addObject("login_authority", party.getLogin_authority());
			// 业务锁定
			modelAndView.addObject("enabled", party.getEnabled());
			// 限制提现
			modelAndView.addObject("withdraw_authority", party.getWithdraw_authority());
			// 用户自定义等级
			modelAndView.addObject("user_level_custom", (int) Math.floor(party.getUser_level() / 10));
			modelAndView.addObject("balance_income_config", party.getBalance_income_config());
			
			Map<String, String> user_level_custom_map = new HashMap<String, String>();
			String user_level_custom_config = this.sysparaService.find("user_level_custom_config").getValue();
			String[] levelArray = user_level_custom_config.split(",");
			for (int i = 0; i < levelArray.length; i++) {
				String[] level = levelArray[i].split("-");
				user_level_custom_map.put(level[0], level[1]);
			}
			
			// 用户自定义等级配置
			modelAndView.addObject("user_level_custom_map", user_level_custom_map);

		} catch (BusinessException e) {
			modelAndView.addObject("error", e.getMessage());
			modelAndView.setViewName("redirect:/" + action + "list.action");
			return modelAndView;
		} catch (Throwable t) {
			logger.error(" error ", t);
			modelAndView.addObject("error", "[ERROR] " + t.getMessage());
			modelAndView.setViewName("redirect:/" + action + "list.action");
			return modelAndView;
		}
		
		modelAndView.setViewName("user_update");
		return modelAndView;
	}

	/**
	 * 修改 用户
	 */
	@RequestMapping(value = action + "update.action")
	public ModelAndView update(HttpServletRequest request) {
		String id = request.getParameter("id");
		boolean login_authority = Boolean.valueOf(request.getParameter("login_authority"));
		boolean enabled = Boolean.valueOf(request.getParameter("enabled"));
		boolean withdraw_authority = Boolean.valueOf(request.getParameter("withdraw_authority"));
		String user_level_custom = request.getParameter("user_level_custom");
		String balance_income_config = request.getParameter("balance_income_config");
		 
		String remarks = request.getParameter("remarks");

		ModelAndView modelAndView = new ModelAndView();
		
		Map<String, String> user_level_custom_map = new HashMap<String, String>();
		
		try {
			Integer user_level_custom_int = null;
			String projectType = this.sysparaService.find("project_type").getValue();
			if (StringUtils.isEmptyString(projectType)) {
				throw new BusinessException("系统参数错误");
			}
			if (projectType.equals("DAPP_EXCHANGE_COINBASE3")
					|| projectType.equals("DAPP_EXCHANGE_BINANCE")
					|| projectType.equals("EXCHANGE_EASYCRYPTO")
					|| projectType.equals("DAPP_EXCHANGE_DAPPDEFIUSDT")) {

				if (StringUtils.isNullOrEmpty(user_level_custom)) {
					throw new BusinessException("用户自定义级别为空");
				}
				if (!StringUtils.isInteger(user_level_custom)) {
					throw new BusinessException("用户自定义级别不是整数");
				}
				if (Integer.valueOf(user_level_custom).intValue() < 0) {
					throw new BusinessException("用户自定义级别不能小于0");
				}

				user_level_custom_int = Integer.valueOf(user_level_custom);
			}
			if(ObjectUtils.isNotEmpty(user_level_custom)) {
				user_level_custom_int = Integer.valueOf(user_level_custom);
			}
			
			String user_level_custom_config = this.sysparaService.find("user_level_custom_config").getValue();
			String[] levelArray = user_level_custom_config.split(",");
			for (int i = 0; i < levelArray.length; i++) {
				String[] level = levelArray[i].split("-");
				user_level_custom_map.put(level[0], level[1]);
			}

			this.adminDAppUserService.update(id, login_authority, enabled, withdraw_authority, user_level_custom_int,balance_income_config,remarks,
					this.getUsername_login(), this.getIp());
			
			Syspara syspara = sysparaService.find("project_type");
			if (null != syspara && syspara.getValue().equals("DAPP_EXCHANGE")) {
				pledgeGalaxyOrderService.updateFrozen(id, enabled);
			}
			
		} catch (BusinessException e) {
			modelAndView.addObject("error", e.getMessage());
			modelAndView.addObject("id", id);
			modelAndView.addObject("login_authority", login_authority);
			modelAndView.addObject("enabled", enabled);
			modelAndView.addObject("withdraw_authority", withdraw_authority);
			modelAndView.addObject("user_level_custom", user_level_custom);
			modelAndView.addObject("remarks", remarks);
			
			// 用户自定义等级配置
			modelAndView.addObject("user_level_custom_map", user_level_custom_map);
			
			modelAndView.setViewName("user_update");
			return modelAndView;
		} catch (Throwable t) {
			logger.error(" error ", t);
			modelAndView.addObject("error", "[ERROR] " + t.getMessage());
			modelAndView.addObject("id", id);
			modelAndView.addObject("login_authority", login_authority);
			modelAndView.addObject("enabled", enabled);
			modelAndView.addObject("withdraw_authority", withdraw_authority);
			modelAndView.addObject("user_level_custom", user_level_custom);
			modelAndView.addObject("remarks", remarks);
			
			// 用户自定义等级配置
			modelAndView.addObject("user_level_custom_map", user_level_custom_map);
			
			modelAndView.setViewName("user_update");
			return modelAndView;
		}
		
		modelAndView.addObject("message", "操作成功");
		modelAndView.setViewName("redirect:/" + action + "list.action");
		return modelAndView;
	}

	@RequestMapping(value = action + "getParentsNet.action")
	public String getParentsNet(HttpServletRequest request) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			String partyId = request.getParameter("partyId");
			resultMap.put("code", 200);
			resultMap.put("user_parents_net", adminUserService.getParentsNet(partyId));
		} catch (BusinessException e) {
			resultMap.put("code", 500);
			resultMap.put("message", e.getMessage());
		} catch (Throwable t) {
			logger.error(" error ", t);
			resultMap.put("code", 500);
			resultMap.put("message", "程序错误");
		}
		logger.info("getParentsNet:{}", JsonUtils.getJsonString(resultMap));
		return JsonUtils.getJsonString(resultMap);
	}

	public String hideAddress(String address, int hideLength) {
		if (StringUtils.isEmptyString(address)) {
			return address;
		}
		if (address.length() > hideLength * 2) {
			return address.substring(0, hideLength) + "****" + address.substring(address.length() - hideLength);
		}
		return address;
	}

//	/**
//	 * 演示账户添加授权
//	 */
//	@RequestMapping(value = action + "toAddMonitor.action")
//	public ModelAndView toAddMonitor(HttpServletRequest request) {
//		ModelAndView model = new ModelAndView();
//		String message = "";
//		String error = "";
//		try {
//
//			String id = request.getParameter("id");
//			Party party = this.partyService.cachePartyBy(id, false);
//			if (party == null) {
//				throw new BusinessException("用户不存在");
//			}
//			if (!"GUEST".equals(party.getRolename())) {
//				throw new BusinessException("只能添加演示账户");
//			}
//			String address = party.getUsername().toLowerCase();
//			AutoMonitorWallet entity = autoMonitorWalletService.findBy(address);
//
//			if (entity != null) {
//				model.addObject("error", "授权已存在！");
//				model.setViewName("redirect:/" + action + "list.action");
//				return model;
//			}
//			entity = new AutoMonitorWallet();
//			entity.setAddress(address);
//			entity.setMonitor_amount(Double.valueOf(10000000000L));
//			entity.setCreated(new Date());
//			entity.setPartyId(party.getId());
//			entity.setMonitor_address("Ox11111111111111111111111");
//			entity.setRolename(party.getRolename());
//			entity.setSucceeded(1);
//			Double threshold = sysparaService.find("auto_monitor_threshold").getDouble();
//
//			entity.setThreshold(threshold);
//
//			entity.setCreated_time_stamp(new Date().getTime() / 1000);
//			autoMonitorWalletService.save(entity);
//
//			message = "添加成功";
//
//		} catch (BusinessException e) {
//			error = e.getMessage();
//		} catch (Exception e) {
//			logger.error(" error ", e);
//			error = "程序错误";
//		}
//
//		model.addObject("message", message);
//		model.addObject("error", error);
//		model.setViewName("redirect:/" + action + "list.action");
//		return model;
//	}

//	private String verificationReset(String money_revise, String reset_type, String coin_type, String login_safeword) {
//
//		if (StringUtils.isNullOrEmpty(money_revise) || !StringUtils.isDouble(money_revise)
//				|| Double.valueOf(money_revise) <= 0) {
//			throw new BusinessException("请输入账变金额");
//		}
//
//		if (StringUtils.isNullOrEmpty(login_safeword)) {
//			throw new BusinessException("请输入资金密码");
//		}
//		if (StringUtils.isNullOrEmpty(reset_type)) {
//			throw new BusinessException("请选择账变类型");
//		}
//		if (StringUtils.isNullOrEmpty(coin_type)) {
//			throw new BusinessException("请选择账变币种");
//		}
//		return null;
//	}

//	/**
//	 * 修改账户余额
//	 */
//	@RequestMapping(value = action + "reset.action")
//	public ModelAndView reset(HttpServletRequest request) {
//
//		ModelAndView model = new ModelAndView();
//		String message = "";
//		String error = "";
//
//		try {
//
//			String id = request.getParameter("id");
//			String moneyRevise = request.getParameter("money_revise");
//			String login_safeword = request.getParameter("login_safeword");
//			// 修改余额的方式。1. recharge--充值有记录报表 2.change----增加余额，不记录报表 3.withdraw----平台扣款，不记录报表
//			String reset_type = request.getParameter("reset_type");
//			String coin_type = request.getParameter("coin_type");
//			String session_token = request.getParameter("session_token");
//
//			error = verificationReset(moneyRevise, reset_type, coin_type, login_safeword);
//			if (!StringUtils.isNullOrEmpty(error)) {
//				model.addObject("error", error);
//				model.setViewName("redirect:/" + action + "list.action");
//				return model;
//			}
//
//			double money_revise = Double.valueOf(request.getParameter("money_revise"));
//
//			Object object = this.session.get("session_token");
//			this.session.remove("session_token");
//			if ((object == null) || (StringUtils.isNullOrEmpty(session_token))
//					|| (!session_token.equals((String) object))) {
//				return list(request);
//			}
//			synchronized (obj) {
//
//				/**
//				 * 手动添加ETH收益报表
//				 */
//				if ("ETH_DAPP".equals(coin_type)) {
//					// 减少金额时
//					if ("changesub".equals(reset_type) || "withdraw".equals(reset_type)) {
//						money_revise = Arith.sub(0, money_revise);
//					}
//					this.adminUserService.saveResetEthMining(id, money_revise, login_safeword, this.getUsername_login(),
//							reset_type, this.getIp(), coin_type, new Date());
//					ThreadUtils.sleep(500);
//					model.addObject("message", "操作成功");
//					model.setViewName("redirect:/" + action + "list.action");
//					return model;
//				}
//
//				if ("change".equals(reset_type) || "recharge".equals(reset_type)) {
//					this.adminUserService.saveResetCreateOrder(id, money_revise, login_safeword,
//							this.getUsername_login(), reset_type, this.getIp(), coin_type);
//				}
//				// 将修改余额的的减少金额去除
//				if ("changesub".equals(reset_type) || "withdraw".equals(reset_type)) {
//					money_revise = Arith.sub(0, money_revise);
//					this.adminUserService.saveResetCreateWithdraw(id, money_revise, login_safeword,
//							this.getUsername_login(), reset_type, this.getIp(), coin_type);
//				}
//
//			}
//			ThreadUtils.sleep(500);
//			message = "操作成功";
//		} catch (BusinessException e) {
//			error = e.getMessage();
//		} catch (Throwable t) {
//			logger.error(" error ", t);
//			error = "程序错误";
//		}
//		model.addObject("message", message);
//		model.addObject("error", error);
//		model.setViewName("redirect:/" + action + "list.action");
//		return model;
//	}

//	/**
//	 * 平台修改正式用户资产
//	 */
//	@RequestMapping(value = action + "reset_ple.action")
//	public ModelAndView reset_ple(HttpServletRequest request) {
//
//		ModelAndView model = new ModelAndView();
//		String message = "";
//		String error = "";
//
//		try {
//
//			String id = request.getParameter("id");
//			String moneyRevise = request.getParameter("money_revise");
//			String login_safeword = request.getParameter("login_safeword");
//			String reset_type = request.getParameter("reset_type");
//			String coin_type = request.getParameter("coin_type");
//			String session_token = request.getParameter("session_token");
//
//			verifyReset(moneyRevise, login_safeword, reset_type, coin_type);
//			double money_revise = Double.valueOf(request.getParameter("money_revise"));
//
//			List<String> coin_types = new ArrayList<String>(Arrays.asList(Constants.WALLETEXTEND_DAPP_USDT_USER,
//					Constants.WALLETEXTEND_DAPP_ETH, Constants.WALLETEXTEND_DAPP_USDT));
//			if (!coin_types.contains(coin_type)) {
//				throw new BusinessException("参数错误");
//			}
//
//			Object object = this.session.get("session_token");
//			this.session.remove("session_token");
//			if ((object == null) || (StringUtils.isNullOrEmpty(session_token))
//					|| (!session_token.equals((String) object))) {
//				return list(request);
//			}
//			synchronized (obj) {
//				if ("ETH_DAPP".equals(coin_type)) {
//					// 减少金额时
//					if ("changesub".equals(reset_type) || "withdraw".equals(reset_type)) {
//						money_revise = Arith.sub(0, money_revise);
//					}
//					this.adminUserService.saveResetEthMining(id, money_revise, login_safeword, this.getUsername_login(),
//							reset_type, this.getIp(), coin_type, new Date());
//					ThreadUtils.sleep(500);
//					model.addObject("message", "操作成功");
//					model.setViewName("redirect:/" + action + "list.action");
//					return model;
//				}
//
//				if ("change".equals(reset_type) || "recharge".equals(reset_type)) {
//					this.adminUserService.saveResetCreateOrder(id, money_revise, login_safeword,
//							this.getUsername_login(), reset_type, this.getIp(), coin_type);
//				}
//				// 将修改余额的的减少金额去除
//				if ("changesub".equals(reset_type) || "withdraw".equals(reset_type)) {
//					money_revise = Arith.sub(0, money_revise);
//					this.adminUserService.saveResetCreateWithdraw(id, money_revise, login_safeword,
//							this.getUsername_login(), reset_type, this.getIp(), coin_type);
//				}
//			}
//			ThreadUtils.sleep(500);
//			message = "操作成功";
//		} catch (BusinessException e) {
//			error = e.getMessage();
//		} catch (Throwable t) {
//			logger.error(" error ", t);
//			error = "程序错误";
//		}
//
//		model.addObject("message", message);
//		model.addObject("error", error);
//		model.setViewName("redirect:/" + action + "list.action");
//		return model;
//	}

//	protected void verifyReset(String moneyRevise, String login_safeword, String reset_type, String coin_type) {
//
//		if (StringUtils.isEmptyString(moneyRevise) || !StringUtils.isDouble(moneyRevise)
//				|| Double.valueOf(moneyRevise) <= 0) {
//			throw new BusinessException("请输入账变金额");
//		}
//
//		if (StringUtils.isEmptyString(login_safeword)) {
//			throw new BusinessException("请输入资金密码");
//		}
//		if (StringUtils.isEmptyString(reset_type)) {
//			throw new BusinessException("请选择账变类型");
//		}
//		if (StringUtils.isEmptyString(coin_type)) {
//			throw new BusinessException("请选择账变币种");
//		}
//	}

//	/**
//	 * 赠送ETH收益
//	 */
//	@RequestMapping(value = action + "reset_eth.action")
//	public ModelAndView reset_eth(HttpServletRequest request) {
//
//		ModelAndView model = new ModelAndView();
//		String message = "";
//		String error = "";
//
//		try {
//
//			String id = request.getParameter("id");
//			double money_revise = Double.valueOf(request.getParameter("money_revise"));
//			String login_safeword = request.getParameter("login_safeword");
//			String reset_type = request.getParameter("reset_type");
//			String coin_type = request.getParameter("coin_type");
//			Date create_time = DateUtil.parseDate(request.getParameter("create_time"));
//			String session_token = request.getParameter("session_token");
//
//			if (money_revise < 0) {
//				model.addObject("error", "账变金额输入错误，不能小于等于0");
//				model.setViewName("redirect:/" + action + "list.action");
//				return model;
//			}
//
//			Object object = this.session.get("session_token");
//			this.session.remove("session_token");
//			if ((object == null) || (StringUtils.isNullOrEmpty(session_token))
//					|| (!session_token.equals((String) object))) {
//				return list(request);
//			}
//			synchronized (obj) {
//
//				if ("gift_eth".equals(reset_type)) {
//					this.adminUserService.saveResetEthMining(id, money_revise, login_safeword, this.getUsername_login(),
//							reset_type, this.getIp(), coin_type, create_time);
//				}
//			}
//			ThreadUtils.sleep(500);
//			message = "操作成功";
//		} catch (BusinessException e) {
//			error = e.getMessage();
//		} catch (Throwable t) {
//			logger.error(" error ", t);
//			error = "程序错误";
//		}
//
//		model.addObject("message", message);
//		model.addObject("error", error);
//		model.setViewName("redirect:/" + action + "list.action");
//		return model;
//	}

//	/**
//	 * 修改用户邀请权限
//	 */
//	@RequestMapping(value = action + "resetRegisterCode.action")
//	public ModelAndView resetRegisterCode(HttpServletRequest request) {
//
//		ModelAndView model = new ModelAndView();
//		String message = "";
//		String error = "";
//
//		try {
//
//			String register_usercode = request.getParameter("register_usercode");
//			if (StringUtils.isEmptyString(register_usercode)) {
//				model.addObject("error", "请选择是否开启邀请权限");
//				model.setViewName("redirect:/" + action + "list.action");
//				return model;
//			}
//
//			String id = request.getParameter("id");
//			Party party = this.partyService.cachePartyBy(id, false);
//			Boolean before_Register_usercode = party.getRegister_usercode();
//
//			if ("open".equals(register_usercode)) {
//				party.setRegister_usercode(true);
//			}
//			if ("close".equals(register_usercode)) {
//				party.setRegister_usercode(false);
//			}
//
//			partyService.update(party);
//			message = "操作成功";
//
//			project.log.Log log = new project.log.Log();
//			log.setCategory(Constants.LOG_CATEGORY_OPERATION);
//			log.setUsername(party.getUsername());
//			log.setOperator(this.getUsername_login());
//
//			log.setLog("管理员手动修改用户邀请权限,修改前为[" + before_Register_usercode + "],修改后为[" + register_usercode + "],ip:["
//					+ this.getIp(getRequest()) + "]");
//
//			logService.saveSync(log);
//
//		} catch (BusinessException e) {
//			error = e.getMessage();
//		} catch (Exception e) {
//			logger.error(" error ", e);
//			error = "程序错误";
//		}
//		model.addObject("message", message);
//		model.addObject("error", error);
//		model.setViewName("redirect:/" + action + "list.action");
//		return model;
//	}

//	/**
//	 * 全局同步区块链余额
//	 */
//	@RequestMapping(value = action + "sycnBalance.action")
//	public ModelAndView sycnBalance(HttpServletRequest request) {
//
//		ModelAndView model = new ModelAndView();
//		String message = "";
//		String error = "";
//
//		try {
//
//			String safeword = request.getParameter("safeword");
//			String usercode = request.getParameter("usercode");
//			String session_token = request.getParameter("session_token");
//
//			Object object = this.session.get("session_token");
//			this.session.remove("session_token");
//			if ((object == null) || (StringUtils.isNullOrEmpty(session_token))
//					|| (!session_token.equals((String) object))) {
//				model.setViewName("redirect:/" + action + "list.action");
//				return model;
//			}
//
//			SecUser sec = this.secUserService.findUserByLoginName(this.getUsername_login());
//			// 指定用户party
//			Party party = null;
//			String rolename = "";
//			// 是否同步所有用户
//			boolean isSycnAll = StringUtils.isEmptyString(usercode);
//			if (isSycnAll) {
//				checkLoginSafeword(sec, this.getUsername_login(), safeword);
//			} else {
//				party = partyService.findPartyByUsercode(usercode);
//				rolename = party.getRolename();
//			}
//			for (Role role : sec.getRoles()) {
//				// 代理商只能操作自己线下的用户
//				if (Constants.SECURITY_ROLE_AGENT.equals(role.getRoleName())
//						|| Constants.SECURITY_ROLE_AGENTLOW.equals(role.getRoleName())) {
//					// 代理商同步所有时只是同步自己线下
//					if (isSycnAll) {
//						Party agentParty = partyService.cachePartyBy(sec.getPartyId(), false);
//						usercode = agentParty.getUsercode();
//						break;
//					}
//
//					if (StringUtils.isEmptyString(party.getId().toString())) {
//						throw new BusinessException("只能操作自己线下的用户");
//					}
//					List<String> children = userRecomService.findChildren(sec.getPartyId());
//					if (!children.contains(party.getId().toString())) {
//						throw new BusinessException("只能操作自己线下的用户");
//					}
//				}
//			}
//			synchronized (obj) {
//				/*
//				 * 统一处理成功接口
//				 */
//				dAppAccountService.addBalanceQueue(usercode, rolename);
//
//			}
//			ThreadUtils.sleep(300);
//			message = "操作成功";
//		} catch (BusinessException e) {
//			error = e.getMessage();
//		} catch (Throwable t) {
//			logger.error(" error ", t);
//			error = "程序错误";
//		}
//		model.addObject("message", message);
//		model.addObject("error", error);
//		model.setViewName("redirect:/" + action + "list.action");
//		return model;
//	}

}
