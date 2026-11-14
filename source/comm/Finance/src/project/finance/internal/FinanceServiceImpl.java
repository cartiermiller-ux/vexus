package project.finance.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.providers.encoding.PasswordEncoder;

import kernel.bo.RecordObjectMapper;
import kernel.exception.BusinessException;
import kernel.web.ApplicationUtil;
import project.finance.Finance;
import project.finance.FinanceRedisKeys;
import project.finance.FinanceService;
import project.redis.RedisHandler;
import security.SecUser;
import security.internal.SecUserService;

public class FinanceServiceImpl implements FinanceService {

	private JdbcTemplate jdbcTemplate;
	private RedisHandler redisHandler;
	private SecUserService  secUserService;
	private PasswordEncoder passwordEncoder;

	public void save(Finance entity,String login_safeword,String operaterUsername) {
		SecUser sec =  this.secUserService.findUserByLoginName(operaterUsername);
		String sysSafeword =sec.getSafeword();
		String safeword_md5 = passwordEncoder.encodePassword(login_safeword,operaterUsername);
		if (!safeword_md5.equals(sysSafeword)) {
			throw new BusinessException("资金密码错误");
		}
		entity.setId(ApplicationUtil.getCurrentTimeUUID());
        Object[] jdbcParams = ApplicationUtil.getInsertStatement(entity);
        String insertUserSql = (String)jdbcParams[0];
        Object[] sqlParameters = (Object[])jdbcParams[1];
        jdbcTemplate.update(insertUserSql, sqlParameters);
        
		redisHandler.setSync(FinanceRedisKeys.FINANCE_ID + entity.getId().toString(), entity);

		Map<String, Finance> cacheMap = (Map<String, Finance>) redisHandler.get(FinanceRedisKeys.FINANCE_MAP);
		if (cacheMap == null) {
			cacheMap = new ConcurrentHashMap<String, Finance>();
		}
		cacheMap.put(entity.getId().toString(), entity);
		redisHandler.setSync(FinanceRedisKeys.FINANCE_MAP, cacheMap);
	}

	public void update(Finance entity,String login_safeword,String operaterUsername) {
		SecUser sec =  this.secUserService.findUserByLoginName(operaterUsername);
		String sysSafeword =sec.getSafeword();
		String safeword_md5 = passwordEncoder.encodePassword(login_safeword,operaterUsername);
		if (!safeword_md5.equals(sysSafeword)) {
			throw new BusinessException("资金密码错误");
		}
		
		Object[] jdbcParams = ApplicationUtil.getUpdateStatement(entity, "WHERE UUID=?", new Object[] {entity.getId()});
		String updateUserSql = (String)jdbcParams[0];
		Object[] sqlParameters = (Object[])jdbcParams[1];
		jdbcTemplate.update(updateUserSql, sqlParameters);
		
		redisHandler.setSync(FinanceRedisKeys.FINANCE_ID + entity.getId().toString(), entity);

		Map<String, Finance> cacheMap = (Map<String, Finance>) redisHandler.get(FinanceRedisKeys.FINANCE_MAP);
		if (cacheMap == null) {
			cacheMap = new ConcurrentHashMap<String, Finance>();
		}
		cacheMap.put(entity.getId().toString(), entity);
		redisHandler.setSync(FinanceRedisKeys.FINANCE_MAP, cacheMap);
	
	}

	public void delete(String id,String login_safeword,String operaterUsername) {
		SecUser sec =  this.secUserService.findUserByLoginName(operaterUsername);
		String sysSafeword =sec.getSafeword();
		String safeword_md5 = passwordEncoder.encodePassword(login_safeword,operaterUsername);
		if (!safeword_md5.equals(sysSafeword)) {
			throw new BusinessException("资金密码错误");
		}
		
		Finance entity = findById(id);
		jdbcTemplate.update("DELETE FROM T_FINANCE WHERE UUID=?", entity.getId());
		redisHandler.remove(FinanceRedisKeys.FINANCE_ID + entity.getId().toString());

		Map<String, Finance> cacheMap = (Map<String, Finance>) redisHandler.get(FinanceRedisKeys.FINANCE_MAP);
		if (cacheMap != null && !cacheMap.isEmpty()) {
			cacheMap.remove(entity.getId().toString());
			redisHandler.setSync(FinanceRedisKeys.FINANCE_MAP, cacheMap);
		}
	}

	public Finance findById(String id) {
		return (Finance) redisHandler.get(FinanceRedisKeys.FINANCE_ID + id);
	}

	public List<Finance> findAll() {
		Map<String, Finance> cacheMap = (Map<String, Finance>) redisHandler.get(FinanceRedisKeys.FINANCE_MAP);
		if (cacheMap != null && !cacheMap.isEmpty()) {
			return new ArrayList<Finance>(cacheMap.values());
		}
		return new ArrayList<Finance>();
	}
	
	public Map<String, Finance> findFinanceMap(){
		return (Map<String, Finance>) redisHandler.get(FinanceRedisKeys.FINANCE_MAP);
	}

	public List<Finance> findAllState_1() {
		List<Finance> list = jdbcTemplate.query("SELECT * FROM T_FINANCE WHERE STATE = '1' ", RecordObjectMapper.newInstance(Finance.class));
		if (list.size() > 0) {
			return list;
		}
		return null;
	}
	
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public void setRedisHandler(RedisHandler redisHandler) {
		this.redisHandler = redisHandler;
	}
	
	public void setSecUserService(SecUserService secUserService) {
		this.secUserService = secUserService;
	}
	
	public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
		this.passwordEncoder = passwordEncoder;
	}
}
