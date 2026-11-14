package project.user.token;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Table;

import kernel.bo.EntityObject;

@Table(name="T_TOKEN")
public class Token  extends EntityObject{
	private static final long serialVersionUID = -5132505045848059321L;

	@Column(name="PARTY_ID")
	private Serializable partyId;
	
	 @Column(name="TOKEN")
	private String token;

	public Serializable getPartyId() {
		return partyId;
	}

	public void setPartyId(Serializable partyId) {
		this.partyId = partyId;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}
}
