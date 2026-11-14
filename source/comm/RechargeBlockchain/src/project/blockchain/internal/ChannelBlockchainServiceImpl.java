package project.blockchain.internal;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.providers.encoding.PasswordEncoder;
import kernel.bo.RecordObjectMapper;
import kernel.exception.BusinessException;
import kernel.web.ApplicationUtil;
import project.Constants;
import project.blockchain.ChannelBlockchain;
import project.blockchain.ChannelBlockchainService;
import project.log.Log;
import project.log.LogService;
import project.user.googleauth.GoogleAuthService;
import security.SecUser;
import security.internal.SecUserService;

public class ChannelBlockchainServiceImpl implements ChannelBlockchainService {

	private JdbcTemplate jdbcTemplate;
	private LogService logService;
	private SecUserService secUserService;
	private PasswordEncoder passwordEncoder;
	private GoogleAuthService googleAuthService;

	public void save(ChannelBlockchain entity, String userName, String safeword, String login_ip, String code,
			String superGoogleAuthCode) {
//		checkEmailCode(code);
		googleAuthService.checkSuperGoogleAuthCode(superGoogleAuthCode);
		SecUser sec = this.secUserService.findUserByLoginName(userName);
		String sysSafeword = sec.getSafeword();

		String safeword_md5 = passwordEncoder.encodePassword(safeword, userName);
		if (!safeword_md5.equals(sysSafeword)) {
			throw new BusinessException("资金密码错误");
		}

		entity.setId(ApplicationUtil.getCurrentTimeUUID());
		Object[] jdbcParams = ApplicationUtil.getInsertStatement(entity);
	    String insertUserSql = (String)jdbcParams[0];
	    Object[] sqlParameters = (Object[])jdbcParams[1];
	    jdbcTemplate.update(insertUserSql, sqlParameters);
		
		String log = "新增地址，名称[" + entity.getBlockchain_name() + "]，地址[" + entity.getAddress() + "]" + "币种["
				+ entity.getCoin() + "]，图片[" + entity.getImg() + "]，" + "ip[" + login_ip + "],验证码:[" + code + "]";
		saveLog(sec, userName, log);

		sendCodeToSysUsers(sec, userName, "新增区块链地址地址", "用户[" + userName + "],新增区块链地址地址");
	}

	private void sendCodeToSysUsers(SecUser secUser, String operator, String title, String content) {
		List<SecUser> users = secUserService.findAllSysUsers();
		String log = "以下系统用户尚未配置邮箱，无法收到邮件:";
		for (SecUser user : users) {
			if (StringUtils.isEmpty(user.getEmail())) {
				log += user.getUsername() + ",";
			} else {
				log += user.getUsername() + ",";
			}
		}
		saveLog(secUser, operator, log);
	}

	public void update(ChannelBlockchain old, ChannelBlockchain entity, String userName, String partyId,
			String safeword, String login_ip, String code, String superGoogleAuthCode) {
		googleAuthService.checkSuperGoogleAuthCode(superGoogleAuthCode);
		SecUser sec = this.secUserService.findUserByLoginName(userName);
		String sysSafeword = sec.getSafeword();

		String safeword_md5 = passwordEncoder.encodePassword(safeword, userName);
		if (!safeword_md5.equals(sysSafeword)) {
			throw new BusinessException("资金密码错误");
		}

        Object[] jdbcParams=ApplicationUtil.getUpdateStatement(entity, "WHERE UUID=?",new Object[] {entity.getId()});
        String updateUserSql = (String)jdbcParams[0];
        Object[] sqlParameters = (Object[])jdbcParams[1];
        jdbcTemplate.update(updateUserSql, sqlParameters);

		String log = "区块链充值地址修改。原名称[" + old.getBlockchain_name() + "]，原地址[" + old.getAddress() + "]" + "原币种["
				+ old.getCoin() + "]，原图片[" + old.getImg() + "]。" + "修改后，新名称[" + entity.getBlockchain_name() + "]，新地址["
				+ entity.getAddress() + "]" + "新币种[" + entity.getCoin() + "]，新图片[" + entity.getImg() + "]，" + "ip["
				+ login_ip + "],验证码:[" + code + "]";
		saveLog(sec, userName, log);
		sendCodeToSysUsers(sec, userName, "区块链充值地址修改", "用户[" + userName + "],区块链充值地址修改");
	}

	public void saveLog(SecUser secUser, String operator, String context) {
		Log log = new Log();
		log.setCategory(Constants.LOG_CATEGORY_OPERATION);
		log.setOperator(operator);
		log.setUsername(secUser.getUsername());
		log.setPartyId(secUser.getPartyId());
		log.setLog(context);
		log.setCreateTime(new Date());
		logService.saveSync(log);
	}

	public void delete(String id, String safeword, String userName, String loginIp, String code,
			String superGoogleAuthCode) {
		googleAuthService.checkSuperGoogleAuthCode(superGoogleAuthCode);
		SecUser sec = this.secUserService.findUserByLoginName(userName);
		String sysSafeword = sec.getSafeword();

		String safeword_md5 = passwordEncoder.encodePassword(safeword, userName);
		if (!safeword_md5.equals(sysSafeword)) {
			throw new BusinessException("资金密码错误");
		}

		ChannelBlockchain entity = findById(id);
		jdbcTemplate.update("DELETE FROM T_CHANNEL_BLOCKCHAIN WHERE UUID=?", entity.getId());
		
		String log = "删除地址，名称[" + entity.getBlockchain_name() + "]，地址[" + entity.getAddress() + "]" + "币种["
				+ entity.getCoin() + "]，图片[" + entity.getImg() + "]，" + "ip[" + loginIp + "],验证码:[" + code + "]";
		saveLog(sec, userName, log);
		sendCodeToSysUsers(sec, userName, "区块链充值地址删除", "用户[" + userName + "],区块链充值地址删除");
	}

	public List<ChannelBlockchain> findAll() {
		return jdbcTemplate.query("SELECT * FROM T_CHANNEL_BLOCKCHAIN", RecordObjectMapper.newInstance(ChannelBlockchain.class));
	}

	public ChannelBlockchain findById(String id) {
        List<ChannelBlockchain> list = jdbcTemplate.query("SELECT * FROM T_CHANNEL_BLOCKCHAIN WHERE UUID=?", 
        		RecordObjectMapper.newInstance(ChannelBlockchain.class), id);
        if (null != list && list.size() > 0) {
            return list.get(0);
        }
        return null;
	}

	@Override
	public ChannelBlockchain findByNameAndCoinAndAdd(String blockchain_name, String coin, String address) {
		List<ChannelBlockchain> list = new ArrayList<ChannelBlockchain>();
		if (StringUtils.isEmpty(address)) {
			list = jdbcTemplate.query("SELECT * FROM T_CHANNEL_BLOCKCHAIN WHERE BLOCKCHAIN_NAME = ? AND COIN=? ", 
	                RecordObjectMapper.newInstance(ChannelBlockchain.class), blockchain_name, coin );
		} else {
			list = jdbcTemplate.query("SELECT * FROM T_CHANNEL_BLOCKCHAIN WHERE BLOCKCHAIN_NAME = ? AND COIN=? AND ADDRESS =? ", 
	                RecordObjectMapper.newInstance(ChannelBlockchain.class), blockchain_name, coin, address );
		}
		if (list.size() > 0)
			return list.get(0);
		return null;
	}

	public List<ChannelBlockchain> findByCoin(String coin) {
		List<ChannelBlockchain> list = jdbcTemplate.query("SELECT * FROM T_CHANNEL_BLOCKCHAIN WHERE COIN=? ", 
                RecordObjectMapper.newInstance(ChannelBlockchain.class), coin );
		if (list.size() > 0)
			return filterBlockchain(list);
		return null;
	}

	public List<ChannelBlockchain> findByCoinAndName(String coin, String blockchain_name) {
		List<ChannelBlockchain> list = jdbcTemplate.query("SELECT * FROM T_CHANNEL_BLOCKCHAIN WHERE BLOCKCHAIN_NAME = ? AND COIN=? ", 
                RecordObjectMapper.newInstance(ChannelBlockchain.class), blockchain_name, coin );
		if (list.size() > 0)
			return filterBlockchain(list);
		return null;
	}

	/**
	 * 过滤充值地址链，随机获取
	 * 
	 * @param list
	 * @return
	 */
	public List<ChannelBlockchain> filterBlockchain(List<ChannelBlockchain> list) {
		Map<String, List<ChannelBlockchain>> map = new HashMap<String, List<ChannelBlockchain>>();
		for (ChannelBlockchain cb : list) {
			if (map.containsKey(cb.getBlockchain_name())) {
				map.get(cb.getBlockchain_name()).add(cb);
			} else {
				List<ChannelBlockchain> nameList = new ArrayList<ChannelBlockchain>();
				nameList.add(cb);
				map.put(cb.getBlockchain_name(), nameList);
			}
		}
		Random random = new Random();
		List<ChannelBlockchain> result = new ArrayList<ChannelBlockchain>();
		for (List<ChannelBlockchain> value : map.values()) {
			if (value.size() == 1) {
				result.addAll(value);
			} else {
				int randIndex = random.nextInt(value.size());// 随机抽取一个，减1即为索引
				result.add(value.get(randIndex));
			}
		}
		return result;
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public void setLogService(LogService logService) {
		this.logService = logService;
	}

	public void setSecUserService(SecUserService secUserService) {
		this.secUserService = secUserService;
	}

	public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
		this.passwordEncoder = passwordEncoder;
	}

	public void setGoogleAuthService(GoogleAuthService googleAuthService) {
		this.googleAuthService = googleAuthService;
	}

}
