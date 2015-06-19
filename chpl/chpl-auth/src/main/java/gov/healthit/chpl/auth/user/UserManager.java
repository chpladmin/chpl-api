package gov.healthit.chpl.auth.user;


import gov.healthit.chpl.auth.permission.UserPermissionRetrievalException;

import java.util.List;

import org.springframework.security.acls.model.Permission;
import org.springframework.security.acls.model.Sid;

public interface UserManager {
	
	
	public void addAclPermission(UserImpl user, Sid recipient, Permission permission);
	
	
	public void deleteAclPermission(UserImpl user, Sid recipient, Permission permission);
	
	
	public void deleteRole(UserImpl user, String role) throws UserRetrievalException;
	
	
	public void create(UserImpl user);
	
	
	public void update(UserImpl user) throws UserRetrievalException;
	
	
	public void delete(UserImpl user);
	
	
	public List<UserImpl> getAll();
	
	
	public User getById(Long id) throws UserRetrievalException;
	
	
	public User getByUserName(String uname) throws UserRetrievalException;


	void grantRole(UserImpl user, String role) throws UserRetrievalException, UserManagementException, UserPermissionRetrievalException;


	void grantAdmin(UserImpl user) throws UserRetrievalException, UserPermissionRetrievalException;
	
	
}
