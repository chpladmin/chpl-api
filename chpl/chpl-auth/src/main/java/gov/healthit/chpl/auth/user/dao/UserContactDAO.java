package gov.healthit.chpl.auth.user.dao;

import gov.healthit.chpl.auth.user.UserContactEntity;


public interface UserContactDAO {

	public void create(UserContactEntity contact);
	
	public void update(UserContactEntity contact);
	
	public void delete(Long userId);
	
}
