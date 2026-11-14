package project.wallet.rate;

import javax.persistence.Column;
import javax.persistence.Table;

import kernel.bo.EntityObject;

/**
 * 
 * 货币汇率。Wallet汇WalletExtend或WalletExtend汇Wallet的汇率值
 */
@Table(name="T_EXCHANGE_RATE")
public class ExchangeRate extends EntityObject {

	public static final String IN = "in";
	
	public static final String OUT = "out";

	private static final long serialVersionUID = 5591037881881001013L;
	
	/**
	 * out兑出 in 兑入
	 */
	@Column(name="OUT_OR_IN")
	private String out_or_in;
	
	/**
	 * 汇率
	 */
	@Column(name="RATA")
	private double rata;

	/**
	 * 货币，见Constants定义
	 */
	@Column(name="CURRENCY")
	private String currency;

	@Column(name="NAME")
	private String name;

	/**
	 * 货币符号
	 */
	@Column(name="CURRENCY_SYMBOL")
	private String currency_symbol;

	public String getOut_or_in() {
		return out_or_in;
	}

	public void setOut_or_in(String out_or_in) {
		this.out_or_in = out_or_in;
	}

	public double getRata() {
		return rata;
	}

	public void setRata(double rata) {
		this.rata = rata;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCurrency_symbol() {
		return currency_symbol;
	}

	public void setCurrency_symbol(String currency_symbol) {
		this.currency_symbol = currency_symbol;
	}

}
