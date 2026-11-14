package project.monitor.loan;

import javax.persistence.Column;
import javax.persistence.Table;

import kernel.bo.EntityObject;

/**
 * @author JORGE
 * @description 借贷参数实体类
 * 每个实例代表一个配置项,配置项中每个复合字段值使用英文逗号分割值
 */
@Table(name="T_LOAN_PARAM")
public class LoanParam extends EntityObject{
	/**
	 * 序列化ID
	 */
	private static final long serialVersionUID = -1590224831484585648L;
	
	/**
	 * 借贷期限(天)
	 * exam:1,3,7,30,90,180
	 */
	@Column(name="TERM")
	private String term;
	
	/**
	 * 借贷最大额度
	 * exam:USDT:1000,ETH:800,BTC:300
	 */
	@Column(name="MAX_QUOTA")
	private String maxQuota;
	
	/**
	 * 借贷最小额度
	 * exam:USDT:100,ETH:80,BTC:30
	 */
	@Column(name="MIN_QUOTA")
	private String minQuota;
	
	/**
	 * 还款周期(天)
	 * exam:3,7,30,60,90,180,360
	 */
	@Column(name="REPAY_CYCLE")
	private String repayCycle;
	
	/**
	 * 日利率(浮点数,百分比)
	 * exam:0.0003,0.0006,0.001
	 */
	@Column(name="DAILY_RATE")
	private String dailyRate;
	
	/**
	 * 还款方式
	 * exam:1:到期还本息,2:到期还本金,3:到期还利息
	 */
	@Column(name="REPAYMENT")
	private String repayment;
	
	/**
	 * 放款机构ID
	 */
	@Column(name="LENDING_INSTITUTION")
	private String lendingInstitution;
	
	/**
	 * 放款机构名称
	 */
	@Column(name="LENDING_NAME")
	private String lendingName;

	public String getTerm() {
		return term;
	}

	public void setTerm(String term) {
		this.term = term;
	}

	public String getMaxQuota() {
		return maxQuota;
	}

	public void setMaxQuota(String maxQuota) {
		this.maxQuota = maxQuota;
	}

	public String getMinQuota() {
		return minQuota;
	}

	public void setMinQuota(String minQuota) {
		this.minQuota = minQuota;
	}

	public String getRepayCycle() {
		return repayCycle;
	}

	public void setRepayCycle(String repayCycle) {
		this.repayCycle = repayCycle;
	}

	public String getDailyRate() {
		return dailyRate;
	}

	public void setDailyRate(String dailyRate) {
		this.dailyRate = dailyRate;
	}

	public String getRepayment() {
		return repayment;
	}

	public void setRepayment(String repayment) {
		this.repayment = repayment;
	}

	public String getLendingInstitution() {
		return lendingInstitution;
	}

	public void setLendingInstitution(String lendingInstitution) {
		this.lendingInstitution = lendingInstitution;
	}

	public String getLendingName() {
		return lendingName;
	}

	public void setLendingName(String lendingName) {
		this.lendingName = lendingName;
	}
	
	
}
