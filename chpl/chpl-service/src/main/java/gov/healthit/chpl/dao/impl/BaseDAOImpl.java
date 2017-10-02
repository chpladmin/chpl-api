package gov.healthit.chpl.dao.impl;


import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;


public class BaseDAOImpl {

	@PersistenceContext protected EntityManager entityManager;
	@Autowired MessageSource messageSource;

	public EntityManager getEntityManager() {
		return entityManager;
	}

	public void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

}
