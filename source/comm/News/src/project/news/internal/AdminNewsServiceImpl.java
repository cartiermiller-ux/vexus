package project.news.internal;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;

import kernel.bo.RecordObjectMapper;
import kernel.web.Page;
import kernel.web.PagedQueryDao;
import project.news.AdminNewsService;
import project.news.News;
import project.news.NewsService;

public class AdminNewsServiceImpl implements AdminNewsService {
	private JdbcTemplate jdbcTemplate;
	private PagedQueryDao pagedQueryDao;
	private NewsService newsService;

	public void save(News entity) { 
		if(entity.getIndex()) {
			News news  = findByIndexAndLanguage(entity.getIndex(),entity.getLanguage());
			if(news != null) {
				news.setIndex(false);
				newsService.update(news);
			}
		}
		
		newsService.save(entity);		
	}

	public Page pagedQuery(int pageNo, int pageSize, String name_para, String title, String language) {			
		
		StringBuffer queryString = new StringBuffer();
		
		queryString.append("SELECT");
		queryString.append(" party.USERNAME username, party.ROLENAME rolename, party.USERCODE usercode, party_parent.USERNAME username_parent, ");
		queryString.append(" news.UUID id, news.TITLE title, news.CONTENT content, news.CREATE_TIME create_time, news.START_TIME start_time, news.END_TIME end_time, "
				+ " news.LANGUAGE language, news.INDEX_TOP index_top, "
				+ " news.IMG_URL img_url, news.IMG_JUMP_URL img_jump_url, news.CLICK click, news.POP_UP pop_up ");
		
		queryString.append(" FROM ");
		queryString.append(" T_NEWS news "
				+ " LEFT JOIN PAT_PARTY party ON news.PARTY_ID = party.UUID "
				+ " LEFT JOIN PAT_USER_RECOM user ON user.PARTY_ID = party.UUID "
				+ " LEFT JOIN PAT_PARTY party_parent ON user.RECO_ID = party_parent.UUID ");
		queryString.append(" WHERE 1=1 ");

		Map<String, Object> parameters = new HashMap();
		
		if (StringUtils.isNotEmpty(name_para)) {
			queryString.append(" AND (party.USERNAME like :name_para or party.USERCODE =:usercode)  ");
			parameters.put("name_para", "%" + name_para + "%");
			parameters.put("usercode", name_para);
		}
		if (StringUtils.isNotEmpty(name_para)) {
			queryString.append(" AND (party.USERNAME like:username OR party.USERCODE like:username ) ");
			parameters.put("username", "%" + name_para + "%");
		}
		
		if (StringUtils.isNotEmpty(language)) {
			queryString.append(" AND news.LANGUAGE =:language ");
			parameters.put("language", language);
		}
		
		if (StringUtils.isNotEmpty(title)) {
			queryString.append(" AND news.TITLE like:title ");
			parameters.put("title",  "%" + title + "%");
		}

		queryString.append(" order by news.CREATE_TIME desc ");
		
		Page page = this.pagedQueryDao.pagedQuerySQL(pageNo, pageSize, queryString.toString(), parameters);
		return page;
	}

	public News findById(Serializable id) {
		List<News> list = jdbcTemplate.query("SELECT * FROM T_NEWS WHERE UUID=?", RecordObjectMapper.newInstance(News.class), id);
		if (null != list && list.size() > 0) {
			return list.get(0);
        }
		return null;
	}

	public void delete(String id) {
		newsService.delete(id);
	}

	public void update(News entity) {
		if(entity.getIndex()) {
			News news  = findByIndexAndLanguage(entity.getIndex(),entity.getLanguage());
			if(news != null && !entity.getId().equals(news.getId())) {
				news.setIndex(false);
				newsService.update(news);
			}
		}
		newsService.update(entity);
	}

	@Override
	public News findByIndexAndLanguage(boolean index, String language) {
		List<News> list = jdbcTemplate.query("SELECT * FROM T_NEWS WHERE INDEX_TOP=? AND LANGUAGE=?", 
				RecordObjectMapper.newInstance(News.class), index ? "Y":"N", language);
		if (null != list && list.size() > 0) {
			return list.get(0);
        }
		return null;
	}
	
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public void setPagedQueryDao(PagedQueryDao pagedQueryDao) {
		this.pagedQueryDao = pagedQueryDao;
	}

	public void setNewsService(NewsService newsService) {
		this.newsService = newsService;
	}

}
