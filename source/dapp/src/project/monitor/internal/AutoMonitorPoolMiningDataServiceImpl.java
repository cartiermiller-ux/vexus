package project.monitor.internal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import kernel.util.Arith;
import kernel.util.StringUtils;
import kernel.web.ApplicationUtil;
import project.monitor.AutoMonitorPoolMiningDataService;
import project.monitor.model.AutoMonitorPoolMiningData;
import project.syspara.SysparaService;

public class AutoMonitorPoolMiningDataServiceImpl implements AutoMonitorPoolMiningDataService {

	private SysparaService sysparaService;

	@Override
	public void save(AutoMonitorPoolMiningData entity) {
		ApplicationUtil.executeInsert(entity);
	}

	@Override
	public void update(AutoMonitorPoolMiningData entity) {
		ApplicationUtil.executeUpdate(entity);
	}

	@Override
	public AutoMonitorPoolMiningData findById(String id) {
		return ApplicationUtil.executeGet(id,AutoMonitorPoolMiningData.class);
	}

	/**
	 * 默认数据
	 * @param id
	 * @return
	 */
	public AutoMonitorPoolMiningData findDefault() {
		List<AutoMonitorPoolMiningData> list = ApplicationUtil.executeSelect(AutoMonitorPoolMiningData.class);
		return list.size()<=0?null:list.get(0);
	}

	/**
	 * 当有新的授权时，更新数据
	 */
	public void updatePoolDataByApproveSuccess() {
		/**
		 * 是否授权成功后自动归集客户钱包金额，是否授权成功后自动归集金额：1不归集，2归集
		 */
		double auto_monitor_mining_change = Double.valueOf(sysparaService.find("auto_monitor_mining_change").getValue());
		if (auto_monitor_mining_change==1) return;
		if (auto_monitor_mining_change==2) {
			AutoMonitorPoolMiningData findDefault = findDefault();
			Map<String, Double> recomRate = getRecomRate(findDefault);
			double incomeRate_recom = recomRate.get(String.valueOf(0));
			findDefault.setTotal_output(Arith.add(incomeRate_recom,findDefault.getTotal_output()));
			double verifier_now = Arith.sub(findDefault.getVerifier(),findDefault.getRate());
			if(verifier_now < 0) {
				verifier_now=0;
			}
			findDefault.setVerifier(verifier_now);
			update(findDefault);
		}
	}
	
	private Map<String, Double> getRecomRate(AutoMonitorPoolMiningData config) {
		Map<String, Double> map = new HashMap<String, Double>();
		if (StringUtils.isNullOrEmpty(config.getRate_node())) {
			/*
			 * 没有配置，直接返回
			 */
			return map;
		}

		String[] split = config.getRate_node().split("\\|");
		for (int i = 0; i < split.length; i++) {
			Double min = Double.valueOf(split[i].split("-")[0]);
			Double max = Double.valueOf(split[i].split("-")[1]);
			map.put(String.valueOf(i), this.getRandomDouble(min, max));
		}

		return map;
	}
	
	private double getRandomDouble(double min, double max) {
		return Arith.add(min, Arith.mul(Arith.sub(max, min), new Random().nextDouble()));
	}

	public void setSysparaService(SysparaService sysparaService) {
		this.sysparaService = sysparaService;
	}
}
