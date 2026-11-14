package project.ddos;

import kernel.web.Page;
import project.ddos.model.IpMenu;

public interface AdminIpMenuService {

	Page pagedQuery(int pageNo, int pageSize, String ip_para, String type_para);

	public void save(IpMenu entity, String operatorUsername, String loginSafeword, String ip);

	public void update(IpMenu entity, String operatorUsername, String loginSafeword, String ip);

	public void delete(String menu_ip, String operatorUsername, String loginSafeword, String ip);
}