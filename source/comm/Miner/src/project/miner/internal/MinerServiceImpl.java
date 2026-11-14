package project.miner.internal;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;

import kernel.exception.BusinessException;
import kernel.util.Arith;
import kernel.web.ApplicationUtil;
import project.data.DataService;
import project.data.model.Realtime;
import project.miner.MinerRedisKeys;
import project.miner.MinerService;
import project.miner.model.Miner;
import project.redis.RedisHandler;
import project.syspara.SysparaService;

public class MinerServiceImpl implements MinerService {

	protected JdbcTemplate jdbcTemplate;
	protected RedisHandler redisHandler;
	protected SysparaService sysparaService;
	protected DataService dataService;

	public Miner cacheById(String id) {
		return (Miner) redisHandler.get(MinerRedisKeys.MINER_ID + id);
	}

	public void save(Miner entity) {

		Object[] jdbcParams = ApplicationUtil.getInsertStatement(entity);
		String insertUserSql = (String)jdbcParams[0];
		Object[] sqlParameters = (Object[])jdbcParams[1];
		jdbcTemplate.update(insertUserSql,sqlParameters);
		
		redisHandler.setSync(MinerRedisKeys.MINER_ID + entity.getId().toString(), entity);

		Map<String, Miner> map = (Map<String, Miner>) redisHandler.get(MinerRedisKeys.MINER_MAP);
		if (map == null) {
			map = new ConcurrentHashMap<String, Miner>();
		}
		map.put(entity.getId().toString(), entity);
		redisHandler.setSync(MinerRedisKeys.MINER_MAP, map);

	}

	public void update(Miner entity) {
		
		Object[] jdbcParams=ApplicationUtil.getUpdateStatement(entity,"WHERE UUID=?",new Object[] {entity.getId()});
		String updateUserSql=(String)jdbcParams[0];
		Object[] sqlParameters=(Object[])jdbcParams[1];
		jdbcTemplate.update(updateUserSql,sqlParameters);
		
		redisHandler.setSync(MinerRedisKeys.MINER_ID + entity.getId().toString(), entity);

		Map<String, Miner> map = (Map<String, Miner>) redisHandler.get(MinerRedisKeys.MINER_MAP);
		if (map == null) {
			map = new ConcurrentHashMap<String, Miner>();
		}
		map.put(entity.getId().toString(), entity);
		redisHandler.setSync(MinerRedisKeys.MINER_MAP, map);
	}

	public void delete(String id) {
		Miner entity = findById(id);
		if (null != entity) {
			jdbcTemplate.update("DELETE FROM T_MINER WHERE UUID=?", entity.getId());

			redisHandler.remove(MinerRedisKeys.MINER_ID + entity.getId().toString());
			Map<String, Miner> map = (Map<String, Miner>) redisHandler.get(MinerRedisKeys.MINER_MAP);
			if (map != null && !map.isEmpty()) {
				map.remove(entity.getId().toString());
				redisHandler.setSync(MinerRedisKeys.MINER_MAP, map);
			}
		}
	}

	public Miner findById(String id) {
		return (Miner) redisHandler.get(MinerRedisKeys.MINER_ID + id);
	}

	public List<Miner> findAll() {
		Map<String, Miner> map = (Map<String, Miner>) redisHandler.get(MinerRedisKeys.MINER_MAP);
		if (map != null) {
			List<Miner> list = new ArrayList<>(map.values());
			list.sort(new Miner());
			return list;
		}
		return new ArrayList<>();
	}
	
	public Map<String, Miner> findMinerMap() {
		return (Map<String, Miner>) redisHandler.get(MinerRedisKeys.MINER_MAP);
	}

	public List<Miner> findAllState_1() {
		List<Miner> list = new ArrayList<Miner>();
		for (Miner miner : findAll()) {
			if ("1".equals(miner.getState())) {
				list.add(miner);
			}
		}
		return list;
	}
	
	public Map<String, Object> buildMiner(Miner miner) {
		Map<String, Object> result = new HashMap<String, Object>();

		result.put("id", miner.getId());
		result.put("name", miner.getName());
		result.put("name_en", miner.getName_en());
		result.put("name_cn", miner.getName_cn());
		result.put("daily_rate", miner.getDaily_rate());
		result.put("investment_min", miner.getInvestment_min());
		result.put("investment_max", miner.getInvestment_max());
		result.put("state", miner.getState());
		result.put("on_sale", miner.getOn_sale());
		result.put("test", miner.getTest());
		
		double symbolProfit = 0;
		if (miner.getTest()) {
			Double minerTestProfit = sysparaService.find("miner_test_profit").getDouble();
			result.put("all_rate", Arith.mul(minerTestProfit, miner.getCycle()));
			result.put("cycle", miner.getCycle());
			result.put("daily_rate", minerTestProfit);
			symbolProfit = minerTestProfit;
		} else {
			result.put("all_rate", Arith.mul(miner.getDaily_rate(), 30));
			result.put("cycle", miner.getCycle_close());
			// 100为单位的币种收益
			symbolProfit = Arith.div(Arith.mul(100, miner.getDaily_rate()), 100);
		}
		
		String buyCurrency = miner.getBuyCurrency();
		String outputCurrency = miner.getOutputCurrency();
		
//		// U购买 产出U
//		if ("usdt".equals(buyCurrency) && "usdt".equals(outputCurrency)) {
//			symbolProfit = symbolProfit;
//		}
		
		// U购买 产出第三方币
		if ("usdt".equals(buyCurrency) && !"usdt".equals(outputCurrency)) {
			List<Realtime> realtimes = this.dataService.realtime(outputCurrency);
			if (null == realtimes || realtimes.size() <= 0) {
				throw new BusinessException("行情获取异常，稍后再试");
			}
			symbolProfit = Arith.div(symbolProfit, realtimes.get(0).getClose());
		}
		
		// 第三方币购买 产出U
		if (!"usdt".equals(buyCurrency) && "usdt".equals(outputCurrency)) {
			List<Realtime> realtimes = this.dataService.realtime(buyCurrency);
			if (null == realtimes || realtimes.size() <= 0) {
				throw new BusinessException("行情获取异常，稍后再试");
			}
			symbolProfit = Arith.mul(symbolProfit, realtimes.get(0).getClose());
		}
		
		// 第三方币购买 产出第三方币
		if (!"usdt".equals(buyCurrency) && !"usdt".equals(outputCurrency)) {
//			// A购买 产出A
//			if (buyCurrency.equals(outputCurrency)) {
//				symbolProfit = symbolProfit;
//			}
			// A购买 产出B
			if (!buyCurrency.equals(outputCurrency)) {
				List<Realtime> realtimes = this.dataService.realtime(buyCurrency);
				if (null == realtimes || realtimes.size() <= 0) {
					throw new BusinessException("行情获取异常，稍后再试");
				}
				symbolProfit = Arith.mul(symbolProfit, realtimes.get(0).getClose());
				
				List<Realtime> realtimeList = this.dataService.realtime(outputCurrency);
				if (null == realtimeList || realtimeList.size() <= 0) {
					throw new BusinessException("行情获取异常，稍后再试");
				}
				symbolProfit = Arith.div(symbolProfit, realtimeList.get(0).getClose());
			}
		}
		
		result.put("symbol_profit",new BigDecimal(symbolProfit).setScale(4,RoundingMode.HALF_UP));
		result.put("buyCurrency", buyCurrency);
		result.put("outputCurrency", outputCurrency);
		result.put("miner_profit_symbol","usdt");
		result.put("algorithm", miner.getAlgorithm());
		result.put("computing_power", miner.getComputing_power());
		result.put("computing_power_unit", miner.getComputing_power_unit());
		result.put("power", miner.getPower());
		result.put("product_factory", miner.getProduct_factory());
		result.put("product_size", miner.getProduct_size());
		result.put("weight", miner.getWeight());
		result.put("work_temperature_min", miner.getWork_temperature_min());
		result.put("work_temperature_max", miner.getWork_temperature_max());
		result.put("work_humidity_min", miner.getWork_humidity_min());
		result.put("work_humidity_max", miner.getWork_humidity_max());
		result.put("internet", miner.getInternet());

		return result;
	}

	public Map<String, Object> getBindOne(Miner miner) {
		Map<String, Object> result = new HashMap<String, Object>();

		result.put("id", miner.getId());
		result.put("name", miner.getName());
		result.put("name_en", miner.getName_en());
		result.put("name_cn", miner.getName_cn());
		result.put("daily_rate", miner.getDaily_rate());
		result.put("investment_min", miner.getInvestment_min());
		result.put("investment_max", miner.getInvestment_max());
		result.put("state", miner.getState());
		result.put("on_sale", miner.getOn_sale());
		result.put("test", miner.getTest());
		Double miner_test_profit = sysparaService.find("miner_test_profit").getDouble();
		if (miner.getTest()) {
			result.put("all_rate", Arith.mul(miner_test_profit, miner.getCycle()));
			result.put("cycle", miner.getCycle());
			result.put("daily_rate", miner_test_profit);
		} else {
			result.put("all_rate", Arith.mul(miner.getDaily_rate(), 30));
			result.put("cycle", miner.getCycle_close());
		}

		// 根据产生的收益转化成指定的币种
		String miner_profit_symbol = sysparaService.find("miner_profit_symbol").getValue();
		// 矿机购买时使用的币种，则产生
		String miner_buy_symbol = sysparaService.find("miner_buy_symbol").getValue();
		double symbol_profit = miner.getTest() ? miner_test_profit
				: Arith.div(Arith.mul(100, miner.getDaily_rate()), 100);// 100为单位的币种收益
		// 收益转化成U
		if (StringUtils.isNotEmpty(miner_buy_symbol) && !"usdt".equalsIgnoreCase(miner_buy_symbol)) {
			List<Realtime> realtime_list = this.dataService.realtime(miner_buy_symbol);
			Realtime realtime = null;
			if (realtime_list.size() > 0) {
				realtime = realtime_list.get(0);
			} else {
				throw new BusinessException("行情获取异常，稍后再试");
			}
			symbol_profit = Arith.mul(symbol_profit, realtime.getClose());
		}

		if (StringUtils.isNotEmpty(miner_profit_symbol) && !"usdt".equalsIgnoreCase(miner_profit_symbol)) {
			List<Realtime> realtime_list = this.dataService.realtime(miner_profit_symbol);
			Realtime realtime = null;
			if (realtime_list.size() > 0) {
				realtime = realtime_list.get(0);
			} else {
				throw new BusinessException("行情获取异常，稍后再试");
			}
			symbol_profit = Arith.div(symbol_profit, realtime.getClose());
			result.put("symbol_profit", symbol_profit);
		} else {
			result.put("symbol_profit", symbol_profit);
		}
		result.put("miner_profit_symbol",
				StringUtils.isEmpty(miner_profit_symbol) ? "USDT" : miner_profit_symbol.toUpperCase());
		// 基础信息
		result.put("algorithm", miner.getAlgorithm());
		result.put("computing_power", miner.getComputing_power());
		result.put("computing_power_unit", miner.getComputing_power_unit());
		result.put("power", miner.getPower());
		result.put("product_factory", miner.getProduct_factory());
		result.put("product_size", miner.getProduct_size());
		result.put("weight", miner.getWeight());
		result.put("work_temperature_min", miner.getWork_temperature_min());
		result.put("work_temperature_max", miner.getWork_temperature_max());
		result.put("work_humidity_min", miner.getWork_humidity_min());
		result.put("work_humidity_max", miner.getWork_humidity_max());
		result.put("internet", miner.getInternet());

		return result;
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public void setRedisHandler(RedisHandler redisHandler) {
		this.redisHandler = redisHandler;
	}

	public void setSysparaService(SysparaService sysparaService) {
		this.sysparaService = sysparaService;
	}

	public void setDataService(DataService dataService) {
		this.dataService = dataService;
	}

}
