package project.log.internal;

import java.util.Date;

import kernel.web.ApplicationUtil;
import project.log.CodeLog;
import project.log.CodeLogService;

public class CodeLogServiceImpl implements CodeLogService {
	
	@Override
	public void saveSync(CodeLog entity) {
		entity.setCreateTime(new Date());
		ApplicationUtil.executeInsert(entity);
	}
	
	@Override
	public void saveAsyn(CodeLog entity) {
		entity.setCreateTime(new Date());
		AbstractLogQueue.add(entity);
	}
}
