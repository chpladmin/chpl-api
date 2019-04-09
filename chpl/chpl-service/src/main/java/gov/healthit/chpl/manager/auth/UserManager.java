package gov.healthit.chpl.manager.auth;

import java.util.List;

import com.nulabinc.zxcvbn.Strength;

import gov.healthit.chpl.domain.auth.User;
import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.dto.auth.UserResetTokenDTO;
import gov.healthit.chpl.exception.UserCreationException;
import gov.healthit.chpl.exception.UserManagementException;
import gov.healthit.chpl.exception.UserPermissionRetrievalException;
import gov.healthit.chpl.exception.UserRetrievalException;

public interface UserManager {

    /**
     * Minimum Password strength required for new users and password changes.
     */
    int MIN_PASSWORD_STRENGTH = 3;

    UserDTO create(UserDTO userInfo) throws UserCreationException, UserRetrievalException;

    UserDTO update(UserDTO user) throws UserRetrievalException;

    void delete(UserDTO user) throws UserRetrievalException, UserPermissionRetrievalException, UserManagementException;

    void delete(String userName) throws UserRetrievalException, UserPermissionRetrievalException, UserManagementException ;

    List<UserDTO> getAll();

    List<UserDTO> getUsersWithPermission(String permissionName);

    UserDTO getById(Long id) throws UserRetrievalException;

    UserDTO getByName(String userName) throws UserRetrievalException;

    User getUserInfo(String userName) throws UserRetrievalException;

    void updateFailedLoginCount(UserDTO userToUpdate) throws UserRetrievalException;

    void updateUserPassword(String userName, String password) throws UserRetrievalException;

    String getEncodedPassword(UserDTO user) throws UserRetrievalException;

    String encodePassword(String password);

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
