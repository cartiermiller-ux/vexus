package project.log;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Table;

import kernel.bo.EntityObject;

@Table(name="T_API_LOG")
public class ApiLog extends EntityObject implements AbstractLog {
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
	 * ip
	 */
	@Column(name="IP")
	private String ip;

	/**
	 * 接口名
	 */
	@Column(name="API")
	private String api;
	
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

	/**
	 * 创建时间
	 */
	@Column(name="CREATE_TIME")
	private Date createTime;

	public Serializable getPartyId() {
		return partyId;
	}

	public String getIp() {
		return ip;
	}

	public String getLog() {
		return log;
	}

	public String getExtra() {
		return extra;
	}

	public String getApi() {
		return api;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setPartyId(Serializable partyId) {
		this.partyId = partyId;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public void setLog(String log) {
		this.log = log;
	}

	public void setExtra(String extra) {
		this.extra = extra;
	}

	public void setApi(String api) {
		this.api = api;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

}
