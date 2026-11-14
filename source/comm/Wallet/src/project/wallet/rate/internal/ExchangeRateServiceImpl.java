package project.wallet.rate.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import kernel.web.ApplicationUtil;
import kernel.web.Page;
import project.RedisKeys;
import project.redis.RedisHandler;
import project.wallet.rate.ExchangeRate;
import project.wallet.rate.ExchangeRateService;

@SuppressWarnings("unchecked")
public class ExchangeRateServiceImpl implements ExchangeRateService {

	private RedisHandler redisHandler;

	@Override
	public List<ExchangeRate> findBy(String out_or_in) {
		Map<String, ExchangeRate> map = (Map<String, ExchangeRate>) redisHandler.get(RedisKeys.EXCHANGE_RATE_OUTORIN + out_or_in);
		return null==map?Collections.emptyList():new ArrayList<ExchangeRate>(map.values());
	}

	@Override
	public ExchangeRate findBy(String out_or_in, String currency) {
		Map<String, ExchangeRate> map = (Map<String, ExchangeRate>) redisHandler.get(RedisKeys.EXCHANGE_RATE_OUTORIN + out_or_in);
		return null==map?null:map.get(currency);
	}

	public Page pagedQuery(int pageNo, int pageSize, String out_or_in) {
		StringBuilder selectSqlBuilder=new StringBuilder("WHERE 1=1 ");
		ArrayList<Object> whereParamList=new ArrayList<Object>();
		if (null!=out_or_in && !(out_or_in=out_or_in.trim()).isEmpty()) {
			selectSqlBuilder.append("AND OUT_OR_IN=? ");
			whereParamList.add(out_or_in);
		}
		
		selectSqlBuilder.append("ORDER BY UUID DESC LIMIT ?,?");
		
		Page page = new Page(pageNo, pageSize, Integer.MAX_VALUE);
		whereParamList.add(page.getFirstElementNumber());
		whereParamList.add(pageSize);
		
		List<ExchangeRate> list=ApplicationUtil.executeSelect(ExchangeRate.class, selectSqlBuilder.toString(),whereParamList.toArray(new Object[] {whereParamList.size()}));
		page.setElements(list);
		return page;
	}

	@Override
	public void update(ExchangeRate entity) {
		redisHandler.setSync(RedisKeys.EXCHANGE_RATE_ID + entity.getId().toString(), entity);
		redisHandler.setSync(RedisKeys.EXCHANGE_RATE_CURRENCY + entity.getCurrency(), entity);
		Map<String, ExchangeRate> map = (Map<String, ExchangeRate>) redisHandler.get(RedisKeys.EXCHANGE_RATE_OUTORIN + entity.getOut_or_in());
		if (null==map) map = new ConcurrentHashMap<String, ExchangeRate>();
		map.put(entity.getCurrency(), entity);
		redisHandler.setSync(RedisKeys.EXCHANGE_RATE_OUTORIN + entity.getOut_or_in(), map);
		ApplicationUtil.executeUpdate(entity, "WHERE UUID=?", new Object[] {entity.getId()});
	}

	@Override
	public ExchangeRate get(String id) {
		return (ExchangeRate) redisHandler.get(RedisKeys.EXCHANGE_RATE_ID + id);
	}

	public void setRedisHandler(RedisHandler redisHandler) {
		this.redisHandler = redisHandler;
	}
}
