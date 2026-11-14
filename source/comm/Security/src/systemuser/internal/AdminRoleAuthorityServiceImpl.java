package systemuser.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.security.providers.encoding.PasswordEncoder;

import kernel.exception.BusinessException;
import kernel.util.StringUtils;
import kernel.web.ApplicationUtil;
import project.Constants;
import project.user.googleauth.GoogleAuthService;
import security.Resource;
import security.ResourceService;
import security.Role;
import security.RoleService;
import security.SecUser;
import security.internal.SecUserService;
import systemuser.AdminRoleAuthorityService;
import systemuser.ResourceMappingService;
import systemuser.model.ResourceMapping;

@SuppressWarnings({"unchecked","rawtypes"})
public class AdminRoleAuthorityServiceImpl implements AdminRoleAuthorityService{

	private RoleService roleService;
	
	private SecUserService secUserService;
	
	private PasswordEncoder passwordEncoder;
	
	private ResourceService resourceService;
	
	private GoogleAuthService googleAuthService;
	
	private ResourceMappingService resourceMappingService;
	
	/**
	 * 角色列表
	 * @return
	 */
	public List<Map<String,Object>> getAllRole(){
		ArrayList<String> roles = new ArrayList<String>(Constants.ROLE_MAP.keySet());
		roles.remove(Constants.SECURITY_ROLE_FINANCE);
		roles.remove(Constants.SECURITY_ROLE_CUSTOMER);
		roles.remove(Constants.SECURITY_ROLE_MAINTAINER);
		roles.remove(Constants.SECURITY_ROLE_AGENT);
		roles.remove(Constants.SECURITY_ROLE_C2C);
		String inSubStatement=roles.stream().map(roleName->"'"+roleName+"'").collect(Collectors.joining(",","(",") GROUP BY role.UUID"));
		
		StringBuilder sqlBuilder=new StringBuilder("SELECT role.UUID id,role.ROLE_NAME roleName,GROUP_CONCAT(DISTINCT r_name.NAME separator ' , ') names ");
		sqlBuilder.append("FROM SCT_ROLE role LEFT JOIN SCT_ROLE_RESOURCE role_resource ON role_resource.ROLE_UUID=role.UUID ");
		sqlBuilder.append("LEFT JOIN SCT_RESOURCE_MAPPING resource_mapping ON resource_mapping.RESOURCE_UUID=role_resource.RESOURCE_UUID ");
		sqlBuilder.append("LEFT JOIN SCT_RESOURCE_SET_NAME r_name ON r_name.UUID=resource_mapping.SET_UUID ");
		sqlBuilder.append("WHERE ROLE_NAME NOT IN");
		sqlBuilder.append(inSubStatement);
		
		List<Map> list=ApplicationUtil.executeDQL(sqlBuilder.toString(),Map.class);
		if(null==list || list.isEmpty()) return new ArrayList<Map<String,Object>>();
		
		return list.stream().map(map->(Map<String,Object>)map).collect(Collectors.toList());
	}
	
	/**
	 * 获取角色所有的映射id
	 * @param roleId
	 * @return
	 */
	public List<String> getRoleResourceMappingIdById(String roleId){
		Role role = roleService.get(roleId);
		if(null==role) throw new BusinessException("角色不存在");
		
		Set<Resource> resources = role.getResources();
		if(CollectionUtils.isEmpty(resources)) return new ArrayList<String>();
		
		List<Map<String, Object>> list=getResourceName(getOPResourceIdByResources(resources));
		return list.stream().map(map->map.get("set_id").toString()).collect(Collectors.toList());
	}
	
	/**
	 * 根据资源获取操作权限id
	 * @param resources
	 * @return
	 */
	private List<String> getOPResourceIdByResources(Collection<Resource> resources){
		return resources.stream().filter(r->Resource.RESOURCE_TYPE_OPERATION.equals(r.getResType())).map(r->r.getId().toString()).collect(Collectors.toList());
	}
	
	/**
	 * 根据资源id列表 获取到映射的名字和id
	 * @param resourcesIds
	 * @return
	 */
	public List<Map<String, Object>> getResourceName(List<String> resourcesIds){
		if(null!=resourcesIds && resourcesIds.size()==0) return new ArrayList<Map<String, Object>>();
		
		StringBuilder sqlBuilder=new StringBuilder("SELECT r_map.SET_UUID set_id,r_name.NAME name,GROUP_CONCAT(r_map.RESOURCE_UUID separator ',') resources ");
		sqlBuilder.append("FROM SCT_RESOURCE_MAPPING r_map LEFT JOIN SCT_RESOURCE_SET_NAME r_name ON r_name.UUID=r_map.SET_UUID WHERE 1=1 ");
		if(resourcesIds!=null) {
			sqlBuilder.append("AND r_map.RESOURCE_UUID IN").append(resourcesIds.stream().filter(id->null!=id && !id.isEmpty()).map(rid->"'"+rid+"'").collect(Collectors.joining(",","(",") ")));
		}
		sqlBuilder.append("GROUP BY r_map.SET_UUID");
		
		List<Map> list=ApplicationUtil.executeDQL(sqlBuilder.toString(),Map.class);
		if(null==list || list.isEmpty()) return new ArrayList<Map<String,Object>>();
		
		return list.stream().map(map->(Map<String,Object>)map).collect(Collectors.toList());
	}
	
	/**
	 * 根据映射id 更新角色资源
	 * @param roleId
	 * @param resourceMapIds 映射id ("a,b,c"的形式)
	 */
	public void updateRoleResource(String roleId,String resourceMapIds,String operaterUsername,String loginSafeword,String code,String ip,String superGoogleAuthCode) {
		googleAuthService.checkSuperGoogleAuthCode(superGoogleAuthCode);
		checkLoginSafeword(operaterUsername,loginSafeword);
		
		Role role = roleService.get(roleId);
		if(null==role) throw new BusinessException("角色不存在");
		
		List<Map<String, Object>> beforeResourceMap = getResourceName(getOPResourceIdByResources(role.getResources()));
		resourceMapIds = checkResourceUserRecord(resourceMapIds, operaterUsername, beforeResourceMap);
		
		List<String> ids = new LinkedList<String>();
		if(StringUtils.isEmptyString(resourceMapIds)) {
			role.setResources(new HashSet<Resource>());
		}else {
			List<ResourceMapping> mappings = resourceMappingService.findBySetIds(Arrays.asList(resourceMapIds.replaceAll(" ", "").split(",")));
			for(ResourceMapping mapping:mappings) {
				ids.add(mapping.getResource_id());
				ids.add(Resource.RESOURCE_TYPE_URL+"_"+mapping.getResource_id());
			}
			List<Resource> list = resourceService.getByIds(ids);
			role.setResources(new HashSet<Resource>(list));
		}
		
		/*
		 * 如果客服默认添加客服中心权限，个人中心是属于客服默认权限，所以mapping映射表没有存在映射关系，不会因为修改而不添加
		 * 补充添加，不会因为修改了权限了导致消失
		 */
		if(Constants.SECURITY_ROLE_CUSTOMER.equals(role.getRoleName())) {
			Set<Resource> resources = role.getResources();
			resources.add(resourceService.get("OP_ADMIN_ONLINECHAT"));
			role.setResources(resources);
		}
		
		List<String> beforeResourceName = beforeResourceMap.stream()
				.filter(map->{Object name=map.get("name");return name!=null&&!name.toString().isEmpty();})
				.map(map->map.get("name").toString()).collect(Collectors.toList());
		
		List<Map<String, Object>> afterResourceMap = getResourceName(getOPResourceIdByResources(role.getResources()));
		List<String> afterResourceName = afterResourceMap.stream()
				.filter(map->{Object name=map.get("name");return name!=null&&!name.toString().isEmpty();})
				.map(map->map.get("name").toString()).collect(Collectors.toList());
		
		roleService.update(role,operaterUsername,String.join(",", beforeResourceName),String.join(",", afterResourceName),code,ip);
		
	}
	/**
	 * 假分核查权限检验处理
	 * @param resourceMapIds
	 * @param operaterUsername
	 * @param beforeResourceMap
	 */
	private String checkResourceUserRecord(String resourceMapIds,String operaterUsername,List<Map<String, Object>> beforeResourceMap) {
		if("root".equals(operaterUsername) || CollectionUtils.isEmpty(beforeResourceMap)) return resourceMapIds;
		
		boolean hasUR = false;
		for(Map<String, Object> data:beforeResourceMap) { //非root操作，有假分权限 且 新权限中无假分权限则加回
			if("SECURITY_USER_RECORD".equals(data.get("set_id").toString())
					&&(StringUtils.isEmptyString(resourceMapIds)||resourceMapIds.indexOf("SECURITY_USER_RECORD")==-1)) {
				resourceMapIds+=", SECURITY_USER_RECORD";
				hasUR = true;
				break;
			}
		}
		
		//非root操作，无假分权限则移除
		if(!hasUR && resourceMapIds.indexOf("SECURITY_USER_RECORD")!=-1) {
			resourceMapIds.replace("SECURITY_USER_RECORD", "");
		}
		
		return resourceMapIds;
	}

	/**
	 * 验证登录人资金密码
	 * @param operatorUsername
	 * @param loginSafeword
	 */
	private void checkLoginSafeword(String operatorUsername,String loginSafeword) {
		SecUser sec = secUserService.findUserByLoginName(operatorUsername);
		String safeword_md5 = passwordEncoder.encodePassword(loginSafeword, operatorUsername);
		if (!safeword_md5.equals(sec.getSafeword())) throw new BusinessException("登录人资金密码错误");
	}
	
	public void delete(String roleId,String operaterUsername,String loginSafeword,String code,String ip,String superGoogleAuthCode) {
		googleAuthService.checkSuperGoogleAuthCode(superGoogleAuthCode);
		checkLoginSafeword(operaterUsername,loginSafeword);
		
		Role role = roleService.get(roleId);
		if(null==role) throw new BusinessException("角色不存在");
		
		if(Constants.ROLE_MAP.containsKey(role.getRoleName())) throw new BusinessException("该权限无法删除");
		
		roleService.removeById(role.getId().toString(),operaterUsername,ip);
	}
	
	public void setRoleService(RoleService roleService) {
		this.roleService = roleService;
	}

	public void setResourceService(ResourceService resourceService) {
		this.resourceService = resourceService;
	}

	public void setResourceMappingService(ResourceMappingService resourceMappingService) {
		this.resourceMappingService = resourceMappingService;
	}
	
	public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
		this.passwordEncoder = passwordEncoder;
	}
	
	public void setSecUserService(SecUserService secUserService) {
		this.secUserService = secUserService;
	}
	
	public void setGoogleAuthService(GoogleAuthService googleAuthService) {
		this.googleAuthService = googleAuthService;
	}
}
