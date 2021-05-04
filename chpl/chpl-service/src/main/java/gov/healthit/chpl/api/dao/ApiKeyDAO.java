package gov.healthit.chpl.api.dao;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import gov.healthit.chpl.api.domain.ApiKeyDTO;
import gov.healthit.chpl.api.entity.ApiKeyEntity;
import gov.healthit.chpl.caching.CacheNames;
import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.util.AuthUtil;

@Repository("apiKeyDAO")
public class ApiKeyDAO extends BaseDAOImpl {

    public ApiKeyDTO create(ApiKeyDTO dto) throws EntityCreationException {

        ApiKeyEntity entity = null;
        try {
            if (dto.getId() != null) {
                entity = this.getEntityById(dto.getId());
            }
        } catch (final EntityRetrievalException e) {
            throw new EntityCreationException(e);
        }

        if (entity != null) {
            throw new EntityCreationException("An entity with this ID already exists.");
        } else {

            entity = new ApiKeyEntity();
            entity.setApiKey(dto.getApiKey());
            entity.setEmail(dto.getEmail());
            entity.setNameOrganization(dto.getNameOrganization());
            entity.setCreationDate(dto.getCreationDate());
            entity.setUnrestricted(dto.getUnrestricted());
            if (dto.getLastModifiedDate() == null) {
                entity.setLastModifiedDate(new Date());
            } else {
                entity.setLastModifiedDate(dto.getLastModifiedDate());
            }
            if (dto.getLastUsedDate() == null) {
                entity.setLastUsedDate(new Date());
            } else {
                entity.setLastUsedDate(dto.getLastUsedDate());
            }
            entity.setDeleted(dto.getDeleted());
            if (dto.getLastModifiedUser() == null) {
                entity.setLastModifiedUser(AuthUtil.getAuditId());
            } else {
                entity.setLastModifiedUser(dto.getLastModifiedUser());
            }
            create(entity);
        }
        return new ApiKeyDTO(entity);
    }

    public ApiKeyDTO update(ApiKeyDTO dto) throws EntityRetrievalException {

        ApiKeyEntity entity = getEntityById(dto.getId());

        entity.setApiKey(dto.getApiKey());
        entity.setEmail(dto.getEmail());
        entity.setNameOrganization(dto.getNameOrganization());
        entity.setCreationDate(dto.getCreationDate());
        entity.setDeleted(dto.getDeleted());
        entity.setUnrestricted(dto.getUnrestricted());
        if (dto.getLastModifiedDate() == null) {
            entity.setLastModifiedDate(new Date());
        } else {
            entity.setLastModifiedDate(dto.getLastModifiedDate());
        }
        if (dto.getLastModifiedUser() == null) {
            entity.setLastModifiedUser(AuthUtil.getAuditId());
        } else {
            entity.setLastModifiedUser(dto.getLastModifiedUser());
        }
        entity.setLastUsedDate(dto.getLastUsedDate());
        entity.setDeleteWarningSentDate(dto.getDeleteWarningSentDate());
        update(entity);

        return new ApiKeyDTO(entity);
    }

    public void delete(Long id) {
        Query query = entityManager.createQuery("UPDATE ApiKeyEntity SET deleted = true WHERE api_key_id = :entityid");
        query.setParameter("entityid", id);
        query.executeUpdate();
    }

    public List<ApiKeyDTO> findAll(Boolean includeDeleted) {

        List<ApiKeyEntity> entities = getAllEntities(includeDeleted);
        List<ApiKeyDTO> dtos = new ArrayList<>();

        for (ApiKeyEntity entity : entities) {
            ApiKeyDTO dto = new ApiKeyDTO(entity);
            dtos.add(dto);
        }
        return dtos;

    }

    public List<ApiKeyDTO> findAllUnrestricted() {

        List<ApiKeyEntity> entities = getAllUnrestrictedApiKeyEntities();
        List<ApiKeyDTO> dtos = new ArrayList<>();

        for (ApiKeyEntity entity : entities) {
            ApiKeyDTO dto = new ApiKeyDTO(entity);
            dtos.add(dto);
        }
        return dtos;

    }

    public ApiKeyDTO getById(Long id) throws EntityRetrievalException {

        ApiKeyDTO dto = null;
        ApiKeyEntity entity = getEntityById(id);

        if (entity != null) {
            dto = new ApiKeyDTO(entity);
        }
        return dto;

    }

    public ApiKeyDTO getByKey(String apiKey) throws EntityRetrievalException {

        ApiKeyDTO dto = null;
        ApiKeyEntity entity = getEntityByKey(apiKey);

        if (entity != null) {
            dto = new ApiKeyDTO(entity);
        }
        return dto;
    }

    public List<ApiKeyDTO> findAllRevoked() {

        List<ApiKeyEntity> entities = getAllRevokedEntities();
        List<ApiKeyDTO> dtos = new ArrayList<>();

        for (ApiKeyEntity entity : entities) {
            ApiKeyDTO dto = new ApiKeyDTO(entity);
            dtos.add(dto);
        }
        return dtos;

    }

    public ApiKeyDTO getRevokedKeyByKey(String apiKey) {

        ApiKeyDTO dto = null;
        ApiKeyEntity entity = getRevokedEntityByKey(apiKey);

        if (entity != null) {
            dto = new ApiKeyDTO(entity);
        }
        return dto;
    }

    public List<ApiKeyDTO> findAllNotUsedInXDays(Integer days) {
        List<ApiKeyEntity> entities = getAllNotUsedInXDays(days);
        List<ApiKeyDTO> dtos = new ArrayList<>();

        for (ApiKeyEntity entity : entities) {
            ApiKeyDTO dto = new ApiKeyDTO(entity);
            dtos.add(dto);
        }
        return dtos;
    }

    public List<ApiKeyDTO> findAllToBeRevoked(Integer daysSinceWarningSent) {
        List<ApiKeyEntity> entities = getAllToBeRevoked(daysSinceWarningSent);
        List<ApiKeyDTO> dtos = new ArrayList<>();

        for (ApiKeyEntity entity : entities) {
            ApiKeyDTO dto = new ApiKeyDTO(entity);
            dtos.add(dto);
        }
        return dtos;
    }

    private void create(final ApiKeyEntity entity) {
        entityManager.persist(entity);
        entityManager.flush();
    }

    private void update(final ApiKeyEntity entity) {

        entityManager.merge(entity);
        entityManager.flush();

    }

    private List<ApiKeyEntity> getAllEntities(final Boolean includeDeleted) {
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

    private List<ApiKeyEntity> getAllNotUsedInXDays(final Integer days) {
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

    private List<ApiKeyEntity> getAllToBeRevoked(final Integer daysSinceWarningSent) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, (-1 * daysSinceWarningSent));
        List<ApiKeyEntity> result = entityManager.createQuery(
                "from ApiKeyEntity where deleted <> true AND deleteWarningSentDate < :targetDate",
                ApiKeyEntity.class)
                .setParameter("targetDate", cal.getTime())
                .getResultList();
        return result;
    }

    private ApiKeyEntity getEntityById(final Long entityId) throws EntityRetrievalException {
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

    private ApiKeyEntity getEntityByKey(final String key) throws EntityRetrievalException {
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

    private ApiKeyEntity getRevokedEntityByKey(final String key) {
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
