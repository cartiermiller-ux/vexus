package project.wallet.consumer;

import java.io.Serializable;

public class WalletMessage implements Serializable {
	private static final long serialVersionUID = 2239789461218349202L;

	/**
	 * 无参构造函数
	 */
	public WalletMessage() {
	}


	/**
	 * 构造函数
	 * 
	 * @param partyId
	 * @param money
	 */
	public WalletMessage(Serializable partyId, double money) {
		this.partyId = partyId;
		this.money = money;
	}
	public WalletMessage(Serializable partyId, double money, double lockMoney) {
		this.partyId = partyId;
		this.money = money;
		this.lockMoney = lockMoney;
	}
	public WalletMessage(Serializable partyId, double money, double lockMoney, double freezeMoney) {
		this.partyId = partyId;
		this.money = money;
		this.lockMoney = lockMoney;
		this.freezeMoney = freezeMoney;
	}

	private Serializable partyId;
	/**
	 * 现金
	 */
	private double money = 0.0D;
	
	private double lockMoney = 0.0D;
	/**
	 * 冻结数量
	 */
	private double freezeMoney = 0.0D;
	
	public Serializable getPartyId() {
		return partyId;
	}

	public void setPartyId(Serializable partyId) {
		this.partyId = partyId;
	}

	public double getMoney() {
		return money;
	}

	public void setMoney(double money) {
		this.money = money;
	}

	public double getLockMoney() {
		return lockMoney;
	}

	public void setLockMoney(double lockMoney) {
		this.lockMoney = lockMoney;
	}

	public double getFreezeMoney() {
		return freezeMoney;
	}

	public void setFreezeMoney(double freezeMoney) {
		this.freezeMoney = freezeMoney;
	}

}
