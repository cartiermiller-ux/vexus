package project.news.data.loadcache;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import kernel.bo.RecordObjectMapper;
import project.RedisKeys;
import project.news.News;
import project.redis.RedisHandler;

public class NewsLoadCacheService {
	
	private static final Logger logger = LoggerFactory.getLogger(NewsLoadCacheService.class);

	private JdbcTemplate jdbcTemplate;
	private RedisHandler redisHandler;

	public void loadcache() {
		load();
		logger.info("完成News数据加载redis");
	}

	public void load() {
		
		List<News> list = jdbcTemplate.query("SELECT * FROM T_NEWS", RecordObjectMapper.newInstance(News.class));
		
		Map<String, Map<String, News>> cacheLanguage = new ConcurrentHashMap<String, Map<String, News>>();

		for (News news : list) {
			if (cacheLanguage.containsKey(news.getLanguage())) {
				Map<String, News> map = cacheLanguage.get(news.getLanguage());
				map.put(news.getId().toString(), news);
				cacheLanguage.put(news.getLanguage(), map);
			} else {
				Map<String, News> cache = new ConcurrentHashMap<String, News>();
				cache.put(news.getId().toString(), news);
				cacheLanguage.put(news.getLanguage(), cache);
			}
			redisHandler.setSync(RedisKeys.NEWS_ID + news.getId().toString(), news);
		}
		for (Entry<String, Map<String, News>> entry : cacheLanguage.entrySet()) {
			redisHandler.setSync(RedisKeys.NEWS_LANGUAGE + entry.getKey(), entry.getValue());
		}
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public void setRedisHandler(RedisHandler redisHandler) {
		this.redisHandler = redisHandler;
	}

}
