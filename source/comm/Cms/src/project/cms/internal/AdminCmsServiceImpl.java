package project.cms.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;

import kernel.bo.RecordObjectMapper;
import kernel.web.ApplicationUtil;
import kernel.web.Page;
import project.cms.AdminCmsService;
import project.cms.Cms;
import project.cms.CmsRedisKeys;
import project.cms.PropertiesUtilCms;
import project.redis.RedisHandler;

public class AdminCmsServiceImpl implements AdminCmsService {

	private JdbcTemplate jdbcTemplate;
	
	private RedisHandler redisHandler;
	
	/**
	 * 新增Cms记录
	 */
	public void saveCms(Cms entity) {
		String insertSql = "INSERT INTO T_CMS(UUID,TITLE,CONTENT,CREATE_TIME,MODEL,LANGUAGE,CONTENT_CODE) VALUES (?,?,?,?,?,?,?)";
		jdbcTemplate.update(insertSql,entity.getId(),entity.getTitle(),entity.getContent(),
				entity.getCreateTime(),entity.getModel(),entity.getLanguage(),entity.getContent_code());
		
		redisHandler.setSync(CmsRedisKeys.CMS_LANGUAGE_CONTENTCODE + entity.getLanguage() + entity.getContent_code(), entity);
		
		Map<String, Cms> cacheLanguage = (Map<String, Cms>) redisHandler.get(CmsRedisKeys.CMS_LANGUAGE + entity.getLanguage());
		if(cacheLanguage==null) {
			cacheLanguage =  new ConcurrentHashMap<String, Cms>();
		}
		cacheLanguage.put(entity.getContent_code(), entity);
		redisHandler.setSync(CmsRedisKeys.CMS_LANGUAGE+entity.getLanguage(),cacheLanguage);
	}
	
	public void update(Cms entity,String strOriginalLanguage,String strOriginalContentCode) {
		//清理 CMS_LANGUAGE_CONTENTCODE_?0?1（?0：language ?1：contentcode）
		String strRedisKey = CmsRedisKeys.CMS_LANGUAGE_CONTENTCODE + strOriginalLanguage + strOriginalContentCode;		
		redisHandler.remove(strRedisKey);
		
		//清理 CMS_LANGUAGE_?0（?0：language）
		Map<String, Cms> cacheLanguage = (Map<String, Cms>) redisHandler.get(CmsRedisKeys.CMS_LANGUAGE + strOriginalLanguage);
		if(null != cacheLanguage) {
			for(Map.Entry<String, Cms> it : cacheLanguage.entrySet()){
				Cms cms = it.getValue();
				if(null != cms) {
					String strContentCode = cms.getContent_code();
					if(!strContentCode.isEmpty()) {
						if(0 == strContentCode.compareTo(strOriginalContentCode)) {
							cacheLanguage.remove(strOriginalContentCode);
							redisHandler.setSync(CmsRedisKeys.CMS_LANGUAGE + strOriginalLanguage,cacheLanguage);
							break;
						}
					}
				}
			}		
		}	
		
		String updateSql = "UPDATE T_CMS SET TITLE=?, CONTENT=?, MODEL=?, LANGUAGE=?, CONTENT_CODE=? WHERE UUID=?";
		jdbcTemplate.update(updateSql,entity.getTitle(), entity.getContent(), entity.getModel(), 
				entity.getLanguage(), entity.getContent_code(), entity.getId());
		
		redisHandler.setSync(CmsRedisKeys.CMS_LANGUAGE_CONTENTCODE + entity.getLanguage() + entity.getContent_code(), entity);
		
		if(cacheLanguage==null) {
			cacheLanguage = new ConcurrentHashMap<String, Cms>();
		}
		cacheLanguage.put(entity.getContent_code(), entity);
		redisHandler.setSync(CmsRedisKeys.CMS_LANGUAGE + entity.getLanguage(), cacheLanguage);
		
	}

	public Page pagedQuery(int pageNo, int pageSize, String language, String title,String content_code_para) {
		if (pageNo <= 0) pageNo = 1;
		Page page = new Page(pageNo, pageSize, Integer.MAX_VALUE);
		
		StringBuilder whereBuilder=new StringBuilder("WHERE 1=1 ");
		ArrayList<Object> params=new ArrayList<Object>();
		
		if (StringUtils.isNotEmpty(language)) {
			whereBuilder.append("AND LANGUAGE=? ");
			params.add(language);
		}
		
		if (StringUtils.isNotEmpty(title)) {
			whereBuilder.append("AND TITLE LIKE ? ");
			params.add("%"+title+"%");
		}
		
		if (StringUtils.isNotEmpty(content_code_para)) {
			whereBuilder.append("AND CONTENT_CODE=? ");
			params.add(content_code_para);
		}
		
		whereBuilder.append("ORDER BY CREATE_TIME DESC LIMIT ?,? ");
		params.add(page.getFirstElementNumber());
		params.add(pageSize);
		
		List<Cms> list=ApplicationUtil.executeSelect(Cms.class,whereBuilder.toString(),params.toArray(new Object[params.size()]));
		page.setElements(list);
		return page;
	}

	public Cms findById(String id) {
		List<Cms> list = jdbcTemplate.query("SELECT * FROM T_CMS WHERE UUID=?", RecordObjectMapper.newInstance(Cms.class), id);
		if (null != list && list.size() > 0) {
			return list.get(0);
        }
		return null;
	}

	public Map<String, String> getModelDatasDictionary() {
		String datas = PropertiesUtilCms.getProperty("system_cms_model_list");
		String[] splits = datas.split(",");
		Map<String, String> map = new HashMap<String, String>();
		for (String data : splits) {
			map.put(data, data);
		}
		return map;
	}

	public void delete(Cms cms) {
		if (cms != null) {
			jdbcTemplate.update("DELETE FROM T_CMS WHERE UUID=?", cms.getId());
			
			redisHandler.setSync(CmsRedisKeys.CMS_LANGUAGE_CONTENTCODE + cms.getLanguage() + cms.getContent_code(), null);
			Map<String, Cms> cacheLanguage = (Map<String, Cms>) redisHandler.get(CmsRedisKeys.CMS_LANGUAGE + cms.getLanguage());
			if (cacheLanguage != null) {
				cacheLanguage.remove(cms.getContent_code());
			}
			redisHandler.setSync(CmsRedisKeys.CMS_LANGUAGE + cms.getLanguage(), cacheLanguage);
		}
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public void setRedisHandler(RedisHandler redisHandler) {
		this.redisHandler = redisHandler;
	}
}
