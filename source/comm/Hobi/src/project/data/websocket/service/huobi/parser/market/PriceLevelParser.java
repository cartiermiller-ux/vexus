package project.data.websocket.service.huobi.parser.market;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import project.data.websocket.model.market.PriceLevel;
import project.data.websocket.service.huobi.parser.HuobiModelParser;

public class PriceLevelParser implements HuobiModelParser<PriceLevel> {

	@Override
	public PriceLevel parse(JSONObject json) {
		return null;
	}

	@Override
	public PriceLevel parse(JSONArray json) {
		PriceLevel level = new PriceLevel();
		level.setPrice(json.getBigDecimal(0));
		level.setAmount(json.getBigDecimal(1));
		return level;
	}

	@Override
	public List<PriceLevel> parseArray(JSONArray jsonArray) {
		if (jsonArray == null || jsonArray.size() <= 0) {
			return new ArrayList<>();
		}
		List<PriceLevel> list = new ArrayList<>(jsonArray.size());
		for (int i = 0; i < jsonArray.size(); i++) {
			JSONArray array = jsonArray.getJSONArray(i);
			list.add(parse(array));
		}
		return list;
	}
}
