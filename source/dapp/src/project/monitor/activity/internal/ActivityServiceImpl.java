package project.monitor.activity.internal;

import java.util.Date;
import java.util.List;

import kernel.web.ApplicationUtil;
import project.monitor.activity.Activity;
import project.monitor.activity.ActivityService;

public class ActivityServiceImpl implements ActivityService {

	@Override
	public List<Activity> findBeforeDate(Date sendtime) {
		return ApplicationUtil.executeSelect(Activity.class,"WHERE SEND_TIME>=?",new Object[] {sendtime});
	}
	
	@Override
	public Activity get(String id) {
		return ApplicationUtil.executeGet(id,Activity.class);
	}
	
	@Override
	public void save(Activity entity) {
		ApplicationUtil.executeInsert(entity);
	}

	@Override
	public void update(Activity entity) {
		ApplicationUtil.executeUpdate(entity);
	}

	@Override
	public void delete(Activity entity) {
		ApplicationUtil.executeDelete(entity);
	}
}
