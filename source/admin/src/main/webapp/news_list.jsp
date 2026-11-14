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
			<h3>新闻管理</h3>
			
			<%@ include file="include/alert.jsp"%>
			
			<!-- //////////////////////////////////////////////////////////////////////////// -->
			<!-- START queryForm -->
			<div class="row">
				<div class="col-md-12">
					<div class="panel panel-default">

						<div class="panel-title">查询条件</div>
						<div class="panel-body">
						
							<form class="form-horizontal" action="<%=basePath%>normal/adminNewsAction!list.action" 
								method="post" id="queryForm">
								
								<input type="hidden" name="pageNo" id="pageNo" value="${pageNo}" />
								
								<div class="col-md-12 col-lg-4">
									<fieldset>
										<div class="control-group">
											<div class="controls">
											<input id="title_para" name="title_para"
													class="form-control " placeholder="标题" value="${title_para}"/>
											</div>
										</div>
									</fieldset>
								</div>
								
								<div class="col-md-12 col-lg-4">
									<fieldset>
										<div class="control-group">
											<div class="controls">
												<select id="language_para" name="language_para" class="form-control " >
													<option value="">所有语言</option>
													<c:forEach items="${languageMap}" var="item">
														<option value="${item.key}" <c:if test="${language_para == item.key}">selected="true"</c:if> >${item.value}</option>
													</c:forEach>
												</select>
											</div>
										</div>
									</fieldset>
								</div>
									
								<div class="col-md-12 col-lg-4">
									<fieldset>
										<div class="control-group">
											<div class="controls">
												<input id="name_para" name="name_para"
													class="form-control " placeholder="用户名(钱包地址)、UID" value="${name_para}" />
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
								|| security.isResourceAccessible('OP_NEWS_OPERATE')}">
					
						<a href="<%=basePath%>normal/adminNewsAction!toAdd.action" class="btn btn-light" style="margin-bottom: 10px">
							<i class="fa fa-pencil"></i>新增新闻</a>
								
					</c:if>
						
					<div class="panel-body">
					
						<table class="table table-bordered table-striped">
						
							<thead>
								<tr>
								
									<td>用户名</td>
									<td>玩家/代理UID（空是全局）</td>
									<td>账户类型</td>
									<td>推荐人</td>									
									<td>标题</td>
									<td>图片</td>
									<td>图片跳转链接</td>
									<td>可否点击</td>
									<td>是否弹出</td>
									<td>滚动新闻</td>
									<!-- <td>置顶类型</td> -->
									<td>开始时间</td>
									<td>结束时间</td>																		
									<td>创建时间</td> 
									<td class="col-md-1">语言</td>
									
									<c:if test="${security.isRolesAccessible('ROLE_ROOT,ROLE_ADMIN')
												 || security.isResourceAccessible('OP_NEWS_OPERATE')}">
										
										<td class="col-md-2"></td>
										
									</c:if>

								</tr>
							</thead>
							
							<tbody>
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
												<c:when test="${item.rolename=='AGENTLOW'}">
													<span class="right label label-primary">${item.roleNameDesc}</span>
												</c:when>
												<c:otherwise>
												 	${item.roleNameDesc}
												</c:otherwise>	
											</c:choose>
										</td>
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
										
										<td>${item.title}</td>
										
										<td>
											<img width="40px" height="40px" src="<%=base%>wap/public/showimg!showImg.action?imagePath=${item.img_url}" />
										</td>
										<td>${item.img_jump_url}</td>
										<td>
											<c:if test="${item.click==1}">
												<span class="right label label-success">
													是
												</span>
											</c:if>
											<c:if test="${item.click!=1}">
												<span class="right label label-danger">
													否
												</span>
											</c:if>
										</td>
										<td>
											<c:if test="${item.pop_up==1}">
												<span class="right label label-success">
													是
												</span>
											</c:if>
											<c:if test="${item.pop_up!=1}">
												<span class="right label label-danger">
													否
												</span>
											</c:if>
										</td>										
										<td>
											<c:if test="${item.index_top=='Y'}">
												<span class="right label label-success">
													是
												</span>
											</c:if>
											<c:if test="${item.index_top=='N'}">
												<span class="right label label-danger">
													否
												</span>
											</c:if>										
										</td>
										<%-- <td>
											<c:if test="${item.index_top_type=='Y'}">
												<span class="right label label-success">
													文字类型
												</span>
											</c:if>
											<c:if test="${item.index_top_type=='N'}">
												图片类型
											</c:if>										
										</td> --%>
										<td>${item.start_time}</td>
										<td>${item.end_time}</td>
										<td>${item.create_time}</td>
										<td class="col-md-2">
											<c:if test="${item.language=='zh-CN'}">
												简体中文
											</c:if>
											<c:if test="${item.language=='CN'}">
												繁体中文
											</c:if>
											<c:if test="${item.language=='en'}">
												英语
											</c:if>
											<c:if test="${item.language=='ru'}">
												俄文
											</c:if>
											<c:if test="${item.language=='Japanese'}">
												日文
											</c:if>
											<c:if test="${item.language=='Korean'}">
												韩文
											</c:if>
											<c:if test="${item.language=='pt'}">
												葡萄牙语
											</c:if>
											<c:if test="${item.language=='es'}">
												西班牙语
											</c:if>
											<c:if test="${item.language=='th'}">
												泰语
											</c:if>
											<c:if test="${item.language=='fr'}">
												法语
											</c:if>
										</td>
										
										<td>
									
											<c:if test="${security.isRolesAccessible('ROLE_ROOT,ROLE_ADMIN')
														 || security.isResourceAccessible('OP_NEWS_OPERATE')}">
												
												<div class="btn-group">
													<button type="button" class="btn btn-light">操作</button>
													<button type="button"
														class="btn btn-light dropdown-toggle"
														data-toggle="dropdown" aria-expanded="false">
														<span class="caret"></span> <span class="sr-only">Toggle Dropdown</span>
													</button>
													<ul class="dropdown-menu" role="menu">
													
														<li><a href="<%=basePath%>normal/adminNewsAction!toUpdate.action?id=${item.id}">修改</a></li>
														<li><a href="javascript:ondelete('${item.id}')">删除</a></li>
	
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

	<%@ include file="include/js.jsp"%>
															
	<form action="<%=basePath%>normal/adminNewsAction!delete.action"
		method="post" id="ondelete">
		
		<input type="hidden" name="pageNo" id="pageNo" value="${pageNo}" />			
		<input type="hidden" name="id" id="news_id" value="${id}" />
		
		<div class="col-sm-1">
			<!-- 模态框（Modal） -->
			<div class="modal fade" id="modal_succeeded" tabindex="-1"
				role="dialog" aria-labelledby="myModalLabel"
				aria-hidden="true">
				<div class="modal-dialog">
					<div class="modal-content" >
					
						<div class="modal-header">
							<button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
							<h4 class="modal-title" id="myModalLabel">确认删除</h4>
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
	
	<script type="text/javascript">
	
		function getallname(name){
			$("#usernallName").html(name);
			$("#user_all_name_a").attr("href","https://etherscan.io/address/"+name);
			$("#net_form").modal("show");
		}
		
		function ondelete(id) {				
			$("#news_id").val(id);
			$('#modal_succeeded').modal("show");
		}
		
	</script>

</body>

</html>
