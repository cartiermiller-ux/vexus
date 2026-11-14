package project.data.websocket.model.market;

import java.util.List;

public class MarketDepth {

	private long version;

	private Long ts;

	private List<PriceLevel> bids;

	private List<PriceLevel> asks;

	public long getVersion() {
		return version;
	}

	public void setVersion(long version) {
		this.version = version;
	}

	public Long getTs() {
		return ts;
	}

	public void setTs(Long ts) {
		this.ts = ts;
	}

	public List<PriceLevel> getBids() {
		return bids;
	}

	public void setBids(List<PriceLevel> bids) {
		this.bids = bids;
	}

	public List<PriceLevel> getAsks() {
		return asks;
	}

	public void setAsks(List<PriceLevel> asks) {
		this.asks = asks;
	}

}
