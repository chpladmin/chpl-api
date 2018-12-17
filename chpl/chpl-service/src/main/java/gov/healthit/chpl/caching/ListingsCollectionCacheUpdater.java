package gov.healthit.chpl.caching;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import net.sf.ehcache.CacheManager;

@Component
public class ListingsCollectionCacheUpdater {
    private static final Logger LOGGER = LogManager.getLogger(ListingsCollectionCacheUpdater.class);

    @Autowired
    private PreFetchedCaches prefetchedCaches;

    @Async
    public synchronized void refreshCacheAsync() {
        refreshCache();
    }

    public synchronized void refreshCacheSync() {
        refreshCache();
    }

    /**
     * Load the listings collection cache into a pre-fetched cache.
     * Once it is loaded replace the live cache with the pre-fetched one.
     * Method is synchronized so that if multiple actions are happening
     * that trigger the cache refresh it will be refreshed in the order
     * of the actions and always have the most recently updated data.
     * I'm not 100% sure this is necessary?
     */
    private void refreshCache() {
        CacheManager manager = CacheManager.getInstance();
        prefetchedCaches.loadPreFetchedBasicSearch();
        LOGGER.debug("Replacing live listings collection cache with pre-fetched data.");
        CacheReplacer.replaceCache(manager.getCache(CacheNames.COLLECTIONS_LISTINGS),
                manager.getCache(CacheNames.COLLECTIONS_PREFETCHED_LISTINGS));
    }
}
