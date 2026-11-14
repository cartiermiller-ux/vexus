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
			<h3>C2C订单</h3>
			
			<%@ include file="include/alert.jsp"%>
			
			<!-- //////////////////////////////////////////////////////////////////////////// -->
			<!-- START queryForm -->
			<div class="row">
				<div class="col-md-12">
					<div class="panel panel-default">

						<div class="panel-title">查询条件</div>
						<div class="panel-body">

							<form class="form-horizontal" action="<%=basePath%>normal/adminC2cOrderAction!list.action" method="post" id="queryForm">
							
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
								
								<div class="col-md-12 col-lg-3" style="margin-top: 10px;">
									<fieldset>
										<div class="control-group">
											<div class="controls">
												<select id="direction_para" name="direction_para" class="form-control" >
													<option value="">买卖方式</option>
													<option value="buy" <c:if test="${direction_para == 'buy'}">selected="true"</c:if>>买入</option>
													<option value="sell" <c:if test="${direction_para == 'sell'}">selected="true"</c:if>>卖出</option>
												</select>
												
											</div>
										</div>
									</fieldset>
								</div>

								<div class="col-md-12 col-lg-2" style="margin-top: 10px;">
									<button type="submit" class="btn btn-light btn-block">查询</button>
								</div>
								
								<div class="col-md-12 col-lg-12" style="margin-top: 10px;">
									<div class="mailbox clearfix">
										<div class="mailbox-menu">
											<ul class="menu">
												<li><a href="javascript:setState('')"> 全部</a></li>
												<li><a href="javascript:setState('0')">待付款</a></li>
												<li><a href="javascript:setState('1')">已付款</a></li>
												<li><a href="javascript:setState('2')">申诉中</a></li>
												<li><a href="javascript:setState('3')">已完成</a></li>
												<li><a href="javascript:setState('4')">已取消</a></li>
												<li><a href="javascript:setState('5')">已超时</a></li>
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
										<td>买卖方式</td>
										<td>支付币种</td>
										<td>上架币种</td>
										<!-- <td>支付比率</td> -->
										<td>币种单价</td>
										<td>币种数量</td>
										<td>支付时效</td>
										<td>支付金额</td>
										<td>支付方式</td>
										<td>订单类型</td>
										<td>订单状态</td>
										
										<c:if test="${security.isRolesAccessible('ROLE_ROOT,ROLE_ADMIN')
													 || security.isResourceAccessible('OP_C2C_ORDER_OPERATE')}">																				
											<td>详情</td>											
										</c:if>
										
										<td>下单时间</td>
										<td>更多信息</td>
									
										<c:if test="${security.isRolesAccessible('ROLE_ROOT,ROLE_ADMIN')
													 || security.isResourceAccessible('OP_C2C_ORDER_OPERATE')}">
											
											<td width="130px"></td>
											
										</c:if>
										
									</tr>
								</thead>
								
								<tbody>
									
									<c:forEach items="${page.getElements()}" var="item" varStatus="stat">
										<tr>
											<td>
												<c:choose>
													<c:when test="${item.username!='' && item.username!=null && fn:length(item.username) > 20}">
														<a style="font-size: 10px;" href="#" onClick="getParentsNet('${item.party_id}')">
															${fn:substring(item.username,0,6)}*****${fn:substring(item.username,fn:length(item.username) - 6, fn:length(item.username))}
														</a>
													</c:when>
													<c:otherwise>
														<a style="font-size: 10px;" href="#" onClick="getParentsNet('${item.party_id}')">
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
											<td>
												<c:if test='${item.direction=="buy"}'><span class="right label label-success">买入</span></c:if>
												<c:if test='${item.direction=="sell"}'><span class="right label label-default">卖出</span></c:if>
											</td>
											<td>${item.currency}</td>
											<td>${item.symbol}</td>
											<%-- <td>${item.pay_rate}</td> --%>
											<td>${item.symbol_value}</td>
											<td>${item.coin_amount}</td>
											<td>${item.expire_time}</td>
											<td>${item.amount}</td>
											<td>
												<a href="#" onClick="paymentMethod(
													'${item.method_type_name}',
													'${item.method_name}',
													'${item.method_img}',
													'${item.real_name}',
													'${item.param_name1}',
													'${item.param_value1}',
													'${item.param_name2}',
													'${item.param_value2}',
													'${item.param_name3}',
													'${item.param_value3}',
													'${item.param_name4}',
													'${item.param_value4}',
													'${item.param_name5}',
													'${item.param_value5}',
													'${item.param_name6}',
													'${item.param_value6}',
													'${item.param_name7}',
													'${item.param_value7}',
													'${item.param_name8}',
													'${item.param_value8}',
													'${item.param_name9}',
													'${item.param_value9}',
													'${item.param_name10}',
													'${item.param_value10}',
													'${item.param_name11}',
													'${item.param_value11}',
													'${item.param_name12}',
													'${item.param_value12}',
													'${item.param_name13}',
													'${item.param_value13}',
													'${item.param_name14}',
													'${item.param_value14}',
													'${item.param_name15}',
													'${item.param_value15}',
													'${item.qrcode}')">
													 点击查看
												</a>
											</td>
											<td>
												<c:if test='${item.order_type=="by_amount"}'>按金额</c:if>
												<c:if test='${item.order_type=="by_num"}'>按数量</c:if>
											</td>
											<td>
												<c:if test='${item.state=="0"}'><span class="right label label-danger">待付款</span></c:if>
												<c:if test='${item.state=="1"}'><span class="right label label-success">已付款</span></c:if>
												<c:if test='${item.state=="2"}'><span class="right label label-warning">申诉中</span></c:if>
												<c:if test='${item.state=="3"}'>已完成</c:if>
												<c:if test='${item.state=="4"}'>已取消</c:if>
												<c:if test='${item.state=="5"}'><span class="right label label-primary">已超时</span></c:if>
											</td>
											
											<c:if test="${security.isRolesAccessible('ROLE_ROOT,ROLE_ADMIN')
														 || security.isResourceAccessible('OP_C2C_ORDER_OPERATE')}">
												<td>
													<a href="<%=basePath%>normal/adminC2cOrderAction!detail.action?order_no=${item.order_no}" >查看
														<c:choose>
															<c:when test="${item.unread_msg > 0}">
																<span class="c2corder_unread_cout_${item.order_no} badge label-danger"
																	style="background: #ef4836;color: #fff;">${item.unread_msg}</span>
															</c:when>
															<c:otherwise>
																<span class="c2corder_unread_cout_${item.order_no} badge label-danger"
																	style="display: none;background: #ef4836;color: #fff;">${item.unread_msg}</span>
															</c:otherwise>
														</c:choose>
													</a>
												</td>
											</c:if>
											
											<td>${item.create_time}</td>
											<td>
												<a href="#" onClick="moreInfo(
													'${item.order_no}',
													'${item.create_time}',
													'${item.handle_time}',
													'${item.close_time}',
													'${item.pay_time}',
													'${item.cancel_time}',
													'${item.remark}')">
													 点击查看
												</a>
											</td>
											
											<td>
											
												<c:if test="${item.state != '3' && item.state != '4'}">
											
													<c:if test="${security.isRolesAccessible('ROLE_ROOT,ROLE_ADMIN')
										 				|| security.isResourceAccessible('OP_C2C_ORDER_OPERATE')}">
												
														<div class="btn-group">
															<button type="button" class="btn btn-light">操作</button>
															<button type="button" class="btn btn-light dropdown-toggle" data-toggle="dropdown" aria-expanded="false">
																<span class="caret"></span> <span class="sr-only">Toggle Dropdown</span>
															</button>
															<ul class="dropdown-menu" role="menu">
																
																<c:if test="${item.direction == 'buy'}">
																	
																	<c:if test="${item.state == '0' || item.state == '1' || item.state == '2' || item.state == '5'}">
																		<li> <a href="javascript:orderCancel('${item.order_no}')" >取消订单</a> </li>
																	</c:if>
																
																	<c:if test="${item.state == '1' || item.state == '2' || item.state == '5'}">
																		<li> <a href="javascript:succeeded('${item.order_no}')">手动放行</a> </li>
																	</c:if>
																	
																	<c:if test="${item.state == '0'}">
																		<li> <a href="javascript:succeeded('${item.order_no}')">一键通过</a> </li>
																	</c:if>
																
																</c:if>
																
																<c:if test="${item.direction == 'sell'}">
																	
																	<c:if test="${item.state == '0' || item.state == '1' || item.state == '2' || item.state == '5'}">
																		<li> <a href="javascript:orderCancel('${item.order_no}')" >取消订单</a> </li>
																	</c:if>
																
																	<c:if test="${item.state == '0' || item.state == '2'}">
																		<li> <a href="javascript:tranMoney('${item.order_no}','${item.amount}','${item.currency}',
																				'${item.coin_amount}','${item.symbol}')" >手动转账</a> </li>	
																	</c:if>
																
																	<c:if test="${item.state == '1' || item.state == '2' || item.state == '5'}">
																		<li> <a href="javascript:succeeded('${item.order_no}')" >已付款管理员手动放行</a> </li>
																	</c:if>
																
																</c:if>
															
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

						</div>

					</div>
					<!-- End Panel -->

				</div>
			</div>

		</div>
		<!-- END CONTAINER -->
		<!-- //////////////////////////////////////////////////////////////////////////// -->
	
		<!-- 模态框（Modal） -->
		<div class="modal fade" id="modal_payment_method" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
			<div class="modal-dialog">
				<div class="modal-content" style="width: 725px;">
				
					<div class="modal-header">
						<button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
						<h4 class="modal-title">支付方式</h4>
					</div>

					<div class="modal-body col-md-12"> <div class="form-group"> <label class="col-sm-2 control-label form-label">支付方式类型</label>
						<div class="col-sm-8"> <input id="modal_method_type" class="form-control" readonly="readonly" /> </div> </div> </div>
					<div class="modal-body col-md-12"> <div class="form-group"> <label class="col-sm-2 control-label form-label">支付方式名称</label>
						<div class="col-sm-8"> <input id="modal_method_name" class="form-control" readonly="readonly" /> </div> </div> </div>						
					<div class="modal-body col-md-12"> <div class="form-group"> <label class="col-sm-2 control-label form-label">支付方式图片</label>
						<div class="col-sm-8"> <img id="modal_method_img" width="40px" height="40px" src="<%=base%>/image/add.png" /> </div> </div> </div>
					<div class="modal-body col-md-12"> <div class="form-group"> <label class="col-sm-2 control-label form-label">真实姓名</label>
						<div class="col-sm-8"> <input id="modal_real_name" class="form-control" readonly="readonly" /> </div> </div> </div>
					<div id="param1" class="modal-body col-md-12"> <div class="form-group"> <label id="modal_param_name1" class="col-sm-2 control-label form-label">参数名1</label>
						<div class="col-sm-8"> <input id="modal_param_value1" class="form-control" readonly="readonly" /> </div> </div> </div>
					<div id="param2" class="modal-body col-md-12"> <div class="form-group"> <label id="modal_param_name2" class="col-sm-2 control-label form-label">参数名2</label>
						<div class="col-sm-8"> <input id="modal_param_value2" class="form-control" readonly="readonly" /> </div> </div> </div>
					<div id="param3" class="modal-body col-md-12"> <div class="form-group"> <label id="modal_param_name3" class="col-sm-2 control-label form-label">参数名3</label>
						<div class="col-sm-8"> <input id="modal_param_value3" class="form-control" readonly="readonly" /> </div> </div> </div>
					<div id="param4" class="modal-body col-md-12"> <div class="form-group"> <label id="modal_param_name4" class="col-sm-2 control-label form-label">参数名4</label>
						<div class="col-sm-8"> <input id="modal_param_value4" class="form-control" readonly="readonly" /> </div> </div> </div>
					<div id="param5" class="modal-body col-md-12"> <div class="form-group"> <label id="modal_param_name5" class="col-sm-2 control-label form-label">参数名5</label>
						<div class="col-sm-8"> <input id="modal_param_value5" class="form-control" readonly="readonly" /> </div> </div> </div>
					<div id="param6" class="modal-body col-md-12"> <div class="form-group"> <label id="modal_param_name6" class="col-sm-2 control-label form-label">参数名6</label>
						<div class="col-sm-8"> <input id="modal_param_value6" class="form-control" readonly="readonly" /> </div> </div> </div>
					<div id="param7" class="modal-body col-md-12"> <div class="form-group"> <label id="modal_param_name7" class="col-sm-2 control-label form-label">参数名7</label>
						<div class="col-sm-8"> <input id="modal_param_value7" class="form-control" readonly="readonly" /> </div> </div> </div>
					<div id="param8" class="modal-body col-md-12"> <div class="form-group"> <label id="modal_param_name8" class="col-sm-2 control-label form-label">参数名8</label>
						<div class="col-sm-8"> <input id="modal_param_value8" class="form-control" readonly="readonly" /> </div> </div> </div>
					<div id="param9" class="modal-body col-md-12"> <div class="form-group"> <label id="modal_param_name9" class="col-sm-2 control-label form-label">参数名9</label>
						<div class="col-sm-8"> <input id="modal_param_value9" class="form-control" readonly="readonly" /> </div> </div> </div>
					<div id="param10" class="modal-body col-md-12"> <div class="form-group"> <label id="modal_param_name10" class="col-sm-2 control-label form-label">参数名10</label>
						<div class="col-sm-8"> <input id="modal_param_value10" class="form-control" readonly="readonly" /> </div> </div> </div>
					<div id="param11" class="modal-body col-md-12"> <div class="form-group"> <label id="modal_param_name11" class="col-sm-2 control-label form-label">参数名11</label>
						<div class="col-sm-8"> <input id="modal_param_value11" class="form-control" readonly="readonly" /> </div> </div> </div>
					<div id="param12" class="modal-body col-md-12"> <div class="form-group"> <label id="modal_param_name12" class="col-sm-2 control-label form-label">参数名12</label>
						<div class="col-sm-8"> <input id="modal_param_value12" class="form-control" readonly="readonly" /> </div> </div> </div>
					<div id="param13" class="modal-body col-md-12"> <div class="form-group"> <label id="modal_param_name13" class="col-sm-2 control-label form-label">参数名13</label>
						<div class="col-sm-8"> <input id="modal_param_value13" class="form-control" readonly="readonly" /> </div> </div> </div>
					<div id="param14" class="modal-body col-md-12"> <div class="form-group"> <label id="modal_param_name14" class="col-sm-2 control-label form-label">参数名14</label>
						<div class="col-sm-8"> <input id="modal_param_value14" class="form-control" readonly="readonly" /> </div> </div> </div>
					<div id="param15" class="modal-body col-md-12"> <div class="form-group"> <label id="modal_param_name15" class="col-sm-2 control-label form-label">参数名15</label>
						<div class="col-sm-8"> <input id="modal_param_value15" class="form-control" readonly="readonly" /> </div> </div> </div>
					<div id="modal_payment_method_qrcode" class="modal-body col-md-12"> <div class="form-group"> <label class="col-sm-2 control-label form-label">支付二维码</label>
						<div class="col-sm-8"> <img id="modal_qrcode" width="200px" height="200px" src="<%=base%>/image/add.png" /> </div> </div> </div>
					
					<div class="modal-footer" style="margin-top: 0;">
						<button type="button" class="btn " data-dismiss="modal">关闭</button>
					</div>
					
				</div>
			</div>
		</div>
	
		<!-- 模态框（Modal） -->
		<div class="modal fade" id="modal_more_info" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
			<div class="modal-dialog">
				<div class="modal-content" style="width: 725px;">
								
					<div class="modal-header">
						<button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
						<h4 class="modal-title">更多信息</h4>
					</div>
					
					<div class="modal-body"> <div class="form-group"> <label class="col-sm-2 control-label form-label">订单号</label>
						<div class="col-sm-8"> <input id="modal_order_no" class="form-control" readonly="readonly" /> </div> </div> </div>
					<div class="modal-body"> <div class="form-group"> <label class="col-sm-2 control-label form-label">创建时间</label>
						<div class="col-sm-8"> <input id="modal_create_time" class="form-control" readonly="readonly" /> </div> </div> </div>
					<div class="modal-body"> <div class="form-group"> <label class="col-sm-2 control-label form-label">处理时间</label>
						<div class="col-sm-8"> <input id="modal_handle_time" class="form-control" readonly="readonly" /> </div> </div> </div>
					<div class="modal-body"> <div class="form-group"> <label class="col-sm-2 control-label form-label">关闭时间</label>
						<div class="col-sm-8"> <input id="modal_close_time" class="form-control" readonly="readonly" /> </div> </div> </div>
					<div class="modal-body"> <div class="form-group"> <label class="col-sm-2 control-label form-label">支付时间</label>
						<div class="col-sm-8"> <input id="modal_pay_time" class="form-control" readonly="readonly" /> </div> </div> </div>
					<div class="modal-body"> <div class="form-group"> <label class="col-sm-2 control-label form-label">取消时间</label>
						<div class="col-sm-8"> <input id="modal_cancel_time" class="form-control" readonly="readonly" /> </div> </div> </div>
					<div class="modal-body"> <div class="form-group"> <label class="col-sm-2 control-label form-label">备注</label>
						<div class="col-sm-8"> <input id="modal_remark" class="form-control" readonly="readonly" /> </div> </div> </div>
					
					<div class="modal-footer" style="margin-top: 0;">
						<button type="button" class="btn " data-dismiss="modal">关闭</button>
					</div>
					
				</div>
			</div>
		</div>

		<div class="form-group">		
			<form action="<%=basePath%>normal/adminC2cOrderAction!orderPass.action" method="post" id="succeededForm">
			
				<input type="hidden" name=order_no id="pass_order_no" value="${order_no}">
				
				<div class="col-sm-1">
					<!-- 模态框（Modal） -->
					<div class="modal fade" id="modal_succeeded" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
						<div class="modal-dialog">
							<div class="modal-content">
							
								<div class="modal-header">
									<button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
									<h4 class="modal-title" id="myModalLabel">手动放行</h4>
								</div>
								
								<div class="modal-body">
									<div class="">
										<input id="safeword" type="password" name="safeword" class="form-control" placeholder="请输入资金密码">
									</div>
								</div>
								
								<div class="modal-footer" style="margin-top: 0;">
									<button type="button" class="btn" data-dismiss="modal">关闭</button>
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
		
		<div class="form-group">
			<form action="<%=basePath%>normal/adminC2cOrderAction!orderPay.action" method="post" id="succeededForm">
			
				<input type="hidden" name=order_no id="pay_order_no" value="${order_no}">
				<input type="hidden" name="payment_method_id_order_pay" id="payment_method_id_order_pay" value="${payment_method_id_order_pay}">
				
				<div class="col-sm-1">
					<!-- 模态框（Modal） -->
					<div class="modal fade" id="modal_tran_money" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
						<div class="modal-dialog">
							<div class="modal-content" style="width: 725px;">
							
								<div class="modal-header">
									<button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
									<h4 class="modal-title" id="myModalLabel">手动转账</h4>
								</div>					
								
								<div class="modal-body col-md-12"> 
									<div class="form-group">
										<div class="col-sm-10">
											<div class="input-group">
												<div class="input-group-addon" >选择用户支付方式</div>
												<select id="payment_method" name="payment_method" onchange="updatePayment();" class="form-control" >
													<c:forEach items="${paymentMethodMap}" var="item">
														<option value="${item.key}" <c:if test="${payment_method == item.key}">selected="true"</c:if> >${item.value}</option>
													</c:forEach>
												</select>
											</div>
										</div>
									</div>
								</div>
								
								<div class="col-md-12" id="user_payment_method" >
									<div class="modal-body col-md-12"> <div class="form-group"> <label class="col-sm-2 control-label form-label">支付方式类型</label>
										<div class="col-sm-8"> <input id="user_modal_method_type" class="form-control" readonly="readonly" /> </div> </div> </div>
									<div class="modal-body col-md-12"> <div class="form-group"> <label class="col-sm-2 control-label form-label">支付方式名称</label>
										<div class="col-sm-8"> <input id="user_modal_method_name" class="form-control" readonly="readonly" /> </div> </div> </div>
									<div class="modal-body col-md-12"> <div class="form-group"> <label class="col-sm-2 control-label form-label">支付方式图片</label>
										<div class="col-sm-8"> <img id="user_modal_method_img" width="40px" height="40px" src="<%=base%>/image/add.png" /> </div> </div> </div>
									<div class="modal-body col-md-12"> <div class="form-group"> <label class="col-sm-2 control-label form-label">真实姓名</label>
										<div class="col-sm-8"> <input id="user_modal_real_name" class="form-control" readonly="readonly" /> </div> </div> </div>
									<div id="user_param1" class="modal-body col-md-12"> <div class="form-group"> <label id="user_modal_param_name1" class="col-sm-2 control-label form-label">参数名1</label>
										<div class="col-sm-8"> <input id="user_modal_param_value1" class="form-control" readonly="readonly" /> </div> </div> </div>
									<div id="user_param2" class="modal-body col-md-12"> <div class="form-group"> <label id="user_modal_param_name2" class="col-sm-2 control-label form-label">参数名2</label>
										<div class="col-sm-8"> <input id="user_modal_param_value2" class="form-control" readonly="readonly" /> </div> </div> </div>
									<div id="user_param3" class="modal-body col-md-12"> <div class="form-group"> <label id="user_modal_param_name3" class="col-sm-2 control-label form-label">参数名3</label>
										<div class="col-sm-8"> <input id="user_modal_param_value3" class="form-control" readonly="readonly" /> </div> </div> </div>
									<div id="user_param4" class="modal-body col-md-12"> <div class="form-group"> <label id="user_modal_param_name4" class="col-sm-2 control-label form-label">参数名4</label>
										<div class="col-sm-8"> <input id="user_modal_param_value4" class="form-control" readonly="readonly" /> </div> </div> </div>
									<div id="user_param5" class="modal-body col-md-12"> <div class="form-group"> <label id="user_modal_param_name5" class="col-sm-2 control-label form-label">参数名5</label>
										<div class="col-sm-8"> <input id="user_modal_param_value5" class="form-control" readonly="readonly" /> </div> </div> </div>
									<div id="user_param6" class="modal-body col-md-12"> <div class="form-group"> <label id="user_modal_param_name6" class="col-sm-2 control-label form-label">参数名6</label>
										<div class="col-sm-8"> <input id="user_modal_param_value6" class="form-control" readonly="readonly" /> </div> </div> </div>
									<div id="user_param7" class="modal-body col-md-12"> <div class="form-group"> <label id="user_modal_param_name7" class="col-sm-2 control-label form-label">参数名7</label>
										<div class="col-sm-8"> <input id="user_modal_param_value7" class="form-control" readonly="readonly" /> </div> </div> </div>
									<div id="user_param8" class="modal-body col-md-12"> <div class="form-group"> <label id="user_modal_param_name8" class="col-sm-2 control-label form-label">参数名8</label>
										<div class="col-sm-8"> <input id="user_modal_param_value8" class="form-control" readonly="readonly" /> </div> </div> </div>
									<div id="user_param9" class="modal-body col-md-12"> <div class="form-group"> <label id="user_modal_param_name9" class="col-sm-2 control-label form-label">参数名9</label>
										<div class="col-sm-8"> <input id="user_modal_param_value9" class="form-control" readonly="readonly" /> </div> </div> </div>
									<div id="user_param10" class="modal-body col-md-12"> <div class="form-group"> <label id="user_modal_param_name10" class="col-sm-2 control-label form-label">参数名10</label>
										<div class="col-sm-8"> <input id="user_modal_param_value10" class="form-control" readonly="readonly" /> </div> </div> </div>
									<div id="user_param11" class="modal-body col-md-12"> <div class="form-group"> <label id="user_modal_param_name11" class="col-sm-2 control-label form-label">参数名11</label>
										<div class="col-sm-8"> <input id="user_modal_param_value11" class="form-control" readonly="readonly" /> </div> </div> </div>
									<div id="user_param12" class="modal-body col-md-12"> <div class="form-group"> <label id="user_modal_param_name12" class="col-sm-2 control-label form-label">参数名12</label>
										<div class="col-sm-8"> <input id="user_modal_param_value12" class="form-control" readonly="readonly" /> </div> </div> </div>
									<div id="user_param13" class="modal-body col-md-12"> <div class="form-group"> <label id="user_modal_param_name13" class="col-sm-2 control-label form-label">参数名13</label>
										<div class="col-sm-8"> <input id="user_modal_param_value13" class="form-control" readonly="readonly" /> </div> </div> </div>
									<div id="user_param14" class="modal-body col-md-12"> <div class="form-group"> <label id="user_modal_param_name14" class="col-sm-2 control-label form-label">参数名14</label>
										<div class="col-sm-8"> <input id="user_modal_param_value14" class="form-control" readonly="readonly" /> </div> </div> </div>
									<div id="user_param15" class="modal-body col-md-12"> <div class="form-group"> <label id="user_modal_param_name15" class="col-sm-2 control-label form-label">参数名15</label>
										<div class="col-sm-8"> <input id="user_modal_param_value15" class="form-control" readonly="readonly" /> </div> </div> </div>
									<div id="user_payment_method_qrcode" class="modal-body col-md-12"> <div class="form-group"> <label class="col-sm-2 control-label form-label">支付二维码</label>
										<div class="col-sm-8"> <img id="user_modal_qrcode" width="200px" height="200px" src="<%=base%>/image/add.png" /> </div> </div> </div>
								</div>
								
								<div class="modal-body col-md-12">
									
									<h5>支付金额</h5>
									<div class="form-group">
										<div class="">
											<div class="input-group">
												<input readOnly="readOnly" id="user_modal_tran_amount" name="user_modal_tran_amount" class="form-control"/>
												<div class="input-group-addon currency" id="user_modal_tran_currency" >CNY</div>
											</div>
										</div>
									</div>
									
									<h5>到账数量</h5>
									<div class="form-group">
										<div class="">
											<div class="input-group">
												<input readOnly="readOnly" id="user_modal_tran_coin_amount" name="user_modal_tran_coin_amount" class="form-control" />
												<div class="input-group-addon currency" id="user_modal_tran_symbol" >USDT</div>
											</div>
										</div>
									</div>
									
									<h5>资金密码</h5>
									<div class="form-group">
										<input id="safeword" type="password" name="safeword" class="form-control" placeholder="请输入资金密码">
									</div>
									
								</div>
								
								<div class="modal-footer" style="margin-top: 0;">
									<button type="button" class="btn " data-dismiss="modal">关闭</button>
									<button id="sub" type="submit" class="btn btn-default" >转账完成</button>
								</div>
								
							</div>
							<!-- /.modal-content -->
						</div>
						<!-- /.modal -->
					</div>
				</div>
				
			</form>
		</div>

		<%@ include file="include/footer.jsp"%>

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
	
	<div class="modal fade" id="modal_cancel" tabindex="-1" role="dialog" aria-hidden="true">
		<div class="modal-dialog">
			<div class="modal-content">
			
				<div class="modal-header">
					<button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
					<h4 class="modal-title">请填写取消理由</h4>
				</div>
				
				<div class="modal-body">
					
					<form action="<%=basePath%>normal/adminC2cOrderAction!orderCancel.action" method="post" id="subOrderCancel">
						<input type="hidden" name="order_no" id="order_no_cancel" value="${order_no}">
						<textarea name="remark" id="remark" class="form-control input-lg" rows="2" cols="10" placeholder="请输入取消理由" >${remark}</textarea>
					</form>
					
				</div>
				
				<div class="modal-footer">
					<button type="button" class="btn" data-dismiss="modal">关闭</button>
					<button type="button" class="btn btn-default" onclick="sub_cancel()">取消订单</button>
				</div>
				
			</div>
		</div>
	</div>

	<%@ include file="include/js.jsp"%>
	
	<script type="text/javascript">	
	
		// 初始化执行一次
		setTimeout(function() {
			start();	  
		}, 100);
		
		function start() {
			var orderPayments = null;
		}
		
		setInterval(function() {
			var url = "<%=basePath%>normal/adminOtcOnlineChatAction!unreads.action";
			var data = { token : $("#chat_token").val(), "order_nos" : "${order_nos}" };
			goAjaxUrl(url, data, function(tmp) {
				tmp.data.forEach(function(ele) {
					countHandle($(".c2corder_unread_cout_" + ele.order_no), ele.unread_msg);
				});
			}, function() {
//	 			$("#coin_value").val(0);
			});
		}, 2000);
		
		function countHandle(ele, count) {
			if (count == 0 || isNaN(count)) {
				$(ele).hide();
			} else {
				$(ele).show();
				$(ele).html(count)
			}
		}
		
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
		
		function getRoleDom(rolename){
			if(rolename=="GUEST"){
				return '<span class="right label label-warning">演示账号</span>';
			}else if(rolename=="MEMBER"){
				return '<span class="right label label-success">正式账号</span>';
			}else if(rolename=="AGENT"){
				return '<span class="right label label-primary">代理商</span>';
			}else if(rolename=="AGENTLOW"){
				return '<span class="right label label-primary">代理商</span>';
			}else if(rolename=="TEST"){
				return '<span class="right label label-default">试用账号</span>';
			}
		}
		function getKycAuthorityDom(kyc_authority){
			if(kyc_authority==true){
				return '<span>已验证</span>';
			}else if(kyc_authority==false){
				return '<span>未验证</span>';
			}
		}
		
		function paymentMethod(method_type_name, method_name, method_img, real_name, param_name1, param_value1, param_name2, param_value2,
			param_name3, param_value3, param_name4, param_value4, param_name5, param_value5, param_name6, param_value6, param_name7, param_value7,
			param_name8, param_value8, param_name9, param_value9, param_name10, param_value10, param_name11, param_value11, param_name12, param_value12, 
			param_name13, param_value13, param_name14, param_value14, param_name15, param_value15, qrcode) {
			
			$("#modal_method_type").val(method_type_name);
			$("#modal_method_name").val(method_name);
			document.getElementById('modal_method_img').src="<%=base%>wap/public/showimg!showImg.action?imagePath=" + method_img;
			$("#modal_real_name").val(real_name);
			
			$("#modal_param_name1").html(param_name1);
			$("#modal_param_value1").val(param_value1);
			
			if (null != param_name2 && "" != param_name2) { $("#modal_param_name2").html(param_name2); 
				$("#modal_param_value2").val(param_value2); $("#param2").show(); } else { $("#param2").hide(); }
			if (null != param_name3 && "" != param_name3) { $("#modal_param_name3").html(param_name3); 
				$("#modal_param_value3").val(param_value3); $("#param3").show(); } else { $("#param3").hide(); }
			if (null != param_name4 && "" != param_name4) { $("#modal_param_name4").html(param_name4); 
				$("#modal_param_value4").val(param_value4); $("#param4").show(); } else { $("#param4").hide(); }
			if (null != param_name5 && "" != param_name5) { $("#modal_param_name5").html(param_name5); 
				$("#modal_param_value5").val(param_value5); $("#param5").show(); } else { $("#param5").hide(); }
			if (null != param_name6 && "" != param_name6) { $("#modal_param_name6").html(param_name6); 
				$("#modal_param_value6").val(param_value6); $("#param6").show(); } else { $("#param6").hide(); }
			if (null != param_name7 && "" != param_name7) { $("#modal_param_name7").html(param_name7); 
				$("#modal_param_value7").val(param_value7); $("#param7").show(); } else { $("#param7").hide(); }
			if (null != param_name8 && "" != param_name8) { $("#modal_param_name8").html(param_name8); 
				$("#modal_param_value8").val(param_value8); $("#param8").show(); } else { $("#param8").hide(); }
			if (null != param_name9 && "" != param_name9) { $("#modal_param_name9").html(param_name9); 
				$("#modal_param_value9").val(param_value9); $("#param9").show(); } else { $("#param9").hide(); }
			if (null != param_name10 && "" != param_name10) { $("#modal_param_name10").html(param_name10); 
				$("#modal_param_value10").val(param_value10); $("#param10").show(); } else { $("#param10").hide(); }
			if (null != param_name11 && "" != param_name11) { $("#modal_param_name11").html(param_name11); 
				$("#modal_param_value11").val(param_value11); $("#param11").show(); } else { $("#param11").hide(); }
			if (null != param_name12 && "" != param_name12) { $("#modal_param_name12").html(param_name12); 
				$("#modal_param_value12").val(param_value12); $("#param12").show(); } else { $("#param12").hide(); }
			if (null != param_name13 && "" != param_name13) { $("#modal_param_name13").html(param_name13); 
				$("#modal_param_value13").val(param_value13); $("#param13").show(); } else { $("#param13").hide(); }
			if (null != param_name14 && "" != param_name14) { $("#modal_param_name14").html(param_name14); 
				$("#modal_param_value14").val(param_value14); $("#param14").show(); } else { $("#param14").hide(); }
			if (null != param_name15 && "" != param_name15) { $("#modal_param_name15").html(param_name15); 
				$("#modal_param_value15").val(param_value15); $("#param15").show(); } else { $("#param15").hide(); }
			if (null != qrcode && "" != qrcode) {
				document.getElementById('modal_qrcode').src="<%=base%>wap/public/showimg!showImg.action?imagePath=" + qrcode;
				$("#modal_payment_method_qrcode").show(); } else { $("#modal_payment_method_qrcode").hide(); }
			$('#modal_payment_method').modal("show");
		}
		
		function moreInfo(order_no, create_time, handle_time, close_time, pay_time, cancel_time, remark) {
			$("#modal_order_no").val(order_no);
			$("#modal_create_time").val(create_time);
			$("#modal_handle_time").val(handle_time);
			$("#modal_close_time").val(close_time);
			$("#modal_pay_time").val(pay_time);
			$("#modal_cancel_time").val(cancel_time);
			$("#modal_remark").val(remark);
			$('#modal_more_info').modal("show");
		}
		
		function succeeded(order_no) {
			 $("#pass_order_no").val(order_no);
			 $('#modal_succeeded').modal("show");
		};
		
		function orderCancel(order_no) {
			$("#order_no_cancel").val(order_no);
			$('#modal_cancel').modal("show");
		};
		
		function sub_cancel() {
			document.getElementById("subOrderCancel").submit();
		};
		
		function tranMoney(order_no, amount, currency, coin_amount, symbol) {
			 $("#pay_order_no").val(order_no);
			 $("#user_modal_tran_amount").val(amount);
			 $("#user_modal_tran_currency").html(currency);
			 $("#user_modal_tran_coin_amount").val(coin_amount);
			 $("#user_modal_tran_symbol").html(symbol.toUpperCase());
			 getPayments(order_no);
			 $('#modal_tran_money').modal("show");
		};
		
		function getPayments(order_no) {
			var url = "<%=basePath%>normal/adminC2cOrderAction!getOrderPayments.action";
			var data = {"order_no" : order_no};
			goAjaxUrl(url, data, function(tmp) {
					orderPayments = tmp.payments;
					
					if (null == tmp || '' == tmp) {
						$("#user_payment_method").hide();
					} else {
						
						var content = '';
						for (var i = 0; i < tmp.payments.length; i++) {
							content += '<option value="' + tmp.payments[i].methodConfigId + '" <c:if test="${payment_method == ' + tmp.payments[i].methodConfigId 
								+ '}">selected="true"</c:if> >' + tmp.payments[i].methodName + '【' + tmp.payments[i].paramValue1 + '】' + '</option>';
						}					
						$("#payment_method").html(content);
						
						setPayment(tmp.payments[0])
						$("#user_payment_method").show();
					}
				}, function() {
			});
		}
		
		function updatePayment() {
			var methodConfigId = $("#payment_method").val();
			for (var i = 0; i < orderPayments.length; i++) {
				if (orderPayments[i].methodConfigId == methodConfigId) {
					setPayment(orderPayments[i]);
					break;
				}
			}
		}
		
		function setPayment(payment) {

			var mci = payment.methodConfigId;
			var pid = payment.id;
			if (null != mci && '' != mci && null != pid && '' != pid) {
				$("#payment_method").val(mci);
				$("#payment_method_id_order_pay").val(pid);
			}
				
			$("#user_modal_method_type").val(payment.methodTypeName);
			$("#user_modal_method_name").val(payment.methodName);
			document.getElementById('user_modal_method_img').src="<%=base%>wap/public/showimg!showImg.action?imagePath=" + payment.methodImg;
			$("#user_modal_real_name").val(payment.realName);
			
			$("#user_modal_param_name1").html(payment.paramName1);
			$("#user_modal_param_value1").val(payment.paramValue1);
			
			if (null != payment.paramName2 && "" != payment.paramName2) { $("#modal_param_name2").html(payment.paramName2); 
				$("#modal_param_value2").val(payment.paramValue2); $("#user_param2").show(); } else { $("#user_param2").hide(); }
			if (null != payment.paramName3 && "" != payment.paramName3) { $("#modal_param_name3").html(payment.paramName3); 
				$("#modal_param_value3").val(payment.paramValue3); $("#user_param3").show(); } else { $("#user_param3").hide(); }
			if (null != payment.paramName4 && "" != payment.paramName4) { $("#modal_param_name4").html(payment.paramName4); 
				$("#modal_param_value4").val(payment.paramValue4); $("#user_param4").show(); } else { $("#user_param4").hide(); }
			if (null != payment.paramName5 && "" != payment.paramName5) { $("#modal_param_name5").html(payment.paramName5); 
				$("#modal_param_value5").val(payment.paramValue5); $("#user_param5").show(); } else { $("#user_param5").hide(); }
			if (null != payment.paramName6 && "" != payment.paramName6) { $("#modal_param_name6").html(payment.paramName6); 
				$("#modal_param_value6").val(payment.paramValue6); $("#user_param6").show(); } else { $("#user_param6").hide(); }
			if (null != payment.paramName7 && "" != payment.paramName7) { $("#modal_param_name7").html(payment.paramName7); 
				$("#modal_param_value7").val(payment.paramValue7); $("#user_param7").show(); } else { $("#user_param7").hide(); }
			if (null != payment.paramName8 && "" != payment.paramName8) { $("#modal_param_name8").html(payment.paramName8); 
				$("#modal_param_value8").val(payment.paramValue8); $("#user_param8").show(); } else { $("#user_param8").hide(); }
			if (null != payment.paramName9 && "" != payment.paramName9) { $("#modal_param_name9").html(payment.paramName9); 
				$("#modal_param_value9").val(payment.paramValue9); $("#user_param9").show(); } else { $("#user_param9").hide(); }
			if (null != payment.paramName10 && "" != payment.paramName10) { $("#modal_param_name10").html(payment.paramName10); 
				$("#modal_param_value10").val(payment.paramValue10); $("#user_param10").show(); } else { $("#user_param10").hide(); }
			if (null != payment.paramName11 && "" != payment.paramName11) { $("#modal_param_name11").html(payment.paramName11); 
				$("#modal_param_value11").val(payment.paramValue11); $("#user_param11").show(); } else { $("#user_param11").hide(); }
			if (null != payment.paramName12 && "" != payment.paramName12) { $("#modal_param_name12").html(payment.paramName12); 
				$("#modal_param_value12").val(payment.paramValue12); $("#user_param12").show(); } else { $("#user_param12").hide(); }
			if (null != payment.paramName13 && "" != payment.paramName13) { $("#modal_param_name13").html(payment.paramName13); 
				$("#modal_param_value13").val(payment.paramValue13); $("#user_param13").show(); } else { $("#user_param13").hide(); }
			if (null != payment.paramName14 && "" != payment.paramName14) { $("#modal_param_name14").html(payment.paramName14); 
				$("#modal_param_value14").val(payment.paramValue14); $("#user_param14").show(); } else { $("#user_param14").hide(); }
			if (null != payment.paramName15 && "" != payment.paramName15) { $("#modal_param_name15").html(payment.paramName15); 
				$("#modal_param_value15").val(payment.paramValue15); $("#user_param15").show(); } else { $("#user_param15").hide(); }
			if (null != payment.qrcode && "" != payment.qrcode) {
				document.getElementById('user_modal_qrcode').src="<%=base%>wap/public/showimg!showImg.action?imagePath=" + payment.qrcode;
				$("#user_payment_method_qrcode").show(); } else { $("#user_payment_method_qrcode").hide(); }
		}
		
		function goAjaxUrl(targetUrl, data, Func, Fail) {
			$.ajax({
				url : targetUrl,
				data : data,
				type : 'get',
				dataType : "json",
				success: function (res) {
				    if (res.code == 200) {
				    	Func(res);
				    } else if (res.code == 500) {
				    	Fail();
				    	swal({
							title : res.message,
							text : "",
							type : "warning",
							showCancelButton : true,
							confirmButtonColor : "#DD6B55",
							confirmButtonText : "确认",
							closeOnConfirm : false
						});
				    }
				},
				error : function (XMLHttpRequest, textStatus, errorThrown) {
					console.log("请求错误");
				}
			});
		}
		
		function goNewAjaxUrl(targetUrl,data,Func,Fail){
					$.ajax({
						url:targetUrl,
						data:data,
						type : 'get',
						dataType : "json",
						success: function (res) {
							// var tmp = $.parseJSON(res)
							var tmp = res
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
