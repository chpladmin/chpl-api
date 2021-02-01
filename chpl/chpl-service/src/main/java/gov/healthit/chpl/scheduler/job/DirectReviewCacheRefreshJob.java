package gov.healthit.chpl.scheduler.job;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.service.DirectReviewCachingService;

@DisallowConcurrentExecution
public class DirectReviewCacheRefreshJob extends DownloadableResourceCreatorJob {
    private static final Logger LOGGER = LogManager.getLogger("directReviewCacheRefreshJobLogger");

    @Autowired
    private DirectReviewCachingService directReviewService;

    public DirectReviewCacheRefreshJob() throws Exception {
        super(LOGGER);
    }

    @Override
    public void execute(final JobExecutionContext jobContext) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        LOGGER.info("********* Starting the Direct Review Cache Refresh job. *********");
        try {
            directReviewService.populateDirectReviewsCache();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            LOGGER.info("********* Completed the Direct Review Cache Refresh job. *********");
        }
    }
}
