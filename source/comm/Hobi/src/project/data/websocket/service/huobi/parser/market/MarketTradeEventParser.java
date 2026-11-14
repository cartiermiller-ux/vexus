package project.data.websocket.service.huobi.parser.market;

import java.util.List;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import project.data.websocket.model.market.MarketTradeEvent;
import project.data.websocket.service.huobi.parser.HuobiModelParser;
import project.data.websocket.service.huobi.utils.DataUtils;

public class MarketTradeEventParser implements HuobiModelParser<MarketTradeEvent> {

  @Override
  public MarketTradeEvent parse(JSONObject json) {
    String dataKey = DataUtils.getDataKey(json);

    JSONObject data = json.getJSONObject(dataKey);
    JSONArray dataArray = data.getJSONArray("data");
    
    MarketTradeEvent event = new MarketTradeEvent();
    event.setCh(json.getString("ch"));
    event.setTs(json.getLong("ts"));
    event.setList(new MarketTradeParser().parseArray(dataArray));
    return event;
  }

  @Override
  public MarketTradeEvent parse(JSONArray json) {
    return null;
  }

  @Override
  public List<MarketTradeEvent> parseArray(JSONArray jsonArray) {
    return null;
  }
}
