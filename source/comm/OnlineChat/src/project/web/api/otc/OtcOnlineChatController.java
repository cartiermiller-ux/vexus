package project.web.api.otc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import kernel.exception.BusinessException;
import kernel.util.DateUtils;
import kernel.util.JsonUtils;
import kernel.util.StringUtils;
import kernel.web.BaseAction;
import kernel.web.ResultObject;
import project.Constants;
import project.onlinechat.otc.OtcMessageUser;
import project.onlinechat.otc.OtcOnlineChatMessage;
import project.onlinechat.otc.OtcOnlineChatMessageService;

@RestController
@CrossOrigin
public class OtcOnlineChatController extends BaseAction {
	
	private Logger logger = LoggerFactory.getLogger(OtcOnlineChatController.class);
	
	@Resource
	private OtcOnlineChatMessageService otcOnlineChatMessageService;
	
	public final String action = "api/otcOnlinechat";

	/**
	 * 消息列表
	 */
	@RequestMapping(action + "!list.action")
	public Object list(HttpServletRequest request) {
		ResultObject resultObject = new ResultObject();
		resultObject = this.readSecurityContextFromSession(resultObject);
		if (!"0".equals(resultObject.getCode())) {
			return JsonUtils.getJsonString(resultObject);
		}
		String partyId = getLoginPartyId();
		try {
			String messageId = request.getParameter("messageId");
			String orderNo = request.getParameter("orderNo");
			// 首页的时候才更新未读数
			if (StringUtils.isNullOrEmpty(messageId)) {
				OtcMessageUser cacheMessageUser = otcOnlineChatMessageService.cacheMessageUser(orderNo);
				if (cacheMessageUser != null && cacheMessageUser.getUser_unreadmsg() > 0) {
					otcOnlineChatMessageService.updateUnread(partyId, "read", orderNo);
				}
			}

			List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
			List<OtcOnlineChatMessage> list = otcOnlineChatMessageService.cacheGetList(messageId, 10, orderNo, "user");
			for (OtcOnlineChatMessage message : list) {
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("id", message.getId());
				if (partyId.equals(message.getPartyId())) {
					map.put("send_receive", "send");
				} else {
					map.put("send_receive", "receive");
				}
				
				String type = message.getType();
				map.put("type", type);
				String content = message.getContent();
				if ("img".equals(type)) {
					content = Constants.WEB_URL + "/public/showimg!showImg.action?imagePath=" + content;
				}
				map.put("content", content);
				map.put("createtime", DateUtils.format(message.getCreateTime(), "MM-dd HH:mm"));
				data.add(map);
			}
			resultObject.setData(data);
		} catch (BusinessException e) {
			resultObject.setCode("1");
			resultObject.setMsg(e.getMessage());
		} catch (Exception e) {
			resultObject.setCode("1");
			resultObject.setMsg("Program error!");
			logger.error("error:", e);
		}
		return resultObject;
	}

	/**
	 * 发送消息
	 */
	@RequestMapping(value = action + "!send.action")
	public Object send(HttpServletRequest request) {
		ResultObject resultObject = new ResultObject();
		try {
			String content = request.getParameter("content");
			String type = request.getParameter("type");
			String orderNo = request.getParameter("orderNo");
			if (StringUtils.isNullOrEmpty(content.trim()) || StringUtils.isNullOrEmpty(type)) {
				return resultObject;
			}
			
			if(StringUtils.isEmptyString(orderNo)) {
				throw new BusinessException("订单号不能未空");
			}

			resultObject = this.readSecurityContextFromSession(resultObject);
			if (!"0".equals(resultObject.getCode())) {
				return JsonUtils.getJsonString(resultObject);
			}
			otcOnlineChatMessageService.saveSend(getLoginPartyId(), type, content, null, orderNo);
		} catch (BusinessException e) {
			resultObject.setCode("1");
			resultObject.setMsg(e.getMessage());
		} catch (Exception e) {
			resultObject.setCode("1");
			resultObject.setMsg("程序错误");
			logger.error("error:", e);
		}
		return resultObject;
	}

	/**
	 * 获取未读消息数
	 */
	@RequestMapping(action + "!unread.action")
	public Object unread(HttpServletRequest request) {
		ResultObject resultObject = new ResultObject();
		resultObject = this.readSecurityContextFromSession(resultObject);
		if (!"0".equals(resultObject.getCode())) {
			return JsonUtils.getJsonString(resultObject);
		}
		try {
			int unreadMsg = 0;
			String orderNo = request.getParameter("orderNo");
			if(StringUtils.isEmptyString(orderNo)) {
				throw new BusinessException("订单号不能未空");
			}
			unreadMsg = otcOnlineChatMessageService.unreadMsg(orderNo, getLoginPartyId());
			resultObject.setData(unreadMsg);

		} catch (BusinessException e) {
			resultObject.setCode("1");
			resultObject.setMsg(e.getMessage());
		} catch (Exception e) {
			resultObject.setCode("1");
			resultObject.setMsg("程序错误");
			logger.error("error:", e);
		}
		return resultObject;
	}

	public void setOtcOnlineChatMessageService(OtcOnlineChatMessageService otcOnlineChatMessageService) {
		this.otcOnlineChatMessageService = otcOnlineChatMessageService;
	}

}
