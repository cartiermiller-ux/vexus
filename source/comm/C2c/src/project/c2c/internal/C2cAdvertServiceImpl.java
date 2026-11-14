package project.c2c.internal;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.jdbc.core.JdbcTemplate;

import kernel.bo.RecordObjectMapper;
import kernel.exception.BusinessException;
import kernel.util.Arith;
import kernel.util.StringUtils;
import kernel.web.Page;
import kernel.web.PagedQueryDao;
import project.Constants;
import project.RedisKeys;
import project.c2c.C2cAdvert;
import project.c2c.C2cAdvertService;
import project.c2c.C2cOrder;
import project.c2c.C2cPaymentMethod;
import project.data.DataService;
import project.data.model.Realtime;
import project.item.ItemService;
import project.item.model.Item;
import project.redis.RedisHandler;
import project.syspara.SysparaService;
import project.wallet.rate.ExchangeRate;
import project.wallet.rate.ExchangeRateService;

public class C2cAdvertServiceImpl implements C2cAdvertService {

	private PagedQueryDao pagedDao;
	private RedisHandler redisHandler;
	private ExchangeRateService exchangeRateService;
	private ItemService itemService;
	private DataService dataService;
	private SysparaService sysparaService;
	private JdbcTemplate jdbcTemplate;

	public Page pagedQuery(int pageNo, int pageSize, String c2c_user_id, String direction, String currency, String symbol, String amount, 
			Integer on_sale, Integer closed, boolean is_c2c_user) {

		StringBuffer queryString = new StringBuffer();

		Map<String, Object> parameters = new HashMap<String, Object>();
		
//		queryString.append(" SELECT ");
//		queryString.append(" advert.UUID id, advert.DIRECTION direction, advert.CURRENCY currency, advert.SYMBOL symbol, advert.PAY_RATE pay_rate, advert.PAY_TYPE pay_type, ");
//		queryString.append(" advert.SYMBOL_VALUE symbol_value, advert.COIN_AMOUNT coin_amount, advert.INVESTMENT_MIN investment_min, advert.INVESTMENT_MAX investment_max, ");
//		queryString.append(" advert.DEPOSIT deposit, advert.DEPOSIT_OPEN deposit_open, advert.ON_SALE on_sale, advert.SORT_INDEX sort_index, ");
//		queryString.append(" advert.EXPIRE_TIME expire_time, advert.TRANSACTION_TERMS transaction_terms, advert.ORDER_MSG order_msg, ");
//		queryString.append(" advert.REMARK remark, advert.CREATE_TIME create_time, advert.UPDATE_TIME update_time, ");
//		queryString.append(" c2c_user.UUID c2c_user_id, c2c_user.NICK_NAME nick_name, c2c_user.HEAD_IMG head_img, c2c_user.C2C_USER_TYPE c2c_user_type, c2c_user.C2C_USER_CODE c2c_user_code, ");
//		queryString.append(" party.UUID party_id, party.USERCODE user_code, party.USERNAME user_name ");
//		
//		queryString.append(" FROM T_C2C_ADVERT advert ");
//		queryString.append(" LEFT JOIN T_C2C_USER c2c_user ON c2c_user.UUID = advert.C2C_USER_ID ");
//		queryString.append(" LEFT JOIN PAT_PARTY party ON party.UUID = c2c_user.C2C_USER_PARTY_ID ");		
//		queryString.append(" WHERE 1=1 ");
//		queryString.append(" AND advert.ON_SALE = 1 ");
		
		if (is_c2c_user) {
			queryString.append(" SELECT ");
			queryString.append(" advert.UUID id, advert.C2C_USER_ID c2c_user_id, advert.DIRECTION direction, advert.CURRENCY currency, advert.SYMBOL symbol, advert.PAY_TYPE pay_type, ");
			queryString.append(" advert.SYMBOL_VALUE symbol_value, advert.COIN_AMOUNT coin_amount, advert.INVESTMENT_MIN investment_min, advert.INVESTMENT_MAX investment_max, ");
			queryString.append(" advert.ON_SALE on_sale, advert.TRANSACTION_TERMS transaction_terms ");
			
			queryString.append(" FROM T_C2C_ADVERT advert ");
			queryString.append(" WHERE 1=1 ");	
						
			if (StringUtils.isNotEmpty(c2c_user_id)) {
				queryString.append(" AND advert.C2C_USER_ID = :c2c_user_id ");
				parameters.put("c2c_user_id", c2c_user_id);
			}
		} else {
			queryString.append(" SELECT ");
			queryString.append(" advert.UUID id, advert.C2C_USER_ID c2c_user_id, advert.DIRECTION direction, advert.CURRENCY currency, advert.SYMBOL symbol, advert.PAY_TYPE pay_type, ");
			queryString.append(" advert.SYMBOL_VALUE symbol_value, advert.COIN_AMOUNT coin_amount, advert.INVESTMENT_MIN investment_min, advert.INVESTMENT_MAX investment_max, ");
			queryString.append(" advert.ON_SALE on_sale, advert.TRANSACTION_TERMS transaction_terms, c2c_user.NICK_NAME nick_name, c2c_user.HEAD_IMG head_img, ");
			queryString.append(" c2c_user.THIRTY_DAYS_ORDER thirty_days_order, c2c_user.THIRTY_DAYS_ORDER_RATIO thirty_days_order_ratio, ");
			queryString.append(" param_base_set.THIRTY_DAYS_ORDER thirty_days_order_base, param_base_set.THIRTY_DAYS_ORDER_RATIO thirty_days_order_ratio_base ");
			
			queryString.append(" FROM T_C2C_ADVERT advert ");
			queryString.append(" LEFT JOIN T_C2C_USER c2c_user ON c2c_user.UUID = advert.C2C_USER_ID ");
			queryString.append(" LEFT JOIN T_C2C_USER_PARAM_BASE_SET param_base_set ON c2c_user.C2C_USER_PARTY_ID = param_base_set.C2C_USER_PARTY_ID ");
			queryString.append(" WHERE 1=1 ");
			
			if (StringUtils.isNotEmpty(c2c_user_id)) {
				queryString.append(" AND advert.C2C_USER_ID != :c2c_user_id ");
				parameters.put("c2c_user_id", c2c_user_id);
			}
		}
		
		if (StringUtils.isNotEmpty(direction)) {
			queryString.append(" AND advert.DIRECTION = :direction ");
			parameters.put("direction", direction);
		}
		
		if (StringUtils.isNotEmpty(currency)) {
			queryString.append(" AND advert.CURRENCY = :currency ");
			parameters.put("currency", currency);
		}
		
		if (StringUtils.isNotEmpty(symbol)) {
			queryString.append(" AND advert.SYMBOL = :symbol ");
			parameters.put("symbol", symbol);
		}
		
		if (StringUtils.isNotEmpty(amount)) {
			double amount_double = Double.valueOf(amount).doubleValue();
			queryString.append(" AND (advert.COIN_AMOUNT * advert.SYMBOL_VALUE) >= :amount_double ");
			parameters.put("amount_double", amount_double);
		}
		
		if (null != on_sale) {
			queryString.append(" AND advert.ON_SALE = :on_sale ");
			parameters.put("on_sale", on_sale);
		}
		
		if (null != closed) {
			queryString.append(" AND advert.CLOSED = :closed ");
			parameters.put("closed", closed);
		}
		
	    if (C2cAdvert.DIRECTION_BUY.equals(direction)) {
	    	// 用户买币从低到高
	    	queryString.append(" order by advert.SYMBOL_VALUE asc ");
	    } else if (C2cAdvert.DIRECTION_SELL.equals(direction)) {
	    	// 用户卖币从高到低
	    	queryString.append(" order by advert.SYMBOL_VALUE desc ");
	    } else {
	    	queryString.append(" order by advert.SORT_INDEX desc,advert.CREATE_TIME desc ");
	    }

		Page page = this.pagedDao.pagedQuerySQL(pageNo, pageSize, queryString.toString(), parameters);
		
		DecimalFormat df = new DecimalFormat("#.##");
		// 币种默认保留8位
		DecimalFormat dfCoin = new DecimalFormat("#.########");
		
		for (Map<String, Object> data : (List<Map<String, Object>>) page.getElements()) {
			data.put("symbol_value", df.format(data.get("symbol_value")));
			data.put("coin_amount", dfCoin.format(data.get("coin_amount")));
		}
		
		return page;
	}

	public Page pagedQueryQuick(int pageNo, int pageSize, String currency, String symbol, String direction, double amount, double coinAmount, String method_config_id) {
		
		StringBuffer queryString = new StringBuffer();
		
		queryString.append(" SELECT ");
		queryString.append(" advert.UUID id, advert.C2C_USER_ID c2c_user_id, advert.DIRECTION direction ");
		
		queryString.append(" FROM T_C2C_ADVERT advert ");
		queryString.append(" WHERE 1=1 ");
		queryString.append(" AND advert.ON_SALE = 1 ");
		queryString.append(" AND advert.CLOSED = 0 ");

		Map<String, Object> parameters = new HashMap<String, Object>();
		
		if (StringUtils.isNotEmpty(direction)) {
			queryString.append(" AND advert.DIRECTION = :direction ");
			parameters.put("direction", direction);
		}
		
		if (StringUtils.isNotEmpty(currency)) {
			queryString.append(" AND advert.CURRENCY = :currency ");
			parameters.put("currency", currency);
		}
		
		if (StringUtils.isNotEmpty(symbol)) {
			queryString.append(" AND advert.SYMBOL = :symbol ");
			parameters.put("symbol", symbol);
		}
		
		if (amount > 0d) {
			// 按支付金额支付
			queryString.append(" AND advert.INVESTMENT_MIN <= :amount  ");
			queryString.append(" AND advert.INVESTMENT_MAX >= :amount  ");
			queryString.append(" AND advert.COIN_AMOUNT >= (:amount / advert.SYMBOL_VALUE)");
			parameters.put("amount", amount);
		}
		
		if (coinAmount > 0d) {
			// 按币种数量支付
			queryString.append(" AND advert.COIN_AMOUNT >= :coinAmount  ");
			queryString.append(" AND advert.INVESTMENT_MIN <= (:coinAmount * advert.SYMBOL_VALUE)");
			queryString.append(" AND advert.INVESTMENT_MAX >= (:coinAmount * advert.SYMBOL_VALUE)");
			parameters.put("coinAmount", coinAmount);
		}
		
		if (StringUtils.isNotEmpty(method_config_id)) {
			queryString.append(" AND advert.PAY_TYPE like :method_config_id ");
			parameters.put("method_config_id", "%" + method_config_id + "%");
		}
		
		queryString.append(" order by advert.SORT_INDEX desc,advert.CREATE_TIME desc ");
		
		Page page = this.pagedDao.pagedQuerySQL(pageNo, pageSize, queryString.toString(), parameters);		
		return page;
	}
	
	/*
	 * 获取 用户所有支付方式  最优价格广告
	 */
	public Map<String, Map<String, Object>> getBestPriceAdvert(String currency, String symbol, String direction, String order_type, double amount, double coinAmount, List<C2cPaymentMethod> cpmList) {
		
		Map<String, Map<String, Object>> retMap = new HashMap<String, Map<String,Object>>();
		
		Set<String> methodConfigIdSet = new HashSet<String>();
		for (int i = 0; i < cpmList.size(); i++) {
			C2cPaymentMethod cpm = cpmList.get(i);
			if (null != cpm) {
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("id", cpm.getId().toString());
				map.put("payment_method_id", cpm.getId().toString());
				map.put("method_name", cpm.getMethodName());
				map.put("method_img", cpm.getMethodImg());				
				map.put("method_img_path", Constants.WEB_URL + "/public/showimg!showImg.action?imagePath=" + cpm.getMethodImg());
				map.put("advert_id", "");
				map.put("advert_price", 0D);
				retMap.put(cpm.getId().toString(), map);
				methodConfigIdSet.add(cpm.getMethodConfigId());
			}
		}
		
		// 支付方式模板 对应的最低最高价格
		Map<String, String> advertIdMap = new HashMap<String, String>();
		Map<String, Double> minPriceMap = new HashMap<String, Double>();
		Map<String, Double> maxPriceMap = new HashMap<String, Double>();
		
		Map<String, Double> mapPrice = (Map<String, Double>) this.redisHandler.get(RedisKeys.C2C_ADVERT_CURRENCY_SYMBOL_DIRECTION + currency + symbol + direction);
		if (null == mapPrice || 0 == mapPrice.size()) {
			return retMap;
		} else {
			for (String id : mapPrice.keySet()) {
				C2cAdvert advert = this.get(id);
				
				if (0 == advert.getOnSale()) {
					continue;
				}

				if (1 == advert.getClosed()) {
					continue;
				}
				
				if (C2cOrder.ORDER_TYPE_BY_AMOUNT.equals(order_type)) {
					// 按支付金额支付
					if (amount < advert.getInvestmentMin() || amount > advert.getInvestmentMax() || advert.getCoinAmount() < Arith.div(amount, advert.getSymbolValue())) {
						continue;
					}
				} else {
					// 按币种数量支付
					double amount1 = Arith.mul(coinAmount, advert.getSymbolValue());
					if (amount1 < advert.getInvestmentMin() || amount1 > advert.getInvestmentMax() || advert.getCoinAmount() < coinAmount) {
						continue;
					}
				}
				
				String payType = advert.getPayType();
				if (StringUtils.isEmptyString(payType)) {
					continue;
				}
				String[] payTypeArr = payType.split(",");
				for (int i = 0; i < payTypeArr.length; i++) {
					String mci = payTypeArr[i];
					if (methodConfigIdSet.contains(mci)) {
						
						if (C2cAdvert.DIRECTION_BUY.equals(direction)) {
							// 买币
							Double nowMin = minPriceMap.get(mci);
							if (null == nowMin) {
								minPriceMap.put(mci, advert.getSymbolValue());
								advertIdMap.put(mci, advert.getId().toString());
							} else {
								if (nowMin.doubleValue() > advert.getSymbolValue()) {
									minPriceMap.put(mci, advert.getSymbolValue());
									advertIdMap.put(mci, advert.getId().toString());
								}
							}
						} else if (C2cAdvert.DIRECTION_SELL.equals(direction)) {
							// 卖币							
							Double nowMax = maxPriceMap.get(mci);
							if (null == nowMax) {
								maxPriceMap.put(mci, advert.getSymbolValue());
								advertIdMap.put(mci, advert.getId().toString());
							} else {
								if (nowMax.doubleValue() < advert.getSymbolValue()) {
									maxPriceMap.put(mci, advert.getSymbolValue());
									advertIdMap.put(mci, advert.getId().toString());
								}
							}
						}
					}
				}
			}
		}
		
		for (int i = 0; i < cpmList.size(); i++) {
			C2cPaymentMethod cpm = cpmList.get(i);
			if (null != cpm) {
				
				Map<String, Object> map = retMap.get(cpm.getId().toString());
				
				if (C2cAdvert.DIRECTION_BUY.equals(direction)) {
					// 买币
					Double minPrice = minPriceMap.get(cpm.getMethodConfigId());
					if (null != minPrice) {
						map.put("advert_id", advertIdMap.get(cpm.getMethodConfigId()));
						map.put("advert_price", minPrice);
					}
				} else if (C2cAdvert.DIRECTION_SELL.equals(direction)) {
					// 卖币
					Double maxPrice = maxPriceMap.get(cpm.getMethodConfigId());
					if (null != maxPrice) {
						map.put("advert_id", advertIdMap.get(cpm.getMethodConfigId()));
						map.put("advert_price", maxPrice);
					}
				}
				
				retMap.put(cpm.getId().toString(), map);
			}
		}
		
		Map<String, Map<String, Object>> resMap = new HashMap<String, Map<String, Object>>();
		for (String id : retMap.keySet()) {
			Map<String, Object> map = retMap.get(id);
			if (null != map && null != map.get("advert_id") && null != map.get("advert_price") 
					&& StringUtils.isNotEmpty(map.get("advert_id").toString()) && 0 != ((Double) map.get("advert_price")).doubleValue()) {
				resMap.put(id, map);
			}
		}
		
		return resMap;
	}
	
	public C2cAdvert get(String id) {
		return (C2cAdvert) this.redisHandler.get(RedisKeys.C2C_ADVERT_ID + id);
	}
	
	public List<C2cAdvert> getByC2cUserId(String c2c_user_id) {
		Map<String, C2cAdvert> map = (Map<String, C2cAdvert>) this.redisHandler.get(RedisKeys.C2C_ADVERT_C2C_USER_ID + c2c_user_id);
		if (null == map || map.isEmpty()) {
			return new ArrayList<C2cAdvert>();
		} else {
			List<C2cAdvert> list = new ArrayList<C2cAdvert>();
			for (C2cAdvert advert : map.values()) {
				if (null != advert) {
					list.add(advert);
				}
			}
			return list;
		}
	}
	
	public void save(C2cAdvert entity) {
		if (null == entity) {
			return;
		}
		
		String insertSql = "INSERT INTO T_C2C_ADVERT(UUID,C2C_USER_ID,DIRECTION,CURRENCY,SYMBOL,SYMBOL_CLOSE,PAY_RATE,PAY_TYPE,SYMBOL_VALUE,COIN_AMOUNT,INVESTMENT_MIN,INVESTMENT_MAX,DEPOSIT,DEPOSIT_OPEN,ON_SALE,CLOSED,SORT_INDEX,EXPIRE_TIME,TRANSACTION_TERMS,ORDER_MSG,REMARK,CREATE_TIME,UPDATE_TIME) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		jdbcTemplate.update(
				insertSql,entity.getId(),entity.getC2cUserId(),entity.getDirection(), 
				entity.getCurrency(),entity.getSymbol(),entity.getSymbolClose(),entity.getPayRate(), 
				entity.getPayType(),entity.getSymbolValue(),entity.getCoinAmount(),entity.getInvestmentMin(), 
        		entity.getInvestmentMax(),entity.getDeposit(),entity.getDepositOpen(),entity.getOnSale(), 
        		entity.getClosed(),entity.getSortIndex(),entity.getExpireTime(),entity.getTransactionTerms(), 
        		entity.getOrderMsg(),entity.getRemark(),entity.getCreateTime(),entity.getUpdateTime());
        
		this.redisHandler.setSync(RedisKeys.C2C_ADVERT_ID + entity.getId().toString(), entity);

		// 承兑商的广告
		Map<String, C2cAdvert> map = (Map<String, C2cAdvert>) this.redisHandler.get(RedisKeys.C2C_ADVERT_C2C_USER_ID + entity.getC2cUserId());
		if (null == map) {
			map = new ConcurrentHashMap<String, C2cAdvert>();
		}
		map.put(entity.getId().toString(), entity);
		this.redisHandler.setSync(RedisKeys.C2C_ADVERT_C2C_USER_ID + entity.getC2cUserId(), map);

		// 广告上架币种单价
		Map<String, Double> mapPrice = (Map<String, Double>) this.redisHandler.get(
				RedisKeys.C2C_ADVERT_CURRENCY_SYMBOL_DIRECTION + entity.getCurrency() + entity.getSymbol() + entity.getDirection());
		if (null == mapPrice) {
			mapPrice = new ConcurrentHashMap<String, Double>();
		}
		mapPrice.put(entity.getId().toString(), entity.getSymbolValue());
		this.redisHandler.setSync(RedisKeys.C2C_ADVERT_CURRENCY_SYMBOL_DIRECTION + entity.getCurrency() + entity.getSymbol() + entity.getDirection(), mapPrice);
	}

	public void update(C2cAdvert entity) {
		if (null == entity) {
			return;
		}
		C2cAdvert oldEntity = (C2cAdvert) this.redisHandler.get(RedisKeys.C2C_ADVERT_ID + entity.getId().toString());
		
		String updateSql = "UPDATE T_C2C_ADVERT SET C2C_USER_ID=?, DIRECTION=?, CURRENCY=?, SYMBOL=?, SYMBOL_CLOSE=?, PAY_RATE=?, PAY_TYPE=?, SYMBOL_VALUE=?, COIN_AMOUNT=?, INVESTMENT_MIN=?, INVESTMENT_MAX=?, DEPOSIT=?, DEPOSIT_OPEN=?, ON_SALE=?, CLOSED=?, SORT_INDEX=?, EXPIRE_TIME=?, TRANSACTION_TERMS=?, ORDER_MSG=?, REMARK=?, UPDATE_TIME=? WHERE UUID=?";
		jdbcTemplate.update(updateSql, entity.getC2cUserId(), entity.getDirection(), entity.getCurrency(), entity.getSymbol(), entity.getSymbolClose(), entity.getPayRate(), entity.getPayType(), 
        		entity.getSymbolValue(), entity.getCoinAmount(), entity.getInvestmentMin(), entity.getInvestmentMax(), entity.getDeposit(), entity.getDepositOpen(), entity.getOnSale(), entity.getClosed(), entity.getSortIndex(), entity.getExpireTime(), entity.getTransactionTerms(), 
        		entity.getOrderMsg(), entity.getRemark(), entity.getUpdateTime(), entity.getId());
		
		this.redisHandler.setSync(RedisKeys.C2C_ADVERT_ID + entity.getId().toString(), entity);

		// 承兑商的广告
		Map<String, C2cAdvert> map = (Map<String, C2cAdvert>) this.redisHandler.get(RedisKeys.C2C_ADVERT_C2C_USER_ID + entity.getC2cUserId());
		if (null == map) {
			map = new ConcurrentHashMap<String, C2cAdvert>();
		}
		map.put(entity.getId().toString(), entity);
		this.redisHandler.setSync(RedisKeys.C2C_ADVERT_C2C_USER_ID + entity.getC2cUserId(), map);	
		
		// 广告上架币种单价
		if (null != oldEntity && (!oldEntity.getCurrency().equals(entity.getCurrency()) 
				|| !oldEntity.getSymbol().equals(entity.getSymbol()) 
				|| !oldEntity.getDirection().equals(entity.getDirection()))) {
			// 删除之前的
			Map<String, Double> mapPriceOld = (Map<String, Double>) this.redisHandler.get(
					RedisKeys.C2C_ADVERT_CURRENCY_SYMBOL_DIRECTION + oldEntity.getCurrency() + oldEntity.getSymbol() + oldEntity.getDirection());
			if (null != mapPriceOld) {
				this.redisHandler.remove(RedisKeys.C2C_ADVERT_CURRENCY_SYMBOL_DIRECTION + oldEntity.getCurrency() + oldEntity.getSymbol() + oldEntity.getDirection());
			}
		}
		Map<String, Double> mapPrice = (Map<String, Double>) this.redisHandler.get(
				RedisKeys.C2C_ADVERT_CURRENCY_SYMBOL_DIRECTION + entity.getCurrency() + entity.getSymbol() + entity.getDirection());
		if (null == mapPrice) {
			mapPrice = new ConcurrentHashMap<String, Double>();
		}
		mapPrice.put(entity.getId().toString(), entity.getSymbolValue());
		this.redisHandler.setSync(RedisKeys.C2C_ADVERT_CURRENCY_SYMBOL_DIRECTION + entity.getCurrency() + entity.getSymbol() + entity.getDirection(), mapPrice);
	}

	public void delete(String id) {
		C2cAdvert entity = this.get(id);
		if (entity != null) {
						
			C2cAdvert oldEntity = (C2cAdvert) this.redisHandler.get(RedisKeys.C2C_ADVERT_ID + entity.getId().toString());
			
	        jdbcTemplate.update("DELETE FROM T_C2C_ADVERT WHERE UUID=?", entity.getId());
	        
			this.redisHandler.remove(RedisKeys.C2C_ADVERT_ID + entity.getId().toString());

			Map<String, C2cAdvert> map = (Map<String, C2cAdvert>) this.redisHandler.get(RedisKeys.C2C_ADVERT_C2C_USER_ID + entity.getC2cUserId());
			if (map != null && !map.isEmpty()) {
				map.remove(id);
			} else {
				map = new ConcurrentHashMap<String, C2cAdvert>();
			}
			this.redisHandler.setSync(RedisKeys.C2C_ADVERT_C2C_USER_ID + entity.getC2cUserId(), map);
			
			Map<String, Double> mapPriceOld = (Map<String, Double>) this.redisHandler.get(
					RedisKeys.C2C_ADVERT_CURRENCY_SYMBOL_DIRECTION + oldEntity.getCurrency() + oldEntity.getSymbol() + oldEntity.getDirection());
			if (null != mapPriceOld) {
				this.redisHandler.remove(RedisKeys.C2C_ADVERT_CURRENCY_SYMBOL_DIRECTION + oldEntity.getCurrency() + oldEntity.getSymbol() + oldEntity.getDirection());
			}
		}
	}

	public C2cAdvert findById(String id) {
        List<C2cAdvert> list = jdbcTemplate.query("SELECT * FROM T_C2C_ADVERT WHERE UUID=?", RecordObjectMapper.newInstance(C2cAdvert.class), id);
        if (null != list && list.size() > 0) {
        	return list.get(0);
        }
        return null;
	}

	/**
	 * 获取 支付币种Map
	 */
	public Map<String, String> getCurrencyMap() {
		
		// 获取 C2C支付币种配置
		Map<String, String> curMap = this.getC2cSyspara("c2c_advert_currency");
		if (null == curMap) {
			curMap = new HashMap<String, String>();
		}
		
		Map<String, String> currencyMap = new HashMap<String, String>();
		
		List<ExchangeRate> exchangeRateList = this.exchangeRateService.findBy(Constants.OUT_OR_IN_DEFAULT);
		for (ExchangeRate er : exchangeRateList) {
			if (curMap.keySet().contains(er.getCurrency())) {
				currencyMap.put(er.getCurrency(), String.format("%s(%s)", er.getCurrency(), er.getName()));
			}
		}
		return currencyMap;
	}

	/**
	 * 获取 上架币种Map
	 */
	public Map<String, String> getSymbolMap() {
		
		// 获取 C2C上架币种配置
		Map<String, String> symMap = this.getC2cSyspara("c2c_advert_symbol");
		if (null == symMap) {
			symMap = new HashMap<String, String>();
		}
		
		Map<String, String> symbolMap = new HashMap<String, String>();

		List<Item> itemList = this.itemService.cacheGetAll();
		if (symMap.keySet().contains("USDT")) {
			symbolMap.put("usdt", "USDT");
		}
		for (Item item : itemList) {
			if (symMap.keySet().contains(item.getSymbol().toUpperCase())) {
				symbolMap.put(item.getSymbol(), item.getSymbol().toUpperCase());
			}
		}
		return symbolMap;
	}
	
	/**
	 * 计算广告参数
	 */
	public Map<String, Object> getComputeValue(double deposit_total, String currency, String symbol, double coin_amount, double symbol_value) {
		
		Map<String, Object> result = new HashMap<String, Object>();
		
		double symbol_close = 0d;
		
		if (symbol.equals("usdt")) {
			symbol_close = 1;
		} else {
			List<Realtime> list = this.dataService.realtime(symbol);
			if (0 == list.size()) {
//				throw new BusinessException(symbol.toUpperCase() + "行情获取异常，请重试");
				throw new BusinessException("行情获取异常，请重试");
			}
			Realtime realtime = list.get(0);
			symbol_close = realtime.getClose();
		}
		
		if (0 == symbol_close) {
//			throw new BusinessException(symbol.toUpperCase() + "行情获取异常，请重试");
			throw new BusinessException("行情获取异常，请重试");
		}
		
		ExchangeRate ex = this.exchangeRateService.findBy(Constants.OUT_OR_IN_DEFAULT, currency);
		
		// 支付比率=支付币种汇率*上架币种实时行情价/币种单价；例如，支付比率95%，1USDT=7.3CNY*1*95%=6.935CNY
		double payRate = Arith.mul(Arith.div(Arith.mul(ex.getRata(), symbol_close), symbol_value), 100);
		
		// 广告保证金=交易币种数量*上架币种实时行情价
		double depositOpen = Arith.mul(coin_amount, symbol_close);
		
		// 支付币种市价=支付币种汇率*上架币种实时行情价；
		double price = Arith.mul(ex.getRata(), symbol_close);
		
		// 交易币种数量=广告派单额度/上架币种实时行情价
		double coinAmountMax = Arith.div(deposit_total, symbol_close);
		
		// 最小支付金额
		double investmentMinLimit = 0;
		
		// C2C承兑商广告：单笔订单最低限额不能低于（支付币种金额折算成USDT）
		Object obj = this.sysparaService.find("c2c_user_advert_investment_min_limit");
		if (null != obj) {
			String c2c_user_advert_investment_min_limit = this.sysparaService.find("c2c_user_advert_investment_min_limit").getValue();
			if (!StringUtils.isEmptyString(c2c_user_advert_investment_min_limit)) {
				double limit_usdt = Double.valueOf(c2c_user_advert_investment_min_limit).doubleValue();
				investmentMinLimit = Arith.mul(Arith.div(limit_usdt, symbol_close), symbol_value);
			}
		}
		
		// 最大支付金额
		double investmentMaxLimit = Arith.mul(coinAmountMax, symbol_value);
		
		DecimalFormat df = new DecimalFormat("#.########");
		
		result.put("pay_rate", (int) payRate);
		result.put("deposit_open", df.format(new Double(depositOpen)));
		result.put("all_deposit", df.format(Arith.sub(deposit_total, depositOpen)));
		result.put("symbol_close", df.format(new Double(symbol_close)));
		result.put("price", df.format(new Double(price)));
		result.put("coin_amount_max", df.format(new Double(coinAmountMax)));
		result.put("investment_min_limit", df.format(new Double(investmentMinLimit)));
		result.put("investment_max_limit", df.format(new Double(investmentMaxLimit)));
		return result;
	}
	
	/**
	 * 获取所有上架币种单价
	 */
	public Map<String, String> getAllSymbolPrice(String currency) {
		
		DecimalFormat df = new DecimalFormat("#.##");
		
		Map<String, String> allPrice = new HashMap<String, String>();
		
		ExchangeRate ex = this.exchangeRateService.findBy(Constants.OUT_OR_IN_DEFAULT, currency);
		
		Map<String, String> allSymbol = this.getC2cSyspara("c2c_advert_symbol");
		if (null == allSymbol || 0 == allSymbol.size()) {
			return new HashMap<String, String>();
		}
		
		for (String symbol : allSymbol.keySet()) {
			symbol = symbol.toLowerCase();
			double symbol_close = 0d;

			if (symbol.equals("usdt")) {
				symbol_close = 1;
			} else {
				List<Realtime> list = this.dataService.realtime(symbol);
				if (0 == list.size()) {
					continue;
				}
				Realtime realtime = list.get(0);
				symbol_close = realtime.getClose();
			}			
			if (0 == symbol_close) {
				continue;
			}
			
			// 支付币种市价=支付币种汇率*上架币种实时行情价；
			double price = Arith.mul(ex.getRata(), symbol_close);
			allPrice.put(symbol, df.format(new Double(price)));
		}
		
		return allPrice;
	}
	
	/*
	 * 获取 C2C支付方式类型、C2C支付币种配置、C2C上架币种配置、C2C广告支付时效
	 */
	public Map<String, String> getC2cSyspara(String syspara) {
		
		if ("c2c_payment_method_type".equals(syspara)) {
			
			// C2C支付方式类型
			Map<String, String> pmtMap = new HashMap<String, String>();
			Object obj = this.sysparaService.find("c2c_payment_method_type");
			if (null != obj) {
				String pmtStr = this.sysparaService.find("c2c_payment_method_type").getValue().toString();
				String[] pmtArray = pmtStr.split("&&");
				for (int i = 0; i < pmtArray.length; i++) {
					String[] pmt = pmtArray[i].split("##");
					pmtMap.put(pmt[0], pmt[1]);
				}
				return pmtMap;
			}
		} else if ("c2c_advert_currency".equals(syspara)) {
			
			// C2C支付币种配置
			Map<String, String> acMap = new HashMap<String, String>();
			Object obj = this.sysparaService.find("c2c_advert_currency");
			if (null != obj) {
				String acStr = this.sysparaService.find("c2c_advert_currency").getValue().toString();
				String[] acArray = acStr.split("&&");
				for (int i = 0; i < acArray.length; i++) {
					String[] ac = acArray[i].split("##");
					acMap.put(ac[0], ac[1]);
				}
				return acMap;
			}
		} else if ("c2c_advert_symbol".equals(syspara)) {
			
			// C2C上架币种配置
			Map<String, String> asMap = new HashMap<String, String>();
			Object obj = this.sysparaService.find("c2c_advert_symbol");
			if (null != obj) {
				String asStr = this.sysparaService.find("c2c_advert_symbol").getValue().toString();
				String[] asArray = asStr.split("##");
				for (int i = 0; i < asArray.length; i++) {
					asMap.put(asArray[i], asArray[i]);
				}
				return asMap;
			}
		} else if ("c2c_advert_expire_time".equals(syspara)) {
			
			// C2C广告支付时效
			Map<String, String> aetMap = new HashMap<String, String>();
			Object obj = this.sysparaService.find("c2c_advert_expire_time");
			if (null != obj) {
				String aetStr = this.sysparaService.find("c2c_advert_expire_time").getValue().toString();
				String[] aetArray = aetStr.split("&&");
				for (int i = 0; i < aetArray.length; i++) {
					String[] aet = aetArray[i].split("##");
					aetMap.put(aet[0], aet[1]);
				}
				return aetMap;
			}
		}
		
		return new HashMap<String, String>();
	}
	
	/*
	 * 获取 语种说明
	 */
	public String getLanguageIntro() {		
		Map<String, String> langMap = Constants.LANGUAGE;
		String retStr = "";
		for (String lang : langMap.keySet()) {
			String langName = langMap.get(lang);
			if (StringUtils.isEmptyString(retStr)) {
				retStr = lang + " " + langName;
			} else {
				retStr += "; " + lang + " " + langName;
			}
		}
		return retStr + ";";
	}
	
	/*
	 * 获取 支付方式类型说明
	 */
	public String getMethodTypeIntro() {		
		Map<String, String> pmtMap = this.getC2cSyspara("c2c_payment_method_type");
		String retStr = "";
		for (String pmt : pmtMap.keySet()) {
			String pmtName = pmtMap.get(pmt);
			if (StringUtils.isEmptyString(retStr)) {
				retStr = pmt + " " + pmtName;
			} else {
				retStr += "; " + pmt + " " + pmtName;
			}
		}
		return retStr + ";";
	}

	public void setPagedDao(PagedQueryDao pagedDao) {
		this.pagedDao = pagedDao;
	}

	public void setRedisHandler(RedisHandler redisHandler) {
		this.redisHandler = redisHandler;
	}

	public void setExchangeRateService(ExchangeRateService exchangeRateService) {
		this.exchangeRateService = exchangeRateService;
	}

	public void setItemService(ItemService itemService) {
		this.itemService = itemService;
	}

	public void setDataService(DataService dataService) {
		this.dataService = dataService;
	}

	public void setSysparaService(SysparaService sysparaService) {
		this.sysparaService = sysparaService;
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

}
