package project.news;

import java.io.Serializable;

import kernel.web.Page;

public interface NewsService {

	public News findById(Serializable id);
	
	public News cacheById(Serializable id);

	public News getIndex(String language);

	public void save(News entity);

	public void delete(String id);

	public void update(News entity);

	public Page cachePagedQuery(int pageNo, int pageSize, String language);

	public Page cachePagedQuery_v2(int pageNo, int pageSize, String language, boolean popup);

	public Page cachePagedQuery_v2_token(int pageNo, int pageSize, String language, String partyId, boolean popup);

}
