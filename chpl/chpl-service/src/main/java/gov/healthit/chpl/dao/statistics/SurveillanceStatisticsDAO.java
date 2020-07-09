package gov.healthit.chpl.dao.statistics;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.domain.DateRange;
import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.dto.NonconformityTypeStatisticsDTO;
import gov.healthit.chpl.entity.surveillance.NonconformityAggregatedStatisticsEntity;
import gov.healthit.chpl.entity.surveillance.SurveillanceEntity;
import gov.healthit.chpl.scheduler.job.summarystatistics.data.EmailCertificationBodyStatistic;

@Repository("surveillanceStatisticsDAO")
public class SurveillanceStatisticsDAO extends BaseDAOImpl {
    /**
     * Total # of Surveillance Activities.
     */
    public Long getTotalSurveillanceActivities(final DateRange dateRange) {
        String hql = "SELECT count(*) " + "FROM SurveillanceEntity " + "WHERE ";
        if (dateRange == null) {
            hql += " deleted = false";
        } else {
            hql += "(deleted = false AND creationDate <= :endDate) " + " OR "
                    + "(deleted = true AND creationDate <= :endDate AND lastModifiedDate > :endDate) ";
        }

        Query query = entityManager.createQuery(hql);
        if (dateRange != null) {
            query.setParameter("endDate", dateRange.getEndDate());
        }
        return (Long) query.getSingleResult();
    }

    /**
     * Open Surveillance Activities.
     */
    public Long getTotalOpenSurveillanceActivities(final DateRange dateRange) {
        String hql = "SELECT count(*) " + "FROM SurveillanceEntity " + "WHERE startDate <= now() "
                + "AND (endDate IS NULL OR endDate >= now()) ";
        if (dateRange == null) {
            hql += " AND deleted = false";
        } else {
            hql += "AND ((deleted = false AND creationDate <= :endDate) " + " OR "
                    + "(deleted = true AND creationDate <= :endDate AND lastModifiedDate > :endDate)) ";
        }

        Query query = entityManager.createQuery(hql);
        if (dateRange != null) {
            query.setParameter("endDate", dateRange.getEndDate());
        }
        return (Long) query.getSingleResult();
    }

    /**
     * Closed Surveillance Activities.
     */
    public Long getTotalClosedSurveillanceActivities(final DateRange dateRange) {
        String hql = "SELECT count(*) " + "FROM SurveillanceEntity " + "WHERE startDate <= now() "
                + "AND (endDate IS NOT NULL AND endDate <= now()) ";
        if (dateRange == null) {
            hql += " AND deleted = false";
        } else {
            hql += "AND ((deleted = false AND creationDate <= :endDate) " + " OR "
                    + "(deleted = true AND creationDate <= :endDate AND lastModifiedDate > :endDate)) ";
        }

        Query query = entityManager.createQuery(hql);
        if (dateRange != null) {
            query.setParameter("endDate", dateRange.getEndDate());
        }
        return (Long) query.getSingleResult();
    }

    /**
     * Total # of NCs.
     */
    public Long getTotalNonConformities(final DateRange dateRange) {
        String hql = "SELECT count(*) " + "FROM SurveillanceNonconformityEntity " + "WHERE ";
        if (dateRange == null) {
            hql += " deleted = false";
        } else {
            hql += "(deleted = false AND creationDate <= :endDate) " + " OR "
                    + "(deleted = true AND creationDate <= :endDate AND lastModifiedDate > :endDate) ";
        }

        Query query = entityManager.createQuery(hql);
        if (dateRange != null) {
            query.setParameter("endDate", dateRange.getEndDate());
        }
        return (Long) query.getSingleResult();
    }

    /**
     * Open NCs.
     */
    public Long getTotalOpenNonconformities(final DateRange dateRange) {
        String hql = "SELECT count(*) " + "FROM SurveillanceNonconformityEntity " + "WHERE nonconformityStatusId = 1 ";
        if (dateRange == null) {
            hql += " AND deleted = false";
        } else {
            hql += " AND ((deleted = false AND creationDate <= :endDate) " + " OR "
                    + "(deleted = true AND creationDate <= :endDate AND lastModifiedDate > :endDate)) ";
        }

        Query query = entityManager.createQuery(hql);
        if (dateRange != null) {
            query.setParameter("endDate", dateRange.getEndDate());
        }
        return (Long) query.getSingleResult();
    }

    /**
     * Open NCs By ACB.
     */
    public List<EmailCertificationBodyStatistic> getTotalOpenNonconformitiesByAcb(final DateRange dateRange) {
        String hql = "SELECT cb.name, count(*) "
                + "FROM CertifiedProductEntity cp, "
                + "CertificationBodyEntity cb, "
                + "SurveillanceEntity s, "
                + "SurveillanceRequirementEntity sr, "
                + "SurveillanceNonconformityEntity sn "
                + "WHERE sn.nonconformityStatusId = 1 "
                + "AND cp.certificationBodyId = cb.id "
                + "AND cp.id = s.certifiedProductId "
                + "AND s.id = sr.surveillanceId "
                + "AND sr.id = sn.surveillanceRequirementId ";

        if (dateRange == null) {
            hql += "AND sn.deleted = false ";
        } else {
            hql += "AND ((sn.deleted = false AND sn.creationDate <= :endDate) " + " OR "
                    + "(sn.deleted = true AND sn.creationDate <= :endDate AND sn.lastModifiedDate > :endDate)) ";
        }

        hql += "GROUP BY name ";
        hql += "ORDER BY cb.name ";

        Query query = entityManager.createQuery(hql);

        if (dateRange != null) {
            query.setParameter("endDate", dateRange.getEndDate());
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
    public Long getTotalClosedNonconformities(final DateRange dateRange) {
        String hql = "SELECT count(*) " + "FROM SurveillanceNonconformityEntity " + "WHERE nonconformityStatusId = 2 ";
        if (dateRange == null) {
            hql += " AND deleted = false";
        } else {
            hql += " AND ((deleted = false AND creationDate <= :endDate) " + " OR "
                    + "(deleted = true AND creationDate <= :endDate AND lastModifiedDate > :endDate)) ";
        }

        Query query = entityManager.createQuery(hql);
        if (dateRange != null) {
            query.setParameter("endDate", dateRange.getEndDate());
        }
        return (Long) query.getSingleResult();
    }

    /**
     * Open Surveillance Activities By ACB.
     */
    public List<EmailCertificationBodyStatistic> getTotalOpenSurveillanceActivitiesByAcb(final DateRange dateRange) {
        String hql = "SELECT cb.name, count(*) "
                + "FROM CertifiedProductEntity cp, "
                + "CertificationBodyEntity cb, "
                + "SurveillanceEntity s "
                + "WHERE s.startDate <= now() "
                + "AND (s.endDate IS NULL OR s.endDate >= now()) "
                + "AND cp.certificationBodyId = cb.id "
                + "AND cp.id = s.certifiedProductId ";

        if (dateRange == null) {
            hql += "AND s.deleted = false ";
        } else {
            hql += "AND ((s.deleted = false AND s.creationDate <= :endDate) " + " OR "
                    + "(s.deleted = true AND s.creationDate <= :endDate AND s.lastModifiedDate > :endDate)) ";
        }

        hql += "GROUP BY name ";
        hql += "ORDER BY cb.name ";

        Query query = entityManager.createQuery(hql);

        if (dateRange != null) {
            query.setParameter("endDate", dateRange.getEndDate());
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
     * Examine nonconformities to get a count of how many of each type of NC there are.
     *
     * @return a list of the DTOs that hold the counts
     */
    public List<NonconformityTypeStatisticsDTO> getAllNonconformitiesByCriterion() {
        Query query = entityManager.createQuery("SELECT data "
                + "FROM NonconformityAggregatedStatisticsEntity data "
                + "LEFT OUTER JOIN FETCH data.certificationCriterionEntity cce "
                + "LEFT OUTER JOIN FETCH cce.certificationEdition ",
                NonconformityAggregatedStatisticsEntity.class);

        List<NonconformityAggregatedStatisticsEntity> entities = query.getResultList();

        List<NonconformityTypeStatisticsDTO> dtos = new ArrayList<NonconformityTypeStatisticsDTO>();
        for (NonconformityAggregatedStatisticsEntity entity : entities) {
            NonconformityTypeStatisticsDTO dto = new NonconformityTypeStatisticsDTO();
            dto.setNonconformityCount(entity.getNonconformityCount());
            dto.setNonconformityType(entity.getNonconformityType());
            if (entity.getCertificationCriterionId() != null && entity.getCertificationCriterionEntity() != null) {
                CertificationCriterionDTO criterion = new CertificationCriterionDTO(entity.getCertificationCriterionEntity());
                dto.setCriterion(criterion);
            }
            dtos.add(dto);
        }

        return dtos;
    }

    public List<SurveillanceEntity> getAllSurveillancesWithNonconformities() {
        String hql = "FROM SurveillanceEntity se "
                + "JOIN FETCH se.surveilledRequirements sre "
                + "JOIN FETCH sre.nonconformities nc "
                + "JOIN FETCH nc.nonconformityStatus ncs "
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
