package gov.healthit.chpl.scheduler.job;

import java.util.List;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.caching.CacheNames;
import gov.healthit.chpl.search.ListingSearchManager;
import gov.healthit.chpl.search.domain.ListingSearchResult;
import lombok.extern.log4j.Log4j2;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

@Log4j2
public class ReplaceListingSearchCacheJob implements Job {
    public static final String JOB_NAME = "replaceListingSearchCacheJob";

    @Autowired
    private ListingSearchManager listingSearchManager;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        LOGGER.info("********* Starting the Replace Listing Search Cache job. *********");
        List<ListingSearchResult> allListings = listingSearchManager.getAllListingsFromDb();
        CacheManager.getInstance().getCache(CacheNames.COLLECTIONS_SEARCH).acquireWriteLockOnKey(CacheNames.COLLECTIONS_SEARCH);
        CacheManager.getInstance().getCache(CacheNames.COLLECTIONS_SEARCH).replace(new Element(CacheNames.COLLECTIONS_SEARCH, allListings));
        CacheManager.getInstance().getCache(CacheNames.COLLECTIONS_SEARCH).releaseWriteLockOnKey(CacheNames.COLLECTIONS_SEARCH);
        LOGGER.info("********* Completed the Replace Listing Search Cache job. *********");
    }
}
