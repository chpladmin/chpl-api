package gov.healthit.chpl.caching;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import gov.healthit.chpl.util.PropertyUtil;

public abstract class CacheRefreshListener {
    private static final Logger LOGGER = LogManager.getLogger(CacheRefreshListener.class);
    @Autowired
    private PropertyUtil propUtil;

    protected void refreshCache() {
        if (propUtil.isAsyncCacheRefreshEnabled()) {
            LOGGER.debug("Refreshing cache asynchronously");
            refreshCacheAsync();
        } else {
            LOGGER.debug("Refreshing cache synchronously");
            refreshCacheSync();
        }
    }

    protected abstract void refreshCacheAsync();
    protected abstract void refreshCacheSync();
}
