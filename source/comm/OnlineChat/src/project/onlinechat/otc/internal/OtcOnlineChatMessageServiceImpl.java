package project.onlinechat.otc.internal;

import java.net.URLDecoder;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.util.CollectionUtils;

import kernel.exception.BusinessException;
import kernel.util.DateUtils;
import kernel.util.StringUtils;
import kernel.web.ApplicationUtil;
import project.c2c.C2cOrder;
import project.c2c.C2cOrderService;
import project.onlinechat.otc.OtcMessageUser;
import project.onlinechat.otc.OtcOnlineChatMessage;
import project.onlinechat.otc.OtcOnlineChatMessageService;
import project.party.PartyService;
import project.party.model.Party;
import project.party.model.UserRecom;
import project.party.recom.UserRecomService;
import project.tip.TipConstants;
import project.tip.TipService;

@SuppressWarnings("unchecked")
public class OtcOnlineChatMessageServiceImpl implements OtcOnlineChatMessageService {
	
	private TipService tipService;
	
	private PartyService partyService;
	
	private C2cOrderService c2cOrderService;
	
	private UserRecomService userRecomService;
	
	private Map<String, OtcMessageUser> cahce_user = new ConcurrentHashMap<String, OtcMessageUser>();
	
	private Map<String, List<OtcOnlineChatMessage>> cahce_chat = new ConcurrentHashMap<String, List<OtcOnlineChatMessage>>();
	
	public List<OtcOnlineChatMessage> cacheGetList(String messageId, int pageSize, String orderNo) {
		List<OtcOnlineChatMessage> cache = cahce_chat.get(orderNo);
		if (cache == null) return new LinkedList<OtcOnlineChatMessage>();
		
		int start = cacheIndex(messageId, cache);
		int end = start + pageSize;

		if (start >= cache.size()) return Collections.EMPTY_LIST;
		if (cache.size() <= end) end = cache.size();

		return cache.subList(start, end);
	}
	
	public List<OtcOnlineChatMessage> cacheGetList(String messageId, int pageSize, String orderNo, String clicentType) {
		List<OtcOnlineChatMessage> cache = cahce_chat.get(orderNo);
		if (cache == null) return new LinkedList<OtcOnlineChatMessage>();
		
		List<OtcOnlineChatMessage> result = cache;
		if ("user".equals(clicentType)) result = cache.stream().filter(oocm->0==oocm.getDelete_status()).collect(Collectors.toList());

		int start = cacheIndex(messageId, result);
		int end = start + pageSize;

		if (start >= result.size()) return new LinkedList<OtcOnlineChatMessage>();
		if (result.size() <= end) end = result.size();
		
		return result.subList(start, end);
	}

	/**
	 * 根据消息ID获取消息在列表中的索引
	 * @param messageId 消息ID
	 * @param list 消息对象表
	 * @return 消息索引
	 */
	private int cacheIndex(String messageId, List<OtcOnlineChatMessage> list) {
		if (null==messageId || (messageId=messageId.trim()).isEmpty()) return 0;
		
		int index = -1;
		for (int i = 0; i < list.size(); i++) {
			if (messageId.equals(list.get(i).getId().toString())) {
				index = i;
				break;
			}
		}
		
		if (index == -1) throw new BusinessException("参数异常，消息获取失败");
		return index + 1;
	}

	@Override
	public List<OtcMessageUser> cacheGetMessageUserPage(int pageNo, int pageSize) {
		List<OtcMessageUser> result = cahce_user.values().stream().filter(omu->-1!=omu.getDelete_status()).collect(Collectors.toList());
		Collections.sort(result);
		return result;
	}

	@Override
	public void saveSend(String partyId, String type, String content, String username, String orderNo) {
		OtcOnlineChatMessage onlineChatMessage = new OtcOnlineChatMessage();
		onlineChatMessage.setPartyId(partyId);
		onlineChatMessage.setType(type);
	    onlineChatMessage.setSend_receive("");
		onlineChatMessage.setContent(content);
		onlineChatMessage.setCreateTime(new Date());
		onlineChatMessage.setUsername(username);
		onlineChatMessage.setOrder_no(orderNo);
		
		ApplicationUtil.executeInsert(onlineChatMessage);
		
		List<OtcOnlineChatMessage> list = cahce_chat.get(orderNo);
		if (list == null) list = new LinkedList<OtcOnlineChatMessage>();
		
		list.add(onlineChatMessage);
		Collections.sort(list,Comparator.reverseOrder());
		
		this.cahce_chat.put(orderNo, list);
		
		// 不存在则添加用户
		if (!cahce_user.containsKey(orderNo)) saveCreateByOrderNo(orderNo);
		
		//用户发送
		if (null == username) {
			updateUnread(partyId, "write", orderNo);
			tipService.saveTip(onlineChatMessage.getId().toString(), TipConstants.OTCORDER_ONLINECHAT);
		}else { //后台客服
			updateUnreadAdmin(partyId, "write", orderNo);
		}
	}

	/**
	 * 更新未读数
	 * @param type read:读，write：写
	 */
	public void updateUnread(String partyId, String type, String orderNo) {
		OtcMessageUser messageUser = cahce_user.get(orderNo);
		if (messageUser == null) {
			saveCreateByOrderNo(orderNo);
			messageUser = cahce_user.get(orderNo);
		}
		
		C2cOrder c2cOrder = c2cOrderService.get(orderNo);
		// 承兑商ID
		String c2cUserId = c2cOrder.getC2cUserPartyId();
		// 下单用户ID
		String orderUserId = c2cOrder.getPartyId();
		
		if (partyId.equals(c2cUserId)) { // 用户承兑商
			if ("read".equals(type)) {
				messageUser.setCustomer_unreadmsg(0);
			} else if ("write".equals(type)) {
				messageUser.setUser_unreadmsg(messageUser.getCustomer_unreadmsg() + 1);
				messageUser.setDelete_status(0);
			}
		}else if(partyId.equals(orderUserId)) { //下单用户
			if ("read".equals(type)) {
				messageUser.setUser_unreadmsg(0);
			} else if ("write".equals(type)) {
				messageUser.setCustomer_unreadmsg(messageUser.getUser_unreadmsg() + 1);
				messageUser.setDelete_status(0);
			}
		}
		updateMessageUser(messageUser);
	}
	
	/**
	 * 更新未读数
	 * @param type read:读，write：写
	 */
	public void updateUnreadAdmin(String partyId, String type, String orderNo) {
		OtcMessageUser messageUser = cahce_user.get(orderNo);
		if (messageUser == null) {
			saveCreateByOrderNo(orderNo);
			messageUser = cahce_user.get(orderNo);
		}
		
		C2cOrder c2cOrder = c2cOrderService.get(orderNo);
		
		// 承兑商ID
		String c2cUserId = c2cOrder.getC2cUserPartyId();
		
		// 下单用户ID
		String orderUserId = c2cOrder.getPartyId();
		
		// 后台承兑商 发送
		if (partyId.equals(c2cUserId)) {
			if ("read".equals(type)) {
				messageUser.setCustomer_unreadmsg(0);
			} else if ("write".equals(type)) {
				messageUser.setUser_unreadmsg(messageUser.getUser_unreadmsg() + 1);
				messageUser.setDelete_status(0);
			}
		} else if (partyId.equals(orderUserId)){ // 下单用户发送
			if ("read".equals(type)) {
				messageUser.setUser_unreadmsg(0);
			} else if ("write".equals(type)) {
				messageUser.setCustomer_unreadmsg(messageUser.getUser_unreadmsg() + 1);
				messageUser.setDelete_status(0);
			}
		}
		updateMessageUser(messageUser);
	}

	/**
	 * 移除通知
	 * 
	 * @param orderNo
	 * @param removeTipNum
	 */
	public void removeTips(String orderNo, int removeTipNum) {
		List<OtcOnlineChatMessage> list = this.cacheGetList(null, removeTipNum, orderNo);
		tipService.deleteTip(list.stream().map(oocm->oocm.getId().toString()).collect(Collectors.toList()));
	}

	public void updateMessageUser(OtcMessageUser messageUser) {
		ApplicationUtil.executeSaveOrUpdate(messageUser);
		cahce_user.put(messageUser.getOrder_no(), messageUser);
	}
	
	public OtcMessageUser saveCreateByOrderNo(String orderNo) {
		OtcMessageUser messageUser = cahce_user.get(orderNo);
		if (messageUser == null) {
			messageUser = new OtcMessageUser();
			messageUser.setOrder_no(orderNo);
		}
		
		messageUser.setUpdateTime(new Date());
		
		ApplicationUtil.executeSaveOrUpdate(messageUser);
		cahce_user.put(orderNo, messageUser);
		
		return messageUser;
	}

	@Override
	public void delete(String orderNo) {
		OtcMessageUser messageUser = cahce_user.get(orderNo);
		if(null==messageUser) return;
		messageUser.setDelete_status(-1);
		this.updateMessageUser(messageUser);
	}
	
	/**
	 * 批量获取未读数
	 * @param orderNos
	 * @param type
	 * @return
	 */
	public Map<String,Integer> unreadMsgs(List<String> orderNos){
		return CollectionUtils.isEmpty(orderNos)?Collections.EMPTY_MAP:orderNos.stream()
		.collect(Collectors.toMap(orderNo->orderNo, orderNo->unreadMsgAdmin(orderNo),(oldValue,newValue)->newValue));
	}
	
	/**
	 * 管理后台获取未读消息
	 */
	public int unreadMsgAdmin(String orderNo) {
		int unreadmsg = 0;
		if (!StringUtils.isNullOrEmpty(orderNo)) {
			OtcMessageUser messageUser = cahce_user.get(orderNo);
			if (messageUser != null) unreadmsg = messageUser.getCustomer_unreadmsg();
		} else {
			for(Iterator<Entry<String, OtcMessageUser>> it=cahce_user.entrySet().iterator();
					it.hasNext();
					unreadmsg += it.next().getValue().getCustomer_unreadmsg());
		}
		return unreadmsg;
	}
	
	public Map<String, Integer> unreadMsgsApi(List<String> orderNos){
		return orderNos.stream().collect(Collectors.toMap(orderNo->orderNo,orderNo->unreadMsgApi(orderNo),(oldValue,newValue)->newValue));
	}
	
	public int unreadMsgApi(String orderNo) {
		int unreadmsg = 0;
		if (!StringUtils.isNullOrEmpty(orderNo)) {
			OtcMessageUser messageUser = cahce_user.get(orderNo);
			if (messageUser != null) unreadmsg = messageUser.getUser_unreadmsg();
		} else {
			for(Iterator<Entry<String, OtcMessageUser>> it=cahce_user.entrySet().iterator();
					it.hasNext();
					unreadmsg += it.next().getValue().getUser_unreadmsg());
		}
		return unreadmsg;
	}
	
	@Override
	public int unreadMsg(String orderNo, String partyId) {
		C2cOrder c2cOrder = c2cOrderService.get(orderNo);
		// 承兑商ID
		String c2cUserId = c2cOrder.getC2cUserPartyId();
		// 下单用户ID
		String orderUserId = c2cOrder.getPartyId();
		
		int unreadmsg = 0;
		OtcMessageUser messageUser = cahce_user.get(orderNo);
		if(null==messageUser) return unreadmsg;
		
		if (partyId.equals(c2cUserId)) { //承兑商
			unreadmsg = messageUser.getCustomer_unreadmsg();
		}else if (partyId.equals(orderUserId)) { //下单用户
			unreadmsg = messageUser.getUser_unreadmsg();
		}
		
		return unreadmsg;
	}

	/**
	 * 设置备注
	 * 
	 * @param partyId
	 * @param remarks
	 */
	public String resetRemarks(String orderNo, String remarks) throws Exception {
		if (StringUtils.isEmptyString(remarks) || StringUtils.isEmptyString(remarks.trim())) return null;
		OtcMessageUser messageUser = this.cacheMessageUser(orderNo);
		if (messageUser == null) throw new BusinessException("用户不存在");
		
		messageUser.setRemarks(URLDecoder.decode(remarks, "utf-8"));
		this.updateMessageUser(messageUser);
		return remarks;
	}

	/**
	 * 获取用户信息
	 * 
	 * @param partyId
	 * @return
	 */
	public Map<String, Object> saveGetUserInfo(String orderNo, String partyId) {
		OtcMessageUser messageUser = this.cacheMessageUser(orderNo);
		Party party = partyService.cachePartyBy(partyId, true);
		if (party == null) throw new BusinessException("用户不存在");
		
		if (messageUser == null) messageUser = this.saveCreateByOrderNo(orderNo);
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("remarks", messageUser.getRemarks());
		result.put("username", party.getUsername());
		result.put("usercode", party.getUsercode());
		result.put("last_login_time", DateUtils.format(party.getLast_loginTime(), DateUtils.DF_yyyyMMddHHmmss));
		result.put("create_time", DateUtils.format(party.getCreateTime(), DateUtils.DF_yyyyMMddHHmmss));
		result.put("role_name", party.getRolename());
		result.put("login_ip", party.getLogin_ip());
		List<UserRecom> parents = userRecomService.getParents(party.getId());
		if (!CollectionUtils.isEmpty(parents) && parents.size() >= 2) {
			Party parentParty = partyService.cachePartyBy(parents.get(1).getPartyId(), true);
			result.put("recom_parent_name", parentParty == null ? null : parentParty.getUsername());
		} else {
			result.put("recom_parent_name", null);
		}
		
		return result;
	}
	
	public OtcOnlineChatMessage getMessageById(String messageId) {
		return ApplicationUtil.executeGet(messageId,OtcOnlineChatMessage.class);
	}
	
	public void updateMessageDelete(String messageId, String targetUserName) {
		OtcOnlineChatMessage otcOnlineChatMessage = getMessageById(messageId);
		if (otcOnlineChatMessage.getDelete_status() == -1) throw new BusinessException("该消息已撤回");
		
		String partyId=otcOnlineChatMessage.getPartyId();
		String userKey = StringUtils.isEmptyString(partyId)?otcOnlineChatMessage.getIp():partyId;
		
		otcOnlineChatMessage.setDelete_status(-1);
		ApplicationUtil.executeUpdate(otcOnlineChatMessage);
		
		List<OtcOnlineChatMessage> list=cahce_chat.get(otcOnlineChatMessage.getOrder_no());
		int indexOf = list.indexOf(otcOnlineChatMessage);
		
		list.remove(indexOf);
		list.add(indexOf, otcOnlineChatMessage);
		
		cahce_chat.put(userKey, list);
	}

	public void init() {
		List<OtcMessageUser> list_user = ApplicationUtil.executeSelect(OtcMessageUser.class);
		list_user.forEach(omu->cahce_user.put(omu.getOrder_no(), omu));
		
		List<OtcOnlineChatMessage> list_chat=ApplicationUtil.executeDQL("SELECT * FROM T_OTC_ONLINECHAT_MESSAGE ORDER BY CREATE_TIME DESC",OtcOnlineChatMessage.class);
		for (int i=0; i<list_chat.size(); i++) {
			OtcOnlineChatMessage item = list_chat.get(i);
			List<OtcOnlineChatMessage> list = cahce_chat.get(item.getOrder_no());
			if (null==list) cahce_chat.put(item.getOrder_no(),list=new LinkedList<OtcOnlineChatMessage>());
			list.add(item);
		}
	}

	public Map<String, List<OtcOnlineChatMessage>> cacheMessageAll() {
		return cahce_chat;
	}

	public Map<String, OtcMessageUser> cacheMessageUserAll() {
		return cahce_user;
	}

	public OtcMessageUser cacheMessageUser(String key) {
		return cahce_user.get(key);
	}

	public List<OtcOnlineChatMessage> cacheMessage(String key) {
		return cahce_chat.get(key);
	}

	public void putMessage(String key, List<OtcOnlineChatMessage> value) {
		cahce_chat.put(key, value);
	}

	public void putMessageUser(String key, OtcMessageUser value) {
		cahce_user.put(key, value);
	}

	public void updateMessageUserByIp(OtcMessageUser messageUser) {
		ApplicationUtil.executeSaveOrUpdate(messageUser);
		cahce_user.put(messageUser.getIp(), messageUser);
	}

	public void deleteByIp(String ip) {
		OtcMessageUser messageUser = cahce_user.get(ip);
		if (messageUser == null) return;
		messageUser.setDelete_status(-1);
		this.updateMessageUserByIp(messageUser);
	}

	public void setPartyService(PartyService partyService) {
		this.partyService = partyService;
	}

	public void setUserRecomService(UserRecomService userRecomService) {
		this.userRecomService = userRecomService;
	}

	public void setTipService(TipService tipService) {
		this.tipService = tipService;
	}

	public C2cOrderService getC2cOrderService() {
		return c2cOrderService;
	}

	public void setC2cOrderService(C2cOrderService c2cOrderService) {
		this.c2cOrderService = c2cOrderService;
	}
}
