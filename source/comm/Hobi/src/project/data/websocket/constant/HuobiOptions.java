package project.data.websocket.constant;

import project.data.websocket.constant.enums.ExchangeEnum;

public class HuobiOptions implements Options {

	private String restHost = "https://api-aws.huobi.pro";

	private String websocketHost = "wss://api-aws.huobi.pro";

	private boolean websocketAutoConnect = true;

	@Override
	public ExchangeEnum getExchange() {
		return ExchangeEnum.HUOBI;
	}

	@Override
	public String getRestHost() {
		return this.restHost;
	}

	@Override
	public String getWebSocketHost() {
		return this.websocketHost;
	}

	@Override
	public boolean isWebSocketAutoConnect() {
		return this.websocketAutoConnect;
	}

}
