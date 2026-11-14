package project.data.loadcache;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import kernel.web.ApplicationUtil;
import project.party.PartyRedisKeys;
import project.party.model.Party;
import project.party.model.UserRecom;
import project.redis.RedisHandler;

public class PartyLoadCacheService {
	
	private RedisHandler redisHandler;

	private static final Logger logger = LoggerFactory.getLogger(PartyLoadCacheService.class);
	
	public void loadcache() {
		loadParty();
		loadUserRecom();
		logger.info("完成Party数据加载redis");
	}

	private void loadParty() {
		List<Party> list=ApplicationUtil.executeSelect(Party.class);
		Map<String, Party> cache = new ConcurrentHashMap<String, Party>();
		Map<String, Date> onlineCache = new ConcurrentHashMap<String, Date>();
		
		for (int i = 0; i < list.size(); i++) {
			Party party = list.get(i);
			redisHandler.setSync(PartyRedisKeys.PARTY_ID + party.getId().toString(), party);
			redisHandler.setSync(PartyRedisKeys.PARTY_USERNAME + party.getUsername(), party);
			cache.put(list.get(i).getId().toString(), party);
		}
		
		redisHandler.setSync(PartyRedisKeys.PARTY_ONLINEUSER, onlineCache);
	}
	
	private void loadUserRecom() {
		Map<String, List<UserRecom>> map = new ConcurrentHashMap<String, List<UserRecom>>();
		List<UserRecom> list = ApplicationUtil.executeSelect(UserRecom.class);
		for (int i = 0; i < list.size(); i++) {
			redisHandler.setSync(PartyRedisKeys.USER_RECOM_PARTYID + list.get(i).getPartyId(), list.get(i));
			List<UserRecom> recos = map.get(list.get(i).getReco_id().toString());
			if (recos == null) recos = new ArrayList<UserRecom>();
			recos.add(list.get(i));
			map.put(list.get(i).getReco_id().toString(), recos);
		}
		
		Map<String, Object> params = new ConcurrentHashMap<String, Object>();
		Iterator<Entry<String, List<UserRecom>>> it = map.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, List<UserRecom>> entry = it.next();
			params.put(PartyRedisKeys.USER_RECOM_RECO_ID + entry.getKey(), entry.getValue());
		}
		redisHandler.setBatchSync(params);

	}

	public void setRedisHandler(RedisHandler redisHandler) {
		this.redisHandler = redisHandler;
	}

}
