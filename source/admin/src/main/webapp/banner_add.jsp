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
			<h3>横幅管理</h3>
			
			<%@ include file="include/alert.jsp"%>
			
			<!-- //////////////////////////////////////////////////////////////////////////// -->
			<!-- START queryForm -->
			<form action="<%=basePath%>normal/adminBannerAction!list.action" method="post" id="queryForm">
				<input type="hidden" name="pageNo" id="pageNo" value="${pageNo}" />
			</form>
			<!-- END queryForm -->
			<!-- //////////////////////////////////////////////////////////////////////////// -->
			
			<div class="row">
				<div class="col-md-12 col-lg-12">
					<div class="panel panel-default">

						<div class="panel-title">
							新增横幅
							<ul class="panel-tools">
								<li><a class="icon minimise-tool"><i class="fa fa-minus"></i></a></li>
								<li><a class="icon expand-tool"><i class="fa fa-expand"></i></a></li>
							</ul>
						</div>

						<div class="panel-body">
						
							<form class="form-horizontal" action="<%=basePath%>normal/adminBannerAction!add.action"
								method="post" name="mainForm" id="mainForm" enctype="multipart/form-data">
								
								<input type="hidden" name="id" id="id" value="${id}" />
								<input type="hidden" name="img" id="img" value="${img}" />
								
								<div class="form-group">
									<label class="col-sm-2 control-label form-label">业务代码</label>
									<div class="col-sm-4">
										<input id="content_code" name="content_code" class="form-control " 
											placeholder="业务代码(同种内容不同语言代码相同)" value="${content_code}" />
									</div>
								</div>
								
								<div class="form-group">
									<label class="col-sm-2 control-label form-label">图片</label>
									<div class="col-sm-3">
										<input type="file" id="fileName" name="fileName" onchange="upload();" style="position: absolute; opacity: 0;">
										<label for="fileName"> <img width="150px" height="55px" id="show_img" style="float: left;"
											src="<%=base%>/image/add.png" alt="点击上传图片" />
										</label>
									</div>
								</div>
								
								<div class="form-group">
									<label class="col-sm-2 control-label form-label">路径</label>
									<div class="col-sm-4">
										<input id="url" name="url" class="form-control " placeholder="路径" value="${url}" />
									</div>
								</div>
								
								<p class="ballon color1">排序索引，数字越小越靠前</p>
								<div class="form-group">
									<label class="col-sm-2 control-label form-label">排序索引</label>
									<div class="col-sm-4">
										<input id="sort_index" name="sort_index" class="form-control " placeholder="排序索引" value="${sort_index}" />
									</div>
								</div>
								
								<div class="form-group">
									<label class="col-sm-2 control-label form-label">模块</label>
									<div class="col-sm-2">
										<select id="model" name="model" class="form-control " >
											<c:forEach items="${modelMap}" var="item">
												<option value="${item.key}" <c:if test="${model == item.key}">selected="true"</c:if> >${item.value}</option>
											</c:forEach>
										</select>
									</div>
								</div>
								
								<div class="form-group">
									<label class="col-sm-2 control-label form-label">语言</label>
									<div class="col-sm-2">
										<select id="language" name="language" class="form-control " >
											<c:forEach items="${languageMap}" var="item">
												<option value="${item.key}" <c:if test="${language == item.key}">selected="true"</c:if> >${item.value}</option>
											</c:forEach>
										</select>
									</div>
								</div>
								
								<div class="form-group">
									<label class="col-sm-2 control-label form-label">是否可点击</label>
									<div class="col-sm-3">
										<div class="input-group">
											<%-- <s:select id="click" cssClass="form-control " name="click"
												list="#{'0':'否','1':'是'}" listKey="key" listValue="value"
												value="on_sale" /> --%>
											<select id="click" name="click" class="form-control " >
												<option value="0" <c:if test="${click == '0'}">selected="true"</c:if> >否</option>
												<option value="1" <c:if test="${click == '1'}">selected="true"</c:if> >是</option>
											</select>											
										</div>
									</div>
								</div>
								
								<div class="form-group">
									<label class="col-sm-2 control-label form-label">是否展示</label>
									<div class="col-sm-3">
										<div class="input-group">
											<%-- <s:select id="on_show" cssClass="form-control "
												name="on_show" list="#{'0':'否','1':'是'}" listKey="key"
												listValue="value" value="on_sale" /> --%>
											<select id="on_show" name="on_show" class="form-control " >
												<option value="0" <c:if test="${on_show == '0'}">selected="true"</c:if> >否</option>
												<option value="1" <c:if test="${on_show == '1'}">selected="true"</c:if> >是</option>
											</select>
										</div>
									</div>
								</div>

								<div class="col-sm-1">
									<!-- 模态框（Modal） -->
									<div class="modal fade" id="modal_succeeded" tabindex="-1"
										role="dialog" aria-labelledby="myModalLabel"
										aria-hidden="true">
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
															<input id="login_safeword" type="password" name="login_safeword" 
																class="login_safeword" placeholder="请输入登录人资金密码">
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
			$('#modal_succeeded').modal("show");
		}
	</script>

	<script type="text/javascript">
	
		//初始化执行一次
		setTimeout(function() {
			start();	  
		}, 100);
		
		function start(){
			var img = $("#img").val();
			if(img!=null&&img!=""){
				var show_img = document.getElementById('show_img');
				show_img.src="<%=base%>wap/public/showimg!showImg.action?imagePath="+img;
			}
		}
		
		function upload(){
			var fileReader = new FileReader();
		 	var formData = new FormData();
		 	var file = document.getElementById('fileName').files[0];
		 	formData.append("file", file);
		 	$.ajax({
	          	type: "POST",
	          	url: "<%=basePath%>normal/uploadimg!execute.action?random=" + Math.random(),
				data: formData,
			 	dataType: "json",
		        contentType: false,  
		        processData: false, 
				success : function(data) {
					$("#img").val(data.data)
					var show_img = document.getElementById('show_img');
					show_img.src="<%=base%>wap/public/showimg!showImg.action?imagePath=" + data.data;
				},
				error : function(XMLHttpRequest, textStatus, errorThrown) {
					console.log("请求错误");
				}
			});
		}
		
	</script>
	
</body>

</html>
