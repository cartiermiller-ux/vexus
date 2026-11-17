<%@ page language="java" pageEncoding="utf-8" isELIgnored="false" import="java.io.*" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<jsp:useBean id="security" class="security.web.BaseSecurityAction" scope="page" />
<%@ include file="include/pagetop.jsp"%>
<%
    String configFile = "/www/wwwroot/vexus/site/forex-paper-config.json";
    String message = "";
    if ("POST".equalsIgnoreCase(request.getMethod())) {
        request.setCharacterEncoding("utf-8");
        String configJsonParam = request.getParameter("config");
        if (configJsonParam == null || configJsonParam.trim().isEmpty()) {
            message = "配置内容不能为空";
        } else {
            Writer w = null;
            try {
                w = new OutputStreamWriter(new FileOutputStream(configFile), "utf-8");
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
        br = new BufferedReader(new InputStreamReader(new FileInputStream(configFile), "utf-8"));
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
<!DOCTYPE html>
<html>
<head>
<%@ include file="include/head.jsp"%>
</head>
<body>

	<div class="ifr-dody">
		<div class="ifr-con">
			<h3>外汇模拟交易配置</h3>
			<% if (!message.isEmpty()) { %>
			<div class="alert alert-success" role="alert"><%= message %></div>
			<% } %>
			<div class="panel panel-default">
				<div class="panel-heading"><h3 class="panel-title">基础配置</h3></div>
				<div class="panel-body">
					<form class="form-horizontal" method="post" id="cfgForm">
						<input type="hidden" name="config" id="configInput">
						<div class="form-group">
							<label class="col-sm-2 control-label">模拟交易总开关</label>
							<div class="col-sm-4">
								<select class="form-control" id="enabled">
									<option value="1">启用</option>
									<option value="0">暂停</option>
								</select>
							</div>
							<label class="col-sm-2 control-label">初始虚拟资金（USD）</label>
							<div class="col-sm-4">
								<input type="number" class="form-control" id="initialBalance" min="0" step="1000">
							</div>
						</div>
						<div class="form-group">
							<label class="col-sm-2 control-label">默认杠杆</label>
							<div class="col-sm-4">
								<input type="number" class="form-control" id="defaultLeverage" min="1" step="1">
							</div>
							<label class="col-sm-2 control-label">可选杠杆</label>
							<div class="col-sm-4">
								<input type="text" class="form-control" id="leverageOptions" placeholder="1,10,20,50,100">
							</div>
						</div>
						<div class="form-group">
							<label class="col-sm-2 control-label">点差（pip）</label>
							<div class="col-sm-4">
								<input type="number" class="form-control" id="spread" min="0" step="0.1">
							</div>
							<label class="col-sm-2 control-label">手续费</label>
							<div class="col-sm-4">
								<input type="number" class="form-control" id="commission" min="0" step="0.01">
							</div>
						</div>
					</form>
				</div>
			</div>

			<div class="panel panel-default">
				<div class="panel-heading"><h3 class="panel-title">可交易品种</h3></div>
				<div class="panel-body">
					<table class="table table-bordered table-striped">
						<thead>
							<tr>
								<th style="width:80px">启用</th>
								<th>品种</th>
								<th>TradingView 代码</th>
								<th>基础货币</th>
								<th>报价货币</th>
								<th>小数位</th>
								<th>操作</th>
							</tr>
						</thead>
						<tbody id="pairsBody"></tbody>
					</table>
				</div>
			</div>


			<div class="panel panel-default">
				<div class="panel-heading"><h3 class="panel-title">新增交易品种</h3></div>
				<div class="panel-body">
					<div class="form-inline">
						<input type="text" class="form-control" id="newCode" placeholder="代码，如 USDSEK" style="width:150px">
						<input type="text" class="form-control" id="newName" placeholder="名称，如 USD/SEK" style="width:150px;margin-left:8px">
						<input type="text" class="form-control" id="newTv" placeholder="TV代码，如 FX:USDSEK" style="width:180px;margin-left:8px">
						<input type="text" class="form-control" id="newBase" placeholder="基础货币 USD" style="width:120px;margin-left:8px">
						<input type="text" class="form-control" id="newQuote" placeholder="报价货币 SEK" style="width:120px;margin-left:8px">
						<input type="number" class="form-control" id="newDecimals" placeholder="小数位" value="5" style="width:90px;margin-left:8px">
						<input type="number" class="form-control" id="newPip" placeholder="pip" value="0.0001" step="0.0001" style="width:100px;margin-left:8px">
						<button type="button" class="btn btn-primary" id="addPairBtn" style="margin-left:8px">添加品种</button>
					</div>
					<div class="help-block" style="margin-top:8px">添加后点击保存配置，前台外汇模拟盘会立即出现新品种。</div>
				</div>
			</div>

			<div class="form-group">
				<div class="col-sm-12">
					<button type="button" class="btn btn-primary" id="saveBtn">保存配置</button>
					<a class="btn btn-default" href="/tradfi/forex/" target="_blank">打开外汇模拟交易页</a>
				</div>
			</div>
		</div>
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

document.getElementById('enabled').value = cfg.enabled === false ? '0' : '1';
document.getElementById('initialBalance').value = Number(cfg.initial_balance) || 100000;
document.getElementById('defaultLeverage').value = Number(cfg.default_leverage) || 100;
document.getElementById('leverageOptions').value = (cfg.leverage_options || []).join(',');
document.getElementById('spread').value = Number(cfg.spread_pips) || 1.2;
document.getElementById('commission').value = Number(cfg.commission) || 0;

var tbody = document.getElementById('pairsBody');
function renderPairs(){
  tbody.innerHTML = '';
  Object.keys(allPairs).forEach(function(k){
    var p = allPairs[k];
    var tr = document.createElement('tr');
    tr.innerHTML = '<td><input type="checkbox" data-pair="'+k+'" checked></td>'
      + '<td>'+(p.name || k)+'</td>'
      + '<td>'+(p.tv || '')+'</td>'
      + '<td>'+(p.base || '')+'</td>'
      + '<td>'+(p.quote || '')+'</td>'
      + '<td>'+(p.decimals == null ? 5 : p.decimals)+'</td>'
      + '<td><button type="button" class="btn btn-xs btn-danger" data-del="'+k+'">删除</button></td>';
    tbody.appendChild(tr);
  });
  Array.prototype.forEach.call(document.querySelectorAll('[data-del]'), function(b){
    b.onclick = function(){
      delete allPairs[b.getAttribute('data-del')];
      renderPairs();
    };
  });
}
renderPairs();

document.getElementById('addPairBtn').addEventListener('click', function(){
  var code = (document.getElementById('newCode').value || '').trim().toUpperCase();
  var name = (document.getElementById('newName').value || '').trim();
  var tv = (document.getElementById('newTv').value || '').trim();
  var base = (document.getElementById('newBase').value || '').trim().toUpperCase();
  var quote = (document.getElementById('newQuote').value || '').trim().toUpperCase();
  if (!/^[A-Z0-9]{3,16}$/.test(code)) { alert('请输入有效品种代码，例如 USDSEK'); return; }
  if (!name) name = base && quote ? base + '/' + quote : code;
  if (!tv) tv = 'FX:' + code;
  if (!/^[A-Z]{3}$/.test(base) || !/^[A-Z]{3}$/.test(quote)) { alert('基础货币和报价货币必须是 3 位代码'); return; }
  allPairs[code] = {
    name: name, tv: tv, base: base, quote: quote,
    decimals: Number(document.getElementById('newDecimals').value) || 5,
    pip: Number(document.getElementById('newPip').value) || 0.0001
  };
  renderPairs();
  document.getElementById('newCode').value = '';
  document.getElementById('newName').value = '';
  document.getElementById('newTv').value = '';
});

document.getElementById('saveBtn').addEventListener('click', function(){
  var selected = {};
  Array.prototype.forEach.call(document.querySelectorAll('[data-pair]'), function(cb){
    if (cb.checked) {
      var k = cb.getAttribute('data-pair');
      if (allPairs[k]) selected[k] = allPairs[k];
    }
  });
  var lev = document.getElementById('leverageOptions').value.split(',').map(function(x){return Number(x.trim());}).filter(function(x){return x > 0;});
  if (!lev.length) lev = [1];
  cfg.enabled = document.getElementById('enabled').value === '1';
  cfg.initial_balance = Number(document.getElementById('initialBalance').value) || 0;
  cfg.default_leverage = Number(document.getElementById('defaultLeverage').value) || 1;
  cfg.leverage_options = lev;
  cfg.spread_pips = Number(document.getElementById('spread').value) || 0;
  cfg.commission = Number(document.getElementById('commission').value) || 0;
  cfg.pairs = selected;
  document.getElementById('configInput').value = JSON.stringify(cfg);
  document.getElementById('cfgForm').submit();
});
</script>
</body>
</html>
