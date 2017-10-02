package gov.healthit.chpl.caching;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.dao.search.CertifiedProductSearchDAO;
import gov.healthit.chpl.domain.search.CertifiedProductFlatSearchResult;

@Component
public class PreFetchedCaches {
	private static final Logger logger = LogManager.getLogger(PreFetchedCaches.class);
	@Autowired private CertifiedProductSearchDAO certifiedProductSearchDao;
	
	@Transactional
	@CacheEvict(value = CacheNames.COLLECTIONS_PREFETCHED_LISTINGS, beforeInvocation = true, allEntries = true)
	@Cacheable(CacheNames.COLLECTIONS_PREFETCHED_LISTINGS)
	public List<CertifiedProductFlatSearchResult> loadPreFetchedBasicSearch(){
		logger.info("Loading PreFetchedBasicSearch");
		List<CertifiedProductFlatSearchResult> results = certifiedProductSearchDao.getAllCertifiedProducts();
		return results;
	}
}
