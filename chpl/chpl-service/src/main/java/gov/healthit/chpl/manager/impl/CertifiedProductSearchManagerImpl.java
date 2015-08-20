package gov.healthit.chpl.manager.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import gov.healthit.chpl.dao.CQMResultDAO;
import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.dao.CertificationEditionDAO;
import gov.healthit.chpl.dao.CertificationResultDAO;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dao.PracticeTypeDAO;
import gov.healthit.chpl.dao.ProductClassificationTypeDAO;
import gov.healthit.chpl.dao.ProductDAO;
import gov.healthit.chpl.dao.ProductVersionDAO;
import gov.healthit.chpl.dao.VendorDAO;
import gov.healthit.chpl.domain.CQMResultDetails;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.CertifiedProductSearchResult;
import gov.healthit.chpl.domain.PopulateSearchOptions;
import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.dto.ProductDTO;
import gov.healthit.chpl.manager.CertifiedProductSearchManager;

@Service
public class CertifiedProductSearchManagerImpl implements CertifiedProductSearchManager {

	@Autowired
	private CertificationBodyDAO certificationBodyDAO;
	
	@Autowired
	private CertifiedProductDAO certifiedProductDAO;
	
	@Autowired
	private CertificationResultDAO certificationResultDAO;
	
	@Autowired
	private CQMResultDAO cqmResultDAO;
	
	@Autowired
	private CertificationEditionDAO certificationEditionDAO;
	
	@Autowired
	private ProductClassificationTypeDAO productClassificationTypeDAO;
	
	@Autowired
	private ProductVersionDAO productVersionDAO;
	
	@Autowired
	private ProductDAO productDAO;
	
	@Autowired
	private PracticeTypeDAO practiceTypeDAO;
	
	@Autowired
	private VendorDAO vendorDAO;
	
	
	@Override
	public List<CertifiedProductSearchResult> search(String query) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<CertifiedProductSearchResult> getAllCertifiedProducts() throws EntityRetrievalException {
		
		List<CertifiedProductSearchResult> searchResults = new ArrayList<>();
		
		for (CertifiedProductDTO dto : certifiedProductDAO.findAll()){
			
			CertifiedProductSearchResult searchResult = new CertifiedProductSearchResult();
			
			searchResult.setId(dto.getId());
			searchResult.setCertificationEdition(
					certificationEditionDAO.getById(dto.getCertificationEditionId())
					.getYear()
					);
			searchResult.setCertifyingBody(
					certificationBodyDAO.getById(dto.getCertificationBodyId())
					.getName()
					);
			//searchResult.setCertsAndCQMs(certsAndCQMs);
			searchResult.setChplNum(dto.getChplProductNumber());
			searchResult.setClassification(
					productClassificationTypeDAO.getById(dto.getProductClassificationTypeId()
					).getName()
					);
			
			
			Long practiceTypeId = dto.getPracticeTypeId();
			String practiceTypeName = practiceTypeDAO.getById(practiceTypeId).getName();
			searchResult.setPracticeType(practiceTypeName);
			
			Long productId = productVersionDAO.getById(dto.getProductVersionId()).getProductId();
			ProductDTO product =  productDAO.getById(productId);
			String productName = product.getName();
			searchResult.setProduct(productName);
			
			Long vendorId = product.getVendorId();
			String vendorName = vendorDAO.getById(vendorId).getName();
			searchResult.setVendor(vendorName);
			
			searchResult.setVersion(
					productVersionDAO.getById(dto.getProductVersionId())
					.getVersion()
					);
		}
		
		return searchResults;
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
	public List<CQMResultDetails> getCQMResults() {
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
