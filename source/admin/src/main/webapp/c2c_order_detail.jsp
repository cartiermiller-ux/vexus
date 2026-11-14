<%@ page language="java" pageEncoding="utf-8"%>
<jsp:useBean id="security" class="security.web.BaseSecurityAction" scope="page" />

<%@ include file="include/pagetop.jsp"%>

<!DOCTYPE html>
<html>

<head>

<%@ include file="include/head.jsp"%>

<style type="text/css">
/*滚动条样式*/
.otc-chat .otc-conv::-webkit-scrollbar {width: 4px;}
.otc-chat .otc-conv::-webkit-scrollbar-thumb {
	border-radius: 10px;
	-webkit-box-shadow: inset 0 0 5px rgba(0,0,0,0.2);
	background: rgba(0,0,0,0.2);
}
.otc-chat .otc-conv::-webkit-scrollbar-track {
	-webkit-box-shadow: inset 0 0 5px rgba(0,0,0,0.2);
	border-radius: 0;
	background: rgba(0,0,0,0.1);
}
.otc-chat .msg-list::-webkit-scrollbar {width: 4px;}
.otc-chat .msg-list::-webkit-scrollbar-thumb {
	border-radius: 10px;
	-webkit-box-shadow: inset 0 0 5px rgba(0,0,0,0.2);
	background: rgba(0,0,0,0.2);
}
.otc-chat .msg-list::-webkit-scrollbar-track {
	-webkit-box-shadow: inset 0 0 5px rgba(0,0,0,0.2);
	border-radius: 0;
	background: rgba(0,0,0,0.1);
}
.otc-chat .unreadhide{display: none;}
.otc-chat .item-li-on{background: #D5EAFF;}
.otc-chat .chat .otc-conv{height: calc( 100% - 223px);overflow: auto;}
.conv-loading{position:fixed;top: 175px;left:25%;right: 0;height: 100%;text-align: center;}
.conv-loading img{width: 50px;margin-top: 100px;}
.otc-chat .chat .otc-conv .ballon {
    max-width: 80%;
    word-break: break-all;
	margin: 5px 0;
	padding: 4px 20px;
	white-space: pre-line;
}
.otc-chat .chat .otc-conv .color2 {
    background: #B6DEFF;
    color: #153440;
}
.otc-chat .chat .otc-conv .date{
	border:0;
	padding: 0;
	text-align: center;
    font-size: 11px;
}
.otc-chat .chat .otc-conv li{
	margin: 0;   
	padding-left: 40px;
	display: block;
    position: relative;
}
.otc-chat .right-msg {
    text-align: right;
    padding-right: 40px;
    padding-left: 0px!important;
}
.otc-chat .right-msg .img {
    right: 0;
    left: unset!important;
}
.otc-chat .chat .otc-conv .img {
    border-radius: 999px;
    width: 30px;
    height: 30px;
    position: absolute;
    left: 0;
    top: 0;
}
.otc-chat .chat .otc-conv{height: calc( 100% - 240px);overflow: auto;padding: 0 10px;}	
.otc-chat .write {
/* 	padding: 0 20px!important; */
}
.otc-chat .chat .c2c-title h1 {
    display: inline-block;
    line-height: 10px;
}
.otc-chat .chat .c2c-title {
    height: 60px;
    padding: 12px 20px;
    background: #fff;
    border-bottom: 1px solid rgba(0, 0, 0, 0.08);
    position: relative;
}
.c2c-title .user-detail span{margin-right: 20px;}
.otc-chat{
	position: relative;
	padding: 20px 410px 0 10px;
	width: 100%;
	height: 700px;
	background-color: #ffffff;
	line-height: normal;
}
.otc-chat .mailbox-inbox{color: #404040;border: none;}
.otc-chat .chat{
	width: 360px;
	height: 645px;
	position: fixed;
	right: 20px;
	top: 5px;
	bottom: 20px;
	border: none;
	box-shadow: 0 0 7px 1px #ddd;
	border-radius: 2px;
}
.otc-w1{font-size: 22px;margin-bottom: 15px;color: #545454;padding-right: 38px;}
.otc-w1 text{font-size: 14px;}
.otc-w2{font-size: 14px;font-weight: 600;}
.otc-w2 text{font-size: 14px;font-weight: 400;color: #666666;margin-left: 50px;}
.otc-w3{font-size: 14px;font-weight: 600;}
.otc-w3 text{font-size: 14px;color: #499972;margin-left: 50px;}
.otc-w4{
	margin-top: 20px;
	margin-left: 25px;
	font-weight: 400;
	font-size: 12px;
	color: #999999;
	position: relative;
}
.otc-w4:after{
	position: absolute;
	content: '';
	left: 60px;
	right: 0;
	top: 50%;
	height: 0.5px;
	background-color: #dddddd;
}
.otc-w5{margin-top: 20px;font-size: 12px;}
.otc-w5 text{margin-right: 20px;}
.otc-w5 img{width: 50px;}
.otc-w6{
	margin-top: 20px;
	font-size: 14px;
	font-weight: 600;
	color: #e72e2e;
}
.otc-w7{
	margin-top: 20px;
	font-size: 14px;
	font-weight: 600;
	color: #499972;
}
.otc-btn{
	display: inline-block;
	padding: 0 10px;
	height: 35px;
	line-height: 35px;
	text-align: center;
	color: #ffffff;
	background-color: #638bd4;
	font-size: 13px;
	border-radius: 2px;
	margin-top: 5px;
	margin-right: 30px;
	cursor: pointer;
}
.otc-btn:hover{background-color: #759de6;}
.otc-cancel-btn{
	display: inline-block;
	padding: 0 10px;
	height: 35px;
	line-height: 35px;
	text-align: center;
	font-size: 13px;
	border-radius: 2px;
	margin-top: 5px;
	margin-right: 30px;
	cursor: pointer;
	background-color: #e4e4e4;
	color:#333333;
}
.otc-cancel-btn:hover{background-color: #eee;}
.otc-photo-add {
	position: absolute;
	right: 30px;
/* 	top: 5px; */
	width: 30px;
	height: 30px;
	cursor: pointer;
}
.otc-chat .img-show {
    width: 200px;
    cursor: pointer;
}
.btn-refresh{
	padding: 5px 10px;
	position: fixed;
	right: 400px;
	top: 5px;
	color: #638bd4;
	border: 1px solid #cccccc;
	background-color: #ffffff;
	font-size: 12px;
	border-radius: 2px;
	cursor: pointer;
}
.btn-refresh:hover{color: #759de6;}
.otc-conv .msg-back{
	cursor: pointer;
	position: absolute;
	top: 0px;
    right: 16px;
    background-color: #ffffff;
    padding: 6px;
    box-shadow: 0px 0px 3px 0 #333333;
    z-index: 10;
}
.c2c-text {
	margin-top: 5px;
	margin-left: 50px;
	font-size: 18px;
	font-weight: 600;
	color: #545454;
	margin-left: 25px;
}
.c2c-success {
	color: #5cb85c;
}
.c2c-info {
	color: #5bc0de;
}
.c2c-warning {
	color: #f0ad4e;
}
.c2c-danger {
	color: #d9534f;
}
.c2c-price {
	margin-top: 5px;
	margin-left: 25px;
	margin-right: 20px;
	font-size: 14px;
	font-weight: 600;
}
.c2c-price span {
	font-size: 14px;
	font-weight: 400;
}
.c2c-margin-left {
	margin-left: 75px;
}
.c2c-payment {
	margin-top: 15px;
	margin-right: 20px;
	font-size: 12px;
	font-weight: 600;
}
.c2c-payment-param-value {
	margin-top: 15px;
	margin-right: 20px;
	font-size: 12px;
	font-weight: 300;
}
.c2c-payment-img {
	width: 50px;	
	height: 50px;
}
.c2c-username {
	font-size: 14px;
	font-weight: 600;
}
.right-msg.color-gray{position: relative;}
.right-msg.color-gray p{background-color: #cdcbcb!important;}
.right-msg.color-gray img:nth-last-child(1){filter: grayscale(1);margin: 5px 0;}
.right-msg.color-gray:before{
	content: "↶";
	font-size: 16px;
	position: absolute;
	right: 74px;
	top: -12px;
    }
</style>

</head>

<body>

	<%@ include file="include/loading.jsp"%>

	<!-- //////////////////////////////////////////////////////////////////////////// -->
		
	<input type="hidden" value="${detail_map.c2c_user_party_id}" id="otc_partyid" />
	<input type="hidden" value="${detail_map.party_id}" id="user_party_id" />
	<input type="hidden" value="1" id="otc_pageno" />
	<input type="hidden" value="" id="otc_message_id" />
	<input type="hidden" value="${detail_map.order_no}" id="order_no" />	
	<input type="hidden" value="0" id="otc_isScroll" /><!-- 是否滚动底部 0到底/1没到底 -->
	<input type="hidden" value="1" id="otc_hasmore" /><!-- 是否有上一页 0无/1有 -->
	<input type="hidden" id="chat_token"/>
			
	<!-- START CONTENT -->
	<div class="ifr-dody">
	
		<!-- Start Mailbox -->
		<div class="ifr-con mailbox clearfix otc-chat">
		
		<%@ include file="include/alert.jsp"%>

			<!-- Start Mailbox Container -->
			<div class="">
			
				<!-- Start Mailbox Inbox -->
				<div class="padding-0">
					<ul class="mailbox-inbox">
						
						<div class="btn-refresh"><a href="<%=basePath%>normal/adminC2cOrderAction!detail.action?order_no=${detail_map.order_no}">刷新订单</a></div>
												
						<div class="c2c-text">
							<c:if test='${detail_map.direction == "buy"}'>								
								<span class="c2c-warning">${detail_map.c2c_user_nick_name}</span>  向  
								<span class="c2c-success">${detail_map.username }</span>  出售  
								<span class="c2c-danger">${detail_map.coin_amount} ${detail_map.symbol}</span>
							</c:if>							
							<c:if test='${detail_map.direction == "sell"}'>
								<span class="c2c-warning">${detail_map.username }</span>  向  
								<span class="c2c-success">${detail_map.c2c_user_nick_name}</span>  出售  
								<span class="c2c-danger">${detail_map.coin_amount} ${detail_map.symbol}</span>
							</c:if>
						</div>		
						
						<div class="c2c-price">
							单价：<span class="c2c-success">${detail_map.symbol_value} ${detail_map.currency}/${detail_map.symbol}</span>
							总价：<span class="c2c-danger">${detail_map.amount} ${detail_map.currency}</span>
						</div>
						
						<div class="otc-w4 ">订单状态</div>
						<!-- 订单状态：0未付款/1已付款/2申诉中/3已完成/4已取消/5已超时 -->
						<div class="otc_order_state c2c-margin-left">
							<c:if test='${detail_map.state == 0}'>
								<div class="otc-w6">等待付款。。。</div>
							</c:if>
							<c:if test='${detail_map.state == 1}'>
								<div class="otc-w7">已付款</div>
							</c:if>
							<c:if test='${detail_map.state == 2}'>
								<div class="otc-w6">申诉中</div>
							</c:if>
							<c:if test='${detail_map.state == 3}'>
								<div class="otc-w7">已完成</div>
							</c:if>
							<c:if test='${detail_map.state == 4}'>
								<div class="otc-w7">已取消</div>
							</c:if>
							<c:if test='${detail_map.state == 5}'>
								<div class="otc-w6">已超时</div>
							</c:if>
						</div>
						
						<div class="otc-w4">时间</div>
						<div class="otc_order_time">
							<div class="c2c-payment c2c-margin-left">
								下单时间： <span class="c2c-payment-param-value">${detail_map.create_time}</span>
								<c:if test="${detail_map.cancel_time != null}">
									取消时间： <span class="c2c-payment-param-value">${detail_map.cancel_time}</span>
								</c:if>
								<c:if test="${detail_map.pay_time != null}">
									付款时间： <span class="c2c-payment-param-value">${detail_map.pay_time}</span>
								</c:if>
								<c:if test="${detail_map.handle_time != null}">
									完成时间： <span class="c2c-payment-param-value">${detail_map.handle_time}</span>
								</c:if>
							</div>
						</div>
						
						<div class="otc-w4">订单号</div>
						<div class="otc-w5 c2c-margin-left"> <span class="right label label-danger">${detail_map.order_no}</span></div>
						
						<div class="otc-w4">操作</div>
						<c:if test="${security.isRolesAccessible('ROLE_ROOT,ROLE_ADMIN')
			 					|| security.isResourceAccessible('OP_C2C_ORDER_OPERATE')}">
							
							<div class="otc_order_opera c2c-margin-left">
								
								<c:if test="${detail_map.state != '3' && detail_map.state != '4'}">
					 				
					 				<c:if test="${detail_map.direction == 'buy'}">
										
										<c:if test="${detail_map.state == '0' || detail_map.state == '1' || detail_map.state == '2' || detail_map.state == '5'}">
											<div class="otc-cancel-btn" onclick="orderCancel('${detail_map.order_no}')">取消订单</div>
										</c:if>
										
										<c:if test="${detail_map.state == '1' || detail_map.state == '2' || detail_map.state == '5'}">
											<div class="otc-btn" onclick="succeeded('${detail_map.order_no}')">手动放行</div>
										</c:if>
																														
									</c:if>
					 				
					 				<c:if test="${detail_map.direction == 'sell'}">
										
										<c:if test="${detail_map.state == '0' || detail_map.state == '1' || detail_map.state == '2' || detail_map.state == '5'}">
											<div class="otc-cancel-btn" onclick="orderCancel('${detail_map.order_no}')">取消订单</div>
										</c:if>
															
										<c:if test="${detail_map.state == '0' || detail_map.state == '2'}">
											<div class="otc-btn" onclick="tranMoney('${detail_map.order_no}', '${detail_map.amount}',
												'${detail_map.currency}', '${detail_map.coin_amount}','${detail_map.symbol}')">手动转账</div>
										</c:if>
									
										<c:if test="${detail_map.state == '1' || detail_map.state == '2' || detail_map.state == '5'}">
											<div class="otc-btn" onclick="succeeded('${detail_map.order_no}')">已付款管理员手动放行</div>
										</c:if>
															
									</c:if>
									
								</c:if>
							
							</div>
								
						</c:if>
						
						<div class="otc-w4">收款信息</div>
						<div class="">
							
							<div class="c2c-payment c2c-margin-left">支付方式类型: <span class="c2c-payment-param-value">${detail_map.method_type_name}</span></div>
							<div class="c2c-payment c2c-margin-left">支付方式名称: <span class="c2c-payment-param-value">${detail_map.method_name}</span></div>
							<div class="c2c-payment c2c-margin-left">支付方式图片: <img id="c2c-payment-img" width="40px" height="40px" src="<%=base%>wap/public/showimg!showImg.action?imagePath=${detail_map.method_img}" /></div>
							<div class="c2c-payment c2c-margin-left">真实姓名: <span class="c2c-payment-param-value">${detail_map.real_name}</span></div>
							<div class="c2c-payment c2c-margin-left">${detail_map.param_name1}: <span class="c2c-payment-param-value">${detail_map.param_value1}</span></div>
							<c:if test='${null != detail_map.param_name2 && "" != detail_map.param_name2}'> 
								<div class="c2c-payment c2c-margin-left">${detail_map.param_name2}: <span class="c2c-payment-param-value">${detail_map.param_value2}</span></div>
							</c:if>
							<c:if test='${null != detail_map.param_name3 && "" != detail_map.param_name3}'> 
								<div class="c2c-payment c2c-margin-left">${detail_map.param_name3}: <span class="c2c-payment-param-value">${detail_map.param_value3}</span></div>
							</c:if>
							<c:if test='${null != detail_map.param_name4 && "" != detail_map.param_name4}'> 
								<div class="c2c-payment c2c-margin-left">${detail_map.param_name4}: <span class="c2c-payment-param-value">${detail_map.param_value4}</span></div>
							</c:if>
							<c:if test='${null != detail_map.param_name5 && "" != detail_map.param_name5}'> 
								<div class="c2c-payment c2c-margin-left">${detail_map.param_name5}: <span class="c2c-payment-param-value">${detail_map.param_value5}</span></div>
							</c:if>
							<c:if test='${null != detail_map.param_name6 && "" != detail_map.param_name6}'> 
								<div class="c2c-payment c2c-margin-left">${detail_map.param_name6}: <span class="c2c-payment-param-value">${detail_map.param_value6}</span></div>
							</c:if>
							<c:if test='${null != detail_map.param_name7 && "" != detail_map.param_name7}'> 
								<div class="c2c-payment c2c-margin-left">${detail_map.param_name7}: <span class="c2c-payment-param-value">${detail_map.param_value7}</span></div>
							</c:if>
							<c:if test='${null != detail_map.param_name8 && "" != detail_map.param_name8}'> 
								<div class="c2c-payment c2c-margin-left">${detail_map.param_name8}: <span class="c2c-payment-param-value">${detail_map.param_value8}</span></div>
							</c:if>
							<c:if test='${null != detail_map.param_name9 && "" != detail_map.param_name9}'> 
								<div class="c2c-payment c2c-margin-left">${detail_map.param_name9}: <span class="c2c-payment-param-value">${detail_map.param_value9}</span></div>
							</c:if>
							<c:if test='${null != detail_map.param_name10 && "" != detail_map.param_name10}'> 
								<div class="c2c-payment c2c-margin-left">${detail_map.param_name10}: <span class="c2c-payment-param-value">${detail_map.param_value10}</span></div>
							</c:if>
							<c:if test='${null != detail_map.param_name11 && "" != detail_map.param_name11}'> 
								<div class="c2c-payment c2c-margin-left">${detail_map.param_name11}: <span class="c2c-payment-param-value">${detail_map.param_value11}</span></div>
							</c:if>
							<c:if test='${null != detail_map.param_name12 && "" != detail_map.param_name12}'> 
								<div class="c2c-payment c2c-margin-left">${detail_map.param_name12}: <span class="c2c-payment-param-value">${detail_map.param_value12}</span></div>
							</c:if>
							<c:if test='${null != detail_map.param_name13 && "" != detail_map.param_name13}'> 
								<div class="c2c-payment c2c-margin-left">${detail_map.param_name13}: <span class="c2c-payment-param-value">${detail_map.param_value13}</span></div>
							</c:if>
							<c:if test='${null != detail_map.param_name14 && "" != detail_map.param_name14}'> 
								<div class="c2c-payment c2c-margin-left">${detail_map.param_name14}: <span class="c2c-payment-param-value">${detail_map.param_value14}</span></div>
							</c:if>
							<c:if test='${null != detail_map.param_name15 && "" != detail_map.param_name15}'> 
								<div class="c2c-payment c2c-margin-left">${detail_map.param_name15}: <span class="c2c-payment-param-value">${detail_map.param_value15}</span></div>
							</c:if>
							<c:if test='${null != detail_map.qrcode && "" != detail_map.qrcode}'> 
								<div class="c2c-payment c2c-margin-left">支付二维码: <img id="qrcode" width="200px" height="200px" src="<%=base%>wap/public/showimg!showImg.action?imagePath=${detail_map.qrcode}" /></div>
							</c:if>
							
						</div>
						
					</ul>
				</div>
				<!-- End Mailbox Inbox -->
				
				<!-- Start Chat -->
				<div class="chat padding-0">
				
					<!-- Start Title -->
					<div class="c2c-title">
						<span class="c2c-username">用户名称</span>
						<span class="c2c-t-bz">(这是备注)</span>
						<div class="user-detail">
							<span>UID：<span class="c2c-usercode"></span></span>
			            	<span>账户类型：<span class="c2c-role-name"></span></span>
						</div>						
					</div>
					<!-- End Title -->
					
					<!-- Start Conv -->
					<ul class="otc-conv"  id="otc-conv-all">					
					</ul>
					<!-- End Conv -->
					
					<div class="write">
						<div class="">
							<div class="otc-photo-add c2c-send-img">
								<img class="item-image" style="float: right;" width="30px" height="30px" src="<%=basePath%>img/chat/icon-add-old.png" alt="">					
								<input type="file" style="visibility: hidden;" accept="image/*" name="uploader-input" class="uploader-file" id="otc-upload">
							</div>
							<p><textarea id="otc-send-textarea" class="textarea form-control" placeholder="ctrl+enter发送消息" style="height:100px; width:100%;"></textarea></p>
							<button id="otc-send-btn" onclick="otc_sendMsg('text')" style="float: right;" class="btn btn-default margin-l-5">发送</button>
							<button id="otc-unsend-btn" style="float: right;margin-bottom: 0px;" class="btn btn-default margin-l-5">发送</button>
							<button onclick="otc_clearSendWord()" style="float: right;margin-bottom: 0px;" type="reset" class="btn">清空</button>
						</div>
					</div>
					
				</div>
				<!-- End Chat -->
			</div>
			<!-- End Mailbox Container -->
			
		</div>
		<!-- End Mailbox -->
		
		<%@ include file="include/footer.jsp"%>
		
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
									<div class="col-sm-6">
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
								<div id="user_qrcode"  class="modal-body col-md-12"> <div class="form-group"> <label class="col-sm-2 control-label form-label">支付二维码</label>
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
		
		setInterval(function () {
			var url = "<%=basePath%>normal/adminC2cOrderAction!getAjaxOrderDetail.action";
			var data = { "order_no": "${detail_map.order_no}" };
			goAjaxUrl(url, data, function(tmp) {
				var styleClass = '';
				var content = '';
				var timeHtml = "";
				var btnHtml = "";
				var state = Number(tmp.data.state);
				switch (state) {
					case 0: styleClass="otc-w6"; content="等待付款。。。";break;
					case 1: styleClass="otc-w7"; content="已付款"; break;
					case 2: styleClass="otc-w6"; content="申诉中"; break;
					case 3: styleClass="otc-w7"; content="已完成"; break;
					case 4: styleClass="otc-w7"; content="已取消"; break;
					case 5: styleClass="otc-w6"; content="已超时"; break;
				}
				timeHtml = '<div class="c2c-payment c2c-margin-left">';
				if (tmp.data.create_time != null) {
					timeHtml += '下单时间： <span class="c2c-payment-param-value">' + tmp.data.create_time + '</span>';
				}
				if (tmp.data.cancel_time != null) {
					timeHtml += '取消时间： <span class="c2c-payment-param-value">' + tmp.data.cancel_time + '</span>';
				}
				if (tmp.data.pay_time != null) {
					timeHtml += '付款时间： <span class="c2c-payment-param-value">' + tmp.data.pay_time + '</span>';
				}
				if (tmp.data.handle_time != null) {
					timeHtml += '完成时间： <span class="c2c-payment-param-value">' + tmp.data.handle_time + '</span>';
				}
				timeHtml += '</div>';
				if ("buy" == tmp.data.direction) {
					if (state == 0 || state == 1 || state == 2 || state == 5) {
						btnHtml += '<div class="otc-cancel-btn" onclick="orderCancel(\'' + tmp.data.order_no + '\')">取消订单</div>';
					}
					if (state == 1 || state == 2 || state == 5) {
						btnHtml += '<div class="otc-btn" onclick="succeeded(\'' + tmp.data.order_no + '\')">手动放行</div>';
					}
				} else if ("sell" == tmp.data.direction) {
					if (state == 0 || state == 1 || state == 2 || state == 5) {
						btnHtml += '<div class="otc-cancel-btn" onclick="orderCancel(\'' + tmp.data.order_no + '\')">取消订单</div>';
					}
					if (state == 0 || state == 2) {
						btnHtml += '<div class="otc-btn" onclick="tranMoney(\'' + tmp.data.order_no+'\',\'' + tmp.data.amount + '\',\'' + tmp.data.currency + '\',\'' + tmp.data.coin_amount + '\',\'' + tmp.data.symbol + '\')">手动转账</div>';
					}
					if (state == 1 || state == 2 || state == 5) {
						btnHtml += '<div class="otc-btn" onclick="succeeded(\'' + tmp.data.order_no + '\')">已付款管理员手动放行</div>';
					}
				}
				$(".otc_order_state").addClass(styleClass);
				$(".otc_order_state").html(content);
				$(".otc_order_time").html(timeHtml);
				$(".otc_order_opera").html(btnHtml);
			}, function() {
	//			$("#coin_value").val(0);
			});
		}, 5000);
		
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
							content += '<option value="' + tmp.payments[i].methodConfigId + '" <c:if test="${payment_method == ' + tmp.payments[i].methodConfigId + '}">selected="true"</c:if> >' + tmp.payments[i].methodName + '</option>';
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
			
			if (null != payment.paramName2 && "" != payment.paramName2) { $("#user_modal_param_name2").html(payment.paramName2); 
				$("#user_modal_param_value2").val(payment.paramValue2); $("#user_param2").show(); } else { $("#user_param2").hide(); }
			if (null != payment.paramName3 && "" != payment.paramName3) { $("#user_modal_param_name3").html(payment.paramName3); 
				$("#user_modal_param_value3").val(payment.paramValue3); $("#user_param3").show(); } else { $("#user_param3").hide(); }
			if (null != payment.paramName4 && "" != payment.paramName4) { $("#user_modal_param_name4").html(payment.paramName4); 
				$("#user_modal_param_value4").val(payment.paramValue4); $("#user_param4").show(); } else { $("#user_param4").hide(); }
			if (null != payment.paramName5 && "" != payment.paramName5) { $("#user_modal_param_name5").html(payment.paramName5); 
				$("#user_modal_param_value5").val(payment.paramValue5); $("#user_param5").show(); } else { $("#user_param5").hide(); }
			if (null != payment.paramName6 && "" != payment.paramName6) { $("#user_modal_param_name6").html(payment.paramName6); 
				$("#user_modal_param_value6").val(payment.paramValue6); $("#user_param6").show(); } else { $("#user_param6").hide(); }
			if (null != payment.paramName7 && "" != payment.paramName7) { $("#user_modal_param_name7").html(payment.paramName7); 
				$("#user_modal_param_value7").val(payment.paramValue7); $("#user_param7").show(); } else { $("#user_param7").hide(); }
			if (null != payment.paramName8 && "" != payment.paramName8) { $("#user_modal_param_name8").html(payment.paramName8); 
				$("#user_modal_param_value8").val(payment.paramValue8); $("#user_param8").show(); } else { $("#user_param8").hide(); }
			if (null != payment.paramName9 && "" != payment.paramName9) { $("#user_modal_param_name9").html(payment.paramName9); 
				$("#user_modal_param_value9").val(payment.paramValue9); $("#user_param9").show(); } else { $("#user_param9").hide(); }
			if (null != payment.paramName10 && "" != payment.paramName10) { $("#user_modal_param_name10").html(payment.paramName10); 
				$("#user_modal_param_value10").val(payment.paramValue10); $("#user_param10").show(); } else { $("#user_param10").hide(); }
			if (null != payment.paramName11 && "" != payment.paramName11) { $("#user_modal_param_name11").html(payment.paramName11); 
				$("#user_modal_param_value11").val(payment.paramValue11); $("#user_param11").show(); } else { $("#user_param11").hide(); }
			if (null != payment.paramName12 && "" != payment.paramName12) { $("#user_modal_param_name12").html(payment.paramName12); 
				$("#user_modal_param_value12").val(payment.paramValue12); $("#user_param12").show(); } else { $("#user_param12").hide(); }
			if (null != payment.paramName13 && "" != payment.paramName13) { $("#user_modal_param_name13").html(payment.paramName13); 
				$("#user_modal_param_value13").val(payment.paramValue13); $("#user_param13").show(); } else { $("#user_param13").hide(); }
			if (null != payment.paramName14 && "" != payment.paramName14) { $("#user_modal_param_name14").html(payment.paramName14); 
				$("#user_modal_param_value14").val(payment.paramValue14); $("#user_param14").show(); } else { $("#user_param14").hide(); }
			if (null != payment.paramName15 && "" != payment.paramName15) { $("#user_modal_param_name15").html(payment.paramName15); 
				$("#user_modal_param_value15").val(payment.paramValue15); $("#user_param15").show(); } else { $("#user_param15").hide(); }
			if (null != payment.qrcode && "" != payment.qrcode) { 
				document.getElementById('user_modal_qrcode').src="<%=base%>wap/public/showimg!showImg.action?imagePath=" + payment.qrcode;
				$("#user_qrcode").show(); } else { $("#user_qrcode").hide(); }
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
		
// #################################################################################################################################
		
		window.onload = function() {
			otc_getChatToken();
		}
		
		var chatHeight = $(".otc-conv").height() + 40;
		$(window).resize(function() {
			chatHeight = $(".otc-conv").height() + 40;
		});
		
		// 定时器
		var setInt = null;
		
		// 用户列表
		function otc_getMsgList(otc_partyid) {
		}			
				
		// 创建新用户消息列表
		function otc_searchMsg() {
			var username = $("#mailboxsearch").val();
			$.ajax({
				type : 'GET',
				data : {
					uid: username,
					token:$("#chat_token").val()
				},
				url : "<%=basePath%>normal/adminOtcOnlineChatAction!create.action",
				success : function(data) {
					var res = $.parseJSON(data);
					if (res.code == 0) {
						otc_getMsgList(res.data);
					} else if (res.code == 1) {
						swal({ title : res.msg, timer : 2000});
					}
					$("#mailboxsearch").val('');
				},
				error: function(err) {}
			})
		}
		
		// 聊天记录列表
		function otc_getMsgDetailList(otc_partyid, show_img) {
			$.ajax({
				type: 'GET',
				data: {
					message_id: '',
					partyid: otc_partyid,
					order_no: $("#order_no").val(),
					show_img: show_img,
					token: $("#chat_token").val()
				},
				url: "<%=basePath%>normal/adminOtcOnlineChatAction!list.action",
				success: function(data) {
					$(".otc-conv-loading").hide();
					var res = $.parseJSON(data);
					if (res.code == 0) {
						if(show_img){
							var msgDetailList = res.data;
							for (var i = 0; i < msgDetailList.length; i++) {
								var classId = "." + msgDetailList[i].id;
								if($(classId).hasClass("img-show")){
									$(classId).attr('src',msgDetailList[i].content);
									$(classId).removeClass("img-loading");
								}
							}
						} else {
							var msgDetailList = res.data.reverse();
							var msgDetailContent = '';
							if (msgDetailList.length>0) {
								$("#otc_message_id").val(msgDetailList[0].id);
							}
							for (var i = 0; i < msgDetailList.length; i++) {
								var divid = "#" + msgDetailList[i].id;
								if ($(divid).html()) { continue; }
								// if ('时间') {
									msgDetailContent += '<div id="'
									+ msgDetailList[i].id + '"><li class="date">'
									+ msgDetailList[i].createtime + '</li>'
								// }
								if (msgDetailList[i].send_receive == 'receive') {
									var className = "";
									if (msgDetailList[i].delete_status == -1) {
										className = "color-gray";
									} else {
										className = "";
									}
									if (msgDetailList[i].type == 'img') {
										if (msgDetailList[i].content) {//如果图片存在就显示图片
											msgDetailContent += '<li class="right-msg '+className+'"><img src="'
											+'<%=basePath%>img/chat/my-ico.png' + '" alt="img" class="img"><br><img src="'
											+ msgDetailList[i].content +'" alt="img" class="img-show '
											+ msgDetailList[i].id +'"></li></div>'
										} else {//如果图片不存在就显示加载中图片
											msgDetailContent += '<li class="right-msg '+className+'"><img src="'
											+'<%=basePath%>img/chat/my-ico.png' + '" alt="img" class="img"><br><img src="'
											+ '<%=basePath%>img/chat/loadin<%=basePath%>img/chat/f' +'" alt="img" class="img-show img-loading '
											+ msgDetailList[i].id +'"></li></div>'
										}
									} else {
										msgDetailContent += '<li class="right-msg '+className+'"><img src="'
										+'<%=basePath%>img/chat/my-ico.png' + '" alt="img" class="img"><p class="ballon color1">'
										+ msgDetailList[i].content + '</p></li></div>'
									}						
								} else if (msgDetailList[i].send_receive == 'send') {						
									if (msgDetailList[i].type == 'img') {
										if (msgDetailList[i].content) {
											msgDetailContent += '<li><img src="'
											+'<%=basePath%>img/chat/my-ico2.png' + '" alt="img" class="img"><br><img src="'
											+ msgDetailList[i].content +'" alt="img" class="img-show '
											+ msgDetailList[i].id +'"></li></div>'
										} else {
											msgDetailContent += '<li><img src="'
											+'<%=basePath%>img/chat/my-ico2.png' + '" alt="img" class="img"><br><img src="'
											+ '<%=basePath%>img/chat/loading.gif' +'" alt="img" class="img-show img-loading '
											+ msgDetailList[i].id +'"></li></div>'
										}
									} else {
										msgDetailContent += '<li><img src="'
										+'<%=basePath%>img/chat/my-ico2.png' + '" alt="img" class="img"><p class="ballon color2">'
										+ msgDetailList[i].content + '</p></li></div>'
									}						
								}
								if (otc_partyid == $("#otc_partyid").val()) {
									$(".otc-conv").append(msgDetailContent);
									msgDetailContent = ''
								}							
							}						
						}
					}
				},
				error: function(err) {}
			})
		}			
				
		// 定时检查是否需要滚动条置底操作
		setInterval(function() {
			var scrollHeight = $(".otc-conv")[0].scrollHeight
			if ($("#otc_isScroll").val() != 1) {
				$(".fixed-right-b").hide();
				$(".otc-conv").animate({
					scrollTop: scrollHeight
				}, 500);
			} else {
				$(".fixed-right-b").show();
			}
		}, 700)
				
		var timeout;// 用于存储定时器的变量
		// 鼠标点击事件，用户判断是否用鼠标拖动滚动条
		$(".otc-conv").mousedown(function(e) {
			timeout = setTimeout(function() {
				$(".otc-conv").scroll(function() {
					$("#otc_isScroll").val(1);
					var scroH = $(".otc-conv").scrollTop();  //滚动高度
					var scrollHeight = $(".otc-conv")[0].scrollHeight
					scroH = Math.ceil(scroH);
					chatHeight = $(".otc-conv").height() + 40;
					if (scroH+chatHeight >= scrollHeight) {  //滚动条滑到底部啦
						$("#otc_isScroll").val(0);
					} else{
						$("#otc_isScroll").val(1);
					}
				})				 
			}, 200);//鼠标按下0.2秒后发生事件
		});
		$(document).mouseup(function() {
			clearTimeout(timeout);//清理掉定时器
		});
		$(document).mouseout(function() {
			clearTimeout(timeout);//清理掉定时器
		});			
				
		// 鼠标滚动事件
		$(document).on("mousewheel DOMMouseScroll", function (e) {
			chatHeight = $(".otc-conv").height() + 40;
			$("#otc_isScroll").val(1);
			var scroH = $(".otc-conv").scrollTop();  //滚动高度
			var scrollHeight = $(".otc-conv")[0].scrollHeight
			scroH = Math.ceil(scroH);
			var delta = (e.originalEvent.wheelDelta && (e.originalEvent.wheelDelta > 0 ? 1 : -1)) ||  // chrome & ie
							(e.originalEvent.detail && (e.originalEvent.detail > 0 ? -1 : 1));              // firefox
			if (delta > 0) {
				// 向上滚
				if(scroH<200&&scrollHeight>chatHeight){
					var page_no = $("#otc_pageno").val();
					page_no ++;
					$("#otc_pageno").val(page_no);
					if($("#otc_hasmore").val() == 1){
						otc_getMsgDetailMoreList($("#otc_partyid").val(),false);
						otc_getMsgDetailMoreList($("#otc_partyid").val(),true);
					}
				}
			} else if (delta < 0) {
				// 向下滚
				if (scroH+chatHeight+100>=scrollHeight){  //滚动条滑到底部啦
					$("#otc_isScroll").val(0);
				} else{
					$("#otc_isScroll").val(1);
				}
			}
		});
		
		// 上一页消息内容列表
		function otc_getMsgDetailMoreList(otc_partyid, show_img) {
			$.ajax({
				type: 'GET',
				data: {
					message_id: $("#otc_message_id").val(),
					partyid: otc_partyid,
					order_no: $("#order_no").val(),
					show_img: show_img,
					token:$("#chat_token").val()
				},
				url: "<%=basePath%>normal/adminOtcOnlineChatAction!list.action",
				success: function(data) {
					var res = $.parseJSON(data);
					if (res.code == 0) {
						if (show_img) {
							var msgDetailList = res.data;
							for (var i = 0; i < msgDetailList.length; i++) {
								var classId = "." + msgDetailList[i].id;
								if ($(classId).hasClass("img-show")) {
									$(classId).attr('src',msgDetailList[i].content);
									$(classId).removeClass("img-loading");
								}
							}
						} else {
							var msgDetailList = res.data;
							var msgDetailContent = '';
							if (msgDetailList.length==0) {
								$("#otc_hasmore").val(0);
								return
							}
							$("#otc_message_id").val(msgDetailList[0].id);
							for (var i = 0; i < msgDetailList.length; i++) {
								var divid = "#" + msgDetailList[i].id;
								if ($(divid).html()) { continue; }
								// if ('时间') {
									msgDetailContent += '<div id="'
									+ msgDetailList[i].id + '"><li class="date">'
									+ msgDetailList[i].createtime + '</li>'
								// }
								if (msgDetailList[i].send_receive == 'receive') {
									if (msgDetailList[i].type == 'img') {
										if (msgDetailList[i].content) {
											msgDetailContent += '<li class="right-msg"><img src="'
											+ '<%=basePath%>img/chat/my-ico.png' + '" alt="img" class="img"><br><img src="'
											+ msgDetailList[i].content +'" alt="img" class="img-show '
											+ msgDetailList[i].id +'"></li></div>'
										} else {
											msgDetailContent += '<li class="right-msg"><img src="'
											+ '<%=basePath%>img/chat/my-ico.png' + '" alt="img" class="img"><br><img src="'
											+ '<%=basePath%>img/chat/loading.gif' +'" alt="img" class="img-show img-loading '
											+ msgDetailList[i].id +'"></li></div>'
										}
									} else {
										msgDetailContent += '<li class="right-msg"><img src="'
										+ '<%=basePath%>img/chat/my-ico.png' + '" alt="img" class="img"><p class="ballon color1">'
										+ msgDetailList[i].content + '</p></li></div>'
									}		
								} else if (msgDetailList[i].send_receive == 'send') {
			
									if (msgDetailList[i].type == 'img') {
										if (msgDetailList[i].content) {
											msgDetailContent += '<li><img src="'
											+ '<%=basePath%>img/chat/my-ico2.png' + '" alt="img" class="img"><br><img src="'
											+ msgDetailList[i].content +'" alt="img" class="img-show '
											+ msgDetailList[i].id +'"></li></div>'
										} else {
											msgDetailContent += '<li><img src="'
											+ '<%=basePath%>img/chat/my-ico2.png' + '" alt="img" class="img"><br><img src="'
											+ '<%=basePath%>img/chat/loading.gif' +'" alt="img" class="img-show img-loading '
											+ msgDetailList[i].id +'"></li></div>'
										}
									} else {
										msgDetailContent += '<li><img src="'
										+ '<%=basePath%>img/chat/my-ico2.png' + '" alt="img" class="img"><p class="ballon color2">'
										+ msgDetailList[i].content + '</p></li></div>'
									}		
								}
								$(".otc-conv").prepend(msgDetailContent);
								msgDetailContent = '';
							}
							// $(".otc-conv>li").eq(0).before(msgDetailContent);
						}	
					}
				},
				error: function(err) {}
			})
		}
		
		// 图片点击事件
		$(".otc-conv").on("click", "img", function() {
			var imgUrl = $(this).attr("src");
	// 		$(".bigImg").show();
	// 		$(".bigImg img").attr("src", imgUrl);
	
			var newwin=window.open()
			var height="height:"+window.screen.height+";";
			var width="width:"+window.screen.width+";";
	// 		var height="height:"+window.screen.availHeight+";";
	// 		var width="width:"+window.screen.availWidth+";";
			newwin.document.write("<div style='display: table-cell;text-align: center;vertical-align: middle;background: #0e0e0e;"+height+width+"'><img src="+imgUrl+" /></div>")
		})
		
		// 定时刷新用户列表与聊天记录
		function otc_chat_interval() {
			otc_getMsgDetailList($("#otc_partyid").val(), false)
			otc_getMsgDetailList($("#otc_partyid").val(), true)
			setInt = setInterval(function() {
				if ($("#otc_partyid").val() && $("#otc_isScroll").val() == 0) {
					// 聊天记录在第一页 滚动条在底部 当前有聊天时，刷新聊天记录
					otc_getMsgDetailList($("#otc_partyid").val(), false)
					otc_getMsgDetailList($("#otc_partyid").val(), true)
				}
			}, 1000)
		}
		
		function otc_chat_interval_clear() {
			clearInterval(setInt);
			setInt = null;
		}
		$(".otc-chat").keypress(function (e) {
			var stat = false;
			if (e.keyCode == 10) {
				otc_sendMsg("text");
			}
		})
		
		$("#otc-unsend-btn").hide();
		$("#otc-send-btn").show();
		
		// 发送消息
		function otc_sendMsg(type) {
			if (!$("#otc-send-textarea").val()) {
				swal("请输入内容");
				return
			}
			$("#otc-unsend-btn").show();
			$("#otc-send-btn").hide();
			$("#otc-send-textarea").attr("disabled","disabled")
			
			$("#otc_isScroll").val(0)
			$.ajax({
				type: 'POST',
				data: {
					type: type,
					content: encodeURI($("#otc-send-textarea").val()),
					partyid: $("#otc_partyid").val(),
					order_no: $("#order_no").val(),
					token:$("#chat_token").val()
				},
				url: "<%=basePath%>normal/adminOtcOnlineChatAction!send.action",
				success: function(data) {
					var res = $.parseJSON(data);
					if (res.code == 0) {
						otc_getMsgDetailList($("#otc_partyid").val(),false);
						otc_getMsgDetailList($("#otc_partyid").val(),true);
						$("#otc-send-textarea").val('');
						
					}else if (res.code == 1) {
						swal(res.msg);
					}
					$("#otc-unsend-btn").hide();
					$("#otc-send-btn").show();
					$("#otc-send-textarea").removeAttr("disabled")
					$("#otc-send-textarea").focus();
				},
				error: function(err) {
					$("#otc-unsend-btn").hide();
					$("#otc-send-btn").show();
					$("#otc-send-textarea").removeAttr("disabled")
					$("#otc-send-textarea").focus();
				}
			})
		}
		
		function otc_goBottom() {
			// 滚动到底部
			var scrollHeight = $(".otc-conv")[0].scrollHeight
			$(".otc-conv").animate({
				scrollTop: scrollHeight
			}, 500);
			otc_getMsgDetailList($("#otc_partyid").val(),false)
			otc_getMsgDetailList($("#otc_partyid").val(),true)
			$("#otc_isScroll").val(0)
		}
		
		// 清除输入框文字
		function otc_clearSendWord() {
			$("#otc-send-textarea").val('');
		}
		
		// 图片上传
		$(".otc-photo-add img").click(function() {
			$('#otc-upload').click();
		})
		$('#otc-upload').on('change', function (event) {
			var $file = event.currentTarget;
			var file = $file.files[0];				
			if (!file) {return}

			var windowURL = window.URL || window.webkitURL;
			var dataURL = windowURL.createObjectURL(file);
			var timeId = new Date().getTime();
			
			var loadingImg = '<div timeid="'
				+ timeId +'"><li class="date"></li><li class="right-msg" id="loadingimg"><img src="'
				+'<%=basePath%>img/chat/my-ico.png' + '" alt="img" class="img"><br><img timeId="'
				+ timeId +'" src="'
				+ '<%=basePath%>img/chat/loading.gif' +'" alt="img" class="img-show img-loading"></li></div>'
			
			$(".conv").append(loadingImg);
			otc_goBottom();
			
			if (!/image\/\w+/.test(file.type)) {
				alert("只能选择图片");
				return false;
			}

			var formData = new FormData();
			formData.append("file", file);
			
			$.ajax({
		    	type: "POST",
		        url: "<%=basePath%>normal/uploadimg!execute.action?random=" + Math.random(),
				data: formData,
				dataType: "json",
		    	contentType: false,  
		        processData: false, 
				success : function(data) {
					otc_sendFile(data.data, timeId, dataURL);
				},
				error : function(XMLHttpRequest, textStatus, errorThrown) {
					console.log("请求错误");
				}
			});
		});
		
		function otc_sendFile(content, timeId, dataURL) { 
			var formData = new FormData();
			formData.append("content", content);
			formData.append("partyid",$("#otc_partyid").val());
			formData.append("order_no",$("#order_no").val());
			formData.append("type","img");
			formData.append("token",$("#chat_token").val());
			
			$.ajax({
				url: "<%=basePath%>normal/adminOtcOnlineChatAction!send.action",
				dataType:'json',
				type:'POST',
				data: formData,
				processData : false, // 使数据不做处理
				contentType : false, // 不要设置Content-Type请求头
				success: function(data){
					var res = data;							
					if (res.code == 0) {
						var timeChoose = $("[timeid="+ timeId +"]");
// 						timeChoose.attr("id", res.data.chat_id);
// 						timeChoose.find(".date").html(res.data.updatetime);
						timeChoose.find(".img-show").attr("src",dataURL);
						timeChoose.find(".img-show").removeClass("img-loading");
						otc_getMsgDetailList($("#otc_partyid").val(),false);
						otc_getMsgDetailList($("#otc_partyid").val(),true);
						// $(".item-image").attr("src", "<%=basePath%>img/chat/icon-add-old.png");
					}else if (res.code == 1) {
						swal(res.msg);
					}
					$('#upload').val("");
				},
				error:function(response){
					$("#loadingimg").remove();
					$('#upload').val("");
				}
		});
	}
		
		function otc_getUserInfo() {
			$.ajax({
				type: 'GET',
				data: {
					partyid: $("#user_party_id").val(),
					order_no: $("#order_no").val(),
					token:$("#chat_token").val()
				},
				url: "<%=basePath%>normal/adminOtcOnlineChatAction!saveGetUserInfo.action",
				success: function(data) {
					var res = $.parseJSON(data);
					if (res.code == 0) {
			            var remarks = "";
			            if(res.data.remarks){
			            	remarks = "(" + res.data.remarks + ")";
			            }
			            $(".c2c-username").html(res.data.username);
						$(".c2c-t-bz").html(remarks);
						$(".otc-w1 text").html('('+res.data.usercode+')');
						$(".c2c-usercode").html(res.data.usercode);
						var role_name = "";
						if(!res.data.role_name){
							role_name = "游客"
						}
						if(res.data.role_name=="MEMBER"){
							role_name = "正式用户"
						}
						if(res.data.role_name=="GUEST"){
							role_name = "演示用户"
						}
						if(res.data.role_name=="AGENT"){
							role_name = "代理商"
						}
						$(".c2c-role-name").html(role_name);
					}
				},
				error: function(err) {}
			})		
		}
		otc_chat_interval();
		otc_getUserInfo();
		// otc_getChatToken();
	
	    function otc_getChatToken(){
			$.ajax({
				type: 'GET',
				async:false,
				data: {
					
				},
				url: "<%=basePath%>public/newAdminOnlineChatAction!getChatToken.action",
				success: function(data) {
					var res = $.parseJSON(data);
					if (res.code == 0) {
						$("#chat_token").val(res.data);
						otc_chat_interval();
						otc_getUserInfo();
					}
				},
				error: function(err) {}
			})		
		}
			    
	 	// 右键事件
	    document.getElementById("otc-conv-all").oncontextmenu = function(e){
	 		return false;
	   	}
	    $(document).click(function (e) {
	        var $target = $(e.target);    //点击表情选择按钮和表情选择框以外的地方 隐藏表情选择框
	        if (!$target.is('.msg-back')) {
	        	$(".msg-back").remove();
	        }
	    });
	    $(".otc-conv").on("mousedown",".right-msg .ballon",function(e){
			  if($(this).parent().hasClass("color-gray")){return}
	          if(3 == e.which){
	        	 	$(".msg-back").remove();
					var htmlCon = '<div class="msg-back">撤回</div>'
	          }
	          $(this).parent().append(htmlCon);
		})
		
		$(".otc-conv").on("click",".msg-back",function(e){
			var messageId = $(this).parent().parent().attr("id");
			var _this = $(this);
			swal({
				title : "是否确认撤回?",
				text : "",
				type : "warning",
				showCancelButton : true,
				confirmButtonColor : "#DD6B55",
				confirmButtonText : "确认",
				cancelButtonText: "取消",
// 				closeOnConfirm : false
			}, function(isConfirm){
				if (isConfirm) {						
					$.ajax({
						type: 'GET',
						async:false,
						data: {
							message_id: messageId,
							token:$("#chat_token").val()
						},
						url: "<%=basePath%>normal/adminOtcOnlineChatAction!deleteOnlineChatMessage.action",
						success: function(data) {
						var res = $.parseJSON(data);
						if (res.code == 0) {
							_this.parent().addClass("color-gray");
							$(".msg-back").remove();
							setTimeout(function(){swal("已撤回！");},500)
						}else{
							setTimeout(function(){swal(res.msg);},500)
						}
					},
					error: function(err) {}
				})				
				} 
			});
		})
	
	</script>

</body>

</html>
