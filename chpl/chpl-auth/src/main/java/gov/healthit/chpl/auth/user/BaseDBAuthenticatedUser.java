package gov.healthit.chpl.auth.user;

import gov.healthit.chpl.auth.permission.PermissionMappingManager;

import javax.persistence.Transient;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

@Configurable
public abstract class BaseDBAuthenticatedUser implements User {
	
	private static final long serialVersionUID = 1L;
	
	@Transient
	@Autowired
	private PermissionMappingManager permissionMappingManager;

	public PermissionMappingManager getPermissionMappingManager() {
		return permissionMappingManager;
	}

	public void setPermissionMappingManager(
			PermissionMappingManager permissionMappingManager) {
		this.permissionMappingManager = permissionMappingManager;
	}
	

}
