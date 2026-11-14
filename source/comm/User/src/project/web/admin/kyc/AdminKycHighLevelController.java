package project.web.admin.kyc;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import kernel.exception.BusinessException;
import kernel.web.Page;
import kernel.web.PageActionSupport;
import project.Constants;
import project.user.kyc.AdminKycHighLevelService;

/**
 * 用户高级认证
 */
@RestController
public class AdminKycHighLevelController extends PageActionSupport {

	private Logger logger=LoggerFactory.getLogger(AdminKycHighLevelController.class);

	@Autowired
	private AdminKycHighLevelService adminKycHighLevelService;

	private final String action = "normal/adminKycHighLevelAction!";

	/**
	 * 获取 用户高级认证 列表
	 */
	@RequestMapping(action + "list.action")
	public ModelAndView list(HttpServletRequest request) {
		String message = request.getParameter("message");
		String error = request.getParameter("error");
		String name_para = request.getParameter("name_para");
		String state_para = request.getParameter("state_para");
		String rolename_para = request.getParameter("rolename_para");

		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("kyc_highlevel_list");

		int pageNo = 1;
		int pageSize = 20;
		Page page = null;
		try {
			pageNo = this.checkAndSetPageNo(request.getParameter("pageNo"));
			page = this.adminKycHighLevelService.pagedQuery(pageNo, pageSize, name_para, state_para, rolename_para, this.getLoginPartyId());
			
			String preImg = Constants.WEB_URL + "/public/showimg!showImg.action?imagePath=";
			
			for (Map<String, Object> map : (List<Map<String, Object>>) page.getElements()) {
				
				map.put("relatives_name_encode", map.get("relatives_name").toString().replace("\'", "\\\'").replace("\"", "\\\""));

				if ("".equals(map.get("idimg_1")) || map.get("idimg_1") == null) {
					map.put("idimg_1", preImg + "qr/id_img3.jpg");
				} else {
					map.put("idimg_1", preImg + map.get("idimg_1").toString());
				}
				
				if ("".equals(map.get("idimg_2")) || map.get("idimg_2") == null) {
					map.put("idimg_2", preImg + "qr/id_img3.jpg");
				} else {
					map.put("idimg_2", preImg + map.get("idimg_2").toString());
				}
				
				if ("".equals(map.get("idimg_3")) || map.get("idimg_3") == null) {
					map.put("idimg_3", preImg + "qr/id_img3.jpg");
				} else {
					map.put("idimg_3", preImg + map.get("idimg_3").toString());
				}
				
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
		modelAndView.addObject("state_para", state_para);
		modelAndView.addObject("rolename_para", rolename_para);
		return modelAndView;
	}

	/**
	 * 审核通过
	 */
	@RequestMapping(action + "savePassed.action")
	public ModelAndView savePassed(HttpServletRequest request) {
		String partyId = request.getParameter("partyId");

		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("redirect:/" + action + "list.action");
		
		try {
			
			this.adminKycHighLevelService.savePassed(partyId);
			
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

	/**
	 * 驳回
	 */
	@RequestMapping(action + "saveFailed.action")
	public ModelAndView saveFailed(HttpServletRequest request) {
		String partyId = request.getParameter("partyId");
		String msg = request.getParameter("msg");

		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("redirect:/" + action + "list.action");
		
		try {
			
			this.adminKycHighLevelService.saveFailed(partyId, msg);
			
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

	private String getPath(HttpServletRequest request) {
		return String.format("%s://%s:%s%s", request.getScheme(), request.getServerName(), request.getServerPort(), request.getContextPath());
	}

}
