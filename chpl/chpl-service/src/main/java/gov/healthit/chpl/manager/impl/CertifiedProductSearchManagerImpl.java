package gov.healthit.chpl.manager.impl;

import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.caching.CacheNames;
import gov.healthit.chpl.dao.CertifiedProductSearchResultDAO;
import gov.healthit.chpl.dao.search.CertifiedProductSearchDAO;
import gov.healthit.chpl.domain.search.CertifiedProductBasicSearchResult;
import gov.healthit.chpl.domain.search.CertifiedProductFlatSearchResult;
import gov.healthit.chpl.domain.search.SearchRequest;
import gov.healthit.chpl.domain.search.SearchResponse;
import gov.healthit.chpl.manager.CertifiedProductSearchManager;

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
    public SearchResponse search(final SearchRequest searchRequest) {

        Collection<CertifiedProductBasicSearchResult> searchResults = searchDao.search(searchRequest);
        int totalCountSearchResults = searchDao.getTotalResultCount(searchRequest);

        SearchResponse response = new SearchResponse(Integer.valueOf(totalCountSearchResults),
                searchResults, searchRequest.getPageSize(), searchRequest.getPageNumber());
        return response;
    }
}
