<%@ page language="java" pageEncoding="utf-8" isELIgnored="false"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

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
			<h3>C2C广告配置</h3>
			
			<%@ include file="include/alert.jsp"%>
			
			<!-- //////////////////////////////////////////////////////////////////////////// -->
			<!-- START queryForm -->
			<form action="<%=basePath%>normal/adminC2cAdvertAction!list.action" method="post" id="queryForm">
				<input type="hidden" name="pageNo" id="pageNo" value="${pageNo}" />
			</form>
			<!-- END queryForm -->
			<!-- //////////////////////////////////////////////////////////////////////////// -->
			
			<div class="row">
				<div class="col-md-12 col-lg-12">
					<div class="panel panel-default">

						<div class="panel-title">
							修改C2C广告
							<ul class="panel-tools">
								<li><a class="icon minimise-tool"><i class="fa fa-minus"></i></a></li>
								<li><a class="icon expand-tool"><i class="fa fa-expand"></i></a></li>
							</ul>
						</div>

						<div class="panel-body">
						
							<form class="form-horizontal" action="<%=basePath%>normal/adminC2cAdvertAction!update.action"
								method="post" name="mainForm" id="mainForm">
								
								<input type="hidden" name="input_payment_method1" id="input_payment_method1" value="${payment_method1}" />
								<input type="hidden" name="input_payment_method2" id="input_payment_method2" value="${payment_method2}" />
								<input type="hidden" name="input_payment_method3" id="input_payment_method3" value="${payment_method3}" />
								<input type="hidden" name="symbol_close" id="symbol_close" value="${symbol_close}" />
								<input type="hidden" name="id" id="advert_id" value="${id}" />
								<input type="hidden" name="deposit_old" id="deposit_old" value="${deposit_old}" />
								<input type="hidden" name="deposit_total" id="deposit_total" value="${deposit_total}" />
								
								<div class="form-group">
									<label class="col-sm-2 control-label form-label">承兑商UID</label>
									<div class="col-sm-8">
										<input id="c2c_user_code" name="c2c_user_code" class="form-control " readOnly="readOnly" placeholder="承兑商UID" onchange="getDeposit();" value="${c2c_user_code}" />
									</div>
								</div>

								<div class="form-group">
									<label class="col-sm-2 control-label form-label">承兑商剩余保证金</label>
									<div class="col-sm-8">
										<div class="input-group">
											<input id="all_deposit" name="all_deposit" class="form-control " readOnly="readOnly" value="${all_deposit}" />
											<div class="input-group-addon" >USDT</div>
										</div>
									</div>
								</div>
								
								<div class="form-group">
									<label class="col-sm-2 control-label form-label"></label>
									<div class="col-sm-8">
										<p class="ballon color1">
											广告保证金：广告保证金不能大于承兑商剩余保证金
										</p>
									</div>
								</div>
								
								<div class="form-group">
									<label class="col-sm-2 control-label form-label">广告保证金</label>
									<div class="col-sm-8">
										<div class="input-group">
											<input id="deposit" name="deposit" class="form-control " readOnly="readOnly" value="${deposit}" />
											<div class="input-group-addon" >USDT</div>
										</div>
									</div>
								</div>
								
								<div class="form-group">
									<label class="col-sm-2 control-label form-label">买卖方式</label>
									<div class="col-sm-2">
										<select id="direction" name="direction" class="form-control " >
											<option value="buy" <c:if test="${direction == 'buy'}">selected="true"</c:if> >买入</option>
											<option value="sell" <c:if test="${direction == 'sell'}">selected="true"</c:if> >卖出</option>
										</select>
									</div>
								</div>
								
								<div class="form-group">
									<label class="col-sm-2 control-label form-label">支付方式</label>
									<div class="col-sm-2">
										<div class="input-group">
											<div class="input-group-addon" >方式一</div>
											<select id="payment_method1" name="payment_method1" class="form-control" >
												<option value="">请选择方式一</option>
												<c:forEach items="${paymentMethodMap}" var="item">
													<option value="${item.key}" <c:if test="${payment_method1 == item.key}">selected="true"</c:if> >${item.value}</option>
												</c:forEach>
											</select>
										</div>
									</div>
									<div class="col-sm-2">
										<div class="input-group">
											<div class="input-group-addon" >方式二</div>
											<select id="payment_method2" name="payment_method2" class="form-control" >
												<option value="">请选择方式二</option>
												<c:forEach items="${paymentMethodMap}" var="item">
													<option value="${item.key}" <c:if test="${payment_method2 == item.key}">selected="true"</c:if> >${item.value}</option>
												</c:forEach>
											</select>
										</div>
									</div>
									<div class="col-sm-2">
										<div class="input-group">
											<div class="input-group-addon" >方式三</div>
											<select id="payment_method3" name="payment_method3" class="form-control" >
												<option value="">请选择方式三</option>
												<c:forEach items="${paymentMethodMap}" var="item">
													<option value="${item.key}" <c:if test="${payment_method3 == item.key}">selected="true"</c:if> >${item.value}</option>
												</c:forEach>
											</select>
										</div>
									</div>
								</div>
								
								<div class="form-group">
									<label class="col-sm-2 control-label form-label">支付币种</label>
									<div class="col-sm-2">
										<select id="currency" name="currency" class="form-control" onchange="getComputeValue();" >
											<c:forEach items="${currencyMap}" var="item">
												<option value="${item.key}" <c:if test="${currency == item.key}">selected="true"</c:if> >${item.value}</option>
											</c:forEach>
										</select>
									</div>
								</div>
								
								<div class="form-group">
									<label class="col-sm-2 control-label form-label">上架币种</label>
									<div class="col-sm-2">
										<select id="symbol" name="symbol" class="form-control" onchange="getComputeValue();" >
											<c:forEach items="${symbolMap}" var="item">
												<option value="${item.key}" <c:if test="${symbol == item.key}">selected="true"</c:if> >${item.value}</option>
											</c:forEach>
										</select>
									</div>
								</div>
								
								<div class="form-group">
									<label class="col-sm-2 control-label form-label"></label>
									<div class="col-sm-8">
										<p class="ballon color1">
											交易币种数量：交易币种数量=广告保证金/上架币种实时行情价
										</p>
									</div>
								</div>
								
								<div class="form-group">
									<label class="col-sm-2 control-label form-label">交易币种数量</label>
									<div class="col-sm-4">
										<div class="input-group">
											<input id="coin_amount" name="coin_amount" class="form-control " onchange="getComputeValue();" value="${coin_amount}" />
											<div class="input-group-addon symbol" >USDT</div>
										</div>
									</div>
									<label class="col-sm-1 control-label form-label">最大可交易数量</label>
									<div class="col-sm-3">
										<div class="input-group">
											<input id="coin_amount_max" name="coin_amount_max" class="form-control " readOnly="readOnly" value="${coin_amount_max}" />
											<div class="input-group-addon symbol" >USDT</div>
										</div>
									</div>
								</div>
								
								<div class="form-group">
									<label class="col-sm-2 control-label form-label"></label>
									<div class="col-sm-8">
										<p class="ballon color1">
											币种单价：币种单价=支付币种汇率*上架币种实时行情价*支付比率；例如，支付比率95%，1USDT=7.3CNY*1*95%=6.935CNY
											<br />币种市价：币种市价=支付币种汇率*上架币种实时行情价；
										</p>
									</div>
								</div>
								
								<div class="form-group">
									<label class="col-sm-2 control-label form-label">币种单价</label>
									<div class="col-sm-4">
										<div class="input-group">
											<input id="symbol_value" name="symbol_value" class="form-control " onchange="getComputeValue();" value="${symbol_value}" />
											<div class="input-group-addon currency" >CNY</div>
										</div>
									</div>
									<label class="col-sm-1 control-label form-label">币种市价</label>
									<div class="col-sm-3">
										<div class="input-group">
											<input id="price" name="price" class="form-control " readOnly="readOnly" value="${price}" />
											<div class="input-group-addon currency" >CNY</div>
										</div>
									</div>
								</div>
								
								<div style="display: none">
								
									<div class="form-group">
										<label class="col-sm-2 control-label form-label"></label>
										<div class="col-sm-8">
											<p class="ballon color1">
												支付比率: 币种单价=支付币种汇率*上架币种实时行情价*支付比率
											</p>
										</div>
									</div>
	
									<div class="form-group">
										<label class="col-sm-2 control-label form-label">支付比率</label>
										<div class="col-sm-2">
											<div class="input-group">
												<input id="pay_rate" name="pay_rate" class="form-control " readOnly="readOnly" value="${pay_rate}" />
												<div class="input-group-addon">%</div>										 
											</div>
										</div>
									</div>
									
								</div>
								
								<div class="form-group">
									<label class="col-sm-2 control-label form-label">单笔订单支付金额区间</label>
									<div class="col-sm-3">
										<div class="input-group">
											<input id="investment_min" name="investment_min" class="form-control " value="${investment_min}" />
										 	<div class="input-group-addon">--</div>
											<input id="investment_max" name="investment_max" class="form-control " value="${investment_max}" />
										 	<div class="input-group-addon currency">CNY</div>										 
										</div>
									</div>
									<label class="col-sm-1 control-label form-label">最小最大限额</label>
									<div class="col-sm-4">
										<div class="input-group">
											<input id="investment_min_limit" name="investment_min_limit" class="form-control " readOnly="readOnly" value="${investment_min_limit}" />
										 	<div class="input-group-addon">--</div>
											<input id="investment_max_limit" name="investment_max_limit" class="form-control " readOnly="readOnly" value="${investment_max_limit}" />
										 	<div class="input-group-addon currency">CNY</div>
										</div>
									</div>
								</div>
								
								<div class="form-group">
									<label class="col-sm-2 control-label form-label">是否上架</label>
									<div class="col-sm-2">
										<select id="on_sale" name="on_sale" class="form-control " >
											<option value="0" <c:if test="${on_sale == '0'}">selected="true"</c:if> >下架</option>
											<option value="1" <c:if test="${on_sale == '1'}">selected="true"</c:if> >上架</option>
										</select>
									</div>
								</div>
								
								<div class="form-group">
									<label class="col-sm-2 control-label form-label"></label>
									<div class="col-sm-8">
										<p class="ballon color1">
											排序索引: 默认0，值越大越靠前
										</p>
									</div>
								</div>
								
								<div class="form-group">
									<label class="col-sm-2 control-label form-label">排序索引</label>
									<div class="col-sm-8">
										<input id="sort_index" name="sort_index" class="form-control " placeholder="排序索引" value="${sort_index}" />
									</div>
								</div>
								
								<div class="form-group">
									<label class="col-sm-2 control-label form-label"></label>
									<div class="col-sm-8">
										<p class="ballon color1">
											支付时效: 
											<br />1. 用户下单后，在支付时效内未付款，订单会自动取消；
											<br />2. 订单付款后，在支付时效内未确认收款，订单会变成已超时状态，在后台取消或确认收款；
										</p>
									</div>
								</div>
								
								<div class="form-group">
									<label class="col-sm-2 control-label form-label">支付时效（单位：分钟）</label>
									<div class="col-sm-2">
										<select id="expire_time" name="expire_time" class="form-control" >
											<c:forEach items="${expireTimeMap}" var="item">
												<option value="${item.key}" <c:if test="${expire_time == item.key}">selected="true"</c:if> >${item.value}</option>
											</c:forEach>
										</select>
									</div>
								</div>
								
								<div class="form-group">
									<label class="col-sm-2 control-label form-label">交易条款</label>
									<div class="col-sm-8">
										<textarea id="transaction_terms" name="transaction_terms" class="form-control input-lg" rows="4" cols="20" >${transaction_terms}</textarea>
									</div>
								</div>
								
								<div class="form-group" style="display: none">
									<label class="col-sm-2 control-label form-label">订单自动消息</label>
									<div class="col-sm-8">
										<textarea id="order_msg" name="order_msg" class="form-control input-lg" rows="4" cols="20" >${order_msg}</textarea>
									</div>
								</div>
								
								<div class="form-group">
									<label class="col-sm-2 control-label form-label">备注</label>
									<div class="col-sm-8">
										<input id="remark" name="remark" class="form-control " placeholder="备注" value="${remark}" />
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
	
	<script src="<%=basePath%>js/util.js" type="text/javascript"></script>
	
	<script type="text/javascript">
	
		// 初始化执行一次
		setTimeout(function() {
			start();	  
		}, 100);
		
		function start() {
			var c2c_user_code = $("#c2c_user_code").val();
			if (c2c_user_code != null && c2c_user_code != "") {
				getDeposit();
				getComputeValue();
			}
		}
		
		function getDeposit() {
			
			var content1 = '<option value="">请选择方式一</option>';
			var content2 = '<option value="">请选择方式二</option>';
			var content3 = '<option value="">请选择方式三</option>';
			
			var url = "<%=basePath%>normal/adminC2cAdvertAction!getC2cUserDeposit.action";
			var data = {
					"c2c_user_code" : $("#c2c_user_code").val(),
					"advert_id" : $("#advert_id").val()
				};
			goAjaxUrl(url, data, function(tmp) {
				$("#all_deposit").val(tmp.all_deposit);
				$("#deposit_total").val(tmp.deposit_total);
// 				$("#deposit").val(tmp.deposit_total * 1 - tmp.all_deposit * 1);
				
				for (var i = 0; i < tmp.paymentMethodList.length; i++) {
					content1 += '<option value="' + tmp.paymentMethodList[i].methodConfigId + '" <c:if test="${payment_method1 == ' + tmp.paymentMethodList[i].methodConfigId + '}">selected="true"</c:if> >' + tmp.paymentMethodList[i].methodName + '</option>';
					content2 += '<option value="' + tmp.paymentMethodList[i].methodConfigId + '" <c:if test="${payment_method2 == ' + tmp.paymentMethodList[i].methodConfigId + '}">selected="true"</c:if> >' + tmp.paymentMethodList[i].methodName + '</option>';
					content3 += '<option value="' + tmp.paymentMethodList[i].methodConfigId + '" <c:if test="${payment_method3 == ' + tmp.paymentMethodList[i].methodConfigId + '}">selected="true"</c:if> >' + tmp.paymentMethodList[i].methodName + '</option>';
				}
				
				$("#payment_method1").html(content1);
				$("#payment_method2").html(content2);
				$("#payment_method3").html(content3);
				
				$("#payment_method1").val($("#input_payment_method1").val());
				$("#payment_method2").val($("#input_payment_method2").val());
				$("#payment_method3").val($("#input_payment_method3").val());
			}, function() {
				$("#all_deposit").val(0);
				$("#deposit_total").val(0);
// 				$("#deposit").val(0);
				
				$("#payment_method1").html(content1);
				$("#payment_method2").html(content2);
				$("#payment_method3").html(content3);
				
				$("#payment_method1").val($("#input_payment_method1").val());
				$("#payment_method2").val($("#input_payment_method2").val());
				$("#payment_method3").val($("#input_payment_method3").val());
			});
		}

		function getComputeValue() {
			$(".currency").html($("#currency").val());
			$(".symbol").html($("#symbol").val().toUpperCase());
			
			var url = "<%=basePath%>normal/adminC2cAdvertAction!compute.action";
			var data = {
				"deposit_total" : $("#deposit_total").val(),
				"currency" : $("#currency").val(),
				"symbol" : $("#symbol").val(),
				"symbol_close" : $("#symbol_close").val(),
				"coin_amount" : $("#coin_amount").val(),
				"symbol_value" : $("#symbol_value").val()
			};
			goAjaxUrl(url, data, function(tmp) {
				$("#pay_rate").val(tmp.pay_rate);
				$("#deposit").val(tmp.deposit_open);
				$("#all_deposit").val(tmp.all_deposit);
				$("#symbol_close").val(tmp.symbol_close);
				$("#price").val(tmp.price);
				$("#coin_amount_max").val(tmp.coin_amount_max);
				$("#investment_min_limit").val(tmp.investment_min_limit);
				$("#investment_max_limit").val(tmp.investment_max_limit);
			}, function() {
				$("#pay_rate").val(0);
				$("#deposit").val(0);
				$("#all_deposit").val($("#deposit_total").val());
				$("#symbol_close").val(0);
				$("#price").val(0);
				$("#coin_amount_max").val(0);
				$("#investment_min_limit").val(0);
				$("#investment_max_limit").val(0);
			});
		}
		
		function goAjaxUrl(targetUrl, data, Func, Fail) {
			$.ajax({
				url : targetUrl,
				data : data,
				type : 'get',
				dataType : "json",
				success : function (res) {
				    if (res.code == 200) {
				    	Func(res);
				    } else if (res.code == 500) {
				    	Fail();
				    	swal({
							title : res.message,
							text : "",
							type : "warning",
							showCancelButton : true,
							confirmButtonColor : "#DD6B55",
							confirmButtonText : "确认",
							closeOnConfirm : false
						});
				    }
				},
				error : function (XMLHttpRequest, textStatus, errorThrown) {
					console.log("请求错误");
				}
			});
		}
	
		function submit() {
			/* swal({
				title : "是否保存?",
				text : "",
				type : "warning",
				showCancelButton : true,
				confirmButtonColor : "#DD6B55",
				confirmButtonText : "确认",
				closeOnConfirm : false
			}, function() {
				var sHTML = $('#content_text').val();
				$("#content").val(sHTML);
				document.getElementById("mainForm").submit();
			}); */
			$('#modal_succeeded').modal("show");
		}
		
	</script>

</body>

</html>
