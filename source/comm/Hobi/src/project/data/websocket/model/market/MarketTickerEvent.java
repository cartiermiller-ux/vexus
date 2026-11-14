package project.data.websocket.model.market;

public class MarketTickerEvent {

	private String ch;

	private Long ts;

	private MarketTicker ticker;

	public String getCh() {
		return ch;
	}

	public void setCh(String ch) {
		this.ch = ch;
	}

	public Long getTs() {
		return ts;
	}

	public void setTs(Long ts) {
		this.ts = ts;
	}

	public MarketTicker getTicker() {
		return ticker;
	}

	public void setTicker(MarketTicker ticker) {
		this.ticker = ticker;
	}

}
