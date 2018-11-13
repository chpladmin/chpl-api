package gov.healthit.chpl.caching;

import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.dao.search.CertifiedProductSearchDAO;
import gov.healthit.chpl.domain.search.CertifiedProductFlatSearchResult;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

@Component
public class ListingsCollectionCacheUpdater {
    private static final Logger LOGGER = LogManager.getLogger(ListingsCollectionCacheUpdater.class);

    @Autowired
    private PreFetchedCaches prefetchedCaches;

    /**
     * Load the listings collection cache into a pre-fetched cache.
     * Once it is loaded replace the live cache with the pre-fetched one.
     * Method is synchronized so that if multiple actions are happening
     * that trigger the cache refresh it will be refreshed in the order
     * of the actions and always have the most recently updated data.
     * I'm not 100% sure this is necessary?
     */
    public synchronized void refreshCache() {
        CacheManager manager = CacheManager.getInstance();
        prefetchedCaches.loadPreFetchedBasicSearch();
        LOGGER.debug("Adding cache generation time to prefetched cache.");
        manager.getCache(CacheNames.COLLECTIONS_PREFETCHED_LISTINGS).putQuiet(
                new Element("CACHE_GENERATION_TIME", (new Date().getTime())));
        CacheReplacer.replaceCache(manager.getCache(CacheNames.COLLECTIONS_LISTINGS),
                manager.getCache(CacheNames.COLLECTIONS_PREFETCHED_LISTINGS));
    }
}
