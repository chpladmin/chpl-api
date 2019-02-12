package gov.healthit.chpl.caching;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.dao.CQMCriterionDAO;
import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.dao.CertificationCriterionDAO;
import gov.healthit.chpl.dao.CertificationEditionDAO;
import gov.healthit.chpl.dao.CertificationStatusDAO;
import gov.healthit.chpl.dao.search.CertifiedProductSearchDAO;
import gov.healthit.chpl.domain.search.CertifiedProductFlatSearchResult;

@Component
public class PrefetchedCacheLoader {
    private static final Logger LOGGER = LogManager.getLogger(PrefetchedCacheLoader.class);
    @Autowired
    private CertifiedProductSearchDAO certifiedProductSearchDao;
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
