package gov.healthit.chpl.scheduler.job;

import java.util.List;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

import gov.healthit.chpl.caching.CacheNames;
import gov.healthit.chpl.search.ListingSearchManager;
import gov.healthit.chpl.search.domain.ListingSearchResult;
import lombok.extern.log4j.Log4j2;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

@Log4j2
public class ReplaceListingSearchCacheJob implements Job {

    @Autowired
    private ListingSearchManager listingSearchManager;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        LOGGER.info("Retrieving data for listing search cache.");
        List<ListingSearchResult> allListings = listingSearchManager.getAllListings();
        CacheManager.getInstance().getCache(null).replace(new Element(CacheNames.COLLECTIONS_SEARCH, allListings));
        LOGGER.info("Completed retrieving data for listing search cache.");
    }
}
