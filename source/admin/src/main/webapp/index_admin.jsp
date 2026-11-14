<%@ page language="java" pageEncoding="utf-8" isELIgnored="false"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
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
			<h3>综合查询</h3>

			<div class="col-md-12 col-lg-12">
				<h4>今日数据统计(00:00-24:00)</h4>
				<ul class="topstats clearfix">

					<li class="arrow"></li>
					<li class="col-xs-6 col-lg-2">
						<span class="title"><i class="fa fa-jpy"></i>USDT账户余额</span>
						<h3 class="color-up"> <fmt:formatNumber value="${statistics.sum_usdt_amount}" pattern="#0.00" /> </h3>
					</li>

					<li class="col-xs-6 col-lg-2">
						<span class="title"><i class="fa fa-clock-o"></i>充值金额</span>
						<h3> <fmt:formatNumber value="${statistics.recharge}" /> </h3>
					</li>

					<li class="col-xs-6 col-lg-2">
						<span class="title"><i class="fa fa-clock-o"></i>提现金额</span>
						<h3> <fmt:formatNumber value="${statistics.withdraw}" /> </h3>
					</li>

					<li class="col-xs-6 col-lg-2">
						<span class="title"><i class="fa fa-clock-o"></i>充提差额</span>
						<h3> <fmt:formatNumber value="${statistics.balance_amount}" /> </h3>
					</li>

					<li class="col-xs-6 col-lg-2">
						<span class="title"><i class="fa fa-line-chart"></i>收益</span> 
						<c:if test="${statistics.totle_income >= 0}">
							<h3 class="color-up"> <fmt:formatNumber value="${statistics.totle_income}" pattern="#0.00" /> </h3>
						</c:if> 
						<c:if test="${statistics.totle_income < 0}">
							<h3 class="color-down"> <fmt:formatNumber value="${statistics.totle_income}" pattern="#0.00" /> </h3>
						</c:if>
					</li>

					<li class="col-xs-6 col-lg-2">
						<span class="title"><i class="fa fa-group"></i>总用户数/今日新增/今日充值人数</span>
						<h3> <span>${statistics.all_user_count}/${statistics.today_user_count}/${statistics.today_recharge_user_count}</span> </h3>
					</li>
					
				</ul>
			</div>

			<c:if test="${security.isRolesAccessible('ROLE_ROOT,ROLE_ADMIN')
						 || security.isResourceListAccessible('OP_EXCHANGE_RECHARGE_CHECK,OP_EXCHANGE_RECHARGE_OPERATE,OP_EXCHANGE_WITHDRAW_CHECK,OP_EXCHANGE_WITHDRAW_OPERATE,OP_USER_KYC_CHECK,OP_USER_KYC_OPERATE,OP_USER_KYC_HIGH_LEVEL_CHECK,OP_USER_KYC_HIGH_LEVEL_OPERATE')}">
					 			
				<div class="col-md-12">
					<h4>待处理提醒</h4>
					<ul class="panel quick-menu clearfix">
										
						<c:if test="${security.isRolesAccessible('ROLE_ROOT,ROLE_ADMIN')
								 || security.isResourceAccessible('OP_EXCHANGE_RECHARGE_CHECK')
								 || security.isResourceAccessible('OP_EXCHANGE_RECHARGE_OPERATE')}">
								 
							<li class="col-sm-3">
								<a href="<%=basePath%>normal/adminRechargeBlockchainOrderAction!list.action">
									<i class="fa fa-qrcode"></i> 充值订单 
									<span class="recharge_blockchain_order_untreated_cout badge label-danger" style="display: none">0</span>
								</a>
							</li>
							
						</c:if>
						
						<c:if test="${security.isRolesAccessible('ROLE_ROOT,ROLE_ADMIN')
								 || security.isResourceAccessible('OP_EXCHANGE_WITHDRAW_CHECK')
								 || security.isResourceAccessible('OP_EXCHANGE_WITHDRAW_OPERATE')}">
								 
							<li class="col-sm-3">
								<a href="<%=basePath%>normal/adminWithdrawAction!list.action">
									<i class="fa fa-credit-card"></i> 提现订单 
									<span class="withdraw_order_untreated_cout badge label-danger" style="display: none">0</span>
								</a>
							</li>
							
						</c:if>
						
						<c:if test="${security.isRolesAccessible('ROLE_ROOT,ROLE_ADMIN')
								 || security.isResourceAccessible('OP_USER_KYC_CHECK')
								 || security.isResourceAccessible('OP_USER_KYC_OPERATE')}">
								 
							<li class="col-sm-3">
								<a href="<%=basePath%>normal/adminKycAction!list.action"> 
									<i class="fa fa-star-o"></i> 用户基础认证 
									<span class="kyc_untreated_cout badge label-danger" style="display: none">0</span>
								</a>
							</li>
							
						</c:if>
						
						<c:if test="${security.isRolesAccessible('ROLE_ROOT,ROLE_ADMIN')
								 || security.isResourceAccessible('OP_USER_KYC_HIGH_LEVEL_CHECK')
								 || security.isResourceAccessible('OP_USER_KYC_HIGH_LEVEL_OPERATE')}">
								 
							<li class="col-sm-3">
								<a href="<%=basePath%>normal/adminKycHighLevelAction!list.action">
									<i class="fa fa-star"></i> 用户高级认证 
									<span class="kyc_high_level_untreated_cout badge label-danger" style="display: none">0</span>
								</a>
							</li>
							
						</c:if>
						
					</ul>
				</div>
				
			</c:if>

		</div>
		<!-- END CONTAINER -->
		<!-- //////////////////////////////////////////////////////////////////////////// -->

		<%@ include file="include/footer.jsp"%>

	</div>
	<!-- End Content -->
	<!-- //////////////////////////////////////////////////////////////////////////// -->

	<%@ include file="include/js.jsp"%>
	
	<script type="text/javascript">

		setInterval(function() {
			var data = {};
			goSumTipsAjaxUrl('<%=basePath%>normal/adminTipAction!getTips.action', data);
		}, 5000);
		
		function goSumTipsAjaxUrl(targetUrl, data) {
			$.ajax({
				url : targetUrl,
				data : data,
				type : 'get',
				dataType : "json",
				success : function(data) {
					var temp = data;
					initTipCountHandle();
					// 遍历tip
					if (temp.tipList.length > 0) {
						temp.tipList.forEach(function(ele) {
							countHandle($(ele.tip_dom_name), ele.tip_content_sum);
						});
					}
				}
			});
		}
		
		function initTipCountHandle() {
			// 业务
// 			countHandle($(".automonitor_approve_order_untreated_cout"), 0);
// 			countHandle($(".automonitor_threshold_order_untreated_cout"), 0);
// 			countHandle($(".contract_order_untreated_cout"), 0);
// 			countHandle($(".futures_order_untreated_cout"), 0);
// 			countHandle($(".automonitor_pledge_galaxy_order_untreated_cout"), 0);
			// 用户
			countHandle($(".kyc_untreated_cout"), 0);
			countHandle($(".kyc_high_level_untreated_cout"), 0);
// 			countHandle($(".user_safeword_apply_untreated_cout"), 0);
			// 财务
// 			countHandle($(".automonitor_withdraw_order_untreated_cout"),0);
			countHandle($(".withdraw_order_untreated_cout"), 0);
			countHandle($(".recharge_blockchain_order_untreated_cout"), 0);
		}
		
		function countHandle(ele, count) {
// 			if (ele == ".kyc_untreated_cout" 
// 				|| ele == ".kyc_high_level_untreated_cout" 
// 				|| ele == ".withdraw_order_untreated_cout" 
// 				|| ele == ".recharge_blockchain_order_untreated_cout") {
				if (count == 0 || isNaN(count)) {
					$(ele).hide();
				} else {
					$(ele).show();
					$(ele).html(count)
				}
// 			}
		}
	
	</script>

</body>

</html>
