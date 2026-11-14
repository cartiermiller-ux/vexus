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

	<div class="ifr-dody">
	
		<div class="ifr-con">
			<h3>用户端内容管理</h3>
			
			<%@ include file="include/alert.jsp"%>
			
			<div class="row">
				<div class="col-md-12">
					<div class="panel panel-default">

						<div class="panel-title">查询条件</div>
						<div class="panel-body">
						
							<form class="form-horizontal" action="<%=basePath%>normal/adminCmsAction!list.action" 
								method="post" id="queryForm">
								
								<input type="hidden" name="pageNo" id="pageNo" value="${pageNo}">
								
								<div class="col-md-12 col-lg-4">
									<fieldset>
										<div class="control-group">
											<div class="controls">
											<input id="para_title" name="para_title"
													class="form-control " placeholder="标题" value="${para_title}"/>
											</div>
										</div>
									</fieldset>
								</div>
								
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
								
								<div class="col-md-12 col-lg-4">
									<fieldset>
										<div class="control-group">
											<div class="controls">
												<input id="content_code_para" name="content_code_para"
														class="form-control " placeholder="业务代码" value="${content_code_para}"/>
											</div>
										</div>
									</fieldset>
								</div>
								
								<div class="col-md-12 col-lg-2" style="margin-top:15px;">
									<button type="submit" class="btn  btn-block btn-light">查询</button>
								</div>
								
							</form>
							
						</div>
					</div>
				</div>
			</div>

			<div class="row">
				<div class="col-md-12">
					<!-- Start Panel -->
					<div class="panel panel-default">
					<div class="panel-title">查询结果</div>
					
					<c:if test="${security.isRolesAccessible('ROLE_ROOT')}">
					
						<a href="<%=basePath%>normal/adminCmsAction!toAdd.action" class="btn btn-light" style="margin-bottom: 10px">
							<i class="fa fa-pencil"></i>新增公告</a>
								
					</c:if>
					
					<div class="panel-body">
					
						<table class="table table-bordered table-striped">
						
							<thead>
								<tr>
									<td >标题</td>
									<td class="col-md-2">语言</td>
									<td class="col-md-2">业务代码</td>
									
									<c:if test="${security.isRolesAccessible('ROLE_ROOT,ROLE_ADMIN')
												 || security.isResourceAccessible('OP_CMS_OPERATE')}">
									
										<td class="col-md-2"></td>
										
									</c:if>
									
								</tr>
							</thead>
							
							<tbody>
								<c:forEach items="${page.getElements()}" var="item" varStatus="stat">
									<tr>
										<td>${item.title}</td>	
										<td class="col-md-2">${item.language}</td>
										<td class="col-md-2">${item.content_code}</td>
											
										<td>
									
											<c:if test="${security.isRolesAccessible('ROLE_ROOT,ROLE_ADMIN')
														 || security.isResourceAccessible('OP_CMS_OPERATE')}">
											
												<a href="<%=basePath%>normal/adminCmsAction!toUpdate.action?id=${item.id}"
													class="btn btn-light" style="margin-bottom: 10px">修改</a>
											
											</c:if>
											
										</td>
										
									</tr>
								</c:forEach>
							</tbody>
							
						</table>
						
						<%@ include file="include/page_simple.jsp"%>
						
					</div>

					</div>
				</div>
			</div>

		</div>
		
		<%@ include file="include/footer.jsp"%>
		
	</div>

	<%@ include file="include/js.jsp"%>

	<form action="<%=basePath%>normal/adminCmsAction!delete.action" method="post" id="ondelete">
	
		<input type="hidden" name="pageNo" id="pageNo" value="${pageNo}">
		<input type="hidden" name="news_id" id="id" value="${news_id}">
		
	</form>
	
	<script type="text/javascript">
		function ondelete(id) {
			$("#news_id").val(id);
			swal({
				title : "是否确认删除?",
				text : "",
				type : "warning",
				showCancelButton : true,
				confirmButtonColor : "#DD6B55",
				confirmButtonText : "确认",
				closeOnConfirm : false
			}, function() {
				document.getElementById("ondelete").submit();
			});
		}
	</script>

</body>

</html>
