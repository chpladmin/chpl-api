package gov.healthit.chpl.caching;

import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.dao.search.CertifiedProductSearchDAO;
import gov.healthit.chpl.domain.KeyValueModelStatuses;
import gov.healthit.chpl.domain.SimpleCertificationId;
import gov.healthit.chpl.domain.search.CertifiedProductFlatSearchResult;
import gov.healthit.chpl.manager.CertificationIdManager;
import gov.healthit.chpl.manager.PrecacheableDimensionalDataManager;

@Component
public class PrefetchedCacheLoader {
    private static final Logger LOGGER = LogManager.getLogger(PrefetchedCacheLoader.class);
    @Autowired
    private CertifiedProductSearchDAO certifiedProductSearchDao;
    @Autowired
    private CertificationIdManager certIdManager;
    @Autowired
    private PrecacheableDimensionalDataManager precacheableDataManager;

    @Transactional
    @CacheEvict(value = CacheNames.PREFETCHED_COLLECTIONS_LISTINGS, beforeInvocation = true, allEntries = true)
    @Cacheable(CacheNames.PREFETCHED_COLLECTIONS_LISTINGS)
    public List<CertifiedProductFlatSearchResult> loadPrefetchedListingCollection() {
        LOGGER.info("Loading prefetched listings collection");
        List<CertifiedProductFlatSearchResult> results = certifiedProductSearchDao.getAllCertifiedProducts();
        LOGGER.info("Completed loading all listings.");
        return results;
    }

    @Transactional
    @CacheEvict(value = CacheNames.PREFETCHED_ALL_CERT_IDS, beforeInvocation = true, allEntries = true)
    @Cacheable(CacheNames.PREFETCHED_ALL_CERT_IDS)
    public List<SimpleCertificationId> loadPrefetchedCertificationIds() {
        LOGGER.info("Loading prefetched certification IDs");
        List<SimpleCertificationId> results =  certIdManager.getAll();
        LOGGER.info("Completed loading all certification IDs.");
        return results;
    }

    @Transactional
    @CacheEvict(value = CacheNames.PREFETCHED_ALL_CERT_IDS_WITH_PRODUCTS, beforeInvocation = true, allEntries = true)
    @Cacheable(CacheNames.PREFETCHED_ALL_CERT_IDS_WITH_PRODUCTS)
    public List<SimpleCertificationId> loadPrefetchedCertificationIdsWithProducts() {
        LOGGER.info("Loading prefetched certification IDs with products");
        List<SimpleCertificationId> results = certIdManager.getAllWithProducts();
        LOGGER.info("Completed loading all certification IDs with products.");
        return results;
    }

    @Transactional
    @CacheEvict(value = CacheNames.PREFETCHED_PRODUCT_NAMES, beforeInvocation = true, allEntries = true)
    @Cacheable(CacheNames.PREFETCHED_PRODUCT_NAMES)
    public Set<KeyValueModelStatuses> loadPrefetchedProductNames() {
        LOGGER.info("Loading prefetched product names");
        Set<KeyValueModelStatuses> results = precacheableDataManager.getProducts();
        LOGGER.info("Completed loading product names.");
        return results;
    }

    @Transactional
    @CacheEvict(value = CacheNames.PREFETCHED_DEVELOPER_NAMES, beforeInvocation = true, allEntries = true)
    @Cacheable(CacheNames.PREFETCHED_DEVELOPER_NAMES)
    public Set<KeyValueModelStatuses> loadPrefetchedDeveloperNames() {
        LOGGER.info("Loading prefetched developer names");
        Set<KeyValueModelStatuses> results = precacheableDataManager.getDevelopers();
        LOGGER.info("Completed loading developer names.");
        return results;
    }
}
