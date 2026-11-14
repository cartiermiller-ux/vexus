package project.web.data.job;

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

import kernel.util.ThreadUtils;
import kernel.web.ResultObject;
import project.data.DataCache;
import project.data.model.Realtime;
import project.item.ItemService;
import project.item.model.Item;
import project.web.data.websocket.WebSocketServer;

/**
 * 行情数据推送Job
 *
 */
public class RealtimePushJob implements Runnable {
	
	private static final Logger logger = LoggerFactory.getLogger(RealtimePushJob.class);

	private ItemService itemService;

	public void start() {
		new Thread(this, "realtimePushJob").start();
		if (logger.isInfoEnabled())
			logger.info("启动realtimePushJob！");
	}

	public void run() {

		while (true) {
			try {
				this.realtimeHandle();
			} catch (Exception e) {
				logger.error("run fail", e);
			} finally {
				ThreadUtils.sleep(1000);
			}
		}
	}

	private void realtimeHandle() {
		try {
			Map<String, String> realtimeResultMap = new HashMap<>();
			
			// 行情实时价格
			if (!WebSocketServer.realtimeMap.isEmpty()) {
				
				// 客户端请求的所有币种，去重集合
				Set<String> symbolSet = new HashSet<String>();
				for (String socketKey : WebSocketServer.realtimeMap.keySet()) {
					String symbolKey = socketKey.split("_")[2];
					symbolSet.add(symbolKey);
				}
				
				for (String symbol : symbolSet) {
					Realtime realtimeData = DataCache.getRealtime(symbol);
					if(null==realtimeData) {
						logger.error("Not Found Realtime For Symbol: {}",symbol);
						continue;
					}
					
					this.realtimeRevise(realtimeResultMap, realtimeData, symbol);
				}
				
				if (realtimeResultMap.isEmpty()) {
					return;
				}
				
				for (String socketKey : WebSocketServer.realtimeMap.keySet()) {
//					long timeMillins = System.currentTimeMillis();
					WebSocketServer server = WebSocketServer.realtimeMap.get(socketKey);
//					if (server.getTimeStr() != 0 && timeMillins > server.getTimeStr()) {
//						server.onClose();
//						return;
//					}
					
					String type = socketKey.split("_")[1];
					String symbolKey = socketKey.split("_")[2];
					server.sendToMessageById(socketKey, realtimeResultMap.get(symbolKey), type);
				}
			}
			
		} catch (Throwable e) {
			e.printStackTrace();
		}

	}

	/**
	 * 行情实时价格解析
	 */
	private void realtimeRevise(Map<String, String> realtimeResultMap, Realtime realtime, String symbol) {
			
		ResultObject realtimeResult = new ResultObject();
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("symbol", symbol);
		map.put("ts", realtime.getTs());
		map.put("current_time", realtime.getCurrent_time());
		map.put("name", realtime.getName());
		map.put("change_ratio", realtime.getChange_ratio());
		double close = realtime.getClose();
		
		Item item = this.itemService.cacheBySymbol(realtime.getSymbol(), true);
		
		if (item.getDecimals() == null || item.getDecimals() < 0) {
			map.put("open", realtime.getOpen());
			map.put("close", close);
			map.put("high", realtime.getHigh());
			map.put("low", realtime.getLow());
			map.put("volume", realtime.getVolume());
			map.put("amount", realtime.getAmount());
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

			map.put("open", df.format(realtime.getOpen()));
			map.put("close", df.format(close));
			map.put("high", df.format(realtime.getHigh()));
			map.put("low", df.format(realtime.getLow()));
			map.put("volume", df.format(realtime.getVolume()));
			map.put("amount", df.format(realtime.getAmount()));

		}
		list.add(map);
		realtimeResult.setData(list);
		realtimeResultMap.put(realtime.getSymbol(), JSONObject.toJSONString(realtimeResult));
	}

	public void setItemService(ItemService itemService) {
		this.itemService = itemService;
	}

}
