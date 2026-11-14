<%@ page language="java" pageEncoding="utf-8" isELIgnored="false"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
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
	<div class="ifr-dody">
		<input type="hidden" name="session_token" id="session_token" value="${session_token}" />
		<div class="ifr-con">
			<h3>质押借币订单</h3>
			<%@ include file="include/alert.jsp"%>
			<div class="row">
				<div class="col-md-12">
					<div class="panel panel-default">
						<div class="panel-title">查询条件</div>
						<div class="panel-body">
							<form class="form-horizontal"
								action="<%=basePath%>normal/adminLoanOrder!list.action" method="post" id="queryForm">
								<input type="hidden" name="pageNo" id="pageNo" value="${pageNo}">
								<input type="hidden" name="state" id="state" value="${state}" />
								<div class="col-md-12 col-lg-3">
									<fieldset>
										<div class="control-group">
											<div class="controls">
												<input id="orderNo" name="orderNo"
													class="form-control " placeholder="订单号（完整）" value="${orderNo}" />
											</div>
										</div>
									</fieldset>
								</div>

								<div class="col-md-12 col-lg-3">
									<fieldset>
										<div class="control-group">
											<div class="controls">
												<input id="userParam" name="userParam" class="form-control "
													placeholder="用户名、UID" value="${userParam}" />
											</div>
										</div>
									</fieldset>
								</div>

								<div class="col-md-12 col-lg-2">
									<fieldset>
										<div class="control-group">
											<div class="controls">
												<select id="roleName" name="roleName"
													class="form-control ">
													<option value="">所有账号</option>
													<option value="MEMBER"
														<c:if test="${roleName == 'MEMBER'}">selected="true"</c:if>>正式账号</option>
													<option value="GUEST"
														<c:if test="${roleName == 'GUEST'}">selected="true"</c:if>>演示账号</option>
												</select>
											</div>
										</div>
									</fieldset>
								</div>

								<div class="col-md-12 col-lg-2">
									<button type="submit" class="btn btn-light btn-block">查询</button>
								</div>
								
								<div class="col-md-12 col-lg-12" style="margin-top: 10px;">
									<div class="mailbox clearfix">
										<div class="mailbox-menu">
											<ul class="menu">
												<li><a href="javascript:setState('')"> 全部</a></li>
												<li><a href="javascript:setState(1)"> 计息中</a></li>
												<li><a href="javascript:setState(2)"> 已结清</a></li>
												<li><a href="javascript:setState(3)"> 强平结清</a></li>
											</ul>
										</div>
									</div>
								</div>

							</form>

						</div>

					</div>
				</div>
			</div>

			<div class="row">
				<div class="col-md-12">
					<div class="panel panel-default">
						<div class="panel-title">查询结果</div>
						<div class="panel-body">
							<table class="table table-bordered table-striped">
								<thead>
									<tr>
										<td>用户名</td>
										<td>UID</td>
										<td>账户类型</td>
										<td>订单号</td>
										<td>订单类型</td>
										<td>借款金额</td>
										<td>订单状态</td>
										<td>贷款币种</td>
										<td>质押率</td>
										<td>总负债</td>
										<td>总利息</td>
										<td>时利率</td>
										<td>借款周期</td>
										<td>借款时间</td>
										<td>到期时间</td>
										<!-- <td width="150px"></td> -->
									</tr>
								</thead>

								<tbody style="font-size: 13px;">
									<c:forEach items="${page.getElements()}" var="item" varStatus="stat">
									<tr>
									    <td>${item.username}</td>
										<td>${item.usercode}</td>											
										<td>
											<c:choose>
												<c:when test="${item.rolename=='GUEST'}">
													<span class="right label label-warning">演示账号</span>
												</c:when>
												<c:when test="${item.rolename=='TEST'}">
													<span class="right label label-default">${item.roleNameDesc}</span>
												</c:when>
												<c:when test="${item.rolename=='MEMBER'}">
													<span class="right label label-success">正式账号</span>
												</c:when>
												<c:otherwise>--</c:otherwise>	
											</c:choose>
										</td>
										<td>
										    <a style="font-size: 10px;" href="#" onClick="queryLoanRelation('${item.id}')">
												${item.orderNo}
											</a>
										</td>
										<td>
										    <c:if test="${item.orderType == 1}">借款</c:if> 
										</td>
										<td>${item.loanAmount}</td>
										<td>
											<c:if test="${item.state == 1}">计息中</c:if> 
											<c:if test="${item.state == 2}">已结清</c:if> 
											<c:if test="${item.state == 3}">强平结清</c:if> 
										</td>
										<td>${item.loanCurrency}</td>
										<td>${item.pledgeRate}</td>
										<td>${item.debtAmount}</td>
										<td>${item.interestAmount}</td>
										<td>${item.hourlyRate}</td>
										<td>${item.loanCycle}</td>
										<td>${item.createTime}</td>
										<td>${item.expireTime}</td>
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
	
	<div class="form-group">
		<div class="col-sm-1">
			<div class="modal fade" id="loanRelation" tabindex="-1"
				role="dialog" aria-labelledby="myModalLabel"
				aria-hidden="true">
				<div class="modal-dialog">
					<div class="modal-content">
						<div class="modal-header">
							<button type="button" class="close"
								data-dismiss="modal" aria-hidden="true">&times;</button>
							<h4 class="modal-title" id="myModalLabel">质押记录</h4>
						</div>
						
						<div class="modal-body" style="max-height: 400px;overflow-y: scroll;">
							<table class="table table-bordered table-striped" >
								<thead>
									<tr>
										<td>订单类型</td>
										<td>金额</td>
										<td>质押类型</td>
										<td>质押金额</td>
										<td>质押币种</td>
										<td>时间</td>
									</tr>
								</thead>
								<tbody id="modalLoanRelation">
									<%@ include file="include/loading.jsp"%>
								</tbody>
							</table>
						</div>
						
					</div>
				</div>
			</div>
		</div>
	</div>
	
	<%@ include file="include/js.jsp"%>
	
	<script type="text/javascript">
	
		function queryLoanRelation(id){
			$("#loanRelation").modal("show");		
			var url = "<%=basePath%>normal/adminLoanOrder!queryLoanRelation.action";
			var data = {"id" : id};
			goNewAjaxUrl(url, data, function(tmp){
				var str = '';
				var content = '';
				for(var i=0; i<tmp.loanRelation.length; i++){
					str += '<tr>'
						+'<td>'+ getOrderType(tmp.loanRelation[i].orderType) +'</td>'
						+'<td>'+ geAmount(tmp.loanRelation[i].loanAmount) +'</td>'
						+'<td>'+ getPledgeType(tmp.loanRelation[i].pledgeType) +'</td>'
						+'<td>'+ tmp.loanRelation[i].pledgeAmount +'</td>'
						+'<td>'+ getPledgeCurrency(tmp.loanRelation[i].pledgeCurrency) +'</td>'
						+'<td>'+ tmp.loanRelation[i].createTime +'</td>'
						+'</tr>';
				}
				$("#modalLoanRelation").html(str);			
			},function(){
			});
		}
		
		function getOrderType(orderType){
			if(orderType == "1"){
				return '<span class="right label label-warning">借款</span>';
			}else if(orderType == "2"){
				return '<span class="right label label-success">新增质押</span>';
			}else if(orderType == "3"){
				return '<span class="right label label-primary">续借</span>';
			}else if(orderType == "4"){
				return '<span class="right label label-default">还款</span>';
			}else if(orderType == "5"){
				return '<span class="right label label-default">强平结清</span>';
			}
		}
		
		function getPledgeType(pledgeType){
			if(pledgeType == "1"){
				return '币';
			}else {
				return '--';
			}
		}
		
		function geAmount(amount){
			if(amount > 0){
				return amount;
			}else {
				return '--';
			}
		}
		
		function getPledgeCurrency(currency){
			if(null != currency && "" != currency){
				return currency;
			}else {
				return '--';
			}
		}
	
		function setState(state) {
			document.getElementById("state").value = state;
			document.getElementById("queryForm").submit();
		}
		
		function goNewAjaxUrl(targetUrl,data,Func,Fail){
			$.ajax({
				url:targetUrl,
				data:data,
				type : 'get',
				dataType : "json",
				success: function (res) {
					// var tmp = $.parseJSON(res)
					var tmp = res;
				    if(tmp.code==200){
				    	Func(tmp);
				    }else if(tmp.code==500){
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
