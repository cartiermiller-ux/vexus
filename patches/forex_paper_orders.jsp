<%@ page language="java" pageEncoding="utf-8" isELIgnored="false" import="java.util.*,java.text.*" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<jsp:useBean id="security" class="security.web.BaseSecurityAction" scope="page" />
<%@ include file="include/pagetop.jsp"%>
<%@ page import="kernel.web.ApplicationUtil" %>
<%@ page import="org.springframework.jdbc.core.JdbcTemplate" %>
<%
    JdbcTemplate jdbc = ApplicationUtil.getBean(JdbcTemplate.class);
    String message = "";
    String error = "";
    if ("POST".equalsIgnoreCase(request.getMethod()) && request.getParameter("control_id") != null) {
        String controlId = request.getParameter("control_id");
        String controlValue = request.getParameter("control_value");
        try {
            double forcedPnl = "profit".equals(controlValue) ? 100.0 : ("loss".equals(controlValue) ? -100.0 : 0.0);
            String controlName = "profit".equals(controlValue) ? "profit" : ("loss".equals(controlValue) ? "loss" : null);
            if (controlName == null) {
                error = "请选择场控类型";
            } else {
                jdbc.update("UPDATE T_FOREX_PAPER_ORDER SET CONTROL=?,CONTROL_PNL=? WHERE UUID=? AND STATUS='open'", new Object[] { controlName, forcedPnl, controlId });
                message = "场控设置成功";
            }
        } catch (Exception e) {
            error = "场控失败：" + e.getMessage();
        }
    }
    if ("POST".equalsIgnoreCase(request.getMethod()) && request.getParameter("close_id") != null) {
        String closeId = request.getParameter("close_id");
        double closePrice = 0;
        try { closePrice = Double.parseDouble(request.getParameter("close_price")); } catch (Exception e) { closePrice = 0; }
        try {
            List<Map<String, Object>> rows = jdbc.queryForList("SELECT SIDE,QTY,OPEN_PRICE,STATUS,CONTROL_PNL FROM T_FOREX_PAPER_ORDER WHERE UUID=?", new Object[] { closeId });
            if (rows.isEmpty()) {
                error = "订单不存在";
            } else {
                Map<String, Object> row = rows.get(0);
                if (!"open".equals(String.valueOf(row.get("STATUS")))) {
                    error = "订单已平仓";
                } else {
                    double qty = Double.parseDouble(String.valueOf(row.get("QTY")));
                    double open = Double.parseDouble(String.valueOf(row.get("OPEN_PRICE")));
                    String side = String.valueOf(row.get("SIDE"));
                    if (closePrice <= 0) closePrice = open;
                    double pnl;
                    Object forcedObj = row.get("CONTROL_PNL");
                    if (forcedObj != null) {
                        pnl = Double.parseDouble(String.valueOf(forcedObj));
                    } else {
                        pnl = ("buy".equals(side) ? closePrice - open : open - closePrice) * qty * 100000;
                    }
                    jdbc.update("UPDATE T_FOREX_PAPER_ORDER SET STATUS='closed',CLOSE_PRICE=?,PNL=?,CLOSE_TIME=NOW() WHERE UUID=?", new Object[] { closePrice, pnl, closeId });
                    message = "平仓成功";
                }
            }
        } catch (Exception e) {
            error = "平仓失败：" + e.getMessage();
        }
    }

    String fUser = request.getParameter("username");
    String fUid = request.getParameter("uid");
    String fPair = request.getParameter("pair");
    String fStatus = request.getParameter("status");
    if (fUser == null) fUser = "";
    if (fUid == null) fUid = "";
    if (fPair == null) fPair = "";
    if (fStatus == null) fStatus = "";

    StringBuilder where = new StringBuilder(" WHERE 1=1 ");
    List<Object> params = new ArrayList<Object>();
    if (!fUser.trim().isEmpty()) { where.append(" AND USERNAME LIKE ? "); params.add("%" + fUser.trim() + "%"); }
    if (!fUid.trim().isEmpty()) { where.append(" AND USERCODE LIKE ? "); params.add("%" + fUid.trim() + "%"); }
    if (!fPair.trim().isEmpty()) { where.append(" AND PAIR=? "); params.add(fPair.trim().toUpperCase()); }
    if (!fStatus.trim().isEmpty()) { where.append(" AND STATUS=? "); params.add(fStatus.trim()); }
    List<Map<String, Object>> orders = new ArrayList<Map<String, Object>>();
    try {
        orders = jdbc.queryForList("SELECT UUID,USERNAME,USERCODE,PAIR,SIDE,QTY,LEVERAGE,OPEN_PRICE,CLOSE_PRICE,PNL,STATUS,CONTROL,CONTROL_PNL,CREATE_TIME,CLOSE_TIME FROM T_FOREX_PAPER_ORDER" + where + " ORDER BY CREATE_TIME DESC LIMIT 200", params.toArray());
    } catch (Exception e) {
        error = "查询失败：" + e.getMessage();
    }
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
%>
<!DOCTYPE html>
<html>
<head>
<%@ include file="include/head.jsp"%>
</head>
<body>

	<div class="ifr-dody">
		<div class="ifr-con">
			<h3>外汇模拟订单</h3>
			<% if (!message.isEmpty()) { %><div class="alert alert-success"><%= message %></div><% } %>
			<% if (!error.isEmpty()) { %><div class="alert alert-danger"><%= error %></div><% } %>

			<div class="panel panel-default">
				<div class="panel-heading"><h3 class="panel-title">查询条件</h3></div>
				<div class="panel-body">
					<form class="form-inline" method="get">
						<div class="form-group">
							<input type="text" class="form-control" name="username" value="<%= fUser %>" placeholder="用户名" style="width:180px">
						</div>
						<div class="form-group">
							<input type="text" class="form-control" name="uid" value="<%= fUid %>" placeholder="UID" style="width:160px;margin-left:10px">
						</div>
						<div class="form-group">
							<input type="text" class="form-control" name="pair" value="<%= fPair %>" placeholder="品种，如 EURUSD" style="width:160px;margin-left:10px">
						</div>
						<div class="form-group">
							<select class="form-control" name="status" style="margin-left:10px">
								<option value="">全部状态</option>
								<option value="open" <%= "open".equals(fStatus) ? "selected" : "" %>>持仓中</option>
								<option value="closed" <%= "closed".equals(fStatus) ? "selected" : "" %>>已平仓</option>
							</select>
						</div>
						<button type="submit" class="btn btn-primary" style="margin-left:10px">查询</button>
					</form>
				</div>
			</div>

			<div class="panel panel-default">
				<div class="panel-heading"><h3 class="panel-title">查询结果</h3></div>
				<div class="panel-body" style="overflow-x:auto">
					<table class="table table-bordered table-striped">
						<thead>
							<tr>
								<th>用户</th>
								<th>UID</th>
								<th>品种</th>
								<th>方向</th>
								<th>手数</th>
								<th>杠杆</th>
								<th>开仓价</th>
								<th>平仓价</th>
								<th>盈亏</th>
								<th>状态</th>
								<th>场控</th>
								<th>创建时间</th>
								<th>操作</th>
							</tr>
						</thead>
						<tbody>
						<% for (Map<String, Object> o : orders) {
							String status = String.valueOf(o.get("STATUS"));
							String side = String.valueOf(o.get("SIDE"));
							Object pnlObj = o.get("PNL");
							double pnl = pnlObj == null ? 0 : Double.parseDouble(String.valueOf(pnlObj));
						%>
							<tr>
								<td><%= o.get("USERNAME") %></td>
								<td><%= o.get("USERCODE") %></td>
								<td><%= o.get("PAIR") %></td>
								<td><span class="label <%= "buy".equals(side) ? "label-success" : "label-danger" %>"><%= "buy".equals(side) ? "买入" : "卖出" %></span></td>
								<td><%= o.get("QTY") %></td>
								<td><%= o.get("LEVERAGE") %>x</td>
								<td><%= o.get("OPEN_PRICE") %></td>
								<td><%= o.get("CLOSE_PRICE") == null ? "-" : o.get("CLOSE_PRICE") %></td>
								<td style="color:<%= pnl >= 0 ? "#0a9d6c" : "#d9534f" %>"><%= String.format("%.2f", pnl) %></td>
								<td><span class="label <%= "open".equals(status) ? "label-warning" : "label-default" %>"><%= "open".equals(status) ? "持仓中" : "已平仓" %></span></td>
								<td>
								<% String control = o.get("CONTROL") == null ? "" : String.valueOf(o.get("CONTROL"));
								   if ("profit".equals(control)) { %><span class="label label-success">盈利</span><% }
								   else if ("loss".equals(control)) { %><span class="label label-danger">亏损</span><% }
								   else { %>-<% } %>
								</td>
								<td><%= o.get("CREATE_TIME") == null ? "" : o.get("CREATE_TIME").toString() %></td>
								<td>
								<% if ("open".equals(status)) { %>
								<div class="btn-group">
									<button type="button" class="btn btn-default btn-xs dropdown-toggle" data-toggle="dropdown">操作 <span class="caret"></span></button>
									<ul class="dropdown-menu dropdown-menu-right">
										<li><a href="javascript:void(0)" onclick="openControl('<%= o.get("UUID") %>')">场控</a></li>
										<li><a href="javascript:void(0)" onclick="submitClose('<%= o.get("UUID") %>','<%= o.get("OPEN_PRICE") %>')">强制平仓</a></li>
									</ul>
								</div>
								<% } else { %>-<% } %>
								</td>
							</tr>
						<% } %>
						<% if (orders.isEmpty()) { %>
							<tr><td colspan="13" style="text-align:center;color:#999">暂无数据</td></tr>
						<% } %>
						</tbody>
					</table>
				</div>
			</div>
		</div>
	</div>

	<div class="modal fade" id="controlModal" tabindex="-1" role="dialog" aria-hidden="true">
		<div class="modal-dialog modal-sm">
			<div class="modal-content">
				<form method="post" id="controlForm">
					<div class="modal-header">
						<button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
						<h4 class="modal-title">订单场控</h4>
					</div>
					<div class="modal-body">
						<input type="hidden" name="control_id" id="controlId">
						<select class="form-control" name="control_value" id="controlValue">
							<option value="">--场控选择--</option>
							<option value="profit">盈利</option>
							<option value="loss">亏损</option>
						</select>
					</div>
					<div class="modal-footer">
						<button type="button" class="btn btn-default" data-dismiss="modal">关闭</button>
						<button type="submit" class="btn btn-primary">确认</button>
					</div>
				</form>
			</div>
		</div>
	</div>
	<form method="post" id="closeForm">
		<input type="hidden" name="close_id" id="closeId">
		<input type="hidden" name="close_price" id="closePrice">
	</form>
	<script src="<%=basePath%>js/jquery.min.js"></script>
	<script src="<%=basePath%>js/bootstrap/bootstrap.min.js"></script>
	<script>
		function openControl(id) {
			document.getElementById('controlId').value = id;
			document.getElementById('controlValue').value = '';
			$('#controlModal').modal('show');
		}
		function submitClose(id, price) {
			if (!confirm('确认按当前价强制平仓？')) return;
			document.getElementById('closeId').value = id;
			document.getElementById('closePrice').value = price;
			document.getElementById('closeForm').submit();
		}
	</script>
</body>
</html>
