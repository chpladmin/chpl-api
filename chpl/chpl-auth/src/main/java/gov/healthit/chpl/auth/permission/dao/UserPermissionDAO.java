package gov.healthit.chpl.auth.permission.dao;

import java.util.List;

import gov.healthit.chpl.auth.permission.UserPermissionEntity;
import gov.healthit.chpl.auth.permission.UserPermissionRetrievalException;


public interface UserPermissionDAO {
	
	public void create(UserPermissionEntity permission);
	
	public void update(UserPermissionEntity permission);
	
	public void deactivate(String authority);
	
	public void deactivate(Long permissionId);
	
	public UserPermissionEntity getPermissionFromAuthority(String authority) throws UserPermissionRetrievalException;
	
	public List<UserPermissionEntity> findAll();

}
