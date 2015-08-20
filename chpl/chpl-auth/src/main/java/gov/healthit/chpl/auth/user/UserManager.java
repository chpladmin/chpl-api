package gov.healthit.chpl.auth.user;


import gov.healthit.chpl.auth.json.UserCreationJSONObject;
import gov.healthit.chpl.auth.json.UserInfoJSONObject;
import gov.healthit.chpl.auth.permission.UserPermissionDTO;
import gov.healthit.chpl.auth.permission.UserPermissionRetrievalException;

import java.util.List;
import java.util.Set;

public interface UserManager {
			
	
	public void create(UserCreationJSONObject userInfo) throws UserCreationException, UserRetrievalException;
	
	
	public void update(UserInfoJSONObject userInfo) throws UserRetrievalException;
	
	
	public void delete(UserDTO user);
	
	
	public void delete(String userName) throws UserRetrievalException;
	
	
	public List<UserDTO> getAll();
	
	
	public UserDTO getById(Long id) throws UserRetrievalException;
	
	
	public UserDTO getByName(String userName) throws UserRetrievalException;
	
	
	public UserInfoJSONObject getUserInfo(String userName) throws UserRetrievalException;
	

	public void grantRole(String userName, String role) throws UserRetrievalException, UserManagementException, UserPermissionRetrievalException;


	public void grantAdmin(String userName) throws UserRetrievalException, UserPermissionRetrievalException, UserManagementException;

	
	public void removeRole(String userName, String role) throws UserRetrievalException, UserPermissionRetrievalException;
	
	
	public void removeRole(UserDTO user, String role) throws UserRetrievalException, UserPermissionRetrievalException;
	
	
	public void updateUserPassword(String userName, String password) throws UserRetrievalException;
	
	
	public String getEncodedPassword(UserDTO user) throws UserRetrievalException;


	public String encodePassword(String password);


	public Set<UserPermissionDTO> getGrantedPermissionsForUser(UserDTO user);
	
	
}
