package project.cms.data.loadcache;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import kernel.bo.RecordObjectMapper;
import project.cms.Cms;
import project.cms.CmsRedisKeys;
import project.redis.RedisHandler;

public class CmsLoadCacheService {
	
	private static final Logger logger = LoggerFactory.getLogger(CmsLoadCacheService.class);

	private JdbcTemplate jdbcTemplate;
	private RedisHandler redisHandler;

	public void loadcache() {
		load();
		logger.info("完成Cms数据加载redis");
	}

	public void load() {
		List<Cms> list = jdbcTemplate.query("SELECT * FROM T_CMS", RecordObjectMapper.newInstance(Cms.class));
		Map<String, Map<String, Cms>> cacheLanguage = new ConcurrentHashMap<String, Map<String, Cms>>();

		for (Cms cms : list) {
			if (cacheLanguage.containsKey(cms.getLanguage())) {
				Map<String, Cms> map = cacheLanguage.get(cms.getLanguage());
				map.put(cms.getContent_code(), cms);
				cacheLanguage.put(cms.getLanguage(), map);
			} else {
				Map<String, Cms> cache = new ConcurrentHashMap<String, Cms>();
				cache.put(cms.getContent_code(), cms);
				cacheLanguage.put(cms.getLanguage(), cache);
			}
			redisHandler.setSync(CmsRedisKeys.CMS_LANGUAGE_CONTENTCODE + cms.getLanguage() + cms.getContent_code(), cms);
		}
		for (Entry<String, Map<String, Cms>> entry : cacheLanguage.entrySet()) {
			redisHandler.setSync(CmsRedisKeys.CMS_LANGUAGE + entry.getKey(), entry.getValue());
		}
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public void setRedisHandler(RedisHandler redisHandler) {
		this.redisHandler = redisHandler;
	}

}
