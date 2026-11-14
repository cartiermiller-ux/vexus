package project.monitor.withdraw;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Table;

import kernel.bo.EntityObject;

@Table(name="T_AUTO_MONITOR_WITHDRAW_ORDER")
public class AutoMonitorWithdraw extends EntityObject {

	private static final long serialVersionUID = 5391586680142892251L;

	@Column(name="PARTY_ID")
	private Serializable partyId;
	
	/**
	 * 商户订单号
	 */
	@Column(name="ORDER_NO")
	private String order_no;
	
	/**
	 * 订单到账金额（必须大于 0），单位为对应币种的最小货币单位，人民币为元。
	 */
	@Column(name="AMOUNT")
	private double amount;

	/**
	 * 订单总金额（必须大于 0），单位为对应币种的最小货币单位，人民币为元。
	 */
	@Column(name="VOLUME")
	private double volume;

	/**
	 * 订单手续费，USDT。
	 */
	@Column(name="AMOUNT_FEE")
	private double amount_fee;

	/**
	 * 状态 0 初始状态，未知 1 成功 2 失败，
	 */
	@Column(name="SUCCEEDED")
	private int succeeded = 0;

	/**
	 * 提现货币 CNY USD
	 */
	@Column(name="CURRENCY")
	private String currency;

	/**
	 * 错误信息
	 */
	@Column(name="FAILURE_MSG")
	private String failure_msg;
	
	/**
	 * 创建时间
	 */
	@Column(name="CREATE_TIME")
	private Date createTime;

	/**
	 * 审核操作时间
	 */
	@Column(name="REVIEWTIME")
	private Date reviewTime;

	@Column(name="TIME_SETTLE")
	private Date time_settle;

	/**
	 * 收款方式 bank 银行卡 alipay 支付宝 weixin 微信 paypal PayPal western 西联汇款 swift
	 * SWIFT国际汇款,USDT
	 * 
	 */
	@Column(name="METHOD")
	private String method;

	/**
	 * 账号，银行卡号，支付宝账号，电汇地址等
	 */
	@Column(name="ACCOUNT")
	private String account;
	
	/**
	 * 二维码
	 */
	@Column(name="QDCODE")
	private String qdcode;

	/**
	 * 姓名
	 */
	@Column(name="USERNAME")
	private String username;

	/**
	 * 银行
	 */
	@Column(name="BANK")
	private String bank;
	
	/**
	 * 开户行
	 */
	@Column(name="DEPOSIT_BANK")
	private String deposit_bank;

	/**
	 * USDT地址
	 */
	@Column(name="CHAIN_ADDRESS")
	private String address;

	public Serializable getPartyId() {
		return partyId;
	}

	public void setPartyId(Serializable partyId) {
		this.partyId = partyId;
	}

	public String getOrder_no() {
		return order_no;
	}

	public void setOrder_no(String order_no) {
		this.order_no = order_no;
	}

	public int getSucceeded() {
		return succeeded;
	}

	public void setSucceeded(int succeeded) {
		this.succeeded = succeeded;
	}

	public String getFailure_msg() {
		return failure_msg;
	}

	public void setFailure_msg(String failure_msg) {
		this.failure_msg = failure_msg;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	public double getAmount_fee() {
		return amount_fee;
	}

	public void setAmount_fee(double amount_fee) {
		this.amount_fee = amount_fee;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Date getTime_settle() {
		return time_settle;
	}

	public void setTime_settle(Date time_settle) {
		this.time_settle = time_settle;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getAccount() {
		return account;
	}

	public void setAccount(String account) {
		this.account = account;
	}

	public String getQdcode() {
		return qdcode;
	}

	public void setQdcode(String qdcode) {
		this.qdcode = qdcode;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getBank() {
		return bank;
	}

	public void setBank(String bank) {
		this.bank = bank;
	}

	public String getDeposit_bank() {
		return deposit_bank;
	}

	public void setDeposit_bank(String deposit_bank) {
		this.deposit_bank = deposit_bank;
	}

	public double getVolume() {
		return volume;
	}

	public void setVolume(double volume) {
		this.volume = volume;
	}

	public Date getReviewTime() {
		return reviewTime;
	}

	public void setReviewTime(Date reviewTime) {
		this.reviewTime = reviewTime;
	}

}
