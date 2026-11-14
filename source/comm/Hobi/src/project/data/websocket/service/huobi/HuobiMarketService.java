package project.data.websocket.service.huobi;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSONObject;

import project.data.websocket.client.MarketClient;
import project.data.websocket.client.req.market.SubMarketDepthRequest;
import project.data.websocket.client.req.market.SubMarketTickerRequest;
import project.data.websocket.client.req.market.SubMarketTradeRequest;
import project.data.websocket.constant.Options;
import project.data.websocket.constant.enums.DepthStepEnum;
import project.data.websocket.model.market.MarketDepthEvent;
import project.data.websocket.model.market.MarketTickerEvent;
import project.data.websocket.model.market.MarketTradeEvent;
import project.data.websocket.service.huobi.connection.HuobiRestConnection;
import project.data.websocket.service.huobi.connection.HuobiWebSocketConnection;
import project.data.websocket.service.huobi.parser.market.MarketDepthEventParser;
import project.data.websocket.service.huobi.parser.market.MarketTickerEventParser;
import project.data.websocket.service.huobi.parser.market.MarketTradeEventParser;
import project.data.websocket.utils.InputChecker;
import project.data.websocket.utils.ResponseCallback;
import project.data.websocket.utils.SymbolUtils;

public class HuobiMarketService implements MarketClient {

	private Options options;

	private HuobiRestConnection restConnection;

	public HuobiMarketService(Options options) {
		this.options = options;
		restConnection = new HuobiRestConnection(options);
	}

	public static final String WEBSOCKET_MARKET_DEPTH_TOPIC = "market.$symbol.depth.$type";
	public static final String WEBSOCKET_MARKET_TRADE_TOPIC = "market.$symbol.trade.detail";
	public static final String WEBSOCKET_MARKET_TICKER_TOPIC = "market.$symbol.ticker";

	@Override
	public void subMarketDepth(SubMarketDepthRequest request, ResponseCallback<MarketDepthEvent> callback) {
		// 检查参数
		InputChecker.checker().shouldNotNull(request.getSymbol(), "symbol");

		// 格式化symbol为数组
		List<String> symbolList = SymbolUtils.parseSymbols(request.getSymbol());

		// 检查数组
		InputChecker.checker().checkSymbolList(symbolList);

		String step = request.getStep() == null ? DepthStepEnum.STEP0.getStep() : request.getStep().getStep();
		List<String> commandList = new ArrayList<>(symbolList.size());
		symbolList.forEach(symbol -> {

			String topic = WEBSOCKET_MARKET_DEPTH_TOPIC.replace("$symbol", symbol).replace("$type", step);

			JSONObject command = new JSONObject();
			command.put("sub", topic);
			command.put("id", System.nanoTime());
			commandList.add(command.toJSONString());
		});

		HuobiWebSocketConnection.createMarketConnection(options, commandList, new MarketDepthEventParser(), callback,
				false);

	}

	@Override
	public void subMarketTrade(SubMarketTradeRequest request, ResponseCallback<MarketTradeEvent> callback) {

		// 检查参数
		InputChecker.checker().shouldNotNull(request.getSymbol(), "symbol");

		// 格式化symbol为数组
		List<String> symbolList = SymbolUtils.parseSymbols(request.getSymbol());

		// 检查数组
		InputChecker.checker().checkSymbolList(symbolList);

		List<String> commandList = new ArrayList<>(symbolList.size());
		symbolList.forEach(symbol -> {

			String topic = WEBSOCKET_MARKET_TRADE_TOPIC.replace("$symbol", symbol);

			JSONObject command = new JSONObject();
			command.put("sub", topic);
			command.put("id", System.nanoTime());
			commandList.add(command.toJSONString());
		});

		HuobiWebSocketConnection.createMarketConnection(options, commandList, new MarketTradeEventParser(), callback,
				false);
	}
	
	@Override
	public void subMarketTicker(SubMarketTickerRequest request, ResponseCallback<MarketTickerEvent> callback) {
		// 检查参数
		InputChecker.checker().shouldNotNull(request.getSymbol(), "symbol");

		// 格式化symbol为数组
		List<String> symbolList = SymbolUtils.parseSymbols(request.getSymbol());

		// 检查数组
		InputChecker.checker().checkSymbolList(symbolList);

		List<String> commandList = new ArrayList<>(symbolList.size());
		symbolList.forEach(symbol -> {

			String topic = WEBSOCKET_MARKET_TICKER_TOPIC.replace("$symbol", symbol);

			JSONObject command = new JSONObject();
			command.put("sub", topic);
			command.put("id", System.nanoTime());
			commandList.add(command.toJSONString());
		});

		HuobiWebSocketConnection.createMarketConnection(options, commandList, new MarketTickerEventParser(), callback,
				false);
	}

	public Options getOptions() {
		return options;
	}

	public void setOptions(Options options) {
		this.options = options;
	}

	public HuobiRestConnection getRestConnection() {
		return restConnection;
	}

	public void setRestConnection(HuobiRestConnection restConnection) {
		this.restConnection = restConnection;
	}

}
