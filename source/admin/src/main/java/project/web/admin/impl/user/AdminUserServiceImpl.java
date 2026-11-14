package project.web.admin.impl.user;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.providers.encoding.PasswordEncoder;

import kernel.exception.BusinessException;
import kernel.util.Arith;
import kernel.util.DateUtils;
import kernel.util.StringUtils;
import kernel.web.ApplicationUtil;
import kernel.web.Page;
import kernel.web.PagedQueryDao;
import project.Constants;
import project.data.DataService;
import project.data.model.Realtime;
import project.log.Log;
import project.log.LogService;
import project.log.MoneyLog;
import project.log.MoneyLogService;
import project.monitor.AutoMonitorDAppLogService;
import project.monitor.model.AutoMonitorDAppLog;
import project.monitor.pledge.PledgeConfig;
import project.monitor.pledge.PledgeConfigService;
import project.monitor.pledge.PledgeOrder;
import project.monitor.pledge.PledgeOrderService;
import project.party.PartyService;
import project.party.model.Party;
import project.party.model.UserRecom;
import project.party.recom.UserRecomService;
import project.syspara.Syspara;
import project.syspara.SysparaService;
import project.user.QRGenerateService;
import project.user.User;
import project.user.UserData;
import project.user.UserDataService;
import project.user.UserService;
import project.wallet.Wallet;
import project.wallet.WalletExtend;
import project.wallet.WalletLog;
import project.wallet.WalletLogService;
import project.wallet.WalletService;
import project.web.admin.service.user.AdminUserService;
import security.Role;
import security.RoleService;
import security.SaltSigureUtils;
import security.SecUser;
import security.internal.SecUserService;

public class AdminUserServiceImpl implements AdminUserService {
	
	protected LogService logService;
	
	protected DataService dataService;
	
	protected UserService userService;
	
	protected RoleService roleService;
	
	protected PartyService partyService;
	
	protected PagedQueryDao pagedQueryDao;
	
	protected WalletService walletService;
	
	protected SysparaService sysparaService;
	
	protected SecUserService secUserService;
	
	protected UserDataService userDataService;

	protected MoneyLogService moneyLogService;
	
	protected PasswordEncoder passwordEncoder;
	
	protected UserRecomService userRecomService;
	
	protected WalletLogService walletLogService;

	protected QRGenerateService qRGenerateService;
	
	protected PledgeOrderService pledgeOrderService;
	
	protected PledgeConfigService pledgeConfigService;
	
	protected AutoMonitorDAppLogService autoMonitorDAppLogService;
	
	private static final Logger logger=LoggerFactory.getLogger(AdminUserServiceImpl.class);
	
	/**
	 * 用户基础管理
	 */
	public Page pagedQuery(int pageNo, int pageSize, String name_para, String rolename, String checkedPartyId, Boolean online, String loginIp_para) {
		StringBuffer queryString = new StringBuffer(
				"SELECT party.UUID id, party.UUID parent_id, party.NAME name, party.USERNAME username,party.EMAIL email,party.PHONE phone,party.EMAIL_AUTHORITY email_authority,party.PHONE_AUTHORITY phone_authority, "
				+ "party.LOGINAUTHORITY login_authority, "
				+ "party.LAST_LOGIN_TIME last_loginTime, party.ENABLED enabled, party.ROLENAME rolename, party.CREATE_TIME create_time, "
				+ "party.REMARKS remarks, party.USERCODE usercode, "
				+ "party_parent.USERNAME username_parent, party.LOGIN_IP login_ip, party.GIFT_USER gift_user, party.USER_LEVEL user_level, "
				+ " party.REGSITER_USERCODE register_usercode "
				+ " ");
		
		queryString.append(
				" FROM PAT_PARTY party LEFT JOIN PAT_USER_RECOM user ON user.PARTY_ID = party.UUID "
				+ " LEFT JOIN PAT_PARTY party_parent ON user.RECO_ID = party_parent.UUID "
				+ " WHERE 1 = 1 ");

		Map<String, Object> parameters = new HashMap<String, Object>();
		
		if (!StringUtils.isNullOrEmpty(checkedPartyId)) {
			List<String> checked_list = this.userRecomService.findChildren(checkedPartyId);
			checked_list.add(checkedPartyId);
			if (checked_list.size() == 0) {
				return Page.EMPTY_PAGE;
			}
			queryString.append(" AND party.UUID in(:checked_list)");
			parameters.put("checked_list", checked_list);
		}

		if (!StringUtils.isNullOrEmpty(name_para)) {
			queryString.append(" AND (party.USERNAME like:username OR party.USERCODE like:username ) ");
			parameters.put("username","%" + name_para + "%");
		}
		
		if (!StringUtils.isNullOrEmpty(loginIp_para)) {
			queryString.append(" AND party.LOGIN_IP = :loginIp_para ");
			parameters.put("loginIp_para",loginIp_para);
		}
		
		if (!StringUtils.isNullOrEmpty(rolename)) {
			queryString.append(" AND party.ROLENAME =:rolename");
			parameters.put("rolename", rolename);
		}	

		queryString.append(" AND party.ROLENAME in(:rolenames)");
		parameters.put("rolenames", Arrays.asList(Constants.SECURITY_ROLE_GUEST,Constants.SECURITY_ROLE_MEMBER,Constants.SECURITY_ROLE_TEST));

		queryString.append(" order by party.CREATE_TIME desc ");

		Page page = this.pagedQueryDao.pagedQuerySQL(pageNo, pageSize, queryString.toString(), parameters);
		return page;
	}

	/**
	 * DAPP_用户管理
	 */
	public Page dappPagedQuery(int pageNo, int pageSize, String name_para, String rolename, String checkedPartyId, Boolean online, String loginIp_para) {
		StringBuffer queryString = new StringBuffer(
				"SELECT party.UUID id, party.NAME name, party.USERNAME username, "
				+ "party.LOGINAUTHORITY login_authority, "
				+ "party.LAST_LOGIN_TIME last_loginTime, party.ENABLED enabled, party.ROLENAME rolename, party.CREATE_TIME create_time, "
				+ "party.USERCODE usercode, "
				+ "party_parent.USERNAME username_parent, "
				+ "party.REGSITER_USERCODE register_usercode, wallet_extend_eth.AMOUNT eth_money, wallet_extend_usdt.AMOUNT money, wallet_extend_eth_dapp.AMOUNT eth_dapp, wallet_extend_usdt_dapp.AMOUNT usdt_dapp, wallet_extend_experience_gold.AMOUNT experience_gold, "
				+ "monitor_wallet.SUCCEEDED monitor_succeeded "
				+ " ");
		
		queryString.append(
				" FROM PAT_PARTY party LEFT JOIN PAT_USER_RECOM user ON user.PARTY_ID = party.UUID "
				+ " LEFT JOIN PAT_PARTY party_parent ON user.RECO_ID = party_parent.UUID "
				+ " LEFT JOIN T_AUTO_MONITOR_WALLET monitor_wallet ON monitor_wallet.PARTY_ID = party.UUID "
				+ " LEFT JOIN T_WALLET_EXTEND wallet_extend_usdt ON ( party.UUID = wallet_extend_usdt.PARTY_ID and wallet_extend_usdt.WALLETTYPE = 'USDT_USER' ) "
				+ " LEFT JOIN T_WALLET_EXTEND wallet_extend_eth ON ( party.UUID = wallet_extend_eth.PARTY_ID and wallet_extend_eth.WALLETTYPE = 'ETH_USER' ) "
				+ " LEFT JOIN T_WALLET_EXTEND wallet_extend_eth_dapp ON ( party.UUID = wallet_extend_eth_dapp.PARTY_ID and wallet_extend_eth_dapp.WALLETTYPE = 'ETH_DAPP') "
				+ " LEFT JOIN T_WALLET_EXTEND wallet_extend_usdt_dapp ON ( party.UUID = wallet_extend_usdt_dapp.PARTY_ID and wallet_extend_usdt_dapp.WALLETTYPE = 'USDT_DAPP') "
				+ " LEFT JOIN T_WALLET_EXTEND wallet_extend_experience_gold ON ( party.UUID = wallet_extend_experience_gold.PARTY_ID and wallet_extend_experience_gold.WALLETTYPE = 'EXPERIENCE_GOLD') "
				+ "  WHERE 1 = 1 ");

		Map<String, Object> parameters = new HashMap<String, Object>();
		
		if (!StringUtils.isNullOrEmpty(checkedPartyId)) {
			List<String> checked_list = this.userRecomService.findChildren(checkedPartyId);
			checked_list.add(checkedPartyId);
			if (checked_list.size() == 0) {
				return Page.EMPTY_PAGE;
			}
			queryString.append(" and party.UUID in(:checked_list)");
			parameters.put("checked_list", checked_list);
		}

		if (!StringUtils.isNullOrEmpty(name_para)) {
			queryString.append("AND (party.USERNAME like:username OR party.USERCODE like:username ) ");
			parameters.put("username", "%" + name_para + "%");
		}
		
		if (!StringUtils.isNullOrEmpty(loginIp_para)) {
			queryString.append(" AND party.LOGIN_IP = :loginIp_para  ");
			parameters.put("loginIp_para", loginIp_para);
		}
		
		if (!StringUtils.isNullOrEmpty(rolename)) {
			queryString.append(" and party.ROLENAME =:rolename");
			parameters.put("rolename", rolename);
		}

		queryString.append(" and party.ROLENAME in(:rolenames)");
		parameters.put("rolenames", Arrays.asList(Constants.SECURITY_ROLE_GUEST, Constants.SECURITY_ROLE_MEMBER, Constants.SECURITY_ROLE_TEST));

		queryString.append(" order by party.CREATE_TIME desc ");

		Page page = this.pagedQueryDao.pagedQuerySQL(pageNo, pageSize, queryString.toString(), parameters);
		return page;
	}
	
	/**
	 * 交易所_用户管理
	 */
	public Page exchangePagedQuery(int pageNo, int pageSize, String name_para, String rolename,  String checkedPartyId, Boolean online, String loginIp_para) {
		
		StringBuffer queryString = new StringBuffer(
				"SELECT party.UUID id, party.NAME name, party.USERNAME username, "
				+ " party.LOGINAUTHORITY login_authority, party.WITHDRAW_LIMIT_AMOUNT withdraw_limit_amount, party.WITHDRAW_LIMIT_NOW_AMOUNT withdraw_limit_now_amount, "
				+ " party.LAST_LOGIN_TIME last_loginTime, party.ENABLED enabled, party.ROLENAME rolename, party.CREATE_TIME create_time, "
				+ " party.REMARKS remarks, wallet.MONEY money, party.USERCODE usercode, "
				+ " party_parent.USERNAME username_parent, party.LOGIN_IP login_ip, party.GIFT_USER gift_user, party.USER_LEVEL user_level, "
				+ " party.REGSITER_USERCODE register_usercode "			
				+ " ");
		
		queryString.append(
				" FROM PAT_PARTY party LEFT JOIN PAT_USER_RECOM user ON user.PARTY_ID = party.UUID "
				+ " LEFT JOIN T_WALLET wallet ON wallet.PARTY_ID = party.UUID "
				+ " LEFT JOIN PAT_PARTY party_parent ON user.RECO_ID = party_parent.UUID "
				
				+ " WHERE 1 = 1 ");

		Map<String, Object> parameters = new HashMap<String, Object>();
		
		if (!StringUtils.isNullOrEmpty(checkedPartyId)) {
			List<String> checked_list = this.userRecomService.findChildren(checkedPartyId);
			checked_list.add(checkedPartyId);
			if (checked_list.size() == 0) {
				return Page.EMPTY_PAGE;
			}
			queryString.append(" and party.UUID in(:checked_list)");
			parameters.put("checked_list", checked_list);
		}

		if (!StringUtils.isNullOrEmpty(name_para)) {
			queryString.append("AND (party.USERNAME like:username OR party.USERCODE like:username ) ");
			parameters.put("username","%" + name_para + "%");
		}
		
		if (!StringUtils.isNullOrEmpty(loginIp_para)) {
			queryString.append(" AND party.LOGIN_IP = :loginIp_para  ");
			parameters.put("loginIp_para",loginIp_para);
		}
		
		if (!StringUtils.isNullOrEmpty(rolename)) {
			queryString.append(" and   party.ROLENAME =:rolename");
			parameters.put("rolename", rolename);
		}

		queryString.append(" and party.ROLENAME in(:rolenames)");
		parameters.put("rolenames", Arrays.asList(Constants.SECURITY_ROLE_GUEST, Constants.SECURITY_ROLE_MEMBER, Constants.SECURITY_ROLE_TEST));

		queryString.append(" order by party.CREATE_TIME desc ");

		Page page = this.pagedQueryDao.pagedQuerySQL(pageNo, pageSize, queryString.toString(), parameters);
			
		return page;
	}
	
	@Override
	public void save(String username, String password, boolean login_authority, boolean enabled, String remarks,
			String operatorUsername, String ip, String parents_usercode, String userMoney) {
		username = username.trim();
		password = password.trim();
		
		
		if (secUserService.findUserByLoginName(username) != null) {
			throw new BusinessException("用户名重复");
		}
		/**
		 * 用户code
		 */
		String usercode = getUsercode();
		
		if (!StringUtils.isNullOrEmpty(parents_usercode)) {
			Party party_parents=partyService.findPartyByUsercode(parents_usercode);
			if (party_parents==null ) {
				throw new BusinessException("推荐码不正确");
			}
		}

		int ever_user_level_num = this.sysparaService.find("ever_user_level_num").getInteger();
		int ever_user_level_num_custom = this.sysparaService.find("ever_user_level_num_custom").getInteger();
		
		/**
		 * party
		 */
		Party party = new Party();
		party.setUsername(username);
		party.setLogin_authority(login_authority);
		party.setEnabled(enabled);
		party.setRemarks(remarks);
		party.setUsercode(usercode);
		party.setUser_level(ever_user_level_num_custom * 10 + ever_user_level_num);
		
		party.setSafeword(passwordEncoder.encodePassword("000000", SaltSigureUtils.saltfigure));

		party.setRolename(Constants.SECURITY_ROLE_GUEST);

		party = partyService.save(party);
		
		if (!StringUtils.isNullOrEmpty(parents_usercode)) {
			Party party_parents=partyService.findPartyByUsercode(parents_usercode);
			if (party_parents==null ) {
				throw new BusinessException("推荐码不正确");
			}
			UserRecom userRecom = new UserRecom();
			userRecom.setPartyId(party.getId());
			userRecom.setReco_id(party_parents.getId().toString());// 父类partyId
			this.userRecomService.save(userRecom);
		}

		/**
		 * SecUser
		 */
		Role role = this.roleService.findRoleByName(Constants.SECURITY_ROLE_GUEST);

		SecUser secUser = new SecUser();
		secUser.setPartyId(String.valueOf(party.getId()));
		secUser.getRoles().add(role);

		secUser.setUsername(username);
		secUser.setPassword(password);
		secUser.setEnabled(login_authority);

		this.secUserService.saveUser(secUser);

		/**
		 * usdt账户
		 */
		Wallet wallet = new Wallet();
		wallet.setPartyId(party.getId().toString());		
		if(StringUtils.isNotEmpty(userMoney)) {
			wallet.setMoney(Double.parseDouble(userMoney));
		}
		this.walletService.save(wallet);
        
		
		User user = new User();
		user.setPartyId(party.getId());
		ApplicationUtil.executeInsert(user);
		
		project.log.Log log = new project.log.Log();
		log.setCategory(Constants.LOG_CATEGORY_OPERATION);
		log.setUsername(party.getUsername());
		log.setOperator(operatorUsername);
		log.setLog("ip:"+ip+",管理员手动新增了演示用户:"+username);
		logService.saveSync(log);

	}
	
	@Override
	public void update(String partyId, boolean login_authority, boolean enabled, boolean withdraw_authority,String remarks,String operatorUsername,String ip) {
		Party party = this.partyService.cachePartyBy(partyId, false);
		SecUser sec =  this.secUserService.findUserByLoginName(operatorUsername);
		for (Role role : sec.getRoles()) {
			//代理商只能修改演示账户
			if (Constants.SECURITY_ROLE_AGENT.equals(role.getRoleName())||Constants.SECURITY_ROLE_AGENTLOW.equals(role.getRoleName())) {
				if(party!=null&&!Constants.SECURITY_ROLE_GUEST.equals(party.getRolename())) {
					throw new BusinessException("只能修改演示账户");
				}
				List<String> children = userRecomService.findChildren(sec.getPartyId());
				if(!children.contains(partyId)) {
					throw new BusinessException("只能修改自己线下的用户演示账户");
					
				}
			}
		}
		
		String logtxt = MessageFormat.format("ip:"+ip+",管理员手动修改了用户信息,用户名:{0},原登录权限:{1},原是否锁定:{2},原提现权限:{3},原备注:{4}"
				, party.getUsername(),party.getLogin_authority(),party.getEnabled(),party.getWithdraw_authority(),party.getRemarks());
		party.setRemarks(remarks);
		party.setLogin_authority(login_authority);
		party.setEnabled(enabled);
		party.setWithdraw_authority(withdraw_authority);
		this.partyService.update(party);
		logtxt +=MessageFormat.format(",新登录权限:{0},新是否锁定:{1},新提现权限:{2},新备注:{3}"
				, party.getLogin_authority(),party.getEnabled(),party.getWithdraw_authority(),party.getRemarks());
		SecUser secUser = secUserService.findUserByPartyId(partyId);
		secUser.setEnabled(login_authority);

		this.secUserService.update(secUser);

		project.log.Log log = new project.log.Log();
		log.setCategory(Constants.LOG_CATEGORY_OPERATION);
		log.setUsername(party.getUsername());
		log.setOperator(operatorUsername);
		log.setLog(logtxt);
		logService.saveSync(log);
	}
	
	/**
	 * DAPP修改收益账户（ETH）余额
	 */
	@Override
	public void saveResetEthMining(String partyId,double money_revise,String safeword,String operator_name,String reset_type,String ip,String coin_type, Date create_time) {

		if (money_revise == 0 || coin_type == "") {
			return;
		}
		
		Party party = this.partyService.cachePartyBy(partyId, false);
		
		WalletExtend walletExtend = this.walletService.saveExtendByPara(partyId, coin_type);
		
		double amount_before = walletExtend.getAmount();
		
		if (Arith.add(money_revise, walletExtend.getAmount()) < 0.0D) {
			throw new BusinessException("操作失败！修正后账户余额小于0。");
		}
		
		SecUser sec = this.secUserService.findUserByLoginName(operator_name);
		String sysSafeword = sec.getSafeword();
		
		String safeword_md5 = this.passwordEncoder.encodePassword(safeword, operator_name);
		if (!safeword_md5.equals(sysSafeword)) {
			throw new BusinessException("资金密码错误");
		}

		// 更新金额
		this.walletService.updateExtend(walletExtend.getPartyId().toString(), coin_type, money_revise);
		
		// 账变日志
		MoneyLog moneyLog = new MoneyLog();
		moneyLog.setCategory(Constants.MONEYLOG_CATEGORY_COIN);
		moneyLog.setAmount_before(amount_before);
		moneyLog.setAmount(money_revise);
		moneyLog.setAmount_after(Arith.add(walletExtend.getAmount(), money_revise));			
		moneyLog.setPartyId(partyId);
		moneyLog.setWallettype(coin_type.toUpperCase());
		moneyLog.setContent_type(Constants.MONEYLOG_CONTENT_RECHARGE);
		
		// 钱包日志
		WalletLog walletLog = new WalletLog();
		walletLog.setCategory(Constants.MONEYLOG_CATEGORY_RECHARGE);
		walletLog.setPartyId(partyId);
		walletLog.setOrder_no("");
		walletLog.setStatus(1);
		walletLog.setAmount(money_revise);
		walletLog.setWallettype(coin_type.toUpperCase());
		this.walletLogService.save(walletLog);
		
		// 操作日志
//		Party party = this.partyService.cachePartyBy(partyId, true);
		Log log = new Log();
		log.setCategory(Constants.LOG_CATEGORY_OPERATION);
		log.setUsername(party.getUsername());
		log.setOperator(operator_name);
		
//		this.userDataService.saveRechargeHandle(partyId, money_revise,coin_type);
		log.setLog("ip:"+ip+",管理员手动添加ETH收益金。修改添加[" + money_revise + "]");
		moneyLog.setLog("管理员手动添加ETH收益金");
		
		this.moneyLogService.save(moneyLog);
		this.logService.saveSync(log);
		
		Realtime realtime = this.dataService.realtime("eth").get(0);
		
		// 前端日志
		AutoMonitorDAppLog dAppLog = new AutoMonitorDAppLog();
		dAppLog.setPartyId(partyId);
		dAppLog.setExchange_volume(money_revise);
		dAppLog.setAmount(Arith.mul(money_revise, realtime.getClose()));
		dAppLog.setAction(AutoMonitorDAppLog.ACTION_TRANSFER);
		dAppLog.setCreateTime(create_time);
		
		this.autoMonitorDAppLogService.save(dAppLog);
	}

	/**
	 * DAPP/交易所 修改余额 增加
	 */
	@Override
	public void saveResetCreateOrder(String partyId, double money_revise, String safeword, String operator_name, String reset_type, String ip, String coin_type) {

		if (money_revise == 0 || coin_type == "") {
			return;
		}
		
		if ("usdt".equals(coin_type)) {
			// 交易所修改usdt
			Party party = this.partyService.cachePartyBy(partyId, false);
			
			Wallet wallet = this.walletService.saveWalletByPartyId(partyId);
			double amount_before = wallet.getMoney();
			if (Arith.add(money_revise, wallet.getMoney()) < 0.0D) {
				throw new BusinessException("操作失败！修正后账户余额小于0。");
			}
			
			SecUser sec = this.secUserService.findUserByLoginName(operator_name);
			String sysSafeword = sec.getSafeword();			
			String safeword_md5 = this.passwordEncoder.encodePassword(safeword, operator_name);
			if (!safeword_md5.equals(sysSafeword)) {
				throw new BusinessException("资金密码错误");
			}
			
			// 更新金额
			this.walletService.update(wallet.getPartyId().toString(), money_revise);
			
			// 账变日志
			MoneyLog moneyLog = new MoneyLog();
			moneyLog.setCategory(Constants.MONEYLOG_CATEGORY_COIN);
			moneyLog.setAmount_before(amount_before);
			moneyLog.setAmount(money_revise);
			moneyLog.setAmount_after(Arith.add(wallet.getMoney(), money_revise));			
			moneyLog.setPartyId(partyId);
			moneyLog.setWallettype(Constants.WALLET);
			moneyLog.setContent_type(Constants.MONEYLOG_CONTENT_RECHARGE);
			
			// 钱包日志
			WalletLog walletLog = new WalletLog();
			walletLog.setCategory(Constants.MONEYLOG_CATEGORY_RECHARGE);
			walletLog.setPartyId(partyId);
			walletLog.setOrder_no("");
			walletLog.setStatus(1);
			walletLog.setAmount(money_revise);
			walletLog.setWallettype("USDT");
			this.walletLogService.save(walletLog);
			
			// 操作日志			
			Log log = new Log();
			log.setCategory(Constants.LOG_CATEGORY_OPERATION);
			log.setUsername(party.getUsername());
			log.setOperator(operator_name);
			
			// change----添加赠送金额
			if("change".equals(reset_type)) {
				
				// 只有正式用户才需要记录报表
				if (null != party && Constants.SECURITY_ROLE_MEMBER.equals(party.getRolename())) {
					this.userDataService.saveGiftMoneyHandle(partyId, money_revise);
				}
				
				log.setLog("ip:"+ip+",管理员手动添加赠送金额。修改币种[usdt]，修改数量[" + money_revise + "]");
				moneyLog.setLog("管理员手动添加赠送金额");
				
				this.checkGiftUserLine(party);
			}
			
			// recharge--添加充值金额
			if("recharge".equals(reset_type)) {
				
				// 只有正式用户才需要记录报表
				if (null != party && Constants.SECURITY_ROLE_MEMBER.equals(party.getRolename())) {
					this.userDataService.saveRechargeHandle(partyId, money_revise, "usdt");
				}
				
				log.setLog("ip:"+ip+",管理员手动添加充值金额。修改币种[usdt]，修改数量[" + money_revise + "]");
				moneyLog.setLog("管理员手动添加充值金额");
			}
			
			this.moneyLogService.save(moneyLog);
			this.logService.saveSync(log);
						
			// 充值到账后给他增加提现流水限制金额			
			party.setWithdraw_limit_amount(Arith.add(party.getWithdraw_limit_amount(), money_revise));
			this.partyService.update(party);
		} else {
			// 交易所修改btc、eth；DAPP修改质押账户（USDT）【USDT_DAPP】；DAPP演示用户修改DAPP余额【USDT_USER】；
			Party party = this.partyService.cachePartyBy(partyId, false);
			
			WalletExtend walletExtend = this.walletService.saveExtendByPara(partyId, coin_type);
			double amount_before = walletExtend.getAmount();
			if (Arith.add(money_revise, walletExtend.getAmount()) < 0.0D) {
				throw new BusinessException("操作失败！修正后账户余额小于0。");
			}
			
			SecUser sec =  this.secUserService.findUserByLoginName(operator_name);
			String sysSafeword =sec.getSafeword();
			
			String safeword_md5 = this.passwordEncoder.encodePassword(safeword, operator_name);
			if (!safeword_md5.equals(sysSafeword)) {
				throw new BusinessException("资金密码错误");
			}
			
			this.walletService.updateExtend(walletExtend.getPartyId().toString(), coin_type, money_revise);
			
			// 账变日志
			MoneyLog moneyLog = new MoneyLog();
			moneyLog.setCategory(Constants.MONEYLOG_CATEGORY_COIN);
			moneyLog.setAmount_before(amount_before);
			moneyLog.setAmount(money_revise);
			moneyLog.setAmount_after(Arith.add(walletExtend.getAmount(), money_revise));		
			moneyLog.setPartyId(partyId);
			moneyLog.setWallettype(coin_type.toUpperCase());
			moneyLog.setContent_type(Constants.MONEYLOG_CONTENT_RECHARGE);
			
			// 钱包日志
			WalletLog walletLog = new WalletLog();
			walletLog.setCategory(Constants.MONEYLOG_CATEGORY_RECHARGE);
			walletLog.setPartyId(partyId);
			walletLog.setOrder_no("");
			walletLog.setStatus(1);
			walletLog.setAmount(money_revise);
			walletLog.setWallettype(coin_type.toUpperCase());
			walletLogService.save(walletLog);
			
			// 操作日志
//			Party party = this.partyService.cachePartyBy(partyId, true);
			Log log = new Log();
			log.setCategory(Constants.LOG_CATEGORY_OPERATION);
			log.setUsername(party.getUsername());
			log.setOperator(operator_name);
			
			// recharge--添加充值金额
			if("recharge".equals(reset_type)) {
				String coin_str= "";
				
				if("USDT_DAPP".equals(coin_type)) {
					coin_str="[质押账户(USDT)]";
					
					PledgeOrder entity_before = this.pledgeOrderService.findByPartyId(partyId);

					if (entity_before == null) {
						
						PledgeConfig entity_config = this.pledgeConfigService.findById("2c948a827cd5f779017cd2322f5d0001");
						PledgeOrder entity = new PledgeOrder();
						
						entity.setPartyId(partyId);
						entity.setConfig(entity_config.getConfig());
						entity.setUsdt(entity_config.getUsdt());
						entity.setLimit_days(entity_config.getLimit_days());
						entity.setEth(entity_config.getEth());
						entity.setTitle(entity_config.getTitle());
						entity.setTitle_img(entity_config.getTitle_img());

						entity.setContent(entity_config.getContent());
						entity.setContent_img(entity_config.getContent_img());
						entity.setApply(true);
						entity.setSendtime( DateUtils.addDate(DateUtils.toDate(DateUtils.format(new Date(), DateUtils.DEFAULT_DATE_FORMAT)), entity.getLimit_days()));
						entity.setApplyTime(new Date());

//						entity.setSendtime(this.sendtime);
						entity.setCreateTime(new Date());
						
						this.pledgeOrderService.save(entity);
					}					
				}
				
				if("USDT_USER".equals(coin_type)) {
					coin_str="[用户钱包USDT映射]";
				}
				
				// 只有正式用户才需要记录报表
				if (null != party && Constants.SECURITY_ROLE_MEMBER.equals(party.getRolename())) {
					this.userDataService.saveRechargeHandle(partyId, money_revise, coin_type);
				}
				
				log.setLog("ip:"+ip+",管理员手动添加充值金额。修改币种["+coin_type+"]"+coin_str+"，修改数量[" + money_revise + "]");
				moneyLog.setLog("管理员手动添加充值金额");
			}
			
			this.moneyLogService.save(moneyLog);
			this.logService.saveSync(log);
		}
	}

	/**
	 * DAPP/交易所 修改余额 减少
	 */
	public void saveResetCreateWithdraw(String partyId, double money_revise, String safeword, String operator_name, String reset_type, String ip, String coin_type) {
		// money_revise为负值
		if (money_revise == 0 || coin_type == "") {
			return;
		}
		
		if ("usdt".equals(coin_type)) {
			// 交易所修改usdt
			Wallet wallet = this.walletService.saveWalletByPartyId(partyId);			
			double amount_before = wallet.getMoney();			
			if (wallet.getMoney() < money_revise) {
				throw new BusinessException("余额不足");
			}			
			if (Arith.add(money_revise, wallet.getMoney()) < 0.0D) {
				throw new BusinessException("操作失败！修正后账户余额小于0。");
			}
			
			// 判断资金密码
			SecUser sec =  this.secUserService.findUserByLoginName(operator_name);
			String sysSafeword =sec.getSafeword();
			String safeword_md5 = this.passwordEncoder.encodePassword(safeword, operator_name);
			if (!safeword_md5.equals(sysSafeword)) {
				throw new BusinessException("资金密码错误");
			}
			
			// 更新金额
			this.walletService.update(wallet.getPartyId().toString(), money_revise);
			
			// 账变日志
			MoneyLog moneyLog = new MoneyLog();
			moneyLog.setCategory(Constants.MONEYLOG_CATEGORY_COIN);
			moneyLog.setAmount_before(amount_before);
			moneyLog.setAmount(money_revise);
			moneyLog.setAmount_after(Arith.add(wallet.getMoney(), money_revise));
			moneyLog.setPartyId(partyId);
			moneyLog.setWallettype(Constants.WALLET);
			moneyLog.setContent_type(Constants.MONEYLOG_CONTENT_WITHDRAW);		
			
			// 钱包日志
			WalletLog walletLog = new WalletLog();
			walletLog.setCategory("withdraw");
			walletLog.setPartyId(partyId);
			walletLog.setStatus(1);
			walletLog.setAmount(money_revise);
			walletLog.setWallettype(Constants.WALLET);
			this.walletLogService.save(walletLog);
			
			SecUser SecUser =  secUserService.findUserByPartyId(partyId);
			
			Log log = new Log();
			log.setCategory(Constants.LOG_CATEGORY_OPERATION);
			log.setUsername(SecUser.getUsername());
			log.setOperator(operator_name);
				
			log.setLog("ip:"+ip+",管理员手动减少充值金额，修改币种[usdt]，" + "修改数量["+ money_revise + "]");
			moneyLog.setLog("管理员手动减少充值金额，减少余额不记录报表");
			
			this.moneyLogService.save(moneyLog);
			this.logService.saveSync(log);
		} else {
			// 交易所修改btc、eth；DAPP修改质押账户（USDT）【USDT_DAPP】；DAPP演示用户修改DAPP余额【USDT_USER】；
			WalletExtend walletExtend = this.walletService.saveExtendByPara(partyId, coin_type);			
			double amount_before = walletExtend.getAmount();			
			if (walletExtend.getAmount() < money_revise) {
				throw new BusinessException("余额不足");
			}			
			if (Arith.add(money_revise, walletExtend.getAmount()) < 0.0D) {
				throw new BusinessException("操作失败！修正后["+coin_type.toUpperCase()+"]账户余额小于0。");
			}
			
			// 判断资金密码
			SecUser sec =  this.secUserService.findUserByLoginName(operator_name);
			String sysSafeword =sec.getSafeword();
			String safeword_md5 = this.passwordEncoder.encodePassword(safeword, operator_name);
			if (!safeword_md5.equals(sysSafeword)) {
				throw new BusinessException("资金密码错误");
			}
			
			// 更新金额
			this.walletService.updateExtend(walletExtend.getPartyId().toString(), coin_type, money_revise);
			
			// 账变日志
			MoneyLog moneyLog = new MoneyLog();
			moneyLog.setCategory(Constants.MONEYLOG_CATEGORY_COIN);
			moneyLog.setAmount_before(amount_before);
			moneyLog.setAmount(money_revise);
			moneyLog.setAmount_after(Arith.add(walletExtend.getAmount(), money_revise));
			moneyLog.setPartyId(partyId);
			moneyLog.setWallettype(coin_type.toUpperCase());
			moneyLog.setContent_type(Constants.MONEYLOG_CONTENT_WITHDRAW);
			
			// 钱包日志
			WalletLog walletLog = new WalletLog();
			walletLog.setCategory("withdraw");
			walletLog.setPartyId(partyId);
			walletLog.setStatus(1);
			walletLog.setAmount(money_revise);
			walletLog.setWallettype(coin_type.toUpperCase());
			this.walletLogService.save(walletLog);
			
			SecUser SecUser =  secUserService.findUserByPartyId(partyId);
			
			Log log = new Log();
			log.setCategory(Constants.LOG_CATEGORY_OPERATION);
			log.setUsername(SecUser.getUsername());
			log.setOperator(operator_name);

			String coin_str= "";
			if("USDT_DAPP".equals(coin_type)) {
				coin_str="[质押账户(USDT)]";
			}
			if("USDT_USER".equals(coin_type)) {
				coin_str="[用户钱包USDT映射]";
			}

			log.setLog("ip:"+ip+",管理员手动减少充值金额，修改币种["+coin_type+"]"+coin_str+"，" + "修改数量["+ money_revise + "]");
			moneyLog.setLog("管理员手动减少充值金额");
			
			this.moneyLogService.save(moneyLog);
			this.logService.saveSync(log);			
		}
	}
	
	/**
	 * 检验是否达到赠送用户的达标线
	 * @param party
	 */
	public void checkGiftUserLine(Party party) {
		if(!party.getGift_money_flag()) {
			party.setGift_money_flag(true);
		}
		if(party.getGift_user()) {//已经是赠送用户则无需判定
			return;
		}
		Map<String, UserData> datas = userDataService.cacheByPartyId(party.getId().toString());
		Double giftUserLine = sysparaService.find("gift_user_line").getDouble();
		String gift_user_date_start = sysparaService.find("gift_user_date_start").getValue();
		double giftMoney = giftMoney(datas, gift_user_date_start, null);
		if(giftMoney>=giftUserLine) {
			party.setGift_user(true);
		}
	}
	
	/**
	 * 时间范围内的充值总额
	 * @param datas
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	private double giftMoney(Map<String, UserData> datas, String startTime, String endTime) {
		if (datas == null || datas.isEmpty())
			return 0;
		double giftMoney = 0;
		for (Entry<String, UserData> valueEntry : datas.entrySet()) {
			UserData userdata = valueEntry.getValue();
			Date time = userdata.getCreateTime();
			if (!StringUtils.isNullOrEmpty(startTime)) {
				Date startDate = DateUtils.toDate(startTime, DateUtils.DF_yyyyMMdd);
				int intervalDays = DateUtils.getIntervalDaysByTwoDate(startDate, time);// 开始-数据时间
				if (intervalDays > 0) // 开始>数据时间 ，则过滤
					continue;
			}
			if (!StringUtils.isNullOrEmpty(endTime)) {
				Date endDate = DateUtils.toDate(endTime, DateUtils.DF_yyyyMMdd);
				int intervalDays = DateUtils.getIntervalDaysByTwoDate(endDate, time);// 结束-数据时间
				if (intervalDays < 0) // 结束<数据时间
					continue;
			}
			giftMoney = Arith.add(userdata.getGift_money(), giftMoney);
		}

		return giftMoney;
	}
	
	public void saveResetWithdraw(String partyId,double money_withdraw,String operator_username,String ip) {
		Party party = this.partyService.cachePartyBy(partyId,false);
		double last_amount = party.getWithdraw_limit_amount();
		if (Arith.add(last_amount, money_withdraw) < 0) {
			throw new BusinessException("修改后金额不能小于0");
		}
		
		party.setWithdraw_limit_amount(Arith.add(party.getWithdraw_limit_amount(), money_withdraw));
		
		double after_party = party.getWithdraw_limit_amount();
		this.partyService.update(party);
		
		/**
		 * 操作日志 
		 */
		SecUser SecUser =  secUserService.findUserByPartyId(partyId);
		Log log = new Log();
		log.setCategory(Constants.LOG_CATEGORY_OPERATION);
		log.setUsername(SecUser.getUsername());
		log.setOperator(operator_username);
		log.setLog("ip:"+ip+",管理员手动修改提现限制流水。修改前数量为["+last_amount+"]，"
				+ "修改数量为["+money_withdraw+"]，修改后数量为["
				+ after_party + "]。");

		logService.saveSync(log);		
	}

	/**
	 * 变更质押体验金
	 */
	public void updateExperienceGold(String partyId, double changeValue, String operatorName, String ip) {
		
		WalletExtend walletExtend = this.walletService.saveExtendByPara(partyId, Constants.WALLETEXTEND_EXPERIENCE_GOLD);			
		double amountBefore = walletExtend.getAmount();			
		if (Arith.add(changeValue, amountBefore) < 0.0D) {
			throw new BusinessException("操作失败，变更后质押体验金小于0。");
		}
		
		// 更新金额
		this.walletService.updateExtend(walletExtend.getPartyId().toString(), Constants.WALLETEXTEND_EXPERIENCE_GOLD, changeValue);
		
		// 账变日志
		MoneyLog moneyLog = new MoneyLog();
		moneyLog.setCategory(Constants.MONEYLOG_EXPERIENCE_GOLD);
		moneyLog.setAmount_before(amountBefore);
		moneyLog.setAmount(changeValue);
		moneyLog.setAmount_after(Arith.add(walletExtend.getAmount(), changeValue));
		moneyLog.setPartyId(partyId);
		moneyLog.setWallettype(Constants.WALLETEXTEND_EXPERIENCE_GOLD);
		moneyLog.setContent_type("");
		moneyLog.setLog("管理员手动变更质押体验金");
		
		SecUser SecUser =  secUserService.findUserByPartyId(partyId);
		Log log = new Log();
		log.setCategory(Constants.LOG_CATEGORY_OPERATION);
		log.setUsername(SecUser.getUsername());
		log.setOperator(operatorName);
		log.setLog("ip:" + ip + ",管理员手动变更质押体验金，" + "修改数量[" + changeValue + "]");
		
		this.moneyLogService.save(moneyLog);
		this.logService.saveSync(log);	
	}
	
	public void saveResetLock(String partyId,double moneyRevise,String safeword,String operatorName,String resetType,String ip,String coinType) {
		SecUser sec =  this.secUserService.findUserByLoginName(operatorName);
		String sysSafeword =sec.getSafeword();

		String safeword_md5 = passwordEncoder.encodePassword(safeword, operatorName);
		if (!safeword_md5.equals(sysSafeword)) {
			throw new BusinessException("资金密码错误");
		}
		
		double amount_before = 0D;
		double lock_amount_before = 0D;
		double freeze_amount_before = 0D;
		//更改的可用金额
		double changeMoney = 0d;
		//更改的锁定金额
		double lockMoney = 0.0d;
		//更改的冻结金额
		double freezeMoney = 0.0d;
		if("usdt".equals(coinType)) {
			Wallet wallet = this.walletService.saveWalletByPartyId(partyId);
			amount_before = wallet.getMoney();
			lock_amount_before = wallet.getLock_money();
			freeze_amount_before = wallet.getFreeze_money();
			Map<String, Object> map = checkChangeMoney(moneyRevise, resetType, amount_before, lock_amount_before,freeze_amount_before);
			changeMoney = Double.valueOf(map.get("changeMoney").toString());
			lockMoney = Double.valueOf(map.get("lockMoney").toString());
			freezeMoney = Double.valueOf(map.get("freezeMoney").toString());
			walletService.updateWithLockAndFreeze(wallet.getPartyId().toString(),changeMoney,lockMoney,freezeMoney);
		}else {
			WalletExtend walletExtend = this.walletService.saveExtendByPara(partyId,coinType);
			amount_before = walletExtend.getAmount();
			lock_amount_before = walletExtend.getLock_amount();
			freeze_amount_before = walletExtend.getFreeze_amount();
			Map<String, Object> map = checkChangeMoney(moneyRevise, resetType, amount_before, lock_amount_before,freeze_amount_before);
			changeMoney = Double.valueOf(map.get("changeMoney").toString());
			lockMoney = Double.valueOf(map.get("lockMoney").toString());
			freezeMoney = Double.valueOf(map.get("freezeMoney").toString());
			walletService.updateExtendWithLockAndFreeze(walletExtend.getPartyId().toString(), coinType, changeMoney, lockMoney,freezeMoney);
		}
		
		/*
		 * 保存账变日志
		 */
		MoneyLog moneyLog = new MoneyLog();
		moneyLog.setCategory(Constants.MONEYLOG_CATEGORY_COIN);
		moneyLog.setAmount_before(amount_before);
		moneyLog.setAmount(changeMoney);
		moneyLog.setAmount_after(Arith.add(amount_before, changeMoney));
		
		moneyLog.setPartyId(partyId);
		moneyLog.setWallettype(coinType.toUpperCase());
		moneyLog.setContent_type(Constants.MONEYLOG_CONTENT_SYS_LOCK);

		/**
		 * 操作日志
		 */
		SecUser SecUser =  secUserService.findUserByPartyId(partyId);
		Log log = new Log();
		log.setCategory(Constants.LOG_CATEGORY_OPERATION);
		log.setUsername(SecUser.getUsername());
		log.setOperator(operatorName);
		
		String logInfo = "";
		String logResetType="";
		double lockOrFreezeMoney=0d;
		if("moneryToLock".equals(resetType)) {//余额转到锁定
			logInfo = "管理员手动操作,可用金额->锁定金额";
			logResetType="锁定";
			lockOrFreezeMoney = lockMoney;
			moneyLog.setLog(logInfo);
			moneyLogService.save(moneyLog);
		}else if("lockToMoney".equals(resetType)) {//锁定转到余额
			logInfo = "管理员手动操作,锁定金额->可用金额";
			logResetType="锁定";
			lockOrFreezeMoney = lockMoney;
			moneyLog.setLog(logInfo);
			moneyLogService.save(moneyLog);
		}else if("addLock".equals(resetType)) {
			logInfo = "管理员手动添加锁定金额";
			logResetType="锁定";
			lockOrFreezeMoney = lockMoney;
			log.setExtra(Constants.MONEYLOG_CONTENT_SYS_MONEY_ADD_LOCK);
		}else if("subLock".equals(resetType)) {
			logInfo = "管理员手动减少锁定金额";
			logResetType="锁定";
			lockOrFreezeMoney = lockMoney;
			log.setExtra(Constants.MONEYLOG_CONTENT_SYS_MONEY_SUB_LOCK);
		}else if("moneryToFreeze".equals(resetType)) {//余额转到冻结
			logInfo = "管理员手动操作,可用金额->冻结金额";
			logResetType="冻结";
			lockOrFreezeMoney = freezeMoney;
			moneyLog.setLog(logInfo);
			moneyLogService.save(moneyLog);
		}else if("freezeToMoney".equals(resetType)) {//冻结转到余额
			logInfo = "管理员手动操作,冻结金额->可用金额";
			logResetType="冻结";
			lockOrFreezeMoney = freezeMoney;
			moneyLog.setLog(logInfo);
			moneyLogService.save(moneyLog);
		}
		
		String logText = MessageFormat.format("ip:{0},{1}。修改币种[{2}],修改可用数量[{3}],修改{4}数量[{5}]", ip,logInfo,coinType,changeMoney,logResetType,lockOrFreezeMoney);
		log.setLog(logText);
		logService.saveSync(log);
	}
	
	private Map<String,Object> checkChangeMoney(double moneyRevise, String resetType, double amountBefore,
			double lockAmountBefore,
			double freezeAmountBefore) {
		Map<String,Object> map = new HashMap<String, Object>();
		//更改的可用金额
		double changeMoney = 0d;
		//更改的锁定金额
		double lockMoney = 0.0d;
		//更改的冻结金额
		double freezeMoney = 0.0d;
		if(StringUtils.isEmptyString(resetType)) {
			throw new BusinessException("请选择转移类型");
		}else if("moneryToLock".equals(resetType)) {//余额转到锁定
			if(moneyRevise>amountBefore) {
				throw new BusinessException("操作失败！修正后账户余额小于0。");
			}
			changeMoney = Arith.sub(0,moneyRevise);
			lockMoney = moneyRevise;
		}else if("lockToMoney".equals(resetType)) {
			if(moneyRevise>lockAmountBefore) {
				throw new BusinessException("操作失败！修正后账户锁定余额小于0。");
			}
			changeMoney = moneyRevise;
			lockMoney = Arith.sub(0, moneyRevise);
		}else if("addLock".equals(resetType)) {
			changeMoney = 0;
			lockMoney = moneyRevise;
		}else if("subLock".equals(resetType)) {
			if(moneyRevise>lockAmountBefore) {
				throw new BusinessException("操作失败！修正后账户锁定余额小于0。");
			}
			changeMoney = 0;
			lockMoney = Arith.sub(0,moneyRevise);
		}else if("moneryToFreeze".equals(resetType)) {//余额转到冻结
			if(moneyRevise>amountBefore) {
				throw new BusinessException("操作失败！修正后账户余额小于0。");
			}
			changeMoney = Arith.sub(0,moneyRevise);
			freezeMoney = moneyRevise;
		}else if("freezeToMoney".equals(resetType)) {
			if(moneyRevise>freezeAmountBefore) {
				throw new BusinessException("操作失败！修正后账户冻结余额小于0。");
			}
			changeMoney = moneyRevise;
			freezeMoney = Arith.sub(0, moneyRevise);
		}else {
			throw new BusinessException("请选择转移类型");
		}
		map.put("changeMoney", changeMoney);
		map.put("lockMoney", lockMoney);
		map.put("freezeMoney", freezeMoney);
		return map;
	}
	
	private String getUsercode() {
		Syspara syspara = sysparaService.find("user_uid_sequence");
		int random = (int) (Math.random() * 3 + 1);
		int user_uid_sequence = syspara.getInteger() + random;
		syspara.setValue(user_uid_sequence);
		sysparaService.update(syspara);

		String usercode = String.valueOf(user_uid_sequence);
		return usercode;
	}

	public void setqRGenerateService(QRGenerateService qRGenerateService) {
		this.qRGenerateService = qRGenerateService;
	}
	
	public class PosterThread implements Runnable {
		String image_name;
		String usercode;

		public void run() {
			try {
				qRGenerateService.generate_poster(image_name, usercode);

			} catch (Exception e) {
				logger.error("error:", e);
			}
		}

		public PosterThread(String image_name, String usercode) {
			this.image_name = image_name;
			this.usercode = usercode;
		}
	}
	
	/**
	 * 父类网络
	 */
	public List<Map<String,Object>> getParentsNet(String partyId){
		List<Map<String,Object>> result = new ArrayList<Map<String,Object>>();
		Map<String,Object> data = new HashMap<String, Object>();
		Party party = partyService.cachePartyBy(partyId, false);
		data.put("username", party.getUsername());
		data.put("usercode", party.getUsercode());
		data.put("rolename", party.getRolename());
		data.put("kyc_authority", party.getKyc_authority());
		result.add(data);
		List<UserRecom> parents = userRecomService.getParents(partyId);
		for(UserRecom recom:parents) {
			data = new HashMap<String, Object>();
			Party parent = partyService.cachePartyBy(recom.getReco_id().toString(), false);
			data.put("username", parent.getUsername());
			data.put("usercode", parent.getUsercode());
			data.put("rolename", parent.getRolename());
			data.put("kyc_authority", party.getKyc_authority());
			result.add(data);
		}
		Collections.reverse(result);
		return result;
	}

	public void setLogService(LogService logService) {
		this.logService = logService;
	}

	public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
		this.passwordEncoder = passwordEncoder;
	}

	public void setSysparaService(SysparaService sysparaService) {
		this.sysparaService = sysparaService;
	}

	public void setWalletLogService(WalletLogService walletLogService) {
		this.walletLogService = walletLogService;
	}
	
	public void setDataService(DataService dataService) {
		this.dataService = dataService;
	}

	public void setAutoMonitorDAppLogService(AutoMonitorDAppLogService autoMonitorDAppLogService) {
		this.autoMonitorDAppLogService = autoMonitorDAppLogService;
	}

	public void setPledgeOrderService(PledgeOrderService pledgeOrderService) {
		this.pledgeOrderService = pledgeOrderService;
	}

	public void setPledgeConfigService(PledgeConfigService pledgeConfigService) {
		this.pledgeConfigService = pledgeConfigService;
	}
	
	public void setPagedQueryDao(PagedQueryDao pagedQueryDao) {
		this.pagedQueryDao = pagedQueryDao;
	}

	public void setUserRecomService(UserRecomService userRecomService) {
		this.userRecomService = userRecomService;
	}

	public void setWalletService(WalletService walletService) {
		this.walletService = walletService;
	}

	public void setPartyService(PartyService partyService) {
		this.partyService = partyService;
	}

	public void setSecUserService(SecUserService secUserService) {
		this.secUserService = secUserService;
	}

	public void setRoleService(RoleService roleService) {
		this.roleService = roleService;
	}

	public void setMoneyLogService(MoneyLogService moneyLogService) {
		this.moneyLogService = moneyLogService;
	}

	public void setUserDataService(UserDataService userDataService) {
		this.userDataService = userDataService;
	}

	public void setUserService(UserService userService) {
		this.userService = userService;
	}
}
