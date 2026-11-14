package project.monitor.internal;

import java.text.MessageFormat;
import java.util.List;

import kernel.exception.BusinessException;
import kernel.util.StringUtils;
import kernel.web.ApplicationUtil;
import project.Constants;
import project.log.LogService;
import project.monitor.AdminDAppUserService;
import project.party.PartyService;
import project.party.model.Party;
import project.party.model.UserRecom;
import project.party.recom.UserRecomService;
import project.syspara.Syspara;
import project.syspara.SysparaService;
import project.user.User;
import project.wallet.Wallet;
import project.wallet.WalletService;
import security.Role;
import security.RoleService;
import security.SecUser;
import security.internal.SecUserService;

public class AdminDAppUserServiceImpl implements AdminDAppUserService {
	
	private LogService logService;
	
	private RoleService roleService;

	private PartyService partyService;
	
	private WalletService walletService;

	private SysparaService sysparaService;
	
	private SecUserService secUserService;

	private UserRecomService userRecomService;

	@Override
	public void save(String address, boolean login_authority, boolean withdraw_authority, boolean enabled,String remarks, String code, String operator, String ip) {
		address = address.trim();
		//统一转换成小写
		address = address.toLowerCase();
		
		//操作人为代理商时，默认添加代理商的usercode
		SecUser sec =  this.secUserService.findUserByLoginName(operator);
		if(!StringUtils.isEmptyString(sec.getPartyId())) {
			Party party = this.partyService.cachePartyBy(sec.getPartyId(), false);
			for (Role role : sec.getRoles()) {
				if (Constants.SECURITY_ROLE_AGENT.equals(role.getRoleName())||Constants.SECURITY_ROLE_AGENTLOW.equals(role.getRoleName())) {
					code=party.getUsercode();
				}
			}
		}
		
		if (partyService.findPartyByUsername(address) != null) {
			throw new BusinessException("地址已存在");
		}

		/**
		 * 用户code
		 */
		String usercode = getUsercode();

		Party party_reco = null;
		if (!StringUtils.isNullOrEmpty(code)) {
			party_reco = this.partyService.findPartyByUsercode(code);
			if (party_reco == null) {
				throw new BusinessException("上级UID推荐码不正确");
			}

		}
		
		int ever_user_level_num = this.sysparaService.find("ever_user_level_num").getInteger();
		int ever_user_level_num_custom = this.sysparaService.find("ever_user_level_num_custom").getInteger();

		/**
		 * party
		 */
		Party party = new Party();
		party.setUsername(address);
		party.setLogin_authority(login_authority);
		party.setWithdraw_authority(withdraw_authority);
		party.setEnabled(enabled);
		party.setRemarks(remarks);
		party.setUsercode(usercode);
		party.setUser_level(ever_user_level_num_custom * 10 + ever_user_level_num);

		party.setRolename(Constants.SECURITY_ROLE_GUEST);

		party = partyService.save(party);

		User user = new User();
		user.setPartyId(party.getId());
		ApplicationUtil.executeInsert(user);

		if (party_reco != null) {
			UserRecom userRecom = new UserRecom();
			userRecom.setPartyId(party.getId());
			userRecom.setReco_id(party_reco.getId());// 父类partyId
			this.userRecomService.save(userRecom);
		}

		/**
		 * SecUser
		 */
		Role role = this.roleService.findRoleByName(Constants.SECURITY_ROLE_GUEST);

		SecUser secUser = new SecUser();
		secUser.setPartyId(String.valueOf(party.getId()));
		secUser.getRoles().add(role);

		secUser.setUsername(address);
		secUser.setEnabled(login_authority);
		this.secUserService.saveUser(secUser);

		/**
		 * usdt账户
		 */
		Wallet wallet = new Wallet();
		wallet.setPartyId(party.getId().toString());
		this.walletService.save(wallet);

		/**
		 * 加入节点操作，未完成
		 */

		project.log.Log log = new project.log.Log();
		log.setCategory(Constants.LOG_CATEGORY_OPERATION);
		log.setUsername(party.getUsername());
		log.setOperator(operator);
		log.setLog("ip:" + ip + ",管理员手动新增了演示用户:" + address);
		logService.saveSync(log);

	}

	@Override
	public void update(String partyId, boolean login_authority, boolean enabled, boolean withdraw_authority, Integer user_level_custom,
			String balance_income_config,String remarks, String operatorUsername, String ip) {
		SecUser sec =  this.secUserService.findUserByLoginName(operatorUsername);
		
		Party party = this.partyService.cachePartyBy(partyId, false);

		for (Role role : sec.getRoles()) {
			//代理商只能修改演示账户
			if (Constants.SECURITY_ROLE_AGENT.equals(role.getRoleName())||Constants.SECURITY_ROLE_AGENTLOW.equals(role.getRoleName())) {
//					&&party!=null&&!Constants.SECURITY_ROLE_GUEST.equals(party.getRolename())) {
				if(party!=null&&!Constants.SECURITY_ROLE_GUEST.equals(party.getRolename())) {
					throw new BusinessException("只能修改演示账户");
				}
				List<String> children = userRecomService.findChildren(sec.getPartyId());
				if(!children.contains(partyId)) {
					throw new BusinessException("只能修改自己线下的用户演示账户");
					
				}
			}
		}
		String logtxt = MessageFormat.format(
				"ip:" + ip + ",管理员手动修改了用户信息,用户名:{0},原登录权限:{1},原是否业务锁定:{2},原提现权限:{3},原备注:{4}", 
				party.getUsername(),
				party.getLogin_authority(), 
				party.getEnabled(), 
				party.getWithdraw_authority(), 
				party.getRemarks());
		party.setRemarks(remarks);
		party.setLogin_authority(login_authority);
		party.setEnabled(enabled);
		party.setWithdraw_authority(withdraw_authority);
		party.setBalance_income_config(balance_income_config);
		if (null != user_level_custom) {			
			// 十进制个位表示系统级别：1/新注册；2/邮箱谷歌手机其中有一个已验证；3/用户实名认证；4/用户高级认证；
			// 十进制十位表示自定义级别：对应在前端显示为如VIP1 VIP2等级、黄金 白银等级；
			// 如：级别11表示：新注册的前端显示为VIP1；
			int userLevel = party.getUser_level();
			party.setUser_level(user_level_custom.intValue() * 10 + (int) (userLevel % 10));
		}
			
		this.partyService.update(party);
		
		logtxt += MessageFormat.format(",新登录权限:{0},新是否业务锁定:{1},新提现权限:{2},新备注:{3}", party.getLogin_authority(),
				party.getEnabled(), party.getWithdraw_authority(), party.getRemarks());
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

	private String getUsercode() {
		Syspara syspara = sysparaService.find("user_uid_sequence");
		int random = (int) (Math.random() * 3 + 1);
		int user_uid_sequence = syspara.getInteger() + random;
		syspara.setValue(user_uid_sequence);
		sysparaService.update(syspara);

		String usercode = String.valueOf(user_uid_sequence);
		return usercode;
	}

	public void setPartyService(PartyService partyService) {
		this.partyService = partyService;
	}

	public void setSysparaService(SysparaService sysparaService) {
		this.sysparaService = sysparaService;
	}

	public void setUserRecomService(UserRecomService userRecomService) {
		this.userRecomService = userRecomService;
	}

	public void setWalletService(WalletService walletService) {
		this.walletService = walletService;
	}

	public void setLogService(LogService logService) {
		this.logService = logService;
	}

	public void setSecUserService(SecUserService secUserService) {
		this.secUserService = secUserService;
	}

	public void setRoleService(RoleService roleService) {
		this.roleService = roleService;
	}
}
