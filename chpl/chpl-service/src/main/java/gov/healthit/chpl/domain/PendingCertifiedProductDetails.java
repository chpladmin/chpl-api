package gov.healthit.chpl.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.util.StringUtils;

import gov.healthit.chpl.dto.CQMResultCriteriaDTO;
import gov.healthit.chpl.dto.PendingCertificationResultAdditionalSoftwareDTO;
import gov.healthit.chpl.dto.PendingCertificationResultDTO;
import gov.healthit.chpl.dto.PendingCertificationResultTestDataDTO;
import gov.healthit.chpl.dto.PendingCertificationResultTestFunctionalityDTO;
import gov.healthit.chpl.dto.PendingTestParticipantDTO;
import gov.healthit.chpl.dto.PendingCertificationResultTestProcedureDTO;
import gov.healthit.chpl.dto.PendingCertificationResultTestStandardDTO;
import gov.healthit.chpl.dto.PendingCertificationResultTestTaskDTO;
import gov.healthit.chpl.dto.PendingCertificationResultTestTaskParticipantDTO;
import gov.healthit.chpl.dto.PendingTestTaskDTO;
import gov.healthit.chpl.dto.PendingCertificationResultTestToolDTO;
import gov.healthit.chpl.dto.PendingCertificationResultUcdProcessDTO;
import gov.healthit.chpl.dto.PendingCertifiedProductAccessibilityStandardDTO;
import gov.healthit.chpl.dto.PendingCertifiedProductDTO;
import gov.healthit.chpl.dto.PendingCertifiedProductQmsStandardDTO;
import gov.healthit.chpl.dto.PendingCertifiedProductTargetedUserDTO;
import gov.healthit.chpl.dto.PendingCqmCertificationCriterionDTO;
import gov.healthit.chpl.dto.PendingCqmCriterionDTO;

public class PendingCertifiedProductDetails extends CertifiedProductSearchDetails {
	private String recordStatus;
	
	public PendingCertifiedProductDetails() {}
	
	public PendingCertifiedProductDetails(PendingCertifiedProductDTO dto) {
		this.setId(dto.getId());
		this.setErrorMessages(dto.getErrorMessages());
		this.setWarningMessages(dto.getWarningMessages());
		this.setRecordStatus(dto.getRecordStatus());
		this.setChplProductNumber(dto.getUniqueId());
		this.setReportFileLocation(dto.getReportFileLocation());
		this.setSedReportFileLocation(dto.getSedReportFileLocation());
		this.setSedIntendedUserDescription(dto.getSedIntendedUserDescription());
		this.setSedTestingEnd(dto.getSedTestingEnd());
		this.setAcbCertificationId(dto.getAcbCertificationId());
		this.setIcs(dto.getIcs());
		this.setAccessibilityCertified(dto.getAccessibilityCertified());
		
		Map<String, Object> classificationTypeMap = new HashMap<String, Object>();
		if(dto.getProductClassificationId() == null) {
			classificationTypeMap.put("id", null);
		} else {
			classificationTypeMap.put("id", dto.getProductClassificationId());
		}
		classificationTypeMap.put("name", dto.getProductClassificationName());
		this.setClassificationType(classificationTypeMap);
		
		this.setOtherAcb(null);
		this.setCertificationStatus(null);
		
		Developer developer = new Developer();
		developer.setDeveloperId(dto.getDeveloperId());
		developer.setName(dto.getDeveloperName());
		developer.setWebsite(dto.getDeveloperWebsite());

		Contact developerContact = new Contact();
		developerContact.setLastName(dto.getDeveloperContactName());
		developerContact.setEmail(dto.getDeveloperEmail());
		developerContact.setPhoneNumber(dto.getDeveloperPhoneNumber());
		developer.setContact(developerContact);

		if(dto.getDeveloperAddress() != null) {
			Address address = new Address();
			address.setAddressId(dto.getDeveloperAddress().getId());
			address.setLine1(dto.getDeveloperStreetAddress());
			address.setCity(dto.getDeveloperCity());
			address.setState(dto.getDeveloperState());
			address.setZipcode(dto.getDeveloperZipCode());
			address.setCountry(dto.getDeveloperAddress().getCountry());
			developer.setAddress(address);
		} else {
			Address address = new Address();
			address.setLine1(dto.getDeveloperStreetAddress());
			address.setCity(dto.getDeveloperCity());
			address.setState(dto.getDeveloperState());
			address.setZipcode(dto.getDeveloperZipCode());
			developer.setAddress(address);
		}
		
		this.setDeveloper(developer);
		
		Map<String, Object> productMap = new HashMap<String, Object>();
		if(dto.getProductId() == null) {
			productMap.put("id", null);
		} else {
			productMap.put("id", dto.getProductId());
		}
		productMap.put("name", dto.getProductName());
		if(dto.getProductVersionId() == null) {
			productMap.put("versionId", null);
		} else {
			productMap.put("versionId", dto.getProductVersionId()+"");
		}
		productMap.put("version", dto.getProductVersion());
		this.setProduct(productMap);
		
		Map<String, Object> certificationEditionMap = new HashMap<String, Object>();
		if(dto.getCertificationEditionId() == null) {
			certificationEditionMap.put("id", null);
		} else {
			certificationEditionMap.put("id", dto.getCertificationEditionId());
		}
		certificationEditionMap.put("name", dto.getCertificationEdition());
		this.setCertificationEdition(certificationEditionMap);
		
		Map<String, Object> practiceTypeMap = new HashMap<String, Object>();
		if(dto.getPracticeTypeId() == null) {
			practiceTypeMap.put("id", null);
		} else {
			practiceTypeMap.put("id", dto.getPracticeTypeId());
		}
		practiceTypeMap.put("name", dto.getPracticeType());
		this.setPracticeType(practiceTypeMap);
		
		Map<String, Object> certifyingBodyMap = new HashMap<String, Object>();
		if(dto.getCertificationBodyId() == null) {
			certifyingBodyMap.put("id", null);
		} else {
			certifyingBodyMap.put("id", dto.getCertificationBodyId());
		}
		certifyingBodyMap.put("name", dto.getCertificationBodyName());
		this.setCertifyingBody(certifyingBodyMap);
		
		Map<String, Object> testingLabMap = new HashMap<String, Object>();
		if(dto.getTestingLabId() == null) {
			testingLabMap.put("id", null);
		} else {
			testingLabMap.put("id", dto.getTestingLabId());
		}
		testingLabMap.put("name", dto.getTestingLabName());
		this.setTestingLab(testingLabMap);
		
		if(dto.getCertificationDate() != null) {
			this.setCertificationDate(dto.getCertificationDate().getTime());
		}
		
		if(dto.getCertificationCriterion() == null) {
			this.setCountCerts(0);
		} else {
			int certCount = 0;
			for(PendingCertificationResultDTO cert : dto.getCertificationCriterion()) {
				if(cert.getMeetsCriteria()) {
					certCount++;
				}
			}
			this.setCountCerts(certCount);
		}
		
		if(dto.getCqmCriterion() == null) {
			this.setCountCqms(0);
		} else {
			int cqmCount = 0;
			Set<String> cqmsMet = new HashSet<String>();
			for(PendingCqmCriterionDTO cqm : dto.getCqmCriterion()) {
				if(!cqmsMet.contains(cqm.getCmsId()) && cqm.isMeetsCriteria()) {
					cqmsMet.add(cqm.getCmsId());
					cqmCount++;
				}
			}
			this.setCountCqms(cqmCount);
		}
		
		this.setVisibleOnChpl(false);
		this.setTransparencyAttestation(dto.getTransparencyAttestation());
		this.setTransparencyAttestationUrl(dto.getTransparencyAttestationUrl());
		
		List<PendingCertifiedProductQmsStandardDTO> qmsDtos = dto.getQmsStandards();
		if(qmsDtos != null && qmsDtos.size() > 0) {
			for(PendingCertifiedProductQmsStandardDTO qmsDto : qmsDtos) {
				CertifiedProductQmsStandard qms = new CertifiedProductQmsStandard();
				qms.setApplicableCriteria(qmsDto.getApplicableCriteria());
				qms.setQmsModification(qmsDto.getModification());
				qms.setQmsStandardName(qmsDto.getName());
				qms.setQmsStandardId(qmsDto.getQmsStandardId());
				this.getQmsStandards().add(qms);
			}
		}
		
		List<PendingCertifiedProductTargetedUserDTO> tuDtos = dto.getTargetedUsers();
		if(tuDtos != null && tuDtos.size() > 0) {
			for(PendingCertifiedProductTargetedUserDTO tuDto : tuDtos) {
				CertifiedProductTargetedUser tu = new CertifiedProductTargetedUser();
				tu.setTargetedUserId(tuDto.getTargetedUserId());
				tu.setTargetedUserName(tuDto.getName());
				this.getTargetedUsers().add(tu);
			}
		}
		
		List<PendingCertifiedProductAccessibilityStandardDTO> asDtos = dto.getAccessibilityStandards();
		if(asDtos != null && asDtos.size() > 0) {
			for(PendingCertifiedProductAccessibilityStandardDTO asDto : asDtos) {
				CertifiedProductAccessibilityStandard as = new CertifiedProductAccessibilityStandard();
				as.setAccessibilityStandardId(asDto.getAccessibilityStandardId());
				as.setAccessibilityStandardName(asDto.getName());
				this.getAccessibilityStandards().add(as);
			}
		}
		
		List<CertificationResult> certList = new ArrayList<CertificationResult>();
		for(PendingCertificationResultDTO certCriterion : dto.getCertificationCriterion()) {
			CertificationResult cert = new CertificationResult();
			cert.setNumber(certCriterion.getNumber());
			cert.setTitle(certCriterion.getTitle());
			cert.setSuccess(certCriterion.getMeetsCriteria());
			cert.setGap(certCriterion.getGap());
			cert.setSed(certCriterion.getSed());
			cert.setG1Success(certCriterion.getG1Success());
			cert.setG2Success(certCriterion.getG2Success());
			cert.setApiDocumentation(certCriterion.getApiDocumentation());
			cert.setPrivacySecurityFramework(certCriterion.getPrivacySecurityFramework());
			
			if(certCriterion.getUcdProcesses() != null && certCriterion.getUcdProcesses().size() > 0) {
				for(PendingCertificationResultUcdProcessDTO ucdDto : certCriterion.getUcdProcesses()) {
					CertificationResultUcdProcess ucd = new CertificationResultUcdProcess();
					ucd.setUcdProcessId(ucdDto.getUcdProcessId());
					ucd.setUcdProcessName(ucdDto.getUcdProcessName());
					ucd.setUcdProcessDetails(ucdDto.getUcdProcessDetails());
					cert.getUcdProcesses().add(ucd);
				}
				cert.setSed(Boolean.TRUE);
			}
			
			if(certCriterion.getAdditionalSoftware() != null) {
				for(PendingCertificationResultAdditionalSoftwareDTO as : certCriterion.getAdditionalSoftware()) {
					CertificationResultAdditionalSoftware software = new CertificationResultAdditionalSoftware();
					software.setCertifiedProductId(as.getCertifiedProductId());
					software.setCertifiedProductNumber(as.getChplId());
					software.setName(as.getName());
					software.setVersion(as.getVersion());
					software.setJustification(as.getJustification());
					software.setGrouping(as.getGrouping());
					cert.getAdditionalSoftware().add(software);
				}
			} else {
				cert.setAdditionalSoftware(null);
			}
			
			if(certCriterion.getTestData() != null) {
				for(PendingCertificationResultTestDataDTO td: certCriterion.getTestData()) {
					CertificationResultTestData testData = new CertificationResultTestData();
					testData.setVersion(td.getVersion());
					testData.setAlteration(td.getAlteration());
					cert.getTestDataUsed().add(testData);
				}
			} else {
				cert.setTestDataUsed(null);
			}
			
			if(certCriterion.getTestTools() != null) {
				for(PendingCertificationResultTestToolDTO tt : certCriterion.getTestTools()) {
					CertificationResultTestTool testTool = new CertificationResultTestTool();
					testTool.setTestToolId(tt.getTestToolId());
					testTool.setTestToolName(tt.getName());
					testTool.setTestToolVersion(tt.getVersion());
					cert.getTestToolsUsed().add(testTool);
				}
			} else {
				cert.setTestToolsUsed(null);
			}
			
			if(certCriterion.getTestFunctionality() != null) {
				for(PendingCertificationResultTestFunctionalityDTO tf : certCriterion.getTestFunctionality()) {
					CertificationResultTestFunctionality testFunc = new CertificationResultTestFunctionality();
					testFunc.setTestFunctionalityId(tf.getTestFunctionalityId());
					testFunc.setName(tf.getNumber());
					cert.getTestFunctionality().add(testFunc);
				}
			} else {
				cert.setTestFunctionality(null);
			}
			
			if(certCriterion.getTestProcedures() != null) {
				for(PendingCertificationResultTestProcedureDTO tp : certCriterion.getTestProcedures()) {
					CertificationResultTestProcedure testProc = new CertificationResultTestProcedure();
					testProc.setTestProcedureId(tp.getTestProcedureId());
					testProc.setTestProcedureVersion(tp.getVersion());
					cert.getTestProcedures().add(testProc);
				}
			} else {
				cert.setTestProcedures(null);
			}
			
			if(certCriterion.getTestStandards() != null) {
				for(PendingCertificationResultTestStandardDTO ts : certCriterion.getTestStandards()) {
					CertificationResultTestStandard testStd = new CertificationResultTestStandard();
					testStd.setTestStandardId(ts.getTestStandardId());
					testStd.setTestStandardName(ts.getName());
					cert.getTestStandards().add(testStd);
				}
			} else {
				cert.setTestStandards(null);
			}
			
			if(certCriterion.getTestTasks() != null && certCriterion.getTestTasks().size() > 0) {
				cert.setSed(Boolean.TRUE);
				for(PendingCertificationResultTestTaskDTO ttDto : certCriterion.getTestTasks()) {
					if(ttDto.getPendingTestTask() != null) {
						PendingTestTaskDTO tt = ttDto.getPendingTestTask();
						CertificationResultTestTask task = new CertificationResultTestTask();
						task.setUniqueId(tt.getUniqueId());
						task.setDescription(tt.getDescription());
						task.setTaskErrors(tt.getTaskErrors());
						task.setTaskErrorsStddev(tt.getTaskErrorsStddev());
						task.setTaskPathDeviationObserved(tt.getTaskPathDeviationObserved());
						task.setTaskPathDeviationOptimal(tt.getTaskPathDeviationOptimal());
						task.setTaskRating(tt.getTaskRating());
						task.setTaskRatingScale(tt.getTaskRatingScale());
						task.setTaskSuccessAverage(tt.getTaskSuccessAverage());
						task.setTaskSuccessStddev(tt.getTaskSuccessStddev());
						task.setTaskTimeAvg(tt.getTaskTimeAvg());
						task.setTaskTimeDeviationObservedAvg(tt.getTaskTimeDeviationObservedAvg());
						task.setTaskTimeDeviationOptimalAvg(tt.getTaskTimeDeviationOptimalAvg());
						task.setTaskTimeStddev(tt.getTaskTimeStddev());
						
						if(ttDto.getTaskParticipants() != null) {
							for(PendingCertificationResultTestTaskParticipantDTO ptDto : ttDto.getTaskParticipants()) {
								if(ptDto.getTestParticipant() != null) {
									PendingTestParticipantDTO pt = ptDto.getTestParticipant();
									CertificationResultTestParticipant part = new CertificationResultTestParticipant();
									part.setUniqueId(pt.getUniqueId());
									part.setAge(pt.getAge());
									part.setAssistiveTechnologyNeeds(pt.getAssistiveTechnologyNeeds());
									part.setComputerExperienceMonths(pt.getComputerExperienceMonths());
									part.setEducationTypeId(pt.getEducationTypeId());
									if(pt.getEducationType() != null) {
										part.setEducationTypeName(pt.getEducationType().getName());
									}
									part.setGender(pt.getGender());
									part.setOccupation(pt.getOccupation());
									part.setProductExperienceMonths(pt.getProductExperienceMonths());
									part.setProfessionalExperienceMonths(pt.getProfessionalExperienceMonths());
									task.getTestParticipants().add(part);
								}
							}
						}
						cert.getTestTasks().add(task);
					}
				}
			} else {
				cert.setTestTasks(null);
			}
			
			certList.add(cert);
		}
		this.setCertificationResults(certList);
		
		//set cqm results
		List<CQMResultDetails> cqmResults = new ArrayList<CQMResultDetails>();
		for(PendingCqmCriterionDTO pendingCqm : dto.getCqmCriterion()) {
			boolean existingCms = false;
			if(!StringUtils.isEmpty(pendingCqm.getCmsId())) {
				for(CQMResultDetails result : cqmResults) {
					if(!dto.getCertificationEdition().equals("2011") && result.getCmsId().equals(pendingCqm.getCmsId())) {
						existingCms = true;
						result.getSuccessVersions().add(pendingCqm.getVersion());
					}
				}
			}
			
			if(!existingCms) {
				CQMResultDetails cqm = new CQMResultDetails();
				cqm.setCmsId(pendingCqm.getCmsId());
				cqm.setNqfNumber(pendingCqm.getNqfNumber());
				cqm.setNumber(pendingCqm.getCqmNumber());
				cqm.setTitle(pendingCqm.getTitle());
				cqm.setTypeId(pendingCqm.getTypeId());
				cqm.setDomain(pendingCqm.getDomain());
				if(!dto.getCertificationEdition().equals("2011") && !StringUtils.isEmpty(pendingCqm.getCmsId())) {
					cqm.getSuccessVersions().add(pendingCqm.getVersion());
				} else if(!StringUtils.isEmpty(pendingCqm.getNqfNumber())) {
					cqm.setSuccess(pendingCqm.isMeetsCriteria());
				}
				//now add criteria mappings to all of our cqms
				List<PendingCqmCertificationCriterionDTO> criteria = pendingCqm.getCertifications();
				if(criteria != null && criteria.size() > 0) {
					for(PendingCqmCertificationCriterionDTO criteriaDTO : criteria) {
						CQMResultCertification c = new CQMResultCertification();
						c.setCertificationId(criteriaDTO.getCertificationId());
						c.setCertificationNumber(criteriaDTO.getCertificationCriteriaNumber());
						c.setId(criteriaDTO.getId());
						cqm.getCriteria().add(c);
					}
				}
				
				cqmResults.add(cqm);
			}
		}
		this.setCqmResults(cqmResults);
	}

	public String getRecordStatus() {
		return recordStatus;
	}

	public void setRecordStatus(String recordStatus) {
		this.recordStatus = recordStatus;
	}
}
