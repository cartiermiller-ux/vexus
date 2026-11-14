<%@ page language="java" pageEncoding="utf-8" isELIgnored="false"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%@ include file="include/pagetop.jsp"%>

<!DOCTYPE html>
<html>

<head>
<%@ include file="include/head.jsp"%>
</head>

<body>

	<%@ include file="include/loading.jsp"%>

	<!-- //////////////////////////////////////////////////////////////////////////// -->
	<!-- START CONTENT -->
	<div class="ifr-dody">
	
		<!-- //////////////////////////////////////////////////////////////////////////// -->
		<!-- START CONTAINER -->
		<div class="ifr-con">		
			<h3>C2C承兑商管理</h3>
			
			<%@ include file="include/alert.jsp"%>
			
			<!-- //////////////////////////////////////////////////////////////////////////// -->
			<!-- START queryForm -->
			<form action="<%=basePath%>normal/adminC2cUserAction!list.action" method="post" id="queryForm">
				<input type="hidden" name="pageNo" id="pageNo" value="${pageNo}" />
			</form>
			<!-- END queryForm -->
			<!-- //////////////////////////////////////////////////////////////////////////// -->
			
			<form class="form-horizontal" action="<%=basePath%>normal/adminSystemUserAction!list.action" method="post" id="queryFormSystemUser">				
			</form>
			
			<div class="row">
				<div class="col-md-12 col-lg-12">
					<div class="panel panel-default">

						<div class="panel-title">
							修改承兑商信息
							<ul class="panel-tools">
								<li><a class="icon minimise-tool"><i class="fa fa-minus"></i></a></li>
								<li><a class="icon expand-tool"><i class="fa fa-expand"></i></a></li>
							</ul>
						</div>

						<div class="panel-body">
							<form class="form-horizontal" action="<%=basePath%>normal/adminC2cUserAction!update.action"
								method="post" name="mainForm" id="mainForm">
								
								<input type="hidden" name="id" id="id" value="${id}" />
								<input type="hidden" name="head_img" id="img_url" value="${head_img}" >
								
								<h5>基础信息</h5>
								
								<div class="form-group">
									<label class="col-sm-2 control-label form-label">C2C管理员</label>
									<div class="col-sm-2">
										<select id="c2c_manager_party_id" name="c2c_manager_party_id" class="form-control" >
											<option value="">请选择</option>
											<c:forEach items="${c2cManagerMap}" var="item">
												<option value="${item.key}" <c:if test="${c2c_manager_party_id == item.key}">selected="true"</c:if> >${item.value}</option>
											</c:forEach>
										</select>
									</div>
									<div class="col-sm-2">
										<a href="javascript:addC2cManager()" class="btn btn-default">添加C2C管理员</a>
									</div>
								</div>

								<div class="form-group">
									<label class="col-sm-2 control-label form-label">承兑商类型</label>
									<div class="col-sm-8">
										<input type="hidden" id="c2c_user_type" name="c2c_user_type" class="form-control" readOnly="readOnly" value="${c2c_user_type}" />
										<input id="c2c_user_type_name" name="c2c_user_type_name" class="form-control" readOnly="readOnly" value="${c2c_user_type_name}" />
									</div>
								</div>
								
								<div class="form-group">
									<label class="col-sm-2 control-label form-label">承兑商UID</label>
									<div class="col-sm-8">
										<input id="c2c_user_code" name="c2c_user_code" class="form-control" readOnly="readOnly" value="${c2c_user_code}" />
									</div>
								</div>
								
								<div class="form-group">
									<label class="col-sm-2 control-label form-label">承兑商昵称</label>
									<div class="col-sm-8">
										<input id="nick_name" name="nick_name" class="form-control" placeholder="请输入承兑商昵称" value="${nick_name}" />
									</div>
								</div>
								
								<div class="form-group">
									<label class="col-sm-2 control-label form-label">承兑商头像</label>
									<div class="col-sm-3">
										<input type="file" id="fileName_img" name="fileName_img" onchange="upload_img();" style="position: absolute; opacity: 0;"> 
										<label for="fileName_img"> 
											<img width="40px" height="40px" id="show_img_content" src="<%=base%>/image/add.png" alt="点击上传图片" />
										</label>
									</div>
								</div>
								
								<div class="form-group">
									<label class="col-sm-2 control-label form-label">用户UID</label>
									<div class="col-sm-8">
										<input id="user_code" name="user_code" class="form-control" readOnly="readOnly" value="${user_code}" />
									</div>
								</div>
								
								<div class="form-group">
									<label class="col-sm-2 control-label form-label">用户名</label>
									<div class="col-sm-8">
										<input id="user_name" name="user_name" class="form-control" readOnly="readOnly" value="${user_name}" />
									</div>
								</div>
								
								<div class="form-group">
									<label class="col-sm-2 control-label form-label">剩余保证金</label>
									<div class="col-sm-3">
										<div class="input-group">
											<input id="deposit" name="deposit" class="form-control" readOnly="readOnly" value="${deposit}" />
											<div class="input-group-addon">USDT</div>
										</div>
									</div>
								</div>
								
								<div class="form-group">
									<label class="col-sm-2 control-label form-label">保证金</label>
									<div class="col-sm-3">
										<div class="input-group">
											<input id="deposit_open" name="deposit_open" class="form-control" readOnly="readOnly" value="${deposit_open}" />
											<div class="input-group-addon">USDT</div>
										</div>
									</div>
								</div>
								
								<div style="display: none">
								
									<div class="form-group" >
										<label class="col-sm-2 control-label form-label"></label>
										<div class="col-sm-8">
											<p class="ballon color1">
												保证金赠送比率: 保证金实际到账额度=充值额度*（1 + 保证金赠送比率/100）
											</p>
										</div>
									</div>
									
									<div class="form-group" >
										<label class="col-sm-2 control-label form-label">保证金赠送比率</label>
										<div class="col-sm-2">
											<div class="input-group">
												<input id="deposit_gift_rate" name="deposit_gift_rate" class="form-control" value="${deposit_gift_rate}" />
												<div class="input-group-addon">%</div>
											</div>
										</div>
									</div>
									
								</div>
								
								<h5>统计数据</h5>
								
								<div class="form-group">
									<label class="col-sm-2 control-label form-label"></label>
									<div class="col-sm-8">
										<p class="ballon color1">
											说明：
											<br />1.【基础设置】是后台配置的，【统计值】是系统每天定时统计的；
											<br />2.“30日成单率”、“30日平均放行时间”、“30日平均付款时间” 3个参数：【基础设置】不为0时，【最终值】=【基础设置】；否则，【最终值】=【统计值】；
											<br />3.其它参数：【最终值】=【基础设置】+【统计值】；
										</p>
									</div>
								</div>
								
								<div class="form-group">
									<label class="col-sm-2 control-label form-label">30日成单数</label>
									<div class="col-sm-3">
										<div class="input-group">
											<div class="input-group-addon">基础设置</div>
											<input onchange="updateStatistic();" id="thirty_days_order_base" name="thirty_days_order_base" class="form-control" value="${thirty_days_order_base}" />
											<div class="input-group-addon">单</div>
										</div>
									</div>
									<div class="col-sm-3">
										<div class="input-group">
											<div class="input-group-addon">统计值</div>
											<input id="thirty_days_order" name="thirty_days_order" class="form-control" readOnly="readOnly" value="${thirty_days_order}" />
											<div class="input-group-addon">单</div>
										</div>
									</div>
									<div class="col-sm-3">
										<div class="input-group">
											<div class="input-group-addon">最终值</div>
											<input id="thirty_days_order_result" name="thirty_days_order_result" class="form-control" readOnly="readOnly" value="${thirty_days_order_result}" />
											<div class="input-group-addon">单</div>
										</div>
									</div>
								</div>
								
								<div class="form-group">
									<label class="col-sm-2 control-label form-label">30日成单率</label>
									<div class="col-sm-3">
										<div class="input-group">
											<div class="input-group-addon">基础设置</div>
											<input onchange="updateStatistic();" id="thirty_days_order_ratio_base" name="thirty_days_order_ratio_base" class="form-control" value="${thirty_days_order_ratio_base}" />
											<div class="input-group-addon">%</div>
										</div>
									</div>
									<div class="col-sm-3">
										<div class="input-group">
											<div class="input-group-addon">统计值</div>
											<input id="thirty_days_order_ratio" name="thirty_days_order_ratio" class="form-control" readOnly="readOnly" value="${thirty_days_order_ratio}" />
											<div class="input-group-addon">%</div>
										</div>
									</div>
									<div class="col-sm-3">
										<div class="input-group">
											<div class="input-group-addon">最终值</div>
											<input id="thirty_days_order_ratio_result" name="thirty_days_order_ratio_result" class="form-control" readOnly="readOnly" value="${thirty_days_order_ratio_result}" />
											<div class="input-group-addon">%</div>
										</div>
									</div>
								</div>
								
								<div class="form-group">
									<label class="col-sm-2 control-label form-label">30日平均放行时间</label>
									<div class="col-sm-3">
										<div class="input-group">
											<div class="input-group-addon">基础设置</div>
											<input onchange="updateStatistic();" id="thirty_days_pass_average_time_base" name="thirty_days_pass_average_time_base" class="form-control" value="${thirty_days_pass_average_time_base}" />
											<div class="input-group-addon">秒</div>
										</div>
									</div>
									<div class="col-sm-3">
										<div class="input-group">
											<div class="input-group-addon">统计值</div>
											<input id="thirty_days_pass_average_time" name="thirty_days_pass_average_time" class="form-control" readOnly="readOnly" value="${thirty_days_pass_average_time}" />
											<div class="input-group-addon">秒</div>
										</div>
									</div>
									<div class="col-sm-3">
										<div class="input-group">
											<div class="input-group-addon">最终值</div>
											<input id="thirty_days_pass_average_time_result" name="thirty_days_pass_average_time_result" class="form-control" readOnly="readOnly" value="${thirty_days_pass_average_time_result}" />
											<div class="input-group-addon">秒</div>
										</div>
									</div>
								</div>
								
								<div class="form-group">
									<label class="col-sm-2 control-label form-label">30日平均付款时间</label>
									<div class="col-sm-3">
										<div class="input-group">
											<div class="input-group-addon">基础设置</div>
											<input onchange="updateStatistic();" id="thirty_days_pay_average_time_base" name="thirty_days_pay_average_time_base" class="form-control" value="${thirty_days_pay_average_time_base}" />
											<div class="input-group-addon">秒</div>
										</div>
									</div>
									<div class="col-sm-3">
										<div class="input-group">
											<div class="input-group-addon">统计值</div>
											<input id="thirty_days_pay_average_time" name="thirty_days_pay_average_time" class="form-control" readOnly="readOnly" value="${thirty_days_pay_average_time}" />
											<div class="input-group-addon">秒</div>
										</div>
									</div>
									<div class="col-sm-3">
										<div class="input-group">
											<div class="input-group-addon">最终值</div>
											<input id="thirty_days_pay_average_time_result" name="thirty_days_pay_average_time_result" class="form-control" readOnly="readOnly" value="${thirty_days_pay_average_time_result}" />
											<div class="input-group-addon">秒</div>
										</div>
									</div>
								</div>
								
								<div class="form-group">
									<label class="col-sm-2 control-label form-label">30日交易量</label>
									<div class="col-sm-3">
										<div class="input-group">
											<div class="input-group-addon">基础设置</div>
											<input onchange="updateStatistic();" id="thirty_days_amount_base" name="thirty_days_amount_base" class="form-control" value="${thirty_days_amount_base}" />
											<div class="input-group-addon">USDT</div>
										</div>
									</div>
									<div class="col-sm-3">
										<div class="input-group">
											<div class="input-group-addon">统计值</div>
											<input id="thirty_days_amount" name="thirty_days_amount" class="form-control" readOnly="readOnly" value="${thirty_days_amount}" />
											<div class="input-group-addon">USDT</div>
										</div>
									</div>
									<div class="col-sm-3">
										<div class="input-group">
											<div class="input-group-addon">最终值</div>
											<input id="thirty_days_amount_result" name="thirty_days_amount_result" class="form-control" readOnly="readOnly" value="${thirty_days_amount_result}" />
											<div class="input-group-addon">USDT</div>
										</div>
									</div>
								</div>
								
								<div class="form-group">
									<label class="col-sm-2 control-label form-label">买交易量</label>
									<div class="col-sm-3">
										<div class="input-group">
											<div class="input-group-addon">基础设置</div>
											<input onchange="updateStatistic();" id="buy_amount_base" name="buy_amount_base" class="form-control" value="${buy_amount_base}" />
											<div class="input-group-addon">USDT</div>
										</div>
									</div>
									<div class="col-sm-3">
										<div class="input-group">
											<div class="input-group-addon">统计值</div>
											<input id="buy_amount" name="buy_amount" class="form-control" readOnly="readOnly" value="${buy_amount}" />
											<div class="input-group-addon">USDT</div>
										</div>
									</div>
									<div class="col-sm-3">
										<div class="input-group">
											<div class="input-group-addon">最终值</div>
											<input id="buy_amount_result" name="buy_amount_result" class="form-control" readOnly="readOnly" value="${buy_amount_result}" />
											<div class="input-group-addon">USDT</div>
										</div>
									</div>
								</div>
								
								<div class="form-group">
									<label class="col-sm-2 control-label form-label">卖交易量</label>
									<div class="col-sm-3">
										<div class="input-group">
											<div class="input-group-addon">基础设置</div>
											<input onchange="updateStatistic();" id="sell_amount_base" name="sell_amount_base" class="form-control" value="${sell_amount_base}" />
											<div class="input-group-addon">USDT</div>
										</div>
									</div>
									<div class="col-sm-3">
										<div class="input-group">
											<div class="input-group-addon">统计值</div>
											<input id="sell_amount" name="sell_amount" class="form-control" readOnly="readOnly" value="${sell_amount}" />
											<div class="input-group-addon">USDT</div>
										</div>
									</div>
									<div class="col-sm-3">
										<div class="input-group">
											<div class="input-group-addon">最终值</div>
											<input id="sell_amount_result" name="sell_amount_result" class="form-control" readOnly="readOnly" value="${sell_amount_result}" />
											<div class="input-group-addon">USDT</div>
										</div>
									</div>
								</div>
								
								<div class="form-group">
									<label class="col-sm-2 control-label form-label">总交易量</label>
									<div class="col-sm-3">
										<div class="input-group">
											<div class="input-group-addon">基础设置</div>
											<input onchange="updateStatistic();" id="total_amount_base" name="total_amount_base" class="form-control" value="${total_amount_base}" />
											<div class="input-group-addon">USDT</div>
										</div>
									</div>
									<div class="col-sm-3">
										<div class="input-group">
											<div class="input-group-addon">统计值</div>
											<input id="total_amount" name="total_amount" class="form-control" readOnly="readOnly" value="${total_amount}" />
											<div class="input-group-addon">USDT</div>
										</div>
									</div>
									<div class="col-sm-3">
										<div class="input-group">
											<div class="input-group-addon">最终值</div>
											<input id="total_amount_result" name="total_amount_result" class="form-control" readOnly="readOnly" value="${total_amount_result}" />
											<div class="input-group-addon">USDT</div>
										</div>
									</div>
								</div>
								
								<div class="form-group">
									<label class="col-sm-2 control-label form-label">账号创建天数</label>
									<div class="col-sm-3">
										<div class="input-group">
											<div class="input-group-addon">基础设置</div>
											<input onchange="updateStatistic();" id="account_create_days_base" name="account_create_days_base" class="form-control" value="${account_create_days_base}" />
											<div class="input-group-addon">天</div>
										</div>
									</div>
									<div class="col-sm-3">
										<div class="input-group">
											<div class="input-group-addon">统计值</div>
											<input id="account_create_days" name="account_create_days" class="form-control" readOnly="readOnly" value="${account_create_days}" />
											<div class="input-group-addon">天</div>
										</div>
									</div>
									<div class="col-sm-3">
										<div class="input-group">
											<div class="input-group-addon">最终值</div>
											<input id="account_create_days_result" name="account_create_days_result" class="form-control" readOnly="readOnly" value="${account_create_days_result}" />
											<div class="input-group-addon">天</div>
										</div>
									</div>
								</div>
								
								<div class="form-group">
									<label class="col-sm-2 control-label form-label">首次交易至今天数</label>
									<div class="col-sm-3">
										<div class="input-group">
											<div class="input-group-addon">基础设置</div>
											<input onchange="updateStatistic();" id="first_exchange_days_base" name="first_exchange_days_base" class="form-control" value="${first_exchange_days_base}" />
											<div class="input-group-addon">天</div>
										</div>
									</div>
									<div class="col-sm-3">
										<div class="input-group">
											<div class="input-group-addon">统计值</div>
											<input id="first_exchange_days" name="first_exchange_days" class="form-control" readOnly="readOnly" value="${first_exchange_days}" />
											<div class="input-group-addon">天</div>
										</div>
									</div>
									<div class="col-sm-3">
										<div class="input-group">
											<div class="input-group-addon">最终值</div>
											<input id="first_exchange_days_result" name="first_exchange_days_result" class="form-control" readOnly="readOnly" value="${first_exchange_days_result}" />
											<div class="input-group-addon">天</div>
										</div>
									</div>
								</div>
								
								<div class="form-group">
									<label class="col-sm-2 control-label form-label">交易人数</label>
									<div class="col-sm-3">
										<div class="input-group">
											<div class="input-group-addon">基础设置</div>
											<input onchange="updateStatistic();" id="exchange_users_base" name="exchange_users_base" class="form-control" value="${exchange_users_base}" />
											<div class="input-group-addon">人</div>
										</div>
									</div>
									<div class="col-sm-3">
										<div class="input-group">
											<div class="input-group-addon">统计值</div>
											<input id="exchange_users" name="exchange_users" class="form-control" readOnly="readOnly" value="${exchange_users}" />
											<div class="input-group-addon">人</div>
										</div>
									</div>
									<div class="col-sm-3">
										<div class="input-group">
											<div class="input-group-addon">最终值</div>
											<input id="exchange_users_result" name="exchange_users_result" class="form-control" readOnly="readOnly" value="${exchange_users_result}" />
											<div class="input-group-addon">人</div>
										</div>
									</div>
								</div>
								
								<div class="form-group">
									<label class="col-sm-2 control-label form-label">买成单数</label>
									<div class="col-sm-3">
										<div class="input-group">
											<div class="input-group-addon">基础设置</div>
											<input onchange="updateStatistic();" id="buy_success_orders_base" name="buy_success_orders_base" class="form-control" value="${buy_success_orders_base}" />
											<div class="input-group-addon">单</div>
										</div>
									</div>
									<div class="col-sm-3">
										<div class="input-group">
											<div class="input-group-addon">统计值</div>
											<input id="buy_success_orders" name="buy_success_orders" class="form-control" readOnly="readOnly" value="${buy_success_orders}" />
											<div class="input-group-addon">单</div>
										</div>
									</div>
									<div class="col-sm-3">
										<div class="input-group">
											<div class="input-group-addon">最终值</div>
											<input id="buy_success_orders_result" name="buy_success_orders_result" class="form-control" readOnly="readOnly" value="${buy_success_orders_result}" />
											<div class="input-group-addon">单</div>
										</div>
									</div>
								</div>
								
								<div class="form-group">
									<label class="col-sm-2 control-label form-label">卖成单数</label>
									<div class="col-sm-3">
										<div class="input-group">
											<div class="input-group-addon">基础设置</div>
											<input onchange="updateStatistic();" id="sell_success_orders_base" name="sell_success_orders_base" class="form-control" value="${sell_success_orders_base}" />
											<div class="input-group-addon">单</div>
										</div>
									</div>
									<div class="col-sm-3">
										<div class="input-group">
											<div class="input-group-addon">统计值</div>
											<input id="sell_success_orders" name="sell_success_orders" class="form-control" readOnly="readOnly" value="${sell_success_orders}" />
											<div class="input-group-addon">单</div>
										</div>
									</div>
									<div class="col-sm-3">
										<div class="input-group">
											<div class="input-group-addon">最终值</div>
											<input id="sell_success_orders_result" name="sell_success_orders_result" class="form-control" readOnly="readOnly" value="${sell_success_orders_result}" />
											<div class="input-group-addon">单</div>
										</div>
									</div>
								</div>
								
								<div class="form-group">
									<label class="col-sm-2 control-label form-label">总成单数</label>
									<div class="col-sm-3">
										<div class="input-group">
											<div class="input-group-addon">基础设置</div>
											<input onchange="updateStatistic();" id="total_success_orders_base" name="total_success_orders_base" class="form-control" value="${total_success_orders_base}" />
											<div class="input-group-addon">单</div>
										</div>
									</div>
									<div class="col-sm-3">
										<div class="input-group">
											<div class="input-group-addon">统计值</div>
											<input id="total_success_orders" name="total_success_orders" class="form-control" readOnly="readOnly" value="${total_success_orders}" />
											<div class="input-group-addon">单</div>
										</div>
									</div>
									<div class="col-sm-3">
										<div class="input-group">
											<div class="input-group-addon">最终值</div>
											<input id="total_success_orders_result" name="total_success_orders_result" class="form-control" readOnly="readOnly" value="${total_success_orders_result}" />
											<div class="input-group-addon">单</div>
										</div>
									</div>
								</div>
								
								<div class="form-group">
									<label class="col-sm-2 control-label form-label">好评数</label>
									<div class="col-sm-3">
										<div class="input-group">
											<div class="input-group-addon">基础设置</div>
											<input onchange="updateStatistic();" id="appraise_good_base" name="appraise_good_base" class="form-control" value="${appraise_good_base}" />
											<div class="input-group-addon">次</div>
										</div>
									</div>
									<div class="col-sm-3">
										<div class="input-group">
											<div class="input-group-addon">统计值</div>
											<input id="appraise_good" name="appraise_good" class="form-control" readOnly="readOnly" value="${appraise_good}" />
											<div class="input-group-addon">次</div>
										</div>
									</div>
									<div class="col-sm-3">
										<div class="input-group">
											<div class="input-group-addon">最终值</div>
											<input id="appraise_good_result" name="appraise_good_result" class="form-control" readOnly="readOnly" value="${appraise_good_result}" />
											<div class="input-group-addon">次</div>
										</div>
									</div>
								</div>
								
								<div class="form-group">
									<label class="col-sm-2 control-label form-label">差评数</label>
									<div class="col-sm-3">
										<div class="input-group">
											<div class="input-group-addon">基础设置</div>
											<input onchange="updateStatistic();" id="appraise_bad_base" name="appraise_bad_base" class="form-control" value="${appraise_bad_base}" />
											<div class="input-group-addon">次</div>
										</div>
									</div>
									<div class="col-sm-3">
										<div class="input-group">
											<div class="input-group-addon">统计值</div>
											<input id="appraise_bad" name="appraise_bad" class="form-control" readOnly="readOnly" value="${appraise_bad}" />
											<div class="input-group-addon">次</div>
										</div>
									</div>
									<div class="col-sm-3">
										<div class="input-group">
											<div class="input-group-addon">最终值</div>
											<input id="appraise_bad_result" name="appraise_bad_result" class="form-control" readOnly="readOnly" value="${appraise_bad_result}" />
											<div class="input-group-addon">次</div>
										</div>
									</div>
								</div>
								
								<div style="display: none">
								
									<h5>通知配置</h5>
									
									<div class="form-group">
										<label class="col-sm-2 control-label form-label"></label>
										<div class="col-sm-8">
											<p class="ballon color1">
												说明：
												<br />1.【基础设置】是后台配置的，【用户设置】取决于用户前端操作设置；
												<br />2.【基础设置】和【用户设置】只要有一个为“打开”，【最终状态】就为“打开”；
											</p>
										</div>
									</div>								
									
									<div class="form-group">
										<label class="col-sm-2 control-label form-label">订单邮件通知</label>
										<div class="col-sm-3">	
											<div class="input-group">
												<div class="input-group-addon" >基础设置</div>									
												<select onchange="updateNotice();" id="order_mail_notice_open_base" name="order_mail_notice_open_base" class="form-control" >
													<option value="0" <c:if test="${order_mail_notice_open_base=='0'}">selected="true"</c:if>>关闭</option>
													<option value="1" <c:if test="${order_mail_notice_open_base=='1'}">selected="true"</c:if>>打开</option>
												</select>
											</div>
										</div>
										<div class="col-sm-3">
											<div class="input-group">
												<div class="input-group-addon" >用户设置</div>
												<select id="order_mail_notice_open" name="order_mail_notice_open" class="form-control" disabled="disabled" >
													<option value="0" <c:if test="${order_mail_notice_open=='0'}">selected="true"</c:if>>关闭</option>
													<option value="1" <c:if test="${order_mail_notice_open=='1'}">selected="true"</c:if>>打开</option>
												</select>
											</div>
										</div>
										<div class="col-sm-3">
											<div class="input-group">
												<div class="input-group-addon" >最终设置</div>
												<select id="order_mail_notice_open_result" name="order_mail_notice_open_result" class="form-control" disabled="disabled" >
													<option value="0" <c:if test="${order_mail_notice_open_result=='0'}">selected="true"</c:if>>关闭</option>
													<option value="1" <c:if test="${order_mail_notice_open_result=='1'}">selected="true"</c:if>>打开</option>
												</select>
											</div>
										</div>
									</div>
									
									<div class="form-group">
										<label class="col-sm-2 control-label form-label">订单短信通知</label>
										<div class="col-sm-3">	
											<div class="input-group">
												<div class="input-group-addon" >基础设置</div>										
												<select onchange="updateNotice();" id="order_sms_notice_open_base" name="order_sms_notice_open_base" class="form-control" >
													<option value="0" <c:if test="${order_sms_notice_open_base=='0'}">selected="true"</c:if>>关闭</option>
													<option value="1" <c:if test="${order_sms_notice_open_base=='1'}">selected="true"</c:if>>打开</option>
												</select>
											</div>
										</div>
										<div class="col-sm-3">	
											<div class="input-group">
												<div class="input-group-addon" >用户设置</div>
												<select id="order_sms_notice_open" name="order_sms_notice_open" class="form-control" disabled="disabled" >
													<option value="0" <c:if test="${order_sms_notice_open=='0'}">selected="true"</c:if>>关闭</option>
													<option value="1" <c:if test="${order_sms_notice_open=='1'}">selected="true"</c:if>>打开</option>
												</select>
											</div>
										</div>
										<div class="col-sm-3">	
											<div class="input-group">
												<div class="input-group-addon" >最终设置</div>
												<select id="order_sms_notice_open_result" name="order_sms_notice_open_result" class="form-control" disabled="disabled" >
													<option value="0" <c:if test="${order_sms_notice_open_result=='0'}">selected="true"</c:if>>关闭</option>
													<option value="1" <c:if test="${order_sms_notice_open_result=='1'}">selected="true"</c:if>>打开</option>
												</select>
											</div>
										</div>
									</div>
									
									<div class="form-group">
										<label class="col-sm-2 control-label form-label">订单APP通知</label>
										<div class="col-sm-3">	
											<div class="input-group">
												<div class="input-group-addon" >基础设置</div>										
												<select onchange="updateNotice();" id="order_app_notice_open_base" name="order_app_notice_open_base" class="form-control" >
													<option value="0" <c:if test="${order_app_notice_open_base=='0'}">selected="true"</c:if>>关闭</option>
													<option value="1" <c:if test="${order_app_notice_open_base=='1'}">selected="true"</c:if>>打开</option>
												</select>
											</div>
										</div>
										<div class="col-sm-3">	
											<div class="input-group">
												<div class="input-group-addon" >用户设置</div>
												<select id="order_app_notice_open" name="order_app_notice_open" class="form-control" disabled="disabled" >
													<option value="0" <c:if test="${order_app_notice_open=='0'}">selected="true"</c:if>>关闭</option>
													<option value="1" <c:if test="${order_app_notice_open=='1'}">selected="true"</c:if>>打开</option>
												</select>
											</div>
										</div>
										<div class="col-sm-3">	
											<div class="input-group">
												<div class="input-group-addon" >最终设置</div>
												<select id="order_app_notice_open_result" name="order_app_notice_open_result" class="form-control" disabled="disabled" >
													<option value="0" <c:if test="${order_app_notice_open_result=='0'}">selected="true"</c:if>>关闭</option>
													<option value="1" <c:if test="${order_app_notice_open_result=='1'}">selected="true"</c:if>>打开</option>
												</select>
											</div>
										</div>
									</div>
									
									<div class="form-group">
										<label class="col-sm-2 control-label form-label">申诉邮件通知</label>
										<div class="col-sm-3">	
											<div class="input-group">
												<div class="input-group-addon" >基础设置</div>
												<select onchange="updateNotice();" id="appeal_mail_notice_open_base" name="appeal_mail_notice_open_base" class="form-control" >
													<option value="0" <c:if test="${appeal_mail_notice_open_base=='0'}">selected="true"</c:if>>关闭</option>
													<option value="1" <c:if test="${appeal_mail_notice_open_base=='1'}">selected="true"</c:if>>打开</option>
												</select>
											</div>
										</div>
										<div class="col-sm-3">
											<div class="input-group">
												<div class="input-group-addon" >用户设置</div>
												<select id="appeal_mail_notice_open" name="appeal_mail_notice_open" class="form-control" disabled="disabled" >
													<option value="0" <c:if test="${appeal_mail_notice_open=='0'}">selected="true"</c:if>>关闭</option>
													<option value="1" <c:if test="${appeal_mail_notice_open=='1'}">selected="true"</c:if>>打开</option>
												</select>
											</div>
										</div>
										<div class="col-sm-3">
											<div class="input-group">
												<div class="input-group-addon" >最终设置</div>
												<select id="appeal_mail_notice_open_result" name="appeal_mail_notice_open_result" class="form-control" disabled="disabled" >
													<option value="0" <c:if test="${appeal_mail_notice_open_result=='0'}">selected="true"</c:if>>关闭</option>
													<option value="1" <c:if test="${appeal_mail_notice_open_result=='1'}">selected="true"</c:if>>打开</option>
												</select>
											</div>
										</div>
									</div>
									
									<div class="form-group">
										<label class="col-sm-2 control-label form-label">申诉短信通知</label>
										<div class="col-sm-3">		
											<div class="input-group">
												<div class="input-group-addon" >基础设置</div>									
												<select onchange="updateNotice();" id="appeal_sms_notice_open_base" name="appeal_sms_notice_open_base" class="form-control" >
													<option value="0" <c:if test="${appeal_sms_notice_open_base=='0'}">selected="true"</c:if>>关闭</option>
													<option value="1" <c:if test="${appeal_sms_notice_open_base=='1'}">selected="true"</c:if>>打开</option>
												</select>
											</div>
										</div>
										<div class="col-sm-3">	
											<div class="input-group">
												<div class="input-group-addon" >用户设置</div>
												<select id="appeal_sms_notice_open" name="appeal_sms_notice_open" class="form-control" disabled="disabled" >
													<option value="0" <c:if test="${appeal_sms_notice_open=='0'}">selected="true"</c:if>>关闭</option>
													<option value="1" <c:if test="${appeal_sms_notice_open=='1'}">selected="true"</c:if>>打开</option>
												</select>
											</div>
										</div>
										<div class="col-sm-3">	
											<div class="input-group">
												<div class="input-group-addon" >最终设置</div>
												<select id="appeal_sms_notice_open_result" name="appeal_sms_notice_open_result" class="form-control" disabled="disabled" >
													<option value="0" <c:if test="${appeal_sms_notice_open_result=='0'}">selected="true"</c:if>>关闭</option>
													<option value="1" <c:if test="${appeal_sms_notice_open_result=='1'}">selected="true"</c:if>>打开</option>
												</select>
											</div>
										</div>
									</div>
									
									<div class="form-group">
										<label class="col-sm-2 control-label form-label">申诉APP通知</label>									
										<div class="col-sm-3">
											<div class="input-group">
												<div class="input-group-addon" >基础设置</div>
												<select onchange="updateNotice();" id="appeal_app_notice_open_base" name="appeal_app_notice_open_base" class="form-control" >
													<option value="0" <c:if test="${appeal_app_notice_open_base=='0'}">selected="true"</c:if>>关闭</option>
													<option value="1" <c:if test="${appeal_app_notice_open_base=='1'}">selected="true"</c:if>>打开</option>
												</select>
											</div>
										</div>
										<div class="col-sm-3">		
											<div class="input-group">
												<div class="input-group-addon" >用户设置</div>									
												<select id="appeal_app_notice_open" name="appeal_app_notice_open" class="form-control" disabled="disabled" >
													<option value="0" <c:if test="${appeal_app_notice_open=='0'}">selected="true"</c:if>>关闭</option>
													<option value="1" <c:if test="${appeal_app_notice_open=='1'}">selected="true"</c:if>>打开</option>
												</select>
											</div>
										</div>
										<div class="col-sm-3">		
											<div class="input-group">
												<div class="input-group-addon" >最终设置</div>									
												<select id="appeal_app_notice_open_result" name="appeal_app_notice_open_result" class="form-control" disabled="disabled" >
													<option value="0" <c:if test="${appeal_app_notice_open_result=='0'}">selected="true"</c:if>>关闭</option>
													<option value="1" <c:if test="${appeal_app_notice_open_result=='1'}">selected="true"</c:if>>打开</option>
												</select>
											</div>
										</div>
									</div>
									
									<div class="form-group">
										<label class="col-sm-2 control-label form-label">聊天APP通知</label>
										<div class="col-sm-3">
											<div class="input-group">
												<div class="input-group-addon" >基础设置</div>										
												<select onchange="updateNotice();" id="chat_app_notice_open_base" name="chat_app_notice_open_base" class="form-control" >
													<option value="0" <c:if test="${chat_app_notice_open_base=='0'}">selected="true"</c:if>>关闭</option>
													<option value="1" <c:if test="${chat_app_notice_open_base=='1'}">selected="true"</c:if>>打开</option>
												</select>
											</div>
										</div>
										<div class="col-sm-3">		
											<div class="input-group">
												<div class="input-group-addon" >用户设置</div>										
												<select id="chat_app_notice_open" name="chat_app_notice_open" class="form-control" disabled="disabled" >
													<option value="0" <c:if test="${chat_app_notice_open=='0'}">selected="true"</c:if>>关闭</option>
													<option value="1" <c:if test="${chat_app_notice_open=='1'}">selected="true"</c:if>>打开</option>
												</select>
											</div>
										</div>
										<div class="col-sm-3">		
											<div class="input-group">
												<div class="input-group-addon" >最终设置</div>										
												<select id="chat_app_notice_open_result" name="chat_app_notice_open_result" class="form-control" disabled="disabled" >
													<option value="0" <c:if test="${chat_app_notice_open_result=='0'}">selected="true"</c:if>>关闭</option>
													<option value="1" <c:if test="${chat_app_notice_open_result=='1'}">selected="true"</c:if>>打开</option>
												</select>
											</div>
										</div>
									</div>
									
									<div class="form-group">
										<label class="col-sm-2 control-label form-label">安全邮件通知</label>
										<div class="col-sm-3">
											<div class="input-group">
												<div class="input-group-addon" >基础设置</div>										
												<select onchange="updateNotice();" id="security_mail_notice_open_base" name="security_mail_notice_open_base" class="form-control" >
													<option value="0" <c:if test="${security_mail_notice_open_base=='0'}">selected="true"</c:if>>关闭</option>
													<option value="1" <c:if test="${security_mail_notice_open_base=='1'}">selected="true"</c:if>>打开</option>
												</select>
											</div>
										</div>
										<div class="col-sm-3">		
											<div class="input-group">
												<div class="input-group-addon" >用户设置</div>
												<select id="security_mail_notice_open" name="security_mail_notice_open" class="form-control" disabled="disabled" >
													<option value="0" <c:if test="${security_mail_notice_open=='0'}">selected="true"</c:if>>关闭</option>
													<option value="1" <c:if test="${security_mail_notice_open=='1'}">selected="true"</c:if>>打开</option>
												</select>
											</div>
										</div>
										<div class="col-sm-3">		
											<div class="input-group">
												<div class="input-group-addon" >最终设置</div>
												<select id="security_mail_notice_open_result" name="security_mail_notice_open_result" class="form-control" disabled="disabled" >
													<option value="0" <c:if test="${security_mail_notice_open_result=='0'}">selected="true"</c:if>>关闭</option>
													<option value="1" <c:if test="${security_mail_notice_open_result=='1'}">selected="true"</c:if>>打开</option>
												</select>
											</div>
										</div>
									</div>
									
									<div class="form-group">
										<label class="col-sm-2 control-label form-label">安全短信通知</label>
										<div class="col-sm-3">
											<div class="input-group">
												<div class="input-group-addon" >基础设置</div>										
												<select onchange="updateNotice();" id="security_sms_notice_open_base" name="security_sms_notice_open_base" class="form-control" >
													<option value="0" <c:if test="${security_sms_notice_open_base=='0'}">selected="true"</c:if>>关闭</option>
													<option value="1" <c:if test="${security_sms_notice_open_base=='1'}">selected="true"</c:if>>打开</option>
												</select>
											</div>
										</div>
										<div class="col-sm-3">		
											<div class="input-group">
												<div class="input-group-addon" >用户设置</div>
												<select id="security_sms_notice_open" name="security_sms_notice_open" class="form-control" disabled="disabled" >
													<option value="0" <c:if test="${security_sms_notice_open=='0'}">selected="true"</c:if>>关闭</option>
													<option value="1" <c:if test="${security_sms_notice_open=='1'}">selected="true"</c:if>>打开</option>
												</select>
											</div>
										</div>
										<div class="col-sm-3">		
											<div class="input-group">
												<div class="input-group-addon" >最终设置</div>
												<select id="security_sms_notice_open_result" name="security_sms_notice_open_result" class="form-control" disabled="disabled" >
													<option value="0" <c:if test="${security_sms_notice_open_result=='0'}">selected="true"</c:if>>关闭</option>
													<option value="1" <c:if test="${security_sms_notice_open_result=='1'}">selected="true"</c:if>>打开</option>
												</select>
											</div>
										</div>
									</div>
									
									<div class="form-group">
										<label class="col-sm-2 control-label form-label">安全APP通知</label>
										<div class="col-sm-3">
											<div class="input-group">
												<div class="input-group-addon" >基础设置</div>											
												<select onchange="updateNotice();" id="security_app_notice_open_base" name="security_app_notice_open_base" class="form-control" >
													<option value="0" <c:if test="${security_app_notice_open_base=='0'}">selected="true"</c:if>>关闭</option>
													<option value="1" <c:if test="${security_app_notice_open_base=='1'}">selected="true"</c:if>>打开</option>
												</select>
											</div>
										</div>
										<div class="col-sm-3">		
											<div class="input-group">
												<div class="input-group-addon" >用户设置</div>										
												<select id="security_app_notice_open" name="security_app_notice_open" class="form-control" disabled="disabled" >
													<option value="0" <c:if test="${security_app_notice_open=='0'}">selected="true"</c:if>>关闭</option>
													<option value="1" <c:if test="${security_app_notice_open=='1'}">selected="true"</c:if>>打开</option>
												</select>
											</div>
										</div>
										<div class="col-sm-3">		
											<div class="input-group">
												<div class="input-group-addon" >最终设置</div>										
												<select id="security_app_notice_open_result" name="security_app_notice_open_result" class="form-control" disabled="disabled" >
													<option value="0" <c:if test="${security_app_notice_open_result=='0'}">selected="true"</c:if>>关闭</option>
													<option value="1" <c:if test="${security_app_notice_open_result=='1'}">selected="true"</c:if>>打开</option>
												</select>
											</div>
										</div>
									</div>
									
								</div>
								
								<h5>验证状态</h5>			
								
								<div class="form-group">
									<label class="col-sm-2 control-label form-label"></label>
									<div class="col-sm-8">
										<p class="ballon color1">
											说明：
											<br />1.【基础设置】是后台配置的，【用户状态】取决于用户前端操作验证认证；
											<br />2.【基础设置】和【用户状态】只要有一个为“已验证”，【最终状态】就为“已验证”；
										</p>
									</div>
								</div>
								
								<div class="form-group">
									<label class="col-sm-2 control-label form-label">手机验证状态</label>
									<div class="col-sm-3">	
										<div class="input-group">
											<div class="input-group-addon" >基础设置</div>									
											<select onchange="updateVerif();" id="phone_authority_base" name="phone_authority_base" class="form-control" >
												<option value="N" <c:if test="${phone_authority_base=='N'}">selected="true"</c:if>>未验证</option>
												<option value="Y" <c:if test="${phone_authority_base=='Y'}">selected="true"</c:if>>已验证</option>
											</select>
										</div>
									</div>
									<div class="col-sm-3">
										<div class="input-group">
											<div class="input-group-addon" >用户状态</div>
											<select id="phone_authority" name="phone_authority" class="form-control" disabled="disabled" >
												<option value="N" <c:if test="${phone_authority=='N'}">selected="true"</c:if>>未验证</option>
												<option value="Y" <c:if test="${phone_authority=='Y'}">selected="true"</c:if>>已验证</option>
											</select>
										</div>
									</div>
									<div class="col-sm-3">
										<div class="input-group">
											<div class="input-group-addon" >最终状态</div>
											<select id="phone_authority_result" name="phone_authority_result" class="form-control" disabled="disabled" >
												<option value="N" <c:if test="${phone_authority_result=='N'}">selected="true"</c:if>>未验证</option>
												<option value="Y" <c:if test="${phone_authority_result=='Y'}">selected="true"</c:if>>已验证</option>
											</select>
										</div>
									</div>
								</div>
								
								<div class="form-group">
									<label class="col-sm-2 control-label form-label">邮箱验证状态</label>
									<div class="col-sm-3">	
										<div class="input-group">
											<div class="input-group-addon" >基础设置</div>
											<select onchange="updateVerif();" id="email_authority_base" name="email_authority_base" class="form-control" >
												<option value="N" <c:if test="${email_authority_base=='N'}">selected="true"</c:if>>未验证</option>
												<option value="Y" <c:if test="${email_authority_base=='Y'}">selected="true"</c:if>>已验证</option>
											</select>
										</div>
									</div>
									<div class="col-sm-3">
										<div class="input-group">
											<div class="input-group-addon" >用户状态</div>
											<select id="email_authority" name="email_authority" class="form-control" disabled="disabled" >
												<option value="N" <c:if test="${email_authority=='N'}">selected="true"</c:if>>未验证</option>
												<option value="Y" <c:if test="${email_authority=='Y'}">selected="true"</c:if>>已验证</option>
											</select>
										</div>
									</div>
									<div class="col-sm-3">
										<div class="input-group">
											<div class="input-group-addon" >最终状态</div>
											<select id="email_authority_result" name="email_authority_result" class="form-control" disabled="disabled" >
												<option value="N" <c:if test="${email_authority_result=='N'}">selected="true"</c:if>>未验证</option>
												<option value="Y" <c:if test="${email_authority_result=='Y'}">selected="true"</c:if>>已验证</option>
											</select>
										</div>
									</div>
								</div>
								
								<div class="form-group">
									<label class="col-sm-2 control-label form-label">身份认证状态</label>
									<div class="col-sm-3">	
										<div class="input-group">
											<div class="input-group-addon" >基础设置</div>
											<select onchange="updateVerif();" id="kyc_authority_base" name="kyc_authority_base" class="form-control" >
												<option value="N" <c:if test="${kyc_authority_base=='N'}">selected="true"</c:if>>未验证</option>
												<option value="Y" <c:if test="${kyc_authority_base=='Y'}">selected="true"</c:if>>已验证</option>
											</select>
										</div>
									</div>
									<div class="col-sm-3">
										<div class="input-group">
											<div class="input-group-addon" >用户状态</div>
											<select id="kyc_authority" name="kyc_authority" class="form-control" disabled="disabled" >
												<option value="N" <c:if test="${kyc_authority=='N'}">selected="true"</c:if>>未验证</option>
												<option value="Y" <c:if test="${kyc_authority=='Y'}">selected="true"</c:if>>已验证</option>
											</select>
										</div>
									</div>
									<div class="col-sm-3">
										<div class="input-group">
											<div class="input-group-addon" >最终状态</div>
											<select id="kyc_authority_result" name="kyc_authority_result" class="form-control" disabled="disabled" >
												<option value="N" <c:if test="${kyc_authority_result=='N'}">selected="true"</c:if>>未验证</option>
												<option value="Y" <c:if test="${kyc_authority_result=='Y'}">selected="true"</c:if>>已验证</option>
											</select>
										</div>
									</div>
								</div>
								
								<div class="form-group">
									<label class="col-sm-2 control-label form-label">高级认证状态</label>
									<div class="col-sm-3">	
										<div class="input-group">
											<div class="input-group-addon" >基础设置</div>
											<select onchange="updateVerif();" id="kyc_highlevel_authority_base" name="kyc_highlevel_authority_base" class="form-control" >
												<option value="N" <c:if test="${kyc_highlevel_authority_base=='N'}">selected="true"</c:if>>未验证</option>
												<option value="Y" <c:if test="${kyc_highlevel_authority_base=='Y'}">selected="true"</c:if>>已验证</option>
											</select>
										</div>
									</div>
									<div class="col-sm-3">
										<div class="input-group">
											<div class="input-group-addon" >用户状态</div>
											<select id="kyc_highlevel_authority" name="kyc_highlevel_authority" class="form-control" disabled="disabled" >
												<option value="N" <c:if test="${kyc_highlevel_authority=='N'}">selected="true"</c:if>>未验证</option>
												<option value="Y" <c:if test="${kyc_highlevel_authority=='Y'}">selected="true"</c:if>>已验证</option>
											</select>
										</div>
									</div>
									<div class="col-sm-3">
										<div class="input-group">
											<div class="input-group-addon" >最终状态</div>
											<select id="kyc_highlevel_authority_result" name="kyc_highlevel_authority_result" class="form-control" disabled="disabled" >
												<option value="N" <c:if test="${kyc_highlevel_authority_result=='N'}">selected="true"</c:if>>未验证</option>
												<option value="Y" <c:if test="${kyc_highlevel_authority_result=='Y'}">selected="true"</c:if>>已验证</option>
											</select>
										</div>
									</div>
								</div>

								<div class="form-group">
									<label for="input002" class="col-sm-2 control-label form-label">备注</label>
									<div class="col-sm-8">
										<textarea id="remark" name="remark" class="form-control input-lg" rows="4" cols="20" >${remark}</textarea>									
									</div>
								</div>

				                <div class="col-sm-1">
									<!-- 模态框（Modal） -->
									<div class="modal fade" id="modal_succeeded" tabindex="-1"
										role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
										<div class="modal-dialog">
											<div class="modal-content" >
											
												<div class="modal-header">
													<button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
													<h4 class="modal-title" id="myModalLabel">确认修改</h4>
												</div>
												
												<div class="modal-body">
												
													<div class="form-group" >
														<label for="input002" class="col-sm-3 control-label form-label">登录人资金密码</label>
														<div class="col-sm-4">
															<input id="login_safeword" type="password" name="login_safeword"
																class="login_safeword" placeholder="请输入登录人资金密码" >
														</div>
													</div>

												</div>
												
												<div class="modal-footer" style="margin-top: 0;">
													<button type="button" class="btn " data-dismiss="modal">关闭</button>
													<button id="sub" type="submit" class="btn btn-default" >确认</button>
												</div>
												
											</div>
											<!-- /.modal-content -->
										</div>
										<!-- /.modal -->
									</div>
								</div>

								<div class="form-group">
									<div class="col-sm-offset-2 col-sm-10">
										<a href="javascript:goUrl(${pageNo})" class="btn">取消</a> 
										<a href="javascript:submit()" class="btn btn-default">保存</a>
									</div>
								</div>

							</form>

						</div>

					</div>
				</div>
			</div>

		</div>
		<!-- END CONTAINER -->
		<!-- //////////////////////////////////////////////////////////////////////////// -->

		<%@ include file="include/footer.jsp"%>

	</div>
	<!-- End Content -->
	<!-- //////////////////////////////////////////////////////////////////////////// -->

	<%@ include file="include/js.jsp"%>

	<script type="text/javascript">
	
		// 初始化执行一次
		setTimeout(function() {
			start();	  
		}, 100);
		
		function start() {			
			var img_url = $("#img_url").val();
			var show_img_content = document.getElementById('show_img_content');
			show_img_content.src="<%=base%>wap/public/showimg!showImg.action?imagePath="+img_url;
			
			updateStatistic();
			updateNotice();
			updateVerif();
		}
		
		function upload_img() {
			var fileReader = new FileReader();
			var formData = new FormData();
			var file = document.getElementById('fileName_img').files[0];
			formData.append("file", file);
			$.ajax({
		    	type: "POST",
		        url: "<%=basePath%>normal/uploadimg!execute.action?random=" + Math.random(),
				data: formData,
				dataType: "json",
			    contentType: false,  
			    processData: false, 
				success : function(data) {
					$("#img_url").val(data.data)
					var show_img = document.getElementById('show_img_content');
					show_img.src="<%=base%>wap/public/showimg!showImg.action?imagePath=" + data.data;
				},
				error : function(XMLHttpRequest, textStatus, errorThrown) {
					console.log("请求错误");
				}
			});
		}
		
		function addC2cManager() {
			$('#queryFormSystemUser').submit();
		}
	
		function submit() {
			$('#modal_succeeded').modal("show");
		}
		
		function updateStatistic() {
			
			var thirty_days_order_base = $("#thirty_days_order_base").val();
			if (null == thirty_days_order_base || "" == thirty_days_order_base) {thirty_days_order_base = 0;$("#thirty_days_order_base").val(0)}
			var thirty_days_order = $("#thirty_days_order").val();
			if (null == thirty_days_order || "" == thirty_days_order) {thirty_days_order = 0;$("#thirty_days_order").val(0)}
			$("#thirty_days_order_result").val(thirty_days_order_base * 1 + thirty_days_order * 1);

			var thirty_days_order_ratio_base = $("#thirty_days_order_ratio_base").val();
			if (null == thirty_days_order_ratio_base || "" == thirty_days_order_ratio_base) {thirty_days_order_ratio_base = 0;$("#thirty_days_order_ratio_base").val(0)}
			var thirty_days_order_ratio = $("#thirty_days_order_ratio").val();
			if (null == thirty_days_order_ratio || "" == thirty_days_order_ratio) {thirty_days_order_ratio = 0;$("#thirty_days_order_ratio").val(0)}
			if (0 != thirty_days_order_ratio_base) {
				$("#thirty_days_order_ratio_result").val(thirty_days_order_ratio_base);
			} else {
				$("#thirty_days_order_ratio_result").val(thirty_days_order_ratio);
			}

			var thirty_days_pass_average_time_base = $("#thirty_days_pass_average_time_base").val();
			if (null == thirty_days_pass_average_time_base || "" == thirty_days_pass_average_time_base) {thirty_days_pass_average_time_base = 0;$("#thirty_days_pass_average_time_base").val(0)}
			var thirty_days_pass_average_time = $("#thirty_days_pass_average_time").val();
			if (null == thirty_days_pass_average_time || "" == thirty_days_pass_average_time) {thirty_days_pass_average_time = 0;$("#thirty_days_pass_average_time").val(0)}
			if (0 != thirty_days_pass_average_time_base) {
				$("#thirty_days_pass_average_time_result").val(thirty_days_pass_average_time_base);
			} else {
				$("#thirty_days_pass_average_time_result").val(thirty_days_pass_average_time);
			}

			var thirty_days_pay_average_time_base = $("#thirty_days_pay_average_time_base").val();
			if (null == thirty_days_pay_average_time_base || "" == thirty_days_pay_average_time_base) {thirty_days_pay_average_time_base = 0;$("#thirty_days_pay_average_time_base").val(0)}
			var thirty_days_pay_average_time = $("#thirty_days_pay_average_time").val();
			if (null == thirty_days_pay_average_time || "" == thirty_days_pay_average_time) {thirty_days_pay_average_time = 0;$("#thirty_days_pay_average_time").val(0)}
			if (0 != thirty_days_pay_average_time_base) {
				$("#thirty_days_pay_average_time_result").val(thirty_days_pay_average_time_base);
			} else {
				$("#thirty_days_pay_average_time_result").val(thirty_days_pay_average_time);
			}
			
			var thirty_days_amount_base = $("#thirty_days_amount_base").val();
			if (null == thirty_days_amount_base || "" == thirty_days_amount_base) {thirty_days_amount_base = 0;$("#thirty_days_amount_base").val(0)}
			var thirty_days_amount = $("#thirty_days_amount").val();
			if (null == thirty_days_amount || "" == thirty_days_amount) {thirty_days_amount = 0;$("#thirty_days_amount").val(0)}
			$("#thirty_days_amount_result").val(thirty_days_amount_base * 1 + thirty_days_amount * 1);
			
			var buy_amount_base = $("#buy_amount_base").val();
			if (null == buy_amount_base || "" == buy_amount_base) {buy_amount_base = 0;$("#buy_amount_base").val(0)}
			var buy_amount = $("#buy_amount").val();
			if (null == buy_amount || "" == buy_amount) {buy_amount = 0;$("#buy_amount").val(0)}
			$("#buy_amount_result").val(buy_amount_base * 1 + buy_amount * 1);
			
			var sell_amount_base = $("#sell_amount_base").val();
			if (null == sell_amount_base || "" == sell_amount_base) {sell_amount_base = 0;$("#sell_amount_base").val(0)}
			var sell_amount = $("#sell_amount").val();
			if (null == sell_amount || "" == sell_amount) {sell_amount = 0;$("#sell_amount").val(0)}
			$("#sell_amount_result").val(sell_amount_base * 1 + sell_amount * 1);
			
			var total_amount_base = $("#total_amount_base").val();
			if (null == total_amount_base || "" == total_amount_base) {total_amount_base = 0;$("#total_amount_base").val(0)}
			var total_amount = $("#total_amount").val();
			if (null == total_amount || "" == total_amount) {total_amount = 0;$("#total_amount").val(0)}
			$("#total_amount_result").val(total_amount_base * 1 + total_amount * 1);
			
			var account_create_days_base = $("#account_create_days_base").val();
			if (null == account_create_days_base || "" == account_create_days_base) {account_create_days_base = 0;$("#account_create_days_base").val(0)}
			var account_create_days = $("#account_create_days").val();
			if (null == account_create_days || "" == account_create_days) {account_create_days = 0;$("#account_create_days").val(0)}
			$("#account_create_days_result").val(account_create_days_base * 1 + account_create_days * 1);
			
			var first_exchange_days_base = $("#first_exchange_days_base").val();
			if (null == first_exchange_days_base || "" == first_exchange_days_base) {first_exchange_days_base = 0;$("#first_exchange_days_base").val(0)}
			var first_exchange_days = $("#first_exchange_days").val();
			if (null == first_exchange_days || "" == first_exchange_days) {first_exchange_days = 0;$("#first_exchange_days").val(0)}
			$("#first_exchange_days_result").val(first_exchange_days_base * 1 + first_exchange_days * 1);
			
			var exchange_users_base = $("#exchange_users_base").val();
			if (null == exchange_users_base || "" == exchange_users_base) {exchange_users_base = 0;$("#exchange_users_base").val(0)}
			var exchange_users = $("#exchange_users").val();
			if (null == exchange_users || "" == exchange_users) {exchange_users = 0;$("#exchange_users").val(0)}
			$("#exchange_users_result").val(exchange_users_base * 1 + exchange_users * 1);
			
			var buy_success_orders_base = $("#buy_success_orders_base").val();
			if (null == buy_success_orders_base || "" == buy_success_orders_base) {buy_success_orders_base = 0;$("#buy_success_orders_base").val(0)}
			var buy_success_orders = $("#buy_success_orders").val();
			if (null == buy_success_orders || "" == buy_success_orders) {buy_success_orders = 0;$("#buy_success_orders").val(0)}
			$("#buy_success_orders_result").val(buy_success_orders_base * 1 + buy_success_orders * 1);
			
			var sell_success_orders_base = $("#sell_success_orders_base").val();
			if (null == sell_success_orders_base || "" == sell_success_orders_base) {sell_success_orders_base = 0;$("#sell_success_orders_base").val(0)}
			var sell_success_orders = $("#sell_success_orders").val();
			if (null == sell_success_orders || "" == sell_success_orders) {sell_success_orders = 0;$("#sell_success_orders").val(0)}
			$("#sell_success_orders_result").val(sell_success_orders_base * 1 + sell_success_orders * 1);
			
			var total_success_orders_base = $("#total_success_orders_base").val();
			if (null == total_success_orders_base || "" == total_success_orders_base) {total_success_orders_base = 0;$("#total_success_orders_base").val(0)}
			var total_success_orders = $("#total_success_orders").val();
			if (null == total_success_orders || "" == total_success_orders) {total_success_orders = 0;$("#total_success_orders").val(0)}
			$("#total_success_orders_result").val(total_success_orders_base * 1 + total_success_orders * 1);
			
			var appraise_good_base = $("#appraise_good_base").val();
			if (null == appraise_good_base || "" == appraise_good_base) {appraise_good_base = 0;$("#appraise_good_base").val(0)}
			var appraise_good = $("#appraise_good").val();
			if (null == appraise_good || "" == appraise_good) {appraise_good = 0;$("#appraise_good").val(0)}
			$("#appraise_good_result").val(appraise_good_base * 1 + appraise_good * 1);
			
			var appraise_bad_base = $("#appraise_bad_base").val();
			if (null == appraise_bad_base || "" == appraise_bad_base) {appraise_bad_base = 0;$("#appraise_bad_base").val(0)}
			var appraise_bad = $("#appraise_bad").val();
			if (null == appraise_bad || "" == appraise_bad) {appraise_bad = 0;$("#appraise_bad").val(0)}
			$("#appraise_bad_result").val(appraise_bad_base * 1 + appraise_bad * 1);
		}
		
		function updateNotice() {
			if ("1" == $("#order_mail_notice_open_base").val() || "1" == $("#order_mail_notice_open").val()) {
				$("#order_mail_notice_open_result").val("1");} else {$("#order_mail_notice_open_result").val("0");}
			if ("1" == $("#order_sms_notice_open_base").val() || "1" == $("#order_sms_notice_open").val()) {
				$("#order_sms_notice_open_result").val("1");} else {$("#order_sms_notice_open_result").val("0");}
			if ("1" == $("#order_app_notice_open_base").val() || "1" == $("#order_app_notice_open").val()) {
				$("#order_app_notice_open_result").val("1");} else {$("#order_app_notice_open_result").val("0");}
			if ("1" == $("#appeal_mail_notice_open_base").val() || "1" == $("#appeal_mail_notice_open").val()) {
				$("#appeal_mail_notice_open_result").val("1");} else {$("#appeal_mail_notice_open_result").val("0");}
			if ("1" == $("#appeal_sms_notice_open_base").val() || "1" == $("#appeal_sms_notice_open").val()) {
				$("#appeal_sms_notice_open_result").val("1");} else {$("#appeal_sms_notice_open_result").val("0");}
			if ("1" == $("#appeal_app_notice_open_base").val() || "1" == $("#appeal_app_notice_open").val()) {
				$("#appeal_app_notice_open_result").val("1");} else {$("#appeal_app_notice_open_result").val("0");}
			if ("1" == $("#chat_app_notice_open_base").val() || "1" == $("#chat_app_notice_open").val()) {
				$("#chat_app_notice_open_result").val("1");} else {$("#chat_app_notice_open_result").val("0");}
			if ("1" == $("#security_mail_notice_open_base").val() || "1" == $("#security_mail_notice_open").val()) {
				$("#security_mail_notice_open_result").val("1");} else {$("#security_mail_notice_open_result").val("0");}
			if ("1" == $("#security_sms_notice_open_base").val() || "1" == $("#security_sms_notice_open").val()) {
				$("#security_sms_notice_open_result").val("1");} else {$("#security_sms_notice_open_result").val("0");}
			if ("1" == $("#security_app_notice_open_base").val() || "1" == $("#security_app_notice_open").val()) {
				$("#security_app_notice_open_result").val("1");} else {$("#security_app_notice_open_result").val("0");}
		}
		
		function updateVerif() {
			if ("Y" == $("#phone_authority_base").val() || "Y" == $("#phone_authority").val()) {
				$("#phone_authority_result").val("Y");} else {$("#phone_authority_result").val("N");}
			if ("Y" == $("#email_authority_base").val() || "Y" == $("#email_authority").val()) {
				$("#email_authority_result").val("Y");} else {$("#email_authority_result").val("N");}
			if ("Y" == $("#kyc_authority_base").val() || "Y" == $("#kyc_authority").val()) {
				$("#kyc_authority_result").val("Y");} else {$("#kyc_authority_result").val("N");}
			if ("Y" == $("#kyc_highlevel_authority_base").val() || "Y" == $("#kyc_highlevel_authority").val()) {
				$("#kyc_highlevel_authority_result").val("Y");} else {$("#kyc_highlevel_authority_result").val("N");}
		}
		
	</script>
	
</body>

</html>
