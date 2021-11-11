package gov.healthit.chpl.scheduler.job.onetime.criteriaremoval;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.dao.CertificationCriterionDAO;
import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.scheduler.job.QuartzJob;
import lombok.extern.log4j.Log4j2;
import net.sf.ehcache.CacheManager;

@Log4j2(topic = "removeCriteriaJobLogger")
public class RemoveCriteriaJob extends QuartzJob {

    @Autowired
    private CertificationCriterionDAO certCriteriaDao;

    private static final String[] CRITERIA_TO_REMOVE = {
            "170.315 (a)(10):Drug-Formulary and Preferred Drug List Checks",
            "170.315 (a)(13):Patient-Specific Education Resources",
            "170.315 (e)(2):Secure Messaging"};

    public RemoveCriteriaJob() throws Exception {
        super();
    }

    @Override
    public void execute(JobExecutionContext jobContext) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        LOGGER.info("********* Starting the Remove Criteria job. *********");
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
        LOGGER.info("********* Completed the Remove Criteria job. *********");
    }
}
