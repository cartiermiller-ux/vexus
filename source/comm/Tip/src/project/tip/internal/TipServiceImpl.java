package project.tip.internal;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import kernel.util.StringUtils;
import kernel.web.ApplicationUtil;
import project.syspara.SysparaService;
import project.tip.TipConstants;
import project.tip.TipService;
import project.tip.model.Tip;
import security.SecUser;
import security.internal.SecUserService;

public class TipServiceImpl implements TipService {
	
	private SecUserService secUserService;
	
	private SysparaService sysparaService;
	
	private Map<String, Tip> tipCache = new ConcurrentHashMap<String, Tip>();
	
	private static final Logger logger=LoggerFactory.getLogger(TipServiceImpl.class);
	
	/**
	 * 初始化数据
	 */
	public void init() {
		List<Tip> list=ApplicationUtil.executeSelect(Tip.class);
		if(null==list || list.isEmpty()) return;
		list.forEach(tip->tipCache.put(tip.getBusiness_id(), tip));
	}

	/**
	 * 新增通知
	 * @param businessId 业务id(唯一性)
	 * @param model 模块
	 */
	public void saveTip(String businessId, String model) {
		try {
			Tip tip=tipCache.get(businessId);
			if (null==tip) tip=new Tip();
			
			tip.setBusiness_id(businessId);
			tip.setCreate_time(new Date());
			tip.setModel(model);
			tip.setTime_stamp(new Date().getTime());
			
			ApplicationUtil.executeSaveOrUpdate(tip);
			tipCache.put(businessId, tip);
		} catch (Exception e) {
			logger.error("fail put tip businessId:{" + businessId + "},model:{" + model + "},e:{}", e);
		}
	}
	
	/**
	 * 新增通知
	 * @param businessId 业务id(唯一性)
	 * @param model      模块
	 */
	public void saveNewTip(String businessId, String model,String remark) {
		try {
			Tip tip = tipCache.get(businessId);
			if (null == tip) tip = new Tip();
			
			tip.setBusiness_id(businessId);
			tip.setCreate_time(new Date());
			tip.setModel(model);
			tip.setTime_stamp(new Date().getTime());
			tip.setTarget_username(remark);
			
			ApplicationUtil.executeSaveOrUpdate(tip);
			tipCache.put(businessId, tip);
		} catch (Exception e) {
			logger.error("fail put tip businessId22:{" + businessId + "},model:{" + model + "},e:{}", e);
		}
	}

	/**
	 * 新增通知
	 * @param tip 消息通知
	 */
	public void saveTip(Tip tip) {
		try {
			tip.setCreate_time(new Date());
			tip.setTime_stamp(new Date().getTime());
			
			ApplicationUtil.executeInsert(tip);
			tipCache.put(tip.getBusiness_id(), tip);
		} catch (Exception e) {
			logger.error("fail put tip businessId:{" + tip.getBusiness_id() + "},model:{" + tip.getModel() + "},e:{}", e);
		}
	}

	/**
	 * 移除通知
	 * @param businessId
	 */
	public void deleteTip(String businessId) {
		try {
			ApplicationUtil.executeDelete(Tip.class,"WHERE BUSINESS_ID=?",new Object[] {businessId});
			tipCache.remove(businessId);
		} catch (Exception e) {
			logger.error("fail remove tip businessId:{" + businessId + "},e:{}", e);
		}
	}

	/**
	 * 批量移除通知
	 * @param businessId
	 */
	public void deleteTip(List<String> businessIds) {
		if(null==businessIds || businessIds.isEmpty()) return;
		List<Object[]> paramList=businessIds.stream().map(id->new Object[] {id}).collect(Collectors.toList());
		ApplicationUtil.executeBatchDelete(Tip.class, "WHERE BUSINESS_ID=?", paramList);
		for (String id : businessIds) tipCache.remove(id);
	}
	
	/**
	 * 获取总数 数据
	 * @param username
	 * @return
	 */
	public List<Map<String, Object>> cacheSumTips(String username) {
		List<Map<String, Object>> result = new LinkedList<Map<String, Object>>();

		//获取参数用户的所有提示记录
		List<Tip> filterTips = filterTips(username, null);
		if (CollectionUtils.isEmpty(filterTips)) return result;
		
		//获取用户所有模块资源权限
		Set<String> resourceIds = getPrivileges(username);
		
		//将模块资源列表转换为模块到提示集合的映射字典
		Map<String, List<Tip>> modelMap = modelData(filterTips);
		for (Entry<String, List<Tip>> entry : modelMap.entrySet()) {
			if (resourceIds.contains(entry.getKey())) {//有权限则返回通知
				result.add(tipData(entry.getKey(), entry.getValue()));
			}
		}
		
		return result;
	}

	/**
	 * 获取指定模块的新通知数据
	 * @param username
	 * @return
	 */
	public LinkedList<Map<String, Object>> cacheNewTipsByModel(String username, Long lastTimeStamp, String model) {
		LinkedList<Map<String, Object>> result = new LinkedList<Map<String, Object>>();

		//获取参数用户的所有提示记录
		List<Tip> filterNewTips = filterTips(username, lastTimeStamp);
		if (CollectionUtils.isEmpty(filterNewTips)) return result;
		
		//将模块资源列表转换为模块到提示集合的映射字典
		Map<String, List<Tip>> modelMap = modelData(filterNewTips);
		List<Tip> tipList = modelMap.get(model);
		
		if (CollectionUtils.isEmpty(tipList)) return result;
		
		result.add(tipNewData(model, tipList));
		return result;
	}

	/**
	 * 获取新通知数据
	 * @param username
	 * @return 
	 */
	public LinkedList<Map<String, Object>> cacheNewTips(String username, Long lastTimeStamp) {
		LinkedList<Map<String, Object>> result=new LinkedList<Map<String, Object>>();

		//获取参数用户的所有提示记录
		List<Tip> filterNewTips = filterTips(username, lastTimeStamp);
		if (CollectionUtils.isEmpty(filterNewTips)) return result;
		
		//获取用户所有模块资源权限
		Set<String> resourceIds = getPrivileges(username);
		
		//将模块资源列表转换为模块到提示集合的映射字典
		Map<String, List<Tip>> modelMap = modelData(filterNewTips);
		
		for (Entry<String, List<Tip>> entry : modelMap.entrySet()) {
			if (resourceIds.contains(entry.getKey())) { //有权限则返回通知
				result.add(tipNewData(entry.getKey(), entry.getValue()));
			}
		}
		
		return result;
	}
	
	
	/**
	 * 获取用户所有权限集合
	 * @param username 用户名
	 * @return 权限字串集合
	 */
	private Set<String> getPrivileges(String username) {
		SecUser user = this.secUserService.findUserByLoginName(username);
		return user.getRoles().stream().flatMap(role->role.getResources().stream()).map(resource->resource.getId().toString()).collect(Collectors.toSet());
	}
	
	/**
	 * 过滤参数用户指定时间戳之后的提示记录
	 * @param username 用户名
	 * @param lastTimeStamp 时间戳
	 * @return 提示列表
	 */
	private List<Tip> filterTips(final String username, final Long lastTimeStamp) {
		ArrayList<Tip> tipList = new ArrayList<Tip>(tipCache.values());
		CollectionUtils.filter(tipList, new Predicate() { //过滤查找新生成的通知数据
			@Override
			public boolean evaluate(Object paramObject) {
				Tip tip = (Tip) paramObject;
				if (TipConstants.MUST_USERNAME_MODEL.containsKey(tip.getModel())) {
					if (StringUtils.isNotEmpty(tip.getTarget_username()) && username.equals(tip.getTarget_username())) {
						return lastTimeStamp == null || tip.getTime_stamp() > lastTimeStamp;
					} else {
						return false;
					}
				}
				return lastTimeStamp == null || ((Tip) paramObject).getTime_stamp() > lastTimeStamp;
			}
		});
		return tipList;
	}

	/**
	 * 每个模块对应的提醒数据列表
	 * @param tips 提醒数据列表
	 * @return 提醒数据字典
	 */
	private Map<String, List<Tip>> modelData(Collection<Tip> tips) {
		HashMap<String, List<Tip>> modelMap = new HashMap<String, List<Tip>>();
		
		for (Tip tip : tips) {
			List<Tip> list = modelMap.get(tip.getModel());
			if (null==list) modelMap.put(tip.getModel(),list=new ArrayList<Tip>());
			list.add(tip);
		}
		
		return modelMap;
	}

	/**
	 * 构建模块的通知数据(New)
	 * @param model 模块
	 * @param list 模块列表数据
	 * @return 通知数据字典
	 */
	private Map<String, Object> tipNewData(String model, final List<Tip> list) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		// 模块提示数量
		resultMap.put("tip_content_num", list.size());
		// 生成html通知
		String htmlMessage = MessageFormat.format(TipConstants.MESSAGE_MAP.get(model),MessageFormat.format("<span class=\"label label-danger\">{0}</span>", list.size()));
		// 模块提示消息
		resultMap.put("tip_message", list.size()>0? htmlMessage:"");
		// 模块请求url
		resultMap.put("tip_url", list.size()>0?TipConstants.ACTION_MAP.get(model) : "");
		// 是否右下角提示
		resultMap.put("tip_show",sysparaService.find("tip_noshow_models").getValue().indexOf(model)==-1);
		return resultMap;
	}
	
	/**
	 * 构建模块的通知数据(New)
	 * @param model 模块
	 * @param list  模块列表数据
	 * @return 通知数据字典
	 */
	private Map<String, Object> tipData(String model, final List<Tip> list) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		// 页面上对应的classname(可自定义)
		resultMap.put("tip_dom_name", TipConstants.DOM_MAP.get(model));
		// 模块总数
		resultMap.put("tip_content_sum", list.size());
		// 模块新增数
		return resultMap;
	}

	public void setSecUserService(SecUserService secUserService) {
		this.secUserService = secUserService;
	}

	public void setSysparaService(SysparaService sysparaService) {
		this.sysparaService = sysparaService;
	}
}
