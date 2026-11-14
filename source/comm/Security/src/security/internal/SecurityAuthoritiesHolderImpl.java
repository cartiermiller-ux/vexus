package security.internal;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import kernel.web.ApplicationUtil;
import security.Resource;
import security.Role;

public class SecurityAuthoritiesHolderImpl implements SecurityAuthoritiesHolder {

	private Map<String, Map<String, String>> cache = new HashMap<String, Map<String, String>>();

	private List<Resource> getResourcesByType(String resType) {
		List<Resource> resources=ApplicationUtil.executeSelect(Resource.class,"WHERE RES_TYPE=?",new Object[] {resType});
		String roleSql="SELECT * FROM SCT_ROLE R WHERE EXISTS (SELECT ROLE_UUID FROM SCT_ROLE_RESOURCE WHERE RESOURCE_UUID=? AND ROLE_UUID=R.UUID)";
		
		for(Resource resource:resources) {
			List<Role> roleList=ApplicationUtil.executeDQL(roleSql,new Object[] {resource.getId()}, Role.class);
			if(null==roleList || roleList.isEmpty()) continue;
			resource.setRoles(roleList.stream().filter(role->null!=role).collect(Collectors.toSet()));
		}
		
		return resources;
	}

	public Map<String, String> loadAuthorities(String resType) {
		Map<String, String> authorities = cache.get(resType);
		if (authorities != null) return authorities;
		
		cache.put(resType, authorities=new LinkedHashMap<String, String>());
		List<Resource> urlResources = getResourcesByType(resType);
		
		Collections.sort(urlResources,new Comparator<Resource>() {
            public int compare(Resource o1, Resource o2) {
            	if (o1.getResString().length()<o2.getResString().length()) {
					return 1;
				} else if (o1.getResString().length()==o2.getResString().length()){
					return 0;
				}else {
					return -1;
				}
            }
		  });
		
		for (Resource resource : urlResources) {
			authorities.put(resource.getResString(),resource.getRoleAuthorities());
		}
		
		return authorities;
	}

    @Override
    public void clean() {
        cache = new HashMap<String, Map<String, String>>();
    }
}
