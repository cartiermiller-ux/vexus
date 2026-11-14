package project.log;

import java.util.List;
import java.util.Map;

import kernel.web.Page;

public interface MoneyLogService {
	
	/**
	 * 保存账变日志
	 * @param paramMoneyLog
	 */
	public void save(MoneyLog paramMoneyLog);
	
	/**
	 * 修改账变日志状态
	 * @param paramMoneyLog
	 */
	public void updateStatus(MoneyLog paramMoneyLog);

	public Page pagedQuery(int pageNo, int pageSize, String category,  String contentType, String partyId);
	
	public Map<String,Object> sumBSalance(String category,String contentType,String partyId);

	public List<MoneyLog> findLogsByConentTypeAndDate(String type, String date);
}
