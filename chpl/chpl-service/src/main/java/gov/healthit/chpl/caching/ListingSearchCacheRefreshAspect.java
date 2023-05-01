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
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

@Component
@Aspect
@Log4j2(topic = "listingSearchCacheRefreshLogger")
public class ListingSearchCacheRefreshAspect {
    private static final String REFRESHING = "refreshing";
    private static final String NEEDS_REFRESHED = "needs refreshed";
    private static final String IDLE = "idle";

    private CacheManager cacheManager;
    private ListingSearchManager listingSearchManager;

    @Autowired
    public ListingSearchCacheRefreshAspect(CacheManager cacheManager, ListingSearchManager listingSearchManager) {
        this.cacheManager = cacheManager;
        this.listingSearchManager = listingSearchManager;
    }

    @AfterReturning("@annotation(ListingSearchCacheRefresh)")
    public void listingSearchCacheRefresh(JoinPoint joinPoint) {

        if (!getCacheRefreshingStatus().equals(IDLE)) {
            LOGGER.info("LISTING COLLECTION - ALREADY IN PROCESS - SKIPPING");
            setCacheRefreshingStatus(NEEDS_REFRESHED);
        } else {
            setCacheRefreshingStatus(REFRESHING);
            LOGGER.info("REFRESHING LISTING COLLECTION - START");
            Object key = cacheManager.getCache(CacheNames.COLLECTIONS_SEARCH).getKeys().get(0);

            Thread thread = new Thread(() -> {
                    while (!getCacheRefreshingStatus().equals(IDLE)) {
                        LOGGER.info("REFRESHING LISTING COLLECTION IN NEW THREAD");
                        List<ListingSearchResult> results = listingSearchManager.getAllListingsNoCache();
                        cacheManager.getCache(CacheNames.COLLECTIONS_SEARCH).put(new Element(key, results));
                        if (getCacheRefreshingStatus().equals(NEEDS_REFRESHED)) {
                            setCacheRefreshingStatus(REFRESHING);
                        } else {
                            setCacheRefreshingStatus(IDLE);
                        }
                        LOGGER.info("COMPLETED REFRESHING LISTING COLLECTION IN NEW THREAD");
                    }
            });
            thread.start();
        }
    }

    private String getCacheRefreshingStatus() {
        Cache cache = cacheManager.getCache(CacheNames.LISTING_SEARCH_CACHE_REFRESH_STATUS);
        if (cache == null) {
            return IDLE;
        } else if (cache.get(CacheNames.LISTING_SEARCH_CACHE_REFRESH_STATUS) == null) {
            setCacheRefreshingStatus(IDLE);
        }
        return (String) cache.get(CacheNames.LISTING_SEARCH_CACHE_REFRESH_STATUS).getObjectValue();
    }

    private void setCacheRefreshingStatus(String status) {
        cacheManager.getCache(CacheNames.LISTING_SEARCH_CACHE_REFRESH_STATUS).put(new Element(CacheNames.LISTING_SEARCH_CACHE_REFRESH_STATUS, status));
    }
}
