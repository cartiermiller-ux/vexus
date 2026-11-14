package project.party.recom.internal;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import kernel.exception.BusinessException;
import kernel.util.StringUtils;
import kernel.web.ApplicationUtil;
import project.party.PartyRedisKeys;
import project.party.model.UserRecom;
import project.party.recom.UserRecomService;
import project.redis.RedisHandler;

@SuppressWarnings("unchecked")
public class UserRecomServiceImpl implements UserRecomService {
	
	private RedisHandler redisHandler;
	
	private static final Logger logger=LoggerFactory.getLogger(UserRecomServiceImpl.class);

	/**
	 * 存储推荐关系
	 */
	public void save(UserRecom entity) {
		if(null==entity) return;
		
		checkRecommended(entity.getPartyId(), entity.getReco_id());
		
		if(null==entity.getId()) entity.setId(ApplicationUtil.getCurrentTimeUUID());
		ApplicationUtil.executeInsert(entity);
		
		redisHandler.setSync(PartyRedisKeys.USER_RECOM_PARTYID + entity.getPartyId().toString(), entity);
		List<UserRecom> recos = (List<UserRecom>) redisHandler.get(PartyRedisKeys.USER_RECOM_RECO_ID + entity.getReco_id().toString());
		if (recos == null) recos=new ArrayList<UserRecom>();
		recos.add(entity);
		
		redisHandler.setSync(PartyRedisKeys.USER_RECOM_RECO_ID + entity.getReco_id().toString(), recos);
	}

	/**
	 * 修改推荐关系
	 */
	public void update(Serializable partyId, Serializable reco_id) {
		//检查是否存在推荐关系,如果存在则抛出异常
		checkRecommended(partyId, reco_id);
		
		//先查缓存再查询数据库
		UserRecom entity=findByPartyId(partyId);
		if (null==entity) {
			List<UserRecom> list=ApplicationUtil.executeSelect(UserRecom.class,"WHERE PARTY_ID=?",new Object[] {partyId});
			if(null!=list && list.size()>0) {
				entity=list.get(0);
				if(list.size()>1) logger.error("data record duplication and get first record,found multiple same PARTY_ID: {}",partyId);
			}
		}
		
		//如果缓存和数据库中都没有找到该用户则创建该用户
		if (null==entity) {
			entity = new UserRecom();
			entity.setPartyId(partyId);
			entity.setId(ApplicationUtil.getCurrentTimeUUID());
		}
		
		//变更推荐人之前获取以前的(旧的)推荐人,方便后续从旧推荐人的推荐集合中移除他自己
		Serializable reco_id_old = entity.getReco_id();
		
		//绑定或变更新的推荐人
		entity.setReco_id(reco_id);
		
		//保存或修改推荐关系
		ApplicationUtil.executeInsertOrUpdate(entity,"WHERE UUID=?",new Object[] {entity.getId()});
		redisHandler.setSync(PartyRedisKeys.USER_RECOM_PARTYID + entity.getPartyId().toString(), entity);
		
		//如果当前用户以前有推荐人,则从他以前的推荐人的推荐集合中移除他自己
		if (null!=reco_id_old) {
			List<UserRecom> recos_oldList = (List<UserRecom>) redisHandler.get(PartyRedisKeys.USER_RECOM_RECO_ID + reco_id_old.toString());
			while(recos_oldList.remove(entity));
			redisHandler.setSync(PartyRedisKeys.USER_RECOM_RECO_ID + reco_id_old.toString(), recos_oldList);
		}

		//从新推荐人的推荐集合中加入当前用户自己
		List<UserRecom> recos = (List<UserRecom>) redisHandler.get(PartyRedisKeys.USER_RECOM_RECO_ID + reco_id.toString());
		if (null==recos) recos = new ArrayList<UserRecom>();
		recos.add(entity);
		redisHandler.setSync(PartyRedisKeys.USER_RECOM_RECO_ID + reco_id.toString(), recos);
	}
	
	/**
	 * 根据用户ID从缓存中获取此用户的推荐关系
	 * @param partyId 用户ID
	 * @return 推荐关系实体对象
	 */
	public UserRecom findByPartyId(Serializable partyId) {
		return null==partyId?null:(UserRecom) redisHandler.get(PartyRedisKeys.USER_RECOM_PARTYID+partyId);
	}

	/**
	 * 向上查找用户的推荐树
	 * @param partyId 用户ID
	 * @return 推荐树节点列表(含用户自己)
	 */
	public List<UserRecom> getParents(Serializable partyId) {
		LinkedList<UserRecom> list = new LinkedList<UserRecom>();
		if (partyId == null) return list;
		list = findParents(partyId, list);
		return list;
	}
	
	/**
	 * 向上查找用户的祖先节点推荐树(含用户自己)
	 * @param partyId 用户ID
	 * @param list 推荐树节点列表
	 * @return 推荐树节点列表(含用户自己)
	 */
	private LinkedList<UserRecom> findParents(Serializable partyId, LinkedList<UserRecom> list) {
		if(null==partyId) return list;
		UserRecom userRecom = (UserRecom) redisHandler.get(PartyRedisKeys.USER_RECOM_PARTYID + partyId);
		if(null==userRecom) return list;
		
		list.add(userRecom);
		findParents(userRecom.getReco_id(), list);
		
		return list;
	}

	/**
	 * 向下查找参数ID对应推荐人推荐的下级用户集合
	 * @param partyId 推荐人ID
	 * @return 推荐的下级用户列表(直接下级用户集合,不含孙子及后裔节点)
	 */
	public List<UserRecom> findRecoms(Serializable partyId) {
		List<UserRecom> list = (List<UserRecom>) redisHandler.get(PartyRedisKeys.USER_RECOM_RECO_ID + partyId.toString());
		return null==list?new ArrayList<UserRecom>():list;
	}
	
	/**
	 * 向下查找参数ID对应推荐人推荐的下级用户ID集合
	 * @param partyId 推荐人ID
	 * @return 推荐的下级用户ID列表(直接下级用户集合,不含孙子及后裔节点)
	 */
	public List<String> findRecomsToPartyId(Serializable partyId) {
		List<UserRecom> recom_list = findRecoms(partyId);
		return recom_list.stream().map(userRecom->userRecom.getPartyId().toString()).collect(Collectors.toList());
	}

	/**
	 * 检查两用户间是否存在直接或间接的推荐关系
	 * @param partyId1 第一个用户
	 * @param partyId2 第二个用户
	 */
	public void checkRecommended(Serializable partyId1, Serializable partyId2) {
		String errorTip="两用户间已经存在直接或间接推荐关系,不能修改已经存在的推荐关系层级的先后顺序!";
		List<UserRecom> recom_list1 = getParents(partyId1);
		for (int i = 0; i < recom_list1.size(); i++) {
			if (partyId2.toString().equals(((UserRecom) recom_list1.get(i)).getReco_id().toString())) {
				throw new BusinessException(errorTip);
			}
		}

		List<UserRecom> recom_list2 = getParents(partyId2);
		for (int i = 0; i < recom_list2.size(); i++) {
			if (partyId1.toString().equals(((UserRecom) recom_list2.get(i)).getReco_id().toString())) {
				throw new BusinessException(errorTip);
			}
		}
	}

	/**
	 * 向下查找用户的后代节点推荐ID树(不含用户自己)
	 * @param partyId 用户ID
	 * @return 节点ID推荐树(不含用户自己)
	 */
	public List<String> findChildren(Serializable partyId) {
		return findChildren(partyId, new ArrayList<String>());
	}

	/**
	 * 向下查找用户的后代节点推荐ID树(不含用户自己)
	 * @param partyId 用户ID
	 * @param list 节点ID推荐树
	 * @return 节点ID推荐树(不含用户自己)
	 */
	private ArrayList<String> findChildren(Serializable partyId, ArrayList<String> list) {
		List<UserRecom> recom_list = findRecoms(partyId);
		for (int i = 0; i < recom_list.size(); i++) {
			list.add(((UserRecom) recom_list.get(i)).getPartyId().toString());
			findChildren(((UserRecom) recom_list.get(i)).getPartyId().toString(), list);
		}
		return list;
	}
	
	/**
	 * 获取用户的后代节点推荐ID序列串(不含用户自己)
	 * @param loginPartyId 用户ID
	 * @return 后代节点推荐ID序列串(不含用户自己)
	 */
	public String findChildrensIds(String loginPartyId) {
		if (StringUtils.isNullOrEmpty(loginPartyId)) return null;
		List<String> children = findChildren(loginPartyId);
		return children.isEmpty()?null:String.join(",",children.stream().map(id->"'"+id+"'").collect(Collectors.toSet()));
	}

	public void setRedisHandler(RedisHandler redisHandler) {
		this.redisHandler = redisHandler;
	}
}
