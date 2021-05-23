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
import gov.healthit.chpl.dto.statistics.ListingToCriterionForCuresAchievementStatisticDTO;
import gov.healthit.chpl.entity.CertificationStatusType;
import gov.healthit.chpl.entity.statistics.ListingToCriterionForCuresAchievementStatisticEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import lombok.extern.log4j.Log4j2;

@Repository("listingToCriterionForCuresAchievementStatisticsDAO")
@Log4j2
public class ListingToCriterionForCuresAchievementStatisticsDAO extends BaseDAOImpl {
    private List<String> activeStatusNames;

    public ListingToCriterionForCuresAchievementStatisticsDAO() {
        activeStatusNames = Stream.of(CertificationStatusType.Active.getName(),
                CertificationStatusType.SuspendedByAcb.getName(),
                CertificationStatusType.SuspendedByOnc.getName())
                .collect(Collectors.toList());
    }

    public List<Long> getListingIdsWithoutCuresUpdateStatus() {
        String hql = "SELECT DISTINCT listing.id "
                + "FROM CertifiedProductDetailsEntitySimple listing "
                + "WHERE listing.certificationStatusName IN (:statusNames) "
                + "AND listing.curesUpdate = false "
                + "AND listing.deleted = false ";
        Query query = entityManager.createQuery(hql, Long.class);
        query.setParameter("statusNames", activeStatusNames);
        return query.getResultList();
    }

    public List<ListingToCriterionForCuresAchievementStatisticDTO> findAll() {
        List<ListingToCriterionForCuresAchievementStatisticEntity> entities = this.findAllEntities();
        return entities.stream()
                .map(entity -> entity.toDto())
                .collect(Collectors.toList());
    }

    public List<ListingToCriterionForCuresAchievementStatisticDTO> getStatisticsForDate(LocalDate statisticDate) {
        Query query = entityManager.createQuery("SELECT stats "
                + "FROM ListingToCriterionForCuresAchievementStatisticEntity stats "
                + "LEFT OUTER JOIN FETCH stats.certificationCriterion cce "
                + "LEFT OUTER JOIN FETCH cce.certificationEdition "
                + "WHERE (stats.deleted = false) "
                + "AND stats.statisticDate = :statisticDate ",
                ListingToCriterionForCuresAchievementStatisticEntity.class);
        query.setParameter("statisticDate", statisticDate);
        List<ListingToCriterionForCuresAchievementStatisticEntity> entities = query.getResultList();
        return entities.stream()
                .map(entity -> entity.toDto())
                .collect(Collectors.toList());
    }

    public void delete(Long id) throws EntityRetrievalException {
        ListingToCriterionForCuresAchievementStatisticEntity toDelete = getEntityById(id);
        if (toDelete != null) {
            toDelete.setDeleted(true);
            toDelete.setLastModifiedUser(getUserId(User.SYSTEM_USER_ID));
            update(toDelete);
        }
    }

    public void create(ListingToCriterionForCuresAchievementStatisticDTO dto)
            throws EntityCreationException, EntityRetrievalException {
        ListingToCriterionForCuresAchievementStatisticEntity entity = new ListingToCriterionForCuresAchievementStatisticEntity();
        entity.setListingId(dto.getListingId());
        entity.setCertificationCriterionId(dto.getCriterion().getId());
        entity.setStatisticDate(dto.getStatisticDate());
        entity.setLastModifiedUser(getUserId(User.SYSTEM_USER_ID));
        entity.setLastModifiedDate(new Date());
        entity.setCreationDate(new Date());
        entity.setDeleted(false);

        create(entity);
    }

    private List<ListingToCriterionForCuresAchievementStatisticEntity> findAllEntities() {
        Query query = entityManager.createQuery("SELECT stats "
                + "FROM ListingToCriterionForCuresAchievementStatisticEntity stats "
                + "LEFT OUTER JOIN FETCH stats.certificationCriterion cce "
                + "LEFT OUTER JOIN FETCH cce.certificationEdition "
                + "WHERE (stats.deleted = false)",
                ListingToCriterionForCuresAchievementStatisticEntity.class);
        return query.getResultList();
    }

    private ListingToCriterionForCuresAchievementStatisticEntity getEntityById(Long id) throws EntityRetrievalException {
        ListingToCriterionForCuresAchievementStatisticEntity entity = null;
        Query query = entityManager.createQuery("SELECT stats "
                + "FROM ListingToCriterionForCuresAchievementStatisticEntity stats "
                + "LEFT OUTER JOIN FETCH stats.certificationCriterion cce "
                + "LEFT OUTER JOIN FETCH cce.certificationEdition "
                + "WHERE (stats.deleted = false) "
                + "AND (stats.id = :id)",
                ListingToCriterionForCuresAchievementStatisticEntity.class);
        query.setParameter("id", id);
        List<ListingToCriterionForCuresAchievementStatisticEntity> result = query.getResultList();

        if (result.size() == 1) {
            entity = result.get(0);
        } else {
            throw new EntityRetrievalException("Data error. Did not find only one entity.");
        }
        return entity;
    }
}
