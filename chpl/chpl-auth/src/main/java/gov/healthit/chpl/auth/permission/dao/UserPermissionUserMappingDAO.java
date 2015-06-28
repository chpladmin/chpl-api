package gov.healthit.chpl.auth.permission.dao;

import java.util.List;
import java.util.Set;

import gov.healthit.chpl.auth.permission.UserPermissionUserMapping;


public interface UserPermissionUserMappingDAO {
	
	public void create(UserPermissionUserMapping permissionMapping);
	
	public void update(UserPermissionUserMapping permissionMapping);
	
	public void deactivate(UserPermissionUserMapping permissionMapping);
	
	public void deactivate(Long userId, Long permissionId);
	
	public List<UserPermissionUserMapping> findPermissionMappingsForUser(Long userId);
	
	public List<UserPermissionUserMapping> findUserMappingsForPermission(Long permissionID);
	
}
