package project.data.websocket.service.huobi.parser.market;

import java.util.List;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import project.data.websocket.model.market.MarketTickerEvent;
import project.data.websocket.service.huobi.parser.HuobiModelParser;
import project.data.websocket.service.huobi.utils.DataUtils;

public class MarketTickerEventParser implements HuobiModelParser<MarketTickerEvent> {

	@Override
	public MarketTickerEvent parse(JSONObject json) {
		String dataKey = DataUtils.getDataKey(json);
		MarketTickerEvent event = new MarketTickerEvent();
		event.setCh(json.getString("ch"));
		event.setTs(json.getLong("ts"));
		event.setTicker(new MarketTickerParser().parse(json.getJSONObject(dataKey)));
		return event;
	}

	@Override
	public MarketTickerEvent parse(JSONArray json) {
		return null;
	}

	@Override
	public List<MarketTickerEvent> parseArray(JSONArray jsonArray) {
		return null;
	}
}
