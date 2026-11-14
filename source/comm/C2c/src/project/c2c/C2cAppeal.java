package project.c2c;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Table;

import kernel.bo.EntityObject;

@Table(name="T_C2C_APPEAL")
public class C2cAppeal extends EntityObject {

	private static final long serialVersionUID = -7865789470407956262L;

	/**
	 * 关联订单号
	 */
	@Column(name="ORDER_NO")
	private String orderNo;

	/**
	 * 申诉理由
	 */
	@Column(name="REASON")
	private String reason;

	/**
	 * 申诉描述
	 */
	@Column(name="DESCRIPTION")
	private String description;

	/**
	 * 申诉凭证
	 */
	@Column(name="IMG")
	private String img;

	/**
	 * 联系人
	 */
	@Column(name="NAME")
	private String name;

	/**
	 * 联系电话
	 */
	@Column(name="PHONE")
	private String phone;

	/**
	 * 申诉状态：0已提交/1已处理
	 */
	@Column(name="STATE")
	private String state;
	
	/**
	 * 创建时间
	 */
	@Column(name="CREATE_TIME")
	private Date createTime;
	
	/**
	 * 更新时间
	 */
	@Column(name="UPDATE_TIME")
	private Date updateTime;
	
	public String getOrderNo() {
		return orderNo;
	}

	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getImg() {
		return img;
	}

	public void setImg(String img) {
		this.img = img;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}

}
