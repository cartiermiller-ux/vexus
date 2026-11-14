package project.ddos.internal;

import java.util.Date;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;

import kernel.bo.RecordObjectMapper;
import kernel.util.DateUtils;
import kernel.web.ApplicationUtil;
import project.ddos.DdosRedisKeys;
import project.ddos.IpMenuService;
import project.ddos.model.IpMenu;
import project.ddos.utils.BlacklistIpSerivceTimeWindow;
import project.ddos.utils.LocklistIpSerivceTimeWindow;
import project.redis.RedisHandler;

/**
 * @author JORGE
 * @description IP黑白名单服务
 */
public class IpMenuServiceImpl implements IpMenuService {
	
	private RedisHandler redisHandler;
	
	private JdbcTemplate jdbcTemplate;
	
	private LocklistIpSerivceTimeWindow locklistIpSerivceTimeWindow;
	
	private BlacklistIpSerivceTimeWindow blacklistIpSerivceTimeWindow;
	
	public void init() {
		List<IpMenu> list = jdbcTemplate.query("SELECT * FROM T_IP_MENU WHERE DELETE_STATUS=0",RecordObjectMapper.newInstance(IpMenu.class));
		for (IpMenu ipMenu : list) {
			if (DateUtils.addHour(ipMenu.getLast_opera_time(), 24).after(new Date())) { // 黑名单 时间+1天>现在
				switch (ipMenu.getType()) {
				case IpMenu.IP_BLACK:
					blacklistIpSerivceTimeWindow.putBlackIp(ipMenu.getIp(), ipMenu.getIp());
					break;
				case IpMenu.IP_LOCK:
					locklistIpSerivceTimeWindow.putLockIp(ipMenu.getIp(), ipMenu.getIp());
					break;
				}
			} else {
				ipMenu.setDelete_status(-1);
				checkTimeWindows(ipMenu);
				jdbcTemplate.update("UPDATE T_IP_MENU SET DELETE_STATUS=-1 WHERE UUID='"+ipMenu.getId().toString()+"'");
				redisHandler.setSync(DdosRedisKeys.IP_MENU_IP + ipMenu.getIp(), ipMenu);
			}
		}
	}

	@Override
	public void save(IpMenu entity) {
		if(null==entity) return;
		checkTimeWindows(entity);
		if(null==entity.getId()) entity.setId(ApplicationUtil.getCurrentTimeUUID());
		
		ApplicationUtil.executeInsert(entity);
		redisHandler.setSync(DdosRedisKeys.IP_MENU_IP + entity.getIp(), entity);
	}

	@Override
	public void update(IpMenu entity) {
		if(null==entity) return;
		checkTimeWindows(entity);
		
		ApplicationUtil.executeUpdate(entity, "WHERE UUID=?", new Object[] {entity.getId()});
		redisHandler.setSync(DdosRedisKeys.IP_MENU_IP + entity.getIp(), entity);
	}

	@Override
	public void delete(IpMenu entity) {
		if(null==entity) return;
		
		ApplicationUtil.executeDelete(IpMenu.class, "WHERE UUID=?", new Object[] {entity.getId()});
		redisHandler.remove(entity.getIp());
	}

	@Override
	public IpMenu cacheByIp(String ip) {
		return (IpMenu) redisHandler.get(DdosRedisKeys.IP_MENU_IP + ip);
	}

	/**
	 * 新增ip到白名单
	 * @param ip
	 */
	public void saveIpMenuWhite(String ip) {
		IpMenu ipMenu = this.cacheByIp(ip);
		if (null == ipMenu) {
			ipMenu = new IpMenu();
			ipMenu.setCreate_time(new Date());
			ipMenu.setDelete_status(0);
			ipMenu.setLast_opera_time(new Date());
			ipMenu.setType(IpMenu.IP_WHITE);
			ipMenu.setIp(ip);
			this.save(ipMenu);
		} else if (ipMenu.getDelete_status() == -1 || !IpMenu.IP_WHITE.equals(ipMenu.getType())) {// 名单被删除或者不是白名单
			ipMenu.setDelete_status(0);
			ipMenu.setLast_opera_time(new Date());
			ipMenu.setType(IpMenu.IP_WHITE);
			this.update(ipMenu);
		}
	}

	public void checkTimeWindows(IpMenu entity) {
		if (entity.getDelete_status() == -1) {
			blacklistIpSerivceTimeWindow.delBlackIp(entity.getIp());
			locklistIpSerivceTimeWindow.delLockIp(entity.getIp());
			return;
		}

		switch (entity.getType()) {
		case IpMenu.IP_WHITE:
			if (blacklistIpSerivceTimeWindow.getBlackIp(entity.getIp()) != null) {// 白名单直接删除黑名单缓存
				blacklistIpSerivceTimeWindow.delBlackIp(entity.getIp());
			}
			if (locklistIpSerivceTimeWindow.getLockIp(entity.getIp()) != null) {// 白名单直接删除锁定名单缓存
				locklistIpSerivceTimeWindow.delLockIp(entity.getIp());
			}
			break;
		case IpMenu.IP_BLACK:
			if (locklistIpSerivceTimeWindow.getLockIp(entity.getIp()) != null) {// 删除锁定名单缓存
				locklistIpSerivceTimeWindow.delLockIp(entity.getIp());
			}
			blacklistIpSerivceTimeWindow.putBlackIp(entity.getIp(), entity.getIp());
			break;
		case IpMenu.IP_LOCK:
			if (blacklistIpSerivceTimeWindow.getBlackIp(entity.getIp()) != null) {// 删除黑名单缓存
				blacklistIpSerivceTimeWindow.delBlackIp(entity.getIp());
			}
			locklistIpSerivceTimeWindow.putLockIp(entity.getIp(), entity.getIp());
			break;
		}
	}

	public void setRedisHandler(RedisHandler redisHandler) {
		this.redisHandler = redisHandler;
	}

	public void setBlacklistIpSerivceTimeWindow(BlacklistIpSerivceTimeWindow blacklistIpSerivceTimeWindow) {
		this.blacklistIpSerivceTimeWindow = blacklistIpSerivceTimeWindow;
	}

	public void setLocklistIpSerivceTimeWindow(LocklistIpSerivceTimeWindow locklistIpSerivceTimeWindow) {
		this.locklistIpSerivceTimeWindow = locklistIpSerivceTimeWindow;
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}
}
