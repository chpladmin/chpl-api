package gov.healthit.chpl.certifiedproduct;

import java.util.List;

import gov.healthit.chpl.domain.CQMResultDetails;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationStatusEvent;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.ListingMeasure;
import gov.healthit.chpl.exception.EntityRetrievalException;

public interface CertifiedProductDetailsManager {
    CertifiedProductSearchDetails getCertifiedProductDetailsByChplProductNumber(String chplProductNumber) throws EntityRetrievalException;
    CertifiedProductSearchDetails getCertifiedProductDetails(Long certifiedProductId) throws EntityRetrievalException;
    CertifiedProductSearchDetails getCertifiedProductDetailsBasicByChplProductNumber(String chplProductNumber) throws EntityRetrievalException;
    CertifiedProductSearchDetails getCertifiedProductDetailsBasic(Long certifiedProductId) throws EntityRetrievalException;
    List<CQMResultDetails> getCertifiedProductCqms(Long certifiedProductId) throws EntityRetrievalException;
    List<CQMResultDetails> getCertifiedProductCqms(String chplProductNumber) throws EntityRetrievalException;
    List<CertificationResult> getCertifiedProductCertificationResults(Long certifiedProductId) throws EntityRetrievalException;
    List<CertificationResult> getCertifiedProductCertificationResults(String chplProductNumber) throws EntityRetrievalException;
    List<ListingMeasure> getCertifiedProductMeasures(Long listingId) throws EntityRetrievalException;
    List<ListingMeasure> getCertifiedProductMeasures(Long listingId, Boolean checkIfListingExists) throws EntityRetrievalException;
    List<ListingMeasure> getCertifiedProductMeasures(String chplProductNumber) throws EntityRetrievalException;
    List<CertificationStatusEvent> getCertificationStatusEvents(Long listingId) throws EntityRetrievalException;
}
