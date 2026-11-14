package project.loan;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Table;

/**
 * 借币订单实体
 */
@Table(name="T_LOAN_ORDER")
public class LoanOrder implements Serializable {
	/**
	 * 序列号
	 */
	private static final long serialVersionUID = -8634950101772123796L;
	
	/**
	 * 主键
	 */
	@Column(name="UUID")
	private String id;

	/**
	 * 订单号
	 */
	@Column(name="ORDER_NO")
	private String orderNo;
	
	/**
	 * 用户ID
	 */
	@Column(name="PARTY_ID")
	private String partyId;
	
	/**
	 * 订单类型: 1 借款;2新增质押;3续借;4还款;5强平结清
	 */
	@Column(name="ORDER_TYPE")
	private int orderType;
	
	/**
	 * 借款金额
	 */
	@Column(name="LOAN_AMOUNT")
	private double loanAmount;
	
	/**
	 * 订单状态: 1计息中;2已结清;3强平结清;4已逾期
	 */
	@Column(name="STATE")
	private int state;
	
	/**
	 * 贷款币种
	 */
	@Column(name="LOAN_CURRENCY")
	private String loanCurrency;
	
	/**
	 * 质押的币种
	 */
	@Column(name="PLEDGE_CURRENCY")
	private String pledgeCurrency;
	
	/**
	 * 质押金额
	 */
	@Column(name="PLEDGE_AMOUNT")
	private double pledgeAmount;
	
	/**
	 * 质押率
	 */
	@Column(name="PLEDGE_RATE")
	private double pledgeRate;
	
	/**
	 * 质押类型:1币质押
	 */
	@Column(name="PLEDGE_TYPE")
	private int pledgeType;
	
	/**
	 * 总负债
	 */
	@Column(name="DEBT_AMOUNT")
	private double debtAmount;
	
	/**
	 * 总利息
	 */
	@Column(name="INTEREST_AMOUNT")
	private double interestAmount;
	
	/**
	 * 总逾期
	 */
	@Column(name="OVERDUE_AMOUNT")
	private double overdueAmount;
	
	/**
	 * 逾期时利率
	 */
	@Column(name="OVERDUE_RATE")
	private double overdueRate;
	
	/**
	 * 时利率
	 */
	@Column(name="HOURLY_RATE")
	private double hourlyRate;
	
	/**
	 * 借款周期
	 */
	@Column(name="LOAN_CYCLE")
	private int loanCycle;
	
	/**
	 * 借款时间
	 */
	@Column(name="CREATE_TIME")
	private Date createTime;
	
	/**
	 * 到期时间
	 */
	@Column(name="EXPIRE_TIME")
	private Date expireTime;
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getOrderNo() {
		return orderNo;
	}

	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo;
	}

	public String getPartyId() {
		return partyId;
	}

	public void setPartyId(String partyId) {
		this.partyId = partyId;
	}

	public int getOrderType() {
		return orderType;
	}

	public void setOrderType(int orderType) {
		this.orderType = orderType;
	}

	public double getLoanAmount() {
		return loanAmount;
	}

	public void setLoanAmount(double loanAmount) {
		this.loanAmount = loanAmount;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public String getLoanCurrency() {
		return loanCurrency;
	}

	public void setLoanCurrency(String loanCurrency) {
		this.loanCurrency = loanCurrency;
	}
	
	public String getPledgeCurrency() {
		return pledgeCurrency;
	}

	public void setPledgeCurrency(String pledgeCurrency) {
		this.pledgeCurrency = pledgeCurrency;
	}

	public double getPledgeAmount() {
		return pledgeAmount;
	}

	public void setPledgeAmount(double pledgeAmount) {
		this.pledgeAmount = pledgeAmount;
	}

	public double getPledgeRate() {
		return pledgeRate;
	}

	public void setPledgeRate(double pledgeRate) {
		this.pledgeRate = pledgeRate;
	}

	public double getDebtAmount() {
		return debtAmount;
	}

	public void setDebtAmount(double debtAmount) {
		this.debtAmount = debtAmount;
	}

	public double getInterestAmount() {
		return interestAmount;
	}

	public void setInterestAmount(double interestAmount) {
		this.interestAmount = interestAmount;
	}

	public double getOverdueAmount() {
		return overdueAmount;
	}

	public void setOverdueAmount(double overdueAmount) {
		this.overdueAmount = overdueAmount;
	}

	public double getOverdueRate() {
		return overdueRate;
	}

	public void setOverdueRate(double overdueRate) {
		this.overdueRate = overdueRate;
	}

	public double getHourlyRate() {
		return hourlyRate;
	}

	public void setHourlyRate(double hourlyRate) {
		this.hourlyRate = hourlyRate;
	}

	public int getLoanCycle() {
		return loanCycle;
	}

	public void setLoanCycle(int loanCycle) {
		this.loanCycle = loanCycle;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Date getExpireTime() {
		return expireTime;
	}

	public void setExpireTime(Date expireTime) {
		this.expireTime = expireTime;
	}

	public int getPledgeType() {
		return pledgeType;
	}

	public void setPledgeType(int pledgeType) {
		this.pledgeType = pledgeType;
	}
	
}
