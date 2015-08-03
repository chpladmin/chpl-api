package gov.healthit.chpl.manager;

import gov.healthit.chpl.domain.CQMResult;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.CertifiedProductSearchResult;

import java.util.List;

public interface CertifiedProductSearchManager {
	

	public List<CertifiedProductSearchResult> search(String query);
	public List<CertifiedProductSearchResult> getAllCertifiedProducts();
	public CertifiedProductSearchDetails getCertifiedProductDetails(Long certifiedProductId);
	public List<CertificationResult> getCertifications();
	public List<CQMResult> getCQMResults();
	public List<String> getClassificationNames();
	public List<String> getEditionNames();
	public List<String> getPracticeTypeNames();
	public List<String> getProductNames();
	public List<String> getVendorNames();
	public List<String> getCertBodyNames();

	
}
