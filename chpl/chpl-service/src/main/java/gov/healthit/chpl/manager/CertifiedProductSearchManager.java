package gov.healthit.chpl.manager;

import java.util.List;

import gov.healthit.chpl.domain.SearchRequest;
import gov.healthit.chpl.domain.SearchResponse;
import gov.healthit.chpl.domain.search.CertifiedProductFlatSearchResult;

public interface CertifiedProductSearchManager {

    List<CertifiedProductFlatSearchResult> search();

    SearchResponse search(SearchRequest searchRequest);
}
