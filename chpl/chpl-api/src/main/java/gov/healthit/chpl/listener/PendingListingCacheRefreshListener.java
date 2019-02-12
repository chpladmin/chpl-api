package gov.healthit.chpl.listener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import gov.healthit.chpl.caching.PendingListingCacheUpdater;
import gov.healthit.chpl.domain.PendingCertifiedProductDetails;
import gov.healthit.chpl.util.PropertyUtil;

/**
 * Listener that determines when to update the cache of certification IDs.
 * @author kekey
 *
 */
@Component
@Aspect
public class PendingListingCacheRefreshListener {
    private static final Logger LOGGER = LogManager.getLogger(PendingListingCacheRefreshListener.class);
    @Autowired
    private  PendingListingCacheUpdater cacheUpdater;
    @Autowired
    private PropertyUtil propUtil;

    /**
     * After a pending listing is uploaded, refresh the pending listings cache.
     * @param file the file that was uploaded.
     */
    @AfterReturning(
            "execution(* gov.healthit.chpl.web.controller.CertifiedProductController.upload(..)) && "
            + "args(file,..)")
    public void afterPendingListingUploaded(final MultipartFile file) {
        LOGGER.debug("A pending listing was uploaded. Refreshing the pending listing cache. ");
        if (propUtil.isAsyncCacheRefreshEnabled()) {
            refreshCacheAsync();
        } else {
            refreshCache();
        }
    }

    /**
     * After a pending listing is confirmed, refresh the pending listings cache.
     * @param pendingCp the details of the pending listing to confirm.
     */
    @AfterReturning(
            "execution(* gov.healthit.chpl.web.controller.CertifiedProductController.confirmPendingCertifiedProduct(..)) && "
            + "args(pendingCp,..)")
    public void afterPendingListingConfirmed(final PendingCertifiedProductDetails pendingCp) {
        LOGGER.debug("A pending listing was confirmed. Refreshing the pending listing cache. ");
        if (propUtil.isAsyncCacheRefreshEnabled()) {
            refreshCacheAsync();
        } else {
            refreshCache();
        }
    }

    /**
     * After a pending listing is rejected, refresh the pending listings cache.
     * @param pcpId the id of the listing that was rejected.
     */
    @AfterReturning(
            "execution(* gov.healthit.chpl.web.controller.CertifiedProductController.rejectPendingCertifiedProduct(..)) && "
            + "args(pcpId,..)")
    public void afterPendingListingRejected(final Long pcpId) {
        LOGGER.debug("A pending listing was rejected. Refreshing the pending listing cache. ");
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
