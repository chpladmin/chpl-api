package gov.healthit.chpl.scheduler.job;

import java.util.List;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.caching.CacheNames;
import gov.healthit.chpl.search.ListingSearchManager;
import gov.healthit.chpl.search.domain.ListingSearchResult;
import lombok.extern.log4j.Log4j2;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

@Log4j2(topic = "replaceListingSearchCacheJobLogger")
@DisallowConcurrentExecution
public class ReplaceListingSearchCacheJob implements Job {
    public static final String JOB_NAME = "replaceListingSearchCacheJob";
    public static final Long WRITE_LOCK_ACQUIRE_TIMEOUT = 15000L;

    @Autowired
    private ListingSearchManager listingSearchManager;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        LOGGER.info("********* Starting the Replace Listing Search Cache job. *********");
        try {
	        LOGGER.info("Starting retreival of all listings");
	        List<ListingSearchResult> allListings = listingSearchManager.getAllListingsFromDb();
	        LOGGER.info("Completed retreival of all listings");
	        LOGGER.info("Acquiring lock on cache");
	        if (tryToGetWriteLock()) {
		        LOGGER.info("Replacing cache");
		        CacheManager.getInstance().getCache(CacheNames.COLLECTIONS_SEARCH).replace(new Element(CacheNames.COLLECTIONS_SEARCH, allListings));
		        LOGGER.info("Releasing lock on cache");
		        CacheManager.getInstance().getCache(CacheNames.COLLECTIONS_SEARCH).releaseWriteLockOnKey(CacheNames.COLLECTIONS_SEARCH);
	        } else {
	        	LOGGER.info("Could not acquire a write lock on the cache after {}ms", WRITE_LOCK_ACQUIRE_TIMEOUT);	
	        }
        } catch (Exception e) {
        	LOGGER.error("There was an error trying to update the COLLECTION_SEARCH cache.", e);
        }
        LOGGER.info("********* Completed the Replace Listing Search Cache job. *********");
    }
    
    public boolean tryToGetWriteLock() {
    	try {
			return CacheManager.getInstance().getCache(CacheNames.COLLECTIONS_SEARCH).tryWriteLockOnKey(CacheNames.COLLECTIONS_SEARCH, WRITE_LOCK_ACQUIRE_TIMEOUT);
		} catch (Exception e) {
			return false;
		}
    }
}
