package gov.healthit.chpl.manager.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import gov.healthit.chpl.dao.CQMResultDAO;
import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.dao.CertificationResultDAO;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.domain.CQMResult;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.CertifiedProductSearchResult;
import gov.healthit.chpl.domain.PopulateSearchOptions;
import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.manager.CertifiedProductSearchManager;

public class CertifiedProductSearchManagerImpl implements CertifiedProductSearchManager {

	@Autowired
	private CertificationBodyDAO certificationBodyDAO;
	
	@Autowired
	private CertifiedProductDAO certifiedProductDAO;
	
	@Autowired
	private CertificationResultDAO certificationResultDAO;
	
	@Autowired
	private CQMResultDAO cqmResultDAO;
	
	@Override
	public List<CertifiedProductSearchResult> search(String query) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<CertifiedProductSearchResult> getAllCertifiedProducts() {
		
		List<CertifiedProductSearchResult> searchResults = new ArrayList<>();
		
		for (CertifiedProductDTO dto : certifiedProductDAO.findAll()){
			
			CertifiedProductSearchResult searchResult = new CertifiedProductSearchResult();
			
			searchResult.setId(id);
			searchResult.setCertificationEdition(dto.getCertificationEditionId());
			searchResult.setCertifyingBody(
					certificationBodyDAO.getById(dto.getCertificationBodyId())
					.getName()
					);
			searchResult.setCertsAndCQMs(certsAndCQMs);
			searchResult.setChplNum(dto.getChplProductNumber());
			searchResult.setClassification(dto.getProductClassificationTypeId());
			searchResult.setPracticeType(dto.getPracticeTypeId());
			searchResult.setProduct();
			searchResult.setVendor(dto.get);
			searchResult.setVersion(version);
			
		}
		
		return ;
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

	@Override
	public PopulateSearchOptions getPopulateSearchOptions() {
		// TODO Auto-generated method stub
		return null;
	}

}
