package gov.healthit.chpl.report.criteriamigrationreport;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.activity.history.explorer.CertificationResultActivityHistoryHelper;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.util.CertificationStatusUtil;
import gov.healthit.chpl.util.DateUtil;

@Component
public class OriginalToUpdatedCriterionCountService {

    private CertifiedProductDAO certifiedProductDAO;
    private CertificationResultActivityHistoryHelper activityStatisticsHelper;

    @Autowired
    public OriginalToUpdatedCriterionCountService(CertifiedProductDAO certifiedProductDAO, CertificationResultActivityHistoryHelper activityStatisticsHelper) {
        this.certifiedProductDAO = certifiedProductDAO;
        this.activityStatisticsHelper = activityStatisticsHelper;
    }

    public Integer generateCountForDate(CriteriaMigrationDefinition cmd, LocalDate startDate, Logger logger) {
        return calculateCurrentStatistics(cmd, startDate, logger);
    }

    private Integer calculateCurrentStatistics(CriteriaMigrationDefinition cmd, LocalDate startDate, Logger logger) {
        Date currentDate = new Date();

        Integer listingCount = 0;

        List<Long> listingIdsAttestingToCriterion = certifiedProductDAO.getListingIdsAttestingToCriterion(
                cmd.getUpdatedCriterion().getId(),
                CertificationStatusUtil.getActiveStatuses());

        logger.info("Checking for listing that had {} at one time and now have {}.", cmd.getOriginalCriterion().getNumber(), cmd.getUpdatedCriterion().getNumber());
        for (Long listingId : listingIdsAttestingToCriterion) {
            if (activityStatisticsHelper.didListingRemoveAttestationToCriterionDuringTimeInterval(listingId,
                    cmd.getOriginalCriterion(),
                    DateUtil.toDate(startDate),
                    currentDate)) {
                listingCount++;
            }
        }
        logger.info("Found {} listings meeting above criteria.", listingCount);
        return listingCount;
    }

}
