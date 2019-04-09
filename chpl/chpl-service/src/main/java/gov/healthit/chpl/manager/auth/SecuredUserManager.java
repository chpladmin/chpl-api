package gov.healthit.chpl.manager.auth;

import java.util.List;

import gov.healthit.chpl.auth.entity.UserEntity;
import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.exception.UserCreationException;
import gov.healthit.chpl.exception.UserManagementException;
import gov.healthit.chpl.exception.UserPermissionRetrievalException;
import gov.healthit.chpl.exception.UserRetrievalException;

public interface SecuredUserManager {

    public UserDTO create(UserDTO user, String encodedPassword) throws UserCreationException, UserRetrievalException;

    public UserDTO update(UserDTO user) throws UserRetrievalException;

    public void updateContactInfo(UserEntity user);

    public void delete(UserDTO user)
            throws UserRetrievalException, UserPermissionRetrievalException, UserManagementException;

    public List<UserDTO> getAll();

    public UserDTO getById(Long id) throws UserRetrievalException;

    public List<UserDTO> getUsersWithPermission(String permissionName);

    public void updatePassword(UserDTO user, String encodedPassword) throws UserRetrievalException;

    public void updateFailedLoginCount(UserDTO user) throws UserRetrievalException;

    public void updateAccountLockedStatus(UserDTO user) throws UserRetrievalException;

    public UserDTO getBySubjectName(String userName) throws UserRetrievalException;

}
