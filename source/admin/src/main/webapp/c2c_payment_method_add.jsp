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
			<h3>C2C支付方式管理</h3>
			
			<%@ include file="include/alert.jsp"%>
			
			<!-- //////////////////////////////////////////////////////////////////////////// -->
			<!-- START queryForm -->
			<form action="<%=basePath%>normal/adminC2cPaymentMethodAction!list.action"
				method="post" id="queryForm">
				<input type="hidden" name="pageNo" id="pageNo" value="${pageNo}" />
			</form>
			<!-- END queryForm -->
			<!-- //////////////////////////////////////////////////////////////////////////// -->
			
			<div class="row">
				<div class="col-md-12 col-lg-12">
					<div class="panel panel-default">

						<div class="panel-title">
							新增支付方式
							<ul class="panel-tools">
								<li><a class="icon minimise-tool"><i class="fa fa-minus"></i></a></li>
								<li><a class="icon expand-tool"><i class="fa fa-expand"></i></a></li>
							</ul>
						</div>

						<div class="panel-body">
							
							<form class="form-horizontal" action="<%=basePath%>normal/adminC2cPaymentMethodAction!add.action"
								method="post" name="mainForm" id="mainForm">
								
								<input type="hidden" name="id" id="id" value="${id}" />
								<input type="hidden" name="qrcode_img" id="img_url" value="${qrcode_img}" >
								
								<div class="form-group">
									<label class="col-sm-2 control-label form-label">用户UID</label>
									<div class="col-sm-8">
										<input id="user_code" name="user_code" class="form-control " placeholder="用户UID" value="${user_code}" />
									</div>
								</div>
								
								<div class="form-group">
									<label class="col-sm-2 control-label form-label">支付方式模板</label>
									<div class="col-sm-2">
										<select id="method_config_id" name="method_config_id" onchange="update_method_config_info();" class="form-control " >
											<c:forEach items="${methodConfigMap}" var="item">
												<option value="${item.key}" <c:if test="${method_config_id == item.key}">selected="true"</c:if> >${item.value}</option>
											</c:forEach>
										</select>
									</div>
								</div>
								
								<div class="form-group">
									<label class="col-sm-2 control-label form-label">支付方式图片</label>
									<div class="col-sm-3">
										<label> 
											<img width="40px" height="40px" id="method_img_content" src="" />
										</label>
									</div>
								</div>
								
								<div class="form-group">
									<label class="col-sm-2 control-label form-label">支付方式类型</label>
									<div class="col-sm-2">
										<select id="method_type" name="method_type" class="form-control " disabled="disabled" >
											<c:forEach items="${methodTypeMap}" var="item">
												<option value="${item.key}" <c:if test="${method_type == item.key}">selected="true"</c:if> >${item.value}</option>
											</c:forEach>
										</select>
									</div>
								</div>
								
								<div class="form-group">
									<label class="col-sm-2 control-label form-label">真实姓名</label>
									<div class="col-sm-8">
										<input id="real_name" name="real_name" class="form-control " placeholder="真实姓名" value="${real_name}" />
									</div>
								</div>

								<div id="param1" class="form-group">
									<label id="param_name1" class="col-sm-2 control-label form-label">参数名1（必填）</label>
									<div class="col-sm-8">
										<input id="param_value1" name="param_value1" class="form-control " placeholder="参数值1" value="${param_value1}" />
									</div>
								</div>
								
								<div id="param2" class="form-group">
									<label id="param_name2" class="col-sm-2 control-label form-label">参数名2（选填）</label>
									<div class="col-sm-8">
										<input id="param_value2" name="param_value2" class="form-control " placeholder="参数值2" value="${param_value2}" />
									</div>
								</div>
								
								<div id="param3" class="form-group">
									<label id="param_name3" class="col-sm-2 control-label form-label">参数名3（选填）</label>
									<div class="col-sm-8">
										<input id="param_value3" name="param_value3" class="form-control " placeholder="参数值3" value="${param_value3}" />
									</div>
								</div>
								
								<div id="param4" class="form-group">
									<label id="param_name4" class="col-sm-2 control-label form-label">参数名4（选填）</label>
									<div class="col-sm-8">
										<input id="param_value4" name="param_value4" class="form-control " placeholder="参数值4" value="${param_value4}" />
									</div>
								</div>
								
								<div id="param5" class="form-group">
									<label id="param_name5" class="col-sm-2 control-label form-label">参数名5（选填）</label>
									<div class="col-sm-8">
										<input id="param_value5" name="param_value5" class="form-control " placeholder="参数值5" value="${param_value5}" />
									</div>
								</div>
								
								<div id="param6" class="form-group">
									<label id="param_name6" class="col-sm-2 control-label form-label">参数名6（选填）</label>
									<div class="col-sm-8">
										<input id="param_value6" name="param_value6" class="form-control " placeholder="参数值6" value="${param_value6}" />
									</div>
								</div>
								
								<div id="param7" class="form-group">
									<label id="param_name7" class="col-sm-2 control-label form-label">参数名7（选填）</label>
									<div class="col-sm-8">
										<input id="param_value7" name="param_value7" class="form-control " placeholder="参数值7" value="${param_value7}" />
									</div>
								</div>
								
								<div id="param8" class="form-group">
									<label id="param_name8" class="col-sm-2 control-label form-label">参数名8（选填）</label>
									<div class="col-sm-8">
										<input id="param_value8" name="param_value8" class="form-control " placeholder="参数值8" value="${param_value8}" />
									</div>
								</div>
								
								<div id="param9" class="form-group">
									<label id="param_name9" class="col-sm-2 control-label form-label">参数名9（选填）</label>
									<div class="col-sm-8">
										<input id="param_value9" name="param_value9" class="form-control " placeholder="参数值9" value="${param_value9}" />
									</div>
								</div>
								
								<div id="param10" class="form-group">
									<label id="param_name10" class="col-sm-2 control-label form-label">参数名10（选填）</label>
									<div class="col-sm-8">
										<input id="param_value10" name="param_value10" class="form-control " placeholder="参数值10" value="${param_value10}" />
									</div>
								</div>
								
								<div id="param11" class="form-group">
									<label id="param_name11" class="col-sm-2 control-label form-label">参数名11（选填）</label>
									<div class="col-sm-8">
										<input id="param_value11" name="param_value11" class="form-control " placeholder="参数值11" value="${param_value11}" />
									</div>
								</div>
								
								<div id="param12" class="form-group">
									<label id="param_name12" class="col-sm-2 control-label form-label">参数名12（选填）</label>
									<div class="col-sm-8">
										<input id="param_value12" name="param_value12" class="form-control " placeholder="参数值12" value="${param_value12}" />
									</div>
								</div>
								
								<div id="param13" class="form-group">
									<label id="param_name13" class="col-sm-2 control-label form-label">参数名13（选填）</label>
									<div class="col-sm-8">
										<input id="param_value13" name="param_value13" class="form-control " placeholder="参数值13" value="${param_value13}" />
									</div>
								</div>
								
								<div id="param14" class="form-group">
									<label id="param_name14" class="col-sm-2 control-label form-label">参数名14（选填）</label>
									<div class="col-sm-8">
										<input id="param_value14" name="param_value14" class="form-control " placeholder="参数值14" value="${param_value14}" />
									</div>
								</div>
								
								<div id="param15" class="form-group">
									<label id="param_name15" class="col-sm-2 control-label form-label">参数名15（选填）</label>
									<div class="col-sm-8">
										<input id="param_value15" name="param_value15" class="form-control " placeholder="参数值15" value="${param_value15}" />
									</div>
								</div>
								
								<div class="form-group">
									<label class="col-sm-2 control-label form-label">支付二维码</label>
									<div class="col-sm-3">
										<input type="file" id="fileName_img" name="fileName_img" onchange="upload_img();" style="position: absolute; opacity: 0;"> 
										<label for="fileName_img"> 
											<img width="40px" height="40px" id="show_img_content" src="<%=base%>/image/add.png" alt="点击上传图片" />
										</label>
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
			
			update_method_config_info();
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
		
		function update_method_config_info() {
			var method_config_id = $("#method_config_id").val();
			var formData = new FormData();
			formData.append("method_config_id", method_config_id);
			$.ajax({
		    	type: "POST",
		        url: "<%=basePath%>normal/adminC2cPaymentMethodAction!getPaymentMethodConfig.action?random=" + Math.random(),
				data: formData,
				dataType: "json",
			    contentType: false,  
			    processData: false, 
				success : function(data) {
					var method_img = document.getElementById('method_img_content');
					method_img.src="<%=base%>wap/public/showimg!showImg.action?imagePath=" + data.methodConfigMap.method_img;
					$("#method_type").val(data.methodConfigMap.method_type);
					$("#param_name1").html(data.methodConfigMap.param_name1);					
					if (null == data.methodConfigMap.param_name2 || "" == data.methodConfigMap.param_name2) {
						$("#param2").hide();
					} else {
						$("#param2").show();
						$("#param_name2").html(data.methodConfigMap.param_name2);
					}
					if (null == data.methodConfigMap.param_name3 || "" == data.methodConfigMap.param_name3) {
						$("#param3").hide();
					} else {
						$("#param3").show();
						$("#param_name3").html(data.methodConfigMap.param_name3);
					}
					if (null == data.methodConfigMap.param_name4 || "" == data.methodConfigMap.param_name4) {
						$("#param4").hide();
					} else {
						$("#param4").show();
						$("#param_name4").html(data.methodConfigMap.param_name4);
					}
					if (null == data.methodConfigMap.param_name5 || "" == data.methodConfigMap.param_name5) {
						$("#param5").hide();
					} else {
						$("#param5").show();
						$("#param_name5").html(data.methodConfigMap.param_name5);
					}
					if (null == data.methodConfigMap.param_name6 || "" == data.methodConfigMap.param_name6) {
						$("#param6").hide();
					} else {
						$("#param6").show();
						$("#param_name6").html(data.methodConfigMap.param_name6);
					}
					if (null == data.methodConfigMap.param_name7 || "" == data.methodConfigMap.param_name7) {
						$("#param7").hide();
					} else {
						$("#param7").show();
						$("#param_name7").html(data.methodConfigMap.param_name7);
					}
					if (null == data.methodConfigMap.param_name8 || "" == data.methodConfigMap.param_name8) {
						$("#param8").hide();
					} else {
						$("#param8").show();
						$("#param_name8").html(data.methodConfigMap.param_name8);
					}
					if (null == data.methodConfigMap.param_name9 || "" == data.methodConfigMap.param_name9) {
						$("#param9").hide();
					} else {
						$("#param9").show();
						$("#param_name9").html(data.methodConfigMap.param_name9);
					}
					if (null == data.methodConfigMap.param_name10 || "" == data.methodConfigMap.param_name10) {
						$("#param10").hide();
					} else {
						$("#param10").show();
						$("#param_name10").html(data.methodConfigMap.param_name10);
					}
					if (null == data.methodConfigMap.param_name11 || "" == data.methodConfigMap.param_name11) {
						$("#param11").hide();
					} else {
						$("#param11").show();
						$("#param_name11").html(data.methodConfigMap.param_name11);
					}
					if (null == data.methodConfigMap.param_name12 || "" == data.methodConfigMap.param_name12) {
						$("#param12").hide();
					} else {
						$("#param12").show();
						$("#param_name12").html(data.methodConfigMap.param_name12);
					}
					if (null == data.methodConfigMap.param_name13 || "" == data.methodConfigMap.param_name13) {
						$("#param13").hide();
					} else {
						$("#param13").show();
						$("#param_name13").html(data.methodConfigMap.param_name13);
					}
					if (null == data.methodConfigMap.param_name14 || "" == data.methodConfigMap.param_name14) {
						$("#param14").hide();
					} else {
						$("#param14").show();
						$("#param_name14").html(data.methodConfigMap.param_name14);
					}
					if (null == data.methodConfigMap.param_name15 || "" == data.methodConfigMap.param_name15) {
						$("#param15").hide();
					} else {
						$("#param15").show();
						$("#param_name15").html(data.methodConfigMap.param_name15);
					}
				},
				error : function(XMLHttpRequest, textStatus, errorThrown) {
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
				document.getElementById("mainForm").submit();
			}); */
			$('#modal_succeeded').modal("show");
		}
		
	</script>

</body>

</html>
