package project.web.api;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

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
 * 股票/指数行情（腾讯行情公开接口，服务端代理，浏览器无 CORS 问题）。
 * 系统参数 stock_symbols 可配置，逗号分隔，例如：
 * sh000001,sz399001,sz399006,sh600519,sz000858,sh601318,hk00700,usAAPL
 */
@RestController
@CrossOrigin
public class StockController {

    private static final Logger logger = LoggerFactory.getLogger(StockController.class);

    private static volatile JSONObject cache = null;
    private static volatile long cacheAt = 0L;

    @Autowired
    private SysparaService sysparaService;

    @RequestMapping("/api/stock!list.action")
    public Object list(HttpServletRequest request) {
        JSONObject result = new JSONObject();
        try {
            long now = System.currentTimeMillis();
            if (cache != null && now - cacheAt < 8000L) {
                return cache;
            }
            String codes = para("stock_symbols",
                    "sh000001,sz399001,sz399006,sh000300,sh600519,sz000858,sh601318,sz300750,hk00700,usAAPL,usTSLA,usNVDA");
            codes = codes.replace(" ", "");
            if (codes.length() > 600) {
                codes = codes.substring(0, 600);
            }
            String text = fetch(codes);
            JSONArray list = new JSONArray();
            String[] lines = text.split(";");
            for (String line : lines) {
                int a = line.indexOf("=\"");
                int b = line.lastIndexOf("\"");
                if (a < 0 || b <= a + 1) {
                    continue;
                }
                String payload = line.substring(a + 2, b);
                String[] f = payload.split("~", -1);
                if (f.length < 35 || f[1] == null || f[1].length() == 0) {
                    continue;
                }
                String var = line.substring(0, a).trim();
                if (var.startsWith("v_")) {
                    var = var.substring(2);
                }
                JSONObject item = new JSONObject();
                item.put("market", var);
                item.put("name", f[1]);
                item.put("code", f[2]);
                item.put("price", num(f[3]));
                item.put("prevClose", num(f[4]));
                item.put("open", num(f[5]));
                item.put("time", f[30]);
                item.put("change", num(f[31]));
                item.put("changePercent", num(f[32]));
                item.put("high", num(f[33]));
                item.put("low", num(f[34]));
                if (f.length > 36) {
                    item.put("volume", num(f[36]));
                }
                String c = f[2] == null ? "" : f[2];
                if (c.startsWith("000") || c.startsWith("399") || c.startsWith("sh000") || c.startsWith("sz399")) {
                    item.put("type", "index");
                } else if (c.startsWith("sh6") || c.startsWith("sz0") || c.startsWith("sz3")) {
                    item.put("type", "stock");
                } else {
                    item.put("type", "global");
                }
                list.add(item);
            }
            JSONObject data = new JSONObject();
            data.put("list", list);
            data.put("updated", new SimpleDateFormat("HH:mm:ss").format(new Date()));
            data.put("source", "tencent");
            result.put("code", "0");
            result.put("msg", null);
            result.put("data", data);
            cache = result;
            cacheAt = now;
        } catch (Throwable t) {
            logger.error("stock list error", t);
            result.put("code", "1");
            result.put("msg", "行情服务暂时不可用");
        }
        return result;
    }

    @RequestMapping("/api/stock!kline.action")
    public Object kline(HttpServletRequest request) {
        JSONObject result = new JSONObject();
        try {
            String symbol = request.getParameter("symbol");
            if (symbol == null || symbol.length() == 0 || symbol.length() > 24 || !symbol.matches("[A-Za-z0-9._-]+")) {
                result.put("code", "1");
                result.put("msg", "symbol invalid");
                return result;
            }
            String url = "https://web.ifzq.gtimg.cn/appstock/app/fqkline/get?param=" + symbol + ",day,,,160,qfq";
            String text = fetchUtf8(url);
            JSONObject root = JSON.parseObject(text);
            JSONObject data = root == null ? null : root.getJSONObject("data");
            JSONObject node = data == null ? null : data.getJSONObject(symbol);
            JSONArray arr = null;
            if (node != null) {
                arr = node.getJSONArray("qfqday");
                if (arr == null) {
                    arr = node.getJSONArray("day");
                }
            }
            if (arr == null) {
                result.put("code", "1");
                result.put("msg", "no kline data");
                return result;
            }
            JSONArray list = new JSONArray();
            for (int i = 0; i < arr.size(); i++) {
                JSONArray row = arr.getJSONArray(i);
                if (row == null || row.size() < 6) {
                    continue;
                }
                JSONObject k = new JSONObject();
                k.put("date", row.getString(0));
                k.put("open", num(row.getString(1)));
                k.put("close", num(row.getString(2)));
                k.put("high", num(row.getString(3)));
                k.put("low", num(row.getString(4)));
                k.put("volume", num(row.getString(5)));
                list.add(k);
            }
            JSONObject out = new JSONObject();
            out.put("list", list);
            out.put("symbol", symbol);
            result.put("code", "0");
            result.put("msg", null);
            result.put("data", out);
        } catch (Throwable t) {
            logger.error("stock kline error", t);
            result.put("code", "1");
            result.put("msg", "K线服务暂时不可用");
        }
        return result;
    }

    private String para(String code, String def) {
        try {
            Syspara sp = sysparaService.find(code);
            if (sp == null || sp.getValue() == null || sp.getValue().trim().length() == 0) {
                return def;
            }
            return sp.getValue();
        } catch (Throwable t) {
            return def;
        }
    }

    private double num(String s) {
        try {
            return Double.parseDouble(s);
        } catch (Throwable t) {
            return 0d;
        }
    }

    private String fetchUtf8(String url) throws Exception {
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(8000);
            conn.setReadTimeout(15000);
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");
            conn.setRequestProperty("Referer", "https://gu.qq.com/");
            int status = conn.getResponseCode();
            if (status != 200) {
                throw new RuntimeException("kline http status " + status);
            }
            InputStream is = conn.getInputStream();
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

    private String fetch(String codes) throws Exception {
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) new URL("https://qt.gtimg.cn/q=" + codes).openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(8000);
            conn.setReadTimeout(12000);
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");
            conn.setRequestProperty("Referer", "https://gu.qq.com/");
            int status = conn.getResponseCode();
            if (status != 200) {
                throw new RuntimeException("stock http status " + status);
            }
            InputStream is = conn.getInputStream();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buf = new byte[8192];
            int n;
            while ((n = is.read(buf)) > 0) {
                bos.write(buf, 0, n);
            }
            is.close();
            return new String(bos.toByteArray(), "GBK");
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }
}
