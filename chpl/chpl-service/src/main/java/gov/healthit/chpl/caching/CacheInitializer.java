package gov.healthit.chpl.caching;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class CacheInitializer {
    private Boolean enableCacheInitialization;
    private AsynchronousCacheInitialization asynchronousCacheInitialization;

    @Autowired
    public CacheInitializer(AsynchronousCacheInitialization asynchronousCacheInitialization,
            @Value("${enableCacheInitialization}") Boolean enableCacheInitialization) {
        this.asynchronousCacheInitialization = asynchronousCacheInitialization;
        this.enableCacheInitialization = enableCacheInitialization;
    }

    public static List<String> getPreInitializedCaches() {
        List<String> caches = new ArrayList<String>();
        caches.add(CacheNames.COLLECTIONS_LISTINGS);
        caches.add(CacheNames.COLLECTIONS_SEARCH);
//        caches.add(CacheNames.ALL_CERT_IDS);
//        caches.add(CacheNames.ALL_CERT_IDS_WITH_PRODUCTS);
        // all below caches make up the search options
        caches.add(CacheNames.EDITION_NAMES);
        caches.add(CacheNames.CERTIFICATION_STATUSES);
        caches.add(CacheNames.PRACTICE_TYPE_NAMES);
        caches.add(CacheNames.CLASSIFICATION_NAMES);
        caches.add(CacheNames.PRODUCT_NAMES);
        caches.add(CacheNames.DEVELOPER_NAMES);
        caches.add(CacheNames.CQM_CRITERION_NUMBERS);
        caches.add(CacheNames.CERTIFICATION_CRITERION_NUMBERS);
        return caches;
    }

    @EventListener
    public void initializePreloadedCaches(ContextRefreshedEvent event) {
        if (enableCacheInitialization != null && enableCacheInitialization) {
            try {
                asynchronousCacheInitialization.initializeSearchOptions();
                asynchronousCacheInitialization.initializeCertificationIdsGetAll();
                asynchronousCacheInitialization.initializeCertificationIdsGetAllWithProducts();
                asynchronousCacheInitialization.initializeBasicSearchAndDirectReviews();
            } catch (Exception e) {
                LOGGER.error("Caching failed to initialize", e);
            }
        }
    }
}
