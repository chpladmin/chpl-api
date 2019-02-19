package gov.healthit.chpl.caching.updater;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.caching.CacheNames;
import gov.healthit.chpl.caching.CacheReplacer;
import gov.healthit.chpl.caching.PrefetchedCacheLoader;
import net.sf.ehcache.CacheManager;

@Component
public class DeveloperNamesCacheUpdater {
    private static final Logger LOGGER = LogManager.getLogger(DeveloperNamesCacheUpdater.class);

    @Autowired
    private PrefetchedCacheLoader prefetchedCaches;

    @Async
    public synchronized void refreshCacheAsync() {
        refreshCache();
    }

    public synchronized void refreshCacheSync() {
        refreshCache();
    }

    /**
     * Load the developer names cache into a pre-fetched cache.
     * Once it is loaded replace the live cache with the pre-fetched one.
     */
    private void refreshCache() {
        CacheManager manager = CacheManager.getInstance();
        prefetchedCaches.loadPrefetchedDeveloperNames();
        LOGGER.debug("Replacing live developer names cache with pre-fetched data.");
        CacheReplacer.replaceCache(manager.getCache(CacheNames.DEVELOPER_NAMES),
                manager.getCache(CacheNames.PREFETCHED_DEVELOPER_NAMES));
    }
}
