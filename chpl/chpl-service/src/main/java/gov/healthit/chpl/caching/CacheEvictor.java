package gov.healthit.chpl.caching;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import net.sf.ehcache.CacheManager;

@Component
@Aspect
public class CacheEvictor {
	private static final Logger logger = LogManager.getLogger(CacheEvictor.class);
	
	@Autowired private CacheUtil cacheUtil;
	@Autowired private PreFetchedCaches preFetchedCaches;
	
	@After("@annotation(ClearBasicSearch)")
	@Async
	public void evictPreFetchedBasicSearch(){
		CacheManager manager = cacheUtil.getMyCacheManager();
		logger.debug("Evicted " + CacheNames.PRE_FETCHED_BASIC_SEARCH);
		preFetchedCaches.initializePreFetchedBasicSearch();
		CacheReplacer.replaceCache(manager.getCache(CacheNames.BASIC_SEARCH), manager.getCache(CacheNames.PRE_FETCHED_BASIC_SEARCH));
	}
}
