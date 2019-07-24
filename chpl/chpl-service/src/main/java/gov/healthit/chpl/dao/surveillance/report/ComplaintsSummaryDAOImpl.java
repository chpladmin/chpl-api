package gov.healthit.chpl.dao.surveillance.report;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.dto.surveillance.report.SurveillanceOutcomeDTO;

@Repository("complaintSummaryDao")
public class ComplaintsSummaryDAOImpl extends BaseDAOImpl implements ComplaintSummaryDAO {
    private static final Logger LOGGER = LogManager.getLogger(ComplaintsSummaryDAOImpl.class);
    private PrivilegedSurveillanceDAO survDao;

    @Autowired
    public ComplaintsSummaryDAOImpl(final PrivilegedSurveillanceDAO survDao) {
        this.survDao = survDao;
    }

    @Override
    public Long getTotalComplaints(final Long acbId, final Date startDate, final Date endDate) {
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

    @Override
    public Long getTotalComplaintsFromOnc(final Long acbId, final Date startDate, final Date endDate) {
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

    @Override
    public Long getTotalComplaintsResultingInSurveillance(final Long acbId, final Date startDate, final Date endDate) {
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

    @Override
    public Long getTotalSurveillanceRelatedToComplaints(final Long acbId, final Date startDate, final Date endDate) {
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

    @Override
    public Long getTotalComplaintsResultingInNonconformities(final Long acbId, final Date startDate, final Date endDate) {
        Query query = entityManager.createQuery("SELECT COUNT(DISTINCT c) "
                    + "FROM ComplaintEntity c "
                    + "JOIN c.certificationBody "
                    + "JOIN c.surveillances survs "
                    + "WHERE c.deleted = false "
                    + "AND c.certificationBodyId = :acbId "
                    + "AND c.receivedDate <= :endDate "
                    + "AND (c.closedDate IS NULL OR c.closedDate >= :startDate) "
                    + "AND survs.id IN ("
                        + "SELECT (DISTINCT ps.surveillanceId) "
                        + "FROM PrivilegedSurveillanceEntity ps "
                        + "WHERE ps.deleted = false "
                        + "AND ps.surveillanceOutcomeId in (:outcomeIds) "
                + ")",
                Long.class);
        List<SurveillanceOutcomeDTO> allOutcomes = survDao.getSurveillanceOutcomes();
        List<Long> relevantOutcomeIds = new ArrayList<Long>();
        for (SurveillanceOutcomeDTO outcome : allOutcomes) {
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

    @Override
    public Long getTotalNonconformitiesRelatedToComplaints(final Long acbId, final Date startDate, final Date endDate) {
        Query query = entityManager.createQuery("SELECT COUNT(DISTINCT ps) "
                + "FROM PrivilegedSurveillanceEntity ps "
                + "WHERE ps.deleted = false "
                + "AND ps.surveillanceOutcomeId in (:outcomeIds) "
                + "AND ps.surveillanceId in ("
                    + "SELECT DISTINCT survs.id "
                    + "FROM ComplaintEntity c "
                    + "JOIN c.certificationBody "
                    + "JOIN c.surveillances survs "
                    + "WHERE c.deleted = false "
                    + "AND c.certificationBodyId = :acbId "
                    + "AND c.receivedDate <= :endDate "
                    + "AND (c.closedDate IS NULL OR c.closedDate >= :startDate)"
                + ")",
                Long.class);
        List<SurveillanceOutcomeDTO> allOutcomes = survDao.getSurveillanceOutcomes();
        List<Long> relevantOutcomeIds = new ArrayList<Long>();
        for (SurveillanceOutcomeDTO outcome : allOutcomes) {
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
