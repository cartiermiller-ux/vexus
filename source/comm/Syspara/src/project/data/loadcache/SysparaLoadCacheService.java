package project.data.loadcache;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import kernel.web.ApplicationUtil;
import project.RedisKeys;
import project.redis.RedisHandler;
import project.syspara.Syspara;

public class SysparaLoadCacheService {

	private RedisHandler redisHandler;
	
	private static final Logger logger=LoggerFactory.getLogger(SysparaLoadCacheService.class);

	public void loadcache() {
		loadSyspara();
		logger.info("完成Syspara数据加载redis");
	}

	private void loadSyspara() {
		Map<String, Syspara> cache = new ConcurrentHashMap<String, Syspara>();
		List<Syspara> list=ApplicationUtil.executeSelect(Syspara.class);
		for (int i = 0; i < list.size(); i++) {
			Syspara syspara = list.get(i);
			redisHandler.setSync(RedisKeys.SYSPARA_CODE + syspara.getCode(), syspara);
			cache.put(list.get(i).getId().toString(), syspara);
		}
		redisHandler.setSync(RedisKeys.SYSPARA_MAP, cache);
	}

	public void setRedisHandler(RedisHandler redisHandler) {
		this.redisHandler = redisHandler;
	}
}
