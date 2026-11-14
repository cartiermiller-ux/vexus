package project.wallet;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Table;

import kernel.bo.EntityObject;
/**
 * 充提记录
 *
 */
@Table(name="T_WALLET_LOG")
public class WalletLog extends EntityObject {
	private static final long serialVersionUID = 5914244062518608589L;
	@Column(name="PARTY_ID")
	private Serializable partyId;
	
	@Column(name="ORDER_NO")
	private String order_no;
	
	@Column(name="AMOUNT")
	private double amount = 0.0D;
	
	// 日志类型，见Constants
	@Column(name="CATEGORY")
	private String category;

	@Column(name="STATUS")
	private int status;

	// 创建时间
	@Column(name="CREATE_TIME")
	private Date createTime;

	private String createTimeStr;
	
	/**
	 * 交易币种
	 */
	@Column(name="WALLETTYPE")
	private String wallettype;

	public Serializable getPartyId() {
		return partyId;
	}

	public void setPartyId(Serializable partyId) {
		this.partyId = partyId;
	}

	public String getOrder_no() {
		return order_no;
	}

	public void setOrder_no(String order_no) {
		this.order_no = order_no;
	}

	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public String getCreateTimeStr() {
		return createTimeStr;
	}

	public void setCreateTimeStr(String createTimeStr) {
		this.createTimeStr = createTimeStr;
	}

	public String getWallettype() {
		return wallettype;
	}

	public void setWallettype(String wallettype) {
		this.wallettype = wallettype;
	}

}
