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
import gov.healthit.chpl.domain.DescriptiveModel;
import gov.healthit.chpl.domain.KeyValueModel;
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
	public Set<KeyValueModel> getClassificationNames() {
		
		List<ProductClassificationTypeDTO> classificationTypes = productClassificationTypeDAO.findAll();
		Set<KeyValueModel> classificationTypeNames = new HashSet<KeyValueModel>();
		
		for (ProductClassificationTypeDTO dto : classificationTypes) {
			classificationTypeNames.add(new KeyValueModel(dto.getId(), dto.getName()));
		}
		
		return classificationTypeNames;
	}

	@Transactional
	@Override
	public Set<KeyValueModel> getEditionNames() {
		
		List<CertificationEditionDTO> certificationEditions = certificationEditionDAO.findAll();
		Set<KeyValueModel> editionNames = new HashSet<KeyValueModel>();
		
		for (CertificationEditionDTO dto : certificationEditions) {
			editionNames.add(new KeyValueModel(dto.getId(), dto.getYear()));
		}
		
		return editionNames;
	}

	@Transactional
	@Override
	public Set<KeyValueModel> getCertificationStatuses() {
		List<CertificationStatusDTO> certificationStatuses = certificationStatusDao.findAll();
		Set<KeyValueModel> results = new HashSet<KeyValueModel>();
		
		for(CertificationStatusDTO dto : certificationStatuses) {
			results.add(new KeyValueModel(dto.getId(), dto.getStatus()));
		}
		
		return results;
	}
	
	@Transactional
	@Override
	public Set<KeyValueModel> getPracticeTypeNames() {
		
		List<PracticeTypeDTO> practiceTypeDTOs = practiceTypeDAO.findAll();
		Set<KeyValueModel> practiceTypeNames = new HashSet<KeyValueModel>();
		
		for (PracticeTypeDTO dto : practiceTypeDTOs) {
			practiceTypeNames.add(new KeyValueModel(dto.getId(), dto.getName()));
		}
		
		return practiceTypeNames;
	}

	@Transactional
	@Override
	public Set<KeyValueModel> getProductNames() {
		
		List<ProductDTO> productDTOs = this.productDAO.findAll();
		Set<KeyValueModel> productNames = new HashSet<KeyValueModel>();
		
		for (ProductDTO dto : productDTOs) {
			productNames.add(new KeyValueModel(dto.getId(), dto.getName()));
		}
		
		return productNames;
	}

	@Transactional
	@Override
	public Set<KeyValueModel> getVendorNames() {
		
		List<VendorDTO> vendorDTOs = this.vendorDAO.findAll();
		Set<KeyValueModel> vendorNames = new HashSet<KeyValueModel>();
		
		for (VendorDTO dto : vendorDTOs) {
			vendorNames.add(new KeyValueModel(dto.getId(), dto.getName()));
		}
		
		return vendorNames;
	}

	@Transactional
	@Override
	public Set<KeyValueModel> getCertBodyNames() {
		
		List<CertificationBodyDTO> dtos = this.certificationBodyDAO.findAll();
		Set<KeyValueModel> acbNames = new HashSet<KeyValueModel>();
		
		for (CertificationBodyDTO dto : dtos) {
			acbNames.add(new KeyValueModel(dto.getId(), dto.getName()));
		}
		
		return acbNames;
	}
	
	@Transactional
	@Override
	public Set<DescriptiveModel> getCertificationCriterionNumbers(){

		List<CertificationCriterionDTO> dtos = this.certificationCriterionDAO.findAll();
		Set<DescriptiveModel> criterionNames = new HashSet<DescriptiveModel>();
		
		for (CertificationCriterionDTO dto : dtos) {
			criterionNames.add( new DescriptiveModel(dto.getId(), dto.getNumber(), dto.getTitle()));
		}
		
		return criterionNames;
		
	}
	
	@Transactional
	@Override
	public Set<DescriptiveModel> getCQMCriterionNumbers(){

		List<CQMCriterionDTO> dtos = this.cqmCriterionDAO.findAll();
		Set<DescriptiveModel> criterionNames = new HashSet<DescriptiveModel>();
		
		for (CQMCriterionDTO dto : dtos) {
			
			String idNumber;
			
			if (dto.getCmsId() != null){
				idNumber = dto.getCmsId();
			} else {
				idNumber = dto.getNqfNumber();
			}
			
			criterionNames.add( new DescriptiveModel(dto.getId(), idNumber, dto.getTitle()));
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
