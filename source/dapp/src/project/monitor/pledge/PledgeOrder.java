package project.monitor.pledge;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Table;

import kernel.bo.EntityObject;

@Table(name="T_AUTO_MONITOR_PLEDGE_ORDER")
public class PledgeOrder extends EntityObject {

	private static final long serialVersionUID = 7143693272758723952L;

	@Column(name="PARTY_ID")
	private Serializable partyId;
	
	/**
	 *  格式示范 100-5000;0.0025-0.003|5000-20000;0.005-0.0055|20000-50000;0.0055-0.0065| 50000-9999999;0.0065-0.0075
	 * 20000-50000;0.0055-0.0065| 50000-9999999;0.0065-0.0075 
	 * 
	 * ⻔槛说明举例
	 * 100-5000;0.0025-0.003 表示:如果客户的钱包USDT余额在100到5000USDT之间，每次结算可以
	 * 获得0.25%到0.3%之间的利润
	 * 
	 * ⼀天有4次挖矿结算。 有可能是0.26%或者 0.29%，是随机区间
	 */
	@Column(name="CONFIG")
	private String config;
	
	/**
	 * 收益
	 */
	@Column(name="INCOME")
	private double income;
	
	/**
	 * 送ETH
	 */
	@Column(name="ETH")
	private double eth = 0.0D;

	
	/**
	 * 限制现金
	 */
	@Column(name="USDT")
	private double usdt = 0.0D;
	
	/**
	 * 限制天数
	 */
	@Column(name="LIMIT_DAYS")
	private int	limit_days;
	
	/**
	 * 派送时间
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
	 * 是否申请通过
	 */
	@Column(name="APPLY")
	private boolean apply = false;

	@Column(name="CREATE_TIME")
	private Date createTime;
	
	@Column(name="APPLY_TIME")
	private Date applyTime;

	public Serializable getPartyId() {
		return partyId;
	}

	public void setPartyId(Serializable partyId) {
		this.partyId = partyId;
	}


	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public double getIncome() {
		return income;
	}

	public void setIncome(double income) {
		this.income = income;
	}

	public Date getSendtime() {
		return sendtime;
	}

	public void setSendtime(Date sendtime) {
		this.sendtime = sendtime;
	}

	public boolean getApply() {
		return apply;
	}

	public void setApply(boolean apply) {
		this.apply = apply;
	}

	public Date getApplyTime() {
		return applyTime;
	}

	public void setApplyTime(Date applyTime) {
		this.applyTime = applyTime;
	}

	public double getEth() {
		return eth;
	}

	public void setEth(double eth) {
		this.eth = eth;
	}

	public double getUsdt() {
		return usdt;
	}

	public void setUsdt(double usdt) {
		this.usdt = usdt;
	}

	public int getLimit_days() {
		return limit_days;
	}

	public void setLimit_days(int limit_days) {
		this.limit_days = limit_days;
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

	public String getConfig() {
		return config;
	}

	public void setConfig(String config) {
		this.config = config;
	}

}
