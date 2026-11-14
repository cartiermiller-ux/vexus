package project.data.internal;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import kernel.util.DateUtils;
import kernel.web.ApplicationUtil;
import kernel.web.Page;
import project.data.DataCache;
import project.data.KlineService;
import project.data.model.Kline;
import project.data.model.Realtime;
import project.syspara.SysparaService;

public class KlineServiceImpl implements KlineService {
	
	private SysparaService sysparaService;
	
	private static final Logger logger = LoggerFactory.getLogger(KlineServiceImpl.class);
	
	public void saveInit(String symbol,String line,List<Kline> klineList) {
		ApplicationUtil.executeDelete(Kline.class, "WHERE SYMBOL=? AND PERIOD=?", new Object[] {symbol,line});
		ApplicationUtil.executeBatchInsert(klineList);
	}

	@Override
	public void saveOne(String symbol, String line) {
		Realtime realtime = DataCache.getRealtime(symbol);
		if (realtime == null) {
			logger.error("saveOne error, realtime is null,symbol [" + symbol + "]");
			return;
		}

		Kline lastOne = null;

		List<Kline> list = this.find(symbol, line, 1);
		if (list.size() > 0) {
			lastOne = list.get(0);
		}
		
		String key = symbol + "_" + line;
		Kline hobiOne = DataCache.getKline_hobi().get(key);
		if (hobiOne == null || lastOne == null) {
			// 取不到远程数据，直接退出
			return;
		}

		Kline kline = this.bulidKline(realtime, lastOne, hobiOne, line);

		kline.setPeriod(line);
		if(null==kline.getId()) kline.setId(ApplicationUtil.getCurrentTimeUUID());
		ApplicationUtil.executeInsert(kline);

		KlineTimeObject timeObject = DataCache.getKline(symbol, line);
		if (timeObject == null) {
			timeObject = new KlineTimeObject();
		}
		timeObject.getKline().add(kline);
		timeObject.setLastTime(new Date());
		DataCache.putKline(symbol, line, timeObject);
	}

	public Kline bulidKline(Realtime realtime, Kline lastOne, Kline hobiOne, String line) {
		Kline kline = new Kline();
		kline.setSymbol(realtime.getSymbol());
		kline.setTs(realtime.getTs());
		kline.setOpen(realtime.getOpen());
		kline.setHigh(realtime.getHigh());
		kline.setLow(realtime.getLow());
		kline.setClose(realtime.getClose());
		/**
		 * 新传回来的volume是固定的 需要除以Arith.div(realtime.getVolume(), 倍数)
		 */
		kline.setVolume(realtime.getVolume());

		if (lastOne != null) {
			kline.setOpen(lastOne.getClose());
		}
		int interval = this.sysparaService.find("data_interval").getInteger().intValue() / 1000;

		HighLow highLow = null;
		switch (line) {
		case "1min":
			highLow = HighLowHandle.get(realtime.getSymbol(), (60) / interval, interval);
			break;

		case "5min":
			highLow = HighLowHandle.get(realtime.getSymbol(), (60 * 5) / interval, interval);
			break;
		case "15min":
			highLow = HighLowHandle.get(realtime.getSymbol(), (60 * 15) / interval, interval);
			break;
		case "30min":
			highLow = HighLowHandle.get(realtime.getSymbol(), (60 * 30) / interval, interval);
			break;

		case "60min":
			highLow = HighLowHandle.get(realtime.getSymbol(), (60 * 60) / interval, interval);
			break;

		case "4hour":
			highLow = HighLowHandle.get(realtime.getSymbol(), (60 * 60 * 4) / interval, interval);
			break;
		case "1day":
			highLow = HighLowHandle.get(realtime.getSymbol(), (60 * 60 * 24) / interval, interval);
			break;

		case Kline.PERIOD_1WEEK:
			highLow = HighLowHandle.getByDay(realtime.getSymbol(), 7);
			break;

		case Kline.PERIOD_1MON:
			highLow = HighLowHandle.getByDay(realtime.getSymbol(), 30);
			break;

		}

		if (highLow != null && highLow.getHigh() != null) {
			kline.setHigh(highLow.getHigh());
		}
		if (highLow != null && highLow.getLow() != null) {
			kline.setLow(highLow.getLow());
		}

		kline.setVolume(hobiOne.getVolume());

		return kline;
	}

	@Override
	public List<Kline> find(String symbol, String line, int pageSize) {
		Page page = new Page(0, pageSize, Integer.MAX_VALUE);
		List<Kline> list=ApplicationUtil.executeSelect(Kline.class,"WHERE SYMBOL=? AND PERIOD=? ORDER BY TS DESC LIMIT ?,?",new Object[] {symbol,line,page.getFirstElementNumber(),pageSize});
		Collections.sort(list);
		return list;
	}

	@Override
	public void delete(String line, int days) {
		Long ts = DateUtils.addDate(new Date(), days).getTime();
		ApplicationUtil.executeDelete(Kline.class, "WHERE PERIOD=? AND TS<?", new Object[] {line,ts});
	}

	public void setSysparaService(SysparaService sysparaService) {
		this.sysparaService = sysparaService;
	}
}
