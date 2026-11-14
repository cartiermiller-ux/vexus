package project.web.api;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import kernel.exception.BusinessException;
import kernel.util.DateUtils;
import kernel.web.ResultObject;
import project.cms.Cms;
import project.cms.CmsService;

/**
 * 用户端内容管理
 */
@RestController
@CrossOrigin
public class CmsController {

	private Logger logger = LoggerFactory.getLogger(CmsController.class);

	@Autowired
    private CmsService cmsService;
    
	private final String action = "/api/cms!";
	
	/**
	 * 获取 用户端内容管理
	 */
	@RequestMapping(action + "get.action")
	public Object get(HttpServletRequest request) {
		// 业务代码， 同种内容 不同语言下的code相同
		String content_code = request.getParameter("content_code");
		String language = request.getParameter("language");
		
		ResultObject resultObject = new ResultObject();
		
		try {			
			
			Cms cms = this.cmsService.cacheByCodeAndLanguage(content_code, language);

			resultObject.setData(cms);
			
		} catch (BusinessException e) {
			resultObject.setCode("1");
			resultObject.setMsg(e.getMessage());
		} catch (Throwable t) {
			resultObject.setCode("1");
			resultObject.setMsg("程序错误");
			logger.error("error:", t);
		}

		return resultObject;
	}
	
	/**
	 * 获取 用户端内容管理 列表
	 */
	@RequestMapping(action + "list.action")
	public Object list(HttpServletRequest request) {
		String model = request.getParameter("model");
		String language = request.getParameter("language");
		
		ResultObject resultObject = new ResultObject();
		
		try {
			
			List<Cms> cacheListByModel = this.cmsService.cacheListByModelAndLanguage(model, language);

			for (Cms cms : cacheListByModel) {
				cms.setCreateTimeStr(DateUtils.format(cms.getCreateTime(), DateUtils.DF_yyyyMMddHHmmss));
			}
			
			resultObject.setData(cacheListByModel);
			
		} catch (BusinessException e) {
			resultObject.setCode("1");
			resultObject.setMsg(e.getMessage());
		} catch (Throwable t) {
			resultObject.setCode("1");
			resultObject.setMsg("程序错误");
			logger.error("error:", t);
		}

		return resultObject;
	}
	
}
