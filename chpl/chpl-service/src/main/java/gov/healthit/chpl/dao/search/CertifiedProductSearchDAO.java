package gov.healthit.chpl.dao.search;

import java.util.List;

import gov.healthit.chpl.domain.search.CertifiedProductBasicSearchResult;

public interface CertifiedProductSearchDAO {
	
	public List<CertifiedProductBasicSearchResult> getAllCertifiedProducts();
}
