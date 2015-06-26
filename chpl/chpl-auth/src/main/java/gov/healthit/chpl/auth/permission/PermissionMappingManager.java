package gov.healthit.chpl.auth.permission;

import gov.healthit.chpl.auth.user.UserImpl;

public interface PermissionMappingManager {
	
	public void create(UserPermissionUserMapping userPermissionMapping);
	
	public void update(UserPermissionUserMapping userPermissionMapping);
	
	public void delete(UserPermissionUserMapping userPermissionMapping);
	
	public void grant(UserImpl user, UserPermission permission);
	
	public void revoke(UserImpl user, UserPermission permission);
}
