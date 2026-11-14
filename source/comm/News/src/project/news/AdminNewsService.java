package project.news;

import java.io.Serializable;

import kernel.web.Page;

public interface AdminNewsService {

	public News findById(Serializable id);

	public void save(News entity);

	public void delete(String id);

	public void update(News entity);

	public Page pagedQuery(int pageNo, int pageSize, String name_para, String title, String language);
	
	public News findByIndexAndLanguage(boolean index,String language);

}
