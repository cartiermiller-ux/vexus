package project.finance;

import java.util.List;
import java.util.Map;

import project.miner.model.Miner;

/**
 * 理财产品
 * 
 * @author User
 *
 */
public interface FinanceService {

	public void save(Finance finance,String login_safeword,String operaterUsername);

	public void update(Finance finance,String login_safeword,String operaterUsername);

	public Finance findById(String id);

	public void delete(String id,String login_safeword,String operaterUsername);

	public List<Finance> findAll();
	
	public Map<String, Finance> findFinanceMap();

	public List<Finance> findAllState_1();

}
