package gov.healthit.chpl.scheduler.job.summarystatistics.data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import gov.healthit.chpl.compliance.surveillance.entity.SurveillanceEntity;
import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.domain.concept.CertificationEditionConcept;
import gov.healthit.chpl.util.DateUtil;
import jakarta.persistence.Query;

@Repository("surveillanceStatisticsDAO")
public class SurveillanceStatisticsDAO extends BaseDAOImpl {
    private List<Long> retiredEditions;

    @Autowired
    public SurveillanceStatisticsDAO() {
        this.retiredEditions = Stream.of(
                CertificationEditionConcept.CERTIFICATION_EDITION_2011.getId(),
                CertificationEditionConcept.CERTIFICATION_EDITION_2014.getId())
                .toList();
    }

    public Long getTotalSurveillanceActivities(Date endDate) {
        String hql = "SELECT count(surv) "
                + "FROM SurveillanceEntity surv "
                + "JOIN surv.certifiedProduct cp "
                + "WHERE cp.certificationEditionId NOT IN (:retiredEditions) ";
        if (endDate == null) {
            hql += " AND surv.deleted = false";
        } else {
            hql += " AND (surv.deleted = false AND surv.startDate <= :endDate) "
                    + " OR (surv.deleted = true AND surv.startDate <= :endDate AND surv.lastModifiedDate > :endDate) ";
        }

        Query query = entityManager.createQuery(hql);
        query.setParameter("retiredEditions", retiredEditions);
        if (endDate != null) {
            query.setParameter("endDate", DateUtil.toLocalDate(endDate.getTime()));
        }
        return (Long) query.getSingleResult();
    }

    public Long getTotalOpenSurveillanceActivities(Date endDate) {
        String hql = "SELECT count(surv) "
                + "FROM SurveillanceEntity surv "
                + "JOIN surv.certifiedProduct cp "
                + "WHERE cp.certificationEditionId NOT IN (:retiredEditions) "
                + "AND surv.startDate <= CURRENT_DATE() "
                + "AND (surv.endDate IS NULL OR surv.endDate >= CURRENT_DATE()) ";
        if (endDate == null) {
            hql += " AND surv.deleted = false";
        } else {
            hql += "AND ((surv.deleted = false AND surv.startDate <= :endDate) "
                    + " OR (surv.deleted = true AND surv.startDate <= :endDate AND surv.lastModifiedDate > :endDate)) ";
        }

        Query query = entityManager.createQuery(hql);
        query.setParameter("retiredEditions", retiredEditions);
        if (endDate != null) {
            query.setParameter("endDate", DateUtil.toLocalDate(endDate.getTime()));
        }
        return (Long) query.getSingleResult();
    }

    public Long getTotalClosedSurveillanceActivities(Date endDate) {
        String hql = "SELECT count(surv) "
                + "FROM SurveillanceEntity surv "
                + "JOIN surv.certifiedProduct cp "
                + "WHERE cp.certificationEditionId NOT IN (:retiredEditions) "
                + "AND surv.startDate <= CURRENT_DATE() "
                + "AND (surv.endDate IS NOT NULL AND surv.endDate <= CURRENT_DATE()) ";
        if (endDate == null) {
            hql += " AND surv.deleted = false";
        } else {
            hql += "AND ((surv.deleted = false AND surv.endDate <= :endDate) "
                    + " OR (surv.deleted = true AND surv.endDate <= :endDate AND surv.lastModifiedDate > :endDate)) ";
        }

        Query query = entityManager.createQuery(hql);
        query.setParameter("retiredEditions", retiredEditions);
        if (endDate != null) {
            query.setParameter("endDate", DateUtil.toLocalDate(endDate.getTime()));
        }
        return (Long) query.getSingleResult();
    }

    public Long getTotalNonConformities(Date endDate) {
        String hql = "SELECT count(nc) "
                + "FROM SurveillanceEntity surv "
                + "JOIN surv.surveilledRequirements reqs "
                + "JOIN reqs.nonconformities nc "
                + "JOIN surv.certifiedProduct cp "
                + "WHERE cp.certificationEditionId NOT IN (:retiredEditions) ";
        if (endDate == null) {
            hql += " AND nc.deleted = false";
        } else {
            hql += "AND ((nc.deleted = false AND nc.dateOfDetermination <= :endDate) "
                    + " OR (nc.deleted = true AND nc.dateOfDetermination <= :endDate AND nc.lastModifiedDate > :endDate)) ";
        }

        Query query = entityManager.createQuery(hql);
        query.setParameter("retiredEditions", retiredEditions);
        if (endDate != null) {
            query.setParameter("endDate", DateUtil.toLocalDate(endDate.getTime()));
        }
        return (Long) query.getSingleResult();
    }

    public Long getTotalOpenNonconformities(Date endDate) {
        String hql = "SELECT count(nc) "
                + "FROM SurveillanceEntity surv "
                + "JOIN surv.surveilledRequirements reqs "
                + "JOIN reqs.nonconformities nc "
                + "JOIN surv.certifiedProduct cp "
                + "WHERE cp.certificationEditionId NOT IN (:retiredEditions) "
                + "AND nc.nonconformityCloseDate IS NULL ";
        if (endDate == null) {
            hql += " AND nc.deleted = false";
        } else {
            hql += " AND ((nc.deleted = false AND nc.dateOfDetermination <= :endDate) "
                    + " OR (nc.deleted = true AND nc.dateOfDetermination <= :endDate AND nc.lastModifiedDate > :endDate)) ";
        }

        Query query = entityManager.createQuery(hql);
        query.setParameter("retiredEditions", retiredEditions);
        if (endDate != null) {
            query.setParameter("endDate", DateUtil.toLocalDate(endDate.getTime()));
        }
        return (Long) query.getSingleResult();
    }

    public List<CertificationBodyStatistic> getTotalOpenNonconformitiesByAcb(Date endDate) {
        String hql = "SELECT cb.name, count(*) "
                + "FROM CertifiedProductEntity cp, "
                + "CertificationBodyEntity cb, "
                + "SurveillanceEntity s, "
                + "SurveillanceRequirementEntity sr, "
                + "SurveillanceNonconformityEntity sn "
                + "WHERE sn.nonconformityCloseDate IS NULL "
                + "AND cp.certificationEditionId NOT IN (:retiredEditions) "
                + "AND cp.certificationBodyId = cb.id "
                + "AND cp.id = s.certifiedProductId "
                + "AND s.id = sr.surveillanceId "
                + "AND sr.id = sn.surveillanceRequirementId ";

        if (endDate == null) {
            hql += "AND sn.deleted = false ";
        } else {
            hql += "AND ((sn.deleted = false AND sn.dateOfDetermination <= :endDate) "
                    + " OR (sn.deleted = true AND sn.dateOfDetermination <= :endDate AND sn.lastModifiedDate > :endDate)) ";
        }
        hql += "GROUP BY name ";
        hql += "ORDER BY cb.name ";

        Query query = entityManager.createQuery(hql);
        query.setParameter("retiredEditions", retiredEditions);
        if (endDate != null) {
            query.setParameter("endDate", DateUtil.toLocalDate(endDate.getTime()));
        }

        List<Object[]> results = query.getResultList();
        List<CertificationBodyStatistic> cbStats = new ArrayList<CertificationBodyStatistic>();
        for (Object[] obj : results) {
            CertificationBodyStatistic stat = new CertificationBodyStatistic();
            stat.setAcbName(obj[0].toString());
            stat.setCount(Long.valueOf(obj[1].toString()));
            cbStats.add(stat);
        }
        return cbStats;
    }

    public Long getTotalClosedNonconformities(Date endDate) {
        String hql = "SELECT count(nc) "
                + "FROM SurveillanceEntity surv "
                + "JOIN surv.surveilledRequirements reqs "
                + "JOIN reqs.nonconformities nc "
                + "JOIN surv.certifiedProduct cp "
                + "WHERE cp.certificationEditionId NOT IN (:retiredEditions) "
                + "AND nc.nonconformityCloseDate IS NOT NULL ";
        if (endDate == null) {
            hql += " AND nc.deleted = false";
        } else {
            hql += " AND ((nc.deleted = false AND nc.nonconformityCloseDate <= :endDate) "
                    + " OR (nc.deleted = true AND nc.nonconformityCloseDate <= :endDate AND nc.lastModifiedDate > :endDate)) ";
        }

        Query query = entityManager.createQuery(hql);
        query.setParameter("retiredEditions", retiredEditions);
        if (endDate != null) {
            query.setParameter("endDate", DateUtil.toLocalDate(endDate.getTime()));
        }
        return (Long) query.getSingleResult();
    }

    public List<CertificationBodyStatistic> getTotalOpenSurveillanceActivitiesByAcb(Date endDate) {
        String hql = "SELECT cb.name, count(*) "
                + "FROM CertifiedProductEntity cp, "
                + "CertificationBodyEntity cb, "
                + "SurveillanceEntity s "
                + "WHERE s.startDate <= CURRENT_DATE() "
                + "AND (s.endDate IS NULL OR s.endDate >= CURRENT_DATE()) "
                + "AND cp.certificationBodyId = cb.id "
                + "AND cp.certificationEditionId NOT IN (:retiredEditions) "
                + "AND cp.id = s.certifiedProductId ";

        if (endDate == null) {
            hql += "AND s.deleted = false ";
        } else {
            hql += "AND ((s.deleted = false AND s.startDate <= :endDate) "
                    + " OR (s.deleted = true AND s.startDate <= :endDate AND s.lastModifiedDate > :endDate)) ";
        }
        hql += "GROUP BY name ";
        hql += "ORDER BY cb.name ";

        Query query = entityManager.createQuery(hql);
        query.setParameter("retiredEditions", retiredEditions);
        if (endDate != null) {
            query.setParameter("endDate", DateUtil.toLocalDate(endDate.getTime()));
        }

        List<Object[]> results = query.getResultList();
        List<CertificationBodyStatistic> cbStats = new ArrayList<CertificationBodyStatistic>();
        for (Object[] obj : results) {
            CertificationBodyStatistic stat = new CertificationBodyStatistic();
            stat.setAcbName(obj[0].toString());
            stat.setCount(Long.valueOf(obj[1].toString()));
            cbStats.add(stat);
        }
        return cbStats;
    }

    public List<SurveillanceEntity> getAllSurveillancesWithNonconformities() {
        String hql = "SELECT surv "
                + "FROM SurveillanceEntity surv "
                + "JOIN FETCH surv.surveilledRequirements reqs "
                + "JOIN FETCH reqs.nonconformities nc "
                + "JOIN FETCH surv.certifiedProduct cp "
                + "WHERE cp.certificationEditionId NOT IN (:retiredEditions) "
                + "AND surv.deleted = false "
                + "AND reqs.deleted = false "
                + "AND nc.deleted = false ";
        Query query = entityManager.createQuery(hql);
        query.setParameter("retiredEditions", retiredEditions);
        return query.getResultList();
    }

    public List<SurveillanceEntity> getAllSurveillances() {
        String hql = "SELECT surv "
                + "FROM SurveillanceEntity surv "
                + "JOIN surv.certifiedProduct cp "
                + "JOIN FETCH surv.surveilledRequirements req "
                + "WHERE cp.certificationEditionId NOT IN (:retiredEditions) "
                + "AND surv.deleted = false ";
        Query query = entityManager.createQuery(hql);
        query.setParameter("retiredEditions", retiredEditions);
        return query.getResultList();
    }
}
