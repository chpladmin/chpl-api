package gov.healthit.chpl.aspect;

import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.caching.CacheNames;
import gov.healthit.chpl.caching.CacheReplacer;
import gov.healthit.chpl.dao.search.CertifiedProductSearchDAO;
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.ListingUpdateRequest;
import gov.healthit.chpl.domain.UpdateDevelopersRequest;
import gov.healthit.chpl.domain.UpdateProductsRequest;
import gov.healthit.chpl.domain.UpdateVersionsRequest;
import gov.healthit.chpl.domain.search.CertifiedProductFlatSearchResult;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

@Component
@Aspect
public class CacheRefreshAspect {
    private static final Logger LOGGER = LogManager.getLogger(CacheRefreshAspect.class);
    private CertifiedProductSearchDAO certifiedProductSearchDao;

    /**
     * Autowired constructor for dependency injection.
     */
    @Autowired
    public CacheRefreshAspect(final CertifiedProductSearchDAO certifiedProductSearchDao) {
        this.certifiedProductSearchDao = certifiedProductSearchDao;
    }

    @AfterReturning("execution(* "
            + "gov.healthit.chpl.web.controller.DeveloperController.update(..)) && "
            + "args(developerInfo,..)")
    public void refreshCacheOnDeveloperUpdate(final UpdateDevelopersRequest developerInfo) {
        //TODO
        //check if developer name changed
        //check if developer status changed
        //check if previous developers changed??
        refreshCache();
    }

    @AfterReturning("execution(* "
            + "gov.healthit.chpl.web.controller.ProductController.update(..)) && "
            + "args(productInfo,..)")
    public void refreshCacheOnProductUpdate(final UpdateProductsRequest productInfo) {
        //TODO
        //check if product name changed
        refreshCache();
    }

    @AfterReturning("execution(* "
            + "gov.healthit.chpl.web.controller.ProductVersionController.update(..)) && "
            + "args(versionInfo,..)")
    public void refreshCacheOnVersionUpdate(final UpdateVersionsRequest versionInfo) {
        //TODO
        //check if version name changed
        refreshCache();
    }

    @AfterReturning("execution(* "
            + "gov.healthit.chpl.web.controller.CertificationBodyController.update(..)) && "
            + "args(acbInfo,..)")
    public void refreshCacheOnCertificationBodyUpdate(final CertificationBody acbInfo) {
        //TODO
        //check if acb name changed
        refreshCache();
    }

    @AfterReturning("execution(* "
            + "gov.healthit.chpl.web.controller.CertifiedProductController.update(..)) && "
            + "args(updateRequest,..)")
    public void refreshCacheOnListingUpdate(final ListingUpdateRequest updateRequest) {
        //TODO
        //just update anyway? or check a bunch of things?
        refreshCache();
    }

    /**
     * Load the main search cache into a pre-fetched cache.
     * Once it is loaded replace the live cache with the pre-fetched one.
     */
    private void refreshCache() {
        LOGGER.info("Refreshing listings cache...");
        LOGGER.info("Getting cache manager.");
        CacheManager manager = CacheManager.getInstance();
        LOGGER.info("Got cache manager: " + manager.getName());
        loadPreFetchedBasicSearch();
        LOGGER.info("Adding cache generation time to prefetched cache.");
        manager.getCache(CacheNames.COLLECTIONS_PREFETCHED_LISTINGS).putQuiet(
                new Element("CACHE_GENERATION_TIME", (new Date().getTime())));
        LOGGER.info("Replacing listings cache with prefetched cache.");
        CacheReplacer.replaceCache(manager.getCache(CacheNames.COLLECTIONS_LISTINGS),
                manager.getCache(CacheNames.COLLECTIONS_PREFETCHED_LISTINGS));
    }

    /**
     * Clear and then reload the pre-fetched cache.
     */
    @Transactional
    @CacheEvict(value = CacheNames.COLLECTIONS_PREFETCHED_LISTINGS, beforeInvocation = true, allEntries = true)
    @Cacheable(CacheNames.COLLECTIONS_PREFETCHED_LISTINGS)
    private void loadPreFetchedBasicSearch() {
        LOGGER.info("Loading PreFetchedBasicSearch");
        List<CertifiedProductFlatSearchResult> results = certifiedProductSearchDao.getAllCertifiedProducts();
        LOGGER.info("Completed database call to get all listings.");
    }
}
