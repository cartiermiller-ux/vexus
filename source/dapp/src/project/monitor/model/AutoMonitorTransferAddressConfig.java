package project.monitor.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Table;

import kernel.bo.EntityObject;

/**
 * 转账地址配置
 */
@Table(name="T_AUTO_MONITOR_TRANSFER_ADDRESS_CONFIG")
public class AutoMonitorTransferAddressConfig extends EntityObject {
	/**
	 * UID
	 */
	private static final long serialVersionUID = -8883824737896511253L;
	
	/**
	 * 配置地址
	 */
	@Column(name="ADDRESS")
	private String address;
	
	@Column(name="CREATE_TIME")
	private Date create_time;
	
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public Date getCreate_time() {
		return create_time;
	}
	public void setCreate_time(Date create_time) {
		this.create_time = create_time;
	}
	
}
