package project.wallet.consumer;

import java.io.Serializable;

public class WalletExtendMessage implements Serializable {

	private static final long serialVersionUID = 2239789461218349202L;

	/**
	 * 无参构造函数
	 */
	public WalletExtendMessage() {
	}

	/**
	 * 构造函数
	 */
	public WalletExtendMessage(Serializable partyId, String walletType, double volumn) {
		this.partyId = partyId;
		this.volumn = volumn;
		this.walletType = walletType;
	}
	
	/**
	 * 构造函数
	 */
	public WalletExtendMessage(Serializable partyId, String walletType, double volumn, double frozenAmount) {
		this.partyId = partyId;
		this.walletType = walletType;
		this.volumn = volumn;
		this.frozenAmount = frozenAmount;
	}

	public WalletExtendMessage(Serializable partyId, String walletType, double volumn, double lockVolumn, double freezeVolumn) {
		this.partyId = partyId;
		this.volumn = volumn;
		this.walletType = walletType;
		this.freezeVolumn = freezeVolumn;
		this.lockVolumn = lockVolumn;
	}
	
	private Serializable partyId;
	
	/**
	 * 数量
	 */
	private double volumn = 0.0D;
	/**
	 * 锁定数量
	 */
	private double lockVolumn = 0.0D;
	/**
	 * 冻结数量
	 */
	private double freezeVolumn = 0.0D;
	
	/**
	 * 冻结金额
	 */
	private double frozenAmount = 0.0D;

	/**
	 * 币种类型
	 */
	private String walletType;
	

	public double getLockVolumn() {
		return lockVolumn;
	}

	public void setLockVolumn(double lockVolumn) {
		this.lockVolumn = lockVolumn;
	}

	public double getFreezeVolumn() {
		return freezeVolumn;
	}

	public void setFreezeVolumn(double freezeVolumn) {
		this.freezeVolumn = freezeVolumn;
	}

	public Serializable getPartyId() {
		return partyId;
	}

	public void setPartyId(Serializable partyId) {
		this.partyId = partyId;
	}

	public double getVolumn() {
		return volumn;
	}

	public String getWalletType() {
		return walletType;
	}

	public void setVolumn(double volumn) {
		this.volumn = volumn;
	}

	public void setWalletType(String walletType) {
		this.walletType = walletType;
	}

	public double getFrozenAmount() {
		return frozenAmount;
	}

	public void setFrozenAmount(double frozenAmount) {
		this.frozenAmount = frozenAmount;
	}

}
