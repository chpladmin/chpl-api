package gov.healthit.chpl.scheduler.job;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.dao.CertificationCriterionDAO;
import gov.healthit.chpl.dto.CertificationCriterionDTO;

/**
 * The RemoveCriteriaJob sets the removed flag to "true" for the following 2015 criteria:
 * (a)(6), (a)(7), (a)(8), (a)(10), (a)(11), (a)(13), (b)(4), (b)(5), and (e)(2).
 */
public class RemoveCriteriaJob extends QuartzJob {
    private static final Logger LOGGER = LogManager.getLogger("removeCriteriaJobLogger");

    @Autowired
    private CertificationCriterionDAO certCriteriaDao;

    private static final String[] CRITERIA_TO_REMOVE = {"170.315 (a)(6)", "170.315 (a)(7)",
            "170.315 (a)(8)", "170.315 (a)(10)", "170.315 (a)(11)", "170.315 (a)(13)",
            "170.315 (b)(4)", "170.315 (b)(5)", "170.315 (e)(2)"};

    public RemoveCriteriaJob() throws Exception {
        super();
    }

    @Override
    public void execute(final JobExecutionContext jobContext) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        LOGGER.info("********* Starting the Remove Criteria job. *********");

        for (String criteria : CRITERIA_TO_REMOVE) {
            try {
                CertificationCriterionDTO certDto = certCriteriaDao.getByName(criteria);
                certDto.setRemoved(true);
                certCriteriaDao.update(certDto);
                LOGGER.info("Updated criteria " + criteria);
            } catch (final Exception ex) {
                LOGGER.error("Exception updating criteria " + criteria, ex);
            }
        }
        LOGGER.info("********* Completed the Remove Criteria job. *********");
    }
}
