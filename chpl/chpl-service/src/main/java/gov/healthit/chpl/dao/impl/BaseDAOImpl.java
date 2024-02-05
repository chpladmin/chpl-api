package gov.healthit.chpl.dao.impl;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;

import gov.healthit.chpl.util.ErrorMessageUtil;

public class BaseDAOImpl {
    public static final String SCHEMA_NAME = "openchpl";
    public static final String SHARED_STORE_SCHEMA_NAME = "shared_store";
    public static final int BATCH_SIZE = 20;

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

    public Session getSession() {
        return getEntityManager().unwrap(Session.class);
    }

    protected void create(final Object entity) {
        entityManager.persist(entity);
        entityManager.flush();
        entityManager.clear();
    }

    protected void update(final Object entity) {
        entityManager.merge(entity);
        entityManager.flush();
        entityManager.clear();
    }
}
