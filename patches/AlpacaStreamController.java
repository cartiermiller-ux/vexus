package project.web.api;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import project.syspara.Syspara;
import project.syspara.SysparaService;

/**
 * SSE endpoint backed by one shared Alpaca IEX WebSocket (see AlpacaStreamHub).
 */
@RestController
@CrossOrigin
public class AlpacaStreamController {

    @Autowired
    private SysparaService sysparaService;

    @RequestMapping("/api/alpaca!stream.action")
    public void stream(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/event-stream;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache, no-transform");
        response.setHeader("X-Accel-Buffering", "no");

        PrintWriter out = response.getWriter();
        String keyId = para("alpaca_key_id", "");
        String secret = para("alpaca_secret", "");
        if (keyId.length() == 0 || secret.length() == 0) {
            AlpacaStreamHub.Packet.write(out, "status", "{\"status\":\"not_configured\"}");
            return;
        }

        String symbols = request.getParameter("symbols");
        if (symbols == null || symbols.trim().length() == 0) {
            symbols = "AAPL,TSLA,NVDA";
        }
        symbols = symbols.toUpperCase().replace(" ", "");
        if (symbols.length() > 400 || !symbols.matches("[A-Z0-9.,-]+")) {
            symbols = "AAPL,TSLA,NVDA";
        }
        String feed = para("alpaca_feed", "iex");

        AlpacaStreamHub hub = AlpacaStreamHub.getInstance();
        hub.subscribe(out, symbols, keyId, secret, feed);
        try {
            while (!out.checkError()) {
                try {
                    Thread.sleep(500L);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        } finally {
            hub.unsubscribe(out);
        }
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
}
