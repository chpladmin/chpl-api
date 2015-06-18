package gov.healthit.chpl.auth.permission.dao;

import java.util.List;

import gov.healthit.chpl.auth.permission.UserPermission;
import gov.healthit.chpl.auth.permission.UserPermissionRetrievalException;


public interface UserPermissionDAO {
	
	public void create(UserPermission permission);
	
	public void update(UserPermission permission);
	
	public void deactivate(String authority);
	
	public void deactivate(Long permissionId);
	
	public UserPermission getPermissionFromAuthority(String authority) throws UserPermissionRetrievalException;
	
	public List<UserPermission> findAll();

}
