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

    private CacheManager cacheManager;

    @Autowired
    public ListingSearchCacheableAspect(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }
    @Around("@annotation(ListingSearchCacheable)")
    public Object getListingSearchCollection(ProceedingJoinPoint joinPoint) throws Throwable {
        ValueWrapper cacheItem = cacheManager.getCache(CacheNames.COLLECTIONS_SEARCH).get(CacheKeyNames.LISTING_SEARCH_KEY);
        if (cacheItem == null || cacheItem.get() == null) {
            Object functionValue = joinPoint.proceed();
            cacheManager.getCache(CacheNames.COLLECTIONS_SEARCH).put(CacheKeyNames.LISTING_SEARCH_KEY, functionValue);
            return functionValue;
        } else {
            return cacheItem.get();
        }
    }
}
