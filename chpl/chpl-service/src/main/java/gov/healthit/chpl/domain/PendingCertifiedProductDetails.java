package gov.healthit.chpl.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.util.StringUtils;

import gov.healthit.chpl.dto.PendingCertificationCriterionDTO;
import gov.healthit.chpl.dto.PendingCertifiedProductDTO;
import gov.healthit.chpl.dto.PendingCqmCriterionDTO;

public class PendingCertifiedProductDetails extends CertifiedProductSearchDetails {
	
	private String uploadNotes;
	private String recordStatus;
	private Map<String, String> vendorAddress;
	
	public PendingCertifiedProductDetails() {}
	
	public PendingCertifiedProductDetails(PendingCertifiedProductDTO dto) {
		this.setId(dto.getId());
		this.setRecordStatus(dto.getRecordStatus());
		this.setTestingLabId(null);
		this.setChplProductNumber(null);
		this.setReportFileLocation(dto.getReportFileLocation());
		this.setQualityManagementSystemAtt(null);
		this.setAcbCertificationId(dto.getAcbCertificationId());
		
		Map<String, String> classificationTypeMap = new HashMap<String, String>();
		if(dto.getProductClassificationId() == null) {
			classificationTypeMap.put("id", null);
		} else {
			classificationTypeMap.put("id", dto.getProductClassificationId()+"");
		}
		classificationTypeMap.put("name", dto.getProductClassificationName());
		this.setClassificationType(classificationTypeMap);
		
		this.setOtherAcb(null);
		this.setCertificationStatusId(null);
		
		Map<String, String> vendorMap = new HashMap<String, String>();
		if(dto.getVendorId() == null) {
			vendorMap.put("id", null);
		} else {
			vendorMap.put("id", dto.getVendorId()+"");
		}
		vendorMap.put("name", dto.getVendorName());
		vendorMap.put("email", dto.getVendorEmail());
		vendorMap.put("website", dto.getVendorWebsite());
		this.setVendor(vendorMap);
		
		vendorAddress = new HashMap<String, String>();
		if(dto.getVendorAddress() == null || dto.getVendorAddress().getId() == null) {
			vendorAddress.put("id", null);
		} else {
			vendorAddress.put("id", dto.getVendorAddress().getId()+"");
		}
		vendorAddress.put("streetLine1", dto.getVendorStreetAddress());
		vendorAddress.put("city", dto.getVendorCity());
		vendorAddress.put("state", dto.getVendorState());
		vendorAddress.put("zipcode", dto.getVendorZipCode());
		
		Map<String, String> productMap = new HashMap<String, String>();
		if(dto.getProductId() == null) {
			productMap.put("id", null);
		} else {
			productMap.put("id", dto.getProductId() + "");
		}
		productMap.put("name", dto.getProductName());
		if(dto.getProductVersionId() == null) {
			productMap.put("versionId", null);
		} else {
			productMap.put("versionId", dto.getProductVersionId()+"");
		}
		productMap.put("version", dto.getProductVersion());
		this.setProduct(productMap);
		
		Map<String, String> certificationEditionMap = new HashMap<String, String>();
		if(dto.getCertificationEditionId() == null) {
			certificationEditionMap.put("id", null);
		} else {
			certificationEditionMap.put("id", dto.getCertificationEditionId()+"");
		}
		certificationEditionMap.put("name", dto.getCertificationEdition());
		this.setCertificationEdition(certificationEditionMap);
		
		Map<String, String> practiceTypeMap = new HashMap<String, String>();
		if(dto.getPracticeTypeId() == null) {
			practiceTypeMap.put("id", null);
		} else {
			practiceTypeMap.put("id", dto.getPracticeTypeId()+"");
		}
		practiceTypeMap.put("name", dto.getPracticeType());
		this.setPracticeType(practiceTypeMap);
		
		Map<String, String> certifyingBodyMap = new HashMap<String, String>();
		if(dto.getCertificationBodyId() == null) {
			certifyingBodyMap.put("id", null);
		} else {
			certifyingBodyMap.put("id", dto.getCertificationBodyId()+"");
		}
		certifyingBodyMap.put("name", dto.getCertificationBodyName());
		this.setCertifyingBody(certifyingBodyMap);
		
		this.setCertificationDate(dto.getCertificationDate().getTime()+"");
		
		if(dto.getCertificationCriterion() == null) {
			this.setCountCerts(0);
		} else {
			this.setCountCerts(dto.getCertificationCriterion().size());
		}
		
		if(dto.getCqmCriterion() == null) {
			this.setCountCqms(0);
		} else {
			this.setCountCqms(dto.getCqmCriterion().size());
		}
		
		this.setVisibleOnChpl(false);
		this.setUploadNotes(dto.getUploadNotes());
		
		List<AdditionalSoftware> softwareList = new ArrayList<AdditionalSoftware>();
		if(!StringUtils.isEmpty(dto.getAdditionalSoftware())) {
			AdditionalSoftware software = new AdditionalSoftware();
			if(dto.getAdditionalSoftwareId() != null) {
				software.setAdditionalSoftwareid(dto.getAdditionalSoftwareId());
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
			CQMResultDetails cqm = new CQMResultDetails();
			cqm.setCmsId(cqmCriterion.getCmsId());
			cqm.setNqfNumber(cqmCriterion.getNqfNumber());
			cqm.setNumber(cqmCriterion.getCqmNumber());
			cqm.setSuccess(cqmCriterion.isMeetsCriteria());
			cqm.setTitle(cqmCriterion.getTitle());
			cqm.setVersion(cqmCriterion.getVersion());
			cqmResults.add(cqm);
		}
		this.setCqmResults(cqmResults);
	}

	public String getUploadNotes() {
		return uploadNotes;
	}

	public void setUploadNotes(String uploadNotes) {
		this.uploadNotes = uploadNotes;
	}

	public Map<String, String> getVendorAddress() {
		return vendorAddress;
	}

	public void setVendorAddress(Map<String, String> vendorAddress) {
		this.vendorAddress = vendorAddress;
	}

	public String getRecordStatus() {
		return recordStatus;
	}

	public void setRecordStatus(String recordStatus) {
		this.recordStatus = recordStatus;
	}
}
