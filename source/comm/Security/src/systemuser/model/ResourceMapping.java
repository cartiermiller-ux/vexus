package systemuser.model;


import javax.persistence.Column;
import javax.persistence.Table;

import kernel.bo.EntityObject;

/**
 * 权限资源映射
 */
@Table(name="SCT_RESOURCE_MAPPING")
public class ResourceMapping extends EntityObject {

	private static final long serialVersionUID = 3575411649937943312L;
	
	/**
	 * 关联ResourceSetName表UUID
	 */
	@Column(name="SET_UUID")
	private String set_id;

	/**
	 * 关联Resource表UUID
	 */
	@Column(name="RESOURCE_UUID")
	private String resource_id;

	public String getSet_id() {
		return set_id;
	}

	public void setSet_id(String set_id) {
		this.set_id = set_id;
	}

	public String getResource_id() {
		return resource_id;
	}

	public void setResource_id(String resource_id) {
		this.resource_id = resource_id;
	}
}
