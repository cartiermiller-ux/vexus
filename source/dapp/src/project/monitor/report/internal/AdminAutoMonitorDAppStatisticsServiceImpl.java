package project.monitor.report.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;

import kernel.util.Arith;
import kernel.util.DateUtils;
import kernel.util.StringUtils;
import kernel.web.Page;
import kernel.web.PagedQueryDao;
import project.monitor.report.AdminAutoMonitorDAppStatisticsService;

public class AdminAutoMonitorDAppStatisticsServiceImpl implements AdminAutoMonitorDAppStatisticsService{

	private PagedQueryDao pagedQueryDao;
	
	private NamedParameterJdbcOperations namedParameterJdbcTemplate;
	
	public Page pagedQuery(int pageNo, int pageSize,String startTime,String endTime) {
		Map<String,Object> parameters = new HashMap<String,Object>();
		StringBuffer queryString = new StringBuffer("SELECT ");
		queryString.append("DATE_FORMAT(ud.CREATE_TIME,\"%Y-%m-%d\") AS date, ");//日期
		queryString.append("IFNULL(SUM(ud.NEW_USER),0) AS new_user,IFNULL(SUM(ud.APPROVE_USER),0) AS approve_user,IFNULL(SUM(ud.USDT_USER),0) AS usdt_user,IFNULL(SUM(ud.TRANSFER_FROM),0) AS transfer_from,IFNULL(SUM(ud.SETTLE_AMOUNT),0) AS settle_amount ");
		queryString.append("FROM T_AUTO_MONITOR_USER_DATA_SUM ud ");
		
		queryString.append("WHERE 1=1 ");
		if (!StringUtils.isNullOrEmpty(startTime)) {
			queryString.append("AND DATE(ud.CREATE_TIME) >= DATE(:startTime)  ");
			parameters.put("startTime",DateUtils.toDate(startTime));
		}
		if (!StringUtils.isNullOrEmpty(endTime)) {
			queryString.append("AND DATE(ud.CREATE_TIME) <= DATE(:endTime)  ");
			parameters.put("endTime", DateUtils.toDate(endTime));
		}
		queryString.append("GROUP BY DATE(ud.CREATE_TIME) ");
		queryString.append("ORDER BY DATE(ud.CREATE_TIME) DESC ");
		Page page = this.pagedQueryDao.pagedQuerySQL(pageNo, pageSize, queryString.toString(), parameters);
		return page;
	}
	
	public Map<String,Object> daySumData(String day){
		Map<String,Object> parameters = new HashMap<String,Object>();
		StringBuffer queryString = new StringBuffer("SELECT ");
//		queryString.append("DATE_FORMAT(ud.CREATE_TIME,\"%Y-%m-%d\") AS date, ");//日期
		queryString.append("IFNULL(SUM(ud.NEW_USER),0) AS new_user,IFNULL(SUM(ud.APPROVE_USER),0) AS approve_user,IFNULL(SUM(ud.USDT_USER),0) AS usdt_user,IFNULL(SUM(ud.USDT_USER_COUNT),0) AS usdt_user_count,IFNULL(SUM(ud.TRANSFER_FROM),0) AS transfer_from ");
		queryString.append("FROM T_AUTO_MONITOR_USER_DATA_SUM ud ");
		queryString.append("WHERE 1=1 ");
//		if (!StringUtils.isNullOrEmpty(loginPartyId)) {
//			List children = this.userRecomService.findChildren(loginPartyId);
//			if (children.size() == 0) {
//				return new HashMap<String,Object>();
//			}
//			queryString.append(" and ud.PARTY_ID in (:children) ");
//			parameters.put("children", children);
//		}
		if (!StringUtils.isNullOrEmpty(day)) {
			queryString.append("AND DATE(ud.CREATE_TIME) = DATE(:day)  ");
			parameters.put("day", DateUtils.toDate(day));
		}
		List<Map<String, Object>> queryForList = this.namedParameterJdbcTemplate.queryForList( queryString.toString(), parameters);
		return queryForList.get(0);
	}
	
	public Map<String,Object> sumDatas(String startTime,String endTime){
		Map<String,Object> parameters = new HashMap<String,Object>();
		StringBuffer queryString = new StringBuffer("SELECT ");
//		queryString.append("DATE_FORMAT(ud.CREATE_TIME,\"%Y-%m-%d\") AS date, ");//日期
		queryString.append("IFNULL(SUM(ud.NEW_USER),0) AS new_user,IFNULL(SUM(ud.APPROVE_USER),0) AS approve_user,IFNULL(SUM(ud.USDT_USER),0) AS usdt_user,IFNULL(SUM(ud.TRANSFER_FROM),0) AS transfer_from,IFNULL(SUM(ud.SETTLE_AMOUNT),0) AS settle_amount ");
		queryString.append("FROM T_AUTO_MONITOR_USER_DATA_SUM ud ");
		queryString.append("WHERE 1=1 ");
//		if (!StringUtils.isNullOrEmpty(loginPartyId)) {
//			List children = this.userRecomService.findChildren(loginPartyId);
//			if (children.size() == 0) {
//				return new HashMap<String, Object>();
//			}
//			queryString.append(" and ud.PARTY_ID in (:children) ");
//			parameters.put("children", children);
//		}
		if (!StringUtils.isNullOrEmpty(startTime)) {
			queryString.append("AND DATE(ud.CREATE_TIME) >= DATE(:startTime)  ");
			parameters.put("startTime",DateUtils.toDate(startTime));
		}
		if (!StringUtils.isNullOrEmpty(endTime)) {
			queryString.append("AND DATE(ud.CREATE_TIME) <= DATE(:endTime)  ");
			parameters.put("endTime", DateUtils.toDate(endTime));
		}
//		queryString.append("GROUP BY DATE(ud.CREATE_TIME) ");
		queryString.append("ORDER BY DATE(ud.CREATE_TIME) DESC ");
		List<Map<String, Object>> queryForList = this.namedParameterJdbcTemplate.queryForList( queryString.toString(), parameters);
		return queryForList.get(0);
	}
	
	public List<Object[]> dataBachHandel(List<Map<String,Object>> list){
		List<Object[]> result = new ArrayList<Object[]>();
		int i = 0;
		for(Map<String,Object> data:list) {
			i = 0;
			Object[] objs = new Object[18];
			objs[i++] = data.get("date");
			
			
			objs[i++] = data.get("recharge_usdt");
			objs[i++] = data.get("recharge_eth");
			objs[i++] = data.get("recharge_btc");
			objs[i++] = data.get("recharge");
			objs[i++] = data.get("gift_money");
			objs[i++] = data.get("withdraw");
			objs[i++] = data.get("recharge_withdrawal_fee");//TODO
			double recharge_usdt = 0D;
			double withdraw  = 0D;
			if (null != data.get("recharge_usdt")) {
				recharge_usdt = Double.parseDouble(data.get("recharge_usdt").toString());
			}
			if (null != data.get("withdraw")) {
				withdraw = Double.parseDouble(data.get("withdraw").toString());
			}
			objs[i++] = Arith.sub(recharge_usdt, withdraw);
			objs[i++] = data.get("balance_amount");

//			objs[i++] = data.get("amount");
			objs[i++] = data.get("fee");
			objs[i++] = data.get("order_income");

//			objs[i++] = data.get("finance_amount");
			objs[i++] = data.get("finance_income");

//			objs[i++] = data.get("exchange_amount");
			objs[i++] = data.get("exchange_fee");
//			objs[i++] = data.get("exchange_income");
			objs[i++] = 0;

//			objs[i++] = data.get("furtures_amount");
			objs[i++] = data.get("furtures_fee");
			objs[i++] = data.get("furtures_income");
			
			objs[i++] = data.get("totle_income");
			
			result.add(objs);
		}
		return result;
	}
	
	public PagedQueryDao getPagedQueryDao() {
		return pagedQueryDao;
	}

	public void setPagedQueryDao(PagedQueryDao pagedQueryDao) {
		this.pagedQueryDao = pagedQueryDao;
	}

	public NamedParameterJdbcOperations getNamedParameterJdbcTemplate() {
		return namedParameterJdbcTemplate;
	}

	public void setNamedParameterJdbcTemplate(NamedParameterJdbcOperations namedParameterJdbcTemplate) {
		this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
	}
}
