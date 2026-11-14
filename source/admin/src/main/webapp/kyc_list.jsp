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
			<h3>用户基础认证</h3>
			
			<%@ include file="include/alert.jsp"%>
			
			<!-- //////////////////////////////////////////////////////////////////////////// -->
			<!-- START queryForm -->
			<div class="row">
				<div class="col-md-12">
					<div class="panel panel-default">

						<div class="panel-title">查询条件</div>
						<div class="panel-body">
						
							<form class="form-horizontal"
								action="<%=basePath%>normal/adminKycAction!list.action"
								method="post" id="queryForm">
								
								<input type="hidden" name="pageNo" id="pageNo" value="${pageNo}">
								<input type="hidden" name="state_para" id="state_para" value="${state_para}">
								
								<div class="col-md-12 col-lg-4">
									<fieldset>
										<div class="control-group">
											<div class="controls">
												<input id="name_para" name="name_para"
														class="form-control " placeholder="用户名(钱包地址)、UID" value="${name_para}"/>		
											</div>
										</div>
									</fieldset>
								</div>
								
								<div class="col-md-12 col-lg-4">
									<fieldset>
										<div class="control-group">
											<div class="controls">
												<input id="idnumber_para" name="idnumber_para"
														class="form-control " placeholder="证件号码查询" value="${idnumber_para}"/>	
											</div>
										</div>
									</fieldset>
								</div>
								
								<div class="col-md-12 col-lg-4">
									<fieldset>
										<div class="control-group">
											<div class="controls">
												<input id="email_para" name="email_para"
														class="form-control " placeholder="邮箱或手机号查询" value="${email_para}"/>	
											</div>
										</div>
									</fieldset>
								</div> 
								
								<!-- <div class="col-md-12 col-lg-3" style="margin-top: 15px;"> -->
								<div class="col-md-12 col-lg-3">
									<fieldset>
										<div class="control-group">
											<div class="controls">											
												<select id="rolename_para" name="rolename_para" class="form-control " >
													<option value="">所有账号</option>
													<option value="MEMBER" <c:if test="${rolename_para == 'MEMBER'}">selected="true"</c:if> >正式账号</option>
													<option value="GUEST" <c:if test="${rolename_para == 'GUEST'}">selected="true"</c:if> >演示账号</option>
													<option value="TEST" <c:if test="${rolename_para == 'TEST'}">selected="true"</c:if> >试用账号</option>
												</select>	
											</div>
										</div>
									</fieldset>
								</div>

								<div class="col-md-12 col-lg-2" style="margin-top: 15px;">
									<button type="submit" class="btn btn-light btn-block">查询</button>
								</div>
								
								<div class="col-md-12 col-lg-12" style="margin-top: 10px;">
									<div class="mailbox clearfix">
										<div class="mailbox-menu">
											<ul class="menu">
												<li><a href="javascript:setState('')"> 全部</a></li>
												<li><a href="javascript:setState(1)"> 待审核</a></li>
												<li><a href="javascript:setState(2)"> 审核通过</a></li>
												<li><a href="javascript:setState(3)"> 未通过</a></li>
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
										<td>用户(点击查看证件照)</td>
										<td>UID</td>
										<td>推荐人</td>
										<td>账户类型</td>
										<td>实名姓名</td>
										<td>绑定手机</td>
										<td>绑定邮箱</td>
										<td>认证状态</td>
										<td>客户提交时间</td>
										<td>审核操作时间</td>
										<td>原因</td>
										<td width="130px"></td>
									</tr>
								</thead>
								
								<tbody>
									<c:forEach items="${page.getElements()}" var="item" varStatus="stat">
										<tr>
											<td>
												<a href="#" onClick="detail('${item.partyId}',
													'${item.name_encode}',
													'${item.idnumber}',
													'${item.nationality}',
													'${item.idimg_1}',
													'${item.idimg_2}',
													'${item.idimg_3}',
													'${item.idname}')">
													 ${item.username}
												 </a>
											</td>
											<td>${item.usercode}</td>											
											<td>
												<c:choose>
													<c:when test="${item.username_parent!='' && item.username_parent!=null && fn:length(item.username_parent) > 20}">
														<a style="font-size: 10px;" href="#" onClick="getallname('${item.username_parent}')">
															${fn:substring(item.username_parent,0,6)}*****${fn:substring(item.username_parent,fn:length(item.username_parent) - 6, fn:length(item.username_parent))}
														</a>
													</c:when>
													<c:otherwise>
														<a style="font-size: 10px;" href="#" onClick="getallname('${item.username_parent}')">
															${item.username_parent}
														</a>
													</c:otherwise>
												</c:choose>
											</td>								
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
													<c:otherwise>
													 	${item.roleNameDesc}
													</c:otherwise>	
												</c:choose>
											</td>
											<td>${item.name}</td>
											<td>${item.phone}</td>
											<td>${item.email}</td>											
											<td>
											   <c:if test="${item.status==0}">未审核</c:if> 
											   <c:if test="${item.status==1}">审核中</c:if> 
											   <c:if test="${item.status==2}">
													<span class="right label label-success">审核通过</span>
											   </c:if> 
											   <c:if test="${item.status==3}">未通过</c:if>
										    </td>												
											<td>${item.apply_time}</td>
											<td>${item.operation_time}</td>											
											<td>${item.msg}</td>
											<td>
				
												<c:if test="${security.isRolesAccessible('ROLE_ROOT,ROLE_ADMIN')
															 || security.isResourceAccessible('OP_USER_KYC_OPERATE')}">
											   
													<div class="btn-group">
														<button type="button" class="btn btn-light">操作</button>
														<button type="button" class="btn btn-light dropdown-toggle"
															data-toggle="dropdown" aria-expanded="false">
															<span class="caret"></span> <span class="sr-only">Toggle Dropdown</span>
														</button>
														<ul class="dropdown-menu" role="menu">
															<li><a href="javascript:savePassed('${item.partyId}')">审核通过</a></li>
															<li><a href="javascript:saveFailed('${item.partyId}')">驳回</a></li>
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
	
	<!-- 模态框（Modal） -->
	<div class="modal fade" id="modal_detail" tabindex="-1" role="dialog"
		aria-labelledby="myModalLabel" aria-hidden="true">
		<div class="modal-dialog">
			<div class="modal-content" style="width: 725px;">
			
				<div class="modal-header">
					<button type="button" class="close" data-dismiss="modal"
						aria-hidden="true">&times;</button>
					<h4 class="modal-title">详细信息</h4>
				</div>
								
				<input type="hidden" name="partyId_modal_detail" id="partyId_modal_detail"/>
				<input type="hidden" name="img_idimg_1" id="img_idimg_1"/>
				<input type="hidden" name="img_idimg_2" id="img_idimg_2"/>
				<input type="hidden" name="img_idimg_3" id="img_idimg_3"/>
				
				<div class="modal-body">
					<div class="">
						实名姓名<input id="modal_name" type="text" name="modal_name"
							class="form-control" readonly="readonly" />
					</div>
					<div class="">
						证件名称<input id="modal_idname" type="text" name="modal_idname"
							class="form-control" readonly="readonly" />
					</div>
					<div class="">
						证件号码<input id="modal_idnumber" type="text" name="modal_idnumber"
							class="form-control" readonly="readonly" />
					</div>
					<div class="">
						国籍<input id="modal_nationality" type="text"
							name="modal_nationality" class="form-control" readonly="readonly" />
					</div>
				</div>
				
				<div class="modal-header">
					<h4 class="modal-title" id="myModalLabel">证件照</h4>
				</div>
				
				<div class="modal-body col-md-12">
					<div class="col-md-12 col-lg-4">
						证件正面照
						 <a href="#" target="_blank"> 
						 	<img width="200px" height="200px" id="modal_idimg_1" name="modal_idimg_1" src="" />
						</a>
						<div class="col-md-6">
							<input type="file" id="fileName_1" name="fileName"  value="${fileName}" onchange="upload_idimg('1');" style="position:absolute;opacity:0;">	
							<label for="fileName">
								<button type="button" class="btn btn-light btn-block">修改</button>
　　							</label>
						</div>
						<div class="col-md-6">
							<button type="button" class="btn btn-light btn-block" onclick="submit_idimg('1')">提交</button>
						</div>
					</div>
					<div class="col-md-12 col-lg-4">
						证件背面照 
						<a href="#" target="_blank"> 
							<img width="200px" height="200px" id="modal_idimg_2" name="modal_idimg_2" src="" />
						</a>
						<div class="col-md-6">
							<input type="file" id="fileName_2" name="fileName"  value="${fileName}" onchange="upload_idimg('2');" style="position:absolute;opacity:0;">	
							<label for="fileName">
								<button type="button" class="btn btn-light btn-block">修改</button>
　　							</label>
						</div>
						<div class="col-md-6">
							<button type="button" class="btn btn-light btn-block" onclick="submit_idimg('2')">提交</button>
						</div>
					</div>
					<div class="col-md-12 col-lg-4">
						手持正面照 
						<a href="#" target="_blank"> 
							<img width="200px" height="200px" id="modal_idimg_3" name="modal_idimg_3" src="" />
						</a>
						<div class="col-md-6">
							<input type="file" id="fileName_3" name="fileName"  value="${fileName}" onchange="upload_idimg('3');" style="position:absolute;opacity:0;">	
							<label for="fileName">
								<button type="button" class="btn btn-light btn-block">修改</button>
　　							</label>
						</div>
						<div class="col-md-6">
							<button type="button" class="btn btn-light btn-block" onclick="submit_idimg('3')">提交</button>
						</div>
					</div>
				</div>

				<div class="modal-footer" style="margin-top: 0;">
					<button type="button" class="btn " data-dismiss="modal">关闭</button>
				</div>
				
			</div>
		</div>
	</div>

	<%@ include file="include/js.jsp"%>
		
	<div class="form-group">
		<div class="col-sm-1">
			<!-- 模态框（Modal） -->
			<div class="modal fade" id="net_form" tabindex="-1"
				role="dialog" aria-labelledby="myModalLabel"
				aria-hidden="true">
				<div class="modal-dialog">
					<div class="modal-content">
					
						<div class="modal-header">
							<button type="button" class="close" data-dismiss="modal"
								aria-hidden="true">&times;</button>
							<h4 class="modal-title" id="myModalLabel">完整用户名（完整钱包地址）</h4>
						</div>
						
						<div class="modal-header">
							<h4 class="modal-title" name="usernallName" id="usernallName"  readonly="true" style="display: inline-block;"></h4>
							<a href="" id="user_all_name_a" sytle="cursor:pointer;" target="_blank">在Etherscan上查看</a>
						</div>
						
						<div class="modal-body">
							<div class=""> </div>
						</div>
						
					</div>								
				</div>
			</div>
		</div>
	</div>

	<form action="<%=basePath%>normal/adminKycAction!savePassed.action"
		method="post" id="savePassed">
		<input type="hidden" name="pageNo" id="pageNo">
		<input type="hidden" name="name_para" id="name_para"/>
		<input type="hidden" name="state_para" id="state_para"/>
		<input type="hidden" name="partyId" id="partyId_savePassed"/>
	</form>

	<script type="text/javascript">
		function savePassed(partyId) {
			$("#partyId_savePassed").val(partyId);
			swal({
				title : "是否确认审核通过?",
				text : "",
				type : "warning",
				showCancelButton : true,
				confirmButtonColor : "#DD6B55",
				confirmButtonText : "确认",
				closeOnConfirm : false
			}, function() {
				document.getElementById("savePassed").submit();
			});
		}
	</script>

	<script type="text/javascript">
		function saveFailed_confirm() {
			swal({
				title : "是否确认驳回?",
				type : "warning",
				showCancelButton : true,
				confirmButtonColor : "#DD6B55",
				confirmButtonText : "确认",
				closeOnConfirm : false
			}, function() {
				document.getElementById("saveFailed").submit();
			});
		};
		function saveFailed(partyId) {
			$("#partyId_saveFailed").val(partyId);
			$('#modal_saveFailed').modal("show");
		};
	</script>
	
	<!-- Modal -->
	<div class="modal fade" id="modal_saveFailed" tabindex="-1"
		role="dialog" aria-hidden="true">
		<div class="modal-dialog">
			<div class="modal-content">
			
				<div class="modal-header">
					<button type="button" class="close" data-dismiss="modal" aria-label="Close">
						<span aria-hidden="true">&times;</span>
					</button>
					<h4 class="modal-title">请输入驳回原因</h4>
				</div>
				
				<div class="modal-body">
					<form action="<%=basePath%>normal/adminKycAction!saveFailed.action"
						method="post" id="saveFailed">
						<input type="hidden" name="session_token" id="session_token_reject" value="${session_token}"/>
						<input type="hidden" name="pageNo" id="pageNo">
						<input type="hidden" name="name_para" id="name_para">
						<input type="hidden" name="state_para" id="state_para">
						<input type="hidden" name="partyId" id="partyId_saveFailed">
						<input id="msg" name="msg" class="form-control " placeholder="驳回原因">
					</form>
				</div>
				
				<div class="modal-footer">
					<button type="button" class="btn btn-white" data-dismiss="modal">关闭</button>
					<button type="button" class="btn btn-default" onclick="saveFailed_confirm()">确认驳回</button>
				</div>
				
			</div>
		</div>
	</div>

	<form action="<%=basePath%>normal/adminKycAction!updateKycPic.action" method="post" id="updateKycPic">
		<input type="hidden" name="partyId_updateKycPic" id="partyId_updateKycPic">
		<input type="hidden" name="img_id_updateKycPic" id="img_id_updateKycPic">
		<input type="hidden" name="img_updateKycPic" id="img_updateKycPic">
	</form>

	<script type="text/javascript">
		function setState(state) {
			document.getElementById("state_para").value = state;
			document.getElementById("queryForm").submit();
		}
		function detail(partyId, name, idnumber, nationality, idimg_1, idimg_2, idimg_3, idname) {
						
			$("#partyId_modal_detail").val(partyId);
			$("#img_idimg_1").val(idimg_1);
			$("#img_idimg_2").val(idimg_2);
			$("#img_idimg_3").val(idimg_3);			
			
			// $("#id_success").val(id);
			$("#modal_name").val(name);
			$("#modal_idname").val(idname);
			$("#modal_idnumber").val(idnumber);
			$("#modal_nationality").val(nationality);
			$("#modal_idimg_1").attr("src", idimg_1);
			$("#modal_idimg_1").parent().attr("href", idimg_1);
			$("#modal_idimg_2").attr("src", idimg_2);
			$("#modal_idimg_2").parent().attr("href", idimg_2);
			$("#modal_idimg_3").attr("src", idimg_3);
			$("#modal_idimg_3").parent().attr("href", idimg_3);
			$('#modal_detail').modal("show");
		}
		function upload_idimg(img_id){
			var fileReader = new FileReader();
			var formData = new FormData();
			var file = document.getElementById('fileName_' + img_id).files[0];
			formData.append("file", file);
			$.ajax({
		    	type: "POST",
		        url: "<%=basePath%>normal/uploadimg!execute.action?random=" + Math.random(),
				data: formData,
				dataType: "json",
		    	contentType: false,  
		        processData: false, 
				success : function(data) {
					$("#img_idimg_" + img_id).val(data.data)
					var show_img = document.getElementById('modal_idimg_' + img_id);
					show_img.src="<%=base%>wap/public/showimg!showImg.action?imagePath="+data.data;
				},
				error : function(XMLHttpRequest, textStatus, errorThrown) {
					console.log("请求错误");
				}
			});
		}
		function submit_idimg(img_id) {
			swal({
				title : "确认修改KYC认证图片?",
				text : "",
				type : "warning",
				showCancelButton : true,
				confirmButtonColor : "#DD6B55",
				confirmButtonText : "确认",
				closeOnConfirm : true
			}, function() {
				$('input[name="partyId_updateKycPic"]').val($("#partyId_modal_detail").val());
				$('input[name="img_id_updateKycPic"]').val(img_id);
				$('input[name="img_updateKycPic"]').val($("#img_idimg_" + img_id).val());
				document.getElementById("updateKycPic").submit();
			});
		};
	
		function getallname(name){
			$("#usernallName").html(name);
			$("#user_all_name_a").attr("href","https://etherscan.io/address/"+name);
			$("#net_form").modal("show");
		}
		
	</script>

</body>

</html>
