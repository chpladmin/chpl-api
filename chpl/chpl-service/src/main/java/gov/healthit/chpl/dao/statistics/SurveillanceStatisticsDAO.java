package gov.healthit.chpl.dao.statistics;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.domain.surveillance.NonconformityClassification;
import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.dto.NonconformityTypeStatisticsDTO;
import gov.healthit.chpl.entity.surveillance.NonconformityTypeEntity;
import gov.healthit.chpl.entity.surveillance.SurveillanceEntity;
import gov.healthit.chpl.entity.surveillance.SurveillanceNonconformityEntity;
import gov.healthit.chpl.scheduler.job.summarystatistics.data.EmailCertificationBodyStatistic;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.util.DateUtil;

@Repository("surveillanceStatisticsDAO")
public class SurveillanceStatisticsDAO extends BaseDAOImpl {

    private CertificationCriterionService certificationCriterionService;

    @Autowired
    public SurveillanceStatisticsDAO(CertificationCriterionService certificationCriterionService) {
        this.certificationCriterionService = certificationCriterionService;
    }

    /**
     * Total # of Surveillance Activities.
     */
    public Long getTotalSurveillanceActivities(Date endDate) {
        String hql = "SELECT count(*) " + "FROM SurveillanceEntity " + "WHERE ";
        if (endDate == null) {
            hql += " deleted = false";
        } else {
            hql += "(deleted = false AND startDate <= :endDate) " + " OR "
                    + "(deleted = true AND startDate <= :endDate AND lastModifiedDate > :endDate) ";
        }

        Query query = entityManager.createQuery(hql);
        if (endDate != null) {
            query.setParameter("endDate", DateUtil.toLocalDate(endDate.getTime()));
        }
        return (Long) query.getSingleResult();
    }

    /**
     * Open Surveillance Activities.
     */
    public Long getTotalOpenSurveillanceActivities(Date endDate) {
        String hql = "SELECT count(*) " + "FROM SurveillanceEntity " + "WHERE startDate <= now() "
                + "AND (endDate IS NULL OR endDate >= now()) ";
        if (endDate == null) {
            hql += " AND deleted = false";
        } else {
            hql += "AND ((deleted = false AND startDate <= :endDate) " + " OR "
                    + "(deleted = true AND startDate <= :endDate AND lastModifiedDate > :endDate)) ";
        }

        Query query = entityManager.createQuery(hql);
        if (endDate != null) {
            query.setParameter("endDate", DateUtil.toLocalDate(endDate.getTime()));
        }
        return (Long) query.getSingleResult();
    }

    /**
     * Closed Surveillance Activities.
     */
    public Long getTotalClosedSurveillanceActivities(Date endDate) {
        String hql = "SELECT count(*) " + "FROM SurveillanceEntity " + "WHERE startDate <= now() "
                + "AND (endDate IS NOT NULL AND endDate <= now()) ";
        if (endDate == null) {
            hql += " AND deleted = false";
        } else {
            hql += "AND ((deleted = false AND endDate <= :endDate) " + " OR "
                    + "(deleted = true AND endDate <= :endDate AND lastModifiedDate > :endDate)) ";
        }

        Query query = entityManager.createQuery(hql);
        if (endDate != null) {
            query.setParameter("endDate", DateUtil.toLocalDate(endDate.getTime()));
        }
        return (Long) query.getSingleResult();
    }

    /**
     * Total # of NCs.
     */
    public Long getTotalNonConformities(Date endDate) {
        String hql = "SELECT count(*) " + "FROM SurveillanceNonconformityEntity " + "WHERE ";
        if (endDate == null) {
            hql += " deleted = false";
        } else {
            hql += "(deleted = false AND dateOfDetermination <= :endDate) "
                    + " OR (deleted = true AND dateOfDetermination <= :endDate AND lastModifiedDate > :endDate) ";
        }

        Query query = entityManager.createQuery(hql);
        if (endDate != null) {
            query.setParameter("endDate", DateUtil.toLocalDate(endDate.getTime()));
        }
        return (Long) query.getSingleResult();
    }

    /**
     * Open NCs.
     */
    public Long getTotalOpenNonconformities(Date endDate) {
        String hql = "SELECT count(*) " + "FROM SurveillanceNonconformityEntity "
                + "WHERE nonconformityCloseDate IS NULL ";
        if (endDate == null) {
            hql += " AND deleted = false";
        } else {
            hql += " AND ((deleted = false AND dateOfDetermination <= :endDate) "
                    + " OR (deleted = true AND dateOfDetermination <= :endDate AND lastModifiedDate > :endDate)) ";
        }

        Query query = entityManager.createQuery(hql);
        if (endDate != null) {
            query.setParameter("endDate", DateUtil.toLocalDate(endDate.getTime()));
        }
        return (Long) query.getSingleResult();
    }

    /**
     * Open NCs By ACB.
     */
    public List<EmailCertificationBodyStatistic> getTotalOpenNonconformitiesByAcb(Date endDate) {
        String hql = "SELECT cb.name, count(*) "
                + "FROM CertifiedProductEntity cp, "
                + "CertificationBodyEntity cb, "
                + "SurveillanceEntity s, "
                + "SurveillanceRequirementEntity sr, "
                + "SurveillanceNonconformityEntity sn "
                + "WHERE sn.nonconformityCloseDate IS NULL "
                + "AND cp.certificationBodyId = cb.id "
                + "AND cp.id = s.certifiedProductId "
                + "AND s.id = sr.surveillanceId "
                + "AND sr.id = sn.surveillanceRequirementId ";

        if (endDate == null) {
            hql += "AND sn.deleted = false ";
        } else {
            hql += "AND ((sn.deleted = false AND sn.dateOfDetermination <= :endDate) "
                    + " OR " + "(sn.deleted = true AND sn.dateOfDetermination <= :endDate AND sn.lastModifiedDate > :endDate)) ";
        }

        hql += "GROUP BY name ";
        hql += "ORDER BY cb.name ";

        Query query = entityManager.createQuery(hql);

        if (endDate != null) {
            query.setParameter("endDate", DateUtil.toLocalDate(endDate.getTime()));
        }

        List<Object[]> results = query.getResultList();
        List<EmailCertificationBodyStatistic> cbStats = new ArrayList<EmailCertificationBodyStatistic>();
        for (Object[] obj : results) {
            EmailCertificationBodyStatistic stat = new EmailCertificationBodyStatistic();
            stat.setAcbName(obj[0].toString());
            stat.setCount(Long.valueOf(obj[1].toString()));
            cbStats.add(stat);
        }
        return cbStats;
    }

    /**
     * Closed NCs.
     */
    public Long getTotalClosedNonconformities(Date endDate) {
        String hql = "SELECT count(*) "
                + "FROM SurveillanceNonconformityEntity "
                + "WHERE nonconformityCloseDate IS NOT NULL ";
        if (endDate == null) {
            hql += " AND deleted = false";
        } else {
            hql += " AND ((deleted = false AND nonconformityCloseDate <= :endDate) "
                    + " OR " + "(deleted = true AND nonconformityCloseDate <= :endDate AND lastModifiedDate > :endDate)) ";
        }

        Query query = entityManager.createQuery(hql);
        if (endDate != null) {
            query.setParameter("endDate", DateUtil.toLocalDate(endDate.getTime()));
        }
        return (Long) query.getSingleResult();
    }

    /**
     * Open Surveillance Activities By ACB.
     */
    public List<EmailCertificationBodyStatistic> getTotalOpenSurveillanceActivitiesByAcb(Date endDate) {
        String hql = "SELECT cb.name, count(*) "
                + "FROM CertifiedProductEntity cp, "
                + "CertificationBodyEntity cb, "
                + "SurveillanceEntity s "
                + "WHERE s.startDate <= now() "
                + "AND (s.endDate IS NULL OR s.endDate >= now()) "
                + "AND cp.certificationBodyId = cb.id "
                + "AND cp.id = s.certifiedProductId ";

        if (endDate == null) {
            hql += "AND s.deleted = false ";
        } else {
            hql += "AND ((s.deleted = false AND s.startDate <= :endDate) "
                    + " OR " + "(s.deleted = true AND s.startDate <= :endDate AND s.lastModifiedDate > :endDate)) ";
        }

        hql += "GROUP BY name ";
        hql += "ORDER BY cb.name ";

        Query query = entityManager.createQuery(hql);

        if (endDate != null) {
            query.setParameter("endDate", DateUtil.toLocalDate(endDate.getTime()));
        }

        List<Object[]> results = query.getResultList();
        List<EmailCertificationBodyStatistic> cbStats = new ArrayList<EmailCertificationBodyStatistic>();
        for (Object[] obj : results) {
            EmailCertificationBodyStatistic stat = new EmailCertificationBodyStatistic();
            stat.setAcbName(obj[0].toString());
            stat.setCount(Long.valueOf(obj[1].toString()));
            cbStats.add(stat);
        }
        return cbStats;
    }

    /**
     * Examine nonconformities to get a count of how many of each type of NC there
     * are.
     *
     * @return a list of the DTOs that hold the counts
     */
    public List<NonconformityTypeStatisticsDTO> getAllNonconformitiesByCriterion() {
        List<SurveillanceNonconformityEntity> allNonconformities = entityManager.createQuery(
                "SELECT sne "
                + "FROM SurveillanceNonconformityEntity sne "
                + "JOIN FETCH sne.type ncType "
                + "WHERE sne.deleted = false ", SurveillanceNonconformityEntity.class)
                .getResultList();

        List<NonconformityTypeEntity> nonconformityTypes = entityManager.createQuery(
                "FROM NonconformityTypeEntity e ", NonconformityTypeEntity.class)
                .getResultList();

        return nonconformityTypes.stream()
            .map(ncType ->
                NonconformityTypeStatisticsDTO.builder()
                        .nonconformityCount(allNonconformities.stream()
                                .filter(nc -> nc.getType().getId().equals(ncType.getId()))
                                .count())
                        .nonconformityType(ncType.getClassification().equals(NonconformityClassification.REQUIREMENT.toString())
                                ? ncType.getTitle()
                                : null)
                        .criterion(ncType.getClassification().equals(NonconformityClassification.CRITERION.toString())
                                ?  new CertificationCriterionDTO(certificationCriterionService.get(ncType.getId()))
                                : null)
                        .build())
            .filter(dto -> !dto.getNonconformityCount().equals(0L))
            .toList();
    }

    public List<SurveillanceEntity> getAllSurveillancesWithNonconformities() {
        String hql = "FROM SurveillanceEntity se "
                + "JOIN FETCH se.surveilledRequirements sre "
                + "JOIN FETCH sre.nonconformities nc "
                + "JOIN FETCH se.certifiedProduct cp "
                + "WHERE se.deleted = false "
                + "AND sre.deleted = false "
                + "AND nc.deleted = false ";
        return entityManager.createQuery(hql, SurveillanceEntity.class)
                .getResultList();
    }

    public List<SurveillanceEntity> getAllSurveillances() {
        String hql = "FROM SurveillanceEntity se "
                + "JOIN FETCH se.surveilledRequirements sre "
                + "WHERE se.deleted = false ";
        return entityManager.createQuery(hql, SurveillanceEntity.class)
                .getResultList();
    }
}
