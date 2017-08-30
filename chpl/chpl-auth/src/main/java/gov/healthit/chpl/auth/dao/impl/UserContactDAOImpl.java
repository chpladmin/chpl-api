package gov.healthit.chpl.auth.dao.impl;

import java.util.Date;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.auth.BaseDAOImpl;
import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.auth.dao.UserContactDAO;
import gov.healthit.chpl.auth.entity.UserContactEntity;


@Repository(value="userContactDAO")
public class UserContactDAOImpl extends BaseDAOImpl implements UserContactDAO {

	@Override
	@Transactional
	public void create(UserContactEntity contact) {
		
		entityManager.persist(contact);
		
	}

	@Override
	@Transactional
	public void update(UserContactEntity contact) {
		
		entityManager.merge(contact);
		
	}

	@Override
	public void delete(Long contactId) {
		Query query = entityManager.createQuery("UPDATE UserContact "
				+ "SET deleted = true, "
				+ " last_updated_date = NOW(), "
				+ " last_updated_user = " + Util.getCurrentUser().getId() 
				+ "WHERE contact_id = :contactid");
		query.setParameter("contactid", contactId);
		query.executeUpdate();
	}

	@Override 
	public void delete(UserContactEntity contact) {
		contact.setLastModifiedDate(new Date());
		contact.setLastModifiedUser(Util.getCurrentUser().getId());
		contact.setDeleted(true);
		update(contact);
	}
}
