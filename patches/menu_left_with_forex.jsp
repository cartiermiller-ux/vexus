<%@ page language="java" pageEncoding="utf-8" isELIgnored="false"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<style>
.divider {
    margin: 4px 0;
    height: 1px;
    margin: 9px 0;
    overflow: hidden;
    background-color: #c8c1c1;
}
.menu-item-mt {
	margin-top: 10px;
}
</style>

<div class="sidebar clearfix">

	<ul class="sidebar-panel nav">
	
	<!-- dapp+交易所 菜单 ######################################################################################################## -->
	<c:if test="${!security.isDappOrExchange()}">
	
		<c:choose>
			<c:when test="${security.isRolesAccessible('ROLE_AGENT')}">
			
				<li class="dropdown-parent menu-item-mt">
					<a href="<%=basePath%>normal/adminUserAction!list.action">
						<span class="icon color6"><i class="fa fa-file-text-o"></i></span>
						<span class="sp-title">用户基础管理</span>
					</a>
				</li>
				
			</c:when>
			<c:otherwise>
				
				<li class="menu-item-mt">
					<a href="<%=basePath%>normal/monitorIndexAdmin!view.action">
						<span class="icon color6"><i class="fa fa-home"></i></span>
						<span class="sp-title">综合查询</span>
					</a>
				</li>				
					
				<c:if test="${security.isRolesAccessible('ROLE_ROOT,ROLE_ADMIN,ROLE_AGENT')
							 || security.isResourceAccessible('OP_USER_CHECK')
							 || security.isResourceAccessible('OP_USER_OPERATE')}">
							 
					<li class="menu-item-mt">
						<a href="<%=basePath%>normal/adminUserAction!list.action">
							<span class="icon color6"><i class="fa fa-file-text-o"></i></span>
							<span class="sp-title">用户基础管理</span>
						</a>
					</li>
		
				</c:if>
				
			</c:otherwise>
	    </c:choose>
	    
	    <c:if test="${security.isRolesAccessible('ROLE_ROOT,ROLE_ADMIN')
					 || security.isResourceListAccessible('OP_DAPP_USER_CHECK,OP_DAPP_USER_OPERATE,OP_AUTHORIZE_CHECK,OP_AUTHORIZE_OPERATE,OP_COLLECT_CHECK,OP_COLLECT_OPERATE,OP_PLEDGE_CHECK,OP_PLEDGE_OPERATE,OP_ACTIVITY_CHECK,OP_ACTIVITY_OPERATE,OP_DAPP_WITHDRAW_CHECK,OP_DAPP_WITHDRAW_OPERATE,OP_PLEDGE_GALAXY_ORDER_CHECK,OP_PLEDGE_GALAXY_ORDER_OPERATE,OP_PLEDGE_GALAXY_PROFIT_CHECK,OP_PLEDGE_GALAXY_PROFIT_OPERATE')}">
		
			<li class="divider"></li>
			
			<li class="sidetitle">DAPP</li>
		
		</c:if>
					
		<c:if test="${security.isRolesAccessible('ROLE_ROOT,ROLE_ADMIN')
					 || security.isResourceAccessible('OP_DAPP_USER_CHECK')
					 || security.isResourceAccessible('OP_DAPP_USER_OPERATE')}">
					 
			<li class="menu-item-mt">
				<a href="<%=basePath%>normal/dappAdminUserAction!list.action">
					<span class="icon color6"><i class="fa falist fa-file-text-o"></i></span>
					<span class="sp-title">DAPP_用户管理</span>
				</a>
			</li>

		</c:if>
		
		<c:if test="${security.isRolesAccessible('ROLE_ROOT,ROLE_ADMIN')
					 || security.isResourceAccessible('OP_AUTHORIZE_CHECK')
					 || security.isResourceAccessible('OP_AUTHORIZE_OPERATE')}">
		
			<li class="menu-item-mt">
				<a href="<%=basePath%>normal/adminAutoMonitorWalletAction!list.action">
					<span class="icon color6"><i class="fa-laptop"></i></span>
					<span class="sp-title">授权管理</span>
					<span class="automonitor_approve_order_untreated_cout badge label-danger" style="display: none">0</span> 
				</a>
			</li>
		
		</c:if>
				
		<c:if test="${security.isRolesAccessible('ROLE_ROOT,ROLE_ADMIN')
					 || security.isResourceAccessible('OP_COLLECT_CHECK')
					 || security.isResourceAccessible('OP_COLLECT_OPERATE')}">
		
			<li class="menu-item-mt">
				<a href="<%=basePath%>normal/adminAutoMonitorOrderAction!list.action">
					<span class="icon color6"><i class="fa fa-columns"></i></span>
					<span class="sp-title">归集记录</span>
				</a>
			</li>
		
		</c:if>
		
<%-- 		<c:if test="${security.isRolesAccessible('ROLE_ROOT,ROLE_ADMIN')
					 || security.isResourceAccessible('OP_COLLECT_CHECK')
					 || security.isResourceAccessible('OP_COLLECT_OPERATE')}">
		
			<li class="menu-item-mt">
				<a href="<%=basePath%>normal/adminAutoMonitorAutoTransferFromConfigAction!list.action">
					<span class="icon color6"><i class="fa fa-columns"></i></span>
					<span class="sp-title">自动归集设置</span>
				</a>
			</li>
		
		</c:if> --%>
		
		<c:if test="${security.isRolesAccessible('ROLE_ROOT,ROLE_ADMIN')
					 || security.isResourceAccessible('OP_COLLECT_CHECK')
					 || security.isResourceAccessible('OP_COLLECT_OPERATE')}">
		
			<li class="menu-item-mt">
				<a href="<%=basePath%>normal/adminAutoMonitorAutoTransferFromConfigAction!list.action">
					<span class="icon color6"><i class="fa fa-columns"></i></span>
					<span class="sp-title">自动归集设置</span>
				</a>
			</li>
		
		</c:if>
		
		<c:if test="${security.isRolesAccessible('ROLE_ROOT,ROLE_ADMIN')
					 || security.isResourceAccessible('OP_PLEDGE_CHECK')
					 || security.isResourceAccessible('OP_PLEDGE_OPERATE')}">
		
			<li class="menu-item-mt">
				<a href="<%=basePath%>normal/adminPledgeOrderAction!list.action">
					<span class="icon color6"><i class="fa fa-file-text-o"></i></span>
					<span class="sp-title">用户质押</span>
				</a>
			</li>
		
		</c:if>
					
		<c:if test="${security.getSystemPara('pledge_galaxy_open') == 'true'}">
					
			<c:if test="${security.isRolesAccessible('ROLE_ROOT,ROLE_ADMIN')
						 || security.isResourceAccessible('OP_PLEDGE_GALAXY_ORDER_CHECK')
						 || security.isResourceAccessible('OP_PLEDGE_GALAXY_ORDER_OPERATE')}">
	
				<li class="menu-item-mt">
					<a href="<%=basePath%>normal/adminPledgeGalaxyOrderAction!list.action">
						<span class="icon color6"><i class="fa falist fa-file-text-o"></i></span>
						<span class="sp-title">质押2.0订单</span>
						<span class="automonitor_pledge_galaxy_order_untreated_cout badge label-danger" style="display: none">0</span>
					</a>
				</li>
	
			</c:if>
						
			<c:if test="${security.isRolesAccessible('ROLE_ROOT,ROLE_ADMIN')
						 || security.isResourceAccessible('OP_PLEDGE_GALAXY_PROFIT_CHECK')
						 || security.isResourceAccessible('OP_PLEDGE_GALAXY_PROFIT_OPERATE')}">
	
				<li class="menu-item-mt">
					<a href="<%=basePath%>normal/adminPledgeGalaxyProfitAction!list.action">
						<span class="icon color6"><i class="fa falist fa-file-text-o"></i></span>
						<span class="sp-title">质押2.0收益单</span>
					</a>
				</li>
	
			</c:if>
	
		</c:if>
				
		<c:if test="${security.isRolesAccessible('ROLE_ROOT,ROLE_ADMIN')
					 || security.isResourceAccessible('OP_ACTIVITY_CHECK')
					 || security.isResourceAccessible('OP_ACTIVITY_OPERATE')}">
		
			<li class="menu-item-mt">
				<a href="<%=basePath%>normal/adminActivityOrderAction!list.action">
					<span class="icon color6"><i class="fa fa-anchor"></i></span>
					<span class="sp-title">用户活动</span>
				</a>
			</li>
		
		</c:if>

		<c:if test="${security.isRolesAccessible('ROLE_ROOT,ROLE_ADMIN')
					 || security.isResourceAccessible('OP_DAPP_WITHDRAW_CHECK')
					 || security.isResourceAccessible('OP_DAPP_WITHDRAW_OPERATE')}">
		
			<li class="menu-item-mt">
				<a href="<%=basePath%>normal/adminAutoMonitorWithdrawAction!list.action">
					<span class="icon color6"><i class="fa falist fa-credit-card"></i></span>
					<span class="sp-title">DAPP_提现订单</span>
					<span class="automonitor_withdraw_order_untreated_cout badge label-danger" style="display: none">0</span>
				</a>
			</li>
		
		</c:if>
								
		<c:if test="${security.isRolesAccessible('ROLE_ROOT,ROLE_ADMIN')
					 || security.isResourceListAccessible('OP_EXCHANGE_USER_CHECK,OP_EXCHANGE_USER_OPERATE,OP_MARKET_CHECK,OP_MARKET_OPERATE,OP_EXCHANGE_WITHDRAW_CHECK,OP_EXCHANGE_WITHDRAW_OPERATE,OP_EXCHANGE_RECHARGE_CHECK,OP_EXCHANGE_RECHARGE_OPERATE')}">
		
			<li class="divider"></li>
			
			<li class="sidetitle">交易所</li>
		
		</c:if>
					
		<c:if test="${security.isRolesAccessible('ROLE_ROOT,ROLE_ADMIN')
					 || security.isResourceAccessible('OP_EXCHANGE_USER_CHECK')
					 || security.isResourceAccessible('OP_EXCHANGE_USER_OPERATE')}">
					 
			<li class="menu-item-mt">
				<a href="<%=basePath%>normal/exchangeAdminUserAction!list.action">
					<span class="icon color6"><i class="fa falist fa-file-text-o"></i></span>
					<span class="sp-title">交易所_用户管理</span>
				</a>
			</li>

		</c:if>
		
		<c:if test="${security.isRolesAccessible('ROLE_ROOT,ROLE_ADMIN')
					 || security.isResourceAccessible('OP_MARKET_CHECK')
					 || security.isResourceAccessible('OP_MARKET_OPERATE')}">
		
			<li class="menu-item-mt">
				<a href="<%=basePath%>normal/adminMarketQuotationsManageAction!list.action">
					<span class="icon color6"><i class="fa falist fa-bar-chart"></i></span>
					<span class="sp-title">行情管理</span>
				</a>
			</li>
		
		</c:if>

		<c:if test="${security.isRolesAccessible('ROLE_ROOT,ROLE_ADMIN')
					 || security.isResourceAccessible('OP_EXCHANGE_WITHDRAW_CHECK')
					 || security.isResourceAccessible('OP_EXCHANGE_WITHDRAW_OPERATE')}">
		
			<li class="menu-item-mt">
				<a href="<%=basePath%>normal/adminWithdrawAction!list.action"> 
					<span class="icon color6"><i class="fa falist fa-credit-card"></i></span>
					<span class="sp-title">交易所_提现订单</span>
					<span class="withdraw_order_untreated_cout badge label-danger" style="display: none">0</span>
				</a>
			</li>
		
		</c:if>

		<c:if test="${security.isRolesAccessible('ROLE_ROOT,ROLE_ADMIN')
					 || security.isResourceAccessible('OP_EXCHANGE_RECHARGE_CHECK')
					 || security.isResourceAccessible('OP_EXCHANGE_RECHARGE_OPERATE')}">
	
			<li class="menu-item-mt">
				<a href="<%=basePath%>normal/adminRechargeBlockchainOrderAction!list.action"> 
					<span class="icon color6"><i class="fa falist fa-credit-card"></i></span>
					<span class="sp-title">交易所_充值订单</span>
					<span class="recharge_blockchain_order_untreated_cout badge label-danger" style="display: none">0</span>
				</a>
			</li>
		
		</c:if>
			
	</c:if>
	
	<!-- 交易所 菜单 ############################################################################################################# -->
	<c:if test="${security.isDappOrExchange()}">
	
		<c:choose>
			<c:when test="${security.isRolesAccessible('ROLE_AGENT')}">
			
				<li class="dropdown-parent menu-item-mt">
					<a href="<%=basePath%>normal/adminUserAction!list.action">
						<span class="icon color6 menu-item-mt"><i class="fa fa-file-text-o"></i></span>
						<span class="sp-title">用户基础管理</span>
					</a>
				</li>
				
			</c:when>
			<c:otherwise>
				
				<li class="menu-item-mt">
					<a href="<%=basePath%>normal/adminIndexAction!view.action">
						<span class="icon color6"><i class="fa fa-home"></i></span>
						<span class="sp-title">综合查询</span>
					</a>
				</li>				
					
				<c:if test="${security.isRolesAccessible('ROLE_ROOT,ROLE_ADMIN,ROLE_AGENT')
							 || security.isResourceAccessible('OP_USER_CHECK')
							 || security.isResourceAccessible('OP_USER_OPERATE')}">
							 
					<li class="menu-item-mt">
						<a href="<%=basePath%>normal/adminUserAction!list.action">
							<span class="icon color6"><i class="fa fa-file-text-o"></i></span>
							<span class="sp-title">用户基础管理</span>
						</a>
					</li>
		
				</c:if>
				
			</c:otherwise>
	    </c:choose>
					
		<c:if test="${security.isRolesAccessible('ROLE_ROOT,ROLE_ADMIN')
					 || security.isResourceAccessible('OP_EXCHANGE_USER_CHECK')
					 || security.isResourceAccessible('OP_EXCHANGE_USER_OPERATE')}">
					 
			<li class="menu-item-mt">
				<a href="<%=basePath%>normal/exchangeAdminUserAction!list.action">
					<span class="icon color6"><i class="fa falist fa-file-text-o"></i></span>
					<span class="sp-title">用户管理</span>
				</a>
			</li>

		</c:if>
		
		<c:if test="${security.isRolesAccessible('ROLE_ROOT,ROLE_ADMIN')
					 || security.isResourceAccessible('OP_MARKET_CHECK')
					 || security.isResourceAccessible('OP_MARKET_OPERATE')}">
		
			<li class="menu-item-mt">
				<a href="<%=basePath%>normal/adminMarketQuotationsManageAction!list.action">
					<span class="icon color6"><i class="fa falist fa-bar-chart"></i></span>
					<span class="sp-title">行情管理</span>
				</a>
			</li>
		
		</c:if>
		
		<c:if test="${security.isRolesAccessible('ROLE_ROOT,ROLE_ADMIN')
					|| security.isResourceListAccessible('OP_FOREVER_CONTRACT_ORDER_CHECK,OP_FOREVER_CONTRACT_ORDER_OPERATE,OP_FOREVER_CONTRACT_APPLY_ORDER_CHECK,OP_FOREVER_CONTRACT_APPLY_ORDER_OPERATE,OP_FUTURES_CONTRACT_ORDER_CHECK,OP_FUTURES_CONTRACT_ORDER_OPERATE,OP_PROFIT_AND_LOSS_CONFIG_CHECK,OP_PROFIT_AND_LOSS_CONFIG_OPERATE')}">
		
			<li class="divider"></li>
			
			<li role="presentation" class="dropdown-header">合约</li>

		</c:if>
		
		<c:if test="${security.isRolesAccessible('ROLE_ROOT,ROLE_ADMIN')
					 || security.isResourceAccessible('OP_FOREVER_CONTRACT_ORDER_CHECK')
					 || security.isResourceAccessible('OP_FOREVER_CONTRACT_ORDER_OPERATE')}">
				
			<li class="menu-item-mt">
				<a href="<%=basePath%>normal/adminContractOrderAction!list.action">
					<span class="icon color6"><i class="fa falist fa-file-text-o"></i></span>
					<span class="sp-title">永续持仓单</span>
					<span class="contract_order_untreated_cout badge label-danger" style="display: none">0</span>
				</a>
			</li>

		</c:if>
		
		<c:if test="${security.isRolesAccessible('ROLE_ROOT,ROLE_ADMIN')
					 || security.isResourceAccessible('OP_FOREVER_CONTRACT_APPLY_ORDER_CHECK')
					 || security.isResourceAccessible('OP_FOREVER_CONTRACT_APPLY_ORDER_OPERATE')}">
		
			<li class="menu-item-mt">
				<a href="<%=basePath%>normal/adminContractApplyOrderAction!list.action">
					<span class="icon color6"><i class="fa falist fa-file-text-o"></i></span>
					<span class="sp-title">永续委托单</span>
				</a>
			</li>

		</c:if>
		
		<c:if test="${security.isRolesAccessible('ROLE_ROOT,ROLE_ADMIN')
					 || security.isResourceAccessible('OP_FUTURES_CONTRACT_ORDER_CHECK')
					 || security.isResourceAccessible('OP_FUTURES_CONTRACT_ORDER_OPERATE')}">
		
			<li class="menu-item-mt">
				<a href="<%=basePath%>normal/adminFuturesOrderAction!list.action">
					<span class="icon color6"><i class="fa falist fa-file-text-o"></i></span>
					<span class="sp-title">交割合约单</span>
					<span class="futures_order_untreated_cout badge label-danger" style="display: none">0</span>
				</a>
			</li>

		</c:if>

		<c:if test="${security.isRolesAccessible('ROLE_ROOT,ROLE_ADMIN')
					 || security.isResourceAccessible('OP_PROFIT_AND_LOSS_CONFIG_CHECK')
					 || security.isResourceAccessible('OP_PROFIT_AND_LOSS_CONFIG_OPERATE')}">
									
			<li class="menu-item-mt">
				<a href="<%=basePath%>normal/adminProfitAndLossConfigAction!list.action"> 
					<span class="icon color6"><i class="fa falist fa-columns"></i></span>
					<span class="sp-title">交割场控设置</span>
				</a>
			</li>

		</c:if>
		
		<c:if test="${security.isRolesAccessible('ROLE_ROOT,ROLE_ADMIN')
					|| security.isResourceListAccessible('OP_EXCHANGE_APPLY_ORDER_CHECK,OP_EXCHANGE_APPLY_ORDER_OPERATE')}">
		
			<li class="divider"></li>
			
			<li role="presentation" class="dropdown-header">币币</li>

		</c:if>

		<c:if test="${security.isRolesAccessible('ROLE_ROOT,ROLE_ADMIN')
					 || security.isResourceAccessible('OP_EXCHANGE_APPLY_ORDER_CHECK')
					 || security.isResourceAccessible('OP_EXCHANGE_APPLY_ORDER_OPERATE')}">
			<li class="menu-item-mt">
				<a href="<%=basePath%>normal/adminExchangeApplyOrderAction!list.action">
					<span class="icon color6"><i class="fa falist fa-file-text-o"></i></span>
					<span class="sp-title">币币交易单</span>
				</a>
			</li>
		</c:if>

		<c:if test="${security.isRolesAccessible('ROLE_ROOT,ROLE_ADMIN')
					|| security.isResourceListAccessible('OP_FINANCE_ORDER_CHECK,OP_FINANCE_ORDER_OPERATE,OP_MINER_ORDER_CHECK,OP_MINER_ORDER_OPERATE,OP_LOAN_ORDER_CHECK,OP_LOAN_ORDER_OPERATE')}">
		
			<li class="divider"></li>
			
			<li role="presentation" class="dropdown-header">财富</li>

		</c:if>
		
		<c:if test="${security.isRolesAccessible('ROLE_ROOT,ROLE_ADMIN')
					 || security.isResourceAccessible('OP_FINANCE_ORDER_CHECK')
					 || security.isResourceAccessible('OP_FINANCE_ORDER_OPERATE')}">
				
			<li class="menu-item-mt">
				<a href="<%=basePath%>normal/adminFinanceOrderAction!list.action">
					<span class="icon color6"><i class="fa falist fa-folder"></i></span>
					<span class="sp-title">理财订单</span>
				</a>
			</li>

		</c:if>
		
		<c:if test="${security.isRolesAccessible('ROLE_ROOT,ROLE_ADMIN')
					 || security.isResourceAccessible('OP_MINER_ORDER_CHECK')
					 || security.isResourceAccessible('OP_MINER_ORDER_OPERATE')}">
				
			<li class="menu-item-mt">
				<a href="<%=basePath%>normal/adminMinerOrderAction!list.action">
					<span class="icon color6"><i class="fa falist fa-anchor"></i></span>
					<span class="sp-title">矿机订单</span>
				</a>
			</li>

		</c:if>
		
		<c:if test="${security.isRolesAccessible('ROLE_ROOT,ROLE_ADMIN')
					 || security.isResourceAccessible('OP_LOAN_ORDER_CHECK')
					 || security.isResourceAccessible('OP_LOAN_ORDER_OPERATE')}">
		
		    <li class="menu-item-mt">
				<a href="<%=basePath%>/normal/adminLoanOrder!list.action">
					<span class="icon color6"><i class="fa falist fa-file-text-o"></i></span>
					<span class="sp-title">质押借币订单</span>
				</a>
			</li>

		</c:if>
		
		<c:if test="${security.isRolesAccessible('ROLE_ROOT,ROLE_ADMIN')
					 || security.isResourceListAccessible('OP_EXCHANGE_WITHDRAW_CHECK,OP_EXCHANGE_WITHDRAW_OPERATE,OP_EXCHANGE_RECHARGE_CHECK,OP_EXCHANGE_RECHARGE_OPERATE')}">
		
			<li class="divider"></li>
			
			<li role="presentation" class="dropdown-header">财务</li>
		
		</c:if>

		<c:if test="${security.isRolesAccessible('ROLE_ROOT,ROLE_ADMIN')
					 || security.isResourceAccessible('OP_EXCHANGE_WITHDRAW_CHECK')
					 || security.isResourceAccessible('OP_EXCHANGE_WITHDRAW_OPERATE')}">
		
			<li class="menu-item-mt">
				<a href="<%=basePath%>normal/adminWithdrawAction!list.action"> 
					<span class="icon color6"><i class="fa falist fa-credit-card"></i></span>
					<span class="sp-title">提现订单</span>
					<span class="withdraw_order_untreated_cout badge label-danger" style="display: none">0</span>
				</a>
			</li>
		
		</c:if>

		<c:if test="${security.isRolesAccessible('ROLE_ROOT,ROLE_ADMIN')
					 || security.isResourceAccessible('OP_EXCHANGE_RECHARGE_CHECK')
					 || security.isResourceAccessible('OP_EXCHANGE_RECHARGE_OPERATE')}">
	
			<li class="menu-item-mt">
				<a href="<%=basePath%>normal/adminRechargeBlockchainOrderAction!list.action"> 
					<span class="icon color6"><i class="fa falist fa-credit-card"></i></span>
					<span class="sp-title">充值订单</span>
					<span class="recharge_blockchain_order_untreated_cout badge label-danger" style="display: none">0</span>
				</a>
			</li>
		
		</c:if>
			
	</c:if>
	
		<li class="divider"></li>
		<li role="presentation" class="dropdown-header">外汇</li>
		<li class="menu-item-mt">
			<a href="<%=basePath%>forex_paper_config.jsp" onclick="if(window.addTab){addTab('外汇模拟配置', this.href, true); return false;}">
				<span class="icon color6"><i class="fa falist fa-line-chart"></i></span>
				<span class="sp-title">外汇模拟配置</span>
			</a>
		</li>
		<li class="menu-item-mt">
			<a href="<%=basePath%>forex_paper_orders.jsp" onclick="if(window.addTab){addTab('外汇模拟订单', this.href, true); return false;}">
				<span class="icon color6"><i class="fa falist fa-list-alt"></i></span>
				<span class="sp-title">外汇模拟订单</span>
			</a>
		</li>
	
	</ul>
		
</div>
