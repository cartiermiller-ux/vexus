package project.web.api;

import java.io.IOException;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import kernel.exception.BusinessException;
import kernel.util.StringUtils;
import kernel.web.ApplicationUtil;
import kernel.web.BaseAction;
import kernel.web.ResultObject;
import project.Constants;
import project.c2c.C2cAppeal;
import project.c2c.C2cAppealService;
import project.c2c.C2cOrder;
import project.c2c.C2cOrderService;
import project.party.PartyService;
import project.party.model.Party;
import project.tip.TipConstants;
import project.tip.TipService;

/**
 * C2C申诉
 */
@RestController
@CrossOrigin
public class C2cAppealController extends BaseAction {

	private Logger logger = LoggerFactory.getLogger(C2cAppealController.class);

	@Autowired
	private C2cAppealService c2cAppealService;
	@Autowired
	private C2cOrderService c2cOrderService;
	@Autowired
	private PartyService partyService;
	@Autowired
	private TipService tipService;
    
	private final String action = "/api/c2cAppeal!";
	
	/**
	 * 申诉申请
	 */
	@RequestMapping(action + "apply.action")
	public Object apply(HttpServletRequest request) throws IOException {
		String order_no = request.getParameter("order_no");		
		String reason = request.getParameter("reason");
		String description = request.getParameter("description");
		String img = request.getParameter("img");
		String name = request.getParameter("name");
		String phone = request.getParameter("phone");

		ResultObject resultObject = new ResultObject();
		resultObject = this.readSecurityContextFromSession(resultObject);		
		if (!"0".equals(resultObject.getCode())) {
			return resultObject;
		}
		
		try {
			
			if (StringUtils.isEmptyString(order_no)) {
				throw new BusinessException("申诉订单号不正确");
			}
			
			if (StringUtils.isEmptyString(reason)) {
				throw new BusinessException("请输入申诉原因");
			}
			
			if (StringUtils.isEmptyString(img)) {
				throw new BusinessException("请上传申诉凭证");
			}
			
			String partyId = this.getLoginPartyId();
			
			C2cOrder order = this.c2cOrderService.get(order_no);
			if (null == order || !partyId.equals(order.getPartyId())) {
				throw new BusinessException("订单不存在");
			}
			
			C2cAppeal appeal = this.c2cAppealService.get(order_no);
			if (null != appeal) {
				throw new BusinessException("该订单已提交申诉");
			}
			
			C2cAppeal entity = new C2cAppeal();
			entity.setId(ApplicationUtil.getCurrentTimeUUID());
			entity.setOrderNo(order_no);
			entity.setReason(reason);
			entity.setDescription(description);
			entity.setImg(img);
			entity.setName(name);
			entity.setPhone(phone);
			entity.setState("0");
			entity.setCreateTime(new Date());
			entity.setUpdateTime(new Date());
			
			this.c2cAppealService.save(entity);
			
			order.setState("2");
			this.c2cOrderService.update(order);
			
			Party party = this.partyService.cachePartyBy(partyId, false);
			if (Constants.SECURITY_ROLE_MEMBER.equals(party.getRolename())) {
				this.tipService.saveTip(entity.getId().toString(), TipConstants.C2C_APPEAL);
			}
			
			this.c2cOrderService.c2cSendMessageByState(order, "2");
			
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
	 * 获取 申诉详情
	 */
	@RequestMapping(action + "get.action")
	public Object get(HttpServletRequest request) throws IOException {
		String order_no = request.getParameter("order_no");

		ResultObject resultObject = new ResultObject();
		resultObject = this.readSecurityContextFromSession(resultObject);
		if (!"0".equals(resultObject.getCode())) {
			return resultObject;
		}
		
		try {
			
			if (StringUtils.isEmptyString(order_no)) {
				throw new BusinessException("申诉订单号不正确");
			}
			
			C2cAppeal c2cAppeal = this.c2cAppealService.get(order_no);
			if (null == c2cAppeal) {
				throw new BusinessException("申诉不存在");
			}
			
			if (StringUtils.isNotEmpty(c2cAppeal.getImg())) {
				String path = Constants.WEB_URL + "/public/showimg!showImg.action?imagePath=" + c2cAppeal.getImg();
				c2cAppeal.setImg(path);
			}

			resultObject.setData(c2cAppeal);
			
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
