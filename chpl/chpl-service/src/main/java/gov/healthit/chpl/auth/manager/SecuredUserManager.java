package gov.healthit.chpl.auth.manager;

import java.util.List;
import java.util.Set;

import gov.healthit.chpl.auth.dto.UserDTO;
import gov.healthit.chpl.auth.dto.UserPermissionDTO;
import gov.healthit.chpl.auth.entity.UserEntity;
import gov.healthit.chpl.auth.permission.UserPermissionRetrievalException;
import gov.healthit.chpl.auth.user.UserCreationException;
import gov.healthit.chpl.auth.user.UserManagementException;
import gov.healthit.chpl.auth.user.UserRetrievalException;

public interface SecuredUserManager {

    public UserDTO create(UserDTO user, String encodedPassword) throws UserCreationException, UserRetrievalException;

    public UserDTO update(UserDTO user) throws UserRetrievalException;

    public void updateContactInfo(UserEntity user);

    public void delete(UserDTO user)
            throws UserRetrievalException, UserPermissionRetrievalException, UserManagementException;

    public List<UserDTO> getAll();

    public UserDTO getById(Long id) throws UserRetrievalException;

    public List<UserDTO> getUsersWithPermission(String permissionName);

    // Not used publicly
    // public void addAclPermission(UserDTO user, Sid recipient, Permission
    // permission);

    // Not used publicly
    // public void deleteAclPermission(UserDTO user, Sid recipient, Permission
    // permission);

    public void grantRole(String userName, String role)
            throws UserRetrievalException, UserManagementException, UserPermissionRetrievalException;

    public void grantAdmin(String userName)
            throws UserPermissionRetrievalException, UserRetrievalException, UserManagementException;

    public void removeRole(UserDTO user, String role)
            throws UserRetrievalException, UserPermissionRetrievalException, UserManagementException;

    public void removeRole(String userName, String role)
            throws UserRetrievalException, UserPermissionRetrievalException, UserManagementException;

    public void removeAdmin(String userName)
            throws UserPermissionRetrievalException, UserRetrievalException, UserManagementException;

    public void updatePassword(UserDTO user, String encodedPassword) throws UserRetrievalException;

    public void updateFailedLoginCount(UserDTO user) throws UserRetrievalException;

    public void updateAccountLockedStatus(UserDTO user) throws UserRetrievalException;

    public Set<UserPermissionDTO> getGrantedPermissionsForUser(UserDTO user);

    public UserDTO getBySubjectName(String userName) throws UserRetrievalException;

}
