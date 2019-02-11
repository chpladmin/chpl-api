package gov.healthit.chpl.listener;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.caching.CertIdsCacheUpdater;
import gov.healthit.chpl.util.PropertyUtil;

/**
 * Listener that determines when to update the cache of certification IDs.
 * @author kekey
 *
 */
@Component
@Aspect
public class CertIdsCacheRefreshListener {
    private static final Logger LOGGER = LogManager.getLogger(CertIdsCacheRefreshListener.class);
    @Autowired
    private CertIdsCacheUpdater cacheUpdater;
    @Autowired
    private PropertyUtil propUtil;

    /**
     * After a certification ID is created refresh the certification IDs cache.
     * @param ids list of ids used to create the certification ID
     */
    @AfterReturning(
            "execution(* gov.healthit.chpl.web.controller.CertificationIdController.createCertificationId(..)) && "
            + "args(ids,..)")
    public void afterCertificationIdCreated(final List<Long> ids) {
        LOGGER.debug("A certification ID was created. Refreshing certification ID caches. ");
        if (propUtil.isAsyncCacheRefreshEnabled()) {
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
