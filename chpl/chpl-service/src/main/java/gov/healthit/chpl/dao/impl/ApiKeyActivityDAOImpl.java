package gov.healthit.chpl.dao.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.ApiKeyActivityDAO;
import gov.healthit.chpl.dto.ApiKeyActivityDTO;
import gov.healthit.chpl.entity.ApiKeyActivityEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.util.AuthUtil;

@Repository("apiKeyActivityDAO")
public class ApiKeyActivityDAOImpl extends BaseDAOImpl implements ApiKeyActivityDAO {

    @Override
    public ApiKeyActivityDTO create(ApiKeyActivityDTO dto) throws EntityCreationException {

        ApiKeyActivityEntity entity = null;
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

            entity = new ApiKeyActivityEntity();
            entity.setApiKeyId(dto.getApiKeyId());
            entity.setApiCallPath(dto.getApiCallPath());
            entity.setApiCallMethod(dto.getApiCallMethod());

            if (dto.getLastModifiedDate() != null) {
                entity.setLastModifiedDate(dto.getLastModifiedDate());
            } else {
                entity.setLastModifiedDate(new Date());
            }
            if (dto.getCreationDate() != null) {
                entity.setCreationDate(dto.getCreationDate());
            } else {
                entity.setCreationDate(new Date());
            }
            entity.setDeleted(dto.getDeleted());

            if (AuthUtil.getCurrentUser() == null) {
                entity.setLastModifiedUser(-3L);
            } else {
                entity.setLastModifiedUser(AuthUtil.getAuditId());
            }

            create(entity);
        }
        return new ApiKeyActivityDTO(entity);
    }

    @Override
    public ApiKeyActivityDTO update(ApiKeyActivityDTO dto) throws EntityRetrievalException {

        ApiKeyActivityEntity entity = getEntityById(dto.getId());

        entity.setApiKeyId(dto.getApiKeyId());
        entity.setApiCallPath(dto.getApiCallPath());
        entity.setApiCallMethod(dto.getApiCallMethod());

        if (dto.getLastModifiedDate() != null) {
            entity.setLastModifiedDate(dto.getLastModifiedDate());
        } else {
            entity.setLastModifiedDate(new Date());
        }
        entity.setDeleted(dto.getDeleted());
        entity.setLastModifiedUser(AuthUtil.getAuditId());
        update(entity);

        return new ApiKeyActivityDTO(entity);
    }

    @Override
    public void delete(Long id) {

        Query query = entityManager
                .createQuery("UPDATE ApiKeyActivityEntity SET deleted = true WHERE api_activity_id = :entityid");
        query.setParameter("entityid", id);
        query.executeUpdate();

    }

    @Override
    public List<ApiKeyActivityDTO> findAll() {

        List<ApiKeyActivityEntity> entities = getAllEntities();
        List<ApiKeyActivityDTO> dtos = new ArrayList<>();

        for (ApiKeyActivityEntity entity : entities) {
            ApiKeyActivityDTO dto = new ApiKeyActivityDTO(entity);
            dtos.add(dto);
        }
        return dtos;

    }

    @Override
    public List<ApiKeyActivityDTO> findAll(Integer pageNumber, Integer pageSize) {

        List<ApiKeyActivityEntity> entities = getAllEntities(pageNumber, pageSize);
        List<ApiKeyActivityDTO> dtos = new ArrayList<>();

        for (ApiKeyActivityEntity entity : entities) {
            ApiKeyActivityDTO dto = new ApiKeyActivityDTO(entity);
            dtos.add(dto);
        }
        return dtos;

    }

    @Override
    public ApiKeyActivityDTO getById(Long id) throws EntityRetrievalException {

        ApiKeyActivityDTO dto = null;
        ApiKeyActivityEntity entity = getEntityById(id);
        if (entity != null) {
            dto = new ApiKeyActivityDTO(entity);
        }
        return dto;

    }

    @Override
    public List<ApiKeyActivityDTO> findByKeyId(Long apiKeyId) {
        List<ApiKeyActivityEntity> entities = getActivityEntitiesByKeyId(apiKeyId);
        List<ApiKeyActivityDTO> dtos = new ArrayList<ApiKeyActivityDTO>();

        for (ApiKeyActivityEntity entity : entities) {
            ApiKeyActivityDTO dto = new ApiKeyActivityDTO(entity);
            dtos.add(dto);
        }
        return dtos;
    }

    @Override
    public List<ApiKeyActivityDTO> findByKeyId(Long apiKeyId, Integer pageNumber, Integer pageSize) {

        List<ApiKeyActivityEntity> entities = getActivityEntitiesByKeyId(apiKeyId, pageNumber, pageSize);
        List<ApiKeyActivityDTO> dtos = new ArrayList<ApiKeyActivityDTO>();

        for (ApiKeyActivityEntity entity : entities) {
            ApiKeyActivityDTO dto = new ApiKeyActivityDTO(entity);
            dtos.add(dto);
        }
        return dtos;
    }

    private void create(ApiKeyActivityEntity entity) {

        entityManager.persist(entity);
        entityManager.flush();
    }

    private void update(ApiKeyActivityEntity entity) {

        entityManager.merge(entity);
        entityManager.flush();

    }

    public List<ApiKeyActivityEntity> getAllEntities() {

        List<ApiKeyActivityEntity> result = entityManager
                .createQuery("from ApiKeyActivityEntity where (NOT deleted = true) ", ApiKeyActivityEntity.class)
                .getResultList();
        return result;
    }

    private ApiKeyActivityEntity getEntityById(Long entityId) throws EntityRetrievalException {

        ApiKeyActivityEntity entity = null;

        Query query = entityManager.createQuery(
                "from ApiKeyActivityEntity where (NOT deleted = true) AND (id = :entityid) ",
                ApiKeyActivityEntity.class);
        query.setParameter("entityid", entityId);
        List<ApiKeyActivityEntity> result = query.getResultList();

        if (result.size() > 1) {
            throw new EntityRetrievalException("Data error. Duplicate api key id in database.");
        } else if (result.size() == 1) {
            entity = result.get(0);
        }
        return entity;
    }

    private List<ApiKeyActivityEntity> getAllEntities(Integer pageNumber, Integer pageSize) {

        Query query = entityManager.createQuery("from ApiKeyActivityEntity where (NOT deleted = true) ",
                ApiKeyActivityEntity.class);
        query.setMaxResults(pageSize);
        query.setFirstResult(pageNumber * pageSize);
        List<ApiKeyActivityEntity> result = query.getResultList();

        return result;
    }

    private List<ApiKeyActivityEntity> getActivityEntitiesByKeyId(Long keyId) {

        Query query = entityManager.createQuery(
                "from ApiKeyActivityEntity where (NOT deleted = true) AND (api_key_id = :apikeyid) ",
                ApiKeyActivityEntity.class);
        query.setParameter("apikeyid", keyId);
        List<ApiKeyActivityEntity> result = query.getResultList();
        return result;

    }

    private List<ApiKeyActivityEntity> getActivityEntitiesByKeyId(Long keyId, Integer pageNumber, Integer pageSize) {

        Query query = entityManager.createQuery(
                "from ApiKeyActivityEntity where (NOT deleted = true) AND (api_key_id = :apikeyid) ",
                ApiKeyActivityEntity.class);
        query.setParameter("apikeyid", keyId);
        query.setMaxResults(pageSize);
        query.setFirstResult(pageNumber * pageSize);

        List<ApiKeyActivityEntity> result = query.getResultList();
        return result;

    }

    /**
     * Gets a list of ApiKeyActivityDTOs using parameters to filter API keys by
     * creation start & end dates Also sorts results in ascending or descending
     * order Parameters: String apiKeyFilter - String of API key(s) Integer
     * pageNumber - page of the API key Integer pageSize - size of the page
     * boolean dateAscending - true if API key creation date is sorted in
     * ascending order; false if descending Long startDate - Filter out keys
     * before the creation start date Long endDate - Filter out keys after the
     * creation end date Returns: List of ApiKeyActivityDTOs
     */
    @Override
    public List<ApiKeyActivityDTO> getApiKeyActivity(String keyString, Integer pageNumber, Integer pageSize,
            boolean dateAscending, Long startDate, Long endDate) {
        List<ApiKeyActivityEntity> entities = getActivityEntitiesByKeyStringWithFilter(keyString, pageNumber, pageSize,
                dateAscending, startDate, endDate);
        List<ApiKeyActivityDTO> dtos = new ArrayList<ApiKeyActivityDTO>();

        for (ApiKeyActivityEntity entity : entities) {
            ApiKeyActivityDTO dto = new ApiKeyActivityDTO(entity);
            dtos.add(dto);
        }
        return dtos;
    }

    /**
     * Gets activity entities by an API key string. Uses parameters that filter
     * by start and endDate. Parameters: String apiKeyFilter - String of API
     * key(s) Integer pageNumber - page of the API key Integer pageSize - size
     * of the page boolean dateAscending - true if API key creation date is
     * sorted in ascending order; false if descending Long startDate - Filter
     * out keys before the creation start date Long endDate - Filter out keys
     * after the creation end date Returns: List of ApiKeyActivityEntity
     */
    public List<ApiKeyActivityEntity> getActivityEntitiesByKeyStringWithFilter(String apiKeyFilter, Integer pageNumber,
            Integer pageSize, boolean dateAscending, Long startDateMilli, Long endDateMilli) {
        Date startDate = new Date();
        Date endDate = new Date();

        String queryStr = "FROM ApiKeyActivityEntity a " + "WHERE (NOT a.deleted = true) ";
        if (startDateMilli != null) {
            startDate = new Date(startDateMilli);
            queryStr += "AND a.creationDate >= :startDate ";
        }
        if (endDateMilli != null) {
            endDate = new Date(endDateMilli);
            queryStr += "AND a.creationDate <= :endDate ";
        }
        if (apiKeyFilter != null && !apiKeyFilter.isEmpty() && apiKeyFilter.length() > 1) {
            queryStr += "AND a.apiKeyId ";
            if (apiKeyFilter.substring(0, 1).contentEquals("!")) {
                apiKeyFilter = apiKeyFilter.substring(1);
                queryStr += "NOT IN ";
            } else {
                queryStr += "IN ";
            }
            queryStr += "(SELECT id FROM ApiKeyEntity WHERE apiKey = :apiKeyFilter) ";
        }
        queryStr += "ORDER BY a.creationDate ";
        if (dateAscending) {
            queryStr += "ASC";
        } else {
            queryStr += "DESC";
        }

        Query query = entityManager.createQuery(queryStr, ApiKeyActivityEntity.class);
        if (startDateMilli != null) {
            query.setParameter("startDate", startDate);
        }
        if (endDateMilli != null) {
            query.setParameter("endDate", endDate);
        }
        if (apiKeyFilter != null && !apiKeyFilter.isEmpty() && apiKeyFilter.length() > 1) {
            query.setParameter("apiKeyFilter", apiKeyFilter);
        }
        query.setMaxResults(pageSize);
        query.setFirstResult(pageNumber * pageSize);
        List<ApiKeyActivityEntity> result = query.getResultList();
        return result;
    }
}
