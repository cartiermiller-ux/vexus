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
			<h3>C2C支付方式模板</h3>
			
			<%@ include file="include/alert.jsp"%>
			
			<!-- //////////////////////////////////////////////////////////////////////////// -->
			<!-- START queryForm -->
			<form action="<%=basePath%>normal/adminC2cPaymentMethodConfigAction!list.action" method="post" id="queryForm">
				<input type="hidden" name="pageNo" id="pageNo" value="${pageNo}" />
			</form>
			<!-- END queryForm -->
			<!-- //////////////////////////////////////////////////////////////////////////// -->
			
			<div class="row">
				<div class="col-md-12 col-lg-12">
					<div class="panel panel-default">

						<div class="panel-title">
							支付方式类型 多语言配置
							<ul class="panel-tools">
								<li><a class="icon minimise-tool"><i class="fa fa-minus"></i></a></li>
								<li><a class="icon expand-tool"><i class="fa fa-expand"></i></a></li>
							</ul>
						</div>

						<div class="panel-body">
						
							<form class="form-horizontal" action="<%=basePath%>normal/adminC2cPaymentMethodConfigAction!pmtLang.action"
								method="post" name="mainForm" id="mainForm">
								
								<div class="form-group">
									<label class="col-sm-2 control-label form-label"></label>
									<div class="col-sm-8">
										<p class="ballon color1">
											1. 多语言格式： 语言和翻译“##”分隔，不同语言“&&“分隔，如  zh-CN##账户号&&en##Account Number											
											<br />2. 语种： ${languageIntro}											
											<br />3. 支付方式类型： ${methodTypeIntro}
										</p>
									</div>
								</div>
								
								<c:forEach items="${methodTypeList}" var="item" varStatus="stat">
								
									<div class="form-group">
										<label class="col-sm-2 control-label form-label">类型 ${item.typeId}</label>
										<div class="col-sm-8">
											<input class="form-control " readonly="readonly" value="${item.name}" />
										</div>
									</div>
									<div class="form-group">
										<label class="col-sm-2 control-label form-label">类型 ${item.typeId}<br />多语言</label>
										<div class="col-sm-8">
											<textarea id="${item.trans_typeId}" class="form-control input-lg" rows="4" cols="20" >${item.trans}</textarea>
										</div>
										<div class="col-sm-2"> <a href="javascript:savePmtLangTrans('${item.trans_typeId}', '${item.name}')" class="btn btn-default">保存</a> </div>
									</div>
									
								</c:forEach>
				                
								<div class="form-group">
									<div class="col-sm-offset-2 col-sm-10">
										<a href="javascript:goUrl(${pageNo})" class="btn">返回</a>
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
		
		function savePmtLangTrans(trans_typeId, name) {			
			var url = "<%=basePath%>normal/adminC2cPaymentMethodConfigAction!savePmtTranslate.action";
			var data = 
			{
				"name": name,
				"trans": document.getElementById(trans_typeId).value
			};
			goNewAjaxUrl(url, data, function(tmp) {
			}, function() {
			});
		}
		
		function goNewAjaxUrl(targetUrl, data, Func, Fail) {
			$.ajax({
				url: targetUrl,
				data: data,
				type: 'get',
				dataType: "json",
				success: function (res) {
					// var tmp = $.parseJSON(res)
					var tmp = res;
				    if (tmp.code == 200) {
				    	Func(tmp);
				    	swal({
							title : "保存成功",
							text : "",
							type : "success",
							showCancelButton : false,
							confirmButtonColor : "#DD6B55",
							confirmButtonText : "确认",
							closeOnConfirm : false
						});
				    } else if (tmp.code == 500) {
				    	Fail();
				    	swal({
							title : tmp.message,
							text : "",
							type : "warning",
							showCancelButton : true,
							confirmButtonColor : "#DD6B55",
							confirmButtonText : "确认",
							closeOnConfirm : false
						});
				    }
				},
				error : function(XMLHttpRequest, textStatus, errorThrown) {
					swal({
						title : "请求错误",
						text : "",
						type : "warning",
						showCancelButton : true,
						confirmButtonColor : "#DD6B55",
						confirmButtonText : "确认",
						closeOnConfirm : false
					});
					console.log("请求错误");
				}
			});
		}
		
	</script>

</body>

</html>
