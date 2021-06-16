package gov.healthit.chpl.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.search.CertifiedProductBasicSearchResult;
import gov.healthit.chpl.domain.search.SearchRequest;
import gov.healthit.chpl.manager.CertifiedProductSearchManager;
import lombok.NoArgsConstructor;

@Component("listingSearchService")
@NoArgsConstructor
public class ListingSearchService {
    private CertifiedProductSearchManager cpSearchManager;

    @Autowired
    public ListingSearchService(CertifiedProductSearchManager cpSearchManager) {
        this.cpSearchManager = cpSearchManager;
    }

    public List<CertifiedProductBasicSearchResult> search(SearchRequest searchRequest) {
        //TODO: validate parameters
        //TODO: trim parameters

        List<CertifiedProductBasicSearchResult> listings = cpSearchManager.getSearchListingCollection();
        //TODO: filter and sort everything

        return listings;
    }
}
