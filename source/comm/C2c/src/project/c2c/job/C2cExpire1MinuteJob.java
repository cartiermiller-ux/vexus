package project.c2c.job;

import java.util.Date;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import kernel.exception.BusinessException;
import kernel.util.ThreadUtils;
import project.RedisKeys;
import project.c2c.C2cOrder;
import project.c2c.C2cOrderLock;
import project.c2c.C2cOrderService;
import project.redis.RedisHandler;
import project.tip.TipService;

/**
 * C2C订单支付时效 1分钟执行一次
 */
public class C2cExpire1MinuteJob {
	
	private static Logger logger = LoggerFactory.getLogger(C2cExpire1MinuteJob.class);

	private RedisHandler redisHandler;
	private C2cOrderService c2cOrderService;
	private TipService tipService;

	public void taskJob() {
		
		try {
			
			Map<String, Long> map = (Map<String, Long>) this.redisHandler.get(RedisKeys.C2C_ORDER_NO_EXPIRE_TIME);
			if (null != map) {
				
				for (String orderNo : map.keySet()) {
					
					long now = (new Date()).getTime();
					
					// 订单自动取消
					if (orderNo.contains("_auto_cancel")) {
						long expireAutoCancel = map.get(orderNo);
						if (now > expireAutoCancel) {
							
							C2cOrder order = this.c2cOrderService.get(orderNo.replace("_auto_cancel", ""));
							if (null != order && "0".equals(order.getState())) {
																
								boolean lock = false;
								
								try {
									
									if (!C2cOrderLock.add(order.getOrderNo())) {
										throw new BusinessException(1, "订单自动取消：获取订单锁失败！");
									}
									
									lock = true;
									
									order.setRemark("订单号:"+order.getOrderNo()+"定时器自动取消订单：下单后未付款超时取消");
									
									this.c2cOrderService.saveOrderCancel(order, "timer");
												
								} catch (Throwable t) {
									logger.error("error:", t);
								} finally {
									if (lock) {
										ThreadUtils.sleep(100);
										C2cOrderLock.remove(order.getOrderNo());
									}
								}
							}
						}
					}
					
					// 确认收款超时
					if (orderNo.contains("_expire")) {
						long expire = map.get(orderNo);
						if (now > expire) {
							
							C2cOrder order = this.c2cOrderService.get(orderNo.replace("_expire", ""));
							if (null != order && "1".equals(order.getState())) {
								
								order.setState("5");
								this.c2cOrderService.update(order);
								this.tipService.deleteTip(order.getId().toString());
							}
						}
					}
				}
			}
			
		} catch (Throwable t) {
			logger.error("C2cExpire1MinuteJob run fail t:", t);
		}
	}
	
	public void setRedisHandler(RedisHandler redisHandler) {
		this.redisHandler = redisHandler;
	}

	public void setC2cOrderService(C2cOrderService c2cOrderService) {
		this.c2cOrderService = c2cOrderService;
	}

	public void setTipService(TipService tipService) {
		this.tipService = tipService;
	}

}
