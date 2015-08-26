package gov.healthit.chpl.manager.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.dao.CQMResultDAO;
import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.dao.CertificationEditionDAO;
import gov.healthit.chpl.dao.CertificationResultDAO;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.PracticeTypeDAO;
import gov.healthit.chpl.dao.ProductClassificationTypeDAO;
import gov.healthit.chpl.dao.ProductDAO;
import gov.healthit.chpl.dao.ProductVersionDAO;
import gov.healthit.chpl.dao.VendorDAO;
import gov.healthit.chpl.domain.PopulateSearchOptions;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.CertificationEditionDTO;
import gov.healthit.chpl.dto.PracticeTypeDTO;
import gov.healthit.chpl.dto.ProductClassificationTypeDTO;
import gov.healthit.chpl.dto.ProductDTO;
import gov.healthit.chpl.dto.VendorDTO;
import gov.healthit.chpl.manager.SearchMenuManager;

@Service
public class SearchMenuManagerImpl implements SearchMenuManager {
	
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
	
	
	@Transactional
	@Override
	public Set<String> getClassificationNames() {
		
		List<ProductClassificationTypeDTO> classificationTypes = productClassificationTypeDAO.findAll();
		Set<String> classificationTypeNames = new HashSet<String>();
		
		for (ProductClassificationTypeDTO dto : classificationTypes) {
			classificationTypeNames.add(dto.getName());
		}
		
		return classificationTypeNames;
	}

	@Transactional
	@Override
	public Set<String> getEditionNames() {
		
		List<CertificationEditionDTO> certificationEditions = certificationEditionDAO.findAll();
		Set<String> editionNames = new HashSet<String>();
		
		for (CertificationEditionDTO dto : certificationEditions) {
			editionNames.add(dto.getYear());
		}
		
		return editionNames;
	}

	@Transactional
	@Override
	public Set<String> getPracticeTypeNames() {
		
		List<PracticeTypeDTO> practiceTypeDTOs = practiceTypeDAO.findAll();
		Set<String> practiceTypeNames = new HashSet<String>();
		
		for (PracticeTypeDTO dto : practiceTypeDTOs) {
			practiceTypeNames.add(dto.getName());
		}
		
		return practiceTypeNames;
	}

	@Transactional
	@Override
	public Set<String> getProductNames() {
		
		List<ProductDTO> productDTOs = this.productDAO.findAll();
		Set<String> productNames = new HashSet<String>();
		
		for (ProductDTO dto : productDTOs) {
			productNames.add(dto.getName());
		}
		
		return productNames;
	}

	@Transactional
	@Override
	public Set<String> getVendorNames() {
		
		List<VendorDTO> vendorDTOs = this.vendorDAO.findAll();
		Set<String> vendorNames = new HashSet<String>();
		
		for (VendorDTO dto : vendorDTOs) {
			vendorNames.add(dto.getName());
		}
		
		return vendorNames;
	}

	@Transactional
	@Override
	public Set<String> getCertBodyNames() {
		
		List<CertificationBodyDTO> dtos = this.certificationBodyDAO.findAll();
		Set<String> acbNames = new HashSet<String>();
		
		for (CertificationBodyDTO dto : dtos) {
			acbNames.add(dto.getName());
		}
		
		return acbNames;
	}

	@Transactional
	@Override
	public PopulateSearchOptions getPopulateSearchOptions() {
		
		PopulateSearchOptions searchOptions = new PopulateSearchOptions();
		searchOptions.setCertBodyNames(this.getCertBodyNames());
		searchOptions.setEditions(this.getEditionNames());
		searchOptions.setPracticeTypeNames(this.getPracticeTypeNames());
		searchOptions.setProductClassifications(this.getClassificationNames());
		searchOptions.setProductNames(this.getProductNames());
		searchOptions.setVendorNames(this.getVendorNames());
		return searchOptions;
		
	}

}
