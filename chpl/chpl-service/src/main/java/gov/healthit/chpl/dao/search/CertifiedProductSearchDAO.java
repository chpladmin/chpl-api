package gov.healthit.chpl.dao.search;

import java.util.List;

import gov.healthit.chpl.domain.search.CertifiedProductFlatSearchResult;

public interface CertifiedProductSearchDAO {
	
	public Long getListingIdByUniqueChplNumber(String chplProductNumber);
	public List<CertifiedProductFlatSearchResult> getAllCertifiedProducts();
}
