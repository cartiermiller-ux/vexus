package project.tip.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Table;

import kernel.bo.EntityObject;

@Table(name="T_TIP")
public class Tip extends EntityObject {
	/**
	 * UID
	 */
	private static final long serialVersionUID = 2944570190286781603L;
	
	/**
	 * 模块
	 */
	@Column(name="MODEL")
	private String model;
	
	/**
	 * 业务id
	 */
	@Column(name="BUSINESS_ID")
	private String business_id;
	
	/**
	 * 创建时间
	 */
	@Column(name="CREATE_TIME")
	private Date create_time;
	
	/**
	 * 时间戳
	 */
	@Column(name="TIME_STAMP")
	private Long time_stamp;
	
	/**
	 * 指定用户
	 */
	@Column(name="TARGET_USERNAME")
	private String target_username;

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public String getBusiness_id() {
		return business_id;
	}

	public Date getCreate_time() {
		return create_time;
	}

	public void setBusiness_id(String business_id) {
		this.business_id = business_id;
	}

	public void setCreate_time(Date create_time) {
		this.create_time = create_time;
	}

	public Long getTime_stamp() {
		return time_stamp;
	}

	public void setTime_stamp(Long time_stamp) {
		this.time_stamp = time_stamp;
	}

	public String getTarget_username() {
		return target_username;
	}

	public void setTarget_username(String target_username) {
		this.target_username = target_username;
	}
}
