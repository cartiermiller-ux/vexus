package project.data.websocket.service.huobi.parser.market;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import project.data.websocket.model.market.MarketTrade;
import project.data.websocket.service.huobi.parser.HuobiModelParser;

public class MarketTradeParser implements HuobiModelParser<MarketTrade> {

	  @Override
	  public MarketTrade parse(JSONObject json) {

	    Long tradeId = json.getLong("trade-id");
	    if (tradeId == null) {
	      tradeId = json.getLong("tradeId");
	    }
	    
	    MarketTrade trade = new MarketTrade();
	    trade.setId(json.get("id") == null ? null : json.get("id").toString());
	    trade.setTradeId(tradeId);
	    trade.setPrice(new BigDecimal(json.getString("price")));
	    trade.setAmount(new BigDecimal(json.getString("amount")));
	    trade.setDirection(json.getString("direction"));
	    trade.setTs(json.getLong("ts"));
	    return trade;
	  }

	  @Override
	  public MarketTrade parse(JSONArray json) {
	    return null;
	  }

	  @Override
	  public List<MarketTrade> parseArray(JSONArray jsonArray) {
	    if (jsonArray == null || jsonArray.size() <= 0) {
	      return new ArrayList<>();
	    }

	    List<MarketTrade> list = new ArrayList<>();
	    for (int i = 0; i < jsonArray.size(); i++) {
	      JSONObject jsonObject = jsonArray.getJSONObject(i);
	      list.add(parse(jsonObject));
	    }
	    return list;
	  }
	}
