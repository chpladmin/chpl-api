package gov.healthit.chpl.dao.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dao.CriterionProductStatisticsDAO;
import gov.healthit.chpl.dto.CriterionProductStatisticsDTO;
import gov.healthit.chpl.entity.CriterionProductStatisticsEntity;

/**
 * The implementation for CriterionProductStatisticsDAO.
 * @author alarned
 *
 */
@Repository("criterionProductStatisticsDAO")
public class CriterionProductStatisticsDAOImpl extends BaseDAOImpl implements CriterionProductStatisticsDAO {
    private static final long MODIFIED_USER_ID = -3L;

    @Override
    public List<CriterionProductStatisticsDTO> findAll() {
        List<CriterionProductStatisticsEntity> result = this.findAllEntities();
        List<CriterionProductStatisticsDTO> dtos = new ArrayList<CriterionProductStatisticsDTO>(result.size());
        for (CriterionProductStatisticsEntity entity : result) {
            dtos.add(new CriterionProductStatisticsDTO(entity));
        }
        return dtos;
    }

    @Override
    public void delete(final Long id) throws EntityRetrievalException {
        CriterionProductStatisticsEntity toDelete = getEntityById(id);

        if (toDelete != null) {
            toDelete.setDeleted(true);
            toDelete.setLastModifiedUser(getUserId());
            entityManager.merge(toDelete);
        }

    }

    @Override
    public CriterionProductStatisticsEntity create(final CriterionProductStatisticsDTO dto)
            throws EntityCreationException, EntityRetrievalException {
        CriterionProductStatisticsEntity entity = new CriterionProductStatisticsEntity();
        entity.setProductCount(dto.getProductCount());
        entity.setCertificationCriterionId(dto.getCertificationCriterionId());

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

    private List<CriterionProductStatisticsEntity> findAllEntities() {
        Query query = entityManager
                .createQuery("SELECT a from CriterionProductStatisticsEntity a where (NOT a.deleted = true)");
        return query.getResultList();
    }

    private CriterionProductStatisticsEntity getEntityById(final Long id) throws EntityRetrievalException {
        CriterionProductStatisticsEntity entity = null;

        Query query = entityManager.createQuery(
                "from CriterionProductStatisticsEntity a where (NOT deleted = true) AND (id = :entityid) ",
                CriterionProductStatisticsEntity.class);
        query.setParameter("entityid", id);
        List<CriterionProductStatisticsEntity> result = query.getResultList();

        if (result.size() > 1) {
            throw new EntityRetrievalException("Data error. Duplicate id in database.");
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
