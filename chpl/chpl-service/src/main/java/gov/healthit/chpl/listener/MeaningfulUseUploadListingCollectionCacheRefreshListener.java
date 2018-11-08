package gov.healthit.chpl.listener;

import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.caching.CacheNames;
import gov.healthit.chpl.caching.CacheReplacer;
import gov.healthit.chpl.caching.ListingsCollectionCacheUpdater;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

@Component
@Aspect
public class MeaningfulUseUploadListingCollectionCacheRefreshListener {
    private static final Logger LOGGER = LogManager.getLogger(MeaningfulUseUploadListingCollectionCacheRefreshListener.class);
    @Autowired
    private ListingsCollectionCacheUpdater cacheUpdater;

    /**
     * The meaningful use upload job publishes a certified product activity
     * message for each listing with updated MUU number in the upload file.
     * This can be many (thousands of?) activities, so it would not be good
     * to listen on the activity - instead we listen on the completion of the
     * upload job which has run in a separate thread behind the scenes
     * and refresh the cache when the entire job is completed.
     */
    @AfterReturning("execution(* gov.healthit.chpl.job.MeaningfulUseUploadJob.run(..))")
    @Async
    public void afterMeaningfulUseUploadComplete() {
        LOGGER.debug("MUU Upload Complete. Refreshing listings collection cache.");
        cacheUpdater.refreshCache();
    }
}
