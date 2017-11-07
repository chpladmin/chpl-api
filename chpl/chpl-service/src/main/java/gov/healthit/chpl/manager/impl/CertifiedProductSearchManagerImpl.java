package gov.healthit.chpl.manager.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.caching.CacheNames;
import gov.healthit.chpl.caching.CacheUtil;
import gov.healthit.chpl.dao.CertifiedProductSearchResultDAO;
import gov.healthit.chpl.dao.search.CertifiedProductSearchDAO;
import gov.healthit.chpl.domain.search.CertifiedProductBasicSearchResult;
import gov.healthit.chpl.domain.search.CertifiedProductFlatSearchResult;
import gov.healthit.chpl.domain.search.SearchRequest;
import gov.healthit.chpl.domain.search.SearchResponse;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.manager.CertifiedProductSearchManager;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

@Service
public class CertifiedProductSearchManagerImpl implements CertifiedProductSearchManager {

    @Autowired
    CertifiedProductSearchResultDAO certifiedProductSearchResultDAO;
    
    @Autowired
    CertifiedProductSearchDAO searchDao;
    
    @Autowired
    CertifiedProductSearchDAO basicCpSearchDao;

    @Transactional(readOnly = true)
    @Override
    @Cacheable(CacheNames.COLLECTIONS_LISTINGS)
    public List<CertifiedProductFlatSearchResult> search() {
        List<CertifiedProductFlatSearchResult> results = basicCpSearchDao.getAllCertifiedProducts();
        return results;
    }

    @Transactional
    @Override
    public SearchResponse search(SearchRequest searchRequest) {

        List<CertifiedProductBasicSearchResult> searchResults = searchDao.search(searchRequest);
        //TODO
        //Integer countSearchResults = certifiedProductSearchResultDAO.countMultiFilterSearchResults(searchRequest)
        //        .intValue();

        SearchResponse response = new SearchResponse(50, searchResults, searchRequest.getPageSize(),
                searchRequest.getPageNumber());
        return response;
    }
}
