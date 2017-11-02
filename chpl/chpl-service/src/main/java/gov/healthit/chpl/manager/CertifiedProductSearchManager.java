package gov.healthit.chpl.manager;

import java.util.List;

import gov.healthit.chpl.domain.search.CertifiedProductFlatSearchResult;
import gov.healthit.chpl.domain.search.SearchRequest;
import gov.healthit.chpl.domain.search.SearchResponse;

public interface CertifiedProductSearchManager {

    List<CertifiedProductFlatSearchResult> search();

    SearchResponse search(SearchRequest searchRequest);
}
