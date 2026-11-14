package project.finance.data.loadcache;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import kernel.bo.RecordObjectMapper;
import project.finance.Finance;
import project.finance.FinanceRedisKeys;
import project.redis.RedisHandler;

public class FinanceLoadCacheService {
	
	private static final Logger logger = LoggerFactory.getLogger(FinanceLoadCacheService.class);

	private JdbcTemplate jdbcTemplate;
	private RedisHandler redisHandler;

	public void loadcache() {
		load();
		logger.info("完成Finance数据加载redis");
	}

	public void load() {
		List<Finance> list = jdbcTemplate.query("SELECT * FROM T_FINANCE", RecordObjectMapper.newInstance(Finance.class));
		Map<String, Finance> cacheMap = new ConcurrentHashMap<String, Finance>();
		for (Finance finance : list) {
			cacheMap.put(finance.getId().toString(), finance);
			redisHandler.setSync(FinanceRedisKeys.FINANCE_ID + finance.getId().toString(), finance);
		}
		redisHandler.setSync(FinanceRedisKeys.FINANCE_MAP, cacheMap);
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public void setRedisHandler(RedisHandler redisHandler) {
		this.redisHandler = redisHandler;
	}

}
