package project.data.internal;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;

import kernel.util.Arith;
import kernel.util.DateUtils;
import kernel.web.ApplicationUtil;
import kernel.web.Page;
import project.data.DataCache;
import project.data.DataDBService;
import project.data.job.RealtimeQueue;
import project.data.model.Realtime;
import project.item.ItemService;
import project.item.model.Item;
import project.syspara.SysparaService;

public class DataDBServiceImpl implements DataDBService {
	
	private NamedParameterJdbcOperations namedParameterJdbcTemplate;
	
	private SysparaService sysparaService;
	
	private ItemService itemService;
	
	@Override
	public void saveAsyn(Realtime entity) {
		Realtime current = DataCache.getRealtime(entity.getSymbol());
		if (current == null || current.getTs() != entity.getTs()) {

			Item item = itemService.cacheBySymbol(entity.getSymbol(), true);
			/**
			 * 交易量倍数不为空或0时修改倍数
			 */
			if (item.getMultiple() > 0) {
				entity.setVolume(Arith.mul(entity.getVolume(), item.getMultiple()));
				entity.setAmount(Arith.mul(entity.getAmount(), item.getMultiple()));
			}

			Double high = DataCache.getRealtimeHigh().get(entity.getSymbol());
			if (high != null && high >= entity.getClose()) {
				entity.setHigh(high);
			}

			
			Double low = DataCache.getRealtimeLow().get(entity.getSymbol());
			if (low != null && low <= entity.getClose()) {
				entity.setLow(low);
			}
			
			Double h24Before = DataCache.getRealtime24HBeforeOpen().get(entity.getSymbol());
			if (h24Before != null) {
				entity.setOpen(h24Before);
			}
			
			/**
			 * 时间有变化，才保存
			 */
			DataCache.putRealtime(entity.getSymbol(), entity);
			List<Realtime> list = DataCache.getRealtimeHistory().get(entity.getSymbol());
			if (list == null) list = new LinkedList<Realtime>();
			if (entity.getLow() > 0) {
				if (entity.getHigh() < entity.getClose()) {
					entity.setHigh(entity.getClose());
				}
				if (entity.getLow() > entity.getClose()) {
					entity.setLow(entity.getClose());
				}
				/**
				 * 修正最低为0的BUG，直接丢弃
				 */
				list.add(entity);
				DataCache.getRealtimeHistory().put(entity.getSymbol(), list);
				RealtimeQueue.add(entity);
			}
		}
	}

	@Override
	public void saveBatch(List<Realtime> entities) {
		for (int i = 0; i < entities.size(); ApplicationUtil.executeSaveOrUpdate(entities.get(i++)));
	}

	@Override
	public Realtime get(String symbol) {
		Page page = new Page(1, 1, Integer.MAX_VALUE);
		List<Realtime> list = ApplicationUtil.executeSelect(Realtime.class,"WHERE SYMBOL=? LIMIT ?,?",new Object[] {symbol,page.getFirstElementNumber(),page.getPageSize()});
		return list.size()<=0?null:list.get(0);
	}

	public void deleteRealtime(int days) {
		Map<String, Object> parameters = new HashMap<String, Object>();
		Long ts = DateUtils.addDate(new Date(), days).getTime();
		parameters.put("ts", ts);
		this.namedParameterJdbcTemplate.update("DELETE FROM T_REALTIME WHERE ts < :ts", parameters);

	}

	@Override
	public void updateOptimize(String table) {
		ApplicationUtil.executeSQL("OPTIMIZE TABLE "+table);
	}

	@Override
	public List<Realtime> findRealtimeOneDay(String symbol,int num) {		
		Page page = new Page(1,num,Integer.MAX_VALUE);
		return ApplicationUtil.executeSelect(Realtime.class,"WHERE SYMBOL=? ORDER BY TS ASC LIMIT ?,?",new Object[] {symbol,page.getFirstElementNumber(),page.getPageSize()});
	}

	public void setNamedParameterJdbcTemplate(NamedParameterJdbcOperations namedParameterJdbcTemplate) {
		this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
	}

	public void setSysparaService(SysparaService sysparaService) {
		this.sysparaService = sysparaService;
	}

	public void setItemService(ItemService itemService) {
		this.itemService = itemService;
	}
}
