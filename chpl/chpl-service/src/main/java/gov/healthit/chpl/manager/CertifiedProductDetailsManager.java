package gov.healthit.chpl.manager;

import java.util.List;

import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.CQMResultDetails;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;

public interface CertifiedProductDetailsManager {
    CertifiedProductSearchDetails getCertifiedProductDetails(Long certifiedProductId)
            throws EntityRetrievalException;

    CertifiedProductSearchDetails getCertifiedProductDetails(Long certifiedProductId,
            Boolean retrieveAsynchronously) throws EntityRetrievalException;
    
    CertifiedProductSearchDetails getCertifiedProductDetailsBasic(Long certifiedProductId)
            throws EntityRetrievalException;
    
    CertifiedProductSearchDetails getCertifiedProductDetailsBasic(Long certifiedProductId,
            Boolean retrieveAsynchronously) throws EntityRetrievalException;
    
    List<CQMResultDetails> getCertifiedProductCqms(final Long certifiedProductId) 
            throws EntityRetrievalException;
    
    List<CertificationResult> getCertifiedProductCertificationResults(final Long certifiedProductId) 
            throws EntityRetrievalException;
}
