package gov.healthit.chpl.manager.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.dao.CQMCriterionDAO;
import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.dao.CertificationCriterionDAO;
import gov.healthit.chpl.dao.CertificationEditionDAO;
import gov.healthit.chpl.dao.CertificationStatusDAO;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.PracticeTypeDAO;
import gov.healthit.chpl.dao.ProductClassificationTypeDAO;
import gov.healthit.chpl.dao.ProductDAO;
import gov.healthit.chpl.dao.ProductVersionDAO;
import gov.healthit.chpl.dao.VendorDAO;
import gov.healthit.chpl.domain.PopulateSearchOptions;
import gov.healthit.chpl.domain.SimpleModel;
import gov.healthit.chpl.dto.CQMCriterionDTO;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.dto.CertificationEditionDTO;
import gov.healthit.chpl.dto.CertificationStatusDTO;
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
	private CQMCriterionDAO cqmCriterionDAO;
	
	@Autowired
	private CertificationCriterionDAO certificationCriterionDAO;
	
	@Autowired
	private CertificationEditionDAO certificationEditionDAO;
	
	@Autowired
	private CertificationStatusDAO certificationStatusDao;
	
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
	public Set<SimpleModel> getClassificationNames() {
		
		List<ProductClassificationTypeDTO> classificationTypes = productClassificationTypeDAO.findAll();
		Set<SimpleModel> classificationTypeNames = new HashSet<SimpleModel>();
		
		for (ProductClassificationTypeDTO dto : classificationTypes) {
			classificationTypeNames.add(new SimpleModel(dto.getId(), dto.getName()));
		}
		
		return classificationTypeNames;
	}

	@Transactional
	@Override
	public Set<SimpleModel> getEditionNames() {
		
		List<CertificationEditionDTO> certificationEditions = certificationEditionDAO.findAll();
		Set<SimpleModel> editionNames = new HashSet<SimpleModel>();
		
		for (CertificationEditionDTO dto : certificationEditions) {
			editionNames.add(new SimpleModel(dto.getId(), dto.getYear()));
		}
		
		return editionNames;
	}

	@Transactional
	@Override
	public Set<SimpleModel> getCertificationStatuses() {
		List<CertificationStatusDTO> certificationStatuses = certificationStatusDao.findAll();
		Set<SimpleModel> results = new HashSet<SimpleModel>();
		
		for(CertificationStatusDTO dto : certificationStatuses) {
			results.add(new SimpleModel(dto.getId(), dto.getStatus()));
		}
		
		return results;
	}
	
	@Transactional
	@Override
	public Set<SimpleModel> getPracticeTypeNames() {
		
		List<PracticeTypeDTO> practiceTypeDTOs = practiceTypeDAO.findAll();
		Set<SimpleModel> practiceTypeNames = new HashSet<SimpleModel>();
		
		for (PracticeTypeDTO dto : practiceTypeDTOs) {
			practiceTypeNames.add(new SimpleModel(dto.getId(), dto.getName()));
		}
		
		return practiceTypeNames;
	}

	@Transactional
	@Override
	public Set<SimpleModel> getProductNames() {
		
		List<ProductDTO> productDTOs = this.productDAO.findAll();
		Set<SimpleModel> productNames = new HashSet<SimpleModel>();
		
		for (ProductDTO dto : productDTOs) {
			productNames.add(new SimpleModel(dto.getId(), dto.getName()));
		}
		
		return productNames;
	}

	@Transactional
	@Override
	public Set<SimpleModel> getVendorNames() {
		
		List<VendorDTO> vendorDTOs = this.vendorDAO.findAll();
		Set<SimpleModel> vendorNames = new HashSet<SimpleModel>();
		
		for (VendorDTO dto : vendorDTOs) {
			vendorNames.add(new SimpleModel(dto.getId(), dto.getName()));
		}
		
		return vendorNames;
	}

	@Transactional
	@Override
	public Set<SimpleModel> getCertBodyNames() {
		
		List<CertificationBodyDTO> dtos = this.certificationBodyDAO.findAll();
		Set<SimpleModel> acbNames = new HashSet<SimpleModel>();
		
		for (CertificationBodyDTO dto : dtos) {
			acbNames.add(new SimpleModel(dto.getId(), dto.getName()));
		}
		
		return acbNames;
	}
	
	@Transactional
	@Override
	public Set<SimpleModel> getCertificationCriterionNumbers(){

		List<CertificationCriterionDTO> dtos = this.certificationCriterionDAO.findAll();
		Set<SimpleModel> criterionNames = new HashSet<SimpleModel>();
		
		for (CertificationCriterionDTO dto : dtos) {
			criterionNames.add(new SimpleModel(dto.getId(), dto.getNumber()));
		}
		
		return criterionNames;
		
	}
	
	@Transactional
	@Override
	public Set<SimpleModel> getCQMCriterionNumbers(){

		List<CQMCriterionDTO> dtos = this.cqmCriterionDAO.findAll();
		Set<SimpleModel> criterionNames = new HashSet<SimpleModel>();
		
		for (CQMCriterionDTO dto : dtos) {
			criterionNames.add(new SimpleModel(dto.getId(), dto.getNumber()));
		}
		return criterionNames;
	}
	

	@Transactional
	@Override
	public PopulateSearchOptions getPopulateSearchOptions() {
		
		PopulateSearchOptions searchOptions = new PopulateSearchOptions();
		searchOptions.setCertBodyNames(this.getCertBodyNames());
		searchOptions.setEditions(this.getEditionNames());
		searchOptions.setCertificationStatuses(this.getCertificationStatuses());
		searchOptions.setPracticeTypeNames(this.getPracticeTypeNames());
		searchOptions.setProductClassifications(this.getClassificationNames());
		searchOptions.setProductNames(this.getProductNames());
		searchOptions.setVendorNames(this.getVendorNames());
		searchOptions.setCqmCriterionNumbers(this.getCQMCriterionNumbers());
		searchOptions.setCertificationCriterionNumbers(this.getCertificationCriterionNumbers());
		
		return searchOptions;
		
	}

}
