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
			
			<form action="<%=basePath%>normal/adminExchangeRateAction!list.action" method="post" id="queryForm">
				 <input type="hidden" name="pageNo" id="pageNo" value="${pageNo}"/>
			</form>
			
			<div class="row">
				<div class="col-md-12 col-lg-12">
					<div class="panel panel-default">

						<div class="panel-title">
							修改货币汇率
							<ul class="panel-tools">
								<li><a class="icon minimise-tool"><i class="fa fa-minus"></i></a></li>
								<li><a class="icon expand-tool"><i class="fa fa-expand"></i></a></li>
							</ul>
						</div>

						<div class="panel-body">
						
							<form class="form-horizontal" action="<%=basePath%>normal/adminExchangeRateAction!update.action" method="post" name="mainForm" id="mainForm">
							
								<input type="hidden" name="id" id="id" value="${id}"/>
								
								<div class="form-group">
									<label class="col-sm-2 control-label form-label">汇率</label>
									<div class="col-sm-3">
										<input id="rata" name="rata" class="form-control " value="${rata}"/>
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
	
	<script type="text/javascript">
	   function submit(){
		   swal({
			   title : "是否保存?",
				text : "",
				type : "warning",
				showCancelButton : true,
				confirmButtonColor : "#DD6B55",
				confirmButtonText : "确认",
				closeOnConfirm : false
			}, function() {
				document.getElementById("mainForm").submit();
			});	  
	  	}
	</script>
	
</body>

</html>
