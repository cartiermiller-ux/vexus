package project.c2c.internal;

import java.util.ArrayList;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;

import kernel.bo.RecordObjectMapper;
import project.RedisKeys;
import project.c2c.C2cUser;
import project.c2c.C2cUserService;
import project.redis.RedisHandler;

public class C2cUserServiceImpl implements C2cUserService {
	
	private JdbcTemplate jdbcTemplate;
	private RedisHandler redisHandler;
	
	public void save(C2cUser entity) {
		String insertSql = "INSERT INTO T_C2C_USER(UUID,C2C_MANAGER_PARTY_ID,C2C_USER_TYPE,C2C_USER_CODE,C2C_USER_PARTY_ID,"
				+ "NICK_NAME,HEAD_IMG,DEPOSIT,DEPOSIT_OPEN,DEPOSIT_GIFT_RATE,THIRTY_DAYS_ORDER,THIRTY_DAYS_ORDER_RATIO,THIRTY_DAYS_PASS_AVERAGE_TIME,"
				+ "THIRTY_DAYS_PAY_AVERAGE_TIME,THIRTY_DAYS_AMOUNT,BUY_AMOUNT,SELL_AMOUNT,TOTAL_AMOUNT,ACCOUNT_CREATE_DAYS,FIRST_EXCHANGE_DAYS,"
				+ "EXCHANGE_USERS,BUY_SUCCESS_ORDERS,SELL_SUCCESS_ORDERS,TOTAL_SUCCESS_ORDERS,APPRAISE_GOOD,APPRAISE_BAD,ORDER_MAIL_NOTICE_OPEN,"
				+ "ORDER_SMS_NOTICE_OPEN,ORDER_APP_NOTICE_OPEN,APPEAL_MAIL_NOTICE_OPEN,APPEAL_SMS_NOTICE_OPEN,APPEAL_APP_NOTICE_OPEN,CHAT_APP_NOTICE_OPEN,"
				+ "SECURITY_MAIL_NOTICE_OPEN,SECURITY_SMS_NOTICE_OPEN,SECURITY_APP_NOTICE_OPEN,REMARK,CREATE_TIME,UPDATE_TIME) "
				+ "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		jdbcTemplate.update(insertSql,entity.getId(),entity.getC2cManagerPartyId(),entity.getC2cUserType(),entity.getC2cUserCode(),entity.getC2cUserPartyId(),
						entity.getNickName(),entity.getHeadImg(),entity.getDeposit(),entity.getDepositOpen(),entity.getDepositGiftRate(),entity.getThirtyDaysOrder(),
						entity.getThirtyDaysOrderRatio(),entity.getThirtyDaysPassAverageTime(),entity.getThirtyDaysPayAverageTime(),entity.getThirtyDaysAmount(),
						entity.getBuyAmount(),entity.getSellAmount(),entity.getTotalAmount(),entity.getAccountCreateDays(),entity.getFirstExchangeDays(),
						entity.getExchangeUsers(),entity.getBuySuccessOrders(),entity.getSellSuccessOrders(),entity.getTotalSuccessOrders(),entity.getAppraiseGood(),
						entity.getAppraiseBad(),entity.getOrderMailNoticeOpen(),entity.getOrderSmsNoticeOpen(),entity.getOrderAppNoticeOpen(),entity.getAppealMailNoticeOpen(),
						entity.getAppealSmsNoticeOpen(),entity.getAppealAppNoticeOpen(),entity.getChatAppNoticeOpen(),
						entity.getSecurityMailNoticeOpen(),entity.getSecuritySmsNoticeOpen(),entity.getSecurityAppNoticeOpen(),entity.getRemark(),entity.getCreateTime(),entity.getUpdateTime());
				
		this.redisHandler.setSync(RedisKeys.C2C_USER_ID + entity.getId().toString(), entity);
		this.redisHandler.setSync(RedisKeys.C2C_USER_PARTY_ID + entity.getC2cUserPartyId(), entity);
	}

	public void update(C2cUser entity) {
		if (null == entity) {
			return;
		}
		String updateSql = "UPDATE T_C2C_USER SET C2C_MANAGER_PARTY_ID=?,C2C_USER_TYPE=?,C2C_USER_CODE=?,C2C_USER_PARTY_ID=?,NICK_NAME=?,HEAD_IMG=?,"
				+ "DEPOSIT=?,DEPOSIT_OPEN=?,DEPOSIT_GIFT_RATE=?,THIRTY_DAYS_ORDER=?,THIRTY_DAYS_ORDER_RATIO=?,THIRTY_DAYS_PASS_AVERAGE_TIME=?,"
				+ "THIRTY_DAYS_PAY_AVERAGE_TIME=?,THIRTY_DAYS_AMOUNT=?,BUY_AMOUNT=?,SELL_AMOUNT=?,TOTAL_AMOUNT=?,ACCOUNT_CREATE_DAYS=?,"
				+ "FIRST_EXCHANGE_DAYS=?,EXCHANGE_USERS=?,BUY_SUCCESS_ORDERS=?,SELL_SUCCESS_ORDERS=?,TOTAL_SUCCESS_ORDERS=?,APPRAISE_GOOD=?,"
				+ "APPRAISE_BAD=?,ORDER_MAIL_NOTICE_OPEN=?,ORDER_SMS_NOTICE_OPEN=?,ORDER_APP_NOTICE_OPEN=?,APPEAL_MAIL_NOTICE_OPEN=?,"
				+ "APPEAL_SMS_NOTICE_OPEN=?,APPEAL_APP_NOTICE_OPEN=?,CHAT_APP_NOTICE_OPEN=?,SECURITY_MAIL_NOTICE_OPEN=?,"
				+ "SECURITY_SMS_NOTICE_OPEN=?,SECURITY_APP_NOTICE_OPEN=?,REMARK=?,UPDATE_TIME=? WHERE UUID=?";

		jdbcTemplate.update(updateSql,entity.getC2cManagerPartyId(),entity.getC2cUserType(),entity.getC2cUserCode(),entity.getC2cUserPartyId(),
				entity.getNickName(),entity.getHeadImg(),entity.getDeposit(),entity.getDepositOpen(),entity.getDepositGiftRate(),entity.getThirtyDaysOrder(),
				entity.getThirtyDaysOrderRatio(),entity.getThirtyDaysPassAverageTime(),entity.getThirtyDaysPayAverageTime(),entity.getThirtyDaysAmount(),
				entity.getBuyAmount(),entity.getSellAmount(),entity.getTotalAmount(),entity.getAccountCreateDays(),entity.getFirstExchangeDays(),
				entity.getExchangeUsers(),entity.getBuySuccessOrders(),entity.getSellSuccessOrders(),entity.getTotalSuccessOrders(),entity.getAppraiseGood(),
				entity.getAppraiseBad(),entity.getOrderMailNoticeOpen(),entity.getOrderSmsNoticeOpen(),entity.getOrderAppNoticeOpen(),entity.getAppealMailNoticeOpen(),
				entity.getAppealSmsNoticeOpen(),entity.getAppealAppNoticeOpen(),entity.getChatAppNoticeOpen(),
				entity.getSecurityMailNoticeOpen(),entity.getSecuritySmsNoticeOpen(),entity.getSecurityAppNoticeOpen(),entity.getRemark(),entity.getUpdateTime(),entity.getId());
		
		this.redisHandler.setSync(RedisKeys.C2C_USER_ID + entity.getId().toString(), entity);
		this.redisHandler.setSync(RedisKeys.C2C_USER_PARTY_ID + entity.getC2cUserPartyId(), entity);
	}

	public void delete(String id) {
		C2cUser entity = this.get(id);
		if (entity != null) {
			jdbcTemplate.update("DELETE FROM T_C2C_USER WHERE UUID=?", entity.getId());
			this.redisHandler.remove(RedisKeys.C2C_USER_ID + entity.getId().toString());
			this.redisHandler.remove(RedisKeys.C2C_USER_PARTY_ID + entity.getC2cUserPartyId());
		}
	}

	public C2cUser get(String id) {
		return (C2cUser) this.redisHandler.get(RedisKeys.C2C_USER_ID + id);
	}
	
	public C2cUser getByPartyId(String partyId) {
		return (C2cUser) this.redisHandler.get(RedisKeys.C2C_USER_PARTY_ID + partyId);
	}
	
	public C2cUser findByUsercode(String c2cUserCode) {
		List<C2cUser> list = jdbcTemplate.query("SELECT * FROM T_C2C_USER WHERE C2C_USER_CODE=?", RecordObjectMapper.newInstance(C2cUser.class), c2cUserCode);
		if (list.size() > 0)
			return list.get(0);
		return null;
	}
	
	public List<C2cUser> findAll() {
		List<C2cUser> list = jdbcTemplate.query("SELECT * FROM T_C2C_USER", RecordObjectMapper.newInstance(C2cUser.class));
		if (null == list) {
			return new ArrayList<C2cUser>();
		} else {
			return list;
		}
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public void setRedisHandler(RedisHandler redisHandler) {
		this.redisHandler = redisHandler;
	}
	
}
