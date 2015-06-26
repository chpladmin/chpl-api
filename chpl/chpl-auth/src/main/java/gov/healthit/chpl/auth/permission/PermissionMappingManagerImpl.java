package gov.healthit.chpl.auth.permission;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;

import gov.healthit.chpl.auth.permission.dao.UserPermissionDAO;
import gov.healthit.chpl.auth.permission.dao.UserPermissionUserMappingDAO;
import gov.healthit.chpl.auth.user.UserEntity;

public class PermissionMappingManagerImpl implements PermissionMappingManager {
	
	@Autowired
	UserPermissionUserMappingDAO userPermissionUserMappingDAO;
	
	@Autowired
	UserPermissionDAO userPermissionDAO;
	
	public void create(UserPermissionUserMapping userPermissionMapping){
		userPermissionUserMappingDAO.create(userPermissionMapping);
	}
	
	public void update(UserPermissionUserMapping userPermissionMapping){
		userPermissionUserMappingDAO.update(userPermissionMapping);
	}
	
	public void delete(UserPermissionUserMapping userPermissionMapping){
		userPermissionUserMappingDAO.deactivate(userPermissionMapping);
	}
	
	public void grant(UserEntity user, UserPermissionEntity permission){
		
		UserPermissionUserMapping permissionMapping = new UserPermissionUserMapping();
		permissionMapping.setPermission(permission);
		permissionMapping.setUser(user);
		userPermissionUserMappingDAO.create(permissionMapping);
		
	}
	
	@Override
	public void grant(UserEntity user, String authority) throws UserPermissionRetrievalException {
		
		UserPermissionEntity permission = userPermissionDAO.getPermissionFromAuthority(authority);
		if (permission == null){
			throw new UserPermissionRetrievalException("The permission you are trying to grant does not exist");
		}
		
		UserPermissionUserMapping permissionMapping = new UserPermissionUserMapping();
		permissionMapping.setPermission(permission);
		permissionMapping.setUser(user);
		userPermissionUserMappingDAO.create(permissionMapping);
		
	}
	
	public void revoke(UserEntity user, UserPermissionEntity permission){
		
		userPermissionUserMappingDAO.deactivate(user.getId(), permission.getId());
		
	}


	@Override
	public void revoke(UserEntity user, String authority) throws UserPermissionRetrievalException {
		
		UserPermissionEntity permission = userPermissionDAO.getPermissionFromAuthority(authority);
		if (permission == null){
			throw new UserPermissionRetrievalException("The permission you are trying to grant does not exist");
		}
		
		userPermissionUserMappingDAO.deactivate(user.getId(), permission.getId());
	}

	@Override
	public Set<UserPermission> getPermissions(UserEntity user) {
		
		//userPermissionMappingDAO
		
	}

	@Override
	public Set<UserPermissionEntity> getPermissionEntities(UserEntity user) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
