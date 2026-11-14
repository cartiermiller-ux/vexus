package project.web.admin;

import java.text.MessageFormat;
import java.util.Date;
import java.util.List;

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
import project.cms.AdminCmsService;
import project.cms.Cms;
import project.cms.PropertiesUtilCms;
import project.log.LogService;
import security.Role;
import security.SecUser;
import security.internal.SecUserService;

/**
 * 用户端内容管理
 */
@RestController
public class AdminCmsController extends PageActionSupport {

	private Logger logger = LoggerFactory.getLogger(AdminCmsController.class);

	@Autowired
	private AdminCmsService adminCmsService;
	@Autowired
	private SecUserService secUserService;
	@Autowired
	private LogService logService;
	@Autowired
	private PasswordEncoder passwordEncoder;

	private final String action = "normal/adminCmsAction!";

	/**
	 * 获取 用户端内容管理 列表
	 */
	@RequestMapping(action + "list.action")
	public ModelAndView list(HttpServletRequest request) {
		String pageNoStr = request.getParameter("pageNo");
		String message = request.getParameter("message");
		String error = request.getParameter("error");
		String para_language = request.getParameter("para_language");
		String para_title = request.getParameter("para_title");
		String content_code_para = request.getParameter("content_code_para");

		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("cms_list");

		int pageNo=1;
		Page page=null;
		int pageSize = 20;
		try {
			pageNo=checkAndSetPageNo(pageNoStr);
			page = this.adminCmsService.pagedQuery(pageNo, pageSize, para_language, para_title, content_code_para);
			
			for (Cms cms : (List<Cms>) page.getElements()) {				
				cms.setLanguage(Constants.LANGUAGE.get(cms.getLanguage()));
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
		modelAndView.addObject("para_language", para_language);
		modelAndView.addObject("para_title", para_title);
		modelAndView.addObject("content_code_para", content_code_para);
		modelAndView.addObject("languageMap", Constants.LANGUAGE);
		return modelAndView;
	}
	
	/**
	 * 新增 用户端内容管理 页面
	 */
	@RequestMapping(action + "toAdd.action")
	public ModelAndView toAdd(HttpServletRequest request) {
		ModelAndView modelAndView = new ModelAndView();
		
		try {
			if (!this.checkIsRoot()) {
				throw new BusinessException("root 权限下才可添加");
			}
			
			modelAndView.addObject("language", PropertiesUtilCms.getProperty("system_cms_language"));
			modelAndView.addObject("modelMap", Constants.CMS_MODEL);
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

		modelAndView.setViewName("cms_add");
		return modelAndView;
	}

	/**
	 * 新增 用户端内容管理
	 * 
	 * title 标题
	 * content 内容
	 * model 模块
	 * language 语言
	 * content_code 业务代码
	 */
	@RequestMapping(action + "add.action")
	public ModelAndView add(HttpServletRequest request) {
		String login_safeword = request.getParameter("login_safeword");
		String model = request.getParameter("model");
		String language = request.getParameter("language");
		String content_code = request.getParameter("content_code");
		String title = request.getParameter("title");
		String content = request.getParameter("content");

		ModelAndView modelAndView = new ModelAndView();
		
		try {
			
			String error = this.verif(title, content);
			if (!StringUtils.isNullOrEmpty(error)) {
				throw new BusinessException(error);
			}
			
			if (!this.checkIsRoot()) {
				throw new BusinessException("root 权限下才可添加");
			}
			
			String username_login = this.getUsername_login();
			
			SecUser sec = this.secUserService.findUserByLoginName(username_login);
			
			this.checkLoginSafeword(sec, username_login, login_safeword);
			
			if (StringUtils.isEmptyString(model)) {
				throw new BusinessException("请选择模块");
			}
			if (StringUtils.isEmptyString(language)) {
				throw new BusinessException("请选择语言");
			}
			if (StringUtils.isEmptyString(content_code)) {
				throw new BusinessException("请填写业务码");
			}

			Cms entity = new Cms();
			entity.setId(ApplicationUtil.getCurrentTimeUUID());
			entity.setTitle(title);
			entity.setContent(content);
			entity.setCreateTime(new Date());
			entity.setModel(model);
			entity.setLanguage(language);
			entity.setContent_code(content_code);

			this.adminCmsService.saveCms(entity);
			
			String log = null;
			if ("116".equals(content_code) || "117".equals(content_code)) {
				log = MessageFormat.format("ip:" + this.getIp() + ",管理员新增cms，id:{0},标题:{1},语言:{2},模块:{3},业务代码:{4},内容:新增图片", 
						entity.getId(), entity.getTitle(), entity.getLanguage(), entity.getModel(), entity.getContent_code());
			} else {
				log = MessageFormat.format("ip:" + this.getIp() + ",管理员新增cms，id:{0},标题:{1},语言:{2},模块:{3},业务代码:{4},内容:{5}", 
						entity.getId(), entity.getTitle(), entity.getLanguage(), entity.getModel(), entity.getContent_code(), entity.getContent());
			}			
			this.saveLog(sec, username_login, log);
			
		} catch (BusinessException e) {
			modelAndView.addObject("error", e.getMessage());
			modelAndView.addObject("model", model);
			modelAndView.addObject("language", language);
			modelAndView.addObject("content_code", content_code);
			modelAndView.addObject("title", title);
			modelAndView.addObject("content", content);
			modelAndView.addObject("modelMap", Constants.CMS_MODEL);
			modelAndView.addObject("languageMap", Constants.LANGUAGE);
			modelAndView.setViewName("cms_add");
			return modelAndView;
		} catch (Throwable t) {
			logger.error(" error ", t);
			modelAndView.addObject("error", "[ERROR] " + t.getMessage());
			modelAndView.addObject("model", model);
			modelAndView.addObject("language", language);
			modelAndView.addObject("content_code", content_code);
			modelAndView.addObject("title", title);
			modelAndView.addObject("content", content);
			modelAndView.addObject("modelMap", Constants.CMS_MODEL);
			modelAndView.addObject("languageMap", Constants.LANGUAGE);
			modelAndView.setViewName("cms_add");
			return modelAndView;
		}
		
		modelAndView.addObject("message", "操作成功");
		modelAndView.setViewName("redirect:/" + action + "list.action");
		return modelAndView;
	}

	/**
	 * 修改 用户端内容管理 页面
	 * 
	 * title 标题
	 * content 内容
	 * model 模块
	 * language 语言
	 * content_code 业务代码
	 */
	@RequestMapping(action + "toUpdate.action")
	public ModelAndView toUpdate(HttpServletRequest request) {
		String id = request.getParameter("id");

		ModelAndView modelAndView = new ModelAndView();

		try {
			if (StringUtils.isNullOrEmpty(id)) {
				throw new BusinessException("内容不存在或已删除");
			}
						
			Cms entity = this.adminCmsService.findById(id);
			if (null == entity) {
				throw new BusinessException("内容不存在或已删除");
			}

			modelAndView.addObject("id", id);
			modelAndView.addObject("title", entity.getTitle());
			modelAndView.addObject("content", entity.getContent());
			if (this.checkIsRoot()) {
				modelAndView.addObject("model", entity.getModel());
				modelAndView.addObject("language", entity.getLanguage());
				modelAndView.addObject("content_code", entity.getContent_code());
			}
			modelAndView.addObject("modelMap", Constants.CMS_MODEL);
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
		
		modelAndView.setViewName("cms_update");
		return modelAndView;
	}
	
	/**
	 * 修改 用户端内容管理
	 * 
	 * title 标题
	 * content 内容
	 * model 模块
	 * language 语言
	 * content_code 业务代码
	 */
	@RequestMapping(action + "update.action")
	public ModelAndView update(HttpServletRequest request) {
		String id = request.getParameter("id");
		String model = request.getParameter("model");
		String language = request.getParameter("language");
		String content_code = request.getParameter("content_code");
		String title = request.getParameter("title");
		String content = request.getParameter("content");
		String login_safeword = request.getParameter("login_safeword");

		ModelAndView modelAndView = new ModelAndView();

		try {
			
			String error = this.verif(title, content);
			if (!StringUtils.isNullOrEmpty(error)) {
				throw new BusinessException(error);
			}
			
			String username_login = this.getUsername_login();
			
			SecUser sec = this.secUserService.findUserByLoginName(username_login);
			
			this.checkLoginSafeword(sec, username_login, login_safeword);
			
			if (StringUtils.isNullOrEmpty(id)) {
				throw new BusinessException("内容不存在或已删除");
			}

			Cms entity = new Cms();
			entity = this.adminCmsService.findById(id);
			if (null == entity) {
				throw new BusinessException("内容不存在或已删除");
			}
			
			String log = null;
			if ("116".equals(content_code) || "117".equals(content_code)) {
				log = MessageFormat.format("ip:" + this.getIp() + ",管理员修改cms，id:{0},原标题:{1},原语言:{2},原模块:{3},原业务代码:{4},原内容:旧图片",
						entity.getId(), entity.getTitle(), entity.getLanguage(), entity.getModel(), entity.getContent_code());
			} else {
				log = MessageFormat.format("ip:" + this.getIp() + ",管理员修改cms，id:{0},原标题:{1},原语言:{2},原模块:{3},原业务代码:{4},原内容:{5}",
						entity.getId(), entity.getTitle(), entity.getLanguage(), entity.getModel(), entity.getContent_code(), entity.getContent());
			}
			
			String strOriginalLanguage = entity.getLanguage();
			String strOriginalContentCode = entity.getContent_code();

			entity.setTitle(title);
			entity.setContent(content);

			if (this.checkIsRoot()) {
				if (StringUtils.isEmptyString(model)) {
					throw new BusinessException("请选择模块");
				}
				if (StringUtils.isEmptyString(language)) {
					throw new BusinessException("请选择语言");
				}
				if (StringUtils.isEmptyString(content_code)) {
					throw new BusinessException("请填写业务码");
				}
				entity.setModel(model);
				entity.setLanguage(language);
				entity.setContent_code(content_code);
			}
			//this.adminCmsService.saveOrUpdate(entity);
			this.adminCmsService.update(entity,strOriginalLanguage,strOriginalContentCode);
			
			if ("116".equals(content_code) || "117".equals(content_code)) {
				log += MessageFormat.format(",新标题:{0},新语言:{1},新模块:{2},新业务代码:{3},新内容:新图片", 
						entity.getTitle(), entity.getLanguage(), entity.getModel(), entity.getContent_code());
			} else {
				log += MessageFormat.format(",新标题:{0},新语言:{1},新模块:{2},新业务代码:{3},新内容:{4}", 
						entity.getTitle(), entity.getLanguage(), entity.getModel(), entity.getContent_code(), entity.getContent());
			}
			saveLog(sec, username_login, log);

		} catch (BusinessException e) {
			modelAndView.addObject("error", e.getMessage());
			modelAndView.addObject("id", id);
			modelAndView.addObject("model", model);
			modelAndView.addObject("language", language);
			modelAndView.addObject("content_code", content_code);
			modelAndView.addObject("title", title);
			modelAndView.addObject("content", content);
			modelAndView.addObject("modelMap", Constants.CMS_MODEL);
			modelAndView.addObject("languageMap", Constants.LANGUAGE);
			modelAndView.setViewName("cms_update");
			return modelAndView;
		} catch (Throwable t) {
			logger.error(" error ", t);
			modelAndView.addObject("error", "[ERROR] " + t.getMessage());
			modelAndView.addObject("id", id);
			modelAndView.addObject("model", model);
			modelAndView.addObject("language", language);
			modelAndView.addObject("content_code", content_code);
			modelAndView.addObject("title", title);
			modelAndView.addObject("content", content);
			modelAndView.addObject("modelMap", Constants.CMS_MODEL);
			modelAndView.addObject("languageMap", Constants.LANGUAGE);
			modelAndView.setViewName("cms_update");
			return modelAndView;
		}
		
		modelAndView.addObject("message", "操作成功");
		modelAndView.setViewName("redirect:/" + action + "list.action");
		return modelAndView;
	}

	private boolean checkIsRoot() {
		String username = this.getUsername_login();
		// root才可以改动
		SecUser secUser = this.secUserService.findUserByLoginName(username);
		for (Role role : secUser.getRoles()) {
			if (Constants.SECURITY_ROLE_ROOT.equals(role.getRoleName())) {
				return Boolean.TRUE;
			}
		}
		return Boolean.FALSE;
	}

	public String verif(String title, String content) {
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
		logService.saveSync(log);
	}

}
