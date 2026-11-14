package project.web.api;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import kernel.exception.BusinessException;
import kernel.util.StringUtils;
import kernel.web.BaseAction;
import kernel.web.ResultObject;
import project.Constants;
import project.c2c.C2cPaymentMethodConfig;
import project.c2c.C2cPaymentMethodConfigService;
import project.c2c.C2cTranslate;
import project.c2c.C2cTranslateService;

/**
 * C2C支付方式模板
 */
@RestController
@CrossOrigin
public class C2cPaymentMethodConfigController extends BaseAction {

	private Logger logger = LoggerFactory.getLogger(C2cPaymentMethodConfigController.class);

	@Autowired
	private C2cPaymentMethodConfigService c2cPaymentMethodConfigService;
	@Autowired
	private C2cTranslateService c2cTranslateService;
    
	private final String action = "/api/c2cPaymentMethodConfig!";
	
	/**
	 * 获取 支付方式模板 列表
	 */
	@RequestMapping(action + "list.action")
	public Object list(HttpServletRequest request) {
		String language = request.getParameter("language");
		
		ResultObject resultObject = new ResultObject();
		resultObject = this.readSecurityContextFromSession(resultObject);
		if (!"0".equals(resultObject.getCode())) {
			return resultObject;
		}
		
		try {
			
			Map<String, String> methodConfigMap = this.c2cPaymentMethodConfigService.getMethodConfigMap();
			
			// 多语言
			for (String id : methodConfigMap.keySet()) {
				String name = methodConfigMap.get(id);
				if (null != name) {
					C2cTranslate trans = this.c2cTranslateService.get(name, language);
					if (null != trans) {
						methodConfigMap.put(id, trans.getTranslate());
					}
				}
			}

			resultObject.setData(methodConfigMap);

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
	 * 获取 支付方式模板 详情
	 */
	@RequestMapping(action + "get.action")
	public Object get(HttpServletRequest request) {
		String id = request.getParameter("id");
		String language = request.getParameter("language");
		
		ResultObject resultObject = new ResultObject();
		resultObject = this.readSecurityContextFromSession(resultObject);
		if (!"0".equals(resultObject.getCode())) {
			return resultObject;
		}
		
		try {
			
			C2cPaymentMethodConfig config = this.c2cPaymentMethodConfigService.get(id);
			if (null == config) {
				throw new BusinessException("支付方式模板不存在");
			}
			
			if (StringUtils.isNotEmpty(config.getMethodImg())) {
				String path = Constants.WEB_URL + "/public/showimg!showImg.action?imagePath=" + config.getMethodImg();
				config.setMethodImg(path);
			}

			resultObject.setData(this.c2cTranslateService.translatePmc(config, language));

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
