package gov.healthit.chpl.auth.user;


import gov.healthit.chpl.auth.permission.UserPermissionRetrievalException;

import java.util.List;

import org.springframework.security.acls.model.Permission;
import org.springframework.security.acls.model.Sid;

public interface UserManager {
	
	
	public void addAclPermission(UserEntity user, Sid recipient, Permission permission);
	
	
	public void deleteAclPermission(UserEntity user, Sid recipient, Permission permission);
	
	
	public void deleteRole(UserEntity user, String role) throws UserRetrievalException;
		
	
	public void create(UserDTO userInfo) throws UserCreationException;
	
	
	public void update(UserDTO userInfo) throws UserRetrievalException;
	
	
	public void update(UserEntity user) throws UserRetrievalException;
	
	
	public void delete(UserEntity user);
	
	
	public void delete(String userName) throws UserRetrievalException;
	
	
	public List<UserEntity> getAll();
	
	
	public User getById(Long id) throws UserRetrievalException;
	
	
	public User getByUserName(String uname) throws UserRetrievalException;


	public void grantRole(String userName, String role) throws UserRetrievalException, UserManagementException, UserPermissionRetrievalException;


	public void grantAdmin(UserEntity user) throws UserRetrievalException, UserPermissionRetrievalException, UserManagementException;

	
	public void updateUserPassword(String userName, String password) throws UserRetrievalException;
	
}
