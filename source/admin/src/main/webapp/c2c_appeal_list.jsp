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
			<h3>C2C申诉</h3>
			
			<%@ include file="include/alert.jsp"%>
			
			<!-- //////////////////////////////////////////////////////////////////////////// -->
			<!-- START queryForm -->
			<div class="row">
				<div class="col-md-12">
					<div class="panel panel-default">

						<div class="panel-title">查询条件</div>
						<div class="panel-body">
						
							<form class="form-horizontal" action="<%=basePath%>normal/adminC2cAppealAction!list.action" method="post" id="queryForm">
								
								<input type="hidden" name="pageNo" id="pageNo" value="${pageNo}">
								<input type="hidden" name="status_para" id="status_para" value="${status_para}">
								
								<div class="col-md-12 col-lg-3">
									<fieldset>
										<div class="control-group">
											<div class="controls">
												<input id="order_no_para" name="order_no_para" class="form-control" placeholder="订单号" value="${order_no_para}"/>
											</div>
										</div>
									</fieldset>
								</div>
								
								<div class="col-md-12 col-lg-3">
									<fieldset>
										<div class="control-group">
											<div class="controls">
												<input id="user_code_para" name="user_code_para" class="form-control" placeholder="用户名、用户UID" value="${user_code_para}"/>
											</div>
										</div>
									</fieldset>
								</div>

								<div class="col-md-12 col-lg-3">
									<fieldset>
										<div class="control-group">
											<div class="controls">
												<select id="rolename_para" name="rolename_para" class="form-control" >
													<option value="">所有账号</option>
													<option value="MEMBER" <c:if test="${rolename_para == 'MEMBER'}">selected="true"</c:if> >正式账号</option>
													<option value="GUEST" <c:if test="${rolename_para == 'GUEST'}">selected="true"</c:if> >演示账号</option>
													<option value="TEST" <c:if test="${rolename_para == 'TEST'}">selected="true"</c:if> >试用账号</option>
												</select>
											</div>
										</div>
									</fieldset>
								</div>
								
								<div class="col-md-12 col-lg-3">
									<fieldset>
										<div class="control-group">
											<div class="controls">
												<select id="c2c_user_type_para" name="c2c_user_type_para" class="form-control " >
													<option value="">所有承兑商类型</option>
													<option value="1" <c:if test="${c2c_user_type_para == '1'}">selected="true"</c:if> >后台承兑商</option>
													<option value="2" <c:if test="${c2c_user_type_para == '2'}">selected="true"</c:if> >用户承兑商</option>
												</select>
											</div>
										</div>
									</fieldset>
								</div>
								
								<div class="col-md-12 col-lg-3" style="margin-top: 10px;">
									<fieldset>
										<div class="control-group">
											<div class="controls">
												<input id="c2c_user_code_para" name="c2c_user_code_para" class="form-control" placeholder="承兑商昵称、承兑商UID" value="${c2c_user_code_para}"/>
											</div>
										</div>
									</fieldset>
								</div>
								
								<div class="col-md-12 col-lg-3" style="margin-top: 10px;">
									<fieldset>
										<div class="control-group">
											<div class="controls">
												<input id="c2c_user_party_code_para" name="c2c_user_party_code_para" class="form-control" placeholder="承兑商用户名、承兑商用户UID" value="${c2c_user_party_code_para}"/>
											</div>
										</div>
									</fieldset>
								</div>

								<div class="col-md-12 col-lg-2" style="margin-top: 10px;" >
									<button type="submit" class="btn btn-light btn-block">查询</button>
								</div>
								
								<div class="col-md-12 col-lg-12" style="margin-top: 10px;">
									<div class="mailbox clearfix">
										<div class="mailbox-menu">
											<ul class="menu">
												<li><a href="javascript:setState('')"> 全部</a></li>
												<li><a href="javascript:setState('0')">已提交</a></li>
												<li><a href="javascript:setState('1')">已处理</a></li>
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
										<td>用户名</td>
										<td>用户UID</td>
										<td>账户类型</td>
										<td>承兑商昵称</td>
										<td>承兑商UID</td>
										<td>承兑商用户名</td>
										<td>承兑商用户UID</td>
										<td>关联订单号</td>
										<td>申诉详情</td>
										<td>申诉状态</td>
										<td>创建时间</td>
										<td>更新时间</td>
										<td width="130px"></td>
									</tr>
								</thead>
								
								<tbody>
									<c:forEach items="${page.getElements()}" var="item" varStatus="stat">
										<tr>
											<td>
												<c:choose>
													<c:when test="${item.username!='' && item.username!=null && fn:length(item.username) > 20}">
														<a style="font-size: 10px;" href="#" onClick="getParentsNet('${item.id}')">
															${fn:substring(item.username,0,6)}*****${fn:substring(item.username,fn:length(item.username) - 6, fn:length(item.username))}
														</a>
													</c:when>
													<c:otherwise>
														<a style="font-size: 10px;" href="#" onClick="getParentsNet('${item.id}')">
															${item.username}
														</a>
													</c:otherwise>
												</c:choose>
											</td>
											<td>${item.usercode}</td>
											<td>
												<c:choose>
													<c:when test="${item.rolename=='GUEST'}">
														<span class="right label label-warning">${item.roleNameDesc}</span>
													</c:when>
													<c:when test="${item.rolename=='MEMBER'}">
														<span class="right label label-success">${item.roleNameDesc}</span>
													</c:when>
													<c:when test="${item.rolename=='TEST'}">
														<span class="right label label-default">${item.roleNameDesc}</span>
													</c:when>
													<c:when test="${item.rolename=='AGENT'}">
														<span class="right label label-primary">${item.roleNameDesc}</span>
													</c:when>
													<c:when test="${item.rolename=='AGENTLOW'}">
														<span class="right label label-primary">${item.roleNameDesc}</span>
													</c:when>
													<c:otherwise>
													 	${item.roleNameDesc}
													</c:otherwise>
												</c:choose>
											</td>
											<td>${item.c2c_user_nick_name}</td>
											<td>${item.c2c_user_code}</td>
											<td>
												<c:choose>
													<c:when test="${item.c2c_user_party_name!='' && item.c2c_user_party_name!=null && fn:length(item.c2c_user_party_name) > 20}">
														<a style="font-size: 10px;" href="#" onClick="getParentsNet('${item.c2c_user_party_id}')">
															${fn:substring(item.c2c_user_party_name,0,6)}*****${fn:substring(item.c2c_user_party_name,fn:length(item.c2c_user_party_name) - 6, fn:length(item.c2c_user_party_name))}
														</a>
													</c:when>
													<c:otherwise>
														<a style="font-size: 10px;" href="#" onClick="getParentsNet('${item.c2c_user_party_id}')">
															${item.c2c_user_party_name}
														</a>
													</c:otherwise>
												</c:choose>
											</td>
											<td>${item.c2c_user_party_code}</td>
											<td>${item.order_no}</td>
											<td>
												<a href="#" onClick="appealDetail(
													'${item.reason}',
													'${item.description}',
													'${item.img}',
													'${item.name}',
													'${item.phone}')">
													 点击查看
												</a>
											</td>
											<td>
												<c:if test='${item.state=="0"}'><span class="right label label-danger">已提交</span></c:if>
												<c:if test='${item.state=="1"}'>已处理</c:if>
											</td>
											<td>${item.create_time}</td>
											<td>${item.update_time}</td>
											
											<td>
				
												<c:if test="${item.state != '1'}">
											
													<c:if test="${security.isRolesAccessible('ROLE_ROOT,ROLE_ADMIN')
										 				|| security.isResourceAccessible('OP_C2C_APPEAL_OPERATE')}">
												
														<div class="btn-group">
															<button type="button" class="btn btn-light">操作</button>
															<button type="button" class="btn btn-light dropdown-toggle" data-toggle="dropdown" aria-expanded="false">
																<span class="caret"></span> <span class="sr-only">Toggle Dropdown</span>
															</button>
															<ul class="dropdown-menu" role="menu">
															
																<li> <a href="javascript:handled('${item.order_no}')">已处理</a> </li>
															
															</ul>
														</div>
														
													</c:if>
															
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
	
	<!-- 模态框（Modal） -->
	<div class="modal fade" id="modal_appeal_detail" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
		<div class="modal-dialog">
			<div class="modal-content" style="width: 725px;">
			
				<div class="modal-header">
					<button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
					<h4 class="modal-title">详细信息</h4>
				</div>
				
				<div class="modal-body col-md-12">					
					<div class="form-group">
						<label class="col-sm-2 control-label form-label">申诉理由</label>
						<div class="col-sm-8">
							<input id="modal_reason" class="form-control" readonly="readonly" />
						</div>
					</div>
				</div>
				
				<div class="modal-body col-md-12">
					<div class="form-group">
						<label class="col-sm-2 control-label form-label">申诉描述</label>
						<div class="col-sm-8">
							<textarea id="modal_description" class="form-control input-lg" readonly="readonly" rows="4" cols="20" ></textarea>
						</div>
					</div>
				</div>
				
				<div class="modal-body col-md-12">
					<div class="form-group">
						<label class="col-sm-2 control-label form-label">申诉凭证</label>
						<div class="col-sm-8">
							<a href="#" target="_blank"> 
							 	<img width="200px" height="200px" id="modal_img" src="" />
							</a>
						</div>
					</div>
				</div>
				
				<div class="modal-body col-md-12">					
					<div class="form-group">
						<label class="col-sm-2 control-label form-label">联系人</label>
						<div class="col-sm-8">
							<input id="modal_name" class="form-control" readonly="readonly" />
						</div>
					</div>
				</div>
				
				<div class="modal-body col-md-12">					
					<div class="form-group">
						<label class="col-sm-2 control-label form-label">联系电话</label>
						<div class="col-sm-8">
							<input id="modal_phone" class="form-control" readonly="readonly" />
						</div>
					</div>
				</div>

				<div class="modal-footer" style="margin-top: 0;">
					<button type="button" class="btn " data-dismiss="modal">关闭</button>
				</div>
				
			</div>
		</div>
	</div>

	<div class="form-group">
		<div class="col-sm-1">
			<!-- 模态框（Modal） -->
			<div class="modal fade" id="net_form" tabindex="-1"
				role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
				<div class="modal-dialog">
					<div class="modal-content">
					
						<div class="modal-header">
							<button type="button" class="close"
								data-dismiss="modal" aria-hidden="true">&times;</button>
							<h4 class="modal-title" id="myModalLabel">推荐网络</h4>
						</div>
						
						<div class="modal-body" style="max-height: 400px;overflow-y: scroll;">
						
							<table class="table table-bordered table-striped" >
								<thead>
									<tr>
										<td>层级</td>
										<td>用户名</td>
										<td>UID</td>
										<td>账户类型</td>
										<td>基础认证</td>
									</tr>
								</thead>
								<tbody id="modal_net_table">
									<%@ include file="include/loading.jsp"%>
								</tbody>
							</table>
							
						</div>
					
					</div>
				</div>
			</div>
		</div>
	</div>

	<form action="<%=basePath%>normal/adminC2cAppealAction!handled.action" method="post" id="form_handled">
		<input type="hidden" name="pageNo" id="pageNo" value="${pageNo}">
		<input type="hidden" name="order_no" id="order_no_handled" value="${order_no}">
	</form>

	<%@ include file="include/js.jsp"%>

	<script type="text/javascript">
	
		function setState(state) {
			document.getElementById("status_para").value = state;
			document.getElementById("queryForm").submit();
		}

		function getParentsNet(id) {
			$("#net_form").modal("show");
			var url = "<%=basePath%>normal/adminUserAction!getParentsNet.action";
			var data = {"partyId":id};
			goNewAjaxUrl(url, data, function(tmp) {
				var str='';
				var content='';				
				for (var i = 0; i < tmp.user_parents_net.length; i++) {
					str += '<tr>'
						+'<td>'+(i+1)+'</td>'
						+'<td>'+tmp.user_parents_net[i].username+'</td>'
						+'<td>'+tmp.user_parents_net[i].usercode+'</td>'
						+'<td>'+getRoleDom(tmp.user_parents_net[i].rolename)+'</td>'
						+'<td>'+getKycAuthorityDom(tmp.user_parents_net[i].kyc_authority)+'</td>'
						+'</tr>';
				}
				$("#modal_net_table").html(str);				
			}, function() {
//				$("#coin_value").val(0);
			});
		}
	
		function handled(order_no) {
			$("#order_no_handled").val(order_no);
			swal({
				title : "是否确认已处理?",
				text : "",
				type : "warning",
				showCancelButton : true,
				confirmButtonColor : "#DD6B55",
				confirmButtonText : "确认",
				closeOnConfirm : false
			}, function() {
				document.getElementById("form_handled").submit();
			});
		}

		function appealDetail(reason, description, img, name, phone) {
			$("#modal_reason").val(reason);
			$("#modal_description").val(description);
			document.getElementById('modal_img').src="<%=base%>wap/public/showimg!showImg.action?imagePath=" + img;
			$("#modal_name").val(name);
			$("#modal_phone").val(phone);
			$('#modal_appeal_detail').modal("show");
		}
		
	</script>

</body>

</html>
