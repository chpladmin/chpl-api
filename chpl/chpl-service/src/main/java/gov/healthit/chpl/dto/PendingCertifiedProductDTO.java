package gov.healthit.chpl.dto;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import gov.healthit.chpl.domain.AdditionalSoftware;
import gov.healthit.chpl.domain.CQMResultDetails;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.PendingCertifiedProductDetails;
import gov.healthit.chpl.entity.PendingCertificationCriterionEntity;
import gov.healthit.chpl.entity.PendingCertifiedProductEntity;
import gov.healthit.chpl.entity.PendingCqmCriterionEntity;

public class PendingCertifiedProductDTO {
	
	private Long id;
	private Long practiceTypeId;
    private Long vendorId;
	private AddressDTO vendorAddress;    
    private Long productId;
    private Long productVersionId;    
    private Long certificationEditionId;    
    private Long certificationBodyId;    
    private Long productClassificationId;
    private Long additionalSoftwareId;
    private List<String> errorMessages;
    private List<String> warningMessages;
    
    /**
    * fields directly from the spreadsheet
    **/
    private String uniqueId;    
    private String recordStatus;    
    private String practiceType;    
    private String vendorName;    
    private String productName;    
    private String productVersion;    
    private String certificationEdition;    
    private String acbCertificationId;    
    private String certificationBodyName;
    private String productClassificationName;
    private String productClassificationModule;
    private Date certificationDate;
    private String vendorStreetAddress;
    private String vendorCity;
    private String vendorState;
    private String vendorZipCode;
    private String vendorWebsite;
    private String vendorEmail;
    private String additionalSoftware;
    private String uploadNotes;
    private String reportFileLocation;
	private List<PendingCertificationCriterionDTO> certificationCriterion;
	private List<PendingCqmCriterionDTO> cqmCriterion;
	private Date uploadDate;
	
	public PendingCertifiedProductDTO(){
		this.errorMessages = new ArrayList<String>();	
		this.warningMessages = new ArrayList<String>();
		this.certificationCriterion = new ArrayList<PendingCertificationCriterionDTO>();
		this.cqmCriterion = new ArrayList<PendingCqmCriterionDTO>();
	}
	
	public PendingCertifiedProductDTO(PendingCertifiedProductDetails details) {
		this.id = details.getId();
		if(details.getPracticeType().get("id") != null) {
			String practiceTypeId = details.getPracticeType().get("id").toString();
			this.practiceTypeId =  new Long(practiceTypeId);
		}
		if(details.getPracticeType().get("name") != null) {
			this.practiceType = details.getPracticeType().get("name").toString();
		}
		
		if(details.getVendor().get("id") != null) {
			String vendorId = details.getVendor().get("id").toString();
			this.vendorId = new Long(vendorId);
		}
		if(details.getVendor().get("name") != null) {
			this.vendorName = details.getVendor().get("name").toString();
		}
		if(details.getVendor().get("website") != null) {
			this.vendorWebsite = details.getVendor().get("website").toString();
		}
		if(details.getVendor().get("email") != null) {
			this.vendorEmail = details.getVendor().get("email").toString();
		}
		
		AddressDTO address = new AddressDTO();
		if(details.getVendorAddress().get("id") != null) {
			String addressId = details.getVendor().get("id").toString();
			address.setId(new Long(addressId));
		}
		if(details.getVendorAddress().get("line1") != null) {
			address.setStreetLineOne(details.getVendorAddress().get("line1").toString());
		}
		if(details.getVendorAddress().get("city") != null) {
			address.setCity(details.getVendorAddress().get("city").toString());
		}
		if(details.getVendorAddress().get("state") != null) {
			address.setState(details.getVendorAddress().get("state").toString());
		}
		if(details.getVendorAddress().get("zipcode") != null) {
			address.setZipcode(details.getVendorAddress().get("zipcode").toString());
		}
		this.vendorAddress = address;
		
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
		
		if(details.getAdditionalSoftware() != null && details.getAdditionalSoftware().size() > 0) {
			AdditionalSoftware software = details.getAdditionalSoftware().get(0);
			this.additionalSoftwareId = software.getAdditionalSoftwareid();
		}
		
		if(details.getCertificationDate() != null) {
			this.certificationDate = new Date(details.getCertificationDate());
		}
		
		this.uniqueId = details.getOncId();
		this.recordStatus = details.getRecordStatus();
		this.acbCertificationId = details.getAcbCertificationId(); 
		this.uploadNotes = details.getUploadNotes();
		this.reportFileLocation = details.getReportFileLocation();

		
		//this.productClassificationModule = entity.getProductClassificationModule();
		//this.uploadDate = entity.getCreationDate();
		
		this.certificationCriterion = new ArrayList<PendingCertificationCriterionDTO>();
		this.cqmCriterion = new ArrayList<PendingCqmCriterionDTO>();
		
		List<CertificationResult> certificationResults = details.getCertificationResults();
		for(CertificationResult crResult : certificationResults) {
			PendingCertificationCriterionDTO certDto = new PendingCertificationCriterionDTO();
			certDto.setNumber(crResult.getNumber());
			certDto.setTitle(crResult.getTitle());
			certDto.setMeetsCriteria(crResult.isSuccess());
			this.certificationCriterion.add(certDto);
		}
		List<CQMResultDetails> cqmResults = details.getCqmResults();
		for(CQMResultDetails cqmResult : cqmResults) {
			PendingCqmCriterionDTO cqmDto = new PendingCqmCriterionDTO();
			cqmDto.setCmsId(cqmResult.getCmsId());
			cqmDto.setNqfNumber(cqmResult.getNqfNumber());
			cqmDto.setCqmNumber(cqmResult.getNumber());
			cqmDto.setMeetsCriteria(cqmResult.isSuccess());
			cqmDto.setTitle(cqmResult.getTitle());
			cqmDto.setVersion(cqmResult.getVersion());
			cqmDto.setTypeId(cqmResult.getTypeId());
			cqmDto.setDomain(cqmResult.getDomain());
			this.cqmCriterion.add(cqmDto);
		}	
		this.errorMessages = new ArrayList<String>();	
		this.warningMessages = new ArrayList<String>();
	}
	
	public PendingCertifiedProductDTO(PendingCertifiedProductEntity entity){
		this.id = entity.getId();
		this.practiceTypeId = entity.getPracticeTypeId();
		this.vendorId = entity.getVendorId();
		this.vendorAddress = new AddressDTO(entity.getVendorAddress());
		this.productId = entity.getProductId();
		this.productVersionId = entity.getProductVersionId();
		this.certificationEditionId = entity.getCertificationEditionId();
		this.certificationBodyId = entity.getCertificationBodyId();
		this.productClassificationId = entity.getProductClassificationId();
		this.additionalSoftwareId = entity.getAdditionalSoftwareId();
		
		this.uniqueId = entity.getUniqueId();
		this.recordStatus = entity.getRecordStatus();
		this.practiceType = entity.getPracticeType();
		this.vendorName = entity.getVendorName();
		this.productName = entity.getProductName();
		this.productVersion = entity.getProductVersion();
		this.certificationEdition = entity.getCertificationEdition();
		this.acbCertificationId = entity.getAcbCertificationId();
		this.certificationBodyName = entity.getCertificationBodyName();
		this.productClassificationName = entity.getProductClassificationName();
		this.productClassificationModule = entity.getProductClassificationModule();
		this.certificationDate = entity.getCertificationDate();
		this.vendorStreetAddress = entity.getVendorStreetAddress();
		this.vendorCity = entity.getVendorCity();
		this.vendorState = entity.getVendorState();
		this.vendorZipCode = entity.getVendorZipCode();
		this.vendorWebsite = entity.getVendorWebsite();
		this.vendorEmail = entity.getVendorEmail();
		this.additionalSoftware = entity.getAdditionalSoftware();
		this.uploadNotes = entity.getUploadNotes();
		this.reportFileLocation = entity.getReportFileLocation();
		this.uploadDate = entity.getCreationDate();
		
		this.certificationCriterion = new ArrayList<PendingCertificationCriterionDTO>();
		this.cqmCriterion = new ArrayList<PendingCqmCriterionDTO>();
		
		Set<PendingCertificationCriterionEntity> criterionEntities = entity.getCertificationCriterion();
		if(criterionEntities != null && criterionEntities.size() > 0) {
			for(PendingCertificationCriterionEntity crEntity : criterionEntities) {
				this.certificationCriterion.add(new PendingCertificationCriterionDTO(crEntity));
			}
		}
		Set<PendingCqmCriterionEntity> cqmEntities = entity.getCqmCriterion();
		if(cqmEntities != null && cqmEntities.size() > 0) {
			for(PendingCqmCriterionEntity cqmEntity : cqmEntities) {
				this.cqmCriterion.add(new PendingCqmCriterionDTO(cqmEntity));
			}	
		}
		this.errorMessages = new ArrayList<String>();	
		this.warningMessages = new ArrayList<String>();	
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

	public Long getVendorId() {
		return vendorId;
	}

	public void setVendorId(Long vendorId) {
		this.vendorId = vendorId;
	}

	public AddressDTO getVendorAddress() {
		return vendorAddress;
	}

	public void setVendorAddress(AddressDTO vendorAddress) {
		this.vendorAddress = vendorAddress;
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

	public String getVendorName() {
		return vendorName;
	}

	public void setVendorName(String vendorName) {
		this.vendorName = vendorName;
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

	public String getProductClassificationModule() {
		return productClassificationModule;
	}

	public void setProductClassificationModule(String productClassificationModule) {
		this.productClassificationModule = productClassificationModule;
	}

	public Date getCertificationDate() {
		return certificationDate;
	}

	public void setCertificationDate(Date certificationDate) {
		this.certificationDate = certificationDate;
	}

	public String getVendorStreetAddress() {
		return vendorStreetAddress;
	}

	public void setVendorStreetAddress(String vendorStreetAddress) {
		this.vendorStreetAddress = vendorStreetAddress;
	}

	public String getVendorCity() {
		return vendorCity;
	}

	public void setVendorCity(String vendorCity) {
		this.vendorCity = vendorCity;
	}

	public String getVendorState() {
		return vendorState;
	}

	public void setVendorState(String vendorState) {
		this.vendorState = vendorState;
	}

	public String getVendorZipCode() {
		return vendorZipCode;
	}

	public void setVendorZipCode(String vendorZipCode) {
		this.vendorZipCode = vendorZipCode;
	}

	public String getVendorWebsite() {
		return vendorWebsite;
	}

	public void setVendorWebsite(String vendorWebsite) {
		this.vendorWebsite = vendorWebsite;
	}

	public String getVendorEmail() {
		return vendorEmail;
	}

	public void setVendorEmail(String vendorEmail) {
		this.vendorEmail = vendorEmail;
	}

	public String getAdditionalSoftware() {
		return additionalSoftware;
	}

	public void setAdditionalSoftware(String additionalSoftware) {
		this.additionalSoftware = additionalSoftware;
	}

	public String getUploadNotes() {
		return uploadNotes;
	}

	public void setUploadNotes(String uploadNotes) {
		this.uploadNotes = uploadNotes;
	}

	public String getReportFileLocation() {
		return reportFileLocation;
	}

	public void setReportFileLocation(String reportFileLocation) {
		this.reportFileLocation = reportFileLocation;
	}

	public List<PendingCertificationCriterionDTO> getCertificationCriterion() {
		return certificationCriterion;
	}

	public void setCertificationCriterion(List<PendingCertificationCriterionDTO> certificationCriterion) {
		this.certificationCriterion = certificationCriterion;
	}

	public List<PendingCqmCriterionDTO> getCqmCriterion() {
		return cqmCriterion;
	}

	public void setCqmCriterion(List<PendingCqmCriterionDTO> cqmCriterion) {
		this.cqmCriterion = cqmCriterion;
	}

	public Long getAdditionalSoftwareId() {
		return additionalSoftwareId;
	}

	public void setAdditionalSoftwareId(Long additionalSoftwareId) {
		this.additionalSoftwareId = additionalSoftwareId;
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
}
