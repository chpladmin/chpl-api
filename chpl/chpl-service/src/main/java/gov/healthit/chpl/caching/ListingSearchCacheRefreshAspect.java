package gov.healthit.chpl.caching;

import java.util.List;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.search.ListingSearchManager;
import gov.healthit.chpl.search.domain.ListingSearchResult;
import lombok.extern.log4j.Log4j2;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

@Component
@Aspect
@Log4j2
public class ListingSearchCacheRefreshAspect {

    private CacheManager cacheManager;
    private ListingSearchManager listingSearchManager;
    private enum CacheStatus {
        NONE, REFRESHING
    }

    private CacheStatus status = CacheStatus.NONE;

    @Autowired
    public ListingSearchCacheRefreshAspect(CacheManager cacheManager, ListingSearchManager listingSearchManager) {
        this.cacheManager = cacheManager;
        this.listingSearchManager = listingSearchManager;
    }

    @AfterReturning("@annotation(ListingSearchCacheRefresh)")
    public void listingSearchCacheRefresh(JoinPoint joinPoint) {
        if (status.equals(CacheStatus.REFRESHING)) {
            LOGGER.info("LISTING COLLECTION - ALREADY IN PROCESS - SKIPPING");
            return;
        }
        synchronized(this) {
            status = CacheStatus.REFRESHING;
            LOGGER.info("REFRESHING LISTING COLLECTION - START");
            Object key = cacheManager.getCache(CacheNames.COLLECTIONS_SEARCH).getKeys().get(0);

            Thread thread = new Thread(() -> {
                    LOGGER.info("REFRESHING LISTING COLLECTION IN NEW THREAD");
                    List<ListingSearchResult> results = listingSearchManager.getAllListingsNoCache();
                    cacheManager.getCache(CacheNames.COLLECTIONS_SEARCH).put(new Element(key, results));
                    status = CacheStatus.NONE;
                    LOGGER.info("COMPLETED REFRESHING LISTING COLLECTION IN NEW THREAD");
            });
            thread.start();
        }
    }

}
