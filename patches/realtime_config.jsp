<%@ page language="java" pageEncoding="utf-8" isELIgnored="false" import="java.io.*,java.util.*" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<jsp:useBean id="security" class="security.web.BaseSecurityAction" scope="page" />
<%@ include file="include/pagetop.jsp"%>
<%@ page import="com.alibaba.fastjson.JSON" %>
<%@ page import="com.alibaba.fastjson.JSONArray" %>
<%@ page import="com.alibaba.fastjson.JSONObject" %>
<%
    String cfgPath = "/www/wwwroot/vexus/site/realtime-config.json";
    String message = "";
    JSONObject cfg = new JSONObject();
    if ("POST".equalsIgnoreCase(request.getMethod())) {
        try {
            request.setCharacterEncoding("utf-8");
            JSONObject out = new JSONObject();
            out.put("enabled", "1".equals(request.getParameter("enabled")));
            out.put("interval_ms", Math.max(500, Math.min(5000, Integer.parseInt(request.getParameter("interval_ms")))));
            JSONObject ins = new JSONObject();
            String[] syms = request.getParameterValues("symbol");
            String[] types = request.getParameterValues("ttype");
            String[] bases = request.getParameterValues("base");
            String[] decs = request.getParameterValues("decimals");
            String[] vols = request.getParameterValues("volatility");
            String[] spreads = request.getParameterValues("spread");
            if (syms != null) {
                for (int i = 0; i < syms.length; i++) {
                    String sym = syms[i] == null ? "" : syms[i].trim().toUpperCase();
                    if (sym.isEmpty()) continue;
                    JSONObject item = new JSONObject();
                    item.put("type", types != null && i < types.length ? types[i] : "forex");
                    item.put("base", Double.parseDouble(bases != null && i < bases.length ? bases[i] : "1"));
                    item.put("decimals", Integer.parseInt(decs != null && i < decs.length ? decs[i] : "5"));
                    item.put("volatility", Double.parseDouble(vols != null && i < vols.length ? vols[i] : "0.0002"));
                    item.put("spread", Double.parseDouble(spreads != null && i < spreads.length ? spreads[i] : "0.0001"));
                    ins.put(sym, item);
                }
            }
            out.put("instruments", ins);
            Writer w = new OutputStreamWriter(new FileOutputStream(cfgPath), "utf-8");
            w.write(out.toJSONString());
            w.close();
            message = "保存成功";
            cfg = out;
        } catch (Exception e) {
            message = "保存失败：" + e.getMessage();
        }
    }
    if (cfg.isEmpty()) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(cfgPath), "utf-8"));
            StringBuilder sb = new StringBuilder(); String line;
            while ((line = br.readLine()) != null) sb.append(line);
            br.close();
            cfg = JSON.parseObject(sb.toString());
        } catch (Exception e) { cfg = new JSONObject(); }
    }
    JSONObject instruments = cfg.getJSONObject("instruments");
    if (instruments == null) instruments = new JSONObject();
%>
<!DOCTYPE html>
<html>
<head>
<%@ include file="include/head.jsp"%>
</head>
<body>
<div class="ifr-dody">
  <div class="ifr-con">
    <h3>实时行情控制</h3>
    <% if (!message.isEmpty()) { %><div class="alert alert-success"><%= message %></div><% } %>
    <div class="panel panel-default">
      <div class="panel-heading"><h3 class="panel-title">基础设置</h3></div>
      <div class="panel-body">
        <form method="post" id="cfgForm">
          <div class="form-group">
            <label class="col-sm-2 control-label">行情服务开关</label>
            <div class="col-sm-3">
              <select class="form-control" name="enabled">
                <option value="1" <%= cfg.getBooleanValue("enabled") ? "selected" : "" %>>启用</option>
                <option value="0" <%= !cfg.getBooleanValue("enabled") ? "selected" : "" %>>暂停</option>
              </select>
            </div>
            <label class="col-sm-2 control-label">刷新间隔（毫秒）</label>
            <div class="col-sm-3">
              <input class="form-control" type="number" name="interval_ms" min="500" max="5000" value="<%= cfg.getIntValue("interval_ms") > 0 ? cfg.getIntValue("interval_ms") : 1000 %>">
            </div>
          </div>
          <div class="panel-heading" style="margin-top:15px"><h3 class="panel-title">行情品种</h3></div>
          <table class="table table-bordered table-striped" id="instTable">
            <thead><tr><th>代码</th><th>类型</th><th>基准价</th><th>小数位</th><th>波动率</th><th>点差</th><th>操作</th></tr></thead>
            <tbody>
            <% for (String sym : instruments.keySet()) {
                 JSONObject it = instruments.getJSONObject(sym); %>
              <tr>
                <td><input class="form-control" name="symbol" value="<%= sym %>"></td>
                <td><select class="form-control" name="ttype">
                  <option value="crypto" <%= "crypto".equals(it.getString("type")) ? "selected" : "" %>>crypto</option>
                  <option value="forex" <%= "forex".equals(it.getString("type")) ? "selected" : "" %>>forex</option>
                  <option value="stock" <%= "stock".equals(it.getString("type")) ? "selected" : "" %>>stock</option>
                </select></td>
                <td><input class="form-control" name="base" value="<%= it.getDoubleValue("base") %>"></td>
                <td><input class="form-control" name="decimals" value="<%= it.getIntValue("decimals") %>"></td>
                <td><input class="form-control" name="volatility" value="<%= it.getDoubleValue("volatility") %>"></td>
                <td><input class="form-control" name="spread" value="<%= it.getDoubleValue("spread") %>"></td>
                <td><button type="button" class="btn btn-xs btn-danger" onclick="this.closest('tr').remove()">删除</button></td>
              </tr>
            <% } %>
            </tbody>
          </table>
          <button type="button" class="btn btn-default" id="addRow">新增品种</button>
          <button type="submit" class="btn btn-primary">保存配置</button>
          <a class="btn btn-default" href="/admin/forex_paper_config.jsp">外汇模拟配置</a>
        </form>
      </div>
    </div>
  </div>
</div>
<script>
document.getElementById('addRow').onclick = function(){
  var tb = document.querySelector('#instTable tbody');
  var tr = document.createElement('tr');
  tr.innerHTML = '<td><input class="form-control" name="symbol" placeholder="USDJPY"></td>'
    + '<td><select class="form-control" name="ttype"><option value="forex">forex</option><option value="crypto">crypto</option><option value="stock">stock</option></select></td>'
    + '<td><input class="form-control" name="base" value="1"></td>'
    + '<td><input class="form-control" name="decimals" value="5"></td>'
    + '<td><input class="form-control" name="volatility" value="0.0002"></td>'
    + '<td><input class="form-control" name="spread" value="0.0001"></td>'
    + '<td><button type="button" class="btn btn-xs btn-danger" onclick="this.closest(\'tr\').remove()">删除</button></td>';
  tb.appendChild(tr);
};
</script>
</body>
</html>
