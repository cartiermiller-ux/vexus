package project.c2c.internal;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kernel.util.StringUtils;
import kernel.web.Page;
import kernel.web.PagedQueryDao;
import project.c2c.AdminC2cUserService;
import project.c2c.C2cUser;
import project.c2c.C2cUserService;
import project.syspara.Syspara;
import project.syspara.SysparaService;

public class AdminC2cUserServiceImpl implements AdminC2cUserService {

	private PagedQueryDao pagedDao;
	private SysparaService sysparaService;
	private C2cUserService c2cUserService;

	public Page pagedQuery(int pageNo, int pageSize, String c2c_user_id, Integer c2c_user_type_int, String c2c_user_party_id, String c2c_manager_name_para, String loginPartyId) {

		StringBuffer queryString = new StringBuffer();
		
		queryString.append(" SELECT ");
		queryString.append(" cu.UUID id, cu.NICK_NAME nick_name, cu.HEAD_IMG head_img, cu.DEPOSIT deposit, cu.DEPOSIT_OPEN deposit_open, cu.DEPOSIT_GIFT_RATE deposit_gift_rate, ");
		queryString.append(" cu.C2C_USER_TYPE c2c_user_type, cu.C2C_USER_CODE c2c_user_code, cu.C2C_USER_PARTY_ID c2c_user_party_id, ");
		queryString.append(" cu.THIRTY_DAYS_ORDER thirty_days_order, cu.THIRTY_DAYS_ORDER_RATIO thirty_days_order_ratio, cu.THIRTY_DAYS_PASS_AVERAGE_TIME thirty_days_pass_average_time, cu.THIRTY_DAYS_PAY_AVERAGE_TIME thirty_days_pay_average_time, ");
		queryString.append(" cu.THIRTY_DAYS_AMOUNT thirty_days_amount, cu.BUY_AMOUNT buy_amount, cu.SELL_AMOUNT sell_amount, cu.TOTAL_AMOUNT total_amount, cu.ACCOUNT_CREATE_DAYS account_create_days, cu.FIRST_EXCHANGE_DAYS first_exchange_days, cu.EXCHANGE_USERS exchange_users, ");
		queryString.append(" cu.BUY_SUCCESS_ORDERS buy_success_orders, cu.SELL_SUCCESS_ORDERS sell_success_orders, cu.TOTAL_SUCCESS_ORDERS total_success_orders, ");
		queryString.append(" cu.APPRAISE_GOOD appraise_good, cu.APPRAISE_BAD appraise_bad, ");
		queryString.append(" cu.ORDER_MAIL_NOTICE_OPEN order_mail_notice_open, cu.ORDER_SMS_NOTICE_OPEN order_sms_notice_open, cu.ORDER_APP_NOTICE_OPEN order_app_notice_open, ");
		queryString.append(" cu.APPEAL_MAIL_NOTICE_OPEN appeal_mail_notice_open, cu.APPEAL_SMS_NOTICE_OPEN appeal_sms_notice_open, cu.APPEAL_APP_NOTICE_OPEN appeal_app_notice_open, cu.CHAT_APP_NOTICE_OPEN chat_app_notice_open, ");
		queryString.append(" cu.SECURITY_MAIL_NOTICE_OPEN security_mail_notice_open, cu.SECURITY_SMS_NOTICE_OPEN security_sms_notice_open, cu.SECURITY_APP_NOTICE_OPEN security_app_notice_open, ");
		queryString.append(" cu.REMARK remark, cu.CREATE_TIME create_time, cu.UPDATE_TIME update_time, ");
		queryString.append(" manager.UUID manager_id, manager.USERNAME manager_name, party.USERNAME user_name, party.USERCODE user_code, party.PHONE_AUTHORITY phone_authority, party.EMAIL_AUTHORITY email_authority, party.KYC_AUTHORITY kyc_authority, party.KYC_HIGHLEVEL_AUTHORITY kyc_highlevel_authority ");

		queryString.append(" FROM T_C2C_USER cu ");
		queryString.append(" LEFT JOIN PAT_PARTY party ON cu.C2C_USER_PARTY_ID = party.UUID ");
		queryString.append(" LEFT JOIN SCT_USER manager ON cu.C2C_MANAGER_PARTY_ID = manager.UUID ");
		queryString.append(" WHERE 1=1 ");

		Map<String, Object> parameters = new HashMap<String, Object>();

		if (!StringUtils.isNullOrEmpty(loginPartyId)) {
			queryString.append(" AND cu.C2C_MANAGER_PARTY_ID = :loginPartyId ");
			parameters.put("loginPartyId", loginPartyId);
		}

		if (!StringUtils.isNullOrEmpty(c2c_manager_name_para)) {
			queryString.append(" AND manager.USERNAME like :c2c_manager_name_para ");
			parameters.put("c2c_manager_name_para", "%" + c2c_manager_name_para + "%");
		}

		if (StringUtils.isNotEmpty(c2c_user_id)) {
			queryString.append(" AND (cu.NICK_NAME like :c2c_user_id OR cu.C2C_USER_CODE like :c2c_user_id ) ");
			parameters.put("c2c_user_id", "%" + c2c_user_id + "%");
		}
		
		if (StringUtils.isNotEmpty(c2c_user_party_id)) {
			queryString.append(" AND (party.USERNAME like :c2c_user_party_id OR party.USERCODE like :c2c_user_party_id ) ");
			parameters.put("c2c_user_party_id", "%" + c2c_user_party_id + "%");
		}

		if (c2c_user_type_int != null) {
			queryString.append(" AND cu.C2C_USER_TYPE = :c2c_user_type_int ");
			parameters.put("c2c_user_type_int", c2c_user_type_int);
		}

		queryString.append(" order by cu.CREATE_TIME desc ");

		Page page = this.pagedDao.pagedQuerySQL(pageNo, pageSize, queryString.toString(), parameters);
		
		return page;
	}
	
	public void save(C2cUser c2cUser) {
		c2cUser.setC2cUserCode(this.getUsercode());	
		this.c2cUserService.save(c2cUser);
	}
	
	public void update(C2cUser c2cUser) {
		this.c2cUserService.update(c2cUser);
	}

	public void delete(String id) {
		this.c2cUserService.delete(id);
	}

	public C2cUser findById(Serializable id) {
		return this.c2cUserService.get(id.toString());
	}
	
	private String getUsercode() {
		Syspara syspara = this.sysparaService.find("c2c_user_uid_sequence");
		int random = (int) (Math.random() * 3 + 1);
		int user_uid_sequence = syspara.getInteger() + random;
		syspara.setValue(user_uid_sequence);
		this.sysparaService.update(syspara);
		String usercode = String.valueOf(user_uid_sequence);
		return usercode;
	}
	
	/*
	 * 获取C2C管理员下级承兑商
	 */
	public List<Map<String, Object>> getC2cManagerC2cUser(String manager_id) {
		
		StringBuffer queryString = new StringBuffer();
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("manager_id", manager_id);
		
		queryString.append(" SELECT c2c_user.UUID id, c2c_user.NICK_NAME nick_name, c2c_user.C2C_USER_CODE c2c_user_code, "
				+ "c2c_user.C2C_USER_TYPE c2c_user_type, c2c_user.DEPOSIT_OPEN deposit_open, c2c_user.DEPOSIT deposit "
				+ "FROM T_C2C_USER c2c_user WHERE c2c_user.C2C_MANAGER_PARTY_ID = :manager_id ");
		Page page = this.pagedDao.pagedQuerySQL(1, 1000000, queryString.toString(), parameters);

		if (null == page) {
			return new ArrayList<Map<String,Object>>();
		} else {
			return (List<Map<String, Object>>) page.getElements();
		}
	}
	
	/*
	 * 获取所有C2C管理员
	 */
	public Map<String, Object> getAllC2cManager() {
		
		StringBuffer queryString = new StringBuffer();		
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("role_uuid", "SECURITY_ROLE_C2C");
		
		queryString.append(" SELECT user.UUID id, user.USERNAME user_name FROM SCT_USER user LEFT JOIN SCT_USER_ROLE user_role ON user_role.USER_UUID = user.UUID WHERE user_role.ROLE_UUID = :role_uuid ");
		Page page = this.pagedDao.pagedQuerySQL(1, 1000000, queryString.toString(), parameters);

		Map<String, Object> mapRet = new HashMap<String, Object>();
		for (Map<String, Object> data : (List<Map<String, Object>>) page.getElements()) {
			mapRet.put(data.get("id").toString(), data.get("user_name"));
		}	
		return mapRet;
	}
	
	public void setPagedDao(PagedQueryDao pagedDao) {
		this.pagedDao = pagedDao;
	}

	public void setSysparaService(SysparaService sysparaService) {
		this.sysparaService = sysparaService;
	}

	public void setC2cUserService(C2cUserService c2cUserService) {
		this.c2cUserService = c2cUserService;
	}

}
