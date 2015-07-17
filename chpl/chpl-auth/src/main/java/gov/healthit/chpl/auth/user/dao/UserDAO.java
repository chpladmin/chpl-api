package gov.healthit.chpl.auth.user.dao;
import gov.healthit.chpl.auth.permission.UserPermissionRetrievalException;
import gov.healthit.chpl.auth.user.UserDTO;
import gov.healthit.chpl.auth.user.User;
import gov.healthit.chpl.auth.user.UserCreationException;
import gov.healthit.chpl.auth.user.UserEntity;
import gov.healthit.chpl.auth.user.UserRetrievalException;
import gov.healthit.chpl.auth.user.UserCreationDTO;

import java.util.List;

public interface UserDAO {
	
	public void create(UserCreationDTO user) throws UserCreationException;
	
	public void delete(String uname);
	
	public void delete(Long userId);
	
	public List<UserDTO> findAll();

	public User getById(Long userId) throws UserRetrievalException;
	
	public User getByName(String uname) throws UserRetrievalException;

	public void update(UserDTO user);
	
	public void update(UserCreationDTO userInfo);
	
	public void addPermission(String uname, String authority) throws UserPermissionRetrievalException, UserRetrievalException;
	
	public void removePermission(String uname, String authority);
	
}

