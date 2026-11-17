<%@ page language="java" pageEncoding="utf-8" import="java.io.*" %>
<%
    String path = "/www/wwwroot/vexus/site/forex-paper-config.json";
    String message = "";
    if ("POST".equalsIgnoreCase(request.getMethod())) {
        request.setCharacterEncoding("utf-8");
        String configJsonParam = request.getParameter("config");
        if (configJsonParam == null || configJsonParam.trim().isEmpty()) {
            message = "配置内容不能为空";
        } else {
            Writer w = null;
            try {
                w = new OutputStreamWriter(new FileOutputStream(path), "utf-8");
                w.write(configJsonParam);
                message = "保存成功";
            } catch (Exception e) {
                message = "保存失败：" + e.getMessage();
            } finally {
                if (w != null) { try { w.close(); } catch (Exception ignored) {} }
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
    String cfgJson = sb.toString().trim();
    if (cfgJson.isEmpty()) cfgJson = "{}";
    String safeJson = cfgJson.replace("<", "\\u003c").replace(">", "\\u003e");
%>
<!doctype html>
<html lang="zh-CN">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width,initial-scale=1">
<title>外汇模拟交易配置</title>
<link href="<%=request.getContextPath()%>/css/root.css" rel="stylesheet">
<style>
body{background:#f4f6f8;color:#2d3748;font-family:-apple-system,"Segoe UI","PingFang SC","Microsoft YaHei",sans-serif;margin:0}
.fx-admin{padding:22px 26px;max-width:1200px}
.fx-admin h2{font-size:22px;margin:0 0 6px;font-weight:700}
.fx-admin .sub{color:#718096;font-size:13px;margin-bottom:18px}
.panel{background:#fff;border:1px solid #e2e8f0;border-radius:8px;padding:20px;margin-bottom:16px;box-shadow:0 1px 3px rgba(0,0,0,.04)}
.row{display:flex;flex-wrap:wrap;gap:16px}
.field{flex:1 1 220px;min-width:200px}
.field label{display:block;color:#4a5568;font-size:12px;font-weight:600;margin-bottom:6px}
.field input,.field select{width:100%;height:36px;border:1px solid #cbd5e0;border-radius:6px;padding:0 10px;font-size:13px;color:#2d3748;background:#fff;box-sizing:border-box}
.field input:focus,.field select:focus{outline:0;border-color:#667eea;box-shadow:0 0 0 3px rgba(102,126,234,.15)}
table{width:100%;border-collapse:collapse;margin-top:12px}
th,td{border-bottom:1px solid #edf2f7;padding:10px;text-align:left;font-size:13px}
th{background:#f7fafc;color:#4a5568;font-weight:600}
.switch{position:relative;display:inline-block;width:46px;height:24px}
.switch input{opacity:0;width:0;height:0}
.slider{position:absolute;cursor:pointer;top:0;left:0;right:0;bottom:0;background:#cbd5e0;border-radius:24px;transition:.2s}
.slider:before{position:absolute;content:"";height:18px;width:18px;left:3px;bottom:3px;background:#fff;border-radius:50%;transition:.2s;box-shadow:0 1px 2px rgba(0,0,0,.25)}
input:checked + .slider{background:#5b6cff}
input:checked + .slider:before{transform:translateX(22px)}
.btn-save{height:40px;padding:0 26px;border:0;border-radius:6px;background:#5b6cff;color:#fff;font-weight:600;cursor:pointer;font-size:14px}
.btn-save:hover{background:#4c5ce6}
.msg{padding:10px 14px;border-radius:6px;background:#e6fffa;color:#087f5b;margin-bottom:14px;font-size:13px}
.tip{font-size:12px;color:#a0aec0;margin-top:10px;line-height:1.7}
</style>
</head>
<body>
<div class="fx-admin">
  <h2>外汇模拟交易配置</h2>
  <div class="sub">配置外汇模拟盘的启用状态、虚拟资金、杠杆、点差和可交易品种。保存后前台刷新即生效。</div>

  <% if (!message.isEmpty()) { %><div class="msg"><%= message %></div><% } %>

  <form method="post" id="cfgForm">
    <input type="hidden" name="config" id="configInput">
    <div class="panel">
      <div class="row">
        <div class="field">
          <label>模拟交易总开关</label>
          <label class="switch"><input type="checkbox" id="enabled"><span class="slider"></span></label>
          <div class="tip" id="enabledText">已启用</div>
        </div>
        <div class="field">
          <label>初始虚拟资金（USD）</label>
          <input type="number" id="initialBalance" min="0" step="1000">
        </div>
        <div class="field">
          <label>默认杠杆</label>
          <input type="number" id="defaultLeverage" min="1" step="1">
        </div>
      </div>
      <div class="row" style="margin-top:16px">
        <div class="field">
          <label>可选杠杆（逗号分隔）</label>
          <input type="text" id="leverageOptions" placeholder="1,10,20,50,100">
        </div>
        <div class="field">
          <label>点差（pip）</label>
          <input type="number" id="spread" min="0" step="0.1">
        </div>
        <div class="field">
          <label>手续费</label>
          <input type="number" id="commission" min="0" step="0.01">
        </div>
      </div>
    </div>

    <div class="panel">
      <div style="font-size:15px;font-weight:700;margin-bottom:4px">可交易品种</div>
      <div class="tip" style="margin-top:0;margin-bottom:4px">勾选后该品种在前台外汇模拟盘中开放。</div>
      <table>
        <thead><tr><th style="width:70px">启用</th><th>品种</th><th>TradingView 代码</th><th>基础货币</th><th>报价货币</th><th>小数位</th></tr></thead>
        <tbody id="pairsBody"></tbody>
      </table>
    </div>

    <button type="submit" class="btn-save" id="saveBtn">保存配置</button>
    <div class="tip">保存后前台 <a href="/tradfi/forex/" target="_blank" style="color:#5b6cff">/tradfi/forex/</a> 刷新即生效。</div>
  </form>
</div>

<script>
var cfg = <%= safeJson %>;
if (!cfg || typeof cfg !== 'object') cfg = {};
if (!cfg.pairs || typeof cfg.pairs !== 'object') cfg.pairs = {};
if (typeof cfg.initial_balance === 'undefined') cfg.initial_balance = 100000;
if (typeof cfg.leverage_options === 'undefined') cfg.leverage_options = [1,10,20,50,100];
if (typeof cfg.default_leverage === 'undefined') cfg.default_leverage = 100;
if (typeof cfg.spread_pips === 'undefined') cfg.spread_pips = 1.2;
if (typeof cfg.commission === 'undefined') cfg.commission = 0;
if (typeof cfg.enabled === 'undefined') cfg.enabled = true;

var allPairs = {};
Object.keys(cfg.pairs).forEach(function(k){ allPairs[k] = cfg.pairs[k]; });

document.getElementById('enabled').checked = cfg.enabled !== false;
document.getElementById('initialBalance').value = Number(cfg.initial_balance) || 100000;
document.getElementById('defaultLeverage').value = Number(cfg.default_leverage) || 100;
document.getElementById('leverageOptions').value = (cfg.leverage_options || []).join(',');
document.getElementById('spread').value = Number(cfg.spread_pips) || 1.2;
document.getElementById('commission').value = Number(cfg.commission) || 0;
var enabledText = document.getElementById('enabledText');
function updateEnabledText(){ enabledText.textContent = document.getElementById('enabled').checked ? '已启用' : '已暂停'; }
document.getElementById('enabled').addEventListener('change', updateEnabledText);
updateEnabledText();

function renderPairs(){
  var tbody = document.getElementById('pairsBody');
  tbody.innerHTML = '';
  Object.keys(allPairs).forEach(function(k){
    var p = allPairs[k];
    var tr = document.createElement('tr');
    tr.innerHTML = '<td><input type="checkbox" data-pair="'+k+'" checked></td>'
      + '<td><strong>'+ (p.name || k) +'</strong></td>'
      + '<td>'+(p.tv||'')+'</td>'
      + '<td>'+(p.base||'')+'</td>'
      + '<td>'+(p.quote||'')+'</td>'
      + '<td>'+(p.decimals==null?5:p.decimals)+'</td>';
    tbody.appendChild(tr);
  });
}
renderPairs();

document.getElementById('cfgForm').addEventListener('submit', function(e){
  var selected = {};
  Array.prototype.forEach.call(document.querySelectorAll('[data-pair]'), function(cb){
    if (cb.checked) {
      var k = cb.getAttribute('data-pair');
      if (allPairs[k]) selected[k] = allPairs[k];
    }
  });
  var lev = document.getElementById('leverageOptions').value.split(',').map(function(x){return Number(x.trim());}).filter(function(x){return x>0;});
  if (!lev.length) lev = [1];
  cfg.enabled = document.getElementById('enabled').checked;
  cfg.initial_balance = Number(document.getElementById('initialBalance').value) || 0;
  cfg.default_leverage = Number(document.getElementById('defaultLeverage').value) || 1;
  cfg.leverage_options = lev;
  cfg.spread_pips = Number(document.getElementById('spread').value) || 0;
  cfg.commission = Number(document.getElementById('commission').value) || 0;
  cfg.pairs = selected;
  document.getElementById('configInput').value = JSON.stringify(cfg);
});
</script>
</body>
</html>
