package gov.healthit.chpl.sharedstorage;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;

@Component
public class SharedDataDAO extends BaseDAOImpl {

    public void add(SharedData data) {
        SharedDataEntity entity = SharedDataEntity.builder()
                .primaryKey(SharedDataPrimaryKey.builder()
                        .domain(data.getDomain())
                        .key(data.getKey())
                        .build())
                .value(data.getValue())
                .putDate(LocalDateTime.now())
                .build();

        create(entity);
    }

    public SharedData get(String type, String key) {
        try {
            SharedDataEntity entity = getEntity(type, key);
            if (entity != null) {
                return entity.toDomain();
            } else {
                return null;
            }
        } catch (SharedDataNotFoundException e) {
            return null;
        }

    }

    public void remove(String type, String key) {
        try {
            SharedDataEntity entity = getEntity(type, key);
            if (entity != null) {
                getEntityManager().remove(entity);
            }
        } catch (SharedDataNotFoundException e) {
            return;
        }
    }

    private SharedDataEntity getEntity(String domain, String key) throws SharedDataNotFoundException {
        List<SharedDataEntity> result = entityManager.createQuery(
                "FROM SharedDataEntity sde "
                + "WHERE sde.primaryKey.domain = :domain "
                + "AND sde.primaryKey.key = :key ", SharedDataEntity.class)
                .setParameter("domain", domain)
                .setParameter("key", key)
                .getResultList();

        if (result == null || result.size() == 0) {
            throw new SharedDataNotFoundException(
                    "Data error. SharedData not found in database.");
        }

        if (result.size() == 0) {
            return null;
        }
        return result.get(0);
    }
}
