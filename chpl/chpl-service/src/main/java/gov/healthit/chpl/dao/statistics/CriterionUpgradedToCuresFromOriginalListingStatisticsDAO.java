package gov.healthit.chpl.dao.statistics;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.auth.user.User;
import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.dto.statistics.CriterionUpgradedToCuresFromOriginalListingStatisticDTO;
import gov.healthit.chpl.entity.CertificationStatusType;
import gov.healthit.chpl.entity.statistics.CriterionUpgradedToCuresFromOriginalListingStatisticEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;

@Repository("criterionUpgradedToCuresFromOriginalListingStatisticsDAO")
public class CriterionUpgradedToCuresFromOriginalListingStatisticsDAO extends BaseDAOImpl {
    private List<String> activeStatusNames;

    public CriterionUpgradedToCuresFromOriginalListingStatisticsDAO() {
        activeStatusNames = Stream.of(CertificationStatusType.Active.getName(),
                CertificationStatusType.SuspendedByAcb.getName(),
                CertificationStatusType.SuspendedByOnc.getName())
                .collect(Collectors.toList());
    }


    public List<CriterionUpgradedToCuresFromOriginalListingStatisticDTO> findAll() {
        List<CriterionUpgradedToCuresFromOriginalListingStatisticEntity> entities = this.findAllEntities();
        return entities.stream()
                .map(entity -> entity.toDto())
                .collect(Collectors.toList());
    }

    public List<CriterionUpgradedToCuresFromOriginalListingStatisticDTO> getStatisticsForDate(LocalDate statisticDate) {
        Query query = entityManager.createQuery("SELECT stats "
                + "FROM CriterionUpgradedToCuresFromOriginalListingStatisticEntity stats "
                + "LEFT OUTER JOIN FETCH stats.certificationCriterion cce "
                + "LEFT OUTER JOIN FETCH cce.certificationEdition "
                + "WHERE (stats.deleted = false) "
                + "AND stats.statisticDate = :statisticDate ",
                CriterionUpgradedToCuresFromOriginalListingStatisticEntity.class);
        query.setParameter("statisticDate", statisticDate);
        List<CriterionUpgradedToCuresFromOriginalListingStatisticEntity> entities = query.getResultList();
        return entities.stream()
                .map(entity -> entity.toDto())
                .collect(Collectors.toList());
    }

    public void delete(Long id) throws EntityRetrievalException {
        CriterionUpgradedToCuresFromOriginalListingStatisticEntity toDelete = getEntityById(id);
        if (toDelete != null) {
            toDelete.setDeleted(true);
            toDelete.setLastModifiedUser(getUserId(User.SYSTEM_USER_ID));
            update(toDelete);
        }
    }

    public void create(CriterionUpgradedToCuresFromOriginalListingStatisticDTO dto)
            throws EntityCreationException, EntityRetrievalException {
        CriterionUpgradedToCuresFromOriginalListingStatisticEntity entity = new CriterionUpgradedToCuresFromOriginalListingStatisticEntity();
        entity.setListingCount(dto.getListingsUpgradedFromOriginalCount());
        entity.setCertificationCriterionId(dto.getCriterion().getId());
        entity.setStatisticDate(dto.getStatisticDate());
        entity.setLastModifiedUser(getUserId(User.SYSTEM_USER_ID));
        entity.setLastModifiedDate(new Date());
        entity.setCreationDate(new Date());
        entity.setDeleted(false);

        create(entity);
    }

    private List<CriterionUpgradedToCuresFromOriginalListingStatisticEntity> findAllEntities() {
        Query query = entityManager.createQuery("SELECT stats "
                + "FROM CriterionUpgradedToCuresFromOriginalListingStatisticEntity stats "
                + "LEFT OUTER JOIN FETCH stats.certificationCriterion cce "
                + "LEFT OUTER JOIN FETCH cce.certificationEdition "
                + "WHERE (stats.deleted = false)",
                CriterionUpgradedToCuresFromOriginalListingStatisticEntity.class);
        return query.getResultList();
    }

    private CriterionUpgradedToCuresFromOriginalListingStatisticEntity getEntityById(Long id) throws EntityRetrievalException {
        CriterionUpgradedToCuresFromOriginalListingStatisticEntity entity = null;
        Query query = entityManager.createQuery("SELECT stats "
                + "FROM CriterionUpgradedToCuresFromOriginalListingStatisticEntity stats "
                + "LEFT OUTER JOIN FETCH stats.certificationCriterion cce "
                + "LEFT OUTER JOIN FETCH cce.certificationEdition "
                + "WHERE (stats.deleted = false) "
                + "AND (stats.id = :id)",
                CriterionUpgradedToCuresFromOriginalListingStatisticEntity.class);
        query.setParameter("id", id);
        List<CriterionUpgradedToCuresFromOriginalListingStatisticEntity> result = query.getResultList();

        if (result.size() == 1) {
            entity = result.get(0);
        } else {
            throw new EntityRetrievalException("Data error. Did not find only one entity.");
        }
        return entity;
    }
}
