package project.wallet;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Table;

import kernel.bo.EntityObject;

/**
 * 钱包扩展
 *
 */
@Table(name="T_WALLET_EXTEND")
public class WalletExtend extends EntityObject {
	private static final long serialVersionUID = -926374250240199976L;
	
	@Column(name="PARTY_ID")
	private Serializable partyId;
	/**
	 * 币种，见Constants定义
	 */
	@Column(name="WALLETTYPE")
	private String wallettype;
	/**
	 * 金额
	 */
	@Column(name="AMOUNT")
	private double amount = 0.0D;
	
	/**
	 * 锁定金额
	 */
	@Column(name="LOCK_AMOUNT")
	private double lock_amount = 0.0D;
	
	/**
	 * 冻结金额
	 */
	@Column(name="FROZEN_AMOUNT")
	private double freeze_amount = 0.0D;
	
	public double getLock_amount() {
		return lock_amount;
	}

	public void setLock_amount(double lock_amount) {
		this.lock_amount = lock_amount;
	}

	public double getFreeze_amount() {
		return freeze_amount;
	}

	public void setFreeze_amount(double freeze_amount) {
		this.freeze_amount = freeze_amount;
	}

	/**
	 * 冻结金额
	 */
	private double frozenAmount;

	private String name;

	public Serializable getPartyId() {
		return this.partyId;
	}

	public void setPartyId(Serializable partyId) {
		this.partyId = partyId;
	}

	public String getWallettype() {
		return this.wallettype;
	}

	public void setWallettype(String wallettype) {
		this.wallettype = wallettype;
	}

	public double getAmount() {
		return this.amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	public double getFrozenAmount() {
		return frozenAmount;
	}

	public void setFrozenAmount(double frozenAmount) {
		this.frozenAmount = frozenAmount;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
