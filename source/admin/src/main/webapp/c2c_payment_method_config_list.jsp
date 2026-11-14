<%@ page language="java" pageEncoding="utf-8" isELIgnored="false"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
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
			<h3>C2C支付方式模板</h3>
			
			<%@ include file="include/alert.jsp"%>
			
			<!-- //////////////////////////////////////////////////////////////////////////// -->
			<!-- START queryForm -->
			<div class="row">
				<div class="col-md-12">
					<div class="panel panel-default">

						<div class="panel-title">查询条件</div>
						<div class="panel-body">
						
							<form class="form-horizontal" action="<%=basePath%>normal/adminC2cPaymentMethodConfigAction!list.action" 
								method="post" id="queryForm">
								
								<input type="hidden" name="pageNo" id="pageNo" value="${pageNo}" />
								
								<div class="col-md-12 col-lg-3">
									<fieldset>
										<div class="control-group">
											<div class="controls">
												<select id="method_type_para" name="method_type_para" class="form-control " >
													<option value="">所有支付方式类型</option>
													<c:forEach items="${methodTypeMap}" var="item">
														<option value="${item.key}" <c:if test="${method_type_para == item.key}">selected="true"</c:if> >${item.value}</option>
													</c:forEach>
												</select>
											</div>
										</div>
									</fieldset>
								</div>
								
								<div class="col-md-12 col-lg-3">
									<fieldset>
										<div class="control-group">
											<div class="controls">
											<input id="method_name_para" name="method_name_para"
												class="form-control " placeholder="支付方式名称" value="${method_name_para}"/>
											</div>
										</div>
									</fieldset>
								</div>
								
								<div class="col-md-12 col-lg-2">
									<button type="submit" class="btn  btn-block btn-light">确定</button>
								</div>
								
							</form>
							
						</div>
						
					</div>
				</div>
				
			</div>
			<!-- END queryForm -->
			<!-- //////////////////////////////////////////////////////////////////////////// -->

			<div class="row">
				<div class="col-md-12">
					<!-- Start Panel -->
					<div class="panel panel-default">
					<div class="panel-title">查询结果</div>
					
					<c:if test="${security.isRolesAccessible('ROLE_ROOT,ROLE_ADMIN')
								|| security.isResourceAccessible('OP_C2C_PAYMENT_METHOD_CONFIG_OPERATE')}">
					
						<a href="<%=basePath%>normal/adminC2cPaymentMethodConfigAction!toAdd.action" class="btn btn-light" style="margin-bottom: 10px">
							<i class="fa fa-pencil"></i>新增支付方式模板</a>
					
						<a href="<%=basePath%>normal/adminC2cPaymentMethodConfigAction!pmtLang.action" class="btn btn-light" style="margin-left: 10px;margin-bottom: 10px;">
							<i class="fa fa-pencil"></i>支付方式类型 多语言配置</a>
								
					</c:if>
						
					<div class="panel-body">
					
						<table class="table table-bordered table-striped">
						
							<thead>
								<tr>
								
									<td>支付方式类型</td>
									<td>支付方式名称</td>
									<td>支付方式图片</td>
									<td>参数名1</td>
									<td>参数名2</td>
									<td>参数名3</td>
									<td>参数名4</td>
									<td>参数名5</td>
									<td>参数名6</td>
									<td>参数名7</td>
									<td>参数名8</td>
									<td>参数名9</td>
									<td>参数名10</td>
									<td>参数名11</td>
									<td>参数名12</td>
									<td>参数名13</td>
									<td>参数名14</td>
									<td>参数名15</td>
									<td>创建时间</td>
									<td>更新时间</td>
									
									<c:if test="${security.isRolesAccessible('ROLE_ROOT,ROLE_ADMIN')
												 || security.isResourceAccessible('OP_C2C_PAYMENT_METHOD_CONFIG_OPERATE')}">
										
										<td width="130px"></td>
										
									</c:if>

								</tr>
							</thead>
							
							<tbody>
								<c:forEach items="${page.getElements()}" var="item" varStatus="stat">
									<tr>										
										
										<td>${item.method_type_name}</td>
										<td>${item.method_name}</td>
										<td>
											<img width="40px" height="40px" src="<%=base%>wap/public/showimg!showImg.action?imagePath=${item.method_img}" />
										</td>
										<td>${item.param_name1}</td>
										<td>${item.param_name2}</td>
										<td>${item.param_name3}</td>
										<td>${item.param_name4}</td>
										<td>${item.param_name5}</td>
										<td>${item.param_name6}</td>
										<td>${item.param_name7}</td>
										<td>${item.param_name8}</td>
										<td>${item.param_name9}</td>
										<td>${item.param_name10}</td>
										<td>${item.param_name11}</td>
										<td>${item.param_name12}</td>
										<td>${item.param_name13}</td>
										<td>${item.param_name14}</td>
										<td>${item.param_name15}</td>
										<td>${item.create_time}</td>
										<td>${item.update_time}</td>
										
										<td>
											
											<c:if test="${security.isRolesAccessible('ROLE_ROOT,ROLE_ADMIN')
														 || security.isResourceAccessible('OP_C2C_PAYMENT_METHOD_CONFIG_OPERATE')}">
												
												<div class="btn-group">
													<button type="button" class="btn btn-light">操作</button>
													<button type="button"
														class="btn btn-light dropdown-toggle"
														data-toggle="dropdown" aria-expanded="false">
														<span class="caret"></span> <span class="sr-only">Toggle Dropdown</span>
													</button>
													<ul class="dropdown-menu" role="menu">
													
														<li><a href="<%=basePath%>normal/adminC2cPaymentMethodConfigAction!toUpdate.action?id=${item.id}">修改</a></li>
														<li><a href="javascript:ondelete('${item.id}')">删除</a></li>
														<li><a href="<%=basePath%>normal/adminC2cPaymentMethodConfigAction!toUpdateLanguage.action?id=${item.id}">多语言配置</a></li>
	
													</ul>
												</div>
											
											</c:if>
											
										</td>
										
									</tr>
								</c:forEach>

							</tbody>
						</table>
						
						<%@ include file="include/page_simple.jsp"%>

					</div>

					</div>
					<!-- End Panel -->

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
															
	<form action="<%=basePath%>normal/adminC2cPaymentMethodConfigAction!delete.action"
		method="post" id="ondelete">
		
		<input type="hidden" name="pageNo" id="pageNo" value="${pageNo}" />			
		<input type="hidden" name="id" id="method_id" value="${id}" />
		
		<div class="col-sm-1">
			<!-- 模态框（Modal） -->
			<div class="modal fade" id="modal_succeeded" tabindex="-1"
				role="dialog" aria-labelledby="myModalLabel"
				aria-hidden="true">
				<div class="modal-dialog">
					<div class="modal-content" >
					
						<div class="modal-header">
							<button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
							<h4 class="modal-title" id="myModalLabel">确认删除</h4>
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
		
	</form>
	
	<script type="text/javascript">
		
		function ondelete(id) {				
			$("#method_id").val(id);
			$('#modal_succeeded').modal("show");
		}
		
	</script>

</body>

</html>
