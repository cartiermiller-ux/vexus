package project.data.websocket.constant.enums;

public enum ExchangeEnum {

	HUOBI("huobi");

	private final String code;

	ExchangeEnum(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

}
