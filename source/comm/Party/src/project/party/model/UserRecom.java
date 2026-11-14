package project.party.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Table;

import com.alibaba.fastjson.JSON;

import kernel.bo.EntityObject;

/**
 * 推荐关系
 */
@Table(name="PAT_USER_RECOM")
public class UserRecom extends EntityObject {
	
	@Column(name="PARTY_ID")
	private Serializable partyId;
	
	/**
	 * 推荐人
	 */
	@Column(name="RECO_ID")
	private Serializable reco_id;
	
	private static final long serialVersionUID = 4306215956505507789L;
	
	@Override
	public boolean equals(Object other) {
		if(null==other) return false;
		if(!UserRecom.class.isInstance(other)) return false;
		UserRecom otherUserRecom=(UserRecom)other;
		return getId().equals(otherUserRecom.getId()) && partyId.equals(otherUserRecom.partyId);
	}

	@Override
	public int hashCode() {
		return getId().hashCode()+partyId.hashCode();
	}

	public Serializable getPartyId() {
		return this.partyId;
	}

	public void setPartyId(Serializable partyId) {
		this.partyId = partyId;
	}

	public Serializable getReco_id() {
		return this.reco_id;
	}

	public void setReco_id(Serializable reco_id) {
		this.reco_id = reco_id;
	}
	
    // @Override
    public String toString() {
    	return JSON.toJSONString(this);
    }
}
