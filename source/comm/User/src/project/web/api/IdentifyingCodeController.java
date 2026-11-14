package project.web.api;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mysql.cj.util.StringUtils;

import kernel.exception.BusinessException;
import kernel.web.BaseAction;
import kernel.web.ResultObject;
import project.user.idcode.IdentifyingCodeService;

/**
 * 邮箱或手机绑定
 * 发送验证码
 *
 */
@RestController
@CrossOrigin
public class IdentifyingCodeController extends BaseAction {
	
	@Autowired
	private IdentifyingCodeService identifyingCodeService;

	@RequestMapping("/api/idcode!execute.action")
	public Object execute(HttpServletRequest request) {
		ResultObject resultObject = new ResultObject();
		try {
			// 邮件或手机号
			String target = request.getParameter("target");
			if (StringUtils.isNullOrEmpty(target)) {
				throw new BusinessException("不能为空");
			}
			identifyingCodeService.send(target, this.getIp());
		} catch (BusinessException e) {
			resultObject.setCode("1");
			resultObject.setMsg(e.getMessage());
		}
		return resultObject;
	}

}
