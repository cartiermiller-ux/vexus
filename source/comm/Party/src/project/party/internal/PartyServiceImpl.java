package project.party.internal;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.providers.encoding.PasswordEncoder;
import org.springframework.util.ObjectUtils;

import kernel.bo.RecordObjectMapper;
import kernel.cache.RedisLocalCache;
import kernel.web.ApplicationUtil;
import project.party.PartyRedisKeys;
import project.party.PartyService;
import project.party.model.Party;
import project.redis.RedisHandler;
import security.SaltSigureUtils;
import security.SecUser;
import security.internal.SecUserService;

/**
 * @author GORGE
 * @description 用户持久层
 */
public class PartyServiceImpl implements PartyService {
	/**
	 * Redis工具
	 */
	private RedisHandler redisHandler;
	
	/**
	 * 系统用户持久层
	 */
	private SecUserService secUserService;
	
	/**
	 * 密码工具
	 */
	private PasswordEncoder passwordEncoder;
	
	/**
	 * 本地Redis工具(先取本地缓存再取Redis缓存)
	 */
	private RedisLocalCache redisLocalCache;
	
	/**
	 * 日志组件
	 */
	private static final Logger logger=LoggerFactory.getLogger(PartyServiceImpl.class);
	
	/**
	 * 获取用户认证等级: 1/新注册; 2/邮箱谷歌手机其中有一个已验证; 3/用户实名认证; 4/用户高级认证
	 * @param party 用户对象
	 * @return 认证等级
	 */
	public int getUserLevelByAuth(Party party) {
		SecUser secUser = this.secUserService.findUserByPartyId(party.getId().toString());
		int userLevel = 1;
		if (party.getEmail_authority() || party.getPhone_authority() || secUser.isGoogle_auth_bind()) {
			if (party.getKyc_authority()) {
				userLevel=party.isKyc_highlevel_authority()?4:3;
			} else {
				userLevel = 2;
			}
		} else {
			userLevel = 1;
		}
		return userLevel;
	}

	/**
	 * @param partyId 用户ID
	 * @param localcache 是否需要先读取本地缓存'
	 * @return 用户对象
	 */
	public Party cachePartyBy(Serializable partyId, boolean localcache) {
		Party party = null;
		
		if (localcache) { //先读本地缓存，再读Redis缓存
			party = (Party) redisLocalCache.get(PartyRedisKeys.PARTY_ID + partyId);
		} else { //仅读取Redis缓存
			party = (Party) redisHandler.get(PartyRedisKeys.PARTY_ID + partyId);
		}
		
		return party;
	}
	
	/**
	 * @param partyId 用户ID
	 * @param localcache 是否需要先读取本地缓存'
	 * @return 用户对象
	 */
	public Party cachePartyByUsername(String userName, boolean localcache) {
		Party party = null;
		
		if (localcache) { //先读本地缓存，再读Redis缓存
			party = (Party) redisLocalCache.get(PartyRedisKeys.PARTY_USERNAME + userName);
		} else { //仅读取Redis缓存
			party = (Party) redisHandler.get(PartyRedisKeys.PARTY_USERNAME + userName);
		}
		
		return party;
	}

	/**
	 * @param entity 用户对象
	 * @return 用户对象
	 */
	public Party save(Party entity) {
		if(null==entity) return null;
		JdbcTemplate jdbcTemplate=ApplicationUtil.getBean(JdbcTemplate.class);
		
		entity.setCreateTime(new Date());
		if(null==entity.getId()) entity.setId(ApplicationUtil.getCurrentTimeUUID());
		
		Object[] jdbcParams=ApplicationUtil.getInsertStatement(entity);
		jdbcTemplate.update((String)jdbcParams[0],(Object[])jdbcParams[1]);
		
		redisHandler.setSync(PartyRedisKeys.PARTY_ID + entity.getId(), entity);
		redisHandler.setSync(PartyRedisKeys.PARTY_USERNAME + entity.getUsername(), entity);
		
		return entity;
	}

	/**
	 * 修改或保存实体对象
	 * @param entity 用户对象
	 */
	public void update(Party entity) {
		if(null==entity) return;
		JdbcTemplate jdbcTemplate=ApplicationUtil.getBean(JdbcTemplate.class);
		
		//执行update
		Object[] jdbcParams=ApplicationUtil.getUpdateStatement(entity, "WHERE UUID=?", new Object[] {entity.getId()});
		int count=jdbcTemplate.update((String)jdbcParams[0],(Object[])jdbcParams[1]);
		if(count<=0) { //执行insert
			jdbcParams=ApplicationUtil.getInsertStatement(entity);
			jdbcTemplate.update((String)jdbcParams[0],(Object[])jdbcParams[1]);
		}
		
		redisHandler.setSync(PartyRedisKeys.PARTY_ID + entity.getId(), entity);
		redisHandler.setSync(PartyRedisKeys.PARTY_USERNAME + entity.getUsername(), entity);
	}
	
	/**
	 * 通过用户名查找用户
	 * @param username 用户名
	 * @return 用户对象
	 */
	public Party findPartyByUsername(String username) {
		if(null==username) return null;
		JdbcTemplate jdbcTemplate=ApplicationUtil.getBean(JdbcTemplate.class);
		
		Object[] jdbcParams=ApplicationUtil.getSelectStatement(Party.class, "WHERE USERNAME=?", new Object[] {username});
		List<Party> list=jdbcTemplate.query((String)jdbcParams[0],RecordObjectMapper.newInstance(Party.class),(Object[])jdbcParams[1]);
		
		return list.size() > 0?list.get(0):null;
	}
	
	/**
	 * 根据已验证的电话号码获取Party对象
	 * @param phone 电话号码
	 * @return 用户对象
	 */
	@Override
	public Party findPartyByVerifiedPhone(String phone) {
		if(null==phone) return null;
		JdbcTemplate jdbcTemplate=ApplicationUtil.getBean(JdbcTemplate.class);
		
		Object[] jdbcParams=ApplicationUtil.getSelectStatement(Party.class, "WHERE PHONE=? AND PHONE_AUTHORITY='Y'", new Object[] {phone});
		List<Party> list=jdbcTemplate.query((String)jdbcParams[0],RecordObjectMapper.newInstance(Party.class),(Object[])jdbcParams[1]);
		
		return list.size() > 0?list.get(0):null;
	}
	
	/**
	 * 根据已验证的邮箱获取Party对象
	 * @param email 电子邮件
	 * @return 用户对象
	 */
	@Override
	public Party findPartyByVerifiedEmail(String email) {
		if(null==email) return null;
		JdbcTemplate jdbcTemplate=ApplicationUtil.getBean(JdbcTemplate.class);
		
		Object[] jdbcParams=ApplicationUtil.getSelectStatement(Party.class, "WHERE EMAIL=? AND EMAIL_AUTHORITY='Y'", new Object[] {email});
		List<Party> list=jdbcTemplate.query((String)jdbcParams[0],RecordObjectMapper.newInstance(Party.class),(Object[])jdbcParams[1]);
		
		return list.size() > 0?list.get(0):null;
	}

	/**
	 * 获取所有用户
	 * @return 用户列表
	 */
	public List<Party> getAll() {
		Object[] jdbcParams=ApplicationUtil.getSelectStatement(Party.class);
		return ApplicationUtil.getBean(JdbcTemplate.class).query((String)jdbcParams[0],RecordObjectMapper.newInstance(Party.class),(Object[])jdbcParams[1]);
	}

	/**
	 * 根据用户码获取用户
	 * @param usercode 用户码
	 * @return 用户对象
	 */
	@Override
	public Party findPartyByUsercode(String usercode) {
		if(null==usercode) return null;
		
		Object[] jdbcParams=ApplicationUtil.getSelectStatement(Party.class, "WHERE USERCODE=?", new Object[] {usercode});
		List<Party> list=ApplicationUtil.getBean(JdbcTemplate.class).query((String)jdbcParams[0],RecordObjectMapper.newInstance(Party.class),(Object[])jdbcParams[1]);
		
		return list.size() > 0?list.get(0):null;
	}

	/**
	 * 修改资金密码
	 * @param party 用户对象
	 * @param safeword 资金密码
	 */
	public void updateSafeword(Party party, String safeword) {
		String safeword_md5 = passwordEncoder.encodePassword(safeword, SaltSigureUtils.saltfigure);
		party.setSafeword(safeword_md5);
		this.update(party);
	}

	/**
	 * 检查资金密码是否正确
	 * @param safeword 资金密码
	 * @param partyId 用户ID
	 * @return 资金密码是否正确
	 */
	public boolean checkSafeword(String safeword, String partyId) {
		if (ObjectUtils.isEmpty(safeword)) return Boolean.FALSE;
		
		Party party = this.cachePartyBy(partyId, false);
		if (null==party) {
			logger.error("party is null,id:{}", partyId);
			return Boolean.FALSE;
		}
		
		if (ObjectUtils.isEmpty(party.getSafeword())) return Boolean.FALSE;
		
		String md5 = passwordEncoder.encodePassword(safeword, SaltSigureUtils.saltfigure);
		return md5.equals(party.getSafeword());
	}

	public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
		this.passwordEncoder = passwordEncoder;
	}

	public void setRedisHandler(RedisHandler redisHandler) {
		this.redisHandler = redisHandler;
	}

	public void setRedisLocalCache(RedisLocalCache redisLocalCache) {
		this.redisLocalCache = redisLocalCache;
	}

	public void setSecUserService(SecUserService secUserService) {
		this.secUserService = secUserService;
	}
}
