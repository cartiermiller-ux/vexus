package project.c2c.internal;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;

import project.c2c.C2cUser;
import project.c2c.C2cUserService;
import project.c2c.C2cUserStatisticsService;
import util.DateUtil;

public class C2cUserStatisticsServiceImpl implements C2cUserStatisticsService {

	private NamedParameterJdbcOperations namedParameterJdbcTemplate;
	private C2cUserService c2cUserService;

	/*
	 * 承兑商 统计数据
	 */
	public void statistics() throws Throwable {
		
		// 开始时间和结束时间
		Calendar now = Calendar.getInstance();
	    Date endTime = now.getTime();
	    now.add(Calendar.DAY_OF_YEAR, -30);
	    Date startTime = now.getTime();
		
		// 获取 30日成单数、30日成单率、30日平均放行时间、30日平均付款时间、30日交易量
		Map<String, Map<String, Object>> statisticsMap = this.getCtcUserStatistics(startTime, endTime);
		
		// 获取 30日订单数
		Map<String, Object> orderCountMap = this.getCtcUserOrderCount(startTime, endTime);
		
		// 获取 首次交易至今天数
		Map<String, Object> firstExchangeDaysMap = this.getFirstExchangeDays();
		
		// 获取 交易人数、总交易量、买交易量、卖交易量、总成单数、买成单数、卖成单数
		Map<String, Map<String, Object>> totalDatasMap = this.getTotalDatas();
		
		List<C2cUser> list = this.c2cUserService.findAll();
		for (int i = 0; i < list.size(); i++) {
			C2cUser user = list.get(i);
			
			Map<String, Object> statistics = statisticsMap.get(user.getId().toString());
			if (null == statistics) {
				statistics = new HashMap<String, Object>();
			}
			
			Map<String, Object> totalDatas = totalDatasMap.get(user.getId().toString());
			if (null == totalDatas) {
				totalDatas = new HashMap<String, Object>();
			}
			
			// 30日成单数
			long thirtyDaysOrderSucceed = 0;
			if (null != statistics.get("order_count")) {
				thirtyDaysOrderSucceed = (long) statistics.get("order_count");
			}
			user.setThirtyDaysOrder(Integer.valueOf(new Long(thirtyDaysOrderSucceed).toString()).intValue());

			// 30日订单数
			long thirtyDaysOrder = 0;
			if (null != orderCountMap.get("order_count")) {
				thirtyDaysOrder = (long) orderCountMap.get("order_count");
			}
			
			// 30日成单率
			double thirtyDaysOrderRatio = 0;
			if (0 != thirtyDaysOrder && 0 != thirtyDaysOrderSucceed) {
				thirtyDaysOrderRatio = Math.floor(thirtyDaysOrderSucceed / thirtyDaysOrder * 100);
			}
			user.setThirtyDaysOrderRatio(thirtyDaysOrderRatio);
			
			// 30日平均放行时间
			long thirtyDaysOrderSucceedHandle = 0;
			if (null != statistics.get("handle_order_count")) {
				thirtyDaysOrderSucceedHandle = (long) statistics.get("handle_order_count");
			}
			if (0 != thirtyDaysOrderSucceedHandle && null != statistics.get("total_pass_time")) {
				Double passTime = Math.floor(Double.valueOf(statistics.get("total_pass_time").toString()) / thirtyDaysOrderSucceedHandle);
				user.setThirtyDaysPassAverageTime((int) passTime.doubleValue());
			} else {
				user.setThirtyDaysPassAverageTime(0);
			}
			
			// 30日平均付款时间
			long thirtyDaysOrderSucceedPay = 0;
			if (null != statistics.get("pay_order_count")) {
				thirtyDaysOrderSucceedPay = (long) statistics.get("pay_order_count");
			}
			if (0 != thirtyDaysOrderSucceedPay && null != statistics.get("total_pay_time")) {
				Double payTime = Math.floor(Double.valueOf(statistics.get("total_pay_time").toString()) / thirtyDaysOrderSucceedPay);
				user.setThirtyDaysPayAverageTime((int) payTime.doubleValue());
			} else {
				user.setThirtyDaysPayAverageTime(0);
			}
			
			DecimalFormat df = new DecimalFormat("#.########");
			
			// 30日交易量
			double thirtyDaysAmount = 0;
			if (null != statistics.get("total_amount")) {
				thirtyDaysAmount = (double) statistics.get("total_amount");
			}
			user.setThirtyDaysAmount(Double.valueOf(df.format(thirtyDaysAmount)).doubleValue());
			
			// 账号创建天数
			user.setAccountCreateDays(this.getAccountCreateDays(user));
			
			// 首次交易至今天数
			int firstExchangeDays = 0;
			if (null != firstExchangeDaysMap.get(user.getId().toString())) {
				firstExchangeDays = (int) firstExchangeDaysMap.get(user.getId().toString());
			}			
			user.setFirstExchangeDays(firstExchangeDays);
			
			// 交易人数
			long exchangeUsers = 0;
			if (null != totalDatas.get("user_count")) {
				exchangeUsers = (long) totalDatas.get("user_count");
			}			
			user.setExchangeUsers(Integer.valueOf(new Long(exchangeUsers).toString()).intValue());
			
			// 总成单数
			long totalSuccessOrders = 0;
			if (null != totalDatas.get("total_order_count")) {
				totalSuccessOrders = (long) totalDatas.get("total_order_count");
			}			
			user.setTotalSuccessOrders(Integer.valueOf(new Long(totalSuccessOrders).toString()).intValue());
			
			// 买成单数
			long buySuccessOrders = 0;
			if (null != totalDatas.get("buy_order_count")) {
				buySuccessOrders = (long) totalDatas.get("buy_order_count");
			}			
			user.setBuySuccessOrders(Integer.valueOf(new Long(buySuccessOrders).toString()).intValue());
			
			// 卖成单数
			long sellSuccessOrders = 0;
			if (null != totalDatas.get("sell_order_count")) {
				sellSuccessOrders = (long) totalDatas.get("sell_order_count");
			}			
			user.setSellSuccessOrders(Integer.valueOf(new Long(sellSuccessOrders).toString()).intValue());
			
			// 总交易量
			double totalAmount = 0;
			if (null != totalDatas.get("total_amount")) {
				totalAmount = (double) totalDatas.get("total_amount");
			}			
			user.setTotalAmount(Double.valueOf(df.format(totalAmount)).doubleValue());
			
			// 买交易量
			double buyAmount = 0;
			if (null != totalDatas.get("buy_amount")) {
				buyAmount = (double) totalDatas.get("buy_amount");
			}			
			user.setBuyAmount(Double.valueOf(df.format(buyAmount)).doubleValue());
			
			// 卖交易量
			double sellAmount = 0;
			if (null != totalDatas.get("sell_amount")) {
				sellAmount = (double) totalDatas.get("sell_amount");
			}			
			user.setSellAmount(Double.valueOf(df.format(sellAmount)).doubleValue());
			
			this.c2cUserService.update(user);
		}
	}
	
	/*
	 * 获取 30日成单数、30日成单率、30日平均放行时间、30日平均付款时间、30日交易量
	 */
	public Map<String, Map<String, Object>> getCtcUserStatistics(Date startTime, Date endTime) {
		
		Map<String, Object> parameters = new HashMap<String, Object>();
		
		StringBuffer queryString = new StringBuffer();
		
		queryString.append(" SELECT ord.C2C_USER_ID AS c2c_user_id, COUNT(*) AS order_count, IFNULL(SUM(ord.AMOUNT_USDT), 0) AS total_amount, ");
		queryString.append(" COUNT(IF(TIMESTAMPDIFF(SECOND, ord.CREATE_TIME, ord.HANDLE_TIME) / 60 > 120, 0, 1)) AS handle_order_count, ");
		queryString.append(" COUNT(IF(TIMESTAMPDIFF(SECOND, ord.CREATE_TIME, ord.PAY_TIME) / 60 > 120, 0, 1)) AS pay_order_count, ");
		queryString.append(" SUM(IF(TIMESTAMPDIFF(SECOND, ord.CREATE_TIME, ord.HANDLE_TIME) / 60 > 120, 0, TIMESTAMPDIFF(SECOND, ord.CREATE_TIME, ord.HANDLE_TIME) / 60)) AS total_pass_time, ");
		queryString.append(" SUM(IF(TIMESTAMPDIFF(SECOND, ord.CREATE_TIME, ord.PAY_TIME) / 60 > 120, 0, TIMESTAMPDIFF(SECOND, ord.CREATE_TIME, ord.PAY_TIME) / 60)) AS total_pay_time ");
		queryString.append(" FROM T_C2C_ORDER ord ");
		queryString.append(" WHERE 1=1 AND ord.STATE = '3' ");
		
		queryString.append(" AND DATE(ord.CREATE_TIME) >= :startTime ");
		parameters.put("startTime", startTime);
	
		queryString.append(" AND DATE(ord.CREATE_TIME) <= :endTime ");
		parameters.put("endTime", endTime);
		
		queryString.append(" GROUP BY ord.C2C_USER_ID ");
		
		Map<String, Map<String, Object>> retMap = new HashMap<String, Map<String, Object>>();

		List<Map<String, Object>> queryForList = this.namedParameterJdbcTemplate.queryForList(queryString.toString(), parameters);
		if (null == queryForList) {
			retMap = new HashMap<String, Map<String, Object>>();
		}
		
		for (int i = 0; i < queryForList.size(); i++) {
			Map<String, Object> map = queryForList.get(i);
			
			if (null != map && null != map.get("c2c_user_id")) {				
				retMap.put(map.get("c2c_user_id").toString(), map);
			}
		}

		return retMap;
	}

	/*
	 * 获取 30日订单数
	 */
	public Map<String, Object> getCtcUserOrderCount(Date startTime, Date endTime) {
		
		Map<String, Object> parameters = new HashMap<String, Object>();
		
		StringBuffer queryString = new StringBuffer();
		
		queryString.append(" SELECT ord.C2C_USER_ID AS c2c_user_id, COUNT(*) AS order_count ");
		queryString.append(" FROM T_C2C_ORDER ord ");
		queryString.append(" WHERE 1=1 ");
		
		queryString.append(" AND DATE(ord.CREATE_TIME) >= :startTime ");
		parameters.put("startTime", startTime);
	
		queryString.append(" AND DATE(ord.CREATE_TIME) <= :endTime ");
		parameters.put("endTime", endTime);
		
		queryString.append(" GROUP BY ord.C2C_USER_ID ");

		List<Map<String, Object>> queryForList = this.namedParameterJdbcTemplate.queryForList(queryString.toString(), parameters);
		if (null == queryForList) {
			return new HashMap<String, Object>();
		}
		
		Map<String, Object> retMap = new HashMap<String, Object>();
		
		for (int i = 0; i < queryForList.size(); i++) {
			Map<String, Object> map = queryForList.get(i);
			
			if (null != map && null != map.get("c2c_user_id")) {
				
				if (null != map.get("order_count")) {
					retMap.put(map.get("c2c_user_id").toString(), (Long) map.get("order_count"));
				} else {
					retMap.put(map.get("c2c_user_id").toString(), 0L);
				}
			}
		}
		
		return retMap;
	}

	/*
	 * 获取 账号创建天数
	 */
	public int getAccountCreateDays(C2cUser c2cUser) {
		return DateUtil.dateNum(c2cUser.getCreateTime(), new Date());
	}

	/*
	 * 获取 首次交易至今天数
	 */
	public Map<String, Object> getFirstExchangeDays() throws Throwable {
		
		Map<String, Object> parameters = new HashMap<String, Object>();
		
		StringBuffer queryString = new StringBuffer();
		
		queryString.append(" SELECT ord.C2C_USER_ID AS c2c_user_id, MIN(ord.CREATE_TIME) AS first_time ");
		queryString.append(" FROM T_C2C_ORDER ord ");
		queryString.append(" WHERE 1=1 AND ord.STATE = '3' ");
		
		queryString.append(" GROUP BY ord.C2C_USER_ID ");

		List<Map<String, Object>> queryForList = this.namedParameterJdbcTemplate.queryForList(queryString.toString(), parameters);
		if (null == queryForList) {
			return new HashMap<String, Object>();
		}
		
		Map<String, Object> retMap = new HashMap<String, Object>();
		
		for (int i = 0; i < queryForList.size(); i++) {
			Map<String, Object> map = queryForList.get(i);
			
			if (null != map && null != map.get("c2c_user_id")) {
				
		        try  {
		        	
					SimpleDateFormat sdfT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
					Date firstDate = sdfT.parse(map.get("first_time").toString());
					
					if (null != map.get("first_time")) {
						retMap.put(map.get("c2c_user_id").toString(), DateUtil.dateNum(firstDate, new Date()));
					} else {
						retMap.put(map.get("c2c_user_id").toString(), 0);
					}
					
		        } catch (Throwable t) {
		            throw t;
		        }
			}
		}
		
		return retMap;
	}

	/*
	 * 获取 交易人数、总交易量、买交易量、卖交易量、总成单数、买成单数、卖成单数
	 */
	public Map<String, Map<String, Object>> getTotalDatas() {
		
		Map<String, Object> parameters = new HashMap<String, Object>();
		
		StringBuffer queryString = new StringBuffer();
		
		queryString.append(" SELECT ord.C2C_USER_ID AS c2c_user_id, COUNT(DISTINCT ord.PARTY_ID) AS user_count, ");
		queryString.append(" COUNT(*) AS total_order_count, COUNT(IF(ord.DIRECTION = 'buy', 1, null)) AS buy_order_count, COUNT(IF(ord.DIRECTION = 'sell', 1, null)) AS sell_order_count, IFNULL(SUM(ord.AMOUNT_USDT), 0) AS total_amount, ");
		queryString.append(" IFNULL(SUM(IF(ord.DIRECTION = 'buy', ord.AMOUNT_USDT, 0)), 0) AS buy_amount, IFNULL(SUM(IF(ord.DIRECTION = 'sell', ord.AMOUNT_USDT, 0)), 0) AS sell_amount ");
		queryString.append(" FROM T_C2C_ORDER ord ");
		queryString.append(" WHERE 1=1 AND ord.STATE = '3' ");
		
		queryString.append(" GROUP BY ord.C2C_USER_ID ");
				
		Map<String, Map<String, Object>> retMap = new HashMap<String, Map<String, Object>>();

		List<Map<String, Object>> queryForList = this.namedParameterJdbcTemplate.queryForList(queryString.toString(), parameters);
		if (null == queryForList) {
			retMap = new HashMap<String, Map<String, Object>>();
		}
		
		for (int i = 0; i < queryForList.size(); i++) {
			Map<String, Object> map = queryForList.get(i);
			
			if (null != map && null != map.get("c2c_user_id")) {				
				retMap.put(map.get("c2c_user_id").toString(), map);
			}
		}

		return retMap;
	}

	public void setNamedParameterJdbcTemplate(NamedParameterJdbcOperations namedParameterJdbcTemplate) {
		this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
	}

	public void setC2cUserService(C2cUserService c2cUserService) {
		this.c2cUserService = c2cUserService;
	}

}
