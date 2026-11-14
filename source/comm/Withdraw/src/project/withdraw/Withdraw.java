package project.withdraw;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Table;

import kernel.bo.EntityObject;

@Table(name="T_WITHDRAW_ORDER")
public class Withdraw extends EntityObject {

	private static final long serialVersionUID = 5391586680142892251L;

	@Column(name="PARTY_ID")
	private Serializable partyId;
	/**
	 * 商户订单号
	 */
	@Column(name="ORDER_NO")
	private String order_no;
	/**
	 * 订单总金额（必须大于 0），单位为对应币种的最小货币单位，人民币为元。
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

	@Column(name="METHOD")
	private String method;

	/**
	 * 姓名
	 */
	@Column(name="USERNAME")
	private String username;

	/**
	 * USDT地址
	 */
	@Column(name="CHAIN_ADDRESS")
	private String address;
	
	/**
	 * 二维码地址
	 */
	@Column(name="QDCODE")
	private String qdcode;

	/**
	 * 订单备注
	 */
	@Column(name="REMARKS")
	private String remarks;
	
	/**
	 * 订单提交设备及IP
	 */
	@Column(name="DEVICE_IP")
	private String deviceIp;

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

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
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

	public String getQdcode() {
		return qdcode;
	}

	public void setQdcode(String qdcode) {
		this.qdcode = qdcode;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	public String getDeviceIp() {
		return deviceIp;
	}

	public void setDeviceIp(String deviceIp) {
		this.deviceIp = deviceIp;
	}
}
