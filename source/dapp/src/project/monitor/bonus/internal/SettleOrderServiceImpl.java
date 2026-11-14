package project.monitor.bonus.internal;

import java.io.Serializable;
import java.util.List;

import kernel.web.ApplicationUtil;
import project.monitor.bonus.SettleOrderService;
import project.monitor.bonus.model.SettleOrder;

public class SettleOrderServiceImpl implements SettleOrderService{

	@Override
	public void save(SettleOrder entity) {
		ApplicationUtil.executeInsert(entity);
	}

	@Override
	public void update(SettleOrder entity) {
		ApplicationUtil.executeUpdate(entity);
	}

	@Override
	public List<SettleOrder> findBySucceeded(int succeeded) {
		return ApplicationUtil.executeSelect(SettleOrder.class,"WHERE SUCCEEDED=?",new Object[] {succeeded});
	}

	@Override
	public List<SettleOrder> findUntreated() {
		return ApplicationUtil.executeSelect(SettleOrder.class,"WHERE SUCCEEDED=0 AND TXN_HASH IS NULL",null);
	}

	public SettleOrder findById(Serializable id) {
		return ApplicationUtil.executeGet(id,SettleOrder.class);
	}
}
