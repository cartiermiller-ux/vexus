package project.log;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Table;

import kernel.bo.EntityObject;

@Table(name="T_CODE_LOG")
public class CodeLog extends EntityObject implements AbstractLog{

	private static final long serialVersionUID = 7008440232393696319L;

	/**
	 * 日志归属
	 */
	@Column(name="PARTY_ID")
	private Serializable partyId;
	
	/**
	 * 日志归属
	 */
	@Column(name="USERNAME")
	private String username;

	/**
	 * 日志
	 */
	@Column(name="LOG")
	private String log;

	/**
	 * TARGET
	 */
	@Column(name="TARGET")
	private String target;
	

	// 创建时间
	@Column(name="CREATE_TIME")
	private Date createTime;

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Serializable getPartyId() {
		return partyId;
	}

	public void setPartyId(Serializable partyId) {
		this.partyId = partyId;
	}

	public String getLog() {
		return log;
	}

	public void setLog(String log) {
		this.log = log;
	}
	
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}
}


