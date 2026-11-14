package project.web.admin;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import kernel.util.JsonUtils;
import kernel.util.StringUtils;
import project.tip.TipService;
import security.web.BaseSecurityAction;

/**
 * 管理员消息通知
 */
@RestController
public class AdminTipController extends BaseSecurityAction {
	
	@Autowired
	private TipService tipService;

	private final String action = "normal/adminTipAction!";
	
	@RequestMapping(value = action + "getTips.action")
	public String getTips() {
		if (!StringUtils.isNullOrEmpty(getLoginPartyId())) {
			return "";
		}
		Map<String, Object> result = new HashMap<String, Object>();
	    result.put("tipList", tipService.cacheSumTips(this.getUsername_login()));
		return JsonUtils.getJsonString(result);
	}

	@RequestMapping(value = action + "getNewTips.action")
	public String getNewTips(HttpServletRequest request) {
		if (!StringUtils.isNullOrEmpty(getLoginPartyId())) {
			return "";
		}
		
		Long time_stamp = null;
		if (!StringUtils.isNullOrEmpty(request.getParameter("time_stamp"))) {
			time_stamp = Long.valueOf(request.getParameter("time_stamp"));
		}

		String model = request.getParameter("model");
		
		Map<String, Object> result = new HashMap<String, Object>();
		
		if (!StringUtils.isNullOrEmpty(model)) {
			result.put("tipList", tipService.cacheNewTipsByModel(this.getUsername_login(), time_stamp, model));
		} else {
			result.put("tipList", tipService.cacheNewTips(this.getUsername_login(), time_stamp));
		}

		return JsonUtils.getJsonString(result);
	}

	public void setTipService(TipService tipService) {
		this.tipService = tipService;
	}
}
