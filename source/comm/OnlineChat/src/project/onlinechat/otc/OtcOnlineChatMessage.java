package project.onlinechat.otc;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Table;

import kernel.bo.EntityObject;

/**
 * 客服留言表
 */
@Table(name="T_OTC_ONLINECHAT_MESSAGE")
public class OtcOnlineChatMessage extends EntityObject implements Comparable<OtcOnlineChatMessage> {

	@Column(name="PARTY_ID")
	private String partyId;
	
	/**
	 * send 发送 receive 接收
	 */
	@Column(name="SEND_RECEIVE")
	private String send_receive;
	
	/**
	 * text、img
	 */
	@Column(name="CONTENT_TYPE")
	private String type;

	/**
	 * 留言内容
	 */
	@Column(name="CONTENT")
	private String content;

	@Column(name="CREATE_TIME")
	private Date createTime;

	/**
	 * 用户名(管理员发送时纪录)
	 */
	@Column(name="USERNAME")
	private String username;

	@Column(name="IP")
	private String ip;
	
	/**
	 * 订单号
	 */
	@Column(name="ORDER_NO")
	private String order_no;
	
	/**
	 * 标记删除，-1:删除，0:正常
	 */
	@Column(name="DELETE_STATUS")
	private int delete_status=0;
	
	private static final long serialVersionUID = -4999012202564084751L;

	public String getPartyId() {
		return partyId;
	}

	public void setPartyId(String partyId) {
		this.partyId = partyId;
	}

	public String getSend_receive() {
		return send_receive;
	}

	public void setSend_receive(String send_receive) {
		this.send_receive = send_receive;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	@Override
	public int compareTo(OtcOnlineChatMessage onlineChatMessage) {
		if (this.createTime.after(onlineChatMessage.getCreateTime())) {
			return 1;
		} else if (this.createTime.before(onlineChatMessage.getCreateTime())) {
			return -1;
		}else {
			return 0;
		}
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
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

	public int getDelete_status() {
		return delete_status;
	}

	public void setDelete_status(int delete_status) {
		this.delete_status = delete_status;
	}
}
