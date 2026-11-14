package project.data.websocket.client.req.market;

import project.data.websocket.constant.enums.DepthStepEnum;

public class SubMarketDepthRequest {

	private String symbol;

	private DepthStepEnum step;

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public DepthStepEnum getStep() {
		return step;
	}

	public void setStep(DepthStepEnum step) {
		this.step = step;
	}

}
