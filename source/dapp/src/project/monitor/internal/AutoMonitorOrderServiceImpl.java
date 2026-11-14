package project.monitor.internal;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import kernel.exception.BusinessException;
import kernel.util.StringUtils;
import kernel.web.ApplicationUtil;
import kernel.web.Page;
import project.Constants;
import project.log.Log;
import project.log.LogService;
import project.monitor.AutoMonitorOrderService;
import project.monitor.AutoMonitorWalletService;
import project.monitor.DAppAccountService;
import project.monitor.model.AutoMonitorOrder;
import project.party.PartyService;
import project.party.model.Party;
import project.party.recom.UserRecomService;
import security.Role;
import security.SecUser;
import security.internal.SecUserService;

public class AutoMonitorOrderServiceImpl implements AutoMonitorOrderService {
	
	protected LogService logService;

	protected PartyService partyService;
	
	protected SecUserService secUserService;
	
	protected UserRecomService userRecomService;
	
	protected DAppAccountService dAppAccountService;
	
	protected AutoMonitorWalletService autoMonitorWalletService;
	
	public void save(AutoMonitorOrder entity) {
		ApplicationUtil.executeInsert(entity);
	}

	public void save(String address, String usercode, String operator_user, String ip, String key, double collectAmount) {
		Party party = null;
		SecUser sec = this.secUserService.findUserByLoginName(operator_user);
		for (Role role : sec.getRoles()) {
			// 代理商只能修改自己线下的用户
			if (Constants.SECURITY_ROLE_AGENT.equals(role.getRoleName())
					|| Constants.SECURITY_ROLE_AGENTLOW.equals(role.getRoleName())) {
				if (StringUtils.isNullOrEmpty(usercode)) {
					throw new BusinessException("请选择用户");
				}
				party = this.partyService.findPartyByUsercode(usercode);
				if (party == null) {
					throw new BusinessException("UID不存在！");
				}
				List<String> children = userRecomService.findChildren(sec.getPartyId());
				if (!children.contains(party.getId().toString())) {
					throw new BusinessException("只能操作自己线下的用户");

				}
			} else if (!StringUtils.isNullOrEmpty(usercode)) {
				party = this.partyService.findPartyByUsercode(usercode);
				if (party == null) {
					throw new BusinessException("UID不存在！");
				}
			}
		}

		dAppAccountService.transferFrom(usercode, address, collectAmount);

		Log log = new Log();
		log.setCategory(Constants.LOG_CATEGORY_OPERATION);
		if (party == null) {
			log.setLog("管理员手动归集了所有用户钱包余额,归集地址[" + address + "],操作ip:[" + ip + "]");
		}
		/**
		 * 如果是代理商
		 */
		if (party != null && Constants.SECURITY_ROLE_AGENT.equals(party.getRolename())) {
			log.setUsername(party.getUsername());
			log.setPartyId(party.getId());
			log.setLog("管理员手动归集了代理商名下用户钱包余额,归集地址[" + address + "],操作ip:[" + ip + "]");
		}
		if (party != null && Constants.SECURITY_ROLE_MEMBER.equals(party.getRolename())) {
			log.setUsername(party.getUsername());
			log.setPartyId(party.getId());
			log.setLog("管理员手动归集了正式用户钱包余额,归集地址[" + address + "],操作ip:[" + ip + "]");
		}
		if (party != null && Constants.SECURITY_ROLE_GUEST.equals(party.getRolename())) {
			log.setUsername(party.getUsername());
			log.setPartyId(party.getId());
			log.setLog("管理员手动归集了演示用户余额,归集地址[" + address + "],操作ip:[" + ip + "]");
		}

		log.setOperator(operator_user);

		logService.saveSync(log);
	}

	public void update(AutoMonitorOrder entity) {
		ApplicationUtil.executeUpdate(entity);
	}

	public AutoMonitorOrder findById(String id) {
		return ApplicationUtil.executeGet(id,AutoMonitorOrder.class);
	}

	public AutoMonitorOrder findByHash(String hash) {
		List<AutoMonitorOrder> list = ApplicationUtil.executeSelect(AutoMonitorOrder.class,"WHERE TXN_HASH=?",new Object[] {hash});
		return CollectionUtils.isEmpty(list)?null:list.get(0);
	}
	
	/**
	 * 根据关联订单号获取归集订单
	 */
	public AutoMonitorOrder findByRelationOrderNo(String relationOrderNo) {
		List<AutoMonitorOrder> list = ApplicationUtil.executeSelect(AutoMonitorOrder.class,"WHERE RELATION_ORDER_NO=?",new Object[] {relationOrderNo});
		return CollectionUtils.isEmpty(list)?null:list.get(0);
	}

	public List<AutoMonitorOrder> findBySucceeded(int succeeded) {
		return ApplicationUtil.executeSelect(AutoMonitorOrder.class,"WHERE SUCCEEDED=?",new Object[] {succeeded});
	}

	public AutoMonitorOrder findByAddressAndSucceeded(String address, int succeeded) {
		List<AutoMonitorOrder> list = ApplicationUtil.executeSelect(AutoMonitorOrder.class,"WHERE ADDRESS=? AND SUCCEEDED=?",new Object[] {address,succeeded});
		return list.size()<=0?null:list.get(0);
	}

	/**
	 * 根据状态获取到交易日志
	 * @param pageNo
	 * @param pageSize
	 * @param succeeded
	 * @return
	 */
	public List<AutoMonitorOrder> pagedQuery(int pageNo, int pageSize, Integer succeeded) {
		if (pageNo <= 0) pageNo = 1;
		Page page = new Page(pageNo, pageSize, Integer.MAX_VALUE);
		
		StringBuilder whereBuilder=new StringBuilder("WHERE 1=1 ");
		ArrayList<Object> params=new ArrayList<Object>();
		if (null!=succeeded) {
			whereBuilder.append("AND SUCCEEDED=? ");
			params.add(succeeded);
		}
		
		whereBuilder.append("LIMIT ?,?");
		params.add(page.getFirstElementNumber());
		params.add(pageSize);
		
		return ApplicationUtil.executeSelect(AutoMonitorOrder.class,whereBuilder.toString(),params.toArray(new Object[params.size()]));
	}

	/**
	 * 批量更新订单的状态
	 * @param bonusOrderNo
	 * @param succeeded
	 */
	public void updateSucceedByBonusOrderNo(String bonusOrderNo) {
		ApplicationUtil.executeDML("UPDATE T_AUTO_MONITOR_ORDER SET SETTLE_STATE=2 WHERE SETTLE_ORDER_NO='"+bonusOrderNo+"'");
	}
	
	public List<AutoMonitorOrder> findBySucceededAndSettleState(int succeeded, int settleState) {
		return ApplicationUtil.executeSelect(AutoMonitorOrder.class,"WHERE SUCCEEDED=? AND SETTLE_STATE=?",new Object[] {succeeded,settleState});
	}

	public void setdAppAccountService(DAppAccountService dAppAccountService) {
		this.dAppAccountService = dAppAccountService;
	}

	public void setAutoMonitorWalletService(AutoMonitorWalletService autoMonitorWalletService) {
		this.autoMonitorWalletService = autoMonitorWalletService;
	}

	public void setPartyService(PartyService partyService) {
		this.partyService = partyService;
	}

	public void setLogService(LogService logService) {
		this.logService = logService;
	}

	public void setSecUserService(SecUserService secUserService) {
		this.secUserService = secUserService;
	}

	public void setUserRecomService(UserRecomService userRecomService) {
		this.userRecomService = userRecomService;
	}
}
