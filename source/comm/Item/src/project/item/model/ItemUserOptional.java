package project.item.model;

import javax.persistence.Column;
import javax.persistence.Table;

import kernel.bo.EntityObject;

/**
 * 用户自选币种
 */
@Table(name="T_ITEM_USER_OPTIONAL")
public class ItemUserOptional extends EntityObject {

	private static final long serialVersionUID = 4857935723215615892L;

	@Column(name="PARTY_ID")
	private String partyId;

	/**
	 * 代码
	 */
	@Column(name="SYMBOL")
	private String symbol;

	public String getPartyId() {
		return partyId;
	}

	public String getSymbol() {
		return symbol;
	}

	public void setPartyId(String partyId) {
		this.partyId = partyId;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

}
