package gov.healthit.chpl.auth.manager;

import java.util.List;
import java.util.Set;

import com.nulabinc.zxcvbn.Strength;

import gov.healthit.chpl.auth.dto.UserDTO;
import gov.healthit.chpl.auth.dto.UserPermissionDTO;
import gov.healthit.chpl.auth.dto.UserResetTokenDTO;
import gov.healthit.chpl.auth.json.User;
import gov.healthit.chpl.auth.json.UserCreationJSONObject;
import gov.healthit.chpl.auth.json.UserInfoJSONObject;
import gov.healthit.chpl.auth.permission.UserPermissionRetrievalException;
import gov.healthit.chpl.auth.user.UserCreationException;
import gov.healthit.chpl.auth.user.UserManagementException;
import gov.healthit.chpl.auth.user.UserRetrievalException;

public interface UserManager {

    /**
     * Minimum Password strength required for new users and password changes.
     */
    int MIN_PASSWORD_STRENGTH = 3;

    UserDTO create(UserCreationJSONObject userInfo) throws UserCreationException, UserRetrievalException;

    UserDTO update(User userInfo) throws UserRetrievalException;

    UserDTO update(UserDTO user) throws UserRetrievalException ;

    void delete(UserDTO user) throws UserRetrievalException, UserPermissionRetrievalException, UserManagementException;

    void delete(String userName) throws UserRetrievalException, UserPermissionRetrievalException, UserManagementException ;

    List<UserDTO> getAll();

    List<UserDTO> getUsersWithPermission(String permissionName);

    UserDTO getById(Long id) throws UserRetrievalException;

    UserDTO getByName(String userName) throws UserRetrievalException;

    UserInfoJSONObject getUserInfo(String userName) throws UserRetrievalException;

    void grantRole(String userName, String role) throws UserRetrievalException, UserManagementException, UserPermissionRetrievalException;

    void grantAdmin(String userName) throws UserRetrievalException, UserPermissionRetrievalException, UserManagementException;

    void removeRole(UserDTO user, String role) throws UserRetrievalException, UserPermissionRetrievalException, UserManagementException;

    void removeRole(String userName, String role) throws UserRetrievalException, UserPermissionRetrievalException, UserManagementException;

    void removeAdmin(String userName) throws UserPermissionRetrievalException, UserRetrievalException, UserManagementException;

    void updateFailedLoginCount(UserDTO userToUpdate) throws UserRetrievalException;

    void updateUserPassword(String userName, String password) throws UserRetrievalException;

    String getEncodedPassword(UserDTO user) throws UserRetrievalException;

    String encodePassword(String password);

    Set<UserPermissionDTO> getGrantedPermissionsForUser(UserDTO user);

    /**
     * Retrieve password strength object.
     * @param user user's information
     * @param password the password to check
     * @return a Strength object with password strength information
     */
    public Strength getPasswordStrength(UserDTO user, String password);

    public UserResetTokenDTO createResetUserPasswordToken(String username, String email) throws UserRetrievalException;
    
    public boolean authorizePasswordReset(String token);
    
    public void deletePreviousTokens(String token);

    public UserDTO getByNameUnsecured(String userName) throws UserRetrievalException;

    public void updateUserPasswordUnsecured(String userName, String password) throws UserRetrievalException;
}
