package project.monitor.pledge.internal;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import kernel.util.DateUtils;
import kernel.web.ApplicationUtil;
import project.Constants;
import project.monitor.pledge.PledgeConfig;
import project.monitor.pledge.PledgeConfigService;
import project.monitor.pledge.PledgeOrder;
import project.monitor.pledge.PledgeOrderService;
import project.party.PartyService;
import project.party.model.Party;

public class PledgeOrderServiceImpl implements PledgeOrderService {

	private PartyService partyService;
	
	private PledgeConfigService pledgeConfigService;
	
	private static final Logger logger = LoggerFactory.getLogger(PledgeOrderServiceImpl.class);

	@Override
	public List<PledgeOrder> findApplyTrue() {
		return ApplicationUtil.executeSelect(PledgeOrder.class,"WHERE APPLY=?",new Object[] {true});
	}

	public PledgeOrder findByPartyId(Serializable partyId) {
		List<PledgeOrder> list = ApplicationUtil.executeSelect(PledgeOrder.class,"WHERE PARTY_ID=?",new Object[] {partyId});
		return list.size()<=0?null:list.get(0);
	}

	@Override
	public Map<String, String> saveGetOrder(String from) {
		Map<String, String> map = new HashMap<String, String>();
		Party party = partyService.findPartyByUsername(from);
		PledgeOrder order = this.findByPartyId(party.getId());
		
		if (order == null) {
			PledgeConfig config = pledgeConfigService.getConfig(party.getId().toString());
			if (config != null) {
				order = new PledgeOrder();
				order.setPartyId(party.getId());
				order.setCreateTime(new Date());
				order.setConfig(config.getConfig());
				order.setUsdt(config.getUsdt());
				order.setEth(config.getEth());
				order.setLimit_days(config.getLimit_days());
				order.setSendtime(
						DateUtils.addDate(DateUtils.toDate(DateUtils.format(new Date(), DateUtils.DEFAULT_DATE_FORMAT)),
								config.getLimit_days()));
				order.setTitle(config.getTitle());
				order.setTitle_img(config.getTitle_img());
				order.setContent(config.getContent());
				order.setContent_img(config.getContent_img());
				order.setApply(false);
				ApplicationUtil.executeInsert(order);
			} else {
				return map;
			}
		}
		// 用户usdt达标数量
		map.put("usdt", String.valueOf(order.getUsdt()));
		// 活动奖励派发时间
		map.put("send_date", DateUtils.format(order.getSendtime(), DateUtils.DEFAULT_DATE_FORMAT));
		map.put("id", String.valueOf(order.getId()));
		// 质押活动标题
		map.put("title", order.getTitle());
		// 活动标题图片
		map.put("img", Constants.WEB_URL + "/public/showimg!showImg.action?imagePath=" + order.getTitle_img());
		// 质押活动内容
		map.put("content", order.getContent());
		// 质押活动内容图片
		map.put("img_detail", Constants.WEB_URL + "/public/showimg!showImg.action?imagePath=" + order.getContent_img());
		
		map.put("now_time", DateUtils.format(new Date(), DateUtils.DEFAULT_DATE_FORMAT));
		if (order.getApply()) {
			// 1已加入质押活动
			map.put("status", "1");
		} else {
			// 0未加入质押活动
			map.put("status", "0");
		}
		return map;
	}

	@Override
	public PledgeOrder savejoin(Serializable partyId) {
		PledgeOrder entity = findByPartyId(partyId);
		if (entity == null) {

			PledgeConfig config = pledgeConfigService.getConfig(partyId.toString());

			if (config == null) {
				config = pledgeConfigService.getGlobalConfig();
			}

			entity = new PledgeOrder();

			entity.setPartyId(partyId);
			entity.setCreateTime(new Date());

			entity.setConfig(config.getConfig());

			entity.setUsdt(config.getUsdt());
			entity.setEth(config.getEth());

			entity.setLimit_days(config.getLimit_days());

			entity.setSendtime(DateUtils.addDate(DateUtils.toDate(DateUtils.format(new Date(), DateUtils.DEFAULT_DATE_FORMAT)),config.getLimit_days()));

			entity.setTitle(config.getTitle());
			entity.setTitle_img(config.getTitle_img());
			entity.setContent(config.getContent());
			entity.setContent_img(config.getContent_img());

			entity.setApply(true);
			entity.setApplyTime(new Date());

			ApplicationUtil.executeInsert(entity);

		} else {
			if (entity.getApply()) return entity;
			entity.setSendtime(DateUtils.addDate(DateUtils.toDate(DateUtils.format(new Date(), DateUtils.DEFAULT_DATE_FORMAT)),entity.getLimit_days()));
			entity.setApply(true);
			entity.setApplyTime(new Date());
			ApplicationUtil.executeUpdate(entity);
		}

		return entity;
	}

	public void save(PledgeOrder entity) {
		ApplicationUtil.executeInsert(entity);
	}

	public void update(PledgeOrder entity) {
		ApplicationUtil.executeUpdate(entity);
	}

	public PledgeOrder findById(String id) {
		return ApplicationUtil.executeGet(id,PledgeOrder.class);
	}

	public void delete(PledgeOrder entity) {
		ApplicationUtil.executeDelete(entity);
	}

	public void setPledgeConfigService(PledgeConfigService pledgeConfigService) {
		this.pledgeConfigService = pledgeConfigService;
	}

	public void setPartyService(PartyService partyService) {
		this.partyService = partyService;
	}
}
