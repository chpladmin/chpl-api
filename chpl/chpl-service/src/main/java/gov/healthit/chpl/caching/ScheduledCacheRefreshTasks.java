package gov.healthit.chpl.caching;

import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

/**
 * Caching component used to refresh LISTING COLLECTION.
 * @author alarned
 *
 */
@Component
public class ScheduledCacheRefreshTasks {
    private static final Logger LOGGER = LogManager.getLogger(ScheduledCacheRefreshTasks.class);

    @Autowired
    private Environment env;

    @Autowired
    private PreFetchedCaches preFetchedCaches;

    /**
     * Refresh the Listings cache.
     */
    @Scheduled(initialDelayString = "${listingCacheRefreshInitialDelayMillis}",
            fixedDelayString = "${listingCacheRefreshDelayMillis}")
    public void refreshListingsCache() {
        String loadCacheProperty = env.getProperty("enableCacheInitialization");
        boolean loadCache = false;
        if (!StringUtils.isEmpty(loadCacheProperty)) {
            loadCache = Boolean.parseBoolean(loadCacheProperty);
        }

        if (loadCache) {
            LOGGER.info("Refreshing listings cache...");
            LOGGER.info("Getting cache manager.");
            CacheManager manager = CacheManager.getInstance();
            LOGGER.info("Got cache manager: " + manager.getName());
            preFetchedCaches.loadPreFetchedBasicSearch();
            LOGGER.info("Adding cache generation time to prefetched cache.");
            manager.getCache(CacheNames.COLLECTIONS_PREFETCHED_LISTINGS).putQuiet(
                    new Element("CACHE_GENERATION_TIME", (new Date().getTime())));
            LOGGER.info("Replacing listings cache with prefetched cache.");
            CacheReplacer.replaceCache(manager.getCache(CacheNames.COLLECTIONS_LISTINGS),
                    manager.getCache(CacheNames.COLLECTIONS_PREFETCHED_LISTINGS));
        } else {
            LOGGER.info("Not preloading listings cache.");
        }
    }
}
