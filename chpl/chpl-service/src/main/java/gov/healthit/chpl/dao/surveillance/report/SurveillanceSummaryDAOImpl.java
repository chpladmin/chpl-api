package gov.healthit.chpl.dao.surveillance.report;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.domain.surveillance.SurveillanceType;
import gov.healthit.chpl.dto.CertificationStatusDTO;
import gov.healthit.chpl.dto.SurveillanceTypeDTO;
import gov.healthit.chpl.dto.surveillance.report.QuarterlyReportDTO;
import gov.healthit.chpl.dto.surveillance.report.SurveillanceOutcomeDTO;
import gov.healthit.chpl.dto.surveillance.report.SurveillanceProcessTypeDTO;
import gov.healthit.chpl.dto.surveillance.report.SurveillanceSummaryDTO;
import gov.healthit.chpl.entity.CertificationStatusType;

@Repository("surveillanceSummaryDao")
public class SurveillanceSummaryDAOImpl extends BaseDAOImpl implements SurveillanceSummaryDAO {
    private static final Logger LOGGER = LogManager.getLogger(SurveillanceSummaryDAOImpl.class);

    @Override
    public SurveillanceSummaryDTO getCountOfListingsSurveilledByType(
            final Long acbId, final Date startDate, final Date endDate) {
        String queryStr = "SELECT survType.name, COUNT(DISTINCT listing) "
                + "FROM ListingWithPrivilegedSurveillanceEntity listing "
                + "JOIN listing.surveillances surv "
                + "JOIN surv.surveillanceType survType "
                + "WHERE listing.certificationBodyId = :acbId "
                + "AND listing.deleted = false "
                + "AND surv.deleted = false "
                + "AND surv.startDate <= :endDate "
                + "AND (surv.endDate IS NULL OR surv.endDate >= :startDate) "
                + "GROUP BY survType.name";
        Query query = entityManager.createQuery(queryStr);
        query.setParameter("acbId", acbId);
        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate);

        SurveillanceSummaryDTO result = new SurveillanceSummaryDTO();
        List<Object[]> entities = query.getResultList();
        for (Object[] entity : entities) {
            String survTypeName = (String) entity[0];
            Long count = (Long) entity[1];
            if (survTypeName.equals(SurveillanceType.REACTIVE)) {
                result.setReactiveCount(count);
            } else if (survTypeName.equals(SurveillanceType.RANDOMIZED)) {
                result.setRandomizedCount(count);
            }
        }
        return result;
    }

    @Override
    public SurveillanceSummaryDTO getCountOfSurveillanceProcessTypesBySurveillanceType(final Long acbId,
            final List<SurveillanceProcessTypeDTO> procTypes, final Date startDate, final Date endDate) {
        String queryStr = "SELECT survType.name, COUNT(DISTINCT surv.surveillanceId) "
                + "FROM ListingWithPrivilegedSurveillanceEntity listing "
                + "JOIN listing.surveillances surv "
                + "JOIN surv.surveillanceType survType "
                + "WHERE listing.certificationBodyId = :acbId "
                + "AND listing.deleted = false "
                + "AND surv.surveillanceProcessTypeId IN (:procTypeIds) "
                + "AND surv.startDate <= :endDate "
                + "AND (surv.endDate IS NULL OR surv.endDate >= :startDate) "
                + "GROUP BY survType.name";
        Query query = entityManager.createQuery(queryStr);
        query.setParameter("acbId", acbId);
        List<Long> procTypeIds = new ArrayList<Long>(procTypes.size());
        for (SurveillanceProcessTypeDTO procType : procTypes) {
            procTypeIds.add(procType.getId());
        }
        query.setParameter("procTypeIds", procTypeIds);
        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate);

        SurveillanceSummaryDTO result = new SurveillanceSummaryDTO();
        List<Object[]> entities = query.getResultList();
        for (Object[] entity : entities) {
            String survTypeName = (String) entity[0];
            Long count = (Long) entity[1];
            if (survTypeName.equals(SurveillanceType.REACTIVE)) {
                result.setReactiveCount(count);
            } else if (survTypeName.equals(SurveillanceType.RANDOMIZED)) {
                result.setRandomizedCount(count);
            }
        }
        return result;
    }

    @Override
    public SurveillanceSummaryDTO getCountOfSurveillanceOutcomesBySurveillanceType(final Long acbId,
            final List<SurveillanceOutcomeDTO> outcomes, final Date startDate, final Date endDate) {
        String queryStr = "SELECT survType.name, COUNT(DISTINCT surv.surveillanceId) "
                + "FROM ListingWithPrivilegedSurveillanceEntity listing "
                + "JOIN listing.surveillances surv "
                + "JOIN surv.surveillanceType survType "
                + "WHERE listing.certificationBodyId = :acbId "
                + "AND listing.deleted = false "
                + "AND surv.surveillanceOutcomeId IN (:survOutcomeIds) "
                + "AND surv.startDate <= :endDate "
                + "AND (surv.endDate IS NULL OR surv.endDate >= :startDate) "
                + "GROUP BY survType.name";
        Query query = entityManager.createQuery(queryStr);
        query.setParameter("acbId", acbId);
        List<Long> survOutcomeIds = new ArrayList<Long>(outcomes.size());
        for (SurveillanceOutcomeDTO outcome : outcomes) {
            survOutcomeIds.add(outcome.getId());
        }
        query.setParameter("survOutcomeIds", survOutcomeIds);
        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate);

        SurveillanceSummaryDTO result = new SurveillanceSummaryDTO();
        List<Object[]> entities = query.getResultList();
        for (Object[] entity : entities) {
            String survTypeName = (String) entity[0];
            Long count = (Long) entity[1];
            if (survTypeName.equals(SurveillanceType.REACTIVE)) {
                result.setReactiveCount(count);
            } else if (survTypeName.equals(SurveillanceType.RANDOMIZED)) {
                result.setRandomizedCount(count);
            }
        }
        return result;
    }

    @Override
    public SurveillanceSummaryDTO getCountOfListingStatusBySurveillanceType(final Long acbId,
            final List<CertificationStatusType> statuses, final Date startDate, final Date endDate) {
//        String queryStr = "SELECT survType.name, COUNT(DISTINCT surv.surveillanceId) "
//                + "FROM ListingWithPrivilegedSurveillanceEntity listing "
//                + "JOIN listing.surveillances surv "
//                + "JOIN surv.surveillanceType survType "
//                + "WHERE listing.certificationBodyId = :acbId "
//                + "AND listing.deleted = false "
//                + "AND surv.surveillanceOutcomeId IN (:survOutcomeIds) "
//                + "AND surv.startDate <= :endDate "
//                + "AND (surv.endDate IS NULL OR surv.endDate >= :startDate) "
//                + "GROUP BY survType.name";
//        Query query = entityManager.createQuery(queryStr);
//        query.setParameter("acbId", acbId);
//        List<Long> survOutcomeIds = new ArrayList<Long>(outcomes.size());
//        for (SurveillanceOutcomeDTO outcome : outcomes) {
//            survOutcomeIds.add(outcome.getId());
//        }
//        query.setParameter("survOutcomeIds", survOutcomeIds);
//        query.setParameter("startDate", startDate);
//        query.setParameter("endDate", endDate);

        SurveillanceSummaryDTO result = new SurveillanceSummaryDTO();
//        List<Object[]> entities = query.getResultList();
//        for (Object[] entity : entities) {
//            String survTypeName = (String) entity[0];
//            Long count = (Long) entity[1];
//            if (survTypeName.equals(SurveillanceType.REACTIVE)) {
//                result.setReactiveCount(count);
//            } else if (survTypeName.equals(SurveillanceType.RANDOMIZED)) {
//                result.setRandomizedCount(count);
//            }
//        }
        return result;
    }
}
