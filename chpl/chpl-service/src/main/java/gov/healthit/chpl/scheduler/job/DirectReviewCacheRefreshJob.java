package gov.healthit.chpl.scheduler.job;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.service.DirectReviewCachingService;
import lombok.extern.log4j.Log4j2;

@DisallowConcurrentExecution
@Log4j2(topic = "directReviewCacheRefreshJobLogger")
public class DirectReviewCacheRefreshJob extends QuartzJob {

    @Autowired
    private DirectReviewCachingService directReviewService;

    @Override
    public void execute(final JobExecutionContext jobContext) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        LOGGER.info("********* Starting the Direct Review Cache Refresh job. *********");
        try {
            directReviewService.populateDirectReviewsCache(LOGGER);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            LOGGER.info("********* Completed the Direct Review Cache Refresh job. *********");
        }
    }
}
