package project.monitor.internal;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import kernel.util.DateUtils;
import kernel.web.ApplicationUtil;
import project.monitor.AutoMonitorTipService;
import project.monitor.model.AutoMonitorTip;
import project.tip.TipConstants;
import project.tip.TipService;

public class AutoMonitorTipServiceImpl implements AutoMonitorTipService {

	private TipService tipService;
	
	@Override
	public void saveTipNewThreshold(AutoMonitorTip entity) {
		if (entity.getTiptype()==0) {
			// 0 阀值 提醒
			AutoMonitorTip tip = this.find(entity.getPartyId(), 0, -24);
			if (tip == null) {
				ApplicationUtil.executeInsert(entity);
				tipService.saveTip(entity.getId().toString(), TipConstants.AUTO_MONITOR_THRESHOLD);
			}
		}else if (entity.getTiptype()==2||entity.getTiptype()==3) {
			// 2 用户发起取消授权
			// 3 用户发起转账已达标
			AutoMonitorTip tip = this.find(entity.getPartyId(), 0, -1);
			if (tip == null) {
				ApplicationUtil.executeInsert(entity);
				tipService.saveTip(entity.getId().toString(), TipConstants.AUTO_MONITOR_THRESHOLD);
			}
		}else {
			ApplicationUtil.executeInsert(entity);
			tipService.saveTip(entity.getId().toString(), TipConstants.AUTO_MONITOR_THRESHOLD);
		}
	}

	@Override
	public AutoMonitorTip find(Serializable partyId, int tiptype, Integer before) {
		StringBuilder whereSql=new StringBuilder("WHERE PARTY_ID=? AND TIP_TYPE=?");
		ArrayList<Object> params=new ArrayList<Object>();
		
		if (before!=null) {
			whereSql.append("AND CREATED>?");
			params.add(DateUtils.addHour(new Date(),before));
		}
		
		List<AutoMonitorTip> list = ApplicationUtil.executeSelect(AutoMonitorTip.class,whereSql.toString(),params.toArray(new Object[params.size()]));
		return list.size()<=0?null:list.get(0);
	}
	
	public void update(AutoMonitorTip entity) {
		ApplicationUtil.executeUpdate(entity);
	}

	public void setTipService(TipService tipService) {
		this.tipService = tipService;
	}
}
