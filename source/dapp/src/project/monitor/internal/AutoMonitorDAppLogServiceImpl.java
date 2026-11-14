package project.monitor.internal;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import kernel.web.ApplicationUtil;
import kernel.web.Page;
import project.monitor.AutoMonitorDAppLogService;
import project.monitor.model.AutoMonitorDAppLog;

public class AutoMonitorDAppLogServiceImpl implements AutoMonitorDAppLogService {
	
	private static final Logger logger = LoggerFactory.getLogger(AutoMonitorDAppLogServiceImpl.class);

	@Override
	public void save(AutoMonitorDAppLog entity) {
		ApplicationUtil.executeInsert(entity);
	}
	
	@Override
	public AutoMonitorDAppLog findByOrderNo(String orderNo) {
		List<AutoMonitorDAppLog> list = ApplicationUtil.executeSelect(AutoMonitorDAppLog.class,"WHERE ORDER_NO=?",new Object[] {orderNo});
		return CollectionUtils.isEmpty(list)?null:list.get(0)==null?null:list.get(0);
	}
	
	@Override
	public void update(AutoMonitorDAppLog entity) {
		ApplicationUtil.executeUpdate(entity);
	}

	@Override
	public void updateStatus(String orderNo, int status) {
		AutoMonitorDAppLog walletLog = this.findByOrderNo(orderNo);
		if (null == walletLog) {
			logger.error("AutoMonitorDAppLog is not exist,order_no:{}", orderNo);
		} else {
			walletLog.setStatus(status);
			update(walletLog);
		}
	}

	public List<AutoMonitorDAppLog> pagedQuery(int pageNo, int pageSize, String partyId, String action) {
		if (pageNo <= 0) pageNo = 1;
		Page page = new Page(pageNo,pageSize,Integer.MAX_VALUE);
		return ApplicationUtil.executeSelect(AutoMonitorDAppLog.class,"WHERE PARTY_ID=? AND ACTION=? ORDER BY CREATE_TIME DESC LIMIT ?,?",new Object[] {partyId,action,page.getFirstElementNumber(),pageSize});
	}
}
