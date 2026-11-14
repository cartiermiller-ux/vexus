package security.internal;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.jdbc.core.JdbcTemplate;

import kernel.exception.BusinessException;
import kernel.web.ApplicationUtil;
import project.Constants;
import project.log.Log;
import project.log.LogService;
import security.Resource;
import security.Role;
import security.RoleService;

public class RoleServiceImpl implements RoleService {
	
	private LogService logService;
    
    private SecurityAuthoritiesHolder securityAuthoritiesHolder;
    
	public Role findRoleByName(String roleName) {
    	List<Role> list=ApplicationUtil.executeSelect(Role.class,"WHERE ROLE_NAME=?",new Object[] {roleName});
    	
    	if(null==list || list.isEmpty()) return null;
    	if(list.size()>1) throw new RuntimeException("Found Duplicate Name In Role Table!");
    	
    	return bindResource(list.get(0));
    }

    @Override
    public List<Role> getAll() {
    	List<Role> roleList=ApplicationUtil.executeSelect(Role.class);
    	if(null!=roleList) for(int i=0;i<roleList.size();bindResource(roleList.get(i++)));
        return roleList;
    }

    @Override
    public Role get(String id) {
    	return bindResource(ApplicationUtil.executeGet(id,Role.class));
    }
    
    public void addRole(Role role,String operaterUsername,String ip) {
        Role roleDB = this.findRoleByName(role.getRoleName());
        if (null != roleDB) throw new BusinessException("存在重复的角色名称");
        
        ApplicationUtil.executeInsert(role);
        cascadeResource(role);
        
        securityAuthoritiesHolder.clean();
        
        saveLog(role,operaterUsername,"ip:"+ip+"管理员添加角色："+role.getRoleName());
    }

    public void setSecurityAuthoritiesHolder(SecurityAuthoritiesHolder securityAuthoritiesHolder) {
        this.securityAuthoritiesHolder = securityAuthoritiesHolder;
    }
    
    public void update(Role role,String operaterUsername,String beforeResourceName,String afterResourceName,String code,String ip) {
       List<Integer> roleNameNums=ApplicationUtil.executeDQL("SELECT COUNT(UUID) FROM SCT_ROLE WHERE ROLE_NAME=?",new Object[]{role.getRoleName()},Integer.class);
       if(null!=roleNameNums && !roleNameNums.isEmpty() && roleNameNums.get(0)>1) throw new BusinessException("存在重复的角色名称");
       
       ApplicationUtil.executeSaveOrUpdate(role);
       cascadeResource(role);
       
       securityAuthoritiesHolder.clean();
       
       saveLog(role,operaterUsername,"ip:"+ip+"管理员修改角色名及角色权限，角色名：["+role.getRoleName()+"],原有权限：["+beforeResourceName+"],修改后权限：["+afterResourceName+"],验证码：["+code+"]");
    }

    @Override
    public void removeById(String id,String operaterUsername,String ip) {
    	List<Integer> userNums=ApplicationUtil.executeDQL("SELECT COUNT(USER_UUID) FROM SCT_USER_ROLE WHERE ROLE_UUID=?",new Object[]{id},Integer.class);
    	if(null!=userNums && !userNums.isEmpty() && userNums.get(0)>1) throw new BusinessException("角色被用户关联,不可删除");
    	
    	Role role=ApplicationUtil.executeGet(id, Role.class);
    	if(null!=role) {
    		ApplicationUtil.executeDel(id, Role.class);
    		ApplicationUtil.executeDML("DELETE FROM SCT_ROLE_RESOURCE WHERE ROLE_UUID=?",id);
    	}
    	
        securityAuthoritiesHolder.clean();
        saveLog(role,operaterUsername,"ip:"+ip+"管理员删除角色"+role.getRoleName());
    }
    
    public void saveLog(Role role, String operator,String context) {
		Log log = new Log();
		log.setCategory(Constants.LOG_CATEGORY_OPERATION);
		log.setUsername(operator);
		log.setOperator(operator);
		log.setLog(context);
		log.setCreateTime(new Date());
		logService.saveSync(log);
	}
    
	public void setLogService(LogService logService) {
		this.logService = logService;
	}
	
	/**
	 * 级联角色资源
	 * @param secuser 用户对象
	 */
	private static final void cascadeResource(Role role) {
		if(null==role) return;
		
		JdbcTemplate jdbcTemplate=ApplicationUtil.getBean(JdbcTemplate.class);
		Set<Resource> resources=role.getResources();
		Serializable roleId=role.getId();
		
		jdbcTemplate.update("DELETE FROM SCT_ROLE_RESOURCE WHERE ROLE_UUID=?",roleId);
		if(ObjectUtils.isEmpty(resources)) return;
		
		List<Object[]> paramsList=resources.stream().map(res->new Object[] {res.getId(),roleId}).collect(Collectors.toList());
		jdbcTemplate.batchUpdate("INSERT INTO SCT_ROLE_RESOURCE(RESOURCE_UUID,ROLE_UUID) VALUES(?,?)",paramsList);
	}
	
	/**
	 * 绑定资源到角色
	 * @param role 角色对象
	 * @return 角色对象
	 */
	private static final Role bindResource(Role role) {
		if(null==role) return null;
		String whereSubStatement="WHERE UUID IN(SELECT RESOURCE_UUID FROM SCT_ROLE_RESOURCE WHERE ROLE_UUID=?)";
    	List<Resource> resourceList=ApplicationUtil.executeSelect(Resource.class,whereSubStatement,new Object[] {role.getId()});
    	if(null!=resourceList && !resourceList.isEmpty()) role.setResources(resourceList.stream().filter(res->null!=res).collect(Collectors.toSet()));
    	return role;
	}
}
