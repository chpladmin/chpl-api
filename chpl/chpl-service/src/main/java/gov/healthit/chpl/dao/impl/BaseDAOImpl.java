package gov.healthit.chpl.dao.impl;

import gov.healthit.chpl.util.AuthUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;

public class BaseDAOImpl {
    public static final String SCHEMA_NAME = "openchpl";

    @PersistenceContext
    protected EntityManager entityManager;
    @Autowired
    protected ErrorMessageUtil msgUtil;

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public void setEntityManager(final EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public Long getUserId(final Long defaultUserID) {
        // If there is no user the current context, assume this is a system
        // process
        if (AuthUtil.getCurrentUser() == null || AuthUtil.getCurrentUser().getId() == null) {
            return defaultUserID;
        } else {
            return AuthUtil.getCurrentUser().getId();
        }
    }

    protected void create(final Object entity) {
        entityManager.persist(entity);
        entityManager.flush();
    }

    protected void update(final Object entity) {
        entityManager.merge(entity);
        entityManager.flush();
    }
}
