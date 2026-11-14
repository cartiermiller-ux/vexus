package project.wallet.rate;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Table;

import kernel.bo.EntityObject;

/**
 * 用户汇率配置
 */
@Table(name="T_USER_RATE_CONFIG")
public class UserRateConfig extends EntityObject {
	private static final long serialVersionUID = 1L;
	
	@Column(name="PARTY_ID")
	private Serializable partyId;

	/**
	 * 货币，见Constants定义
	 */
	@Column(name="CURRENCY")
	private String currency;

	public Serializable getPartyId() {
		return partyId;
	}

	public void setPartyId(Serializable partyId) {
		this.partyId = partyId;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

}
