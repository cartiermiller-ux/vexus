package project.user.data.loadcache;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import kernel.bo.RecordObjectMapper;
import project.redis.RedisHandler;
import project.user.UserRedisKeys;
import project.user.kyc.Kyc;
import project.user.kyc.KycHighLevel;
import project.user.token.Token;

public class UserLoadCacheService {
	private static final Logger logger = LoggerFactory.getLogger(UserLoadCacheService.class);

	private JdbcTemplate jdbcTemplate;
	private RedisHandler redisHandler;

	public void loadcache() {
		loadToken();
		loadKyc();
		loadKycHighLevel();
		logger.info("完成User数据加载redis");
	}


	public void loadToken() {
		List<Token> list = jdbcTemplate.query("SELECT * FROM T_TOKEN", RecordObjectMapper.newInstance(Token.class));
		for (Token token : list) {
			redisHandler.setSync(UserRedisKeys.TOKEN + token.getToken(), token.getPartyId().toString());
			redisHandler.setSync(UserRedisKeys.TOKEN_PARTY_ID + token.getPartyId().toString(), token);
		}
	}
	
	public void loadKyc() {
		List<Kyc> list = jdbcTemplate.query("SELECT * FROM T_KYC", RecordObjectMapper.newInstance(Kyc.class));
		Map<String, Object> params = new ConcurrentHashMap<String, Object>();
		for (Kyc kyc : list) {
			params.put(UserRedisKeys.KYC_PARTY_ID + kyc.getPartyId().toString(), kyc);
		}
		this.redisHandler.setBatchSync(params);
	}

	public void loadKycHighLevel() {
		List<KycHighLevel> list = jdbcTemplate.query("SELECT * FROM T_KYC_HIGH_LEVEL", RecordObjectMapper.newInstance(KycHighLevel.class));
		for (KycHighLevel kycHighLevel : list) {
			redisHandler.setSync(UserRedisKeys.KYC_HIGHLEVEL_PARTY_ID + kycHighLevel.getPartyId().toString(),
					kycHighLevel);
		}
	}

	public void setRedisHandler(RedisHandler redisHandler) {
		this.redisHandler = redisHandler;
	}


	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

}
