package project.onlinechat.internal;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.apache.commons.collections.Predicate;
import org.springframework.util.CollectionUtils;

import kernel.exception.BusinessException;
import kernel.util.DateUtils;
import kernel.util.StringUtils;
import kernel.web.ApplicationUtil;
import project.onlinechat.MessageUser;
import project.onlinechat.OnlineChatMessage;
import project.onlinechat.OnlineChatMessageService;
import project.party.PartyService;
import project.party.model.Party;
import project.party.model.UserRecom;
import project.party.recom.UserRecomService;
import project.tip.TipConstants;
import project.tip.TipService;
import project.tip.model.Tip;
import systemuser.CustomerService;
import systemuser.model.Customer;

public class OnlineChatMessageServiceImpl implements OnlineChatMessageService {
	
	private TipService tipService;
	
	private PartyService partyService;
	
	private CustomerService customerService;
	
	private UserRecomService userRecomService;

	protected static final Object OnlineChatMessage = null;
	
	private Map<String, MessageUser> cahce_user = new ConcurrentHashMap<String, MessageUser>();
	
	private Map<String, List<OnlineChatMessage>> cahce_chat = new ConcurrentHashMap<String, List<OnlineChatMessage>>();

	/**
	 * @param messageId
	 * @param pageSize
	 * @param partyId
	 * @param clicentType 请求的客户端类型，用户端user，客服端 不传
	 * @return
	 */
	public List<OnlineChatMessage> cacheGetList(String messageId, int pageSize, String partyId, String... clicentType) {
		List<OnlineChatMessage> cache = cahce_chat.get(partyId);
		if (cache == null) return new LinkedList<OnlineChatMessage>();
		
		List<OnlineChatMessage> result = new ArrayList<OnlineChatMessage>();
		result.addAll(cache);

		int start = cacheIndex(messageId, result);
		int end = start + pageSize;

		if (start >= result.size()) return new LinkedList<OnlineChatMessage>();
		
		if (cache.size() <= end) end = result.size();
		
		return result.subList(start, end);
	}

	/**
	 * 获取消息的索引
	 * 
	 * @param messageId
	 * @param list
	 * @return
	 */
	private int cacheIndex(String messageId, List<OnlineChatMessage> list) {
		if (StringUtils.isEmptyString(messageId)) return 0;
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
	public List<MessageUser> cacheGetMessageUserPage(int pageNo, int pageSize, String username) {
		List<MessageUser> list = new ArrayList<MessageUser>(cahce_user.values());
		List<MessageUser> result = new ArrayList<MessageUser>();
		for (MessageUser user : list) {
			if (user.getDelete_status() == -1) continue;
			if (StringUtils.isEmptyString(user.getTarget_username()) || !username.equals(user.getTarget_username())) continue;
			result.add(user);
		}
		
		Collections.sort(result);
		return result;
	}

	@Override
	public OnlineChatMessage saveSend(String partyId, String type, String send_receive, String content,String username) {
		OnlineChatMessage onlineChatMessage = new OnlineChatMessage();
		onlineChatMessage.setPartyId(partyId);
		onlineChatMessage.setType(type);
		onlineChatMessage.setSend_receive(send_receive);
		onlineChatMessage.setContent(content);
		onlineChatMessage.setCreateTime(new Date());
		onlineChatMessage.setUsername(username);

		//存库
		ApplicationUtil.executeInsert(onlineChatMessage);
		
		//更新缓存
		List<OnlineChatMessage> list = cahce_chat.get(partyId);
		if (list == null) cahce_chat.put(partyId,list=new LinkedList<OnlineChatMessage>());
		list.add(onlineChatMessage);
		Collections.sort(list,Collections.reverseOrder());
		
		if (!cahce_user.containsKey(partyId)) saveCreateByPartyId(partyId);
		
		switch (send_receive) {
			case "receive":// 客服发送
				updateUnread(partyId, "user", "write");
				break;
			case "send":// 用户发送
				updateUnread(partyId, "customer", "write");
				break;
		}
		
		return onlineChatMessage;
	}

	public String userSendTarget(String partyId, Date sendTime, String targetUsername) {
		if (StringUtils.isNotEmpty(targetUsername)) {
			Customer customer = customerService.cacheByUsername(targetUsername);
			
			// 表示该用户被有客服权限的系统用户接手
			if (customer == null) return targetUsername;
			
			// 当前在聊的客服是否在线
			if (customer.getOnline_state() == 1) return customer.getUsername();
		}

		// 不在线则重新分配
		Customer customer = this.customerService.cacheOnlineOne();
		if (null == customer) return null;
		
		while (true) {
			customer.setLast_message_user(partyId);
			customer.setLast_customer_time(sendTime);
			boolean update = customerService.update(customer, true);
			if (update) {// 更新成功，退出
				break;
			} else {// 未成功，说明已下线，重新分配新客服
				customer = this.customerService.cacheOnlineOne();
				if (null == customer) return null;
			}
		}
		
		return customer.getUsername();
	}

	/**
	 * 更新未读数
	 * @param partyId
	 * @param user_customer 更新对象，用户，客服
	 * @param type          read:读，write：写
	 */
	public void updateUnread(final String partyId, String user_customer, String type) {
		MessageUser messageUser = cahce_user.get(partyId);
		if (messageUser == null) {
			saveCreateByPartyId(partyId);
			messageUser = cahce_user.get(partyId);
		}
		
		int removeTipNum = 0;
		switch (user_customer) {
		case "user":
			if ("read".equals(type)) {
				messageUser.setUser_unreadmsg(0);
			} else if ("write".equals(type)) {
				messageUser.setUser_unreadmsg(messageUser.getUser_unreadmsg() + 1);
				messageUser.setDelete_status(0);
			}
			break;
		case "customer":
			if ("read".equals(type)) {
				removeTipNum = messageUser.getCustomer_unreadmsg();
				messageUser.setCustomer_unreadmsg(0);
			} else if ("write".equals(type)) {
				messageUser.setCustomer_unreadmsg(messageUser.getCustomer_unreadmsg() + 1);
				messageUser.setDelete_status(0);
				
				String targetUsername = this.userSendTarget(partyId, new Date(),messageUser.getTarget_username());
				if (StringUtils.isNotEmpty(targetUsername) && !targetUsername.equals(messageUser.getTarget_username())) {
					Customer customer = customerService.cacheByUsername(targetUsername);
					// 客服不存在或者回复内容无效则不回复
					if (customer != null && customer.getAuto_answer() != null && !StringUtils.isEmptyString(customer.getAuto_answer().trim())) {
						// 客服自动回复一条
						saveSend(partyId, "text", "receive", customer.getAuto_answer(),targetUsername + "_SYSTEM");
					}
				}
				
				messageUser.setTarget_username(targetUsername);
				if (StringUtils.isNotEmpty(targetUsername)) {// 指定的在线客服存在，则发起通知
					Tip tip = new Tip();
					tip.setBusiness_id(this.cahce_chat.get(partyId).get(0).getId().toString());
					tip.setModel(TipConstants.ONLINECHAT);
					tip.setTarget_username(targetUsername);
					tipService.saveTip(tip);
				}
			}
			break;
		}
		
		updateMessageUser(messageUser);
		if (removeTipNum > 0) removeTips(messageUser.getPartyId(), removeTipNum);
	}

	/**
	 * 移除通知
	 * @param partyId
	 * @param removeTipNum
	 */
	public void removeTips(String partyId, int removeTipNum) {
		List<OnlineChatMessage> list = this.cacheGetList(null, removeTipNum, partyId);
		tipService.deleteTip(list.stream().map(m->m.getId().toString()).collect(Collectors.toList()));
	}

	public void updateMessageUser(MessageUser messageUser) {
		ApplicationUtil.executeSaveOrUpdate(messageUser);
		cahce_user.put(messageUser.getPartyId(), messageUser);
	}

	public void saveCreateByPartyId(String partyId) {
		Party party = partyService.cachePartyBy(partyId, true);
		if (party == null) throw new BusinessException("无效的UID");
		
		MessageUser messageUser = cahce_user.get(party.getId().toString());
		if (null==messageUser) cahce_user.put(party.getId().toString(),messageUser=new MessageUser());
		messageUser.setPartyId(party.getId().toString());
		messageUser.setUpdateTime(new Date());
		
		ApplicationUtil.executeSaveOrUpdate(messageUser);
	}

	@Override
	public MessageUser saveCreate(String uid, String username) {
		Party party = partyService.findPartyByUsercode(uid);
		if (party == null) {
			party = partyService.findPartyByUsername(uid);
			if (party == null) throw new BusinessException("用户不存在");
		}
		
		MessageUser messageUser = cahce_user.get(party.getId().toString());
		if (messageUser == null) cahce_user.put(party.getId().toString(),messageUser=new MessageUser());
		
		messageUser.setDelete_status(0);
		messageUser.setUpdateTime(new Date());
		messageUser.setTarget_username(username);
		messageUser.setPartyId(party.getId().toString());
		
		ApplicationUtil.executeSaveOrUpdate(messageUser);
		
		return messageUser;
	}

	@Override
	public void delete(String partyId) {
		MessageUser messageUser = cahce_user.get(partyId);
		if (messageUser == null) return;
		
		messageUser.setDelete_status(-1);
		messageUser.setTarget_username(null);
		
		this.updateMessageUser(messageUser);
	}

	@Override
	public int unreadMsg(String partyId, String type, String targetUsername) {
		int unreadmsg = 0;
		if (!StringUtils.isNullOrEmpty(partyId)) {
			MessageUser messageUser = cahce_user.get(partyId);
			if (messageUser != null) {
				switch (type) {
					case "user":
						unreadmsg = messageUser.getUser_unreadmsg();
						break;
					case "customer":
						unreadmsg = messageUser.getCustomer_unreadmsg();
						break;
				}
			}
		} else {
			for(Iterator<Entry<String, MessageUser>> it=cahce_user.entrySet().iterator();it.hasNext();) {
				Entry<String, MessageUser> entry = it.next();
				if (StringUtils.isEmptyString(targetUsername) || !targetUsername.equals(entry.getValue().getTarget_username())) continue;
				switch (type) {
					case "user":
						unreadmsg += entry.getValue().getUser_unreadmsg();
						break;
					case "customer":
						unreadmsg += entry.getValue().getCustomer_unreadmsg();
						break;
				}
			}
		}

		return unreadmsg;
	}

	/**
	 * 设置备注
	 * @param partyId
	 * @param remarks
	 */
	public String updateResetRemarks(String partyId, String remarks) throws Exception {
		if (StringUtils.isEmptyString(remarks) || StringUtils.isEmptyString(remarks.trim())) return null;
		MessageUser messageUser = this.cacheMessageUser(partyId);
		if (messageUser == null) throw new BusinessException("用户不存在");
		messageUser.setRemarks(URLDecoder.decode(remarks, "utf-8"));
		this.updateMessageUser(messageUser);
		return remarks;
	}

	/**
	 * 获取用户信息
	 * @param partyId
	 * @return
	 */
	public Map<String, Object> getUserInfo(String partyId) {
		Party party = partyService.cachePartyBy(partyId, false);
		if (party == null) throw new BusinessException("用户不存在");
		
		MessageUser messageUser = this.cacheMessageUser(partyId);
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("partyId", partyId);
		result.put("remarks", messageUser.getRemarks());
		result.put("username", party.getUsername());
		result.put("usercode", party.getUsercode());
		result.put("last_login_time", DateUtils.format(party.getLast_loginTime(), DateUtils.DF_yyyyMMddHHmmss));
		result.put("create_time", DateUtils.format(party.getCreateTime(), DateUtils.DF_yyyyMMddHHmmss));
		result.put("role_name", party.getRolename());
		result.put("login_ip", party.getLogin_ip());
		List<UserRecom> parents = userRecomService.getParents(party.getId());
		if (!CollectionUtils.isEmpty(parents) && parents.size() >= 1) {
			Party parentParty = partyService.cachePartyBy(parents.get(0).getReco_id(), true);
			result.put("recom_parent_name", parentParty == null ? null : parentParty.getUsername());
		} else {
			result.put("recom_parent_name", null);
		}
		
		return result;
	}

	public void init() {
		List<MessageUser> list_user = ApplicationUtil.executeSelect(MessageUser.class);
		
		for (int i = 0; i < list_user.size(); i++) {
			MessageUser item = list_user.get(i);
			if (StringUtils.isEmptyString(item.getPartyId())) {
				this.cahce_user.put(item.getIp(), item);
			} else {
				this.cahce_user.put(item.getPartyId(), item);
			}
		}
		
		List<OnlineChatMessage> list_chat = ApplicationUtil.executeDQL("SELECT * FROM T_ONLINECHAT_MESSAGE ORDER BY CREATE_TIME DESC",OnlineChatMessage.class);
		for (int i = 0; i < list_chat.size(); i++) {
			OnlineChatMessage item = list_chat.get(i);
			
			List<OnlineChatMessage> list = null;
			if (StringUtils.isEmptyString(item.getPartyId())) {
				list = cahce_chat.get(item.getIp());
			} else {
				list = cahce_chat.get(item.getPartyId());
			}
			if (list == null) list = new LinkedList<OnlineChatMessage>();
			
			list.add(item);
			if (StringUtils.isEmptyString(item.getPartyId())) {
				this.cahce_chat.put(item.getIp(), list);
			} else {
				this.cahce_chat.put(item.getPartyId(), list);
			}
		}
	}

	public Map<String, List<OnlineChatMessage>> cacheMessageAll() {
		return cahce_chat;
	}

	public Map<String, MessageUser> cacheMessageUserAll() {
		return cahce_user;
	}

	public MessageUser cacheMessageUser(String key) {
		return cahce_user.get(key);
	}

	public List<OnlineChatMessage> cacheMessage(String key) {
		return cahce_chat.get(key);
	}

	public void putMessage(String key, List<OnlineChatMessage> value) {
		cahce_chat.put(key, value);
	}

	public void putMessageUser(String key, MessageUser value) {
		cahce_user.put(key, value);
	}

	public void updateMessageUserByIp(MessageUser messageUser) {
		ApplicationUtil.executeSaveOrUpdate(messageUser);
		cahce_user.put(messageUser.getIp(), messageUser);
	}

	public void deleteByIp(String ip) {
		MessageUser messageUser = cahce_user.get(ip);
		if(null==messageUser) return;
		
		messageUser.setDelete_status(-1);
		messageUser.setTarget_username(null);
		this.updateMessageUserByIp(messageUser);
	}

	/**
	 * 未分配到客服的用户，分配客服
	 * 
	 * @return
	 */
	public void updateNoAnwserUser(String username) {
		List<MessageUser> users = new ArrayList<MessageUser>(this.cacheMessageUserAll().values());
		org.apache.commons.collections.CollectionUtils.filter(users, new Predicate() {
			@Override
			public boolean evaluate(Object arg0) {
				return ((MessageUser) arg0).getCustomer_unreadmsg()>0 && StringUtils.isEmptyString(((MessageUser) arg0).getTarget_username());
			}
		});
		
		if (CollectionUtils.isEmpty(users)) return;
		
		for (MessageUser user : users) {
			user.setTarget_username(username);
			if (StringUtils.isEmptyString(user.getPartyId())) {
				this.updateMessageUserByIp(user);
			} else {
				this.updateMessageUser(user);
			}
		}
	}

	public OnlineChatMessage getMessageById(String messageId) {
		return  ApplicationUtil.executeGet(messageId, OnlineChatMessage.class);
	}

	public void updateMessageDelete(String messageId, String targetUserName) {
		OnlineChatMessage onlineChatMessage = getMessageById(messageId);
		if (onlineChatMessage.getDelete_status() == -1) throw new BusinessException("该消息已撤回");
		
		//游客或者登录用户
		String userKey = StringUtils.isEmptyString(onlineChatMessage.getPartyId())?onlineChatMessage.getIp():onlineChatMessage.getPartyId();
		MessageUser messageUser = cahce_user.get(userKey);
		if (StringUtils.isEmptyString(messageUser.getTarget_username()) || !targetUserName.equals(messageUser.getTarget_username())) {
			throw new BusinessException("并非当前客服接手的用户，无法撤回");
		}
		
		if (!"receive".equals(onlineChatMessage.getSend_receive())) {
			throw new BusinessException("只能撤回客服发送消息");
		}
		
		onlineChatMessage.setDelete_status(-1);
		ApplicationUtil.executeUpdate(onlineChatMessage);
		
		List<OnlineChatMessage> list = cahce_chat.get(userKey);
		int indexOf = list.indexOf(onlineChatMessage);
		
		list.remove(indexOf);
		list.add(indexOf, onlineChatMessage);
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

	public void setCustomerService(CustomerService customerService) {
		this.customerService = customerService;
	}
}
