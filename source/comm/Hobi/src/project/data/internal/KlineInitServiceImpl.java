package project.data.internal;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import kernel.web.ApplicationUtil;
import project.data.DataCache;
import project.data.KlineService;
import project.data.model.Kline;
import project.hobi.HobiDataService;
import project.item.ItemService;

public class KlineInitServiceImpl implements KlineInitService {
	
	private ItemService itemService;
	
	private KlineService klineService;
	
	private HobiDataService hobiDataService;
	
	private static final Pattern COMMA_REGEX=Pattern.compile(",");
	
	private static final Logger logger = LoggerFactory.getLogger(KlineInitServiceImpl.class);

	@Override
	public void klineInit(String symbols) {
		this.hobiDataService=ApplicationUtil.getBean(HobiDataService.class);
		DelayThread lockDelayThread=new DelayThread(symbols, klineService);
		this.itemService=ApplicationUtil.getBean(ItemService.class);
		Thread t = new Thread(lockDelayThread);
		t.start();
	}

	public class DelayThread implements Runnable {
		private KlineService klineService;
		private String symbol;

		public void run() {
			try {
				String[] symbols = COMMA_REGEX.split(symbol);
				for (int i = 0; i < symbols.length; i++) {
					this.saveInit(symbols[i], Kline.PERIOD_1MIN);
					this.saveInit(symbols[i], Kline.PERIOD_5MIN);
					this.saveInit(symbols[i], Kline.PERIOD_15MIN);
					this.saveInit(symbols[i], Kline.PERIOD_30MIN);
					this.saveInit(symbols[i], Kline.PERIOD_60MIN);
					this.saveInit(symbols[i], Kline.PERIOD_4HOUR);
					this.saveInit(symbols[i], Kline.PERIOD_1DAY);
					this.saveInit(symbols[i], Kline.PERIOD_1MON);
					this.saveInit(symbols[i], Kline.PERIOD_1WEEK);
				}
			} catch (Exception e) {
				logger.error("manual init Kline occur error:", e);
			}
		}
		
		private final void saveInit(String symbol, String line) {
			List<Kline> list = hobiDataService.kline(itemService.cacheBySymbol(symbol, true).getSymbol_data(), line, null,0);
			if(null!=list && !list.isEmpty()) {
				list.forEach(kline->{if(null==kline.getId()) kline.setId(ApplicationUtil.getCurrentTimeUUID());});
				klineService.saveInit(symbol,line,list);
			}
			
			KlineTimeObject model = new KlineTimeObject();
			Collections.sort(list);
			model.setKline(list);
			model.setLastTime(new Date());
			
			DataCache.putKline(symbol, line, model);
		}

		public DelayThread(String symbol, KlineService klineService) {
			this.symbol = symbol;
			this.klineService = klineService;
		}
	}

	public void setKlineService(KlineService klineService) {
		this.klineService = klineService;
	}

}
