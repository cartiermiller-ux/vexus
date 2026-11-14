package project.c2c.internal;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.jdbc.core.JdbcTemplate;

import kernel.bo.RecordObjectMapper;
import project.RedisKeys;
import project.c2c.C2cAppeal;
import project.c2c.C2cAppealService;
import project.redis.RedisHandler;

public class C2cAppealServiceImpl implements C2cAppealService {

	private JdbcTemplate jdbcTemplate;
	private RedisHandler redisHandler;
	
	public C2cAppeal get(String order_no) {
		C2cAppeal appeal = (C2cAppeal) this.redisHandler.get(RedisKeys.C2C_APPEAL_ORDER_NO + order_no);
		if (null != appeal) {
			return appeal;
		} else {
			List<C2cAppeal> list = jdbcTemplate.query("SELECT * FROM T_C2C_APPEAL WHERE ORDER_NO=?", RecordObjectMapper.newInstance(C2cAppeal.class), order_no);
			if (list.size() > 0) {
				return (C2cAppeal) list.get(0);
			}
			return null;
		}
	}
	
	public void save(C2cAppeal entity) {
		if (null == entity) {
			return;
		}
		String insertSql = "INSERT INTO T_C2C_APPEAL(UUID,ORDER_NO,REASON,DESCRIPTION,IMG,NAME,PHONE,STATE,CREATE_TIME,UPDATE_TIME) VALUES (?,?,?,?,?,?,?,?,?,?)";
		jdbcTemplate.update(insertSql,entity.getId(),entity.getOrderNo(),entity.getReason(),entity.getDescription(),entity.getImg(),entity.getName(),entity.getPhone(),entity.getState(),entity.getCreateTime(),entity.getUpdateTime());
		if (null != entity && Arrays.asList("0").contains(entity.getState())) {
			// 只有已提交的申诉存入redis			
			this.redisHandler.setSync(RedisKeys.C2C_APPEAL_ORDER_NO + entity.getOrderNo(), entity);
		}
	}

	public void update(C2cAppeal entity) {
		if (null != entity) {
	        String insertSql="UPDATE T_C2C_APPEAL SET ORDER_NO=?, REASON=?, DESCRIPTION=?, IMG=?, NAME=?, PHONE=?, STATE=?, UPDATE_TIME=? WHERE UUID=?";
	        jdbcTemplate.update(insertSql, entity.getOrderNo(), entity.getReason(), entity.getDescription(), entity.getImg(), 
	        		entity.getName(), entity.getPhone(), entity.getState(), entity.getUpdateTime(),entity.getId());
			if (Arrays.asList("0").contains(entity.getState())) {
				// 只有已提交的申诉存入redis
				this.redisHandler.setSync(RedisKeys.C2C_APPEAL_ORDER_NO + entity.getOrderNo(), entity);
			} else {
				this.redisHandler.remove(RedisKeys.C2C_APPEAL_ORDER_NO + entity.getOrderNo());
			}
		}
	}

	public void delete(String id) {
		C2cAppeal entity = this.get(id);
		if (entity != null) {
			jdbcTemplate.update("DELETE FROM T_C2C_APPEAL WHERE UUID=?", entity.getId());
			this.redisHandler.remove(RedisKeys.C2C_APPEAL_ORDER_NO + entity.getOrderNo());
		}
	}

	public C2cAppeal findById(String id) {
		 List<C2cAppeal> list = jdbcTemplate.query("SELECT * FROM T_C2C_APPEAL WHERE UUID=?", RecordObjectMapper.newInstance(C2cAppeal.class), id);
	        if (null != list && list.size() > 0) {
	        	return list.get(0);
	        }
	        return null;
	}
	
	/*
	 * 查询未处理申诉数量，根据广告ID
	 */
	public Long findNoHandleAppealsCountByAdvertId(String c2cAdvertId) {

		List find = jdbcTemplate.queryForList(" SELECT COUNT(*) FROM T_C2C_APPEAL appeal LEFT JOIN T_C2C_ORDER ord ON appeal.ORDER_NO = ord.ORDER_NO WHERE ord.C2C_ADVERT_ID = ? AND appeal.state = '0' ", String.class, c2cAdvertId);
		return CollectionUtils.isEmpty(find) ? 0L : (null == find.get(0) ? 0L : Long.valueOf(find.get(0).toString()));
	}

	public void setRedisHandler(RedisHandler redisHandler) {
		this.redisHandler = redisHandler;
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}
	
}
