<%@ page language="java" pageEncoding="utf-8" isELIgnored="false"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<jsp:useBean id="security" class="security.web.BaseSecurityAction" scope="page" />

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
				<input type="hidden" name="pageNo" id="pageNo" value="${pageNo}">
			</form>
			<!-- END queryForm -->
			<!-- //////////////////////////////////////////////////////////////////////////// -->
			
			<form class="form-horizontal" action="<%=basePath%>normal/adminSystemUserAction!list.action" method="post" id="queryFormSystemUser">				
			</form>
			
			<div class="row">
				<div class="col-md-12 col-lg-12">
					<div class="panel panel-default">

						<div class="panel-title">
							新增承兑商
							<ul class="panel-tools">
								<li><a class="icon minimise-tool"><i class="fa fa-minus"></i></a></li>
								<li><a class="icon expand-tool"><i class="fa fa-expand"></i></a></li>
							</ul>
						</div>

						<div class="panel-body">
						
							<form class="form-horizontal" action="<%=basePath%>normal/adminC2cUserAction!add.action" method="post" name="mainForm" id="mainForm">								
								
								<input type="hidden" name="head_img" id="img_url" value="${head_img}" >
								
								<h5>基础信息</h5>							
								
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
								
								<%-- <div class="form-group">
									<label class="col-sm-2 control-label form-label">剩余保证金</label>
									<div class="col-sm-8">
										<input id="deposit" name="deposit" class="form-control" readOnly="readOnly" value="${deposit}" />
									</div>
								</div>
								
								<div class="form-group">
									<label class="col-sm-2 control-label form-label">保证金</label>
									<div class="col-sm-8">
										<input id="deposit_open" name="deposit_open" class="form-control" readOnly="readOnly" value="${deposit_open}" />
									</div>
								</div> --%>
								
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
								
								<div id="login_info" >
								
									<div class="form-group">
										<label class="col-sm-2 control-label form-label"></label>
										<div class="col-sm-8">
											<p class="ballon color1">
												前端登录信息：只有后台承兑商需要配置，用户承兑商已经注册过了
											</p>
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
													<h4 class="modal-title" id="myModalLabel">确认新增</h4>
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
			var img_url = $("#img_url").val();
			var show_img_content = document.getElementById('show_img_content');
			show_img_content.src="<%=base%>wap/public/showimg!showImg.action?imagePath="+img_url;
			
			update_c2c_user_info();
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
		
		function update_c2c_user_info() {
			var c2c_user_type = $("#c2c_user_type").val();			
			if ("1" == c2c_user_type) {
				// 后台承兑商
				$("#div_c2c_user_code").hide();
				$("#login_info").show();
			} else if ("2" == c2c_user_type) {
				// 用户承兑商
				$("#div_c2c_user_code").show();
				$("#login_info").hide();
			}
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

	<%-- <script>
	
		function sendsms() {
		  	var email=$("#email").val();
	 		if ($(".reg-yz").hasClass("disabled")) {
				return false;
			}
			if (isNull(email)) {
				$("#email").focus();
				sweet_warning("请输入邮件地址");
			} else {
				$(".reg-yz").addClass("disabled");
				$.ajax( {
					type : "POST",
					url : "<%=basePath%>normal/identifyingCodeAction.action?random=" + Math.random(),
					data : {
						'email' : email
					},
					success : function(data) {
						if (data == 0) {
							setTimeout(function() {
								var count = 60;
								$(".reg-yz").val(count + "秒");
								$(".reg-yz").css('background-color', '#c4c4c4');
								var timeout = setInterval(CountDown, 1000);
								function CountDown() {
									if (count > 0) {
										count--;
										$(".reg-yz").val(count + " 秒");
									} else {
										clearInterval(timeout);
										$(".reg-yz").removeClass("disabled").val("获取验证码");
										$(".reg-yz").removeAttr("style");
									}
								}
							}, 1);
						} else {
							sweet_warning(data);
							$(".reg-yz").removeClass("disabled");
						}
					},
					error : function() {
						alerttop("系统异常，请稍后再试");
					}
				});
			}
		};
		
	</script> --%>
	
</body>

</html>
