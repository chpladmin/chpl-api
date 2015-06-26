package gov.healthit.chpl.auth.user;


import gov.healthit.chpl.auth.permission.UserPermissionRetrievalException;

import java.util.List;

import org.springframework.security.acls.model.Permission;
import org.springframework.security.acls.model.Sid;

public interface UserManager {
	
	
	public void addAclPermission(UserEntity user, Sid recipient, Permission permission);
	
	
	public void deleteAclPermission(UserEntity user, Sid recipient, Permission permission);
	
	
	public void deleteRole(UserEntity user, String role) throws UserRetrievalException;
	
	
	public void create(UserEntity user);
	
	
	public void update(UserEntity user) throws UserRetrievalException;
	
	
	public void delete(UserEntity user);
	
	
	public List<UserEntity> getAll();
	
	
	public User getById(Long id) throws UserRetrievalException;
	
	
	public User getByUserName(String uname) throws UserRetrievalException;


	void grantRole(UserEntity user, String role) throws UserRetrievalException, UserManagementException, UserPermissionRetrievalException;


	void grantAdmin(UserEntity user) throws UserRetrievalException, UserPermissionRetrievalException;
	
	
}
