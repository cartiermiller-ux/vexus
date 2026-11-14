package project.item.internal;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import kernel.exception.BusinessException;
import kernel.web.ApplicationUtil;
import kernel.web.Page;
import project.item.AdminItemService;
import project.item.ItemService;
import project.item.model.Item;

public class AdminItemServiceImpl implements AdminItemService {
	private Logger logger = LoggerFactory.getLogger(AdminItemServiceImpl.class);
	private ItemService itemService;

	public Page pagedQuery(int pageNo, int pageSize, String market, String symbol) {
		Page page = new Page(pageNo, pageSize, Integer.MAX_VALUE);
		
		StringBuilder selectSqlBuilder=new StringBuilder("WHERE 1=1 ");
		ArrayList<Object> sqlParams=new ArrayList<Object>();
		if(null!=symbol && !(symbol=symbol.trim()).isEmpty()) {
			selectSqlBuilder.append("AND SYMBOL LIKE ? ");
			sqlParams.add("%"+symbol+"%");
		}
		
		selectSqlBuilder.append("LIMIT ?,?");
		sqlParams.add(page.getFirstElementNumber());
		sqlParams.add(pageSize);
		
		List<Item> list=ApplicationUtil.executeSelect(Item.class, selectSqlBuilder.toString(), sqlParams.toArray(new Object[sqlParams.size()]));
		page.setElements(list);
		return page;
	}

	@Override
	public Item get(String id) {
		List<Item> list=ApplicationUtil.executeSelect(Item.class, "WHERE UUID=?", new Object[] {id});
		return (null==list || list.isEmpty())?null:list.get(0);
	}

	@Override
	public void update(Item entity) {
		itemService.update(entity);
	}

	@Override
	public void save(Item entity) {
		Item item = itemService.cacheBySymbolData(entity.getSymbol_data());
		if (item != null) throw new BusinessException("交易品种已存在");
		itemService.add(entity);
	}

	public List<String> getSymbolsByMarket(String market) {
		return ApplicationUtil.executeDQL("SELECT SYMBOL FROM T_ITEM WHERE MARKET=?",new Object[] {market},String.class);
	}

	public Page pagedQuerySymbolsByMarket(int pageNo, int pageSize, String market) {
		Page page = new Page(pageNo, pageSize, Integer.MAX_VALUE);
		
		StringBuilder selectSqlBuilder=new StringBuilder("SELECT SYMBOL FROM T_ITEM WHERE 1=1 ");
		ArrayList<Object> sqlParams=new ArrayList<Object>();
		if(null!=market && !(market=market.trim()).isEmpty()) {
			selectSqlBuilder.append("AND MARKET=? ");
			sqlParams.add(market);
		}
		
		selectSqlBuilder.append("LIMIT ?,?");
		sqlParams.add(page.getFirstElementNumber());
		sqlParams.add(pageSize);
		
		List<String> list=ApplicationUtil.executeDQL(selectSqlBuilder.toString(),sqlParams.toArray(new Object[sqlParams.size()]),String.class);
		page.setElements(list);
		return page;
	}

	public boolean checkSymbolExit(String symbol) {
		List<Item> list = ApplicationUtil.executeSelect(Item.class, "WHERE SYMBOL=?",new Object[] {symbol});
		return null!=list && !list.isEmpty() && null!=list.get(0);
	}

	public void setItemService(ItemService itemService) {
		this.itemService = itemService;
	}

	@Override
	public List<Item> getItems() {
		return ApplicationUtil.executeSelect(Item.class);
	}
	
	@Override
	public List<String> getSymbols() {
		return ApplicationUtil.executeDQL("SELECT SYMBOL FROM T_ITEM", String.class);
	}
}
