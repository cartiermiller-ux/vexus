package project.web.admin;

import java.io.File;
import java.text.MessageFormat;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.providers.encoding.PasswordEncoder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import kernel.exception.BusinessException;
import kernel.util.ImageUtils;
import kernel.util.PropertiesLoaderUtils;
import kernel.util.StringUtils;
import kernel.web.ApplicationUtil;
import kernel.web.Page;
import kernel.web.PageActionSupport;
import project.Constants;
import project.cms.AdminBannerService;
import project.cms.Banner;
import project.cms.BannerService;
import project.cms.PropertiesUtilCms;
import project.log.LogService;
import security.Role;
import security.SecUser;
import security.internal.SecUserService;

/**
 * 横幅管理
 */
@RestController
public class AdminBannerController extends PageActionSupport {

	private Logger logger = LoggerFactory.getLogger(AdminBannerController.class);
	
	private static Properties properties = PropertiesLoaderUtils.loadProperties("config/system.properties");

	@Autowired
	private AdminBannerService adminBannerService;
	@Autowired
	private BannerService bannerService;
	@Autowired
	private SecUserService secUserService;
	@Autowired
	private LogService logService;
	@Autowired
	private PasswordEncoder passwordEncoder;

	private final String action = "normal/adminBannerAction!";

	/**
	 * 获取 横幅管理 列表
	 */
	@RequestMapping(action + "list.action")
	public ModelAndView list(HttpServletRequest request) {
		String pageNoStr = request.getParameter("pageNo");
		String message = request.getParameter("message");
		String error = request.getParameter("error");
		String para_language = request.getParameter("para_language");

		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("banner_list");

		int pageNo=1;
		Page page=null;
		int pageSize=30;
		try {
			pageNo=checkAndSetPageNo(pageNoStr);
			page = this.adminBannerService.pagedQuery(pageNo, pageSize, para_language);
			
			for (Banner banner : (List<Banner>) page.getElements()) {
				banner.setLanguage(Constants.LANGUAGE.get(banner.getLanguage()));
				banner.setModel(Constants.BANNER_MODEL.get(banner.getModel()));
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
		modelAndView.addObject("languageMap", Constants.LANGUAGE);
		return modelAndView;
	}
	
	/**
	 * 新增 横幅管理 页面
	 */
	@RequestMapping(action + "toAdd.action")
	public ModelAndView toAdd(HttpServletRequest request) {
		ModelAndView modelAndView = new ModelAndView();
		
		try {
			if (!this.checkIsRoot()) {
				throw new BusinessException("root 权限下才可添加");
			}
			
			modelAndView.addObject("language", PropertiesUtilCms.getProperty("system_cms_language"));
			modelAndView.addObject("modelMap", Constants.BANNER_MODEL);
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

		modelAndView.setViewName("banner_add");
		return modelAndView;
	}

	/**
	 * 新增 横幅管理
	 * 
	 * model 模块
	 * language 语言
	 * content_code 业务代码
	 * img 展示图片
	 * url 访问路径
	 * click 是否可点击
	 * on_show 是否展示
	 * sort_index 排序索引
	 * fileName 文件名
	 */
	@RequestMapping(action + "add.action")
	public ModelAndView add(HttpServletRequest request) {
		String login_safeword = request.getParameter("login_safeword");
		String model = request.getParameter("model");
		String language = request.getParameter("language");
		String content_code = request.getParameter("content_code");
		String img = request.getParameter("img");
		String url = request.getParameter("url");
		String click = request.getParameter("click");
		String on_show = request.getParameter("on_show");
		String sort_index = request.getParameter("sort_index");

		ModelAndView modelAndView = new ModelAndView();
		
		try {
			
			String error = this.verif(img, click, on_show, sort_index);
			if (!StringUtils.isNullOrEmpty(error)) {
				throw new BusinessException(error);
			}
			
			int click_int = Integer.valueOf(click).intValue();
			int on_show_int = Integer.valueOf(on_show).intValue();
			int sort_index_int = Integer.valueOf(sort_index).intValue();
			
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

			String fileDir = properties.getProperty("images.dir");
			File imgFile = new File(fileDir + "/" + img);

			Banner entity = new Banner();
			entity.setId(ApplicationUtil.getCurrentTimeUUID());
			entity.setCreateTime(new Date());
			entity.setModel(model);
			entity.setLanguage(language);
			entity.setContent_code(content_code);
			entity.setUrl(url);
			entity.setClick(click_int);
			entity.setOn_show(on_show_int);
			entity.setSort_index(sort_index_int);
			entity.setImage(ImageUtils.reduceImg(imgFile, 1f));
			this.bannerService.save(entity);			
			
			String log = MessageFormat.format(
					"ip:" + this.getIp() + ",管理员新增banner，id:{0},路径:{1},语言:{2},模块:{3},业务代码:{4},索引:{5},可否点击:{6},是否展示:{7}", 
					entity.getId(), entity.getUrl(), entity.getLanguage(), entity.getModel(), entity.getContent_code(), entity.getSort_index(), entity.getClick(), entity.getOn_show());
			
			this.saveLog(sec, username_login, log);
			
		} catch (BusinessException e) {
			modelAndView.addObject("error", e.getMessage());
			modelAndView.addObject("model", model);
			modelAndView.addObject("language", language);
			modelAndView.addObject("content_code", content_code);
			modelAndView.addObject("img", img);
			modelAndView.addObject("url", url);
			modelAndView.addObject("click", click);
			modelAndView.addObject("on_show", on_show);
			modelAndView.addObject("sort_index", sort_index);
			modelAndView.addObject("modelMap", Constants.BANNER_MODEL);
			modelAndView.addObject("languageMap", Constants.LANGUAGE);
			modelAndView.setViewName("banner_add");
			return modelAndView;
		} catch (Throwable t) {
			logger.error(" error ", t);
			modelAndView.addObject("error", "[ERROR] " + t.getMessage());
			modelAndView.addObject("model", model);
			modelAndView.addObject("language", language);
			modelAndView.addObject("content_code", content_code);
			modelAndView.addObject("img", img);
			modelAndView.addObject("url", url);
			modelAndView.addObject("click", click);
			modelAndView.addObject("on_show", on_show);
			modelAndView.addObject("sort_index", sort_index);
			modelAndView.addObject("modelMap", Constants.BANNER_MODEL);
			modelAndView.addObject("languageMap", Constants.LANGUAGE);
			modelAndView.setViewName("banner_add");
			return modelAndView;
		}
		
		modelAndView.addObject("message", "操作成功");
		modelAndView.setViewName("redirect:/" + action + "list.action");
		return modelAndView;
	}

	/**
	 * 修改 横幅管理 页面
	 */
	@RequestMapping(action + "toUpdate.action")
	public ModelAndView toUpdate(HttpServletRequest request) {
		String id = request.getParameter("id");

		ModelAndView modelAndView = new ModelAndView();

		try {
			
			if (StringUtils.isNullOrEmpty(id)) {
				throw new BusinessException("内容不存在或已删除");
			}

			Banner entity = this.bannerService.cacheById(id);
			if (null == entity) {
				throw new BusinessException("内容不存在或已删除");
			}

			modelAndView.addObject("id", id);
			modelAndView.addObject("img", entity.getImage());
			modelAndView.addObject("on_show", entity.getOn_show());
			modelAndView.addObject("click", entity.getClick());
			modelAndView.addObject("sort_index", entity.getSort_index());
			if (this.checkIsRoot()) {
				modelAndView.addObject("model", entity.getModel());
				modelAndView.addObject("language", entity.getLanguage());
				modelAndView.addObject("content_code", entity.getContent_code());
				modelAndView.addObject("url", entity.getUrl());
			}
			modelAndView.addObject("modelMap", Constants.BANNER_MODEL);
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
		
		modelAndView.setViewName("banner_update");
		return modelAndView;
	}
	
	/**
	 * 修改 横幅管理
	 */
	@RequestMapping(action + "update.action")
	public ModelAndView update(HttpServletRequest request) {
		String id = request.getParameter("id");
		String img = request.getParameter("img");
		String on_show = request.getParameter("on_show");
		String click = request.getParameter("click");
		String sort_index = request.getParameter("sort_index");
		String model = request.getParameter("model");
		String language = request.getParameter("language");
		String content_code = request.getParameter("content_code");
		String url = request.getParameter("url");
		String login_safeword = request.getParameter("login_safeword");
		String imgFileName = request.getParameter("imgFileName");

		ModelAndView modelAndView = new ModelAndView();

		try {
			
			String error = this.verif(img, click, on_show, sort_index);
			if (!StringUtils.isNullOrEmpty(error)) {
				throw new BusinessException(error);
			}
			
			int click_int = Integer.valueOf(click).intValue();
			int on_show_int = Integer.valueOf(on_show).intValue();
			int sort_index_int = Integer.valueOf(sort_index).intValue();
			
			String username_login = this.getUsername_login();
			
			SecUser sec = this.secUserService.findUserByLoginName(username_login);
			
			this.checkLoginSafeword(sec, username_login, login_safeword);
			
			if (StringUtils.isNullOrEmpty(id)) {
				throw new BusinessException("内容不存在或已删除");
			}

			Banner entity = new Banner();
			entity = this.bannerService.cacheById(id);
			if (null == entity) {
				throw new BusinessException("内容不存在或已删除");
			}
			
			String log = MessageFormat.format(
					"ip:" + this.getIp() + ",管理员修改banner，id:{0},原路径:{1},原语言:{2},原模块:{3},原业务代码:{4},原索引:{5},原可否点击:{6},原是否展示:{7}",
					entity.getId(), entity.getUrl(), entity.getLanguage(), entity.getModel(), entity.getContent_code(), entity.getSort_index(), entity.getClick(), entity.getOn_show());

			entity.setClick(click_int);
			entity.setOn_show(on_show_int);
			entity.setSort_index(sort_index_int);
			
			if (!StringUtils.isEmptyString(imgFileName)) {
				String fileDir = properties.getProperty("images.dir");
				File imgFile = new File(fileDir + "/" + imgFileName);
				entity.setImage(ImageUtils.reduceImg(imgFile, 1f));
			}

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
				entity.setUrl(url);
			}
			
			this.bannerService.update(entity);

			log += MessageFormat.format(",新路径:{0},新语言:{1},新模块:{2},新业务代码:{3},新索引:{4},新可否点击:{5},新是否展示:{6}", 
					entity.getUrl(), entity.getLanguage(), entity.getModel(), entity.getContent_code(), entity.getSort_index(), entity.getClick(), entity.getOn_show());
			
			this.saveLog(sec, username_login, log);

		} catch (BusinessException e) {
			modelAndView.addObject("error", e.getMessage());
			modelAndView.addObject("id", id);
			modelAndView.addObject("img", img);
			modelAndView.addObject("on_show", on_show);
			modelAndView.addObject("click", click);
			modelAndView.addObject("sort_index", sort_index);
			modelAndView.addObject("model", model);
			modelAndView.addObject("language", language);
			modelAndView.addObject("content_code", content_code);
			modelAndView.addObject("url", url);
			modelAndView.addObject("imgFileName", imgFileName);
			modelAndView.addObject("modelMap", Constants.BANNER_MODEL);
			modelAndView.addObject("languageMap", Constants.LANGUAGE);
			modelAndView.setViewName("banner_update");
			return modelAndView;
		} catch (Throwable t) {
			logger.error(" error ", t);
			modelAndView.addObject("error", "[ERROR] " + t.getMessage());
			modelAndView.addObject("id", id);
			modelAndView.addObject("img", img);
			modelAndView.addObject("on_show", on_show);
			modelAndView.addObject("click", click);
			modelAndView.addObject("sort_index", sort_index);
			modelAndView.addObject("model", model);
			modelAndView.addObject("language", language);
			modelAndView.addObject("content_code", content_code);
			modelAndView.addObject("url", url);
			modelAndView.addObject("imgFileName", imgFileName);
			modelAndView.addObject("modelMap", Constants.BANNER_MODEL);
			modelAndView.addObject("languageMap", Constants.LANGUAGE);
			modelAndView.setViewName("banner_update");
			return modelAndView;
		}
		
		modelAndView.addObject("message", "操作成功");
		modelAndView.setViewName("redirect:/" + action + "list.action");
		return modelAndView;
	}

	/**
	 * 删除 横幅管理
	 */
	@RequestMapping(action + "delete.action")
	public ModelAndView delete(HttpServletRequest request) {
		String id = request.getParameter("id");
		String login_safeword = request.getParameter("login_safeword");

		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("redirect:/" + action + "list.action");
		
		try {
			
			if (!this.checkIsRoot()) {
				throw new BusinessException("权限不足");
			}
			
			if (StringUtils.isNullOrEmpty(id)) {
				throw new BusinessException("请传入id");
			}
			
			String username_login = this.getUsername_login();
			
			SecUser sec = this.secUserService.findUserByLoginName(username_login);
			
			this.checkLoginSafeword(sec, username_login, login_safeword);
			
			Banner entity = this.bannerService.cacheById(id);
			if (null == entity) {
				throw new BusinessException("内容不存在或已删除");
			}
			
			this.bannerService.delete(id);
			
			String log = MessageFormat.format(
					"ip:" + this.getIp() + ",管理员删除banner，id:{0},路径:{1},语言:{2},模块:{3},业务代码:{4},索引:{5},可否点击:{6},是否展示:{7}", 
					entity.getId(), entity.getUrl(), entity.getLanguage(), entity.getModel(), entity.getContent_code(), entity.getSort_index(), entity.getClick(), entity.getOn_show());
			
			this.saveLog(sec, username_login, log);
			
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

	public String verif(String img, String click, String on_show, String sort_index) {
		
		if (StringUtils.isNullOrEmpty(img)) {
			return "请上传图片！";
		}
		
		if (StringUtils.isNullOrEmpty(click)) {
			throw new BusinessException("是否可点击不能为空");
		}
		if (!StringUtils.isInteger(click)) {
			throw new BusinessException("是否可点击不是整数");
		}
		if (Integer.valueOf(click).intValue() < 0) {
			throw new BusinessException("是否可点击不能小于0");
		}
		
		if (StringUtils.isNullOrEmpty(on_show)) {
			throw new BusinessException("是否展示不能为空");
		}
		if (!StringUtils.isInteger(on_show)) {
			throw new BusinessException("是否展示不是整数");
		}
		if (Integer.valueOf(on_show).intValue() < 0) {
			throw new BusinessException("是否展示不能小于0");
		}
		
		if (StringUtils.isNullOrEmpty(sort_index)) {
			throw new BusinessException("排序索引不能为空");
		}
		if (!StringUtils.isInteger(sort_index)) {
			throw new BusinessException("排序索引不是整数");
		}
		if (Integer.valueOf(sort_index).intValue() < 0) {
			throw new BusinessException("排序索引不能小于0");
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
