package gov.healthit.chpl.manager;

import gov.healthit.chpl.domain.CertifiedProductSearchDetailsJSONObject;
import gov.healthit.chpl.domain.CertifiedProductSearchResultJSONObject;

import java.util.List;

public interface CertifiedProductSearchManager {
	

	public List<CertifiedProductSearchResultJSONObject> search(String query);
	public List<CertifiedProductSearchResultJSONObject> getAll();
	public CertifiedProductSearchDetailsJSONObject getDetails(Long certifiedProductId);

}
