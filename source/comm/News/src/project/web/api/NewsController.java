package project.web.api;

import java.util.ArrayList;
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
import kernel.util.StringUtils;
import kernel.web.ApplicationUtil;
import kernel.web.BaseAction;
import kernel.web.ResultObject;
import project.news.News;
import project.news.NewsService;

/**
 * 新闻
 */
@RestController
@CrossOrigin
public class NewsController extends BaseAction {

	private Logger logger = LoggerFactory.getLogger(NewsController.class);

	@Autowired
	private NewsService newsService;
    
	private final String action = "/api/news!";
	
	/**
	 * 获取 新闻
	 */
	@RequestMapping(action + "get.action")
	public Object get(HttpServletRequest request) {
		String id = request.getParameter("id");
		String index = request.getParameter("index");
		String language = request.getParameter("language");
		
		ResultObject resultObject = new ResultObject();
		
		try {
			
			if (StringUtils.isEmptyString(index)) {
				index = "false";
			}
			
			boolean index_bool = Boolean.valueOf(index).booleanValue();
			
			News news = new News();
			
			if (index_bool) {
				news = this.newsService.getIndex(language);
			} else {
				news = this.newsService.cacheById(id);
			}
			
			if (news != null) {
				news.setCreateTimeStr(DateUtils.format(news.getCreateTime(), DateUtils.DF_yyyyMMddHHmmss));
			}

			resultObject.setData(news);

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
	 * 获取 新闻 列表
	 */
	@RequestMapping(action + "list.action")
	public Object list(HttpServletRequest request) {
		String page_no = request.getParameter("page_no");
		String language = request.getParameter("language");
		
		ResultObject resultObject = new ResultObject();
		
		try {

			if (StringUtils.isNullOrEmpty(page_no)) {
				page_no = "1";
			}
			if (!StringUtils.isInteger(page_no)) {
				throw new BusinessException("页码不是整数");
			}
			if (Integer.valueOf(page_no).intValue() <= 0) {
				throw new BusinessException("页码不能小于等于0");
			}

			int page_no_int = Integer.valueOf(page_no).intValue();
			
//			List<News> list = this.newsService.cachePagedQuery(page_no_int, 20, language).getElements();
			List<News> list = this.newsService.cachePagedQuery(1, 1000000, language).getElements();
			if (list != null) {
				for (int i = 0; i < list.size(); i++) {
					list.get(i).setCreateTimeStr(DateUtils.format(list.get(i).getCreateTime(), DateUtils.DF_yyyyMMddHHmmss));
				}
			}
			
			resultObject.setData(list);			
			
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
	 * 获取 新闻 列表 版本2
	 */
	@RequestMapping(action + "list_v2.action")
	public Object list_v2(HttpServletRequest request) {
		String page_no = request.getParameter("page_no");
		String language = request.getParameter("language");
		String token = request.getParameter("token");
		
		ResultObject resultObject = new ResultObject();
		
		try {

			if (StringUtils.isNullOrEmpty(page_no)) {
				page_no = "1";
			}
			if (!StringUtils.isInteger(page_no)) {
				throw new BusinessException("页码不是整数");
			}
			if (Integer.valueOf(page_no).intValue() <= 0) {
				throw new BusinessException("页码不能小于等于0");
			}

			int page_no_int = Integer.valueOf(page_no).intValue();
			
			List<News> list = new ArrayList<News>();
			
			if (StringUtils.isEmptyString(token)) {
				list = this.newsService.cachePagedQuery_v2(1, 1000000, language, false).getElements();
			} else {
				String partyId = this.getLoginPartyId();
				list = this.newsService.cachePagedQuery_v2_token(1, 1000000, language, partyId, false).getElements();				
			}
			
			if (list != null) {
				for (int i = 0; i < list.size(); i++) {
					list.get(i).setCreateTimeStr(DateUtils.format(list.get(i).getCreateTime(), DateUtils.DF_yyyyMMddHHmmss));
				}
			}
			
			resultObject.setData(list);			
			
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
	 * 获取 弹出新闻 列表 版本2
	 */
	@RequestMapping(action + "list_v2_popup.action")
	public Object list_v2_popup(HttpServletRequest request) {
		String page_no = request.getParameter("page_no");
		String language = request.getParameter("language");
		String token = request.getParameter("token");
		
		ResultObject resultObject = new ResultObject();
		
		try {

			if (StringUtils.isNullOrEmpty(page_no)) {
				page_no = "1";
			}
			if (!StringUtils.isInteger(page_no)) {
				throw new BusinessException("页码不是整数");
			}
			if (Integer.valueOf(page_no).intValue() <= 0) {
				throw new BusinessException("页码不能小于等于0");
			}
			
			int page_no_int = Integer.valueOf(page_no).intValue();
			
			List<News> list = new ArrayList<News>();
			
			if (StringUtils.isEmptyString(token)) {
				list = this.newsService.cachePagedQuery_v2(1, 1000000, language, true).getElements();
			} else {
				String partyId = this.getLoginPartyId();
				list = this.newsService.cachePagedQuery_v2_token(1, 1000000, language, partyId, true).getElements();				
			}
			
			if (list != null) {
				for (int i = 0; i < list.size(); i++) {
					list.get(i).setCreateTimeStr(DateUtils.format(list.get(i).getCreateTime(), DateUtils.DF_yyyyMMddHHmmss));
				}
			}
			
			resultObject.setData(list);			
			
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
