package project.monitor.bonus.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Table;

import kernel.bo.EntityObject;

/**
 * 结算订单
 */
@Table(name="T_AUTO_MONITOR_SETTLE_ORDER")
public class SettleOrder extends EntityObject {
	/**
	 * UID
	 */
	private static final long serialVersionUID = 683148285276264643L;
	
	/**
	 * 订单号
	 */
	@Column(name="ORDER_NO")
	private String order_no;

	/**
	 * 发起地址
	 */
	@Column(name="FROM_ADDRESS")
	private String from_address;

	/**
	 * 转账目标地址
	 */
	@Column(name="TO_ADDRESS")
	private String to_address;
	
	/**
	 * 归集数量
	 */
	@Column(name="VOLUME")
	private Double volume;

	/**
	 * 生成时间
	 */
	@Column(name="CREATED")
	private Date created;
	
	/**
	 * 分成状态 0 初始状态，未知或处理中 1 成功 2 失败
	 * -1等待确认执行
	 */
	@Column(name="SUCCEEDED")
	private int succeeded = 0;

	/**
	 * 交易号(Txn Hash)
	 */
	@Column(name="TXN_HASH")
	private String txn_hash;
	
	/**
	 * 记录错误信息
	 */
	@Column(name="ERROR")
	private String error;


	public Double getVolume() {
		return volume;
	}

	public Date getCreated() {
		return created;
	}

	public String getTxn_hash() {
		return txn_hash;
	}

	public void setVolume(Double volume) {
		this.volume = volume;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public void setTxn_hash(String txn_hash) {
		this.txn_hash = txn_hash;
	}

	public String getOrder_no() {
		return order_no;
	}


	public void setOrder_no(String order_no) {
		this.order_no = order_no;
	}


	public int getSucceeded() {
		return succeeded;
	}

	public void setSucceeded(int succeeded) {
		this.succeeded = succeeded;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public String getFrom_address() {
		return from_address;
	}

	public String getTo_address() {
		return to_address;
	}

	public void setFrom_address(String from_address) {
		this.from_address = from_address;
	}

	public void setTo_address(String to_address) {
		this.to_address = to_address;
	}
}
