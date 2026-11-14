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
			<form action="<%=basePath%>normal/adminC2cPaymentMethodAction!list.action" method="post" id="queryForm">
				<input type="hidden" name="pageNo" id="pageNo" value="${pageNo}" />
			</form>
			<!-- END queryForm -->
			<!-- //////////////////////////////////////////////////////////////////////////// -->
			
			<div class="row">
				<div class="col-md-12 col-lg-12">
					<div class="panel panel-default">

						<div class="panel-title">
							修改支付方式
							<ul class="panel-tools">
								<li><a class="icon minimise-tool"><i class="fa fa-minus"></i></a></li>
								<li><a class="icon expand-tool"><i class="fa fa-expand"></i></a></li>
							</ul>
						</div>

						<div class="panel-body">
						
							<form class="form-horizontal" action="<%=basePath%>normal/adminC2cPaymentMethodAction!update.action"
								method="post" name="mainForm" id="mainForm">
								
								<input type="hidden" name="id" id="id" value="${id}" />
								<input type="hidden" name="qrcode_img" id="img_url" value="${qrcode_img}" >
								
								<div class="form-group">
									<label class="col-sm-2 control-label form-label">用户UID</label>
									<div class="col-sm-8">
										<input id="user_code" name="user_code" class="form-control " readonly="readonly" placeholder="用户UID" value="${user_code}" />
									</div>
								</div>
								
								<div class="form-group">
									<label class="col-sm-2 control-label form-label">用户名</label>
									<div class="col-sm-8">
										<input id="user_name" name="user_name" class="form-control " readonly="readonly" placeholder="用户名" value="${user_name}" />
									</div>
								</div>
								
								<div class="form-group">
									<label class="col-sm-2 control-label form-label">支付方式名称</label>
									<div class="col-sm-8">
										<input id="method_name" name="method_name" class="form-control " readonly="readonly" placeholder="支付方式名称" value="${method_name}" />
									</div>
								</div>
								
								<div class="form-group">
									<label class="col-sm-2 control-label form-label">支付方式图片</label>
									<div class="col-sm-3">
										<label> 
											<img width="40px" height="40px" id="method_img_content" src="<%=base%>wap/public/showimg!showImg.action?imagePath=${method_img}" />
											<input type="hidden" id="method_img" name="method_img" class="form-control " value="${method_img}" />
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
								
								<div class="form-group">
									<label class="col-sm-2 control-label form-label">${param_name1}（必填）</label>
									<div class="col-sm-8">
										<input id="param_value1" name="param_value1" class="form-control " placeholder="${param_name1}" value="${param_value1}" />
									</div>
								</div>
								
								<c:if test="${null != param_name2 && '' != param_name2}">
								
									<div class="form-group">
										<label class="col-sm-2 control-label form-label">${param_name2}（选填）</label>
										<div class="col-sm-8">
											<input id="param_value2" name="param_value2" class="form-control " placeholder="${param_name2}" value="${param_value2}" />
										</div>
									</div>
															
								</c:if>
								
								<c:if test="${null != param_name3 && '' != param_name3}">
								
									<div class="form-group">
										<label class="col-sm-2 control-label form-label">${param_name3}（选填）</label>
										<div class="col-sm-8">
											<input id="param_value3" name="param_value3" class="form-control " placeholder="${param_name3}" value="${param_value3}" />
										</div>
									</div>
															
								</c:if>
								
								<c:if test="${null != param_name4 && '' != param_name4}">
								
									<div class="form-group">
										<label class="col-sm-2 control-label form-label">${param_name4}（选填）</label>
										<div class="col-sm-8">
											<input id="param_value4" name="param_value4" class="form-control " placeholder="${param_name4}" value="${param_value4}" />
										</div>
									</div>
															
								</c:if>
								
								<c:if test="${null != param_name5 && '' != param_name5}">
								
									<div class="form-group">
										<label class="col-sm-2 control-label form-label">${param_name5}（选填）</label>
										<div class="col-sm-8">
											<input id="param_value5" name="param_value5" class="form-control " placeholder="${param_name5}" value="${param_value5}" />
										</div>
									</div>
															
								</c:if>
								
								<c:if test="${null != param_name6 && '' != param_name6}">
								
									<div class="form-group">
										<label class="col-sm-2 control-label form-label">${param_name6}（选填）</label>
										<div class="col-sm-8">
											<input id="param_value6" name="param_value6" class="form-control " placeholder="${param_name6}" value="${param_value6}" />
										</div>
									</div>
															
								</c:if>
								
								<c:if test="${null != param_name7 && '' != param_name7}">
								
									<div class="form-group">
										<label class="col-sm-2 control-label form-label">${param_name7}（选填）</label>
										<div class="col-sm-8">
											<input id="param_value7" name="param_value7" class="form-control " placeholder="${param_name7}" value="${param_value7}" />
										</div>
									</div>
															
								</c:if>
								
								<c:if test="${null != param_name8 && '' != param_name8}">
								
									<div class="form-group">
										<label class="col-sm-2 control-label form-label">${param_name8}（选填）</label>
										<div class="col-sm-8">
											<input id="param_value8" name="param_value8" class="form-control " placeholder="${param_name8}" value="${param_value8}" />
										</div>
									</div>
															
								</c:if>
								
								<c:if test="${null != param_name9 && '' != param_name9}">
								
									<div class="form-group">
										<label class="col-sm-2 control-label form-label">${param_name9}（选填）</label>
										<div class="col-sm-8">
											<input id="param_value9" name="param_value9" class="form-control " placeholder="${param_name9}" value="${param_value9}" />
										</div>
									</div>
															
								</c:if>
								
								<c:if test="${null != param_name10 && '' != param_name10}">
								
									<div class="form-group">
										<label class="col-sm-2 control-label form-label">${param_name10}（选填）</label>
										<div class="col-sm-8">
											<input id="param_value10" name="param_value10" class="form-control " placeholder="${param_name10}" value="${param_value10}" />
										</div>
									</div>
															
								</c:if>
								
								<c:if test="${null != param_name11 && '' != param_name11}">
								
									<div class="form-group">
										<label class="col-sm-2 control-label form-label">${param_name11}（选填）</label>
										<div class="col-sm-8">
											<input id="param_value11" name="param_value11" class="form-control " placeholder="${param_name11}" value="${param_value11}" />
										</div>
									</div>
															
								</c:if>
								
								<c:if test="${null != param_name12 && '' != param_name12}">
								
									<div class="form-group">
										<label class="col-sm-2 control-label form-label">${param_name12}（选填）</label>
										<div class="col-sm-8">
											<input id="param_value12" name="param_value12" class="form-control " placeholder="${param_name12}" value="${param_value12}" />
										</div>
									</div>
															
								</c:if>
								
								<c:if test="${null != param_name13 && '' != param_name13}">
								
									<div class="form-group">
										<label class="col-sm-2 control-label form-label">${param_name13}（选填）</label>
										<div class="col-sm-8">
											<input id="param_value13" name="param_value13" class="form-control " placeholder="${param_name13}" value="${param_value13}" />
										</div>
									</div>
															
								</c:if>
								
								<c:if test="${null != param_name14 && '' != param_name14}">
								
									<div class="form-group">
										<label class="col-sm-2 control-label form-label">${param_name14}（选填）</label>
										<div class="col-sm-8">
											<input id="param_value14" name="param_value14" class="form-control " placeholder="${param_name14}" value="${param_value14}" />
										</div>
									</div>
															
								</c:if>
								
								<c:if test="${null != param_name15 && '' != param_name15}">
								
									<div class="form-group">
										<label class="col-sm-2 control-label form-label">${param_name15}（选填）</label>
										<div class="col-sm-8">
											<input id="param_value15" name="param_value15" class="form-control " placeholder="${param_name15}" value="${param_value15}" />
										</div>
									</div>
															
								</c:if>
								
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
									<div class="col-sm-10">
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
