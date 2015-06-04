package gov.healthit.chpl.auth.user.dao;
import gov.healthit.chpl.auth.user.User;
import gov.healthit.chpl.auth.user.UserImpl;
import gov.healthit.chpl.auth.user.UserRetrievalException;

import java.util.List;

public interface UserDAO {
	
	public void create(UserImpl user);
	
	public void deactivate(String uname);
	
	public void deactivate(Long userId);
	
	public List<UserImpl> findAll();

	public UserImpl getById(Long userId) throws UserRetrievalException;
	
	public UserImpl getByName(String uname) throws UserRetrievalException;

	public void update(UserImpl user);
}

