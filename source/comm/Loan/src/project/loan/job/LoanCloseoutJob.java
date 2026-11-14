package project.loan.job;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import kernel.util.ThreadUtils;
import project.loan.LoanConstants;
import project.loan.LoanOrder;
import project.loan.LoanOrderService;
import util.LockFilter;

/**
 * 定时计算强平
 *
 */
public class LoanCloseoutJob implements Runnable {
	
	private Logger logger = LoggerFactory.getLogger(LoanCloseoutJob.class);
	
	protected LoanOrderService loanOrderService;
	
	public void start() {
		new Thread(this, "LoanCloseoutJob").start();
	}
	
	public void run() {
		while (true) {
			try {
				List<String> list = new ArrayList<>();
				List<LoanOrder> orders = loanOrderService.cacheOrders();
				if (null != orders && orders.size() > 0) {
					for (LoanOrder order : orders) {
						if (LoanConstants.PLEDGE_ORDER_STATE_SETTLE == order.getState() 
								|| LoanConstants.PLEDGE_ORDER_STATE_CLOSEOUT == order.getState()) {
							// 加入移除列表
							list.add(order.getId());
							continue;
						}
						boolean lock = false;
						try {
							if (!LockFilter.add(order.getId())) {
								return;
							}
							lock = true;
							Map<String, Double> rateMap = loanOrderService.calculatePledgeRate(order.getPledgeCurrency(), order.getDebtAmount(), order.getPledgeAmount());
							// 达到强平率
							if (rateMap.get("pledgeRate") >= LoanConstants.PLEDGE_RATE_CLOSEOUT) {
								loanOrderService.updateCloseout(order, rateMap.get("pledgeRate"));
								// 加入移除列表
								list.add(order.getId());
							}
						} catch (Throwable t) {
							logger.error("LoanCloseoutJob taskExecutor.execute() fail", t);
						} finally {
							if (lock) {
								ThreadUtils.sleep(200);
								LockFilter.remove(order.getId());
							}
						}
					}
					
					loanOrderService.cacheRemoveOrders(list);
				}
			} catch (Throwable e) {
				logger.error("LoanCloseoutJob taskExecutor.execute() fail", e);
			}finally {
				ThreadUtils.sleep(1000 * 30);
			}
		}
	}

	public void setLoanOrderService(LoanOrderService loanOrderService) {
		this.loanOrderService = loanOrderService;
	}
	
	
}
