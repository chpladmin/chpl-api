package gov.healthit.chpl.caching;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.auth.authentication.AdminUserAuthenticator;
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
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.CertificationIdAndCertifiedProductDTO;
import gov.healthit.chpl.dto.CertificationIdDTO;
import gov.healthit.chpl.manager.CertificationIdManager;
import gov.healthit.chpl.manager.PendingCertifiedProductManager;

@Component
public class PrefetchedCacheLoader {
    private static final Logger LOGGER = LogManager.getLogger(PrefetchedCacheLoader.class);
    @Autowired
    private CertifiedProductSearchDAO certifiedProductSearchDao;
    @Autowired
    private CertificationIdManager certificationIdManager;
    @Autowired
    private CertificationBodyDAO certificationBodyDAO;
    @Autowired
    private PendingCertifiedProductManager pcpManager;

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
        List<SimpleCertificationId> results =  certificationIdManager.getAll();
        LOGGER.info("Completed database call to get all certification IDs.");
        return results;
    }

    @Transactional
    @CacheEvict(value = CacheNames.PREFETCHED_ALL_CERT_IDS_WITH_PRODUCTS, beforeInvocation = true, allEntries = true)
    @Cacheable(CacheNames.PREFETCHED_ALL_CERT_IDS_WITH_PRODUCTS)
    public List<SimpleCertificationId> loadPrefetchedCertificationIdsWithProducts() {
        LOGGER.info("Loading prefetched certification IDs with products");
        List<SimpleCertificationId> results = certificationIdManager.getAllWithProducts();
        LOGGER.info("Completed database call to get all certification IDs with products.");
        return results;
    }

    @Transactional
    @CacheEvict(value = CacheNames.PREFETCHED_FIND_PENDING_LISTINGS_BY_ACB_ID, beforeInvocation = true, allEntries = true)
    @Cacheable(CacheNames.PREFETCHED_FIND_PENDING_LISTINGS_BY_ACB_ID)
    public void loadPrefetchedPendingListings() {
        List<CertificationBodyDTO> acbs = certificationBodyDAO.findAllActive();
        //assume the admin role to query for pending certified products
        Authentication actor = new AdminUserAuthenticator();
        SecurityContextHolder.getContext().setAuthentication(actor);
        for (CertificationBodyDTO dto : acbs) {
            pcpManager.getPendingCertifiedProductsCached(dto.getId());
        }
        SecurityContextHolder.getContext().setAuthentication(null);
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
