<%@ page language="java" pageEncoding="utf-8" import="java.util.*,java.text.*" %>
<%@ page import="kernel.web.ApplicationUtil" %>
<%@ page import="org.springframework.jdbc.core.JdbcTemplate" %>
<%@ page import="com.alibaba.fastjson.JSON" %>
<%@ page import="com.alibaba.fastjson.JSONArray" %>
<%@ page import="com.alibaba.fastjson.JSONObject" %>
<%@ page import="project.user.token.TokenService" %>
<%@ page import="project.party.PartyService" %>
<%@ page import="project.party.model.Party" %>
<%
    response.setContentType("application/json;charset=UTF-8");
    response.setHeader("Cache-Control", "no-store");
    JSONObject result = new JSONObject();
    try {
        String action = request.getParameter("action");
        String token = request.getParameter("token");
        if (action == null) action = "list";
        TokenService tokenService = ApplicationUtil.getBean(TokenService.class);
        String partyId = tokenService == null || token == null ? null : tokenService.cacheGet(token);
        if (partyId == null || partyId.trim().isEmpty()) {
            result.put("code", "401");
            result.put("msg", "请先登录");
            out.print(result.toJSONString());
            return;
        }
        PartyService partyService = ApplicationUtil.getBean(PartyService.class);
        Party party = partyService.cachePartyBy(partyId, false);
        String username = party == null ? "" : party.getUsername();
        String usercode = party == null ? "" : party.getUsercode();
        JdbcTemplate jdbc = ApplicationUtil.getBean(JdbcTemplate.class);

        if ("list".equals(action)) {
            List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT UUID,PAIR,SIDE,QTY,LEVERAGE,OPEN_PRICE,CLOSE_PRICE,PNL,STATUS,TAKE_PROFIT,STOP_LOSS,CREATE_TIME,CLOSE_TIME FROM T_FOREX_PAPER_ORDER WHERE PARTY_ID=? AND STATUS='open' ORDER BY CREATE_TIME DESC",
                new Object[] { partyId });
            JSONArray data = new JSONArray();
            for (Map<String, Object> row : rows) {
                JSONObject item = new JSONObject();
                item.put("id", row.get("UUID"));
                item.put("pair", row.get("PAIR"));
                item.put("side", row.get("SIDE"));
                item.put("qty", row.get("QTY"));
                item.put("leverage", row.get("LEVERAGE"));
                item.put("open", row.get("OPEN_PRICE"));
                item.put("tp", row.get("TAKE_PROFIT"));
                item.put("sl", row.get("STOP_LOSS"));
                item.put("status", row.get("STATUS"));
                item.put("createTime", row.get("CREATE_TIME") == null ? "" : row.get("CREATE_TIME").toString());
                data.add(item);
            }
            result.put("code", "0");
            result.put("data", data);
            result.put("username", username);
            result.put("uid", usercode);
        } else if ("open".equals(action)) {
            String pair = request.getParameter("pair");
            String side = request.getParameter("side");
            double qty = parseDouble(request.getParameter("qty"), 0);
            int leverage = (int) parseDouble(request.getParameter("leverage"), 1);
            double price = parseDouble(request.getParameter("price"), 0);
            double tp = parseDouble(request.getParameter("tp"), 0);
            double sl = parseDouble(request.getParameter("sl"), 0);
            if (pair == null || !pair.matches("[A-Za-z0-9]{3,16}") || qty <= 0 || price <= 0 || !("buy".equals(side) || "sell".equals(side))) {
                result.put("code", "1");
                result.put("msg", "参数错误");
                out.print(result.toJSONString());
                return;
            }
            String id = UUID.randomUUID().toString().replace("-", "");
            jdbc.update("INSERT INTO T_FOREX_PAPER_ORDER (UUID,PARTY_ID,USERNAME,USERCODE,PAIR,SIDE,QTY,LEVERAGE,OPEN_PRICE,TAKE_PROFIT,STOP_LOSS,STATUS,CREATE_TIME) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,NOW())",
                new Object[] { id, partyId, username, usercode, pair.toUpperCase(), side, qty, leverage, price, tp > 0 ? tp : null, sl > 0 ? sl : null, "open" });
            result.put("code", "0");
            result.put("id", id);
        } else if ("close".equals(action)) {
            String id = request.getParameter("id");
            double closePrice = parseDouble(request.getParameter("price"), 0);
            if (id == null || id.length() > 40) {
                result.put("code", "1");
                result.put("msg", "参数错误");
                out.print(result.toJSONString());
                return;
            }
            List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT SIDE,QTY,OPEN_PRICE FROM T_FOREX_PAPER_ORDER WHERE UUID=? AND PARTY_ID=? AND STATUS='open'",
                new Object[] { id, partyId });
            if (rows.isEmpty()) {
                result.put("code", "1");
                result.put("msg", "持仓不存在或已平仓");
                out.print(result.toJSONString());
                return;
            }
            Map<String, Object> row = rows.get(0);
            String side = String.valueOf(row.get("SIDE"));
            double qty = parseDouble(String.valueOf(row.get("QTY")), 0);
            double open = parseDouble(String.valueOf(row.get("OPEN_PRICE")), 0);
            double pnl = ("buy".equals(side) ? closePrice - open : open - closePrice) * qty * 100000;
            jdbc.update("UPDATE T_FOREX_PAPER_ORDER SET STATUS='closed',CLOSE_PRICE=?,PNL=?,CLOSE_TIME=NOW() WHERE UUID=? AND PARTY_ID=?",
                new Object[] { closePrice, pnl, id, partyId });
            result.put("code", "0");
            result.put("pnl", pnl);
        } else {
            result.put("code", "1");
            result.put("msg", "unknown action");
        }
    } catch (Throwable e) {
        result.put("code", "500");
        result.put("msg", "服务器错误");
    }
    out.print(result.toJSONString());
%>
<%!
    private double parseDouble(String s, double def) {
        try { return Double.parseDouble(s); } catch (Throwable t) { return def; }
    }
%>
