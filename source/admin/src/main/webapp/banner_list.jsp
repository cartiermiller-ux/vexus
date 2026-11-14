<%@ page language="java" pageEncoding="utf-8" isELIgnored="false"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
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
			<h3>横幅管理</h3>
			
			<%@ include file="include/alert.jsp"%>
			
			<!-- //////////////////////////////////////////////////////////////////////////// -->
			<!-- START queryForm -->
			<div class="row">
				<div class="col-md-12">
					<div class="panel panel-default">

						<div class="panel-title">查询条件</div>
						<div class="panel-body">
						
							<form class="form-horizontal" action="<%=basePath%>normal/adminBannerAction!list.action"
								method="post" id="queryForm">
								
								<input type="hidden" name="pageNo" id="pageNo" value="${pageNo}" />

								<div class="col-md-12 col-lg-4">
									<fieldset>
										<div class="control-group">
											<div class="controls">
												<select id="para_language" name="para_language" class="form-control " >
													<option value="">所有语言</option>
													<c:forEach items="${languageMap}" var="item">
														<option value="${item.key}" <c:if test="${para_language == item.key}">selected="true"</c:if> >${item.value}</option>
													</c:forEach>
												</select>
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
					
						<c:if test="${security.isRolesAccessible('ROLE_ROOT')}">
						
							<a href="<%=basePath%>normal/adminBannerAction!toAdd.action" class="btn btn-light" style="margin-bottom: 10px">
								<i class="fa fa-pencil"></i>新增横幅</a>
									
						</c:if>
						
						<div class="panel-body">
						
							<table class="table table-bordered table-striped">
							
								<thead>
									<tr>
					
										<c:if test="${security.isRolesAccessible('ROLE_ROOT')}">
										
											<td>业务代码</td>
											<td>模块</td>
									
										</c:if>
						
										<td class="col-md-2">语言</td>
										<!-- <td >访问路径</td> -->
										<td>图片</td>
										<td>可否点击</td>
										<td>是否展示</td>
										<td>排序</td>
										<!-- <td class="col-md-2" width="150px">日期</td> -->
									
										<c:if test="${security.isRolesAccessible('ROLE_ROOT,ROLE_ADMIN')
													 || security.isResourceAccessible('OP_BANNER_OPERATE')}">
											
											<td class="col-md-2"></td>
											
										</c:if>
										
									</tr>
								</thead>
								
								<tbody>
									<c:forEach items="${page.getElements()}" var="item" varStatus="stat">
										<tr>
					
											<c:if test="${security.isRolesAccessible('ROLE_ROOT')}">

												<td>${item.content_code}</td>
												<td>${item.model}</td>
									
											</c:if>
										
											<td class="col-md-2">${item.language}</td>
											<%-- <td class="col-md-2">${item.url}</td> --%>
											<td class="col-md-2"><img alt="" width="150px" height="55px" src="${item.image}"></td>
											<td>
												<c:if test="${item.click==1}">
													<span class="right label label-success">
														是
													</span>
												</c:if>
												<c:if test="${item.click!=1}">
													<span class="right label label-danger">
														否
													</span>
												</c:if>
											</td>
											<td>
												<c:if test="${item.on_show==1}">
													<span class="right label label-success">
														是
													</span>
												</c:if>
												<c:if test="${item.on_show!=1}">
													<span class="right label label-danger">
														否
													</span>
												</c:if>
											</td>
											<td>${item.sort_index}</td>
											<%-- <td>${item.createTime}</td> --%>

											<td>
									
												<c:if test="${security.isRolesAccessible('ROLE_ROOT,ROLE_ADMIN')
															 || security.isResourceAccessible('OP_BANNER_OPERATE')}">

													<div class="btn-group">
														<button type="button" class="btn btn-light">操作</button>
														<button type="button" class="btn btn-light dropdown-toggle"
															data-toggle="dropdown" aria-expanded="false">
															<span class="caret"></span> <span class="sr-only">Toggle Dropdown</span>
														</button>
														<ul class="dropdown-menu" role="menu">

															<li><a href="<%=basePath%>normal/adminBannerAction!toUpdate.action?id=${item.id}">修改</a></li>
																				
															<c:if test="${security.isRolesAccessible('ROLE_ROOT')}">

																<li><a href="javascript:ondelete('${item.id}')">删除</a></li>
									
															</c:if>

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

	<form action="<%=basePath%>normal/adminBannerAction!delete.action" method="post" id="ondelete">
	
		<input type="hidden" name="pageNo" id="pageNo" value="${pageNo}" />			
		<input type="hidden" name="id" id="news_id" value="${id}" />
		
		<div class="col-sm-1">
			<!-- 模态框（Modal） -->
			<div class="modal fade" id="modal_succeeded" tabindex="-1"
				role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
				<div class="modal-dialog">
					<div class="modal-content">
					
						<div class="modal-header">
							<button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
							<h4 class="modal-title" id="myModalLabel">确认删除</h4>
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
		
	</form>
	
	<script type="text/javascript">
		function ondelete(id) {
			$("#news_id").val(id);
			$('#modal_succeeded').modal("show");
		}
	</script>

</body>

</html>
