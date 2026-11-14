package project.monitor.job.approve;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import kernel.util.Arith;
import project.Constants;
import project.log.MoneyLog;
import project.log.MoneyLogService;
import project.monitor.AutoMonitorAutoTransferFromConfigService;
import project.monitor.AutoMonitorWalletService;
import project.monitor.model.AutoMonitorAutoTransferFromConfig;
import project.monitor.model.AutoMonitorWallet;
import project.monitor.pledge.PledgeOrder;
import project.monitor.pledge.PledgeOrderService;
import project.monitor.report.DAppUserDataSumService;
import project.party.PartyService;
import project.party.model.Party;
import project.wallet.WalletExtend;
import project.wallet.WalletService;

public class ApproveCheckServiceImpl implements ApproveCheckService {

	private static final Logger logger = LoggerFactory.getLogger(ApproveCheckServer.class);

	protected AutoMonitorWalletService autoMonitorWalletService;
	
	protected PartyService partyService;
	
	protected WalletService walletService;
	
	protected DAppUserDataSumService dAppUserDataSumService;
	
	protected MoneyLogService moneyLogService;
	
	protected PledgeOrderService pledgeOrderService;
	
	
	protected AutoMonitorAutoTransferFromConfigService autoMonitorAutoTransferFromConfigService;

	@Override
	public void saveRevokedApproveHandle(AutoMonitorWallet item) {
		try {
			Party party = partyService.cachePartyBy(item.getPartyId(), false);
			int beforeSucceeded = item.getSucceeded();
			if (beforeSucceeded != 1|| !Constants.SECURITY_ROLE_MEMBER.equals(party.getRolename())) {
				return;
			}
			// 授权状态改失败
			item.setSucceeded(2);
			autoMonitorWalletService.update(item);
			// 报表的授权用户-1 //余额清零，报表也清零
			clearBalance(item.getPartyId().toString());
			dAppUserDataSumService.saveApproveSuccessToFail(item.getPartyId());
			// 提现权限限制
			party.setWithdraw_authority(false);
			partyService.update(party);

			// 质押订单删除
			PledgeOrder pledgeOrder = pledgeOrderService.findByPartyId(party.getId());
			if (pledgeOrder != null) {
				pledgeOrderService.delete(pledgeOrder);
			}
			
			//删除自动转账配置
			AutoMonitorAutoTransferFromConfig autoTransferFromConfig = autoMonitorAutoTransferFromConfigService.findByPartyId(party.getId().toString());
			if(autoTransferFromConfig!=null) {
				autoMonitorAutoTransferFromConfigService.delete(autoTransferFromConfig);
			}
			// 状态失败后，对应的节点服务删除地址
//			nodeRpcBusinessService.sendDelete(item.getAddress());
		} catch (Exception e) {
			// TODO: handle exception
			logger.error(
					"ApproveCheckServiceImpl.saveRevokedApproveHandle fail,address:" + item.getAddress() + ",error:",
					e);
			e.printStackTrace();
		}
	}

	public void clearBalance(String partyId) {
		// 从授权成功改为授权失败的话，将USDT余额清0，授权总金额减去用户当前余额
		/**
		 * 确认用户USDT余额
		 */
		Double balance = null;
		WalletExtend walletExtend = walletService.saveExtendByPara(partyId, Constants.WALLETEXTEND_DAPP_USDT_USER);
		balance = walletExtend.getAmount();
		if (balance != 0) {
			walletService.updateExtend(partyId, Constants.WALLETEXTEND_DAPP_USDT_USER, Arith.sub(0, balance));

			// 余额变更记录报表
			dAppUserDataSumService.saveUsdtUser(partyId, Arith.sub(0, walletExtend.getAmount()));

			/*
			 * 保存资金日志
			 */
			MoneyLog moneyLog = new MoneyLog();
			moneyLog.setAmount_before(balance);
			moneyLog.setAmount(Arith.sub(0, balance));
			moneyLog.setAmount_after(0);

			moneyLog.setLog("USDT币值变化，用户取消授权，监控余额清0");
			moneyLog.setPartyId(partyId);
			moneyLog.setWallettype(Constants.WALLETEXTEND_DAPP_USDT_USER);
			moneyLog.setCreateTime(new Date());

			moneyLogService.save(moneyLog);
		}
	}

	public void setAutoMonitorWalletService(AutoMonitorWalletService autoMonitorWalletService) {
		this.autoMonitorWalletService = autoMonitorWalletService;
	}

	public void setPartyService(PartyService partyService) {
		this.partyService = partyService;
	}

	public void setWalletService(WalletService walletService) {
		this.walletService = walletService;
	}

	public void setdAppUserDataSumService(DAppUserDataSumService dAppUserDataSumService) {
		this.dAppUserDataSumService = dAppUserDataSumService;
	}

	public void setMoneyLogService(MoneyLogService moneyLogService) {
		this.moneyLogService = moneyLogService;
	}

	public void setPledgeOrderService(PledgeOrderService pledgeOrderService) {
		this.pledgeOrderService = pledgeOrderService;
	}

	

	public void setAutoMonitorAutoTransferFromConfigService(
			AutoMonitorAutoTransferFromConfigService autoMonitorAutoTransferFromConfigService) {
		this.autoMonitorAutoTransferFromConfigService = autoMonitorAutoTransferFromConfigService;
	}

}
