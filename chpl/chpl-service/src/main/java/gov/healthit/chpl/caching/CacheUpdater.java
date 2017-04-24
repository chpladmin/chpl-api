package gov.healthit.chpl.caching;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;

import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;

@Component
@Aspect
public class CacheUpdater {
	private static final Logger logger = LogManager.getLogger(CacheUpdater.class);
	
	@Autowired private CacheUtil cacheUtil;
	@Autowired private PreFetchedCaches preFetchedCaches;
	
	@After("@annotation(ClearBasicSearch)")
	@Async
	public Future<Boolean> updateBasicSearch() throws IllegalStateException, CacheException, ClassCastException, InterruptedException, ExecutionException{
		CacheManager manager = cacheUtil.getMyCacheManager();
		logger.info("Evicted " + CacheNames.PRE_FETCHED_BASIC_SEARCH);
		preFetchedCaches.initializePreFetchedBasicSearch();
		CacheReplacer.replaceCache(manager.getCache(CacheNames.BASIC_SEARCH), manager.getCache(CacheNames.PRE_FETCHED_BASIC_SEARCH));
		return new AsyncResult<>(true);
	}
}
