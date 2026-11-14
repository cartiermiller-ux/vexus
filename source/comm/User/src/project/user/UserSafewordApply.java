package project.user;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Table;

import kernel.bo.EntityObject;

@Table(name="T_USER_SAFEWORD_APPLY")
public class UserSafewordApply extends EntityObject {
	
	private static final long serialVersionUID = -4196439149608747292L;
	
	/**
	 * 实名姓名
	 */
	@Column(name="PARTY_ID")
	private Serializable partyId;
	
	/**
	 * 证件正面照
	 */
	@Column(name="IDCARD_PATH_FRONT")
	private String idcard_path_front;
	
	/**
	 * 证件背面照
	 */
	@Column(name="IDCARD_PATH_BACK")
	private String idcard_path_back;
	
	/**
	 * 正面手持证件照
	 */
	@Column(name="IDCARD_PATH_HOLD")
	private String idcard_path_hold;
	
	/**
	 * 资金密码
	 */
	@Column(name="SAFEWORD")
	private String safeword;
	
	/**
	 * 1审核中 ，2 审核通过,3审核未通过
	 */
	@Column(name="STATUS")
	private int status;
	
	/**
	 * 审核消息，未通过原因
	 */
	@Column(name="MSG")
	private String msg;
	
	/**
	 * 创建时间
	 */
	@Column(name="CREATE_TIME")
	private Date create_time;
	
	/**
	 * 审核时间
	 */
	@Column(name="APPLY_TIME")
	private Date apply_time;
	
    /**
     * 操作类型
     */
	@Column(name="OPERATE")
    private int operate;
	
    /**
     * 备注
     */
	@Column(name="REMARK")
    private String remark;

	public Serializable getPartyId() {
		return partyId;
	}

	public void setPartyId(Serializable partyId) {
		this.partyId = partyId;
	}

	public String getIdcard_path_front() {
		return idcard_path_front;
	}

	public void setIdcard_path_front(String idcard_path_front) {
		this.idcard_path_front = idcard_path_front;
	}

	public String getIdcard_path_back() {
		return idcard_path_back;
	}

	public void setIdcard_path_back(String idcard_path_back) {
		this.idcard_path_back = idcard_path_back;
	}

	public String getIdcard_path_hold() {
		return idcard_path_hold;
	}

	public void setIdcard_path_hold(String idcard_path_hold) {
		this.idcard_path_hold = idcard_path_hold;
	}

	public String getSafeword() {
		return safeword;
	}

	public void setSafeword(String safeword) {
		this.safeword = safeword;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public Date getCreate_time() {
		return create_time;
	}

	public void setCreate_time(Date create_time) {
		this.create_time = create_time;
	}

	public Date getApply_time() {
		return apply_time;
	}

	public void setApply_time(Date apply_time) {
		this.apply_time = apply_time;
	}

	public int getOperate() {
		return operate;
	}

	public void setOperate(int operate) {
		this.operate = operate;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}
	
}
