package gov.healthit.chpl.dao.search;

import java.util.List;

import gov.healthit.chpl.domain.search.CertifiedProductBasicSearchResult;
import gov.healthit.chpl.domain.search.CertifiedProductFlatSearchResult;

public interface CertifiedProductSearchDAO {
	
	public List<CertifiedProductFlatSearchResult> getAllCertifiedProducts();
}
