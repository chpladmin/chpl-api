package gov.healthit.chpl.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.util.StringUtils;

import gov.healthit.chpl.dto.PendingCertificationCriterionDTO;
import gov.healthit.chpl.dto.PendingCertifiedProductDTO;
import gov.healthit.chpl.dto.PendingCqmCriterionDTO;

public class PendingCertifiedProductDetails extends CertifiedProductSearchDetails {
	
	private String oncId;
	private List<String> errorMessages;
	private List<String> warningMessages;
	private String uploadNotes;
	private String recordStatus;
	private Map<String, Object> developerAddress;
	
	public PendingCertifiedProductDetails() {}
	
	public PendingCertifiedProductDetails(PendingCertifiedProductDTO dto) {
		this.setId(dto.getId());
		this.setOncId(dto.getUniqueId());
		this.setErrorMessages(dto.getErrorMessages());
		this.setWarningMessages(dto.getWarningMessages());
		this.setRecordStatus(dto.getRecordStatus());
		this.setChplProductNumber(null);
		this.setReportFileLocation(dto.getReportFileLocation());
		this.setQualityManagementSystemAtt(null);
		this.setAcbCertificationId(dto.getAcbCertificationId());
		this.setIcs(dto.getIcs());
		this.setSedTesting(dto.getSedTesting());
		this.setQmsTesting(dto.getQmsTesting());
		
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
		
		if(dto.getCertificationDate() != null) {
			this.setCertificationDate(dto.getCertificationDate().getTime());
		}
		
		if(dto.getCertificationCriterion() == null) {
			this.setCountCerts(0);
		} else {
			int certCount = 0;
			for(PendingCertificationCriterionDTO cert : dto.getCertificationCriterion()) {
				if(cert.isMeetsCriteria()) {
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
		//TODO: this needs to be included in the upload
		this.setPrivacyAttestation(false);
		this.setUploadNotes(dto.getUploadNotes());
		
		List<AdditionalSoftware> softwareList = new ArrayList<AdditionalSoftware>();
		if(!StringUtils.isEmpty(dto.getAdditionalSoftware())) {
			AdditionalSoftware software = new AdditionalSoftware();
			if(dto.getAdditionalSoftwareId() != null) {
				software.setAdditionalSoftwareId(dto.getAdditionalSoftwareId());
			}
			software.setName(dto.getAdditionalSoftware());
			softwareList.add(software);
		}
		this.setAdditionalSoftware(softwareList);
		
		List<CertificationResult> certList = new ArrayList<CertificationResult>();
		for(PendingCertificationCriterionDTO certCriterion : dto.getCertificationCriterion()) {
			CertificationResult cert = new CertificationResult();
			cert.setNumber(certCriterion.getNumber());
			cert.setTitle(certCriterion.getTitle());
			cert.setSuccess(certCriterion.isMeetsCriteria());
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

	public String getUploadNotes() {
		return uploadNotes;
	}

	public void setUploadNotes(String uploadNotes) {
		this.uploadNotes = uploadNotes;
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

	public String getOncId() {
		return oncId;
	}

	public void setOncId(String oncId) {
		this.oncId = oncId;
	}

	public List<String> getErrorMessages() {
		return errorMessages;
	}

	public void setErrorMessages(List<String> errorMessages) {
		this.errorMessages = errorMessages;
	}

	public List<String> getWarningMessages() {
		return warningMessages;
	}

	public void setWarningMessages(List<String> warningMessages) {
		this.warningMessages = warningMessages;
	}
}
