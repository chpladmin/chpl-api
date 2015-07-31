package gov.healthit.chpl.manager.impl;

import java.util.List;

import gov.healthit.chpl.json.CertifiedProductSearchDetailsJSONObject;
import gov.healthit.chpl.json.CertifiedProductSearchResultJSONObject;
import gov.healthit.chpl.manager.CertifiedProductSearchManager;

public class CertifiedProductSearchManagerImpl implements CertifiedProductSearchManager {

	//TODO Add CertificationResultDAO
	//TODO add CQMResultDAO
	
	@Override
	public List<CertifiedProductSearchResultJSONObject> search(String query) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<CertifiedProductSearchResultJSONObject> getAll() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CertifiedProductSearchDetailsJSONObject getDetails(
			Long certifiedProductId) {
		// TODO Auto-generated method stub
		return null;
	}

}
