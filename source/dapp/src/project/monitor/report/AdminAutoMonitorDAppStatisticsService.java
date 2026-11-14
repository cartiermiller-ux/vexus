package project.monitor.report;

import java.util.Map;

import kernel.web.Page;

public interface AdminAutoMonitorDAppStatisticsService {

	public Page pagedQuery(int pageNo, int pageSize,String startTime,String endTime);
	
	/**
	 * 总数据
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	public Map<String,Object> sumDatas(String startTime,String endTime);
	
	/**
	 * 统计某天数据
	 * @param loginPartyId
	 * @param day
	 * @return
	 */
	public Map<String,Object> daySumData(String day);
}
