package gov.healthit.chpl.listener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.caching.updater.ListingsCollectionCacheUpdater;
import gov.healthit.chpl.util.PropertyUtil;

@Component
@Aspect
public class MeaningfulUseUploadListingCollectionCacheRefreshListener {
    private static final Logger LOGGER = LogManager.getLogger(MeaningfulUseUploadListingCollectionCacheRefreshListener.class);
    @Autowired
    private ListingsCollectionCacheUpdater cacheUpdater;
    @Autowired
    private PropertyUtil propUtil;

    /**
     * The meaningful use upload job publishes a certified product activity
     * message for each listing with updated MUU number in the upload file.
     * This can be many (thousands of?) activities, so it would not be good
     * to listen on the activity - instead we listen on the completion of the
     * upload job which has run in a separate thread behind the scenes
     * and refresh the cache when the entire job is completed.
     */
    @AfterReturning("execution(* gov.healthit.chpl.job.MeaningfulUseUploadJob.run(..))")
    public void afterMeaningfulUseUploadComplete() {
        LOGGER.debug("MUU Upload Complete. Refreshing listings collection cache.");
        if(propUtil.isAsyncCacheRefreshEnabled()) {
            refreshCacheAsync();
        } else {
            refreshCache();
        }
    }

    private void refreshCacheAsync() {
        cacheUpdater.refreshCacheAsync();
    }

    private void refreshCache() {
        cacheUpdater.refreshCacheSync();
    }
}
