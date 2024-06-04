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
        return calculateCurrentStatistics(cmd, reportDate, startDate, logger);
    }

    private Integer calculateCurrentStatistics(CriteriaMigrationDefinition cmd, LocalDate reportDate, LocalDate startDate, Logger logger) {
        logger.info("Calculating original criteria upgraded to cures statistics for " + reportDate);
        return certifiedProductDAO.getListingIdsAttestingToCriterion(
                cmd.getOriginalCriterion().getId(),
                CertificationStatusUtil.getActiveStatuses()).size();
    }

}
