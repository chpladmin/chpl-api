package gov.healthit.chpl.auth.user.dao.impl;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.auth.BaseDAOImpl;
import gov.healthit.chpl.auth.user.UserContact;
import gov.healthit.chpl.auth.user.dao.UserContactDAO;


@Repository(value="userContactDAO")
public class UserContactDAOImpl extends BaseDAOImpl implements UserContactDAO {

	@Override
	public void create(UserContact contact) {
		
		entityManager.persist(contact);
		
	}

	@Override
	public void update(UserContact contact) {
		
		entityManager.merge(contact);
		
	}

	@Override
	public void delete(Long contactId) {
		
		Query query = entityManager.createQuery("UPDATE UserContact SET deleted = true WHERE c.contact_id = :contactid");
		query.setParameter("contactid", contactId);
		query.executeUpdate();

	}

}
