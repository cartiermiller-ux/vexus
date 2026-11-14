package project.monitor.internal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kernel.util.DateUtils;
import kernel.util.StringUtils;
import kernel.web.Page;
import kernel.web.PagedQueryDao;
import project.monitor.AdminAutoMonitorOrderService;
import project.party.recom.UserRecomService;

public class AdminAutoMonitorOrderServiceImpl implements AdminAutoMonitorOrderService {
	
	private PagedQueryDao pagedQueryDao;
	
	private UserRecomService userRecomService;
	
	@Override
	public Page pagedQuery(int pageNo, int pageSize,String username_para, String succeeded_para,String order_para,String startTime, String endTime,String loginPartyId,String settle_order_no_para,String settle_state_para) {
		StringBuffer queryString = new StringBuffer();
		queryString.append("SELECT");
		queryString.append(" party.USERNAME username ,party.ROLENAME rolename,party.USERCODE usercode, ");
		queryString.append(" recharge.UUID id,recharge.ORDER_NO order_no, "
				+ " recharge.CREATED created,recharge.TXN_HASH txn_hash,recharge.VOLUME volume,recharge.MONITOR_ADDRESS monitor_address, recharge.SUCCEEDED succeeded,recharge.ERROR error,");  
		queryString.append(" "
				+ "recharge.ADDRESS address,recharge.CHANNEL_ADDRESS channel_address, ");
		queryString.append(" "
				+ "recharge.SETTLE_TIME settle_time,recharge.SETTLE_ORDER_NO settle_order_no,recharge.SETTLE_AMOUNT settle_amount,recharge.SETTLE_STATE settle_state ");
		queryString.append(" FROM ");
		queryString.append(
				" T_AUTO_MONITOR_ORDER recharge "
				+ "LEFT JOIN PAT_PARTY party ON recharge.PARTY_ID = party.UUID "
				+ "  ");
		queryString.append(" WHERE 1=1 ");

		Map<String, Object> parameters = new HashMap<String, Object>();

		if (!StringUtils.isNullOrEmpty(order_para)) {
			queryString.append(" and recharge.ORDER_NO = :orderNo  ");
			parameters.put("orderNo", order_para);

		}
		if (!StringUtils.isNullOrEmpty(username_para)) {
			queryString.append(" and   recharge.ADDRESS =:username_para");
			parameters.put("username_para", username_para);
		}
		if (!StringUtils.isNullOrEmpty(succeeded_para)) {
			queryString.append(" and recharge.SUCCEEDED = :succeeded_para  ");
			parameters.put("succeeded_para", succeeded_para);

		}
		if (!StringUtils.isNullOrEmpty(settle_order_no_para)) {
			queryString.append(" and   recharge.SETTLE_ORDER_NO =:settle_order_no_para");
			parameters.put("settle_order_no_para", settle_order_no_para);
		}
		if (!StringUtils.isNullOrEmpty(settle_state_para)) {
			queryString.append(" and recharge.SETTLE_STATE = :settle_state_para  ");
			parameters.put("settle_state_para", settle_state_para);
			
		}
		if (!StringUtils.isNullOrEmpty(startTime)) {
			queryString.append(" AND DATE(recharge.CREATED) >= DATE(:startTime)  ");
			parameters.put("startTime",DateUtils.toDate(startTime));
		}
		if (!StringUtils.isNullOrEmpty(endTime)) {
			queryString.append(" AND DATE(recharge.CREATED) <= DATE(:endTime)  ");
			parameters.put("endTime", DateUtils.toDate(endTime));
		}
		if (!StringUtils.isNullOrEmpty(loginPartyId)) {

			List<String> checked_list = this.userRecomService.findChildren(loginPartyId);
			checked_list.add(loginPartyId);
			if (checked_list.size() == 0) {
				return new Page();
			}
			queryString.append(" and   party.UUID in(:checked_list)");
			parameters.put("checked_list", checked_list);
		}
		queryString.append(" order by recharge.CREATED desc ");
		Page page = this.pagedQueryDao.pagedQuerySQL(pageNo, pageSize, queryString.toString(), parameters);

		return page;
	}

	public void setPagedQueryDao(PagedQueryDao pagedQueryDao) {
		this.pagedQueryDao = pagedQueryDao;
	}
	
	public void setUserRecomService(UserRecomService userRecomService) {
		this.userRecomService = userRecomService;
	}
}
