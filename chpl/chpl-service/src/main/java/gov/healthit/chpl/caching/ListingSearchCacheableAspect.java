package gov.healthit.chpl.caching;

import java.util.Date;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache.ValueWrapper;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import lombok.extern.log4j.Log4j2;

@Component
@Aspect
@Log4j2
public class ListingSearchCacheableAspect {
    private static final String COLLECTIONS_SEARCH_KEY = "collections search";

    private CacheManager cacheManager;

    @Autowired
    public ListingSearchCacheableAspect(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }
    @Around("@annotation(ListingSearchCacheable)")
    public Object getListingSearchCollection(ProceedingJoinPoint joinPoint) throws Throwable {
        Date start = new Date();
        ValueWrapper cacheItem = cacheManager.getCache(CacheNames.COLLECTIONS_SEARCH).get(COLLECTIONS_SEARCH_KEY);
        if (cacheItem == null || cacheItem.get() == null) {
            Object functionValue = joinPoint.proceed();
            cacheManager.getCache(CacheNames.COLLECTIONS_SEARCH).put(COLLECTIONS_SEARCH_KEY, functionValue);
            Date end = new Date();
            LOGGER.info("Time to get Listing Collection no cache: {} ms", (end.getTime() - start.getTime()));
            return functionValue;
        } else {
            Date end = new Date();
            LOGGER.info("Time to get Listing Collection: {} ms", (end.getTime() - start.getTime()));
            return cacheItem.get();
        }
    }
}
