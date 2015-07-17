package gov.healthit.chpl.auth.user;


import gov.healthit.chpl.auth.permission.UserPermissionRetrievalException;

import java.util.List;

import org.springframework.security.acls.model.Permission;
import org.springframework.security.acls.model.Sid;

public interface UserManager {
	
	
	public void addAclPermission(User user, Sid recipient, Permission permission);
	
	
	public void deleteAclPermission(User user, Sid recipient, Permission permission);
		
	
	public void create(UserCreationDTO userInfo) throws UserCreationException, UserRetrievalException;
	
	
	public void update(UserDTO userInfo) throws UserRetrievalException;
	
	
	public void delete(User user);
	
	
	public void delete(String userName) throws UserRetrievalException;
	
	
	public List<UserDTO> getAll();
	
	
	public UserDTO getById(Long id) throws UserRetrievalException;
	
	
	public UserDTO getByName(String userName);
	

	public void grantRole(String userName, String role) throws UserRetrievalException, UserManagementException, UserPermissionRetrievalException;


	public void grantAdmin(String userName) throws UserRetrievalException, UserPermissionRetrievalException, UserManagementException;

	
	public void deleteRole(User user, String role) throws UserRetrievalException;
	
	
	public void updateUserPassword(String userName, String password) throws UserRetrievalException;
	
	
	public void getPassword(UserDTO user) throws UserRetrievalException;
	
}
