package gov.healthit.chpl.auth.manager;


import gov.healthit.chpl.auth.dto.UserDTO;
import gov.healthit.chpl.auth.dto.UserPermissionDTO;
import gov.healthit.chpl.auth.json.User;
import gov.healthit.chpl.auth.json.UserCreationJSONObject;
import gov.healthit.chpl.auth.json.UserInfoJSONObject;
import gov.healthit.chpl.auth.permission.UserPermissionRetrievalException;
import gov.healthit.chpl.auth.user.UserCreationException;
import gov.healthit.chpl.auth.user.UserManagementException;
import gov.healthit.chpl.auth.user.UserRetrievalException;

import java.util.List;
import java.util.Set;

public interface UserManager {
			
	
	public UserDTO create(UserCreationJSONObject userInfo) throws UserCreationException, UserRetrievalException;
	
	
	public UserDTO update(User userInfo) throws UserRetrievalException;
	public UserDTO update(UserDTO user) throws UserRetrievalException ;
	
	public void delete(UserDTO user) throws UserRetrievalException, UserPermissionRetrievalException, UserManagementException;
	
	
	public void delete(String userName) throws UserRetrievalException, UserPermissionRetrievalException, UserManagementException ;
	
	
	public List<UserDTO> getAll();
	
	
	public UserDTO getById(Long id) throws UserRetrievalException;
	
	
	public UserDTO getByName(String userName) throws UserRetrievalException;
	
	
	public UserInfoJSONObject getUserInfo(String userName) throws UserRetrievalException;


	public void grantRole(String userName, String role) throws UserRetrievalException, UserManagementException, UserPermissionRetrievalException;


	public void grantAdmin(String userName) throws UserRetrievalException, UserPermissionRetrievalException, UserManagementException;

	
	public void removeRole(UserDTO user, String role) throws UserRetrievalException, UserPermissionRetrievalException, UserManagementException;
	public void removeRole(String userName, String role) throws UserRetrievalException, UserPermissionRetrievalException, UserManagementException;
	public void removeAdmin(String userName) throws UserPermissionRetrievalException, UserRetrievalException, UserManagementException;
	
	public void updateFailedLoginCount(UserDTO userToUpdate) throws UserRetrievalException;
	public void updateUserPassword(String userName, String password) throws UserRetrievalException;
	public String resetUserPassword(String username, String email) throws UserRetrievalException;
	
	public String getEncodedPassword(UserDTO user) throws UserRetrievalException;


	public String encodePassword(String password);


	public Set<UserPermissionDTO> getGrantedPermissionsForUser(UserDTO user);
	
	
}
