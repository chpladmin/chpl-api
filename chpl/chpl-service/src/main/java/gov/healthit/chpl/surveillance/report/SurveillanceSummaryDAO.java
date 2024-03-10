package gov.healthit.chpl.surveillance.report;

import java.time.LocalDate;
import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.domain.surveillance.SurveillanceType;
import gov.healthit.chpl.surveillance.report.domain.SurveillanceSummary;
import lombok.extern.log4j.Log4j2;

@Repository("surveillanceSummaryDao")
@Log4j2
public class SurveillanceSummaryDAO extends BaseDAOImpl {

    public SurveillanceSummary getCountOfListingsSurveilledByType(Long acbId, LocalDate startDate, LocalDate endDate) {
        String queryStr = "SELECT survType.name, COUNT(DISTINCT listing) "
                + "FROM ListingWithSurveillanceEntity listing "
                + "JOIN listing.surveillances surv "
                + "JOIN surv.surveillanceType survType "
                + "WHERE listing.certificationBodyId = :acbId "
                + "AND listing.deleted = false "
                + "AND surv.startDate <= :endDate "
                + "AND (surv.endDate IS NULL OR surv.endDate >= :startDate) "
                + "GROUP BY survType.name";
        Query query = entityManager.createQuery(queryStr);
        query.setParameter("acbId", acbId);
        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate);

        SurveillanceSummary result = new SurveillanceSummary();
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
}
