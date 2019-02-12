package gov.healthit.chpl.caching;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import net.sf.ehcache.CacheManager;

@Component
public class PendingListingCacheUpdater {
    private static final Logger LOGGER = LogManager.getLogger(PendingListingCacheUpdater.class);

    @Autowired
    private PrefetchedCacheLoader prefetchedCaches;

    @Async
    public synchronized void refreshCacheAsync() {
        refreshPendingListingCache();
    }

    public synchronized void refreshCacheSync() {
        refreshPendingListingCache();
    }

    /**
     * Load the pending listing cache into a pre-fetched cache.
     */
    private void refreshPendingListingCache() {
        CacheManager manager = CacheManager.getInstance();
        prefetchedCaches.loadPrefetchedPendingListings();
        LOGGER.debug("Replacing live pending listings cache with pre-fetched data.");
        CacheReplacer.replaceCache(manager.getCache(CacheNames.FIND_PENDING_LISTINGS_BY_ACB_ID),
                manager.getCache(CacheNames.PREFETCHED_FIND_PENDING_LISTINGS_BY_ACB_ID));
    }

    /**
     * Load the cert ids with products cache into a pre-fetched cache.
     */
    private void refreshCertIdsWithProductsCache() {
        CacheManager manager = CacheManager.getInstance();
        prefetchedCaches.loadPrefetchedCertificationIdsWithProducts();
        LOGGER.debug("Replacing live cert ids with products cache with pre-fetched data.");
        CacheReplacer.replaceCache(manager.getCache(CacheNames.ALL_CERT_IDS_WITH_PRODUCTS),
                manager.getCache(CacheNames.PREFETCHED_ALL_CERT_IDS_WITH_PRODUCTS));
    }
}
