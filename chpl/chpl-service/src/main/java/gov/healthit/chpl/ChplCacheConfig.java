package gov.healthit.chpl;

import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import gov.healthit.chpl.caching.CacheNames;
import net.sf.ehcache.Cache;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.CacheConfiguration.TransactionalMode;
import net.sf.ehcache.config.PersistenceConfiguration;
import net.sf.ehcache.config.PersistenceConfiguration.Strategy;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;
import org.springframework.core.io.ClassPathResource;

@Configuration
@EnableCaching
@Log4j2
public class ChplCacheConfig {
    private static final int MAX_ENTRIES_LOCAL_HEAP = 10000;
    private static final int MAX_ENTRIES_LOCAL_HEAP_LISTING_COLLECTION = 300000;
    private static final int MAX_ENTRIES_LOCAL_DISK = 10000000;
    private static final int DISK_SPOOL_BUFFER_SIZE_MB = 20;

    @Bean
    public EhCacheManagerFactoryBean ehCacheCacheManager() {
        EhCacheManagerFactoryBean cmfb = new EhCacheManagerFactoryBean();
        ClassPathResource ehCacheConfigResource = new ClassPathResource("ehcache.xml");
        if(ehCacheConfigResource.exists()){
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
        backingManager.addCacheIfAbsent(createCache(CacheNames.ALL_CERT_IDS));
        backingManager.addCacheIfAbsent(createCache(CacheNames.ALL_CERT_IDS_WITH_PRODUCTS));
        backingManager.addCacheIfAbsent(createCache(CacheNames.ALL_DEVELOPERS));
        backingManager.addCacheIfAbsent(createCache(CacheNames.ALL_DEVELOPERS_INCLUDING_DELETED));
        backingManager.addCacheIfAbsent(createCache(CacheNames.CERTIFICATION_CRITERION_NUMBERS));
        backingManager.addCacheIfAbsent(createCache(CacheNames.CERTIFICATION_CRITERION_WITH_EDITIONS));
        backingManager.addCacheIfAbsent(createCache(CacheNames.CERTIFICATION_STATUSES));
        backingManager.addCacheIfAbsent(createCache(CacheNames.CLASSIFICATION_NAMES));
        backingManager.addCacheIfAbsent(createCache(CacheNames.COLLECTIONS_DEVELOPERS));
        backingManager.addCacheIfAbsent(createCache(CacheNames.COLLECTIONS_LISTINGS));
        backingManager.addCacheIfAbsent(createCache(CacheNames.CQM_CRITERION));
        backingManager.addCacheIfAbsent(createCache(CacheNames.CQM_CRITERION_NUMBERS));
        backingManager.addCacheIfAbsent(createCache(CacheNames.DEVELOPER_NAMES));
        backingManager.addCacheIfAbsent(createCache(CacheNames.EDITIONS));
        backingManager.addCacheIfAbsent(createCache(CacheNames.EDITION_NAMES));
        backingManager.addCacheIfAbsent(createCache(CacheNames.FIND_SURVEILLANCE_NONCONFORMITY_STATUS_TYPE));
        backingManager.addCacheIfAbsent(createCache(CacheNames.FIND_SURVEILLANCE_REQ_TYPE));
        backingManager.addCacheIfAbsent(createCache(CacheNames.FIND_SURVEILLANCE_RESULT_TYPE));
        backingManager.addCacheIfAbsent(createCache(CacheNames.GET_ALL_UNRESTRICTED_APIKEYS));
        backingManager.addCacheIfAbsent(createCache(CacheNames.GET_DECERTIFIED_DEVELOPERS));
        backingManager.addCacheIfAbsent(createCache(CacheNames.JOB_TYPES));
        backingManager.addCacheIfAbsent(createCache(CacheNames.MACRA_MEASURES));
        backingManager.addCacheIfAbsent(createCache(CacheNames.PRACTICE_TYPE_NAMES));
        backingManager.addCacheIfAbsent(createCache(CacheNames.PRODUCT_NAMES));
        backingManager.addCacheIfAbsent(createCache(CacheNames.TEST_DATA));
        backingManager.addCacheIfAbsent(createCache(CacheNames.TEST_PROCEDURES));
        backingManager.addCacheIfAbsent(createCache(CacheNames.TEST_FUNCTIONALITY_MAPS));
        backingManager.addCacheIfAbsent(createCache(CacheNames.UPLOAD_TEMPLATE_VERSIONS));

        backingManager.addCacheIfAbsent(createCache(CacheNames.PREFETCHED_COLLECTIONS_LISTINGS));
        backingManager.addCacheIfAbsent(createCache(CacheNames.PREFETCHED_ALL_CERT_IDS));
        backingManager.addCacheIfAbsent(createCache(CacheNames.PREFETCHED_ALL_CERT_IDS_WITH_PRODUCTS));
        backingManager.addCacheIfAbsent(createCache(CacheNames.PREFETCHED_PRODUCT_NAMES));
        backingManager.addCacheIfAbsent(createCache(CacheNames.PREFETCHED_DEVELOPER_NAMES));
        return cacheManager;
    }

    private Cache createCache(String name) {
        int maxEntriesLocalHeap = (name.equals(CacheNames.COLLECTIONS_LISTINGS)
                || name.equals(CacheNames.PREFETCHED_COLLECTIONS_LISTINGS))
                ? MAX_ENTRIES_LOCAL_HEAP_LISTING_COLLECTION : MAX_ENTRIES_LOCAL_HEAP;
        Cache cache = new Cache(
                new CacheConfiguration(name, maxEntriesLocalHeap)
                  .memoryStoreEvictionPolicy(MemoryStoreEvictionPolicy.LFU)
                  .eternal(true)
                  .transactionalMode(TransactionalMode.OFF)
                  .maxEntriesLocalDisk(MAX_ENTRIES_LOCAL_DISK)
                  .diskSpoolBufferSizeMB(DISK_SPOOL_BUFFER_SIZE_MB)
                  .persistence(new PersistenceConfiguration().strategy(Strategy.LOCALTEMPSWAP)));
        return cache;
    }
}
