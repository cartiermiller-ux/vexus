package project.data.websocket.service.huobi.connection;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;
import project.data.websocket.constant.Options;
import project.data.websocket.constant.enums.ConnectionStateEnum;
import project.data.websocket.service.huobi.parser.HuobiModelParser;
import project.data.websocket.utils.ConnectionFactory;
import project.data.websocket.utils.IdGenerator;
import project.data.websocket.utils.InternalUtils;
import project.data.websocket.utils.ResponseCallback;
import project.data.websocket.utils.WebSocketConnection;
import project.data.websocket.utils.WebSocketWatchDog;

public class HuobiWebSocketConnection extends WebSocketListener implements WebSocketConnection {

	private Logger log = LoggerFactory.getLogger(HuobiWebSocketConnection.class);

	public static final String HUOBI_TRADING_WEBSOCKET_PATH = "/ws/v1";
	
	public static final String HUOBI_TRADING_WEBSOCKET_V2_PATH = "/ws/v2";
	
	public static final String HUOBI_MARKET_WEBSOCKET_PATH = "/ws";

	public static final String AUTH_VERSION_V1 = "v1";

	private long lastReceivedTime;

	private WebSocket webSocket;

	private Request okhttpRequest;

	private ConnectionStateEnum state;

	private Long id;

	private List<String> commandList;

	private HuobiModelParser parser;

	private ResponseCallback callback;

	private boolean autoClose;

	private boolean authNeed;

	private Options options;

	private long delayInSecond;

	private String host;

	private String authVersion = AUTH_VERSION_V1;

	private HuobiWebSocketConnection() {
	}

	public static HuobiWebSocketConnection createAssetConnection(Options options, List<String> commandList,
			HuobiModelParser parser, ResponseCallback callback, Boolean autoClose) {

		return createConnection(options, commandList, parser, callback, autoClose, true);
	}

	public static HuobiWebSocketConnection createMarketConnection(Options options, List<String> commandList,
			HuobiModelParser parser, ResponseCallback callback, boolean autoClose) {
		return createConnection(options, commandList, parser, callback, autoClose, false);
	}

	public static HuobiWebSocketConnection createConnection(Options options, List<String> commandList,
			HuobiModelParser parser, ResponseCallback callback, Boolean autoClose, boolean authNeed) {
		return createConnection(options, commandList, parser, callback, autoClose, authNeed, AUTH_VERSION_V1);
	}

	public static HuobiWebSocketConnection createConnection(Options options, List<String> commandList,
			HuobiModelParser parser, ResponseCallback callback, Boolean autoClose, boolean authNeed,
			String authVersion) {

		HuobiWebSocketConnection connection = new HuobiWebSocketConnection();
		connection.setOptions(options);
		connection.setCommandList(commandList);
		connection.setParser(parser);
		connection.setCallback(callback);
		connection.setAuthNeed(authNeed);
		connection.setAutoClose(autoClose);
		connection.setId(IdGenerator.getNextId());
		connection.setAuthVersion(authVersion);

		// 创建websocket请求
		String url = options.getWebSocketHost() + HUOBI_MARKET_WEBSOCKET_PATH;
		Request request = new Request.Builder().url(url).build();
		connection.setOkhttpRequest(request);

		try {
			connection.setHost(new URL(options.getRestHost()).getHost());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

		// 开启链接
		connection.connect();
		return connection;
	}

	void connect() {
		if (state == ConnectionStateEnum.CONNECTED) {
			log.info("[Connection][" + this.getId() + "] Already connected");
			return;
		}
		log.info("[Connection][" + this.getId() + "] Connecting...");
		webSocket = ConnectionFactory.createWebSocket(okhttpRequest, this);
	}

	public void reConnect(int delayInSecond) {
		log.warn("[Sub][" + this.getId() + "] Reconnecting after " + delayInSecond + " seconds later");
		if (webSocket != null) {
			webSocket.cancel();
			webSocket = null;
		}
		this.delayInSecond = delayInSecond;
		state = ConnectionStateEnum.DELAY_CONNECT;
	}

	public void reConnect() {
		if (delayInSecond != 0) {
			delayInSecond--;
		} else {
			connect();
		}
	}

	public long getLastReceivedTime() {
		return this.lastReceivedTime;
	}

	void send(List<String> commandList) {
		if (commandList == null || commandList.size() <= 0) {
			return;
		}
		commandList.forEach(command -> {
			send(command);
		});
	}

	public void send(String str) {
		boolean result = false;
		log.info("[Connection Send]{}", str);
		if (webSocket != null) {
			result = webSocket.send(str);
		}
		if (!result) {
			log.error("[Connection Send][" + this.getId() + "] Failed to send message");
			closeOnError();
		}
	}

	@Override
	public void onMessage(WebSocket webSocket, String text) {
		super.onMessage(webSocket, text);
		lastReceivedTime = System.currentTimeMillis();

		log.debug("[On Message Text]:{}", text);
		try {
			JSONObject json = JSON.parseObject(text);

			if (json.containsKey("action")) {
				String action = json.getString("action");
				if ("ping".equals(action)) {
					processPingOnV2TradingLine(json, webSocket);
				} else if ("push".equals(action)) {
					onReceive(json);
				}
				if ("req".equals(action)) {
					String ch = json.getString("ch");
					if ("auth".equals(ch)) {
						send(commandList);
					}

				}

			}

		} catch (Exception e) {
			log.error("[On Message][{}]: catch exception:", this.getId(), e);
			closeOnError();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onMessage(WebSocket webSocket, ByteString bytes) {
		super.onMessage(webSocket, bytes);
		try {

			lastReceivedTime = System.currentTimeMillis();

			String data;
			try {
				data = new String(InternalUtils.decode(bytes.toByteArray()));
			} catch (IOException e) {
				log.error("[Connection On Message][" + this.getId() + "] Receive message error: " + e.getMessage());
				closeOnError();
				return;
			}
			log.debug("[Connection On Message][{}] {}", this.getId(), data);
			JSONObject jsonObject = JSON.parseObject(data);

			if (jsonObject.containsKey("status") && !"ok".equals(jsonObject.getString("status"))) {
				String errorCode = jsonObject.getString("err-code");
				String errorMsg = jsonObject.getString("err-msg");
				onError(errorCode + ": " + errorMsg, null);
				log.error("[Connection On Message][" + this.getId() + "] Got error from server: " + errorCode + "; "
						+ errorMsg);
				close();
			} else if (jsonObject.containsKey("op")) {
				String op = jsonObject.getString("op");
				if (op.equals("notify")) {
					onReceive(jsonObject);
				} else if (op.equals("ping")) {
					processPingOnTradingLine(jsonObject, webSocket);
				} else if (op.equals("auth")) {
					send(commandList);
				} else if (op.equals("req")) {
					onReceiveAndClose(jsonObject);
				}
			} else if (jsonObject.containsKey("ch") || jsonObject.containsKey("rep")) {
				onReceiveAndClose(jsonObject);
			} else if (jsonObject.containsKey("ping")) {
				processPingOnMarketLine(jsonObject, webSocket);
			} else if (jsonObject.containsKey("subbed")) {
			}
		} catch (Exception e) {
			log.error("[Connection On Message][" + this.getId() + "] Unexpected error: " + e.getMessage());
			closeOnError();
		}
	}

	private void onError(String errorMessage, Throwable e) {
		log.error("[Connection error][" + this.getId() + "] " + errorMessage,e);
		closeOnError();
	}

	private void onReceiveAndClose(JSONObject jsonObject) {
		onReceive(jsonObject);
		if (autoClose) {
			close();
		}
	}

	@SuppressWarnings("unchecked")
	private void onReceive(JSONObject jsonObject) {
		Object obj = null;
		try {
			obj = parser.parse(jsonObject);
		} catch (Exception e) {
			onError("Process error: " + e.getMessage() + " You should capture the exception in your error handler", e);
			return;
		}

		callback.onResponse(obj);
	}

	private void processPingOnTradingLine(JSONObject jsonObject, WebSocket webSocket) {
		long ts = jsonObject.getLong("ts");
		webSocket.send(String.format("{\"op\":\"pong\",\"ts\":%d}", ts));
	}

	private void processPingOnMarketLine(JSONObject jsonObject, WebSocket webSocket) {
		long ts = jsonObject.getLong("ping");
		webSocket.send(String.format("{\"pong\":%d}", ts));
	}

	private void processPingOnV2TradingLine(JSONObject jsonWrapper, WebSocket webSocket) {
		long ts = jsonWrapper.getJSONObject("data").getLong("ts");
		String pong = String.format("{\"action\": \"pong\",\"params\": {\"ts\": %d}}", ts);
		webSocket.send(pong);
	}

	public ConnectionStateEnum getState() {
		return state;
	}

	@Override
	public Long getConnectionId() {
		return this.getId();
	}

	public void close() {
		log.error("[Connection close][" + this.getId() + "] Closing normally");
		webSocket.cancel();
		webSocket = null;
		WebSocketWatchDog.onClosedNormally(this);
	}

	@Override
	public void onClosed(WebSocket webSocket, int code, String reason) {
		super.onClosed(webSocket, code, reason);
		if (state == ConnectionStateEnum.CONNECTED) {
			state = ConnectionStateEnum.IDLE;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onOpen(WebSocket webSocket, Response response) {
		super.onOpen(webSocket, response);
		this.webSocket = webSocket;
		log.info("[Connection][" + this.getId() + "] Connected to server");
		if (options.isWebSocketAutoConnect()) {
			WebSocketWatchDog.onConnectionCreated(this);
		}

		state = ConnectionStateEnum.CONNECTED;
		lastReceivedTime = System.currentTimeMillis();

		// 不需要验签的话，直接把命令发出去就好
		commandList.forEach(command -> {
			send(command);
		});
		
	}

	@Override
	public void onFailure(WebSocket webSocket, Throwable t, Response response) {
		if(null!=response) {
			try {
				response.close();
				ResponseBody rb=response.body();
				if(null!=rb) rb.close();
			}catch(Throwable e) {
				log.error("close body occur error:",e);
			}
		}
		
		onError("Unexpected error: " + t.getMessage(), t);
		closeOnError();
	}

	private void closeOnError() {
		if (webSocket != null) {
			this.webSocket.cancel();
			state = ConnectionStateEnum.CLOSED_ON_ERROR;
			log.error("[Connection error][" + this.getId() + "] Connection is closing due to error");
		}
	}

	public WebSocket getWebSocket() {
		return webSocket;
	}

	public void setWebSocket(WebSocket webSocket) {
		this.webSocket = webSocket;
	}

	public Request getOkhttpRequest() {
		return okhttpRequest;
	}

	public void setOkhttpRequest(Request okhttpRequest) {
		this.okhttpRequest = okhttpRequest;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public List<String> getCommandList() {
		return commandList;
	}

	public void setCommandList(List<String> commandList) {
		this.commandList = commandList;
	}

	public HuobiModelParser getParser() {
		return parser;
	}

	public void setParser(HuobiModelParser parser) {
		this.parser = parser;
	}

	public ResponseCallback getCallback() {
		return callback;
	}

	public void setCallback(ResponseCallback callback) {
		this.callback = callback;
	}

	public boolean isAutoClose() {
		return autoClose;
	}

	public void setAutoClose(boolean autoClose) {
		this.autoClose = autoClose;
	}

	public boolean isAuthNeed() {
		return authNeed;
	}

	public void setAuthNeed(boolean authNeed) {
		this.authNeed = authNeed;
	}

	public Options getOptions() {
		return options;
	}

	public void setOptions(Options options) {
		this.options = options;
	}

	public long getDelayInSecond() {
		return delayInSecond;
	}

	public void setDelayInSecond(long delayInSecond) {
		this.delayInSecond = delayInSecond;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public void setLastReceivedTime(long lastReceivedTime) {
		this.lastReceivedTime = lastReceivedTime;
	}

	public void setState(ConnectionStateEnum state) {
		this.state = state;
	}

	public String getAuthVersion() {
		return authVersion;
	}

	public void setAuthVersion(String authVersion) {
		this.authVersion = authVersion;
	}

	@Override
	public String toString() {
		return "HuobiWebSocketConnection [log=" + log + ", lastReceivedTime=" + lastReceivedTime + ", webSocket="
				+ webSocket + ", okhttpRequest=" + okhttpRequest + ", state=" + state + ", id=" + id + ", commandList="
				+ commandList + ", parser=" + parser + ", callback=" + callback + ", autoClose=" + autoClose
				+ ", authNeed=" + authNeed + ", options=" + options + ", delayInSecond=" + delayInSecond + ", host="
				+ host + ", authVersion=" + authVersion + "]";
	}

}
