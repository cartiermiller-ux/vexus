package project.redis.interal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.task.TaskExecutor;

import kernel.util.ThreadUtils;
import project.redis.RedisHandler;

public class RedisHandlerImpl implements RedisHandler, InitializingBean, Runnable {
	
	private Redis redis;
	
	private TaskExecutor taskExecutor;
	
	private Logger logger = LoggerFactory.getLogger(RedisHandlerImpl.class);

	/**
	 * get
	 * @param key
	 * @param object
	 */
	public Object get(String key) {
		return redis.get(key);
	}

	/**
	 * 批量get，与单个get存在性能区别，一次连接（redispool），遍历取到数据后返回
	 */
	public Object[] getList(String[] keys) {
		return redis.getList(keys);
	}
	
	@Override
	public <V> HashMap<String,V> getMap(Set<String> keys) {
		return redis.getMap(keys);
	}
	
	/**
	 * set 同步
	 * 
	 * @param key
	 * @param object
	 */
	public void setSync(String key, Object object) {
		redis.setSync(key, object);
	}

	/**
	 * set 批量同步
	 * 
	 * @param params 需要写入的 k-v 数据
	 */
	public void setBatchSync(Map<String, Object> params) {
		redis.setBatchSync(params);
	}

	/**
	 * set 异步
	 * 
	 * @param key
	 * @param object
	 */
	public void setAsyn(String key, Object object) {
		AsynItem item = new AsynItem(key, object, AsynItem.TYPE_MAP);
		AsynItemQueue.add(item);
	}

	public void remove(String key) {
		redis.remove(key);
	}

	/**
	 * push 同步
	 */
	public void pushSync(String key, Object object) {
		redis.pushSync(key, object);
	}

	/**
	 * push 异步。批量处理在业务里考虑
	 * 
	 */
	public void pushAsyn(String key, Object object) {
		AsynItem item = new AsynItem(key, object, AsynItem.TYPE_QUEUE);
		AsynItemQueue.add(item);
	}

	/**
	 * push 异步。批量处理在业务里考虑
	 * 
	 */
	public void pushBatchAsyn(List<Map<String, Object>> params) {
		redis.pushBatchSync(params);
	}

	/**
	 * 从队列尾取一个Object
	 * 
	 */
	public Object poll(String key) {
		return redis.poll(key);
	}

	/**
	 * 服务运行： 1. 从消息队列获取message 2.调用currentProvider发送短信
	 */
	public void run() {
		List<AsynItem> list = new ArrayList<AsynItem>();
		while (true) {

			try {
				
				AsynItem item = AsynItemQueue.poll();
				if (item != null) {
					list.add(item);
				}

				if ((item == null && list.size() > 0) || list.size() >= 100) {
					taskExecutor.execute(new HandleRunner(list));
					list = new ArrayList<AsynItem>();
				}
				if (item == null) {
					ThreadUtils.sleep(50);
				}
			} catch (Throwable e) {
				logger.error("RedisHandlerImpl taskExecutor.execute() fail", e);

			}
		}
	}

	public class HandleRunner implements Runnable {
		private List<AsynItem> list;

		public HandleRunner(List<AsynItem> list) {
			this.list = list;
		}

		public void run() {
			try {

				Map<String, Object> params_map = new ConcurrentHashMap<String, Object>();
				List<Map<String, Object>> params_queue = new ArrayList<Map<String, Object>>();
				for (int i = 0; i < list.size(); i++) {
					AsynItem item = list.get(i);
					if (AsynItem.TYPE_MAP.equals(item.getType())) {
						params_map.put(item.getKey(), item.getObject());
					} else if (AsynItem.TYPE_QUEUE.equals(item.getType())) {
						Map<String, Object> map = new HashMap<String, Object>();
						map.put(item.getKey(), item.getObject());
						params_queue.add(map);
					}
				}

				if (params_map.size() > 0) {
					redis.setBatchSync(params_map);
				}
				if (params_queue.size() > 0) {
					redis.pushBatchSync(params_queue);
				}

			} catch (Throwable t) {
				logger.error("RedisHandlerImpl taskExecutor.execute() fail", t);
			}

		}

	}

	public void afterPropertiesSet() throws Exception {
		new Thread(this, "RedisHandlerImplServer").start();
		if (logger.isInfoEnabled()) {
			logger.info("启动Redis(RedisHandlerImplServer)服务！");
		}

	}

	public void setRedis(Redis redis) {
		this.redis = redis;
	}

	public void setTaskExecutor(TaskExecutor taskExecutor) {
		this.taskExecutor = taskExecutor;
	}
}
