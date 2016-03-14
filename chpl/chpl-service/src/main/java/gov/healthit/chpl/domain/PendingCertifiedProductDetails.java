package gov.healthit.chpl.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.util.StringUtils;

import gov.healthit.chpl.dto.PendingCertificationResultAdditionalSoftwareDTO;
import gov.healthit.chpl.dto.PendingCertificationResultDTO;
import gov.healthit.chpl.dto.PendingCertificationResultTestDataDTO;
import gov.healthit.chpl.dto.PendingCertificationResultTestFunctionalityDTO;
import gov.healthit.chpl.dto.PendingCertificationResultTestProcedureDTO;
import gov.healthit.chpl.dto.PendingCertificationResultTestStandardDTO;
import gov.healthit.chpl.dto.PendingCertificationResultTestToolDTO;
import gov.healthit.chpl.dto.PendingCertificationResultUcdProcessDTO;
import gov.healthit.chpl.dto.PendingCertifiedProductDTO;
import gov.healthit.chpl.dto.PendingCertifiedProductQmsStandardDTO;
import gov.healthit.chpl.dto.PendingCertifiedProductTargetedUserDTO;
import gov.healthit.chpl.dto.PendingCqmCriterionDTO;

public class PendingCertifiedProductDetails extends CertifiedProductSearchDetails {
	private String recordStatus;
	private Map<String, Object> developerAddress;
	
	public PendingCertifiedProductDetails() {}
	
	public PendingCertifiedProductDetails(PendingCertifiedProductDTO dto) {
		this.setId(dto.getId());
		this.setErrorMessages(dto.getErrorMessages());
		this.setWarningMessages(dto.getWarningMessages());
		this.setRecordStatus(dto.getRecordStatus());
		this.setChplProductNumber(dto.getUniqueId());
		this.setReportFileLocation(dto.getReportFileLocation());
		this.setSedReportFileLocation(dto.getSedReportFileLocation());
		this.setAcbCertificationId(dto.getAcbCertificationId());
		this.setIcs(dto.getIcs());
		
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
		
		Map<String, Object> developerMap = new HashMap<String, Object>();
		if(dto.getDeveloperId() == null) {
			developerMap.put("id", null);
		} else {
			developerMap.put("id", dto.getDeveloperId());
		}
		developerMap.put("name", dto.getDeveloperName());
		developerMap.put("email", dto.getDeveloperEmail());
		developerMap.put("website", dto.getDeveloperWebsite());
		Contact developerContact = new Contact();
		developerContact.setLastName(dto.getDeveloperContactName());
		developerContact.setEmail(dto.getDeveloperEmail());
		developerContact.setPhoneNumber(dto.getDeveloperPhoneNumber());
		developerMap.put("contact", developerContact);
		this.setDeveloper(developerMap);
		
		developerAddress = new HashMap<String, Object>();
		if(dto.getDeveloperAddress() == null || dto.getDeveloperAddress().getId() == null) {
			developerAddress.put("id", null);
		} else {
			developerAddress.put("id", dto.getDeveloperAddress().getId());
		}
		developerAddress.put("line1", dto.getDeveloperStreetAddress());
		developerAddress.put("city", dto.getDeveloperCity());
		developerAddress.put("state", dto.getDeveloperState());
		developerAddress.put("zipcode", dto.getDeveloperZipCode());
		
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
			
			if(certCriterion.getUcdProcesses() != null && certCriterion.getUcdProcesses().size() > 0) {
				for(PendingCertificationResultUcdProcessDTO ucdDto : certCriterion.getUcdProcesses()) {
					CertificationResultUcdProcess ucd = new CertificationResultUcdProcess();
					ucd.setUcdProcessId(ucdDto.getUcdProcessId());
					ucd.setUcdProcessName(ucdDto.getUcdProcessName());
					ucd.setUcdProcessDetails(ucdDto.getUcdProcessDetails());
					cert.getUcdProcesses().add(ucd);
				}
			}
			
			if(certCriterion.getAdditionalSoftware() != null) {
				for(PendingCertificationResultAdditionalSoftwareDTO as : certCriterion.getAdditionalSoftware()) {
					CertificationResultAdditionalSoftware software = new CertificationResultAdditionalSoftware();
					software.setCertifiedProductId(as.getCertifiedProductId());
					software.setName(as.getName());
					software.setVersion(as.getVersion());
					software.setJustification(as.getJustification());
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
					testFunc.setNumber(tf.getNumber());
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
					testStd.setTestStandardNumber(ts.getNumber());
					cert.getTestStandards().add(testStd);
				}
			} else {
				cert.setTestStandards(null);
			}
			certList.add(cert);
		}
		this.setCertificationResults(certList);
		
		//set cqm results
		List<CQMResultDetails> cqmResults = new ArrayList<CQMResultDetails>();
		for(PendingCqmCriterionDTO cqmCriterion : dto.getCqmCriterion()) {
			boolean existingCms = false;
			if(!StringUtils.isEmpty(cqmCriterion.getCmsId())) {
				for(CQMResultDetails result : cqmResults) {
					if(dto.getCertificationEdition().equals("2014") && result.getCmsId().equals(cqmCriterion.getCmsId())) {
						existingCms = true;
						result.getSuccessVersions().add(cqmCriterion.getVersion());
					}
				}
			}
			
			if(!existingCms) {
				CQMResultDetails cqm = new CQMResultDetails();
				cqm.setCmsId(cqmCriterion.getCmsId());
				cqm.setNqfNumber(cqmCriterion.getNqfNumber());
				cqm.setNumber(cqmCriterion.getCqmNumber());
				cqm.setTitle(cqmCriterion.getTitle());
				cqm.setTypeId(cqmCriterion.getTypeId());
				cqm.setDomain(cqmCriterion.getDomain());
				if(dto.getCertificationEdition().equals("2014") && !StringUtils.isEmpty(cqmCriterion.getCmsId())) {
					cqm.getSuccessVersions().add(cqmCriterion.getVersion());
				} else if(!StringUtils.isEmpty(cqmCriterion.getNqfNumber())) {
					cqm.setSuccess(cqmCriterion.isMeetsCriteria());
				}
				cqmResults.add(cqm);
			}
		}
		this.setCqmResults(cqmResults);
	}

	public Map<String, Object> getDeveloperAddress() {
		return developerAddress;
	}

	public void setDeveloperAddress(Map<String, Object> developerAddress) {
		this.developerAddress = developerAddress;
	}

	public String getRecordStatus() {
		return recordStatus;
	}

	public void setRecordStatus(String recordStatus) {
		this.recordStatus = recordStatus;
	}
}
