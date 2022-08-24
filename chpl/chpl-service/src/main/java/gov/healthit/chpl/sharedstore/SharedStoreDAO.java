package gov.healthit.chpl.sharedstore;

import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaBuilder.In;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.Root;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;

@Component
public class SharedStoreDAO extends BaseDAOImpl {

    @Transactional()
    public void add(SharedStore data) {
        SharedStoreEntity entity = SharedStoreEntity.builder()
                .primaryKey(SharedStorePrimaryKey.builder()
                        .domain(data.getDomain())
                        .key(data.getKey())
                        .build())
                .value(data.getValue())
                .putDate(LocalDateTime.now())
                .build();

        create(entity);
    }

    @Transactional(readOnly = true)
    public SharedStore get(String type, String key) {
        SharedStoreEntity entity = getEntity(type, key);
        if (entity != null) {
            return entity.toDomain();
        } else {
            return null;
        }
    }

    @Transactional()
    public void remove(String type, String key) {
        remove(type, List.of(key));
    }

    @Transactional()
    public void remove(String type, List<String> keys) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaDelete<SharedStoreEntity> delete = cb.createCriteriaDelete(SharedStoreEntity.class);
        Root<SharedStoreEntity> root = delete.from(SharedStoreEntity.class);
        In<String> inClause = cb.in(root.get("primaryKey").get("key"));
        keys.forEach(key -> inClause.value(key));
        delete.where(inClause, cb.equal(root.get("primaryKey").get("domain"), type));
        entityManager.createQuery(delete).executeUpdate();
    }

    @Transactional()
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
                + "AND sse.primaryKey.key = :key ", SharedStoreEntity.class)
                .setParameter("domain", domain)
                .setParameter("key", key)
                .getResultList();

        if (result.size() == 0) {
            return null;
        }
        return result.get(0);
    }
}
