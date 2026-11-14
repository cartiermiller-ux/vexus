package project.cms.internal;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import kernel.exception.BusinessException;
import project.cms.Cms;
import project.cms.CmsRedisKeys;
import project.cms.CmsService;
import project.cms.PropertiesUtilCms;
import project.redis.RedisHandler;

public class CmsServiceImpl implements CmsService {
	
	private RedisHandler redisHandler;

	public Cms cacheByCodeAndLanguage(String contentCode, String language) {
		Cms cms = (Cms) redisHandler.get(CmsRedisKeys.CMS_LANGUAGE_CONTENTCODE + language+contentCode);
		return cms;
	} 

	public List<Cms> cacheListByModelAndLanguage(String model, String language) {
		language = StringUtils.isEmpty(language) ? PropertiesUtilCms.getProperty("system_cms_language") : language;
		
		Map<String, Cms> cacheLanguage = (Map<String, Cms>) redisHandler.get(CmsRedisKeys.CMS_LANGUAGE + language);
		if (null == cacheLanguage || cacheLanguage.isEmpty()) {
			throw new BusinessException("无该语种的配置");
		}
		
		List<Cms> result = new LinkedList<>();
		for (Cms cms : cacheLanguage.values()) {
			// 满足model相等，且语言相等的内容
			if (((StringUtils.isEmpty(model) && StringUtils.isEmpty(cms.getModel())) || model.equals(cms.getModel()))
					) {
				result.add(cms);
			}
		}
		result.sort(new Cms());
		// 按时间降序
//		Collections.sort(result, new Comparator<Cms>() {
//			@Override
//			public int compare(Cms paramT1, Cms paramT2) {
//				// TODO Auto-generated method stub
//				return -paramT1.getCreateTime().compareTo(paramT2.getCreateTime());
//			}
//
//		});
		return result;
	}

	public void setRedisHandler(RedisHandler redisHandler) {
		this.redisHandler = redisHandler;
	}

}
