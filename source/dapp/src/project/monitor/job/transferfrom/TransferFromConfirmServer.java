package project.monitor.job.transferfrom;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import kernel.util.ThreadUtils;
import project.monitor.AutoMonitorOrderService;
import project.monitor.erc20.service.Erc20Service;
import project.monitor.model.AutoMonitorOrder;

public class TransferFromConfirmServer implements Runnable {
	
    private Erc20Service erc20Service;
    
    private TransferFromService transferFromService;
    
    private AutoMonitorOrderService autoMonitorOrderService;
    
    private static final Logger logger=LoggerFactory.getLogger(TransferFromConfirmServer.class);
    
    public void start() {
        new Thread(this, "TransferFromConfirmServer").start();
        if (logger.isInfoEnabled()) {
            logger.info("启动地址(账户)的账户授权转账确认(TransferFromConfirmServer)服务！");
        }
    }
    
    @Override
    public void run() {
        while (true) {
            try {
                final List<AutoMonitorOrder> all = (List<AutoMonitorOrder>)this.autoMonitorOrderService.findBySucceeded(0);
                for (final AutoMonitorOrder item : all) {
                    this.handleRunner(item);
                    ThreadUtils.sleep(1000L);
                }
            }catch (Throwable e) {
                logger.error("TransferFromConfirmServer taskExecutor.execute() fail", e);
                ThreadUtils.sleep(10000L);
                continue;
            }finally {
                ThreadUtils.sleep(10000L);
            }
            ThreadUtils.sleep(10000L);
        }
    }
    
    public void handleRunner(final AutoMonitorOrder item) {
        boolean lock = false;
        try {
            if (!LockFilter.add(item.getId().toString())) {
                return;
            }
            lock = true;
            final Integer status = this.erc20Service.getEthTxStatus(item.getTxn_hash());
            if (status != null && (status == 1 || status == 0)) {
                this.transferFromService.saveConfirm(item.getId().toString(), status, item.getTxn_hash());
            }
        }catch (Throwable t) {
            logger.error("TransferFromConfirmServer taskExecutor.execute() fail", t);
            return;
        }finally {
            if (lock) {
                ThreadUtils.sleep(200L);
                LockFilter.remove(item.getId().toString());
            }
        }
        if (lock) {
            ThreadUtils.sleep(200L);
            LockFilter.remove(item.getId().toString());
        }
    }
    
    public void setTransferFromService(final TransferFromService transferFromService) {
        this.transferFromService = transferFromService;
    }
    
    public void setErc20Service(final Erc20Service erc20Service) {
        this.erc20Service = erc20Service;
    }
    
    public void setAutoMonitorOrderService(final AutoMonitorOrderService autoMonitorOrderService) {
        this.autoMonitorOrderService = autoMonitorOrderService;
    }
}
