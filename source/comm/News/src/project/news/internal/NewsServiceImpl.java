package project.news.internal;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;

import kernel.bo.RecordObjectMapper;
import kernel.web.Page;
import project.Constants;
import project.RedisKeys;
import project.news.News;
import project.news.NewsService;
import project.party.PartyService;
import project.party.model.Party;
import project.party.model.UserRecom;
import project.party.recom.UserRecomService;
import project.redis.RedisHandler;

public class NewsServiceImpl implements NewsService {
	private JdbcTemplate jdbcTemplate;
	private RedisHandler redisHandler;
	private PartyService partyService;
	private UserRecomService userRecomService;

	public void save(News entity) {
		entity.setCreateTime(new Date());
		
		String insertSql = "INSERT INTO T_NEWS(UUID,TITLE,CONTENT,CREATE_TIME,LANGUAGE,INDEX_TOP,PARTY_ID,IMG_URL,IMG_JUMP_URL,"
				+ "CLICK,POP_UP,START_TIME,END_TIME) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";
		jdbcTemplate.update(insertSql,entity.getId(),entity.getTitle(),entity.getContent(),entity.getCreateTime(),entity.getLanguage(),
				entity.getIndex()?"Y":"N",entity.getPartyId(),entity.getImgUrl(),entity.getImgJumpUrl(),entity.getClick(),entity.getPopUp(),entity.getStartTime(),entity.getEndTime());

		redisHandler.setSync(RedisKeys.NEWS_ID + entity.getId().toString(), entity);

		Map<String, News> cacheLanguage = (Map<String, News>) redisHandler
				.get(RedisKeys.NEWS_LANGUAGE + entity.getLanguage());
		if (cacheLanguage == null) {
			cacheLanguage = new ConcurrentHashMap<String, News>();
		}
		cacheLanguage.put(entity.getId().toString(), entity);
		redisHandler.setSync(RedisKeys.NEWS_LANGUAGE + entity.getLanguage(), cacheLanguage);

	}

	public Page cachePagedQuery(int pageNo, int pageSize, String language) {
		if (!StringUtils.isNotEmpty(language)) {
			language = "en";
		}
		Map<String, News> cacheLanguage = (Map<String, News>) redisHandler.get(RedisKeys.NEWS_LANGUAGE + language);
		List<News> list = null;
		if (cacheLanguage != null) {
			list = new ArrayList<News>(cacheLanguage.values());
		}

		if (CollectionUtils.isEmpty(list))
			return Page.EMPTY_PAGE;
		list.sort(new News());

		Page page = new Page(pageNo, pageSize, Integer.MAX_VALUE);
		// 0-29 30-59
		int start = page.getFirstElementNumber();
		if (start >= list.size()) {
			return new Page();
		}
//		int start = (pageNo - 1) * pageSize;
		int end = start + pageSize;
		if (list.size() <= end)
			end = list.size();

		page.setElements(list.subList(start, end));

//		StringBuffer queryString = new StringBuffer("");
//		queryString.append(" FROM News WHERE 1=1 ");
//		Map parameters = new HashMap();
//		if (StringUtils.isNotEmpty(language)) {
//			queryString.append("AND language=:language ");
//			parameters.put("language", language);
//		}
//
//		queryString.append(" order by createTime desc ");
//		Page page = this.pagedDao.pagedQueryHql(pageNo, pageSize, queryString.toString(), parameters);
		return page;
	}

	public Page cachePagedQuery_v2(int pageNo, int pageSize, String language, boolean popup) {
		
		if (!StringUtils.isNotEmpty(language)) {
			language = "en";
		}
		
		Map<String, News> cacheLanguage = (Map<String, News>) this.redisHandler.get(RedisKeys.NEWS_LANGUAGE + language);
		List<News> newsList = new ArrayList<News>();
		if (cacheLanguage != null) {
			newsList = new ArrayList<News>(cacheLanguage.values());
		}
		
		Date date = new Date();
		
		List<News> list = new ArrayList<News>();
		for (int i = 0; i < newsList.size(); i++) {
			News news = newsList.get(i);
			
			if ((null != news.getStartTime() && date.before(news.getStartTime())) 
					|| (null != news.getEndTime() && news.getEndTime().before(date))) {
				continue;
			}
			
			if (null != news) {
				
				if (null == news.getPartyId() || kernel.util.StringUtils.isEmptyString(news.getPartyId().toString())) {
					// 全局配置
					if (popup) {
						if (1 == news.getPopUp()) {
							list.add(news);
						}
					} else {
						if (0 == news.getPopUp()) {
							list.add(news);
						}
					}
				}
			}
		}

		if (CollectionUtils.isEmpty(list))
			return Page.EMPTY_PAGE;
		list.sort(new News());

		Page page = new Page(pageNo, pageSize, Integer.MAX_VALUE);
		// 0-29 30-59
		int start = page.getFirstElementNumber();
		if (start >= list.size()) {
			return new Page();
		}
//		int start = (pageNo - 1) * pageSize;
		int end = start + pageSize;
		if (list.size() <= end)
			end = list.size();

		page.setElements(list.subList(start, end));
		return page;
	}

	public Page cachePagedQuery_v2_token(int pageNo, int pageSize, String language, String partyId, boolean popup) {
		
		if (!StringUtils.isNotEmpty(language)) {
			language = "en";
		}
		
		List<String> parents_partyid = new ArrayList<String>();
		
		if (!kernel.util.StringUtils.isEmptyString(partyId)) {
			List<UserRecom> parents = this.userRecomService.getParents(partyId);
			for (int j = 0; j < parents.size(); j++) {			
				Party party = this.partyService.cachePartyBy(parents.get(j).getReco_id(), true);
				if (null == party) {
					continue;
				}
				if (!Constants.SECURITY_ROLE_AGENT.equals(party.getRolename())
						&& !Constants.SECURITY_ROLE_AGENTLOW.equals(party.getRolename())) {
					// 非代理
					continue;
				}			
				parents_partyid.add(party.getId().toString());
			}
		}
		
		Map<String, News> cacheLanguage = (Map<String, News>) this.redisHandler.get(RedisKeys.NEWS_LANGUAGE + language);
		List<News> newsList = new ArrayList<News>();
		if (cacheLanguage != null) {
			newsList = new ArrayList<News>(cacheLanguage.values());
		}
		
		Date date = new Date();
		
		List<News> list = new ArrayList<News>();
		for (int i = 0; i < newsList.size(); i++) {
			News news = newsList.get(i);
			
			if ((null != news.getStartTime() && date.before(news.getStartTime())) 
					|| (null != news.getEndTime() && news.getEndTime().before(date))) {
				continue;
			}
			
			if (null != news) {
				
				if (null == news.getPartyId() || kernel.util.StringUtils.isEmptyString(news.getPartyId().toString())) {
					// 全局配置
					if (popup) {
						if (1 == news.getPopUp()) {
							list.add(news);
						}
					} else {
						if (0 == news.getPopUp()) {
							list.add(news);
						}
					}
				} else {
					
					if (news.getPartyId().toString().equals(partyId)) {
						// 用户配置
						if (popup) {
							if (1 == news.getPopUp()) {
								list.add(news);
							}
						} else {
							if (0 == news.getPopUp()) {
								list.add(news);
							}
						}
					}
					
					if (parents_partyid.contains(news.getPartyId().toString())) {
						// 代理配置
						if (popup) {
							if (1 == news.getPopUp()) {
								list.add(news);
							}
						} else {
							if (0 == news.getPopUp()) {
								list.add(news);
							}
						}
					}
				}
			}
		}

		if (CollectionUtils.isEmpty(list))
			return Page.EMPTY_PAGE;
		list.sort(new News());

		Page page = new Page(pageNo, pageSize, Integer.MAX_VALUE);
		// 0-29 30-59
		int start = page.getFirstElementNumber();
		if (start >= list.size()) {
			return new Page();
		}
//		int start = (pageNo - 1) * pageSize;
		int end = start + pageSize;
		if (list.size() <= end)
			end = list.size();

		page.setElements(list.subList(start, end));
		return page;
	}

	public News findById(Serializable id) {
		List<News> list = jdbcTemplate.query("SELECT * FROM T_NEWS WHERE UUID=?", RecordObjectMapper.newInstance(News.class), id);
		if (null != list && list.size() > 0) {
			return list.get(0);
        }
		return null;
	}

	public News cacheById(Serializable id) {
		News news = (News) redisHandler.get(RedisKeys.NEWS_ID + id.toString());
		return news;
	}

	public void delete(String id) {
		News entity = findById(id);
		if (entity != null) {
			jdbcTemplate.update("DELETE FROM T_NEWS WHERE UUID=?", entity.getId());

			redisHandler.remove(RedisKeys.NEWS_ID + entity.getId().toString());
			Map<String, News> cacheLanguage = (Map<String, News>) redisHandler
					.get(RedisKeys.NEWS_LANGUAGE + entity.getLanguage());
			if (cacheLanguage != null) {
				cacheLanguage.remove(entity.getId().toString());
			}
			redisHandler.setSync(RedisKeys.NEWS_LANGUAGE + entity.getLanguage(), cacheLanguage);

		}
	}

	public void update(News entity) {
		if (null == entity) {
			return;
		}
		String updateSql = "UPDATE T_NEWS SET TITLE=?,CONTENT=?,LANGUAGE=?,INDEX_TOP=?,PARTY_ID=?,IMG_URL=?,IMG_JUMP_URL=?,CLICK=?,POP_UP=?,START_TIME=?,END_TIME=? WHERE UUID=?";
		jdbcTemplate.update(updateSql,entity.getTitle(),entity.getContent(),entity.getLanguage(),entity.getIndex(),entity.getPartyId(),
				entity.getImgUrl(),entity.getImgJumpUrl(),entity.getClick(),entity.getPopUp(),entity.getStartTime(),entity.getEndTime(),entity.getId());

		redisHandler.setSync(RedisKeys.NEWS_ID + entity.getId().toString(), entity);

		Map<String, News> cacheLanguage = (Map<String, News>) redisHandler.get(RedisKeys.NEWS_LANGUAGE + entity.getLanguage());
		if (cacheLanguage == null) {
			cacheLanguage = new ConcurrentHashMap<String, News>();
		}else {
			cacheLanguage = new ConcurrentHashMap<String, News>(cacheLanguage);
		}
		cacheLanguage.put(entity.getId().toString(), entity);
		redisHandler.setSync(RedisKeys.NEWS_LANGUAGE + entity.getLanguage(), cacheLanguage);

	}

	@Override
	public News getIndex(String language) {

		Map<String, News> cacheLanguage = (Map<String, News>) redisHandler.get(RedisKeys.NEWS_LANGUAGE + language);
		List<News> list = new ArrayList<News>();
		if (cacheLanguage != null) {
			
			Date date = new Date();
			
			for (News news : cacheLanguage.values()) {
				
				if ((null != news.getStartTime() && date.before(news.getStartTime())) 
						|| (null != news.getEndTime() && news.getEndTime().before(date))) {
					continue;
				}
				
				if (news.getIndex() && 0 == news.getPopUp()) {
					list.add(news);
				}
			}
		}

		if (CollectionUtils.isEmpty(list))
			return null;
		list.sort(new News());

		return list.get(0);

	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public void setRedisHandler(RedisHandler redisHandler) {
		this.redisHandler = redisHandler;
	}

	public void setPartyService(PartyService partyService) {
		this.partyService = partyService;
	}

	public void setUserRecomService(UserRecomService userRecomService) {
		this.userRecomService = userRecomService;
	}

}
