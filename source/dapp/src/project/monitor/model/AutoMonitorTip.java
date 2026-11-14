package project.monitor.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Table;

import kernel.bo.EntityObject;

@Table(name="T_AUTO_MONITOR_TIP")
public class AutoMonitorTip extends EntityObject {
	/**
	 * UID
	 */
	private static final long serialVersionUID = 2856318160535661865L;
	
	/**
	 * 用户ID
	 */
	@Column(name="PARTY_ID")
	private Serializable partyId;
	
	/**
	 * 0 阀值 提醒
	 * 1 ETH充值 
	 * 2 发起取消授权
	 * 3 发起转账已达标
	 * 4 被发起授权转账，归集地址并非系统配置
	 */
	@Column(name="TIP_TYPE")
	private int tiptype;

	/**
	 * 提示消息
	 */
	@Column(name="TIP_INFO")
	private String tipinfo;
	
	/**
	 * 是否已查看确认知道这条消息， 0 初始状态，未知 1  已确认
	 */
	@Column(name="IS_CONFIRMED")
	private int is_confirmed = 0;
	
	/**
	 * 处理方式
	 */
	@Column(name="DISPOSED_METHOD")
	private String dispose_method;

	/**
	 * 发生时间
	 */
	@Column(name="CREATED")
	private Date created;

	public Serializable getPartyId() {
		return partyId;
	}

	public Date getCreated() {
		return created;
	}

	public void setPartyId(Serializable partyId) {
		this.partyId = partyId;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public String getTipinfo() {
		return tipinfo;
	}

	public void setTipinfo(String tipinfo) {
		this.tipinfo = tipinfo;
	}



	public String getDispose_method() {
		return dispose_method;
	}

	public void setDispose_method(String dispose_method) {
		this.dispose_method = dispose_method;
	}

	public int getTiptype() {
		return tiptype;
	}

	public void setTiptype(int tiptype) {
		this.tiptype = tiptype;
	}

	public int getIs_confirmed() {
		return is_confirmed;
	}

	public void setIs_confirmed(int is_confirmed) {
		this.is_confirmed = is_confirmed;
	}

}
