package project.user;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Table;

import kernel.bo.EntityObject;

@Table(name="T_USERDATASUM")
public class UserDataSum extends EntityObject {

	private static final long serialVersionUID = 1256269500666828481L;

	@Column(name="PARTY_ID")
	private Serializable partyId;
	
	/**
	 * 推荐人推荐总数。 用户是4级，代理则是伞下所有推荐用户
	 */
	@Column(name="RECO_NUM")
	private int reco_num;
	
	/**
	 * 伞下用户充值总额（目前是4级）
	 */
	@Column(name="RECHARGE_SUM")
	private double recharge_sum;

	public Serializable getPartyId() {
		return partyId;
	}

	public void setPartyId(Serializable partyId) {
		this.partyId = partyId;
	}

	public int getReco_num() {
		return reco_num;
	}

	public void setReco_num(int reco_num) {
		this.reco_num = reco_num;
	}

	public double getRecharge_sum() {
		return recharge_sum;
	}

	public void setRecharge_sum(double recharge_sum) {
		this.recharge_sum = recharge_sum;
	}
}
