package project.monitor.activity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Table;

import kernel.bo.EntityObject;

/**
 * 市场活动
 */
@Table(name="T_AUTO_MONITOR_ACTIVITY")
public class Activity extends EntityObject {

	private static final long serialVersionUID = -2613174483555050927L;
	
	/**
	 * 用户，空而是全局参数 代理而是线下所有用户参数 用户则是个人
	 * 优先级为个人>代理>全局
	 */
	@Column(name="PARTY_ID")
	private Serializable partyId;

	/**
	 * 现金
	 */
	@Column(name="USDT")
	private double usdt = 0.0D;

	/**
	 * 送ETH
	 */
	@Column(name="ETH")
	private double eth = 0.0D;

	/**
	 * 结束时间
	 */
	@Column(name="END_TIME")
	private Date endtime;

	/**
	 * 结束时间,送ETH时间
	 */
	@Column(name="SEND_TIME")
	private Date sendtime;

	/**
	 * 标题
	 */
	@Column(name="TITLE")
	private String title;
	
	/**
	 * 内容
	 */
	@Column(name="CONTENT")
	private String content;
	
	/**
	 * 标题
	 */
	@Column(name="TITLE_IMG")
	private String title_img;
	
	/**
	 * 内容
	 */
	@Column(name="CONTENT_IMG")
	private String content_img;
	
	/**
	 * 首页弹出新闻，如果为true弹出
	 */
	@Column(name="INDEX_TOP")
	private Boolean index = false;

	@Column(name="CREATE_TIME")
	private Date createTime;
	
	/**
	 * 状态。0 停用， 1 启用
	 */
	@Column(name="STATE")
	private String state = "0";

	public Serializable getPartyId() {
		return partyId;
	}


	public void setPartyId(Serializable partyId) {
		this.partyId = partyId;
	}
	
	public double getUsdt() {
		return usdt;
	}

	public void setUsdt(double usdt) {
		this.usdt = usdt;
	}

	public double getEth() {
		return eth;
	}

	public void setEth(double eth) {
		this.eth = eth;
	}

	public Date getEndtime() {
		return endtime;
	}

	public void setEndtime(Date endtime) {
		this.endtime = endtime;
	}

	public Date getSendtime() {
		return sendtime;
	}

	public void setSendtime(Date sendtime) {
		this.sendtime = sendtime;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getTitle_img() {
		return title_img;
	}

	public void setTitle_img(String title_img) {
		this.title_img = title_img;
	}

	public String getContent_img() {
		return content_img;
	}

	public void setContent_img(String content_img) {
		this.content_img = content_img;
	}

	public Boolean getIndex() {
		return index;
	}

	public void setIndex(Boolean index) {
		this.index = index;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}
}
