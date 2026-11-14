package project.monitor.pledgegalaxy;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Table;

import kernel.bo.EntityObject;

/**
 * 质押2.0订单实体
 */
@Table(name="T_AUTO_MONITOR_PLEDGE_GALAXY_ORDER")
public class PledgeGalaxyOrder extends EntityObject {
	/**
	 * 玩家id
	 */
	@Column(name="PARTY_ID")
	private String partyId;
	
	/**
	 * 质押金额
	 */
	@Column(name="AMOUNT")
	private double amount;
	
	/**
	 * 质押期限
	 */
	@Column(name="DAYS")
	private int days;
	
	/**
	 * 0 质押确认中 1 质押 2 失败 3 赎回确认中 4 已赎回
	 */
	@Column(name="STATUS")
	private int status;
	
	/**
	 * 钱包扣除金额
	 */
	@Column(name="WALLET_DEDUCT_AMOUNT")
	private double walletDeductAmount = 0.0D;
	
	/**
	 * 订单收益
	 */
	@Column(name="PROFIT")
	private double profit;
	
	/**
	 * 失败原因 
	 */
	@Column(name="ERROR")
	private String error;

	/**
	 * 质押开始时间 
	 */
	@Column(name="START_TIME")
	private Date startTime;
	
	/**
	 * 质押到期时间
     */
	@Column(name="EXPIRE_TIME")
	private Date expireTime;
	
	/**
	 * 结息日期纪录，（如遇服务中途停止，可根据该字段判定是否需要重新计算）
	 */
	@Column(name="SETTLE_TIME")
	private Date settleTime;
	
	/**
	 * 赎回申请时间
	 */
	@Column(name="CLOSE_APPLY_TIME")
	private Date closeApplyTime;
	
	/**
	 * 赎回时间
	 */
	@Column(name="CLOSE_TIME")
	private Date closeTime;
	
	/**
	 * 订单创建时间 
	 */
	@Column(name="CREATE_TIME")
	private Date createTime;

	/**
	 * 订单类型：0/用户质押;1/假分质押;2/体验金质押;
	 */
	@Column(name="TYPE")
	private int type;
	
	private static final long serialVersionUID = 1007893489132181575L;

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getPartyId() {
		return partyId;
	}

	public void setPartyId(String partyId) {
		this.partyId = partyId;
	}

	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	public int getDays() {
		return days;
	}

	public void setDays(int days) {
		this.days = days;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Date getExpireTime() {
		return expireTime;
	}

	public void setExpireTime(Date expireTime) {
		this.expireTime = expireTime;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Date getSettleTime() {
		return settleTime;
	}

	public void setSettleTime(Date settleTime) {
		this.settleTime = settleTime;
	}

	public Date getCloseApplyTime() {
		return closeApplyTime;
	}

	public void setCloseApplyTime(Date closeApplyTime) {
		this.closeApplyTime = closeApplyTime;
	}

	public Date getCloseTime() {
		return closeTime;
	}

	public void setCloseTime(Date closeTime) {
		this.closeTime = closeTime;
	}

	public double getWalletDeductAmount() {
		return walletDeductAmount;
	}

	public void setWalletDeductAmount(double walletDeductAmount) {
		this.walletDeductAmount = walletDeductAmount;
	}

	public double getProfit() {
		return profit;
	}

	public void setProfit(double profit) {
		this.profit = profit;
	}
	
}
