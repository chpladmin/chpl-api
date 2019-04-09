package gov.healthit.chpl.dao.auth;

import gov.healthit.chpl.auth.entity.UserContactEntity;


public interface UserContactDAO {

	public void create(UserContactEntity contact);
	
	public void update(UserContactEntity contact);
	
	public void delete(Long userId);
	public void delete(UserContactEntity contact);
}
