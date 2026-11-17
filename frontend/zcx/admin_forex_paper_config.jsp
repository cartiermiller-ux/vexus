<%@ page language="java" pageEncoding="utf-8" import="java.io.*" %>
<%
    String path = "/www/wwwroot/vexus/site/forex-paper-config.json";
    String message = "";
    if ("POST".equalsIgnoreCase(request.getMethod())) {
        request.setCharacterEncoding("utf-8");
        String json = request.getParameter("json");
        if (json == null || json.trim().isEmpty()) {
            message = "配置内容不能为空";
        } else {
            FileWriter fw = null;
            try {
                fw = new FileWriter(path);
                fw.write(json);
                message = "保存成功";
            } catch (Exception e) {
                message = "保存失败：" + e.getMessage();
            } finally {
                if (fw != null) { try { fw.close(); } catch (Exception ignored) {} }
            }
        }
    }
    StringBuilder sb = new StringBuilder();
    BufferedReader br = null;
    try {
        br = new BufferedReader(new InputStreamReader(new FileInputStream(path), "utf-8"));
        String line;
        while ((line = br.readLine()) != null) { sb.append(line).append("\n"); }
    } catch (Exception e) {
        sb.append("{}\n");
    } finally {
        if (br != null) { try { br.close(); } catch (Exception ignored) {} }
    }
%>
<!doctype html>
<html lang="zh-CN">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width,initial-scale=1">
<title>外汇模拟交易配置 - Vexus Admin</title>
<style>
body{margin:0;background:#0b0914;color:#fff;font-family:-apple-system,"Segoe UI","PingFang SC","Microsoft YaHei",sans-serif}
.wrap{max-width:900px;margin:0 auto;padding:34px 22px}
h1{font-size:24px;margin:0 0 8px}p{color:rgba(255,255,255,.55);font-size:13px;line-height:1.7}
textarea{width:100%;height:520px;background:#15122a;color:#dcd6ff;border:1px solid rgba(167,139,250,.3);border-radius:12px;padding:16px;font:12px/1.6 Consolas,monospace;outline:none;box-sizing:border-box}
button{margin-top:14px;height:42px;padding:0 28px;border:0;border-radius:9px;background:linear-gradient(135deg,#6d4aff,#a855f7);color:#fff;font-weight:700;cursor:pointer}
.msg{margin:12px 0;color:#0ECB81;font-size:13px}.msg.err{color:#F6465D}
a{color:#b9a7ff;font-size:12px}
</style>
</head>
<body>
<div class="wrap">
  <h1>外汇模拟交易配置</h1>
  <p>修改后保存，前端页面刷新后生效。字段：initial_balance 初始资金，leverage_options 杠杆，default_leverage 默认杠杆，spread_pips 点差，pairs 可交易品种。</p>
  <% if (!message.isEmpty()) { %><div class="msg"><%= message %></div><% } %>
  <form method="post">
    <textarea name="json"><%= sb.toString().replace("&","&amp;").replace("<","&lt;").replace(">","&gt;") %></textarea>
    <button type="submit">保存配置</button>
  </form>
  <p><a href="/tradfi/forex/" target="_blank">打开外汇模拟交易页</a></p>
</div>
</body>
</html>
