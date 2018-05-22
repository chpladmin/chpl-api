package gov.healthit.chpl.dao.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dao.ActiveListingsStatisticsDAO;
import gov.healthit.chpl.dto.ActiveListingsStatisticsDTO;
import gov.healthit.chpl.entity.ActiveListingsStatisticsEntity;

/**
 * The implementation for ActiveListingsStatisticsDAO.
 * @author alarned
 *
 */
@Repository("activeListingsStatisticsDAO")
public class ActiveListingsStatisticsDAOImpl extends BaseDAOImpl implements ActiveListingsStatisticsDAO {
    private static final long MODIFIED_USER_ID = -3L;

    @Override
    public List<ActiveListingsStatisticsDTO> findAll() {
        List<ActiveListingsStatisticsEntity> result = this.findAllEntities();
        List<ActiveListingsStatisticsDTO> dtos = new ArrayList<ActiveListingsStatisticsDTO>(result.size());
        for (ActiveListingsStatisticsEntity entity : result) {
            dtos.add(new ActiveListingsStatisticsDTO(entity));
        }
        return dtos;
    }

    @Override
    public void delete(final Long id) throws EntityRetrievalException {
        ActiveListingsStatisticsEntity toDelete = getEntityById(id);

        if (toDelete != null) {
            toDelete.setDeleted(true);
            toDelete.setLastModifiedUser(getUserId());
            entityManager.merge(toDelete);
        }
    }

    @Override
    public ActiveListingsStatisticsEntity create(final ActiveListingsStatisticsDTO dto)
            throws EntityCreationException, EntityRetrievalException {
        ActiveListingsStatisticsEntity entity = new ActiveListingsStatisticsEntity();
        entity.setDeveloperCount(dto.getDeveloperCount());
        entity.setProductCount(dto.getProductCount());
        entity.setCertificationEditionId(dto.getCertificationEditionId());

        if (dto.getDeleted() != null) {
            entity.setDeleted(dto.getDeleted());
        } else {
            entity.setDeleted(false);
        }
        if (dto.getLastModifiedUser() != null) {
            entity.setLastModifiedUser(dto.getLastModifiedUser());
        } else {
            entity.setLastModifiedUser(getUserId());
        }
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

        entityManager.persist(entity);
        entityManager.flush();
        return entity;

    }

    private List<ActiveListingsStatisticsEntity> findAllEntities() {
        Query query = entityManager.createQuery("from ActiveListingsStatisticsEntity alse "
                + "LEFT OUTER JOIN FETCH alse.certificationEdition "
                + "where (alse.deleted = false)",
                ActiveListingsStatisticsEntity.class);
        return query.getResultList();
    }

    private ActiveListingsStatisticsEntity getEntityById(final Long id) throws EntityRetrievalException {
        ActiveListingsStatisticsEntity entity = null;

        Query query = entityManager.createQuery("from ActiveListingsStatisticsEntity alse "
                + "LEFT OUTER JOIN FETCH alse.certificationEdition "
                + "where (alse.deleted = false) AND (alse.id = :entityid) ",
                ActiveListingsStatisticsEntity.class);
        query.setParameter("entityid", id);
        List<ActiveListingsStatisticsEntity> result = query.getResultList();

        if (result.size() > 1) {
            throw new EntityRetrievalException("Data error. Duplicate address id in database.");
        } else if (result.size() == 1) {
            entity = result.get(0);
        }

        return entity;
    }

    private Long getUserId() {
        // If there is no user the current context, assume this is a system
        // process
        if (Util.getCurrentUser() == null || Util.getCurrentUser().getId() == null) {
            return MODIFIED_USER_ID;
        } else {
            return Util.getCurrentUser().getId();
        }
    }
}
