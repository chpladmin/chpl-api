package gov.healthit.chpl.listener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.caching.CacheRefreshListener;
import gov.healthit.chpl.caching.updater.ProductNamesCacheUpdater;
import gov.healthit.chpl.domain.PendingCertifiedProductDetails;
import gov.healthit.chpl.domain.SplitProductsRequest;
import gov.healthit.chpl.domain.UpdateProductsRequest;

/**
 * Listener that determines when to update the product names cache.
 * @author kekey
 *
 */
@Component
@Aspect
public class ProductNamesCacheRefreshListener extends CacheRefreshListener {
    private static final Logger LOGGER = LogManager.getLogger(ProductNamesCacheRefreshListener.class);
    @Autowired
    private ProductNamesCacheUpdater cacheUpdater;

    /**
     * After a product is updated refresh the product names cache.
     * @param productInfo product update request object
     */
    @AfterReturning(
            "execution(* gov.healthit.chpl.web.controller.ProductController.updateProduct(..)) && "
            + "args(productInfo,..)")
    public void afterProductUpdate(final UpdateProductsRequest productInfo) {
        LOGGER.debug("A product was updated or merged. Refreshing product names cache.");
        refreshCache();
    }

    /**
     * After a product is split refresh the product names cache.
     * @param productId product id that is being split
     * @param splitRequest request object with instructions how to split the product
     */
    @AfterReturning(
            "execution(* gov.healthit.chpl.web.controller.ProductController.splitProduct(..)) && "
            + "args(productId,splitRequest,..)")
    public void afterProductSplit(final Long productId, final SplitProductsRequest splitRequest) {
        LOGGER.debug("A product was split. Refreshing product names cache.");
        refreshCache();
    }

    /**
     * After a listing is confirmed refresh the product names cache.
     * The user may have created a new product in the process of confirmation.
     * @param pendingCp pending listing object
     */
    @AfterReturning(
            "execution(* gov.healthit.chpl.web.controller.CertifiedProductController.confirmPendingCertifiedProduct(..)) && "
            + "args(pendingCp,..)")
    public void afterListingConfirm(final PendingCertifiedProductDetails pendingCp) {
        LOGGER.debug("A listing was confirmed. Refreshing product names cache.");
        refreshCache();
    }

    protected void refreshCacheAsync() {
        cacheUpdater.refreshCacheAsync();
    }

    protected void refreshCacheSync() {
        cacheUpdater.refreshCacheSync();
    }
}
