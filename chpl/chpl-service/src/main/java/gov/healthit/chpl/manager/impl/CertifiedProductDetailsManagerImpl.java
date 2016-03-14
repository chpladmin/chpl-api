package gov.healthit.chpl.manager.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.dao.CQMCriterionDAO;
import gov.healthit.chpl.dao.CQMResultDAO;
import gov.healthit.chpl.dao.CQMResultDetailsDAO;
import gov.healthit.chpl.dao.CertificationEventDAO;
import gov.healthit.chpl.dao.CertificationResultDetailsDAO;
import gov.healthit.chpl.dao.CertifiedProductQmsStandardDAO;
import gov.healthit.chpl.dao.CertifiedProductSearchResultDAO;
import gov.healthit.chpl.dao.CertifiedProductTargetedUserDAO;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dao.EventTypeDAO;
import gov.healthit.chpl.domain.CQMCriterion;
import gov.healthit.chpl.domain.CQMResultCriteria;
import gov.healthit.chpl.domain.CQMResultDetails;
import gov.healthit.chpl.domain.CertificationEvent;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultAdditionalSoftware;
import gov.healthit.chpl.domain.CertificationResultTestData;
import gov.healthit.chpl.domain.CertificationResultTestFunctionality;
import gov.healthit.chpl.domain.CertificationResultTestProcedure;
import gov.healthit.chpl.domain.CertificationResultTestStandard;
import gov.healthit.chpl.domain.CertificationResultTestTool;
import gov.healthit.chpl.domain.CertificationResultUcdProcess;
import gov.healthit.chpl.domain.CertifiedProductQmsStandard;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.CertifiedProductTargetedUser;
import gov.healthit.chpl.dto.CQMCriterionDTO;
import gov.healthit.chpl.dto.CQMResultCriteriaDTO;
import gov.healthit.chpl.dto.CQMResultDetailsDTO;
import gov.healthit.chpl.dto.CertificationEventDTO;
import gov.healthit.chpl.dto.CertificationResultAdditionalSoftwareDTO;
import gov.healthit.chpl.dto.CertificationResultDetailsDTO;
import gov.healthit.chpl.dto.CertificationResultTestDataDTO;
import gov.healthit.chpl.dto.CertificationResultTestFunctionalityDTO;
import gov.healthit.chpl.dto.CertificationResultTestProcedureDTO;
import gov.healthit.chpl.dto.CertificationResultTestStandardDTO;
import gov.healthit.chpl.dto.CertificationResultTestToolDTO;
import gov.healthit.chpl.dto.CertificationResultUcdProcessDTO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.dto.CertifiedProductQmsStandardDTO;
import gov.healthit.chpl.dto.CertifiedProductTargetedUserDTO;
import gov.healthit.chpl.dto.EventTypeDTO;
import gov.healthit.chpl.manager.CertificationResultManager;
import gov.healthit.chpl.manager.CertifiedProductDetailsManager;
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
	
	@Autowired
	private CertificationResultManager certResultManager;
	
	@Autowired
	private CertificationEventDAO certificationEventDAO;
	
	@Autowired
	private EventTypeDAO eventTypeDAO;
	
	@Autowired
	private CertificationResultRules certRules;

	private CQMCriterionDAO cqmCriterionDAO;
	
	private List<CQMCriterion> cqmCriteria = new ArrayList<CQMCriterion>();
	
	@Autowired
	public CertifiedProductDetailsManagerImpl(CQMCriterionDAO cqmCriterionDAO){
		this.cqmCriterionDAO = cqmCriterionDAO;
		loadCQMCriteria();
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
			
		searchDetails.getCertificationEdition().put("id", dto.getCertificationEditionId());
		searchDetails.getCertificationEdition().put("name", dto.getYear());
		
		if(!StringUtils.isEmpty(dto.getChplProductNumber())) {
			searchDetails.setChplProductNumber(dto.getChplProductNumber());
		} else {
			searchDetails.setChplProductNumber(dto.getYearCode() + "." + dto.getTestingLabCode() + "." + dto.getCertificationBodyCode() + "." + 
				dto.getDeveloperCode() + "." + dto.getProductCode() + "." + dto.getVersionCode() + 
				"." + dto.getIcsCode() + "." + dto.getAdditionalSoftwareCode() + 
				"." + dto.getCertifiedDateCode());
		}

		searchDetails.getCertificationStatus().put("id", dto.getCertificationStatusId());
		searchDetails.getCertificationStatus().put("name", dto.getCertificationStatusName());
			
		searchDetails.getCertifyingBody().put("id", dto.getCertificationBodyId());
		searchDetails.getCertifyingBody().put("name", dto.getCertificationBodyName());
		searchDetails.getCertifyingBody().put("code", dto.getCertificationBodyCode());
					
		searchDetails.getClassificationType().put("id", dto.getProductClassificationTypeId());
		searchDetails.getClassificationType().put("name", dto.getProductClassificationName());
		
		searchDetails.setOtherAcb(dto.getOtherAcb());
		
		searchDetails.getPracticeType().put("id", dto.getPracticeTypeId());
		searchDetails.getPracticeType().put("name", dto.getPracticeTypeName());
		
		searchDetails.getProduct().put("id",dto.getProductId());
		searchDetails.getProduct().put("name",dto.getProductName());
		searchDetails.getProduct().put("versionId",dto.getProductVersionId());
		searchDetails.getProduct().put("version", dto.getProductVersion());
				
		searchDetails.setReportFileLocation(dto.getReportFileLocation());
		searchDetails.setSedReportFileLocation(dto.getSedReportFileLocation());
		
		searchDetails.getTestingLab().put("id", dto.getTestingLabId());
		searchDetails.getTestingLab().put("name", dto.getTestingLabName());
		searchDetails.getTestingLab().put("code", dto.getTestingLabCode());
		
		searchDetails.getDeveloper().put("id", dto.getDeveloperId());
		searchDetails.getDeveloper().put("name", dto.getDeveloperName());
		searchDetails.getDeveloper().put("code", dto.getDeveloperCode());
		searchDetails.getDeveloper().put("website", dto.getDeveloperWebsite());
		
		searchDetails.setVisibleOnChpl(dto.getVisibleOnChpl());
		searchDetails.setApiDocumentation(dto.getApiDocumentation());
		searchDetails.setIcs(dto.getIcs());
		searchDetails.setProductAdditionalSoftware(dto.getProductAdditionalSoftware());
		searchDetails.setTransparencyAttestationUrl(dto.getTransparencyAttestationUrl());
		searchDetails.setTransparencyAttestation(dto.getTransparencyAttestation());

		searchDetails.setTermsOfUse(dto.getTermsOfUse());
		searchDetails.setLastModifiedDate(dto.getLastModifiedDate().getTime());
		
		searchDetails.setCountCerts(dto.getCountCertifications());
		searchDetails.setCountCqms(dto.getCountCqms());
		searchDetails.setCountCorrectiveActionPlans(dto.getCountCorrectiveActionPlans());
		searchDetails.setCountCurrentCorrectiveActionPlans(dto.getCountCurrentCorrectiveActionPlans());
		searchDetails.setCountClosedCorrectiveActionPlans(dto.getCountClosedCorrectiveActionPlans());
		
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
		
		//get cert criteria results
		List<CertificationResultDetailsDTO> certificationResultDetailsDTOs = certificationResultDetailsDAO.getCertificationResultDetailsByCertifiedProductId(dto.getId());
		List<CertificationResult> certificationResults = new ArrayList<CertificationResult>();
		
		for (CertificationResultDetailsDTO certResult : certificationResultDetailsDTOs){
			CertificationResult result = new CertificationResult(certResult);
			//override optional boolean values
			if(!certRules.hasCertOption(certResult.getNumber(), CertificationResultRules.GAP)) {
				result.setGap(null);
			}
			if(!certRules.hasCertOption(certResult.getNumber(), CertificationResultRules.SED)) {
				result.setSed(null);
			}
			if(!certRules.hasCertOption(certResult.getNumber(), CertificationResultRules.G1_SUCCESS)) {
				result.setG1Success(null);
			}
			if(!certRules.hasCertOption(certResult.getNumber(), CertificationResultRules.G2_SUCCESS)) {
				result.setG2Success(null);
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
			
			if(certRules.hasCertOption(certResult.getNumber(), CertificationResultRules.UCD_FIELDS)) {
				List<CertificationResultUcdProcessDTO> ucdProcesses = certResultManager.getUcdProcessesForCertificationResult(certResult.getId());
				for(CertificationResultUcdProcessDTO currResult : ucdProcesses) {
					CertificationResultUcdProcess ucdResult = new CertificationResultUcdProcess(currResult);
					result.getUcdProcesses().add(ucdResult);
				}
			} else {
				result.setUcdProcesses(null);
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
						CQMResultCriteria c = new CQMResultCriteria();
						c.setCriteriaId(criteriaDTO.getCriterionId());
						c.setId(criteriaDTO.getId());
						if(criteriaDTO.getCriterion() != null) {
							c.setCriteriaNumber(criteriaDTO.getCriterion().getNumber());
						}
						cqmResult.getCriteria().add(c);
					}
				}
			}
		}
		
		searchDetails.setCqmResults(cqmResults);
		
		searchDetails.setCertificationEvents(getCertificationEvents(dto.getId()));
		
		return searchDetails;
	}
	
	public List<CQMCriterion> getCqmCriteria() {
		return cqmCriteria;
	}
	
	public void setCqmCriteria(List<CQMCriterion> cqmCriteria) {
		this.cqmCriteria = cqmCriteria;
	}
	
	private List<CertificationEvent> getCertificationEvents(Long certifiedProductId) throws EntityRetrievalException{
		
		List<CertificationEvent> certEvents = new ArrayList<CertificationEvent>();
		List<CertificationEventDTO> eventDTOs = certificationEventDAO.findByCertifiedProductId(certifiedProductId);	
		
		for (CertificationEventDTO event : eventDTOs){
			
			CertificationEvent ce = new CertificationEvent();
			
			ce.setId(event.getId());
			ce.setCity(event.getCity());
								
			ce.setEventDate(event.getEventDate().getTime() + "");
			ce.setLastModifiedUser(event.getLastModifiedUser());
			ce.setLastModifiedDate(event.getLastModifiedDate().getTime() + "");
			ce.setState(event.getState());
			ce.setEventTypeId(event.getEventTypeId());
			
			EventTypeDTO eventTypeDTO = eventTypeDAO.getById(event.getEventTypeId());
			ce.setEventTypeDescription(eventTypeDTO.getDescription());
			ce.setEventTypeName(eventTypeDTO.getName());
			
			certEvents.add(ce);
		}
		
		return certEvents;
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
