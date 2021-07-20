package gov.healthit.chpl.api.dao;

import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Query;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import gov.healthit.chpl.api.domain.ApiKey;
import gov.healthit.chpl.api.entity.ApiKeyEntity;
import gov.healthit.chpl.caching.CacheNames;
import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.util.AuthUtil;

@Repository("apiKeyDAO")
public class ApiKeyDAO extends BaseDAOImpl {

    public ApiKey create(ApiKey apiKey) throws EntityCreationException {
        ApiKeyEntity entity = null;
        try {
            if (apiKey.getId() != null) {
                entity = this.getEntityById(apiKey.getId());
            }
        } catch (final EntityRetrievalException e) {
            throw new EntityCreationException(e);
        }

        if (entity != null) {
            throw new EntityCreationException("An entity with this ID already exists.");
        } else {

            entity = new ApiKeyEntity();
            entity.setApiKey(apiKey.getKey());
            entity.setEmail(apiKey.getEmail());
            entity.setNameOrganization(apiKey.getName());
            entity.setUnrestricted(apiKey.isUnrestricted());
            entity.setLastModifiedUser(AuthUtil.getAuditId());
            entity.setDeleted(false);
            create(entity);
        }
        return entity.toDomain();
    }

    public ApiKey update(ApiKey apiKey) throws EntityRetrievalException {
        ApiKeyEntity entity = getEntityById(apiKey.getId());
        entity.setApiKey(apiKey.getKey());
        entity.setEmail(apiKey.getEmail());
        entity.setNameOrganization(apiKey.getName());
        entity.setLastModifiedUser(AuthUtil.getAuditId());
        entity.setLastUsedDate(apiKey.getLastUsedDate());
        entity.setDeleteWarningSentDate(apiKey.getDeleteWarningSentDate());
        entity.setUnrestricted(apiKey.isUnrestricted());
        update(entity);
        return entity.toDomain();
    }

    public void delete(Long id) throws EntityRetrievalException {
        ApiKeyEntity entity = getEntityById(id);
        entity.setDeleted(true);
        entity.setLastModifiedUser(AuthUtil.getAuditId());
        update(entity);
    }

    public List<ApiKey> findAll(Boolean includeDeleted) {
        List<ApiKeyEntity> entities = getAllEntities(includeDeleted);
        return entities.stream().map(entity -> entity.toDomain()).collect(Collectors.toList());

    }

    public List<ApiKey> findAllUnrestricted() {
        List<ApiKeyEntity> entities = getAllUnrestrictedApiKeyEntities();
        return entities.stream().map(entity -> entity.toDomain()).collect(Collectors.toList());
    }

    public ApiKey getById(Long id) throws EntityRetrievalException {
        ApiKeyEntity entity = getEntityById(id);
        if (entity != null) {
            return entity.toDomain();
        }
        return null;

    }

    public ApiKey getByKey(String apiKey) throws EntityRetrievalException {
        ApiKeyEntity entity = getEntityByKey(apiKey);
        if (entity != null) {
            return entity.toDomain();
        }
        return null;
    }

    public List<ApiKey> findAllRevoked() {
        List<ApiKeyEntity> entities = getAllRevokedEntities();
        return entities.stream().map(entity -> entity.toDomain()).collect(Collectors.toList());
    }

    public ApiKey getRevokedKeyByKey(String apiKey) {
        ApiKeyEntity entity = getRevokedEntityByKey(apiKey);
        if (entity != null) {
            return entity.toDomain();
        }
        return null;
    }

    public List<ApiKey> findAllNotUsedInXDays(Integer days) {
        List<ApiKeyEntity> entities = getAllNotUsedInXDays(days);
        return entities.stream().map(entity -> entity.toDomain()).collect(Collectors.toList());
    }

    public List<ApiKey> findAllToBeRevoked(Integer daysSinceWarningSent) {
        List<ApiKeyEntity> entities = getAllToBeRevoked(daysSinceWarningSent);
        return entities.stream().map(entity -> entity.toDomain()).collect(Collectors.toList());
    }

    private List<ApiKeyEntity> getAllEntities(Boolean includeDeleted) {
        List<ApiKeyEntity> result;
        if (includeDeleted) {
            result = entityManager
                    .createQuery("from ApiKeyEntity ", ApiKeyEntity.class)
                    .getResultList();
        } else {
            result = entityManager
                    .createQuery("from ApiKeyEntity where (deleted = false) ", ApiKeyEntity.class)
                    .getResultList();
        }
        return result;
    }

    @Cacheable(CacheNames.GET_ALL_UNRESTRICTED_APIKEYS)
    private List<ApiKeyEntity> getAllUnrestrictedApiKeyEntities() {
        List<ApiKeyEntity> result = entityManager
                .createQuery("from ApiKeyEntity where (NOT deleted = true) AND unrestricted = true",
                        ApiKeyEntity.class)
                .getResultList();
        return result;
    }

    private List<ApiKeyEntity> getAllNotUsedInXDays(Integer days) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, (-1 * days));
        List<ApiKeyEntity> result = entityManager.createQuery(
                "from ApiKeyEntity where deleted <> true "
                        + "AND lastUsedDate < :targetDate "
                        + "AND deleteWarningSentDate is null",
                ApiKeyEntity.class)
                .setParameter("targetDate", cal.getTime())
                .getResultList();
        return result;
    }

    private List<ApiKeyEntity> getAllToBeRevoked(Integer daysSinceWarningSent) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, (-1 * daysSinceWarningSent));
        List<ApiKeyEntity> result = entityManager.createQuery(
                "from ApiKeyEntity where deleted <> true AND deleteWarningSentDate < :targetDate",
                ApiKeyEntity.class)
                .setParameter("targetDate", cal.getTime())
                .getResultList();
        return result;
    }

    private ApiKeyEntity getEntityById(Long entityId) throws EntityRetrievalException {
        ApiKeyEntity entity = null;
        Query query = entityManager.createQuery(
                "from ApiKeyEntity "
                        + "where (api_key_id = :entityid) "
                        + "and deleted <> true",
                ApiKeyEntity.class);
        query.setParameter("entityid", entityId);
        List<ApiKeyEntity> result = query.getResultList();

        if (result == null || result.size() == 0) {
            String msg = msgUtil.getMessage("apikey.notFound");
            throw new EntityRetrievalException(msg);
        } else if (result.size() > 1) {
            throw new EntityRetrievalException("Data error. Duplicate api key id in database.");
        } else if (result.size() == 1) {
            entity = result.get(0);
        }
        return entity;
    }

    private ApiKeyEntity getEntityByKey(String key) throws EntityRetrievalException {
        ApiKeyEntity entity = null;
        Query query = entityManager.createQuery("from ApiKeyEntity where (NOT deleted = true) AND (api_key = :apikey) ",
                ApiKeyEntity.class);
        query.setParameter("apikey", key);
        List<ApiKeyEntity> result = query.getResultList();

        if (result == null || result.size() == 0) {
            String msg = msgUtil.getMessage("apikey.notFound");
            throw new EntityRetrievalException(msg);
        } else {
            entity = result.get(0);
        }
        return entity;
    }

    private List<ApiKeyEntity> getAllRevokedEntities() {
        List<ApiKeyEntity> result = entityManager
                .createQuery("from ApiKeyEntity where (deleted = true) ", ApiKeyEntity.class).getResultList();
        return result;
    }

    private ApiKeyEntity getRevokedEntityByKey(String key) {
        ApiKeyEntity entity = null;

        Query query = entityManager.createQuery("from ApiKeyEntity where (deleted = true) AND (api_key = :apikey) ",
                ApiKeyEntity.class);
        query.setParameter("apikey", key);
        List<ApiKeyEntity> result = query.getResultList();

        if (result != null && result.size() > 0) {
            entity = result.get(0);
        }
        return entity;
    }
}
