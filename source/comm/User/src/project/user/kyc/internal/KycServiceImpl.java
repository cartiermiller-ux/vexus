package project.user.kyc.internal;

import java.text.MessageFormat;
import java.util.Date;
import java.util.List;

import kernel.exception.BusinessException;
import kernel.web.ApplicationUtil;
import project.redis.RedisHandler;
import project.syspara.SysparaService;
import project.user.UserRedisKeys;
import project.user.kyc.Kyc;
import project.user.kyc.KycService;

public class KycServiceImpl implements KycService {

	private RedisHandler redisHandler;
	
	private SysparaService sysparaService;

	@Override
	public Kyc get(String partyId) {
		Kyc kyc = (Kyc) redisHandler.get(UserRedisKeys.KYC_PARTY_ID + partyId);
		if (kyc != null) return kyc;
		
		kyc = new Kyc();
		kyc.setPartyId(partyId);
		return kyc;
	}

	@Override
	public void save(Kyc entity) {
		Kyc kyc = get(entity.getPartyId().toString());
		
		// 实名认证是否判断身份证号唯一（true:是，false:否）
		Object obj = this.sysparaService.find("kyc_idnumber_only");
		if (null != obj) {
			if ("true".equals(this.sysparaService.find("kyc_idnumber_only").getValue())) {
				List<Kyc> kycs = findByIdNumber(entity.getIdnumber().trim());
				if (kycs != null) {
					for (Kyc kyc_othde : kycs) {
						if (kyc_othde.getStatus() == 2 && !kyc_othde.getPartyId().equals(entity.getPartyId())) {
							throw new BusinessException("身份证号已实名过!");
						}
					}
				}				
			}
		}

		if (kyc.getId() == null) {
			entity.setApply_time(new Date());
			entity.setId(ApplicationUtil.getCurrentTimeUUID());
			this.insertKyc(entity);
			redisHandler.setSync(UserRedisKeys.KYC_PARTY_ID + entity.getPartyId().toString(), entity);
		} else {
			kyc.setIdnumber(entity.getIdnumber());
			kyc.setStatus(entity.getStatus());
			kyc.setIdname(entity.getIdname());
			kyc.setName(entity.getName());
			kyc.setIdimg_1(entity.getIdimg_1());
			kyc.setIdimg_2(entity.getIdimg_2());
			kyc.setIdimg_3(entity.getIdimg_3());
			kyc.setNationality(entity.getNationality());
			if(entity.getStatus()==1) {
				kyc.setApply_time(new Date());
			}else {
				kyc.setOperation_time(new Date());
			}
			kyc.setMsg(entity.getMsg());
			this.updateKyc(kyc);
			entity.setId(kyc.getId());
			redisHandler.setSync(UserRedisKeys.KYC_PARTY_ID + kyc.getPartyId().toString(), kyc);
		}
	}
    
    @Override
    public void saveNoIdNumOnly(Kyc entity) {
        Kyc kyc = get(entity.getPartyId().toString());
        
        if (kyc.getId() == null) {
            entity.setApply_time(new Date());
            kyc.setId(ApplicationUtil.getCurrentTimeUUID());
			this.insertKyc(entity);
            redisHandler.setSync(UserRedisKeys.KYC_PARTY_ID + entity.getPartyId().toString(), entity);
        } else {
            kyc.setIdnumber(entity.getIdnumber());
            kyc.setStatus(entity.getStatus());
            kyc.setIdname(entity.getIdname());
            kyc.setName(entity.getName());
            kyc.setIdimg_1(entity.getIdimg_1());
            kyc.setIdimg_2(entity.getIdimg_2());
            kyc.setIdimg_3(entity.getIdimg_3());
            kyc.setNationality(entity.getNationality());
            
            if (entity.getStatus() == 1) {
                kyc.setApply_time(new Date());
            } else {
                kyc.setOperation_time(new Date());
            }
            
            kyc.setMsg(entity.getMsg());
            this.updateKyc(kyc);
            entity.setId(kyc.getId());
            redisHandler.setSync(UserRedisKeys.KYC_PARTY_ID + kyc.getPartyId().toString(), kyc);
        }
    }

	/**
	 * 查询是否有多个实名用户
	 * 
	 * @return
	 */
	public List<Kyc> findByIdNumber(String idNumber) {
		List<Kyc> list = ApplicationUtil.executeSelect(Kyc.class,"WHERE IDNUMBER=?",new Object[] {idNumber});
		return list.size()<=0?null:list;
	}

	/**
	 * 验证审核结果
	 */
	public String checkApplyResult(String partyId) throws BusinessException {
		Kyc kyc = get(partyId);
		if (null == kyc.getId())
			return "";
		String msg = "";
		switch (kyc.getStatus()) {
		case 0:
			msg = "已经提交申请，请等待审核";
			break;
		case 1:
			msg = "审核中";
			break;
		case 2:
			msg = "审核已通过";
			break;
		case 3:
			msg = MessageFormat.format("审核未通过,原因:{0}", kyc.getMsg());
			break;
		default:
			msg = "审核状态异常请联系客服";
			break;
		}
		if (kyc.getStatus() != 3) {
			throw new BusinessException(msg);
		} else {
			return msg;
		}
	}

	public boolean isPass(String partyId) {
		Kyc kyc = get(partyId);
		if (null == kyc)
			return Boolean.FALSE;
		return kyc.getStatus() == 2;
	}

	public void delete(String partyId) {
		ApplicationUtil.executeDelete(Kyc.class,"WHERE PARTY_ID=?",new Object[] {partyId});
		redisHandler.remove(UserRedisKeys.KYC_PARTY_ID + partyId);
	}
	
    public void insertKyc(Kyc kyc) {
        ApplicationUtil.executeInsert(kyc);
    }
    
    public void updateKyc(Kyc kyc) {
        ApplicationUtil.executeUpdate(kyc,"WHERE UUID=?", new Object[] {kyc.getId()});
    }

	public void setRedisHandler(RedisHandler redisHandler) {
		this.redisHandler = redisHandler;
	}

	public void setSysparaService(SysparaService sysparaService) {
		this.sysparaService = sysparaService;
	}
}
