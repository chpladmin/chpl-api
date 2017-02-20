package gov.healthit.chpl.caching;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.dao.search.CertifiedProductSearchDAO;
import gov.healthit.chpl.domain.search.BasicSearchResponse;
import gov.healthit.chpl.domain.search.CertifiedProductFlatSearchResult;

@Component
public class PreFetchedCaches {
	private static final Logger logger = LogManager.getLogger(CacheInitializor.class);
	@Autowired private CertifiedProductSearchDAO certifiedProductSearchDao;
	
	@Transactional
	@Cacheable(CacheNames.PRE_FETCHED_BASIC_SEARCH)
	public BasicSearchResponse initializePreFetchedBasicSearch(){
		logger.debug("Initializing PreFetchedBasicSearch");
		BasicSearchResponse response = new BasicSearchResponse();
		List<CertifiedProductFlatSearchResult> results = certifiedProductSearchDao.getAllCertifiedProducts();
		response.setResults(results);
		return response;
	}
}
