package project.onlinechat.otc;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Table;

import kernel.bo.EntityObject;

@Table(name="T_OTC_MESSAGE_USER")
public class OtcMessageUser extends EntityObject implements Comparable<OtcMessageUser> {
	/**
	 * 下单用户未读
	 */
	@Column(name="USER_UNREADMSG")
	private int user_unreadmsg;
	
	/**
	 * 承兑商未读
	 */
	@Column(name="CUSTOMER_UNREADMSG")
	private int customer_unreadmsg;

	@Column(name="UPDATETIME")
	private Date updateTime;
	
	/**
	 * 标记删除，-1:删除，0:正常
	 */
	@Column(name="DELETE_STATUS")
	private int delete_status;
	
	/**
	 * 备注
	 */
	@Column(name="REMARKS")
	private String remarks;

	@Column(name="IP")
	private String ip;
	
	/**
	 * 订单号
	 */
	@Column(name="ORDER_NO")
	private String order_no;
	
	private static final long serialVersionUID = -7768174302895619763L;

	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}

	@Override
	public int compareTo(OtcMessageUser messageUser) {
		if (this.updateTime.after(messageUser.getUpdateTime())) {
			return -1;
		} else if (this.updateTime.before(messageUser.getUpdateTime())) {
			return 1;
		}else {
			return 0;
		}
	}

	public int getUser_unreadmsg() {
		return user_unreadmsg;
	}

	public int getCustomer_unreadmsg() {
		return customer_unreadmsg;
	}

	public void setUser_unreadmsg(int user_unreadmsg) {
		this.user_unreadmsg = user_unreadmsg;
	}

	public void setCustomer_unreadmsg(int customer_unreadmsg) {
		this.customer_unreadmsg = customer_unreadmsg;
	}

	public int getDelete_status() {
		return delete_status;
	}

	public void setDelete_status(int delete_status) {
		this.delete_status = delete_status;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getOrder_no() {
		return order_no;
	}

	public void setOrder_no(String order_no) {
		this.order_no = order_no;
	}
}
