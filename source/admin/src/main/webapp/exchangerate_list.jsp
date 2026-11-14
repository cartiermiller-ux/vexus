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
			<h3>汇率配置</h3>
			
			<%@ include file="include/alert.jsp"%>
			
			<div class="row">
				<div class="col-md-12">
					<div class="panel panel-default">

						<div class="panel-title">查询条件</div>
						<div class="panel-body">
						
							<form class="form-horizontal"
								action="<%=basePath%>normal/adminExchangeRateAction!list.action"
								method="post" id="queryForm">
								
								<input type="hidden" name="pageNo" id="pageNo" value="${pageNo}">
								
								<div class="col-md-12 col-lg-6">
									<fieldset>
										<div class="control-group">
											<div class="controls">
												<input id="currency" name="currency"
													class="form-control " placeholder="货币代码" value="${currency}"/>
											</div>
										</div>
									</fieldset>
								</div>

								<div class="col-md-12 col-lg-2">
									<button type="submit" class="btn btn-light btn-block">查询</button>
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
						<div class="panel-body">
						
							<table class="table table-bordered table-striped">
							
								<thead>
									<tr>
										<td>货币</td>
										<td>汇入汇出</td>
										<td>汇率</td>
										<td width="130px"></td>
									</tr>
								</thead>
								
								<tbody>
								    <c:forEach items="${page.getElements()}" var="item" varStatus="stat">
										<tr>
											<td>${item.name}(${item.currency})</td>
											<td>
												<c:if test="${item.out_or_in=='in'}">汇入</c:if>
												<c:if test="${item.out_or_in=='out'}">汇出</c:if>
											</td>
											<td>${item.rata}</td>
										
											<td>
											
												<c:if test="${security.isRolesAccessible('ROLE_ROOT,ROLE_ADMIN')
															 || security.isResourceAccessible('OP_EXCHANGE_RATE_OPERATE')}">
												
													<div class="btn-group">
														<button type="button" class="btn btn-light">操作</button>
														<button type="button" class="btn btn-light dropdown-toggle"
															data-toggle="dropdown" aria-expanded="false">
															<span class="caret"></span> <span class="sr-only">Toggle Dropdown</span>
														</button>
														<ul class="dropdown-menu" role="menu">
														
															<li><a href="<%=basePath%>normal/adminExchangeRateAction!toUpdate.action?
															id=${item.id}&rata=${item.rata}">修改</a></li>
															
														</ul>
													</div>
													
												</c:if>
											
											</td>

										</tr>
									</c:forEach>
								</tbody>
								
							</table>
							
							<%@ include file="include/page_simple.jsp"%>
							
							<!-- <nav> -->
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
	
</body>

</html>
