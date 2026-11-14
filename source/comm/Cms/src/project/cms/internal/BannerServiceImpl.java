package project.cms.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.jdbc.core.JdbcTemplate;

import kernel.bo.RecordObjectMapper;
import kernel.util.DateUtils;
import project.cms.Banner;
import project.cms.BannerService;

public class BannerServiceImpl implements BannerService {
	
	private JdbcTemplate jdbcTemplate;

	// base64过大，不适合做redis
	private Map<String, Banner> cache = new ConcurrentHashMap<String, Banner>();

	public void init() {
		List<Banner> list = jdbcTemplate.query("SELECT * FROM T_BANNER", RecordObjectMapper.newInstance(Banner.class));
		for (Banner banner : list) {
			cache.put(banner.getId().toString(), banner);
		}
	}

	public void save(Banner entity) {
		String insertSql = "INSERT INTO T_BANNER(UUID,LANGUAGE,CONTENT_CODE,IMAGE,URL,MODEL,ON_SHOW,SORT_INDEX,CLICK,CREATE_TIME) VALUES (?,?,?,?,?,?,?,?,?,?)";
		jdbcTemplate.update(insertSql,entity.getId(),entity.getLanguage(),entity.getContent_code(),entity.getImage(),entity.getUrl(),entity.getModel(),
				entity.getOn_show(),entity.getSort_index(),entity.getClick(),entity.getCreateTime());
		
		cache.put(entity.getId().toString(), entity);
	}

	public void update(Banner entity) {
		String updateSql = "UPDATE T_BANNER SET LANGUAGE=?,CONTENT_CODE=?,IMAGE=?,URL=?,MODEL=?,ON_SHOW=?,SORT_INDEX=?,CLICK=? WHERE UUID=?";
		jdbcTemplate.update(updateSql,entity.getLanguage(),entity.getContent_code(),entity.getImage(),entity.getUrl(),entity.getModel(),
				entity.getOn_show(),entity.getSort_index(),entity.getClick(),entity.getId());
		cache.put(entity.getId().toString(), entity);
	}

	public void delete(String id) {
		Banner entity = cacheById(id);
		if(null != entity) {
			jdbcTemplate.update("DELETE FROM T_BANNER WHERE UUID=?", entity.getId());
			cache.remove(entity.getId());
		}
	}

	public Banner cacheById(String id) {
		return cache.get(id);
	}

	@Override
	public List<Banner> cacheListByModelAndLanguage(String model, String language) {
		// TODO Auto-generated method stub
		List<Banner> list = new ArrayList<Banner>();
		for (Banner banner : cache.values()) {
			if (banner.getOn_show() == 1 && model.equals(banner.getModel()) && language.equals(banner.getLanguage())) {
				list.add(banner);
			}
		}
		Collections.sort(list);
		return list;
	}

	@Override
	public Banner cacheByCodeAndLanguage(String contentCode, String language) {
		for (Banner banner : cache.values()) {
			if (banner.getOn_show() == 1 && contentCode.equals(banner.getContent_code())
					&& language.equals(banner.getLanguage())) {
				return banner;
			}
		}
		return null;
	}

	public Map<String, Object> bindOne(Banner entity) {
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("content_code", entity.getContent_code());
		result.put("image", entity.getImage());
		result.put("index", entity.getSort_index());
		result.put("language", entity.getLanguage());
		result.put("model", entity.getModel());
		result.put("url", entity.getUrl());
		result.put("click", entity.getClick());
		result.put("create_time", DateUtils.format(entity.getCreateTime(), DateUtils.DF_yyyyMMddHHmm));
		result.put("id", entity.getId());

		return result;
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}
	
}
