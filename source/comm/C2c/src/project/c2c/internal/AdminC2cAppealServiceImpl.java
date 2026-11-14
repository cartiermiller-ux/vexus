package project.c2c.internal;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import kernel.exception.BusinessException;
import kernel.util.StringUtils;
import kernel.web.Page;
import kernel.web.PagedQueryDao;
import project.Constants;
import project.c2c.AdminC2cAppealService;
import project.c2c.C2cAppeal;
import project.c2c.C2cAppealService;
import project.log.LogService;
import project.tip.TipService;
import security.SecUser;
import security.internal.SecUserService;

public class AdminC2cAppealServiceImpl implements AdminC2cAppealService {

	private PagedQueryDao pagedDao;
	private C2cAppealService c2cAppealService;
	private SecUserService secUserService;
	private LogService logService;
	private TipService tipService;

	public Page pagedQuery(int pageNo, int pageSize, Integer status_int, String order_no_para, String user_code_para, String rolename_para, 
			String c2c_user_code_para, Integer c2c_user_type_int, String c2c_user_party_code_para, String loginPartyId) {

		StringBuffer queryString = new StringBuffer();
		
		queryString.append(" SELECT ");
		queryString.append(" c2c_appeal.UUID id, c2c_appeal.ORDER_NO order_no, c2c_appeal.REASON reason, c2c_appeal.DESCRIPTION description, c2c_appeal.IMG img, ");
		queryString.append(" c2c_appeal.NAME name, c2c_appeal.PHONE phone, c2c_appeal.STATE state, c2c_appeal.CREATE_TIME create_time, c2c_appeal.UPDATE_TIME update_time, ");
		queryString.append(" c2c_order.C2C_USER_CODE c2c_user_code, c2c_order.C2C_USER_NICK_NAME c2c_user_nick_name, ");
		queryString.append(" c2c_order.C2C_USER_PARTY_CODE c2c_user_party_code, c2c_order.C2C_USER_PARTY_NAME c2c_user_party_name, ");
		queryString.append(" party.USERCODE usercode, party.USERNAME username, party.ROLENAME rolename ");
		
		queryString.append(" FROM T_C2C_APPEAL c2c_appeal ");
		queryString.append(" LEFT JOIN T_C2C_ORDER c2c_order ON c2c_appeal.ORDER_NO = c2c_order.ORDER_NO ");
		queryString.append(" LEFT JOIN PAT_PARTY party ON party.UUID = c2c_order.PARTY_ID ");
		queryString.append(" LEFT JOIN T_C2C_USER c2c_user ON c2c_user.UUID = c2c_order.C2C_USER_ID ");
		queryString.append(" WHERE 1=1 ");

		Map<String, Object> parameters = new HashMap<String, Object>();

		if (status_int != null) {
			queryString.append(" AND c2c_appeal.STATE = :status_int  ");
			parameters.put("status_int", status_int);
		}
		
		if (StringUtils.isNotEmpty(order_no_para)) {
			queryString.append(" AND c2c_appeal.ORDER_NO = :order_no_para ");
			parameters.put("order_no_para", order_no_para);
		}

		if (StringUtils.isNotEmpty(user_code_para)) {
			queryString.append(" AND (party.USERNAME like :user_code_para OR party.USERCODE like :user_code_para) ");
			parameters.put("user_code_para", "%" + user_code_para + "%");
		}
		
		if (StringUtils.isNotEmpty(rolename_para)) {
			queryString.append(" AND party.ROLENAME = :rolename_para ");
			parameters.put("rolename_para", rolename_para);
		}

		if (StringUtils.isNotEmpty(c2c_user_code_para)) {
			queryString.append(" AND (c2c_order.C2C_USER_NICK_NAME like :c2c_user_code_para OR c2c_order.C2C_USER_CODE like :c2c_user_code_para) ");
			parameters.put("c2c_user_code_para", "%" + c2c_user_code_para + "%");
		}

		if (c2c_user_type_int != null) {
			queryString.append(" and c2c_order.C2C_USER_TYPE = :c2c_user_type_int  ");
			parameters.put("c2c_user_type_int", c2c_user_type_int);
		}

		if (StringUtils.isNotEmpty(c2c_user_party_code_para)) {
			queryString.append(" AND (c2c_order.C2C_USER_PARTY_NAME like :c2c_user_party_code_para OR c2c_order.C2C_USER_PARTY_CODE like :c2c_user_party_code_para) ");
			parameters.put("c2c_user_party_code_para", "%" + c2c_user_party_code_para + "%");
		}

		if (StringUtils.isNotEmpty(loginPartyId)) {
			queryString.append(" AND c2c_user.C2C_MANAGER_PARTY_ID = :loginPartyId ");
			parameters.put("loginPartyId", loginPartyId);
		}
		
		queryString.append(" order by FIELD(c2c_appeal.STATE, '1', '0') DESC, c2c_appeal.CREATE_TIME DESC ");

		Page page = this.pagedDao.pagedQuerySQL(pageNo, pageSize, queryString.toString(), parameters);
		
		return page;
	}
	
	public void handled(C2cAppeal c2cAppeal, String operatorUsername, String partyId) {
		
		if ("1".equals(c2cAppeal.getState())) {
			throw new BusinessException("申诉已处理了");
		}
		
		c2cAppeal.setState("1");
		c2cAppeal.setUpdateTime(new Date());
		this.c2cAppealService.update(c2cAppeal);
		
		this.tipService.deleteTip(c2cAppeal.getId().toString());
		
		SecUser secUser = this.secUserService.findUserByPartyId(partyId);
		
		this.saveLog(secUser.getUsername(), operatorUsername, "处理申诉", partyId);		
	}
	
	public C2cAppeal get(String order_no) {
		return this.c2cAppealService.get(order_no);	
	}
	
	 public void saveLog(String order_username, String operator, String context, String orderPartyId) {
		project.log.Log log = new project.log.Log();
		log.setCategory(Constants.LOG_CATEGORY_C2C);
		log.setUsername(order_username);
		log.setPartyId(orderPartyId);
		log.setOperator(operator);
		log.setLog(context);
		log.setCreateTime(new Date());
		logService.saveSync(log);
	}

	public void setPagedDao(PagedQueryDao pagedDao) {
		this.pagedDao = pagedDao;
	}

	public void setC2cAppealService(C2cAppealService c2cAppealService) {
		this.c2cAppealService = c2cAppealService;
	}

	public void setSecUserService(SecUserService secUserService) {
		this.secUserService = secUserService;
	}

	public void setLogService(LogService logService) {
		this.logService = logService;
	}

	public void setTipService(TipService tipService) {
		this.tipService = tipService;
	}

}
