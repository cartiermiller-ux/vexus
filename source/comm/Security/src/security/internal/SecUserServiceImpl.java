package security.internal;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.providers.encoding.PasswordEncoder;

import com.google.common.collect.Sets;

import kernel.bo.RecordObjectMapper;
import kernel.exception.BusinessException;
import kernel.util.StringUtils;
import kernel.web.ApplicationUtil;
import security.Resource;
import security.Role;
import security.SecUser;

/**
 * @author JORGE
 * @description SecUser实体持久层操作
 */
public class SecUserServiceImpl implements SecUserService {
	/**
	 * 密码工具
	 */
	private PasswordEncoder passwordEncoder;
	
	public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
		this.passwordEncoder = passwordEncoder;
	}
	
	public void saveUser(SecUser user) {
		if(null==user) return;
		JdbcTemplate jdbcTemplate=ApplicationUtil.getBean(JdbcTemplate.class);
		
		List<String> uuids=jdbcTemplate.queryForList("SELECT UUID FROM SCT_USER WHERE USERNAME=?", String.class, user.getUsername());
		if (ObjectUtils.isNotEmpty(uuids)) throw new BusinessException("系统存在相同[系统登录名]！");
		
		user.setCreateTime(new Date());
		if(null==user.getId()) user.setId(ApplicationUtil.getCurrentTimeUUID());
		user.setPassword(passwordEncoder.encodePassword(user.getPassword(), user.getUsername()));
		
		ApplicationUtil.executeInsert(user);
		cascadeRole(user);
	}
	
	/**
	 * 根据用户名删除用户
	 * 删除用户后,此删除的用户不再关联角色
	 * @param user 需要删除的用户
	 */
	public void deleteUser(SecUser user) {
		if(null==user) return;
		JdbcTemplate jdbcTemplate=ApplicationUtil.getBean(JdbcTemplate.class);
		
		String uuid=null;
		List<String> uuids=jdbcTemplate.queryForList("SELECT UUID FROM SCT_USER WHERE USERNAME=?", String.class, user.getUsername());
		if (ObjectUtils.isEmpty(uuids) || null==(uuid=uuids.get(0))) throw new BusinessException("系统用户登录名不存在！");
		
		jdbcTemplate.update("DELETE FROM SCT_USER WHERE UUID=?",uuid);
		jdbcTemplate.update("DELETE FROM SCT_USER_ROLE WHERE USER_UUID=?",uuid);
	}
	
	/**
	 * 根据用户名查找用户
	 * @param userName 用户名
	 * @return 用户信息(包含关联角色与权限)
	 */
	public SecUser findUserByLoginName(String userName) {
		if(ObjectUtils.isEmpty(userName)) return null;
		JdbcTemplate jdbcTemplate=ApplicationUtil.getBean(JdbcTemplate.class);
		
		String userSql="SELECT * FROM SCT_USER WHERE USERNAME=?";
		List<SecUser> users=jdbcTemplate.query(userSql, RecordObjectMapper.newInstance(SecUser.class), userName);
		
		SecUser user=null;
		if(null==users || users.isEmpty() || null==(user=users.get(0))) return null;
		
		return bindRoleResource(user);
	}
	
	/**
	 * 根据邮箱查找用户
	 * @param userName 用户名
	 * @return 用户信息(包含关联角色与权限)
	 */
	public SecUser findUserByLoginName2(String emailPhone) {
		String userName = emailPhone;
		if(ObjectUtils.isEmpty(emailPhone)) return null;
		String sql = "";
		if(isValidPhone(emailPhone)) {
			sql ="SELECT * FROM PAT_PARTY WHERE PHONE=?"; 
		}else if(isValidEmail(emailPhone)) {
			sql="SELECT * FROM PAT_PARTY WHERE EMAIL=?";
		}	
		JdbcTemplate jdbcTemplate=ApplicationUtil.getBean(JdbcTemplate.class);
		if(ObjectUtils.isNotEmpty(sql)) {			
			List<SecUser> userList=jdbcTemplate.query(sql, RecordObjectMapper.newInstance(SecUser.class), emailPhone);
			if(org.springframework.util.ObjectUtils.isEmpty(userList)) {
				return null;
			}
			userName = userList.get(0).getUsername();
		}
		String userSql="SELECT * FROM SCT_USER WHERE USERNAME=?";
		List<SecUser> users=jdbcTemplate.query(userSql, RecordObjectMapper.newInstance(SecUser.class), userName);
		
		SecUser user=null;
		if(null==users || users.isEmpty() || null==(user=users.get(0))) return null;
		
		return bindRoleResource(user);
	}
	
	@Override
	public SecUser findValidUserByLoginName(String loginName, String[] rolesArrty) {
		if(ObjectUtils.isEmpty(loginName) || ObjectUtils.isEmpty(rolesArrty)) return null;
		
		SecUser user = findUserByLoginName(loginName);
		if (null == user) return null;
		
		Set<Role> userRoleSet=user.getRoles();
		if(null==userRoleSet || userRoleSet.isEmpty()) return null;
		
		Set<String> allRoleSet=Sets.newHashSet(rolesArrty);
		allRoleSet.retainAll(userRoleSet.stream().map(role->role.getRoleName()).collect(Collectors.toSet()));
		
		return allRoleSet.isEmpty()?null:user;
	}

	@Override
	public void updatePassword(String username, String oldpassword, String newPassword) {
		if(ObjectUtils.isEmpty(username) || ObjectUtils.isEmpty(oldpassword) || ObjectUtils.isEmpty(newPassword)) return;
		JdbcTemplate jdbcTemplate=ApplicationUtil.getBean(JdbcTemplate.class);
		
		List<SecUser> users=jdbcTemplate.query("SELECT * FROM SCT_USER WHERE USERNAME=?",RecordObjectMapper.newInstance(SecUser.class),username);
		if(null==users || users.isEmpty()) return;
		SecUser user=users.get(0);
		if(null==user) return;
		
		if (passwordEncoder.encodePassword(oldpassword,username).equals(user.getPassword())) {
			jdbcTemplate.update("UPDATE SCT_USER SET PASSWORD=? WHERE UUID=?",passwordEncoder.encodePassword(newPassword,username),user.getId());
		} else {
			throw new BusinessException("旧密码不正确");
		}
	}
	
	@Override
	public void updatePassword(String username, String password) {
		if(ObjectUtils.isEmpty(username) || ObjectUtils.isEmpty(password)) return;
		JdbcTemplate jdbcTemplate=ApplicationUtil.getBean(JdbcTemplate.class);
		
		List<SecUser> users=jdbcTemplate.query("SELECT * FROM SCT_USER WHERE USERNAME=?",RecordObjectMapper.newInstance(SecUser.class),username);
		if(null==users || users.isEmpty()) throw new BusinessException("没有找到用户");
		
		SecUser user=users.get(0);
		if(null==user) throw new BusinessException("没有找到用户");
		
		jdbcTemplate.update("UPDATE SCT_USER SET PASSWORD=? WHERE UUID=?",passwordEncoder.encodePassword(password,username),user.getId());
	}
	
	@Override
	public void updateSafeword(String username, String oldpassword, String newPassword) {
		if(ObjectUtils.isEmpty(username) || ObjectUtils.isEmpty(oldpassword) || ObjectUtils.isEmpty(newPassword)) return;
		JdbcTemplate jdbcTemplate=ApplicationUtil.getBean(JdbcTemplate.class);
		
		List<SecUser> users=jdbcTemplate.query("SELECT * FROM SCT_USER WHERE USERNAME=?",RecordObjectMapper.newInstance(SecUser.class),username);
		if(null==users || users.isEmpty()) return;
		SecUser user=users.get(0);
		if(null==user) return;
		
		String oldpassword_encoder = passwordEncoder.encodePassword(oldpassword, username);
		if(!StringUtils.isNullOrEmpty(user.getSafeword())) {
			if (oldpassword_encoder.equals(user.getSafeword())) {
				jdbcTemplate.update("UPDATE SCT_USER SET SAFEWORD=? WHERE UUID=?",passwordEncoder.encodePassword(newPassword,username),user.getId());
			} else {
				throw new BusinessException("旧密码不正确");
			}
		}else {
			jdbcTemplate.update("UPDATE SCT_USER SET SAFEWORD=? WHERE UUID=?",passwordEncoder.encodePassword(newPassword,username),user.getId());
		}
	}

	@Override
	public void updateSafeword(String username, String newPassword) {
		if(ObjectUtils.isEmpty(username) || ObjectUtils.isEmpty(newPassword)) return;
		
		JdbcTemplate jdbcTemplate=ApplicationUtil.getBean(JdbcTemplate.class);
		
		List<SecUser> users=jdbcTemplate.query("SELECT * FROM SCT_USER WHERE USERNAME=?",RecordObjectMapper.newInstance(SecUser.class),username);
		if(null==users || users.isEmpty()) throw new BusinessException("没有找到用户");
		SecUser user=users.get(0);
		if(null==user) throw new BusinessException("没有找到用户");
		
		jdbcTemplate.update("UPDATE SCT_USER SET SAFEWORD=? WHERE UUID=?",passwordEncoder.encodePassword(newPassword,username),user.getId());
	}
	
	@Override
	public SecUser findUserByPartyId(Serializable partyId) {
		if(null==partyId) return null;
		
		JdbcTemplate jdbcTemplate=ApplicationUtil.getBean(JdbcTemplate.class);
		List<SecUser> users=jdbcTemplate.query("SELECT * FROM SCT_USER WHERE PARTY_UUID=?",RecordObjectMapper.newInstance(SecUser.class),partyId);
		
		SecUser user=null;
		if(null==users || users.isEmpty() || null==(user=users.get(0))) return null;
		
		return bindRoleResource(user);
	}

	/**
	 * 查找所有系统用户
	 */
	public List<SecUser> findAllSysUsers() {
		JdbcTemplate jdbcTemplate=ApplicationUtil.getBean(JdbcTemplate.class);
		List<SecUser> users=jdbcTemplate.query("SELECT * FROM SCT_USER WHERE PARTY_UUID is null or PARTY_UUID=''",RecordObjectMapper.newInstance(SecUser.class));
		if(null!=users) for(int i=0;i<users.size();bindRoleResource(users.get(i++)));
		return users;
	}
	
	@Override
	public void update(SecUser user) {
		if(null==user) return;
		
		JdbcTemplate jdbcTemplate=ApplicationUtil.getBean(JdbcTemplate.class);
		List<SecUser> users=jdbcTemplate.query("SELECT * FROM SCT_USER WHERE UUID=? OR USERNAME=?",RecordObjectMapper.newInstance(SecUser.class),user.getId(),user.getUsername());
		
		SecUser dbUser=null;
		if(null==users || users.isEmpty() || null==(dbUser=users.get(0))) { //execute insert
			Object[] jdbcParams=ApplicationUtil.getInsertStatement(user);
			String insertUserSql=(String)jdbcParams[0];
			Object[] sqlParameters=(Object[])jdbcParams[1];
			jdbcTemplate.update(insertUserSql,sqlParameters);
		}else { //execute update
			if(null==user.getId()) user.setId(dbUser.getId());
			if(null==user.getUsername()) user.setUsername(dbUser.getUsername());
			if(null==user.getId()) user.setId(ApplicationUtil.getCurrentTimeUUID());
			
			Object[] jdbcParams=ApplicationUtil.getUpdateStatement(user,"WHERE UUID=? OR USERNAME=?",new Object[] {user.getId(),user.getUsername()});
			String updateUserSql=(String)jdbcParams[0];
			Object[] sqlParameters=(Object[])jdbcParams[1];
			jdbcTemplate.update(updateUserSql,sqlParameters);
		}
		
		cascadeRole(user);
	}

	@Override
	public SecUser findUserById(Serializable id) {
		if(null==id) return null;
		
		JdbcTemplate jdbcTemplate=ApplicationUtil.getBean(JdbcTemplate.class);
		List<SecUser> users=jdbcTemplate.query("SELECT * FROM SCT_USER WHERE UUID=?",RecordObjectMapper.newInstance(SecUser.class),id);
		
		SecUser user=null;
		if(null==users || users.isEmpty() || null==(user=users.get(0))) return null;
		
		return bindRoleResource(user);
	}
	
	/**
	 * 级联角色资源
	 * @param secuser 用户对象
	 */
	private static final void cascadeRole(SecUser secuser) {
		if(null==secuser) return;
		
		JdbcTemplate jdbcTemplate=ApplicationUtil.getBean(JdbcTemplate.class);
		Serializable userId=secuser.getId();
		Set<Role> roles=secuser.getRoles();
		
		jdbcTemplate.update("DELETE FROM SCT_USER_ROLE WHERE USER_UUID=?",userId);
		if(ObjectUtils.isEmpty(roles)) return;
		
		List<Object[]> argList=roles.stream().map(role->new Object[] {userId,role.getId()}).collect(Collectors.toList());
		jdbcTemplate.batchUpdate("INSERT INTO SCT_USER_ROLE(USER_UUID,ROLE_UUID) VALUES(?,?)", argList);
	}
	
	/**
	 * 绑定角色资源
	 * @param secuser 用户对象
	 */
	public static final SecUser bindRoleResource(SecUser secuser) {
		if(null==secuser) return secuser;
		JdbcTemplate jdbcTemplate=ApplicationUtil.getBean(JdbcTemplate.class);
		
		String roleSql="SELECT * FROM SCT_ROLE SR WHERE EXISTS (SELECT ROLE_UUID FROM SCT_USER_ROLE WHERE USER_UUID=? AND ROLE_UUID=SR.UUID)";
		List<Role> roleList=jdbcTemplate.query(roleSql, RecordObjectMapper.newInstance(Role.class), secuser.getId());
		
		if(null==roleList || roleList.isEmpty()) return secuser;
		Set<Role> roleSet=roleList.stream().filter(role->null!=role).collect(Collectors.toSet());
		secuser.setRoles(roleSet);
		
		String resoSql="SELECT * FROM SCT_RESOURCE SR WHERE EXISTS (SELECT RESOURCE_UUID FROM SCT_ROLE_RESOURCE WHERE ROLE_UUID=? AND RESOURCE_UUID=SR.UUID)";
		for(Role role:roleSet) {
			List<Resource> resoList=jdbcTemplate.query(resoSql, RecordObjectMapper.newInstance(Resource.class), role.getId());
			if(null==resoList || resoList.isEmpty()) continue;
			role.setResources(resoList.stream().filter(reso->null!=reso).collect(Collectors.toSet()));
		}
		
		return secuser;
	}
	
	// 手机号校验
	private boolean isValidPhone(String username) {
        Pattern p = Pattern.compile("[0-9]*");
        return p.matcher(username).matches();
	}
	
	// 邮箱校验
	private boolean isValidEmail(String username) {
        String regexPattern = "^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@"
                + "[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$";
        return Pattern.compile(regexPattern)
                .matcher(username)
                .matches();
	}
}
