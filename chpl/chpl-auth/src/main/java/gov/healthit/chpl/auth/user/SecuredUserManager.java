package gov.healthit.chpl.auth.user;

import gov.healthit.chpl.auth.permission.UserPermissionDTO;
import gov.healthit.chpl.auth.permission.UserPermissionRetrievalException;

import java.util.List;
import java.util.Set;

import org.springframework.security.acls.model.Permission;
import org.springframework.security.acls.model.Sid;


public interface SecuredUserManager {

	
	public void create(UserDTO user, String encodedPassword) throws UserCreationException, UserRetrievalException;
	
	public void update(UserDTO user) throws UserRetrievalException;
	
	public void updateContactInfo(UserEntity user);
	
	public void delete(UserDTO user);
	
	public List<UserDTO> getAll();
	
	public UserDTO getById(Long id) throws UserRetrievalException;

	public void addAclPermission(UserDTO user, Sid recipient, Permission permission);
	
	public void deleteAclPermission(UserDTO user, Sid recipient, Permission permission);
	
	public void grantRole(String userName, String role) throws UserRetrievalException, UserManagementException, UserPermissionRetrievalException;

	public void grantAdmin(String userName) throws UserPermissionRetrievalException, UserRetrievalException, UserManagementException;
	
	public void removeRole(UserDTO user, String role) throws UserRetrievalException, UserPermissionRetrievalException;
	
	public void removeRole(String userName, String role) throws UserRetrievalException, UserPermissionRetrievalException;
	
	public void updatePassword(UserDTO user, String encodedPassword) throws UserRetrievalException;
	
	public Set<UserPermissionDTO> getGrantedPermissionsForUser(UserDTO user);

	public UserDTO getByName(String userName) throws UserRetrievalException;
	
	
}
