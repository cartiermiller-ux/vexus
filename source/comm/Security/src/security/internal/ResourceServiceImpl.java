package security.internal;

import java.util.List;
import java.util.stream.Collectors;

import kernel.web.ApplicationUtil;
import security.Resource;
import security.ResourceService;
import security.Role;

public class ResourceServiceImpl implements ResourceService{

    @Override
    public Resource get(String id) {
    	return bindRole(ApplicationUtil.executeGet(id, Resource.class));
    }
    
    public List<Resource> getByIds(List<String> idList) {
    	String whereSubStatement=idList.stream().map(id->"'"+id+"'").collect(Collectors.joining(",","WHERE UUID IN(",")"));
    	List<Resource> list=ApplicationUtil.executeSelect(Resource.class,whereSubStatement,null);
    	for(int i=0;i<list.size();bindRole(list.get(i++)));
    	return list;
    }
    
    /**
	 * 绑定角色到资源
	 * @param resource 资源对象
	 * @return 资源对象
	 */
	public static final Resource bindRole(Resource resource) {
		if(null==resource) return null;
		String whereSubStatement="WHERE UUID IN(SELECT ROLE_UUID FROM SCT_ROLE_RESOURCE WHERE RESOURCE_UUID=?)";
    	List<Role> roleList=ApplicationUtil.executeSelect(Role.class,whereSubStatement,new Object[] {resource.getId()});
    	if(null!=roleList && !roleList.isEmpty()) resource.setRoles(roleList.stream().filter(role->null!=role).collect(Collectors.toSet()));
    	return resource;
	}
}
