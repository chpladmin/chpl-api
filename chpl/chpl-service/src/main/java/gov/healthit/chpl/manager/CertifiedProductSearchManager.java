package gov.healthit.chpl.manager;

import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.caching.CacheNames;
import gov.healthit.chpl.dao.CertifiedProductSearchDAO;
import gov.healthit.chpl.domain.search.CertifiedProductBasicSearchResult;
import gov.healthit.chpl.domain.search.CertifiedProductFlatSearchResult;
import gov.healthit.chpl.domain.search.SearchRequest;
import gov.healthit.chpl.domain.search.SearchResponse;

@Service
public class CertifiedProductSearchManager {
    private CertifiedProductSearchDAO searchDao;

    @Autowired
    public CertifiedProductSearchManager(CertifiedProductSearchDAO searchDao) {
        this.searchDao = searchDao;
    }

    @Transactional(readOnly = true)
    @Cacheable(CacheNames.COLLECTIONS_LISTINGS)
    public List<CertifiedProductFlatSearchResult> search() {
        List<CertifiedProductFlatSearchResult> results = searchDao.getAllCertifiedProducts()
                //TODO remove
                .subList(0, 10);
        return results;
    }

    @Transactional
    public SearchResponse search(SearchRequest searchRequest) {

        Collection<CertifiedProductBasicSearchResult> searchResults = searchDao.search(searchRequest);
        int totalCountSearchResults = searchDao.getTotalResultCount(searchRequest);

        SearchResponse response = new SearchResponse(Integer.valueOf(totalCountSearchResults),
                searchResults, searchRequest.getPageSize(), searchRequest.getPageNumber());
        return response;
    }
}
