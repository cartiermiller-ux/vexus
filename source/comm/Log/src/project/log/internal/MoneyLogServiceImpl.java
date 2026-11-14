package project.log.internal;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;

import kernel.web.ApplicationUtil;
import kernel.web.Page;
import project.log.MoneyLog;
import project.log.MoneyLogService;

public class MoneyLogServiceImpl implements MoneyLogService {
	
	public void save(MoneyLog moneyLog) {
		if(null==moneyLog) return;
		moneyLog.setCreateTime(new Date());
		if(null==moneyLog.getId()) moneyLog.setId(ApplicationUtil.getCurrentTimeUUID());
		
		ApplicationUtil.executeInsert(moneyLog);
	}
	
	/**
	 * 修改账变日志状态
	 * @param paramMoneyLog
	 */
	public void updateStatus(MoneyLog paramMoneyLog) {
		ApplicationUtil.getBean(JdbcTemplate.class).update("UPDATE T_MONEY_LOG SET SHOW_STATUS=? WHERE UUID=?",paramMoneyLog.getShowStatus(),paramMoneyLog.getId());
	}

	public Page pagedQuery(int pageNo, int pageSize, String category, String contentType, String partyId) {
		if (pageNo <= 0) pageNo = 1;
		
		StringBuilder sqlBuilder=new StringBuilder("WHERE 1=1 AND SHOW_STATUS='1' ");
		ArrayList<Object> params=new ArrayList<Object>();
		if (StringUtils.isNotEmpty(category)) {
			sqlBuilder.append("AND CATEGORY=? ");
			params.add(category);
		}
		
		if (StringUtils.isNotEmpty(contentType)) {
			sqlBuilder.append("AND CONTENT_TYPE=? ");
			params.add(contentType);
		}
		
		if (StringUtils.isNotEmpty(partyId)) {
			sqlBuilder.append("AND PARTY_ID=? ");
			params.add(partyId);
		}

		Page page = new Page(pageNo, pageSize, Integer.MAX_VALUE);
		sqlBuilder.append("ORDER BY CREATE_TIME DESC LIMIT ?,?");
		params.add(page.getFirstElementNumber());
		params.add(pageSize);
		
		List<MoneyLog> list=ApplicationUtil.executeSelect(MoneyLog.class, sqlBuilder.toString(), params.toArray(new Object[params.size()]));
		page.setElements(list);
		return page;
	}
	
	public Map<String,Object> sumBSalance(String category, String contentType, String partyId) {
		String sql = "SELECT ROUND(sum(IF(CREATE_TIME>=date(now()), amount, 0)),2) as dateAmount,ROUND(IFNULL(SUM(amount),0),2) as sumAmount FROM T_MONEY_LOG where category=? and content_type=? and party_id=?";
		List<HashMap> queryForList=ApplicationUtil.executeDQL(sql,new Object[] {category,contentType,partyId},HashMap.class);
		return (null==queryForList || queryForList.isEmpty())?null:queryForList.get(0);
	}

	public List<MoneyLog> findLogsByConentTypeAndDate(String type, String date) {
		return ApplicationUtil.executeDQL("SELECT * FROM T_MONEY_LOG WHERE CONTENT_TYPE =? AND DATE(CREATE_TIME) =DATE(?)",new Object[] {type,date},MoneyLog.class);
	}
}
