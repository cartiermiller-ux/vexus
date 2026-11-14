package project.data.loadcache;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import kernel.web.ApplicationUtil;
import project.item.ItemRedisKeys;
import project.item.model.Item;
import project.item.model.ItemLever;
import project.item.model.ItemUserOptional;
import project.redis.RedisHandler;

public class ItemLoadCacheService {
	
	private RedisHandler redisHandler;

	private Logger logger = LoggerFactory.getLogger(ItemLoadCacheService.class); 
	
	public void loadcache() {
		loadItem();
		loadItemLever();
		loadItemUserOptional();
		logger.info("完成Item数据加载redis");
	}

	private void loadItem() {
		Map<String, Item> cache = new ConcurrentHashMap<String, Item>();
		List<Item> list=ApplicationUtil.executeSelect(Item.class);
		for (int i = 0; i < list.size(); i++) {
			Item item = list.get(i);
			redisHandler.setSync(ItemRedisKeys.ITEM_SYMBOL + item.getSymbol(), item);
			cache.put(list.get(i).getSymbol(), item);
		}
		redisHandler.setSync(ItemRedisKeys.ITEM_MAP, cache);

	}

	private void loadItemLever() {
		List<ItemLever> list=ApplicationUtil.executeSelect(ItemLever.class);
		Map<String, Map<String, ItemLever>> lever_map = new ConcurrentHashMap<String, Map<String, ItemLever>>();
		for (int i = 0; i < list.size(); i++) {
			ItemLever itemLever = list.get(i);
			Map<String, ItemLever> lever_list = lever_map.get(itemLever.getItem_id());
			if (lever_list == null) {
				lever_list = new ConcurrentHashMap<String, ItemLever>();
			}
			lever_list.put(itemLever.getId().toString(), itemLever);
			lever_map.put(itemLever.getItem_id(), lever_list);

		}

		Map<String, Object> params = new ConcurrentHashMap<String, Object>();
		Iterator<Entry<String, Map<String, ItemLever>>> it = lever_map.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, Map<String, ItemLever>> entry = it.next();
			params.put(ItemRedisKeys.ITEM_LEVER_ID + entry.getKey(), entry.getValue());
		}
		redisHandler.setBatchSync(params);

	}

	public void loadItemUserOptional() {
		Map<String, Map<String, ItemUserOptional>> cacheMap = new ConcurrentHashMap<String, Map<String, ItemUserOptional>>();
		List<ItemUserOptional> list=ApplicationUtil.executeSelect(ItemUserOptional.class);
		for (ItemUserOptional entity : list) {
			if (cacheMap.containsKey(entity.getPartyId())) {
				Map<String, ItemUserOptional> map = cacheMap.get(entity.getPartyId().toString());
				map.put(entity.getSymbol(), entity);
				cacheMap.put(entity.getPartyId().toString(), map);
			} else {
				Map<String, ItemUserOptional> map = new ConcurrentHashMap<String, ItemUserOptional>();
				map.put(entity.getSymbol(), entity);
				cacheMap.put(entity.getPartyId().toString(), map);
			}
		}

		for (Entry<String, Map<String, ItemUserOptional>> entry : cacheMap.entrySet()) {
			redisHandler.setSync(ItemRedisKeys.ITEM_USER_OPTIONAL_MAP_PARTY_ID + entry.getKey(), entry.getValue());
		}
	}

	public void setRedisHandler(RedisHandler redisHandler) {
		this.redisHandler = redisHandler;
	}
}
