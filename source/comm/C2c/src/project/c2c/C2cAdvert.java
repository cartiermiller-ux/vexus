package project.c2c;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Table;

import kernel.bo.EntityObject;

@Table(name="T_C2C_ADVERT")
public class C2cAdvert extends EntityObject {

	private static final long serialVersionUID = 4911926779531882495L;
	
	/**
	 * 买入
	 */
	public final static String DIRECTION_BUY = "buy";
	
	/**
	 * 卖出
	 */
	public final static String DIRECTION_SELL = "sell";

	/**
	 * 承兑商ID
	 */
	@Column(name="C2C_USER_ID")
	private String c2cUserId;

	/**
	 * 买卖方式：buy买/sell卖
	 */
	@Column(name="DIRECTION")
	private String direction;

	/**
	 * 支付币种
	 */
	@Column(name="CURRENCY")
	private String currency;

	/**
	 * 上架币种
	 */
	@Column(name="SYMBOL")
	private String symbol;

	/**
	 * 上架币种实时行情价
	 */
	@Column(name="SYMBOL_CLOSE")
	private double symbolClose;

	/**
	 * 支付比率
	 */
	@Column(name="PAY_RATE")
	private double payRate;

	/**
	 * 支付方式模板id列表：不同id逗号分隔
	 */
	@Column(name="PAY_TYPE")
	private String payType;

	/**
	 * 支付方式模板名称
	 */
	private String payTypeName;
	
	/**
	 * 币种单价
	 */
	@Column(name="SYMBOL_VALUE")
	private double symbolValue;
	
	/**
	 * 币种数量
	 */
	@Column(name="COIN_AMOUNT")
	private double coinAmount;

	/**
	 * 单笔订单最低限额
	 */
	@Column(name="INVESTMENT_MIN")
	private double investmentMin;
	
	/**
	 * 单笔订单最高限额
	 */
	@Column(name="INVESTMENT_MAX")
	private double investmentMax;

	/**
	 * 剩余派单金额
	 */
	@Column(name="DEPOSIT")
	private double deposit;
	
	/**
	 * 派单金额
	 */
	@Column(name="DEPOSIT_OPEN")
	private double depositOpen;
	
	/**
	 * 是否上架：0下架/1上架
	 */
	@Column(name="ON_SALE")
	private int onSale;
	
	/**
	 * 是否关闭：0否/1是
	 */
	@Column(name="CLOSED")
	private int closed;
	
	/**
	 * 排序索引
	 */
	@Column(name="SORT_INDEX")
	private int sortIndex;
	
	/**
	 * 支付时效（单位：分钟）
	 */
	@Column(name="EXPIRE_TIME")
	private int expireTime;

	/**
	 * 交易条款
	 */
	@Column(name="TRANSACTION_TERMS")
	private String transactionTerms;

	/**
	 * 订单自动消息
	 */
	@Column(name="ORDER_MSG")
	private String orderMsg;

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

	public String getC2cUserId() {
		return c2cUserId;
	}

	public void setC2cUserId(String c2cUserId) {
		this.c2cUserId = c2cUserId;
	}

	public String getDirection() {
		return direction;
	}

	public void setDirection(String direction) {
		this.direction = direction;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public double getSymbolClose() {
		return symbolClose;
	}

	public void setSymbolClose(double symbolClose) {
		this.symbolClose = symbolClose;
	}

	public double getPayRate() {
		return payRate;
	}

	public void setPayRate(double payRate) {
		this.payRate = payRate;
	}

	public String getPayType() {
		return payType;
	}

	public void setPayType(String payType) {
		this.payType = payType;
	}

	public String getPayTypeName() {
		return payTypeName;
	}

	public void setPayTypeName(String payTypeName) {
		this.payTypeName = payTypeName;
	}

	public double getSymbolValue() {
		return symbolValue;
	}

	public void setSymbolValue(double symbolValue) {
		this.symbolValue = symbolValue;
	}

	public double getCoinAmount() {
		return coinAmount;
	}

	public void setCoinAmount(double coinAmount) {
		this.coinAmount = coinAmount;
	}

	public double getInvestmentMin() {
		return investmentMin;
	}

	public void setInvestmentMin(double investmentMin) {
		this.investmentMin = investmentMin;
	}

	public double getInvestmentMax() {
		return investmentMax;
	}

	public void setInvestmentMax(double investmentMax) {
		this.investmentMax = investmentMax;
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

	public int getOnSale() {
		return onSale;
	}

	public void setOnSale(int onSale) {
		this.onSale = onSale;
	}

	public int getClosed() {
		return closed;
	}

	public void setClosed(int closed) {
		this.closed = closed;
	}

	public int getSortIndex() {
		return sortIndex;
	}

	public void setSortIndex(int sortIndex) {
		this.sortIndex = sortIndex;
	}

	public int getExpireTime() {
		return expireTime;
	}

	public void setExpireTime(int expireTime) {
		this.expireTime = expireTime;
	}

	public String getTransactionTerms() {
		return transactionTerms;
	}

	public void setTransactionTerms(String transactionTerms) {
		this.transactionTerms = transactionTerms;
	}

	public String getOrderMsg() {
		return orderMsg;
	}

	public void setOrderMsg(String orderMsg) {
		this.orderMsg = orderMsg;
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
