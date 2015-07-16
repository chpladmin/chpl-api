package gov.healthit.chpl.auth.user.dao;
import gov.healthit.chpl.auth.permission.UserPermissionRetrievalException;
import gov.healthit.chpl.auth.user.DatabaseAuthenticatedUser;
import gov.healthit.chpl.auth.user.User;
import gov.healthit.chpl.auth.user.UserEntity;
import gov.healthit.chpl.auth.user.UserRetrievalException;
import gov.healthit.chpl.auth.user.UserUploadDTO;

import java.util.List;

public interface UserDAO {
	
	public void create(UserUploadDTO user);
	
	public void delete(String uname);
	
	public void delete(Long userId);
	
	public List<UserEntity> findAll();

	public User getById(Long userId) throws UserRetrievalException;
	
	public User getByName(String uname) throws UserRetrievalException;

	public void update(DatabaseAuthenticatedUser user);
	
	public void addPermission(String authority);

	public void addPermission(String uname, String authority) throws UserPermissionRetrievalException, UserRetrievalException;
	
	public void removePermission(String uname, String authority);
	
}

