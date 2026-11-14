package project.log.internal;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import kernel.web.ApplicationUtil;
import kernel.web.Page;
import project.log.Log;
import project.log.LogService;

public class LogServiceImpl implements LogService {
	
	@Override
	public void saveSync(Log entity) {
		if(null==entity) return;
		
		entity.setCreateTime(new Date());
		if(null==entity.getId()) entity.setId(ApplicationUtil.getCurrentTimeUUID());
		
		ApplicationUtil.executeInsert(entity);
	}

	@Override
	public void saveAsyn(Log entity) {
		entity.setCreateTime(new Date());
		AbstractLogQueue.add(entity);		
	}
	
	public Page pagedQuery(int pageNo, int pageSize, Serializable partyId, String[] category, String[] extra, Date createTime_begin, Date createTime_end) {
		StringBuilder whereBuilder=new StringBuilder("WHERE 1=1 ");
		if (null!=category) {
			for(int i=0;i<category.length;category[i]="'"+category[i++]+"'");
			whereBuilder.append("AND CATEGORY IN (").append(String.join(",",category)).append(") ");
		}
		
		if (null!=extra) {
			for(int i=0;i<extra.length;extra[i]="'"+extra[i++]+"'");
			whereBuilder.append("AND EXTRA IN (").append(String.join(",",extra)).append(") ");
		}
		
		Page page = new Page(pageNo, pageSize, Integer.MAX_VALUE);
		ArrayList<Object> whereParamList=new ArrayList<Object>();
		
		if (null!=createTime_begin) {
			whereBuilder.append("AND CREATE_TIME>=? ");
			whereParamList.add(createTime_begin);
		}
		
		if (null!=createTime_end) {
			whereBuilder.append("AND CREATE_TIME<? ");
			whereParamList.add(createTime_end);
		}
		
		if (null!=partyId) {
			whereBuilder.append("AND PARTY_ID=? ");
			whereParamList.add(partyId);
		}
		
		whereBuilder.append("ORDER BY CREATE_TIME DESC LIMIT ?,?");
		whereParamList.add(page.getFirstElementNumber());
		whereParamList.add(pageSize);
		
		List<Log> list=ApplicationUtil.executeSelect(Log.class,whereBuilder.toString(), whereParamList.toArray(new Object[whereParamList.size()]));
		page.setElements(list);
		return page;
	}
}
