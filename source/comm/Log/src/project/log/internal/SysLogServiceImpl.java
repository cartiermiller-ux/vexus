package project.log.internal;

import java.util.Date;

import kernel.web.ApplicationUtil;
import project.log.SysLog;
import project.log.SysLogService;

public class SysLogServiceImpl implements SysLogService {

	@Override
	public void saveSync(SysLog entity) {
		entity.setCreateTime(new Date());
		ApplicationUtil.executeInsert(entity);
	}

	@Override
	public void saveAsyn(SysLog entity) {
		entity.setCreateTime(new Date());
		AbstractLogQueue.add(entity);
	}
}
