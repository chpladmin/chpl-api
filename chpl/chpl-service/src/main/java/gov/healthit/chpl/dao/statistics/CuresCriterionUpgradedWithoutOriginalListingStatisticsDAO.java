package gov.healthit.chpl.dao.statistics;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.domain.statistics.CuresCriterionUpgradedWithoutOriginalListingStatistic;
import gov.healthit.chpl.entity.statistics.CuresCriterionUpgradedWithoutOriginalListingStatisticEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;

@Repository("curesCriterionUpgradedWithoutOriginalListingStatisticsDAO")
public class CuresCriterionUpgradedWithoutOriginalListingStatisticsDAO extends BaseDAOImpl {
    public List<CuresCriterionUpgradedWithoutOriginalListingStatistic> findAll() {
        List<CuresCriterionUpgradedWithoutOriginalListingStatisticEntity> entities = this.findAllEntities();
        return entities.stream()
                .map(entity -> entity.toDomain())
                .collect(Collectors.toList());
    }

    public LocalDate getDateOfMostRecentStatistics() {
        LocalDate result = null;
        Query query = entityManager.createQuery("SELECT max(statisticDate) "
                + "FROM CuresCriterionUpgradedWithoutOriginalListingStatisticEntity stats "
                + "WHERE (stats.deleted = false) ",
                LocalDate.class);
        Object queryResult = query.getSingleResult();
        if (queryResult instanceof LocalDate) {
            result = (LocalDate) queryResult;
        }
        return result;
    }

    public List<CuresCriterionUpgradedWithoutOriginalListingStatistic> getStatisticsForDate(LocalDate statisticDate) {
        Query query = entityManager.createQuery("SELECT stats "
                + "FROM CuresCriterionUpgradedWithoutOriginalListingStatisticEntity stats "
                + "LEFT OUTER JOIN FETCH stats.certificationCriterion cce "
                + "LEFT OUTER JOIN FETCH cce.certificationEdition "
                + "LEFT OUTER JOIN FETCH cce.rule "
                + "WHERE (stats.deleted = false) "
                + "AND stats.statisticDate = :statisticDate ",
                CuresCriterionUpgradedWithoutOriginalListingStatisticEntity.class);
        query.setParameter("statisticDate", statisticDate);
        List<CuresCriterionUpgradedWithoutOriginalListingStatisticEntity> entities = query.getResultList();
        return entities.stream()
                .map(entity -> entity.toDomain())
                .collect(Collectors.toList());
    }

    public void delete(Long id) throws EntityRetrievalException {
        CuresCriterionUpgradedWithoutOriginalListingStatisticEntity toDelete = getEntityById(id);
        if (toDelete != null) {
            toDelete.setDeleted(true);
            update(toDelete);
        }
    }

    public void create(CuresCriterionUpgradedWithoutOriginalListingStatistic dto)
            throws EntityCreationException, EntityRetrievalException {
        CuresCriterionUpgradedWithoutOriginalListingStatisticEntity entity = new CuresCriterionUpgradedWithoutOriginalListingStatisticEntity();
        entity.setListingCount(dto.getListingsUpgradedWithoutAttestingToOriginalCount());
        entity.setCertificationCriterionId(dto.getCuresCriterion().getId());
        entity.setStatisticDate(dto.getStatisticDate());
        entity.setDeleted(false);

        create(entity);
    }

    private List<CuresCriterionUpgradedWithoutOriginalListingStatisticEntity> findAllEntities() {
        Query query = entityManager.createQuery("SELECT stats "
                + "FROM CuresCriterionUpgradedWithoutOriginalListingStatisticEntity stats "
                + "LEFT OUTER JOIN FETCH stats.certificationCriterion cce "
                + "LEFT OUTER JOIN FETCH cce.certificationEdition "
                + "LEFT OUTER JOIN FETCH cce.rule "
                + "WHERE (stats.deleted = false)",
                CuresCriterionUpgradedWithoutOriginalListingStatisticEntity.class);
        return query.getResultList();
    }

    private CuresCriterionUpgradedWithoutOriginalListingStatisticEntity getEntityById(Long id) throws EntityRetrievalException {
        CuresCriterionUpgradedWithoutOriginalListingStatisticEntity entity = null;
        Query query = entityManager.createQuery("SELECT stats "
                + "FROM CuresCriterionUpgradedWithoutOriginalListingStatisticEntity stats "
                + "LEFT OUTER JOIN FETCH stats.certificationCriterion cce "
                + "LEFT OUTER JOIN FETCH cce.certificationEdition "
                + "LEFT OUTER JOIN FETCH cce.rule "
                + "WHERE (stats.deleted = false) "
                + "AND (stats.id = :id)",
                CuresCriterionUpgradedWithoutOriginalListingStatisticEntity.class);
        query.setParameter("id", id);
        List<CuresCriterionUpgradedWithoutOriginalListingStatisticEntity> result = query.getResultList();

        if (result.size() == 1) {
            entity = result.get(0);
        } else {
            throw new EntityRetrievalException("Data error. Did not find only one entity.");
        }
        return entity;
    }
}
