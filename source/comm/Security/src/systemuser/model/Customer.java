package systemuser.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Table;

import kernel.bo.EntityObject;

@Table(name="T_CUSTOMER")
public class Customer extends EntityObject {
	/**
	 * UID
	 */
	private static final long serialVersionUID = -166660582843198652L;
	
	/**
	 * 客服名称
	 */
	@Column(name="USERNAME")
	private String username;
	
	/**
	 * 在线状态,0:下线，1：在线
	 */
	@Column(name="ONLINE_STATE")
	private int online_state;
	
	/**
	 * 最后一次分配的时间
	 */
	@Column(name="LAST_CUSTOMER_TIME")
	private Date last_customer_time;
	
	/**
	 * 最后一次上线的时间
	 */
	@Column(name="LAST_ONLINE_TIME")
	private Date last_online_time;
	
	/**
	 * 最后一次下线的时间
	 */
	@Column(name="LAST_OFFLINE_TIME")
	private Date last_offline_time;
	
	/**
	 * 创建时间
	 */
	@Column(name="CREATE_TIME")
	private Date create_time;
	
	/**
	 * 自动回复语句
	 */
	@Column(name="AUTO_ANSWER")
	private String auto_answer;
	
	/**
	 * 最后一次分配的用户
	 */
	private String last_message_user;

	public String getUsername() {
		return username;
	}

	public int getOnline_state() {
		return online_state;
	}

	public Date getLast_customer_time() {
		return last_customer_time;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setOnline_state(int online_state) {
		this.online_state = online_state;
	}

	public void setLast_customer_time(Date last_customer_time) {
		this.last_customer_time = last_customer_time;
	}

	public Date getCreate_time() {
		return create_time;
	}

	public void setCreate_time(Date create_time) {
		this.create_time = create_time;
	}

	public Date getLast_online_time() {
		return last_online_time;
	}

	public void setLast_online_time(Date last_online_time) {
		this.last_online_time = last_online_time;
	}

	public Date getLast_offline_time() {
		return last_offline_time;
	}

	public void setLast_offline_time(Date last_offline_time) {
		this.last_offline_time = last_offline_time;
	}

	public String getLast_message_user() {
		return last_message_user;
	}

	public void setLast_message_user(String last_message_user) {
		this.last_message_user = last_message_user;
	}

	public String getAuto_answer() {
		return auto_answer;
	}

	public void setAuto_answer(String auto_answer) {
		this.auto_answer = auto_answer;
	}

	
}
