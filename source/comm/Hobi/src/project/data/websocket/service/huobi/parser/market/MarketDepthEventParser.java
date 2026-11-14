package project.data.websocket.service.huobi.parser.market;

import java.util.List;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import project.data.websocket.model.market.MarketDepthEvent;
import project.data.websocket.service.huobi.parser.HuobiModelParser;
import project.data.websocket.service.huobi.utils.DataUtils;

public class MarketDepthEventParser implements HuobiModelParser<MarketDepthEvent> {

	@Override
	public MarketDepthEvent parse(JSONObject json) {
		String dataKey = DataUtils.getDataKey(json);
		MarketDepthEvent event = new MarketDepthEvent();
		event.setCh(json.getString("ch"));
		event.setTs(json.getLong("ts"));
		event.setDepth(new MarketDepthParser().parse(json.getJSONObject(dataKey)));
		return event;
	}

	@Override
	public MarketDepthEvent parse(JSONArray json) {
		return null;
	}

	@Override
	public List<MarketDepthEvent> parseArray(JSONArray jsonArray) {
		return null;
	}

}
