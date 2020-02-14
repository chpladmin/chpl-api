package gov.healthit.chpl.scheduler.job;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ff4j.FF4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.FeatureList;
import gov.healthit.chpl.dao.CertificationCriterionDAO;
import gov.healthit.chpl.dto.CertificationCriterionDTO;
import net.sf.ehcache.CacheManager;

/**
 * The RemoveCriteriaJob sets the removed flag to "true" for the following 2015 criteria:
 * (a)(6), (a)(7), (a)(8), (a)(11), (b)(4), and (b)(5).
 */
public class RemoveCriteriaJob extends QuartzJob {
    private static final Logger LOGGER = LogManager.getLogger("removeCriteriaJobLogger");

    @Autowired
    private CertificationCriterionDAO certCriteriaDao;

    @Autowired
    private FF4j ff4j;

    private static final String[] CRITERIA_TO_REMOVE = {"170.315 (a)(6):Problem List",
            "170.315 (a)(7):Medication List",
            "170.315 (a)(8):Medication Allergy List",
            "170.315 (a)(11):Smoking Status",
            "170.315 (b)(4):Common Clinical Data Set Summary Record - Create",
            "170.315 (b)(5):Common Clinical Data Set Summary Record - Receive"};

    public RemoveCriteriaJob() throws Exception {
        super();
    }

    @Override
    public void execute(final JobExecutionContext jobContext) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        LOGGER.info("********* Starting the Remove Criteria job. *********");
        if (!ff4j.check(FeatureList.EFFECTIVE_RULE_DATE)) {
            LOGGER.info(FeatureList.EFFECTIVE_RULE_DATE + " flag is off. No criteria will be removed.");
        } else {
            for (String criteria : CRITERIA_TO_REMOVE) {
                try {
                    String[] criterionElements = criteria.split(":");
                    CertificationCriterionDTO certDto = certCriteriaDao
                            .getByNumberAndTitle(criterionElements[0], criterionElements[1]);
                    certDto.setRemoved(true);
                    certCriteriaDao.update(certDto);
                    LOGGER.info("Updated criteria " + criteria);
                } catch (final Exception ex) {
                    LOGGER.error("Exception updating criteria " + criteria, ex);
                }
            }
            CacheManager.getInstance().clearAll();
        }
        LOGGER.info("********* Completed the Remove Criteria job. *********");
    }
}
