package data.loadcache;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import kernel.bo.RecordObjectMapper;
import project.RedisKeys;
import project.redis.RedisHandler;
import project.wallet.Wallet;
import project.wallet.WalletExtend;
import project.wallet.WalletLog;
import project.wallet.WalletRedisKeys;
import project.wallet.rate.ExchangeRate;
import project.wallet.rate.UserRateConfig;

public class WalletLoadCacheService {
	
	private static final Logger logger = LoggerFactory.getLogger(WalletLoadCacheService.class);
	private JdbcTemplate jdbcTemplate;
	private RedisHandler redisHandler;

	public void loadcache() {
		loadWallet();
		logger.info("完成Wallet数据加载redis");

		loadWalletExtend();
		logger.info("完成WalletExtend数据加载redis");
		
		loadExchangeRate();
		logger.info("完成ExchangeRate数据加载redis");
		loadUserRateConfig();
		logger.info("完成UserRateConfig数据加载redis");
		loadWalletLog();
		logger.info("完成WalletLog数据加载redis");
	}

	public void loadWallet() {
		List<Wallet> list = jdbcTemplate.query("SELECT * FROM T_WALLET", RecordObjectMapper.newInstance(Wallet.class));
		Map<String, Object> params = new ConcurrentHashMap<String, Object>();
		for (Wallet wallet : list) {
			params.put(WalletRedisKeys.WALLET_PARTY_ID + wallet.getPartyId().toString(), wallet);
		}
		redisHandler.setBatchSync(params);
	}

	public void loadWalletExtend() {
		List<WalletExtend> list = jdbcTemplate.query("SELECT * FROM T_WALLET_EXTEND", RecordObjectMapper.newInstance(WalletExtend.class));
		Map<String, Object> params = new ConcurrentHashMap<String, Object>();
		for (WalletExtend walletExtend : list) {
			params.put(
					WalletRedisKeys.WALLET_EXTEND_PARTY_ID + walletExtend.getPartyId() + walletExtend.getWallettype(),
					walletExtend);
		}
		redisHandler.setBatchSync(params);
	}
	
	public void loadExchangeRate() {
		List<ExchangeRate> list = jdbcTemplate.query("SELECT * FROM T_EXCHANGE_RATE", RecordObjectMapper.newInstance(ExchangeRate.class));
		Map<String, Map<String, ExchangeRate>> cacheMap = new ConcurrentHashMap<String, Map<String, ExchangeRate>>();

		for (ExchangeRate exchangeRate : list) {
			if (cacheMap.containsKey(exchangeRate.getOut_or_in())) {
				Map<String, ExchangeRate> map = cacheMap.get(exchangeRate.getOut_or_in());
				map.put(exchangeRate.getCurrency(), exchangeRate);
				cacheMap.put(exchangeRate.getOut_or_in(), map);
			} else {
				Map<String, ExchangeRate> map = new ConcurrentHashMap<String, ExchangeRate>();
				map.put(exchangeRate.getCurrency(), exchangeRate);
				cacheMap.put(exchangeRate.getOut_or_in(), map);
			}
			redisHandler.setSync(RedisKeys.EXCHANGE_RATE_ID + exchangeRate.getId().toString(), exchangeRate);
			redisHandler.setSync(RedisKeys.EXCHANGE_RATE_CURRENCY + exchangeRate.getCurrency(), exchangeRate);
		}

		for (Entry<String, Map<String, ExchangeRate>> entry : cacheMap.entrySet()) {
			redisHandler.setSync(RedisKeys.EXCHANGE_RATE_OUTORIN + entry.getKey(), entry.getValue());
		}
	}
	
	public void loadUserRateConfig() {
		List<UserRateConfig> list = jdbcTemplate.query("SELECT * FROM T_USER_RATE_CONFIG", 
				RecordObjectMapper.newInstance(UserRateConfig.class));
		Map<String, Object> params = new ConcurrentHashMap<String, Object>();
		for (UserRateConfig config : list) {
			params.put(RedisKeys.USER_RATE_CONFIG_PARTY_ID + config.getPartyId().toString(), config);
		}
		redisHandler.setBatchSync(params);
	}
	
	public void loadWalletLog() {
		List<WalletLog> list = jdbcTemplate.query("SELECT * FROM T_WALLET_LOG", RecordObjectMapper.newInstance(WalletLog.class));
		Map<String, Object> params = new ConcurrentHashMap<String, Object>();
		for (WalletLog walletLog : list) {
			params.put(RedisKeys.WALLET_LOG_ORDERNO + walletLog.getOrder_no(), walletLog);
		}
		redisHandler.setBatchSync(params);
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public void setRedisHandler(RedisHandler redisHandler) {
		this.redisHandler = redisHandler;
	}

}
