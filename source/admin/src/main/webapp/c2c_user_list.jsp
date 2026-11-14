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
			<h3>C2C承兑商管理</h3>
			
			<%@ include file="include/alert.jsp"%>
			
			<!-- //////////////////////////////////////////////////////////////////////////// -->
			<!-- START queryForm -->
			<div class="row">
				<div class="col-md-12">
					<div class="panel panel-default">

						<div class="panel-title">查询条件</div>
						<div class="panel-body">

							<form class="form-horizontal" action="<%=basePath%>normal/adminC2cUserAction!list.action" method="post" id="queryForm">								
								
								<input type="hidden" name="pageNo" id="pageNo" value="${pageNo}">
								
								<div class="col-md-12 col-lg-4">
									<fieldset>
										<div class="control-group">
											<div class="controls">
											<input id="c2c_user_id_para" name="c2c_user_id_para"
												class="form-control " placeholder="承兑商昵称、承兑商UID" value="${c2c_user_id_para}"/>
											</div>
										</div>
									</fieldset>
								</div>
								
								<div class="col-md-12 col-lg-4">
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
								
								<div class="col-md-12 col-lg-4">
									<fieldset>
										<div class="control-group">
											<div class="controls">
											<input id="c2c_user_party_id_para" name="c2c_user_party_id_para"
												class="form-control " placeholder="承兑商用户名、用户UID" value="${c2c_user_party_id_para}"/>
											</div>
										</div>
									</fieldset>
								</div>
								
								<c:if test="${security.isRolesAccessible('ROLE_ROOT,ROLE_ADMIN')}">
									<div class="col-md-12 col-lg-4" style="margin-top:15px;">
										<fieldset>
											<div class="control-group">
												<div class="controls">
												<input id="c2c_manager_name_para" name="c2c_manager_name_para"
													class="form-control " placeholder="C2C管理员用户名" value="${c2c_manager_name_para}"/>
												</div>
											</div>
										</fieldset>
									</div>
								</c:if>
								
								<div class="col-md-12 col-lg-2" style="margin-top:15px;">
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

						<c:if test="${security.isRolesAccessible('ROLE_ROOT,ROLE_ADMIN')
									 || security.isResourceAccessible('OP_C2C_USER_OPERATE')}">
							
							<a href="<%=basePath%>/normal/adminC2cUserAction!toAdd.action" class="btn btn-light" style="margin-bottom: 10px">
								<i class="fa fa-pencil"></i>新增承兑商</a>
								
						</c:if>
						
						<div class="panel-body">
						
							<table class="table table-bordered table-striped">
							
								<thead>
									<tr>
									
										<c:if test="${security.isRolesAccessible('ROLE_ROOT,ROLE_ADMIN')}">
											<td>管理员</td>
										</c:if>
										
										<td>昵称</td>
										<td>头像</td>
										<td>UID</td>
										<td>类型</td>
										<td>用户名</td>
										<td>用户UID</td>
										<!-- <td>保证金<br />/剩余<br />/赠送比率</td> -->
										<td>保证金<br />/剩余</td>
										<td>买交易量</td>
										<td>卖交易量</td>
										<td>总交易量</td>
										<td>交易人数</td>
										<td>买成单数</td>
										<td>卖成单数</td>
										<td>总成单数</td>
										<td>创建时间</td>
										<td>更新时间</td>
										<td width="50px">备注</td>
										
										<c:if test="${security.isRolesAccessible('ROLE_ROOT,ROLE_ADMIN')
									 				|| security.isResourceAccessible('OP_C2C_USER_OPERATE')}">
									 				
											<td width="130px"></td>
											
										</c:if>
										
									</tr>
								</thead>
								
								<tbody>
								
									<c:forEach items="${page.getElements()}" var="item" varStatus="stat">
										<tr>
										
											<c:if test="${security.isRolesAccessible('ROLE_ROOT,ROLE_ADMIN')}">
												<td>
													<a style="font-size: 10px;" href="#" onClick="getC2cManagerInfo('${item.manager_id}', '${item.manager_name}', '${item.id}')">
														${item.manager_name}
													</a>
												</td>
											</c:if>
											
											<td>${item.nick_name}</td>
											<td>
												<img width="40px" height="40px" src="<%=base%>wap/public/showimg!showImg.action?imagePath=${item.head_img}" />
											</td>
											<td>${item.c2c_user_code}</td>											
											<td>
												<c:if test="${item.c2c_user_type=='2'}">
													<span class="right label label-success">
														用户
													</span>
												</c:if>
												<c:if test="${item.c2c_user_type=='1'}">
													<span class="right label label-danger">
														后台
													</span>
												</c:if>										
											</td>
											<td>${item.user_name}</td>
											<td>${item.user_code}</td>
											<%-- <td><fmt:formatNumber value="${item.deposit_open}" pattern="#0.00" />
												<br />/<fmt:formatNumber value="${item.deposit}" pattern="#0.00" />
												<br />/<fmt:formatNumber value="${item.deposit_gift_rate}" pattern="#0.00" />%</td> --%>
											<td><fmt:formatNumber value="${item.deposit_open}" pattern="#0.00" />
												<br />/<fmt:formatNumber value="${item.deposit}" pattern="#0.00" /></td>
											<td>${item.buy_amount}</td>
											<td>${item.sell_amount}</td>
											<td>${item.total_amount}</td>
											<td>${item.exchange_users}</td>
											<td>${item.buy_success_orders}</td>
											<td>${item.sell_success_orders}</td>
											<td>${item.total_success_orders}</td>
											<td>${item.create_time}</td>
											<td>${item.update_time}</td>
											<td>${item.remark}</td>
											
											<td>
											
												<c:if test="${security.isRolesAccessible('ROLE_ROOT,ROLE_ADMIN')
									 				|| security.isResourceAccessible('OP_C2C_USER_OPERATE')}">
											
													<div class="btn-group">
														<button type="button" class="btn btn-light">操作</button>
														<button type="button" class="btn btn-light dropdown-toggle" data-toggle="dropdown" aria-expanded="false">
															<span class="caret"></span> <span class="sr-only">Toggle Dropdown</span>
														</button>
														<ul class="dropdown-menu" role="menu">
														
															<li><a href="<%=basePath%>normal/adminC2cUserAction!toUpdate.action?id=${item.id}">修改</a></li>
															<%-- <li><a href="javascript:reset('${item.id}', '${item.deposit_gift_rate}')">修改账户保证金</a></li> --%>
															<li><a href="javascript:reset('${item.id}', '${item.deposit_open}', '${item.deposit}')">修改账户保证金</a></li>
														
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
		<!-- //////////////////////////////////////////////////////////////////////////// -->

		<%@ include file="include/footer.jsp"%>

	</div>
	<!-- End Content -->
	<!-- //////////////////////////////////////////////////////////////////////////// -->
	
	<div class="form-group">
		<div class="col-sm-1">
			<!-- 模态框（Modal） -->
			<div class="modal fade" id="c2c_manager_form" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
				<div class="modal-dialog">
					<div class="modal-content">
					
						<div class="modal-header">
							<button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
							<h4 class="modal-title" id="myModalLabel">C2C管理员</h4>
						</div>
						
						<div class="modal-body"> <div class="form-group"> <label class="col-sm-2 control-label form-label">用户名</label>
							<div class="col-sm-8"> <input id="c2c_manager_user_name" class="form-control" readonly="readonly" /> </div> </div> </div>
						
						<div class="modal-header">
							<h4 class="modal-title" id="myModalLabel">下级承兑商</h4>
						</div>
						
						<div class="modal-body" style="max-height: 400px;overflow-y: scroll;">						
							<table class="table table-bordered table-striped" >
								<thead>
									<tr>
										<td>昵称</td>
										<td>UID</td>
										<td>类型</td>
										<td>保证金/剩余</td>
									</tr>
								</thead>
								<tbody id="modal_c2c_manager_table">
									<%@ include file="include/loading.jsp"%>
								</tbody>
							</table>							
						</div>
							
						<div class="modal-footer" style="margin-top: 0;">
							<button type="button" class="btn " data-dismiss="modal">关闭</button>
							<button id="sub" type="submit" class="btn btn-default" >确认</button>
						</div>
					
					</div>
				</div>
			</div>
		</div>
	</div>
	
	<%-- <div class="form-group">
		<form action="<%=basePath%>normal/adminC2cUserAction!reset.action" method="post" id="resetForm">
		
			<input type="hidden" id="session_token_reset" name="session_token" value="${session_token}" />
			<input type="hidden" id="id_reset" name="id" value="${id}" />
			
			<div class="col-sm-1">
				<!-- 模态框（Modal） -->
				<div class="modal fade" id="modal_reset" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
					<div class="modal-dialog">
						<div class="modal-content">
						
							<div class="modal-header">
								<button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
								<h4 class="modal-title" id="myModalLabel">充值保证金</h4>
							</div>
							
							<div class="modal-body" style="display: none">
								<div class="">
								<span class="help-block">赠送比率</span>
									<div class="input-group">
										<input id="deposit_gift_rate" name="deposit_gift_rate" class="form-control " readOnly="readOnly" />
										<div class="input-group-addon">%</div>
									</div>
								</div>
							</div>
							
							<div class="modal-body">
								<div class="">
									<span class="help-block">充值额度</span>
									<div class="input-group">
										<input id="money_revise" name="money_revise" class="form-control " onBlur="compute();"/>
										<div class="input-group-addon">USDT</div>
									</div>
								</div>
							</div>
							
							<div class="modal-body">
								<div class="">
								<span class="help-block">实际到账额度</span>
									<div class="input-group">
										<input id="real_money_revise" name="real_money_revise" class="form-control " readOnly="readOnly" />
										<div class="input-group-addon">USDT</div>
									</div>
								</div>
							</div>
							
							<div class="modal-header">
								<h4 class="modal-title" id="myModalLabel">资金密码</h4>
							</div>
							
							<div class="modal-body">
								<div class="">
									<input id="safe_password" type="password" name="safe_password" class="form-control" placeholder="请输入资金密码">
								</div>
							</div>
							
							<div class="modal-footer" style="margin-top: 0;">
								<button type="button" class="btn " data-dismiss="modal">关闭</button>
								<button id="sub" type="submit" class="btn btn-default" >确认</button>
							</div>
							
						</div>
						<!-- /.modal-content -->
					</div>
					<!-- /.modal -->
				</div>
			</div>
			
		</form>
	</div> --%>
	
	<div class="form-group">
		<form action="<%=basePath%>normal/adminC2cUserAction!reset.action" method="post" id="resetForm">
		
			<input type="hidden" id="id_reset" name="id" />
			
			<div class="col-sm-1">
				<!-- 模态框（Modal） -->
				<div class="modal fade" id="modal_reset" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
					<div class="modal-dialog">
						<div class="modal-content" style="width: 725px;">
						
							<div class="modal-header">
								<button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
								<h4 class="modal-title" id="myModalLabel">修改 承兑商保证金</h4>
							</div>
							
							<div class="modal-body">
								<div class="col-sm-12">
									<div class="col-sm-2" >
										<select id="recharge_withdraw" name="recharge_withdraw" class="form-control " >
											<option value="recharge" <c:if test="${recharge_withdraw == 'recharge'}">selected="true"</c:if> >充值</option>
											<option value="withdraw" <c:if test="${recharge_withdraw == 'withdraw'}">selected="true"</c:if> >提现</option>
										</select>
									</div>
									<div class="input-group col-sm-10">
										<input id="money_change" name="money_change" class="form-control " onBlur="compute();" />
										<div class="input-group-addon" >USDT</div>
									</div>
								</div>
							</div>
								
							<div class="modal-body">
								<div class="col-sm-12">
									<div class="input-group">
										<div class="input-group-addon" >当前的总保证金</div>
										<input id="deposit_open" name="deposit_open" class="form-control " readOnly="readOnly" />
										<div class="input-group-addon" >USDT</div>
									</div>
								</div>
							</div>
								
							<div class="modal-body">
								<div class="col-sm-12">
									<div class="input-group">
										<div class="input-group-addon" >修改后总保证金</div>
										<input id="deposit_open_change" name="deposit_open_change" class="form-control " readOnly="readOnly" />
										<div class="input-group-addon" >USDT</div>
									</div>
								</div>
							</div>
								
							<div class="modal-body">
								<div class="col-sm-12">
									<div class="input-group">
										<div class="input-group-addon" >当前的剩余保证金</div>
										<input id="deposit" name="deposit" class="form-control " readOnly="readOnly" />
										<div class="input-group-addon" >USDT</div>
									</div>
								</div>
							</div>
								
							<div class="modal-body">
								<div class="col-sm-12">
									<div class="input-group">
										<div class="input-group-addon" >修改后剩余保证金</div>
										<input id="deposit_change" name="deposit_change" class="form-control " readOnly="readOnly" />
										<div class="input-group-addon" >USDT</div>
									</div>
								</div>
							</div>
							
							<div class="modal-header">
								<h4 class="modal-title" id="myModalLabel">资金密码</h4>
							</div>
							
							<div class="modal-body">
								<div class="">
									<input id="safe_password" type="password" name="safe_password" class="form-control" placeholder="请输入资金密码">
								</div>
							</div>
							
							<div class="modal-footer" style="margin-top: 0;">
								<button type="button" class="btn " data-dismiss="modal">关闭</button>
								<button id="sub" type="submit" class="btn btn-default" >确认</button>
							</div>
							
						</div>
						<!-- /.modal-content -->
					</div>
					<!-- /.modal -->
				</div>
			</div>
			
		</form>
	</div>

	<%@ include file="include/js.jsp"%>
	
	 <script type="text/javascript">
		
		function reset(id, deposit_open, deposit) {
			$("#id_reset").val(id);
			$("#deposit_open").val(deposit_open);
			$("#deposit_open_change").val(deposit_open);
			$("#deposit").val(deposit);
			$("#deposit_change").val(deposit);
 			$("#money_change").val("0");
			$('#modal_reset').modal("show");
		}
	 	function compute() {
	 		var recharge_withdraw = $("#recharge_withdraw").val();
	 		var money_change = $("#money_change").val();
	 		if ("recharge" == recharge_withdraw) {
	 			// 充值
	 			$("#deposit_open_change").val($("#deposit_open").val() * 1 + money_change * 1);
	 			$("#deposit_change").val($("#deposit").val() * 1 + money_change * 1);
	 		} else if ("withdraw" == recharge_withdraw) {
	 			// 提现
	 			$("#deposit_open_change").val($("#deposit_open").val() * 1 - money_change * 1);
	 			$("#deposit_change").val($("#deposit").val() * 1 - money_change * 1);
	 		} else {
	 			$("#money_change").val("0");
	 		}
	 	}
	 	
// 		function reset(id, deposit_gift_rate) {
// 			var session_token = $("#session_token").val();
// 			$("#session_token_reset").val(session_token);
// 			$("#id_reset").val(id);
// 			$("#deposit_gift_rate").val(deposit_gift_rate);
// 			$('#modal_reset').modal("show");
// 		}
// 	 	function compute() {
// 	 		var money_revise = $("#money_revise").val();
// 	 		var deposit_gift_rate = $("#deposit_gift_rate").val();
// 	 		var rate = Number(accMul(deposit_gift_rate, 0.01)) + 1;
// 	 		$("#real_money_revise").val(accMul(money_revise, rate));
// 	 	}
// 	 	function accMul(arg1, arg2) {
// 	 	    var m = 0,
// 	 	        s1 = arg1.toString(),
// 	 	        s2 = arg2.toString();
// 	 	    try {
// 	 	        m += s1.split(".")[1].length
// 	 	    } catch (e) {}
// 	 	    try {
// 	 	        m += s2.split(".")[1].length
// 	 	    } catch (e) {}
// 	 	    return Number(s1.replace(".", "")) * Number(s2.replace(".", "")) / Math.pow(10, m)
// 	 	}
	 	
		function getC2cManagerInfo(manager_id, manager_name, c2c_user_id) {
	 		$("#c2c_manager_user_name").val(manager_name);
			$("#c2c_manager_form").modal("show");
			var url = "<%=basePath%>normal/adminC2cUserAction!getC2cManagerInfo.action";
			var data = 
			{
				"manager_id": manager_id,
				"c2c_user_id": c2c_user_id
			};
			goNewAjaxUrl(url, data, function(tmp) {
				var str='';
				var content='';				
				for (var i=0; i<tmp.manager_c2c_user.length; i++) {
					str += '<tr>'
						+ '<td>' + tmp.manager_c2c_user[i].nick_name + '</td>'
						+ '<td>' + tmp.manager_c2c_user[i].c2c_user_code + '</td>'
						+ '<td>' + getC2cUserType(tmp.manager_c2c_user[i].c2c_user_type) + '</td>'
						+ '<td>' + getDepositFloor(tmp.manager_c2c_user[i].deposit_open) + '/' + getDepositFloor(tmp.manager_c2c_user[i].deposit) + '</td>'
						+ '</tr>';
				}
				$("#modal_c2c_manager_table").html(str);
			}, function() {
//				$("#coin_value").val(0);
			});
		}
		
		function getC2cUserType(type) {
			if (type == "2") {
				return '<span class="right label label-success">用户</span>';
			} else if (type == "1") {
				return '<span class="right label label-danger">后台</span>';
			} else {
				return "";
			}
		}
		
		function getDepositFloor(deposit) {
			return Math.floor(deposit * 100) / 100;
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
