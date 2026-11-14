package project.data.model;

import javax.persistence.Column;
import javax.persistence.Table;

import kernel.bo.EntityObject;

@Table(name="T_SYMBOLS")
public class Symbols extends EntityObject {
	/**
	 * UID
	 */
	private static final long serialVersionUID = -6358871995039244069L;
	
	/**
	 * 交易对中的基础币种
	 */
	@Column(name="BASE_CURRENCY")
	private String base_currency;
	
	/**
	 * 交易对中的报价币种
	 */
	@Column(name="QUOTE_CURRENCY")
	private String quote_currency;

	/**
	 * 交易对报价的精度（小数点后位数）
	 */
	@Column(name="PRICE_PRECISION")
	private int price_precision;

	/**
	 * 交易对
	 */
	@Column(name="SYMBOL")
	private String symbol;

	/**
	 * 交易对状态；可能值: [online，offline,suspend] online - 已上线；offline -
	 * 交易对已下线，不可交易；suspend -- 交易暂停
	 */
	@Column(name="STATE")
	private String state;
	
	/**
	 * 交易对杠杆最大倍数
	 */
	@Column(name="LEVERAGE_RATIO")
	private Double leverage_ratio;

	public String getBase_currency() {
		return base_currency;
	}

	public void setBase_currency(String base_currency) {
		this.base_currency = base_currency;
	}

	public String getQuote_currency() {
		return quote_currency;
	}

	public void setQuote_currency(String quote_currency) {
		this.quote_currency = quote_currency;
	}

	public int getPrice_precision() {
		return price_precision;
	}

	public void setPrice_precision(int price_precision) {
		this.price_precision = price_precision;
	}

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public Double getLeverage_ratio() {
		return leverage_ratio;
	}

	public void setLeverage_ratio(Double leverage_ratio) {
		this.leverage_ratio = leverage_ratio;
	}

}
