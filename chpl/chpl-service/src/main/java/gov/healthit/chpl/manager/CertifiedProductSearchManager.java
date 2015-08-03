package gov.healthit.chpl.manager;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.CertifiedProductSearchResult;

import java.util.List;

public interface CertifiedProductSearchManager {
	

	public List<CertifiedProductSearchResult> search(String query);
	public List<CertifiedProductSearchResult> getAll();
	public CertifiedProductSearchDetails getDetails(Long certifiedProductId);

}
