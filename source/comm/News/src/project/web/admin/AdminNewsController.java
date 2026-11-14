package project.web.admin;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.providers.encoding.PasswordEncoder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import kernel.exception.BusinessException;
import kernel.util.StringUtils;
import kernel.web.ApplicationUtil;
import kernel.web.Page;
import kernel.web.PageActionSupport;
import project.Constants;
import project.log.LogService;
import project.news.AdminNewsService;
import project.news.News;
import project.party.PartyService;
import project.party.model.Party;
import security.SecUser;
import security.internal.SecUserService;

/**
 * 新闻管理
 */
@RestController
public class AdminNewsController extends PageActionSupport {

	private Logger logger = LoggerFactory.getLogger(AdminNewsController.class);

	@Autowired
	private AdminNewsService adminNewsService;
	@Autowired
	private SecUserService secUserService;
	@Autowired
	private LogService logService;
	@Autowired
	private PasswordEncoder passwordEncoder;
	@Autowired
	protected PartyService partyService;

	private final String action = "normal/adminNewsAction!";
	
	/**
	 * 获取 新闻管理 列表
	 */
	@RequestMapping(action + "list.action")
	public ModelAndView list(HttpServletRequest request) {
		String pageNoStr = request.getParameter("pageNo");
		String message = request.getParameter("message");
		String error = request.getParameter("error");
		String name_para = request.getParameter("name_para");
		String title_para = request.getParameter("title_para");
		String language_para = request.getParameter("language_para");

		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("news_list");

		int pageNo=1;
		Page page=null;
		int pageSize=20;
		try {
			pageNo=checkAndSetPageNo(pageNoStr);
			page=adminNewsService.pagedQuery(pageNo, pageSize, name_para, title_para, language_para);
			
			List<Map<String, Object>> list = (List<Map<String, Object>>) page.getElements();
			for (int i = 0; i < list.size(); i++) {
				Map<String, Object> map = list.get(i);
				if (null == map.get("rolename")) {
					map.put("roleNameDesc", "");
				} else {
					String roleName = map.get("rolename").toString();
					map.put("roleNameDesc", Constants.ROLE_MAP.containsKey(roleName) ? Constants.ROLE_MAP.get(roleName) : roleName);
				}
			}

		} catch (BusinessException e) {
			modelAndView.addObject("error", e.getMessage());
			return modelAndView;
		} catch (Throwable t) {
			logger.error(" error ", t);
			modelAndView.addObject("error", "[ERROR] " + t.getMessage());
			return modelAndView;
		}

		modelAndView.addObject("pageNo", pageNo);
		modelAndView.addObject("pageSize", pageSize);
		modelAndView.addObject("page", page);
		modelAndView.addObject("message", message);
		modelAndView.addObject("error", error);
		modelAndView.addObject("name_para", name_para);
		modelAndView.addObject("title_para", title_para);
		modelAndView.addObject("language_para", language_para);
		modelAndView.addObject("languageMap", Constants.LANGUAGE);
		return modelAndView;
	}
	
	/**
	 * 新增 新闻管理 页面
	 */
	@RequestMapping(action + "toAdd.action")
	public ModelAndView toAdd(HttpServletRequest request) {
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.addObject("languageMap", Constants.LANGUAGE);
		modelAndView.setViewName("news_add");
		return modelAndView;
	}

	/**
	 * 新增 新闻管理
	 * 
	 * usercode PartyId
	 * img_url 图片地址
	 * img_jump_url 图片跳转链接
	 * click 可否点击
	 * pop_up 是否弹出
	 * title 标题
	 * content 内容
	 * index 是否置顶
	 * language 语言
	 */
	@RequestMapping(action + "add.action")
	public ModelAndView add(HttpServletRequest request) {
		String usercode = request.getParameter("usercode");
		String img_url = request.getParameter("img_url");
		String img_jump_url = request.getParameter("img_jump_url");
		String click = request.getParameter("click");
		String pop_up = request.getParameter("pop_up");
		String title = request.getParameter("title");
		String content = request.getParameter("content");	
		String language = request.getParameter("language");	
		String index = request.getParameter("index");
		String start_time = request.getParameter("start_time");
		String end_time = request.getParameter("end_time");
		String login_safeword = request.getParameter("login_safeword");
		
		ModelAndView modelAndView = new ModelAndView();
		
		try {
			
			String error = this.verif(click, pop_up, title, content);
			if (!StringUtils.isNullOrEmpty(error)) {
				throw new BusinessException(error);
			}

			int click_int = Integer.valueOf(click).intValue();
			int pop_up_int = Integer.valueOf(pop_up).intValue();
			boolean index_bool = Boolean.valueOf(index).booleanValue();
			
			String userNameLogin = this.getUsername_login();
			
			SecUser sec = this.secUserService.findUserByLoginName(userNameLogin);
			this.checkLoginSafeword(sec, userNameLogin, login_safeword);
			
			News news = new News();
			
			if (!StringUtils.isEmptyString(usercode)) {
				Party party = this.partyService.findPartyByUsercode(usercode);
				if (null == party) {
					throw new BusinessException("UID不存在");
				}
				news.setPartyId(party.getId());
			} else {
				news.setPartyId("");
			}
			
			Date start_date = null;
			Date end_date = null;			
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			if (!StringUtils.isEmptyString(start_time)) {
				start_date = sdf.parse(start_time);
			}
			if (!StringUtils.isEmptyString(end_time)) {
				end_date = sdf.parse(end_time);
			}
			news.setId(ApplicationUtil.getCurrentTimeUUID());
			news.setImgUrl(img_url);
			news.setImgJumpUrl(img_jump_url);
			news.setClick(click_int);
			news.setPopUp(pop_up_int);
			news.setTitle(title);
			news.setContent(content);
			news.setLanguage(language);
			news.setIndex(index_bool);
			news.setStartTime(start_date);
			news.setEndTime(end_date);
			this.adminNewsService.save(news);
			
			String log = MessageFormat.format("ip:" + this.getIp() 
				+ ",管理员新增新闻,id:{0},标题:{1},语言:{2},推荐:{3},内容:{4},PartyId:{5},图片地址:{6},图片跳转链接:{7},可否点击:{8},是否弹出:{9},开始时间:{10},结束时间:{11}", 
				news.getId(), news.getTitle(), news.getLanguage(), news.getIndex(), news.getContent(), news.getPartyId(), news.getImgUrl(), news.getImgJumpUrl(), news.getClick(), news.getPopUp(), news.getStartTime(), news.getEndTime());
			this.saveLog(sec, userNameLogin, log);
			
		} catch (BusinessException e) {
			modelAndView.addObject("error", e.getMessage());
			modelAndView.addObject("usercode", usercode);
			modelAndView.addObject("img_url", img_url);
			modelAndView.addObject("img_jump_url", img_jump_url);
			modelAndView.addObject("click", click);
			modelAndView.addObject("pop_up", pop_up);
			modelAndView.addObject("title", title);
			modelAndView.addObject("content", content);
			modelAndView.addObject("language", language);
			modelAndView.addObject("index", index);
			modelAndView.addObject("start_time", start_time);
			modelAndView.addObject("end_time", end_time);
			modelAndView.addObject("languageMap", Constants.LANGUAGE);
			modelAndView.setViewName("news_add");
			return modelAndView;
		} catch (Throwable t) {
			logger.error(" error ", t);
			modelAndView.addObject("error", "[ERROR] " + t.getMessage());	
			modelAndView.addObject("usercode", usercode);
			modelAndView.addObject("img_url", img_url);
			modelAndView.addObject("img_jump_url", img_jump_url);
			modelAndView.addObject("click", click);
			modelAndView.addObject("pop_up", pop_up);	
			modelAndView.addObject("title", title);
			modelAndView.addObject("content", content);
			modelAndView.addObject("language", language);
			modelAndView.addObject("index", index);
			modelAndView.addObject("start_time", start_time);
			modelAndView.addObject("end_time", end_time);
			modelAndView.addObject("languageMap", Constants.LANGUAGE);
			modelAndView.setViewName("news_add");
			return modelAndView;
		}
		
		modelAndView.addObject("message", "操作成功");
		modelAndView.setViewName("redirect:/" + action + "list.action");
		return modelAndView;
	}

	/**
	 * 修改 新闻管理 页面
	 * 
	 * usercode PartyId
	 * img_url 图片地址
	 * img_jump_url 图片跳转链接
	 * click 可否点击
	 * pop_up 是否弹出
	 * title 标题
	 * content 内容
	 * index 是否置顶
	 * language 语言
	 */
	@RequestMapping(action + "toUpdate.action")
	public ModelAndView toUpdate(HttpServletRequest request) {
		String id = request.getParameter("id");

		ModelAndView modelAndView = new ModelAndView();

		try {
			
			News news = this.adminNewsService.findById(id);
			
			if (null != news.getPartyId() && !StringUtils.isEmptyString(news.getPartyId().toString())) {
				Party party = this.partyService.cachePartyBy(news.getPartyId(), true);
				if (null == party) {
					throw new BusinessException("PartyId不存在");
				}
				modelAndView.addObject("usercode", party.getUsercode());
			} else {
				modelAndView.addObject("usercode", "");
			}
			
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

			modelAndView.addObject("id", id);
			modelAndView.addObject("img_url", news.getImgUrl());
			modelAndView.addObject("img_jump_url", news.getImgJumpUrl());
			modelAndView.addObject("click", news.getClick());
			modelAndView.addObject("pop_up", news.getPopUp());
			modelAndView.addObject("title", news.getTitle());
			modelAndView.addObject("content", news.getContent());
			modelAndView.addObject("language", news.getLanguage());
			modelAndView.addObject("index", news.getIndex());
			modelAndView.addObject("start_time", null == news.getStartTime() ? null : sdf.format(news.getStartTime()));
			modelAndView.addObject("end_time", null == news.getEndTime() ? null : sdf.format(news.getEndTime()));
			modelAndView.addObject("languageMap", Constants.LANGUAGE);

		} catch (BusinessException e) {
			modelAndView.addObject("error", e.getMessage());
			modelAndView.setViewName("redirect:/" + action + "list.action");
			return modelAndView;
		} catch (Throwable t) {
			logger.error(" error ", t);
			modelAndView.addObject("error", "[ERROR] " + t.getMessage());
			modelAndView.setViewName("redirect:/" + action + "list.action");
			return modelAndView;
		}
		
		modelAndView.setViewName("news_update");
		return modelAndView;
	}
	
	/**
	 * 修改 新闻管理
	 * 
	 * usercode PartyId
	 * img_url 图片地址
	 * img_jump_url 图片跳转链接
	 * click 可否点击
	 * pop_up 是否弹出
	 * title 标题
	 * content 内容
	 * index 是否置顶
	 * language 语言
	 */
	@RequestMapping(action + "update.action")
	public ModelAndView update(HttpServletRequest request) {
		String id = request.getParameter("id");
		String usercode = request.getParameter("usercode");
		String img_url = request.getParameter("img_url");
		String img_jump_url = request.getParameter("img_jump_url");
		String click = request.getParameter("click");
		String pop_up = request.getParameter("pop_up");
		String title = request.getParameter("title");
		String content = request.getParameter("content");
		String language = request.getParameter("language");
		String index = request.getParameter("index");
		String start_time = request.getParameter("start_time");
		String end_time = request.getParameter("end_time");
		String login_safeword = request.getParameter("login_safeword");

		ModelAndView modelAndView = new ModelAndView();

		try {

			String error = this.verif(click, pop_up, title, content);
			if (!StringUtils.isNullOrEmpty(error)) {
				throw new BusinessException(error);
			}

			int click_int = Integer.valueOf(click).intValue();
			int pop_up_int = Integer.valueOf(pop_up).intValue();
			boolean index_bool = Boolean.valueOf(index).booleanValue();
			
			String userNameLogin = this.getUsername_login();
			
			SecUser sec = this.secUserService.findUserByLoginName(userNameLogin);
			this.checkLoginSafeword(sec, userNameLogin, login_safeword);
			
			News news = this.adminNewsService.findById(id);
			
			String log = MessageFormat.format("ip:" + this.getIp() 
				+ ",管理员更新新闻,id:{0},原标题:{1},原语言:{2},原推荐:{3},原内容:{4},原PartyId:{5},原图片地址:{6},原图片跳转链接:{7},原可否点击:{8},原是否弹出:{9},开始时间:{10},结束时间:{11}", 
				news.getId(), news.getTitle(), news.getLanguage(), news.getIndex(), news.getContent(), news.getPartyId(), news.getImgUrl(), news.getImgJumpUrl(), news.getClick(), news.getPopUp(), news.getStartTime(), news.getEndTime());
			
			if (!StringUtils.isEmptyString(usercode)) {
				Party party = this.partyService.findPartyByUsercode(usercode);
				if (null == party) {
					throw new BusinessException("UID不存在");
				}
				news.setPartyId(party.getId());
			} else {
				news.setPartyId("");
			}
			
			Date start_date = null;
			Date end_date = null;			
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			if (!StringUtils.isEmptyString(start_time)) {
				start_date = sdf.parse(start_time);
			}
			if (!StringUtils.isEmptyString(end_time)) {
				end_date = sdf.parse(end_time);
			}

			news.setImgUrl(img_url);
			news.setImgJumpUrl(img_jump_url);
			news.setClick(click_int);
			news.setPopUp(pop_up_int);
			news.setTitle(title);
			news.setContent(content);
			news.setLanguage(language);
			news.setIndex(index_bool);
			news.setStartTime(start_date);
			news.setEndTime(end_date);
			this.adminNewsService.update(news);
			
			log += MessageFormat.format(",新标题:{0},新语言:{1},新推荐:{2},新内容:{3},新PartyId:{4},新图片地址:{5},新图片跳转链接:{6},新可否点击:{7},新是否弹出:{8},开始时间:{9},结束时间:{10}", 
					news.getTitle(), news.getLanguage(), news.getIndex(), news.getContent(), news.getPartyId(), news.getImgUrl(), news.getImgJumpUrl(), news.getClick(), news.getPopUp(), news.getStartTime(), news.getEndTime());
			this.saveLog(sec, userNameLogin, log);

		} catch (BusinessException e) {
			modelAndView.addObject("error", e.getMessage());			
			modelAndView.addObject("id", id);
			modelAndView.addObject("usercode", usercode);
			modelAndView.addObject("img_url", img_url);
			modelAndView.addObject("img_jump_url", img_jump_url);
			modelAndView.addObject("click", click);
			modelAndView.addObject("pop_up", pop_up);
			modelAndView.addObject("title", title);
			modelAndView.addObject("content", content);
			modelAndView.addObject("language", language);
			modelAndView.addObject("index", index);
			modelAndView.addObject("start_time", start_time);
			modelAndView.addObject("end_time", end_time);
			modelAndView.addObject("languageMap", Constants.LANGUAGE);
			modelAndView.setViewName("news_update");
			return modelAndView;
		} catch (Throwable t) {
			logger.error(" error ", t);
			modelAndView.addObject("error", "[ERROR] " + t.getMessage());		
			modelAndView.addObject("id", id);
			modelAndView.addObject("usercode", usercode);
			modelAndView.addObject("img_url", img_url);
			modelAndView.addObject("img_jump_url", img_jump_url);
			modelAndView.addObject("click", click);
			modelAndView.addObject("pop_up", pop_up);
			modelAndView.addObject("title", title);
			modelAndView.addObject("content", content);
			modelAndView.addObject("language", language);
			modelAndView.addObject("index", index);
			modelAndView.addObject("start_time", start_time);
			modelAndView.addObject("end_time", end_time);
			modelAndView.addObject("languageMap", Constants.LANGUAGE);
			modelAndView.setViewName("news_update");
			return modelAndView;
		}
		
		modelAndView.addObject("message", "操作成功");
		modelAndView.setViewName("redirect:/" + action + "list.action");
		return modelAndView;
	}

	/**
	 * 删除 新闻管理
	 */
	@RequestMapping(action + "delete.action")
	public ModelAndView delete(HttpServletRequest request) {
		String id = request.getParameter("id");
		String login_safeword = request.getParameter("login_safeword");

		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("redirect:/" + action + "list.action");
		
		try {
						
			String userNameLogin = this.getUsername_login();
			
			SecUser sec = this.secUserService.findUserByLoginName(userNameLogin);
			this.checkLoginSafeword(sec, userNameLogin, login_safeword);
			
			News news = this.adminNewsService.findById(id);
			
			String log = MessageFormat.format("ip:" + this.getIp() 
				+ ",管理员删除新闻,id:{0},原标题:{1},原语言:{2},原推荐:{3},原内容:{4},PartyId:{5},图片地址:{6},图片跳转链接:{7},可否点击:{8},是否弹出:{9}", 
				news.getId(), news.getTitle(), news.getLanguage(), news.getIndex(), news.getContent(), news.getPartyId(), news.getImgUrl(), news.getImgJumpUrl(), news.getClick(), news.getPopUp());
			
			this.adminNewsService.delete(id);
			
			this.saveLog(sec, userNameLogin, log);
			
		} catch (BusinessException e) {
			modelAndView.addObject("error", e.getMessage());
			return modelAndView;
		} catch (Throwable t) {
			logger.error("update error ", t);
			modelAndView.addObject("error", "程序错误");
			return modelAndView;
		}
		
		modelAndView.addObject("message", "操作成功");
		return modelAndView;
	}

	public String verif(String click, String pop_up, String title, String content) {
		
		if (StringUtils.isNullOrEmpty(click)) {
			return "可否点击必填";
		}
		if (!StringUtils.isInteger(click)) {
			return "可否点击输入错误，请输入整数";
		}
		if (Integer.valueOf(click).intValue() < 0) {
			return "可否点击不能小于0";
		}
		
		if (StringUtils.isNullOrEmpty(pop_up)) {
			return "是否弹出必填";
		}
		if (!StringUtils.isInteger(pop_up)) {
			return "是否弹出输入错误，请输入整数";
		}
		if (Integer.valueOf(pop_up).intValue() < 0) {
			return "是否弹出不能小于0";
		}
		
		if (StringUtils.isNullOrEmpty(title)) {
			return "请输入标题！";
		}
		
		if (StringUtils.isNullOrEmpty(content)) {
			return "请输入内容！";
		}
		
		return "";
	}

	/**
	 * 验证登录人资金密码
	 */
	protected void checkLoginSafeword(SecUser secUser, String operatorUsername, String loginSafeword) {
//		SecUser sec = this.secUserService.findUserByLoginName(operatorUsername);
		String sysSafeword = secUser.getSafeword();
		String safeword_md5 = this.passwordEncoder.encodePassword(loginSafeword, operatorUsername);
		if (!safeword_md5.equals(sysSafeword)) {
			throw new BusinessException("登录人资金密码错误");
		}		
	}
	
	public void saveLog(SecUser secUser, String operator, String context) {
		project.log.Log log = new project.log.Log();
		log.setCategory(Constants.LOG_CATEGORY_OPERATION);
		log.setOperator(operator);
		log.setUsername(secUser.getUsername());
		log.setPartyId(secUser.getPartyId());
		log.setLog(context);
		log.setCreateTime(new Date());
		this.logService.saveSync(log);
	}

}
