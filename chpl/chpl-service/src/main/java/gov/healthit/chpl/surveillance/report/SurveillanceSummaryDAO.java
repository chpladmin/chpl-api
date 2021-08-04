package gov.healthit.chpl.surveillance.report;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.domain.surveillance.SurveillanceType;
import gov.healthit.chpl.dto.SurveillanceTypeDTO;
import gov.healthit.chpl.entity.listing.ListingWithPrivilegedSurveillanceEntity;
import gov.healthit.chpl.surveillance.report.dto.QuarterlyReportRelevantListingDTO;
import gov.healthit.chpl.surveillance.report.dto.SurveillanceOutcomeDTO;
import gov.healthit.chpl.surveillance.report.dto.SurveillanceProcessTypeDTO;
import gov.healthit.chpl.surveillance.report.dto.SurveillanceSummaryDTO;
import lombok.extern.log4j.Log4j2;

@Repository("surveillanceSummaryDao")
@Log4j2
public class SurveillanceSummaryDAO extends BaseDAOImpl {

    public SurveillanceSummaryDTO getCountOfListingsSurveilledByType(Long acbId, LocalDate startDate, LocalDate endDate) {
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

    public SurveillanceSummaryDTO getCountOfSurveillanceProcessTypesBySurveillanceType(Long acbId,
            List<SurveillanceProcessTypeDTO> procTypes, LocalDate startDate, LocalDate endDate) {
        String queryStr = "SELECT survType.name, COUNT(DISTINCT surv.id) "
                + "FROM ListingWithPrivilegedSurveillanceEntity listing "
                + "JOIN listing.surveillances surv "
                + "JOIN surv.surveillanceType survType "
                + "JOIN surv.privSurvMap privSurvMap "
                + "WHERE listing.certificationBodyId = :acbId "
                + "AND listing.deleted = false "
                + "AND privSurvMap.surveillanceProcessTypeId IN (:procTypeIds) "
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

    public SurveillanceSummaryDTO getCountOfSurveillanceOutcomesBySurveillanceType(Long acbId,
            List<SurveillanceOutcomeDTO> outcomes, LocalDate startDate, LocalDate endDate) {
        String queryStr = "SELECT survType.name, COUNT(DISTINCT surv.id) "
                + "FROM ListingWithPrivilegedSurveillanceEntity listing "
                + "JOIN listing.surveillances surv "
                + "JOIN surv.surveillanceType survType "
                + "JOIN surv.privSurvMap privSurvMap "
                + "WHERE listing.certificationBodyId = :acbId "
                + "AND listing.deleted = false "
                + "AND privSurvMap.surveillanceOutcomeId IN (:survOutcomeIds) "
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

    public List<QuarterlyReportRelevantListingDTO> getListingsBySurveillanceType(Long acbId, SurveillanceTypeDTO survType,
            LocalDate startDate, LocalDate endDate) {
        String queryStr = "SELECT DISTINCT listing "
                + "FROM ListingWithPrivilegedSurveillanceEntity listing "
                + "JOIN listing.surveillances surv "
                + "JOIN surv.surveillanceType survType "
                + "WHERE listing.certificationBodyId = :acbId "
                + "AND listing.deleted = false "
                + "AND survType.name = :survTypeName "
                + "AND surv.startDate <= :endDate "
                + "AND (surv.endDate IS NULL OR surv.endDate >= :startDate) ";

        //get all of the distinct listings that had randomized or reactive surveillance during the date range
        Query query = entityManager.createQuery(queryStr, ListingWithPrivilegedSurveillanceEntity.class);
        query.setParameter("acbId", acbId);
        query.setParameter("survTypeName", survType.getName());
        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate);
        List<ListingWithPrivilegedSurveillanceEntity> entities = query.getResultList();
        List<QuarterlyReportRelevantListingDTO> result = new ArrayList<QuarterlyReportRelevantListingDTO>();
        for (ListingWithPrivilegedSurveillanceEntity entity : entities) {
            result.add(new QuarterlyReportRelevantListingDTO(entity));
        }
        return result;
    }
}
