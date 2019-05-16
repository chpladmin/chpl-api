package gov.healthit.chpl.dao.auth;

import java.util.List;

import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.exception.UserCreationException;
import gov.healthit.chpl.exception.UserRetrievalException;

public interface UserDAO {

    public UserDTO create(UserDTO user, String encodedPassword) throws UserCreationException;

    public UserDTO update(UserDTO user) throws UserRetrievalException;

    public void delete(String uname) throws UserRetrievalException;

    public void delete(Long userId) throws UserRetrievalException;

    public List<UserDTO> findAll();

    public UserDTO findUserByNameAndEmail(String username, String email);

    public UserDTO findUser(UserDTO user);

    public UserDTO getById(Long userId) throws UserRetrievalException;

    public UserDTO getByName(String uname) throws UserRetrievalException;

    public List<UserDTO> getUsersWithPermission(String permissionName);

    public void updatePassword(String uname, String encodedPassword) throws UserRetrievalException;

    public void updateFailedLoginCount(String uname, int failedLoginCount) throws UserRetrievalException;

    public void updateAccountLockedStatus(String uname, boolean locked) throws UserRetrievalException;

    public String getEncodedPassword(UserDTO user) throws UserRetrievalException;

}
