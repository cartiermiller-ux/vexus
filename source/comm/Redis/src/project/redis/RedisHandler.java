package project.redis;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public interface RedisHandler {
	/*
	 * Object get set dell
	 */
	/**
	 * get
	 * 
	 * @param key
	 * @param object
	 */
	public Object get(String key);

	/**
	 * 批量get，与单个get存在性能区别，一次连接（redispool），遍历取到数据后返回
	 */
	public Object[] getList(String[] keys);
	
	/**
	 * 批量get，与单个get存在性能区别，一次连接（redispool），遍历取到数据后返回
	 */
	public <V> HashMap<String,V> getMap(Set<String> keys);

	/**
	 * set 同步
	 * 
	 * @param key
	 * @param object
	 */
	public void setSync(String key, Object object);

	/**
	 * set 批量同步
	 * 
	 * @param params 需要写入的 k-v 数据
	 */
	public void setBatchSync(Map<String, Object> params);

	/**
	 * set 异步
	 * 
	 * @param key
	 * @param object
	 */
	public void setAsyn(String key, Object object);

	public void remove(String key);

	/*
	 * 队列（Queue） push poll
	 */

	/**
	 * push 同步
	 * 
	 */
	public void pushSync(String key, Object object);

	/**
	 * push 异步。批量处理在业务里考虑
	 * 
	 */
	public void pushAsyn(String key, Object object);

	/**
	 * 从队列尾取一个Object,如果为空，则返回null。
	 */
	public Object poll(String key);

}
