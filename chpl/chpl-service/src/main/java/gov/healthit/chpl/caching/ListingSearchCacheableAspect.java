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

    private CacheManager cacheManager;

    @Autowired
    public ListingSearchCacheableAspect(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @Around("@annotation(ListingSearchCacheable)")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        Element cacheItem = cacheManager.getCache(CacheNames.COLLECTIONS_SEARCH).get("ALL_LISTINGS");
        if (cacheItem == null) {
            LOGGER.info("Getting {} from method", CacheNames.COLLECTIONS_SEARCH);
            Element newItem = new Element("ALL_LISTINGS", joinPoint.proceed());
            cacheManager.getCache(CacheNames.COLLECTIONS_SEARCH).put(newItem);
            return newItem.getObjectValue();
        } else {
            //LOGGER.info("Getting {} from cache", CacheNames.COLLECTIONS_SEARCH);
            return cacheItem.getObjectValue();
        }
    }
}
