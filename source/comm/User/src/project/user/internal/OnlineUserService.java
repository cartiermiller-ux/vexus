package project.user.internal;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import project.redis.RedisHandler;
import project.user.UserRedisKeys;

public class OnlineUserService {
	
	private RedisHandler redisHandler;

	public Date get(String partyId) {
		return (Date) redisHandler.get(UserRedisKeys.ONLINEUSER_PARTYID + partyId);
	}

	public List<String> getAll() {
		Map<String, Date> map = (Map<String, Date>) redisHandler.get(UserRedisKeys.ONLINEUSER);
		if (map != null && !map.isEmpty()) {
			return new ArrayList<String>(map.keySet());
		}
		return new ArrayList<String>();

	}

	public void put(String partyId, Date date) {
		redisHandler.setSync(UserRedisKeys.ONLINEUSER_PARTYID + partyId, date);

		Map<String, Date> map = (Map<String, Date>) redisHandler.get(UserRedisKeys.ONLINEUSER);
		if (map == null) {
			map = new ConcurrentHashMap<String, Date>();
		}
		map.put(partyId, date);
		redisHandler.setSync(UserRedisKeys.ONLINEUSER, map);
	}

	public void del(String partyId) {
		redisHandler.remove(UserRedisKeys.ONLINEUSER_PARTYID + partyId);
		Map<String, Date> map = (Map<String, Date>) redisHandler.get(UserRedisKeys.ONLINEUSER);
		if (map != null && !map.isEmpty()) {
			map.remove(partyId);
			redisHandler.setSync(UserRedisKeys.ONLINEUSER, map);
		}
	}

	public void setRedisHandler(RedisHandler redisHandler) {
		this.redisHandler = redisHandler;
	}

}
