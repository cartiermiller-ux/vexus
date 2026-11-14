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
<%-- 	<%@ include file="include/top.jsp"%> --%>
<%-- 	<%@ include file="include/menu_left.jsp"%> --%>

	<!-- //////////////////////////////////////////////////////////////////////////// -->
	<!-- START CONTENT -->
	<div class="ifr-dody">

		<!-- //////////////////////////////////////////////////////////////////////////// -->
		<!-- START CONTAINER -->
		<div class="ifr-con">
			<h3>矿池产出数据</h3>
			
			<!-- //////////////////////////////////////////////////////////////////////////// -->
			<!-- START queryForm -->
			<%@ include file="include/alert.jsp"%>
			<!-- END queryForm -->
			<!-- //////////////////////////////////////////////////////////////////////////// -->

			<div class="row">
				<div class="col-md-12">
					<!-- Start Panel -->
					<div class="panel panel-default">
						<p class="ballon color1">
							矿池产出数据将会根据实际产出进行变动，如修改将会展示修改后的数据 <br /> 前端展示数据 : 数据量*数据倍率
						</p>
						<div class="panel-body">

							<table class="table table-bordered table-striped">
							
								<thead>
									<tr>
										<td>数据名称</td>
										<td>数据量</td>
										<td>数据倍率</td>
										<td width="130px"></td>
									</tr>
								</thead>
								
								<tbody>
									<!-- <s:iterator value="page.elements" status="stat"> -->
									<c:forEach items="${page.getElements()}" var="item" varStatus="stat">
										<tr>
											<td>${item.name_cn)}</td>
											<td>${item.totle)}</td>
											<td>${item.rate)}</td>
											<td>
												<c:if test="${item.status == '0'}">
													<span class="right label label-danger">未启用</span>
												</c:if>
											</td>
											<td>
												<div class="btn-group">
													<button type="button" class="btn btn-light">操作</button>
													<button type="button" class="btn btn-light dropdown-toggle"
														data-toggle="dropdown" aria-expanded="false">
														<span class="caret"></span> <span class="sr-only">Toggle Dropdown</span>
													</button>
													<ul class="dropdown-menu" role="menu">
														<li><a
															href="<%=basePath%>normal/adminAutoMonitorPoolDataAction!toUpdatePrivateKey.action?id=${item.id}">修改</a></li>
														<%-- <li><a href="<%=basePath%>normal/adminMinerParaAction!list.action?miner_id=<s:property value="id" />">周期配置</a></li> --%>
														<!-- 
														<li><a href="<%=basePath%>normal/adminFinanceAction!toDelete.action?id=<s:property value="id" />">删除</a></li>
														 -->
													</ul>
												</div>
											</td>
										</tr>
									<!-- </s:iterator> -->
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
	
	<script src="<%=basePath%>js/bootstrap/bootstrap-treeview.js"></script>
	
	<script>
		$(function() {
			var data = ${result};
			$("#treeview4").treeview({
				color : "#428bca",
				enableLinks : true,
				nodeIcon : "glyphicon glyphicon-user",
				data : data,
				levels : 4,
			});
		});
	</script>

	<script type="text/javascript">
		function enabled(id, address) {
			$('#enabled_id').val(id);
			$('#enabled_address').html(address);
			$('#modal_succeeded').modal("show");
		}
	</script>
	
</body>

</html>
