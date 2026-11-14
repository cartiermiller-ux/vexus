package project.wallet.internal;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import kernel.web.ApplicationUtil;
import kernel.web.Page;
import kernel.web.PagedQueryDao;
import project.RedisKeys;
import project.redis.RedisHandler;
import project.wallet.WalletLog;
import project.wallet.WalletLogService;

public class WalletLogServiceImpl implements WalletLogService {
	
	private static final Logger logger = LoggerFactory.getLogger(WalletLogServiceImpl.class);
	
	private JdbcTemplate jdbcTemplate;
	private PagedQueryDao pagedQueryDao;
	private RedisHandler redisHandler;

	@Override
	public void save(WalletLog entity) {
		entity.setCreateTime(new Date());
		entity.setId(ApplicationUtil.getCurrentTimeUUID());
        Object[] jdbcParams = ApplicationUtil.getInsertStatement(entity);
        String insertUserSql = (String)jdbcParams[0];
        Object[] sqlParameters = (Object[])jdbcParams[1];
        jdbcTemplate.update(insertUserSql, sqlParameters);
		redisHandler.setSync(RedisKeys.WALLET_LOG_ORDERNO + entity.getOrder_no(), entity);
	}

	@Override
	public WalletLog find(String category, String order_no) {
		return find(order_no);
	}

	public WalletLog find(String order_no) {
		WalletLog walletLog = (WalletLog) redisHandler.get(RedisKeys.WALLET_LOG_ORDERNO + order_no);
		return walletLog;
	}

	@Override
	public void update(WalletLog entity) {
        Object[] jdbcParams = ApplicationUtil.getUpdateStatement(entity, "WHERE UUID=?", new Object[] {entity.getId()});
        String updateUserSql = (String)jdbcParams[0];
        Object[] sqlParameters = (Object[])jdbcParams[1];
        jdbcTemplate.update(updateUserSql, sqlParameters);
		redisHandler.setSync(RedisKeys.WALLET_LOG_ORDERNO + entity.getOrder_no(), entity);
	}

	public void updateStatus(String orderNo, int status) {
		// 日志状态更新
		WalletLog walletLog = this.find(orderNo);
		if (null == walletLog) {
			logger.error("walletLog is not exist,order_no:{}", orderNo);
		} else {
			walletLog.setStatus(status);
			this.update(walletLog);
		}
	}

	@Override
	public Page pagedQuery(int pageNo, int pageSize, String partyId, String category,String order_no_null) {
		Map<String, Object> parameters = new HashMap<String, Object>();
		StringBuffer queryString = new StringBuffer("SELECT ");
		queryString.append(
				"log.WALLETTYPE AS wallettype,log.AMOUNT AS amount,log.STATUS AS status,DATE_FORMAT(log.CREATE_TIME,'%Y-%m-%d %H:%i:%S') as createTimeStr, ");
		queryString.append("withdraw.FAILURE_MSG AS withdraw_failure_msg,"
				+ " recharge.DESCRIPTION AS recharge_failure_msg,log.ORDER_NO order_no ");
		queryString.append("FROM T_WALLET_LOG AS log ");
		queryString.append("LEFT JOIN T_WITHDRAW_ORDER AS withdraw ON withdraw.ORDER_NO = log.ORDER_NO ");
		queryString.append("LEFT JOIN T_RECHARGE_BLOCKCHAIN_ORDER AS recharge ON recharge.ORDER_NO = log.ORDER_NO ");

		queryString.append("WHERE 1=1  ");
		queryString.append(" and log.PARTY_ID = :partyId  ");
		parameters.put("partyId", partyId);

		if (StringUtils.isNotEmpty(category)) {
			queryString.append(" and log.CATEGORY = :category ");
			parameters.put("category", category);
		}
		if("1".equals(order_no_null)) {
			queryString.append(" and log.ORDER_NO is not null and log.ORDER_NO !='' ");
		}
		queryString.append(" order by log.CREATE_TIME desc ");

		Page page = this.pagedQueryDao.pagedQuerySQL(pageNo, pageSize, queryString.toString(), parameters);
		return page;
	}

	@Override
	public Page pagedQueryWithdraw(int pageNo, int pageSize, String partyId,String order_no_null) {
		
		Map<String, Object> parameters = new HashMap<String, Object>();
		
		StringBuffer queryString = new StringBuffer("SELECT ");
		
		queryString.append(
				" log.ORDER_NO AS order_no, log.WALLETTYPE AS coin, log.AMOUNT AS amount, log.STATUS AS status, DATE_FORMAT(log.CREATE_TIME,'%Y-%m-%d %H:%i:%S') as createTime, ");
		queryString.append(" withdraw.AMOUNT_FEE AS fee, withdraw.CHAIN_ADDRESS AS 'to', withdraw.FAILURE_MSG AS failure_msg, "
				+ " withdraw.VOLUME AS volume, withdraw.METHOD AS coin_blockchain ");
		
		queryString.append("FROM T_WALLET_LOG AS log ");
		queryString.append("LEFT JOIN T_WITHDRAW_ORDER AS withdraw ON withdraw.ORDER_NO = log.ORDER_NO ");

		queryString.append("WHERE 1=1  ");
		
		queryString.append(" and log.PARTY_ID = :partyId  ");
		parameters.put("partyId", partyId);
		
		queryString.append(" and log.CATEGORY = :category ");
		parameters.put("category", "withdraw");

		if("1".equals(order_no_null)) {
			queryString.append(" and log.ORDER_NO is not null and log.ORDER_NO !='' ");
		}
		
		queryString.append(" order by log.CREATE_TIME desc ");

		Page page = this.pagedQueryDao.pagedQuerySQL(pageNo, pageSize, queryString.toString(), parameters);
		return page;
	}

	@Override
	public Page pagedQueryRecharge(int pageNo, int pageSize, String partyId,String order_no_null) {
		
		Map<String, Object> parameters = new HashMap<String, Object>();
		
		StringBuffer queryString = new StringBuffer("SELECT ");
		
		queryString.append(
				"log.ORDER_NO AS order_no, log.WALLETTYPE AS coin, log.AMOUNT AS amount, log.STATUS AS status, DATE_FORMAT(log.CREATE_TIME,'%Y-%m-%d %H:%i:%S') as createTime, ");
		queryString.append("recharge.DESCRIPTION AS failure_msg, recharge.TX AS hash, recharge.ADDRESS AS address, recharge.CHANNEL_AMOUNT AS volume, ");
		queryString.append("recharge.COIN AS symbol, recharge.BLOCKCHAIN_NAME AS blockchain_name, recharge.TX AS tx ");
		
		queryString.append("FROM T_WALLET_LOG AS log ");
		queryString.append("LEFT JOIN T_RECHARGE_BLOCKCHAIN_ORDER AS recharge ON recharge.ORDER_NO = log.ORDER_NO ");

		queryString.append("WHERE 1=1  ");
		
		queryString.append(" and log.PARTY_ID = :partyId ");
		parameters.put("partyId", partyId);

		queryString.append(" and log.CATEGORY = :category ");
		parameters.put("category", "recharge");

		if("1".equals(order_no_null)) {
			queryString.append(" and log.ORDER_NO is not null and log.ORDER_NO !='' ");
		}
		
		queryString.append(" order by log.CREATE_TIME desc ");

		Page page = this.pagedQueryDao.pagedQuerySQL(pageNo, pageSize, queryString.toString(), parameters);
		return page;
	}

	@Override
	public Page pagedQueryRecords(int pageNo, int pageSize, String partyId, String category, String start_time, String end_time, String wallet_type, Integer status) {
		
		Map<String, Object> parameters = new HashMap<String, Object>();
		
		StringBuffer queryString = new StringBuffer("SELECT ");
		
		queryString.append(
				"log.CATEGORY AS category, log.WALLETTYPE AS wallet_type, log.AMOUNT AS amount, log.STATUS AS status, log.ORDER_NO order_no, DATE_FORMAT(log.CREATE_TIME,'%Y-%m-%d %H:%i:%S') as createTimeStr, ");
		queryString.append("withdraw.FAILURE_MSG AS withdraw_failure_msg, recharge.DESCRIPTION AS recharge_failure_msg ");
		
		queryString.append("FROM T_WALLET_LOG AS log ");
		queryString.append("LEFT JOIN T_WITHDRAW_ORDER AS withdraw ON withdraw.ORDER_NO = log.ORDER_NO ");
		queryString.append("LEFT JOIN T_RECHARGE_BLOCKCHAIN_ORDER AS recharge ON recharge.ORDER_NO = log.ORDER_NO ");

		queryString.append("WHERE 1=1  ");
		
		queryString.append(" and log.ORDER_NO is not null and log.ORDER_NO != ''  ");
		
		queryString.append(" and log.PARTY_ID = :partyId  ");
		parameters.put("partyId", partyId);

		if (StringUtils.isNotEmpty(category)) {
			queryString.append(" and log.CATEGORY = :category ");
			parameters.put("category", category);
		} else {
			queryString.append(" and (log.CATEGORY = 'recharge' or log.CATEGORY = 'withdraw') ");
		}
		
		if (StringUtils.isNotEmpty(start_time)) {
			queryString.append(" and DATE(log.CREATE_TIME) >= DATE(:start_time)  ");
			parameters.put("start_time", start_time);			
		}
		
		if (StringUtils.isNotEmpty(end_time)) {
			queryString.append(" and DATE(log.CREATE_TIME) <= DATE(:end_time)  ");
			parameters.put("end_time", end_time);			
		}

		if (StringUtils.isNotEmpty(wallet_type)) {
			queryString.append(" and log.WALLETTYPE = :wallet_type ");
			parameters.put("wallet_type", wallet_type);
		}

		if (null != status) {
			queryString.append(" and log.STATUS = :status ");
			parameters.put("status", status);
		}
		
		queryString.append(" order by log.CREATE_TIME desc ");

		Page page = this.pagedQueryDao.pagedQuerySQL(pageNo, pageSize, queryString.toString(), parameters);
		return page;
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public void setPagedQueryDao(PagedQueryDao pagedQueryDao) {
		this.pagedQueryDao = pagedQueryDao;
	}

	public void setRedisHandler(RedisHandler redisHandler) {
		this.redisHandler = redisHandler;
	}

}
