package gov.healthit.chpl.auth.permission;

import java.util.Set;

import gov.healthit.chpl.auth.user.UserImpl;

public interface PermissionMappingManager {
	
	public void create(UserPermissionUserMapping userPermissionMapping);
	
	public void update(UserPermissionUserMapping userPermissionMapping);
	
	public void delete(UserPermissionUserMapping userPermissionMapping);
	
	public void grant(UserImpl user, UserPermission permission);
	
	public void grant(UserImpl user, String authority);
	
	public void revoke(UserImpl user, UserPermission permission);
	
	public void revoke(UserImpl user, String authority);
	
	public Set<UserPermission> getPermissions(UserImpl user);
}
