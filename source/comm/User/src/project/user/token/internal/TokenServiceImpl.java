package project.user.token.internal;

import java.io.Serializable;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import kernel.util.StringUtils;
import kernel.util.UUIDGenerator;
import kernel.web.ApplicationUtil;
import project.redis.RedisHandler;
import project.user.UserRedisKeys;
import project.user.token.Token;
import project.user.token.TokenService;

public class TokenServiceImpl implements TokenService {

	private RedisHandler redisHandler;
	
	private static final Logger logger=LoggerFactory.getLogger(TokenServiceImpl.class);

	public String savePut(String partyId,String username) {
		Token token = this.find(partyId);
		if (token == null) {
			token = new Token();
			token.setPartyId(partyId);
		}
		
		String uuid = UUIDGenerator.getUUID();
		token.setToken(uuid);
		
		Serializable id=token.getId();
		if(null == id) id=ApplicationUtil.getCurrentTimeUUID();
		
		ApplicationUtil.executeInsertOrUpdate(token, "WHERE UUID=?", new Object[] {id});
		redisHandler.setSync(UserRedisKeys.TOKEN + uuid, partyId);
		redisHandler.setSync(UserRedisKeys.USER_NAME + uuid, username);
		redisHandler.setSync(UserRedisKeys.TOKEN_PARTY_ID + partyId, token);
		
		return uuid;
	}

	public String cacheGet(String token) {
		if (StringUtils.isNullOrEmpty(token)) {
			logger.error("cacheGet:token is null");
			return null;
		}

		String partyId = (String) redisHandler.get(UserRedisKeys.TOKEN + token);
		if (StringUtils.isNullOrEmpty(partyId)) {
			HttpServletRequest request=ApplicationUtil.getHttpServletRequest();
			if(null==request) {
				logger.error("cacheGet:partyId is null,token:{}",token);
			}else {
				String servletPath = request.getServletPath();
				logger.error("cacheGet:partyId is null,token:{},requestURI:{}",token,servletPath);
			}
			
			return null;
		}

		Token token_redis = (Token) redisHandler.get(UserRedisKeys.TOKEN_PARTY_ID + partyId);
		if (null == token_redis) {
			logger.error("cacheGet:token_redis is null");
			return null;
		}
		
		if (!token.equals(token_redis.getToken())) {
			logger.error("cacheGet:请求token:{}和token_redis{}不同", token, token_redis.getToken());
			return null;
		}
		
		return partyId;
	}

	public String getUserName(String token,String url) {
		if (StringUtils.isNullOrEmpty(token)) {
			logger.error("getUserName:token is null,url:"+url);
			return null;
		}
		
		String userName = (String) redisHandler.get(UserRedisKeys.USER_NAME + token);
		if (StringUtils.isNullOrEmpty(userName)) return null;
		
		return userName;
	}

	public Token find(String partyId) {
		return (Token) redisHandler.get(UserRedisKeys.TOKEN_PARTY_ID + partyId);
	}

	@Override
	public void delete(String token) {
		if (StringUtils.isNullOrEmpty(token)) return;
		
		String partyId = cacheGet(token);
		if (!StringUtils.isNullOrEmpty(partyId)) {
			Token entity = find(partyId);
			if (entity != null) ApplicationUtil.executeDelete(Token.class,"WHERE UUID=?",new Object[] {entity.getId()});
		}
		
		redisHandler.remove(UserRedisKeys.TOKEN + token);
		redisHandler.remove(UserRedisKeys.TOKEN_PARTY_ID + partyId);
	}

	public void setRedisHandler(RedisHandler redisHandler) {
		this.redisHandler = redisHandler;
	}
}
