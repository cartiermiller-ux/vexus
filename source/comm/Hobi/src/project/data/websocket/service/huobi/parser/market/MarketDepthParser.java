package project.data.websocket.service.huobi.parser.market;

import java.util.List;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import project.data.websocket.model.market.MarketDepth;
import project.data.websocket.service.huobi.parser.HuobiModelParser;

public class MarketDepthParser implements HuobiModelParser<MarketDepth> {

	@Override
	public MarketDepth parse(JSONObject json) {
		MarketDepth depth = new MarketDepth();
		depth.setVersion(json.getLong("version"));
		depth.setTs(json.getLong("ts"));
		depth.setBids(new PriceLevelParser().parseArray(json.getJSONArray("bids")));
		depth.setAsks(new PriceLevelParser().parseArray(json.getJSONArray("asks")));
		return depth;
	}

	@Override
	public MarketDepth parse(JSONArray json) {
		return null;
	}

	@Override
	public List<MarketDepth> parseArray(JSONArray jsonArray) {
		return null;
	}

}
