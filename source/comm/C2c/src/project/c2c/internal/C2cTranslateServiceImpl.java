package project.c2c.internal;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;

import kernel.util.StringUtils;
import kernel.web.ApplicationUtil;
import project.Constants;
import project.RedisKeys;
import project.c2c.C2cAdvertService;
import project.c2c.C2cOrder;
import project.c2c.C2cPaymentMethod;
import project.c2c.C2cPaymentMethodConfig;
import project.c2c.C2cTranslate;
import project.c2c.C2cTranslateService;
import project.redis.RedisHandler;

public class C2cTranslateServiceImpl implements C2cTranslateService {

	private JdbcTemplate jdbcTemplate;
	private RedisHandler redisHandler;
	private C2cAdvertService c2cAdvertService;
	
	public C2cTranslate get(String content, String language) {
//		return (C2cTranslate) this.redisHandler.get(RedisKeys.C2C_TRANSLATE_ID + id);
		return (C2cTranslate) this.redisHandler.get(RedisKeys.C2C_TRANSLATE_CONTENT_LANGUAGE + StringUtils.stringToUnicode(content) + language);
	}
	
	public void save(C2cTranslate entity) {
		String insertSql = "INSERT INTO T_C2C_TRANSLATE(UUID,LANGUAGE,CONTENT,TRANSLATE,CREATE_TIME,UPDATE_TIME) VALUES (?,?,?,?,?,?)";
		jdbcTemplate.update(insertSql,entity.getId(),entity.getLanguage(),entity.getContent(),entity.getTranslate(),entity.getCreateTime(),entity.getUpdateTime());
//		this.redisHandler.setSync(RedisKeys.C2C_TRANSLATE_ID + entity.getId().toString(), entity);
		this.redisHandler.setSync(RedisKeys.C2C_TRANSLATE_CONTENT_LANGUAGE + StringUtils.stringToUnicode(entity.getContent()) + entity.getLanguage(), entity);
	}

	public void update(C2cTranslate entity) {
		String updateSql = "UPDATE T_C2C_TRANSLATE SET LANGUAGE=?, CONTENT=?, TRANSLATE=?, UPDATE_TIME=? WHERE UUID=?";
		jdbcTemplate.update(updateSql, entity.getLanguage(), entity.getContent(), entity.getTranslate(), entity.getUpdateTime(), entity.getId());
		if (null != entity) {
//			this.redisHandler.setSync(RedisKeys.C2C_TRANSLATE_ID + entity.getId().toString(), entity);
			this.redisHandler.setSync(RedisKeys.C2C_TRANSLATE_CONTENT_LANGUAGE + StringUtils.stringToUnicode(entity.getContent()) + entity.getLanguage(), entity);
		}
	}

	public void delete(String content, String language) {
		C2cTranslate entity = this.get(content, language);
		if (entity != null) {
			jdbcTemplate.update("DELETE FROM T_C2C_TRANSLATE WHERE UUID=?", entity.getId());
//			this.redisHandler.remove(RedisKeys.C2C_TRANSLATE_ID + entity.getId().toString());
			this.redisHandler.remove(RedisKeys.C2C_TRANSLATE_CONTENT_LANGUAGE + StringUtils.stringToUnicode(entity.getContent()) + entity.getLanguage());
		}
	}
	
	public void saveTranslate(String content, String langTransStr) {
		
		if (StringUtils.isNotEmpty(langTransStr)) {
			
			String[] ltStrArr = langTransStr.split("&&");			
			for (int i = 0; i < ltStrArr.length; i++) {
				
				String ltStr = ltStrArr[i];				
				if (StringUtils.isNotEmpty(ltStr)) {
					
					String[] lt = ltStr.split("##");
					String language = lt[0];
					String translate = lt[1];
					
					if (StringUtils.isNotEmpty(language) && StringUtils.isNotEmpty(translate)) {
												
						C2cTranslate trans = this.get(content, language);
						if (null == trans) {

							C2cTranslate ct = new C2cTranslate();
							ct.setId(ApplicationUtil.getCurrentTimeUUID());
							ct.setLanguage(language);
							ct.setContent(content);
							ct.setTranslate(translate);
							ct.setCreateTime(new Date());
							ct.setUpdateTime(new Date());
							this.save(ct);
						} else {
							trans.setTranslate(translate);
							trans.setUpdateTime(new Date());
							this.update(trans);
						}
					}
				}
			}
		}
	}
	
	public String getTranslate(String content) {
		
		String langTransStr = "";
		
		Map<String, String> langMap = Constants.LANGUAGE;
		for (String lang : langMap.keySet()) {
			C2cTranslate trans = this.get(content, lang);
			if (StringUtils.isEmptyString(langTransStr)) {
				if (null != trans) {
					langTransStr = lang + "##" + trans.getTranslate();
				}
			} else {
				if (null != trans) {
					langTransStr += "&&" + lang + "##" + trans.getTranslate();
				}
			}
		}
		
		return langTransStr;
	}
	
	public C2cPaymentMethodConfig translatePmc(C2cPaymentMethodConfig config, String language) {
		
		Map<String, String> data = this.c2cAdvertService.getC2cSyspara("c2c_payment_method_type");		
		String name = data.get(String.valueOf(config.getMethodType()));
		if (null != name) {
			C2cTranslate trans = this.get(name, language);
			if (null != trans) {
				config.setMethodTypeName(trans.getTranslate());
			}
		}
		
		if (StringUtils.isNotEmpty(config.getMethodName())) {
			C2cTranslate trans = this.get(config.getMethodName(), language);
			if (null != trans) {
				config.setMethodName(trans.getTranslate());
			}
		}
		
		if (StringUtils.isNotEmpty(config.getParamName1())) {
			C2cTranslate trans = this.get(config.getParamName1(), language);
			if (null != trans) {
				config.setParamName1(trans.getTranslate());
			}
		}
		
		if (StringUtils.isNotEmpty(config.getParamName2())) {
			C2cTranslate trans = this.get(config.getParamName2(), language);
			if (null != trans) {
				config.setParamName2(trans.getTranslate());
			}
		}
		
		if (StringUtils.isNotEmpty(config.getParamName3())) {
			C2cTranslate trans = this.get(config.getParamName3(), language);
			if (null != trans) {
				config.setParamName3(trans.getTranslate());
			}
		}
		
		if (StringUtils.isNotEmpty(config.getParamName4())) {
			C2cTranslate trans = this.get(config.getParamName4(), language);
			if (null != trans) {
				config.setParamName4(trans.getTranslate());
			}
		}
		
		if (StringUtils.isNotEmpty(config.getParamName5())) {
			C2cTranslate trans = this.get(config.getParamName5(), language);
			if (null != trans) {
				config.setParamName5(trans.getTranslate());
			}
		}
		
		if (StringUtils.isNotEmpty(config.getParamName6())) {
			C2cTranslate trans = this.get(config.getParamName6(), language);
			if (null != trans) {
				config.setParamName6(trans.getTranslate());
			}
		}
		
		if (StringUtils.isNotEmpty(config.getParamName7())) {
			C2cTranslate trans = this.get(config.getParamName7(), language);
			if (null != trans) {
				config.setParamName7(trans.getTranslate());
			}
		}
		
		if (StringUtils.isNotEmpty(config.getParamName8())) {
			C2cTranslate trans = this.get(config.getParamName8(), language);
			if (null != trans) {
				config.setParamName8(trans.getTranslate());
			}
		}
		
		if (StringUtils.isNotEmpty(config.getParamName9())) {
			C2cTranslate trans = this.get(config.getParamName9(), language);
			if (null != trans) {
				config.setParamName9(trans.getTranslate());
			}
		}
		
		if (StringUtils.isNotEmpty(config.getParamName10())) {
			C2cTranslate trans = this.get(config.getParamName10(), language);
			if (null != trans) {
				config.setParamName10(trans.getTranslate());
			}
		}
		
		if (StringUtils.isNotEmpty(config.getParamName11())) {
			C2cTranslate trans = this.get(config.getParamName11(), language);
			if (null != trans) {
				config.setParamName11(trans.getTranslate());
			}
		}
		
		if (StringUtils.isNotEmpty(config.getParamName12())) {
			C2cTranslate trans = this.get(config.getParamName12(), language);
			if (null != trans) {
				config.setParamName12(trans.getTranslate());
			}
		}
		
		if (StringUtils.isNotEmpty(config.getParamName13())) {
			C2cTranslate trans = this.get(config.getParamName13(), language);
			if (null != trans) {
				config.setParamName13(trans.getTranslate());
			}
		}
		
		if (StringUtils.isNotEmpty(config.getParamName14())) {
			C2cTranslate trans = this.get(config.getParamName14(), language);
			if (null != trans) {
				config.setParamName14(trans.getTranslate());
			}
		}
		
		if (StringUtils.isNotEmpty(config.getParamName15())) {
			C2cTranslate trans = this.get(config.getParamName15(), language);
			if (null != trans) {
				config.setParamName15(trans.getTranslate());
			}
		}

		return config;
	}
	
	public C2cPaymentMethod translatePm(C2cPaymentMethod cpm, String language) {
		
		Map<String, String> data = this.c2cAdvertService.getC2cSyspara("c2c_payment_method_type");		
		String name = data.get(String.valueOf(cpm.getMethodType()));
		if (null != name) {
			C2cTranslate trans = this.get(name, language);
			if (null != trans) {
				cpm.setMethodTypeName(trans.getTranslate());
			}
		}
		
		if (StringUtils.isNotEmpty(cpm.getMethodName())) {
			C2cTranslate trans = this.get(cpm.getMethodName(), language);
			if (null != trans) {
				cpm.setMethodName(trans.getTranslate());
			}
		}
		
		if (StringUtils.isNotEmpty(cpm.getParamName1())) {
			C2cTranslate trans = this.get(cpm.getParamName1(), language);
			if (null != trans) {
				cpm.setParamName1(trans.getTranslate());
			}
		}
		
		if (StringUtils.isNotEmpty(cpm.getParamName2())) {
			C2cTranslate trans = this.get(cpm.getParamName2(), language);
			if (null != trans) {
				cpm.setParamName2(trans.getTranslate());
			}
		}
		
		if (StringUtils.isNotEmpty(cpm.getParamName3())) {
			C2cTranslate trans = this.get(cpm.getParamName3(), language);
			if (null != trans) {
				cpm.setParamName3(trans.getTranslate());
			}
		}
		
		if (StringUtils.isNotEmpty(cpm.getParamName4())) {
			C2cTranslate trans = this.get(cpm.getParamName4(), language);
			if (null != trans) {
				cpm.setParamName4(trans.getTranslate());
			}
		}
		
		if (StringUtils.isNotEmpty(cpm.getParamName5())) {
			C2cTranslate trans = this.get(cpm.getParamName5(), language);
			if (null != trans) {
				cpm.setParamName5(trans.getTranslate());
			}
		}
		
		if (StringUtils.isNotEmpty(cpm.getParamName6())) {
			C2cTranslate trans = this.get(cpm.getParamName6(), language);
			if (null != trans) {
				cpm.setParamName6(trans.getTranslate());
			}
		}
		
		if (StringUtils.isNotEmpty(cpm.getParamName7())) {
			C2cTranslate trans = this.get(cpm.getParamName7(), language);
			if (null != trans) {
				cpm.setParamName7(trans.getTranslate());
			}
		}
		
		if (StringUtils.isNotEmpty(cpm.getParamName8())) {
			C2cTranslate trans = this.get(cpm.getParamName8(), language);
			if (null != trans) {
				cpm.setParamName8(trans.getTranslate());
			}
		}
		
		if (StringUtils.isNotEmpty(cpm.getParamName9())) {
			C2cTranslate trans = this.get(cpm.getParamName9(), language);
			if (null != trans) {
				cpm.setParamName9(trans.getTranslate());
			}
		}
		
		if (StringUtils.isNotEmpty(cpm.getParamName10())) {
			C2cTranslate trans = this.get(cpm.getParamName10(), language);
			if (null != trans) {
				cpm.setParamName10(trans.getTranslate());
			}
		}
		
		if (StringUtils.isNotEmpty(cpm.getParamName11())) {
			C2cTranslate trans = this.get(cpm.getParamName11(), language);
			if (null != trans) {
				cpm.setParamName11(trans.getTranslate());
			}
		}
		
		if (StringUtils.isNotEmpty(cpm.getParamName12())) {
			C2cTranslate trans = this.get(cpm.getParamName12(), language);
			if (null != trans) {
				cpm.setParamName12(trans.getTranslate());
			}
		}
		
		if (StringUtils.isNotEmpty(cpm.getParamName13())) {
			C2cTranslate trans = this.get(cpm.getParamName13(), language);
			if (null != trans) {
				cpm.setParamName13(trans.getTranslate());
			}
		}
		
		if (StringUtils.isNotEmpty(cpm.getParamName14())) {
			C2cTranslate trans = this.get(cpm.getParamName14(), language);
			if (null != trans) {
				cpm.setParamName14(trans.getTranslate());
			}
		}
		
		if (StringUtils.isNotEmpty(cpm.getParamName15())) {
			C2cTranslate trans = this.get(cpm.getParamName15(), language);
			if (null != trans) {
				cpm.setParamName15(trans.getTranslate());
			}
		}

		return cpm;
	}
	
	public C2cOrder translateOrder(C2cOrder order, String language) {
		
		Map<String, String> data = this.c2cAdvertService.getC2cSyspara("c2c_payment_method_type");		
		String name = data.get(String.valueOf(order.getMethodType()));
		if (null != name) {
			C2cTranslate trans = this.get(name, language);
			if (null != trans) {
				order.setMethodTypeName(trans.getTranslate());
			}
		}
		
		if (StringUtils.isNotEmpty(order.getMethodName())) {
			C2cTranslate trans = this.get(order.getMethodName(), language);
			if (null != trans) {
				order.setMethodName(trans.getTranslate());
			}
		}
		
		if (StringUtils.isNotEmpty(order.getParamName1())) {
			C2cTranslate trans = this.get(order.getParamName1(), language);
			if (null != trans) {
				order.setParamName1(trans.getTranslate());
			}
		}
		
		if (StringUtils.isNotEmpty(order.getParamName2())) {
			C2cTranslate trans = this.get(order.getParamName2(), language);
			if (null != trans) {
				order.setParamName2(trans.getTranslate());
			}
		}
		
		if (StringUtils.isNotEmpty(order.getParamName3())) {
			C2cTranslate trans = this.get(order.getParamName3(), language);
			if (null != trans) {
				order.setParamName3(trans.getTranslate());
			}
		}
		
		if (StringUtils.isNotEmpty(order.getParamName4())) {
			C2cTranslate trans = this.get(order.getParamName4(), language);
			if (null != trans) {
				order.setParamName4(trans.getTranslate());
			}
		}
		
		if (StringUtils.isNotEmpty(order.getParamName5())) {
			C2cTranslate trans = this.get(order.getParamName5(), language);
			if (null != trans) {
				order.setParamName5(trans.getTranslate());
			}
		}
		
		if (StringUtils.isNotEmpty(order.getParamName6())) {
			C2cTranslate trans = this.get(order.getParamName6(), language);
			if (null != trans) {
				order.setParamName6(trans.getTranslate());
			}
		}
		
		if (StringUtils.isNotEmpty(order.getParamName7())) {
			C2cTranslate trans = this.get(order.getParamName7(), language);
			if (null != trans) {
				order.setParamName7(trans.getTranslate());
			}
		}
		
		if (StringUtils.isNotEmpty(order.getParamName8())) {
			C2cTranslate trans = this.get(order.getParamName8(), language);
			if (null != trans) {
				order.setParamName8(trans.getTranslate());
			}
		}
		
		if (StringUtils.isNotEmpty(order.getParamName9())) {
			C2cTranslate trans = this.get(order.getParamName9(), language);
			if (null != trans) {
				order.setParamName9(trans.getTranslate());
			}
		}
		
		if (StringUtils.isNotEmpty(order.getParamName10())) {
			C2cTranslate trans = this.get(order.getParamName10(), language);
			if (null != trans) {
				order.setParamName10(trans.getTranslate());
			}
		}
		
		if (StringUtils.isNotEmpty(order.getParamName11())) {
			C2cTranslate trans = this.get(order.getParamName11(), language);
			if (null != trans) {
				order.setParamName11(trans.getTranslate());
			}
		}
		
		if (StringUtils.isNotEmpty(order.getParamName12())) {
			C2cTranslate trans = this.get(order.getParamName12(), language);
			if (null != trans) {
				order.setParamName12(trans.getTranslate());
			}
		}
		
		if (StringUtils.isNotEmpty(order.getParamName13())) {
			C2cTranslate trans = this.get(order.getParamName13(), language);
			if (null != trans) {
				order.setParamName13(trans.getTranslate());
			}
		}
		
		if (StringUtils.isNotEmpty(order.getParamName14())) {
			C2cTranslate trans = this.get(order.getParamName14(), language);
			if (null != trans) {
				order.setParamName14(trans.getTranslate());
			}
		}
		
		if (StringUtils.isNotEmpty(order.getParamName15())) {
			C2cTranslate trans = this.get(order.getParamName15(), language);
			if (null != trans) {
				order.setParamName15(trans.getTranslate());
			}
		}

		return order;
	}
	
	/*
	 * 获取 支付方式类型名称列表
	 */
	public List<String> getAllPaymentMethodTypeName() {
		List<String> nameList = new ArrayList<String>();
		Map<String, String> data = this.c2cAdvertService.getC2cSyspara("c2c_payment_method_type");
		for (String id : data.keySet()) {
			String name = data.get(id);
			if (null != name) {
				nameList.add(name);
			}
		}
		return nameList;
	}
	
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public void setRedisHandler(RedisHandler redisHandler) {
		this.redisHandler = redisHandler;
	}
	
	public void setC2cAdvertService(C2cAdvertService c2cAdvertService) {
		this.c2cAdvertService = c2cAdvertService;
	}
	
}
