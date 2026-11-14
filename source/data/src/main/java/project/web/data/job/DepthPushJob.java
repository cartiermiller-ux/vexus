package project.web.data.job;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;

import kernel.util.Arith;
import kernel.util.ThreadUtils;
import kernel.web.ResultObject;
import project.data.DataCache;
import project.data.internal.DepthTimeObject;
import project.data.job.DataQueue;
import project.data.job.HandleObject;
import project.data.model.Depth;
import project.data.model.DepthEntry;
import project.data.model.Realtime;
import project.item.ItemService;
import project.item.model.Item;
import project.web.data.websocket.WebSocketServer;
import util.RandomUtil;

public class DepthPushJob implements Runnable {

	private Logger logger = LoggerFactory.getLogger(DepthPushJob.class);

	private ItemService itemService;

	public void start() {
		new Thread(this, "depthPushJob").start();
		if (logger.isInfoEnabled())
			logger.info("启动depthPushJob！");
	}

	public void run() {

		while (true) {
			try {
				this.depthHandle();
			} catch (Exception e) {
				logger.error("run fail", e);
			} finally {
				ThreadUtils.sleep(500);
			}
		}

	}

	private void depthHandle() {
		try {
			// 数据处理
			ResultObject depthResult = new ResultObject();
			
			Map<String, String> depthResultMap = new HashMap<>();
			
			if (!WebSocketServer.depthMap.isEmpty()) {
				
				// 客户端请求的所有币种，去重集合
				Set<String> symbolSet = new HashSet<String>();
				for (String socketKey : WebSocketServer.depthMap.keySet()) {
					String symbolKey = socketKey.split("_")[2];
					symbolSet.add(symbolKey);
				}
				
				for (String symbol : symbolSet) {
					DepthTimeObject depth = DataCache.getDepth().get(symbol);
					if (null != depth && null != depth.getDepth()) {
						Depth depthData = depth.getDepth();
						Realtime realtime = DataCache.getRealtime(symbol);
						depthResult.setData(this.depthRevise(depthData, symbol, realtime.getClose()));
					} else {
						Item item = this.itemService.cacheBySymbol(symbol, true);
						HandleObject handleObject = new HandleObject();
						handleObject.setType(HandleObject.type_depth);
						handleObject.setItem(item);
						DataQueue.add(handleObject);
					}
					depthResultMap.put(symbol, JSONObject.toJSONString(depthResult));
					// System.out.println("深度数据 推送 " + JSONObject.toJSONString(depthResult));
				}
				
				
				if (depthResultMap.isEmpty()) {
					return;
				}
				
				for (String socketKey : WebSocketServer.depthMap.keySet()) {
//					long timeMillins = System.currentTimeMillis();
					WebSocketServer server = WebSocketServer.depthMap.get(socketKey);
//					if (server.getTimeStr() != 0 && timeMillins > server.getTimeStr()) {
//						server.onClose();
//						return;
//					}
					String type = socketKey.split("_")[1];
					String symbolKey = socketKey.split("_")[2];
					server.sendToMessageById(socketKey, depthResultMap.get(symbolKey), type);
				}
			}
			
			
		} catch (Throwable e) {
			e.printStackTrace();
		}

	}
	
	/**
	 * 市场深度数据 解析
	 */
	private Map<String, Object> depthRevise(Depth data, String symbol, Double close) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("symbol", symbol);
		map.put("ts", data.getTs());
		Item item = this.itemService.cacheBySymbol(data.getSymbol(), true);
		List<Map<String, Object>> asks_list = new ArrayList<Map<String, Object>>();
		
		for (int i = 0; i < data.getAsks().size(); i++) {
			DepthEntry depthEntry = data.getAsks().get(i);
			Map<String, Object> asks_map = new HashMap<String, Object>();
			
			double addPriceValue = getRandomValue(String.valueOf(depthEntry.getPrice()));
			double addAmountValue = getRandomValue((int)depthEntry.getAmount().doubleValue());
			
			double price = Arith.add(depthEntry.getPrice(), addPriceValue);
			if (price < close) {
				price = Arith.add(close, addPriceValue);
			}
			double amount = Arith.add(depthEntry.getAmount(), addAmountValue);
			
			if (item.getDecimals() == null || item.getDecimals() < 0) {
				asks_map.put("price", price);
				asks_map.put("amount", amount);
			} else {
				String format = "";
				if (item.getDecimals() == 0) {
					format = "#";
				} else {
					format = "#.";
					for (int j = 0; j < item.getDecimals(); j++) {
						format = format + "#";
					}
				}

				DecimalFormat df = new DecimalFormat(format);
				df.setRoundingMode(RoundingMode.FLOOR);// 向下取整

				asks_map.put("price", df.format(price));
				asks_map.put("amount", df.format(amount));

			}
			asks_list.add(asks_map);

		}
		// buy
		map.put("asks", asks_list);
		
		List<Map<String, Object>> bids_list = new ArrayList<Map<String, Object>>();
		
		for (int i = 0; i < data.getBids().size(); i++) {
			DepthEntry depthEntry = data.getBids().get(i);
			String priceTemp = new BigDecimal(String.valueOf(depthEntry.getPrice())).toPlainString();
			double addPriceValue = getRandomValue(priceTemp);
			double addAmountValue = getRandomValue((int)depthEntry.getAmount().doubleValue());
			double price = Arith.add(depthEntry.getPrice(), -addPriceValue);
			if (price >= close ) {
				price = Arith.add(close, -addPriceValue);
			}
			
			if (price < 0) {
				price = close;
			}
			
			double amount = Arith.add(depthEntry.getAmount(), addAmountValue);
			
			Map<String, Object> bids_map = new HashMap<String, Object>();
			if (item.getDecimals() == null || item.getDecimals() < 0) {
				bids_map.put("price", price);
				bids_map.put("amount", amount);
			} else {
				String format = "";
				if (item.getDecimals() == 0) {
					format = "#";
				} else {
					format = "#.";
					for (int j = 0; j < item.getDecimals(); j++) {
						format = format + "#";
					}
				}

				DecimalFormat df = new DecimalFormat(format);

				bids_map.put("price", df.format(price));
				bids_map.put("amount", df.format(amount));

			}
			bids_list.add(bids_map);

		}
		// sell 
		map.put("bids", bids_list);

		return map;
	}
	
	private double getRandomValue(int value) {
		double addValue;
		if (value > 0) {
		    int count = 0;
		    while (value > 0) {
		    	value = value / 10;
		        count++;
		    }
		    // 个
		    if (count == 1) {
		    	addValue = RandomUtil.randomFloat(0.01, 0.1999, 4);
		    	return addValue;
		    } 
		    // 十
		    if (count == 2) {
		    	addValue = RandomUtil.randomFloat(0.1, 0.5999, 4);
		    	return addValue;
		    } 
		    // 百
		    if (count == 3) {
		    	addValue = RandomUtil.randomFloat(0.1, 2.9999, 4);
		    	return addValue;
		    } 
		    // 千
		    if (count == 4) {
		    	addValue = RandomUtil.randomFloat(1, 3.9999, 4);
		    	return addValue;
		    } 
		    // 万
		    if (count == 5) {
		    	addValue = RandomUtil.randomFloat(1, 5.9999, 4);
		    	return addValue;
		    } 
		    // 十万
		    else {
		    	addValue = RandomUtil.randomFloat(1, 5.9999, 4);
		    	return addValue;
		    }
		} else {
			addValue = RandomUtil.randomFloat(0.01, 0.2999, 4);
			return addValue;
		}
	}
	
	private double getRandomValue(String value) {
		double addValue;
		double d = Double.valueOf(value);
		int val = (int)d;
		// 个位数>0
		if (val > 0) {
		    int count = 0;
		    while (val > 0) {
		    	val = val / 10;
		        count++;
		    }
		    // 个
		    if (count == 1) {
		    	addValue = RandomUtil.randomFloat(0.01, 0.1999, 4);
		    	return addValue;
		    } 
		    // 十
		    if (count == 2) {
		    	addValue = RandomUtil.randomFloat(0.1, 0.5999, 4);
		    	return addValue;
		    } 
		    // 百
		    if (count == 3) {
		    	addValue = RandomUtil.randomFloat(0.1, 2.9999, 4);
		    	return addValue;
		    } 
		    // 千
		    if (count == 4) {
		    	addValue = RandomUtil.randomFloat(1, 3.9999, 4);
		    	return addValue;
		    } 
		    // 万
		    if (count == 5) {
		    	addValue = RandomUtil.randomFloat(1, 5.9999, 4);
		    	return addValue;
		    } 
		    // 十万
		    else {
		    	addValue = RandomUtil.randomFloat(1, 5.9999, 4);
		    	return addValue;
		    }
		} 
		// 个位=0
		else {
			String[] valueSplit = value.split("\\.");
			int valueLength = valueSplit[1].length();
//			int charSum = 0;
//			for (char s : valueSplit[1].toCharArray()) {
//                if (String.valueOf(s).equals("0")) {
//                	charSum ++;
//                }
//            }
			if (valueLength <= 4) {
				addValue = RandomUtil.randomFloat(0.001, 0.003, 3);
				return addValue;
			}
			
			if (4 < valueLength && valueLength <= 6 ) {
				addValue = RandomUtil.randomFloat(0.00001, 0.00003, 5);
				return addValue;
			}
			
			if (6 < valueLength && valueLength <= 8 ) {
				addValue = RandomUtil.randomFloat(0.0000001, 0.0000003, 7);
				return addValue;
			}
			
			if (8 < valueLength && valueLength <= 10 ) {
				addValue = RandomUtil.randomFloat(0.0000001, 0.0000003, 9);
				return addValue;
			} else {
				addValue = RandomUtil.randomFloat(0.0000000001, 0.0000000003, 10);
				return addValue;
			}
			// addValue = RandomUtil.randomFloat(0.01, 0.2999, 4);
			// return addValue;
		}
	}

	public void setItemService(ItemService itemService) {
		this.itemService = itemService;
	}
	
}
