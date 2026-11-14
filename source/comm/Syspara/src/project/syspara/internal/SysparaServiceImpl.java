package project.syspara.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import kernel.util.StringUtils;
import kernel.web.ApplicationUtil;
import kernel.web.Page;
import project.RedisKeys;
import project.redis.RedisHandler;
import project.syspara.Syspara;
import project.syspara.SysparaService;

@SuppressWarnings("rawtypes")
public class SysparaServiceImpl implements SysparaService {
	
	private RedisHandler redisHandler;

	@Override
	public Syspara find(String code) {
		Syspara syspara = (Syspara) redisHandler.get(RedisKeys.SYSPARA_CODE + code);
		return null==syspara?null:syspara;
	}

	/**
	 * 数据库读取，主要用于bean初始化，（spring 初始化bean在redis数据加载之前，导致无法读取问题）
	 * @param code
	 * @return
	 */
	public Syspara findByDB(String code) {
		List<Syspara> list=ApplicationUtil.executeSelect(Syspara.class, "WHERE CODE=?", new Object[] {code});
		return (null==list || list.isEmpty())?null:list.get(0);
	}

	@Override
	public void update(Syspara entity) {
		if (0==entity.getType()) return;
		ApplicationUtil.executeUpdate(entity, "WHERE UUID=?", new Object[] {entity.getId()});
		redisHandler.setSync(RedisKeys.SYSPARA_CODE + entity.getCode(), entity);
	}

	@Override
	public Page pagedQuery(int pageNo, int pageSize) {
		if (pageNo <= 0) pageNo = 1;
		Page page = new Page(pageNo, pageSize, Integer.MAX_VALUE);
		List<HashMap> list=ApplicationUtil.executeDQL("SELECT CODE code,SVALUE value,NOTES notes FROM T_SYSPARA ORDER BY CODE DESC LIMIT ?,?",new Object[] {page.getFirstElementNumber(),pageSize},HashMap.class);
		page.setElements(list);
		return page;
	}
	
	/**
	 * 获取 系统参数（ROOT) 列表
	 */
	@Override
	public Page pagedQueryByNotes(int pageNo, int pageSize, String notes_para) {
		if (pageNo <= 0) pageNo = 1;
		Page page = new Page(pageNo, pageSize, Integer.MAX_VALUE);
		
		StringBuilder sqlBuilder=new StringBuilder("SELECT CODE code,SVALUE value,NOTES notes,MODIFY modify FROM T_SYSPARA WHERE STYPE!=3 ");
		ArrayList<Object> params=new ArrayList<Object>();
		if (!StringUtils.isNullOrEmpty(notes_para)) {
			sqlBuilder.append("AND NOTES LIKE ? ");
			params.add("%"+notes_para+"%");
		}
		
		sqlBuilder.append("ORDER BY CODE DESC LIMIT ?,?");
		params.add(page.getFirstElementNumber());
		params.add(pageSize);
		
		List<HashMap> list=ApplicationUtil.executeDQL(sqlBuilder.toString(),params.toArray(new Object[params.size()]),HashMap.class);
		page.setElements(list);
		return page;
	}

	/**
	 * 获取 系统参数（ADMIN) 列表
	 */
	@Override
	public Page pagedQueryByNotesAdmin(int pageNo, int pageSize, String notes_para) {
		if (pageNo <= 0) pageNo = 1;
		Page page = new Page(pageNo, pageSize, Integer.MAX_VALUE);
		
		StringBuilder sqlBuilder=new StringBuilder("SELECT CODE code,SVALUE value,NOTES notes,MODIFY modify FROM T_SYSPARA WHERE STYPE!=3 ");
		ArrayList<Object> params=new ArrayList<Object>();
		if (!StringUtils.isNullOrEmpty(notes_para)) {
			sqlBuilder.append("AND NOTES LIKE ? ");
			params.add("%"+notes_para+"%");
		}
		
		sqlBuilder.append("AND STYPE IN(2,1) ORDER BY CODE DESC LIMIT ?,?");
		params.add(page.getFirstElementNumber());
		params.add(pageSize);
		
		List<HashMap> list=ApplicationUtil.executeDQL(sqlBuilder.toString(),params.toArray(new Object[params.size()]),HashMap.class);
		page.setElements(list);
		return page;
	}

	@Override
	public void save(Syspara entity) {
		if(null==entity.getId()) entity.setId(ApplicationUtil.getCurrentTimeUUID());
		ApplicationUtil.executeInsert(entity);
		redisHandler.setSync(RedisKeys.SYSPARA_CODE + entity.getCode(), entity);
	}

	public void setRedisHandler(RedisHandler redisHandler) {
		this.redisHandler = redisHandler;
	}
}
