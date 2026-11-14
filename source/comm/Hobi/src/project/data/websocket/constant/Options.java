package project.data.websocket.constant;

import project.data.websocket.constant.enums.ExchangeEnum;

public interface Options {

	ExchangeEnum getExchange();

	String getRestHost();

	String getWebSocketHost();

	boolean isWebSocketAutoConnect();

}
