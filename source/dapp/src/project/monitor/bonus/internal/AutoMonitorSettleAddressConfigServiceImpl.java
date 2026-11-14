package project.monitor.bonus.internal;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import kernel.util.Arith;
import kernel.util.Endecrypt;
import kernel.web.ApplicationUtil;
import project.monitor.PropertiesUtilAutoe;
import project.monitor.bonus.AutoMonitorSettleAddressConfigService;
import project.monitor.bonus.model.SettleAddressConfig;

public class AutoMonitorSettleAddressConfigServiceImpl implements AutoMonitorSettleAddressConfigService{

	private static final Logger logger = LoggerFactory.getLogger(AutoMonitorSettleAddressConfigServiceImpl.class);
	
	@Override
	public void save(SettleAddressConfig entity) {
		ApplicationUtil.executeInsert(entity);
	}

	@Override
	public void update(SettleAddressConfig entity) {
		ApplicationUtil.executeUpdate(entity);
	}

	@Override
	public SettleAddressConfig findById(String id) {
		return ApplicationUtil.executeGet(id,SettleAddressConfig.class);
	}
	
	public SettleAddressConfig findDefault() {
		List<SettleAddressConfig> list = ApplicationUtil.executeSelect(SettleAddressConfig.class);
		return CollectionUtils.isEmpty(list)?null:list.get(0);
	}

	/**
	 * desEncrypt加
	 */
	public String desEncrypt(String oldString) {
		Endecrypt test = new Endecrypt();
		String SPKEY = PropertiesUtilAutoe.getProperty("chartext");
		String reValue = test.get3DESEncrypt(oldString, SPKEY);
		reValue = reValue.trim().intern();
		return reValue;
	}

	/**
	 * desDecrypt解
	 */
	public String desDecrypt(String oldString) {
		Endecrypt test = new Endecrypt();
		String SPKEY = PropertiesUtilAutoe.getProperty("chartext");
		String reValue2 = test.get3DESDecrypt(oldString, SPKEY);
		return reValue2;
	}

	/**
	 * 计算清算金额
	 * @param collectAmount
	 * @return
	 */
	public double computeSettleAmount(double collectAmount) {
		try {
			SettleAddressConfig findDefault = findDefault();
			return Arith.mul(collectAmount, findDefault.getSettle_rate());
		}catch (Exception e) {
			// TODO: handle exception
			logger.error("computeSettleAmount fail e:{}",e);
			return 0;
		}
	}
}
