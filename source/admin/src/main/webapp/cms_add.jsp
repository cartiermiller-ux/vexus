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
			<h3>用户端内容管理公告</h3>
			
			<%@ include file="include/alert.jsp"%>
			
			<!-- //////////////////////////////////////////////////////////////////////////// -->
			<!-- START queryForm -->
			<form action="<%=basePath%>normal/adminCmsAction!list.action" method="post" id="queryForm">
			
				<input type="hidden" name="pageNo" id="pageNo" value="${pageNo}" />
				<input type="hidden" name="para_title" id="para_title" value="${para_title}" />
				<input type="hidden" name="para_language" id="para_language" value="${para_language}" />
				
			</form>
			<!-- END queryForm -->
			<!-- //////////////////////////////////////////////////////////////////////////// -->
			
			<div class="row">
				<div class="col-md-12 col-lg-12">
					<div class="panel panel-default">

						<div class="panel-title">
							新增公告
							<ul class="panel-tools">
								<li><a class="icon minimise-tool"><i class="fa fa-minus"></i></a></li>
								<li><a class="icon expand-tool"><i class="fa fa-expand"></i></a></li>
							</ul>
						</div>

						<div class="panel-body">
						
							<form class="form-horizontal" action="<%=basePath%>normal/adminCmsAction!add.action"
								method="post" name="mainForm" id="mainForm">
								
								<input type="hidden" name="id" id="id" value="${id}" />
								
								<div class="form-group">
									<label class="col-sm-1 control-label form-label">标题</label>
									<div class="col-sm-10">
										<input id="title" name="title" class="form-control " placeholder="标题" value="${title}" />
									</div>
								</div>
								
								<div class="form-group">
									<label class="col-sm-1 control-label form-label">业务代码</label>
									<div class="col-sm-10">
										<input id="content_code" name="content_code"
											class="form-control " placeholder="业务代码(同种内容不同语言代码相同)" value="${content_code}" />
									</div>
								</div>
								
								<div class="form-group">
									<label class="col-sm-1 control-label form-label">模块</label>
									<div class="col-sm-2">
										<select id="model" name="model" class="form-control " >
											<c:forEach items="${modelMap}" var="item">
												<option value="${item.key}" <c:if test="${model == item.key}">selected="true"</c:if> >${item.value}</option>
											</c:forEach>
										</select>
									</div>
								</div>
								
								<div class="form-group">
									<label class="col-sm-1 control-label form-label">语言</label>
									<div class="col-sm-2">
										<select id="language" name="language" class="form-control " >
											<c:forEach items="${languageMap}" var="item">
												<option value="${item.key}" <c:if test="${language == item.key}">selected="true"</c:if> >${item.value}</option>
											</c:forEach>
										</select>
									</div>
								</div>

								<div class="form-group">
									<label class="col-sm-1 control-label form-label">内容</label>
									<div class="col-sm-10">
					                    <%-- <textarea class="form-control" rows="20" id="content_text" name="content_text" placeholder="请输入内容...">${content}</textarea> --%>
					                    <textarea class="form-control" rows="15" id="content_text" name="content_text" placeholder="请输入内容...">${content}</textarea>
					                    <input type="hidden" name="content" id="content" value="${content}" />
									</div>
								</div>
								
								<div class="col-sm-1">
									<!-- 模态框（Modal） -->
									<div class="modal fade" id="modal_succeeded" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
										<div class="modal-dialog">
											<div class="modal-content">
											
												<div class="modal-header">
													<button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
													<h4 class="modal-title" id="myModalLabel">确认新增</h4>
												</div>
												
												<div class="modal-body">												
													<div class="form-group">
														<label for="input002" class="col-sm-3 control-label form-label">登录人资金密码</label>
														<div class="col-sm-4">
															<input id="login_safeword" type="password" name="login_safeword" class="login_safeword"
																placeholder="请输入登录人资金密码">
														</div>
													</div>													
												</div>
												
												<div class="modal-footer" style="margin-top: 0;">
													<button type="button" class="btn " data-dismiss="modal">关闭</button>
													<button id="sub" type="submit" class="btn btn-default">确认</button>
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
			var sHTML = $('#content_text').val();
			$("#content").val(sHTML);
			$('#modal_succeeded').modal("show");
		}
	</script>

</body>

</html>
