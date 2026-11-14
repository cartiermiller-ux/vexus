package project.c2c.internal;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;

import kernel.web.Page;
import kernel.web.PagedQueryDao;
import project.RedisKeys;
import project.c2c.AdminC2cPaymentMethodService;
import project.c2c.C2cPaymentMethod;
import project.c2c.C2cPaymentMethodService;
import project.redis.RedisHandler;

public class AdminC2cPaymentMethodServiceImpl implements AdminC2cPaymentMethodService {
	
	private PagedQueryDao pagedDao;
	private C2cPaymentMethodService c2cPaymentMethodService;
	private RedisHandler redisHandler;

	public void save(C2cPaymentMethod entity) {		
		this.c2cPaymentMethodService.save(entity);
		C2cPaymentMethod methodSaved = c2cPaymentMethodService.getC2cPaymentMethod(entity.getId().toString());
		if (null != methodSaved) {
			this.redisHandler.setSync(RedisKeys.C2C_PAYMENT_METHOD_ID + methodSaved.getId().toString(), methodSaved);

			Map<String, C2cPaymentMethod> map = (Map<String, C2cPaymentMethod>) this.redisHandler.get(RedisKeys.C2C_PAYMENT_METHOD_PARTY_ID + methodSaved.getPartyId().toString());
			if (null == map) {
				map = new ConcurrentHashMap<String, C2cPaymentMethod>();
			}
			map.put(methodSaved.getId().toString(), methodSaved);
			this.redisHandler.setSync(RedisKeys.C2C_PAYMENT_METHOD_PARTY_ID + methodSaved.getPartyId().toString(), map);	
			
			Map<String, String> map1 = (Map<String, String>) this.redisHandler.get(RedisKeys.C2C_PAYMENT_METHOD_ID_TYPE);
			if (null == map1) {
				map1 = new ConcurrentHashMap<String, String>();
			}
			map1.put(methodSaved.getId().toString(), String.valueOf(methodSaved.getMethodType()));
			this.redisHandler.setSync(RedisKeys.C2C_PAYMENT_METHOD_ID_TYPE, map1);
		}
	}

	public Page pagedQuery(int pageNo, int pageSize, String user_code, Integer method_type_int, String method_name, String loginPartyId) {
		
		StringBuffer queryString = new StringBuffer();
		
		queryString.append(" SELECT ");
		queryString.append(" cpm.UUID id, cpm.METHOD_TYPE method_type, cpm.METHOD_NAME method_name, cpm.METHOD_IMG method_img, cpm.REAL_NAME real_name, ");
		queryString.append(" cpm.PARAM_NAME1 param_name1, cpm.PARAM_VALUE1 param_value1, cpm.PARAM_NAME2 param_name2, cpm.PARAM_VALUE2 param_value2, ");
		queryString.append(" cpm.PARAM_NAME3 param_name3, cpm.PARAM_VALUE3 param_value3, cpm.PARAM_NAME4 param_name4, cpm.PARAM_VALUE4 param_value4, ");
		queryString.append(" cpm.PARAM_NAME5 param_name5, cpm.PARAM_VALUE5 param_value5, cpm.PARAM_NAME6 param_name6, cpm.PARAM_VALUE6 param_value6, ");
		queryString.append(" cpm.PARAM_NAME7 param_name7, cpm.PARAM_VALUE7 param_value7, cpm.PARAM_NAME8 param_name8, cpm.PARAM_VALUE8 param_value8, ");
		queryString.append(" cpm.PARAM_NAME9 param_name9, cpm.PARAM_VALUE9 param_value9, cpm.PARAM_NAME10 param_name10, cpm.PARAM_VALUE10 param_value10, ");
		queryString.append(" cpm.PARAM_NAME11 param_name11, cpm.PARAM_VALUE11 param_value11, cpm.PARAM_NAME12 param_name12, cpm.PARAM_VALUE12 param_value12, ");
		queryString.append(" cpm.PARAM_NAME13 param_name13, cpm.PARAM_VALUE13 param_value13, cpm.PARAM_NAME14 param_name14, cpm.PARAM_VALUE14 param_value14, ");
		queryString.append(" cpm.PARAM_NAME15 param_name15, cpm.PARAM_VALUE15 param_value15, ");
		queryString.append(" cpm.QRCODE qrcode, cpm.REMARK remark, cpm.CREATE_TIME create_time, cpm.UPDATE_TIME update_time, ");
		queryString.append(" party.UUID party_id, party.USERCODE user_code, party.USERNAME user_name ");

		queryString.append(" FROM T_C2C_PAYMENT_METHOD cpm ");
		queryString.append(" LEFT JOIN PAT_PARTY party ON cpm.PARTY_ID = party.UUID ");
		queryString.append(" LEFT JOIN T_C2C_USER cu ON cu.C2C_USER_PARTY_ID = party.UUID ");
		queryString.append(" WHERE 1=1 ");

		Map<String, Object> parameters = new HashMap<String, Object>();

		if (StringUtils.isNotEmpty(loginPartyId)) {
			queryString.append(" AND cu.C2C_MANAGER_PARTY_ID = :loginPartyId ");
			parameters.put("loginPartyId", loginPartyId);
		}

		if (StringUtils.isNotEmpty(user_code)) {
			queryString.append(" AND (party.USERNAME like:user_code OR party.USERCODE like:user_code ) ");
			parameters.put("user_code", "%" + user_code + "%");
		}

		if (method_type_int != null) {
			queryString.append(" AND cpm.METHOD_TYPE = :method_type_int  ");
			parameters.put("method_type_int", method_type_int);
		}
		
		if (StringUtils.isNotEmpty(method_name)) {
			queryString.append(" AND cpm.METHOD_NAME like :method_name ");
			parameters.put("method_name", "%" + method_name + "%");
		}

		queryString.append(" order by cpm.CREATE_TIME desc ");
		
		Page page = this.pagedDao.pagedQuerySQL(pageNo, pageSize, queryString.toString(), parameters);
		return page;
	}

	public C2cPaymentMethod findById(Serializable id) {
		return this.c2cPaymentMethodService.get(id.toString());
	}

	public void delete(String id) {
		this.c2cPaymentMethodService.delete(id);
	}

	public void update(C2cPaymentMethod entity) {
		boolean state = this.c2cPaymentMethodService.update(entity);
		if (state) {
			this.redisHandler.setSync(RedisKeys.C2C_PAYMENT_METHOD_ID + entity.getId().toString(), entity);
			
			Map<String, C2cPaymentMethod> map = (Map<String, C2cPaymentMethod>) this.redisHandler.get(RedisKeys.C2C_PAYMENT_METHOD_PARTY_ID + entity.getPartyId().toString());
			if (null == map) {
				map = new ConcurrentHashMap<String, C2cPaymentMethod>();
			} else {
				map.remove(entity.getId().toString());
			}
			map.put(entity.getId().toString(), entity);
			this.redisHandler.setSync(RedisKeys.C2C_PAYMENT_METHOD_PARTY_ID + entity.getPartyId().toString(), map);
			
			Map<String, String> map1 = (Map<String, String>) this.redisHandler.get(RedisKeys.C2C_PAYMENT_METHOD_ID_TYPE);
			if (null == map1) {
				map1 = new ConcurrentHashMap<String, String>();
			} else {
				map1.remove(entity.getId().toString());
			}
			map1.put(entity.getId().toString(), String.valueOf(entity.getMethodType()));
			this.redisHandler.setSync(RedisKeys.C2C_PAYMENT_METHOD_ID_TYPE, map1);
		}
	}

	public void setPagedDao(PagedQueryDao pagedDao) {
		this.pagedDao = pagedDao;
	}

	public void setC2cPaymentMethodService(C2cPaymentMethodService c2cPaymentMethodService) {
		this.c2cPaymentMethodService = c2cPaymentMethodService;
	}

	public void setRedisHandler(RedisHandler redisHandler) {
		this.redisHandler = redisHandler;
	}
	
}
