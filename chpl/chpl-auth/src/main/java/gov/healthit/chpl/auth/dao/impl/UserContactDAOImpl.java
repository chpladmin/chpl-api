package gov.healthit.chpl.auth.dao.impl;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.auth.BaseDAOImpl;
import gov.healthit.chpl.auth.dao.UserContactDAO;
import gov.healthit.chpl.auth.entity.UserContactEntity;


@Repository(value="userContactDAO")
public class UserContactDAOImpl extends BaseDAOImpl implements UserContactDAO {

	@Override
	public void create(UserContactEntity contact) {
		
		entityManager.persist(contact);
		
	}

	@Override
	public void update(UserContactEntity contact) {
		
		entityManager.merge(contact);
		
	}

	@Override
	public void delete(Long contactId) {
		
		Query query = entityManager.createQuery("UPDATE UserContact SET deleted = true WHERE contact_id = :contactid");
		query.setParameter("contactid", contactId);
		query.executeUpdate();

	}

}
