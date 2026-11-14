package project.web.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import kernel.exception.BusinessException;
import kernel.web.ResultObject;
import project.cms.Banner;
import project.cms.BannerService;

/**
 * 横幅管理
 */
@RestController
@CrossOrigin
public class BannerController {

	private Logger logger = LoggerFactory.getLogger(BannerController.class);

	@Autowired
	private BannerService bannerService;
    
	private final String action = "/api/banner!";
	
	/**
	 * 获取 横幅
	 * 
	 * language 语言
	 * content_code 业务代码，同种内容不同语言下的code相同
	 */
	@RequestMapping(action + "get.action")
	public Object get(HttpServletRequest request) {
		String language = request.getParameter("language");
		String content_code = request.getParameter("content_code");
		
		ResultObject resultObject = new ResultObject();
		
		try {

			Banner banner = this.bannerService.cacheByCodeAndLanguage(content_code, language);
			
			resultObject.setData(this.bannerService.bindOne(banner));

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
	 * 获取 横幅 列表
	 */
	@RequestMapping(action + "list.action")
	public Object list(HttpServletRequest request) {
		String model = request.getParameter("model");
		String language = request.getParameter("language");
		
		ResultObject resultObject = new ResultObject();
		
		try {
			
			List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
			
			List<Banner> cacheListByModel = this.bannerService.cacheListByModelAndLanguage(model, language);

			for (Banner banner : cacheListByModel) {
				result.add(this.bannerService.bindOne(banner));
			}
			
			resultObject.setData(result);
			
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
