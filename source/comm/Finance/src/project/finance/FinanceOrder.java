package project.finance;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Table;

import kernel.bo.EntityObject;

@Table(name="T_FINANCE_ORDER")
public class FinanceOrder extends EntityObject {

	private static final long serialVersionUID = -726057340004619294L;
	
	/**
	 * 用户ID
	 */
	@Column(name="PARTY_ID")
	private Serializable partyId;
	
	/**
	 * 订单 号
	 */
	@Column(name="ORDER_NO")
	private String order_no;
	
	/**
	 * 理财产品名称
	 */
	private String financeName;
	
	/**
	 * 理财产品名称繁体
	 */
	private String financeName_cn;
	
	/**
	 * 理财产品名称英文
	 */
	private String financeName_en;

	/**
	 * 理财产品Id
	 */
	@Column(name="FINANCE_ID")
	private String financeId;

	/**
	 * 金额
	 */
	@Column(name="AMOUNT")
	private double amount;

	/**
	 * 买入时间
	 */
	@Column(name="CREATE_TIME")
	private Date create_time;
	
	/**
	 * 起息时间 从买入时间第二天开始算
	 */
	@Column(name="EARN_TIME")
	private Date earn_time;

	/**
	 * 截止时间
	 */
	@Column(name="STOP_TIME")
	private Date stop_time;

	/**
	 * 赎回时间=截止时间+1天
	 */
	@Column(name="CLOSE_TIME")
	private Date close_time;

	/**
	 * 收益
	 */
	@Column(name="PROFIT")
	private double profit;
	
	/**
	 * 之前或累计收益
	 */
	@Column(name="PROFIT_BEFORE")
	private double profit_before;

	/**
	 * 状态。0.正常赎回， 1 托管中 ,2提前赎回 (违约)3.取消
	 */
	@Column(name="STATE")
	private String state = "1";
	
	/**
	 * 托管时间，周期
	 */
	@Column(name="CYCLE")
	private int cycle;

	/**
	 * 理财产品图片
	 */
	private String img;
	
	/**
	 * 剩余天数
	 */
	private int days;
	
	/**
	 * 赎回时间=截止时间+1天
	 */
	private String close_timeStr;
	
	/**
	 * 买入时间
	 */
	private String create_timeStr;
	/**
	 * 起息时间 从买入时间第二天开始算
	 */
	private String earn_timeStr;

	/**
	 * 截止时间
	 */
	private String stop_timeStr;

	/**
	 * 日利率(%)
	 */
	private String daily_rate;
	/**
	 * 预计收益
	 */
	private double profit_may;
	
	/**
	 * 理财购买币种
	 */
	private String buyCurrency = "usdt";
	
	/**
	 * 理财购买币种
	 */
	private String outputCurrency = "usdt";

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

	public String getFinanceId() {
		return financeId;
	}

	public void setFinanceId(String financeId) {
		this.financeId = financeId;
	}

	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	public Date getCreate_time() {
		return create_time;
	}

	public void setCreate_time(Date create_time) {
		this.create_time = create_time;
	}

	public Date getClose_time() {
		return close_time;
	}

	public void setClose_time(Date close_time) {
		this.close_time = close_time;
	}

	public double getProfit() {
		return profit;
	}

	public void setProfit(double profit) {
		this.profit = profit;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public void setImg(String img) {
		this.img = img;
	}

	public Date getEarn_time() {
		return earn_time;
	}

	public void setEarn_time(Date earn_time) {
		this.earn_time = earn_time;
	}

	public Date getStop_time() {
		return stop_time;
	}

	public void setStop_time(Date stop_time) {
		this.stop_time = stop_time;
	}

	public int getCycle() {
		return cycle;
	}

	public void setCycle(int cycle) {
		this.cycle = cycle;
	}

	public int getDays() {
		return days;
	}

	public void setDays(int days) {
		this.days = days;
	}

	public String getImg() {
		return img;
	}

	public String getClose_timeStr() {
		return close_timeStr;
	}

	public void setClose_timeStr(String close_timeStr) {
		this.close_timeStr = close_timeStr;
	}

	

	public String getCreate_timeStr() {
		return create_timeStr;
	}

	public void setCreate_timeStr(String create_timeStr) {
		this.create_timeStr = create_timeStr;
	}

	public String getEarn_timeStr() {
		return earn_timeStr;
	}

	public void setEarn_timeStr(String earn_timeStr) {
		this.earn_timeStr = earn_timeStr;
	}

	public String getStop_timeStr() {
		return stop_timeStr;
	}

	public void setStop_timeStr(String stop_timeStr) {
		this.stop_timeStr = stop_timeStr;
	}

	public double getProfit_may() {
		return profit_may;
	}

	public void setProfit_may(double profit_may) {
		this.profit_may = profit_may;
	}

	public String getDaily_rate() {
		return daily_rate;
	}

	public void setDaily_rate(String daily_rate) {
		this.daily_rate = daily_rate;
	}

	public double getProfit_before() {
		return profit_before;
	}

	public void setProfit_before(double profit_before) {
		this.profit_before = profit_before;
	}

	public String getFinanceName() {
		return financeName;
	}

	public void setFinanceName(String financeName) {
		this.financeName = financeName;
	}

	public String getFinanceName_cn() {
		return financeName_cn;
	}

	public void setFinanceName_cn(String financeName_cn) {
		this.financeName_cn = financeName_cn;
	}

	public String getFinanceName_en() {
		return financeName_en;
	}

	public void setFinanceName_en(String financeName_en) {
		this.financeName_en = financeName_en;
	}

	public String getBuyCurrency() {
		return buyCurrency;
	}

	public void setBuyCurrency(String buyCurrency) {
		this.buyCurrency = buyCurrency;
	}

	public String getOutputCurrency() {
		return outputCurrency;
	}

	public void setOutputCurrency(String outputCurrency) {
		this.outputCurrency = outputCurrency;
	}
	
}
