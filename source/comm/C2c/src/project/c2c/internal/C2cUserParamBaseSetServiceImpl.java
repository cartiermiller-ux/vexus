package project.c2c.internal;

import org.springframework.jdbc.core.JdbcTemplate;

import project.RedisKeys;
import project.c2c.C2cUserParamBaseSet;
import project.c2c.C2cUserParamBaseSetService;
import project.redis.RedisHandler;

public class C2cUserParamBaseSetServiceImpl implements C2cUserParamBaseSetService {
	
	private JdbcTemplate jdbcTemplate;
	private RedisHandler redisHandler;
	
	public void save(C2cUserParamBaseSet entity) {
		String insertSql = "INSERT INTO T_C2C_USER_PARAM_BASE_SET(UUID,C2C_USER_PARTY_ID,THIRTY_DAYS_ORDER,THIRTY_DAYS_ORDER_RATIO,"
				+ "THIRTY_DAYS_PASS_AVERAGE_TIME,THIRTY_DAYS_PAY_AVERAGE_TIME,THIRTY_DAYS_AMOUNT,BUY_AMOUNT,SELL_AMOUNT,TOTAL_AMOUNT,"
				+ "ACCOUNT_CREATE_DAYS,FIRST_EXCHANGE_DAYS,EXCHANGE_USERS,BUY_SUCCESS_ORDERS,SELL_SUCCESS_ORDERS,TOTAL_SUCCESS_ORDERS,"
				+ "APPRAISE_GOOD,APPRAISE_BAD,ORDER_MAIL_NOTICE_OPEN,ORDER_SMS_NOTICE_OPEN,ORDER_APP_NOTICE_OPEN,APPEAL_MAIL_NOTICE_OPEN,"
				+ "APPEAL_SMS_NOTICE_OPEN,APPEAL_APP_NOTICE_OPEN,CHAT_APP_NOTICE_OPEN,SECURITY_MAIL_NOTICE_OPEN,SECURITY_SMS_NOTICE_OPEN,"
				+ "SECURITY_APP_NOTICE_OPEN,PHONE_AUTHORITY,EMAIL_AUTHORITY,KYC_AUTHORITY,KYC_HIGHLEVEL_AUTHORITY,CREATE_TIME,UPDATE_TIME) "
				+ "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		jdbcTemplate.update(insertSql,entity.getId(),entity.getC2cUserPartyId(),entity.getThirtyDaysOrder(),entity.getThirtyDaysOrderRatio(),
				entity.getThirtyDaysPassAverageTime(),entity.getThirtyDaysPayAverageTime(),entity.getThirtyDaysAmount(),entity.getBuyAmount(),
				entity.getSellAmount(),entity.getTotalAmount(),entity.getAccountCreateDays(),entity.getFirstExchangeDays(),entity.getExchangeUsers(),
				entity.getBuySuccessOrders(),entity.getSellSuccessOrders(),entity.getTotalSuccessOrders(),entity.getAppraiseGood(),
				entity.getAppraiseBad(),entity.getOrderMailNoticeOpen(),entity.getOrderSmsNoticeOpen(),entity.getOrderAppNoticeOpen(),entity.getAppealMailNoticeOpen(),
				entity.getAppealSmsNoticeOpen(),entity.getAppealAppNoticeOpen(),entity.getChatAppNoticeOpen(),entity.getSecurityMailNoticeOpen(),
				entity.getSecuritySmsNoticeOpen(),entity.getSecurityAppNoticeOpen(),entity.getPhoneAuthority(),entity.getEmailAuthority(),entity.getKycAuthority(),
				entity.getKycHighlevelAuthority(),entity.getCreateTime(),entity.getUpdateTime());
		this.redisHandler.setSync(RedisKeys.C2C_USER_PARAM_BASE_SET_PARTY_ID + entity.getC2cUserPartyId(), entity);
	}

	public void update(C2cUserParamBaseSet entity) {
		String updateSql = "UPDATE T_C2C_USER_PARAM_BASE_SET SET C2C_USER_PARTY_ID=?,THIRTY_DAYS_ORDER=?,THIRTY_DAYS_ORDER_RATIO=?,"
				+ "THIRTY_DAYS_PASS_AVERAGE_TIME=?,THIRTY_DAYS_PAY_AVERAGE_TIME=?,THIRTY_DAYS_AMOUNT=?,BUY_AMOUNT=?,SELL_AMOUNT=?,TOTAL_AMOUNT=?,"
				+ "ACCOUNT_CREATE_DAYS=?,FIRST_EXCHANGE_DAYS=?,EXCHANGE_USERS=?,BUY_SUCCESS_ORDERS=?,SELL_SUCCESS_ORDERS=?,TOTAL_SUCCESS_ORDERS=?,"
				+ "APPRAISE_GOOD=?,APPRAISE_BAD=?,ORDER_MAIL_NOTICE_OPEN=?,ORDER_SMS_NOTICE_OPEN=?,ORDER_APP_NOTICE_OPEN=?,APPEAL_MAIL_NOTICE_OPEN=?,"
				+ "APPEAL_SMS_NOTICE_OPEN=?,APPEAL_APP_NOTICE_OPEN=?,CHAT_APP_NOTICE_OPEN=?,SECURITY_MAIL_NOTICE_OPEN=?,SECURITY_SMS_NOTICE_OPEN=?,"
				+ "SECURITY_APP_NOTICE_OPEN=?,PHONE_AUTHORITY=?,EMAIL_AUTHORITY=?,KYC_AUTHORITY=?,KYC_HIGHLEVEL_AUTHORITY=?,UPDATE_TIME=?"
				+ " WHERE UUID=?";
		jdbcTemplate.update(updateSql,entity.getC2cUserPartyId(),entity.getThirtyDaysOrder(),entity.getThirtyDaysOrderRatio(),
				entity.getThirtyDaysPassAverageTime(),entity.getThirtyDaysPayAverageTime(),entity.getThirtyDaysAmount(),entity.getBuyAmount(),
				entity.getSellAmount(),entity.getTotalAmount(),entity.getAccountCreateDays(),entity.getFirstExchangeDays(),entity.getExchangeUsers(),
				entity.getBuySuccessOrders(),entity.getSellSuccessOrders(),entity.getTotalSuccessOrders(),entity.getAppraiseGood(),
				entity.getAppraiseBad(),entity.getOrderMailNoticeOpen(),entity.getOrderSmsNoticeOpen(),entity.getOrderAppNoticeOpen(),entity.getAppealMailNoticeOpen(),
				entity.getAppealSmsNoticeOpen(),entity.getAppealAppNoticeOpen(),entity.getChatAppNoticeOpen(),entity.getSecurityMailNoticeOpen(),
				entity.getSecuritySmsNoticeOpen(),entity.getSecurityAppNoticeOpen(),entity.getPhoneAuthority(),entity.getEmailAuthority(),entity.getKycAuthority(),
				entity.getKycHighlevelAuthority(),entity.getUpdateTime(),entity.getId());
		
		this.redisHandler.setSync(RedisKeys.C2C_USER_PARAM_BASE_SET_PARTY_ID + entity.getC2cUserPartyId(), entity);
	}

	public void delete(String c2c_user_party_id) {
		C2cUserParamBaseSet entity = this.getByPartyId(c2c_user_party_id);
		if (entity != null) {
			jdbcTemplate.update("DELETE FROM T_C2C_USER_PARAM_BASE_SET WHERE UUID=?", entity.getId());
			this.redisHandler.remove(RedisKeys.C2C_USER_PARAM_BASE_SET_PARTY_ID + entity.getC2cUserPartyId());
		}
	}
	
	public C2cUserParamBaseSet getByPartyId(String c2c_user_party_id) {
		return (C2cUserParamBaseSet) this.redisHandler.get(RedisKeys.C2C_USER_PARAM_BASE_SET_PARTY_ID + c2c_user_party_id);
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public void setRedisHandler(RedisHandler redisHandler) {
		this.redisHandler = redisHandler;
	}
	
}
