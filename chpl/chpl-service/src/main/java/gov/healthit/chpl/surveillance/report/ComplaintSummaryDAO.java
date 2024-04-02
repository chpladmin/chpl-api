package gov.healthit.chpl.surveillance.report;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.surveillance.report.domain.SurveillanceOutcome;

@Repository("complaintSummaryDao")
public class ComplaintSummaryDAO extends BaseDAOImpl {
    private PrivilegedSurveillanceDAO survDao;

    @Autowired
    public ComplaintSummaryDAO(PrivilegedSurveillanceDAO survDao) {
        this.survDao = survDao;
    }

    public Long getTotalComplaints(Long acbId, LocalDate startDate, LocalDate endDate) {
        Query query = entityManager.createQuery("SELECT COUNT(DISTINCT c) "
                + "FROM ComplaintEntity c "
                + "JOIN c.certificationBody "
                + "WHERE c.deleted = false "
                + "AND c.certificationBodyId = :acbId "
                + "AND c.receivedDate <= :endDate "
                + "AND (c.closedDate IS NULL OR c.closedDate >= :startDate)",
                Long.class);
        query.setParameter("acbId", acbId);
        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate);
        Object complaintCount = query.getSingleResult();
        if (complaintCount != null && complaintCount instanceof Long) {
            return (Long) complaintCount;
        }
        return 0L;
    }

    public Long getTotalComplaintsFromOnc(Long acbId, LocalDate startDate, LocalDate endDate) {
        Query query = entityManager.createQuery("SELECT COUNT(DISTINCT c) "
                + "FROM ComplaintEntity c "
                + "JOIN c.certificationBody "
                + "WHERE c.deleted = false "
                + "AND c.certificationBodyId = :acbId "
                + "AND c.oncComplaintId IS NOT NULL "
                + "AND c.receivedDate <= :endDate "
                + "AND (c.closedDate IS NULL OR c.closedDate >= :startDate)",
                Long.class);
        query.setParameter("acbId", acbId);
        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate);
        Object complaintCount = query.getSingleResult();
        if (complaintCount != null && complaintCount instanceof Long) {
            return (Long) complaintCount;
        }
        return 0L;
    }

    public Long getTotalComplaintsResultingInSurveillance(Long acbId, LocalDate startDate, LocalDate endDate) {
        Query query = entityManager.createQuery("SELECT COUNT(DISTINCT c) "
                + "FROM ComplaintEntity c "
                + "JOIN c.certificationBody "
                + "JOIN c.surveillances "
                + "WHERE c.deleted = false "
                + "AND c.certificationBodyId = :acbId "
                + "AND c.receivedDate <= :endDate "
                + "AND (c.closedDate IS NULL OR c.closedDate >= :startDate)",
                Long.class);
        query.setParameter("acbId", acbId);
        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate);
        Object complaintCount = query.getSingleResult();
        if (complaintCount != null && complaintCount instanceof Long) {
            return (Long) complaintCount;
        }
        return 0L;
    }

    public Long getTotalSurveillanceRelatedToComplaints(Long acbId, LocalDate startDate, LocalDate endDate) {
        Query query = entityManager.createQuery("SELECT COUNT(DISTINCT survs) "
                + "FROM ComplaintEntity c "
                + "JOIN c.certificationBody "
                + "JOIN c.surveillances survs "
                + "WHERE c.deleted = false "
                + "AND c.certificationBodyId = :acbId "
                + "AND c.receivedDate <= :endDate "
                + "AND (c.closedDate IS NULL OR c.closedDate >= :startDate)",
                Long.class);
        query.setParameter("acbId", acbId);
        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate);
        Object count = query.getSingleResult();
        if (count != null && count instanceof Long) {
            return (Long) count;
        }
        return 0L;
    }

    public Long getTotalComplaintsResultingInNonconformities(Long acbId, LocalDate startDate, LocalDate endDate) {
        Query query = entityManager.createQuery("SELECT COUNT(DISTINCT c) "
                    + "FROM ComplaintEntity c "
                    + "JOIN c.certificationBody "
                    + "JOIN c.surveillances survs "
                    + "WHERE c.deleted = false "
                    + "AND c.certificationBodyId = :acbId "
                    + "AND c.receivedDate <= :endDate "
                    + "AND (c.closedDate IS NULL OR c.closedDate >= :startDate) "
                    + "AND survs.surveillanceId IN ("
                        + "SELECT DISTINCT qrSurv.surveillanceId "
                        + "FROM QuarterlyReportSurveillanceMapEntity qrSurv "
                        + "WHERE qrSurv.deleted = false "
                        + "AND qrSurv.surveillanceOutcomeId IN (:outcomeIds) "
                + ")",
                Long.class);
        List<SurveillanceOutcome> allOutcomes = survDao.getSurveillanceOutcomes();
        List<Long> relevantOutcomeIds = new ArrayList<Long>();
        for (SurveillanceOutcome outcome : allOutcomes) {
            if (outcome.getName().startsWith("Non-conformity substantiated")) {
                relevantOutcomeIds.add(outcome.getId());
            }
        }
        query.setParameter("outcomeIds", relevantOutcomeIds);
        query.setParameter("acbId", acbId);
        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate);
        Object count = query.getSingleResult();
        if (count != null && count instanceof Long) {
            return (Long) count;
        }
        return 0L;
    }

    public Long getTotalNonconformitiesRelatedToComplaints(Long acbId, LocalDate startDate, LocalDate endDate) {
        Query query = entityManager.createQuery("SELECT COUNT(DISTINCT qrSurv.surveillanceId) "
                + "FROM QuarterlyReportSurveillanceMapEntity qrSurv "
                + "WHERE qrSurv.deleted = false "
                + "AND qrSurv.surveillanceOutcomeId in (:outcomeIds) "
                + "AND qrSurv.surveillanceId in ("
                    + "SELECT DISTINCT survs.surveillanceId "
                    + "FROM ComplaintEntity c "
                    + "JOIN c.certificationBody "
                    + "JOIN c.surveillances survs "
                    + "WHERE c.deleted = false "
                    + "AND c.certificationBodyId = :acbId "
                    + "AND c.receivedDate <= :endDate "
                    + "AND (c.closedDate IS NULL OR c.closedDate >= :startDate)"
                + ")",
                Long.class);
        List<SurveillanceOutcome> allOutcomes = survDao.getSurveillanceOutcomes();
        List<Long> relevantOutcomeIds = new ArrayList<Long>();
        for (SurveillanceOutcome outcome : allOutcomes) {
            if (outcome.getName().startsWith("Non-conformity substantiated")) {
                relevantOutcomeIds.add(outcome.getId());
            }
        }
        query.setParameter("outcomeIds", relevantOutcomeIds);
        query.setParameter("acbId", acbId);
        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate);
        Object count = query.getSingleResult();
        if (count != null && count instanceof Long) {
            return (Long) count;
        }
        return 0L;
    }
}
