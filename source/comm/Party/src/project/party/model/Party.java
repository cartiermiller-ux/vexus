package project.party.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Table;

import kernel.bo.EntityObject;

@Table(name="PAT_PARTY")
public class Party extends EntityObject {
	
	private static final long serialVersionUID = -66585270719884278L;
	
	/**
	 * 角色
	 */
	@Column(name="ROLENAME")
	private String rolename;
	
	/**
	 * 用户code-UID
	 */
	@Column(name="USERCODE")
	private String usercode;
	
	/**
	 * 用户名-用户WSID
	 */
	@Column(name="USERNAME")
	private String username;

	/**
	 * 安全码，资金密码，盐值+safeword MD5编码
	 */
	@Column(name="SAFEWORD")
	private String safeword;

	@Column(name="CREATE_TIME")
	private Date createTime;
	/**
	 * 最后登录时间
	 */
	@Column(name="LAST_LOGIN_TIME")
	private Date last_loginTime;
	/**
	 * 是否锁定，如果锁定可以登录、查看，但不能操作业务有关。
	 */
	@Column(name="ENABLED")
	private boolean enabled = true;
	/**
	 * 登录权限
	 */
	@Column(name="LOGINAUTHORITY")
	private boolean login_authority = true;
	/**
	 * 充值权限
	 */
	@Column(name="RECHARGE_AUTHORITY")
	private boolean recharge_authority = true;

	/**
	 * 提现权限
	 */
	@Column(name="WITHDRAWAUTHORITY")
	private boolean withdraw_authority = true;

	@Column(name="KYC_AUTHORITY")
	private boolean kyc_authority = false;
	
	/**
	 * 高级认证
	 */
	@Column(name="KYC_HIGHLEVEL_AUTHORITY")
	private boolean kyc_highlevel_authority = false;
	
	/**
	 * 邮件群UID
	 */
	@Column(name="EMAIL")
	private String email;

	/**
	 * 是否邮箱已认证
	 */
	@Column(name="EMAIL_AUTHORITY")
	private boolean email_authority = false;

	@Column(name="PHONE")
	private String phone;

	/**
	 * 是否手机已认证
	 */
	@Column(name="PHONE_AUTHORITY")
	private boolean phone_authority = false;
	
	/**
	 * 是否活跃 7天无流水，则认为不是活跃用户呢
	 */
	@Column(name="ACTIVE")
	private boolean active = true;
	/**
	 * 是否在线
	 */
	private boolean online = false;

	/**
	 * 注备
	 */
	@Column(name="REMARKS")
	private String remarks;

	/**
	 * 名称-昵称
	 */
	@Column(name="NAME")
	private String name;
	
	/**
	 * 当日提现限制金额
	 */
	@Column(name="WITHDRAW_LIMIT_AMOUNT")
	private double withdraw_limit_amount;
	
	/**
	 * 余额收益配置金额
	 */
	@Column(name="BALANCE_INCOME_CONFIG")
	private String balance_income_config;

	/**
	 * 当前可用提现流水 WITHDRAW_LIMIT_NOW_AMOUNT
	 */
	@Column(name="WITHDRAW_LIMIT_NOW_AMOUNT")
	private double withdraw_limit_now_amount;

	/**
	 * 登陆Ip
	 * 
	 * @return
	 */
	@Column(name="LOGIN_IP")
	private String login_ip;
	
	/**
	 * 是否为赠送用户（达到限制金额）
	 */
	@Column(name="GIFT_USER")
	private boolean gift_user;
	
	/**
	 * 是否获得过赠送金额
	 */
	@Column(name="GIFT_MONEY_FLAG")
	private boolean gift_money_flag;
	
	/**
	 * 会员等级 默认1
	 * 
	 * 十进制个位表示系统级别：1/新注册；2/邮箱谷歌手机其中有一个已验证；3/用户实名认证；4/用户高级认证；
	 * 十进制十位表示自定义级别：对应在前端显示为如VIP1 VIP2等级、黄金 白银等级；
	 * 如：级别11表示：新注册的前端显示为VIP1；
	 */
	@Column(name="USER_LEVEL")
	private int user_level;
	
	/**
	 * 在推荐码邀请权限开启后，是否拥有的邀请注册权限
	 */
	@Column(name="REGSITER_USERCODE")
	private boolean register_usercode;
	
	/**
	 * 承兑商类型：0不是承兑商/1后台承兑商/2用户承兑商
	 */
	@Column(name="C2C_USER_TYPE")
	private int c2cUserType;

	public Date getCreateTime() {
		return this.createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public String getUsername() {
		return this.username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getSafeword() {
		return this.safeword;
	}

	public void setSafeword(String safeword) {
		this.safeword = safeword;
	}

	public Date getLast_loginTime() {
		return last_loginTime;
	}

	public void setLast_loginTime(Date last_loginTime) {
		this.last_loginTime = last_loginTime;
	}

	public boolean getEnabled() {
		return this.enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public String getRolename() {
		return rolename;
	}

	public void setRolename(String rolename) {
		this.rolename = rolename;
	}

	public boolean getLogin_authority() {
		return login_authority;
	}

	public void setLogin_authority(boolean login_authority) {
		this.login_authority = login_authority;
	}

	public boolean getWithdraw_authority() {
		return withdraw_authority;
	}

	public void setWithdraw_authority(boolean withdraw_authority) {
		this.withdraw_authority = withdraw_authority;
	}

	public String getUsercode() {
		return usercode;
	}

	public void setUsercode(String usercode) {
		this.usercode = usercode;
	}

	public boolean isRecharge_authority() {
		return recharge_authority;
	}

	public void setRecharge_authority(boolean recharge_authority) {
		this.recharge_authority = recharge_authority;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public boolean getEmail_authority() {
		return this.email_authority;
	}

	public void setEmail_authority(boolean email_authority) {
		this.email_authority = email_authority;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isOnline() {
		return online;
	}

	public void setOnline(boolean online) {
		this.online = online;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public boolean getPhone_authority() {
		return this.phone_authority;
	}

	public void setPhone_authority(boolean phone_authority) {
		this.phone_authority = phone_authority;
	}

	public boolean getKyc_authority() {
		return kyc_authority;
	}

	public void setKyc_authority(boolean kyc_authority) {
		this.kyc_authority = kyc_authority;
	}

	public boolean isKyc_highlevel_authority() {
		return kyc_highlevel_authority;
	}

	public void setKyc_highlevel_authority(boolean kyc_highlevel_authority) {
		this.kyc_highlevel_authority = kyc_highlevel_authority;
	}

	public double getWithdraw_limit_amount() {
		return withdraw_limit_amount;
	}

	public void setWithdraw_limit_amount(double withdraw_limit_amount) {
		this.withdraw_limit_amount = withdraw_limit_amount;
	}

	public String getLogin_ip() {
		return login_ip;
	}

	public void setLogin_ip(String login_ip) {
		this.login_ip = login_ip;
	}

	public double getWithdraw_limit_now_amount() {
		return withdraw_limit_now_amount;
	}

	public void setWithdraw_limit_now_amount(double withdraw_limit_now_amount) {
		this.withdraw_limit_now_amount = withdraw_limit_now_amount;
	}

	public boolean getGift_user() {
		return gift_user;
	}

	public void setGift_user(boolean gift_user) {
		this.gift_user = gift_user;
	}

	public boolean getGift_money_flag() {
		return gift_money_flag;
	}

	public void setGift_money_flag(boolean gift_money_flag) {
		this.gift_money_flag = gift_money_flag;
	}

	public int getUser_level() {
		return user_level;
	}

	public void setUser_level(int user_level) {
		this.user_level = user_level;
	}

	public boolean getRegister_usercode() {
		return register_usercode;
	}

	public void setRegister_usercode(boolean register_usercode) {
		this.register_usercode = register_usercode;
	}

	public int getC2cUserType() {
		return c2cUserType;
	}

	public void setC2cUserType(int c2cUserType) {
		this.c2cUserType = c2cUserType;
	}

	public String getBalance_income_config() {
		return balance_income_config;
	}

	public void setBalance_income_config(String balance_income_config) {
		this.balance_income_config = balance_income_config;
	}
	
}
