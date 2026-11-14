package project.log;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Table;

import kernel.bo.EntityObject;

@Table(name="T_LOG")
public class Log extends EntityObject implements AbstractLog{
	/**
	 * Member Description
	 */

	private static final long serialVersionUID = 1837652077217320806L;
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

	// 日志类型，见Constants
	@Column(name="CATEGORY")
	private String category;
	/**
	 * 日志
	 */
	@Column(name="LOG")
	private String log;

	/**
	 * 扩展信息，统计分类时使用
	 */
	@Column(name="EXTRA")
	private String extra;
	
	@Column(name="OPERATOR")
	private String operator;

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


	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getExtra() {
		return extra;
	}

	public void setExtra(String extra) {
		this.extra = extra;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getOperator() {
		return operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}


}
