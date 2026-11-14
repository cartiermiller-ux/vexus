package project.data.websocket.client;

import project.data.websocket.client.req.market.SubMarketDepthRequest;
import project.data.websocket.client.req.market.SubMarketTickerRequest;
import project.data.websocket.client.req.market.SubMarketTradeRequest;
import project.data.websocket.constant.Options;
import project.data.websocket.constant.enums.ExchangeEnum;
import project.data.websocket.exception.SDKException;
import project.data.websocket.model.market.MarketDepthEvent;
import project.data.websocket.model.market.MarketTickerEvent;
import project.data.websocket.model.market.MarketTradeEvent;
import project.data.websocket.service.huobi.HuobiMarketService;
import project.data.websocket.utils.ResponseCallback;

public interface MarketClient {

	void subMarketDepth(SubMarketDepthRequest request, ResponseCallback<MarketDepthEvent> callback);
	
	void subMarketTrade(SubMarketTradeRequest request, ResponseCallback<MarketTradeEvent> callback);
	
	void subMarketTicker(SubMarketTickerRequest request, ResponseCallback<MarketTickerEvent> callback);

	static MarketClient create(Options options) {
		if (options.getExchange().equals(ExchangeEnum.HUOBI)) {
			return new HuobiMarketService(options);
		}
		throw new SDKException(SDKException.INPUT_ERROR, "Unsupport Exchange.");
	}
}
