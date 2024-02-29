package gov.healthit.chpl.scheduler.job;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.caching.CacheNames;
import gov.healthit.chpl.caching.ListingSearchCacheRefresh;
import gov.healthit.chpl.compliance.directreview.DirectReviewCachingService;
import gov.healthit.chpl.search.CertifiedProductSearchManager;
import lombok.extern.log4j.Log4j2;

@DisallowConcurrentExecution
@Log4j2(topic = "directReviewCacheRefreshJobLogger")
public class DirectReviewCacheRefreshJob extends QuartzJob {
    public static final String JOB_NAME = "directReviewCacheRefresh";
    public static final String JOB_GROUP = "systemJobs";

    @Autowired
    private DirectReviewCachingService directReviewService;

    @Autowired
    private CertifiedProductSearchManager certifiedProductSearchManager;

    @Autowired
    private CacheManager cacheManager;

    @Override
    @ListingSearchCacheRefresh
    public void execute(final JobExecutionContext jobContext) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        LOGGER.info("********* Starting the Direct Review Cache Refresh job. *********");
        try {
            directReviewService.populateDirectReviewsCache(LOGGER);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }

        LOGGER.info("Refreshing searchable listing collection (deprecated)");
        cacheManager.getCache(CacheNames.COLLECTIONS_LISTINGS).invalidate();
        certifiedProductSearchManager.getFlatListingCollection();
        LOGGER.info("Completed refreshing searchable listing collection (deprecated)");

        LOGGER.info("********* Completed the Direct Review Cache Refresh job. *********");
    }
}
