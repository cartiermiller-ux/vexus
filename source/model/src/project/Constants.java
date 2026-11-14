package project;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import kernel.util.PropertiesUtil;

public class Constants {
	/*
	 * 角色
	 */
	public static final String SECURITY_ROLE_ROOT = "ROOT";
	public static final String SECURITY_ROLE_ADMIN = "ADMIN";
	public static final String SECURITY_ROLE_FINANCE = "FINANCE";
	public static final String SECURITY_ROLE_CUSTOMER = "CUSTOMER";
	public static final String SECURITY_ROLE_MAINTAINER = "MAINTAINER";
	public static final String SECURITY_ROLE_AGENT = "AGENT";
	public static final String SECURITY_ROLE_C2C = "C2C";

	public static final String SECURITY_ROLE_MEMBER = "MEMBER";
	public static final String SECURITY_ROLE_GUEST = "GUEST";
	public static final String SECURITY_ROLE_TEST = "TEST";
	public static final String SECURITY_ROLE_OTCUSER = "OTCUSER";
	public static final String SECURITY_ROLE_INSIDER = "INSIDER";
	public static final String SECURITY_ROLE_AGENTLOW = "AGENTLOW";	
//	public static final String SECURITY_ROLE_DEBUG = "DEBUG"; // debug用户

	public static Map<String, String> ROLE_MAP = new HashMap<String, String>();
	
	static {
		ROLE_MAP.put(SECURITY_ROLE_ROOT, "超级管理员");
		ROLE_MAP.put(SECURITY_ROLE_ADMIN, "管理员");
		ROLE_MAP.put(SECURITY_ROLE_FINANCE, "对账专员");
		ROLE_MAP.put(SECURITY_ROLE_CUSTOMER, "客服");
		ROLE_MAP.put(SECURITY_ROLE_MAINTAINER, "运维");
		ROLE_MAP.put(SECURITY_ROLE_AGENT, "代理商");
		ROLE_MAP.put(SECURITY_ROLE_C2C, "C2C管理员");

		ROLE_MAP.put(SECURITY_ROLE_MEMBER, "正式用户");
		ROLE_MAP.put(SECURITY_ROLE_GUEST, "演示用户");
		ROLE_MAP.put(SECURITY_ROLE_TEST, "试用用户");
		ROLE_MAP.put(SECURITY_ROLE_OTCUSER, "承兑商");
		ROLE_MAP.put(SECURITY_ROLE_INSIDER, "内部专员");
		ROLE_MAP.put(SECURITY_ROLE_AGENTLOW, "代理商");
//		ROLE_MAP.put(SECURITY_ROLE_DEBUG, "debug用户");
	};
	
	public static final String WEB_URL = PropertiesUtil.getProperty("web_url");

	public static final String IMAGES_DIR = PropertiesUtil.getProperty("images.dir");
	
	/**
	 * 币种
	 */

	/**
	 * 交易账户(USDT)
	 */
	public static final String WALLET = "USDT";
	/**
	 * BTC
	 */
	public static final String WALLETEXTEND_BTC = "BTC";
	/**
	 * ETH
	 */
	public static final String WALLETEXTEND_ETH = "ETH";

	/**
	 * XRP
	 */
	public static final String WALLETEXTEND_XRP = "XRP";
	/**
	 * EOS
	 */
	public static final String WALLETEXTEND_EOS = "EOS";
	/**
	 * LTC
	 */
	public static final String WALLETEXTEND_LTC = "LTC";
	/**
	 * MLCC
	 */
	public static final String WALLETEXTEND_MLCC = "MLCC";

	/**
	 * dapp模块专用
	 * 
	 */
	/**
	 * 用户钱包USDT剩余金额--钱包映射数值
	 */
	public static final String WALLETEXTEND_DAPP_USDT_USER = "USDT_USER";

	/**
	 * 用户钱包ETH剩余金额--钱包映射数值
	 */
	public static final String WALLETEXTEND_DAPP_ETH_USER = "ETH_USER";

	/**
	 * ETH 收益账户
	 */
	public static final String WALLETEXTEND_DAPP_ETH = "ETH_DAPP";  
	
	/**
	 * ETH 交易所账户
	 */
	public static final String WALLETEXTEND_EXCHANGE_ETH = "eth";

	/**
	 * USDT 质押账户
	 */
	public static final String WALLETEXTEND_DAPP_USDT = "USDT_DAPP";
	
	/**
	 * 体验金账户
	 */
	public static final String WALLETEXTEND_EXPERIENCE_GOLD = "EXPERIENCE_GOLD";

	public static Map<String, String> WALLETEXTEND_DAPP_CN = new HashMap<String, String>();
	

	static {
		WALLETEXTEND_DAPP_CN.put(WALLETEXTEND_DAPP_USDT_USER, "用户钱包USDT映射");
		WALLETEXTEND_DAPP_CN.put(WALLETEXTEND_DAPP_ETH_USER, "用户钱包ETH映射");
		WALLETEXTEND_DAPP_CN.put(WALLETEXTEND_DAPP_ETH, "收益账户(ETH)");
		WALLETEXTEND_DAPP_CN.put(WALLETEXTEND_DAPP_USDT, "质押账户(USDT)");
	};
	
	public static Map<String, String> WALLETEXTEND = new HashMap<String, String>();

	static {
		WALLETEXTEND.put(WALLETEXTEND_BTC, "BTC");
		WALLETEXTEND.put(WALLETEXTEND_ETH, "ETH");
		WALLETEXTEND.put(WALLETEXTEND_XRP, "XRP");
		WALLETEXTEND.put(WALLETEXTEND_EOS, "EOS");
		WALLETEXTEND.put(WALLETEXTEND_LTC, "LTC");
		WALLETEXTEND.put(WALLETEXTEND_MLCC, "MLCC");
	};

	/*
	 * 资金变更日志类型
	 */
	/**
	 * 充值
	 */
	public static final String MONEYLOG_CATEGORY_RECHARGE = "recharge";
	/**
	 * 提现
	 */
	public static final String MONEYLOG_CATEGORY_WITHDRAW = "withdraw";
//
	public static final String MONEYLOG_CATEGORY_ORDER = "order";
	/**
	 * 自动机器人（默认）{open:开启，close:关闭}
	 */
	public static final String AUTO_ROBOT = "close";
	/**
	 * 合约交易
	 */
	public static final String MONEYLOG_CATEGORY_CONTRACT = "contract";
	/**
	 * 法币交易
	 */
	public static final String MONEYLOG_CATEGORY_COIN = "coin";

	/**
	 * 币币交易
	 */
	public static final String MONEYLOG_CATEGORY_EXCHANGE = "exchange";
	/**
	 * 矿机交易
	 */
	public static final String MONEYLOG_CATEGORY_MINER = "miner";
	
	/**
	 * 质押借币
	 */
	public static final String MONEYLOG_CATEGORY_LOAN = "loan";
	
	/**
	 * 基金交易
	 */
	public static final String MONEYLOG_CATEGORY_FUND = "fund";
	/**
	 * 质押2.0交易
	 */
	public static final String MONEYLOG_CATEGORY_GALAXY = "galaxy";
	/**
	 * c2c交易
	 */
	public static final String MONEYLOG_CATEGORY_C2C = "c2c";
	/**
	 * otc交易
	 */
	public static final String MONEYLOG_CATEGORY_OTC = "otc";
	/**
	 * ico交易
	 */
	public static final String MONEYLOG_CATEGORY_ICO = "ico";
	/**
	 * 币币杠杆交易
	 */
	public static final String MONEYLOG_CATEGORY_EXCHANGE_LEVER = "exchange_lever";
	/**
	 * 系统奖励
	 */
	public static final String MONEYLOG_CATEGORY_REWARD = "reward";
	/**
	 * 签到奖励
	 */
	public static final String MONEYLOG_CATEGORY_SIGN_IN = "sign_in";
	/**
	 * 活动交易
	 */
	public static final String MONEYLOG_CATEGORY_ACTIVITY = "activity";
	/**
	 * 债权
	 */
	public static final String MONEYLOG_CATEGORY_BOND = "bond";
	
	/**
	 * 体验金
	 */
	public static final String MONEYLOG_EXPERIENCE_GOLD = "experience_gold";

	public static Map<String, String> MONEYLOG_CATEGORY = new HashMap<String, String>();

	static {
		MONEYLOG_CATEGORY.put(MONEYLOG_CATEGORY_CONTRACT, "合约交易");
		MONEYLOG_CATEGORY.put(MONEYLOG_CATEGORY_COIN, "法币交易");
	};

	/**
	 * 充币
	 */
	public static final String MONEYLOG_CONTENT_RECHARGE = "recharge";
	/**
	 * 提币
	 */
	public static final String MONEYLOG_CONTENT_WITHDRAW = "withdraw";
	/**
	 * 永续合约平仓
	 */
	public static final String MONEYLOG_CONTENT_CONTRACT_CLOSE = "contract_close";
	/**
	 * 永续合约建仓
	 */
	public static final String MONEYLOG_CONTENT_CONTRACT_OPEN = "contract_open";
	/**
	 * 交割合约建仓
	 */
	public static final String DELIVERY_MONEYLOG_CONTENT_CONTRACT_OPEN = "delivery_contract_open";
	/**
	 * 交割合约平仓
	 */
	public static final String DELIVERY_MONEYLOG_CONTENT_CONTRACT_CLOSE = "delivery_contract_close";
	/**
	 * 永续合约撤单
	 */

	public static final String MONEYLOG_CONTENT_CONTRACT_CONCEL = "contract_cancel";
	/**
	 * 手续费
	 */
	public static final String MONEYLOG_CONTENT_FEE = "fee";
	/**
	 * 币币买入
	 */
	public static final String MONEYLOG_CONTENT_EXCHANGE_OPEN = "exchange_open";

	/**
	 * 币币卖出
	 */
	public static final String MONEYLOG_CONTENT_EXCHANGE_CLOSE = "exchange_close";
	/**
	 * 币币取消
	 */
	public static final String MONEYLOG_CONTENT_EXCHANGE_CANCEL = "exchange_cancel";
	/**
	 * 矿机买入
	 */
	public static final String MONEYLOG_CONTENT_MINER_BUY = "miner_buy";
	/**
	 * 矿机退回本金
	 */
	public static final String MONEYLOG_CONTENT_MINER_BACK = "miner_back";
	
	/**
	 * 质押2.0下单
	 */
	public static final String MONEYLOG_CONTENT_GALAXY_BUY = "galaxy_buy";
	
	/**
	 * 质押2.0赎回
	 */
	public static final String MONEYLOG_CONTENT_GALAXY_BACK = "galaxy_back";
	
	/**
	 * 质押2.0推荐收益
	 */
	public static final String MONEYLOG_CONTENT_GALAXY_RECOM_PROFIT = "galaxy_recom_profit";
	
	/**
	 * 质押2.0收益
	 */
	public static final String MONEYLOG_CONTENT_GALAXY_PROFIT = "galaxy_profit";
	
	/**
	 * 质押借币 - 冻结
	 */
	public static final String MONEYLOG_CONTENT_LOAN_FROZEN = "loan_frozen";
	
	/**
	 * 质押借币 - 解冻
	 */
	public static final String MONEYLOG_CONTENT_LOAN_THAW = "loan_thaw";
	
	/**
	 * 质押借币 - 强平
	 */
	public static final String MONEYLOG_CONTENT_LOAN_CLOSEOUT = "loan_closeout";
	
	/**
	 * 质押借币 - 借款
	 */
	public static final String MONEYLOG_CONTENT_LOAN_ADD = "loan_add";
	
	/**
	 * 质押借币 - 还款
	 */
	public static final String MONEYLOG_CONTENT_LOAN_REPAY = "loan_repay";
	
	/**
	 * 矿机收益
	 */
	public static final String MONEYLOG_CONTENT_MINER_PROFIT = "miner_profit";
	
	/**
	 * 矿机推荐收益
	 */
	public static final String MONEYLOG_CONTENT_MINER_RECOM_PROFIT = "miner_recom_profit";

	/**
	 * 团队奖励
	 */
	public static final String MONEYLOG_CONTENT_MINER_TEAM_PROFIT = "miner_team_profit";
	/**
	 * 社区奖励
	 */
	public static final String MONEYLOG_CONTENT_MINER_COMMUNITY_PROFIT = "miner_community_profit";
	
	/**
	 * 理财购买
	 */
	public static final String MONEYLOG_CONTENT_FINANCE_BUY = "finance_buy";
	
	/**
	 * 理财赎回
	 */
	public static final String MONEYLOG_CONTENT_FINANCE_BACK = "finance_back";
	/**
	 * 理财收益
	 */
	public static final String MONEYLOG_CONTENT_FINANCE_PROFIT = "finance_profit";
	/**
	 * 理财推荐收益
	 */
	public static final String MONEYLOG_CONTENT_FINANCE_RECOM_PROFIT = "finance_recom_profit";

	/**
	 * 跟单基金策略买入
	 */
	public static final String MONEYLOG_CONTENT_FUND_OPEN = "fund_open";
	/**
	 * 跟单基金策略平仓
	 */
	public static final String MONEYLOG_CONTENT_FUND_CLOSE = "fund_close";
	/**
	 * 跟单基金策略手续费
	 */
	public static final String MONEYLOG_CONTENT_FUND_FEE = "fund_fee";
	/**
	 * c2c卖币
	 */
	public static final String MONEYLOG_CONTENT_C2C_SELL = "c2c_sell";
	/**
	 * c2c买币
	 */
	public static final String MONEYLOG_CONTENT_C2C_BUY = "c2c_buy";
	/**
	 * c2c订单取消
	 */
	public static final String MONEYLOG_CONTENT_C2C_CANCEL = "c2c_cancel";
	/**
	 * otc卖币
	 */
	public static final String MONEYLOG_CONTENT_OTC_SELL = "otc_sell";
	/**
	 * otc买币
	 */
	public static final String MONEYLOG_CONTENT_OTC_BUY = "otc_buy";
	/**
	 * otc订单取消
	 */
	public static final String MONEYLOG_CONTENT_OTC_CANCEL = "otc_cancel";
	/**
	 * 跟单手续费
	 */
	public static final String MONEYLOG_CONTENT_FOLLOW_UP_FEE = "follow_up_fee";
	/**
	 * ICO中签
	 */
	public static final String MONEYLOG_CONTENT_ICO_DRAW = "ico_draw_win";
	/**
	 * ICO购买
	 */
	public static final String MONEYLOG_CONTENT_ICO_BUY = "ico_buy";
	/**
	 * ICO上市
	 */
	public static final String MONEYLOG_CONTENT_ICO_MARKET = "ico_market";
	/**
	 * 币币杠杆平仓
	 */
	public static final String MONEYLOG_CONTENT_EXCHANGE_LEVER_CLOSE = "exchange_lever_close";
	/**
	 * 币币杠杆利息
	 */
	public static final String MONEYLOG_CONTENT_EXCHANGE_LEVER_INTEREST = "exchange_lever_interest";
	/**
	 * 币币杠杆开仓
	 */
	public static final String MONEYLOG_CONTENT_EXCHANGE_LEVER_OPEN = "exchange_lever_open";
	/**
	 * 奖励
	 */
	public static final String MONEYLOG_CONTENT_REWARD = "reward";
	/**
	 * 签到奖励
	 */
	public static final String MONEYLOG_CONTENT_SIGN_IN_PROFIT = "sign_in_profit";
	/**
	 * 活动解锁
	 */
	public static final String MONEYLOG_CONTENT_ACTIVITY_UNLOCK = "activity_unlock";
	/**
	 * 系统锁定转移
	 */
	public static final String MONEYLOG_CONTENT_SYS_LOCK = "sys_lock";
	/**
	 * 系统增加锁定金额
	 */
	public static final String MONEYLOG_CONTENT_SYS_MONEY_ADD_LOCK = "sys_add_lock";
	/**
	 * 系统增加锁定金额
	 */
	public static final String MONEYLOG_CONTENT_SYS_MONEY_SUB_LOCK = "sys_sub_lock";
	/**
	 * 债权买入
	 */
	public static final String MONEYLOG_CONTENT_BOND_BUY = "bond_buy";
	/**
	 * 债权退回本金
	 */
	public static final String MONEYLOG_CONTENT_BOND_BACK = "bond_back";
	/**
	 * 债权收益
	 */
	public static final String MONEYLOG_CONTENT_BOND_PROFIT = "bond_profit";

	public static Map<String, String> MONEYLOG_CONTENT = new HashMap<String, String>();

	static {
		MONEYLOG_CONTENT.put(MONEYLOG_CONTENT_RECHARGE, "充币");
		MONEYLOG_CONTENT.put(MONEYLOG_CONTENT_WITHDRAW, "提币");
		MONEYLOG_CONTENT.put(MONEYLOG_CONTENT_CONTRACT_CLOSE, "永续合约平仓");
		MONEYLOG_CONTENT.put(MONEYLOG_CONTENT_CONTRACT_OPEN, "永续合约建仓");
		MONEYLOG_CONTENT.put(MONEYLOG_CONTENT_CONTRACT_CONCEL, "永续合约撤单");
		MONEYLOG_CONTENT.put(MONEYLOG_CONTENT_FEE, "手续费");
		MONEYLOG_CONTENT.put(MONEYLOG_CONTENT_EXCHANGE_OPEN, "币币买入");
		MONEYLOG_CONTENT.put(MONEYLOG_CONTENT_EXCHANGE_CLOSE, "币币卖出");
		MONEYLOG_CONTENT.put(MONEYLOG_CONTENT_EXCHANGE_CANCEL, "币币取消");
		MONEYLOG_CONTENT.put(MONEYLOG_CONTENT_MINER_BUY, "矿机买入");
		MONEYLOG_CONTENT.put(MONEYLOG_CONTENT_MINER_BACK, "矿机退回本金");
		MONEYLOG_CONTENT.put(MONEYLOG_CONTENT_MINER_PROFIT, "矿机收益");
		MONEYLOG_CONTENT.put(MONEYLOG_CONTENT_MINER_RECOM_PROFIT, "矿机推荐收益");
		MONEYLOG_CONTENT.put(MONEYLOG_CONTENT_FUND_OPEN, "跟单基金策略买入");
		MONEYLOG_CONTENT.put(MONEYLOG_CONTENT_FUND_CLOSE, "跟单基金策略平仓");
		MONEYLOG_CONTENT.put(MONEYLOG_CONTENT_FUND_FEE, "跟单基金策略手续费");
		MONEYLOG_CONTENT.put(MONEYLOG_CONTENT_ICO_DRAW, "ICO中签");
		MONEYLOG_CONTENT.put(MONEYLOG_CONTENT_EXCHANGE_LEVER_CLOSE, "币币杠杆平仓");
		MONEYLOG_CONTENT.put(MONEYLOG_CONTENT_EXCHANGE_LEVER_OPEN, "币币杠杆开仓");
		MONEYLOG_CONTENT.put(MONEYLOG_CONTENT_REWARD, "奖励");
		MONEYLOG_CONTENT.put(MONEYLOG_CONTENT_SIGN_IN_PROFIT, "签到奖励");
		MONEYLOG_CONTENT.put(MONEYLOG_CONTENT_ACTIVITY_UNLOCK, "活动解锁");
		MONEYLOG_CONTENT.put(MONEYLOG_CONTENT_BOND_BUY, "债权买入");
		MONEYLOG_CONTENT.put(MONEYLOG_CONTENT_BOND_BACK, "债权退回本金");
		MONEYLOG_CONTENT.put(MONEYLOG_CONTENT_BOND_PROFIT, "债权收益");
		MONEYLOG_CONTENT.put(MONEYLOG_CONTENT_FINANCE_PROFIT, "理财收益");
		MONEYLOG_CONTENT.put(MONEYLOG_CONTENT_FINANCE_RECOM_PROFIT, "理财推荐收益");
	}
	
	/*
	 * 日志log类型
	 */
	
	/**
	 * 操作
	 */
	public static final String LOG_CATEGORY_OPERATION = "operation";
	
	/**
	 * 安全
	 */
	public static final String LOG_CATEGORY_SECURITY = "security";
	
	/**
	 * C2C
	 */
	public static final String LOG_CATEGORY_C2C = "c2c";

	/**
	 * 安全
	 */
	public static final String LOG_CATEGORY_SECURITY_CODE = "security_code";

	public static Map<String, String> LOG_CATEGORY = new HashMap<String, String>();

	static {
		LOG_CATEGORY.put(LOG_CATEGORY_OPERATION, "用户操作");
		LOG_CATEGORY.put(LOG_CATEGORY_SECURITY, "安全事件");
	};

	public static Map<String, String> CHANNELS = new HashMap<String, String>();

	static {
		CHANNELS.put("idaRechargeService", "IDA三方支付");
	};

	/**
	 * 支持的银行
	 */
	public static Map<String, String> BANK_CODE = new HashMap<String, String>();

	static {
		BANK_CODE.put("102", "中国工商银行");
		BANK_CODE.put("103", "中国农业银行");
		BANK_CODE.put("104", "中国银行");
		BANK_CODE.put("105", "中国建设银行");
		BANK_CODE.put("301", "交通银行");
		BANK_CODE.put("302", "中信银行");
		BANK_CODE.put("303", "中国光大银行");
		BANK_CODE.put("304", "华夏银行");
		BANK_CODE.put("305", "中国民生银行");
		BANK_CODE.put("306", "广东发展银行");
		BANK_CODE.put("307", "平安银行");
		BANK_CODE.put("308", "招商银行");
		BANK_CODE.put("309", "兴业银行");
		BANK_CODE.put("310", "上海浦东发展银行");
		BANK_CODE.put("315", "恒丰银行");
		BANK_CODE.put("403", "中国邮政储蓄银行");
		BANK_CODE.put("501", "汇丰银行");
		BANK_CODE.put("504", "恒生银行(中国)有限公司");
	};

	/**
	 * 支持的银行
	 */
	public static Map<String, String> WITHDRAW_METHOD = new HashMap<String, String>();

	static {
		WITHDRAW_METHOD.put("bank", "银行卡");
		WITHDRAW_METHOD.put("alipay", "支付宝");
		WITHDRAW_METHOD.put("weixin", "微信");
		WITHDRAW_METHOD.put("paypal", "PayPal");
		WITHDRAW_METHOD.put("western", "西联汇款");
		WITHDRAW_METHOD.put("swift", "SWIFT国际汇款");
		WITHDRAW_METHOD.put("USDT", "USDT");
	};

	/**
	 * 支持的国家
	 */
	public static Map<String, String> COUNTRY_CODE = new HashMap<String, String>();

	static {
		COUNTRY_CODE.put("af", "阿富汗");
		COUNTRY_CODE.put("al", "阿尔巴尼亚");
		COUNTRY_CODE.put("dz", "阿尔及利亚");
		COUNTRY_CODE.put("as", "美属萨摩亚");
		COUNTRY_CODE.put("ad", "安道尔");
		COUNTRY_CODE.put("ao", "安哥拉");
		COUNTRY_CODE.put("ai", "安圭拉岛");
		COUNTRY_CODE.put("ag", "安提瓜和巴布达");
		COUNTRY_CODE.put("ar", "阿根廷");
		COUNTRY_CODE.put("am", "亚美尼亚");
		COUNTRY_CODE.put("aw", "阿鲁巴");
		COUNTRY_CODE.put("au", "澳大利亚");
		COUNTRY_CODE.put("at", "奥地利");
		COUNTRY_CODE.put("az", "阿塞拜疆");
		COUNTRY_CODE.put("bs", "巴哈马群岛");
		COUNTRY_CODE.put("bh", "巴林");
		COUNTRY_CODE.put("bd", "孟加拉共和国");
		COUNTRY_CODE.put("bb", "巴巴多斯");
		COUNTRY_CODE.put("by", "白俄罗斯");
		COUNTRY_CODE.put("be", "比利时");
		COUNTRY_CODE.put("bz", "伯利兹");
		COUNTRY_CODE.put("bj", "贝宁");
		COUNTRY_CODE.put("bm", "百慕大群岛");
		COUNTRY_CODE.put("bt", "不丹");
		COUNTRY_CODE.put("bo", "玻利维亚");
		COUNTRY_CODE.put("ba", "波黑(波斯尼亚和黑塞哥维那)");
		COUNTRY_CODE.put("bw", "博茨瓦纳");
		COUNTRY_CODE.put("br", "巴西");
		COUNTRY_CODE.put("bn", "文莱达鲁萨兰国");
		COUNTRY_CODE.put("bg", "保加利亚");
		COUNTRY_CODE.put("bf", "布基纳法索");
		COUNTRY_CODE.put("bi", "布隆迪");
		COUNTRY_CODE.put("kh", "柬埔寨");
		COUNTRY_CODE.put("cm", "喀麦隆");
		COUNTRY_CODE.put("ca", "加拿大");
		COUNTRY_CODE.put("cv", "佛得角");
		COUNTRY_CODE.put("ky", "开曼群岛");
		COUNTRY_CODE.put("cf", "中非共和国");
		COUNTRY_CODE.put("td", "乍得");
		COUNTRY_CODE.put("cl", "智利");
		COUNTRY_CODE.put("cn", "中国");
		COUNTRY_CODE.put("co", "哥伦比亚");
		COUNTRY_CODE.put("km", "科摩罗");
		COUNTRY_CODE.put("cd", "刚果");
		COUNTRY_CODE.put("cg", "刚果民主共和国");
		COUNTRY_CODE.put("ck", "库克群岛");
		COUNTRY_CODE.put("cr", "哥斯达黎加");
		COUNTRY_CODE.put("hr", "克罗地亚");
		COUNTRY_CODE.put("cu", "古巴");
		COUNTRY_CODE.put("cy", "塞浦路斯");
		COUNTRY_CODE.put("cz", "捷克共和国");
		COUNTRY_CODE.put("dk", "丹麦");
		COUNTRY_CODE.put("dj", "吉布提");
		COUNTRY_CODE.put("dm", "多米尼克");
		COUNTRY_CODE.put("do", "多米尼加共和国");
		COUNTRY_CODE.put("sa", "迪拜");
		COUNTRY_CODE.put("ec", "厄瓜多尔");
		COUNTRY_CODE.put("eg", "埃及");
		COUNTRY_CODE.put("sv", "萨尔瓦多");
		COUNTRY_CODE.put("gq", "赤道几内亚");
		COUNTRY_CODE.put("er", "厄立特里亚");
		COUNTRY_CODE.put("ee", "爱沙尼亚");
		COUNTRY_CODE.put("et", "埃塞俄比亚");
		COUNTRY_CODE.put("fk", "福克兰群岛");
		COUNTRY_CODE.put("fo", "法罗群岛");
		COUNTRY_CODE.put("fj", "斐济");
		COUNTRY_CODE.put("fi", "芬兰");
		COUNTRY_CODE.put("fr", "法国");
		COUNTRY_CODE.put("gf", "法属圭亚那");
		COUNTRY_CODE.put("pf", "法属玻利尼西亚");
		COUNTRY_CODE.put("ga", "加蓬");
		COUNTRY_CODE.put("gm", "冈比亚");
		COUNTRY_CODE.put("ge", "格鲁吉亚");
		COUNTRY_CODE.put("de", "德国");
		COUNTRY_CODE.put("gh", "加纳");
		COUNTRY_CODE.put("gi", "直布罗陀");
		COUNTRY_CODE.put("gr", "希腊");
		COUNTRY_CODE.put("gl", "格陵兰岛");
		COUNTRY_CODE.put("gd", "格林纳达");
		COUNTRY_CODE.put("gp", "瓜德罗普");
		COUNTRY_CODE.put("gu", "关岛");
		COUNTRY_CODE.put("gt", "危地马拉");
		COUNTRY_CODE.put("gg", "根西");
		COUNTRY_CODE.put("gn", "几内亚");
		COUNTRY_CODE.put("gw", "几内亚比绍共和国");
		COUNTRY_CODE.put("gy", "圭亚那");
		COUNTRY_CODE.put("ht", "海地");
		COUNTRY_CODE.put("hn", "洪都拉斯");
		COUNTRY_CODE.put("hk", "中国香港");
		COUNTRY_CODE.put("hu", "匈牙利");
		COUNTRY_CODE.put("is", "冰岛");
		COUNTRY_CODE.put("in", "印度");
		COUNTRY_CODE.put("id", "印度尼西亚");
		COUNTRY_CODE.put("ir", "伊朗");
		COUNTRY_CODE.put("iq", "伊拉克");
		COUNTRY_CODE.put("ie", "爱尔兰");
		COUNTRY_CODE.put("im", "马恩");
		COUNTRY_CODE.put("il", "以色列");
		COUNTRY_CODE.put("it", "意大利");
		COUNTRY_CODE.put("jm", "牙买加");
		COUNTRY_CODE.put("jp", "日本");
		COUNTRY_CODE.put("je", "泽西");
		COUNTRY_CODE.put("jo", "约旦");
		COUNTRY_CODE.put("kz", "哈萨克斯坦");
		COUNTRY_CODE.put("ke", "肯尼亚");
		COUNTRY_CODE.put("kp", "朝鲜");
		COUNTRY_CODE.put("kr", "韩国");
		COUNTRY_CODE.put("xk", "科索沃");
		COUNTRY_CODE.put("kw", "科威特");
		COUNTRY_CODE.put("kg", "吉尔吉斯斯坦");
		COUNTRY_CODE.put("la", "老挝人民民主共和国");
		COUNTRY_CODE.put("lv", "拉脱维亚");
		COUNTRY_CODE.put("lb", "黎巴嫩");
		COUNTRY_CODE.put("ls", "莱索托");
		COUNTRY_CODE.put("lr", "利比里亚");
		COUNTRY_CODE.put("ly", "利比亚");
		COUNTRY_CODE.put("li", "列支敦斯登");
		COUNTRY_CODE.put("lt", "立陶宛");
		COUNTRY_CODE.put("lu", "卢森堡");
		COUNTRY_CODE.put("mo", "中国澳门");
		COUNTRY_CODE.put("mk", "马其顿王国");
		COUNTRY_CODE.put("mg", "马达加斯加");
		COUNTRY_CODE.put("mw", "马拉维");
		COUNTRY_CODE.put("my", "马来西亚");
		COUNTRY_CODE.put("mv", "马尔代夫");
		COUNTRY_CODE.put("ml", "马里");
		COUNTRY_CODE.put("mt", "马耳他");
		COUNTRY_CODE.put("mq", "马提尼克");
		COUNTRY_CODE.put("mr", "毛里塔尼亚");
		COUNTRY_CODE.put("mu", "毛里求斯");
		COUNTRY_CODE.put("mx", "墨西哥");
		COUNTRY_CODE.put("md", "摩尔多瓦");
		COUNTRY_CODE.put("mc", "摩纳哥");
		COUNTRY_CODE.put("mn", "蒙古");
		COUNTRY_CODE.put("me", "黑山共和国");
		COUNTRY_CODE.put("ms", "蒙特塞拉特岛");
		COUNTRY_CODE.put("ma", "摩洛哥");
		COUNTRY_CODE.put("mz", "莫桑比克");
		COUNTRY_CODE.put("mm", "缅甸");
		COUNTRY_CODE.put("na", "纳米比亚");
		COUNTRY_CODE.put("np", "尼泊尔");
		COUNTRY_CODE.put("nl", "荷兰");
		COUNTRY_CODE.put("nc", "新喀里多尼亚");
		COUNTRY_CODE.put("nz", "新西兰");
		COUNTRY_CODE.put("ni", "尼加拉瓜");
		COUNTRY_CODE.put("ne", "尼日尔");
		COUNTRY_CODE.put("ng", "尼日利亚");
		COUNTRY_CODE.put("no", "挪威");
		COUNTRY_CODE.put("om", "阿曼");
		COUNTRY_CODE.put("pk", "巴基斯坦");
		COUNTRY_CODE.put("pw", "帕劳");
		COUNTRY_CODE.put("pa", "巴拿马");
		COUNTRY_CODE.put("pg", "巴布亚新几内亚");
		COUNTRY_CODE.put("py", "巴拉圭");
		COUNTRY_CODE.put("pe", "秘鲁");
		COUNTRY_CODE.put("ph", "菲律宾");
		COUNTRY_CODE.put("pl", "波兰");
		COUNTRY_CODE.put("pt", "葡萄牙");
		COUNTRY_CODE.put("pr", "波多黎各");
		COUNTRY_CODE.put("ps", "巴勒斯坦");
		COUNTRY_CODE.put("qa", "卡塔尔");
		COUNTRY_CODE.put("re", "留尼旺");
		COUNTRY_CODE.put("ro", "罗马尼亚");
		COUNTRY_CODE.put("ru", "俄罗斯联邦");
		COUNTRY_CODE.put("rw", "卢旺达");
		COUNTRY_CODE.put("kn", "圣基茨和尼维斯");
		COUNTRY_CODE.put("lc", "圣卢西亚岛");
		COUNTRY_CODE.put("ws", "萨摩亚群岛");
		COUNTRY_CODE.put("sm", "圣马力诺");
		COUNTRY_CODE.put("sn", "塞内加尔");
		COUNTRY_CODE.put("rs", "塞尔维亚");
		COUNTRY_CODE.put("sc", "塞舌尔");
		COUNTRY_CODE.put("sl", "塞拉利昂");
		COUNTRY_CODE.put("sg", "新加坡");
		COUNTRY_CODE.put("sk", "斯洛伐克");
		COUNTRY_CODE.put("si", "斯洛文尼亚");
		COUNTRY_CODE.put("sb", "所罗门群岛");
		COUNTRY_CODE.put("so", "索马里");
		COUNTRY_CODE.put("za", "南非");
		COUNTRY_CODE.put("ss", "南苏丹");
		COUNTRY_CODE.put("es", "西班牙");
		COUNTRY_CODE.put("lk", "斯里兰卡");
		COUNTRY_CODE.put("sd", "苏丹");
		COUNTRY_CODE.put("sr", "苏里南");
		COUNTRY_CODE.put("sz", "斯威士兰");
		COUNTRY_CODE.put("se", "瑞典");
		COUNTRY_CODE.put("ch", "瑞士");
		COUNTRY_CODE.put("tw", "中国台湾");
		COUNTRY_CODE.put("tj", "塔吉克斯坦");
		COUNTRY_CODE.put("tz", "坦桑尼亚");
		COUNTRY_CODE.put("th", "泰国");
		COUNTRY_CODE.put("tl", "东帝汶");
		COUNTRY_CODE.put("tg", "多哥");
		COUNTRY_CODE.put("to", "汤加");
		COUNTRY_CODE.put("tt", "特立尼达和多巴哥");
		COUNTRY_CODE.put("tn", "突尼斯");
		COUNTRY_CODE.put("tr", "土耳其");
		COUNTRY_CODE.put("tm", "土库曼斯坦");
		COUNTRY_CODE.put("tc", "特克斯和凯科斯群岛");
		COUNTRY_CODE.put("ug", "乌干达");
		COUNTRY_CODE.put("ua", "乌克兰");
		COUNTRY_CODE.put("gb", "英国");
		COUNTRY_CODE.put("us", "美国");
		COUNTRY_CODE.put("uy", "乌拉圭");
		COUNTRY_CODE.put("uz", "乌兹别克斯坦");
		COUNTRY_CODE.put("vu", "瓦努阿图");
		COUNTRY_CODE.put("ve", "委内瑞拉");
		COUNTRY_CODE.put("vn", "越南");
		COUNTRY_CODE.put("vi", "英属维尔京群岛");
		COUNTRY_CODE.put("ye", "也门");
		COUNTRY_CODE.put("zm", "赞比亚");
	}

	public static Map<String, String> BLOCKCHAIN_COINS = new HashMap<String, String>();

	static {
		BLOCKCHAIN_COINS.put("USDT", "USDT");
		BLOCKCHAIN_COINS.put("BTC", "BTC");
		BLOCKCHAIN_COINS.put("ETH", "ETH");
		BLOCKCHAIN_COINS.put("HT", "HT");
		BLOCKCHAIN_COINS.put("LTC", "LTC");
	};
	public static Map<String, String> BLOCKCHAIN_COINS_NAME = new HashMap<String, String>();

	static {
		/**
		 * usdt、HT
		 */
		BLOCKCHAIN_COINS_NAME.put("ERC20", "ERC20");
		BLOCKCHAIN_COINS_NAME.put("TRC20", "TRC20");
		/**
		 * usdt
		 */
		BLOCKCHAIN_COINS_NAME.put("OMNI", "OMNI");
		/**
		 * btc
		 */
		BLOCKCHAIN_COINS_NAME.put("BTC", "BTC");
		BLOCKCHAIN_COINS_NAME.put("HBTC", "HBTC");
		/**
		 * eth
		 */
		BLOCKCHAIN_COINS_NAME.put("ETH", "ETH");
		/**
		 * ltc
		 */
		BLOCKCHAIN_COINS_NAME.put("LTC", "LTC");
		BLOCKCHAIN_COINS_NAME.put("HRC20", "HRC20");
		BLOCKCHAIN_COINS_NAME.put("HLTC", "HLTC");
	};

	/**
	 * 语言
	 */
	public static Map<String, String> LANGUAGE = new HashMap<String, String>();

	static {
		LANGUAGE.put("en", "英文");
		LANGUAGE.put("zh-CN", "简体中文");
		LANGUAGE.put("CN", "繁体中文");
		LANGUAGE.put("Japanese", "日文");
		LANGUAGE.put("Korean", "韩文");
		LANGUAGE.put("ru", "俄文");
		LANGUAGE.put("pt", "葡萄牙语");
		LANGUAGE.put("es", "西班牙语");
		LANGUAGE.put("th", "泰语");
		LANGUAGE.put("fr", "法语");
	    LANGUAGE.put("vi", "越南语");
	    LANGUAGE.put("ar", "阿拉伯语");
	    LANGUAGE.put("my", "缅甸语");
	    LANGUAGE.put("de", "德语");
	    LANGUAGE.put("ind", "印尼语");
	    LANGUAGE.put("Italy", "意大利语");
	}

	/**
	 * cms模块
	 */
	public static Map<String, String> CMS_MODEL = new HashMap<String, String>();

	static {
		CMS_MODEL.put("system", "系统");
		CMS_MODEL.put("info", "说明");
		CMS_MODEL.put("help_center", "帮助中心");
		CMS_MODEL.put("knowledge", "百科");
		CMS_MODEL.put("c2c_help_center_newbie", "C2C帮助中心_新手");
		CMS_MODEL.put("c2c_help_center_advanced", "C2C帮助中心_进阶");
		CMS_MODEL.put("c2c_help_center_advertiser", "C2C帮助中心_广告方");
	}
	
	/**
	 * banner模块
	 */
	public static Map<String, String> BANNER_MODEL = new HashMap<String, String>();

	static {
		BANNER_MODEL.put("top", "轮播");
		BANNER_MODEL.put("other", "其他");
		BANNER_MODEL.put("poster", "海报");
	}
	/*
	 * syslog类型
	 */
	/**
	 * T1入账任务
	 */
	public static final String SYSLOG_CATEGORY_1ENTERIN = "1enterin";

	/**
	 * 用户数据处理任务
	 */
	public static final String SYSLOG_CATEGORY_USERDATAPROCESSING = "userdataprocessing";

	// 系统日志
	public static Map<String, String> SYS_LOG_CATEGORY = new HashMap<String, String>();
	static {
		SYS_LOG_CATEGORY.put(SYSLOG_CATEGORY_1ENTERIN, "T1入账任务");
		SYS_LOG_CATEGORY.put(SYSLOG_CATEGORY_USERDATAPROCESSING, "用户数据处理任务");

	};

	public static String LEVEL_ERROR = "error";
	public static String LEVEL_WARN = "warn";
	public static String LEVEL_INFO = "info";
	// 系统日志类型
	public static Map<String, String> SYS_LOG_LEVEL = new HashMap<String, String>();
	static {
		SYS_LOG_LEVEL.put(LEVEL_ERROR, "错误");
		SYS_LOG_LEVEL.put(LEVEL_WARN, "警告");
		SYS_LOG_LEVEL.put(LEVEL_INFO, "信息");
	};

	public static String NOTIFY_TYPE_EMAIL = "email";
	public static String NOTIFY_TYPE_PHONE = "phone";
	public static Map<String, String> NOTIFY_TYPE = new HashMap<String, String>();
	static {
		NOTIFY_TYPE.put(NOTIFY_TYPE_EMAIL, "邮箱");
		NOTIFY_TYPE.put(NOTIFY_TYPE_PHONE, "手机");
	};

	public static String NOTIFY_STATUS_OPEN = "1";
	public static String NOTIFY_STATUS_CLOSE = "0";
	public static Map<String, String> NOTIFY_STATUS = new HashMap<String, String>();
	static {
		NOTIFY_STATUS.put(NOTIFY_STATUS_OPEN, "启用");
		NOTIFY_STATUS.put(NOTIFY_STATUS_CLOSE, "停止");
	};
	/**
	 * 默认汇率转化为in
	 */
	public static String OUT_OR_IN_DEFAULT = "in";

	/**
	 * 理财产品
	 */
	public static final String MONEYLOG_FINANCE = "finance";
	/**
	 * 理财产品交易
	 */
	public static final String MONEYLOG_CATEGORY_FINANCE = "finance";

	public final static String PROFIT_LOSS_TYPE_PROFIT = "profit";
	public final static String PROFIT_LOSS_TYPE_LOSS = "loss";
	public final static String PROFIT_LOSS_TYPE_BUY_PROFIT = "buy_profit";
	public final static String PROFIT_LOSS_TYPE_SELL_PROFIT = "sell_profit";
	/**
	 * 买多盈利并且买空亏损
	 */
	public final static String PROFIT_LOSS_TYPE_BUY_PROFIT_SELL_LOSS = "buy_profit_sell_loss";
	/**
	 * 买空盈利并且买多亏损
	 */
	public final static String PROFIT_LOSS_TYPE_SELL_PROFIT_BUY_LOSS = "sell_profit_buy_loss";

	public static Map<String, String> PROFIT_LOSS_TYPE = new LinkedHashMap<String, String>();
	static {
		PROFIT_LOSS_TYPE.put(PROFIT_LOSS_TYPE_PROFIT, "盈利");
		PROFIT_LOSS_TYPE.put(PROFIT_LOSS_TYPE_LOSS, "亏损");
		PROFIT_LOSS_TYPE.put(PROFIT_LOSS_TYPE_BUY_PROFIT, "买多盈利");
		PROFIT_LOSS_TYPE.put(PROFIT_LOSS_TYPE_SELL_PROFIT, "买空盈利");
		PROFIT_LOSS_TYPE.put(PROFIT_LOSS_TYPE_BUY_PROFIT_SELL_LOSS, "买多盈利并且买空亏损");
		PROFIT_LOSS_TYPE.put(PROFIT_LOSS_TYPE_SELL_PROFIT_BUY_LOSS, "买空盈利并且买多亏损");
	};

	public final static String OTC_PAY_TYPE_WECHAT = "wechat";
	public final static String OTC_PAY_TYPE_ALIPAY = "alipay";
	public final static String OTC_PAY_TYPE_BANK = "bank";
	public static Map<String, String> OTC_PAY_TYPE = new HashMap<String, String>();

	static {
		OTC_PAY_TYPE.put(OTC_PAY_TYPE_WECHAT, "微信");
		OTC_PAY_TYPE.put(OTC_PAY_TYPE_ALIPAY, "支付宝");
		OTC_PAY_TYPE.put(OTC_PAY_TYPE_BANK, "银行卡");
	};
	/**
	 * 币币
	 */
	public final static String OPTIONAL_MODULE_COIN = "coin";
	/**
	 * 交割
	 */
	public final static String OPTIONAL_MODULE_FUTURES = "futures";
	/**
	 * 合约
	 */
	public final static String OPTIONAL_MODULE_CONTRACT = "contract";
	public static List<String> OPTIONAL_MODULE = new ArrayList<String>();
	static {
		OPTIONAL_MODULE.add(OPTIONAL_MODULE_COIN);
		OPTIONAL_MODULE.add(OPTIONAL_MODULE_FUTURES);
		OPTIONAL_MODULE.add(OPTIONAL_MODULE_CONTRACT);
	};
	
//	/**
//	 * C2C支付方式类型：0其它/1银行卡/2虚拟货币/3微信/4支付宝/5PayPal/6西联汇款/7SWIFT国际汇款
//	 */
//	public static Map<String, String> C2C_PAYMENT_METHOD_TYPE = new HashMap<String, String>();
//	
//	static {
//		C2C_PAYMENT_METHOD_TYPE.put("0", "其它");
//		C2C_PAYMENT_METHOD_TYPE.put("1", "银行卡");
//		C2C_PAYMENT_METHOD_TYPE.put("2", "虚拟货币");
//		C2C_PAYMENT_METHOD_TYPE.put("3", "微信");
//		C2C_PAYMENT_METHOD_TYPE.put("4", "支付宝");
//		C2C_PAYMENT_METHOD_TYPE.put("5", "PayPal");
//		C2C_PAYMENT_METHOD_TYPE.put("6", "西联汇款");
//		C2C_PAYMENT_METHOD_TYPE.put("7", "SWIFT国际汇款");
//	};
	
//	/**
//	 * C2C广告 支付时效
//	 */
//	public static Map<String, String> C2C_ADVERT_EXPIRE_TIME = new HashMap<String, String>();
//	
//	static {
//		C2C_ADVERT_EXPIRE_TIME.put("15", "15分钟");
//		C2C_ADVERT_EXPIRE_TIME.put("30", "30分钟");
//		C2C_ADVERT_EXPIRE_TIME.put("45", "45分钟");
//		C2C_ADVERT_EXPIRE_TIME.put("60", "60分钟");
//		C2C_ADVERT_EXPIRE_TIME.put("120", "120分钟");
//	};

}
