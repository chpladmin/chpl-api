package gov.healthit.chpl.listener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.caching.CacheRefreshListener;
import gov.healthit.chpl.caching.updater.DeveloperNamesCacheUpdater;
import gov.healthit.chpl.domain.PendingCertifiedProductDetails;
import gov.healthit.chpl.domain.SplitDeveloperRequest;
import gov.healthit.chpl.domain.UpdateDevelopersRequest;

/**
 * Listener that determines when to update the developer names cache.
 * @author kekey
 *
 */
@Component
@Aspect
public class DeveloperNamesCacheRefreshListener extends CacheRefreshListener {
    private static final Logger LOGGER = LogManager.getLogger(DeveloperNamesCacheRefreshListener.class);
    private DeveloperNamesCacheUpdater cacheUpdater;

    @Autowired
    public DeveloperNamesCacheRefreshListener(final DeveloperNamesCacheUpdater cacheUpdater) {
        this.cacheUpdater = cacheUpdater;
    }

    /**
     * After a developer is updated refresh the developer names cache.
     * @param developerInfo developer update request object
     */
    /*@AfterReturning(
            "execution(* gov.healthit.chpl.web.controller.DeveloperController.updateDeveloper(..)) && "
            + "args(developerInfo,..)")*/
    public void afterDeveloperUpdate(final UpdateDevelopersRequest developerInfo) {
        LOGGER.debug("A developer was updated or merged. Refreshing developer names cache.");
        refreshCache();
    }

    /**
     * After a developer is split refresh the developer names cache.
     * @param developerId developer id that is getting split
     * @param splitRequest the split request data
     */
    /*@AfterReturning(
            "execution(* gov.healthit.chpl.web.controller.DeveloperController.splitDeveloper(..)) && "
            + "args(developerId,splitRequest,..)")*/
    public void afterDeveloperSplit(final Long developerId, final SplitDeveloperRequest splitRequest) {
        LOGGER.debug("A developer was split. Refreshing developer names cache.");
        refreshCache();
    }

    /**
     * After a listing is confirmed refresh the developer names cache.
     * The user may have created a new developer in the process of confirmation.
     * @param pendingCp pending listing object
     */
    /*@AfterReturning(
            "execution(* gov.healthit.chpl.web.controller.CertifiedProductController.confirmPendingCertifiedProduct(..)) && "
            + "args(pendingCp,..)")*/
    public void afterListingConfirm(final PendingCertifiedProductDetails pendingCp) {
        LOGGER.debug("A listing was confirmed. Refreshing developer names cache.");
        refreshCache();
    }

    protected void refreshCacheAsync() {
        cacheUpdater.refreshCacheAsync();
    }

    protected void refreshCacheSync() {
        cacheUpdater.refreshCacheSync();
    }
}
