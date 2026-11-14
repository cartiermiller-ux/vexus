package project.data.websocket.model.market;

public class MarketDepthEvent {

	private String ch;

	private Long ts;

	private MarketDepth depth;

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

	public MarketDepth getDepth() {
		return depth;
	}

	public void setDepth(MarketDepth depth) {
		this.depth = depth;
	}

}
