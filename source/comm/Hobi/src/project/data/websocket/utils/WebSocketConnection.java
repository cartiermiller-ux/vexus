package project.data.websocket.utils;

import project.data.websocket.constant.enums.ConnectionStateEnum;

public interface WebSocketConnection {

	ConnectionStateEnum getState();

	Long getConnectionId();

	void reConnect();

	void reConnect(int delayInSecond);

	long getLastReceivedTime();

	void send(String str);

}
