package project.item.model;

import javax.persistence.Column;
import javax.persistence.Table;

import kernel.bo.EntityObject;

/**
 * 
 * 杠杆定义
 */
@Table(name="T_ITEM_LEVERAGE")
public class ItemLever extends EntityObject {
	
	@Column(name="ITEM_ID")
	private String item_id;
	
	/**
	 * 杠杆倍数
	 */
	@Column(name="LEVER_RATE")
	private double lever_rate;
	
	private static final long serialVersionUID = -6464213099743653537L;

	public String getItem_id() {
		return item_id;
	}

	public void setItem_id(String item_id) {
		this.item_id = item_id;
	}

	public double getLever_rate() {
		return lever_rate;
	}

	public void setLever_rate(double lever_rate) {
		this.lever_rate = lever_rate;
	}

}
