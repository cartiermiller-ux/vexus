package project.c2c;

public interface C2cAppealService {

	public C2cAppeal get(String order_no);

	public void save(C2cAppeal entity);

	public void update(C2cAppeal entity);

	public void delete(String id);

	public C2cAppeal findById(String id);
	
	/*
	 * 查询未处理申诉数量，根据广告ID
	 */
	public Long findNoHandleAppealsCountByAdvertId(String c2cAdvertId);

}
