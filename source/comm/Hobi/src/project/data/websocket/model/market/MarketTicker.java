package project.data.websocket.model.market;

import java.math.BigDecimal;

public class MarketTicker {

	// private String symbol;

	private BigDecimal open;

	private BigDecimal close;

	private BigDecimal low;

	private BigDecimal high;

	private BigDecimal amount;

	private Long count;

	private BigDecimal vol;

	private BigDecimal bid;

	/**
	 * 最高买价对应的量
	 */
	private BigDecimal bidSize;

	private BigDecimal ask;

	/**
	 * 最低卖价对应的量
	 */
	private BigDecimal askSize;
	
	/**
	 * 最新成交价
	 */
	private BigDecimal lastPrice;
	
	/**
	 * 最新成交价对应的量
	 */
	private BigDecimal lastSize;

	public BigDecimal getOpen() {
		return open;
	}

	public void setOpen(BigDecimal open) {
		this.open = open;
	}

	public BigDecimal getClose() {
		return close;
	}

	public void setClose(BigDecimal close) {
		this.close = close;
	}

	public BigDecimal getLow() {
		return low;
	}

	public void setLow(BigDecimal low) {
		this.low = low;
	}

	public BigDecimal getHigh() {
		return high;
	}

	public void setHigh(BigDecimal high) {
		this.high = high;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public Long getCount() {
		return count;
	}

	public void setCount(Long count) {
		this.count = count;
	}

	public BigDecimal getVol() {
		return vol;
	}

	public void setVol(BigDecimal vol) {
		this.vol = vol;
	}

	public BigDecimal getBid() {
		return bid;
	}

	public void setBid(BigDecimal bid) {
		this.bid = bid;
	}

	public BigDecimal getBidSize() {
		return bidSize;
	}

	public void setBidSize(BigDecimal bidSize) {
		this.bidSize = bidSize;
	}

	public BigDecimal getAsk() {
		return ask;
	}

	public void setAsk(BigDecimal ask) {
		this.ask = ask;
	}

	public BigDecimal getAskSize() {
		return askSize;
	}

	public void setAskSize(BigDecimal askSize) {
		this.askSize = askSize;
	}

	public BigDecimal getLastPrice() {
		return lastPrice;
	}

	public void setLastPrice(BigDecimal lastPrice) {
		this.lastPrice = lastPrice;
	}

	public BigDecimal getLastSize() {
		return lastSize;
	}

	public void setLastSize(BigDecimal lastSize) {
		this.lastSize = lastSize;
	}

}
