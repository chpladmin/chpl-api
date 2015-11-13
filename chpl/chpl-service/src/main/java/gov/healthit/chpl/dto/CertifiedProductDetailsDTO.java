package gov.healthit.chpl.dto;

import java.util.Date;

import gov.healthit.chpl.entity.CertifiedProductDetailsEntity;

public class CertifiedProductDetailsDTO {
	
	private Long id;
    private Long testingLabId;
    private String chplProductNumber;
    private String reportFileLocation;
    private String qualityManagementSystemAtt;
    private String acbCertificationId;
    private Long practiceTypeId;
    private String practiceTypeName;
    private Long productClassificationTypeId;
    private String otherAcb;
    private Long certificationStatusId;
    private String certificationStatusName;
    private Long certificationEditionId;
    private String year;
    private Long certificationBodyId;
    private String certificationBodyName;
    private String productClassificationName;
    private Long productVersionId;
    private String productVersion;
    private Long productId;
    private String productName;
    private Long vendorId;
    private String vendorName;
    private Date certificationDate;
    private Integer countCertifications;
    private Integer countCqms;
    private Boolean visibleOnChpl;
    private Date lastModifiedDate;
    private Boolean privacyAttestation;
    
    public CertifiedProductDetailsDTO(){}
    
    public CertifiedProductDetailsDTO(CertifiedProductDetailsEntity entity){
    	
    	this.id = entity.getId();
    	this.acbCertificationId = entity.getAcbCertificationId();
    	this.certificationBodyId = entity.getCertificationBodyId();
    	this.certificationBodyName = entity.getCertificationBodyName();
    	this.certificationEditionId = entity.getCertificationBodyId();
    	this.certificationStatusId = entity.getCertificationStatusId();
    	this.certificationStatusName = entity.getCertificationStatusName();
    	this.chplProductNumber = entity.getChplProductNumber();
    	this.otherAcb = entity.getOtherAcb();
    	this.practiceTypeId = entity.getPracticeTypeId();
       	this.practiceTypeName = entity.getPracticeTypeName();
    	this.productClassificationName = entity.getProductClassificationName();
    	this.productClassificationTypeId = entity.getProductClassificationTypeId();
    	this.productId = entity.getProductId();
    	this.productName = entity.getProductName();
    	this.productVersion = entity.getProductVersion();
    	this.productVersionId = entity.getProductVersionId();
    	this.qualityManagementSystemAtt = entity.getQualityManagementSystemAtt();
    	this.reportFileLocation = entity.getReportFileLocation();
    	this.testingLabId = entity.getTestingLabId();
    	this.vendorId = entity.getVendorId();
    	this.vendorName = entity.getVendorName();
    	this.visibleOnChpl = entity.getVisibleOnChpl();
    	this.privacyAttestation = entity.getPrivacyAttestation();
    	this.year = entity.getYear();
    	this.certificationDate = entity.getCertificationDate();
    	this.countCqms = entity.getCountCqms();
    	this.countCertifications = entity.getCountCertifications();
    	this.lastModifiedDate = entity.getLastModifiedDate();
    }
    
    
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getTestingLabId() {
		return testingLabId;
	}
	public void setTestingLabId(Long testingLabId) {
		this.testingLabId = testingLabId;
	}
	public String getChplProductNumber() {
		return chplProductNumber;
	}
	public void setChplProductNumber(String chplProductNumber) {
		this.chplProductNumber = chplProductNumber;
	}
	public String getReportFileLocation() {
		return reportFileLocation;
	}
	public void setReportFileLocation(String reportFileLocation) {
		this.reportFileLocation = reportFileLocation;
	}
	public String getQualityManagementSystemAtt() {
		return qualityManagementSystemAtt;
	}
	public void setQualityManagementSystemAtt(String qualityManagementSystemAtt) {
		this.qualityManagementSystemAtt = qualityManagementSystemAtt;
	}
	public String getAcbCertificationId() {
		return acbCertificationId;
	}
	public void setAcbCertificationId(String acbCertificationId) {
		this.acbCertificationId = acbCertificationId;
	}
	public Long getPracticeTypeId() {
		return practiceTypeId;
	}
	public void setPracticeTypeId(Long practiceTypeId) {
		this.practiceTypeId = practiceTypeId;
	}
	public Long getProductClassificationTypeId() {
		return productClassificationTypeId;
	}
	public void setProductClassificationTypeId(Long productClassificationTypeId) {
		this.productClassificationTypeId = productClassificationTypeId;
	}
	public String getOtherAcb() {
		return otherAcb;
	}
	public void setOtherAcb(String otherAcb) {
		this.otherAcb = otherAcb;
	}
	public Long getCertificationStatusId() {
		return certificationStatusId;
	}
	public void setCertificationStatusId(Long certificationStatusId) {
		this.certificationStatusId = certificationStatusId;
	}
	public Long getCertificationEditionId() {
		return certificationEditionId;
	}
	public void setCertificationEditionId(Long certificationEditionId) {
		this.certificationEditionId = certificationEditionId;
	}
	public String getYear() {
		return year;
	}
	public void setYear(String year) {
		this.year = year;
	}
	public Long getCertificationBodyId() {
		return certificationBodyId;
	}
	public void setCertificationBodyId(Long certificationBodyId) {
		this.certificationBodyId = certificationBodyId;
	}
	public String getCertificationBodyName() {
		return certificationBodyName;
	}
	public void setCertificationBodyName(String certificationBodyName) {
		this.certificationBodyName = certificationBodyName;
	}
	public Long getProductVersionId() {
		return productVersionId;
	}
	public void setProductVersionId(Long productVersionId) {
		this.productVersionId = productVersionId;
	}
	public String getProductVersion() {
		return productVersion;
	}
	public void setProductVersion(String productVersion) {
		this.productVersion = productVersion;
	}
	public Long getProductId() {
		return productId;
	}
	public void setProductId(Long productId) {
		this.productId = productId;
	}
	public String getProductName() {
		return productName;
	}
	public void setProductName(String productName) {
		this.productName = productName;
	}
	public Long getVendorId() {
		return vendorId;
	}
	public void setVendorId(Long vendorId) {
		this.vendorId = vendorId;
	}
	public String getVendorName() {
		return vendorName;
	}
	public void setVendorName(String vendorName) {
		this.vendorName = vendorName;
	}
	public String getPracticeTypeName() {
		return practiceTypeName;
	}
	public void setPracticeTypeName(String practiceTypeName) {
		this.practiceTypeName = practiceTypeName;
	}
	public Date getCertificationDate() {
		return certificationDate;
	}
	public void setCertificationDate(Date certificationDate) {
		this.certificationDate = certificationDate;
	}
	public String getProductClassificationName() {
		return productClassificationName;
	}
	public void setProductClassificationName(String productClassificationName) {
		this.productClassificationName = productClassificationName;
	}
	public Integer getCountCertifications() {
		return countCertifications;
	}
	public void setCountCertifications(Integer countCertifications) {
		this.countCertifications = countCertifications;
	}
	public Integer getCountCqms() {
		return countCqms;
	}
	public void setCountCqms(Integer countCqms) {
		this.countCqms = countCqms;
	}
	public Boolean getVisibleOnChpl() {
		return visibleOnChpl;
	}
	public void setVisibleOnChpl(Boolean visibleOnChpl) {
		this.visibleOnChpl = visibleOnChpl;
	}

	public Date getLastModifiedDate() {
		return lastModifiedDate;
	}

	public void setLastModifiedDate(Date lastModifiedDate) {
		this.lastModifiedDate = lastModifiedDate;
	}

	public String getCertificationStatusName() {
		return certificationStatusName;
	}

	public void setCertificationStatusName(String certificationStatusName) {
		this.certificationStatusName = certificationStatusName;
	}

	public Boolean getPrivacyAttestation() {
		return privacyAttestation;
	}

	public void setPrivacyAttestation(Boolean privacyAttestation) {
		this.privacyAttestation = privacyAttestation;
	}
	
}
