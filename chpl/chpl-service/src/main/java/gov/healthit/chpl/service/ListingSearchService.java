package gov.healthit.chpl.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.search.CertifiedProductBasicSearchResult;
import gov.healthit.chpl.domain.search.SearchRequest;
import gov.healthit.chpl.domain.search.SearchResponse;
import gov.healthit.chpl.exception.InvalidArgumentsException;
import gov.healthit.chpl.manager.CertifiedProductSearchManager;
import gov.healthit.chpl.manager.DimensionalDataManager;
import gov.healthit.chpl.util.ErrorMessageUtil;
import lombok.NoArgsConstructor;

@Component("listingSearchService")
@NoArgsConstructor
public class ListingSearchService {
    private static final int MAX_PAGE_SIZE = 100;

    private ErrorMessageUtil msgUtil;
    private DimensionalDataManager dimensionalDataManager;
    private CertifiedProductSearchManager cpSearchManager;

    @Autowired
    public ListingSearchService(ErrorMessageUtil msgUtil,
            DimensionalDataManager dimensionalDataManager,
            CertifiedProductSearchManager cpSearchManager) {
        this.msgUtil = msgUtil;
        this.dimensionalDataManager = dimensionalDataManager;
        this.cpSearchManager = cpSearchManager;
    }

    public SearchResponse search(SearchRequest searchRequest) throws InvalidArgumentsException {
        //TODO: validate parameters
        //TODO: trim parameters

        List<CertifiedProductBasicSearchResult> listings = cpSearchManager.getSearchListingCollection().subList(0, 5);
        //TODO: filter and sort everything
        //TODO: get total count of results
        SearchResponse response = new SearchResponse();
        response.setResults(listings);
        return response;
    }

}
