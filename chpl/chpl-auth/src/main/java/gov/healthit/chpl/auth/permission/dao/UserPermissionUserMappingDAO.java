package gov.healthit.chpl.auth.permission.dao;

import gov.healthit.chpl.auth.permission.UserPermissionUserMapping;


public interface UserPermissionUserMappingDAO {
	
	public void create(UserPermissionUserMapping permissionMapping);
	
	public void update(UserPermissionUserMapping permissionMapping);
	
	public void deactivate(UserPermissionUserMapping permissionMapping);
	
	public void deactivate(Long userId, Long permissionId);
	
}
