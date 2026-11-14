package project.web.api;

import java.io.PrintWriter;
import java.net.URI;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * Single shared Alpaca IEX WebSocket connection fan-out to all SSE clients.
 * Alpaca's free plan allows only one concurrent stream, so the server must not
 * open one connection per browser tab.
 */
public final class AlpacaStreamHub {

    private static final Logger logger = LoggerFactory.getLogger(AlpacaStreamHub.class);
    private static final AlpacaStreamHub INSTANCE = new AlpacaStreamHub();

    private final Set<PrintWriter> clients = Collections.synchronizedSet(new HashSet<PrintWriter>());
    private WebSocketClient client;
    private String symbols = "";
    private String keyId = "";
    private String secret = "";
    private String wsUrl = "";
    private boolean authenticated = false;

    private AlpacaStreamHub() {
    }

    public static AlpacaStreamHub getInstance() {
        return INSTANCE;
    }

    public synchronized void subscribe(PrintWriter out, String requestedSymbols, String key, String sec, String feed) {
        clients.add(out);
        this.keyId = key;
        this.secret = sec;
        this.wsUrl = "wss://stream.data.alpaca.markets/v2/" + ("sip".equals(feed) ? "sip" : "iex");
        String merged = mergeSymbols(this.symbols, requestedSymbols);
        this.symbols = merged;
        try {
            ensureClient();
            if (authenticated) {
                subscribeSymbols();
            }
            write(out, "status", "{\"status\":\"connecting\"}");
        } catch (Throwable t) {
            logger.error("alpaca hub connect error", t);
            write(out, "status", "{\"status\":\"error\"}");
        }
    }

    public synchronized void unsubscribe(PrintWriter out) {
        clients.remove(out);
        if (clients.isEmpty()) {
            closeClient();
        }
    }

    private void ensureClient() throws Exception {
        if (client != null && client.isOpen()) {
            return;
        }
        authenticated = false;
        client = new WebSocketClient(new URI(wsUrl)) {
            @Override
            public void onOpen(ServerHandshake handshake) {
                JSONObject auth = new JSONObject();
                auth.put("action", "auth");
                auth.put("key", keyId);
                auth.put("secret", secret);
                send(auth.toJSONString());
            }

            @Override
            public void onMessage(String message) {
                handleMessage(message);
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                authenticated = false;
                broadcast("status", "{\"status\":\"closed\"}");
                client = null;
                maybeReconnect();
            }

            @Override
            public void onError(Exception ex) {
                authenticated = false;
                logger.warn("alpaca hub error: {}", ex == null ? "" : ex.getMessage());
                broadcast("status", "{\"status\":\"error\"}");
            }
        };
        client.connectBlocking(10, TimeUnit.SECONDS);
    }

    private synchronized void subscribeSymbols() {
        if (client == null || !client.isOpen() || !authenticated) {
            return;
        }
        try {
            JSONObject sub = new JSONObject();
            sub.put("action", "subscribe");
            sub.put("trades", JSON.parseArray(JSON.toJSONString(java.util.Arrays.asList(symbols.split(",")))));
            sub.put("quotes", JSON.parseArray(JSON.toJSONString(java.util.Arrays.asList(symbols.split(",")))));
            client.send(sub.toJSONString());
        } catch (Throwable t) {
            logger.warn("alpaca subscribe error", t);
        }
    }

    private synchronized void handleMessage(String message) {
        try {
            JSONArray arr = JSON.parseArray(message);
            if (arr == null) {
                return;
            }
            boolean needSubscribe = false;
            for (int i = 0; i < arr.size(); i++) {
                JSONObject m = arr.getJSONObject(i);
                if (m == null) {
                    continue;
                }
                String type = m.getString("T");
                if ("success".equals(type)) {
                    String msg = m.getString("msg");
                    if ("authenticated".equals(msg)) {
                        authenticated = true;
                        needSubscribe = true;
                    } else if ("subscribed".equals(msg)) {
                        broadcast("status", "{\"status\":\"live\"}");
                    }
                } else if ("error".equals(type)) {
                    broadcast("status", "{\"status\":\"error\",\"msg\":" + JSON.toJSONString(m.getString("msg")) + "}");
                } else if ("t".equals(type)) {
                    JSONObject data = new JSONObject();
                    data.put("symbol", m.getString("S"));
                    data.put("price", m.getDoubleValue("p"));
                    data.put("time", m.getString("t"));
                    broadcast("trade", data.toJSONString());
                } else if ("q".equals(type)) {
                    JSONObject data = new JSONObject();
                    data.put("symbol", m.getString("S"));
                    data.put("bid", m.getDoubleValue("bp"));
                    data.put("ask", m.getDoubleValue("ap"));
                    data.put("time", m.getString("t"));
                    broadcast("quote", data.toJSONString());
                }
            }
            if (needSubscribe) {
                subscribeSymbols();
            }
        } catch (Throwable t) {
            logger.warn("alpaca hub parse error", t);
        }
    }

    private void maybeReconnect() {
        if (clients.isEmpty()) {
            return;
        }
        new Thread(new Runnable() {
            public void run() {
                try {
                    Thread.sleep(3000L);
                    synchronized (AlpacaStreamHub.this) {
                        if (!clients.isEmpty()) {
                            ensureClient();
                        }
                    }
                } catch (Throwable t) {
                    logger.warn("alpaca reconnect error", t);
                }
            }
        }).start();
    }

    private void closeClient() {
        authenticated = false;
        if (client != null) {
            try {
                client.close();
            } catch (Throwable ignore) {
            }
            client = null;
        }
    }

    private void broadcast(String event, String data) {
        PrintWriter[] arr;
        synchronized (clients) {
            arr = clients.toArray(new PrintWriter[0]);
        }
        for (int i = 0; i < arr.length; i++) {
            PrintWriter w = arr[i];
            try {
                Packet.write(w, event, data);
            } catch (Throwable t) {
                clients.remove(w);
            }
        }
    }

    private void write(PrintWriter out, String event, String data) {
        try {
            Packet.write(out, event, data);
        } catch (Throwable ignore) {
        }
    }

    private String mergeSymbols(String existing, String requested) {
        Set<String> set = new HashSet<String>();
        if (existing != null) {
            for (String s : existing.split(",")) {
                if (s.trim().length() > 0) {
                    set.add(s.trim());
                }
            }
        }
        if (requested != null) {
            for (String s : requested.toUpperCase().split(",")) {
                if (s.trim().length() > 0) {
                    set.add(s.trim());
                }
            }
        }
        return String.join(",", set);
    }

    static final class Packet {
        static void write(PrintWriter out, String event, String data) {
            synchronized (out) {
                out.write("event: " + event + "\n");
                out.write("data: " + data + "\n\n");
                out.flush();
            }
        }
    }
}
