package gov.healthit.chpl.sharedstore;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import jakarta.persistence.Query;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaBuilder.In;
import jakarta.persistence.criteria.CriteriaDelete;
import jakarta.persistence.criteria.Root;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class SharedStoreDAO extends BaseDAOImpl {

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void add(SharedStore data) {
        Query query = entityManager.createNamedQuery("upsert");
        query.setParameter("domain", data.getDomain());
        query.setParameter("key", data.getKey());
        query.setParameter("value", data.getValue());
        query.executeUpdate();
    }

    @Transactional(readOnly = true)
    public SharedStore get(String domain, String key) {
        SharedStoreEntity entity = getEntity(domain, key);
        if (entity != null) {
            return entity.toDomain();
        } else {
            return null;
        }
    }

    @Transactional(readOnly = true)
    public List<SharedStore> getAll(String domain) {
        List<SharedStoreEntity> entities = getEntities(domain);
        if (!CollectionUtils.isEmpty(entities)) {
            return entities.stream()
                    .map(entity -> entity.toDomain())
                    .collect(Collectors.toList());
        } else {
            return null;
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void remove(String domain, String key) {
        remove(domain, List.of(key));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void remove(String domain, List<String> keys) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaDelete<SharedStoreEntity> delete = cb.createCriteriaDelete(SharedStoreEntity.class);
        Root<SharedStoreEntity> root = delete.from(SharedStoreEntity.class);
        In<String> inClause = cb.in(root.get("primaryKey").get("key"));
        keys.forEach(key -> inClause.value(key));
        delete.where(inClause, cb.equal(root.get("primaryKey").get("domain"), domain));
        entityManager.createQuery(delete).executeUpdate();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void removeByDomain(String domain) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaDelete<SharedStoreEntity> delete = cb.createCriteriaDelete(SharedStoreEntity.class);
        Root<SharedStoreEntity> root = delete.from(SharedStoreEntity.class);
        delete.where(cb.equal(root.get("primaryKey").get("domain"), domain));
        entityManager.createQuery(delete).executeUpdate();
    }

    private SharedStoreEntity getEntity(String domain, String key) {
        List<SharedStoreEntity> result = entityManager.createQuery(
                "FROM SharedStoreEntity sse "
                        + "WHERE sse.primaryKey.domain = :domain "
                        + "AND sse.primaryKey.key = :key ",
                SharedStoreEntity.class)
                .setParameter("domain", domain)
                .setParameter("key", key)
                .getResultList();

        if (result.size() == 0) {
            return null;
        }

        try {
            getSession().evict(result.get(0));
            return result.get(0);
        } catch (Exception e) {
            LOGGER.error("Error retrieving object from Shared Store ({}:{}) - {}", domain, key, e.getMessage(), e);
            return null;
        }
    }

    private List<SharedStoreEntity> getEntities(String domain) {
        List<SharedStoreEntity> results = entityManager.createQuery(
                "FROM SharedStoreEntity sse "
                        + "WHERE sse.primaryKey.domain = :domain ",
                SharedStoreEntity.class)
                .setParameter("domain", domain)
                .getResultList();

        if (results.size() == 0) {
            return null;
        }

        try {
            results.stream()
                .forEach(result -> getSession().evict(result));
            return results;
        } catch (Exception e) {
            LOGGER.error("Error retrieving objects from Shared Store ({}) - {}", domain, e.getMessage(), e);
            return null;
        }
    }
}
