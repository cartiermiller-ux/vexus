package project.c2c.data.loadcache;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import kernel.bo.RecordObjectMapper;
import kernel.util.StringUtils;
import project.RedisKeys;
import project.c2c.C2cAdvert;
import project.c2c.C2cAppeal;
import project.c2c.C2cOrder;
import project.c2c.C2cPaymentMethod;
import project.c2c.C2cPaymentMethodConfig;
import project.c2c.C2cTranslate;
import project.c2c.C2cTranslateService;
import project.c2c.C2cUser;
import project.c2c.C2cUserParamBaseSet;
import project.redis.RedisHandler;

public class C2cLoadCacheService {
	
	private static final Logger logger = LoggerFactory.getLogger(C2cLoadCacheService.class);

	private RedisHandler redisHandler;
	private JdbcTemplate jdbcTemplate;
	private C2cTranslateService c2cTranslateService;

	public void loadcache() {
		loadC2cPaymentMethodConfig();
		loadC2cTranslate();
		loadC2cPaymentMethod();
		loadC2cUser();
		loadC2cUserParamBaseSet();
		loadC2cAdvert();
		loadC2cOrder();
		loadC2cAppeal();
		logger.info("完成C2C数据加载redis");
	}

	/*
	 * 加载 C2C支付方式模板
	 */
	public void loadC2cPaymentMethodConfig() {
		
		Map<String, String> map = new ConcurrentHashMap<String, String>();
		
		String sql = "SELECT * FROM T_C2C_PAYMENT_METHOD_CONFIG";
		List<C2cPaymentMethodConfig> list = jdbcTemplate.query(sql, RecordObjectMapper.newInstance(C2cPaymentMethodConfig.class));
		
		for (int i = 0; i < list.size(); i++) {	
			
			C2cPaymentMethodConfig entity = list.get(i);
			if (null != entity) {
				this.redisHandler.setSync(RedisKeys.C2C_PAYMENT_METHOD_CONFIG_ID + entity.getId().toString(), entity);				
				map.put(entity.getId().toString(), String.valueOf(entity.getMethodType()));
			}
		}

		this.redisHandler.setSync(RedisKeys.C2C_PAYMENT_METHOD_CONFIG_ID_TYPE, map);
	}

	/*
	 * 加载 C2C翻译
	 */
	public void loadC2cTranslate() {
		
		// 所有的要翻译内容集合
		Set<String> contentSet = new HashSet<String>();
		
		// 支付方式类型名称列表
		List<String> nameList = this.c2cTranslateService.getAllPaymentMethodTypeName();
		for (int i = 0; i < nameList.size(); i++) {
			String name = nameList.get(i);
			if (null != name) {
				contentSet.add(name);
			}
		}
		
		// 获取所有的支付方式模板
		String sql = "SELECT * FROM T_C2C_PAYMENT_METHOD_CONFIG";
		List<C2cPaymentMethodConfig> list = jdbcTemplate.query(sql, RecordObjectMapper.newInstance(C2cPaymentMethodConfig.class));
		
		for (int i = 0; i < list.size(); i++) {
			C2cPaymentMethodConfig config = list.get(i);
			if (null != config) {
				if (StringUtils.isNotEmpty(config.getMethodName())) {
					contentSet.add(config.getMethodName());
				}
				if (StringUtils.isNotEmpty(config.getParamName1())) {
					contentSet.add(config.getParamName1());
				}
				if (StringUtils.isNotEmpty(config.getParamName2())) {
					contentSet.add(config.getParamName2());
				}
				if (StringUtils.isNotEmpty(config.getParamName3())) {
					contentSet.add(config.getParamName3());
				}
				if (StringUtils.isNotEmpty(config.getParamName4())) {
					contentSet.add(config.getParamName4());
				}
				if (StringUtils.isNotEmpty(config.getParamName5())) {
					contentSet.add(config.getParamName5());
				}
				if (StringUtils.isNotEmpty(config.getParamName6())) {
					contentSet.add(config.getParamName6());
				}
				if (StringUtils.isNotEmpty(config.getParamName7())) {
					contentSet.add(config.getParamName7());
				}
				if (StringUtils.isNotEmpty(config.getParamName8())) {
					contentSet.add(config.getParamName8());
				}
				if (StringUtils.isNotEmpty(config.getParamName9())) {
					contentSet.add(config.getParamName9());
				}
				if (StringUtils.isNotEmpty(config.getParamName10())) {
					contentSet.add(config.getParamName10());
				}
				if (StringUtils.isNotEmpty(config.getParamName11())) {
					contentSet.add(config.getParamName11());
				}
				if (StringUtils.isNotEmpty(config.getParamName12())) {
					contentSet.add(config.getParamName12());
				}
				if (StringUtils.isNotEmpty(config.getParamName13())) {
					contentSet.add(config.getParamName13());
				}
				if (StringUtils.isNotEmpty(config.getParamName14())) {
					contentSet.add(config.getParamName14());
				}
				if (StringUtils.isNotEmpty(config.getParamName15())) {
					contentSet.add(config.getParamName15());
				}
			}
		}
		
		// 获取 所有的支付方式模板 翻译
		String sql1 = "SELECT * FROM T_C2C_TRANSLATE";
		List<C2cTranslate> list1 = jdbcTemplate.query(sql1, RecordObjectMapper.newInstance(C2cTranslate.class));
		
		for (int i = 0; i < list1.size(); i++) {
			
			C2cTranslate entity = list1.get(i);
			if (null != entity) {
				
				if (contentSet.contains(entity.getContent())) {
					this.redisHandler.setSync(RedisKeys.C2C_TRANSLATE_CONTENT_LANGUAGE + StringUtils.stringToUnicode(entity.getContent()) + entity.getLanguage(), entity);
				} else {
					// 不需要的翻译删除
					this.c2cTranslateService.delete(entity.getContent(), entity.getLanguage());
				}
			}
		}
	}
	
	/*
	 * 加载 C2C支付方式
	 */
	public void loadC2cPaymentMethod() {
		
		Map<String, List<C2cPaymentMethod>> map = new ConcurrentHashMap<String, List<C2cPaymentMethod>>();
		
		Map<String, String> map1 = new ConcurrentHashMap<String, String>();
		
		String sql = "SELECT * FROM T_C2C_PAYMENT_METHOD";
		List<C2cPaymentMethod> list = jdbcTemplate.query(sql, RecordObjectMapper.newInstance(C2cPaymentMethod.class));
		
		for (int i = 0; i < list.size(); i++) {	
			
			C2cPaymentMethod entity = list.get(i);
			if (null != entity) {		
				this.redisHandler.setSync(RedisKeys.C2C_PAYMENT_METHOD_ID + entity.getId().toString(), entity);				
				map1.put(entity.getId().toString(), String.valueOf(entity.getMethodType()));
			}
			
			List<C2cPaymentMethod> methodList = map.get(entity.getPartyId().toString());
			if (null == methodList) {
				methodList = new ArrayList<C2cPaymentMethod>();
			}
			methodList.add(entity);
			map.put(entity.getPartyId().toString(), methodList);			
		}

		this.redisHandler.setSync(RedisKeys.C2C_PAYMENT_METHOD_ID_TYPE, map1);
		
		for (String key : map.keySet()) {
			List<C2cPaymentMethod> ml = map.get(key);
			if (null != ml) {
				
				Map<String, C2cPaymentMethod> mm = new ConcurrentHashMap<String, C2cPaymentMethod>();

				for (int i = 0; i < ml.size(); i++) {
					C2cPaymentMethod method = ml.get(i);
					if (null != method) {
						mm.put(method.getId().toString(), method);
					}
				}

				this.redisHandler.setSync(RedisKeys.C2C_PAYMENT_METHOD_PARTY_ID + key, mm);
			}
		}
	}

	/*
	 * 加载 C2C承兑商信息
	 */
	public void loadC2cUser() {
		
		String sql = "SELECT * FROM T_C2C_USER";
		List<C2cUser> list = jdbcTemplate.query(sql, RecordObjectMapper.newInstance(C2cUser.class));
		
		for (int i = 0; i < list.size(); i++) {	
			
			C2cUser entity = list.get(i);
			if (null != entity) {
				this.redisHandler.setSync(RedisKeys.C2C_USER_ID + entity.getId().toString(), entity);
				this.redisHandler.setSync(RedisKeys.C2C_USER_PARTY_ID + entity.getC2cUserPartyId(), entity);
			}
		}
	}

	/*
	 * 加载 C2C承兑商参数基础设置信息
	 */
	public void loadC2cUserParamBaseSet() {
		
		String sql = "SELECT * FROM T_C2C_USER_PARAM_BASE_SET";
		List<C2cUserParamBaseSet> list = jdbcTemplate.query(sql, RecordObjectMapper.newInstance(C2cUserParamBaseSet.class));
		
		for (int i = 0; i < list.size(); i++) {	
			
			C2cUserParamBaseSet entity = list.get(i);
			if (null != entity) {
				this.redisHandler.setSync(RedisKeys.C2C_USER_PARAM_BASE_SET_PARTY_ID + entity.getC2cUserPartyId(), entity);
			}
		}
	}

	/*
	 * 加载 C2C广告信息
	 */
	public void loadC2cAdvert() {
		
		Map<String, List<C2cAdvert>> map = new ConcurrentHashMap<String, List<C2cAdvert>>();
		Map<String, List<C2cAdvert>> mapPrice = new ConcurrentHashMap<String, List<C2cAdvert>>();
		
		String sql = "SELECT * FROM T_C2C_ADVERT";
		List<C2cAdvert> list = jdbcTemplate.query(sql, RecordObjectMapper.newInstance(C2cAdvert.class));
		
		for (int i = 0; i < list.size(); i++) {	
			
			C2cAdvert entity = list.get(i);
			if (null != entity) {
				this.redisHandler.setSync(RedisKeys.C2C_ADVERT_ID + entity.getId().toString(), entity);
			}
			
			List<C2cAdvert> advertList = map.get(entity.getC2cUserId());
			if (null == advertList) {
				advertList = new ArrayList<C2cAdvert>();
			}
			advertList.add(entity);
			map.put(entity.getC2cUserId(), advertList);
			
			List<C2cAdvert> advertList1 = mapPrice.get(entity.getCurrency() + entity.getSymbol() + entity.getDirection());
			if (null == advertList1) {
				advertList1 = new ArrayList<C2cAdvert>();
			}
			advertList1.add(entity);
			mapPrice.put(entity.getCurrency() + entity.getSymbol() + entity.getDirection(), advertList1);
		}
		
		for (String key : map.keySet()) {
			List<C2cAdvert> al = map.get(key);
			if (null != al) {
				
				Map<String, C2cAdvert> am = new ConcurrentHashMap<String, C2cAdvert>();

				for (int i = 0; i < al.size(); i++) {
					C2cAdvert advert = al.get(i);
					if (null != advert) {
						am.put(advert.getId().toString(), advert);
					}
				}

				this.redisHandler.setSync(RedisKeys.C2C_ADVERT_C2C_USER_ID + key, am);
			}
		}
		
		for (String key : mapPrice.keySet()) {
			List<C2cAdvert> al = mapPrice.get(key);
			if (null != al) {
				
				Map<String, Double> am = new ConcurrentHashMap<String, Double>();

				for (int i = 0; i < al.size(); i++) {
					C2cAdvert advert = al.get(i);
					if (null != advert) {
						am.put(advert.getId().toString(), advert.getSymbolValue());
					}
				}

				this.redisHandler.setSync(RedisKeys.C2C_ADVERT_CURRENCY_SYMBOL_DIRECTION + key, am);
			}
		}		
	}

	/*
	 * 加载 C2C订单
	 */
	public void loadC2cOrder() {
		
		Map<String, Long> map = new ConcurrentHashMap<String, Long>();
		Map<String, Long> ocMap = new ConcurrentHashMap<String, Long>();
		
		String sql = "SELECT * FROM T_C2C_ORDER";
		List<C2cOrder> list = jdbcTemplate.query(sql, RecordObjectMapper.newInstance(C2cOrder.class));
		
		for (int i = 0; i < list.size(); i++) {	
			
			C2cOrder entity = list.get(i);
			if (null != entity && Arrays.asList("0", "1").contains(entity.getState())) {
				
				// 只有未付款和已付款的订单存入redis				
				this.redisHandler.setSync(RedisKeys.C2C_ORDER_NO + entity.getOrderNo(), entity);
				
				// 订单自动取消时间和确认收款超时时间（0未付款/1已付款）
				if (Arrays.asList("0").contains(entity.getState())) {
					map.put(entity.getOrderNo() + "_auto_cancel", entity.getCreateTime().getTime() + entity.getExpireTime() * 60 * 1000);
					map.put(entity.getOrderNo() + "_expire", entity.getCreateTime().getTime() + entity.getExpireTime() * 60 * 1000 * 2);
				} else if (Arrays.asList("1").contains(entity.getState())) {
					map.put(entity.getOrderNo() + "_auto_cancel", entity.getCreateTime().getTime() + entity.getExpireTime() * 60 * 1000);
					map.put(entity.getOrderNo() + "_expire", entity.getPayTime().getTime() + entity.getExpireTime() * 60 * 1000);
				}
			}
			
			// 用户未结束订单数量（0未付款/1已付款/2申诉中/5已超时）
			if (!Arrays.asList("3", "4").contains(entity.getState())) {
				Long count = ocMap.get(entity.getPartyId());
				if (null == count) {
					ocMap.put(entity.getPartyId(), 1L);
				} else {
					ocMap.put(entity.getPartyId(), count + 1);
				}
			}
		}

		this.redisHandler.setSync(RedisKeys.C2C_ORDER_NO_EXPIRE_TIME, map);
		this.redisHandler.setSync(RedisKeys.C2C_NOFINISH_ORDER_COUNT, ocMap);
	}
	
	/*
	 * 加载 C2C申诉
	 */
	public void loadC2cAppeal() {
		
		String sql = "SELECT * FROM T_C2C_APPEAL";
		List<C2cAppeal> list = jdbcTemplate.query(sql, RecordObjectMapper.newInstance(C2cAppeal.class));
		
		for (int i = 0; i < list.size(); i++) {	
			
			C2cAppeal entity = list.get(i);
			if (null != entity && Arrays.asList("0").contains(entity.getState())) {
				// 只有已提交的申诉存入redis
				
				this.redisHandler.setSync(RedisKeys.C2C_APPEAL_ORDER_NO + entity.getOrderNo(), entity);
			}
		}
	}

	public void setRedisHandler(RedisHandler redisHandler) {
		this.redisHandler = redisHandler;
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public void setC2cTranslateService(C2cTranslateService c2cTranslateService) {
		this.c2cTranslateService = c2cTranslateService;
	}

}
