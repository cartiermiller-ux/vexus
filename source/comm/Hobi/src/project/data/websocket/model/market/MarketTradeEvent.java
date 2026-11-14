package project.data.websocket.model.market;

import java.util.List;

public class MarketTradeEvent {

	private String ch;

	private Long ts;

	private List<MarketTrade> list;

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

	public List<MarketTrade> getList() {
		return list;
	}

	public void setList(List<MarketTrade> list) {
		this.list = list;
	}

}
