package gov.healthit.chpl.dto;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import gov.healthit.chpl.entity.CertificationCriterionEntity;
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
	
	public PendingCertifiedProductDTO(){
		this.certificationCriterion = new ArrayList<PendingCertificationCriterionDTO>();
		this.cqmCriterion = new ArrayList<PendingCqmCriterionDTO>();
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
		
		this.certificationCriterion = new ArrayList<PendingCertificationCriterionDTO>();
		this.cqmCriterion = new ArrayList<PendingCqmCriterionDTO>();
		
		Set<PendingCertificationCriterionEntity> criterionEntities = entity.getCertificationCriterion();
		for(PendingCertificationCriterionEntity crEntity : criterionEntities) {
			this.certificationCriterion.add(new PendingCertificationCriterionDTO(crEntity));
		}
		Set<PendingCqmCriterionEntity> cqmEntities = entity.getCqmCriterion();
		for(PendingCqmCriterionEntity cqmEntity : cqmEntities) {
			this.cqmCriterion.add(new PendingCqmCriterionDTO(cqmEntity));
		}	}

	
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
}
