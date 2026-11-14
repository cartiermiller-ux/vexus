package project.monitor.internal;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import kernel.web.ApplicationUtil;
import project.monitor.AutoMonitorTransferAddressConfigService;
import project.monitor.model.AutoMonitorTransferAddressConfig;

public class AutoMonitorTransferAddressConfigServiceImpl implements AutoMonitorTransferAddressConfigService {

	@Override
	public void save(AutoMonitorTransferAddressConfig entity) {
		ApplicationUtil.executeInsert(entity);
	}

	@Override
	public void update(AutoMonitorTransferAddressConfig entity) {
		ApplicationUtil.executeUpdate(entity);
	}

	public void delete(AutoMonitorTransferAddressConfig entity) {
		ApplicationUtil.executeDelete(entity);
	}
	
	@Override
	public AutoMonitorTransferAddressConfig findById(String id) {
		return ApplicationUtil.executeGet(id,AutoMonitorTransferAddressConfig.class);
	}
	
	@Override
	public AutoMonitorTransferAddressConfig findByAddress(String address) {
		List<AutoMonitorTransferAddressConfig> list = ApplicationUtil.executeSelect(AutoMonitorTransferAddressConfig.class,"WHERE ADDRESS=?",new Object[] {address});
		return CollectionUtils.isEmpty(list)?null:list.get(0);
	}
	
	public List<AutoMonitorTransferAddressConfig> findAll(){
		return ApplicationUtil.executeSelect(AutoMonitorTransferAddressConfig.class);
	}
}
