package project.redis.interal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.ObjectUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;

import project.redis.PropertiesUtilRedis;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;

/**
 * redis操作封装
 */
@SuppressWarnings("unchecked")
public class Redis implements InitializingBean, DisposableBean {
	
	private Logger logger = LoggerFactory.getLogger(Redis.class);
	
	private ShardedJedisPool jedisPool;

	/**
	 * get
	 * @param key
	 * @param object
	 */
	public Object get(String key) {
		if (StringUtils.isBlank(key)) {
			return null;
		}
		ShardedJedis jedis = jedisPool.getResource();
		try {
			String value = jedis.get(key);
			if (value == null) {
				return null;
			}
			return JSON.parse(value);
		} finally {
			jedisPool.returnResource(jedis);
		}
	}

	/**
	 * 批量get，与单个get存在性能区别，一次连接（redispool），遍历取到数据后返回
	 */
	public Object[] getList(String[] keys) {
		ShardedJedis jedis = jedisPool.getResource();
		try {
			int length = keys.length;
			Object[] resultObjects = new Object[length];
			for (int i = 0; i < length; i++) {
				String value = jedis.get(keys[i]);
				resultObjects[i] = (value == null ? null : JSON.parse(value));
			}
			
			return resultObjects;
		} finally {
			jedisPool.returnResource(jedis);
		}
	}
	
	/**
	 * 批量get，与单个get存在性能区别，一次连接（redispool），遍历取到数据后返回
	 */
	public <V> HashMap<String,V> getMap(Set<String> keys) {
		if(null==keys || keys.isEmpty()) return null;
		
		ShardedJedis jedis = jedisPool.getResource();
		HashMap<String,V> resultMap=new HashMap<String,V>();
		try {
			for(String key:keys) {
				String value = jedis.get(key);
				resultMap.put(key,null==value?null:(V)JSON.parse(value));
			}
			return resultMap;
		} finally {
			jedisPool.returnResource(jedis);
		}
	}

	/**
	 * set 同步
	 * @param key
	 * @param object
	 */
	public void setSync(String key, Object object) {
		ShardedJedis jedis = jedisPool.getResource();
		try {
			jedis.set(key, JSON.toJSONString(object, SerializerFeature.WriteClassName));
		} finally {
			jedisPool.returnResource(jedis);
		}
	}

	/**
	 * set 批量同步
	 * 
	 * @param params 需要写入的 k-v 数据
	 */
	public void setBatchSync(Map<String, Object> params) {
		ShardedJedis jedis = jedisPool.getResource();
		try {

			Iterator<Map.Entry<String, Object>> iterator = params.entrySet().iterator();
			while (iterator.hasNext()) {
				Entry<String, Object> entry = iterator.next();
				jedis.set(entry.getKey(), JSON.toJSONString(entry.getValue(), SerializerFeature.WriteClassName));
			}

		} finally {
			jedisPool.returnResource(jedis);
		}
	}

	public void remove(String key) {
		ShardedJedis jedis = jedisPool.getResource();
		try {
			jedis.del(key);
		} finally {
			jedisPool.returnResource(jedis);
		}
	}

	/*
	 * 队列（Queue） push put
	 */

	/**
	 * push 同步
	 * 
	 */
	public void pushSync(String key, Object object) {
		ShardedJedis jedis = jedisPool.getResource();
		try {
			jedis.lpush(key, JSON.toJSONString(object, SerializerFeature.WriteClassName));
		} finally {
			jedisPool.returnResource(jedis);
		}
	}

	/**
	 * push 批量同步
	 * 
	 */
	public void pushBatchSync(List<Map<String, Object>> params) {
		ShardedJedis jedis = jedisPool.getResource();
		try {
			for(Map<String, Object> map:params) {
				for(Entry<String, Object> entry:map.entrySet()) {
					jedis.lpush(entry.getKey(), JSON.toJSONString(entry.getValue(), SerializerFeature.WriteClassName));
				}
			}
		} finally {
			jedisPool.returnResource(jedis);
		}
	}

	/**
	 * 从队列尾取一个Object,如果为空，则在timeout（秒）返回null。立即返回则timeout设置为0
	 * 
	 * @param timeout 秒
	 */
	public Object poll(String key) {
		ShardedJedis jedis = jedisPool.getResource();
		try {
			String value = jedis.rpop(key);
			if (value == null || value.equals("nil")) {
				return null;
			}
			return JSON.parse(value);
		} finally {
			jedisPool.returnResource(jedis);
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		GenericObjectPool.Config config = new GenericObjectPool.Config();
		config.testOnBorrow = Boolean.valueOf(PropertiesUtilRedis.getProperty("redis.pool.testOnBorrow"));
		config.testOnReturn = Boolean.valueOf(PropertiesUtilRedis.getProperty("redis.pool.testOnReturn"));
		config.testWhileIdle = Boolean.valueOf(PropertiesUtilRedis.getProperty("redis.pool.testWhileIdle"));
		config.maxIdle = Integer.valueOf(PropertiesUtilRedis.getProperty("redis.pool.maxIdle"));
		config.minIdle = Integer.valueOf(PropertiesUtilRedis.getProperty("redis.pool.minIdle"));
		config.maxActive = Integer.valueOf(PropertiesUtilRedis.getProperty("redis.pool.maxActive"));
		config.maxWait = Long.valueOf(PropertiesUtilRedis.getProperty("redis.pool.maxWait"));
		config.numTestsPerEvictionRun = Integer
				.valueOf(PropertiesUtilRedis.getProperty("redis.pool.numTestsPerEvictionRun"));
		config.timeBetweenEvictionRunsMillis = Integer
				.valueOf(PropertiesUtilRedis.getProperty("redis.pool.timeBetweenEvictionRunsMillis"));
		config.minEvictableIdleTimeMillis = Integer
				.valueOf(PropertiesUtilRedis.getProperty("redis.pool.minEvictableIdleTimeMillis"));

		int timeout = Integer.valueOf(PropertiesUtilRedis.getProperty("redis.pool.timeout"));
		List<JedisShardInfo> addressList = new ArrayList<JedisShardInfo>();
		String[] address_arr = PropertiesUtilRedis.getProperty("redis.address").split(";");

		if (ObjectUtils.isEmpty(address_arr)) {
			logger.error("redis.address 不能为空! ");
			return;
		}

		for (int i = 0; i < address_arr.length; i++) {
			String[] address = address_arr[i].split(":");
			if (address == null || address.length != 2) {
				logger.error("redis.address 配置不正确!");
				return;
			}
			String host = address[0];
			int port = Integer.valueOf(address[1]);

			logger.info("redis服务器" + (i + 1) + "的地址为: " + host + ":" + port);
			
//			JedisShardInfo jedisShardInfo = new JedisShardInfo(host, port, timeout);
//			jedisShardInfo.setPassword("efwh23jekdhwdefe2");
//			addressList.add(jedisShardInfo);
			
			
			addressList.add(new JedisShardInfo(host, port, timeout));
		}

		jedisPool = new ShardedJedisPool(config, addressList);
		ParserConfig.getGlobalInstance().setAutoTypeSupport(true);//开启fastjson白名单
		logger.info("redis对象池初始化完毕!");
	}

	@Override
	public void destroy() throws Exception {
		jedisPool.destroy();
	}

}
