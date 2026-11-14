package security;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.Column;
import javax.persistence.Table;

import kernel.bo.EntityObject;

/**
 * 资源
 */
@Table(name="SCT_RESOURCE")
public class Resource extends EntityObject {

    private static final long serialVersionUID = 1L;
    
    public static final String RESOURCE_TYPE_URL = "URL";
    
    public static final String RESOURCE_TYPE_OPERATION = "OPERATION";

    @Column(name="RES_STRING")
    private String resString;// 资源串

    @Column(name="RES_TYPE")
    private String resType;// 资源类型

    private Set<Role> roles = new HashSet<Role>(0);// 资源关联的角色

    /**
     * 获取资源对应的色名
     */
    public String getRoleAuthorities() {
    	return roles.stream().map(role->"ROLE_"+role.getRoleName())
    	.collect(Collectors.collectingAndThen(Collectors.toSet(), set->String.join(",",set)));
    }

    public String getResString() {
        return this.resString;
    }

    public void setResString(String resString) {
        this.resString = resString;
    }

    public String getResType() {
        return this.resType;
    }

    public void setResType(String resType) {
        this.resType = resType;
    }

	public Set<Role> getRoles() {
		return roles;
	}

	public void setRoles(Set<Role> roles) {
		this.roles = roles;
	}

}