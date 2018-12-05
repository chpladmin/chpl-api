package gov.healthit.chpl.dao.impl;

import gov.healthit.chpl.auth.Util;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;

public class BaseDAOImpl {
    public static final String SCHEMA_NAME = "openchpl";

    @PersistenceContext
    protected EntityManager entityManager;
    @Autowired
    MessageSource messageSource;

    /**
     * User ID for the "system" user.
     */
    public static final Long SYSTEM_USER_ID = -3L;
    // Other constants can be added for other user types

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public void setEntityManager(final EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public Long getUserId(final Long defaultUserID) {
        // If there is no user the current context, assume this is a system
        // process
        if (Util.getCurrentUser() == null || Util.getCurrentUser().getId() == null) {
            return defaultUserID;
        } else {
            return Util.getCurrentUser().getId();
        }
    }
}
