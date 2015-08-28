package gov.healthit.chpl.auth.dao;
import gov.healthit.chpl.auth.dto.UserDTO;
import gov.healthit.chpl.auth.permission.UserPermissionRetrievalException;
import gov.healthit.chpl.auth.user.UserCreationException;
import gov.healthit.chpl.auth.user.UserRetrievalException;




import java.util.List;

public interface UserDAO {
	
	void create(UserDTO user, String encodedPassword) throws UserCreationException;
	
	public void update(UserDTO user) throws UserRetrievalException;
	
	public void delete(String uname) throws UserRetrievalException;
	
	public void delete(Long userId);
	
	public List<UserDTO> findAll();

	public UserDTO getById(Long userId) throws UserRetrievalException;
	
	public UserDTO getByName(String uname) throws UserRetrievalException;
	
	public void addPermission(String uname, String authority) throws UserPermissionRetrievalException, UserRetrievalException;
	
	public void removePermission(String uname, String authority) throws UserRetrievalException, UserPermissionRetrievalException;
	
	public void updatePassword(String uname, String encodedPassword) throws UserRetrievalException;

	public String getEncodedPassword(UserDTO user) throws UserRetrievalException;

	
}

