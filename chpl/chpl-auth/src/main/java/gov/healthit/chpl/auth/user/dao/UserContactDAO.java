package gov.healthit.chpl.auth.user.dao;

import gov.healthit.chpl.auth.user.UserContact;


public interface UserContactDAO {

	public void create(UserContact contact);
	
	public void update(UserContact contact);
	
	public void delete(Long userId);
	
}
