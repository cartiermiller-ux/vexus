package project.loan;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * 借币订单实体
 */
@Table(name="T_LOAN_RELATION_ORDER")
public class LoanRelationOrder implements Serializable {

	private static final long serialVersionUID = -1309085104344395345L;

	/**
	 * 主键
	 */
	@Id
	@Column(name="UUID")
	private String id;
	
	/**
	 * 借款关联订单ID
	 */
	@Column(name="LOAN_RELATION_ORDER_ID")
	private String loanRelationOrderId;
	
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
	 * 质押类型:1币质押
	 */
	@Column(name="PLEDGE_TYPE")
	private int pledgeType;
	
	/**
	 * 创建时间
	 */
	@Column(name="CREATE_TIME")
	private Date createTime;
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getLoanRelationOrderId() {
		return loanRelationOrderId;
	}

	public void setLoanRelationOrderId(String loanRelationOrderId) {
		this.loanRelationOrderId = loanRelationOrderId;
	}

	public int getPledgeType() {
		return pledgeType;
	}

	public void setPledgeType(int pledgeType) {
		this.pledgeType = pledgeType;
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

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	
}
