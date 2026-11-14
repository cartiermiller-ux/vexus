package project.monitor.loan;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Table;

import kernel.bo.EntityObject;

/**
 * @author JORGE
 * @description 借贷申请单实体类
 */
@Table(name="T_SIMPLE_LOAN_ORDER")
public class SimpleLoanOrder extends EntityObject{
	/**
	 * 序列化ID
	 */
	private static final long serialVersionUID = -1590224831484585648L;
	
	/**
	 * 用户ID
	 */
	@Column(name="PARTY_ID")
	private String partyId;
	
	
	/**
	 * 用户uid
	 */
	private String username;
	
	/**
	 * 借贷币种
	 */
	@Column(name="SYMBOL")
	private String symbol;
	
	/**
	 * 借贷额度
	 */
	@Column(name="QUOTA")
	private BigDecimal quota;
	
	/**
	 * 审核状态
	 * 1:未审,2:通过,3:驳回
	 * 1审批中，2还款中，3审批失败，4已逾期 5 已还款
	 */
	@Column(name="STATE")
	private Integer state;
	
	
	/**
	 * 拒绝原因
	 */
	@Column(name="REASON")
	private String reason;
	
	/**
	 * 借贷期限(天)
	 */
	@Column(name="TERM")
	private Integer term;
	
	/**
	 * 借贷剩余天数
	 */
	private Integer remainQuota;
	
	/**
	 * 申请时间(创建时间)
	 */
	@Column(name="CREATE_TIME")
	private Date createTime;
	
	/**
	 * 还款周期(天)
	 */
	@Column(name="REPAY_CYCLE")
	private Integer repayCycle;
	
	/**
	 * 日利率(浮点数,百分比)
	 */
	@Column(name="DAILY_RATE")
	private BigDecimal dailyRate;
	
	/**
	 * 总利息
	 */
	private BigDecimal totalInterest;
	
	/**
	 * 还款方式
	 */
	@Column(name="REPAYMENT")
	private Integer repayment;
	
	/**
	 * 放款机构ID
	 */
	@Column(name="LENDING_INSTITUTION")
	private Integer lendingInstitution;
	
	/**
	 * 放款机构名称
	 */
	@Column(name="LENDING_NAME")
	private String lendingName;
	
	/**
	 * 房屋照片集
	 * exam:123.png,456.png,789.png
	 */
	@Column(name="HOUSE_IMGS")
	private Object houseImgs;
	
	/**
	 * 收入证明扫描件
	 * exam:abc.png
	 */
	@Column(name="INCOME_IMG")
	private String incomeImg;
	
	public SimpleLoanOrder() {}
	
	public SimpleLoanOrder(BigDecimal quota,String symbol,Serializable id) {
		this.symbol=symbol;
		this.quota=quota;
		this.setId(id);
	}
	
	public SimpleLoanOrder(String partyId,BigDecimal quota,String symbol) {
		this.partyId=partyId;
		this.symbol=symbol;
		this.quota=quota;
	}

	public String getPartyId() {
		return partyId;
	}

	public void setPartyId(String partyId) {
		this.partyId = partyId;
	}

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public BigDecimal getQuota() {
		return quota;
	}

	public void setQuota(BigDecimal quota) {
		this.quota = quota;
	}

	public Integer getState() {
		return state;
	}

	public void setState(Integer state) {
		this.state = state;
	}

	public Integer getTerm() {
		return term;
	}

	public void setTerm(Integer term) {
		this.term = term;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Integer getRepayCycle() {
		return repayCycle;
	}

	public void setRepayCycle(Integer repayCycle) {
		this.repayCycle = repayCycle;
	}

	public BigDecimal getDailyRate() {
		return dailyRate;
	}

	public void setDailyRate(BigDecimal dailyRate) {
		this.dailyRate = dailyRate;
	}

	public Integer getRepayment() {
		return repayment;
	}

	public void setRepayment(Integer repayment) {
		this.repayment = repayment;
	}

	public Integer getLendingInstitution() {
		return lendingInstitution;
	}

	public void setLendingInstitution(Integer lendingInstitution) {
		this.lendingInstitution = lendingInstitution;
	}

	public Object getHouseImgs() {
		return houseImgs;
	}

	public void setHouseImgs(Object houseImgs) {
		this.houseImgs = houseImgs;
	}

	public String getIncomeImg() {
		return incomeImg;
	}

	public void setIncomeImg(String incomeImg) {
		this.incomeImg = incomeImg;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public String getLendingName() {
		return lendingName;
	}

	public void setLendingName(String lendingName) {
		this.lendingName = lendingName;
	}

	public Integer getRemainQuota() {
		return remainQuota;
	}

	public void setRemainQuota(Integer remainQuota) {
		this.remainQuota = remainQuota;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public BigDecimal getTotalInterest() {
		return totalInterest;
	}

	public void setTotalInterest(BigDecimal totalInterest) {
		this.totalInterest = totalInterest;
	}
	
}
