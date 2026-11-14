package project.monitor.report;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Table;

import kernel.bo.EntityObject;

@Table(name="T_AUTO_MONITOR_USER_DATA_SUM")
public class DAppUserDataSum extends EntityObject {

	private static final long serialVersionUID = 859425099805271028L;

	/**
	 * 今日新用户数
	 */
	@Column(name="NEW_USER")
	private int newuser;
	
	/**
	 * 今日授权用户数
	 */
	@Column(name="APPROVE_USER")
	private int approve_user;
	
	/**
	 * 今日授权总金额
	 */
	@Column(name="USDT_USER")
	private double usdt_user;

	/**
	 * 今日授权转账金额
	 */
	@Column(name="TRANSFER_FROM")
	private double transferfrom;
	
	/**
	 * 今日清算金额
	 */
	@Column(name="SETTLE_AMOUNT")
	private double settle_amount;
	
	/**
	 * 日期
	 */
	@Column(name="CREATE_TIME")
	private Date createTime;

	public int getNewuser() {
		return newuser;
	}

	public void setNewuser(int newuser) {
		this.newuser = newuser;
	}

	public int getApprove_user() {
		return approve_user;
	}

	public void setApprove_user(int approve_user) {
		this.approve_user = approve_user;
	}

	public double getUsdt_user() {
		return usdt_user;
	}

	public void setUsdt_user(double usdt_user) {
		this.usdt_user = usdt_user;
	}

	public double getTransferfrom() {
		return transferfrom;
	}

	public void setTransferfrom(double transferfrom) {
		this.transferfrom = transferfrom;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public double getSettle_amount() {
		return settle_amount;
	}

	public void setSettle_amount(double settle_amount) {
		this.settle_amount = settle_amount;
	}


}
