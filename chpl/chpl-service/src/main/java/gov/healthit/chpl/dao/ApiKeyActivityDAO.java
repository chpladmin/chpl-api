package gov.healthit.chpl.dao;

import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.dto.ApiKeyActivityDTO;
import gov.healthit.chpl.entity.ApiKeyActivityEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.util.AuthUtil;

@Repository("apiKeyActivityDAO")
public class ApiKeyActivityDAO extends BaseDAOImpl {

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

            create(entity);
        }
        return new ApiKeyActivityDTO(entity);
    }

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

    public void delete(Long id) {
        Query query = entityManager
                .createQuery("UPDATE ApiKeyActivityEntity SET deleted = true WHERE api_activity_id = :entityid");
        query.setParameter("entityid", id);
        query.executeUpdate();
    }

    private void create(ApiKeyActivityEntity entity) {
        entityManager.persist(entity);
        entityManager.flush();
    }

    private void update(ApiKeyActivityEntity entity) {
        entityManager.merge(entity);
        entityManager.flush();
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
}
