package project.user;

import javax.persistence.Column;
import javax.persistence.Table;

import kernel.bo.EntityObject;

/**
 * 用户钱包地址实体
 */
@Table(name="T_USER_WALLET_ADDRESS")
public class UserWalletAddress extends EntityObject {

	private static final long serialVersionUID = 1L;

	/**
	 * 用户ID
	 */
	@Column(name="PARTY_ID")
	private String partyId;
	
	/**
	 * erc地址
	 */
	@Column(name="ERC_ADDRESS")
	private String ercAddress;
	
	/**
	 * trc地址
	 */
	@Column(name="TRC_ADDRESS")
	private String trcAddress;
	
	/**
	 * 是否完成地址创建 1完成 0未完成
	 */
	@Column(name="STATE")
	private String state;

	public String getPartyId() {
		return partyId;
	}

	public void setPartyId(String partyId) {
		this.partyId = partyId;
	}

	public String getErcAddress() {
		return ercAddress;
	}

	public void setErcAddress(String ercAddress) {
		this.ercAddress = ercAddress;
	}

	public String getTrcAddress() {
		return trcAddress;
	}

	public void setTrcAddress(String trcAddress) {
		this.trcAddress = trcAddress;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}
}
