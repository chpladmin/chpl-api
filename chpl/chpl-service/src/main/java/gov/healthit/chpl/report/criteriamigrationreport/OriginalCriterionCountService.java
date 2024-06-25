package gov.healthit.chpl.report.criteriamigrationreport;

import java.time.LocalDate;

import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.util.CertificationStatusUtil;

@Component
public class OriginalCriterionCountService {
    private CertifiedProductDAO certifiedProductDAO;

    @Autowired
    public OriginalCriterionCountService(CertifiedProductDAO certifiedProductDAO) {
        this.certifiedProductDAO = certifiedProductDAO;
    }

    public Integer generateCountForDate(CriteriaMigrationDefinition cmd, LocalDate reportDate, LocalDate startDate, Logger logger) {
        return calculateCurrentStatistics(cmd, startDate, logger);
    }

    private Integer calculateCurrentStatistics(CriteriaMigrationDefinition cmd, LocalDate startDate, Logger logger) {
        Integer listingCount = certifiedProductDAO.getListingIdsAttestingToCriterion(
                cmd.getOriginalCriterion().getId(),
                CertificationStatusUtil.getActiveStatuses()).size();
        logger.info("Count of listings attesting to {} : {}", cmd.getOriginalCriterion().getNumber(), listingCount);
        return listingCount;
    }

}
