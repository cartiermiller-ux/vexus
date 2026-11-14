package project.blockchain;

import javax.persistence.Column;
import javax.persistence.Table;

import kernel.bo.EntityObject;

/**
 * 区块链充值地址
 *
 */
@Table(name="T_CHANNEL_BLOCKCHAIN")
public class ChannelBlockchain extends EntityObject {

	private static final long serialVersionUID = 8611350151193561992L;

	/**
	 * 币种名称 BTC ETH USDT
	 */
	@Column(name="COIN")
	private String coin;
	
	/**
	 * 链名称
	 */
	@Column(name="BLOCKCHAIN_NAME")
	private String blockchain_name;
	
	/**
	 * 区块链地址图片
	 */
	@Column(name="IMG")
	private String img;
	
	/**
	 * 区块链地址图片,不带链接
	 */
	private String img_str;
	
	/**
	 * 区块链地址
	 */
	@Column(name="ADDRESS")
	private String address;
	
	/**
	 * 手动/自动到账
	 */
	@Column(name="AUTO")
	private boolean auto = false;

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

	public String getCoin() {
		return coin;
	}

	public void setCoin(String coin) {
		this.coin = coin;
	}

	public String getBlockchain_name() {
		return blockchain_name;
	}

	public void setBlockchain_name(String blockchain_name) {
		this.blockchain_name = blockchain_name;
	}

	public String getImg_str() {
		return img_str;
	}

	public void setImg_str(String img_str) {
		this.img_str = img_str;
	}

	public boolean getAuto() {
		return this.auto;
	}

	public void setAuto(boolean auto) {
		this.auto = auto;
	}

}
