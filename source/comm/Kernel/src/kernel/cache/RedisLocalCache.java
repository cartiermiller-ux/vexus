package kernel.cache;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import project.redis.RedisHandler;

@SuppressWarnings("unchecked")
public class RedisLocalCache {

	private ConcurrentHashMap<String, Object> cache = new ConcurrentHashMap<String, Object>();
	
	private RedisHandler redisHandler;

	public Object get(String key) {
		Object obj = cache.get(key);
		if (obj != null) {
			return obj;
		}
		obj = redisHandler.get(key);
		if (obj != null) {
			cache.put(key, obj);
			return obj;
		}
		return null;
	}
	
	public <V> HashMap<String,V> getMap(Set<String> keys) {
		HashMap<String,V> resultMap=new HashMap<String,V>();
		
		HashSet<String> noValueKeys=new HashSet<String>();
		for(String key:keys) {
			Object obj = cache.get(key);
			if(null==obj) {
				noValueKeys.add(key);
			}else {
				resultMap.put(key,(V)obj);
			}
		}
		
		if(noValueKeys.isEmpty()) return resultMap;
		
		HashMap<String,V> redisResultMap=redisHandler.getMap(noValueKeys);
		if(null==redisResultMap || redisResultMap.isEmpty()) return resultMap;
		
		resultMap.putAll(redisResultMap);
		
		return resultMap;
	}

	public void put(String key, Object obj) {
		cache.put(key, obj);
	}
	
	public void putAll(Map<String,Object> keyValMaps) {
		cache.putAll(keyValMaps);
	}

	public void setRedisHandler(RedisHandler redisHandler) {
		this.redisHandler = redisHandler;
	}

}
