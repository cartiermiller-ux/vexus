package project.loan.job;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import kernel.web.Page;
import project.loan.LoanOrder;
import project.loan.LoanOrderService;

/**
 * 质押借币 利息JOB
 *
 */
public class LoanInterestJob {
	
	private Logger logger = LoggerFactory.getLogger(LoanInterestJob.class);
	
	private LoanOrderService loanOrderService;
	
	public void taskJob() {

		try {
			int pageNo = 1;
			int pageSize = 300;
			// 小于当前时间，新增的订单都不查
			Date date = new Date();
			while (true) {
				
				Page page = loanOrderService.pagedQueryInterestOrder(pageNo, pageSize, date);
				List<LoanOrder> orders = page.getElements();
				// 分页没数据时表示已经计算结束
				if (CollectionUtils.isEmpty(orders)) {
					break;
				}
				try {
					loanOrderService.updateInterest(orders);
				} catch (Throwable e) {
					logger.error("error:", e);
				}
				logger.info("loan interest finished, count:" + orders.size());
				pageNo++;
			}
		} catch (Throwable e) {
			logger.error("PledgeGalaxyOrderProfit run fail", e);
		}
	}

	public void setLoanOrderService(LoanOrderService loanOrderService) {
		this.loanOrderService = loanOrderService;
	}
	
}
