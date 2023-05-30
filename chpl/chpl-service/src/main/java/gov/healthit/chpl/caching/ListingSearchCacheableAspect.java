package gov.healthit.chpl.caching;

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
        LOGGER.info("Attempting to get COLLECTIONS_SEARCH");
        ValueWrapper cacheItem = cacheManager.getCache(CacheNames.COLLECTIONS_SEARCH).get(COLLECTIONS_SEARCH_KEY);
        LOGGER.info("Completed getting COLLECTIONS_SEARCH");
        if (cacheItem == null || cacheItem.get() == null) {
            LOGGER.info("COLLECTIONS_SEARCH was null");
            Object functionValue = joinPoint.proceed();
            cacheManager.getCache(CacheNames.COLLECTIONS_SEARCH).put(COLLECTIONS_SEARCH_KEY, functionValue);
            LOGGER.info("COLLECTIONS_SEARCH was added to cache");
            return functionValue;
        } else {
            LOGGER.info("COLLECTIONS_SEARCH was not null");
            return cacheItem.get();
        }
    }
}
