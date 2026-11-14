package project.c2c.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.jdbc.core.JdbcTemplate;

import kernel.bo.RecordObjectMapper;
import project.RedisKeys;
import project.c2c.C2cPaymentMethod;
import project.c2c.C2cPaymentMethodService;
import project.redis.RedisHandler;

public class C2cPaymentMethodServiceImpl implements C2cPaymentMethodService {
	
	private RedisHandler redisHandler;
	private JdbcTemplate jdbcTemplate;

	@Override
	public C2cPaymentMethod get(String id) {
		return (C2cPaymentMethod) this.redisHandler.get(RedisKeys.C2C_PAYMENT_METHOD_ID + id);
	}
	
	@Override
	public C2cPaymentMethod getC2cPaymentMethod(String id) {
		List<C2cPaymentMethod> list = jdbcTemplate.query("SELECT * FROM T_C2C_PAYMENT_METHOD WHERE UUID=?", RecordObjectMapper.newInstance(C2cPaymentMethod.class), id);
        if (null != list && list.size() > 0) {
        	return list.get(0);
        }
        return null;
	}
	
	@Override
	public Map<String, C2cPaymentMethod> getByPartyId(String partyId) {
		Map<String, C2cPaymentMethod> map = (Map<String, C2cPaymentMethod>) this.redisHandler.get(RedisKeys.C2C_PAYMENT_METHOD_PARTY_ID + partyId);
		if (null == map || 0 == map.size()) {
			return new HashMap<String, C2cPaymentMethod>();
		}
		return map;
	}
	
	@Override
	public Map<String, String> getMethodConfigMapByPartyId(String partyId) {		
		Map<String, String> res = new HashMap<String, String>();
		
		Map<String, C2cPaymentMethod> methodMap = this.getByPartyId(partyId);
		if (null == methodMap || 0 == methodMap.size()) {
			return res;
		}
		
		for (String key : methodMap.keySet()) {
			C2cPaymentMethod method = methodMap.get(key);
			if (null != method) {
				res.put(method.getMethodConfigId(), method.getMethodName());
			}
		}
		
		return res;
	}
	
	@Override
	public List<C2cPaymentMethod> getMethodConfigListByPartyId(String partyId) {		
		List<C2cPaymentMethod> res = new ArrayList<C2cPaymentMethod>();
		
		Map<String, C2cPaymentMethod> methodMap = this.getByPartyId(partyId);
		if (null == methodMap || 0 == methodMap.size()) {
			return res;
		}
		
		for (String key : methodMap.keySet()) {
			C2cPaymentMethod method = methodMap.get(key);
			if (null != method) {
				res.add(method);
			}
		}
		
		return res;		
	}
	
	@Override
	public Map<String, String> getMethodIdTypeMap() {
		Map<String, String> map = (Map<String, String>) this.redisHandler.get(RedisKeys.C2C_PAYMENT_METHOD_ID_TYPE);
		if (null == map || map.isEmpty()) {
			return new HashMap<String, String>();
		}
		return map;
	}
	
	@Override
	public void save(C2cPaymentMethod entity) {
		String insertSql = "INSERT INTO T_C2C_PAYMENT_METHOD(UUID,PARTY_ID,METHOD_CONFIG_ID,METHOD_TYPE,METHOD_NAME,METHOD_IMG,"
				+ "REAL_NAME,PARAM_NAME1,PARAM_VALUE1,PARAM_NAME2,PARAM_VALUE2,PARAM_NAME3,PARAM_VALUE3,PARAM_NAME4,PARAM_VALUE4,"
				+ "PARAM_NAME5,PARAM_VALUE5,PARAM_NAME6,PARAM_VALUE6,PARAM_NAME7,PARAM_VALUE7,PARAM_NAME8,PARAM_VALUE8,PARAM_NAME9,"
				+ "PARAM_VALUE9,PARAM_NAME10,PARAM_VALUE10,PARAM_NAME11,PARAM_VALUE11,PARAM_NAME12,PARAM_VALUE12,PARAM_NAME13,"
				+ "PARAM_VALUE13,PARAM_NAME14,PARAM_VALUE14,PARAM_NAME15,PARAM_VALUE15,QRCODE,REMARK,CREATE_TIME,UPDATE_TIME) "
				+ "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		jdbcTemplate.update(insertSql,entity.getId(),entity.getPartyId(),entity.getMethodConfigId(),entity.getMethodType(),entity.getMethodName(),
				entity.getMethodImg(),entity.getRealName(),entity.getParamName1(),entity.getParamValue1(),entity.getParamName2(),entity.getParamValue2(),
				entity.getParamName3(),entity.getParamValue3(),entity.getParamName4(),entity.getParamValue4(),entity.getParamName5(),entity.getParamValue5(),
				entity.getParamName6(),entity.getParamValue6(),entity.getParamName7(),entity.getParamValue7(),entity.getParamName8(),entity.getParamValue8(),
				entity.getParamName9(),entity.getParamValue9(),entity.getParamName10(),entity.getParamValue10(),entity.getParamName11(),entity.getParamValue11(),
				entity.getParamName12(),entity.getParamValue12(),entity.getParamName13(),entity.getParamValue13(),entity.getParamName14(),entity.getParamValue14(),
				entity.getParamName15(),entity.getParamValue15(),entity.getQrcode(),entity.getRemark(),entity.getCreateTime(),entity.getUpdateTime());
	}
	
	@Override
	public void delete(String id) {
		C2cPaymentMethod entity = this.get(id);
		if (entity != null) {
			jdbcTemplate.update("DELETE FROM T_C2C_PAYMENT_METHOD WHERE UUID=?", entity.getId());
			this.redisHandler.remove(RedisKeys.C2C_PAYMENT_METHOD_ID + entity.getId().toString());

			Map<String, C2cPaymentMethod> map = (Map<String, C2cPaymentMethod>) this.redisHandler.get(RedisKeys.C2C_PAYMENT_METHOD_PARTY_ID + entity.getPartyId().toString());
			if (map != null && !map.isEmpty()) {
				map.remove(id);
			} else {
				map = new ConcurrentHashMap<String, C2cPaymentMethod>();
			}
			this.redisHandler.setSync(RedisKeys.C2C_PAYMENT_METHOD_PARTY_ID + entity.getPartyId().toString(), map);

			Map<String, String> map1 = (Map<String, String>) this.redisHandler.get(RedisKeys.C2C_PAYMENT_METHOD_ID_TYPE);
			if (map1 != null && !map1.isEmpty()) {
				map1.remove(id);
			} else {
				map1 = new ConcurrentHashMap<String, String>();
			}
			this.redisHandler.setSync(RedisKeys.C2C_PAYMENT_METHOD_ID_TYPE, map1);
		}
	}

	@Override
	public boolean update(C2cPaymentMethod entity) {
		try {
			String updateSql = "UPDATE T_C2C_PAYMENT_METHOD SET PARTY_ID=?,METHOD_CONFIG_ID=?,METHOD_TYPE=?,METHOD_NAME=?,METHOD_IMG=?,REAL_NAME=?,"
					+ "PARAM_NAME1=?,PARAM_VALUE1=?,PARAM_NAME2=?,PARAM_VALUE2=?,PARAM_NAME3=?,PARAM_VALUE3=?,PARAM_NAME4=?,PARAM_VALUE4=?,"
					+ "PARAM_NAME5=?,PARAM_VALUE5=?,PARAM_NAME6=?,PARAM_VALUE6=?,PARAM_NAME7=?,PARAM_VALUE7=?,PARAM_NAME8=?,PARAM_VALUE8=?,"
					+ "PARAM_NAME9=?,PARAM_VALUE9=?,PARAM_NAME10=?,PARAM_VALUE10=?,PARAM_NAME11=?,PARAM_VALUE11=?,PARAM_NAME12=?,PARAM_VALUE12=?,"
					+ "PARAM_NAME13=?,PARAM_VALUE13=?,PARAM_NAME14=?,PARAM_VALUE14=?,PARAM_NAME15=?,PARAM_VALUE15=?,QRCODE=?,REMARK=?,UPDATE_TIME=? WHERE UUID=?";

			jdbcTemplate.update(updateSql,entity.getPartyId(),entity.getMethodConfigId(),entity.getMethodType(),entity.getMethodName(),
							entity.getMethodImg(),entity.getRealName(),entity.getParamName1(),entity.getParamValue1(),entity.getParamName2(),entity.getParamValue2(),
							entity.getParamName3(),entity.getParamValue3(),entity.getParamName4(),entity.getParamValue4(),entity.getParamName5(),entity.getParamValue5(),
							entity.getParamName6(),entity.getParamValue6(),entity.getParamName7(),entity.getParamValue7(),entity.getParamName8(),entity.getParamValue8(),
							entity.getParamName9(),entity.getParamValue9(),entity.getParamName10(),entity.getParamValue10(),entity.getParamName11(),entity.getParamValue11(),
							entity.getParamName12(),entity.getParamValue12(),entity.getParamName13(),entity.getParamValue13(),entity.getParamName14(),entity.getParamValue14(),
							entity.getParamName15(),entity.getParamValue15(),entity.getQrcode(),entity.getRemark(),entity.getUpdateTime(),entity.getId());
			return true;
		} catch(RuntimeException e) {
			return false;
		}
	}

	public void setRedisHandler(RedisHandler redisHandler) {
		this.redisHandler = redisHandler;
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

}
