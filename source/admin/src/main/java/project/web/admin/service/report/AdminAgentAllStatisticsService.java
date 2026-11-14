package project.web.admin.service.report;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import kernel.web.Page;

public interface AdminAgentAllStatisticsService {
	
	public Page pagedQuery(int pageNo, int pageSize,String startTime,String endTime,String loginPartyId,String usernameOrUid,String roleName,String targetPartyId);

	public String loadExportData(HttpServletResponse response, int pageSize,String startTime,String endTime,String loginPartyId,String usernameOrUid,String roleName,String targetPartyId) throws IOException;
	
	public List<Integer> getRecoNumNetList(String partyId);
}
