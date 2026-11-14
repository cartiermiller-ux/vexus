//package project.web.data.socketio;
//
//import com.corundumstudio.socketio.SocketConfig;
//import com.corundumstudio.socketio.SocketIOServer;
//
//public class SocketIOConfig {
//
//	private String host;
//	
//	private int port;
//	
//	public SocketIOServer socketIOServer() {
//		SocketConfig socketConfig = new SocketConfig();
//		socketConfig.setTcpNoDelay(true);
//		socketConfig.setSoLinger(0);
//		com.corundumstudio.socketio.Configuration config = new com.corundumstudio.socketio.Configuration();
//		config.setSocketConfig(socketConfig);
//		config.setHostname(host);
//		config.setPort(port);
//		return new SocketIOServer(config);
//	}
//
//	public String getHost() {
//		return host;
//	}
//
//	public void setHost(String host) {
//		this.host = host;
//	}
//
//	public int getPort() {
//		return port;
//	}
//
//	public void setPort(int port) {
//		this.port = port;
//	}
//
//}
