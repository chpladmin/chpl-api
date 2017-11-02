package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.domain.search.SearchRequest;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;

public interface CertifiedProductSearchResultDAO {

    CertifiedProductDetailsDTO getById(Long productId) throws EntityRetrievalException;

    // public CertifiedProductDetailsDTO getAllDetailsById(Long productId)
    // throws EntityRetrievalException;
    List<CertifiedProductDetailsDTO> search(SearchRequest searchRequest);

    Long countMultiFilterSearchResults(SearchRequest searchRequest);

}
