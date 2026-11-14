package project.c2c;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Table;

import kernel.bo.EntityObject;

@Table(name="T_C2C_USER")
public class C2cUser extends EntityObject {

	private static final long serialVersionUID = 4911926779531882495L;

	/**
	 * C2C管理员PARTY_ID
	 */
	@Column(name="C2C_MANAGER_PARTY_ID")
	private String c2cManagerPartyId;
	
	/**
	 * 1后台承兑商/2用户承兑商
	 */
	@Column(name="C2C_USER_TYPE")
	private int c2cUserType;

	/**
	 * 承兑商CODE
	 */
	@Column(name="C2C_USER_CODE")
	private String c2cUserCode;

	/**
	 * 承兑商PARTY_ID
	 */
	@Column(name="C2C_USER_PARTY_ID")
	private String c2cUserPartyId;

	/**
	 * 承兑商昵称
	 */
	@Column(name="NICK_NAME")
	private String nickName;

	/**
	 * 承兑商头像
	 */
	@Column(name="HEAD_IMG")
	private String headImg;
	
	/**
	 * 剩余保证金
	 */
	@Column(name="DEPOSIT")
	private double deposit;
	
	/**
	 * 保证金
	 */
	@Column(name="DEPOSIT_OPEN")
	private double depositOpen;
	
	/**
	 * 保证金赠送比率
	 */
	@Column(name="DEPOSIT_GIFT_RATE")
	private double depositGiftRate;
	
	/**
	 * 30日成单数
	 */
	@Column(name="THIRTY_DAYS_ORDER")
	private int thirtyDaysOrder;
	
	/**
	 * 30日成单率
	 */
	@Column(name="THIRTY_DAYS_ORDER_RATIO")
	private double thirtyDaysOrderRatio;
	
	/**
	 * 30日平均放行时间
	 */
	@Column(name="THIRTY_DAYS_PASS_AVERAGE_TIME")
	private int thirtyDaysPassAverageTime;
	
	/**
	 * 30日平均付款时间
	 */
	@Column(name="THIRTY_DAYS_PAY_AVERAGE_TIME")
	private int thirtyDaysPayAverageTime;
	
	/**
	 * 30日交易量
	 */
	@Column(name="THIRTY_DAYS_AMOUNT")
	private double thirtyDaysAmount;
	
	/**
	 * 买交易量
	 */
	@Column(name="BUY_AMOUNT")
	private double buyAmount;
	
	/**
	 * 卖交易量
	 */
	@Column(name="SELL_AMOUNT")
	private double sellAmount;
	
	/**
	 * 总交易量
	 */
	@Column(name="TOTAL_AMOUNT")
	private double totalAmount;
	
	/**
	 * 账号创建天数
	 */
	@Column(name="ACCOUNT_CREATE_DAYS")
	private int accountCreateDays;
	
	/**
	 * 首次交易至今天数
	 */
	@Column(name="FIRST_EXCHANGE_DAYS")
	private int firstExchangeDays;
	
	/**
	 * 交易人数
	 */
	@Column(name="EXCHANGE_USERS")
	private int exchangeUsers;
	
	/**
	 * 买成单数
	 */
	@Column(name="BUY_SUCCESS_ORDERS")
	private int buySuccessOrders;
	
	/**
	 * 卖成单数
	 */
	@Column(name="SELL_SUCCESS_ORDERS")
	private int sellSuccessOrders;
	
	/**
	 * 总成单数
	 */
	@Column(name="TOTAL_SUCCESS_ORDERS")
	private int totalSuccessOrders;
	
	/**
	 * 好评数
	 */
	@Column(name="APPRAISE_GOOD")
	private int appraiseGood;
	
	/**
	 * 差评数
	 */
	@Column(name="APPRAISE_BAD")
	private int appraiseBad;
	
	/**
	 * 订单邮件通知：0关闭/1打开
	 */
	@Column(name="ORDER_MAIL_NOTICE_OPEN")
	private int orderMailNoticeOpen;
	
	/**
	 * 订单短信通知：0关闭/1打开
	 */
	@Column(name="ORDER_SMS_NOTICE_OPEN")
	private int orderSmsNoticeOpen;
	
	/**
	 * 订单APP通知：0关闭/1打开
	 */
	@Column(name="ORDER_APP_NOTICE_OPEN")
	private int orderAppNoticeOpen;
	
	/**
	 * 申诉邮件通知：0关闭/1打开
	 */
	@Column(name="APPEAL_MAIL_NOTICE_OPEN")
	private int appealMailNoticeOpen;
	
	/**
	 * 申诉短信通知：0关闭/1打开
	 */
	@Column(name="APPEAL_SMS_NOTICE_OPEN")
	private int appealSmsNoticeOpen;
	
	/**
	 * 申诉APP通知：0关闭/1打开
	 */
	@Column(name="APPEAL_APP_NOTICE_OPEN")
	private int appealAppNoticeOpen;
	
	/**
	 * 聊天APP通知：0关闭/1打开
	 */
	@Column(name="CHAT_APP_NOTICE_OPEN")
	private int chatAppNoticeOpen;
	
	/**
	 * 安全邮件通知：0关闭/1打开
	 */
	@Column(name="SECURITY_MAIL_NOTICE_OPEN")
	private int securityMailNoticeOpen;
	
	/**
	 * 安全短信通知：0关闭/1打开
	 */
	@Column(name="SECURITY_SMS_NOTICE_OPEN")
	private int securitySmsNoticeOpen;
	
	/**
	 * 安全APP通知：0关闭/1打开
	 */
	@Column(name="SECURITY_APP_NOTICE_OPEN")
	private int securityAppNoticeOpen;
	
	/**
	 * 备注
	 */
	@Column(name="REMARK")
	private String remark;
	
	/**
	 * 创建时间
	 */
	@Column(name="CREATE_TIME")
	private Date createTime;

	/**
	 * 更新时间
	 */
	@Column(name="UPDATE_TIME")
	private Date updateTime;

	public String getC2cManagerPartyId() {
		return c2cManagerPartyId;
	}

	public void setC2cManagerPartyId(String c2cManagerPartyId) {
		this.c2cManagerPartyId = c2cManagerPartyId;
	}

	public int getC2cUserType() {
		return c2cUserType;
	}

	public void setC2cUserType(int c2cUserType) {
		this.c2cUserType = c2cUserType;
	}

	public String getC2cUserCode() {
		return c2cUserCode;
	}

	public void setC2cUserCode(String c2cUserCode) {
		this.c2cUserCode = c2cUserCode;
	}

	public String getC2cUserPartyId() {
		return c2cUserPartyId;
	}

	public void setC2cUserPartyId(String c2cUserPartyId) {
		this.c2cUserPartyId = c2cUserPartyId;
	}

	public String getNickName() {
		return nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

	public String getHeadImg() {
		return headImg;
	}

	public void setHeadImg(String headImg) {
		this.headImg = headImg;
	}

	public double getDeposit() {
		return deposit;
	}

	public void setDeposit(double deposit) {
		this.deposit = deposit;
	}

	public double getDepositOpen() {
		return depositOpen;
	}

	public void setDepositOpen(double depositOpen) {
		this.depositOpen = depositOpen;
	}

	public double getDepositGiftRate() {
		return depositGiftRate;
	}

	public void setDepositGiftRate(double depositGiftRate) {
		this.depositGiftRate = depositGiftRate;
	}

	public int getThirtyDaysOrder() {
		return thirtyDaysOrder;
	}

	public void setThirtyDaysOrder(int thirtyDaysOrder) {
		this.thirtyDaysOrder = thirtyDaysOrder;
	}

	public double getThirtyDaysOrderRatio() {
		return thirtyDaysOrderRatio;
	}

	public void setThirtyDaysOrderRatio(double thirtyDaysOrderRatio) {
		this.thirtyDaysOrderRatio = thirtyDaysOrderRatio;
	}

	public int getThirtyDaysPassAverageTime() {
		return thirtyDaysPassAverageTime;
	}

	public void setThirtyDaysPassAverageTime(int thirtyDaysPassAverageTime) {
		this.thirtyDaysPassAverageTime = thirtyDaysPassAverageTime;
	}

	public int getThirtyDaysPayAverageTime() {
		return thirtyDaysPayAverageTime;
	}

	public void setThirtyDaysPayAverageTime(int thirtyDaysPayAverageTime) {
		this.thirtyDaysPayAverageTime = thirtyDaysPayAverageTime;
	}

	public double getThirtyDaysAmount() {
		return thirtyDaysAmount;
	}

	public void setThirtyDaysAmount(double thirtyDaysAmount) {
		this.thirtyDaysAmount = thirtyDaysAmount;
	}

	public double getBuyAmount() {
		return buyAmount;
	}

	public void setBuyAmount(double buyAmount) {
		this.buyAmount = buyAmount;
	}

	public double getSellAmount() {
		return sellAmount;
	}

	public void setSellAmount(double sellAmount) {
		this.sellAmount = sellAmount;
	}

	public double getTotalAmount() {
		return totalAmount;
	}

	public void setTotalAmount(double totalAmount) {
		this.totalAmount = totalAmount;
	}

	public int getAccountCreateDays() {
		return accountCreateDays;
	}

	public void setAccountCreateDays(int accountCreateDays) {
		this.accountCreateDays = accountCreateDays;
	}

	public int getFirstExchangeDays() {
		return firstExchangeDays;
	}

	public void setFirstExchangeDays(int firstExchangeDays) {
		this.firstExchangeDays = firstExchangeDays;
	}

	public int getExchangeUsers() {
		return exchangeUsers;
	}

	public void setExchangeUsers(int exchangeUsers) {
		this.exchangeUsers = exchangeUsers;
	}

	public int getBuySuccessOrders() {
		return buySuccessOrders;
	}

	public void setBuySuccessOrders(int buySuccessOrders) {
		this.buySuccessOrders = buySuccessOrders;
	}

	public int getSellSuccessOrders() {
		return sellSuccessOrders;
	}

	public void setSellSuccessOrders(int sellSuccessOrders) {
		this.sellSuccessOrders = sellSuccessOrders;
	}

	public int getTotalSuccessOrders() {
		return totalSuccessOrders;
	}

	public void setTotalSuccessOrders(int totalSuccessOrders) {
		this.totalSuccessOrders = totalSuccessOrders;
	}

	public int getAppraiseGood() {
		return appraiseGood;
	}

	public void setAppraiseGood(int appraiseGood) {
		this.appraiseGood = appraiseGood;
	}

	public int getAppraiseBad() {
		return appraiseBad;
	}

	public void setAppraiseBad(int appraiseBad) {
		this.appraiseBad = appraiseBad;
	}

	public int getOrderMailNoticeOpen() {
		return orderMailNoticeOpen;
	}

	public void setOrderMailNoticeOpen(int orderMailNoticeOpen) {
		this.orderMailNoticeOpen = orderMailNoticeOpen;
	}

	public int getOrderSmsNoticeOpen() {
		return orderSmsNoticeOpen;
	}

	public void setOrderSmsNoticeOpen(int orderSmsNoticeOpen) {
		this.orderSmsNoticeOpen = orderSmsNoticeOpen;
	}

	public int getOrderAppNoticeOpen() {
		return orderAppNoticeOpen;
	}

	public void setOrderAppNoticeOpen(int orderAppNoticeOpen) {
		this.orderAppNoticeOpen = orderAppNoticeOpen;
	}

	public int getAppealMailNoticeOpen() {
		return appealMailNoticeOpen;
	}

	public void setAppealMailNoticeOpen(int appealMailNoticeOpen) {
		this.appealMailNoticeOpen = appealMailNoticeOpen;
	}

	public int getAppealSmsNoticeOpen() {
		return appealSmsNoticeOpen;
	}

	public void setAppealSmsNoticeOpen(int appealSmsNoticeOpen) {
		this.appealSmsNoticeOpen = appealSmsNoticeOpen;
	}

	public int getAppealAppNoticeOpen() {
		return appealAppNoticeOpen;
	}

	public void setAppealAppNoticeOpen(int appealAppNoticeOpen) {
		this.appealAppNoticeOpen = appealAppNoticeOpen;
	}

	public int getChatAppNoticeOpen() {
		return chatAppNoticeOpen;
	}

	public void setChatAppNoticeOpen(int chatAppNoticeOpen) {
		this.chatAppNoticeOpen = chatAppNoticeOpen;
	}

	public int getSecurityMailNoticeOpen() {
		return securityMailNoticeOpen;
	}

	public void setSecurityMailNoticeOpen(int securityMailNoticeOpen) {
		this.securityMailNoticeOpen = securityMailNoticeOpen;
	}

	public int getSecuritySmsNoticeOpen() {
		return securitySmsNoticeOpen;
	}

	public void setSecuritySmsNoticeOpen(int securitySmsNoticeOpen) {
		this.securitySmsNoticeOpen = securitySmsNoticeOpen;
	}

	public int getSecurityAppNoticeOpen() {
		return securityAppNoticeOpen;
	}

	public void setSecurityAppNoticeOpen(int securityAppNoticeOpen) {
		this.securityAppNoticeOpen = securityAppNoticeOpen;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}
	
}
