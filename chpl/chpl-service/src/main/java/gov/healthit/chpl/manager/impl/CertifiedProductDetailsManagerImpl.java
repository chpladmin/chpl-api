package gov.healthit.chpl.manager.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.concurrent.SynchronousQueue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.dao.CQMCriterionDAO;
import gov.healthit.chpl.dao.CQMResultDAO;
import gov.healthit.chpl.dao.CQMResultDetailsDAO;
import gov.healthit.chpl.dao.CertificationResultDetailsDAO;
import gov.healthit.chpl.dao.CertificationStatusDAO;
import gov.healthit.chpl.dao.CertificationStatusEventDAO;
import gov.healthit.chpl.dao.CertifiedProductAccessibilityStandardDAO;
import gov.healthit.chpl.dao.CertifiedProductQmsStandardDAO;
import gov.healthit.chpl.dao.CertifiedProductSearchResultDAO;
import gov.healthit.chpl.dao.CertifiedProductTargetedUserDAO;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dao.ListingGraphDAO;
import gov.healthit.chpl.dao.MacraMeasureDAO;
import gov.healthit.chpl.domain.CQMCriterion;
import gov.healthit.chpl.domain.CQMResultCertification;
import gov.healthit.chpl.domain.CQMResultDetails;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultAdditionalSoftware;
import gov.healthit.chpl.domain.CertificationResultTestData;
import gov.healthit.chpl.domain.CertificationResultTestFunctionality;
import gov.healthit.chpl.domain.CertificationResultTestProcedure;
import gov.healthit.chpl.domain.CertificationResultTestStandard;
import gov.healthit.chpl.domain.CertificationResultTestTool;
import gov.healthit.chpl.domain.UcdProcess;
import gov.healthit.chpl.domain.CertificationStatusEvent;
import gov.healthit.chpl.domain.CertifiedProduct;
import gov.healthit.chpl.domain.CertifiedProductAccessibilityStandard;
import gov.healthit.chpl.domain.CertifiedProductQmsStandard;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.CertifiedProductTargetedUser;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.IcsFamilyTreeNode;
import gov.healthit.chpl.domain.InheritedCertificationStatus;
import gov.healthit.chpl.domain.MacraMeasure;
import gov.healthit.chpl.domain.Product;
import gov.healthit.chpl.domain.ProductVersion;
import gov.healthit.chpl.domain.Surveillance;
import gov.healthit.chpl.domain.TestTask;
import gov.healthit.chpl.dto.CQMCriterionDTO;
import gov.healthit.chpl.dto.CQMResultCriteriaDTO;
import gov.healthit.chpl.dto.CQMResultDetailsDTO;
import gov.healthit.chpl.dto.CertificationResultAdditionalSoftwareDTO;
import gov.healthit.chpl.dto.CertificationResultDetailsDTO;
import gov.healthit.chpl.dto.CertificationResultMacraMeasureDTO;
import gov.healthit.chpl.dto.CertificationResultTestDataDTO;
import gov.healthit.chpl.dto.CertificationResultTestFunctionalityDTO;
import gov.healthit.chpl.dto.CertificationResultTestProcedureDTO;
import gov.healthit.chpl.dto.CertificationResultTestStandardDTO;
import gov.healthit.chpl.dto.CertificationResultTestTaskDTO;
import gov.healthit.chpl.dto.CertificationResultTestToolDTO;
import gov.healthit.chpl.dto.CertificationResultUcdProcessDTO;
import gov.healthit.chpl.dto.CertificationStatusDTO;
import gov.healthit.chpl.dto.CertificationStatusEventDTO;
import gov.healthit.chpl.dto.CertifiedProductAccessibilityStandardDTO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.dto.CertifiedProductQmsStandardDTO;
import gov.healthit.chpl.dto.CertifiedProductTargetedUserDTO;
import gov.healthit.chpl.dto.MacraMeasureDTO;
import gov.healthit.chpl.dto.TestParticipantDTO;
import gov.healthit.chpl.manager.CertificationResultManager;
import gov.healthit.chpl.manager.CertifiedProductDetailsManager;
import gov.healthit.chpl.manager.SurveillanceManager;
import gov.healthit.chpl.util.CertificationResultRules;

@Service("certifiedProductDetailsManager")
public class CertifiedProductDetailsManagerImpl implements CertifiedProductDetailsManager {
	private static final Logger logger = LogManager.getLogger(CertifiedProductDetailsManagerImpl.class);

	@Autowired
	private CertifiedProductSearchResultDAO certifiedProductSearchResultDAO;
	
	@Autowired
	private CQMResultDetailsDAO cqmResultDetailsDAO;
	
	@Autowired private CQMResultDAO cqmResultDao;
	
	@Autowired
	private CertificationResultDetailsDAO certificationResultDetailsDAO;
	
	@Autowired
	private CertifiedProductQmsStandardDAO certifiedProductQmsStandardDao;
	
	@Autowired CertifiedProductTargetedUserDAO certifiedProductTargetedUserDao;
	@Autowired CertifiedProductAccessibilityStandardDAO certifiedProductAsDao;
	
	@Autowired
	private CertificationResultManager certResultManager;
	
	@Autowired
	private CertificationStatusEventDAO certStatusEventDao;
	
	@Autowired
	private CertificationStatusDAO certStatusDao;
	
	@Autowired
	private CertificationResultRules certRules;
	
	@Autowired private ListingGraphDAO listingGraphDao;

	@Autowired private SurveillanceManager survManager;
	
	private CQMCriterionDAO cqmCriterionDAO;
	private MacraMeasureDAO macraDao;
	
	private List<CQMCriterion> cqmCriteria = new ArrayList<CQMCriterion>();
	private List<MacraMeasure> macraMeasures = new ArrayList<MacraMeasure>();
	
	@Autowired
	public CertifiedProductDetailsManagerImpl(CQMCriterionDAO cqmCriterionDAO, MacraMeasureDAO macraDao){
		this.cqmCriterionDAO = cqmCriterionDAO;
		this.macraDao = macraDao;
		
		loadCQMCriteria();
		loadCriteriaMacraMeasures();
	}
	

	@Override
	@Transactional
	public CertifiedProductSearchDetails getCertifiedProductDetails(
			Long certifiedProductId) throws EntityRetrievalException {
		
		CertifiedProductDetailsDTO dto = certifiedProductSearchResultDAO.getById(certifiedProductId);
		
		CertifiedProductSearchDetails searchDetails = new CertifiedProductSearchDetails();
		
		searchDetails.setId(dto.getId());
		searchDetails.setAcbCertificationId(dto.getAcbCertificationId());
		
		if(dto.getCertificationDate() != null) {
			searchDetails.setCertificationDate(dto.getCertificationDate().getTime());
		}
		
		if(dto.getDecertificationDate() != null) {
			searchDetails.setDecertificationDate(dto.getDecertificationDate().getTime());
		}
			
		searchDetails.getCertificationEdition().put("id", dto.getCertificationEditionId());
		searchDetails.getCertificationEdition().put("name", dto.getYear());
		
		if(!StringUtils.isEmpty(dto.getChplProductNumber())) {
			searchDetails.setChplProductNumber(dto.getChplProductNumber());
		} else {
			searchDetails.setChplProductNumber(dto.getYearCode() + "." + dto.getTestingLabCode() + "." + dto.getCertificationBodyCode() + "." + 
				dto.getDeveloper().getDeveloperCode() + "." + dto.getProductCode() + "." + dto.getVersionCode() + 
				"." + dto.getIcsCode() + "." + dto.getAdditionalSoftwareCode() + 
				"." + dto.getCertifiedDateCode());
		}

		searchDetails.getCertificationStatus().put("id", dto.getCertificationStatusId());
		searchDetails.getCertificationStatus().put("name", dto.getCertificationStatusName());
		searchDetails.getCertificationStatus().put("date", dto.getCertificationStatusDate());
			
		searchDetails.getCertifyingBody().put("id", dto.getCertificationBodyId());
		searchDetails.getCertifyingBody().put("name", dto.getCertificationBodyName());
		searchDetails.getCertifyingBody().put("code", dto.getCertificationBodyCode());
					
		searchDetails.getClassificationType().put("id", dto.getProductClassificationTypeId());
		searchDetails.getClassificationType().put("name", dto.getProductClassificationName());
		
		searchDetails.setOtherAcb(dto.getOtherAcb());
		
		searchDetails.getPracticeType().put("id", dto.getPracticeTypeId());
		searchDetails.getPracticeType().put("name", dto.getPracticeTypeName());
				
		searchDetails.setReportFileLocation(dto.getReportFileLocation());
		searchDetails.setSedReportFileLocation(dto.getSedReportFileLocation());
		searchDetails.setSedIntendedUserDescription(dto.getSedIntendedUserDescription());
		searchDetails.setSedTestingEnd(dto.getSedTestingEnd());
		
		searchDetails.getTestingLab().put("id", dto.getTestingLabId());
		searchDetails.getTestingLab().put("name", dto.getTestingLabName());
		searchDetails.getTestingLab().put("code", dto.getTestingLabCode());
		
		Developer developer = new Developer(dto.getDeveloper());
		searchDetails.setDeveloper(developer);
		
		Product product = new Product(dto.getProduct());
		searchDetails.setProduct(product);
		
		ProductVersion version = new ProductVersion(dto.getVersion());
		searchDetails.setVersion(version);

		InheritedCertificationStatus ics = new InheritedCertificationStatus();
		ics.setInherits(dto.getIcs());
		searchDetails.setIcs(ics);
		searchDetails.setProductAdditionalSoftware(dto.getProductAdditionalSoftware());
		searchDetails.setTransparencyAttestationUrl(dto.getTransparencyAttestationUrl());
		searchDetails.setTransparencyAttestation(dto.getTransparencyAttestation());

		searchDetails.setLastModifiedDate(dto.getLastModifiedDate().getTime());
		
		searchDetails.setCountCerts(dto.getCountCertifications());
		searchDetails.setCountCqms(dto.getCountCqms());
		searchDetails.setCountSurveillance(dto.getCountSurveillance());
		searchDetails.setCountOpenSurveillance(dto.getCountOpenSurveillance());
		searchDetails.setCountClosedSurveillance(dto.getCountClosedSurveillance());
		searchDetails.setCountOpenNonconformities(dto.getCountOpenNonconformities());
		searchDetails.setCountClosedNonconformities(dto.getCountClosedNonconformities());
		searchDetails.setNumMeaningfulUse(dto.getNumMeaningfulUse());
		
		
		List<Surveillance> cpSurveillance = survManager.getByCertifiedProduct(dto.getId());
		searchDetails.setSurveillance(cpSurveillance);
		
		//get qms standards
		List<CertifiedProductQmsStandardDTO> qmsStandardDTOs = certifiedProductQmsStandardDao.getQmsStandardsByCertifiedProductId(dto.getId());
		List<CertifiedProductQmsStandard> qmsStandardResults = new ArrayList<CertifiedProductQmsStandard>();
		for(CertifiedProductQmsStandardDTO qmsStandardResult : qmsStandardDTOs) {
			CertifiedProductQmsStandard result = new CertifiedProductQmsStandard(qmsStandardResult);
			qmsStandardResults.add(result);
		}
		searchDetails.setQmsStandards(qmsStandardResults);
		
		//get targeted users
		List<CertifiedProductTargetedUserDTO> targetedUserDtos = certifiedProductTargetedUserDao.getTargetedUsersByCertifiedProductId(dto.getId());
		List<CertifiedProductTargetedUser> targetedUserResults = new ArrayList<CertifiedProductTargetedUser>();
		for(CertifiedProductTargetedUserDTO targetedUserDto : targetedUserDtos) {
			CertifiedProductTargetedUser result = new CertifiedProductTargetedUser(targetedUserDto);
			targetedUserResults.add(result);
		}
		searchDetails.setTargetedUsers(targetedUserResults);
		
		//get accessibility standards
		List<CertifiedProductAccessibilityStandardDTO> accessibilityStandardDtos = certifiedProductAsDao.getAccessibilityStandardsByCertifiedProductId(dto.getId());
		List<CertifiedProductAccessibilityStandard> accessibilityStandardResults = new ArrayList<CertifiedProductAccessibilityStandard>();
		for(CertifiedProductAccessibilityStandardDTO accessibilityStandardDto : accessibilityStandardDtos) {
			CertifiedProductAccessibilityStandard result = new CertifiedProductAccessibilityStandard(accessibilityStandardDto);
			accessibilityStandardResults.add(result);
		}
		searchDetails.setAccessibilityStandards(accessibilityStandardResults);
				
		//get cert criteria results
		List<CertificationResultDetailsDTO> certificationResultDetailsDTOs = certificationResultDetailsDAO.getCertificationResultDetailsByCertifiedProductId(dto.getId());
		List<CertificationResult> certificationResults = new ArrayList<CertificationResult>();
		
		for (CertificationResultDetailsDTO certResult : certificationResultDetailsDTOs){
			CertificationResult result = new CertificationResult(certResult);
			//override optional boolean values
			if(!certRules.hasCertOption(certResult.getNumber(), CertificationResultRules.GAP)) {
				result.setGap(null);
			} else if(result.isGap() == null) {
				result.setGap(Boolean.FALSE);
			}
			if(!certRules.hasCertOption(certResult.getNumber(), CertificationResultRules.G1_SUCCESS)) {
				result.setG1Success(null);
			} else if(result.isG1Success() == null) {
				result.setG1Success(Boolean.FALSE);
			}
			if(!certRules.hasCertOption(certResult.getNumber(), CertificationResultRules.G2_SUCCESS)) {
				result.setG2Success(null);
			} else if(result.isG2Success() == null) {
				result.setG2Success(Boolean.FALSE);
			}
			if(!certRules.hasCertOption(certResult.getNumber(), CertificationResultRules.API_DOCUMENTATION)) {
				result.setApiDocumentation(null);
			} else if(result.getApiDocumentation() == null) {
				result.setApiDocumentation("");
			}
			if(!certRules.hasCertOption(certResult.getNumber(), CertificationResultRules.PRIVACY_SECURITY)) {
				result.setPrivacySecurityFramework(null);
			} else if(result.getPrivacySecurityFramework() == null) {
				result.setPrivacySecurityFramework("");
			}
			
			//add all the other data
			if(certRules.hasCertOption(certResult.getNumber(), CertificationResultRules.ADDITIONAL_SOFTWARE)) {
				List<CertificationResultAdditionalSoftwareDTO> certResultSoftware = certResultManager.getAdditionalSoftwareMappingsForCertificationResult(certResult.getId());
				for(CertificationResultAdditionalSoftwareDTO currResult : certResultSoftware) {
					CertificationResultAdditionalSoftware softwareResult = new CertificationResultAdditionalSoftware(currResult);
					result.getAdditionalSoftware().add(softwareResult);
				}
			} else {
				result.setAdditionalSoftware(null);
			}
			
			if(certRules.hasCertOption(certResult.getNumber(), CertificationResultRules.STANDARDS_TESTED)) {
				List<CertificationResultTestStandardDTO> testStandards = certResultManager.getTestStandardsForCertificationResult(certResult.getId());
				for(CertificationResultTestStandardDTO currResult : testStandards) {
					CertificationResultTestStandard testStandardResult = new CertificationResultTestStandard(currResult);
					result.getTestStandards().add(testStandardResult);
				}
			} else {
				result.setTestStandards(null);
			}
			
			if(certRules.hasCertOption(certResult.getNumber(), CertificationResultRules.TEST_TOOLS_USED)) {
				List<CertificationResultTestToolDTO> testTools = certResultManager.getTestToolsForCertificationResult(certResult.getId());
				for(CertificationResultTestToolDTO currResult : testTools) {
					CertificationResultTestTool testToolResult = new CertificationResultTestTool(currResult);
					result.getTestToolsUsed().add(testToolResult);
				}
			} else {
				result.setTestToolsUsed(null);
			}
			
			if(certRules.hasCertOption(certResult.getNumber(), CertificationResultRules.TEST_DATA)) {
				List<CertificationResultTestDataDTO> testData = certResultManager.getTestDataForCertificationResult(certResult.getId());
				for(CertificationResultTestDataDTO currResult : testData) {
					CertificationResultTestData testDataResult = new CertificationResultTestData(currResult);
					result.getTestDataUsed().add(testDataResult);
				}
			} else {
				result.setTestDataUsed(null);
			}
			
			if(certRules.hasCertOption(certResult.getNumber(), CertificationResultRules.TEST_PROCEDURE_VERSION)) {
				List<CertificationResultTestProcedureDTO> testProcedure = certResultManager.getTestProceduresForCertificationResult(certResult.getId());
				for(CertificationResultTestProcedureDTO currResult : testProcedure) {
					CertificationResultTestProcedure testProcedureResult = new CertificationResultTestProcedure(currResult);
					result.getTestProcedures().add(testProcedureResult);
				}
			} else {
				result.setTestProcedures(null);
			}
			
			if(certRules.hasCertOption(certResult.getNumber(), CertificationResultRules.FUNCTIONALITY_TESTED)) {
				List<CertificationResultTestFunctionalityDTO> testFunctionality = certResultManager.getTestFunctionalityForCertificationResult(certResult.getId());
				for(CertificationResultTestFunctionalityDTO currResult : testFunctionality) {
					CertificationResultTestFunctionality testFunctionalityResult = new CertificationResultTestFunctionality(currResult);
					result.getTestFunctionality().add(testFunctionalityResult);
				}
			} else {
				result.setTestFunctionality(null);
			}
			
			if(!certRules.hasCertOption(certResult.getNumber(), CertificationResultRules.G1_MACRA) && 
				!certRules.hasCertOption(certResult.getNumber(), CertificationResultRules.G2_MACRA)) {
				result.setAllowedMacraMeasures(null);
				result.setG1MacraMeasures(null);
				result.setG2MacraMeasures(null);
			} else {
				if(certRules.hasCertOption(certResult.getNumber(), CertificationResultRules.G1_MACRA)) {
					List<CertificationResultMacraMeasureDTO> measures = certResultManager.getG1MacraMeasuresForCertificationResult(certResult.getId());
					for(CertificationResultMacraMeasureDTO currResult : measures) {
						MacraMeasure mmResult = new MacraMeasure(currResult.getMeasure());
						result.getG1MacraMeasures().add(mmResult);
					}
				} else {
					result.setG1MacraMeasures(null);
				}
				
				if(certRules.hasCertOption(certResult.getNumber(), CertificationResultRules.G2_MACRA)) {
					List<CertificationResultMacraMeasureDTO> measures = certResultManager.getG2MacraMeasuresForCertificationResult(certResult.getId());
					for(CertificationResultMacraMeasureDTO currResult : measures) {
						MacraMeasure mmResult = new MacraMeasure(currResult.getMeasure());
						result.getG2MacraMeasures().add(mmResult);
					}
				} else {
					result.setG2MacraMeasures(null);
				}
			}

			//get all SED data for the listing
			//ucd processes and test tasks with participants
			CertificationCriterion criteria = new CertificationCriterion();
			criteria.setNumber(result.getNumber());
			criteria.setTitle(result.getTitle());
			
			if(certRules.hasCertOption(certResult.getNumber(), CertificationResultRules.UCD_FIELDS)) {
				List<CertificationResultUcdProcessDTO> ucdProcesses = certResultManager.getUcdProcessesForCertificationResult(result.getId());
				for(CertificationResultUcdProcessDTO currResult : ucdProcesses) {
					boolean alreadyExists = false;
					UcdProcess newUcd = new UcdProcess(currResult);
					for(UcdProcess currUcd : searchDetails.getSed().getUcdProcesses()) {
						if(newUcd.matches(currUcd)) {
							alreadyExists = true;
							currUcd.getCriteria().add(criteria);
						}
					}
					if(!alreadyExists) {
						newUcd.getCriteria().add(criteria);
						searchDetails.getSed().getUcdProcesses().add(newUcd);
					}
				}
			} else {
				result.setSed(null);
			}
			
			if(certRules.hasCertOption(certResult.getNumber(), CertificationResultRules.TEST_TASK)) {
				List<CertificationResultTestTaskDTO> testTask = certResultManager.getTestTasksForCertificationResult(certResult.getId());
				for(CertificationResultTestTaskDTO currResult : testTask) {
					boolean alreadyExists = false;
					TestTask newTestTask = new TestTask(currResult);
					for(TestTask currTestTask : searchDetails.getSed().getTestTasks()) {
						if(newTestTask.matches(currTestTask)) {
							alreadyExists = true;
							currTestTask.getCriteria().add(criteria);
						}
					}
					if(!alreadyExists) {
						newTestTask.getCriteria().add(criteria);
						searchDetails.getSed().getTestTasks().add(newTestTask);
					}
				}
			} 
			
			//set allowed macra measures (if any)
			for(MacraMeasure measure : macraMeasures) {
				if(measure.getCriteria().getNumber().equals(result.getNumber())) {
					result.getAllowedMacraMeasures().add(measure);
				}
			}
			
			certificationResults.add(result);
		}
		searchDetails.setCertificationResults(certificationResults);
			
		//fill in CQM results, sadly there is different data for NQFs and CMSs
		List<CQMResultDetailsDTO> cqmResultDTOs = cqmResultDetailsDAO.getCQMResultDetailsByCertifiedProductId(dto.getId());
		List<CQMResultDetails> cqmResults = new ArrayList<CQMResultDetails>();
		for (CQMResultDetailsDTO cqmResultDTO : cqmResultDTOs){
			boolean existingCms = false;
			//for a CMS, first check to see if we already have an object with the same CMS id
			//so we can just add to it's success versions. 
			if(!dto.getYear().equals("2011") && !StringUtils.isEmpty(cqmResultDTO.getCmsId())) {
				for(CQMResultDetails result : cqmResults) {
					if(cqmResultDTO.getCmsId().equals(result.getCmsId())) {
						existingCms = true;
						result.getSuccessVersions().add(cqmResultDTO.getVersion());
					}
				}
			}
			
			if(!existingCms) {
				CQMResultDetails result = new CQMResultDetails();
				result.setId(cqmResultDTO.getId());
				result.setCmsId(cqmResultDTO.getCmsId());
				result.setNqfNumber(cqmResultDTO.getNqfNumber());
				result.setNumber(cqmResultDTO.getNumber());
				result.setTitle(cqmResultDTO.getTitle());
				result.setDescription(cqmResultDTO.getDescription());
				result.setTypeId(cqmResultDTO.getCqmCriterionTypeId());
				if(!dto.getYear().equals("2011") && !StringUtils.isEmpty(cqmResultDTO.getCmsId())) {
					result.getSuccessVersions().add(cqmResultDTO.getVersion());
				} else {
					result.setSuccess(cqmResultDTO.getSuccess());
				}
				cqmResults.add(result);
			}
		}	
		
		//now add allVersions for CMSs
		if (!dto.getYear().startsWith("2011")){
			List<CQMCriterion> cqms = getAvailableCQMVersions();
			for(CQMCriterion cqm : cqms) {
				boolean cqmExists = false;
				for(CQMResultDetails details : cqmResults) {
					if(cqm.getCmsId().equals(details.getCmsId())) {
						cqmExists = true;
						details.getAllVersions().add(cqm.getCqmVersion());
					}
				}
				if(!cqmExists) {
					CQMResultDetails result = new CQMResultDetails();
					result.setCmsId(cqm.getCmsId());
					result.setNqfNumber(cqm.getNqfNumber());
					result.setNumber(cqm.getNumber());
					result.setTitle(cqm.getTitle());
					result.setDescription(cqm.getDescription());
					result.setSuccess(Boolean.FALSE);
					result.getAllVersions().add(cqm.getCqmVersion());
					result.setTypeId(cqm.getCqmCriterionTypeId());
					cqmResults.add(result);
				}
			}
		}
		
		//now add criteria mappings to all of our cqms
		for(CQMResultDetails cqmResult : cqmResults) {
			if(cqmResult.isSuccess() && cqmResult.getId() != null) {
				List<CQMResultCriteriaDTO> criteria = cqmResultDao.getCriteriaForCqmResult(cqmResult.getId());
				if(criteria != null && criteria.size() > 0) {
					for(CQMResultCriteriaDTO criteriaDTO : criteria) {
						CQMResultCertification c = new CQMResultCertification();
						c.setCertificationId(criteriaDTO.getCriterionId());
						c.setId(criteriaDTO.getId());
						if(criteriaDTO.getCriterion() != null) {
							c.setCertificationNumber(criteriaDTO.getCriterion().getNumber());
						}
						cqmResult.getCriteria().add(c);
					}
				}
			}
		}
		
		searchDetails.setCqmResults(cqmResults);
		
		searchDetails.setCertificationEvents(getCertificationStatusEvents(dto.getId()));
		
		//get first-level parents and children
		List<CertifiedProductDetailsDTO> children = listingGraphDao.getChildren(dto.getId());
		if(children != null && children.size() > 0) {
			for(CertifiedProductDetailsDTO child : children) {
				searchDetails.getIcs().getChildren().add(new CertifiedProduct(child));
			}
		}
		
		List<CertifiedProductDetailsDTO> parents = listingGraphDao.getParents(dto.getId());
		if(parents != null && parents.size() > 0) {
			for(CertifiedProductDetailsDTO parent : parents) {
				searchDetails.getIcs().getParents().add(new CertifiedProduct(parent));
			}
		}
		
		return searchDetails;
	}
	
	public List<CQMCriterion> getCqmCriteria() {
		return cqmCriteria;
	}
	
	public void setCqmCriteria(List<CQMCriterion> cqmCriteria) {
		this.cqmCriteria = cqmCriteria;
	}
	
	private List<CertificationStatusEvent> getCertificationStatusEvents(Long certifiedProductId) throws EntityRetrievalException{
		
		List<CertificationStatusEvent> certEvents = new ArrayList<CertificationStatusEvent>();
		List<CertificationStatusEventDTO> certStatusDtos = certStatusEventDao.findByCertifiedProductId(certifiedProductId);	
		
		for (CertificationStatusEventDTO certStatusDto : certStatusDtos){
			CertificationStatusEvent cse = new CertificationStatusEvent();
			cse.setId(certStatusDto.getId());								
			cse.setEventDate(certStatusDto.getEventDate().getTime());
			cse.setLastModifiedUser(certStatusDto.getLastModifiedUser());
			cse.setLastModifiedDate(certStatusDto.getLastModifiedDate().getTime());
			
			CertificationStatusDTO statusDto = certStatusDao.getById(certStatusDto.getStatus().getId());
			cse.setCertificationStatusId(statusDto.getId());
			cse.setCertificationStatusName(statusDto.getStatus());
			certEvents.add(cse);
		}
		return certEvents;
	}
	
	private void loadCriteriaMacraMeasures() {
		List<MacraMeasureDTO> dtos = macraDao.findAll();
		for(MacraMeasureDTO dto : dtos) {
			MacraMeasure measure = new MacraMeasure(dto);
			macraMeasures.add(measure);
		}
	}
	
	private void loadCQMCriteria(){
		
		List<CQMCriterionDTO> dtos = cqmCriterionDAO.findAll();
		
		for (CQMCriterionDTO dto: dtos){
			
			CQMCriterion criterion = new CQMCriterion();
			
			criterion.setCmsId(dto.getCmsId());
			criterion.setCqmCriterionTypeId(dto.getCqmCriterionTypeId());
			criterion.setCqmDomain(dto.getCqmDomain());
			criterion.setCqmVersionId(dto.getCqmVersionId());
			criterion.setCqmVersion(dto.getCqmVersion());
			criterion.setCriterionId(dto.getId());
			criterion.setDescription(dto.getDescription());
			criterion.setNqfNumber(dto.getNqfNumber());
			criterion.setNumber(dto.getNumber());
			criterion.setTitle(dto.getTitle());
			cqmCriteria.add(criterion);
			
		}
	}
	
	private List<CQMCriterion> getAvailableCQMVersions(){
		
		List<CQMCriterion> criteria = new ArrayList<CQMCriterion>();
		
		for (CQMCriterion criterion : cqmCriteria){
			
			if (!StringUtils.isEmpty(criterion.getCmsId()) && criterion.getCmsId().startsWith("CMS")){
				criteria.add(criterion);
			}
		}
		return criteria;
	}
	
	private List<CQMCriterion> getAvailableNQFVersions(){
		
		List<CQMCriterion> nqfs = new ArrayList<CQMCriterion>();
		
		for (CQMCriterion criterion : cqmCriteria){
			
			if (StringUtils.isEmpty(criterion.getCmsId())){
				nqfs.add(criterion);
			}
		}
		return nqfs;
	}
	
}
