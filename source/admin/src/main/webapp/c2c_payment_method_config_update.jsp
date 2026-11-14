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
			<h3>C2C支付方式模板</h3>
			
			<%@ include file="include/alert.jsp"%>
			
			<!-- //////////////////////////////////////////////////////////////////////////// -->
			<!-- START queryForm -->
			<form action="<%=basePath%>normal/adminC2cPaymentMethodConfigAction!list.action" method="post" id="queryForm">
				<input type="hidden" name="pageNo" id="pageNo" value="${pageNo}" />
			</form>
			<!-- END queryForm -->
			<!-- //////////////////////////////////////////////////////////////////////////// -->
			
			<div class="row">
				<div class="col-md-12 col-lg-12">
					<div class="panel panel-default">

						<div class="panel-title">
							修改支付方式模板
							<ul class="panel-tools">
								<li><a class="icon minimise-tool"><i class="fa fa-minus"></i></a></li>
								<li><a class="icon expand-tool"><i class="fa fa-expand"></i></a></li>
							</ul>
						</div>

						<div class="panel-body">
						
							<form class="form-horizontal" action="<%=basePath%>normal/adminC2cPaymentMethodConfigAction!update.action"
								method="post" name="mainForm" id="mainForm">
								
								<input type="hidden" name="id" id="id" value="${id}" />
								<input type="hidden" name="method_img" id="img_url" value="${method_img}" >
								
								<div class="form-group">
									<label class="col-sm-2 control-label form-label">支付方式类型</label>
									<div class="col-sm-2">
										<select id="method_type" name="method_type" class="form-control " >
											<c:forEach items="${methodTypeMap}" var="item">
												<option value="${item.key}" <c:if test="${method_type == item.key}">selected="true"</c:if> >${item.value}</option>
											</c:forEach>
										</select>
									</div>
								</div>
								
								<div class="form-group">
									<label class="col-sm-2 control-label form-label">支付方式名称</label>
									<div class="col-sm-8">
										<input id="method_name" name="method_name" class="form-control " placeholder="支付方式名称" value="${method_name}" />
									</div>
								</div>
								
								<div class="form-group">
									<label class="col-sm-2 control-label form-label">支付方式图片</label>
									<div class="col-sm-3">
										<input type="file" id="fileName_img" name="fileName_img" onchange="upload_img();" style="position: absolute; opacity: 0;"> 
										<label for="fileName_img"> 
											<img width="40px" height="40px" id="show_img_content" src="<%=base%>/image/add.png" alt="点击上传图片" />
										</label>
									</div>
								</div>
								
								<div class="form-group">
									<label class="col-sm-2 control-label form-label"></label>
									<div class="col-sm-8">
										<p class="ballon color1">
											参数名1:  配关键数据，如微信账号、支付宝账号、银行卡号、虚拟货币地址等等；
										</p>
									</div>
								</div>
								
								<div class="form-group">
									<label class="col-sm-2 control-label form-label">参数名1（必填）</label>
									<div class="col-sm-8">
										<input id="param_name1" name="param_name1" class="form-control " placeholder="参数名1" value="${param_name1}" />
									</div>
								</div>
								
								<div class="form-group">
									<label class="col-sm-2 control-label form-label">参数名2（选配）</label>
									<div class="col-sm-8">
										<input id="param_name2" name="param_name2" class="form-control " placeholder="参数名2" value="${param_name2}" />
									</div>
								</div>
								
								<div class="form-group">
									<label class="col-sm-2 control-label form-label">参数名3（选配）</label>
									<div class="col-sm-8">
										<input id="param_name3" name="param_name3" class="form-control " placeholder="参数名3" value="${param_name3}" />
									</div>
								</div>
								
								<div class="form-group">
									<label class="col-sm-2 control-label form-label">参数名4（选配）</label>
									<div class="col-sm-8">
										<input id="param_name4" name="param_name4" class="form-control " placeholder="参数名4" value="${param_name4}" />
									</div>
								</div>
								
								<div class="form-group">
									<label class="col-sm-2 control-label form-label">参数名5（选配）</label>
									<div class="col-sm-8">
										<input id="param_name5" name="param_name5" class="form-control " placeholder="参数名5" value="${param_name5}" />
									</div>
								</div>
								
								<div class="form-group">
									<label class="col-sm-2 control-label form-label">参数名6（选配）</label>
									<div class="col-sm-8">
										<input id="param_name6" name="param_name6" class="form-control " placeholder="参数名6" value="${param_name6}" />
									</div>
								</div>
								
								<div class="form-group">
									<label class="col-sm-2 control-label form-label">参数名7（选配）</label>
									<div class="col-sm-8">
										<input id="param_name7" name="param_name7" class="form-control " placeholder="参数名7" value="${param_name7}" />
									</div>
								</div>
								
								<div class="form-group">
									<label class="col-sm-2 control-label form-label">参数名8（选配）</label>
									<div class="col-sm-8">
										<input id="param_name8" name="param_name8" class="form-control " placeholder="参数名8" value="${param_name8}" />
									</div>
								</div>
								
								<div class="form-group">
									<label class="col-sm-2 control-label form-label">参数名9（选配）</label>
									<div class="col-sm-8">
										<input id="param_name9" name="param_name9" class="form-control " placeholder="参数名9" value="${param_name9}" />
									</div>
								</div>
								
								<div class="form-group">
									<label class="col-sm-2 control-label form-label">参数名10（选配）</label>
									<div class="col-sm-8">
										<input id="param_name10" name="param_name10" class="form-control " placeholder="参数名10" value="${param_name10}" />
									</div>
								</div>
								
								<div class="form-group">
									<label class="col-sm-2 control-label form-label">参数名11（选配）</label>
									<div class="col-sm-8">
										<input id="param_name11" name="param_name11" class="form-control " placeholder="参数名11" value="${param_name11}" />
									</div>
								</div>
								
								<div class="form-group">
									<label class="col-sm-2 control-label form-label">参数名12（选配）</label>
									<div class="col-sm-8">
										<input id="param_name12" name="param_name12" class="form-control " placeholder="参数名12" value="${param_name12}" />
									</div>
								</div>
								
								<div class="form-group">
									<label class="col-sm-2 control-label form-label">参数名13（选配）</label>
									<div class="col-sm-8">
										<input id="param_name13" name="param_name13" class="form-control " placeholder="参数名13" value="${param_name13}" />
									</div>
								</div>
								
								<div class="form-group">
									<label class="col-sm-2 control-label form-label">参数名14（选配）</label>
									<div class="col-sm-8">
										<input id="param_name14" name="param_name14" class="form-control " placeholder="参数名14" value="${param_name14}" />
									</div>
								</div>
								
								<div class="form-group">
									<label class="col-sm-2 control-label form-label">参数名15（选配）</label>
									<div class="col-sm-8">
										<input id="param_name15" name="param_name15" class="form-control " placeholder="参数名15" value="${param_name15}" />
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
			var img_url = $("#img_url").val();
			var show_img_content = document.getElementById('show_img_content');
			show_img_content.src="<%=base%>wap/public/showimg!showImg.action?imagePath="+img_url;
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
