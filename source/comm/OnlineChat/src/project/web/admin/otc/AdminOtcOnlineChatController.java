package project.web.admin.otc;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import kernel.exception.BusinessException;
import kernel.util.DateUtils;
import kernel.util.JsonUtils;
import kernel.util.StringUtils;
import kernel.web.PageActionSupport;
import kernel.web.ResultObject;
import project.Constants;
import project.c2c.C2cOrder;
import project.c2c.C2cOrderService;
import project.onlinechat.otc.OtcMessageUser;
import project.onlinechat.otc.OtcOnlineChatMessage;
import project.onlinechat.otc.OtcOnlineChatMessageService;
import project.user.token.Token;
import project.user.token.TokenService;

@RestController
public class AdminOtcOnlineChatController extends PageActionSupport {

	private Logger logger = LoggerFactory.getLogger(AdminOtcOnlineChatController.class);
	
	@Autowired
	private OtcOnlineChatMessageService otcOnlineChatMessageService;
	
	@Autowired
	private TokenService tokenService;
	
	@Autowired
	private C2cOrderService c2cOrderService;
	
	private final String action = "normal/adminOtcOnlineChatAction!";

	@RequestMapping(value = action + "list.action", produces="text/html;charset=UTF-8") 
	public String list(HttpServletRequest request) {
		ResultObject resultObject = new ResultObject();
		resultObject = this.readSecurityContextFromSession(resultObject);
		if (!"0".equals(resultObject.getCode())) {
			return JsonUtils.getJsonString(resultObject);
		}
		
		try {
			String messageId = request.getParameter("message_id");
			String orderNo = request.getParameter("order_no");
			String partyId = request.getParameter("partyid");
			
			// 首页的时候才更新未读数
			if (StringUtils.isNullOrEmpty(messageId) && !StringUtils.isNullOrEmpty(partyId)) {
				OtcMessageUser cacheOtcMessageUser = otcOnlineChatMessageService.cacheMessageUser(orderNo);
				if (cacheOtcMessageUser != null && cacheOtcMessageUser.getCustomer_unreadmsg() > 0) {
					otcOnlineChatMessageService.updateUnreadAdmin(partyId, "read", orderNo);
				}
			}
			List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
			List<OtcOnlineChatMessage> list = otcOnlineChatMessageService.cacheGetList(messageId, 30, orderNo);
			
			C2cOrder c2cOrder = c2cOrderService.get(orderNo);
			// 承兑商ID
			String c2cUserId = c2cOrder.getC2cUserPartyId();
			
			for(OtcOnlineChatMessage message : list) {
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("id", message.getId());
				if (c2cUserId.equals(message.getPartyId())) {
					map.put("send_receive", "receive");
				} else {
					map.put("send_receive", "send");
				}
				String type = message.getType();
				map.put("type", type);
				String content = message.getContent();
				if ("img".equals(type)) {
					content = Constants.WEB_URL + "/public/showimg!showImg.action?imagePath=" + content;
				}
				map.put("content", content);
				map.put("createtime", DateUtils.format(message.getCreateTime(), "MM-dd HH:mm"));
				map.put("delete_status", message.getDelete_status());
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
		return JsonUtils.getJsonString(resultObject);
	}

	@RequestMapping(value = action + "send.action", produces="text/html;charset=UTF-8")
	public String send(HttpServletRequest request) {
		ResultObject resultObject = new ResultObject();
		String token = request.getParameter("token");
		if (StringUtils.isNullOrEmpty(token)) {
			resultObject.setCode("403");
			resultObject.setMsg("请重新登录");
			return JsonUtils.getJsonString(resultObject);
		}
		try {
			// 后台配置的承兑商用户ID
			String partyId = request.getParameter("partyid");
			String type = request.getParameter("type");
			String content = request.getParameter("content");
			String orderNo = request.getParameter("order_no");
			
			if (StringUtils.isNullOrEmpty(partyId)) {
				throw new BusinessException("暂无用户");
			}
			
			if (StringUtils.isNullOrEmpty(content.trim()) || StringUtils.isNullOrEmpty(type)) {
				throw new BusinessException("请输入内容");
			}
			
			if(StringUtils.isEmptyString(orderNo)) {
				throw new BusinessException("订单号不能未空");
			}
			// 文字内容乱码处理
			content = URLDecoder.decode(content.replaceAll("%(?![0-9a-fA-F]{2})", "%25"), "utf-8");
			
			otcOnlineChatMessageService.saveSend(partyId, type, content, this.getUsername_login(), orderNo);

		} catch (BusinessException e) {
			resultObject.setCode("1");
			resultObject.setMsg(e.getMessage());
		} catch (Exception e) {
			resultObject.setCode("1");
			resultObject.setMsg("Program error!");
			logger.error("error:", e);
		}

		return JsonUtils.getJsonString(resultObject);
	}

	@RequestMapping(value = action + "create.action", produces="text/html;charset=UTF-8")
	public String create(HttpServletRequest request) {
		ResultObject resultObject = new ResultObject();
		resultObject = this.readSecurityContextFromSession(resultObject);
		if (!"0".equals(resultObject.getCode())) {
			return JsonUtils.getJsonString(resultObject);
		}
		try {
			String orderNo = request.getParameter("order_no");
			otcOnlineChatMessageService.saveCreateByOrderNo(orderNo);
		} catch (BusinessException e) {
			resultObject.setCode("1");
			resultObject.setMsg(e.getMessage());
		} catch (Exception e) {
			resultObject.setCode("1");
			resultObject.setMsg("Program error!");
			logger.error("error:", e);
		}

		return JsonUtils.getJsonString(resultObject);
	}
	
	@RequestMapping(value = action + "del.action")
	public String del(HttpServletRequest request) {
		ResultObject resultObject = new ResultObject();
		resultObject = this.readSecurityContextFromSession(resultObject);
		if (!"0".equals(resultObject.getCode())) {
			return JsonUtils.getJsonString(resultObject);
		}
		try {
			String partyId = request.getParameter("partyid");
			// ip，表示游客
			if (partyId.indexOf(".") != -1) {
				otcOnlineChatMessageService.deleteByIp(partyId);
			} else {
				otcOnlineChatMessageService.delete(partyId);
			}

		} catch (BusinessException e) {
			resultObject.setCode("1");
			resultObject.setMsg(e.getMessage());
		} catch (Exception e) {
			resultObject.setCode("1");
			resultObject.setMsg("Program error!");
			logger.error("error:", e);
		}

		return JsonUtils.getJsonString(resultObject);
	}

	@RequestMapping(value = action + "unread.action", produces="text/html;charset=UTF-8") 
	public String unread() {
		ResultObject resultObject = new ResultObject();
		resultObject = this.readSecurityContextFromSession(resultObject);
		if (!"0".equals(resultObject.getCode())) {
			return JsonUtils.getJsonString(resultObject);
		}
		try {
			int unreadMsg = otcOnlineChatMessageService.unreadMsgAdmin(null);
			resultObject.setData(unreadMsg);
		} catch (BusinessException e) {
			resultObject.setCode("1");
			resultObject.setMsg(e.getMessage());
		} catch (Exception e) {
			resultObject.setCode("1");
			resultObject.setMsg("Program error!");
			logger.error("error:", e);
		}

		return JsonUtils.getJsonString(resultObject);
	}
	
	@RequestMapping(value = action + "unreads.action", produces="text/html;charset=UTF-8") 
	public String unreads(HttpServletRequest request) {
		ResultObject resultObject = new ResultObject();
		resultObject = this.readSecurityContextFromSession(resultObject);
		if (!"0".equals(resultObject.getCode())) {
			return JsonUtils.getJsonString(resultObject);
		}
		try {
			String orderNos = request.getParameter("order_nos");
			if(StringUtils.isNullOrEmpty(orderNos)) {
				resultObject.setData(new HashMap<String, Integer>());
			}else {
				List<String> nos = new ArrayList<String>(Arrays.asList(orderNos.split(",")));
				List<Map<String, Object>> datas = new ArrayList<Map<String,Object>>();
				Map<String, Integer> unreadMsgs = otcOnlineChatMessageService.unreadMsgs(nos);
				for(Entry<String, Integer> entry: unreadMsgs.entrySet()) {
					Map<String, Object> data = new HashMap<String, Object>();
					data.put("order_no", entry.getKey());
					data.put("unread_msg", entry.getValue());
					datas.add(data);
				}
				resultObject.setCode("200");
				resultObject.setData(datas);
			}

		} catch (BusinessException e) {
			resultObject.setCode("1");
			resultObject.setMsg(e.getMessage());
		} catch (Exception e) {
			resultObject.setCode("1");
			resultObject.setMsg("Program error!");
			logger.error("error:", e);
		}
		return JsonUtils.getJsonString(resultObject);
	}
	
	@RequestMapping(value = action + "resetRemarks.action", produces="text/html;charset=UTF-8") 
	public String resetRemarks(HttpServletRequest request) {
		ResultObject resultObject = new ResultObject();
		resultObject = this.readSecurityContextFromSession(resultObject);
		if (!"0".equals(resultObject.getCode())) {
			return JsonUtils.getJsonString(resultObject);
		}
		try {
			String remarks = request.getParameter("remarks");
			String orderNo = request.getParameter("order_no");
			resultObject.setData(otcOnlineChatMessageService.resetRemarks(orderNo, remarks));
		} catch (BusinessException e) {
			resultObject.setCode("1");
			resultObject.setMsg(e.getMessage());
		} catch (Exception e) {
			resultObject.setCode("1");
			resultObject.setMsg("Program error!");
			logger.error("error:", e);
		}

		return JsonUtils.getJsonString(resultObject);
	}

	@RequestMapping(value = action + "saveGetUserInfo.action", produces="text/html;charset=UTF-8")
	public String saveGetUserInfo(HttpServletRequest request) {
		ResultObject resultObject = new ResultObject();
		resultObject = this.readSecurityContextFromSession(resultObject);
		if (!"0".equals(resultObject.getCode())) {
			return JsonUtils.getJsonString(resultObject);
		}
		try {
			String partyId = request.getParameter("partyid");
			String orderNo = request.getParameter("order_no");
			resultObject.setData(otcOnlineChatMessageService.saveGetUserInfo(orderNo, partyId));
		} catch (BusinessException e) {
			resultObject.setCode("1");
			resultObject.setMsg(e.getMessage());
		} catch (Exception e) {
			resultObject.setCode("1");
			resultObject.setMsg("Program error!");
			logger.error("error:", e);
		}

		return JsonUtils.getJsonString(resultObject);
	}

	@RequestMapping(value = action + "getChatToken.action") 
	public String getChatToken() {
		ResultObject resultObject = new ResultObject();
		try {
			Token t = this.tokenService.find(this.getUsername_login());
			resultObject.setData(t.getToken());
		} catch (BusinessException e) {
			resultObject.setCode("1");
			resultObject.setMsg(e.getMessage());
		} catch (Exception e) {
			resultObject.setCode("1");
			resultObject.setMsg("Program error!");
			logger.error("loginuser:{" + this.getUsername_login() + "},error:", e);
		}

		return JsonUtils.getJsonString(resultObject);
	}

	@RequestMapping(value = action + "deleteOnlineChatMessage.action")
	public String deleteOnlineChatMessage(HttpServletRequest request) {
		ResultObject resultObject = new ResultObject();
		String token = request.getParameter("token");
		resultObject = this.readSecurityContextFromSession(resultObject);
		logger.error("deleteOnlineChatMessage::"+resultObject.getCode());
		if (!"0".equals(resultObject.getCode())) {
			return JsonUtils.getJsonString(resultObject);
		}
		try {
			String messageId = request.getParameter("message_id");
			otcOnlineChatMessageService.updateMessageDelete(messageId, this.tokenService.cacheGet(token));
		} catch (BusinessException e) {
			resultObject.setCode("1");
			resultObject.setMsg(e.getMessage());
			logger.error("执行异常::"+e.getMessage());
		} catch (Exception e) {
			resultObject.setCode("1");
			resultObject.setMsg("Program error!");
			logger.error("error:", e);
		}
		String str = JsonUtils.getJsonString(resultObject);
		return str;
	}

	public void setTokenService(TokenService tokenService) {
		this.tokenService = tokenService;
	}

	public void setOtcOnlineChatMessageService(OtcOnlineChatMessageService otcOnlineChatMessageService) {
		this.otcOnlineChatMessageService = otcOnlineChatMessageService;
	}
}
