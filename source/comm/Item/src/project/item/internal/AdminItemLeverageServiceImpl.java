package project.item.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import kernel.web.ApplicationUtil;
import kernel.web.Page;
import project.item.AdminItemLeverageService;
import project.item.ItemRedisKeys;
import project.item.model.ItemLever;
import project.redis.RedisHandler;

@SuppressWarnings("unchecked")
public class AdminItemLeverageServiceImpl implements AdminItemLeverageService {
	
	private RedisHandler redisHandler;

	@Override
	public Page pagedQuery(int pageNo, int pageSize, String item_id) {
		if (pageNo <= 0) pageNo = 1;
		Page page = new Page(pageNo, pageSize, Integer.MAX_VALUE);
		
		StringBuilder whereBuilder=new StringBuilder("WHERE 1=1 ");
		ArrayList<Object> whereParams=new ArrayList<Object>();
		if (null!=item_id && !(item_id=item_id.trim()).isEmpty()) {
			whereBuilder.append("AND ITEM_ID=? ");
			whereParams.add(item_id);
		}
		
		whereBuilder.append("LIMIT ?,?");
		whereParams.add(page.getFirstElementNumber());
		whereParams.add(pageSize);
		
		List<ItemLever> list=ApplicationUtil.executeSelect(ItemLever.class, whereBuilder.toString(), whereParams.toArray(new Object[whereParams.size()]));
		page.setElements(list);
		return page;
	}

	public ItemLever get(String id) {
		if(null==id || (id=id.trim()).isEmpty()) return null;
		List<ItemLever> list=ApplicationUtil.executeSelect(ItemLever.class,"WHERE UUID=?",new Object[] {id});
		return (null==list || list.isEmpty())?null:list.get(0);
	}

	@Override
	public void save(ItemLever entity) {
		if(null==entity) return;
		if(null==entity.getId()) entity.setId(ApplicationUtil.getCurrentTimeUUID());
		ApplicationUtil.executeInsert(entity);
		
		Map<String, ItemLever> map = (Map<String, ItemLever>) redisHandler.get(ItemRedisKeys.ITEM_LEVER_ID + entity.getItem_id());
		if (null==map) map=new ConcurrentHashMap<String, ItemLever>();
		map.put(entity.getId().toString(), entity);
		redisHandler.setSync(ItemRedisKeys.ITEM_LEVER_ID + entity.getItem_id(), map);
	}

	@Override
	public void delete(String id) {
		ItemLever entity = get(id);
		if(null==entity) return;
		
		ApplicationUtil.executeDelete(ItemLever.class, "WHERE UUID=?", new Object[] {id});
		
		Map<String, ItemLever> map = (Map<String, ItemLever>) redisHandler.get(ItemRedisKeys.ITEM_LEVER_ID + entity.getItem_id());
		if (null!=map) map.remove(id);
		redisHandler.setSync(ItemRedisKeys.ITEM_LEVER_ID + entity.getItem_id(), map);
	}

	public void setRedisHandler(RedisHandler redisHandler) {
		this.redisHandler = redisHandler;
	}
}
