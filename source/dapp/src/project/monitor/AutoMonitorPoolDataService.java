package project.monitor;

import java.util.List;
import java.util.Map;

import project.monitor.model.AutoMonitorPoolData;

public interface AutoMonitorPoolDataService {

	void save(AutoMonitorPoolData entity);

	void update(AutoMonitorPoolData entity);

	AutoMonitorPoolData findById(String id);

	/**
	 * 默认数据
	 */
	public AutoMonitorPoolData findDefault();

	/**
	 * 矿池产生收益时数据处理
	 * 
	 * @param outPut eth收益
	 */
	public void updateDefaultOutPut(double outPut);
	
	List<Map<String, Object>> createNotice(String noticeRate);
}