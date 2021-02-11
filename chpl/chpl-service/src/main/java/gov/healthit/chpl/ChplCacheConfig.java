package gov.healthit.chpl;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import gov.healthit.chpl.caching.CacheNames;
import gov.healthit.chpl.caching.HttpStatusAwareCache;
import lombok.extern.log4j.Log4j2;
import net.sf.ehcache.Cache;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.CacheConfiguration.TransactionalMode;
import net.sf.ehcache.config.PersistenceConfiguration;
import net.sf.ehcache.config.PersistenceConfiguration.Strategy;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;

@Configuration
@EnableCaching
@Log4j2
public class ChplCacheConfig {
    private static final int MAX_ENTRIES_LOCAL_HEAP = 10000;
    private static final int MAX_ENTRIES_LOCAL_HEAP_LISTING_COLLECTION = 300000;
    private static final int MAX_ENTRIES_LOCAL_DISK = 10000000;
    private static final int DISK_SPOOL_BUFFER_SIZE_MB = 20;
    private static final int SIX_HOURS_IN_SECONDS = 6 * 60 * 60;

    @Bean
    public EhCacheManagerFactoryBean ehCacheCacheManager() {
        EhCacheManagerFactoryBean cmfb = new EhCacheManagerFactoryBean();
        ClassPathResource ehCacheConfigResource = new ClassPathResource("ehcache.xml");
        if (ehCacheConfigResource.exists()) {
            LOGGER.info("Configuring ehcahce using the ehcache.xml configuration file found on classpath.");
            cmfb.setConfigLocation(ehCacheConfigResource);
        }
        cmfb.setShared(true);
        return cmfb;
    }

    @Bean
    public CacheManager cacheManager() {
        EhCacheCacheManager cacheManager = new EhCacheCacheManager(ehCacheCacheManager().getObject());
        net.sf.ehcache.CacheManager backingManager = cacheManager.getCacheManager();
        backingManager.addCacheIfAbsent(createEternalCache(CacheNames.ALL_CERT_IDS));
        backingManager.addCacheIfAbsent(createEternalCache(CacheNames.ALL_CERT_IDS_WITH_PRODUCTS));
        backingManager.addCacheIfAbsent(createEternalCache(CacheNames.ALL_DEVELOPERS));
        backingManager.addCacheIfAbsent(createEternalCache(CacheNames.ALL_DEVELOPERS_INCLUDING_DELETED));
        backingManager.addCacheIfAbsent(createEternalCache(CacheNames.CERTIFICATION_CRITERION_NUMBERS));
        backingManager.addCacheIfAbsent(createEternalCache(CacheNames.CERTIFICATION_CRITERION_WITH_EDITIONS));
        backingManager.addCacheIfAbsent(createEternalCache(CacheNames.CERTIFICATION_STATUSES));
        backingManager.addCacheIfAbsent(createEternalCache(CacheNames.CLASSIFICATION_NAMES));
        backingManager.addCacheIfAbsent(createEternalCache(CacheNames.COLLECTIONS_DEVELOPERS));
        backingManager.addCacheIfAbsent(createEternalCache(CacheNames.COLLECTIONS_LISTINGS));
        backingManager.addCacheIfAbsent(createEternalCache(CacheNames.CQM_CRITERION));
        backingManager.addCacheIfAbsent(createEternalCache(CacheNames.CQM_CRITERION_NUMBERS));
        backingManager.addCacheIfAbsent(createEternalCache(CacheNames.DEVELOPER_NAMES));

        //this looks a little weird to me but it's done according to ehcace documentation
        //so that the decorated cache gets properly initialized
        //https://www.ehcache.org/documentation/2.8/apis/cache-decorators.html#creating-a-decorator
        Ehcache drCache = createEternalCache(CacheNames.DIRECT_REVIEWS);
        backingManager.addCacheIfAbsent(drCache);
        Ehcache decoratedDrCache = createDirectReviewCache(drCache, CacheNames.DIRECT_REVIEWS);
        backingManager.replaceCacheWithDecoratedCache(drCache, decoratedDrCache);

        backingManager.addCacheIfAbsent(createEternalCache(CacheNames.EDITIONS));
        backingManager.addCacheIfAbsent(createEternalCache(CacheNames.EDITION_NAMES));
        backingManager.addCacheIfAbsent(createEternalCache(CacheNames.FIND_SURVEILLANCE_NONCONFORMITY_STATUS_TYPE));
        backingManager.addCacheIfAbsent(createEternalCache(CacheNames.FIND_SURVEILLANCE_REQ_TYPE));
        backingManager.addCacheIfAbsent(createEternalCache(CacheNames.FIND_SURVEILLANCE_RESULT_TYPE));
        backingManager.addCacheIfAbsent(createEternalCache(CacheNames.GET_ALL_UNRESTRICTED_APIKEYS));
        backingManager.addCacheIfAbsent(createEternalCache(CacheNames.GET_DECERTIFIED_DEVELOPERS));
        backingManager.addCacheIfAbsent(createEternalCache(CacheNames.JOB_TYPES));
        backingManager.addCacheIfAbsent(createEternalCache(CacheNames.MEASURES));
        backingManager.addCacheIfAbsent(createEternalCache(CacheNames.MEASURE_TYPES));
        backingManager.addCacheIfAbsent(createEternalCache(CacheNames.PRACTICE_TYPE_NAMES));
        backingManager.addCacheIfAbsent(createEternalCache(CacheNames.PRODUCT_NAMES));
        backingManager.addCacheIfAbsent(createEternalCache(CacheNames.TEST_DATA));
        backingManager.addCacheIfAbsent(createEternalCache(CacheNames.TEST_PROCEDURES));
        backingManager.addCacheIfAbsent(createEternalCache(CacheNames.TEST_FUNCTIONALITY_MAPS));
        backingManager.addCacheIfAbsent(createEternalCache(CacheNames.UPLOAD_TEMPLATE_VERSIONS));

        return cacheManager;
    }

    private Ehcache createDirectReviewCache(Ehcache drCache, String name) {
        //return createCache(name, SIX_HOURS_IN_SECONDS);
        //TODO: not sure if we need to change maxEntriesLocalDisk for this cache.
        //Setting it to 0 could cause us to run out of space IF there were tons of DRs
        //but setting to any other number might not make the cache work in the way we want to use it.
        return new HttpStatusAwareCache(drCache);
    }

    private Ehcache createEternalCache(String name) {
        return createCache(name, 0);
    }

    private Ehcache createCache(String name, long ttl) {
        int maxEntriesLocalHeap = name.equals(CacheNames.COLLECTIONS_LISTINGS)
                ? MAX_ENTRIES_LOCAL_HEAP_LISTING_COLLECTION : MAX_ENTRIES_LOCAL_HEAP;
        Cache cache = new Cache(
                new CacheConfiguration(name, maxEntriesLocalHeap)
                  .memoryStoreEvictionPolicy(MemoryStoreEvictionPolicy.LFU)
                  .eternal(ttl == 0 ? true : false)
                  .timeToIdleSeconds(0)
                  .timeToLiveSeconds(ttl)
                  .maxEntriesLocalDisk(MAX_ENTRIES_LOCAL_DISK)
                  .transactionalMode(TransactionalMode.OFF)
                  .diskSpoolBufferSizeMB(DISK_SPOOL_BUFFER_SIZE_MB)
                  .persistence(new PersistenceConfiguration().strategy(Strategy.LOCALTEMPSWAP)));
        return cache;
    }
}
