package project.data.job;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kernel.util.Arith;
import project.data.AdjustmentValueCache;
import project.data.DataCache;
import project.data.internal.DepthTimeObject;
import project.data.internal.TradeTimeObject;
import project.data.model.Depth;
import project.data.model.Realtime;
import project.data.model.Trade;
import project.data.websocket.client.MarketClient;
import project.data.websocket.client.req.market.SubMarketTickerRequest;
import project.data.websocket.constant.HuobiOptions;
import project.data.websocket.model.market.MarketDepthEvent;
import project.data.websocket.model.market.MarketTickerEvent;
import project.data.websocket.model.market.MarketTradeEvent;
import project.hobi.HobiDataService;
import project.item.ItemService;
import project.item.model.Item;

/**
 * 
 * 深度、近期交易记录 websocket启动服务
 */
public class RealtimeWebsocketServer {

	ItemService itemService;
	
	HobiDataService hobiDataService;
	
	public static Map<String, Long> map = new HashMap();

	public void start() {
		List<Item> itemList = itemService.cacheGetAll();
		MarketClient tickerClient = MarketClient.create(new HuobiOptions());
		for (Item item : itemList) {
			String symbol = item.getSymbol_data();
			SubMarketTickerRequest tickerReq = new SubMarketTickerRequest();
			tickerReq.setSymbol(symbol);
			tickerClient.subMarketTicker(tickerReq, (marketTradeEvent) -> {
				// System.out.println(JSONObject.toJSONString(marketTradeEvent));
				ticker(marketTradeEvent, symbol);
			});
		}

//		MarketClient marketClient = MarketClient.create(new HuobiOptions());
//		for (Item item : itemList) {
//			String symbol = item.getSymbol_data();
//			SubMarketDepthRequest depthReq = new SubMarketDepthRequest();
//			depthReq.setSymbol(symbol);
//			depthReq.setStep(DepthStepEnum.STEP2);
//			marketClient.subMarketDepth(depthReq, (marketDetailEvent) -> {
//				// System.out.println(JSONObject.toJSONString(marketDetailEvent));
//				depth(marketDetailEvent, symbol);
//			});
//		}
//		
//		MarketClient tradeClient = MarketClient.create(new HuobiOptions());
//		for (Item item : itemList) {
//			String symbol = item.getSymbol_data();
//			SubMarketTradeRequest tradeReq = new SubMarketTradeRequest();
//			tradeReq.setSymbol(symbol);
//			tradeClient.subMarketTrade(tradeReq, (marketTradeEvent) -> {
//		    	// System.out.println(JSONObject.toJSONString(marketTradeEvent));
//		    	trade(marketTradeEvent, symbol);
//		      });
//		}
	}

	private void ticker(MarketTickerEvent event, String symbol) {
		try {
			
			if (map.containsKey(symbol) && map.get(symbol) > event.getTs()) {
				return;
			}
			map.put(symbol, event.getTs() + 500);
			Realtime realtime = new Realtime();
			Item item = itemService.cacheBySymbolData(symbol);
			symbol = item.getSymbol();
		    item = this.itemService.cacheBySymbol(symbol, true);
			Double currentValue = AdjustmentValueCache.getCurrentValue().get(symbol);
			double close = event.getTicker().getClose().doubleValue();
			double vol = event.getTicker().getVol().doubleValue();
			double amount = event.getTicker().getAmount().doubleValue();
			
			realtime.setSymbol(symbol);
			realtime.setTs(event.getTs());
			realtime.setName(item.getName());
			realtime.setOpen(event.getTicker().getOpen().doubleValue());
			realtime.setClose(close);
			realtime.setHigh(event.getTicker().getHigh().doubleValue());
			realtime.setLow(event.getTicker().getLow().doubleValue());
			realtime.setVolume(vol);
			realtime.setAmount(amount);
			
			if (currentValue != null && currentValue != 0) {
				realtime.setClose(Arith.add(close, currentValue));
				realtime.setVolume(Arith.add(vol, Arith.mul(Arith.div(currentValue, close), vol)));
				realtime.setAmount(Arith.add(amount, Arith.mul(Arith.div(currentValue, close), amount)));
			}

			Double high = DataCache.getRealtimeHigh().get(symbol);
			Double low = DataCache.getRealtimeLow().get(symbol);

			if (high == null || realtime.getClose() > high) {
				DataCache.getRealtimeHigh().put(symbol, close);
			}
			if ((low == null || realtime.getClose() < low) && realtime.getClose() > 0) {
				DataCache.getRealtimeLow().put(symbol, close);
			}

			Realtime current = DataCache.getRealtime(symbol);
			if (current == null || current.getTs() != event.getTs()) {
				// 交易量倍数不为空或0时修改倍数
				if (item.getMultiple() > 0) {
					realtime.setVolume(Arith.mul(realtime.getVolume(), item.getMultiple()));
					realtime.setAmount(Arith.mul(realtime.getAmount(), item.getMultiple()));
				}

				if (high != null && high >= realtime.getClose()) {
					realtime.setHigh(high);
				}
				
				if (low != null && low <= realtime.getClose()) {
					realtime.setLow(low);
				}

				Double h24Before = DataCache.getRealtime24HBeforeOpen().get(symbol);
				if (h24Before != null) {
					realtime.setOpen(h24Before);
				}

				DataCache.putRealtime(symbol, realtime);
				// System.out.println(JSONObject.toJSONString(realtime));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void depth(MarketDepthEvent event, String symbol) {
		Item item = itemService.cacheBySymbolData(symbol);
		item = itemService.cacheBySymbol(item.getSymbol(), false);
		Depth depth = hobiDataService.depthDecorator(event, item);
		if (depth != null) {
			DepthTimeObject timeObject = new DepthTimeObject();
			timeObject.setLastTime(new Date());
			timeObject.setDepth(depth);
			DataCache.getDepth().put(item.getSymbol(), timeObject);
			// System.out.println("深度数据 入缓存" + JSONObject.toJSONString(timeObject));
		}
	}

	private void trade(MarketTradeEvent event, String symbol) {
		Item item = itemService.cacheBySymbolData(symbol);
		item = itemService.cacheBySymbol(item.getSymbol(), false);
		Trade trade = hobiDataService.tradeDecorator(event, item);
		if (trade != null) {
			TradeTimeObject timeObject = new TradeTimeObject();
			timeObject.setLastTime(new Date());
			timeObject.put(item.getSymbol(), trade.getData());
			DataCache.getTrade().put(item.getSymbol(), timeObject);
		}
	}

	public void setItemService(ItemService itemService) {
		this.itemService = itemService;
	}

	public void setHobiDataService(HobiDataService hobiDataService) {
		this.hobiDataService = hobiDataService;
	}

}
