package project.cms;

import java.util.Map;

import kernel.web.Page;

public interface AdminCmsService {
	
	/**
	 * 新增Cms记录
	 */
	public void saveCms(Cms entity);

	/*
	 * 新增接口：内容管理公告模块 修改未清理旧数据
	 */
	public void update(Cms entity,String strOriginalLanguage,String strOriginalContentCode);

	public Page pagedQuery(int pageNo, int pageSize,String language,String title,String content_code_para);

	public Cms findById(String id);

	public void delete(Cms cms);

	public Map<String, String> getModelDatasDictionary();
}
