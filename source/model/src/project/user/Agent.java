package project.user;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Table;

import kernel.bo.EntityObject;

@Table(name="T_AGENT")
public class Agent extends EntityObject {
	
	private static final long serialVersionUID = 8554751796911238962L;

	@Column(name="PARTY_ID")
	private Serializable partyId;

	/**
	 * 父节点
	 */
	@Column(name="PARENT_PARTY_ID")
	private Serializable parent_partyId;

	public Serializable getPartyId() {
		return partyId;
	}

	public void setPartyId(Serializable partyId) {
		this.partyId = partyId;
	}

	public Serializable getParent_partyId() {
		return parent_partyId;
	}

	public void setParent_partyId(Serializable parent_partyId) {
		this.parent_partyId = parent_partyId;
	}
}
