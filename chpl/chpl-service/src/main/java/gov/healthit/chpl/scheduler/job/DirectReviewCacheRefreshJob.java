package gov.healthit.chpl.scheduler.job;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.caching.CacheNames;
import gov.healthit.chpl.search.CertifiedProductSearchManager;
import gov.healthit.chpl.search.ListingSearchManager;
import gov.healthit.chpl.service.DirectReviewCachingService;
import lombok.extern.log4j.Log4j2;
import net.sf.ehcache.CacheManager;

@DisallowConcurrentExecution
@Log4j2(topic = "directReviewCacheRefreshJobLogger")
public class DirectReviewCacheRefreshJob extends QuartzJob {

    @Autowired
    private DirectReviewCachingService directReviewService;

    @Autowired
    private CertifiedProductSearchManager certifiedProductSearchManager;

    @Autowired
    private ListingSearchManager listingSearchManager;

    @Override
    public void execute(final JobExecutionContext jobContext) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        LOGGER.info("********* Starting the Direct Review Cache Refresh job. *********");
        try {
            directReviewService.populateDirectReviewsCache(LOGGER);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }

        LOGGER.info("Refreshing searchable listing data");
        CacheManager.getInstance().getCache(CacheNames.COLLECTIONS_SEARCH).removeAll();
        listingSearchManager.getAllListings();
        LOGGER.info("Completed refreshing searchable listing data");

        LOGGER.info("Refreshing searchable listing collection (deprecated)");
        CacheManager.getInstance().getCache(CacheNames.COLLECTIONS_LISTINGS).removeAll();
        certifiedProductSearchManager.getFlatListingCollection();
        LOGGER.info("Completed refreshing searchable listing collection (deprecated)");

        LOGGER.info("********* Completed the Direct Review Cache Refresh job. *********");
    }
}
