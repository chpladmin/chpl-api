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
public class CertIdsCacheUpdater {
    private static final Logger LOGGER = LogManager.getLogger(CertIdsCacheUpdater.class);

    @Autowired
    private PrefetchedCacheLoader prefetchedCaches;

    @Async
    public synchronized void refreshCacheAsync() {
        refreshCertIdsCache();
        refreshCertIdsWithProductsCache();
    }

    public synchronized void refreshCacheSync() {
        refreshCertIdsCache();
        refreshCertIdsWithProductsCache();
    }

    /**
     * Load the cert ids cache into a pre-fetched cache.
     */
    private void refreshCertIdsCache() {
        CacheManager manager = CacheManager.getInstance();
        prefetchedCaches.loadPrefetchedCertificationIds();
        LOGGER.debug("Replacing live cert ids cache with pre-fetched data.");
        CacheReplacer.replaceCache(manager.getCache(CacheNames.ALL_CERT_IDS),
                manager.getCache(CacheNames.PREFETCHED_ALL_CERT_IDS));
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
