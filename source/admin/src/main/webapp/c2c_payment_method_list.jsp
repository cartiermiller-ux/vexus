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
			<h3>C2C支付方式管理</h3>
			
			<%@ include file="include/alert.jsp"%>
			
			<!-- //////////////////////////////////////////////////////////////////////////// -->
			<!-- START queryForm -->
			<div class="row">
				<div class="col-md-12">
					<div class="panel panel-default">

						<div class="panel-title">查询条件</div>
						<div class="panel-body">
						
							<form class="form-horizontal" action="<%=basePath%>normal/adminC2cPaymentMethodAction!list.action" 
								method="post" id="queryForm">
								
								<input type="hidden" name="pageNo" id="pageNo" value="${pageNo}" />
								
								<div class="col-md-12 col-lg-3">
									<fieldset>
										<div class="control-group">
											<div class="controls">
											<input id="user_code_para" name="user_code_para" class="form-control " placeholder="用户名、用户UID" value="${user_code_para}"/>
											</div>
										</div>
									</fieldset>
								</div>
								
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
								|| security.isResourceAccessible('OP_C2C_PAYMENT_METHOD_OPERATE')}">
					
						<a href="<%=basePath%>normal/adminC2cPaymentMethodAction!toAdd.action" class="btn btn-light" style="margin-bottom: 10px">
							<i class="fa fa-pencil"></i>新增支付方式</a>
								
					</c:if>
						
					<div class="panel-body">
					
						<table class="table table-bordered table-striped">
						
							<thead>
								<tr>
								
									<td>用户名</td>
									<td>用户UID</td>
									<td>支付方式类型</td>
									<td>支付方式名称</td>
									<td>支付方式图片</td>
									<td>真实姓名</td>
									<td>参数名1:<br />参数值1</td>
									<td>参数名2:<br />参数值2</td>
									<td>参数名3:<br />参数值3</td>
									<td>更多参数</td>
									<td>支付二维码</td>
									<td>备注</td>
									<td>创建时间</td>
									<td>更新时间</td>
									
									<c:if test="${security.isRolesAccessible('ROLE_ROOT,ROLE_ADMIN')
												 || security.isResourceAccessible('OP_C2C_PAYMENT_METHOD_OPERATE')}">
										
										<td width="130px"></td>
										
									</c:if>

								</tr>
							</thead>
							
							<tbody>
								<c:forEach items="${page.getElements()}" var="item" varStatus="stat">
									<tr>										
										
										<td>${item.user_name}</td>
										<td>${item.user_code}</td>
										<td>${item.method_type_name}</td>
										<td>${item.method_name}</td>
										<td>
											<img width="40px" height="40px" src="<%=base%>wap/public/showimg!showImg.action?imagePath=${item.method_img}" />
										</td>
										<td>${item.real_name}</td>
										<td>${item.param_name1}:<br />${item.param_value1}</td>
										<td>${item.param_name2}:<br />${item.param_value2}</td>
										<td>${item.param_name3}:<br />${item.param_value3}</td>										
										<td>
											<a href="#" onClick="moreInfo(
												'${item.param_name4}',
												'${item.param_value4}',
												'${item.param_name5}',
												'${item.param_value5}',
												'${item.param_name6}',
												'${item.param_value6}',
												'${item.param_name7}',
												'${item.param_value7}',
												'${item.param_name8}',
												'${item.param_value8}',
												'${item.param_name9}',
												'${item.param_value9}',
												'${item.param_name10}',
												'${item.param_value10}',
												'${item.param_name11}',
												'${item.param_value11}',
												'${item.param_name12}',
												'${item.param_value12}',
												'${item.param_name13}',
												'${item.param_value13}',
												'${item.param_name14}',
												'${item.param_value14}',
												'${item.param_name15}',
												'${item.param_value15}')">
												 点击查看
											</a>
										</td>
										<td>
											<c:if test="${null != item.qrcode && '' != item.qrcode}">
												<img width="40px" height="40px" src="<%=base%>wap/public/showimg!showImg.action?imagePath=${item.qrcode}" />
											</c:if>
										</td>
										<td>${item.remark}</td>
										<td>${item.create_time}</td>
										<td>${item.update_time}</td>
										
										<td>
											
											<c:if test="${security.isRolesAccessible('ROLE_ROOT,ROLE_ADMIN')
														 || security.isResourceAccessible('OP_C2C_PAYMENT_METHOD_OPERATE')}">
												
												<div class="btn-group">
													<button type="button" class="btn btn-light">操作</button>
													<button type="button"
														class="btn btn-light dropdown-toggle"
														data-toggle="dropdown" aria-expanded="false">
														<span class="caret"></span> <span class="sr-only">Toggle Dropdown</span>
													</button>
													<ul class="dropdown-menu" role="menu">
													
														<li><a href="<%=basePath%>normal/adminC2cPaymentMethodAction!toUpdate.action?id=${item.id}">修改</a></li>
														<li><a href="javascript:ondelete('${item.id}')">删除</a></li>
	
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
	
		<!-- 模态框（Modal） -->
		<div class="modal fade" id="modal_more_info" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
			<div class="modal-dialog">
				<div class="modal-content" style="width: 725px;">
				
					<div class="modal-header">
						<button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
						<h4 class="modal-title">更多参数</h4>
					</div>
					
					<div class="modal-body"> <div class="form-group"> <label class="col-sm-3 control-label form-label">参数名4:参数值4</label>
							<div class="col-sm-8"> <input id="modal_param4" class="form-control" readonly="readonly" /> </div> </div> </div>
					<div class="modal-body"> <div class="form-group"> <label class="col-sm-3 control-label form-label">参数名5:参数值5</label>
							<div class="col-sm-8"> <input id="modal_param5" class="form-control" readonly="readonly" /> </div> </div> </div>
					<div class="modal-body"> <div class="form-group"> <label class="col-sm-3 control-label form-label">参数名6:参数值6</label>
							<div class="col-sm-8"> <input id="modal_param6" class="form-control" readonly="readonly" /> </div> </div> </div>
					<div class="modal-body"> <div class="form-group"> <label class="col-sm-3 control-label form-label">参数名7:参数值7</label>
							<div class="col-sm-8"> <input id="modal_param7" class="form-control" readonly="readonly" /> </div> </div> </div>
					<div class="modal-body"> <div class="form-group"> <label class="col-sm-3 control-label form-label">参数名8:参数值8</label>
							<div class="col-sm-8"> <input id="modal_param8" class="form-control" readonly="readonly" /> </div> </div> </div>
					<div class="modal-body"> <div class="form-group"> <label class="col-sm-3 control-label form-label">参数名9:参数值9</label>
							<div class="col-sm-8"> <input id="modal_param9" class="form-control" readonly="readonly" /> </div> </div> </div>
					<div class="modal-body"> <div class="form-group"> <label class="col-sm-3 control-label form-label">参数名10:参数值10</label>
							<div class="col-sm-8"> <input id="modal_param10" class="form-control" readonly="readonly" /> </div> </div> </div>
					<div class="modal-body"> <div class="form-group"> <label class="col-sm-3 control-label form-label">参数名11:参数值11</label>
							<div class="col-sm-8"> <input id="modal_param11" class="form-control" readonly="readonly" /> </div> </div> </div>
					<div class="modal-body"> <div class="form-group"> <label class="col-sm-3 control-label form-label">参数名12:参数值12</label>
							<div class="col-sm-8"> <input id="modal_param12" class="form-control" readonly="readonly" /> </div> </div> </div>
					<div class="modal-body"> <div class="form-group"> <label class="col-sm-3 control-label form-label">参数名13:参数值13</label>
							<div class="col-sm-8"> <input id="modal_param13" class="form-control" readonly="readonly" /> </div> </div> </div>
					<div class="modal-body"> <div class="form-group"> <label class="col-sm-3 control-label form-label">参数名14:参数值14</label>
							<div class="col-sm-8"> <input id="modal_param14" class="form-control" readonly="readonly" /> </div> </div> </div>
					<div class="modal-body"> <div class="form-group"> <label class="col-sm-3 control-label form-label">参数名15:参数值15</label>
							<div class="col-sm-8"> <input id="modal_param15" class="form-control" readonly="readonly" /> </div> </div> </div>
					
					<div class="modal-footer" style="margin-top: 0;">
						<button type="button" class="btn " data-dismiss="modal">关闭</button>
					</div>
					
				</div>
			</div>
		</div>

		<%@ include file="include/footer.jsp"%>

	</div>
	<!-- End Content -->
	<!-- //////////////////////////////////////////////////////////////////////////// -->

	<%@ include file="include/js.jsp"%>
															
	<form action="<%=basePath%>normal/adminC2cPaymentMethodAction!delete.action"
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

		function moreInfo(param_name4, param_value4, param_name5, param_value5, param_name6, param_value6, param_name7, param_value7, 
				param_name8, param_value8, param_name9, param_value9, param_name10, param_value10, param_name11, param_value11, 
				param_name12, param_value12, param_name13, param_value13, param_name14, param_value14, param_name15, param_value15) {
			$("#modal_param4").val(param_name4 + ":" + param_value4);
			$("#modal_param5").val(param_name5 + ":" + param_value5);
			$("#modal_param6").val(param_name6 + ":" + param_value6);
			$("#modal_param7").val(param_name7 + ":" + param_value7);
			$("#modal_param8").val(param_name8 + ":" + param_value8);
			$("#modal_param9").val(param_name9 + ":" + param_value9);
			$("#modal_param10").val(param_name10 + ":" + param_value10);
			$("#modal_param11").val(param_name11 + ":" + param_value11);
			$("#modal_param12").val(param_name12 + ":" + param_value12);
			$("#modal_param13").val(param_name13 + ":" + param_value13);
			$("#modal_param14").val(param_name14 + ":" + param_value14);
			$("#modal_param15").val(param_name15 + ":" + param_value15);
			$('#modal_more_info').modal("show");
		}
		
		function ondelete(id) {				
			$("#method_id").val(id);
			$('#modal_succeeded').modal("show");
		}
		
	</script>

</body>

</html>
