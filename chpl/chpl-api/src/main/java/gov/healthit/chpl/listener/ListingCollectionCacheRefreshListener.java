package gov.healthit.chpl.listener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.caching.CacheRefreshListener;
import gov.healthit.chpl.caching.updater.ListingsCollectionCacheUpdater;
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.ListingUpdateRequest;
import gov.healthit.chpl.domain.PendingCertifiedProductDetails;
import gov.healthit.chpl.domain.SimpleExplainableAction;
import gov.healthit.chpl.domain.SplitDeveloperRequest;
import gov.healthit.chpl.domain.SplitProductsRequest;
import gov.healthit.chpl.domain.SplitVersionsRequest;
import gov.healthit.chpl.domain.Surveillance;
import gov.healthit.chpl.domain.UpdateDevelopersRequest;
import gov.healthit.chpl.domain.UpdateProductsRequest;
import gov.healthit.chpl.domain.UpdateVersionsRequest;

/**
 * Listener that determines when to update the main searchable cache of all listings.
 * @author kekey
 *
 */
@Component
@Aspect
public class ListingCollectionCacheRefreshListener extends CacheRefreshListener {
    private static final Logger LOGGER = LogManager.getLogger(ListingCollectionCacheRefreshListener.class);
    @Autowired
    private ListingsCollectionCacheUpdater cacheUpdater;

    /**
     * After a developer is updated refresh the listings collection cache.
     * @param developerInfo developer request object
     */
    @AfterReturning(
            "execution(* gov.healthit.chpl.web.controller.DeveloperController.updateDeveloper(..)) && "
            + "args(developerInfo,..)")
    public void afterDeveloperUpdate(final UpdateDevelopersRequest developerInfo) {
        LOGGER.debug("A developer was updated. Refreshing listings collection cache. ");
        refreshCache();
    }

    /**
     * After a developer is split refresh the developer names cache.
     * @param developerId developer id getting split
     * @param splitRequest the split request data
     */
    @AfterReturning(
            "execution(* gov.healthit.chpl.web.controller.DeveloperController.splitDeveloper(..)) && "
            + "args(developerId,splitRequest,..)")
    public void afterDeveloperSplit(final Long developerId, final SplitDeveloperRequest splitRequest) {
        LOGGER.debug("A developer was split. Refreshing developer names cache.");
        refreshCache();
    }

    /**
     * After a product is updated refresh the listings collection cache.
     * @param productInfo product update request object
     */
    @AfterReturning(
            "execution(* gov.healthit.chpl.web.controller.ProductController.updateProduct(..)) && "
            + "args(productInfo,..)")
    public void afterProductUpdate(final UpdateProductsRequest productInfo) {
        LOGGER.debug("A product was updated. Refreshing listings collection cache.");
        refreshCache();
    }

    /**
     * After a product is split refresh the listings collection cache.
     * @param productId product id to split
     * @param splitRequest other information what to split
     */
    @AfterReturning(
            "execution(* gov.healthit.chpl.web.controller.ProductController.splitProduct(..)) && "
            + "args(productId,splitRequest,..)")
    public void afterProductSplit(final Long productId, final SplitProductsRequest splitRequest) {
        LOGGER.debug("A product was split. Refreshing listings collection cache.");
        refreshCache();
    }

    /**
     * After a version is updated refresh the listings collection cache.
     * @param versionInfo version update request object
     */
    @AfterReturning(
            "execution(* gov.healthit.chpl.web.controller.ProductVersionController.updateVersion(..)) && "
            + "args(versionInfo,..)")
    public void afterVersionUpdate(final UpdateVersionsRequest versionInfo) {
        LOGGER.debug("A version was updated. Refreshing listings collection cache.");
        refreshCache();
    }

    /**
     * After a version is split refresh the listings collection cache.
     * @param versionId version id to split
     * @param splitRequest request object
     */
    @AfterReturning(
            "execution(* gov.healthit.chpl.web.controller.ProductVersionController.updateVersion(..)) && "
            + "args(versionId,splitRequest,..)")
    public void afterVersionSplit(final Long versionId, final SplitVersionsRequest splitRequest) {
        LOGGER.debug("A version was split. Refreshing listings collection cache.");
        refreshCache();
    }

    /**
     * After an acb is updated refresh the listings collection cache.
     * @param acbInfo certification body update request object
     */
    @AfterReturning(
            "execution(* gov.healthit.chpl.web.controller.CertificationBodyController.updateAcb(..)) && "
            + "args(acbInfo,..)")
    public void afterCertificationBodyUpdate(final CertificationBody acbInfo) {
        LOGGER.debug("An ACB was updated. Refreshing listings collection cache.");
        refreshCache();
    }

    /**
     * After a surveillance is created refresh the listings collection cache.
     * @param survToInsert surveillance that was inserted
     */
    @AfterReturning(
            "execution(* gov.healthit.chpl.web.controller.SurveillanceController.createSurveillance(..)) && "
            + "args(survToInsert,..)")
    public void afterSurveillanceCreation(final Surveillance survToInsert) {
        LOGGER.debug("A surveillance was created. Refreshing listings collection cache.");
        refreshCache();
    }

    /**
     * After a surveillance is updated refresh the listings collection cache.
     * @param survToUpdate surveillance that was updated
     */
    @AfterReturning(
            "execution(* gov.healthit.chpl.web.controller.SurveillanceController.updateSurveillance(..)) && "
            + "args(survToUpdate,..)")
    public void afterSurveillanceUpdate(final Surveillance survToUpdate) {
        LOGGER.debug("A surveillance was updated. Refreshing listings collection cache.");
        refreshCache();
    }

    /**
     * After a surveillance is deleted refresh the listings collection cache.
     * @param surveillanceId surveillance id to deleted
     * @param requestBody user-supplied reason
     */
    @AfterReturning(
            "execution(* gov.healthit.chpl.web.controller.SurveillanceController.deleteSurveillance(..)) && "
            + "args(surveillanceId,requestBody,..)")
    public void afterSurveillanceDeletion(final Long surveillanceId,
            final SimpleExplainableAction requestBody) {
        LOGGER.debug("A surveillance was deleted. Refreshing listings collection cache.");
        refreshCache();
    }

    /**
     * After a new listings is confirmed refresh the listings collection cache.
     * @param pendingCp pending listing object
     */
    @AfterReturning(
            "execution(* gov.healthit.chpl.web.controller.CertifiedProductController.confirmPendingCertifiedProduct(..)) && "
            + "args(pendingCp,..)")
    public void afterListingConfirm(final PendingCertifiedProductDetails pendingCp) {
        LOGGER.debug("A listing was confirmed. Refreshing listings collection cache.");
        refreshCache();
    }

    /**
     * After a listing is updated refresh the listings collection cache.
     * @param updateRequest listing update object
     */
    @AfterReturning(
            "execution(* gov.healthit.chpl.web.controller.CertifiedProductController.updateCertifiedProduct(..)) && "
            + "args(updateRequest,..)")
    public void afterListingUpdate(final ListingUpdateRequest updateRequest) {
        LOGGER.debug("A listing was updated. Refreshing listings collection cache.");
        refreshCache();
    }

    protected void refreshCacheAsync() {
        cacheUpdater.refreshCacheAsync();
    }

    protected void refreshCacheSync() {
        cacheUpdater.refreshCacheSync();
    }
}
