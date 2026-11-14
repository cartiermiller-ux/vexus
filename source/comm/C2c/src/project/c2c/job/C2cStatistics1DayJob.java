package project.c2c.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import project.RedisKeys;
import project.c2c.C2cUserStatisticsService;
import project.redis.RedisHandler;

/*
 * C2C统计数据
 */
public class C2cStatistics1DayJob {
	
	private static Logger logger = LoggerFactory.getLogger(C2cStatistics1DayJob.class);
	
	private C2cUserStatisticsService c2cUserStatisticsService;
	private RedisHandler redisHandler;

	public void taskJob() {
		
		try {
			
			// 统计数据
			// this.c2cUserStatisticsService.statistics();
			
			// 清除订单每日取消次数redis
			this.redisHandler.remove(RedisKeys.C2C_ORDER_CANCEL_DAY_TIMES);
			 
		} catch (Throwable t) {
			logger.error("C2cStatistics1DayJob run fail t:", t);
		}
	}

	public void setC2cUserStatisticsService(C2cUserStatisticsService c2cUserStatisticsService) {
		this.c2cUserStatisticsService = c2cUserStatisticsService;
	}

	public void setRedisHandler(RedisHandler redisHandler) {
		this.redisHandler = redisHandler;
	}

}
