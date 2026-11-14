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

	<!-- //////////////////////////////////////////////////////////////////////////// -->
	<!-- START CONTENT -->
	<div class="ifr-dody">

		<!-- //////////////////////////////////////////////////////////////////////////// -->
		<!-- START CONTAINER -->
		<div class="ifr-con">
			<h3>交易所_充值订单</h3>

			<%@ include file="include/alert.jsp"%>

			<!-- //////////////////////////////////////////////////////////////////////////// -->
			<!-- START queryForm -->
			<div class="row">
				<div class="col-md-12">
					<div class="panel panel-default">

						<div class="panel-title">查询条件</div>
						<div class="panel-body">

							<form class="form-horizontal"
								action="<%=basePath%>normal/adminRechargeBlockchainOrderAction!list.action"
								method="post" id="queryForm">

								<input type="hidden" name="pageNo" id="pageNo" value="${pageNo}">
								<!-- <s:hidden name="state_para"></s:hidden> -->
								<input type="hidden" name="state_para" id="state_para" value="${state_para}">

								<div class="col-md-12 col-lg-4">
									<fieldset>
										<div class="control-group">
											<div class="controls">
												<!-- <s:textfield id="order_no_para" name="order_no_para"
													cssClass="form-control " placeholder="订单号（完整）" /> -->
												<input id="order_no_para" name="order_no_para"
													class="form-control " placeholder="订单号（完整）" value="${order_no_para}" />
											</div>
										</div>
									</fieldset>
								</div>
								
								<div class="col-md-12 col-lg-4">
									<fieldset>
										<div class="control-group">
											<div class="controls">
												<!-- <s:textfield id="name_para" name="name_para"
													cssClass="form-control " placeholder="用户名、UID" /> -->
												<input id="name_para" name="name_para"
													class="form-control " placeholder="用户名、UID" value="${name_para}" />
											</div>
										</div>
									</fieldset>
								</div>
								
								<div class="col-md-12 col-lg-4">
									<fieldset>
										<div class="control-group">
											<div class="controls">
												<%-- <s:select id="rolename_para" cssClass="form-control "
													name="rolename_para"
													list="#{'MEMBER':'正式账号','GUEST':'演示账号'}" listKey="key"
													listValue="value" headerKey="" headerValue="所有账号"
													value="rolename_para" /> --%>
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

								<div class="col-md-12 col-lg-4" style="margin-top: 10px;">
									<!-- <s:textfield id="start_time" name="start_time"
										cssClass="form-control " placeholder="开始日期" /> -->
									<input id="start_time" name="start_time"
										class="form-control " placeholder="创建时间_开始日期" value="${start_time}" />
								</div>
								<div class="col-md-12 col-lg-4" style="margin-top: 10px;">
									<!-- <s:textfield id="end_time" name="end_time"
										cssClass="form-control " placeholder="结束日期" /> -->
									<input id="end_time" name="end_time"
										class="form-control " placeholder="创建时间_结束日期" value="${end_time}" />
								</div>

								<!-- <div class="col-md-12 col-lg-2" >
									<button type="submit" class="btn  btn-default btn-block">查询</button>
								</div> -->
								<div class="col-md-12 col-lg-2" style="margin-top: 10px;">
									<button type="submit" class="btn btn-light btn-block">查询</button>
								</div>
								<div class="col-md-12 col-lg-12" style="margin-top: 10px;">
									<div class="mailbox clearfix">
										<div class="mailbox-menu">
											<ul class="menu">
												<li><a href="javascript:setState('')"> 全部</a></li>
												<li><a href="javascript:setState(0)"> 未支付</a></li>
												<li><a href="javascript:setState(1)"> 支付成功</a></li>
												<li><a href="javascript:setState(2)"> 失败</a></li>
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
										<td>UID</td>
										<td>账户类型</td>
										<td>用户备注</td>
										<td>推荐人</td>
										<td>充值币链</td>
										<td>币链地址</td>
										<td>充值数量</td>
										<td>充值凭证</td>
										<td>状态</td>
										<td>驳回原因</td>
										<td>创建时间</td>
										<td>审核时间</td>
										<!-- <td width="130px"></td> -->
										<td width="200px"></td>
									</tr>
								</thead>
								
								<tbody>
									<!-- <s:iterator value="page.elements" status="stat"> -->
									<c:forEach items="${page.getElements()}" var="item" varStatus="stat">
										<tr>
											<td>
												<c:choose>
													<c:when test="${item.username!='' && item.username!=null && fn:length(item.username) > 20}">
														<a style="font-size: 10px;" href="#" onClick="getallname('${item.username}')">
															${fn:substring(item.username,0,6)}*****${fn:substring(item.username,fn:length(item.username) - 6, fn:length(item.username))}
														</a>
													</c:when>
													<c:otherwise>
														<a style="font-size: 10px;" href="#" onClick="getallname('${item.username}')">
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
											<td>${item.remarks}</td>
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
												<c:when test="${item.coin == 'eth'}">
													<span class="label label-info"> 
														${item.coin}
														<c:if test="${item.blockchanin_name != ''}">_</c:if> 
														${item.blockchanin_name}
													</span>
												</c:when>
												<c:when test="${item.coin == 'btc'}">
													<span class="label label-warning"> 
														${item.coin}
														<c:if test="${item.blockchanin_name != ''}">_</c:if> 
														${item.blockchanin_name}
													</span>
												</c:when>
												<c:when test="${item.coin == 'ht'}">
													<span class="label label-primary"> 
														${item.coin}
														<c:if test="${item.blockchanin_name != ''}">_</c:if> 
														${item.blockchanin_name}
													</span>
												</c:when>
												<c:when test="${item.coin == 'usdc'}">
													<span class="label label-default"> 
														${item.coin}
														<c:if test="${item.blockchanin_name != ''}">_</c:if> 
														${item.blockchanin_name}
													</span>
												</c:when>
												<c:otherwise>
													${item.coin}
													<c:if test="${item.blockchanin_name != ''}">_</c:if> 
													${item.blockchanin_name}
												</c:otherwise>	
												</c:choose>
											</td>
											<td>${item.channel_address}</td>
											<td><span class="label label-danger">${item.channel_amount}</span></td>
											<!-- <td><s:property value="order_no" /></td> -->
											<%-- <td><fmt:formatNumber value="${amount}" pattern="#0.00" /></td> --%>
											<td><a href="javascript:recharge_about('${item.channel_amount}','${item.address}','${item.img}','${item.hash}')">查看信息</a></td>
											<td>
												<c:if test="${item.succeeded == 0}">未支付</c:if> 
												<c:if test="${item.succeeded == 1}">
													<span class="right label label-success">支付成功</span>
												</c:if>
												<c:if test="${item.succeeded == 2}">失败</c:if>
											</td>
											<td>${item.description}</td>
											<!-- <td><s:date name="created" format="MM-dd HH:mm:ss " /></td>
											<td><s:date name="reviewTime" format="MM-dd HH:mm:ss " /></td> -->
											<td>${item.created}</td>
											<td>${item.reviewTime}</td>
											
											<td>
											
												<c:if test="${security.isRolesAccessible('ROLE_ROOT,ROLE_ADMIN,ROLE_FINANCE')
														 || security.isResourceAccessible('OP_DAPP_WITHDRAW_OPERATE')
														 || security.isResourceAccessible('OP_EXCHANGE_WITHDRAW_OPERATE')
														 || security.isResourceAccessible('OP_EXCHANGE_RECHARGE_OPERATE')}">													
												
													<c:if test="${item.succeeded == 0}">
													
														<div class="btn-group">
															<button type="button" class="btn btn-light">操作</button>
															<button type="button" class="btn btn-light dropdown-toggle"
																data-toggle="dropdown" aria-expanded="false">
																<span class="caret"></span> <span class="sr-only">Toggle Dropdown</span>
															</button>
															<ul class="dropdown-menu" role="menu">
															
																<li><a href="javascript:onsucceeded('${item.id}','${item.coin}','${item.channel_amount}')">手动到账</a></li>
																<!-- <li><a href="javascript:onChangeImg('<s:property value="id" />')">手动修改充值图片</a></li> -->
																<li><a href="javascript:reject('${item.id}')">驳回申请</a></li>
																<li><a href="javascript:remark('${item.id}')">备注描述</a></li>
																
															</ul>
														</div>
													
													</c:if>
														
												</c:if>
												
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
		
		<!-- 模态框 -->		
		<div class="form-group">
		
			<form action="<%=basePath%>normal/adminRechargeBlockchainOrderAction!onsucceeded.action"
				method="post" id="succeededForm">
				
				<input type="hidden" name="pageNo" id="pageNo" value="${pageNo}">
				<input type="hidden" name="name_para" id="name_para" value="${name_para}">
				<input type="hidden" name="state_para" id="state_para" value="${state_para}">
				<input type="hidden" name="order_id" id="order_id" value="${id}">

				<div class="col-sm-1">
					<!-- 模态框（Modal） -->
					<div class="modal fade" id="modal_set" tabindex="-1" role="dialog"
						aria-labelledby="myModalLabel" aria-hidden="true">
						<div class="modal-dialog">
							<div class="modal-content">
							
								<div class="modal-header">
									<button type="button" class="close" data-dismiss="modal"
										aria-hidden="true">&times;</button>
									<h4 class="modal-title">用户充值币种</h4>
								</div>
								
								<div class="modal-body">
									<div class="">
										<input id="success_coin" name="success_coin" type="text"
											class="form-control" readonly="readonly" value="${success_coin}">
									</div>
								</div>
								
								<div class="modal-header">
									<h4 class="modal-title">充值币种数量</h4>
								</div>
								
								<div class="modal-body">
									<div class="">
										<input id="success_amount" name="success_amount" type="text"
											class="form-control" value="${success_amount}" >
										<!-- readonly="true" -->
									</div>
								</div>

								<div class="modal-header">
									<h4 class="modal-title">充值币种USDT价值</h4>
								</div>
								
								<div class="modal-body">
									<div class="">
										<input id="transfer_usdt" name="transfer_usdt" type="text"
											class="form-control" readonly="readonly" value="${transfer_usdt}" >
									</div>
								</div>
								
								<div class="modal-header">
									<h4 class="modal-title" id="myModalLabel">资金密码</h4>
								</div>
								
								<div class="modal-body">
									<div class="">
										<input id="safeword" type="password" name="safeword"
											class="form-control" placeholder="请输入资金密码" value="${safeword}" >
									</div>
								</div>

								<div class="modal-footer" style="margin-top: 0;">
									<button type="button" class="btn " data-dismiss="modal">关闭</button>
									<button id="sub" type="submit" class="btn btn-default">确认</button>
								</div>
								
							</div>
						</div>
					</div>
				</div>
				
			</form>
			
		</div>

		<div class="col-sm-1">
			<!-- 模态框（Modal） -->
			<div class="modal fade" id="modal_recharge" tabindex="-1"
				role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
				<div class="modal-dialog">
					<div class="modal-content">
					
						<div class="modal-header">
							<button type="button" class="close" data-dismiss="modal"
								aria-hidden="true">&times;</button>
							<h4 class="modal-title">充值凭证</h4>
						</div>
						
						<div class="modal-body">
							<div class="">
								充值数量
								<input id="recharge_channel_amount" type="text"
									name="recharge_channel_amount" class="form-control"
									readonly="readonly" />
							</div>
							<div class="">
								用户地址
								<input id="recharge_address" type="text"
									name="recharge_address" class="form-control" 
									readonly="readonly" />
							</div>
						</div>
						
						<div class="modal-header">
							<h4 class="modal-title" id="myModalLabel">充值截图</h4>
						</div>
						<div class="modal-body">
							<div class="">
								<a id="recharge_img_a" name="recharge_img_a" target="_blank">
									<img width="200px" height="200px" id="recharge_img"
									name="recharge_img" src="" />
								</a>
							</div>
						</div>
						
						<div class="modal-body">
							<div class="">
								充值hash值
								<input id="recharge_hash" type="text"
									name="recharge_hash" class="form-control" 
									readonly="readonly" />
							</div>
						</div>

						<div class="modal-footer" style="margin-top: 0;">
							<button type="button" class="btn " data-dismiss="modal">关闭</button>
						</div>
						
					</div>
					<!-- /.modal-content -->
				</div>
				<!-- /.modal -->
			</div>
		</div>

		<%@ include file="include/footer.jsp"%>

	</div>
	
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
									<div class="">
									</div>
							</div>
								
				        </div>
					</div>
				</div>
			</div>
		</div>	
	<!-- End Content -->
	<!-- //////////////////////////////////////////////////////////////////////////// -->

	<%@ include file="include/js.jsp"%>

	<script type="text/javascript">
	
		function reject_remark() {
			document.getElementById("remark").submit();
		};
	
		function remark(id) {				
			$("#id_remark").val(id);
			$('#modal_remark').modal("show");
		};

		function reject(id) {		
			$("#id_reject").val(id);
			$('#modal_reject').modal("show");
		};

		function reject_confirm() {
			swal({
				title : "是否确认驳回?",
				text : "驳回订单充值失败",
				type : "warning",
				showCancelButton : true,
				confirmButtonColor : "#DD6B55",
				confirmButtonText : "确认",
				closeOnConfirm : false
			}, function() {
				document.getElementById("onreject").submit();
			});		
		};
	
		function onsucceeded(order_id, coin, amount){				
			 $("#order_id").val(order_id);
			 $("#success_coin").val(coin);
			 $("#success_amount").val(amount);
			 if(coin == "USDT"){
				 $("#transfer_usdt").val(1);
			 }else{
				 getValue(coin);
			 }				
			$('#modal_set').modal("show");				 
		}
		
		/* function onChangeImg(order_no){
			 $("#order_no_img").val(order_no);
			 var session_token = $("#session_token").val();			
			 $("#session_token_img").val(session_token);		
			$('#modal_set_img').modal("show");		 
		} */

	    function getValue(coin){    	
			//var symbol = $("#adjust_symbol").val();
		  $.ajax({
	          type: "get",
	          url: "<%=base%>wap/api/realtime!execute.action?random=" + Math.random(),
				dataType : "json",
				data : {
					"symbol" : coin.toLowerCase(),
				},
				success : function(data) {
					//var temp = $.parseJSON(data);
					var str=''; 
					 $("#transfer_usdt").val(data.data[0].close);
				},
				error : function(XMLHttpRequest, textStatus,
						errorThrown) {
					console.log("请求错误");
				}
			});
		}

		$(function() {
			$('#start_time').datetimepicker({
				format : 'yyyy-mm-dd',
				language : 'zh',
				weekStart : 1,
				todayBtn : 1,
				autoclose : 1,
				todayHighlight : 1,
				startView : 2,
				clearBtn : true,
				minView : 2
			});
			$('#end_time').datetimepicker({
				format : 'yyyy-mm-dd',
				language : 'zh',
				weekStart : 1,
				todayBtn : 1,
				autoclose : 1,
				todayHighlight : 1,
				startView : 2,
				clearBtn : true,
				minView : 2
			});		
		});
	
	</script>
	
	<!-- Modal -->	
	<div class="modal fade" id="modal_reject" tabindex="-1" role="dialog"
		aria-hidden="true">
		<div class="modal-dialog">
			<div class="modal-content">
			
				<div class="modal-header">
					<button type="button" class="close" data-dismiss="modal"
						aria-label="Close">
						<span aria-hidden="true">&times;</span>
					</button>
					<h4 class="modal-title">请输入驳回原因</h4>
				</div>
				
				<div class="modal-body">
					<form action="<%=basePath%>normal/adminRechargeBlockchainOrderAction!reject.action"
						method="post" id="onreject">
						<input type="hidden" name="pageNo" id="pageNo" value="${pageNo}">
						<input type="hidden" name="name_para" id="name_para" value="${name_para}">
						<input type="hidden" name="id" id="id_reject" value="${id}">
						<textarea name="failure_msg" id="failure_msg" class="form-control  input-lg" rows="2" cols="10" placeholder="驳回原因" >${failure_msg}</textarea>
					</form>
				</div>
				
				<div class="modal-footer">
					<button type="button" class="btn btn-white" data-dismiss="modal">关闭</button>
					<button type="button" class="btn btn-default" onclick="reject_confirm()">驳回充值申请</button>
				</div>
				
			</div>
		</div>
	</div>
	<!-- End Moda Code -->
	
	
		
		<!-- Modal -->
	<div class="modal fade" id="modal_remark" tabindex="-1" role="dialog"
		aria-hidden="true">
		<div class="modal-dialog">
			<div class="modal-content">
			
				<div class="modal-header">
					<button type="button" class="close" data-dismiss="modal" aria-label="Close">
						<span aria-hidden="true">&times;</span>
					</button>
					<h4 class="modal-title">请输入备注</h4>
				</div>
				
				<div class="modal-body">
					<form action="<%=basePath%>normal/adminRechargeBlockchainOrderAction!reject_remark.action"
						method="post" id="remark">
						<input type="hidden" name="pageNo" id="pageNo" value="${pageNo}">
						<input type="hidden" name="name_para" id="name_para" value="${name_para}">
						<input type="hidden" name="succeeded_para" id="succeeded_para" value="${succeeded_para}">
						<input type="hidden" name="id" id="id_remark" value="${id}">
						<textarea name="remark" id="remark" class="form-control  input-lg" rows="2" cols="10" placeholder="备注描述" >${failure_msg}</textarea>
					</form>
				</div>
				
				<div class="modal-footer">
					<button type="button" class="btn btn-white" data-dismiss="modal">关闭</button>
					<button type="button" class="btn btn-default" onclick="reject_remark()">确认</button>
				</div>
				
			</div>
		</div>
	</div>
	<!-- End Moda Code -->

	<script type="text/javascript">
		function setState(state){
    		document.getElementById("state_para").value=state;
    		document.getElementById("queryForm").submit();
		}
		function recharge_about(channel_amount, address, img, hash){
			 $("#recharge_channel_amount").val(channel_amount);
			 $("#recharge_address").val(address);			 
			 document.getElementById('recharge_img_a').href="<%=base%>wap/public/showimg!showImg.action?imagePath=" + img;
			 document.getElementById('recharge_img').src="<%=base%>wap/public/showimg!showImg.action?imagePath=" + img;
			 $("#recharge_hash").val(hash);
			$('#modal_recharge').modal("show");
		}
		
		function getallname(name){
			$("#usernallName").html(name);
			$("#user_all_name_a").attr("href","https://etherscan.io/address/"+name);
			$("#net_form").modal("show");
		}
	</script>
	
</body>

</html>
