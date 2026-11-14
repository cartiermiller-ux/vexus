package project.miner.internal;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;

import kernel.bo.RecordObjectMapper;
import project.miner.MinerParaService;
import project.miner.model.MinerPara;

public class MinerParaServiceImpl implements MinerParaService {
	
	private JdbcTemplate jdbcTemplate;

	public void save(MinerPara entity) {
		String insertSql = "INSERT INTO T_MINER_PARA(UUID,CYCLE,AMOUNT,MINER_ID) VALUES (?,?,?,?)";
		jdbcTemplate.update(insertSql,entity.getId(),entity.getCycle(),entity.getAmount(),entity.getMiner_id());
	}

	public void update(MinerPara entity) {
		String updateSql = "UPDATE T_MINER_PARA SET CYCLE=?,AMOUNT=?,MINER_ID=? WHERE UUID=?";
		jdbcTemplate.update(updateSql,entity.getCycle(),entity.getAmount(),entity.getMiner_id(),entity.getId());
	}

	public void delete(String id) {
		MinerPara entity = findById(id);
		if (null != entity) {
			jdbcTemplate.update("DELETE FROM T_MINER_PARA WHERE UUID=?", entity.getId());
		}
	}

	public MinerPara findById(String id) {
		List<MinerPara> list = jdbcTemplate.query("SELECT * FROM T_MINER_PARA WHERE UUID=?", RecordObjectMapper.newInstance(MinerPara.class), id);
		if (null != list && list.size() > 0) {
			return list.get(0);
        }
		return null;
	}

	public List<MinerPara> findByMinerId(String minerId) {
		List<MinerPara> list = jdbcTemplate.query("SELECT * FROM T_MINER_PARA WHERE MINER_ID=?", 
				RecordObjectMapper.newInstance(MinerPara.class), minerId);
		return list;
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}
	
}
