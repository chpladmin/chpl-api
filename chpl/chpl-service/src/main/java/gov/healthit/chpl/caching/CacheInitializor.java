package gov.healthit.chpl.caching;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import javax.annotation.PostConstruct;

import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
@Aspect
public class CacheInitializor {
    private static final Double ONE_SECOND_MILLIS = 1000.0;

    private Integer initializeTimeoutSecs;
    private Long tInitStart;
    private Long tInitEnd;
    private Double tInitElapsedSecs;
    private Future<Boolean> isInitializeSearchOptionsDone;
    private Future<Boolean> isInitializeBasicSearchDone;
    private Future<Boolean> isInitializeCertificationIdsGetAllDone;
    private Future<Boolean> isInitializeCertificationIdsGetAllWithProductsDone;
    private Future<Boolean> isInitializeDirectReviewsDone;
    private String enableCacheInitializationValue;

    private AsynchronousCacheInitialization asynchronousCacheInitialization;
    private Environment env;

    @Autowired
    public CacheInitializor(AsynchronousCacheInitialization asynchronousCacheInitialization,
            Environment env) {
        this.asynchronousCacheInitialization = asynchronousCacheInitialization;
        this.env = env;
    }

    public static List<String> getPreInitializedCaches() {
        List<String> caches = new ArrayList<String>();
        caches.add(CacheNames.COLLECTIONS_LISTINGS);
        caches.add(CacheNames.ALL_CERT_IDS);
        caches.add(CacheNames.ALL_CERT_IDS_WITH_PRODUCTS);
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

    @Async
    @PostConstruct
    public void initialize() {
        enableCacheInitializationValue = env.getProperty("enableCacheInitialization");
        initializeTimeoutSecs = Integer.parseInt(env.getProperty("cacheInitializeTimeoutSecs").toString());

        tInitStart = System.currentTimeMillis();
        if (tInitEnd != null) {
            tInitElapsedSecs = (tInitStart - tInitEnd) / ONE_SECOND_MILLIS;
        }

        if (tInitEnd == null || tInitElapsedSecs > initializeTimeoutSecs) {
            try {
                if (enableCacheInitializationValue != null && enableCacheInitializationValue.equalsIgnoreCase("true")) {
                    if (isInitializeSearchOptionsDone != null && !isInitializeSearchOptionsDone.isDone()) {
                        isInitializeSearchOptionsDone.cancel(true);
                    }
                    isInitializeSearchOptionsDone = asynchronousCacheInitialization.initializeSearchOptions();
                    if (isInitializeCertificationIdsGetAllDone != null
                            && !isInitializeCertificationIdsGetAllDone.isDone()) {
                        isInitializeCertificationIdsGetAllDone.cancel(true);
                    }
                    isInitializeCertificationIdsGetAllDone = asynchronousCacheInitialization
                            .initializeCertificationIdsGetAll();

                    if (isInitializeCertificationIdsGetAllWithProductsDone != null
                            && !isInitializeCertificationIdsGetAllWithProductsDone.isDone()) {
                        isInitializeCertificationIdsGetAllWithProductsDone.cancel(true);
                    }
                    isInitializeCertificationIdsGetAllWithProductsDone = asynchronousCacheInitialization
                            .initializeCertificationIdsGetAllWithProducts();

                    if (isInitializeBasicSearchDone != null && !isInitializeBasicSearchDone.isDone()) {
                        isInitializeBasicSearchDone.cancel(true);
                    }
                    isInitializeBasicSearchDone = asynchronousCacheInitialization.initializeBasicSearch();
                    if (isInitializeDirectReviewsDone != null && !isInitializeDirectReviewsDone.isDone()) {
                        isInitializeDirectReviewsDone.cancel(true);
                    }
                    isInitializeDirectReviewsDone = asynchronousCacheInitialization.initializeDirectReviews();
                }
            } catch (Exception e) {
                LOGGER.error("Caching failed to initialize");
                e.printStackTrace();
            }
        }
        tInitEnd = System.currentTimeMillis();
    }
}
