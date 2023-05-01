package gov.healthit.chpl.caching;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.log4j.Log4j2;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

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
        Element cacheItem = cacheManager.getCache(CacheNames.COLLECTIONS_SEARCH).get(COLLECTIONS_SEARCH_KEY);
        if (cacheItem == null) {
            Element newItem = new Element(COLLECTIONS_SEARCH_KEY, joinPoint.proceed());
            cacheManager.getCache(CacheNames.COLLECTIONS_SEARCH).put(newItem);
            return newItem.getObjectValue();
        } else {
            return cacheItem.getObjectValue();
        }
    }
}
