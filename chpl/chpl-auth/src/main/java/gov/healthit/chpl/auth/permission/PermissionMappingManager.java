package gov.healthit.chpl.auth.permission;

import java.util.Set;

import gov.healthit.chpl.auth.user.UserEntity;

public interface PermissionMappingManager {
	
	public void create(UserPermissionUserMapping userPermissionMapping);
	
	public void update(UserPermissionUserMapping userPermissionMapping);
	
	public void delete(UserPermissionUserMapping userPermissionMapping);
	
	public void grant(UserEntity user, UserPermissionEntity permission);
	
	public void grant(UserEntity user, String authority) throws UserPermissionRetrievalException;
	
	public void revoke(UserEntity user, UserPermissionEntity permission);
	
	public void revoke(UserEntity user, String authority) throws UserPermissionRetrievalException;
	
	public Set<UserPermission> getPermissions(UserEntity user);
	
	public Set<UserPermissionEntity> getPermissionEntities(UserEntity user);
	
}
