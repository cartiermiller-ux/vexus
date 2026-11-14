package project.web.api;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import kernel.web.ApplicationUtil;
import project.syspara.Syspara;
import project.syspara.SysparaService;

/**
 * Shared DeepSeek reply helper used by the legacy online-chat controllers.
 * Reads AI config from t_syspara; returns null when AI is disabled/not configured.
 */
public final class AiSupport {

    private static final Logger logger = LoggerFactory.getLogger(AiSupport.class);

    private AiSupport() {
    }

    public static String reply(String message) {
        try {
            if (message == null || message.trim().length() == 0) {
                return null;
            }
            message = message.trim();
            if (message.length() > 2000) {
                message = message.substring(0, 2000);
            }
            SysparaService sysparaService = ApplicationUtil.getBean(SysparaService.class);
            if (sysparaService == null) {
                return null;
            }
            String enabled = value(sysparaService, "ai_assistant_enabled", "true");
            if (!"true".equalsIgnoreCase(enabled) && !"1".equals(enabled) && !"on".equalsIgnoreCase(enabled)) {
                return null;
            }
            String apiKey = value(sysparaService, "deepseek_api_key", "");
            if (apiKey == null || apiKey.trim().length() == 0) {
                return null;
            }
            String baseUrl = value(sysparaService, "deepseek_base_url", "https://api.deepseek.com");
            if (baseUrl.endsWith("/")) {
                baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
            }
            String model = value(sysparaService, "deepseek_model", "deepseek-flash");
            String systemPrompt = value(sysparaService, "ai_system_prompt", "");
            if (systemPrompt == null || systemPrompt.trim().length() == 0) {
                systemPrompt = "你是 Vexus 加密货币交易所的在线客服助手。请用简体中文、简洁、礼貌地回答用户关于注册、KYC、充值、提现、现货/合约交易、手续费和账户安全的问题。不要承诺收益，不要索要私钥、助记词、密码或验证码。涉及具体订单/资产时提醒用户联系人工客服核实。";
            }

            JSONArray messages = new JSONArray();
            JSONObject system = new JSONObject();
            system.put("role", "system");
            system.put("content", systemPrompt);
            messages.add(system);
            JSONObject user = new JSONObject();
            user.put("role", "user");
            user.put("content", message);
            messages.add(user);

            JSONObject body = new JSONObject();
            body.put("model", model);
            body.put("messages", messages);
            body.put("stream", false);
            body.put("temperature", 0.4);
            body.put("max_tokens", 600);

            String resp = postJson(baseUrl + "/chat/completions", "Bearer " + apiKey, body.toJSONString());
            JSONObject json = JSON.parseObject(resp);
            if (json == null || json.containsKey("error")) {
                return null;
            }
            JSONArray choices = json.getJSONArray("choices");
            if (choices == null || choices.size() == 0) {
                return null;
            }
            JSONObject choice = choices.getJSONObject(0);
            JSONObject msg = choice == null ? null : choice.getJSONObject("message");
            String reply = msg == null ? null : msg.getString("content");
            if (reply == null || reply.trim().length() == 0) {
                return null;
            }
            return reply.trim();
        } catch (Throwable t) {
            logger.error("AiSupport reply error", t);
            return null;
        }
    }

    private static String value(SysparaService sysparaService, String code, String def) {
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

    private static String postJson(String url, String auth, String body) throws Exception {
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(30000);
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", auth);
            byte[] bytes = body.getBytes("UTF-8");
            OutputStream os = conn.getOutputStream();
            os.write(bytes);
            os.flush();
            os.close();
            int status = conn.getResponseCode();
            InputStream is = status >= 400 ? conn.getErrorStream() : conn.getInputStream();
            if (is == null) {
                return null;
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
