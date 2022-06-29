package gov.healthit.chpl.scheduler.job.onetime.criteriaremoval;

import org.ff4j.FF4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.FeatureList;
import gov.healthit.chpl.dao.CertificationCriterionDAO;
import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.scheduler.job.QuartzJob;
import lombok.extern.log4j.Log4j2;
import net.sf.ehcache.CacheManager;

@Log4j2(topic = "removeCriteriaJobLogger")
public class RemoveCriteriaJob extends QuartzJob {

    @Autowired
    private CertificationCriterionDAO certCriteriaDao;

    @Autowired
    private FF4j ff4j;

    private static final String[] CRITERIA_TO_REMOVE = {
            "170.315 (b)(1):Transitions of Care",
            "170.315 (b)(2):Clinical Information Reconciliation and Incorporation",
            "170.315 (b)(3):Electronic Prescribing",
            "170.315 (b)(7):Data Segmentation for Privacy - Send",
            "170.315 (b)(8):Data Segmentation for Privacy - Receive",
            "170.315 (b)(9):Care Plan",
            "170.315 (c)(3):Clinical Quality Measures - Report",
            "170.315 (d)(2):Auditable Events and Tamper-Resistance",
            "170.315 (d)(3):Audit Report(s)",
            "170.315 (d)(10):Auditing Actions on Health Information",
            "170.315 (e)(1):View, Download, and Transmit to 3rd Party",
            "170.315 (f)(5):Transmission to Public Health Agencies - Electronic Case Reporting",
            "170.315 (g)(6):Consolidated CDA Creation",
            "170.315 (g)(8):Application Access - Data Category Request",
            "170.315 (g)(9):Application Access - All Data Request"};


    public RemoveCriteriaJob() throws Exception {
        super();
    }

    @Override
    public void execute(JobExecutionContext jobContext) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        LOGGER.info("********* Starting the Remove Criteria job. *********");

        if (ff4j.check(FeatureList.ERD_PHASE_2)) {
            for (String criteria : CRITERIA_TO_REMOVE) {
                try {
                    String[] criterionElements = criteria.split(":");
                    CertificationCriterionDTO certDto = certCriteriaDao.getByNumberAndTitle(criterionElements[0], criterionElements[1]);
                    certDto.setRemoved(true);
                    certCriteriaDao.update(certDto);
                    LOGGER.info("Updated criteria " + criteria);
                } catch (final Exception ex) {
                    LOGGER.error("Exception updating criteria " + criteria, ex);
                }
            }
            CacheManager.getInstance().clearAll();
        } else {
            LOGGER.info("Could not run job - 'erd-phase-2' flag is not on.");
        }
        LOGGER.info("********* Completed the Remove Criteria job. *********");
    }
}
