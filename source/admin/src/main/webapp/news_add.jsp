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
			<h3>新闻管理</h3>
			
			<%@ include file="include/alert.jsp"%>
			
			<!-- //////////////////////////////////////////////////////////////////////////// -->
			<!-- START queryForm -->
			<form action="<%=basePath%>normal/adminNewsAction!list.action"
				method="post" id="queryForm">
				<input type="hidden" name="pageNo" id="pageNo" value="${pageNo}" />
			</form>
			<!-- END queryForm -->
			<!-- //////////////////////////////////////////////////////////////////////////// -->
			
			<div class="row">
				<div class="col-md-12 col-lg-12">
					<div class="panel panel-default">

						<div class="panel-title">
							添加新闻
							<ul class="panel-tools">
								<li><a class="icon minimise-tool"><i class="fa fa-minus"></i></a></li>
								<li><a class="icon expand-tool"><i class="fa fa-expand"></i></a></li>
							</ul>
						</div>

						<div class="panel-body">
							
							<form class="form-horizontal" action="<%=basePath%>normal/adminNewsAction!add.action"
								method="post" name="mainForm" id="mainForm">
								
								<input type="hidden" name="id" id="id" value="${id}" />
								<input type="hidden" name="img_url" id="img_url" value="${img_url}" >
								
								<div class="form-group">
									<label class="col-sm-1 control-label form-label">玩家/代理UID（空是全局）</label>
									<div class="col-sm-10">
										<input id="usercode" name="usercode" class="form-control " placeholder="请输入UID" value="${usercode}" />
									</div>
								</div>
								
								<div class="form-group">
									<label class="col-sm-1 control-label form-label">标题</label>
									<div class="col-sm-10">
										<input id="title" name="title" class="form-control " placeholder="标题" value="${title}" />
									</div>
								</div>
								
								<div class="form-group">
									<label class="col-sm-1 control-label form-label">图片</label>
									<div class="col-sm-3">
										<input type="file" id="fileName_img" name="fileName_img" value="${fileName_img}"
											onchange="upload_img();" style="position: absolute; opacity: 0;"> 
										<label for="fileName_img"> 
											<img width="90px" height="90px" id="show_img_content" src="<%=base%>/image/add.png" alt="点击上传图片" />
										</label>
									</div>
								</div>
								
								<div class="form-group">
									<label class="col-sm-1 control-label form-label">图片跳转链接</label>
									<div class="col-sm-10">
										<input id="img_jump_url" name="img_jump_url" class="form-control " placeholder="图片跳转链接" value="${img_jump_url}" />
									</div>
								</div>
																
								<div class="form-group">
									<label class="col-sm-1 control-label form-label">可否点击</label>
									<div class="col-sm-3">
										<div class="input-group">
											<select id="click" name="click" class="form-control " >
												<option value="0" <c:if test="${click == '0'}">selected="true"</c:if> >否</option>
												<option value="1" <c:if test="${click == '1'}">selected="true"</c:if> >是</option>
											</select>											
										</div>
									</div>
								</div>
								
								<div class="form-group">
									<label class="col-sm-1 control-label form-label"></label>
									<div class="col-sm-10">
										<p class="ballon color1">
											【是否弹出】和【滚动新闻】只能配一个为是，否则新闻将不显示
										</p>
									</div>
								</div>
								
								<div class="form-group">
									<label class="col-sm-1 control-label form-label">是否弹出</label>
									<div class="col-sm-3">
										<div class="input-group">
											<select id=pop_up name="pop_up" class="form-control " >
												<option value="0" <c:if test="${pop_up == '0'}">selected="true"</c:if> >否</option>
												<option value="1" <c:if test="${pop_up == '1'}">selected="true"</c:if> >是</option>
											</select>
										</div>
									</div>
								</div>	
								
								<div class="form-group">
									<label class="col-sm-1 control-label form-label">滚动新闻</label>
									<div class="col-sm-3">
										<div class="input-group">
											<select id="index" name="index" class="form-control " >
												<option value="true" <c:if test="${index == 'true'}">selected="true"</c:if> >是</option>
												<option value="false" <c:if test="${index == 'false'}">selected="true"</c:if> >否</option>
											</select>
										</div>
									</div>
								</div>							
								
								<div class="form-group">
									<label class="col-sm-1 control-label form-label">语言</label>
									<div class="col-sm-2">
										<select id="language" name="language" class="form-control " >
											<c:forEach items="${languageMap}" var="item">
												<option value="${item.key}" <c:if test="${language == item.key}">selected="true"</c:if> >${item.value}</option>
											</c:forEach>
										</select>
									</div>
								</div>
								
								<div class="form-group">
									<label class="col-sm-1 control-label form-label"></label>
									<div class="col-sm-10">
										<p class="ballon color1">
											当前时间不在【开始时间】和【结束时间】之内时，新闻将不显示
										</p>
									</div>
								</div>

								<div class="form-group">
									<label class="col-sm-1 control-label form-label">开始时间</label>
									<div class="col-sm-2">
										<input id="start_time" name="start_time" class="form-control " value="${start_time}" />
									</div>
								</div>

								<div class="form-group">
									<label class="col-sm-1 control-label form-label">结束时间</label>
									<div class="col-sm-2">
										<input id="end_time" name="end_time" class="form-control " value="${end_time}" />
									</div>
								</div>
								
								<div class="form-group">
				                  <label class="col-sm-1 control-label form-label">内容</label>
				                  <div class="col-sm-10">
				                      <textarea class="form-control" rows="7" id="content_text" name="content_text" placeholder="请输入内容...">${content}</textarea>
				                      <input type="hidden" name="content" id="content" value="${content}" />
				                  </div>
				                </div>

				                <div class="col-sm-1">
									<!-- 模态框（Modal） -->
									<div class="modal fade" id="modal_succeeded" tabindex="-1"
										role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
										<div class="modal-dialog">
											<div class="modal-content" >
											
												<div class="modal-header">
													<button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
													<h4 class="modal-title" id="myModalLabel">确认新增</h4>
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

								<div class="form-group">
									<div class="col-sm-offset-2 col-sm-10">
										<a href="javascript:goUrl(${pageNo})" class="btn">取消</a> 
										<a href="javascript:submit()" class="btn btn-default">保存</a>
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
	
		// 初始化执行一次
		setTimeout(function() {
			start();	  
		}, 100);
		
		function start() {			
			var img_url = $("#img_url").val();
			var show_img_content = document.getElementById('show_img_content');
			show_img_content.src="<%=base%>wap/public/showimg!showImg.action?imagePath="+img_url;
		}
		
		function upload_img() {
			var fileReader = new FileReader();
			var formData = new FormData();
			var file = document.getElementById('fileName_img').files[0];
			formData.append("file", file);
			$.ajax({
		    	type: "POST",
		        url: "<%=basePath%>normal/uploadimg!execute.action?random=" + Math.random(),
				data: formData,
				dataType: "json",
			    contentType: false,  
			    processData: false, 
				success : function(data) {
					$("#img_url").val(data.data)
					var show_img = document.getElementById('show_img_content');
					show_img.src="<%=base%>wap/public/showimg!showImg.action?imagePath=" + data.data;
				},
				error : function(XMLHttpRequest, textStatus, errorThrown) {
					console.log("请求错误");
				}
			});
		}
	
		function submit() {
			/* swal({
				title : "是否保存?",
				text : "",
				type : "warning",
				showCancelButton : true,
				confirmButtonColor : "#DD6B55",
				confirmButtonText : "确认",
				closeOnConfirm : false
			}, function() {				
				document.getElementById("mainForm").submit();
			}); */
			var sHTML = $('#content_text').val();
			$("#content").val(sHTML);
			$('#modal_succeeded').modal("show");
		}		
				
		$.fn.datetimepicker.dates['zh'] = {
			days : [ "星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六", "星期日" ],
			daysShort : [ "日", "一", "二", "三", "四", "五", "六", "日" ],
			daysMin : [ "日", "一", "二", "三", "四", "五", "六", "日" ],
			months : [ "一月", "二月", "三月", "四月", "五月", "六月", "七月", "八月", "九月",
					"十月", "十一月", "十二月" ],
			monthsShort : [ "一", "二", "三", "四", "五", "六", "七", "八", "九", "十",
					"十一", "十二" ],
			meridiem : [ "上午", "下午" ],
			//suffix:      ["st", "nd", "rd", "th"],  
			today : "今天",
			clear : "清空"
		};
		$(function() {
			$('#start_time').datetimepicker({
				format : 'yyyy-mm-dd hh:ii:00',
				minuteStep:1,
				language : 'zh',
				weekStart : 1,
				todayBtn : 1,
				autoclose : 1,
				todayHighlight : 1,
				startView : 2,
				clearBtn : true
			});
		});
		$(function() {
			$('#end_time').datetimepicker({
				format : 'yyyy-mm-dd hh:ii:00',
				minuteStep:1,
				language : 'zh',
				weekStart : 1,
				todayBtn : 1,
				autoclose : 1,
				todayHighlight : 1,
				startView : 2,
				clearBtn : true
			});
		});
		
	</script>

</body>

</html>
