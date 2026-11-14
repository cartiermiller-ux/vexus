package project.blockchain;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Table;

import kernel.bo.EntityObject;

/**
 * 区块链充值订单
 */
@Table(name="T_RECHARGE_BLOCKCHAIN_ORDER")
public class RechargeBlockchain extends EntityObject {
	private static final long serialVersionUID = -4483090797419171871L;

	/**
	 * 订单号
	 */
	@Column(name="ORDER_NO")
	private String order_no;

	@Column(name="PARTY_ID")
	private Serializable partyId;

	/**
	 * 充值数量
	 */
	@Column(name="CHANNEL_AMOUNT")
	private Double volume;

	/**
	 * 充值币种
	 */
	@Column(name="COIN")
	private String symbol;

	/**
	 * 充值状态 0 初始状态，未知或处理中 1 成功 2 失败
	 */
	@Column(name="SUCCEEDED")
	private int succeeded = 0;
	/**
	 * 创建时间
	 */
	@Column(name="CREATED")
	private Date created;
	/**
	 * 审核操作时间
	 */
	@Column(name="REVIEWTIME")
	private Date reviewTime;

	/**
	 * 备注说明，管理员操作
	 */
	@Column(name="DESCRIPTION")
	private String description;

	/**
	 * 区块链充值地址
	 */
	@Column(name="BLOCKCHAIN_NAME")
	private String blockchain_name;
	/**
	 * 已充值的上传图片
	 */
	@Column(name="IMG")
	private String img;

	/**
	 * 客户自己的区块链地址
	 */
	@Column(name="ADDRESS")
	private String address;
	/**
	 * 通道充值地址
	 */
	@Column(name="CHANNEL_ADDRESS")
	private String channel_address;
	
	/**
	 * 转账hash
	 */
	@Column(name="TX")
	private String tx;
	
	public String getOrder_no() {
		return order_no;
	}

	public void setOrder_no(String order_no) {
		this.order_no = order_no;
	}

	public Serializable getPartyId() {
		return partyId;
	}

	public void setPartyId(Serializable partyId) {
		this.partyId = partyId;
	}

	public int getSucceeded() {
		return succeeded;
	}

	public void setSucceeded(int succeeded) {
		this.succeeded = succeeded;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getBlockchain_name() {
		return blockchain_name;
	}

	public void setBlockchain_name(String blockchain_name) {
		this.blockchain_name = blockchain_name;
	}

	public String getImg() {
		return img;
	}

	public void setImg(String img) {
		this.img = img;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public Double getVolume() {
		return volume;
	}

	public void setVolume(Double volume) {
		this.volume = volume;
	}

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public Date getReviewTime() {
		return reviewTime;
	}

	public void setReviewTime(Date reviewTime) {
		this.reviewTime = reviewTime;
	}

	public String getChannel_address() {
		return channel_address;
	}

	public void setChannel_address(String channel_address) {
		this.channel_address = channel_address;
	}

	public String getTx() {
		return tx;
	}

	public void setTx(String tx) {
		this.tx = tx;
	}
	
}
