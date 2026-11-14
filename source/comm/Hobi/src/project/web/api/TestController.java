package project.web.api;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;

import project.data.websocket.client.MarketClient;
import project.data.websocket.client.req.market.SubMarketTickerRequest;
import project.data.websocket.constant.HuobiOptions;

@RestController
@CrossOrigin
public class TestController {

	@RequestMapping("api/test")
	public void test() {
		MarketClient tickerClient = MarketClient.create(new HuobiOptions());
		String symbol = "btcusdt";
		SubMarketTickerRequest tickerReq = new SubMarketTickerRequest();
		tickerReq.setSymbol(symbol);
		tickerClient.subMarketTicker(tickerReq, (marketTradeEvent) -> {
	    	System.out.println(JSONObject.toJSONString(marketTradeEvent));
	    	// trade(marketTradeEvent, symbol);
	      });
	}
}
