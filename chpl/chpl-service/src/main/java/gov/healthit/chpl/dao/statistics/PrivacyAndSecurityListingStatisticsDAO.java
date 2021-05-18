package gov.healthit.chpl.dao.statistics;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import gov.healthit.chpl.auth.user.User;
import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.dto.statistics.PrivacyAndSecurityListingStatisticDTO;
import gov.healthit.chpl.entity.CertificationStatusType;
import gov.healthit.chpl.entity.statistics.PrivacyAndSecurityListingStatisticEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import lombok.extern.log4j.Log4j2;


@Repository("privacyAndSecurityListingStatisticsDAO")
@Log4j2(topic = "curesStatisticsCreatorJobLogger")
public class PrivacyAndSecurityListingStatisticsDAO extends BaseDAOImpl {
    private List<Long> privacyAndSecurityCriteriaIds;
    private List<Long> privacyAndSecurityRequiredCriteriaIds;
    private List<String> activeStatusNames;

    @Autowired
    public PrivacyAndSecurityListingStatisticsDAO(@Value("${privacyAndSecurityCriteria}") String privacyAndSecurityCriteriaIdList,
            @Value("${privacyAndSecurityRequiredCriteria}") String privacyAndSecurityRequiredCriteriaIdList) {
        if (!StringUtils.isEmpty(privacyAndSecurityCriteriaIdList)) {
            privacyAndSecurityCriteriaIds = Stream.of(privacyAndSecurityCriteriaIdList.split(","))
                .map(criterionId -> new Long(criterionId))
                .collect(Collectors.toList());
        } else {
            LOGGER.error("No value found for privacyAndSecurityCriteria property");
        }

        if (!StringUtils.isEmpty(privacyAndSecurityRequiredCriteriaIdList)) {
            privacyAndSecurityRequiredCriteriaIds = Stream.of(privacyAndSecurityRequiredCriteriaIdList.split(","))
                    .map(criterionId -> new Long(criterionId))
                    .collect(Collectors.toList());
        } else {
            LOGGER.error("No value found for privacyAndSecurityRequiredCriteria property");
        }

        activeStatusNames = Stream.of(CertificationStatusType.Active.getName(),
                CertificationStatusType.SuspendedByAcb.getName(),
                CertificationStatusType.SuspendedByOnc.getName())
                .collect(Collectors.toList());
    }

    public Long getListingCountWithPrivacyAndSecurityCriteria() {
        String hql = "SELECT count(listing.id) "
                + "FROM CertifiedProductDetailsEntitySimple listing, CertificationResultEntity cre "
                + "WHERE listing.id = cre.certifiedProductId "
                + "AND listing.certificationStatusName IN (:statusNames) "
                + "AND cre.deleted = false "
                + "AND cre.certificationCriterionId IN (:criterionIds) "
                + "AND cre.success = true "
                + "AND listing.deleted = false ";
        Query query = entityManager.createQuery(hql);
        query.setParameter("statusNames", activeStatusNames);
        query.setParameter("criterionIds", privacyAndSecurityRequiredCriteriaIds);
        Long result = 0L;
        try {
            result = (Long) query.getSingleResult();
        } catch (NoResultException ex) {
            LOGGER.debug("0 active listings have privacy and security criteria.");
        }
        return result;
    }

    public Long getListingCountRequiringPrivacyAndSecurityCriteria() {
        String hql = "SELECT count(*) FROM "
                + "(SELECT DISTINCT listing.id "
                + "FROM CertifiedProductDetailsEntitySimple listing, CertificationResultEntity cre "
                + "WHERE listing.id = cre.certifiedProductId "
                + "AND listing.certificationStatusName IN (:statusNames) "
                + "AND cre.deleted = false "
                + "AND cre.certificationCriterionId IN (:privacyAndSecurityCriteriaIds) "
                + "AND cre.success = true "
                + "AND listing.deleted = false ) "
                + "INTERSECT "
                + "(SELECT DISTINCT listing.id "
                + "FROM CertifiedProductDetailsEntitySimple listing, CertificationResultEntity cre "
                + "WHERE listing.id = cre.certifiedProductId "
                + "AND listing.certificationStatusName IN (:statusNames) "
                + "AND cre.deleted = false "
                + "AND cre.certificationCriterionId IN (:privacyAndSecurityRequiredCriteriaIds) "
                + "AND cre.success = false "
                + "AND listing.deleted = false ) ";
        Query query = entityManager.createQuery(hql);
        query.setParameter("statusNames", activeStatusNames);
        query.setParameter("privacyAndSecurityCriteriaIds", privacyAndSecurityCriteriaIds);
        query.setParameter("privacyAndSecurityRequiredCriteriaIds", privacyAndSecurityRequiredCriteriaIds);
        Long result = 0L;
        try {
            result = (Long) query.getSingleResult();
        } catch (NoResultException ex) {
            LOGGER.debug("0 active listings require privacy and security criteria.");
        }
        return result;
    }

    public List<PrivacyAndSecurityListingStatisticDTO> findAll() {
        List<PrivacyAndSecurityListingStatisticEntity> entities = this.findAllEntities();
        return entities.stream()
                .map(entity -> entity.toDto())
                .collect(Collectors.toList());
    }

    public List<PrivacyAndSecurityListingStatisticDTO> getStatisticsForDate(LocalDate statisticDate) {
        Query query = entityManager.createQuery("SELECT stats "
                + "FROM PrivacyAndSecurityListingStatisticEntity stats "
                + "WHERE (stats.deleted = false) "
                + "AND stats.statisticDate = :statisticDate ",
                PrivacyAndSecurityListingStatisticEntity.class);
        query.setParameter("statisticDate", statisticDate);
        List<PrivacyAndSecurityListingStatisticEntity> entities = query.getResultList();
        return entities.stream()
                .map(entity -> entity.toDto())
                .collect(Collectors.toList());
    }

    public void delete(Long id) throws EntityRetrievalException {
        PrivacyAndSecurityListingStatisticEntity toDelete = getEntityById(id);
        if (toDelete != null) {
            toDelete.setDeleted(true);
            toDelete.setLastModifiedUser(getUserId(User.SYSTEM_USER_ID));
            update(toDelete);
        }
    }

    public void create(PrivacyAndSecurityListingStatisticDTO dto)
            throws EntityCreationException, EntityRetrievalException {
        PrivacyAndSecurityListingStatisticEntity entity = new PrivacyAndSecurityListingStatisticEntity();
        entity.setListingsRequiringPrivacyAndSecurityCount(dto.getListingsRequiringPrivacyAndSecurityCount());
        entity.setListingsWithPrivacyAndSecurityCount(dto.getListingsWithPrivacyAndSecurityCount());
        entity.setStatisticDate(dto.getStatisticDate());
        entity.setLastModifiedUser(getUserId(User.SYSTEM_USER_ID));
        entity.setLastModifiedDate(new Date());
        entity.setCreationDate(new Date());
        entity.setDeleted(false);

        create(entity);
    }

    private List<PrivacyAndSecurityListingStatisticEntity> findAllEntities() {
        Query query = entityManager.createQuery("SELECT stats "
                + "FROM PrivacyAndSecurityListingStatisticEntity stats "
                + "WHERE (stats.deleted = false)",
                PrivacyAndSecurityListingStatisticEntity.class);
        return query.getResultList();
    }

    private PrivacyAndSecurityListingStatisticEntity getEntityById(Long id) throws EntityRetrievalException {
        PrivacyAndSecurityListingStatisticEntity entity = null;
        Query query = entityManager.createQuery("SELECT stats "
                + "FROM PrivacyAndSecurityListingStatisticEntity stats "
                + "WHERE (stats.deleted = false) "
                + "AND (stats.id = :id)",
                PrivacyAndSecurityListingStatisticEntity.class);
        query.setParameter("id", id);
        List<PrivacyAndSecurityListingStatisticEntity> result = query.getResultList();

        if (result.size() == 1) {
            entity = result.get(0);
        } else {
            throw new EntityRetrievalException("Data error. Did not find only one entity.");
        }
        return entity;
    }
}
