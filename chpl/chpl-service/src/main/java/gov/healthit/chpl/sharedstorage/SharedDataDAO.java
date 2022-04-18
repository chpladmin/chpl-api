package gov.healthit.chpl.sharedstorage;

import java.time.LocalDateTime;
import java.util.List;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;

public class SharedDataDAO extends BaseDAOImpl {

    public void create(SharedData data) {
        SharedDataEntity entity = SharedDataEntity.builder()
                .primaryKey(SharedDataPrimaryKey.builder()
                        .type(data.getType())
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

    private SharedDataEntity getEntity(String type, String key) throws SharedDataNotFoundException {
        List<SharedDataEntity> result = entityManager.createQuery(
                "FROM SharedDataEntity sde "
                + "WHERE sde.type = :type "
                + "AND sde.key = : key ", SharedDataEntity.class)
                .setParameter("type", type)
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
