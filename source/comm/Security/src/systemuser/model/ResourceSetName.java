package systemuser.model;

import javax.persistence.Column;
import javax.persistence.Table;

import kernel.bo.EntityObject;
/**
 * 权限资源集
 */
@Table(name="SCT_RESOURCE_SET_NAME")
public class ResourceSetName extends EntityObject {

	private static final long serialVersionUID = 3575411649937943312L;
	
	@Column(name="NAME")
	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
