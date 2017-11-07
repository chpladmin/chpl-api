package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.domain.search.SearchRequest;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;

public interface CertifiedProductSearchResultDAO {

    CertifiedProductDetailsDTO getById(Long productId) throws EntityRetrievalException;

}
