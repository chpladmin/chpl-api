package gov.healthit.chpl.auth.permission;

import org.springframework.beans.factory.annotation.Autowired;

import gov.healthit.chpl.auth.permission.dao.UserPermissionUserMappingDAO;
import gov.healthit.chpl.auth.user.UserImpl;

public class PermissionMappingManagerImpl {
	
	@Autowired
	UserPermissionUserMappingDAO userPermissionUserMappingDAO;
	
	
	public void create(UserPermissionUserMapping userPermissionMapping){
		userPermissionUserMappingDAO.create(userPermissionMapping);
	}
	
	public void update(UserPermissionUserMapping userPermissionMapping){
		userPermissionUserMappingDAO.update(userPermissionMapping);
	}
	
	public void delete(UserPermissionUserMapping userPermissionMapping){
		userPermissionUserMappingDAO.deactivate(userPermissionMapping);
	}
	
	public void grant(UserImpl user, UserPermission permission){
		
		UserPermissionUserMapping permissionMapping = new UserPermissionUserMapping();
		permissionMapping.setPermission(permission);
		permissionMapping.setUser(user);
		userPermissionUserMappingDAO.create(permissionMapping);
		
	}
	
	public void revoke(UserImpl user, UserPermission permission){
		
		userPermissionUserMappingDAO.deactivate(user.getId(), permission.getId());
		
	}
	
}
