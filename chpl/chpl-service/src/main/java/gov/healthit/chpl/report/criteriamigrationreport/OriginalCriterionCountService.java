package gov.healthit.chpl.report.criteriamigrationreport;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.util.CertificationStatusUtil;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class OriginalCriterionCountService {
    private CertifiedProductDAO certifiedProductDAO;

    @Autowired
    public OriginalCriterionCountService(CertifiedProductDAO certifiedProductDAO) {
        this.certifiedProductDAO = certifiedProductDAO;
    }

    public Integer generateCountForDate(CriteriaMigrationDefinition cmd, LocalDate reportDate, LocalDate startDate) {
        return calculateCurrentStatistics(cmd, reportDate, startDate);
    }

    private Integer calculateCurrentStatistics(CriteriaMigrationDefinition cmd, LocalDate reportDate, LocalDate startDate) {
        LOGGER.info("Calculating original criteria upgraded to cures statistics for " + reportDate);
        return certifiedProductDAO.getListingIdsAttestingToCriterion(
                cmd.getOriginalCriterion().getId(),
                CertificationStatusUtil.getActiveStatuses()).size();
    }

}
