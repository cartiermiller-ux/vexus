package systemuser.internal;

import java.util.List;
import java.util.stream.Collectors;

import kernel.web.ApplicationUtil;
import systemuser.ResourceMappingService;
import systemuser.model.ResourceMapping;

public class ResourceMappingServiceImpl implements ResourceMappingService {

	public List<ResourceMapping> findBySetIds(List<String> idList){
		String whereSubStatement=idList.stream().map(id->"'"+id+"'").collect(Collectors.joining(",","WHERE SET_UUID IN(",")"));
		return ApplicationUtil.executeSelect(ResourceMapping.class,whereSubStatement,null);
	}
}
