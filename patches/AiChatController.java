package project.web.api;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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

import kernel.web.ResultObject;
import project.syspara.Syspara;
import project.syspara.SysparaService;

/**
 * 交易所 AI 客服接口（后端代理 DeepSeek，API Key 不下发前端）。
 *
 * 前端：POST /api/ai!chat.action
 *   message: 用户当前问题
 *   history: 可选 JSON 字符串 [{"role":"user","content":"..."},{"role":"assistant","content":"..."}]
 */
@RestController
@CrossOrigin
public class AiChatController {

    private static final Logger logger = LoggerFactory.getLogger(AiChatController.class);

    private static final Map<String, List<Long>> RATE = new ConcurrentHashMap<String, List<Long>>();

    @Autowired
    private SysparaService sysparaService;

    @RequestMapping("/api/ai!chat.action")
    public Object chat(HttpServletRequest request) {
        ResultObject result = new ResultObject();
        try {
            String enabled = para("ai_assistant_enabled", "true");
            if (!"true".equalsIgnoreCase(enabled) && !"1".equals(enabled) && !"on".equalsIgnoreCase(enabled)) {
                result.setCode("1");
                result.setMsg("AI 助手暂未开启");
                return result;
            }

            String message = request.getParameter("message");
            if (message == null || message.trim().length() == 0) {
                result.setCode("1");
                result.setMsg("请输入问题");
                return result;
            }
            message = message.trim();
            if (message.length() > 2000) {
                message = message.substring(0, 2000);
            }

            String ip = clientIp(request);
            if (!allow(ip)) {
                result.setCode("1");
                result.setMsg("提问太快了，请稍后再试");
                return result;
            }

            String apiKey = para("deepseek_api_key", "");
            if (apiKey == null || apiKey.trim().length() == 0) {
                result.setCode("1");
                result.setMsg("AI 客服尚未配置，请联系管理员");
                return result;
            }
            String baseUrl = para("deepseek_base_url", "https://api.deepseek.com");
            if (baseUrl.endsWith("/")) {
                baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
            }
            String model = para("deepseek_model", "deepseek-flash");
            String systemPrompt = para("ai_system_prompt", "");
            if (systemPrompt == null || systemPrompt.trim().length() == 0) {
                systemPrompt = defaultPrompt();
            }

            JSONArray messages = new JSONArray();
            JSONObject system = new JSONObject();
            system.put("role", "system");
            system.put("content", systemPrompt);
            messages.add(system);

            String history = request.getParameter("history");
            if (history != null && history.trim().length() > 0) {
                try {
                    JSONArray arr = JSON.parseArray(history);
                    int start = Math.max(0, arr.size() - 8);
                    for (int i = start; i < arr.size(); i++) {
                        JSONObject m = arr.getJSONObject(i);
                        if (m == null) {
                            continue;
                        }
                        String role = m.getString("role");
                        String content = m.getString("content");
                        if (content == null || content.trim().length() == 0) {
                            continue;
                        }
                        if (!"user".equals(role) && !"assistant".equals(role)) {
                            continue;
                        }
                        if (content.length() > 1200) {
                            content = content.substring(0, 1200);
                        }
                        JSONObject item = new JSONObject();
                        item.put("role", role);
                        item.put("content", content);
                        messages.add(item);
                    }
                } catch (Throwable ignore) {
                    // ignore malformed history
                }
            }
            JSONObject user = new JSONObject();
            user.put("role", "user");
            user.put("content", message);
            messages.add(user);

            JSONObject body = new JSONObject();
            body.put("model", model);
            body.put("messages", messages);
            body.put("stream", false);
            body.put("temperature", 0.4);
            body.put("max_tokens", 800);

            String url = baseUrl + "/chat/completions";
            String resp = postJson(url, "Bearer " + apiKey, body.toJSONString());
            JSONObject json = JSON.parseObject(resp);
            if (json == null) {
                result.setCode("1");
                result.setMsg("AI 服务返回异常，请稍后再试");
                return result;
            }
            if (json.containsKey("error")) {
                JSONObject err = json.getJSONObject("error");
                logger.warn("deepseek api error: {}", err == null ? json.getString("error") : err.getString("message"));
                result.setCode("1");
                result.setMsg("AI 服务暂时不可用，请稍后再试");
                return result;
            }
            JSONArray choices = json.getJSONArray("choices");
            if (choices == null || choices.size() == 0) {
                result.setCode("1");
                result.setMsg("AI 没有返回内容，请换个问法");
                return result;
            }
            JSONObject choice = choices.getJSONObject(0);
            JSONObject msg = choice.getJSONObject("message");
            String reply = msg == null ? null : msg.getString("content");
            if (reply == null || reply.trim().length() == 0) {
                result.setCode("1");
                result.setMsg("AI 没有返回内容，请换个问法");
                return result;
            }
            JSONObject data = new JSONObject();
            data.put("reply", reply);
            result.setData(data);
        } catch (Throwable t) {
            logger.error("ai chat error", t);
            result.setCode("1");
            result.setMsg("AI 服务暂时不可用，请稍后再试");
        }
        return result;
    }

    private String para(String code, String def) {
        try {
            Syspara sp = sysparaService.find(code);
            if (sp == null || sp.getValue() == null) {
                return def;
            }
            return sp.getValue();
        } catch (Throwable t) {
            return def;
        }
    }

    private boolean allow(String ip) {
        long now = System.currentTimeMillis();
        String key = (ip == null ? "unknown" : ip) + "|" + (now / 60000);
        List<Long> list = RATE.get(key);
        if (list == null) {
            list = new ArrayList<Long>();
            RATE.put(key, list);
            if (RATE.size() > 5000) {
                // prevent unbounded growth on very busy hosts
                RATE.clear();
            }
        }
        synchronized (list) {
            if (list.size() >= 30) {
                return false;
            }
            list.add(now);
            return true;
        }
    }

    private String clientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && ip.length() > 0) {
            int comma = ip.indexOf(',');
            if (comma > 0) {
                ip = ip.substring(0, comma);
            }
            return ip.trim();
        }
        ip = request.getHeader("X-Real-IP");
        if (ip != null && ip.length() > 0) {
            return ip.trim();
        }
        return request.getRemoteAddr();
    }

    private String defaultPrompt() {
        return "你是 Vexus 加密货币交易所的 AI 客服助手。请使用用户提问所用的语言回答。"
                + "你的职责：帮助用户了解注册/登录、KYC 认证、充值、提现、现货交易、合约交易、闪兑、手续费、安全设置（谷歌验证/资金密码）等平台功能。"
                + "回答要简洁、分点、可操作；不确定的内容不要编造，建议用户联系人工客服。"
                + "严禁索要或讨论私钥、助记词、登录密码、资金密码、谷歌验证码；"
                + "不提供投资建议、收益承诺或价格预测；不引导用户向站外地址转账。";
    }

    private String postJson(String url, String authorization, String body) throws Exception {
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(60000);
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            conn.setRequestProperty("Authorization", authorization);
            byte[] data = body.getBytes("UTF-8");
            conn.setRequestProperty("Content-Length", String.valueOf(data.length));
            OutputStream os = conn.getOutputStream();
            os.write(data);
            os.flush();
            os.close();

            int status = conn.getResponseCode();
            InputStream is = status >= 200 && status < 300 ? conn.getInputStream() : conn.getErrorStream();
            String text = readAll(is);
            if (status < 200 || status >= 300) {
                logger.warn("deepseek http status={}, body={}", status, text);
                return "{\"error\":{\"message\":\"http status " + status + "\"}}";
            }
            return text;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private String readAll(InputStream is) throws Exception {
        if (is == null) {
            return "";
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        int n;
        while ((n = is.read(buf)) > 0) {
            bos.write(buf, 0, n);
        }
        is.close();
        return new String(bos.toByteArray(), "UTF-8");
    }
}
