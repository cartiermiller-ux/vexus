package project.ddos.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Table;

import kernel.bo.EntityObject;

@Table(name="T_IP_MENU")
public class IpMenu extends EntityObject {
	/**
	 * UID
	 */
	private static final long serialVersionUID = -4196439149608747292L;
	
	/**
	 * 白名单
	 */
	public static final String IP_WHITE = "white";
	
	/**
	 * 黑名单
	 */
	public static final String IP_BLACK = "black";
	
	/**
	 * 锁定名单
	 */
	public static final String IP_LOCK = "lock";
	
	/**
	 * ip
	 */
	@Column(name="IP")
	private String ip;
	
	/**
	 * 类型 ：black:黑名单，white：白名单
	 */
	@Column(name="TYPE")
	private String type;
	
	/**
	 * -1:标记删除，0:正常
	 */
	@Column(name="DELETE_STATUS")
	private int delete_status;
	
	/**
	 * 创建时间
	 */
	@Column(name="CREATE_TIME")
	private Date create_time;
	
	/**
	 * 最后处理时间
	 */
	@Column(name="LAST_OPERA_TIME")
	private Date last_opera_time;

	public String getIp() {
		return ip;
	}

	public String getType() {
		return type;
	}

	public int getDelete_status() {
		return delete_status;
	}

	public Date getCreate_time() {
		return create_time;
	}

	public Date getLast_opera_time() {
		return last_opera_time;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setDelete_status(int delete_status) {
		this.delete_status = delete_status;
	}

	public void setCreate_time(Date create_time) {
		this.create_time = create_time;
	}

	public void setLast_opera_time(Date last_opera_time) {
		this.last_opera_time = last_opera_time;
	}
}
