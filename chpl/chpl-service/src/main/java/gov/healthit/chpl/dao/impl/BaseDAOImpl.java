package gov.healthit.chpl.dao.impl;

import gov.healthit.chpl.auth.Util;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;

public class BaseDAOImpl {

    @PersistenceContext
    protected EntityManager entityManager;
    @Autowired
    MessageSource messageSource;
    
    private static final long MODIFIED_USER_ID = -3L;

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public void setEntityManager(final EntityManager entityManager) {
        this.entityManager = entityManager;
    }
    
    protected Long getUserId() {
        // If there is no user the current context, assume this is a system
        // process
        if (Util.getCurrentUser() == null || Util.getCurrentUser().getId() == null) {
            return MODIFIED_USER_ID;
        } else {
            return Util.getCurrentUser().getId();
        }
    }

}
