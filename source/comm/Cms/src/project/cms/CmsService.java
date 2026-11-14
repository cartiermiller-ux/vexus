package project.cms;

import java.util.List;

public interface CmsService {

	/**
	 * 获取cms根据相关模块和国际化语言
	 */
	public List<Cms> cacheListByModelAndLanguage(String model, String language);

	/**
	 * 获取cms根据相关业务code和国际化语言
	 */
	public Cms cacheByCodeAndLanguage(String contentCode, String language);

}
