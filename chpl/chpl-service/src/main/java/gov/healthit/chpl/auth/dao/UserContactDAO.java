package gov.healthit.chpl.auth.dao;

import gov.healthit.chpl.auth.entity.UserContactEntity;


public interface UserContactDAO {

	public void create(UserContactEntity contact);
	
	public void update(UserContactEntity contact);
	
	public void delete(Long userId);
	public void delete(UserContactEntity contact);
}
