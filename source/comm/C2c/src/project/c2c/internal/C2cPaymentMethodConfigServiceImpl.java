package project.c2c.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.jdbc.core.JdbcTemplate;

import kernel.bo.RecordObjectMapper;
import project.RedisKeys;
import project.c2c.C2cPaymentMethodConfig;
import project.c2c.C2cPaymentMethodConfigService;
import project.redis.RedisHandler;

public class C2cPaymentMethodConfigServiceImpl implements C2cPaymentMethodConfigService {
	
	private RedisHandler redisHandler;
	private JdbcTemplate jdbcTemplate;

	@Override
	public C2cPaymentMethodConfig get(String id) {
		return (C2cPaymentMethodConfig) this.redisHandler.get(RedisKeys.C2C_PAYMENT_METHOD_CONFIG_ID + id);
	}
	
	@Override
	public C2cPaymentMethodConfig getC2cPaymentMethodConfig(String id) {
        List<C2cPaymentMethodConfig> list = jdbcTemplate.query("SELECT * FROM T_C2C_PAYMENT_METHOD_CONFIG WHERE UUID=?", RecordObjectMapper.newInstance(C2cPaymentMethodConfig.class), id);
        if (null != list && list.size() > 0) {
        	return list.get(0);
        }
        return null;
	}
	
	@Override
	public Map<String, String> getMethodConfigIdTypeMap() {
		Map<String, String> map = (Map<String, String>) this.redisHandler.get(RedisKeys.C2C_PAYMENT_METHOD_CONFIG_ID_TYPE);
		if (null == map || map.isEmpty()) {
			return new HashMap<String, String>();
		}
		return map;
	}

	@Override
	public void save(C2cPaymentMethodConfig entity) {
		if (null == entity) {
			return;
		}
		String insertSql = "INSERT INTO T_C2C_PAYMENT_METHOD_CONFIG(UUID,METHOD_TYPE,METHOD_NAME,METHOD_IMG,PARAM_NAME1,PARAM_NAME2,"
				+ "PARAM_NAME3,PARAM_NAME4,PARAM_NAME5,PARAM_NAME6,PARAM_NAME7,PARAM_NAME8,PARAM_NAME9,PARAM_NAME10,PARAM_NAME11,"
				+ "PARAM_NAME12,PARAM_NAME13,PARAM_NAME14,PARAM_NAME15,CREATE_TIME,UPDATE_TIME) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		jdbcTemplate.update(insertSql,entity.getId(),entity.getMethodType(),entity.getMethodName(),entity.getMethodImg(),entity.getParamName1(),
				entity.getParamName2(),entity.getParamName3(),entity.getParamName4(),entity.getParamName5(),entity.getParamName6(),
				entity.getParamName7(),entity.getParamName8(),entity.getParamName9(),entity.getParamName10(),entity.getParamName11(),
				entity.getParamName12(),entity.getParamName13(),entity.getParamName14(),entity.getParamName15(),entity.getCreateTime(),entity.getUpdateTime());
	}

	@Override
	public void delete(String id) {
		C2cPaymentMethodConfig entity = this.get(id);
		if (entity != null) {
			jdbcTemplate.update("DELETE FROM T_C2C_PAYMENT_METHOD_CONFIG WHERE UUID=?", entity.getId());
			this.redisHandler.remove(RedisKeys.C2C_PAYMENT_METHOD_CONFIG_ID + entity.getId().toString());

			Map<String, String> map1 = (Map<String, String>) this.redisHandler.get(RedisKeys.C2C_PAYMENT_METHOD_CONFIG_ID_TYPE);
			if (map1 != null && !map1.isEmpty()) {
				map1.remove(id);
			} else {
				map1 = new ConcurrentHashMap<String, String>();
			}
			this.redisHandler.setSync(RedisKeys.C2C_PAYMENT_METHOD_CONFIG_ID_TYPE, map1);
		}
	}

	@Override
	public boolean update(C2cPaymentMethodConfig entity) {
		try {
			String updateSql = "UPDATE T_C2C_PAYMENT_METHOD_CONFIG SET METHOD_TYPE=?, METHOD_NAME=?, METHOD_IMG=?, "
					+ "PARAM_NAME1=?, PARAM_NAME2=?, PARAM_NAME3=?, PARAM_NAME4=?, PARAM_NAME5=?, PARAM_NAME6=?, PARAM_NAME7=?, PARAM_NAME8=?, PARAM_NAME9=?, PARAM_NAME10=?, PARAM_NAME11=?, PARAM_NAME12=?, PARAM_NAME13=?, PARAM_NAME14=?, PARAM_NAME15=?, "
					+ "UPDATE_TIME=? WHERE UUID=?";
			jdbcTemplate.update(updateSql, entity.getMethodType(), entity.getMethodName(), entity.getMethodImg(), 
					entity.getParamName1(),entity.getParamName2(),entity.getParamName3(),entity.getParamName4(),
					entity.getParamName5(),entity.getParamName6(),entity.getParamName7(),entity.getParamName8(),
					entity.getParamName9(),entity.getParamName10(),entity.getParamName11(),entity.getParamName12(),
					entity.getParamName13(),entity.getParamName14(),entity.getParamName15(),entity.getUpdateTime(), entity.getId());
			return true;
		} catch(RuntimeException e) {
			return false;
		}
	}
	
	public List<C2cPaymentMethodConfig> getAll() {
		List<C2cPaymentMethodConfig> list = jdbcTemplate.query("SELECT * FROM T_C2C_PAYMENT_METHOD_CONFIG", RecordObjectMapper.newInstance(C2cPaymentMethodConfig.class));
		if (null == list) {
			list = new ArrayList<C2cPaymentMethodConfig>();
		}
		return list;
	}
	
	public Map<String, String> getMethodConfigMap() {
		
		List<C2cPaymentMethodConfig> methodConfigList = this.getAll();
		
		Map<String, String> methodConfigMap = new HashMap<String, String>();
		for (int i = 0; i < methodConfigList.size(); i++) {
			C2cPaymentMethodConfig config = methodConfigList.get(i);
			if (null != config) {
				methodConfigMap.put(config.getId().toString(), config.getMethodName());
			}
		}
		
		return methodConfigMap;
	}

	public void setRedisHandler(RedisHandler redisHandler) {
		this.redisHandler = redisHandler;
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

}
