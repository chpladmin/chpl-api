package gov.healthit.chpl.dao;

import java.util.List;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.domain.CriterionProductStatistics;
import gov.healthit.chpl.entity.statistics.CriterionProductStatisticsEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import jakarta.persistence.Query;


@Repository("criterionProductStatisticsDAO")
public class CriterionProductStatisticsDAO extends BaseDAOImpl  {
    public List<CriterionProductStatistics> findAll() {
        return this.findAllEntities().stream()
                .map(entity -> entity.toDomain())
                .toList();
    }

    @Transactional
    public void delete(final Long id) throws EntityRetrievalException {
        CriterionProductStatisticsEntity toDelete = getEntityById(id);

        if (toDelete != null) {
            toDelete.setDeleted(true);
            entityManager.merge(toDelete);
        }
    }

    @Transactional
    public CriterionProductStatisticsEntity create(final CriterionProductStatistics criteirCriterionProductStatistics)
            throws EntityCreationException, EntityRetrievalException {

        CriterionProductStatisticsEntity entity = new CriterionProductStatisticsEntity();
        entity.setProductCount(criteirCriterionProductStatistics.getProductCount());
        entity.setCertificationCriterionId(criteirCriterionProductStatistics.getCertificationCriterionId());

        entityManager.persist(entity);
        entityManager.flush();
        return entity;
    }

    private List<CriterionProductStatisticsEntity> findAllEntities() {
        Query query = entityManager.createQuery("from CriterionProductStatisticsEntity cpse "
                + "LEFT OUTER JOIN FETCH cpse.certificationCriterion cce "
                + "LEFT OUTER JOIN FETCH cce.certificationEdition "
                + "LEFT JOIN FETCH cce.rule "
                + "WHERE (cpse.deleted = false)",
                CriterionProductStatisticsEntity.class);
        return query.getResultList();
    }

    private CriterionProductStatisticsEntity getEntityById(final Long id) throws EntityRetrievalException {
        CriterionProductStatisticsEntity entity = null;

        Query query = entityManager.createQuery("from CriterionProductStatisticsEntity cpse "
                + "LEFT OUTER JOIN FETCH cpse.certificationCriterion cce "
                + "LEFT OUTER JOIN FETCH cce.certificationEdition "
                + "LEFT JOIN FETCH cce.rule "
                + "WHERE (cpse.deleted = false) "
                + "AND (cpse.id = :entityid)",
                CriterionProductStatisticsEntity.class);
        query.setParameter("entityid", id);
        List<CriterionProductStatisticsEntity> result = query.getResultList();

        if (result.size() == 1) {
            entity = result.get(0);
        } else {
            throw new EntityRetrievalException("Data error. Did not find only one entity.");
        }

        return entity;
    }
}
