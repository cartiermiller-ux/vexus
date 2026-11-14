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

<script type="text/javascript">
	$.fn.datetimepicker.dates['zh'] = {
		days : [ "星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六", "星期日" ],
		daysShort : [ "日", "一", "二", "三", "四", "五", "六", "日" ],
		daysMin : [ "日", "一", "二", "三", "四", "五", "六", "日" ],
		months : [ "一月", "二月", "三月", "四月", "五月", "六月", "七月", "八月", "九月","十月", "十一月", "十二月" ],
		monthsShort : [ "一", "二", "三", "四", "五", "六", "七", "八", "九", "十","十一", "十二" ],
		meridiem : [ "上午", "下午" ],
		//suffix:      ["st", "nd", "rd", "th"],
		today : "今天",
		clear : "清空"
	};
	
	$(function() {
		$('#createTime').datetimepicker({
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
	
	//修改订单
	function modifyOrder(){
		var mainForm=document.getElementById('mainForm');
		mainForm.submit();
	}
	
	//值变时上传图片
	function upload_idimg(img_id){
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
				$("form#mainForm>#img_idimg_" + img_id).val(data.data)
				var show_img = document.getElementById('modal_idimg_' + img_id);
				show_img.src="<%=base%>wap/public/showimg!showImg.action?imagePath="+data.data;
			},
			error : function(XMLHttpRequest, textStatus, errorThrown) {
				console.log("请求错误");
			}
		});
	}
</script>
</head>
<body>
	<%@ include file="include/loading.jsp"%>
	<div class="ifr-dody">
		<div class="ifr-con">
			<h3>用户借贷</h3>
			<%@ include file="include/alert.jsp"%>
			<div class="row">
				<div class="col-md-12 col-lg-12">
					<div class="panel panel-default">
						<div class="panel-title">
							修改借贷申请单
							<ul class="panel-tools">
								<li><a class="icon minimise-tool"><i class="fa fa-minus"></i></a></li>
								<li><a class="icon expand-tool"><i class="fa fa-expand"></i></a></li>
							</ul>
						</div>
						
						<div class="panel-body">
							<form id="mainForm" name="mainForm" class="form-horizontal" action="<%=basePath%>normal/loanadmin!modify.action" method="post">
								<input type="hidden" name="orderNo" id="orderNo" value = "${order.id}"/>
								<input type="hidden" name="img_idimg_1" id="img_idimg_1"/>
								<input type="hidden" name="img_idimg_2" id="img_idimg_2"/>
								<input type="hidden" name="img_idimg_3" id="img_idimg_3"/>
								
								<div class="form-group">
									<label class="col-sm-2 control-label form-label">用户账号</label>
									<div class="col-sm-3">
										<input id="partyId" name="partyId" class="form-control" readonly="readonly"  value="${order.username}"/>
									</div>
								</div>
								
								<div class="form-group">
									<label class="col-sm-2 control-label form-label">借贷币种(*)</label>
									<div class="col-sm-3">
										<input id="symbol" name="symbol" class="form-control " value="${order.symbol}" />
									</div>
								</div>
								
								<div class="form-group">
									<label class="col-sm-2 control-label form-label">借贷额度(*)</label>
									<div class="col-sm-3">
										<input id="quota" name="quota" class="form-control " value="${order.quota}" />
									</div>
								</div>
								
								<div class="form-group">
									<label class="col-sm-2 control-label form-label">审核状态</label>
									<div class="col-sm-3">
										<select id="state" name="state" class="form-control " >
											<c:forEach items="${paramMap.stateList}" var="item">
												<option value="${item[0]}" <c:if test="${order.state==item[0]}">selected="true"</c:if> >${item[1]}</option>
											</c:forEach>
										</select>
									</div>
								</div>
								
								<div class="form-group">
									<label class="col-sm-2 control-label form-label">借贷期限(天)</label>
									<div class="col-sm-3">
										<select id="term" name="term" class="form-control " >
											<c:forEach items="${paramMap.termList}" var="item">
												<option value="${item[0]}" <c:if test="${order.term==item[0]}">selected="true"</c:if> >${item[1]}</option>
											</c:forEach>
										</select>
									</div>
								</div>
								
								<div class="form-group">
									<label class="col-sm-2 control-label form-label">申请时间</label>
									<div class="col-sm-3">
										<input id="createTime" name="createTime" class="form-control " value="${order.createTime}" />
									</div>
								</div>
								
								<div class="form-group">
									<label class="col-sm-2 control-label form-label">还款周期(天)</label>
									<div class="col-sm-3">
										<select id="repayCycle" name="repayCycle" class="form-control " >
											<c:forEach items="${paramMap.repayCycleList}" var="item">
												<option value="${item[0]}" <c:if test="${order.repayCycle==item[0]}">selected="true"</c:if> >${item[1]}</option>
											</c:forEach>
										</select>
									</div>
								</div>
								
								<div class="form-group">
									<label class="col-sm-2 control-label form-label">日利率(%)</label>
									<div class="col-sm-3">
										<select id="dailyRate" name="dailyRate" class="form-control " >
											<c:forEach items="${paramMap.dailyRateList}" var="item">
												<option value="${item[0]}" <c:if test="${order.dailyRate==item[0]}">selected="true"</c:if> >${item[1]}</option>
											</c:forEach>
										</select>
									</div>
								</div>
								
								<div class="form-group">
									<label class="col-sm-2 control-label form-label">放款机构</label>
									<div class="col-sm-3">
										<input id="symbol" name="symbol"  class="form-control " readonly="readonly" value="${order.lendingName}" />
									</div>
								</div>
								
								<div class="form-group">
									<label class="col-sm-2 control-label form-label">证件照片</label>
									<div class="col-sm-3">
										<div class="modal-body col-md-12" style="padding-left:0px;margin-left:0px;width:800px;">
											<div class="col-md-12 col-lg-4">
												证件正面照
												 <a href="#" target="_blank"> 
												 	<img width="200px" height="200px" id="modal_idimg_1" name="modal_idimg_1" src="<%=request.getScheme() +"://"+ request.getServerName()%>/wap/public/showimg!showImg.action?imagePath=${order.houseImgs[0]}" />
												</a>
												<div class="col-md-6">
													<input type="file" id="fileName_1" name="fileName"  value="${order.houseImgs[0]}" onchange="upload_idimg('1');" style="position:absolute;opacity:0;">	
													<label for="fileName">
														<button type="button" class="btn btn-light btn-block">修改</button>
						　　							</label>
												</div>
											</div>
											<div class="col-md-12 col-lg-4">
												证件背面照
												<a href="#" target="_blank"> 
													<img width="200px" height="200px" id="modal_idimg_2" name="modal_idimg_2" src="<%=request.getScheme() +"://"+ request.getServerName()%>/wap/public/showimg!showImg.action?imagePath=${order.houseImgs[1]}" />
												</a>
												<div class="col-md-6">
													<input type="file" id="fileName_2" name="fileName"  value="${order.houseImgs[1]}" onchange="upload_idimg('2');" style="position:absolute;opacity:0;">	
													<label for="fileName">
														<button type="button" class="btn btn-light btn-block">修改</button>
						　　							</label>
												</div>
											</div>
											<div class="col-md-12 col-lg-4">
												手持正面照
												<a href="#" target="_blank"> 
													<img width="200px" height="200px" id="modal_idimg_3" name="modal_idimg_3" src="<%=request.getScheme() +"://"+ request.getServerName()%>/wap/public/showimg!showImg.action?imagePath=${order.houseImgs[2]}" />
												</a>
												<div class="col-md-6">
													<input type="file" id="fileName_3" name="fileName"  value="${order.houseImgs[2]}" onchange="upload_idimg('3');" style="position:absolute;opacity:0;">	
													<label for="fileName">
														<button type="button" class="btn btn-light btn-block">修改</button>
						　　							</label>
												</div>
											</div>
										</div>
									</div>
								</div>
								
								<!--
								<div class="form-group">
									<label class="col-sm-2 control-label form-label">收入证明</label>
									<div class="col-sm-3">
										<input id="incomeImg" name="incomeImg" class="form-control " value="${order.incomeImg}" />
									</div>
								</div>
								-->
								
								<div class="form-group">
									<div class="col-sm-offset-2 col-sm-10">
										<a href="<%=basePath%>normal/loanadmin!list.action" class="btn">返回</a> 
										<a href="javascript:modifyOrder()" class="btn btn-default">保存</a>
									</div>
								</div>

							</form>
						</div>
					</div>
				</div>
			</div>
		</div>
		<!-- END CONTAINER -->
		<%@ include file="include/footer.jsp"%>
	</div>
	<%@ include file="include/js.jsp"%>
</body>
</html>
