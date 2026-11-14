package project.wallet;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Table;

import kernel.bo.EntityObject;

/**
 * 钱包
 */
@Table(name="T_WALLET")
public class Wallet extends EntityObject {

	private static final long serialVersionUID = 7522745589282180818L;

	@Column(name="PARTY_ID")
	private Serializable partyId;
	/**
	 * 现金
	 */
	@Column(name="MONEY")
	private double money = 0.0D;
	
	/**
	 * 锁定金额
	 */
	@Column(name="LOCK_MONEY")
	private double lock_money = 0.0D;
	
	/**
	 * 冻结金额
	 */
	@Column(name="FREEZE_MONEY")
	private double freeze_money = 0.0d;
	
	public double getLock_money() {
		return lock_money;
	}

	public void setLock_money(double lock_money) {
		this.lock_money = lock_money;
	}

	public double getFreeze_money() {
		return freeze_money;
	}

	public void setFreeze_money(double freeze_money) {
		this.freeze_money = freeze_money;
	}

	public Serializable getPartyId() {
		return this.partyId;
	}

	public void setPartyId(Serializable partyId) {
		this.partyId = partyId;
	}

	public double getMoney() {
		return this.money;
	}

	public void setMoney(double money) {
		this.money = money;
	}
}
