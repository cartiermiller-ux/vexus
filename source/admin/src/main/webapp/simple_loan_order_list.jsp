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
	<div class="ifr-dody">
		<div class="ifr-con">
			<h3>借贷订单</h3>
			<%@ include file="include/alert.jsp"%>
			<div class="row">
				<div class="col-md-12">
					<div class="panel panel-default">
						<div class="panel-title">查询条件</div>
						<div class="panel-body">
							<form id="queryForm" class="form-horizontal" action="<%=basePath%>normal/loanadmin!list.action" method="post">
								<input type="hidden" name="pageNo" id="pageNo" value="${pageNo}">
								<input type="hidden" name="status" id="status" value="${status}">
								<div class="col-md-12 col-lg-12">
									<div class="col-md-12 col-lg-3">
										<fieldset>
											<div class="control-group">
												<div class="controls">
													<input id="order_no_para" name="orderNo" class="form-control " placeholder="订单号" value="${orderNo}" />
												</div>
											</div>
										</fieldset>
									</div>
									<div class="col-md-12 col-lg-4">
										<fieldset>
											<div class="control-group">
												<div class="controls">
													<input id="name_para" name="userName" class="form-control" placeholder="用户名" value = "${userName}"/>
												</div>
											</div>
										</fieldset>
									</div>
								</div>
								<div class="col-md-12 col-lg-12">
									<div class="col-md-12 col-lg-2" style="margin-top: 10px;" >
										<button type="submit" class="btn btn-light btn-block">查询</button>
									</div>
								</div>
								<div class="col-md-12 col-lg-12" style="margin-top: 10px;">
									<div class="mailbox clearfix">
										<div class="mailbox-menu">
											<ul class="menu">
												<li><a href="javascript:setState('0')">全部</a></li>
												<li><a href="javascript:setState('1')">未审</a></li>
												<li><a href="javascript:setState('2')">通过</a></li>
												<li><a href="javascript:setState('3')">驳回</a></li>
											</ul>
										</div>
									</div>
								</div>
							</form>
						</div>
					</div>
				</div>
			</div>
			<!-- END queryForm -->
			
			<div class="row">
				<div class="col-md-12">
					<!-- Start Panel -->
					<div class="panel panel-default">
						<div class="panel-title">查询结果</div>
						<c:if test="${security.isRolesAccessible('ROLE_ROOT,ROLE_ADMIN') || security.isResourceAccessible('OP_PLEDGE_GALAXY_ORDER_OPERATE')}">
							<a href="<%=basePath%>normal/loanadmin!toAdd.action" class="btn btn-light" style="margin-bottom: 10px">
								<i class="fa fa-pencil">新增</i>
							</a>
						</c:if>
						<div class="panel-body">
							<table class="table table-bordered table-striped">
								<thead>
									<tr>
										<td>订单号</td>
										<td>用户名</td>
										<td>用户类型</td>
										<td>借贷币种</td>
										<td>借贷额度</td>
										<td>借贷利息</td>
										<td>借贷状态</td>
										<td>借贷天数(天)</td>
										<td>申请时间</td>
										<td>还款周期(天)</td>
										<td>日利率(%)</td>
										<td>还款方式</td>
										<td>驳回原因</td>
										<td>放款机构</td>
										<%-- 
										<td>房屋信息</td>
										<td>收入证明</td>
										--%>
										<td width="130px"></td>
									</tr>
								</thead>
								
								<tbody>								
									<c:forEach items="${page.getElements()}" var="item" varStatus="stat">
										<tr>
											<td>${item.id}</td>
										    <td>${item.userName}</td>
										    <td>
										     <c:choose>
					                            <c:when test="${item.rolename=='GUEST'}">
					                              <span class="right label label-warning">${item.roleNameDesc}</span>
					                            </c:when>
					                            <c:when test="${item.rolename=='TEST'}">
					                              <span class="right label label-default">${item.roleNameDesc}</span>
					                            </c:when>
					                            <c:when test="${item.rolename=='MEMBER'}">
					                              <span class="right label label-success">${item.roleNameDesc}</span>
					                            </c:when>
					                            <c:when test="${item.rolename=='AGENT'}">
					                              <span class="right label label-primary">${item.roleNameDesc}</span>
					                            </c:when>
					                            <c:otherwise>
					                               ${item.roleNameDesc}
					                            </c:otherwise>
					                          </c:choose>
					                        </td>
										    <td>${item.symbol}</td>
										    <td>${item.quota}</td>
										    <td>${item.totalInterest}</td>
											<td>${item.state[1]}</td>
											<td>${item.term}</td>
											<td>${item.createTime}</td>
											<td>${item.repayCycle}</td>
											<td>${item.dailyRate}</td>
											<td>${item.repayment[1]}</td>
											<td>${item.reason}</td>
											<td>${item.lendingInstitution[1]}</td>
											<%-- <td>
												<img alt="房屋图片" height="150" width="150" src="<%=basePath%>public/showimg!showImg.action?imagePath=${item.houseImgs}"/>
											</td>
											<td>
												<img alt="收入证明" height="150" width="150" src="<%=basePath%>public/showimg!showImg.action?imagePath=${item.incomeImg}"/>
											</td> --%>
											<td>
												<c:if test="${security.isRolesAccessible('ROLE_ROOT,ROLE_ADMIN') || security.isResourceAccessible('OP_PLEDGE_GALAXY_ORDER_OPERATE')}">
													<div class="btn-group">
														<button type="button" class="btn btn-light">操作</button>
														<button type="button" class="btn btn-light dropdown-toggle"
															data-toggle="dropdown" aria-expanded="false">
															<span class="caret"></span> <span class="sr-only">Toggle Dropdown</span>
														</button>
														<ul class="dropdown-menu" role="menu">
															<li><a href="<%=basePath%>normal/loanadmin!toModify.action?orderNo=${item.id}">修改</a></li>
															<c:if test="${item.state[0]==1}">
																<li><a href="javascript:changeState('${item.id}','pass')">通过(请到用户管理手动添加余额)</a></li>	
																<li><a href="javascript:changeState('${item.id}','reject')">驳回</a></li>
															</c:if>															
															<c:if test="${item.state[0]==2}">
																<li><a href="javascript:changeState('${item.id}','repay')">还款</a></li>
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
		<%@ include file="include/footer.jsp"%>
	</div>
	<!-- End Content -->
	<%@ include file="include/js.jsp"%>
	<script src="<%=basePath%>js/bootstrap/bootstrap-treeview.js"></script>
	
	<!-- Modal -->
	<div class="modal fade" id="modal_saveFailed" tabindex="-1" role="dialog" aria-hidden="true">
		<div class="modal-dialog">
			<div class="modal-content">
			
				<div class="modal-header">
					<button type="button" class="close" data-dismiss="modal" aria-label="Close">
						<span aria-hidden="true">&times;</span>
					</button>
					<h4 class="modal-title">请输入驳回原因</h4>
				</div>
				
				<div class="modal-body">
					<input name="rejectMsg" id="rejectMsg" class="form-control  input-lg" rows="2" cols="10" placeholder="驳回原因"/>
				</div>
				
				<div class="modal-footer">
					<button type="button" class="btn btn-white" data-dismiss="modal">关闭</button>
					<button type="button" class="btn btn-default" onclick="saveFailed_confirm()">确认驳回</button>
				</div>
				
			</div>
		</div>
	</div>
	
	<form id="changeState" action="<%=basePath%>normal/loanadmin!change.action" method="post">
		<input type="hidden" name="reason" id="reason">
		<input type="hidden" name="orderId" id="orderId">
		<input type="hidden" name="statusStr" id="statusStr">
	</form>
	
	<script type="text/javascript">
		function changeState(orderId,status) {
			$("form#changeState>input#orderId").val(orderId);
			if("pass"==status){
				$("form#changeState>input#statusStr").val('2');
				swal({
					title : "是否确认通过?",
					text : "",
					type : "warning",
					showCancelButton : true,
					confirmButtonColor : "#DD6B55",
					confirmButtonText : "确认",
					closeOnConfirm : false
				}, function() {
					document.getElementById("changeState").submit();
				});
				return;
			}else if("repay"==status){
				$("form#changeState>input#statusStr").val('5');
				swal({
					title : "是否确认已还款?",
					text : "",
					type : "warning",
					showCancelButton : true,
					confirmButtonColor : "#DD6B55",
					confirmButtonText : "确认",
					closeOnConfirm : false
				}, function() {
					document.getElementById("changeState").submit();
				});
				return;
			}
			
			$("form#changeState>input#statusStr").val('3');
			$("div#modal_saveFailed").modal("show");
		}
		
		function saveFailed_confirm() {
			$("form#changeState>input#reason").val($("div#modal_saveFailed input#rejectMsg").val());
			document.getElementById("changeState").submit();
		}
	</script>
	
 	<script>
        $(function () {
            var data = ${result};
            $("#treeview4").treeview({
                color: "#428bca",
                enableLinks:true,
                nodeIcon: "glyphicon glyphicon-user",
                data: data,
                levels: 4,
            });
        });
	</script>
	
	<script type="text/javascript">
		function getallname(name){
			$("#usernallName").html(name);
			$("#user_all_name_a").attr("href","https://etherscan.io/address/"+name);
			$("#net_form").modal("show");
		}
		
		function setState(state) {
			document.getElementById("status").value=state;
    		document.getElementById("queryForm").submit();
		}
		
		function toDelete(id) {
			$("#id_delete").val(id);
			$('#modal_delete').modal("show");
		}
	</script>
</body>
</html>
