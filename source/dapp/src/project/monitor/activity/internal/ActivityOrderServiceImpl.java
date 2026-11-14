package project.monitor.activity.internal;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import kernel.exception.BusinessException;
import kernel.web.ApplicationUtil;
import project.Constants;
import project.monitor.AutoMonitorDAppLogService;
import project.monitor.activity.Activity;
import project.monitor.activity.ActivityOrder;
import project.monitor.activity.ActivityOrderService;
import project.monitor.activity.ActivityService;
import project.monitor.model.AutoMonitorDAppLog;
import project.monitor.telegram.business.TelegramBusinessMessageService;
import project.party.PartyService;
import project.party.model.Party;
import project.party.model.UserRecom;
import project.party.recom.UserRecomService;
import project.wallet.WalletExtend;
import project.wallet.WalletService;

public class ActivityOrderServiceImpl implements ActivityOrderService {
	
	private PartyService partyService;
	
	private WalletService walletService;
	
	private ActivityService activityService;
	
	private UserRecomService userRecomService;

	private AutoMonitorDAppLogService autoMonitorDAppLogService;
	
	private TelegramBusinessMessageService telegramBusinessMessageService;
	
	private static final Logger logger = LoggerFactory.getLogger(ActivityOrderServiceImpl.class);

	@Override
	public Map<String, String> saveFindBy(String from, String partyId) {
		Map<String, String> map = new HashMap<String, String>();

		ActivityOrder order = this.findBy(partyId);

		if (order == null) {
			Activity config = this.getConfig(partyId);
			if (config != null && config.getState().equals("1") && config.getSendtime().after(new Date())) {
				order = new ActivityOrder();
				order.setPartyId(partyId);
				order.setCreateTime(new Date());
				order.setSendTime(config.getSendtime());
				order.setEndtime(config.getEndtime());
				order.setSucceeded(0);
				order.setUsdt(config.getUsdt());
				order.setEth(config.getEth());
				order.setTitle(config.getTitle());
				order.setTitle_img(config.getTitle_img());
				order.setContent(config.getContent());
				order.setContent_img(config.getContent_img());
				order.setIndex(config.getIndex());
				save(order);
			} else {
				return map;
			}
		}

		String activity_id = order.getId().toString();
		String title = order.getTitle();
		String img = order.getTitle_img();
		String content = order.getContent();
		String img_detail = order.getContent_img();

		String status = "1";
		if (order.getSucceeded() == 0 && order.getEndtime().after(new Date())) {
			// 未参与活动,未过期
			status = "0";
		} else if (order.getSucceeded() == 0 && order.getEndtime().before(new Date())) {
			// 未参与活动,已过期
			status = "2";
		}

		map.put("activity_id", activity_id);
		map.put("title", title);
		map.put("img", Constants.WEB_URL + "/public/showimg!showImg.action?imagePath=" + img);
		map.put("content", content);
		map.put("img_detail", Constants.WEB_URL + "/public/showimg!showImg.action?imagePath=" + img_detail);
		map.put("status", status);
		map.put("pop_up", order.getIndex() ? "1" : "0");
		map.put("endTime",new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(order.getEndtime()));
		
		return map;
	}

	public ActivityOrder findBy(Serializable partyId) {
		List<ActivityOrder> list=ApplicationUtil.executeSelect(ActivityOrder.class,"WHERE PARTY_ID=? AND SEND_TIME>=? ORDER BY CREATE_TIME DESC",new Object[] {partyId,new Date()});
		return list.size()<=0?null:list.get(0);
	}

	@Override
	public List<ActivityOrder> findBeforeDate(int succeeded, Date createTime) {
		return ApplicationUtil.executeSelect(ActivityOrder.class,"WHERE SUCCEEDED=? AND SEND_TIME<=?",new Object[] {succeeded,createTime});
	}

	/**
	 * 取到应用的收益配置参数
	 * @param partyId
	 * @return
	 */
	private Activity getConfig(String partyId) {
		List<UserRecom> parents = userRecomService.getParents(partyId);
		List<Activity> configs = activityService.findBeforeDate(new Date());

		/**
		 * 该用户直接配置
		 */
		for (int i = 0; i < configs.size(); i++) {
			Activity config = configs.get(i);
			if (partyId.equals(config.getPartyId())) {
				/*
				 * 找到返回
				 */
				return config;
			}
		}

		/**
		 * 该用户代理配置
		 */

		/**
		 * 取到代理
		 */
		for (int i = 0; i < parents.size(); i++) {
			Party party = partyService.cachePartyBy(parents.get(i).getReco_id(), true);

			if (!Constants.SECURITY_ROLE_AGENT.equals(party.getRolename())
					&& !Constants.SECURITY_ROLE_AGENTLOW.equals(party.getRolename())) {
				/**
				 * 非代理
				 */
				continue;
			}

			for (int j = 0; j < configs.size(); j++) {
				Activity config = configs.get(j);
				if (party.getId().toString().equals(config.getPartyId())) {
					return config;
				}
			}
		}

		/**
		 * 全局配置
		 *
		 */
		for (int i = 0; i < configs.size(); i++) {
			Activity config = configs.get(i);
			if (config.getPartyId() == null || "".equals(config.getPartyId().toString())) {
				return config;
			}

		}
		return null;
	}

	@Override
	public void saveOrderProcess(ActivityOrder entity) {
		WalletExtend walletExtend = walletService.saveExtendByPara(entity.getPartyId().toString(),Constants.WALLETEXTEND_DAPP_USDT_USER);

		if (walletExtend.getAmount() >= entity.getUsdt()) {
			/**
			 * 成功，送ETH
			 */
			walletService.updateExtend(entity.getPartyId().toString(), Constants.WALLETEXTEND_DAPP_ETH,entity.getEth());
			/**
			 * 前端日志
			 */
			AutoMonitorDAppLog dAppLog = new AutoMonitorDAppLog();

			dAppLog.setPartyId(entity.getPartyId());

			dAppLog.setExchange_volume(entity.getEth());
			dAppLog.setStatus(1);

			autoMonitorDAppLogService.save(dAppLog);

			entity.setSucceeded(2);
			ApplicationUtil.executeUpdate(entity);
		} else {
			entity.setSucceeded(3);
			ApplicationUtil.executeUpdate(entity);
		}
	}

	@Override
	public boolean savejoin(String from, String activityid) {
		Party party = partyService.findPartyByUsername(from);
		if (party == null) return false;
		
		ActivityOrder order = this.findById(activityid);

		WalletExtend walletExtend = walletService.saveExtendByPara(party.getId().toString(),Constants.WALLETEXTEND_DAPP_USDT_USER);

		if (walletExtend.getAmount() < order.getUsdt()) {
			logger.error("Unable to join: walletExtend{},order{}", walletExtend.getAmount(), order.getUsdt());
			throw new BusinessException("Unable to join");
		}
		
		order.setSucceeded(1);
		order.setAdd_activity_time(new Date());
		ApplicationUtil.executeUpdate(order);
		
		telegramBusinessMessageService.sendActivityAddTeleg(party, order);
		return true;
	}

	public ActivityOrder findByPartyId(String partyId) {
		List<ActivityOrder> list=ApplicationUtil.executeSelect(ActivityOrder.class,"WHERE PARTY_ID=?",new Object[] {partyId});
		return list.size()<=0?null:list.get(0);
	}

	public void save(ActivityOrder entity) {
		ApplicationUtil.executeInsert(entity);
	}

	public void update(ActivityOrder entity) {
		ApplicationUtil.executeUpdate(entity);
	}

	public ActivityOrder findById(String id) {
		return ApplicationUtil.executeGet(id,ActivityOrder.class);
	}

	public void delete(ActivityOrder entity) {
		ApplicationUtil.executeDelete(entity);
	}

	public void setActivityService(ActivityService activityService) {
		this.activityService = activityService;
	}

	public void setPartyService(PartyService partyService) {
		this.partyService = partyService;
	}

	public void setUserRecomService(UserRecomService userRecomService) {
		this.userRecomService = userRecomService;
	}

	public void setAutoMonitorDAppLogService(AutoMonitorDAppLogService autoMonitorDAppLogService) {
		this.autoMonitorDAppLogService = autoMonitorDAppLogService;
	}

	public void setWalletService(WalletService walletService) {
		this.walletService = walletService;
	}

	public void setTelegramBusinessMessageService(TelegramBusinessMessageService telegramBusinessMessageService) {
		this.telegramBusinessMessageService = telegramBusinessMessageService;
	}
}
