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
			<h3>C2C广告配置</h3>
			
			<%@ include file="include/alert.jsp"%>
			
			<!-- //////////////////////////////////////////////////////////////////////////// -->
			<!-- START queryForm -->
			<div class="row">
				<div class="col-md-12">
					<div class="panel panel-default">

						<div class="panel-title">查询条件</div>
						<div class="panel-body">
						
							<form class="form-horizontal" action="<%=basePath%>normal/adminC2cAdvertAction!list.action" 
								method="post" id="queryForm">
								
								<input type="hidden" name="pageNo" id="pageNo" value="${pageNo}" />
								
								<div class="col-md-12 col-lg-3">
									<fieldset>
										<div class="control-group">
											<div class="controls">
											<input id="c2c_user_code_para" name="c2c_user_code_para"
												class="form-control " placeholder="承兑商昵称、承兑商UID" value="${c2c_user_code_para}"/>
											</div>
										</div>
									</fieldset>
								</div>
								
								<div class="col-md-12 col-lg-3">
									<fieldset>
										<div class="control-group">
											<div class="controls">
											<input id="user_code_para" name="user_code_para" 
												class="form-control " placeholder="用户名、用户UID" value="${user_code_para}"/>
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
								
								<div class="col-md-12 col-lg-3">
									<fieldset>
										<div class="control-group">
											<div class="controls">
												<select id="direction_para" name="direction_para" class="form-control " >
													<option value="">所有买卖方式</option>
													<option value="buy" <c:if test="${direction_para == 'buy'}">selected="true"</c:if> >买入</option>
													<option value="sell" <c:if test="${direction_para == 'sell'}">selected="true"</c:if> >卖出</option>
												</select>
											</div>
										</div>
									</fieldset>
								</div>
								
								<div class="col-md-12 col-lg-3" style="margin-top: 10px;">
									<fieldset>
										<div class="control-group">
											<div class="controls">
												<select id="currency_para" name="currency_para" class="form-control " >
													<option value="">所有支付币种</option>
													<c:forEach items="${currencyMap}" var="item">
														<option value="${item.key}" <c:if test="${currency_para == item.key}">selected="true"</c:if> >${item.value}</option>
													</c:forEach>
												</select>
											</div>
										</div>
									</fieldset>
								</div>
								
								<div class="col-md-12 col-lg-3" style="margin-top: 10px;">
									<fieldset>
										<div class="control-group">
											<div class="controls">
												<select id="symbol_para" name="symbol_para" class="form-control " >
													<option value="">所有上架币种</option>
													<c:forEach items="${symbolMap}" var="item">
														<option value="${item.key}" <c:if test="${symbol_para == item.key}">selected="true"</c:if> >${item.value}</option>
													</c:forEach>
												</select>
											</div>
										</div>
									</fieldset>
								</div>
								
								<div class="col-md-12 col-lg-2" style="margin-top: 10px;">
									<button type="submit" class="btn  btn-block btn-light">确定</button>
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
								|| security.isResourceAccessible('OP_C2C_ADVERT_OPERATE')}">
					
						<a href="<%=basePath%>normal/adminC2cAdvertAction!toAdd.action" class="btn btn-light" style="margin-bottom: 10px">
							<i class="fa fa-pencil"></i>新增C2C广告</a>
								
					</c:if>
						
					<div class="panel-body">
					
						<table class="table table-bordered table-striped">
						
							<thead>
								<tr>
								
									<td>承兑商昵称</td>
									<td>承兑商头像</td>
									<td>承兑商UID</td>
									<td>承兑商类型</td>
									<td>用户名</td>
									<td>用户UID</td>
									<td>买卖方式</td>
									<td>支付币种</td>
									<td>上架币种</td>
									<td>支付方式</td>
									<td>币种单价</td>
									<td>币种数量</td>
									<td>单笔订单限额</td>
									<!-- <td>保证金<br />/剩余</td> -->
									<td>保证金</td>
									<td>是否上架</td>
									<!-- <td>是否关闭</td> -->									
									<td>创建时间</td>	
									<td>更多信息</td>
									
									<c:if test="${security.isRolesAccessible('ROLE_ROOT,ROLE_ADMIN')
												 || security.isResourceAccessible('OP_C2C_ADVERT_OPERATE')}">
										
										<td width="130px"></td>
										
									</c:if>

								</tr>
							</thead>
							
							<tbody>
								<c:forEach items="${page.getElements()}" var="item" varStatus="stat">
									<tr>										
										
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
										<td>
											<c:if test="${item.direction=='buy'}">
												<span class="right label label-success">
													买入
												</span>
											</c:if>
											<c:if test="${item.direction=='sell'}">
												<span class="right label label-danger">
													卖出
												</span>
											</c:if>										
										</td>
										<td>${item.currency}</td>
										<td>${item.symbol.toUpperCase()}</td>
										<td>${item.pay_type}</td>
										<td>${item.symbol_value}</td>
										<td>${item.coin_amount}</td>
										<td>${item.investment_min}<br />-${item.investment_max}</td>
										<%-- <td><fmt:formatNumber value="${item.deposit_open}" pattern="#0.00" /><br />/<fmt:formatNumber value="${item.deposit}" pattern="#0.00" /> USDT</td> --%>
										<td><fmt:formatNumber value="${item.deposit}" pattern="#0.00" /></td>
										<td>
											<c:if test="${item.on_sale=='1'}">
												<span class="right label label-success">
													是
												</span>
											</c:if>
											<c:if test="${item.on_sale=='0'}">
												<span class="right label label-danger">
													否
												</span>
											</c:if>										
										</td>
										<%-- <td>
											<c:if test="${item.closed=='1'}">
												<span class="right label label-success">
													是
												</span>
											</c:if>
											<c:if test="${item.closed=='0'}">
												<span class="right label label-danger">
													否
												</span>
											</c:if>										
										</td> --%>
										<td>${item.create_time}</td>							
										<td>
											<a href="#" onClick="moreInfo(
												'${item.pay_rate}',
												'${item.symbol_close}',
												'${item.expire_time}',
												'${item.sort_index}',
												'${item.transaction_terms}',
												'${item.order_msg}',
												'${item.remark}',
												'${item.update_time}')">
												 点击查看
											</a>
										</td>
										
										<td>
											
											<c:if test="${security.isRolesAccessible('ROLE_ROOT,ROLE_ADMIN')
														 || security.isResourceAccessible('OP_C2C_ADVERT_OPERATE')}">
												
												<div class="btn-group">
													<button type="button" class="btn btn-light">操作</button>
													<button type="button"
														class="btn btn-light dropdown-toggle"
														data-toggle="dropdown" aria-expanded="false">
														<span class="caret"></span> <span class="sr-only">Toggle Dropdown</span>
													</button>
													<ul class="dropdown-menu" role="menu">
													
														<li><a href="<%=basePath%>normal/adminC2cAdvertAction!toUpdate.action?id=${item.id}">修改</a></li>
														<%-- <li><a href="javascript:onclose('${item.id}')">关闭</a></li> --%>
														<li><a href="javascript:backAllDeposit('${item.id}')">退还所有保证金</a></li>
	
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
	
		<!-- 模态框（Modal） -->
		<div class="modal fade" id="modal_more_info" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
			<div class="modal-dialog">
				<div class="modal-content" style="width: 725px;">
				
					<div class="modal-header">
						<button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
						<h4 class="modal-title">更多信息</h4>
					</div>
					
					<div class="modal-body col-md-12" style="display: none"> <div class="form-group"> <label class="col-sm-2 control-label form-label">支付比率</label>
							<div class="col-sm-10"> <input id="modal_pay_rate" class="form-control" readonly="readonly" /> </div> </div> </div>
					<div class="modal-body col-md-12"> <div class="form-group"> <label class="col-sm-2 control-label form-label">上架币种实时行情价</label>
							<div class="col-sm-10"> <input id="modal_symbol_close" class="form-control" readonly="readonly" /> </div> </div> </div>
					<div class="modal-body col-md-12"> <div class="form-group"> <label class="col-sm-2 control-label form-label">更新时间</label>
							<div class="col-sm-10"> <input id="modal_update_time" class="form-control" readonly="readonly" /> </div> </div> </div>
					<div class="modal-body col-md-12"> <div class="form-group"> <label class="col-sm-2 control-label form-label">支付时效</label>
							<div class="col-sm-10"> <input id="modal_expire_time" class="form-control" readonly="readonly" /> </div> </div> </div>
					<div class="modal-body col-md-12"> <div class="form-group"> <label class="col-sm-2 control-label form-label">排序索引</label>
							<div class="col-sm-10"> <input id="modal_sort_index" class="form-control" readonly="readonly" /> </div> </div> </div>
					<div class="modal-body col-md-12"> <div class="form-group"> <label class="col-sm-2 control-label form-label">备注</label>
							<div class="col-sm-10"> <input id="modal_remark" class="form-control" readonly="readonly" /> </div> </div> </div>
					
					<div class="modal-body col-md-12">
						<div class="form-group">
							<label class="col-sm-2 control-label form-label">交易条款</label>
							<div class="col-sm-10">
								<textarea id="modal_transaction_terms" class="form-control input-lg" readonly="readonly" rows="4" cols="20" ></textarea>
							</div>
						</div>
					</div>
					
					<div class="modal-body col-md-12" style="display: none">
						<div class="form-group">
							<label class="col-sm-2 control-label form-label">订单自动消息</label>
							<div class="col-sm-10">
								<textarea id="modal_order_msg" class="form-control input-lg" readonly="readonly" rows="4" cols="20" ></textarea>
							</div>
						</div>
					</div>
					
					<div class="modal-footer" style="margin-top: 0;">
						<button type="button" class="btn " data-dismiss="modal">关闭</button>
					</div>
					
				</div>
			</div>
		</div>

		<%@ include file="include/footer.jsp"%>

	</div>
	<!-- End Content -->
	<!-- //////////////////////////////////////////////////////////////////////////// -->

	<%@ include file="include/js.jsp"%>
															
	<form action="<%=basePath%>normal/adminC2cAdvertAction!close.action" method="post" id="ondelete">
		
		<input type="hidden" name="pageNo" id="pageNo" value="${pageNo}" />			
		<input type="hidden" name="id" id="advert_id" value="${id}" />
		
		<div class="col-sm-1">
			<!-- 模态框（Modal） -->
			<div class="modal fade" id="modal_succeeded" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
				<div class="modal-dialog">
					<div class="modal-content" >
					
						<div class="modal-header">
							<button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
							<h4 class="modal-title" id="myModalLabel">确认关闭</h4>
						</div>
						
						<div class="modal-body">
							<div class="form-group" >
								<label for="input002" class="col-sm-3 control-label form-label">登录人资金密码</label>
								<div class="col-sm-4">
									<input id="login_safeword" type="password" name="login_safeword"
										class="login_safeword" placeholder="请输入登录人资金密码" >
								</div>
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
	
	<form action="<%=basePath%>normal/adminC2cAdvertAction!backAllDeposit.action" method="post" id="backAllDeposit">
		
		<input type="hidden" name="id" id="advert_id_back" value="${id}" />
		
		<div class="col-sm-1">
			<!-- 模态框（Modal） -->
			<div class="modal fade" id="modal_succeeded_back" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
				<div class="modal-dialog">
					<div class="modal-content" >
					
						<div class="modal-header">
							<button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
							<h4 class="modal-title" id="myModalLabel">确认 退还所有保证金</h4>
						</div>
						
						<div class="modal-body">
							<div class="form-group" >
								<label for="input002" class="col-sm-3 control-label form-label">登录人资金密码</label>
								<div class="col-sm-4">
									<input id="login_safeword" type="password" name="login_safeword"
										class="login_safeword" placeholder="请输入登录人资金密码" >
								</div>
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
	
	<script type="text/javascript">
	
		function moreInfo(pay_rate,symbol_close,expire_time, sort_index, transaction_terms, order_msg, remark, update_time) {
			$("#modal_pay_rate").val(pay_rate + " %");
			$("#modal_symbol_close").val(symbol_close + " USDT");
			$("#modal_expire_time").val(expire_time + "分钟");
			$("#modal_sort_index").val(sort_index);
			$("#modal_transaction_terms").val(transaction_terms);
			$("#modal_order_msg").val(order_msg);
			$("#modal_remark").val(remark);
			$("#modal_update_time").val(update_time);
			$('#modal_more_info').modal("show");
		}
		
		function onclose(id) {
			$("#advert_id").val(id);
			$('#modal_succeeded').modal("show");
		}
		
		function backAllDeposit(id) {
			$("#advert_id_back").val(id);
			$('#modal_succeeded_back').modal("show");
		}
		
	</script>

</body>

</html>
