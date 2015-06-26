package gov.healthit.chpl.auth.user.dao;
import gov.healthit.chpl.auth.user.UserEntity;
import gov.healthit.chpl.auth.user.UserRetrievalException;

import java.util.List;

public interface UserDAO {
	
	public void create(UserEntity user);
	
	public void deactivate(String uname);
	
	public void deactivate(Long userId);
	
	public List<UserEntity> findAll();

	public UserEntity getById(Long userId) throws UserRetrievalException;
	
	public UserEntity getByName(String uname) throws UserRetrievalException;

	public void update(UserEntity user);
}

