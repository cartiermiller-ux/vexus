package project.web.api;

import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import kernel.exception.BusinessException;
import kernel.util.Arith;
import kernel.util.StringUtils;
import kernel.web.BaseAction;
import kernel.web.ResultObject;
import project.Constants;
import project.data.DataService;
import project.data.model.Realtime;
import project.item.ItemService;
import project.item.model.Item;
import project.wallet.Wallet;
import project.wallet.WalletExtend;
import project.wallet.WalletLogService;
import project.wallet.WalletService;

/**
 * 钱包
 */
@RestController
@CrossOrigin
public class WalletController extends BaseAction {

	private Logger logger = LoggerFactory.getLogger(WalletController.class);

	@Autowired
	protected WalletService walletService;
	@Autowired
	protected ItemService itemService;
	@Autowired
	private DataService dataService;
	@Autowired
	protected WalletLogService walletLogService;

	private final String action = "/api/wallet!";

	/**
	 * 钱包账户资产（usdt、btc、eth）
	 */
	@RequestMapping(action + "get.action")
	public Object get(HttpServletRequest request) throws IOException {
		return this.getWalletExtends(request, false);
	}

	/**
	 * 钱包账户资产（币对）
	 */
	@RequestMapping(action + "getPairs.action")
	public Object getPairs(HttpServletRequest request) throws IOException {		
		String pairs = request.getParameter("pairs");
		
		ResultObject ret = (ResultObject) this.getWalletExtends(request, true);
		
		ResultObject resultObject = new ResultObject();
		
		try {
			
			if (StringUtils.isEmptyString(pairs) || !pairs.contains("/")) {
				throw new BusinessException("参数错误");
			}
			
			List<String> symbolList = Arrays.asList(pairs.split("/"));
			if (null == symbolList || 2 != symbolList.size()) {
				throw new BusinessException("参数错误");
			}
			String symbol1 = symbolList.get(0).toLowerCase();
			String symbol2 = symbolList.get(1).toLowerCase();
			
			// 获取所有币种的全称
			List<Item> list_it = this.itemService.cacheGetByMarket("");
			if (null == list_it || 0 == list_it.size()) {
				list_it = new ArrayList<Item>();
			}
			Map<String, String> itemMap = new HashMap<String, String>();
			for (int i = 0; i < list_it.size(); i++) {
				itemMap.put(list_it.get(i).getSymbol(), list_it.get(i).getSymbolFullName());
			}
			itemMap.put("usdt", "Usdt");
			
			resultObject.setCode(ret.getCode());
			resultObject.setMsg(ret.getMsg());
			if ("0".equals(ret.getCode())) {
				
				Map<String, Object> mapRetDefault = new LinkedHashMap<String, Object>();
				mapRetDefault.put("usdt", 0.0D);
				mapRetDefault.put("no_zero", new ArrayList<Map<String, Object>>());
									
				Map<String, Object> mapDefault1 = new HashMap<String, Object>();
				mapDefault1.put("symbol", symbol1);
				mapDefault1.put("full_name", StringUtils.isEmptyString(itemMap.get(symbol1)) ? symbol1.toUpperCase() : itemMap.get(symbol1));
				mapDefault1.put("volume", 0.0D);
				mapDefault1.put("usdt", 0.0D);
				Map<String, Object> mapDefault2 = new HashMap<String, Object>();
				mapDefault2.put("symbol", symbol2);
				mapDefault1.put("full_name", StringUtils.isEmptyString(itemMap.get(symbol2)) ? symbol2.toUpperCase() : itemMap.get(symbol2));
				mapDefault2.put("volume", 0.0D);
				mapDefault2.put("usdt", 0.0D);
				
				List<Map<String, Object>> pairsDefaultList = new ArrayList<Map<String, Object>>();
				pairsDefaultList.add(mapDefault1);
				pairsDefaultList.add(mapDefault2);
				mapRetDefault.put("pairs", pairsDefaultList);

				Map<String, Object> mapRet = (Map<String, Object>) ret.getData();				
				if (null == mapRet || 0 == mapRet.size()) {					
					mapRet = mapRetDefault;					
				} else {
					
					if (null == mapRet.get("usdt")) {
						mapRet.put("usdt", 0.0D);
					}

					Map<String, Object> map1 = new HashMap<String, Object>();
					Map<String, Object> map2 = new HashMap<String, Object>();
					List<Map<String, Object>> pairsList = new ArrayList<Map<String,Object>>();
					List<Map<String, Object>> noZeroList = new ArrayList<Map<String, Object>>();
					
					List<Map<String, Object>> extendsList = (List<Map<String, Object>>) mapRet.get("extends");
					if (null == extendsList || 0 == extendsList.size()) {
						mapRet.put("pairs", pairsDefaultList);
						mapRet.put("no_zero", new ArrayList<Map<String, Object>>());
					} else {
						
						for (int i = 0; i < extendsList.size(); i++) {
							
							if (symbol1.equals(extendsList.get(i).get("symbol"))) {
								map1 = extendsList.get(i);
								map1.remove("name");
								map1.put("full_name", StringUtils.isEmptyString(itemMap.get(symbol1)) ? symbol1.toUpperCase() : itemMap.get(symbol1));
								continue;
							}
							if (symbol2.equals(extendsList.get(i).get("symbol"))) {
								map2 = extendsList.get(i);
								map2.remove("name");
								map2.put("full_name", StringUtils.isEmptyString(itemMap.get(symbol2)) ? symbol2.toUpperCase() : itemMap.get(symbol2));
								continue;
							}
							
							if (0.0D != (Double) extendsList.get(i).get("volume")) {
								extendsList.get(i).remove("name");
								String symbolI = (String) extendsList.get(i).get("symbol");
								extendsList.get(i).put("full_name", StringUtils.isEmptyString(itemMap.get(symbolI)) ? symbolI.toUpperCase() : itemMap.get(symbolI));
								noZeroList.add(extendsList.get(i));
							}
						}
						
						if (null == map1 || 0 == map1.size()) {
							map1 = mapDefault1;
						}
						if (null == map2 || 0 == map2.size()) {
							map2 = mapDefault2;
						}
						pairsList.add(map1);
						pairsList.add(map2);
						mapRet.put("pairs", pairsList);
						
						mapRet.put("no_zero", noZeroList);
					}
					
					mapRet.remove("extends");
				}
				
				resultObject.setData(mapRet);
			}
			
		} catch (BusinessException e) {
			resultObject.setCode("1");
			resultObject.setMsg(e.getMessage());
		} catch (Throwable t) {
			resultObject.setCode("1");
			resultObject.setMsg("程序错误");
			logger.error("error:", t);
		}

		return resultObject;
	}

	/**
	 * 钱包账户资产（所有币种）
	 */
	@RequestMapping(action + "getAll.action")
	public Object getAll(HttpServletRequest request) throws IOException {
		return this.getWalletExtends(request, true);
	}
	
	/**
	 * all：true/获取全部；false/获取usdt、btc、eth；
	 */
	public Object getWalletExtends(HttpServletRequest request, boolean all) throws IOException {
		String symbol = request.getParameter("symbol");
		
		ResultObject resultObject = new ResultObject();

		try {
			
			Map<String, Object> mapRet = new LinkedHashMap<String, Object>();

			DecimalFormat df2 = new DecimalFormat("#.########");
			// 向下取整
			df2.setRoundingMode(RoundingMode.FLOOR);

			String partyId = this.getLoginPartyId();
			Wallet usdt = null;
			if (StringUtils.isNotEmpty(partyId)) {
				usdt = this.walletService.saveWalletByPartyId(partyId);
			}

			if (null == usdt) {
				usdt = new Wallet();
				usdt.setMoney(0);
				usdt.setLock_money(0);
				usdt.setFreeze_money(0);
				mapRet.put("usdt", usdt.getMoney());
				mapRet.put("lock_money", usdt.getLock_money());
				mapRet.put("freeze_money", usdt.getFreeze_money());
			} else {
				DecimalFormat df = new DecimalFormat("#.##");
				df.setRoundingMode(RoundingMode.FLOOR);
				usdt.setMoney(Double.valueOf(df.format(usdt.getMoney())));
				usdt.setLock_money(Double.valueOf(df.format(usdt.getLock_money())));
				usdt.setFreeze_money(Double.valueOf(df.format(usdt.getFreeze_money())));
				mapRet.put("usdt", usdt.getMoney());
				mapRet.put("lock_money", usdt.getLock_money());
				mapRet.put("freeze_money", usdt.getFreeze_money());
			}

			// 其他币账户
			List<Item> list_it = this.itemService.cacheGetByMarket("");
			List<String> list_symbol = new ArrayList<String>();
					
			if (!StringUtils.isNotEmpty(symbol)) {
				// symbol为空，获取所有的
				for (int i = 0; i < list_it.size(); i++) {
					Item items = list_it.get(i);
					list_symbol.add(items.getSymbol());
				}
			} else {
				List<String> symbolList = Arrays.asList(symbol.split(","));
				for (int i = 0; i < list_it.size(); i++) {
					Item items = list_it.get(i);
					// 只添加所有币种和参数symbol都有的
					if (symbolList.contains(items.getSymbol())) {
						list_symbol.add(items.getSymbol());
					}
				}
			}

			List<Item> items = this.itemService.cacheGetAll();
			// 按id排序
			Collections.sort(items, new Comparator<Item>() {
				@Override
				public int compare(Item arg0, Item arg1) {
					return arg0.getId().toString().compareTo(arg1.getId().toString());
				}
			});
			
			Map<String, Item> itemMap = new HashMap<String, Item>();
			for (int i = 0; i < items.size(); i++) {
				itemMap.put(items.get(i).getSymbol(), items.get(i));
			}	
			
			List<WalletExtend> walletExtends = null;
			if (StringUtils.isNotEmpty(partyId)) {
				walletExtends = this.walletService.findExtend(partyId, list_symbol);
			}
			
			if (null == walletExtends) {
				walletExtends = new ArrayList<WalletExtend>();
			}
			
			List<WalletExtend> walletExtendsRet = new ArrayList<WalletExtend>();

			int temp = 0;
			for (int i = 0; i < list_symbol.size(); i++) {
				
				for (int j = 0; j < walletExtends.size(); j++) {
					
					WalletExtend walletExtend = walletExtends.get(j);
					if (walletExtend.getWallettype().equals(list_symbol.get(i))) {
						walletExtend.setAmount(Double.valueOf(df2.format(walletExtend.getAmount())));
						walletExtend.setLock_amount(Double.valueOf(df2.format(walletExtend.getLock_amount())));
						walletExtend.setFreeze_amount(Double.valueOf(df2.format(walletExtend.getFreeze_amount())));
						walletExtendsRet.add(walletExtend);
						temp = 1;
					}
				}
				
				if (0 == temp) {
					WalletExtend walletExtend = new WalletExtend();
					if (StringUtils.isNotEmpty(partyId)) {
						walletExtend.setPartyId(partyId);
					}
					walletExtend.setWallettype(list_symbol.get(i));
					walletExtend.setAmount(0);
					walletExtend.setLock_amount(0);
					walletExtend.setFreeze_amount(0);
					walletExtend.setName(itemMap.get(list_symbol.get(i)).getName());
					walletExtendsRet.add(walletExtend);
				}
				
				temp = 0;
			}
			
			String symbolsStr = "";
			for (int i = 0; i < list_symbol.size(); i++) {
				if (i != 0) {
					symbolsStr = symbolsStr + "," + list_symbol.get(i);
				} else {
					symbolsStr = list_symbol.get(i);
				}			
			}
			
			List<Realtime> realtime_all = this.dataService.realtime(symbolsStr);
			if (realtime_all.size() <= 0) {
//				throw new BusinessException("系统错误，请稍后重试");
			}
			
			Map<String, Realtime> realtimeMap = new HashMap<String, Realtime>();
			for (int i = 0; i < realtime_all.size(); i++) {
				realtimeMap.put(realtime_all.get(i).getSymbol(), realtime_all.get(i));
			}
			
			List<Map<String, Object>> extendsList = new ArrayList<Map<String,Object>>();
			for (int i = 0; i < walletExtendsRet.size(); i++) {
				
				if (false == all) {
					// 只要btc、eth
					if (!walletExtendsRet.get(i).getWallettype().equals("btc")
							&& !walletExtendsRet.get(i).getWallettype().equals("eth")) {
						continue;
					}					
				}
				
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("name", walletExtendsRet.get(i).getName());
				map.put("symbol", walletExtendsRet.get(i).getWallettype());
				double volume = Arith.add(walletExtendsRet.get(i).getAmount(), walletExtendsRet.get(i).getFrozenAmount());
				map.put("volume", volume);
				map.put("lock_amount", walletExtendsRet.get(i).getLock_amount());
				map.put("freeze_amount", walletExtendsRet.get(i).getFreeze_amount());
				map.put("usable", walletExtendsRet.get(i).getAmount());
				map.put("frozenAmount", walletExtendsRet.get(i).getFrozenAmount());
				
				Realtime rt = realtimeMap.get(walletExtendsRet.get(i).getWallettype());
				if (null != rt) {
					map.put("usdt", Arith.mul(rt.getClose(), volume));
				} else {
					map.put("usdt", 0);
				}
				
				extendsList.add(map);
			}
			
			if (!StringUtils.isNotEmpty(symbol) || symbol.contains("usdt")) {
				// 添加usdt到列表最前面
				Map<String, Object> mapUsdt = new HashMap<String, Object>();
				mapUsdt.put("name", "USDT/USDT");
				mapUsdt.put("symbol", "usdt");
				mapUsdt.put("volume", mapRet.get("usdt"));
				mapUsdt.put("lock_amount", mapRet.get("lock_money"));
				mapUsdt.put("freeze_amount", mapRet.get("freeze_money"));
				mapUsdt.put("usdt", mapRet.get("usdt"));
				mapUsdt.put("usable", mapRet.get("usdt"));
				mapUsdt.put("frozenAmount", 0);
				extendsList.add(0, mapUsdt);
			}
			
			mapRet.put("extends", extendsList);
			
			resultObject.setData(mapRet);

		} catch (BusinessException e) {
			resultObject.setCode("1");
			resultObject.setMsg(e.getMessage());
		} catch (Throwable t) {
			resultObject.setCode("1");
			resultObject.setMsg("程序错误");
			logger.error("error:", t);
		}

		return resultObject;
	}

	/**
	 * 钱包日志  充值category=recharge；提现category=withdraw；
	 */
	@RequestMapping(action + "logs.action")
	public Object logs(HttpServletRequest request) throws IOException {
		String page_no = request.getParameter("page_no");
		String category = request.getParameter("category");

		ResultObject resultObject = new ResultObject();
		resultObject = this.readSecurityContextFromSession(resultObject);
		if (!"0".equals(resultObject.getCode())) {
			return resultObject;
		}

		try {

			if (StringUtils.isNullOrEmpty(page_no)) {
				page_no = "1";
			}
			if (!StringUtils.isInteger(page_no)) {
				throw new BusinessException("页码不是整数");
			}
			if (Integer.valueOf(page_no).intValue() <= 0) {
				throw new BusinessException("页码不能小于等于0");
			}

			int page_no_int = Integer.valueOf(page_no).intValue();

			List<Map<String, Object>> data = this.walletLogService.pagedQuery(page_no_int, 10, this.getLoginPartyId(), category, "1").getElements();
			for (Map<String, Object> log : data) {

				if (null == log.get("wallettype") || !StringUtils.isNotEmpty(log.get("wallettype").toString()))
					log.put("wallettype", Constants.WALLET);
				else {
					log.put("wallettype", log.get("wallettype").toString().toUpperCase());
				}
			}
			
			resultObject.setData(data);

		} catch (BusinessException e) {
			resultObject.setCode("1");
			resultObject.setMsg(e.getMessage());
		} catch (Throwable t) {
			resultObject.setCode("1");
			resultObject.setMsg("程序错误");
			logger.error("error:", t);
		}
		
		return resultObject;
	}

	/**
	 * 钱包历史记录
	 */
	@RequestMapping(action + "records.action")
	public Object records(HttpServletRequest request) throws IOException {		
		// 页码
		String page_no = request.getParameter("page_no");
		// 充值category=recharge；提现category=withdraw；
		String category = request.getParameter("category");
		// 开始时间
		String start_time = request.getParameter("start_time");
		// 结束时间
		String end_time = request.getParameter("end_time");
		// 钱包类型 btc、eth...
		String wallet_type = request.getParameter("wallet_type");
		// 状态：0/初始状态，未知；1/成功；2/失败；
		String status = request.getParameter("status");

		ResultObject resultObject = new ResultObject();
		resultObject = this.readSecurityContextFromSession(resultObject);
		if (!"0".equals(resultObject.getCode())) {
			return resultObject;
		}

		try {
			
			Integer status_int = null;

			if (StringUtils.isNullOrEmpty(status)) {
				status_int = null;
			} else {
				if (!StringUtils.isInteger(status)) {
					throw new BusinessException("状态不是整数");
				}
				if (Integer.valueOf(status).intValue() < 0) {
					throw new BusinessException("状态不能小于0");
				}
				status_int = Integer.valueOf(status);				
			}

			if (StringUtils.isNullOrEmpty(page_no)) {
				page_no = "1";
			}
			if (!StringUtils.isInteger(page_no)) {
				throw new BusinessException("页码不是整数");
			}
			if (Integer.valueOf(page_no).intValue() <= 0) {
				throw new BusinessException("页码不能小于等于0");
			}

			int page_no_int = Integer.valueOf(page_no).intValue();

			List<Map<String, Object>> data = this.walletLogService.pagedQueryRecords(page_no_int, 10, this.getLoginPartyId(), 
					category, start_time, end_time, wallet_type, status_int).getElements();
			for (Map<String, Object> log : data) {

				if (null == log.get("wallet_type") || !StringUtils.isNotEmpty(log.get("wallet_type").toString()))
					log.put("wallet_type", Constants.WALLET);
				else {
					log.put("wallet_type", log.get("wallet_type").toString().toUpperCase());
				}
			}
			
			resultObject.setData(data);

		} catch (BusinessException e) {
			resultObject.setCode("1");
			resultObject.setMsg(e.getMessage());
		} catch (Throwable t) {
			resultObject.setCode("1");
			resultObject.setMsg("程序错误");
			logger.error("error:", t);
		}
		
		return resultObject;
	}

	/**
	 * 获取币种钱包
	 */
	@RequestMapping(action + "list.action")
	public Object list(HttpServletRequest request) {

		ResultObject resultObject = new ResultObject();
		resultObject = this.readSecurityContextFromSession(resultObject);
		if (!"0".equals(resultObject.getCode())) {
			return resultObject;
		}

		try {

			Map<String, Object> map = new LinkedHashMap<String, Object>();
			
			Wallet usdt = null;
			String partyId = this.getLoginPartyId();	
			if (StringUtils.isNotEmpty(partyId)) {
				usdt = this.walletService.saveWalletByPartyId(partyId);
			}
			
			DecimalFormat df2 = new DecimalFormat("#.########");
			// 向下取整
			df2.setRoundingMode(RoundingMode.FLOOR);
			if (null == usdt) {
				usdt = new Wallet();
				usdt.setMoney(0);
				map.put("USDT", usdt);
			} else {
				DecimalFormat df = new DecimalFormat("#.##");
				df.setRoundingMode(RoundingMode.FLOOR);
				usdt.setMoney(Double.valueOf(df.format(usdt.getMoney())));
				map.put("USDT", usdt);
			}

			// 其他币账户
			List<Item> list_it = this.itemService.cacheGetByMarket("");
			List<String> list_symbol = new ArrayList<String>();
			for (int i = 0; i < list_it.size(); i++) {
				Item items = list_it.get(i);
				list_symbol.add(items.getSymbol());
			}

			List<Item> items = this.itemService.cacheGetAll();
			Collections.sort(items, new Comparator<Item>() {
				// 按id排序
				@Override
				public int compare(Item arg0, Item arg1) {
					return arg0.getId().toString().compareTo(arg1.getId().toString());
				}
			});
			
			List<WalletExtend> walletExtends = null;
			if (StringUtils.isNotEmpty(partyId)) {
				walletExtends = this.walletService.findExtend(partyId, list_symbol);
			}
			
			WalletExtend walletExtend = new WalletExtend();

			// 如果是空
			if (null == walletExtends) {
				for (int i = 0; i < items.size(); i++) {
					walletExtend.setWallettype(items.get(i).getSymbol().toUpperCase());
					walletExtend.setAmount(0);
					map.put(walletExtend.getWallettype().toUpperCase(), walletExtend);
				}
			}
			
			// 如果不为空且2个相同
			if (walletExtends != null && walletExtends.size() == items.size()) {

				for (int i = 0; i < walletExtends.size(); i++) {
					if (null == walletExtends.get(i)) {
						continue;
					}
					walletExtend = walletExtends.get(i);
					usdt.setMoney(Double.valueOf(df2.format(usdt.getMoney())));
					walletExtend.setAmount(Double.valueOf(df2.format(walletExtend.getAmount())));

					map.put(walletExtend.getWallettype().toUpperCase(), walletExtend);
				}
			}
			
			// 如果不为空 且数据库里的少于币种
			int temp = 0;
			if (walletExtends != null && walletExtends.size() < items.size()) {
				
				for (int i = 0; i < items.size(); i++) {
					
					for (int j = 0; j < walletExtends.size(); j++) {
						
						walletExtend = walletExtends.get(j);
						if (walletExtend.getWallettype().equals(items.get(i).getSymbol())) {
							walletExtend.setAmount(Double.valueOf(df2.format(walletExtend.getAmount())));
							
							map.put(walletExtend.getWallettype().toUpperCase(), walletExtend);
							temp = 1;
							break;
						}
					}
					
					if (0 == temp) {
						walletExtend = new WalletExtend();
						walletExtend.setWallettype(items.get(i).getSymbol());
						walletExtend.setAmount(0);

						map.put(walletExtend.getWallettype().toUpperCase(), walletExtend);
					} else {						
						temp = 0;
					}
				}
			}
			
			resultObject.setData(map);

		} catch (BusinessException e) {
			resultObject.setCode("1");
			resultObject.setMsg(e.getMessage());
		} catch (Throwable t) {
			resultObject.setCode("1");
			resultObject.setMsg("程序错误");
			logger.error("error:", t);
		}
		
		return resultObject;
	}
	
	/**
	 * 获取usdt余额
	 * 
	 */
	@RequestMapping(action + "getUsdt.action")
	public Object getUsdt(HttpServletRequest request) {
		ResultObject resultObject = new ResultObject();
		// usdt余额
		Map<String, Object> data = new HashMap<String, Object>();
		String partyId = this.getLoginPartyId();
		DecimalFormat df2 = new DecimalFormat("#.##");
		// 向下取整
		df2.setRoundingMode(RoundingMode.FLOOR);
		Wallet wallet = new Wallet();
		if (!"".equals(partyId) && partyId != null) {
			wallet = walletService.saveWalletByPartyId(partyId);
		}
		double money = wallet.getMoney();

		// 账户剩余资金
		data.put("money", df2.format(money));

		resultObject.setData(data);
		return resultObject;
	}

}
