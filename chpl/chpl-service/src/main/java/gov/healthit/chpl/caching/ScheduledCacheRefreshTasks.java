package gov.healthit.chpl.caching;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import net.sf.ehcache.CacheManager;

@Component
public class ScheduledCacheRefreshTasks {
	private static final Logger logger = LogManager.getLogger(ScheduledCacheRefreshTasks.class);

	@Autowired private CacheUtil cacheUtil;
	@Autowired private PreFetchedCaches preFetchedCaches;

    @Scheduled(initialDelayString = "${listingCacheRefreshInitialDelayMillis}",
    		fixedDelayString = "${listingCacheRefreshDelayMillis}")
    public void refreshListingsCache() {
    	logger.info("Refreshing listings cache.");
    	CacheManager manager = cacheUtil.getMyCacheManager();
		preFetchedCaches.loadPreFetchedBasicSearch();
		CacheReplacer.replaceCache(manager.getCache(CacheNames.COLLECTIONS_LISTINGS), manager.getCache(CacheNames.COLLECTIONS_PREFETCHED_LISTINGS));
    }
}