package project.web.api;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import project.syspara.Syspara;
import project.syspara.SysparaService;

/**
 * US stock market data via Alpaca (IEX real-time feed by default).
 * Reads alpaca_key_id / alpaca_secret / alpaca_data_endpoint / alpaca_feed from t_syspara.
 */
@RestController
@CrossOrigin
public class AlpacaController {

    private static final Logger logger = LoggerFactory.getLogger(AlpacaController.class);

    private static volatile JSONObject cache = null;
    private static volatile long cacheAt = 0L;
    private static volatile String cacheSymbols = "";

    @Autowired
    private SysparaService sysparaService;

    @RequestMapping("/api/alpaca!snapshots.action")
    public Object snapshots(HttpServletRequest request) {
        JSONObject result = new JSONObject();
        try {
            String keyId = para("alpaca_key_id", "");
            String secret = para("alpaca_secret", "");
            if (keyId.length() == 0 || secret.length() == 0) {
                result.put("code", "1");
                result.put("msg", "Alpaca 行情未配置");
                return result;
            }
            String symbols = request.getParameter("symbols");
            if (symbols == null || symbols.trim().length() == 0) {
                symbols = "AAPL,TSLA,NVDA";
            }
            symbols = symbols.toUpperCase(Locale.ROOT).replace(" ", "");
            if (symbols.length() > 400 || !symbols.matches("[A-Z0-9.,-]+")) {
                result.put("code", "1");
                result.put("msg", "symbols invalid");
                return result;
            }
            long now = System.currentTimeMillis();
            if (cache != null && now - cacheAt < 2500L && cacheSymbols.equals(symbols)) {
                return cache;
            }
            String base = para("alpaca_data_endpoint", "https://data.alpaca.markets");
            if (base.endsWith("/")) {
                base = base.substring(0, base.length() - 1);
            }
            String feed = para("alpaca_feed", "iex");
            String url = base + "/v2/stocks/snapshots?symbols=" + symbols + "&feed=" + feed;
            String text = get(url, keyId, secret);
            JSONObject root = JSON.parseObject(text);
            if (root == null) {
                result.put("code", "1");
                result.put("msg", "Alpaca 返回异常");
                return result;
            }
            JSONArray list = new JSONArray();
            for (String sym : symbols.split(",")) {
                JSONObject snap = root.getJSONObject(sym);
                if (snap == null) {
                    continue;
                }
                JSONObject q = new JSONObject();
                q.put("symbol", sym);
                double price = 0d;
                JSONObject trade = snap.getJSONObject("latestTrade");
                if (trade != null && trade.getDoubleValue("p") > 0) {
                    price = trade.getDoubleValue("p");
                    q.put("time", trade.getString("t"));
                }
                if (price <= 0d) {
                    JSONObject quote = snap.getJSONObject("latestQuote");
                    if (quote != null) {
                        double ap = quote.getDoubleValue("ap");
                        double bp = quote.getDoubleValue("bp");
                        price = ap > 0 && bp > 0 ? (ap + bp) / 2d : Math.max(ap, bp);
                        q.put("time", quote.getString("t"));
                    }
                }
                double prevClose = 0d;
                JSONObject prev = snap.getJSONObject("prevDailyBar");
                if (prev != null) {
                    prevClose = prev.getDoubleValue("c");
                }
                JSONObject day = snap.getJSONObject("dailyBar");
                double open = day == null ? 0d : day.getDoubleValue("o");
                double high = day == null ? 0d : day.getDoubleValue("h");
                double low = day == null ? 0d : day.getDoubleValue("l");
                double change = price - prevClose;
                double pct = prevClose > 0d ? (change / prevClose) * 100d : 0d;
                q.put("price", price);
                q.put("prevClose", prevClose);
                q.put("open", open);
                q.put("high", high);
                q.put("low", low);
                q.put("change", change);
                q.put("changePercent", pct);
                q.put("source", "alpaca-" + feed);
                list.add(q);
            }
            JSONObject data = new JSONObject();
            data.put("list", list);
            data.put("feed", feed);
            data.put("updated", new SimpleDateFormat("HH:mm:ss").format(new Date()));
            result.put("code", "0");
            result.put("msg", null);
            result.put("data", data);
            cache = result;
            cacheAt = now;
            cacheSymbols = symbols;
        } catch (Throwable t) {
            logger.error("alpaca snapshots error", t);
            result.put("code", "1");
            result.put("msg", "Alpaca 行情暂时不可用");
        }
        return result;
    }

    private String para(String code, String def) {
        try {
            Syspara sp = sysparaService.find(code);
            if (sp == null || sp.getValue() == null || sp.getValue().trim().length() == 0) {
                return def;
            }
            return sp.getValue().trim();
        } catch (Throwable t) {
            return def;
        }
    }

    private String get(String url, String keyId, String secret) throws Exception {
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(8000);
            conn.setReadTimeout(15000);
            conn.setRequestProperty("APCA-API-KEY-ID", keyId);
            conn.setRequestProperty("APCA-API-SECRET-KEY", secret);
            conn.setRequestProperty("User-Agent", "Vexus/1.0");
            int status = conn.getResponseCode();
            InputStream is = status >= 400 ? conn.getErrorStream() : conn.getInputStream();
            if (is == null) {
                throw new RuntimeException("alpaca http " + status);
            }
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buf = new byte[8192];
            int n;
            while ((n = is.read(buf)) > 0) {
                bos.write(buf, 0, n);
            }
            is.close();
            return new String(bos.toByteArray(), "UTF-8");
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }
}
