package gov.healthit.chpl.manager.impl;

import java.util.List;

import gov.healthit.chpl.domain.CQMResult;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.CertifiedProductSearchResult;
import gov.healthit.chpl.manager.CertifiedProductSearchManager;

public class CertifiedProductSearchManagerImpl implements CertifiedProductSearchManager {

	//TODO Add CertificationResultDAO
	//TODO add CQMResultDAO
	
	@Override
	public List<CertifiedProductSearchResult> search(String query) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<CertifiedProductSearchResult> getAllCertifiedProducts() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CertifiedProductSearchDetails getCertifiedProductDetails(
			Long certifiedProductId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<CertificationResult> getCertifications() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<CQMResult> getCQMResults() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getClassificationNames() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getEditionNames() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getPracticeTypeNames() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getProductNames() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getVendorNames() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getCertBodyNames() {
		// TODO Auto-generated method stub
		return null;
	}

}
