package gov.healthit.chpl.manager;

import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;

public interface CertifiedProductDetailsManager {
    CertifiedProductSearchDetails getCertifiedProductDetails(Long certifiedProductId)
            throws EntityRetrievalException;
    
    CertifiedProductSearchDetails getCertifiedProductDetails(Long certifiedProductId,
            Boolean retrieveAsynchronously) throws EntityRetrievalException;
}
