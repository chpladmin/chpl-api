package gov.healthit.chpl.auth.dao;
import gov.healthit.chpl.auth.dto.UserDTO;
import gov.healthit.chpl.auth.entity.UserEntity;
import gov.healthit.chpl.auth.permission.UserPermissionRetrievalException;
import gov.healthit.chpl.auth.user.UserCreationException;
import gov.healthit.chpl.auth.user.UserRetrievalException;




import java.util.List;

public interface UserDAO {
	
	public UserDTO create(UserDTO user, String encodedPassword) throws UserCreationException;
	
	public UserDTO update(UserDTO user) throws UserRetrievalException;
	
	public void delete(String uname) throws UserRetrievalException;
	
	public void delete(Long userId)  throws UserRetrievalException;
	
	public List<UserDTO> findAll();
	
	public List<UserDTO> findByNames(List<String> names);
	public UserDTO findUserByNameAndEmail(String username, String email);
	public UserDTO findUser(UserDTO user);
	
	public UserDTO getById(Long userId) throws UserRetrievalException;
	
	public UserDTO getByName(String uname) throws UserRetrievalException;
		
	public void addPermission(String uname, String authority) throws UserPermissionRetrievalException, UserRetrievalException;
	
	public void removePermission(String uname, String authority) throws UserRetrievalException, UserPermissionRetrievalException;
	
	public void updatePassword(String uname, String encodedPassword) throws UserRetrievalException;
	public void updateFailedLoginCount(String uname, int failedLoginCount) throws UserRetrievalException;
	public void updateAccountLockedStatus(String uname, boolean locked) throws UserRetrievalException;
	public String getEncodedPassword(UserDTO user) throws UserRetrievalException;

	
}

