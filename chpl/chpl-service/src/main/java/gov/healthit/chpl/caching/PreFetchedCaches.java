package gov.healthit.chpl.caching;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.dao.CQMCriterionDAO;
import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.dao.CertificationCriterionDAO;
import gov.healthit.chpl.dao.CertificationEditionDAO;
import gov.healthit.chpl.dao.CertificationIdDAO;
import gov.healthit.chpl.dao.CertificationStatusDAO;
import gov.healthit.chpl.dao.search.CertifiedProductSearchDAO;
import gov.healthit.chpl.domain.SimpleCertificationId;
import gov.healthit.chpl.domain.SimpleCertificationIdWithProducts;
import gov.healthit.chpl.domain.search.CertifiedProductFlatSearchResult;
import gov.healthit.chpl.dto.CertificationIdAndCertifiedProductDTO;
import gov.healthit.chpl.dto.CertificationIdDTO;

@Component
public class PreFetchedCaches {
    private static final Logger LOGGER = LogManager.getLogger(PreFetchedCaches.class);
    @Autowired
    private CertifiedProductSearchDAO certifiedProductSearchDao;
    @Autowired
    private CertificationIdDAO certificationIdDao;
    @Autowired
    private CertificationBodyDAO certificationBodyDAO;
    @Autowired
    private CQMCriterionDAO cqmCriterionDAO;
    @Autowired
    private CertificationCriterionDAO certificationCriterionDAO;
    @Autowired
    private CertificationEditionDAO certificationEditionDAO;
    @Autowired
    private CertificationStatusDAO certificationStatusDao;

    @Transactional
    @CacheEvict(value = CacheNames.PREFETCHED_COLLECTIONS_LISTINGS, beforeInvocation = true, allEntries = true)
    @Cacheable(CacheNames.PREFETCHED_COLLECTIONS_LISTINGS)
    public List<CertifiedProductFlatSearchResult> loadPrefetchedListingCollection() {
        LOGGER.info("Loading prefetched listings collection");
        List<CertifiedProductFlatSearchResult> results = certifiedProductSearchDao.getAllCertifiedProducts();
        LOGGER.info("Completed database call to get all listings.");
        return results;
    }

    @Transactional
    @CacheEvict(value = CacheNames.PREFETCHED_ALL_CERT_IDS, beforeInvocation = true, allEntries = true)
    @Cacheable(CacheNames.PREFETCHED_ALL_CERT_IDS)
    public List<SimpleCertificationId> loadPrefetchedCertificationIds() {
        LOGGER.info("Loading prefetched certification IDs");
        List<SimpleCertificationId> results =  certificationIdDao.findAll();
        LOGGER.info("Completed database call to get all certification IDs.");
        return results;
    }

    @Transactional
    @CacheEvict(value = CacheNames.PREFETCHED_ALL_CERT_IDS_WITH_PRODUCTS, beforeInvocation = true, allEntries = true)
    @Cacheable(CacheNames.PREFETCHED_ALL_CERT_IDS_WITH_PRODUCTS)
    public List<SimpleCertificationId> loadPrefetchedCertificationIdsWithProducts() {
        LOGGER.info("Loading prefetched certification IDs with products");
        List<SimpleCertificationId> results = certificationIdDao.getAllCertificationIdsWithProducts();
        LOGGER.info("Completed database call to get all certification IDs with products.");
        return results;
    }

    //TODO: search options
//    searchMenuManager.getCertBodyNames();
//    searchMenuManager.getEditionNames(false);
//    searchMenuManager.getEditionNames(true);
//    searchMenuManager.getCertificationStatuses();
//    searchMenuManager.getPracticeTypeNames();
//    searchMenuManager.getClassificationNames();
//    searchMenuManager.getProductNames();
//    searchMenuManager.getDeveloperNames();
//    searchMenuManager.getCQMCriterionNumbers(false);
//    searchMenuManager.getCQMCriterionNumbers(true);
//    searchMenuManager.getCertificationCriterionNumbers(false);
//    searchMenuManager.getCertificationCriterionNumbers(true);
}
