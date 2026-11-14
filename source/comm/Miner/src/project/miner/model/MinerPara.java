package project.miner.model;

import javax.persistence.Column;
import javax.persistence.Table;

import kernel.bo.EntityObject;

/**
 * 矿机配置参数
 */
@Table(name="T_MINER_PARA")
public class MinerPara extends EntityObject {

	private static final long serialVersionUID = 1639941028310043811L;

	/**
	 * 矿机id
	 */
	@Column(name="MINER_ID")
	private String miner_id;
	
	/**
	 * 购买周期
	 */
	@Column(name="CYCLE")
	private int cycle;

	/**
	 * 购买价格
	 */
	@Column(name="AMOUNT")
	private double amount;

	public int getCycle() {
		return cycle;
	}

	public double getAmount() {
		return amount;
	}

	public void setCycle(int cycle) {
		this.cycle = cycle;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	public String getMiner_id() {
		return miner_id;
	}

	public void setMiner_id(String miner_id) {
		this.miner_id = miner_id;
	}
	
}
