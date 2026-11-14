//package project.web.data.socketio;
//
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.Map;
//import java.util.Set;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.DisposableBean;
//import org.springframework.beans.factory.InitializingBean;
//
//import com.corundumstudio.socketio.SocketIOClient;
//import com.corundumstudio.socketio.SocketIOServer;
//
//public class SocketIOService implements InitializingBean, DisposableBean {
//	private static final Logger logger = LoggerFactory.getLogger(SocketIOService.class);
//	
//	private SocketIOConfig SocketIOConfig;
//    private SocketIOServer socketIOServer;
//    
//    // 每个主题的订阅用户 topic --> set of clients
//    private Map<String, Set<SocketIOClient>> topicSubscribers = new HashMap<>();
//    
//    // 每个主题的最新消息 topic --> latest content
//    private Map<String, String> topicContent = new HashMap<>();
//   
//    @Override
//    public void afterPropertiesSet() {
//    	socketIOServer = SocketIOConfig.socketIOServer();
//    	
//        // 与前端建立连接
//    	socketIOServer.addConnectListener(client -> {
//            if (null != client.getHandshakeData().getSingleUrlParam("user") 
//            		&& null != client.getHandshakeData().getSingleUrlParam("subscriptions")) {
//            	logger.info("user " + client.getHandshakeData().getSingleUrlParam("user") + " connected to socketio");
//                String subscriptions = client.getHandshakeData().getSingleUrlParam("subscriptions");
//                for (String topic : subscriptions.split(",")) {
//                    if (topicSubscribers.containsKey(topic)) {
//                        topicSubscribers.get(topic).add(client);
//                    } else {
//                        Set<SocketIOClient> s = new HashSet<>();
//                        s.add(client);
//                        topicSubscribers.put(topic, s);
//                    }
//                    if (topicContent.containsKey(topic)) {
//                        client.sendEvent("publish", topic, topicContent.get(topic));
//                    }
//                }
//            }
//        });
//        // 与前端断开连接
//    	socketIOServer.addDisconnectListener(client -> {
//    		if (null != client.getHandshakeData().getSingleUrlParam("user") 
//            		&& null != client.getHandshakeData().getSingleUrlParam("subscriptions")) {
//                logger.info("user " + client.getHandshakeData().getSingleUrlParam("user") + " disconnected to socketio");
//                String subscriptions = client.getHandshakeData().getSingleUrlParam("subscriptions");
//                for (String topic : subscriptions.split(",")) {
//                    topicSubscribers.get(topic).remove(client);
//                }
//    		}
//        });
//    	socketIOServer.start();
//        logger.info("socketio started on port " + socketIOServer.getConfiguration().getPort());
//    }
//
//    @Override
//    public void destroy() {
//        if (null != socketIOServer) {
//        	socketIOServer.stop();
//            logger.info("socketio stopped");
//        }
//    }
//
//    public void publish(String topic, String content) {
//    	System.out.println("topic: " + topic);
//    	System.out.println("content: " + content);
//        topicContent.put(topic, content);
//        System.out.println("11111111111111111111111111111" + topicSubscribers);
//        if (topicSubscribers.containsKey(topic)) {
//            for (SocketIOClient client : topicSubscribers.get(topic)) {
//            	System.out.println("22222222222222222222222222222" + client);
//                client.sendEvent("publish", topic, content);
//            }
//        }
//    }
//
//	public void setSocketIOServer(SocketIOServer socketIOServer) {
//		this.socketIOServer = socketIOServer;
//	}
//
//	public void setSocketIOConfig(SocketIOConfig socketIOConfig) {
//		SocketIOConfig = socketIOConfig;
//	}
//	
//}
