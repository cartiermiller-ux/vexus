package project.log;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Table;

import kernel.bo.EntityObject;

@Table(name="T_MONEY_LOG")
public class MoneyLog extends EntityObject {
	
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");  
	
	@Column(name="LOG")
	private String log;
	
	@Column(name="WALLETTYPE")
	private String wallettype;
	
	@Column(name="CATEGORY")
	private String category;
	
	@Column(name="CREATE_TIME")
	private Date createTime;
	
	@Column(name="PARTY_ID")
	private Serializable partyId;

	@Column(name="AMOUNT")
	private double amount = 0.0D;
	
	//前端显示状态,0不显示,1显示
	@Column(name="SHOW_STATUS")
	private String showStatus = "1";

	@Column(name="AMOUNT_BEFORE")
	private double amount_before = 0.0D;

	@Column(name="AMOUNT_AFTER")
	private double amount_after = 0.0D;
	
	/**
	 * 资金日志提供的内容 ：提币 充币 永续建仓 永续平仓 手续费
	 */
	@Column(name="CONTENT_TYPE")
	private String content_type;
	
	private String createTimeStr;
	
	private static final long serialVersionUID = -5914896022101327097L;

	public String getWallettype() {
		return this.wallettype;
	}

	public void setWallettype(String wallettype) {
		this.wallettype = wallettype;
	}

	public Date getCreateTime() {
		return this.createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Serializable getPartyId() {
		return this.partyId;
	}

	public void setPartyId(Serializable partyId) {
		this.partyId = partyId;
	}

	public double getAmount() {
		return this.amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	public double getAmount_before() {
		return this.amount_before;
	}

	public void setAmount_before(double amount_before) {
		this.amount_before = amount_before;
	}

	public double getAmount_after() {
		return this.amount_after;
	}

	public void setAmount_after(double amount_after) {
		this.amount_after = amount_after;
	}

	public String getLog() {
		return this.log;
	}

	public void setLog(String log) {
		this.log = log;
	}

	public String getCategory() {
		return this.category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getCreateTimeStr() {
		return sdf.format(getCreateTime());
	}

	public void setCreateTimeStr(String createTimeStr) {
		this.createTimeStr = createTimeStr;
	}

	public String getContent_type() {
		return content_type;
	}

	public void setContent_type(String content_type) {
		this.content_type = content_type;
	}

	public String getShowStatus() {
		return showStatus;
	}

	public void setShowStatus(String showStatus) {
		this.showStatus = showStatus;
	}
}
