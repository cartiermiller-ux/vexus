package project.user.kyc.internal;

import java.text.MessageFormat;
import java.util.Date;

import org.springframework.beans.BeanUtils;
import org.springframework.jdbc.core.JdbcTemplate;

import kernel.exception.BusinessException;
import kernel.web.ApplicationUtil;
import project.redis.RedisHandler;
import project.user.UserRedisKeys;
import project.user.kyc.KycHighLevel;
import project.user.kyc.KycHighLevelService;

public class KycHighLevelServiceImpl implements KycHighLevelService {
	private JdbcTemplate jdbcTemplate;
	private RedisHandler redisHandler;

	@Override
	public KycHighLevel get(String partyId) {
		KycHighLevel kycHighLevel = (KycHighLevel) redisHandler.get(UserRedisKeys.KYC_HIGHLEVEL_PARTY_ID + partyId);
		if (kycHighLevel != null)
			return kycHighLevel;
		KycHighLevel obj = new KycHighLevel();
		obj.setPartyId(partyId);
		return obj;
	}

	@Override
	public void save(KycHighLevel entity) {
		KycHighLevel kycHighLevel = get(entity.getPartyId().toString());
		if(entity.getStatus()==1) {
			entity.setApply_time(new Date());
		}else {
			entity.setOperation_time(new Date());
		}
		if (kycHighLevel.getId() == null) {
			entity.setId(ApplicationUtil.getCurrentTimeUUID());
	        Object[] jdbcParams = ApplicationUtil.getInsertStatement(entity);
	        String insertUserSql = (String)jdbcParams[0];
	        Object[] sqlParameters = (Object[])jdbcParams[1];
	        jdbcTemplate.update(insertUserSql, sqlParameters);
			redisHandler.setSync(UserRedisKeys.KYC_HIGHLEVEL_PARTY_ID + entity.getPartyId().toString(), entity);
		} else {
			entity.setId(kycHighLevel.getId());
			BeanUtils.copyProperties(entity, kycHighLevel);
	        Object[] jdbcParams = ApplicationUtil.getUpdateStatement(kycHighLevel, "WHERE UUID=?", new Object[] {kycHighLevel.getId()});
	        String updateUserSql = (String)jdbcParams[0];
	        Object[] sqlParameters = (Object[])jdbcParams[1];
	        jdbcTemplate.update(updateUserSql, sqlParameters);
			redisHandler.setSync(UserRedisKeys.KYC_HIGHLEVEL_PARTY_ID + kycHighLevel.getPartyId().toString(), kycHighLevel);
		}
	}

	/**
	 * 验证审核结果
	 */
	public String checkApplyResult(String partyId) throws BusinessException {
		KycHighLevel kycHighLevel = get(partyId);
		if (null == kycHighLevel.getId())
			return "";
		String msg = "";
		switch (kycHighLevel.getStatus()) {
		case 0:
			msg = "已经提交申请，请等待审核";
			break;
		case 1:
			msg = "审核中";
			break;
		case 2:
			msg = "审核已通过";
			break;
		case 3:
			msg = MessageFormat.format("审核未通过,原因:{0}", kycHighLevel.getMsg());
			break;
		default:
			msg = "审核状态异常请联系客服";
			break;
		}
		if (kycHighLevel.getStatus() != 3) {
			throw new BusinessException(msg);
		} else {
			return msg;
		}
	}

	public void delete(String partyId) {
		KycHighLevel kycHighLevel = get(partyId);
		if (kycHighLevel != null) {
			 jdbcTemplate.update("DELETE FROM T_KYC_HIGH_LEVEL WHERE UUID=?", partyId);
			redisHandler.remove(UserRedisKeys.KYC_HIGHLEVEL_PARTY_ID + partyId);
		}
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public void setRedisHandler(RedisHandler redisHandler) {
		this.redisHandler = redisHandler;
	}

}
