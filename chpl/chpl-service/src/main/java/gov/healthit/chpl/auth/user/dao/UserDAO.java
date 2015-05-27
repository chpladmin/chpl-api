package gov.healthit.chpl.auth.user.dao;
import gov.healthit.chpl.auth.user.User;
import gov.healthit.chpl.auth.user.UserRetrievalException;

import java.util.List;

public interface UserDAO {
	
	public void create(User user);
	
	public void deactivate(String uname);
	
	public void deactivate(Long userId);
	
	public List<User> findAll();

	public User getById(Long userId) throws UserRetrievalException;
	
	public User getByName(String uname) throws UserRetrievalException;

	public void update(User user);
}

