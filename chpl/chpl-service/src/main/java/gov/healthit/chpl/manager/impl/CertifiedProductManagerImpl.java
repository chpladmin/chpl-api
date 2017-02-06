package gov.healthit.chpl.manager.impl;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.mail.MessagingException;
import javax.persistence.EntityNotFoundException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.core.env.Environment;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.auth.SendMailUtil;
import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.caching.CacheNames;
import gov.healthit.chpl.caching.ClearAllCaches;
import gov.healthit.chpl.dao.AccessibilityStandardDAO;
import gov.healthit.chpl.dao.CQMCriterionDAO;
import gov.healthit.chpl.dao.CQMResultDAO;
import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.dao.CertificationCriterionDAO;
import gov.healthit.chpl.dao.CertificationResultDAO;
import gov.healthit.chpl.dao.CertificationStatusDAO;
import gov.healthit.chpl.dao.CertificationStatusEventDAO;
import gov.healthit.chpl.dao.CertifiedProductAccessibilityStandardDAO;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.CertifiedProductQmsStandardDAO;
import gov.healthit.chpl.dao.CertifiedProductTargetedUserDAO;
import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.dao.DeveloperStatusDAO;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dao.MacraMeasureDAO;
import gov.healthit.chpl.dao.QmsStandardDAO;
import gov.healthit.chpl.dao.TargetedUserDAO;
import gov.healthit.chpl.dao.TestFunctionalityDAO;
import gov.healthit.chpl.dao.TestParticipantDAO;
import gov.healthit.chpl.dao.TestProcedureDAO;
import gov.healthit.chpl.dao.TestStandardDAO;
import gov.healthit.chpl.dao.TestTaskDAO;
import gov.healthit.chpl.dao.TestToolDAO;
import gov.healthit.chpl.dao.UcdProcessDAO;
import gov.healthit.chpl.domain.ActivityConcept;
import gov.healthit.chpl.domain.CQMResultCertification;
import gov.healthit.chpl.domain.CQMResultDetails;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultAdditionalSoftware;
import gov.healthit.chpl.domain.CertificationResultTestData;
import gov.healthit.chpl.domain.CertificationResultTestFunctionality;
import gov.healthit.chpl.domain.CertificationResultTestParticipant;
import gov.healthit.chpl.domain.CertificationResultTestProcedure;
import gov.healthit.chpl.domain.CertificationResultTestStandard;
import gov.healthit.chpl.domain.CertificationResultTestTask;
import gov.healthit.chpl.domain.CertificationResultTestTool;
import gov.healthit.chpl.domain.CertificationResultUcdProcess;
import gov.healthit.chpl.domain.CertifiedProductAccessibilityStandard;
import gov.healthit.chpl.domain.CertifiedProductQmsStandard;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.CertifiedProductTargetedUser;
import gov.healthit.chpl.domain.MacraMeasure;
import gov.healthit.chpl.domain.MeaningfulUseUser;
import gov.healthit.chpl.dto.AccessibilityStandardDTO;
import gov.healthit.chpl.dto.AddressDTO;
import gov.healthit.chpl.dto.AgeRangeDTO;
import gov.healthit.chpl.dto.CQMCriterionDTO;
import gov.healthit.chpl.dto.CQMResultCriteriaDTO;
import gov.healthit.chpl.dto.CQMResultDTO;
import gov.healthit.chpl.dto.CQMResultDetailsDTO;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.dto.CertificationResultAdditionalSoftwareDTO;
import gov.healthit.chpl.dto.CertificationResultDTO;
import gov.healthit.chpl.dto.CertificationResultMacraMeasureDTO;
import gov.healthit.chpl.dto.CertificationResultTestDataDTO;
import gov.healthit.chpl.dto.CertificationResultTestFunctionalityDTO;
import gov.healthit.chpl.dto.CertificationResultTestProcedureDTO;
import gov.healthit.chpl.dto.CertificationResultTestStandardDTO;
import gov.healthit.chpl.dto.CertificationResultTestTaskDTO;
import gov.healthit.chpl.dto.CertificationResultTestTaskParticipantDTO;
import gov.healthit.chpl.dto.CertificationResultTestToolDTO;
import gov.healthit.chpl.dto.CertificationResultUcdProcessDTO;
import gov.healthit.chpl.dto.CertificationStatusDTO;
import gov.healthit.chpl.dto.CertificationStatusEventDTO;
import gov.healthit.chpl.dto.CertifiedProductAccessibilityStandardDTO;
import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.dto.CertifiedProductQmsStandardDTO;
import gov.healthit.chpl.dto.CertifiedProductTargetedUserDTO;
import gov.healthit.chpl.dto.ContactDTO;
import gov.healthit.chpl.dto.DeveloperACBMapDTO;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.dto.DeveloperStatusDTO;
import gov.healthit.chpl.dto.EducationTypeDTO;
import gov.healthit.chpl.dto.MacraMeasureDTO;
import gov.healthit.chpl.dto.PendingCertificationResultAdditionalSoftwareDTO;
import gov.healthit.chpl.dto.PendingCertificationResultDTO;
import gov.healthit.chpl.dto.PendingCertificationResultMacraMeasureDTO;
import gov.healthit.chpl.dto.PendingCertificationResultTestDataDTO;
import gov.healthit.chpl.dto.PendingCertificationResultTestFunctionalityDTO;
import gov.healthit.chpl.dto.PendingCertificationResultTestProcedureDTO;
import gov.healthit.chpl.dto.PendingCertificationResultTestStandardDTO;
import gov.healthit.chpl.dto.PendingCertificationResultTestTaskDTO;
import gov.healthit.chpl.dto.PendingCertificationResultTestTaskParticipantDTO;
import gov.healthit.chpl.dto.PendingCertificationResultTestToolDTO;
import gov.healthit.chpl.dto.PendingCertificationResultUcdProcessDTO;
import gov.healthit.chpl.dto.PendingCertifiedProductAccessibilityStandardDTO;
import gov.healthit.chpl.dto.PendingCertifiedProductDTO;
import gov.healthit.chpl.dto.PendingCertifiedProductQmsStandardDTO;
import gov.healthit.chpl.dto.PendingCertifiedProductTargetedUserDTO;
import gov.healthit.chpl.dto.PendingCqmCertificationCriterionDTO;
import gov.healthit.chpl.dto.PendingCqmCriterionDTO;
import gov.healthit.chpl.dto.PendingTestParticipantDTO;
import gov.healthit.chpl.dto.PendingTestTaskDTO;
import gov.healthit.chpl.dto.ProductDTO;
import gov.healthit.chpl.dto.ProductVersionDTO;
import gov.healthit.chpl.dto.QmsStandardDTO;
import gov.healthit.chpl.dto.TargetedUserDTO;
import gov.healthit.chpl.dto.TestFunctionalityDTO;
import gov.healthit.chpl.dto.TestParticipantDTO;
import gov.healthit.chpl.dto.TestProcedureDTO;
import gov.healthit.chpl.dto.TestStandardDTO;
import gov.healthit.chpl.dto.TestTaskDTO;
import gov.healthit.chpl.dto.TestToolDTO;
import gov.healthit.chpl.dto.UcdProcessDTO;
import gov.healthit.chpl.entity.CertificationStatusType;
import gov.healthit.chpl.entity.DeveloperStatusType;
import gov.healthit.chpl.manager.ActivityManager;
import gov.healthit.chpl.manager.CertificationBodyManager;
import gov.healthit.chpl.manager.CertificationResultManager;
import gov.healthit.chpl.manager.CertifiedProductDetailsManager;
import gov.healthit.chpl.manager.CertifiedProductManager;
import gov.healthit.chpl.manager.DeveloperManager;
import gov.healthit.chpl.manager.ProductManager;
import gov.healthit.chpl.manager.ProductVersionManager;
import gov.healthit.chpl.util.CertificationResultRules;
import gov.healthit.chpl.web.controller.results.MeaningfulUseUserResults;

@Service
public class CertifiedProductManagerImpl implements CertifiedProductManager {
	private static final Logger logger = LogManager.getLogger(CertifiedProductManagerImpl.class);
	
	@Autowired SendMailUtil sendMailService;
	@Autowired private Environment env;
	
	@Autowired
	private CertificationResultRules certRules;
	
	@Autowired CertifiedProductDAO cpDao;
	@Autowired CertificationResultDAO certDao;
	@Autowired CertificationCriterionDAO certCriterionDao;
	@Autowired QmsStandardDAO qmsDao;
	@Autowired TargetedUserDAO targetedUserDao;
	@Autowired AccessibilityStandardDAO asDao;
	@Autowired CertifiedProductQmsStandardDAO cpQmsDao;
	@Autowired CertifiedProductTargetedUserDAO cpTargetedUserDao;
	@Autowired CertifiedProductAccessibilityStandardDAO cpAccStdDao;
	@Autowired CQMResultDAO cqmResultDAO;
	@Autowired CQMCriterionDAO cqmCriterionDao;
	@Autowired CertificationBodyDAO acbDao;
	@Autowired DeveloperDAO developerDao;
	@Autowired DeveloperStatusDAO devStatusDao;
	@Autowired DeveloperManager developerManager;
	@Autowired ProductManager productManager;
	@Autowired ProductVersionManager versionManager;
	@Autowired CertificationStatusEventDAO statusEventDao;
	@Autowired CertificationResultManager certResultManager;
	@Autowired TestToolDAO testToolDao;
	@Autowired TestStandardDAO testStandardDao;
	@Autowired TestProcedureDAO testProcDao;
	@Autowired TestFunctionalityDAO testFuncDao;
	@Autowired UcdProcessDAO ucdDao;
	@Autowired TestParticipantDAO testParticipantDao;
	@Autowired TestTaskDAO testTaskDao;
	@Autowired MacraMeasureDAO macraDao;
	@Autowired CertificationStatusDAO certStatusDao;
	
	@Autowired
	public ActivityManager activityManager;
	
	@Autowired
	public CertifiedProductDetailsManager detailsManager;
		
	@Autowired 
	public CertificationBodyManager acbManager;
	
	public CertifiedProductManagerImpl() {
	}
	
	
	@Override
	@Transactional(readOnly = true)
	public CertifiedProductDTO getById(Long id) throws EntityRetrievalException {
		CertifiedProductDTO result = cpDao.getById(id);
		return result;
	}
	
	@Override
	@Transactional(readOnly = true)
	public CertifiedProductDTO getByChplProductNumber(String chplProductNumber) throws EntityRetrievalException {
		CertifiedProductDTO result = cpDao.getByChplNumber(chplProductNumber);
		return result;
	}
	
	@Override
	@Transactional(readOnly=true)
	public boolean chplIdExists(String id) throws EntityRetrievalException {
		if(StringUtils.isEmpty(id)) {
			return false;
		} 
		
		boolean exists = false;
		if(id.startsWith("CHP")) {
			CertifiedProductDTO existing = cpDao.getByChplNumber(id);
			if(existing != null) {
				exists = true;
			}
		} else {
			try {
				CertifiedProductDetailsDTO existing = cpDao.getByChplUniqueId(id);
				if(existing != null) {
					exists = true;
				}
			} catch(EntityRetrievalException ex){
				logger.error("Could not look up " + id, ex);
			}
		}
		return exists;
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<CertifiedProductDetailsDTO> getDetailsByIds(List<Long> ids) throws EntityRetrievalException {
		return cpDao.getDetailsByIds(ids);
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<CertifiedProductDetailsDTO> getAll() {
		return cpDao.findAll();
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<CertifiedProductDetailsDTO> getAllWithEditPermission() {
		List<CertificationBodyDTO> userAcbs = acbManager.getAllForUser(false);
		if(userAcbs == null || userAcbs.size() == 0) {
			return new ArrayList<CertifiedProductDetailsDTO>();
		}
		List<Long> acbIdList = new ArrayList<Long>(userAcbs.size());
		for(CertificationBodyDTO dto : userAcbs) {
			acbIdList.add(dto.getId());
		}
		return cpDao.getDetailsByAcbIds(acbIdList);
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<CertifiedProductDetailsDTO> getByVersion(Long versionId) {
		return cpDao.getDetailsByVersionId(versionId);
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<CertifiedProductDetailsDTO> getByVersionWithEditPermission(Long versionId) {
		List<CertificationBodyDTO> userAcbs = acbManager.getAllForUser(false);
		if(userAcbs == null || userAcbs.size() == 0) {
			return new ArrayList<CertifiedProductDetailsDTO>();
		}
		List<Long> acbIdList = new ArrayList<Long>(userAcbs.size());
		for(CertificationBodyDTO dto : userAcbs) {
			acbIdList.add(dto.getId());
		}
		return cpDao.getDetailsByVersionAndAcbIds(versionId, acbIdList);
	}

	@Override
	@PreAuthorize("(hasRole('ROLE_ACB_STAFF') or hasRole('ROLE_ACB_ADMIN')) "
			+ "and hasPermission(#acbId, 'gov.healthit.chpl.dto.CertificationBodyDTO', admin)")
	@Transactional(readOnly = false)
	@CacheEvict(value = {CacheNames.ALL_DEVELOPERS, CacheNames.ALL_DEVELOPERS_INCLUDING_DELETED, 
			CacheNames.DEVELOPER_NAMES, CacheNames.PRODUCT_NAMES, CacheNames.SEARCH, 
			CacheNames.COUNT_MULTI_FILTER_SEARCH_RESULTS}, allEntries=true)
	public CertifiedProductDTO createFromPending(Long acbId, PendingCertifiedProductDTO pendingCp) 
			throws EntityRetrievalException, EntityCreationException, JsonProcessingException {
		
		CertifiedProductDTO toCreate = new CertifiedProductDTO();
		toCreate.setAcbCertificationId(pendingCp.getAcbCertificationId());
		toCreate.setReportFileLocation(pendingCp.getReportFileLocation());
		toCreate.setSedReportFileLocation(pendingCp.getSedReportFileLocation());
		toCreate.setSedIntendedUserDescription(pendingCp.getSedIntendedUserDescription());
		toCreate.setSedTestingEnd(pendingCp.getSedTestingEnd());
		toCreate.setIcs(pendingCp.getIcs());
		toCreate.setAccessibilityCertified(pendingCp.getAccessibilityCertified());
		toCreate.setPracticeTypeId(pendingCp.getPracticeTypeId());
		toCreate.setProductClassificationTypeId(pendingCp.getProductClassificationId());
		toCreate.setCreationDate(new Date());
		toCreate.setDeleted(false);
		toCreate.setLastModifiedDate(new Date());
		toCreate.setLastModifiedUser(Util.getCurrentUser().getId());
		
		if(pendingCp.getCertificationBodyId() == null) {
			throw new EntityCreationException("ACB ID must be specified.");
		}
		toCreate.setCertificationBodyId(pendingCp.getCertificationBodyId());

		if(pendingCp.getTestingLabId() == null) {
			throw new EntityCreationException("ATL ID must be specified.");
		}
		toCreate.setTestingLabId(pendingCp.getTestingLabId());
		
		if(pendingCp.getCertificationEditionId() == null) {
			throw new EntityCreationException("The ID of an existing certification edition (year) must be provided. A new certification edition cannot be created via this process.");
		}
		toCreate.setCertificationEditionId(pendingCp.getCertificationEditionId());
		
		String status = pendingCp.getRecordStatus();
		if(StringUtils.isEmpty(status)) {
			throw new EntityCreationException("Cannot determine certification status. Is this a new record? An update? A removal?");
		}
		if(status.trim().equalsIgnoreCase("new")) {
			CertificationStatusDTO statusDto = certStatusDao.getByStatusName("Active");
			toCreate.setCertificationStatusId(statusDto.getId());
		}
		toCreate.setTransparencyAttestationUrl(pendingCp.getTransparencyAttestationUrl());

		DeveloperDTO developer = null;
		if(pendingCp.getDeveloperId() == null) {
			DeveloperDTO newDeveloper = new DeveloperDTO();
			if(StringUtils.isEmpty(pendingCp.getDeveloperName())) {
				throw new EntityCreationException("You must provide a developer name to create a new developer.");
			}
			newDeveloper.setName(pendingCp.getDeveloperName());
			newDeveloper.setWebsite(pendingCp.getDeveloperWebsite());
			DeveloperACBMapDTO transparencyMap = new DeveloperACBMapDTO();
			transparencyMap.setAcbId(pendingCp.getCertificationBodyId());
			transparencyMap.setAcbName(pendingCp.getCertificationBodyName());
			transparencyMap.setTransparencyAttestation(pendingCp.getTransparencyAttestation());
			newDeveloper.getTransparencyAttestationMappings().add(transparencyMap);
			AddressDTO developerAddress = pendingCp.getDeveloperAddress();
			newDeveloper.setAddress(developerAddress);
			ContactDTO developerContact = new ContactDTO();
			developerContact.setLastName(pendingCp.getDeveloperContactName());
			developerContact.setPhoneNumber(pendingCp.getDeveloperPhoneNumber());
			developerContact.setEmail(pendingCp.getDeveloperEmail());
			newDeveloper.setContact(developerContact);
			//create the dev, address, and contact
			developer = developerManager.create(newDeveloper);
			pendingCp.setDeveloperId(developer.getId());
		}
		
		if(pendingCp.getProductId() == null) {
			ProductDTO newProduct = new ProductDTO();
			if(pendingCp.getProductName() == null) {
				throw new EntityCreationException("Either product name or ID must be provided.");
			}
			newProduct.setName(pendingCp.getProductName());
			newProduct.setDeveloperId(pendingCp.getDeveloperId());
			newProduct.setReportFileLocation(pendingCp.getReportFileLocation());
			newProduct = productManager.create(newProduct);
			pendingCp.setProductId(newProduct.getId());
		} 
		
		if(pendingCp.getProductVersionId() == null) {
			ProductVersionDTO newVersion = new ProductVersionDTO();
			if(pendingCp.getProductVersion() == null) {
				throw new EntityCreationException("Either version id or version must be provided.");
			}
			newVersion.setVersion(pendingCp.getProductVersion());
			newVersion.setProductId(pendingCp.getProductId());
			newVersion = versionManager.create(newVersion);
			pendingCp.setProductVersionId(newVersion.getId());
		} 
		toCreate.setProductVersionId(pendingCp.getProductVersionId());
		
		String uniqueId = pendingCp.getUniqueId();
		String[] uniqueIdParts = uniqueId.split("\\.");
		toCreate.setProductCode(uniqueIdParts[4]);
		toCreate.setVersionCode(uniqueIdParts[5]);
		toCreate.setIcsCode(uniqueIdParts[6]);
		toCreate.setAdditionalSoftwareCode(uniqueIdParts[7]);
		toCreate.setCertifiedDateCode(uniqueIdParts[8]);
		
		CertifiedProductDTO newCertifiedProduct = cpDao.create(toCreate);
		
		//qms
		if(pendingCp.getQmsStandards() != null && pendingCp.getQmsStandards().size() > 0) {
			for(PendingCertifiedProductQmsStandardDTO qms : pendingCp.getQmsStandards()) {
				CertifiedProductQmsStandardDTO qmsDto = new CertifiedProductQmsStandardDTO();
				if(qms.getQmsStandardId() == null) {
					QmsStandardDTO toAdd = new QmsStandardDTO();
					toAdd.setName(qms.getName());
					toAdd = qmsDao.create(toAdd);
					qmsDto.setQmsStandardId(toAdd.getId());
				} else {
					qmsDto.setQmsStandardId(qms.getQmsStandardId());
				}
				qmsDto.setCertifiedProductId(newCertifiedProduct.getId());
				qmsDto.setApplicableCriteria(qms.getApplicableCriteria());
				qmsDto.setQmsModification(qms.getModification());
				cpQmsDao.createCertifiedProductQms(qmsDto);
			}
		}
		
		//targeted users
		if(pendingCp.getTargetedUsers() != null && pendingCp.getTargetedUsers().size() > 0) {
			for(PendingCertifiedProductTargetedUserDTO tu : pendingCp.getTargetedUsers()) {
				CertifiedProductTargetedUserDTO tuDto = new CertifiedProductTargetedUserDTO();
				if(tu.getTargetedUserId() == null) {
					TargetedUserDTO toAdd = new TargetedUserDTO();
					toAdd.setName(tu.getName());
					toAdd = targetedUserDao.create(toAdd);
					tuDto.setTargetedUserId(toAdd.getId());
				} else {
					tuDto.setTargetedUserId(tu.getTargetedUserId());
				}
				tuDto.setCertifiedProductId(newCertifiedProduct.getId());
				cpTargetedUserDao.createCertifiedProductTargetedUser(tuDto);
			}
		}
		
		//accessibility standards
		if(pendingCp.getAccessibilityStandards() != null && pendingCp.getAccessibilityStandards().size() > 0) {
			for(PendingCertifiedProductAccessibilityStandardDTO as : pendingCp.getAccessibilityStandards()) {
				CertifiedProductAccessibilityStandardDTO asDto = new CertifiedProductAccessibilityStandardDTO();
				asDto.setCertifiedProductId(newCertifiedProduct.getId());

				if(as.getAccessibilityStandardId() != null) {
					asDto.setAccessibilityStandardName(as.getName());
					asDto.setAccessibilityStandardId(as.getAccessibilityStandardId());
				} else {
					//check again for a matching accessibility std because the uesr could have edited
					//it since upload
					AccessibilityStandardDTO match = asDao.getByName(as.getName());
					if(match != null) {
						asDto.setAccessibilityStandardName(match.getName());
						asDto.setAccessibilityStandardId(match.getId());
					} else {
						//if it wasn't there then create it
						AccessibilityStandardDTO asToCreate = new AccessibilityStandardDTO();
						asToCreate.setName(as.getName());
						match = asDao.create(asToCreate);
						asDto.setAccessibilityStandardId(match.getId());
						asDto.setAccessibilityStandardName(match.getName());
					}
				}
				
				if(asDto.getAccessibilityStandardId() != null) {
					cpAccStdDao.createCertifiedProductAccessibilityStandard(asDto);
				} else {
					logger.error("Could not insert accessibility standard with null id. Name was " + as.getName());
				}
			}
		}
				
		//certs
		if(pendingCp.getCertificationCriterion() != null && pendingCp.getCertificationCriterion().size() > 0) {
			
			//participants and tasks are re-used across multiple certifications within the same product
			List<TestParticipantDTO> testParticipantsAdded = new ArrayList<TestParticipantDTO>();
			List<TestTaskDTO> testTasksAdded = new ArrayList<TestTaskDTO>();
			
			for(PendingCertificationResultDTO certResult : pendingCp.getCertificationCriterion()) {
				CertificationCriterionDTO criterion = certCriterionDao.getByName(certResult.getNumber());
				if(criterion == null) {
					throw new EntityCreationException("Could not find certification criterion with number " + certResult.getNumber());
				}
				CertificationResultDTO certResultToCreate = new CertificationResultDTO();
				certResultToCreate.setCertificationCriterionId(criterion.getId());
				certResultToCreate.setCertifiedProduct(newCertifiedProduct.getId());
				certResultToCreate.setSuccessful(certResult.getMeetsCriteria());
				certResultToCreate.setGap(certResult.getGap());
				certResultToCreate.setG1Success(certResult.getG1Success());
				certResultToCreate.setG2Success(certResult.getG2Success());
				if(certResult.getSed() == null) {
					if(certResult.getUcdProcesses() != null && certResult.getUcdProcesses().size() > 0) {
						certResultToCreate.setSed(Boolean.TRUE);
					} else {
						certResultToCreate.setSed(Boolean.FALSE);
					}
				} else {
					certResultToCreate.setSed(certResult.getSed());
				}
				certResultToCreate.setApiDocumentation(certResult.getApiDocumentation());
				certResultToCreate.setPrivacySecurityFramework(certResult.getPrivacySecurityFramework());
				CertificationResultDTO createdCert = certDao.create(certResultToCreate);
				
				if(certResult.getAdditionalSoftware() != null && certResult.getAdditionalSoftware().size() > 0) {
					for(PendingCertificationResultAdditionalSoftwareDTO software : certResult.getAdditionalSoftware()) {
						CertificationResultAdditionalSoftwareDTO as = new CertificationResultAdditionalSoftwareDTO();
						as.setCertifiedProductId(software.getCertifiedProductId());
						as.setJustification(software.getJustification());
						as.setName(software.getName());
						as.setVersion(software.getVersion());
						as.setGrouping(software.getGrouping());
						as.setCertificationResultId(createdCert.getId());
						certDao.addAdditionalSoftwareMapping(as);
					}
				}
				
				if(certResult.getUcdProcesses() != null && certResult.getUcdProcesses().size() > 0) {
					for(PendingCertificationResultUcdProcessDTO ucd : certResult.getUcdProcesses()) {
						CertificationResultUcdProcessDTO ucdDto = new CertificationResultUcdProcessDTO();
						if(ucd.getUcdProcessId() == null) {
							UcdProcessDTO newUcd = new UcdProcessDTO();
							newUcd.setName(ucd.getUcdProcessName());
							newUcd = ucdDao.create(newUcd);
							ucdDto.setUcdProcessId(newUcd.getId());
						} else {
							ucdDto.setUcdProcessId(ucd.getUcdProcessId());
						}
						ucdDto.setCertificationResultId(createdCert.getId());
						ucdDto.setUcdProcessDetails(ucd.getUcdProcessDetails());
						certDao.addUcdProcessMapping(ucdDto);
					}
				}
				
				if(certResult.getTestData() != null && certResult.getTestData().size() > 0) {
					for(PendingCertificationResultTestDataDTO testData : certResult.getTestData()) {
						CertificationResultTestDataDTO testDto = new CertificationResultTestDataDTO();
						testDto.setAlteration(testData.getAlteration());
						testDto.setVersion(testData.getVersion());
						testDto.setCertificationResultId(createdCert.getId());
						certDao.addTestDataMapping(testDto);
					}
				}
				
				if(certResult.getTestFunctionality() != null && certResult.getTestFunctionality().size() > 0) {
					for(PendingCertificationResultTestFunctionalityDTO func : certResult.getTestFunctionality()) {
						if(func.getTestFunctionalityId() != null) {
							CertificationResultTestFunctionalityDTO funcDto = new CertificationResultTestFunctionalityDTO();
							funcDto.setTestFunctionalityId(func.getTestFunctionalityId());
							funcDto.setCertificationResultId(createdCert.getId());
							certDao.addTestFunctionalityMapping(funcDto);
						} else {
							//check again for a matching test tool because the uesr could have edited
							//it since upload
							TestFunctionalityDTO match = testFuncDao.getByNumber(func.getNumber());
							if(match != null) {
								CertificationResultTestFunctionalityDTO funcDto = new CertificationResultTestFunctionalityDTO();
								funcDto.setTestFunctionalityId(match.getId());
								funcDto.setCertificationResultId(createdCert.getId());
								certDao.addTestFunctionalityMapping(funcDto);
							} else {
								logger.error("Could not insert test functionality with null id. Number was " + func.getNumber());
							}
						}
					}
				}
				
				if(certResult.getTestProcedures() != null && certResult.getTestProcedures().size() > 0) {
					for(PendingCertificationResultTestProcedureDTO proc : certResult.getTestProcedures()) {
						CertificationResultTestProcedureDTO procDto = new CertificationResultTestProcedureDTO();
						if(proc.getTestProcedureId() == null) {
							TestProcedureDTO tp = new TestProcedureDTO();
							tp.setVersion(proc.getVersion());
							tp = testProcDao.create(tp);
							procDto.setTestProcedureId(tp.getId());
						} else {
							procDto.setTestProcedureId(proc.getTestProcedureId());
						}
						procDto.setTestProcedureVersion(proc.getVersion());
						procDto.setCertificationResultId(createdCert.getId());
						certDao.addTestProcedureMapping(procDto);
					}
				}
				
				if(certResult.getTestStandards() != null && certResult.getTestStandards().size() > 0) {
					for(PendingCertificationResultTestStandardDTO std : certResult.getTestStandards()) {
						CertificationResultTestStandardDTO stdDto = new CertificationResultTestStandardDTO();
						if(std.getTestStandardId() == null) {
							TestStandardDTO ts = new TestStandardDTO();
							ts.setName(std.getName());
							ts = testStandardDao.create(ts);
							stdDto.setTestStandardId(ts.getId());
						} else {
							stdDto.setTestStandardId(std.getTestStandardId());
						}
						stdDto.setCertificationResultId(createdCert.getId());
						certDao.addTestStandardMapping(stdDto);
					}
				}
				
				if(certResult.getTestTools() != null && certResult.getTestTools().size() > 0) {
					for(PendingCertificationResultTestToolDTO tool : certResult.getTestTools()) {
						if(tool.getTestToolId() != null) {
							CertificationResultTestToolDTO toolDto = new CertificationResultTestToolDTO();
							toolDto.setTestToolId(tool.getTestToolId());
							toolDto.setTestToolVersion(tool.getVersion());
							toolDto.setCertificationResultId(createdCert.getId());
							certDao.addTestToolMapping(toolDto);
						} else {
							//check again for a matching test tool because the user could have edited
							//it since upload
							TestToolDTO match = testToolDao.getByName(tool.getName());
							if(match != null) {
								CertificationResultTestToolDTO toolDto = new CertificationResultTestToolDTO();
								toolDto.setTestToolId(match.getId());
								toolDto.setTestToolVersion(tool.getVersion());
								toolDto.setCertificationResultId(createdCert.getId());
								certDao.addTestToolMapping(toolDto);
							} else {
								logger.error("Could not insert test tool with null id. Name was " + tool.getName());
							}
						}
					}
				}
				
				if(certResult.getG1MacraMeasures() != null && certResult.getG1MacraMeasures().size() > 0) {
					for(PendingCertificationResultMacraMeasureDTO pendingMeasure : certResult.getG1MacraMeasures()) {
						//the validator set the macraMeasure value so it's definitely filled in
						if(pendingMeasure.getMacraMeasure() != null && pendingMeasure.getMacraMeasure().getId() != null) {
							CertificationResultMacraMeasureDTO crMeasure = new CertificationResultMacraMeasureDTO();
							crMeasure.setMeasure(pendingMeasure.getMacraMeasure());
							crMeasure.setCertificationResultId(createdCert.getId());
							certDao.addG1MacraMeasureMapping(crMeasure);
						} else {
							logger.error("Found G1 Macra Measure with null value for " + certResult.getNumber());
						}
					}
				}
				
				if(certResult.getG2MacraMeasures() != null && certResult.getG2MacraMeasures().size() > 0) {
					for(PendingCertificationResultMacraMeasureDTO pendingMeasure : certResult.getG2MacraMeasures()) {
						//the validator set the macraMeasure value so it's definitely filled in
						if(pendingMeasure.getMacraMeasure() != null && pendingMeasure.getMacraMeasure().getId() != null) {
							CertificationResultMacraMeasureDTO crMeasure = new CertificationResultMacraMeasureDTO();
							crMeasure.setMeasure(pendingMeasure.getMacraMeasure());
							crMeasure.setCertificationResultId(createdCert.getId());
							certDao.addG2MacraMeasureMapping(crMeasure);
						} else {
							logger.error("Found G2 Macra Measure with null value for " + certResult.getNumber());
						}
					}
				}
				
				if(certResult.getTestTasks() != null && certResult.getTestTasks().size() > 0) {
					for(PendingCertificationResultTestTaskDTO certTask : certResult.getTestTasks()) {
						//have we already added this one?
						TestTaskDTO existingTt = null;
						for(TestTaskDTO tt : testTasksAdded) {
							if(certTask.getPendingTestTask() != null && 
								certTask.getPendingTestTask().getUniqueId().equals(tt.getPendingUniqueId())) {
								existingTt = tt;
							}
						}
						if(existingTt == null && certTask.getPendingTestTask() != null) {
							PendingTestTaskDTO pendingTask = certTask.getPendingTestTask();
							TestTaskDTO tt = new TestTaskDTO();
							tt.setDescription(pendingTask.getDescription());
							tt.setTaskErrors(pendingTask.getTaskErrors());
							tt.setTaskErrorsStddev(pendingTask.getTaskErrorsStddev());
							tt.setTaskPathDeviationObserved(pendingTask.getTaskPathDeviationObserved());
							tt.setTaskPathDeviationOptimal(pendingTask.getTaskPathDeviationOptimal());
							tt.setTaskRating(pendingTask.getTaskRating());
							tt.setTaskRatingScale(pendingTask.getTaskRatingScale());
							tt.setTaskRatingStddev(pendingTask.getTaskRatingStddev());
							tt.setTaskSuccessAverage(pendingTask.getTaskSuccessAverage());
							tt.setTaskSuccessStddev(pendingTask.getTaskSuccessStddev());
							tt.setTaskTimeAvg(pendingTask.getTaskTimeAvg());
							tt.setTaskTimeDeviationObservedAvg(pendingTask.getTaskTimeDeviationObservedAvg());
							tt.setTaskTimeDeviationOptimalAvg(pendingTask.getTaskTimeDeviationOptimalAvg());
							tt.setTaskTimeStddev(pendingTask.getTaskTimeStddev());
							
							//add test task
							existingTt = testTaskDao.create(tt);
							existingTt.setPendingUniqueId(pendingTask.getUniqueId());
							testTasksAdded.add(existingTt);
						}
						//add mapping from cert result to test task
						CertificationResultTestTaskDTO taskDto = new CertificationResultTestTaskDTO();
						taskDto.setTestTaskId(existingTt.getId());
						taskDto.setCertificationResultId(createdCert.getId());
							
						if(certTask.getTaskParticipants() != null) {
							for(PendingCertificationResultTestTaskParticipantDTO certTaskPart : certTask.getTaskParticipants()) {
								PendingTestParticipantDTO certPart = certTaskPart.getTestParticipant();
								if(certPart != null) {
									TestParticipantDTO existingPart = null;
									for(TestParticipantDTO currPart : testParticipantsAdded) {
										if(currPart.getPendingUniqueId().equals(certPart.getUniqueId())) {
											existingPart = currPart;
										}
									}
									if(existingPart == null) {
										TestParticipantDTO tp = new TestParticipantDTO();
										tp.setAgeRangeId(certPart.getAgeRangeId());
										tp.setAssistiveTechnologyNeeds(certPart.getAssistiveTechnologyNeeds());
										tp.setComputerExperienceMonths(certPart.getComputerExperienceMonths());
										tp.setEducationTypeId(certPart.getEducationTypeId());
										tp.setGender(certPart.getGender());
										tp.setOccupation(certPart.getOccupation());
										tp.setProductExperienceMonths(certPart.getProductExperienceMonths());
										tp.setProfessionalExperienceMonths(certPart.getProfessionalExperienceMonths());
										
										//add participant
										existingPart = testParticipantDao.create(tp);
										existingPart.setPendingUniqueId(certPart.getUniqueId());
										testParticipantsAdded.add(existingPart);
									}
									
									CertificationResultTestTaskParticipantDTO certPartDto = new CertificationResultTestTaskParticipantDTO();
									certPartDto.setTestParticipantId(existingPart.getId());
									certPartDto.setCertTestTaskId(taskDto.getId());
									taskDto.getTaskParticipants().add(certPartDto);
								}
							}
						}
						
						certDao.addTestTaskMapping(taskDto);
					}
				}
			}
		}
		
		//cqms
		//we only insert successful ones, but all of the ones in the pendingDTO 
		//are successful
		if(pendingCp.getCqmCriterion() != null && pendingCp.getCqmCriterion().size() > 0) {
			for(PendingCqmCriterionDTO pendingCqm : pendingCp.getCqmCriterion()) {
				if(pendingCqm.isMeetsCriteria() && !StringUtils.isEmpty(pendingCqm.getVersion())) {
					CQMCriterionDTO criterion = null;
					if(pendingCqm.getCmsId().startsWith("CMS")) {
						criterion = cqmCriterionDao.getCMSByNumberAndVersion(pendingCqm.getCmsId(), pendingCqm.getVersion());
						
						if(criterion == null) {
							throw new EntityCreationException("Could not find a CQM with number " + pendingCqm.getCmsId() + 
									" and version " + pendingCqm.getVersion() + ".");
						}
						
						CQMResultDTO cqmResultToCreate = new CQMResultDTO();
						cqmResultToCreate.setCqmCriterionId(criterion.getId());
						cqmResultToCreate.setCertifiedProductId(newCertifiedProduct.getId());
						cqmResultToCreate.setSuccess(pendingCqm.isMeetsCriteria());
						if(pendingCqm.getCertifications() != null) {
							for(PendingCqmCertificationCriterionDTO cert : pendingCqm.getCertifications()) {
								CQMResultCriteriaDTO certDto = new CQMResultCriteriaDTO();
								if(!StringUtils.isEmpty(cert.getCertificationId())) {
									certDto.setCriterionId(cert.getCertificationId());
									cqmResultToCreate.getCriteria().add(certDto);
								} else if(!StringUtils.isEmpty(cert.getCertificationCriteriaNumber())){
									CertificationCriterionDTO critDto = certCriterionDao.getByName(cert.getCertificationCriteriaNumber());
									if(critDto != null) {
										certDto.setCriterionId(critDto.getId());
										cqmResultToCreate.getCriteria().add(certDto);
									} else {
										logger.error("Could not find a matching certification criterion for '" + cert.getCertificationCriteriaNumber() + "'.");
									}
								} else {
									logger.error("Neither certification id or number was specified.");
								}
							}
						}
						cqmResultDAO.create(cqmResultToCreate);
					}
				}
			}
		}
		
		
		//if all this was successful, insert a certification status event for the certification date
		CertificationStatusDTO activeCertStatus = certStatusDao.getByStatusName(CertificationStatusType.Active.toString());
		CertificationStatusEventDTO certEvent = new CertificationStatusEventDTO();
		certEvent.setCreationDate(new Date());
		certEvent.setDeleted(false);
		Date certificationDate = pendingCp.getCertificationDate();
		certEvent.setEventDate(certificationDate);
		certEvent.setStatus(activeCertStatus);
		certEvent.setCertifiedProductId(newCertifiedProduct.getId());
		statusEventDao.create(certEvent);
		
		CertifiedProductSearchDetails details = detailsManager.getCertifiedProductDetails(newCertifiedProduct.getId());
		activityManager.addActivity(ActivityConcept.ACTIVITY_CONCEPT_CERTIFIED_PRODUCT, details.getId(), "Created "+newCertifiedProduct.getChplProductNumberForActivity(), null, details);
		
		return newCertifiedProduct;
	}
	
	@Override
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@Transactional(readOnly = false) 
	@ClearAllCaches
	public CertifiedProductDTO changeOwnership(Long certifiedProductId, Long acbId) throws EntityRetrievalException, JsonProcessingException, EntityCreationException {
		CertifiedProductDTO toUpdate = cpDao.getById(certifiedProductId);
		toUpdate.setCertificationBodyId(acbId);
		return cpDao.update(toUpdate);
	}
	
	@Override
	@PreAuthorize("hasRole('ROLE_ADMIN') or "
			+ "( (hasRole('ROLE_ACB_STAFF') or hasRole('ROLE_ACB_ADMIN'))"
			+ "  and hasPermission(#acbId, 'gov.healthit.chpl.dto.CertificationBodyDTO', admin)"
			+ ")")
	@Transactional(readOnly = false)
	@CacheEvict(value = {CacheNames.ALL_DEVELOPERS, CacheNames.ALL_DEVELOPERS_INCLUDING_DELETED, CacheNames.SEARCH, 
			CacheNames.COUNT_MULTI_FILTER_SEARCH_RESULTS}, allEntries=true)
	public CertifiedProductDTO update(Long acbId, CertifiedProductDTO dto, CertifiedProductSearchDetails updateRequest) 
			throws AccessDeniedException, EntityRetrievalException, JsonProcessingException, EntityCreationException {		
		//if the updated certification status was suspended by onc or terminated by onc, 
		//change the status of the related developer
		CertificationStatusDTO updatedCertificationStatus = certStatusDao.getById(dto.getCertificationStatusId());
		if(updatedCertificationStatus.getStatus().equals(CertificationStatusType.SuspendedByOnc.toString()) || 
			updatedCertificationStatus.getStatus().equals(CertificationStatusType.TerminatedByOnc.toString())|| 
			updatedCertificationStatus.getStatus().equals(CertificationStatusType.WithdrawnByDeveloperUnderReview.toString())) {
			
			//get developer
			DeveloperDTO cpDeveloper = developerDao.getByVersion(dto.getProductVersionId());
			if(Util.isUserRoleAdmin() && cpDeveloper != null) {
				//find the new developer status
				DeveloperStatusDTO devStatusDto = null;
				if(updatedCertificationStatus.getStatus().equals(CertificationStatusType.SuspendedByOnc.toString())) {
					devStatusDto = devStatusDao.getByName(DeveloperStatusType.SuspendedByOnc.toString());
				} else if(updatedCertificationStatus.getStatus().equals(CertificationStatusType.TerminatedByOnc.toString()) ||
						updatedCertificationStatus.getStatus().equals(CertificationStatusType.WithdrawnByDeveloperUnderReview.toString())) {
					devStatusDto = devStatusDao.getByName(DeveloperStatusType.UnderCertificationBanByOnc.toString());
				} 
				//update the developer status
				if(devStatusDto == null) {
					throw new EntityRetrievalException("Could not locate developer status for certification status " + updatedCertificationStatus.getStatus());
				}
				cpDeveloper.setStatus(devStatusDto);
				developerManager.update(cpDeveloper);
			} else if (!Util.isUserRoleAdmin()) {
				logger.error("User " + Util.getUsername() + " does not have ROLE_ADMIN and cannot change the status of developer for certified product with id " + dto.getId());
				throw new AccessDeniedException("User does not have admin permission to change related developer status.");	
			} else if(cpDeveloper == null) {
				logger.error("Could not find developer for product version with id " + dto.getProductVersionId());
				throw new EntityNotFoundException("No developer could be located for the certified product in the update. Update cannot continue.");
			}
		}
		
		CertifiedProductDTO result = cpDao.update(dto);	
		
		if(updateRequest != null){
			//update qms standards used
			List<CertifiedProductQmsStandardDTO> qmsStandardsToUpdate = new ArrayList<CertifiedProductQmsStandardDTO>();
			for(CertifiedProductQmsStandard newQms : updateRequest.getQmsStandards()) {
				CertifiedProductQmsStandardDTO cpQmsdto = new CertifiedProductQmsStandardDTO();
				cpQmsdto.setId(newQms.getId());
				cpQmsdto.setApplicableCriteria(newQms.getApplicableCriteria());
				cpQmsdto.setCertifiedProductId(dto.getId());
				cpQmsdto.setQmsModification(newQms.getQmsModification());
				cpQmsdto.setQmsStandardId(newQms.getQmsStandardId());
				cpQmsdto.setQmsStandardName(newQms.getQmsStandardName());
				qmsStandardsToUpdate.add(cpQmsdto);
			}
			updateQmsStandards(acbId, dto, qmsStandardsToUpdate);
			
			//update targeted users
			List<CertifiedProductTargetedUserDTO> targetedUsersToUpdate = new ArrayList<CertifiedProductTargetedUserDTO>();
			for(CertifiedProductTargetedUser newTu : updateRequest.getTargetedUsers()) {
				CertifiedProductTargetedUserDTO cptdto = new CertifiedProductTargetedUserDTO();
				cptdto.setId(newTu.getId());
				cptdto.setCertifiedProductId(dto.getId());
				cptdto.setTargetedUserId(newTu.getTargetedUserId());
				cptdto.setTargetedUserName(newTu.getTargetedUserName());
				targetedUsersToUpdate.add(cptdto);
			}
			updateTargetedUsers(acbId, dto, targetedUsersToUpdate);
			
			//update accessibility standards
			List<CertifiedProductAccessibilityStandardDTO> accessibilityStandardsToUpdate = new ArrayList<CertifiedProductAccessibilityStandardDTO>();
			for(CertifiedProductAccessibilityStandard newStd : updateRequest.getAccessibilityStandards()) {
				CertifiedProductAccessibilityStandardDTO cpasdto = new CertifiedProductAccessibilityStandardDTO();
				cpasdto.setId(newStd.getId());
				cpasdto.setCertifiedProductId(dto.getId());
				cpasdto.setAccessibilityStandardId(newStd.getAccessibilityStandardId());
				cpasdto.setAccessibilityStandardName(newStd.getAccessibilityStandardName());
				accessibilityStandardsToUpdate.add(cpasdto);
			}
			updateAccessibilityStandards(acbId, dto, accessibilityStandardsToUpdate);
			
			//update certification date
			updateCertificationDate(acbId, dto, new Date(updateRequest.getCertificationDate()));
			
			//possibly add something to certification status event
			updateCertificationStatusEvents(acbId, dto);
			
			//update product certifications
			updateCertifications(acbId, dto, updateRequest.getCertificationResults());
			
			//update CQMs
			List<CQMResultDetailsDTO> cqmDtos = new ArrayList<CQMResultDetailsDTO>();
			for(CQMResultDetails cqm : updateRequest.getCqmResults()) {
				if(!StringUtils.isEmpty(cqm.getCmsId()) && cqm.getSuccessVersions() != null && cqm.getSuccessVersions().size() > 0) {
					for(String version : cqm.getSuccessVersions()) {
						CQMResultDetailsDTO cqmDto = new CQMResultDetailsDTO();
						cqmDto.setNqfNumber(cqm.getNqfNumber());
						cqmDto.setCmsId(cqm.getCmsId());
						cqmDto.setNumber(cqm.getNumber());
						cqmDto.setCmsId(cqm.getCmsId());
						cqmDto.setNqfNumber(cqm.getNqfNumber());
						cqmDto.setTitle(cqm.getTitle());
						cqmDto.setVersion(version);
						cqmDto.setSuccess(Boolean.TRUE);
						if(cqm.getCriteria() != null && cqm.getCriteria().size() > 0) {
							for(CQMResultCertification criteria : cqm.getCriteria()) {
								CQMResultCriteriaDTO cqmdto = new CQMResultCriteriaDTO();
								cqmdto.setCriterionId(criteria.getCertificationId());
								CertificationCriterionDTO certDto = new CertificationCriterionDTO();
								certDto.setNumber(criteria.getCertificationNumber());
								cqmdto.setCriterion(certDto);
								cqmDto.getCriteria().add(cqmdto);
							}
						}
						cqmDtos.add(cqmDto);
					}
				} else if(StringUtils.isEmpty(cqm.getCmsId())) {
					CQMResultDetailsDTO cqmDto = new CQMResultDetailsDTO();
					cqmDto.setNqfNumber(cqm.getNqfNumber());
					cqmDto.setCmsId(cqm.getCmsId());
					cqmDto.setNumber(cqm.getNumber());
					cqmDto.setCmsId(cqm.getCmsId());
					cqmDto.setNqfNumber(cqm.getNqfNumber());
					cqmDto.setTitle(cqm.getTitle());
					cqmDto.setSuccess(cqm.isSuccess());
					cqmDtos.add(cqmDto);
				}
			}
			updateCqms(acbId, dto, cqmDtos);
		}
		
		return result;
	}	
	
	@Override
	@PreAuthorize("hasRole('ROLE_ADMIN') or "
			+ "( (hasRole('ROLE_ACB_STAFF') or hasRole('ROLE_ACB_ADMIN'))"
			+ "  and hasPermission(#acbId, 'gov.healthit.chpl.dto.CertificationBodyDTO', admin)"
			+ ")")
	@Transactional(readOnly = false)
	public void updateQmsStandards(Long acbId, CertifiedProductDTO productDto, List<CertifiedProductQmsStandardDTO> newQmsStandards)
		throws EntityCreationException, EntityRetrievalException, JsonProcessingException {
		
		List<CertifiedProductQmsStandardDTO> beforeQms = cpQmsDao.getQmsStandardsByCertifiedProductId(productDto.getId());
		List<CertifiedProductQmsStandardDTO> qmsToAdd = new ArrayList<CertifiedProductQmsStandardDTO>();
		List<CertifiedProductQmsStandardDTO> qmsToRemove = new ArrayList<CertifiedProductQmsStandardDTO>();
		
		for (CertifiedProductQmsStandardDTO newQmsStandard : newQmsStandards){
			//try to look up by id and name
			QmsStandardDTO qms = null;
			if(newQmsStandard.getQmsStandardId() != null) {
				qms = qmsDao.getById(newQmsStandard.getQmsStandardId());
			} 
			if(qms == null && !StringUtils.isEmpty(newQmsStandard.getQmsStandardName())) {
				qms = qmsDao.getByName(newQmsStandard.getQmsStandardName());
			}
			
			//if we haven't found it by id or name, create it
			if(qms == null) {
				QmsStandardDTO toCreate = new QmsStandardDTO();
				toCreate.setName(newQmsStandard.getQmsStandardName());
				qms = qmsDao.create(toCreate);
			}
			newQmsStandard.setQmsStandardId(qms.getId());

			
			//does a mapping for this qms std already exist??
			CertifiedProductQmsStandardDTO existingMapping = cpQmsDao.
					lookupMapping(newQmsStandard.getCertifiedProductId(), newQmsStandard.getQmsStandardId());
			if(existingMapping == null) {
				//if the mapping doesn't exist between this std and this product, add it
				qmsToAdd.add(newQmsStandard);
			} else {
				//it exists so update it
				cpQmsDao.updateCertifiedProductQms(newQmsStandard);
			}
		}
		
		for(CertifiedProductQmsStandardDTO currQms : beforeQms) {
			boolean isInUpdate = false;
			for (CertifiedProductQmsStandardDTO newQms : newQmsStandards){
				if(newQms.getQmsStandardId() != null && 
						newQms.getQmsStandardId().longValue() == currQms.getQmsStandardId().longValue()) {
					isInUpdate = true;
				}
			}
			if(!isInUpdate) {
				qmsToRemove.add(currQms);
			}
		}
			
		for(CertifiedProductQmsStandardDTO toAdd : qmsToAdd) {
			cpQmsDao.createCertifiedProductQms(toAdd);
		}
		
		for(CertifiedProductQmsStandardDTO toRemove : qmsToRemove) {
			cpQmsDao.deleteCertifiedProductQms(toRemove.getId());
		}	
	}
	
	@Override
	@PreAuthorize("hasRole('ROLE_ADMIN') or "
			+ "( (hasRole('ROLE_ACB_STAFF') or hasRole('ROLE_ACB_ADMIN'))"
			+ "  and hasPermission(#acbId, 'gov.healthit.chpl.dto.CertificationBodyDTO', admin)"
			+ ")")
	@Transactional(readOnly = false)
	public void updateTargetedUsers(Long acbId, CertifiedProductDTO productDto, List<CertifiedProductTargetedUserDTO> newTargetedUsers)
		throws EntityCreationException, EntityRetrievalException, JsonProcessingException {
		
		List<CertifiedProductTargetedUserDTO> beforeTUs = cpTargetedUserDao.getTargetedUsersByCertifiedProductId(productDto.getId());
		List<CertifiedProductTargetedUserDTO> tusToAdd = new ArrayList<CertifiedProductTargetedUserDTO>();
		List<CertifiedProductTargetedUserDTO> tusToRemove = new ArrayList<CertifiedProductTargetedUserDTO>();
		
		for (CertifiedProductTargetedUserDTO newTu : newTargetedUsers){
			TargetedUserDTO tu = null;
			if(newTu.getTargetedUserId() != null) {
				tu = targetedUserDao.getById(newTu.getTargetedUserId());
			}
			if(tu == null && !StringUtils.isEmpty(newTu.getTargetedUserName())) {
				tu = targetedUserDao.getByName(newTu.getTargetedUserName());
			}
			
			//if we haven't found it by id or name, create it
			if(tu == null) {
				TargetedUserDTO toCreate = new TargetedUserDTO();
				toCreate.setName(newTu.getTargetedUserName());
				tu = targetedUserDao.create(toCreate);
			}
			newTu.setTargetedUserId(tu.getId());

			//if there's no mapping
			CertifiedProductTargetedUserDTO existingMapping = cpTargetedUserDao.
					lookupMapping(newTu.getCertifiedProductId(), newTu.getTargetedUserId());
			if(existingMapping == null) {
				tusToAdd.add(newTu);
			} 
		}
		
		for(CertifiedProductTargetedUserDTO currTu : beforeTUs) {
			boolean isInUpdate = false;
			for (CertifiedProductTargetedUserDTO newTu : newTargetedUsers){
				if(newTu.getTargetedUserId() != null && 
						newTu.getTargetedUserId().longValue() == currTu.getTargetedUserId().longValue()) {
					isInUpdate = true;
				}
			}
			if(!isInUpdate) {
				tusToRemove.add(currTu);
			}
		}
			
		for(CertifiedProductTargetedUserDTO toAdd : tusToAdd) {
			cpTargetedUserDao.createCertifiedProductTargetedUser(toAdd);
		}
		
		for(CertifiedProductTargetedUserDTO toRemove : tusToRemove) {
			cpTargetedUserDao.deleteCertifiedProductTargetedUser(toRemove.getId());
		}	
	}
	
	@Override
	@PreAuthorize("hasRole('ROLE_ADMIN') or "
			+ "( (hasRole('ROLE_ACB_STAFF') or hasRole('ROLE_ACB_ADMIN'))"
			+ "  and hasPermission(#acbId, 'gov.healthit.chpl.dto.CertificationBodyDTO', admin)"
			+ ")")
	@Transactional(readOnly = false)
	public void updateAccessibilityStandards(Long acbId, CertifiedProductDTO productDto, List<CertifiedProductAccessibilityStandardDTO> newStandards)
		throws EntityCreationException, EntityRetrievalException, JsonProcessingException {
		
		List<CertifiedProductAccessibilityStandardDTO> beforeStds = cpAccStdDao.getAccessibilityStandardsByCertifiedProductId(productDto.getId());
		List<CertifiedProductAccessibilityStandardDTO> stdsToAdd = new ArrayList<CertifiedProductAccessibilityStandardDTO>();
		List<CertifiedProductAccessibilityStandardDTO> stdsToRemove = new ArrayList<CertifiedProductAccessibilityStandardDTO>();
		
		for (CertifiedProductAccessibilityStandardDTO newStd : newStandards){
			AccessibilityStandardDTO as = null;
			if(newStd.getAccessibilityStandardId() != null) {
				as = asDao.getById(newStd.getAccessibilityStandardId());
			}
			if(as == null && !StringUtils.isEmpty(newStd.getAccessibilityStandardName())) {
				as = asDao.getByName(newStd.getAccessibilityStandardName());
			}

			//if not found by id or name, create it
			if(as == null) {
				AccessibilityStandardDTO toCreate = new AccessibilityStandardDTO();
				toCreate.setName(newStd.getAccessibilityStandardName());
				as = asDao.create(toCreate);
			}
			newStd.setAccessibilityStandardId(as.getId());

			//if no mapping existed before, add it
			CertifiedProductAccessibilityStandardDTO existingMapping = cpAccStdDao.
					lookupMapping(newStd.getCertifiedProductId(), newStd.getAccessibilityStandardId());
			if(existingMapping == null) {
				stdsToAdd.add(newStd);
			} 
		}
		
		for(CertifiedProductAccessibilityStandardDTO currStd : beforeStds) {
			boolean isInUpdate = false;
			for (CertifiedProductAccessibilityStandardDTO newStd : newStandards){
				if(newStd.getAccessibilityStandardId() != null && 
						newStd.getAccessibilityStandardId().longValue() == currStd.getAccessibilityStandardId().longValue()) {
					isInUpdate = true;
				}
			}
			if(!isInUpdate) {
				stdsToRemove.add(currStd);
			}
		}
			
		for(CertifiedProductAccessibilityStandardDTO toAdd : stdsToAdd) {
			cpAccStdDao.createCertifiedProductAccessibilityStandard(toAdd);
		}
		
		for(CertifiedProductAccessibilityStandardDTO toRemove : stdsToRemove) {
			cpAccStdDao.deleteCertifiedProductAccessibilityStandards(toRemove.getId());
		}	
	}
	
	@Override
	@PreAuthorize("hasRole('ROLE_ADMIN') or "
			+ "( (hasRole('ROLE_ACB_STAFF') or hasRole('ROLE_ACB_ADMIN'))"
			+ "  and hasPermission(#acbId, 'gov.healthit.chpl.dto.CertificationBodyDTO', admin)"
			+ ")")
	@Transactional(readOnly = false)
	public void updateCertificationDate(Long acbId, CertifiedProductDTO productDto, Date newCertDate)
		throws EntityCreationException, EntityRetrievalException, JsonProcessingException {
		CertifiedProductDetailsDTO existingCp = cpDao.getDetailsById(productDto.getId());
		if(existingCp != null && existingCp.getCertificationDate().getTime() != newCertDate.getTime()) {
			CertificationStatusEventDTO certificationEvent = statusEventDao.findInitialCertificationEventForCertifiedProduct(productDto.getId());
			if(certificationEvent != null) {
				certificationEvent.setEventDate(newCertDate);
				statusEventDao.update(certificationEvent);
			}
		}
	}
	
	@Override
	@PreAuthorize("hasRole('ROLE_ADMIN') or "
			+ "( (hasRole('ROLE_ACB_STAFF') or hasRole('ROLE_ACB_ADMIN'))"
			+ "  and hasPermission(#acbId, 'gov.healthit.chpl.dto.CertificationBodyDTO', admin)"
			+ ")")
	@Transactional(readOnly = false)
	public void updateCertificationStatusEvents(Long acbId, CertifiedProductDTO productDto)
		throws EntityCreationException, EntityRetrievalException, JsonProcessingException {
		CertifiedProductDetailsDTO existingCp = cpDao.getDetailsById(productDto.getId());
		if(existingCp != null && 
			existingCp.getCertificationStatusId().longValue() != productDto.getCertificationStatusId().longValue()) {
			CertificationStatusEventDTO certificationEvent = new CertificationStatusEventDTO();
			certificationEvent.setCertifiedProductId(existingCp.getId());
			certificationEvent.setEventDate(new Date());
			CertificationStatusDTO status = certStatusDao.getById(productDto.getCertificationStatusId());
			if(status == null) {
				throw new EntityRetrievalException("No certification status found with id " + productDto.getCertificationStatusId());
			}
			certificationEvent.setStatus(status);
			statusEventDao.create(certificationEvent);
		}
	}
	
	@Override
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_ONC_STAFF')")
	@Transactional(readOnly = false)
	@CacheEvict(value = {CacheNames.GET_DECERTIFIED_DEVELOPERS, CacheNames.SEARCH}, allEntries=true)
	public MeaningfulUseUserResults updateMeaningfulUseUsers(Set<MeaningfulUseUser> meaningfulUseUserSet)
			throws EntityCreationException, EntityRetrievalException, IOException {
		MeaningfulUseUserResults meaningfulUseUserResults = new MeaningfulUseUserResults();
		List<MeaningfulUseUser> errors = new ArrayList<MeaningfulUseUser>();
		List<MeaningfulUseUser> results = new ArrayList<MeaningfulUseUser>();
		
		for(MeaningfulUseUser muu : meaningfulUseUserSet){
			if(StringUtils.isEmpty(muu.getError())){
				try{
					// If bad input, add error for this MeaningfulUseUser and continue
					if((muu.getProductNumber() == null || muu.getProductNumber().isEmpty())){
						muu.setError("Line " + muu.getCsvLineNumber() + ": Field \"chpl_product_number\" has invalid value: \"" + muu.getProductNumber() + "\".");
					}
					else if(muu.getNumberOfUsers() == null){
						muu.setError("Line " + muu.getCsvLineNumber() + ": Field \"num_meaningful_users\" has invalid value: \"" + muu.getNumberOfUsers() + "\".");
					}
					else{
						CertifiedProductDTO dto = new CertifiedProductDTO();
						// check if 2014 edition CHPL Product Number exists
						if(cpDao.getByChplNumber(muu.getProductNumber()) != null){
							dto.setChplProductNumber(muu.getProductNumber());
							dto.setMeaningfulUseUsers(muu.getNumberOfUsers());
						}
						// check if 2015 edition CHPL Product Number exists
						else if (cpDao.getByChplUniqueId(muu.getProductNumber()) != null){
							dto.setChplProductNumber(muu.getProductNumber());
							dto.setMeaningfulUseUsers(muu.getNumberOfUsers());
						}
						// If neither exist, add error
						else{
							throw new EntityRetrievalException();
						}
						
						try{
							CertifiedProductDTO returnDto = cpDao.updateMeaningfulUseUsers(dto);
							muu.setCertifiedProductId(returnDto.getId());
							results.add(muu);
						} catch (EntityRetrievalException e){
							muu.setError("Line " + muu.getCsvLineNumber() + ": Field \"chpl_product_number\" with value \"" + muu.getProductNumber() + "\" is invalid. "
									+ "The provided \"chpl_product_number\" does not exist.");
							errors.add(muu);
						}
					}
				} catch (Exception e){	
					muu.setError("Line " + muu.getCsvLineNumber() + ": Field \"chpl_product_number\" with value \""+ muu.getProductNumber() + "\" is invalid. "
							+ "The provided \"chpl_product_number\" does not exist.");
					errors.add(muu);
				}
			}
			else{
				errors.add(muu);
			}
		}
		
		meaningfulUseUserResults.setMeaningfulUseUsers(results);
		meaningfulUseUserResults.setErrors(errors);
		return meaningfulUseUserResults;
	}
	
	/**
	 * both successes and failures are passed in
	 * @throws JsonProcessingException 
	 */
	@Override
	@PreAuthorize("hasRole('ROLE_ADMIN') or "
			+ "( (hasRole('ROLE_ACB_STAFF') or hasRole('ROLE_ACB_ADMIN'))"
			+ "  and hasPermission(#acbId, 'gov.healthit.chpl.dto.CertificationBodyDTO', admin)"
			+ ")")
	@Transactional(readOnly = false)
	public void updateCertifications(Long acbId, CertifiedProductDTO productDto, List<CertificationResult> newCertResults)
		throws EntityCreationException, EntityRetrievalException, JsonProcessingException {
		
		List<CertificationResultDTO> oldCertificationResults = certDao.findByCertifiedProductId(productDto.getId());
		
		for (CertificationResultDTO oldResult : oldCertificationResults){
			CertificationCriterionDTO criterionDTO = certCriterionDao.getById(oldResult.getCertificationCriterionId());
			
			for (CertificationResult newCertResult : newCertResults){
				//update whether the certification criterion was met or not
				if (newCertResult.getNumber().equals(criterionDTO.getNumber())){	
					// replace the value of the result. we shouldn't have to add or delete any cert results
					// because for certification criteria, all results are always there whether they were
					// successful or not
					oldResult.setSuccessful(newCertResult.isSuccess());
					
					if(certRules.hasCertOption(criterionDTO.getNumber(), CertificationResultRules.GAP)) {
						oldResult.setGap(newCertResult.isGap());
					} else {
						oldResult.setGap(null);
					}
					if(certRules.hasCertOption(criterionDTO.getNumber(), CertificationResultRules.G1_SUCCESS)) {
						oldResult.setG1Success(newCertResult.isG1Success());
					} else {
						oldResult.setG1Success(null);
					}
					if(certRules.hasCertOption(criterionDTO.getNumber(), CertificationResultRules.G2_SUCCESS)) {
						oldResult.setG2Success(newCertResult.isG2Success());
					} else {
						oldResult.setG2Success(null);
					}
					if(certRules.hasCertOption(criterionDTO.getNumber(), CertificationResultRules.API_DOCUMENTATION)) {
						oldResult.setApiDocumentation(newCertResult.getApiDocumentation());
					} else {
						oldResult.setApiDocumentation(null);
					}
					if(certRules.hasCertOption(criterionDTO.getNumber(), CertificationResultRules.PRIVACY_SECURITY)) {
						oldResult.setPrivacySecurityFramework(newCertResult.getPrivacySecurityFramework());
					} else {
						oldResult.setPrivacySecurityFramework(null);
					}
					
					if(certRules.hasCertOption(criterionDTO.getNumber(), CertificationResultRules.SED)) {
						oldResult.setSed(newCertResult.isSed());
					} else {
						oldResult.setSed(null);
					}
					
					if(!certRules.hasCertOption(criterionDTO.getNumber(), CertificationResultRules.UCD_FIELDS) ||
							newCertResult.getUcdProcesses() == null || newCertResult.getUcdProcesses().size() == 0) {
						oldResult.setUcdProcesses(new ArrayList<CertificationResultUcdProcessDTO>());
					} else {
						for(CertificationResultUcdProcess newUcdProcess : newCertResult.getUcdProcesses()) {
							CertificationResultUcdProcessDTO ucd = new CertificationResultUcdProcessDTO();
							ucd.setId(newUcdProcess.getId());
							ucd.setCertificationResultId(oldResult.getId());
							ucd.setUcdProcessDetails(newUcdProcess.getUcdProcessDetails());
							ucd.setUcdProcessId(newUcdProcess.getUcdProcessId());
							ucd.setUcdProcessName(newUcdProcess.getUcdProcessName());
							oldResult.getUcdProcesses().add(ucd);
						}
						oldResult.setSed(Boolean.TRUE);
					}

					if(!certRules.hasCertOption(criterionDTO.getNumber(), CertificationResultRules.ADDITIONAL_SOFTWARE) || 
							newCertResult.getAdditionalSoftware() == null || 
							newCertResult.getAdditionalSoftware().size() == 0) {
						oldResult.setAdditionalSoftware(new ArrayList<CertificationResultAdditionalSoftwareDTO>());
					} else {
						for(CertificationResultAdditionalSoftware newAdditionalSoftware : newCertResult.getAdditionalSoftware()) {
							CertificationResultAdditionalSoftwareDTO software = new CertificationResultAdditionalSoftwareDTO();
							software.setId(newAdditionalSoftware.getId());
							software.setCertificationResultId(oldResult.getId());
							software.setJustification(newAdditionalSoftware.getJustification());
							software.setGrouping(newAdditionalSoftware.getGrouping());
							if(newAdditionalSoftware.getCertifiedProductId() == null && 
									!StringUtils.isEmpty(newAdditionalSoftware.getCertifiedProductNumber())) {
								//look up the certified product
								if(newAdditionalSoftware.getCertifiedProductNumber().startsWith("CHP-")) {
									CertifiedProductDTO cpDto = cpDao.getByChplNumber(newAdditionalSoftware.getCertifiedProductNumber());
									if(cpDto != null) {
										software.setCertifiedProductId(cpDto.getId());
									}
								} else {
									CertifiedProductDetailsDTO cpDto = cpDao.getByChplUniqueId(newAdditionalSoftware.getCertifiedProductNumber());
									if(cpDto != null) {
										software.setCertifiedProductId(cpDto.getId());
									}
								}
							} else if(newAdditionalSoftware.getCertifiedProductId() != null) {
								software.setCertifiedProductId(newAdditionalSoftware.getCertifiedProductId());
							} else {
								software.setName(newAdditionalSoftware.getName());
								software.setVersion(newAdditionalSoftware.getVersion());
							}
							oldResult.getAdditionalSoftware().add(software);
						}
					}
					
					if(!certRules.hasCertOption(criterionDTO.getNumber(), CertificationResultRules.STANDARDS_TESTED) ||
							newCertResult.getTestStandards() == null || newCertResult.getTestStandards().size() == 0) {
						oldResult.setTestStandards(new ArrayList<CertificationResultTestStandardDTO>());
					} else {
						for(CertificationResultTestStandard newTestStandard : newCertResult.getTestStandards()) {
							CertificationResultTestStandardDTO testStandard = new CertificationResultTestStandardDTO();
							testStandard.setId(newTestStandard.getId());
							testStandard.setTestStandardId(newTestStandard.getTestStandardId());
							testStandard.setTestStandardDescription(newTestStandard.getTestStandardDescription());
							testStandard.setTestStandardName(newTestStandard.getTestStandardName());
							testStandard.setCertificationResultId(oldResult.getId());
							testStandard.setDeleted(false);
							oldResult.getTestStandards().add(testStandard);
						}
					}
					
					if(!certRules.hasCertOption(criterionDTO.getNumber(), CertificationResultRules.TEST_TOOLS_USED) ||
							newCertResult.getTestToolsUsed() == null || newCertResult.getTestToolsUsed().size() == 0) {
						oldResult.setTestTools(new ArrayList<CertificationResultTestToolDTO>());
					} else {
						for(CertificationResultTestTool newTestTool : newCertResult.getTestToolsUsed()) {
							CertificationResultTestToolDTO testTool = new CertificationResultTestToolDTO();
							testTool.setId(newTestTool.getId());
							testTool.setTestToolId(newTestTool.getTestToolId());
							testTool.setTestToolName(newTestTool.getTestToolName());
							testTool.setTestToolVersion(newTestTool.getTestToolVersion());
							testTool.setCertificationResultId(oldResult.getId());
							oldResult.getTestTools().add(testTool);
						}
					}
					
					if(!certRules.hasCertOption(criterionDTO.getNumber(), CertificationResultRules.G1_SUCCESS) ||
							newCertResult.getG1MacraMeasures() == null || newCertResult.getG1MacraMeasures().size() == 0) {
						oldResult.setG1Measures(new ArrayList<CertificationResultMacraMeasureDTO>());
					} else {
						for(MacraMeasure newMeasure : newCertResult.getG1MacraMeasures()) {
							CertificationResultMacraMeasureDTO crMeasure = new CertificationResultMacraMeasureDTO();
							crMeasure.setCertificationResultId(oldResult.getId());
							MacraMeasureDTO mmDto = new MacraMeasureDTO();
							mmDto.setId(newMeasure.getId());
							crMeasure.setMeasure(mmDto);
							oldResult.getG1Measures().add(crMeasure);
						}
					}
					
					if(!certRules.hasCertOption(criterionDTO.getNumber(), CertificationResultRules.G2_SUCCESS) ||
							newCertResult.getG2MacraMeasures() == null || newCertResult.getG2MacraMeasures().size() == 0) {
						oldResult.setG2Measures(new ArrayList<CertificationResultMacraMeasureDTO>());
					} else {
						for(MacraMeasure newMeasure : newCertResult.getG2MacraMeasures()) {
							CertificationResultMacraMeasureDTO crMeasure = new CertificationResultMacraMeasureDTO();
							crMeasure.setCertificationResultId(oldResult.getId());
							MacraMeasureDTO mmDto = new MacraMeasureDTO();
							mmDto.setId(newMeasure.getId());
							crMeasure.setMeasure(mmDto);
							oldResult.getG2Measures().add(crMeasure);
						}
					}
					
					if(!certRules.hasCertOption(criterionDTO.getNumber(), CertificationResultRules.TEST_DATA) ||
							newCertResult.getTestDataUsed() == null || newCertResult.getTestDataUsed().size() == 0) {
						oldResult.setTestData(new ArrayList<CertificationResultTestDataDTO>());
					} else {
						for(CertificationResultTestData newTestData : newCertResult.getTestDataUsed()) {
							CertificationResultTestDataDTO testData = new CertificationResultTestDataDTO();
							testData.setId(newTestData.getId());
							testData.setVersion(newTestData.getVersion());
							testData.setAlteration(newTestData.getAlteration());
							testData.setCertificationResultId(oldResult.getId());
							oldResult.getTestData().add(testData);
						}
					}
					
					if(!certRules.hasCertOption(criterionDTO.getNumber(), CertificationResultRules.TEST_PROCEDURE_VERSION) ||
							newCertResult.getTestProcedures() == null || newCertResult.getTestProcedures().size() == 0) {
						oldResult.setTestProcedures(new ArrayList<CertificationResultTestProcedureDTO>());
					} else {
						for(CertificationResultTestProcedure newTestProcedure : newCertResult.getTestProcedures()) {
							CertificationResultTestProcedureDTO testProcedure = new CertificationResultTestProcedureDTO();
							testProcedure.setId(newTestProcedure.getId());
							testProcedure.setTestProcedureId(newTestProcedure.getTestProcedureId());
							testProcedure.setTestProcedureVersion(newTestProcedure.getTestProcedureVersion());
							testProcedure.setCertificationResultId(oldResult.getId());
							oldResult.getTestProcedures().add(testProcedure);
						}
					}
					
					if(!certRules.hasCertOption(criterionDTO.getNumber(), CertificationResultRules.FUNCTIONALITY_TESTED) ||
							newCertResult.getTestFunctionality() == null || newCertResult.getTestFunctionality().size() == 0) {
						oldResult.setTestFunctionality(new ArrayList<CertificationResultTestFunctionalityDTO>());
					} else {
						for(CertificationResultTestFunctionality newTestFunctionality : newCertResult.getTestFunctionality()) {
							CertificationResultTestFunctionalityDTO testFunctionality = new CertificationResultTestFunctionalityDTO();
							testFunctionality.setId(newTestFunctionality.getId());
							testFunctionality.setTestFunctionalityId(newTestFunctionality.getTestFunctionalityId());
							testFunctionality.setTestFunctionalityName(newTestFunctionality.getDescription());
							testFunctionality.setTestFunctionalityNumber(newTestFunctionality.getName());
							testFunctionality.setCertificationResultId(oldResult.getId());
							oldResult.getTestFunctionality().add(testFunctionality);
						}
					}
					
					if(!certRules.hasCertOption(criterionDTO.getNumber(), CertificationResultRules.TEST_TASK) ||
							newCertResult.getTestTasks() == null || newCertResult.getTestTasks().size() == 0) {
						oldResult.setTestTasks(new ArrayList<CertificationResultTestTaskDTO>());
					} else {
						for(CertificationResultTestTask newTestTask : newCertResult.getTestTasks()) {
							CertificationResultTestTaskDTO testTask = new CertificationResultTestTaskDTO();
							testTask.setId(newTestTask.getId());
							testTask.setTestTaskId(newTestTask.getTestTaskId());
							testTask.setCertificationResultId(oldResult.getId());
							TestTaskDTO tt = new TestTaskDTO();
							tt.setId(newTestTask.getTestTaskId());
							tt.setDescription(newTestTask.getDescription());
							tt.setTaskErrors(newTestTask.getTaskErrors());
							tt.setTaskErrorsStddev(newTestTask.getTaskErrorsStddev());
							tt.setTaskPathDeviationObserved(newTestTask.getTaskPathDeviationObserved());
							tt.setTaskPathDeviationOptimal(newTestTask.getTaskPathDeviationOptimal());
							tt.setTaskRating(newTestTask.getTaskRating());
							tt.setTaskRatingScale(newTestTask.getTaskRatingScale());
							tt.setTaskRatingStddev(newTestTask.getTaskRatingStddev());
							tt.setTaskSuccessAverage(newTestTask.getTaskSuccessAverage());
							tt.setTaskSuccessStddev(newTestTask.getTaskSuccessStddev());
							tt.setTaskTimeAvg(newTestTask.getTaskTimeAvg());
							tt.setTaskTimeDeviationObservedAvg(newTestTask.getTaskTimeDeviationObservedAvg());
							tt.setTaskTimeDeviationOptimalAvg(newTestTask.getTaskTimeDeviationOptimalAvg());
							tt.setTaskTimeStddev(newTestTask.getTaskTimeStddev());
							testTask.setTestTask(tt);
							
							if(!certRules.hasCertOption(criterionDTO.getNumber(), CertificationResultRules.TEST_PARTICIPANT) ||
									newTestTask.getTestParticipants() == null || newTestTask.getTestParticipants().size() == 0) {
								testTask.setTaskParticipants(new HashSet<CertificationResultTestTaskParticipantDTO>());
							} else {
								for(CertificationResultTestParticipant newTestParticipant : newTestTask.getTestParticipants()) {
									CertificationResultTestTaskParticipantDTO testParticipant = new CertificationResultTestTaskParticipantDTO();
									testParticipant.setId(newTestParticipant.getId());
									testParticipant.setTestParticipantId(newTestParticipant.getTestParticipantId());
									testParticipant.setCertTestTaskId(newTestTask.getId());
									TestParticipantDTO tp = new TestParticipantDTO();
									tp.setId(newTestParticipant.getTestParticipantId());
									tp.setGender(newTestParticipant.getGender());
									tp.setAssistiveTechnologyNeeds(newTestParticipant.getAssistiveTechnologyNeeds());
									tp.setComputerExperienceMonths(newTestParticipant.getComputerExperienceMonths());
									tp.setAgeRangeId(newTestParticipant.getAgeRangeId());
									AgeRangeDTO age = new AgeRangeDTO();
									age.setId(newTestParticipant.getAgeRangeId());
									age.setAge(newTestParticipant.getAgeRange());
									tp.setAgeRange(age);
									tp.setEducationTypeId(newTestParticipant.getEducationTypeId());
									EducationTypeDTO et = new EducationTypeDTO();
									et.setId(newTestParticipant.getEducationTypeId());
									et.setName(newTestParticipant.getEducationTypeName());
									tp.setEducationType(et);
									tp.setOccupation(newTestParticipant.getOccupation());
									tp.setProductExperienceMonths(newTestParticipant.getProductExperienceMonths());
									tp.setProfessionalExperienceMonths(newTestParticipant.getProfessionalExperienceMonths());
									testParticipant.setTestParticipant(tp);
									testTask.getTaskParticipants().add(testParticipant);
								}
							}
							oldResult.getTestTasks().add(testTask);
						}
					}
					
					certResultManager.update(acbId, oldResult);
					break;
				}
			}
		}
	}
	
	@Override
	@PreAuthorize("hasRole('ROLE_ADMIN') or "
			+ "( (hasRole('ROLE_ACB_STAFF') or hasRole('ROLE_ACB_ADMIN'))"
			+ "  and hasPermission(#acbId, 'gov.healthit.chpl.dto.CertificationBodyDTO', admin)"
			+ ")")
	@Transactional(readOnly = false)
	public void updateCqms(Long acbId, CertifiedProductDTO productDto, List<CQMResultDetailsDTO> cqmResults)
		throws EntityCreationException, EntityRetrievalException, JsonProcessingException {
		List<CQMResultDTO> beforeCQMs = cqmResultDAO.findByCertifiedProductId(productDto.getId());
		
		// Handle NQFs and Additions:
		for (CQMResultDetailsDTO currCqm : cqmResults){
			
			Boolean isNQF = (currCqm.getCmsId() == null);
			if (isNQF){
				for (CQMResultDTO beforeCQM : beforeCQMs){
					
					Long beforeCQMCriterionID = beforeCQM.getCqmCriterionId();
					CQMCriterionDTO beforeCriterionDTO = cqmCriterionDao.getById(beforeCQMCriterionID);
					
					if ((beforeCriterionDTO.getCmsId() == null) && (beforeCriterionDTO.getNqfNumber().equals(currCqm.getNqfNumber()) ) ){
						beforeCQM.setSuccess(currCqm.getSuccess());
						cqmResultDAO.update(beforeCQM);
						break;
					}
				}
			} else {
				CQMResultDTO foundCqm = null;
				for (int i = 0; i < beforeCQMs.size() && foundCqm == null; i++) {
					CQMResultDTO beforeCqm = beforeCQMs.get(i);
					Long beforeCQMCriterionID = beforeCqm.getCqmCriterionId();
					CQMCriterionDTO beforeCriterionDTO = cqmCriterionDao.getById(beforeCQMCriterionID);
					
					if (beforeCriterionDTO.getCmsId().equals(currCqm.getCmsId()) && 
							beforeCriterionDTO.getCqmVersion().equals(currCqm.getVersion())) {
						foundCqm = beforeCqm;
					}
				}
				
				if (foundCqm == null){
					CQMCriterionDTO criterion = null;
					if(StringUtils.isEmpty(currCqm.getCmsId())) {
						criterion = cqmCriterionDao.getNQFByNumber(currCqm.getNumber());
					} else if(currCqm.getCmsId().startsWith("CMS")) {
						criterion = cqmCriterionDao.getCMSByNumberAndVersion(currCqm.getCmsId(), currCqm.getVersion());
					}
					if(criterion == null) {
						throw new EntityRetrievalException("Could not find CQM with number " + currCqm.getCmsId() + " and version " + currCqm.getVersion());
					}
					
					CQMResultDTO newCQMResult = new CQMResultDTO();
					newCQMResult.setCertifiedProductId(productDto.getId());
					newCQMResult.setCqmCriterionId(criterion.getId());
					newCQMResult.setCreationDate(new Date());
					newCQMResult.setDeleted(false);
					newCQMResult.setSuccess(true);
					CQMResultDTO created = cqmResultDAO.create(newCQMResult);
					if(currCqm.getCriteria() != null && currCqm.getCriteria().size() > 0) {
						for(CQMResultCriteriaDTO criteria : currCqm.getCriteria()) {
							criteria.setCqmResultId(created.getId());
							Long mappedCriterionId = findCqmCriterionId(criteria);
							criteria.setCriterionId(mappedCriterionId);
							cqmResultDAO.createCriteriaMapping(criteria);
						}
					}
				} else {
					//update an existing cqm 
					//need to compare current and found cqm in case there are differences
					List<CQMResultCriteriaDTO> existingCriteria = cqmResultDAO.getCriteriaForCqmResult(foundCqm.getId());
					List<CQMResultCriteriaDTO> toAdd = new ArrayList<CQMResultCriteriaDTO>();
					List<CQMResultCriteriaDTO> toRemove = new ArrayList<CQMResultCriteriaDTO>();
					
					for(CQMResultCriteriaDTO existing : existingCriteria) {
						boolean exists = false;
						for(CQMResultCriteriaDTO passedIn : currCqm.getCriteria()) {
							if(existing.getCriterion().getNumber().equals(passedIn.getCriterion().getNumber())) {
								exists = true;
							}
						}
						if(!exists) {
							toRemove.add(existing);
						}
					}
					
					for(CQMResultCriteriaDTO passedIn : currCqm.getCriteria()) {
						boolean exists = false;
						for(CQMResultCriteriaDTO existing : existingCriteria) {
							if(existing.getCriterion().getNumber().equals(passedIn.getCriterion().getNumber())) {
								exists = true;
							}
						}
						if(!exists) {
							toAdd.add(passedIn);
						}
					}
					
					for(CQMResultCriteriaDTO currToAdd : toAdd) {
						currToAdd.setCqmResultId(foundCqm.getId());
						Long mappedCriterionId = findCqmCriterionId(currToAdd);
						currToAdd.setCriterionId(mappedCriterionId);
						cqmResultDAO.createCriteriaMapping(currToAdd);
					}
					for(CQMResultCriteriaDTO currToRemove : toRemove) {
						cqmResultDAO.deleteCriteriaMapping(currToRemove.getId());
					}
				}
			}
		}
		
		// Handle CQM deletions:
		for (CQMCriterionDTO criterion : cqmCriterionDao.findAll()){
			
			Boolean isDeletion = true;
			Boolean isNQF = (criterion.getCmsId() == null);
			
			if (isNQF){
				isDeletion = false;
			} else {
							
				for (CQMResultDetailsDTO cqm : cqmResults){
					
					Boolean cqmIsNQF = (cqm.getCmsId() == null);
					if (!cqmIsNQF){
						if (cqm.getCmsId().equals(criterion.getCmsId())){
							isDeletion = false;
							break;
						}
					}
				}
			}
			if (isDeletion){
				deleteCqmResult(productDto.getId(), criterion.getId());
			}
		}
	}
	
	private void deleteCqmResult(Long certifiedProductId, Long cqmId){
		
		List<CQMResultDTO> cqmResults = cqmResultDAO.findByCertifiedProductId(certifiedProductId);
		
		for (CQMResultDTO cqmResult : cqmResults){
			if (cqmResult.getCqmCriterionId().equals(cqmId)){
				cqmResultDAO.delete(cqmResult.getId());
			}
		}
	}
	
	private Long findCqmCriterionId(CQMResultCriteriaDTO cqm) throws EntityRetrievalException {
		if(cqm.getCriterionId() != null) {
			return cqm.getCriterionId();
		}
		if(cqm.getCriterion() != null && 
				!StringUtils.isEmpty(cqm.getCriterion().getNumber())) {
			CertificationCriterionDTO cert = certCriterionDao.getByName(cqm.getCriterion().getNumber());
			if(cert != null) {
				return cert.getId();
			} else {
				throw new EntityRetrievalException("Could not find certification criteria with number " + cqm.getCriterion().getNumber());
			}
		} else if(cqm.getCriterion() != null && cqm.getCriterion().getId() != null) {
			return cqm.getCriterion().getId();
		} else {
			throw new EntityRetrievalException("A criteria id or number must be provided.");
		}
	}
	
	@Override
	public void checkSuspiciousActivity(CertifiedProductSearchDetails original, CertifiedProductSearchDetails changed) {
		boolean sendMsg = false;
		String activityThresholdDaysStr = env.getProperty("questionableActivityThresholdDays");
		String subject = "CHPL Questionable Activity";
		String htmlMessage = "<p>Activity was detected on certified product " + original.getChplProductNumber(); 
		if(activityThresholdDaysStr.equals("0")) {
			htmlMessage += ".";
		} else {
			htmlMessage += " more than " + activityThresholdDaysStr + " days after it was certified.";
		}
		htmlMessage += "</p>"
				+ "<p>To view the details of this activity go to: " + env.getProperty("chplUrlBegin") + "/#/admin/reports/" + original.getId() + " </p>";
		
		
		if(original.getCertificationEdition().get("name").equals("2011")) {
			sendMsg = true;
		} else {
			int activityThresholdDays = new Integer(activityThresholdDaysStr).intValue();
			long activityThresholdMillis = activityThresholdDays * 24 * 60 * 60 * 1000;
			if(original.getCertificationDate() != null && changed.getCertificationDate() != null &&
			   (changed.getLastModifiedDate().longValue() - original.getCertificationDate().longValue() > activityThresholdMillis)) {
				//if they changed something outside of the suspicious activity window, 
				//check if the change was something that should trigger an email
				
				if(!original.getCertificationStatus().get("id").equals(changed.getCertificationStatus().get("id"))) {
					sendMsg = true;
				}
				
				if( (original.getCqmResults() == null && changed.getCqmResults() != null) || 
					(original.getCqmResults() != null && changed.getCqmResults() == null) ||
					(original.getCqmResults().size() != changed.getCqmResults().size())) {
					sendMsg = true;
				} else if(original.getCqmResults().size() == changed.getCqmResults().size()) {
					for(CQMResultDetails origCqm : original.getCqmResults()) {
						for(CQMResultDetails changedCqm : changed.getCqmResults()) {
							if(origCqm.getCmsId().equals(changedCqm.getCmsId())) {
								if(origCqm.isSuccess().booleanValue() != changedCqm.isSuccess().booleanValue()) {
									sendMsg = true;
								}
							}
						}
					}
				}
				if( (original.getCertificationResults() == null && changed.getCertificationResults() != null) ||
					(original.getCertificationResults() != null && changed.getCertificationResults() == null) ||
					(original.getCertificationResults().size() != changed.getCertificationResults().size())) {
					sendMsg = true;
				} else if(original.getCertificationResults().size() == changed.getCertificationResults().size()) {
					for(CertificationResult origCert : original.getCertificationResults()) {
						for(CertificationResult changedCert : changed.getCertificationResults()) {
							if(origCert.getNumber().equals(changedCert.getNumber())) {
								if(origCert.isSuccess().booleanValue() != changedCert.isSuccess().booleanValue()) {
									sendMsg = true;
								}
								if(origCert.isG1Success() != null || changedCert.isG1Success() != null) {
									if(	(origCert.isG1Success() == null && changedCert.isG1Success() != null) ||
										(origCert.isG1Success() != null && changedCert.isG1Success() == null) ||
										(origCert.isG1Success().booleanValue() != changedCert.isG1Success().booleanValue())) {
										sendMsg = true;
									}
								}
								if(origCert.isG2Success() != null || changedCert.isG2Success() != null) {
									if(	(origCert.isG2Success() == null && changedCert.isG2Success() != null) ||
										(origCert.isG2Success() != null && changedCert.isG2Success() == null) ||
										(origCert.isG2Success().booleanValue() != changedCert.isG2Success().booleanValue())) {
										sendMsg = true;
									}
								}
								if(origCert.isGap() != null || changedCert.isGap() != null) {
									if(	(origCert.isGap() == null && changedCert.isGap() != null) ||
										(origCert.isGap() != null && changedCert.isGap() == null) ||
										(origCert.isGap().booleanValue() != changedCert.isGap().booleanValue())) {
										sendMsg = true;
									}
								}
							}
						}
					}
				}
			}
		}
		
		if(sendMsg) {
			String emailAddr = env.getProperty("questionableActivityEmail");
			String[] emailAddrs = emailAddr.split(";");
			try {
				sendMailService.sendEmail(emailAddrs, subject, htmlMessage);
			} catch(MessagingException me) {
				logger.error("Could not send questionable activity email", me);
			}
		}
	}
}
