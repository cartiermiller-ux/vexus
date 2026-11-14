package project.user.internal;

import java.util.Arrays;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.providers.encoding.PasswordEncoder;

import kernel.exception.BusinessException;
import kernel.util.Arith;
import kernel.util.StringUtils;
import kernel.util.UUIDGenerator;
import project.Constants;
import project.log.MoneyLog;
import project.log.MoneyLogService;
import project.party.PartyService;
import project.party.model.Party;
import project.party.model.UserRecom;
import project.party.recom.UserRecomService;
import project.syspara.Syspara;
import project.syspara.SysparaService;
import project.user.LocalNormalReg;
import project.user.LocalUserService;
import project.user.UserDataService;
import project.user.idcode.IdentifyingCodeTimeWindowService;
import project.wallet.Wallet;
import project.wallet.WalletExtend;
import project.wallet.WalletService;
import security.Role;
import security.RoleService;
import security.SaltSigureUtils;
import security.SecUser;
import security.internal.SecUserService;
import util.LockFilter;
import util.Strings;

public class LocalUserServiceImpl implements LocalUserService {
	
	private RoleService roleService;
	
	private PartyService partyService;
	
	private WalletService walletService;
	
	protected JdbcTemplate jdbcTemplate;
	
	private SecUserService secUserService;
	
	private SysparaService sysparaService;
	
	private UserDataService userDataService;
	
	private PasswordEncoder passwordEncoder;
	
	private UserRecomService userRecomService;
	
	protected MoneyLogService moneyLogService;
	
	private IdentifyingCodeTimeWindowService identifyingCodeTimeWindowService;

	public void saveRegister(LocalNormalReg reg, String type) {
		reg.setUsername(reg.getUsername().trim());
		reg.setPassword(reg.getPassword().trim());
		
		if (!"null".equals(reg.getSafeword()) && !StringUtils.isEmptyString(reg.getSafeword())) {
			reg.setSafeword(reg.getSafeword().trim());
		}

		String key = reg.getUsername();
		String authcode = this.identifyingCodeTimeWindowService.getAuthCode(key);

		if ((authcode == null) || (!authcode.equals(reg.getIdentifying_code()))) {
			throw new BusinessException("验证码不正确");
		}

		Party party_reco = this.partyService.findPartyByUsercode(reg.getReco_usercode());

		if ("true".equals(this.sysparaService.find("register_need_usercode").getValue())) {
			if (null == party_reco) {
				throw new BusinessException("推荐码不正确");
			}
			if (Constants.SECURITY_ROLE_TEST.equals(party_reco.getRolename())) {
				throw new BusinessException("推荐人无权限推荐");
			}
			if (!party_reco.getEnabled()) {
				throw new BusinessException("推荐人无权限推荐");
			}
		}

		if ("true".equals(this.sysparaService.find("register_need_usercode_turn").getValue())) {
			if (!party_reco.getRegister_usercode()) {
				throw new BusinessException("推荐人无权限推荐");
			}
		}

		if (this.secUserService.findUserByLoginName(reg.getUsername()) != null) {
			throw new BusinessException("用户名重复");
		}
		
		int ever_user_level_num = this.sysparaService.find("ever_user_level_num").getInteger();
		int ever_user_level_num_custom = this.sysparaService.find("ever_user_level_num_custom").getInteger();

		Party party = new Party();
		party.setUsername(reg.getUsername());
		party.setUsercode(this.getUsercode());
		party.setUser_level(ever_user_level_num_custom * 10 + ever_user_level_num);
		party.setSafeword(this.passwordEncoder.encodePassword(reg.getSafeword(), SaltSigureUtils.saltfigure));
		party.setRolename(Constants.SECURITY_ROLE_MEMBER);
		party = this.partyService.save(party);
				
		if (type.equals("1")) {
			// 手机注册			
			if (StringUtils.isEmptyString(reg.getUsername()) || reg.getUsername().length() > 20) {
				throw new BusinessException("请输入正确的手机号码");
			}
			this.savePhone(reg.getUsername(), party.getId().toString());
		} else {
			// 邮箱注册
			if (!Strings.isEmail(reg.getUsername())) {
				throw new BusinessException("请输入正确的邮箱地址");
			}
			this.saveEmail(reg.getUsername(), party.getId().toString());
		}

		Role role = this.roleService.findRoleByName(Constants.SECURITY_ROLE_MEMBER);

		SecUser secUser = new SecUser();
		secUser.setPartyId(String.valueOf(party.getId()));
		secUser.getRoles().add(role);
		secUser.setUsername(reg.getUsername());
		secUser.setPassword(reg.getPassword());
		this.secUserService.saveUser(secUser);

		// usdt账户
		Wallet wallet = new Wallet();
		wallet.setPartyId(party.getId().toString());
		this.walletService.save(wallet);

		if (party_reco != null) {
			UserRecom userRecom = new UserRecom();
			userRecom.setPartyId(party.getId());
			// 父类partyId
			userRecom.setReco_id(party_reco.getId());
			this.userRecomService.save(userRecom);
		}

		String uuid = UUIDGenerator.getUUID();
		String partyId = party.getId().toString();
		String partyRecoId = party_reco != null ? party_reco.getId().toString() : "";
		this.jdbcTemplate.execute("INSERT INTO T_USER(UUID,PARTY_ID,PARENT_PARTY_ID) VALUES('"+uuid+"','"+partyId+"','"+partyRecoId+"')");
		
		this.userDataService.saveRegister(party.getId());

		// 用户注册自动赠送金额
		String register_gift_coin = this.sysparaService.find("register_gift_coin").getValue();
		if (!"".equals(register_gift_coin) && register_gift_coin != null) {
			
			String[] register_gift_coins = register_gift_coin.split(",");
			String gift_symbol = register_gift_coins[0];
			double gift_sum = Double.valueOf(register_gift_coins[1]);
			
			if ("usdt".equals(gift_symbol)) {
				
				Wallet walletExtend = this.walletService.saveWalletByPartyId(party.getId());
				double amount_before = walletExtend.getMoney();
				if (Arith.add(gift_sum, walletExtend.getMoney()) < 0.0D) {
					throw new BusinessException("操作失败！修正后账户余额小于0。");
				}
				this.walletService.update(wallet.getPartyId().toString(), gift_sum);
				
				// 保存账变日志
				MoneyLog moneyLog = new MoneyLog();
				moneyLog.setCategory(Constants.MONEYLOG_CATEGORY_COIN);
				moneyLog.setAmount_before(amount_before);
				moneyLog.setAmount(gift_sum);
				moneyLog.setAmount_after(Arith.add(walletExtend.getMoney(), gift_sum));
				moneyLog.setPartyId(party.getId());
				moneyLog.setWallettype(gift_symbol.toUpperCase());
				moneyLog.setContent_type(Constants.MONEYLOG_CONTENT_RECHARGE);
				moneyLog.setLog("用户注册自动赠送金额");
				this.moneyLogService.save(moneyLog);
				
			} else {
				
				WalletExtend walletExtend = this.walletService.saveExtendByPara(party.getId(), gift_symbol);
				double amount_before = walletExtend.getAmount();
				if (Arith.add(gift_sum, walletExtend.getAmount()) < 0.0D) {
					throw new BusinessException("操作失败！修正后账户余额小于0。");
				}
				this.walletService.updateExtend(walletExtend.getPartyId().toString(), gift_symbol, gift_sum);

				// 保存账变日志
				MoneyLog moneyLog = new MoneyLog();
				moneyLog.setCategory(Constants.MONEYLOG_CATEGORY_COIN);
				moneyLog.setAmount_before(amount_before);
				moneyLog.setAmount(gift_sum);
				moneyLog.setAmount_after(Arith.add(walletExtend.getAmount(), gift_sum));
				moneyLog.setPartyId(party.getId());
				moneyLog.setWallettype(gift_symbol.toUpperCase());
				moneyLog.setContent_type(Constants.MONEYLOG_CONTENT_RECHARGE);
				moneyLog.setLog("用户注册自动赠送金额");
				this.moneyLogService.save(moneyLog);
			}
		}

		this.identifyingCodeTimeWindowService.delAuthCode(key);
	}
	
	/**
	 * 手机/邮箱注册（无验证码）
	 */
	public void saveRegisterNoVerifcode(LocalNormalReg reg, String type) {
		
		reg.setUsername(reg.getUsername().trim());
		reg.setPassword(reg.getPassword().trim());
		
		if (!"null".equals(reg.getSafeword()) && !StringUtils.isEmptyString(reg.getSafeword())) {
			reg.setSafeword(reg.getSafeword().trim());
		}

		Party party_reco = this.partyService.findPartyByUsercode(reg.getReco_usercode());

		if ("true".equals(this.sysparaService.find("register_need_usercode").getValue())) {
			if (null == party_reco) {
				throw new BusinessException("推荐码不正确");
			}
			if (Constants.SECURITY_ROLE_TEST.equals(party_reco.getRolename())) {
				throw new BusinessException("推荐人无权限推荐");
			}
			if (!party_reco.getEnabled()) {
				throw new BusinessException("推荐人无权限推荐");
			}
		}

		if ("true".equals(this.sysparaService.find("register_need_usercode_turn").getValue())) {
			if (!party_reco.getRegister_usercode()) {
				throw new BusinessException("推荐人无权限推荐");
			}
		}

		if (this.secUserService.findUserByLoginName(reg.getUsername()) != null) {
			throw new BusinessException("用户名重复");
		}
		
		int ever_user_level_num = this.sysparaService.find("ever_user_level_num").getInteger();
		int ever_user_level_num_custom = this.sysparaService.find("ever_user_level_num_custom").getInteger();

		Party party = new Party();
		party.setUsername(reg.getUsername());
		party.setUsercode(this.getUsercode());
		party.setUser_level(ever_user_level_num_custom * 10 + ever_user_level_num);
		party.setSafeword(this.passwordEncoder.encodePassword(reg.getSafeword(), SaltSigureUtils.saltfigure));
		party.setRolename(Constants.SECURITY_ROLE_MEMBER);
		
		if (type.equals("1")) {
			// 手机注册
			if (StringUtils.isEmptyString(reg.getUsername()) || reg.getUsername().length() > 20) {
				throw new BusinessException("请输入正确的手机号码");
			}
			party.setPhone(reg.getUsername());
			party.setPhone_authority(false);
		} else if (type.equals("2")) {
			// 邮箱注册
			if (!Strings.isEmail(reg.getUsername())) {
				throw new BusinessException("请输入正确的邮箱地址");
			}
			party.setEmail(reg.getUsername());
			party.setEmail_authority(false);
		} else {
			// 用户名注册
			if (reg.getUsername().length() < 6 || reg.getUsername().length() > 30) {
				throw new BusinessException("用户名必须6-30位");
			}
		}
		
		party = this.partyService.save(party);

		Role role = this.roleService.findRoleByName(Constants.SECURITY_ROLE_MEMBER);

		SecUser secUser = new SecUser();
		secUser.setPartyId(String.valueOf(party.getId()));
		secUser.getRoles().add(role);
		secUser.setUsername(reg.getUsername());
		secUser.setPassword(reg.getPassword());
		this.secUserService.saveUser(secUser);

		// usdt账户
		Wallet wallet = new Wallet();
		wallet.setPartyId(party.getId().toString());
		this.walletService.save(wallet);

		if (party_reco != null) {
			UserRecom userRecom = new UserRecom();
			userRecom.setPartyId(party.getId());
			// 父类partyId
			userRecom.setReco_id(party_reco.getId());
			this.userRecomService.save(userRecom);
		}

		String uuid = UUIDGenerator.getUUID();
		String partyId = party.getId().toString();
		String partyRecoId = party_reco != null ? party_reco.getId().toString() : "";
		this.jdbcTemplate.execute("INSERT INTO T_USER(UUID,PARTY_ID,PARENT_PARTY_ID) VALUES('"+uuid+"','"+partyId+"','"+partyRecoId+"')");
		
		this.userDataService.saveRegister(party.getId());

		// 用户注册自动赠送金额
		String register_gift_coin = this.sysparaService.find("register_gift_coin").getValue();
		if (!"".equals(register_gift_coin) && register_gift_coin != null) {
			
			String[] register_gift_coins = register_gift_coin.split(",");
			String gift_symbol = register_gift_coins[0];
			double gift_sum = Double.valueOf(register_gift_coins[1]);
			
			if ("usdt".equals(gift_symbol)) {
				
				Wallet walletExtend = this.walletService.saveWalletByPartyId(party.getId());
				double amount_before = walletExtend.getMoney();
				if (Arith.add(gift_sum, walletExtend.getMoney()) < 0.0D) {
					throw new BusinessException("操作失败！修正后账户余额小于0。");
				}
				this.walletService.update(wallet.getPartyId().toString(), gift_sum);
				
				// 保存账变日志
				MoneyLog moneyLog = new MoneyLog();
				moneyLog.setCategory(Constants.MONEYLOG_CATEGORY_COIN);
				moneyLog.setAmount_before(amount_before);
				moneyLog.setAmount(gift_sum);
				moneyLog.setAmount_after(Arith.add(walletExtend.getMoney(), gift_sum));
				moneyLog.setPartyId(party.getId());
				moneyLog.setWallettype(gift_symbol.toUpperCase());
				moneyLog.setContent_type(Constants.MONEYLOG_CONTENT_RECHARGE);
				moneyLog.setLog("用户注册自动赠送金额");
				this.moneyLogService.save(moneyLog);
				
			} else {
				
				WalletExtend walletExtend = this.walletService.saveExtendByPara(party.getId(), gift_symbol);
				double amount_before = walletExtend.getAmount();
				if (Arith.add(gift_sum, walletExtend.getAmount()) < 0.0D) {
					throw new BusinessException("操作失败！修正后账户余额小于0。");
				}
				this.walletService.updateExtend(walletExtend.getPartyId().toString(), gift_symbol, gift_sum);

				// 保存账变日志
				MoneyLog moneyLog = new MoneyLog();
				moneyLog.setCategory(Constants.MONEYLOG_CATEGORY_COIN);
				moneyLog.setAmount_before(amount_before);
				moneyLog.setAmount(gift_sum);
				moneyLog.setAmount_after(Arith.add(walletExtend.getAmount(), gift_sum));
				moneyLog.setPartyId(party.getId());
				moneyLog.setWallettype(gift_symbol.toUpperCase());
				moneyLog.setContent_type(Constants.MONEYLOG_CONTENT_RECHARGE);
				moneyLog.setLog("用户注册自动赠送金额");
				this.moneyLogService.save(moneyLog);
			}
		}
	}
	
	/**
	 * 体验用户注册
	 */
	public void saveRegisterTest(LocalNormalReg reg, String type) {
		reg.setUsername(reg.getUsername().trim());
		reg.setPassword(reg.getPassword().trim());
		
		if (!"null".equals(reg.getSafeword()) && !StringUtils.isEmptyString(reg.getSafeword())) {
			reg.setSafeword(reg.getSafeword().trim());
		}

		String test_user_code = this.sysparaService.find("test_user_code").getValue();
		if(!"0".equals(test_user_code)) {
			if (!reg.getReco_usercode().equals(test_user_code)) {
				throw new BusinessException("试用码不正确");
			}
		}

		if (this.secUserService.findUserByLoginName(reg.getUsername()) != null) {
			throw new BusinessException("用户名重复");
		}
		
		int ever_user_level_num = this.sysparaService.find("ever_user_level_num").getInteger();
		int ever_user_level_num_custom = this.sysparaService.find("ever_user_level_num_custom").getInteger();

		Party party = new Party();
		party.setUsername(reg.getUsername());
		party.setUsercode(this.getUsercode());
		party.setUser_level(ever_user_level_num_custom * 10 + ever_user_level_num);
		party.setSafeword(this.passwordEncoder.encodePassword(reg.getSafeword(), SaltSigureUtils.saltfigure));
		party.setRolename(Constants.SECURITY_ROLE_TEST);
		
		// 用户名注册
		if (reg.getUsername().length() < 6 || reg.getUsername().length() > 30) {
			throw new BusinessException("用户名必须6-30位");
		}
		
		party = this.partyService.save(party);

		Role role = this.roleService.findRoleByName(Constants.SECURITY_ROLE_TEST);

		SecUser secUser = new SecUser();
		secUser.setPartyId(String.valueOf(party.getId()));
		secUser.getRoles().add(role);
		secUser.setUsername(reg.getUsername());
		secUser.setPassword(reg.getPassword());
		this.secUserService.saveUser(secUser);
		double test_user_money = sysparaService.find("test_user_money").getDouble();
		// usdt账户
		Wallet wallet = new Wallet();
		wallet.setPartyId(party.getId().toString());
		wallet.setMoney(test_user_money);
		this.walletService.save(wallet);
	
		String uuid = UUIDGenerator.getUUID();
		String partyId = party.getId().toString();
		String partyRecoId = "";
		this.jdbcTemplate.execute("INSERT INTO T_USER(UUID,PARTY_ID,PARENT_PARTY_ID) VALUES('"+uuid+"','"+partyId+"','"+partyRecoId+"')");		
		this.userDataService.saveRegister(party.getId());		
	}
	
	/**
	 * 承兑商注册
	 */
	public Party registerC2cUser(String username, String password, String re_password, String type, String usercode, String ip) {
		Party party = null;
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
			
			if (StringUtils.isEmptyString(type) || !Arrays.asList("1", "2").contains(type)) {
				throw new BusinessException("注册类型（手机或邮箱）不能为空");
			}

			LocalNormalReg reg = new LocalNormalReg();
			reg.setUsername(username);
			reg.setPassword(password);
			reg.setSafeword("000000");
			reg.setReco_usercode(usercode);

			this.saveRegisterNoVerifcode(reg, type);
			SecUser secUser = this.secUserService.findUserByLoginName(username);
			party = this.partyService.cachePartyBy(secUser.getPartyId(), false);
		} catch (BusinessException e) {
			throw e;
		} catch (Throwable t) {
			throw t;
		} finally {
			if (lock) {
				LockFilter.remove(username);
			}
		}
		
		return party;
	}

	/**
	 * 无验证码注册
	 */
	public void saveRegisterUsername(LocalNormalReg reg) {
		reg.setUsername(reg.getUsername().trim());
		reg.setPassword(reg.getPassword().trim());
		reg.setSafeword(reg.getSafeword().trim());

		Party party_reco = this.partyService.findPartyByUsercode(reg.getReco_usercode());
//		用户注册是否需要推荐码
		if ("true".equals(sysparaService.find("register_need_usercode").getValue())) {

			if (party_reco == null) {

				throw new BusinessException("推荐码不正确");
			}
			if (Constants.SECURITY_ROLE_TEST.equals(party_reco.getRolename())) {
				throw new BusinessException("推荐人无权限推荐");
			}
			if (!party_reco.getEnabled()) {
				throw new BusinessException("推荐人无权限推荐");
			}

		}

		if ("true".equals(sysparaService.find("register_need_usercode_turn").getValue())) {
			if (!party_reco.getRegister_usercode()) {
				throw new BusinessException("推荐人无权限推荐");
			}
		}

		if (secUserService.findUserByLoginName(reg.getUsername()) != null) {
			throw new BusinessException("用户名重复");
		}

		/**
		 * 用户code
		 */
		String usercode = getUsercode();

		/**
		 * party
		 */
		Party party = new Party();
		party.setUsername(reg.getUsername());
		party.setUsercode(usercode);
		int ever_user_level_num = sysparaService.find("ever_user_level_num").getInteger();
		int ever_user_level_num_custom = this.sysparaService.find("ever_user_level_num_custom").getInteger();
		party.setUser_level(ever_user_level_num_custom * 10 + ever_user_level_num);
		party.setSafeword(passwordEncoder.encodePassword(reg.getSafeword(), SaltSigureUtils.saltfigure));
		party.setRolename(Constants.SECURITY_ROLE_MEMBER);

		party = partyService.save(party);

		/**
		 * SecUser
		 */
		Role role = this.roleService.findRoleByName(Constants.SECURITY_ROLE_MEMBER);

		SecUser secUser = new SecUser();
		secUser.setPartyId(String.valueOf(party.getId()));
		secUser.getRoles().add(role);

		secUser.setUsername(reg.getUsername());

		secUser.setPassword(reg.getPassword());

		this.secUserService.saveUser(secUser);

		/**
		 * usdt账户
		 */
		Wallet wallet = new Wallet();
		wallet.setPartyId(party.getId().toString());
		this.walletService.save(wallet);

		if (party_reco != null) {
			UserRecom userRecom = new UserRecom();
			userRecom.setPartyId(party.getId());
			userRecom.setReco_id(party_reco.getId());// 父类partyId
			this.userRecomService.save(userRecom);
		}

		String uuid = UUIDGenerator.getUUID();
		String partyId = party.getId().toString();
		String partyRecoId = party_reco != null?party_reco.getId().toString():"";
		jdbcTemplate.execute("INSERT INTO T_USER(UUID,PARTY_ID,PARENT_PARTY_ID) VALUES('"+uuid+"','"+partyId+"','"+partyRecoId+"')");
		
		this.userDataService.saveRegister(party.getId());

		/**
		 * 用户注册自动赠送金额 start
		 */
		String register_gift_coin = sysparaService.find("register_gift_coin").getValue();
		if (!"".equals(register_gift_coin) && register_gift_coin != null) {
			String[] register_gift_coins = register_gift_coin.split(",");
			String gift_symbol = register_gift_coins[0];
			double gift_sum = Double.valueOf(register_gift_coins[1]);

			if ("usdt".equals(gift_symbol)) {
				Wallet walletExtend = this.walletService.saveWalletByPartyId(party.getId());
				double amount_before = walletExtend.getMoney();
				if (Arith.add(gift_sum, walletExtend.getMoney()) < 0.0D) {
					throw new BusinessException("操作失败！修正后账户余额小于0。");
				}
				walletService.update(wallet.getPartyId().toString(), gift_sum);
				/*
				 * 保存账变日志
				 */
				MoneyLog moneyLog = new MoneyLog();
				moneyLog.setCategory(Constants.MONEYLOG_CATEGORY_COIN);
				moneyLog.setAmount_before(amount_before);
				moneyLog.setAmount(gift_sum);
				moneyLog.setAmount_after(Arith.add(walletExtend.getMoney(), gift_sum));

				moneyLog.setPartyId(party.getId());
				moneyLog.setWallettype(gift_symbol.toUpperCase());
				moneyLog.setContent_type(Constants.MONEYLOG_CONTENT_RECHARGE);
				moneyLog.setLog("用户注册自动赠送金额");
				this.moneyLogService.save(moneyLog);
			} else {
				WalletExtend walletExtend = this.walletService.saveExtendByPara(party.getId(), gift_symbol);
				double amount_before = walletExtend.getAmount();
				if (Arith.add(gift_sum, walletExtend.getAmount()) < 0.0D) {
					throw new BusinessException("操作失败！修正后账户余额小于0。");
				}
				walletService.updateExtend(walletExtend.getPartyId().toString(), gift_symbol, gift_sum);

				/*
				 * 保存账变日志
				 */
				MoneyLog moneyLog = new MoneyLog();
				moneyLog.setCategory(Constants.MONEYLOG_CATEGORY_COIN);
				moneyLog.setAmount_before(amount_before);
				moneyLog.setAmount(gift_sum);
				moneyLog.setAmount_after(Arith.add(walletExtend.getAmount(), gift_sum));

				moneyLog.setPartyId(party.getId());
				moneyLog.setWallettype(gift_symbol.toUpperCase());
				moneyLog.setContent_type(Constants.MONEYLOG_CONTENT_RECHARGE);
				moneyLog.setLog("用户注册自动赠送金额");
				this.moneyLogService.save(moneyLog);
			}
		}
	}

	/**
	 * 无验证码注册
	 */
	public void saveRegisterUsernameTest(LocalNormalReg reg) {
		reg.setUsername(reg.getUsername().trim());
		reg.setPassword(reg.getPassword().trim());
		reg.setSafeword(reg.getSafeword().trim());

		String test_user_code = sysparaService.find("test_user_code").getValue();

		if (StringUtils.isEmptyString(test_user_code) || !test_user_code.equals(reg.getReco_usercode())) {
			throw new BusinessException("试用码不正确");
		}

		if (secUserService.findUserByLoginName(reg.getUsername()) != null) {
			throw new BusinessException("用户名重复");
		}

		/**
		 * 用户code
		 */
		String usercode = getUsercode();

		/**
		 * party
		 */
		Party party = new Party();
		party.setUsername(reg.getUsername());
		party.setUsercode(usercode);
		int ever_user_level_num = sysparaService.find("ever_user_level_num").getInteger();
		int ever_user_level_num_custom = this.sysparaService.find("ever_user_level_num_custom").getInteger();
		party.setUser_level(ever_user_level_num_custom * 10 + ever_user_level_num);
		party.setSafeword(passwordEncoder.encodePassword(reg.getSafeword(), SaltSigureUtils.saltfigure));
		party.setRolename(Constants.SECURITY_ROLE_TEST);
		// 关闭充值提现权限
		party.setRecharge_authority(false);
		party.setWithdraw_authority(false);
		party = partyService.save(party);

		/**
		 * SecUser
		 */
		Role role = this.roleService.findRoleByName(Constants.SECURITY_ROLE_TEST);

		SecUser secUser = new SecUser();
		secUser.setPartyId(String.valueOf(party.getId()));
		secUser.getRoles().add(role);

		secUser.setUsername(reg.getUsername());

		secUser.setPassword(reg.getPassword());

		this.secUserService.saveUser(secUser);

		double test_user_money = sysparaService.find("test_user_money").getDouble();
		/**
		 * usdt账户
		 */
		Wallet wallet = new Wallet();
		wallet.setPartyId(party.getId().toString());
		wallet.setMoney(test_user_money);
		this.walletService.save(wallet);
	}

	private String getUsercode() {
		Syspara syspara = this.sysparaService.find("user_uid_sequence");
		int random = (int) (Math.random() * 3 + 1);
		int user_uid_sequence = syspara.getInteger() + random;
		syspara.setValue(user_uid_sequence);
		this.sysparaService.update(syspara);
		String usercode = String.valueOf(user_uid_sequence);
		return usercode;
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

	public void setUserRecomService(UserRecomService userRecomService) {
		this.userRecomService = userRecomService;
	}

	@Override
	public String getPhone(String partyId) {
		Party party = this.partyService.cachePartyBy(partyId, false);
		return party.getPhone();
	}

	@Override
	public void savePhone(String phone, String partyId) {
		/**
		 * party
		 */
		Party party = this.partyService.cachePartyBy(partyId, false);
		party.setPhone(phone);
		party.setPhone_authority(true);

		// 十进制个位表示系统级别：1/新注册；2/邮箱谷歌手机其中有一个已验证；3/用户实名认证；4/用户高级认证；
		// 十进制十位表示自定义级别：对应在前端显示为如VIP1 VIP2等级、黄金 白银等级；
		// 如：级别11表示：新注册的前端显示为VIP1；
		int userLevel = party.getUser_level();
		party.setUser_level(((int) Math.floor(userLevel / 10)) * 10 + 2);
				
		this.partyService.update(party);
	}

	@Override
	public String getEmail(String partyId) {
		Party party = this.partyService.cachePartyBy(partyId, false);
		return party.getEmail();
	}

	@Override
	public void saveEmail(String email, String partyId) {
		/**
		 * party
		 */
		Party party = this.partyService.cachePartyBy(partyId, false);
		party.setEmail(email);
		party.setEmail_authority(true);

		// 十进制个位表示系统级别：1/新注册；2/邮箱谷歌手机其中有一个已验证；3/用户实名认证；4/用户高级认证；
		// 十进制十位表示自定义级别：对应在前端显示为如VIP1 VIP2等级、黄金 白银等级；
		// 如：级别11表示：新注册的前端显示为VIP1；
		int userLevel = party.getUser_level();
		party.setUser_level(((int) Math.floor(userLevel / 10)) * 10 + 2);
		
		this.partyService.update(party);
	}

	public void setUserDataService(UserDataService userDataService) {
		this.userDataService = userDataService;
	}
	
	public void setSysparaService(SysparaService sysparaService) {
		this.sysparaService = sysparaService;
	}

	public void setIdentifyingCodeTimeWindowService(IdentifyingCodeTimeWindowService identifyingCodeTimeWindowService) {
		this.identifyingCodeTimeWindowService = identifyingCodeTimeWindowService;
	}

	public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
		this.passwordEncoder = passwordEncoder;
	}

	public void setMoneyLogService(MoneyLogService moneyLogService) {
		this.moneyLogService = moneyLogService;
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}
}
