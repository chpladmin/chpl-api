package gov.healthit.chpl.manager.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.dao.AccessibilityStandardDAO;
import gov.healthit.chpl.dao.AgeRangeDAO;
import gov.healthit.chpl.dao.CQMCriterionDAO;
import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.dao.CertificationCriterionDAO;
import gov.healthit.chpl.dao.CertificationEditionDAO;
import gov.healthit.chpl.dao.CertificationStatusDAO;
import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.dao.DeveloperStatusDAO;
import gov.healthit.chpl.dao.EducationTypeDAO;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dao.PracticeTypeDAO;
import gov.healthit.chpl.dao.ProductClassificationTypeDAO;
import gov.healthit.chpl.dao.ProductDAO;
import gov.healthit.chpl.dao.QmsStandardDAO;
import gov.healthit.chpl.dao.SurveillanceDAO;
import gov.healthit.chpl.dao.TargetedUserDAO;
import gov.healthit.chpl.dao.TestFunctionalityDAO;
import gov.healthit.chpl.dao.TestStandardDAO;
import gov.healthit.chpl.dao.TestToolDAO;
import gov.healthit.chpl.dao.UcdProcessDAO;
import gov.healthit.chpl.domain.DescriptiveModel;
import gov.healthit.chpl.domain.KeyValueModel;
import gov.healthit.chpl.domain.KeyValueModelStatuses;
import gov.healthit.chpl.domain.NonconformityType;
import gov.healthit.chpl.domain.PopulateSearchOptions;
import gov.healthit.chpl.domain.RequirementTypeEnum;
import gov.healthit.chpl.domain.SurveillanceNonconformityStatus;
import gov.healthit.chpl.domain.SurveillanceRequirementType;
import gov.healthit.chpl.domain.SurveillanceRequirementOptions;
import gov.healthit.chpl.domain.SurveillanceResultType;
import gov.healthit.chpl.domain.SurveillanceType;
import gov.healthit.chpl.domain.TestTool;
import gov.healthit.chpl.dto.AccessibilityStandardDTO;
import gov.healthit.chpl.dto.AgeRangeDTO;
import gov.healthit.chpl.dto.CQMCriterionDTO;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.dto.CertificationEditionDTO;
import gov.healthit.chpl.dto.CertificationStatusDTO;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.dto.DeveloperStatusDTO;
import gov.healthit.chpl.dto.EducationTypeDTO;
import gov.healthit.chpl.dto.PracticeTypeDTO;
import gov.healthit.chpl.dto.ProductClassificationTypeDTO;
import gov.healthit.chpl.dto.ProductDTO;
import gov.healthit.chpl.dto.QmsStandardDTO;
import gov.healthit.chpl.dto.TargetedUserDTO;
import gov.healthit.chpl.dto.TestFunctionalityDTO;
import gov.healthit.chpl.dto.TestStandardDTO;
import gov.healthit.chpl.dto.TestToolDTO;
import gov.healthit.chpl.dto.UcdProcessDTO;
import gov.healthit.chpl.manager.SearchMenuManager;

@Service
public class SearchMenuManagerImpl implements SearchMenuManager {
	
	@Autowired
	private CertificationBodyDAO certificationBodyDAO;
	
	@Autowired
	private CQMCriterionDAO cqmCriterionDAO;
	
	@Autowired
	private CertificationCriterionDAO certificationCriterionDAO;
	
	@Autowired
	private CertificationEditionDAO certificationEditionDAO;
	
	@Autowired
	private CertificationStatusDAO certificationStatusDao;
	
	@Autowired private EducationTypeDAO educationTypeDao;
	@Autowired private AgeRangeDAO ageRangeDao;
	@Autowired private TestFunctionalityDAO testFuncDao;
	@Autowired private TestStandardDAO testStandardDao;
	@Autowired private TestToolDAO testToolsDao;
	@Autowired private AccessibilityStandardDAO asDao;
	@Autowired private UcdProcessDAO ucdDao;
	@Autowired private QmsStandardDAO qmsDao;
	@Autowired private TargetedUserDAO tuDao;
	@Autowired private DeveloperStatusDAO devStatusDao;
	@Autowired private SurveillanceDAO survDao;
	
	@Autowired
	private ProductClassificationTypeDAO productClassificationTypeDAO;
	
	@Autowired
	private ProductDAO productDAO;
	
	@Autowired
	private PracticeTypeDAO practiceTypeDAO;
	
	@Autowired
	private DeveloperDAO developerDAO;
	
	
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
	public Set<KeyValueModel> getEditionNames(Boolean simple) {
		
		
		
		List<CertificationEditionDTO> certificationEditions = certificationEditionDAO.findAll();
		Set<KeyValueModel> editionNames = new HashSet<KeyValueModel>();
		
		for (CertificationEditionDTO dto : certificationEditions) {
			
			if (simple){
				if (dto.getYear().equals("2011")){
					continue;
				}
			}
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
	public Set<KeyValueModelStatuses> getProductNames() {
		
		List<ProductDTO> productDTOs = this.productDAO.findAll();
		Set<KeyValueModelStatuses> productNames = new HashSet<KeyValueModelStatuses>();
		
		for (ProductDTO dto : productDTOs) {
			productNames.add(new KeyValueModelStatuses(dto.getId(), dto.getName(), dto.getStatuses()));
		}
		
		return productNames;
	}

	@Transactional
	@Override
	public Set<KeyValueModelStatuses> getDeveloperNames() {
		
		List<DeveloperDTO> developerDTOs = this.developerDAO.findAll();
		Set<KeyValueModelStatuses> developerNames = new HashSet<KeyValueModelStatuses>();
		
		for (DeveloperDTO dto : developerDTOs) {
			developerNames.add(new KeyValueModelStatuses(dto.getId(), dto.getName(), dto.getStatuses()));
		}
		
		return developerNames;
	}

	@Transactional
	@Override
	public Set<KeyValueModel> getCertBodyNames() {
		
		List<CertificationBodyDTO> dtos = this.certificationBodyDAO.findAll(false);
		Set<KeyValueModel> acbNames = new HashSet<KeyValueModel>();
		
		for (CertificationBodyDTO dto : dtos) {
			acbNames.add(new KeyValueModel(dto.getId(), dto.getName()));
		}
		
		return acbNames;
	}
	
	@Transactional
	@Override
	public Set<KeyValueModel> getEducationTypes() {
		
		List<EducationTypeDTO> dtos = this.educationTypeDao.getAll();
		Set<KeyValueModel> educationTypes = new HashSet<KeyValueModel>();
		
		for (EducationTypeDTO dto : dtos) {
			educationTypes.add(new KeyValueModel(dto.getId(), dto.getName()));
		}
		
		return educationTypes;
	}
	
	@Transactional
	@Override
	public Set<KeyValueModel> getAgeRanges() {
		
		List<AgeRangeDTO> dtos = this.ageRangeDao.getAll();
		Set<KeyValueModel> ageRanges = new HashSet<KeyValueModel>();
		
		for (AgeRangeDTO dto : dtos) {
			ageRanges.add(new KeyValueModel(dto.getId(), dto.getAge()));
		}
		
		return ageRanges;
	}
	
	@Transactional
	@Override
	public Set<KeyValueModel> getTestFunctionality() {
		
		List<TestFunctionalityDTO> dtos = this.testFuncDao.findAll();
		Set<KeyValueModel> testFuncs = new HashSet<KeyValueModel>();
		
		for (TestFunctionalityDTO dto : dtos) {
			testFuncs.add(new KeyValueModel(dto.getId(), dto.getNumber(), dto.getName()));
		}
		
		return testFuncs;
	}
	
	@Transactional
	@Override
	public Set<KeyValueModel> getTestTools() {
		
		List<TestToolDTO> dtos = this.testToolsDao.findAll();
		Set<KeyValueModel> testTools = new HashSet<KeyValueModel>();
		
		for (TestToolDTO dto : dtos) {
			TestTool tt = new TestTool(dto.getId(), dto.getName(), dto.getDescription());
			tt.setRetired(dto.isRetired());
			testTools.add(tt);
		}
		
		return testTools;
	}
	
	@Override
	public Set<KeyValueModel> getDeveloperStatuses() {
		List<DeveloperStatusDTO> dtos = this.devStatusDao.findAll();
		Set<KeyValueModel> statuses = new HashSet<KeyValueModel>();
		
		for (DeveloperStatusDTO dto : dtos) {
			statuses.add(new KeyValueModel(dto.getId(), dto.getStatusName()));
		}
		
		return statuses;
	}
	
	@Override
	public Set<KeyValueModel> getAccessibilityStandards() {
		List<AccessibilityStandardDTO> dtos = this.asDao.findAll();
		Set<KeyValueModel> standards = new HashSet<KeyValueModel>();
		
		for(AccessibilityStandardDTO dto : dtos) {
			standards.add(new KeyValueModel(dto.getId(), dto.getName()));
		}
		return standards;
	}

	@Override
	public Set<KeyValueModel> getUcdProcesses() {
		List<UcdProcessDTO> dtos = this.ucdDao.findAll();
		Set<KeyValueModel> ucds = new HashSet<KeyValueModel>();
		
		for(UcdProcessDTO dto : dtos) {
			ucds.add(new KeyValueModel(dto.getId(), dto.getName()));
		}
		return ucds;
	}

	@Override
	public Set<KeyValueModel> getQmsStandards() {
		List<QmsStandardDTO> dtos = this.qmsDao.findAll();
		Set<KeyValueModel> qms = new HashSet<KeyValueModel>();
		
		for(QmsStandardDTO dto : dtos) {
			qms.add(new KeyValueModel(dto.getId(), dto.getName()));
		}
		return qms;
	}
	
	@Override
	public Set<KeyValueModel> getTargetedUesrs() {
		List<TargetedUserDTO> dtos = this.tuDao.findAll();
		Set<KeyValueModel> standards = new HashSet<KeyValueModel>();
		
		for(TargetedUserDTO dto : dtos) {
			standards.add(new KeyValueModel(dto.getId(), dto.getName()));
		}
		return standards;
	}

	@Override
	public Set<KeyValueModel> getTestStandards() {
		List<TestStandardDTO> dtos = testStandardDao.findAll();
		Set<KeyValueModel> std = new HashSet<KeyValueModel>();
		
		for(TestStandardDTO dto : dtos) {
			std.add(new KeyValueModel(dto.getId(), dto.getName(), dto.getDescription()));
		}
		return std;
	}
	
	@Override
	public Set<KeyValueModel> getSurveillanceTypes() {
		List<SurveillanceType> daoResults = survDao.getAllSurveillanceTypes();
		Set<KeyValueModel> results = new HashSet<KeyValueModel>();
		
		for(SurveillanceType result : daoResults) {
			results.add(new KeyValueModel(result.getId(), result.getName()));
		}
		return results;
	}
	
	@Override
	public SurveillanceRequirementOptions getSurveillanceRequirementOptions() {
		SurveillanceRequirementOptions result = new SurveillanceRequirementOptions();
		
		List<CertificationCriterionDTO> criteria2014 = certificationCriterionDAO.findByCertificationEditionYear("2014");
		for(CertificationCriterionDTO crit : criteria2014) {
			result.getCriteriaOptions2014().add(new KeyValueModel(crit.getId(), crit.getNumber(), crit.getDescription()));
		}
		List<CertificationCriterionDTO> criteria2015 = certificationCriterionDAO.findByCertificationEditionYear("2015");
		for(CertificationCriterionDTO crit : criteria2015) {
			result.getCriteriaOptions2015().add(new KeyValueModel(crit.getId(), crit.getNumber(), crit.getDescription()));
		}
		
		result.getTransparencyOptions().add(RequirementTypeEnum.K1.getName());
		result.getTransparencyOptions().add(RequirementTypeEnum.K2.getName());
		return result;
	}
	
	@Override
	public Set<KeyValueModel> getNonconformityTypeOptions() {
		Set<KeyValueModel> result = new HashSet<KeyValueModel>();
		
		List<CertificationCriterionDTO> criteria2014 = certificationCriterionDAO.findByCertificationEditionYear("2014");
		for(CertificationCriterionDTO crit : criteria2014) {
			result.add(new KeyValueModel(crit.getId(), crit.getNumber(), crit.getDescription()));
		}
		List<CertificationCriterionDTO> criteria2015 = certificationCriterionDAO.findByCertificationEditionYear("2015");
		for(CertificationCriterionDTO crit : criteria2015) {
			result.add(new KeyValueModel(crit.getId(), crit.getNumber(), crit.getDescription()));
		}
		
		result.add(new KeyValueModel(null, NonconformityType.K1.getName()));
		result.add(new KeyValueModel(null, NonconformityType.K2.getName()));
		result.add(new KeyValueModel(null, NonconformityType.L.getName()));
		result.add(new KeyValueModel(null, NonconformityType.OTHER.getName()));
		return result;
	}
	
	@Override
	public Set<KeyValueModel> getSurveillanceRequirementTypes() {
		List<SurveillanceRequirementType> daoResults = survDao.getAllSurveillanceRequirementTypes();
		Set<KeyValueModel> results = new HashSet<KeyValueModel>();
		
		for(SurveillanceRequirementType result : daoResults) {
			results.add(new KeyValueModel(result.getId(), result.getName()));
		}
		return results;
	}
	
	@Override
	public Set<KeyValueModel> getSurveillanceResultTypes() {
		List<SurveillanceResultType> daoResults = survDao.getAllSurveillanceResultTypes();
		Set<KeyValueModel> results = new HashSet<KeyValueModel>();
		
		for(SurveillanceResultType result : daoResults) {
			results.add(new KeyValueModel(result.getId(), result.getName()));
		}
		return results;
	}
	
	@Override
	public Set<KeyValueModel> getNonconformityStatusTypes() {
		List<SurveillanceNonconformityStatus> daoResults = survDao.getAllSurveillanceNonconformityStatusTypes();
		Set<KeyValueModel> results = new HashSet<KeyValueModel>();
		
		for(SurveillanceNonconformityStatus result : daoResults) {
			results.add(new KeyValueModel(result.getId(), result.getName()));
		}
		return results;
	}
	
	@Transactional
	@Override
	public Set<DescriptiveModel> getCertificationCriterionNumbers(Boolean simple) throws EntityRetrievalException{

		List<CertificationCriterionDTO> dtos = this.certificationCriterionDAO.findAll();
		Set<DescriptiveModel> criterionNames = new HashSet<DescriptiveModel>();
		
		for (CertificationCriterionDTO dto : dtos) {
			if (simple){
				if (certificationEditionDAO.getById(dto.getCertificationEditionId()).getRetired().equals(true)) {
					continue;
				}
			}
			criterionNames.add( new DescriptiveModel(dto.getId(), dto.getNumber(), dto.getTitle()));
		}
		
		return criterionNames;
		
	}
	
	@Transactional
	@Override
	public Set<DescriptiveModel> getCQMCriterionNumbers(Boolean simple){

		List<CQMCriterionDTO> dtos = this.cqmCriterionDAO.findAll();
		Set<DescriptiveModel> criterionNames = new HashSet<DescriptiveModel>();
		
		for (CQMCriterionDTO dto : dtos) {
			
			String idNumber;
			
			if (simple){
				if (dto.getCmsId() != null){
					idNumber = dto.getCmsId();
				} else {
					continue;
				}
			} else {	
				if (dto.getCmsId() != null){
					idNumber = dto.getCmsId();
				} else {
					idNumber = dto.getNqfNumber();
				}
			}
			
			criterionNames.add( new DescriptiveModel(dto.getId(), idNumber, dto.getTitle()));
		}
		return criterionNames;
	}

	@Transactional
	@Override
	public PopulateSearchOptions getPopulateSearchOptions(Boolean simple) throws EntityRetrievalException {
		
		PopulateSearchOptions searchOptions = new PopulateSearchOptions();
		searchOptions.setCertBodyNames(this.getCertBodyNames());
		searchOptions.setEditions(this.getEditionNames(simple));
		searchOptions.setCertificationStatuses(this.getCertificationStatuses());
		searchOptions.setPracticeTypeNames(this.getPracticeTypeNames());
		searchOptions.setProductClassifications(this.getClassificationNames());
		searchOptions.setProductNames(this.getProductNames());
		searchOptions.setDeveloperNames(this.getDeveloperNames());
		searchOptions.setCqmCriterionNumbers(this.getCQMCriterionNumbers(simple));
		searchOptions.setCertificationCriterionNumbers(this.getCertificationCriterionNumbers(simple));
		
		return searchOptions;
		
	}
}
