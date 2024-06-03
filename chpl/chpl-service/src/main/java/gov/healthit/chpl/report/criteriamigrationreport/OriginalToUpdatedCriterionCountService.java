package gov.healthit.chpl.report.criteriamigrationreport;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.scheduler.job.curesStatistics.CertificationResultActivityHistoryHelper;
import gov.healthit.chpl.util.CertificationStatusUtil;
import gov.healthit.chpl.util.DateUtil;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class OriginalToUpdatedCriterionCountService {

    private CertifiedProductDAO certifiedProductDAO;
    private CertificationResultActivityHistoryHelper activityStatisticsHelper;

    @Autowired
    public OriginalToUpdatedCriterionCountService(CertifiedProductDAO certifiedProductDAO, CertificationResultActivityHistoryHelper activityStatisticsHelper) {
        this.certifiedProductDAO = certifiedProductDAO;
        this.activityStatisticsHelper = activityStatisticsHelper;
    }

    public Integer generateCountForDate(CriteriaMigrationDefinition cmd, LocalDate reportDate, LocalDate startDate) {
        return calculateCurrentStatistics(cmd, reportDate, startDate);
    }

    private Integer calculateCurrentStatistics(CriteriaMigrationDefinition cmd, LocalDate reportDate, LocalDate startDate) {
        LOGGER.info("Calculating original criteria upgraded to cures statistics for " + reportDate);
        Date currentDate = new Date();

        Integer listingCount = 0;

        List<Long> listingIdsAttestingToCriterion = certifiedProductDAO.getListingIdsAttestingToCriterion(
                cmd.getUpdatedCriterion().getId(),
                CertificationStatusUtil.getActiveStatuses());

        if (!CollectionUtils.isEmpty(listingIdsAttestingToCriterion)) {
            LOGGER.info("Listing IDs attesting to criterion ID " + cmd.getUpdatedCriterion().getId() + ": "
                    + listingIdsAttestingToCriterion.stream()
                        .map(listingId -> listingId.toString())
                        .collect(Collectors.joining(",")));
        } else {
            LOGGER.info("No listings attest to criterion ID " + cmd.getUpdatedCriterion().getId());
        }

        for (Long listingId : listingIdsAttestingToCriterion) {
            LOGGER.info("Checking if listing ID " + listingId
                    + " removed attestation of original criterion ID " + cmd.getOriginalCriterion().getId()
                    + " between " + startDate + " and " + currentDate);
            if (activityStatisticsHelper.didListingRemoveAttestationToCriterionDuringTimeInterval(listingId,
                    cmd.getOriginalCriterion(),
                    DateUtil.toDate(startDate),
                    currentDate)) {
                listingCount++;
            }
        }

        return listingCount;
    }

}
