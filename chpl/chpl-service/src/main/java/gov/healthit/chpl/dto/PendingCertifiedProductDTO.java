package gov.healthit.chpl.dto;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import gov.healthit.chpl.domain.CQMResultDetails;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.PendingCertifiedProductDetails;
import gov.healthit.chpl.entity.PendingCertificationResultEntity;
import gov.healthit.chpl.entity.PendingCertifiedProductEntity;
import gov.healthit.chpl.entity.PendingCqmCriterionEntity;

public class PendingCertifiedProductDTO {
	
	private Long id;
	private Long practiceTypeId;
    private Long developerId;
	private AddressDTO developerAddress;    
    private Long productId;
    private Long productVersionId;    
    private Long certificationEditionId;    
    private Long certificationBodyId;    
    private Long productClassificationId;
    private Long testingLabId;
    private List<String> errorMessages;
    private List<String> warningMessages;
    
    /**
    * fields directly from the spreadsheet
    **/
    private String uniqueId;    
    private String recordStatus;    
    private String practiceType; 
    private String testingLabName;
    private String developerName;    
    private String productName;    
    private String productVersion;    
    private String certificationEdition;    
    private String acbCertificationId;    
    private String certificationBodyName;
    private String productClassificationName;
    private Date certificationDate;
    private String developerStreetAddress;
    private String developerCity;
    private String developerState;
    private String developerZipCode;
    private String developerWebsite;
    private String developerEmail;
    private String developerContactName;
    private String developerPhoneNumber;
    private String reportFileLocation;
    private String sedReportFileLocation;
	private Boolean ics;
	private String termsOfUseUrl;
	private Boolean transparencyAttestation;
	
	private List<PendingCertificationResultDTO> certificationCriterion;
	private List<PendingCqmCriterionDTO> cqmCriterion;
	private List<PendingCertifiedProductQmsStandardDTO> qmsStandards;
	
	private Date uploadDate;
	
	public PendingCertifiedProductDTO(){
		this.errorMessages = new ArrayList<String>();	
		this.warningMessages = new ArrayList<String>();
		this.certificationCriterion = new ArrayList<PendingCertificationResultDTO>();
		this.cqmCriterion = new ArrayList<PendingCqmCriterionDTO>();
		this.qmsStandards = new ArrayList<PendingCertifiedProductQmsStandardDTO>();
	}
	
	public PendingCertifiedProductDTO(PendingCertifiedProductDetails details) {
		this();
		this.id = details.getId();
		if(details.getPracticeType().get("id") != null) {
			String practiceTypeId = details.getPracticeType().get("id").toString();
			this.practiceTypeId =  new Long(practiceTypeId);
		}
		if(details.getPracticeType().get("name") != null) {
			this.practiceType = details.getPracticeType().get("name").toString();
		}
		
		if(details.getTestingLab() != null && details.getTestingLab().get("id") != null) {
			this.testingLabId = new Long(details.getTestingLab().get("id").toString());
		}
		if(details.getTestingLab() != null && details.getTestingLab().get("name") != null) {
			this.testingLabName = details.getTestingLab().get("name").toString();
		}
		
		if(details.getDeveloper().get("id") != null) {
			String developerId = details.getDeveloper().get("id").toString();
			this.developerId = new Long(developerId);
		}
		if(details.getDeveloper().get("name") != null) {
			this.developerName = details.getDeveloper().get("name").toString();
		}
		if(details.getDeveloper().get("website") != null) {
			this.developerWebsite = details.getDeveloper().get("website").toString();
		}
		if(details.getDeveloper().get("email") != null) {
			this.developerEmail = details.getDeveloper().get("email").toString();
		}
		if(details.getDeveloper().get("contactName") != null) {
			this.developerContactName = details.getDeveloper().get("contactName").toString();
		}
		if(details.getDeveloper().get("contactPhone") != null) {
			this.developerPhoneNumber = details.getDeveloper().get("contactPhone").toString();
		}
		if(details.getDeveloper().get("transparencyAttestation") != null) {
			this.transparencyAttestation = new Boolean(details.getDeveloper().get("transparencyAttestation").toString());
		}
		
		AddressDTO address = new AddressDTO();
		if(details.getDeveloperAddress().get("id") != null) {
			String addressId = details.getDeveloper().get("id").toString();
			address.setId(new Long(addressId));
		}
		if(details.getDeveloperAddress().get("line1") != null) {
			address.setStreetLineOne(details.getDeveloperAddress().get("line1").toString());
		}
		if(details.getDeveloperAddress().get("city") != null) {
			address.setCity(details.getDeveloperAddress().get("city").toString());
		}
		if(details.getDeveloperAddress().get("state") != null) {
			address.setState(details.getDeveloperAddress().get("state").toString());
		}
		if(details.getDeveloperAddress().get("zipcode") != null) {
			address.setZipcode(details.getDeveloperAddress().get("zipcode").toString());
		}
		this.developerAddress = address;
		
		if(details.getProduct().get("id") != null) {
			String productId = details.getProduct().get("id").toString();
			this.productId = new Long(productId);
		}
		if(details.getProduct().get("name") != null) {
			this.productName = details.getProduct().get("name").toString();
		}
		if(details.getProduct().get("versionId") != null) {
			String productVersionId = details.getProduct().get("versionId").toString();
			this.productVersionId = new Long(productVersionId);
		}
		if(details.getProduct().get("version") != null) {
			this.productVersion = details.getProduct().get("version").toString();
		}
		
		if(details.getCertificationEdition().get("id") != null) {
			String certificationEditionId = details.getCertificationEdition().get("id").toString();
			this.certificationEditionId = new Long(certificationEditionId);
		}
		if(details.getCertificationEdition().get("name") != null) {
			this.certificationEdition = details.getCertificationEdition().get("name").toString();
		}
		
		if(details.getCertifyingBody().get("id") != null) {
			String certificationBodyId = details.getCertifyingBody().get("id").toString();
			this.certificationBodyId = new Long(certificationBodyId);
		}
		if(details.getCertifyingBody().get("name") != null) {
			this.certificationBodyName = details.getCertifyingBody().get("name").toString();
		}
		
		if(details.getClassificationType().get("id") != null) {
			String classificationTypeId = details.getClassificationType().get("id").toString();
			this.productClassificationId = new Long(classificationTypeId);
		}
		if(details.getClassificationType().get("name") != null) {
			this.productClassificationName = details.getClassificationType().get("name").toString();
		}

		if(details.getCertificationDate() != null) {
			this.certificationDate = new Date(details.getCertificationDate());
		}
		
		this.uniqueId = details.getChplProductNumber();
		this.recordStatus = details.getRecordStatus();
		this.acbCertificationId = details.getAcbCertificationId(); 
		this.reportFileLocation = details.getReportFileLocation();
		this.sedReportFileLocation = details.getSedReportFileLocation();
		this.ics = details.getIcs();
		this.termsOfUseUrl = details.getTermsOfUse();
		
		List<CertificationResult> certificationResults = details.getCertificationResults();
		for(CertificationResult crResult : certificationResults) {
			PendingCertificationResultDTO certDto = new PendingCertificationResultDTO();
			certDto.setNumber(crResult.getNumber());
			certDto.setTitle(crResult.getTitle());
			certDto.setMeetsCriteria(crResult.isSuccess());
			this.certificationCriterion.add(certDto);
		}
		List<CQMResultDetails> cqmResults = details.getCqmResults();
		for(CQMResultDetails cqmResult : cqmResults) {
			if(cqmResult.getSuccessVersions() != null && cqmResult.getSuccessVersions().size() > 0) {
				for(String version : cqmResult.getSuccessVersions()) {
					PendingCqmCriterionDTO cqmDto = new PendingCqmCriterionDTO();
					cqmDto.setCmsId(cqmResult.getCmsId());
					cqmDto.setNqfNumber(cqmResult.getNqfNumber());
					cqmDto.setCqmNumber(cqmResult.getNumber());
					cqmDto.setTitle(cqmResult.getTitle());
					cqmDto.setTypeId(cqmResult.getTypeId());
					cqmDto.setDomain(cqmResult.getDomain());
					cqmDto.setMeetsCriteria(Boolean.TRUE);
					cqmDto.setVersion(version);
					this.cqmCriterion.add(cqmDto);
				}
			} else {
				PendingCqmCriterionDTO cqmDto = new PendingCqmCriterionDTO();
				cqmDto.setCmsId(cqmResult.getCmsId());
				cqmDto.setNqfNumber(cqmResult.getNqfNumber());
				cqmDto.setCqmNumber(cqmResult.getNumber());
				cqmDto.setMeetsCriteria(cqmResult.isSuccess());
				cqmDto.setTitle(cqmResult.getTitle());
				cqmDto.setTypeId(cqmResult.getTypeId());
				cqmDto.setDomain(cqmResult.getDomain());
				this.cqmCriterion.add(cqmDto);	
			}
		}	
	}
	
	public PendingCertifiedProductDTO(PendingCertifiedProductEntity entity){		
		this.id = entity.getId();
		this.practiceTypeId = entity.getPracticeTypeId();
		this.testingLabId = entity.getTestingLabId();
		this.developerId = entity.getDeveloperId();
		this.developerAddress = new AddressDTO(entity.getDeveloperAddress());
		this.productId = entity.getProductId();
		this.productVersionId = entity.getProductVersionId();
		this.certificationEditionId = entity.getCertificationEditionId();
		this.certificationBodyId = entity.getCertificationBodyId();
		this.productClassificationId = entity.getProductClassificationId();
		
		this.uniqueId = entity.getUniqueId();
		this.recordStatus = entity.getRecordStatus();
		this.practiceType = entity.getPracticeType();
		this.testingLabName = entity.getTestingLabName();
		this.developerName = entity.getDeveloperName();
		this.productName = entity.getProductName();
		this.productVersion = entity.getProductVersion();
		this.certificationEdition = entity.getCertificationEdition();
		this.acbCertificationId = entity.getAcbCertificationId();
		this.certificationBodyName = entity.getCertificationBodyName();
		this.productClassificationName = entity.getProductClassificationName();
		this.certificationDate = entity.getCertificationDate();
		this.developerStreetAddress = entity.getDeveloperStreetAddress();
		this.developerCity = entity.getDeveloperCity();
		this.developerState = entity.getDeveloperState();
		this.developerZipCode = entity.getDeveloperZipCode();
		this.developerWebsite = entity.getDeveloperWebsite();
		this.developerEmail = entity.getDeveloperEmail();
		this.developerContactName = entity.getDeveloperContactName();
		this.developerPhoneNumber = entity.getDeveloperPhoneNumber();
		this.reportFileLocation = entity.getReportFileLocation();
		this.sedReportFileLocation = entity.getSedReportFileLocation();
		this.ics = entity.getIcs();
		this.termsOfUseUrl = entity.getTermsOfUse();
		this.transparencyAttestation = entity.getTransparencyAttestation();
		
		this.uploadDate = entity.getCreationDate();
		
		Set<PendingCertificationResultEntity> criterionEntities = entity.getCertificationCriterion();
		if(criterionEntities != null && criterionEntities.size() > 0) {
			for(PendingCertificationResultEntity crEntity : criterionEntities) {
				this.certificationCriterion.add(new PendingCertificationResultDTO(crEntity));
			}
		}
		Set<PendingCqmCriterionEntity> cqmEntities = entity.getCqmCriterion();
		if(cqmEntities != null && cqmEntities.size() > 0) {
			for(PendingCqmCriterionEntity cqmEntity : cqmEntities) {
				this.cqmCriterion.add(new PendingCqmCriterionDTO(cqmEntity));
			}	
		}
	}

	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}

	public Long getPracticeTypeId() {
		return practiceTypeId;
	}

	public void setPracticeTypeId(Long practiceTypeId) {
		this.practiceTypeId = practiceTypeId;
	}

	public Long getDeveloperId() {
		return developerId;
	}

	public void setDeveloperId(Long developerId) {
		this.developerId = developerId;
	}

	public AddressDTO getDeveloperAddress() {
		return developerAddress;
	}

	public void setDeveloperAddress(AddressDTO developerAddress) {
		this.developerAddress = developerAddress;
	}

	public Long getProductId() {
		return productId;
	}

	public void setProductId(Long productId) {
		this.productId = productId;
	}

	public Long getProductVersionId() {
		return productVersionId;
	}

	public void setProductVersionId(Long productVersionId) {
		this.productVersionId = productVersionId;
	}

	public Long getCertificationEditionId() {
		return certificationEditionId;
	}

	public void setCertificationEditionId(Long certificationEditionId) {
		this.certificationEditionId = certificationEditionId;
	}

	public Long getCertificationBodyId() {
		return certificationBodyId;
	}

	public void setCertificationBodyId(Long certificationBodyId) {
		this.certificationBodyId = certificationBodyId;
	}

	public Long getProductClassificationId() {
		return productClassificationId;
	}

	public void setProductClassificationId(Long productClassificationId) {
		this.productClassificationId = productClassificationId;
	}

	public String getUniqueId() {
		return uniqueId;
	}

	public void setUniqueId(String uniqueId) {
		this.uniqueId = uniqueId;
	}

	public String getRecordStatus() {
		return recordStatus;
	}

	public void setRecordStatus(String recordStatus) {
		this.recordStatus = recordStatus;
	}

	public String getPracticeType() {
		return practiceType;
	}

	public void setPracticeType(String practiceType) {
		this.practiceType = practiceType;
	}

	public String getDeveloperName() {
		return developerName;
	}

	public void setDeveloperName(String developerName) {
		this.developerName = developerName;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public String getProductVersion() {
		return productVersion;
	}

	public void setProductVersion(String productVersion) {
		this.productVersion = productVersion;
	}

	public String getCertificationEdition() {
		return certificationEdition;
	}

	public void setCertificationEdition(String certificationEdition) {
		this.certificationEdition = certificationEdition;
	}

	public String getAcbCertificationId() {
		return acbCertificationId;
	}

	public void setAcbCertificationId(String acbCertificationId) {
		this.acbCertificationId = acbCertificationId;
	}

	public String getCertificationBodyName() {
		return certificationBodyName;
	}

	public void setCertificationBodyName(String certificationBodyName) {
		this.certificationBodyName = certificationBodyName;
	}

	public String getProductClassificationName() {
		return productClassificationName;
	}

	public void setProductClassificationName(String productClassificationName) {
		this.productClassificationName = productClassificationName;
	}

	public Date getCertificationDate() {
		return certificationDate;
	}

	public void setCertificationDate(Date certificationDate) {
		this.certificationDate = certificationDate;
	}

	public String getDeveloperStreetAddress() {
		return developerStreetAddress;
	}

	public void setDeveloperStreetAddress(String developerStreetAddress) {
		this.developerStreetAddress = developerStreetAddress;
	}

	public String getDeveloperCity() {
		return developerCity;
	}

	public void setDeveloperCity(String developerCity) {
		this.developerCity = developerCity;
	}

	public String getDeveloperState() {
		return developerState;
	}

	public void setDeveloperState(String developerState) {
		this.developerState = developerState;
	}

	public String getDeveloperZipCode() {
		return developerZipCode;
	}

	public void setDeveloperZipCode(String developerZipCode) {
		this.developerZipCode = developerZipCode;
	}

	public String getDeveloperWebsite() {
		return developerWebsite;
	}

	public void setDeveloperWebsite(String developerWebsite) {
		this.developerWebsite = developerWebsite;
	}

	public String getDeveloperEmail() {
		return developerEmail;
	}

	public void setDeveloperEmail(String developerEmail) {
		this.developerEmail = developerEmail;
	}

	public String getReportFileLocation() {
		return reportFileLocation;
	}

	public void setReportFileLocation(String reportFileLocation) {
		this.reportFileLocation = reportFileLocation;
	}

	public List<PendingCertificationResultDTO> getCertificationCriterion() {
		return certificationCriterion;
	}

	public void setCertificationCriterion(List<PendingCertificationResultDTO> certificationCriterion) {
		this.certificationCriterion = certificationCriterion;
	}

	public List<PendingCqmCriterionDTO> getCqmCriterion() {
		return cqmCriterion;
	}

	public void setCqmCriterion(List<PendingCqmCriterionDTO> cqmCriterion) {
		this.cqmCriterion = cqmCriterion;
	}

	public Date getUploadDate() {
		return uploadDate;
	}

	public void setUploadDate(Date uploadDate) {
		this.uploadDate = uploadDate;
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

	public Boolean getIcs() {
		return ics;
	}

	public void setIcs(Boolean ics) {
		this.ics = ics;
	}

	public Long getTestingLabId() {
		return testingLabId;
	}

	public void setTestingLabId(Long testingLabId) {
		this.testingLabId = testingLabId;
	}

	public String getTestingLabName() {
		return testingLabName;
	}

	public void setTestingLabName(String testingLabName) {
		this.testingLabName = testingLabName;
	}

	public String getDeveloperContactName() {
		return developerContactName;
	}

	public void setDeveloperContactName(String developerContactName) {
		this.developerContactName = developerContactName;
	}

	public String getDeveloperPhoneNumber() {
		return developerPhoneNumber;
	}

	public void setDeveloperPhoneNumber(String developerPhoneNumber) {
		this.developerPhoneNumber = developerPhoneNumber;
	}

	public String getSedReportFileLocation() {
		return sedReportFileLocation;
	}

	public void setSedReportFileLocation(String sedReportFileLocation) {
		this.sedReportFileLocation = sedReportFileLocation;
	}

	public List<PendingCertifiedProductQmsStandardDTO> getQmsStandards() {
		return qmsStandards;
	}

	public void setQmsStandards(List<PendingCertifiedProductQmsStandardDTO> qmsStandards) {
		this.qmsStandards = qmsStandards;
	}

	public String getTermsOfUseUrl() {
		return termsOfUseUrl;
	}

	public void setTermsOfUseUrl(String termsOfUseUrl) {
		this.termsOfUseUrl = termsOfUseUrl;
	}

	public Boolean getTransparencyAttestation() {
		return transparencyAttestation;
	}

	public void setTransparencyAttestation(Boolean transparencyAttestation) {
		this.transparencyAttestation = transparencyAttestation;
	}
}
