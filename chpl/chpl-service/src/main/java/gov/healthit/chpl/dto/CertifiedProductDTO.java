package gov.healthit.chpl.dto;


import gov.healthit.chpl.entity.CertifiedProductEntity;

import java.util.Date;

public class CertifiedProductDTO {
	
	
	private Long id;
	private String atcbCertificationId;
	private Long certificationBodyId;
	private Long certificationEditionId;
	private String chplProductNumber;
	private Date creationDate;
	private Boolean deleted;
	private Date lastModifiedDate;
	private Long lastModifiedUser;
	private Long practiceTypeId;
	private Long productClassificationTypeId;
	private Long productVersionId;
	private String qualityManagementSystemAtt;
	private String reportFileLocation;
	private Long testingLabId;
	
	
	public CertifiedProductDTO(){}
	
	public CertifiedProductDTO(CertifiedProductEntity entity){
		
		this.id = entity.getId();
		this.atcbCertificationId = entity.getAtcbCertificationId();
		this.certificationBodyId = entity.getCertificationBodyId();
		this.certificationEditionId = entity.getCertificationEditionId();
		this.chplProductNumber = entity.getChplProductNumber();
		this.creationDate = entity.getCreationDate();
		this.deleted = entity.isDeleted();
		this.lastModifiedDate = entity.getLastModifiedDate();
		this.lastModifiedUser = entity.getLastModifiedUser();
		this.practiceTypeId = entity.getPracticeTypeId();
		this.productClassificationTypeId = entity.getProductClassificationTypeId();
		this.productVersionId = entity.getProductVersionId();
		this.qualityManagementSystemAtt = entity.getQualityManagementSystemAtt();
		this.reportFileLocation = entity.getReportFileLocation();
		this.testingLabId = entity.getTestingLabId();
		
	}
	
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getAtcbCertificationId() {
		return atcbCertificationId;
	}
	public void setAtcbCertificationId(String atcbCertificationId) {
		this.atcbCertificationId = atcbCertificationId;
	}
	public Long getCertificationBodyId() {
		return certificationBodyId;
	}
	public void setCertificationBodyId(Long certificationBodyId) {
		this.certificationBodyId = certificationBodyId;
	}
	public Long getCertificationEditionId() {
		return certificationEditionId;
	}
	public void setCertificationEditionId(Long certificationEditionId) {
		this.certificationEditionId = certificationEditionId;
	}
	public String getChplProductNumber() {
		return chplProductNumber;
	}
	public void setChplProductNumber(String chplProductNumber) {
		this.chplProductNumber = chplProductNumber;
	}
	public Date getCreationDate() {
		return creationDate;
	}
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}
	public Boolean getDeleted() {
		return deleted;
	}
	public void setDeleted(Boolean deleted) {
		this.deleted = deleted;
	}
	public Date getLastModifiedDate() {
		return lastModifiedDate;
	}
	public void setLastModifiedDate(Date lastModifiedDate) {
		this.lastModifiedDate = lastModifiedDate;
	}
	public Long getLastModifiedUser() {
		return lastModifiedUser;
	}
	public void setLastModifiedUser(Long lastModifiedUser) {
		this.lastModifiedUser = lastModifiedUser;
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
	public Long getProductVersionId() {
		return productVersionId;
	}
	public void setProductVersionId(Long productVersionId) {
		this.productVersionId = productVersionId;
	}
	public String getQualityManagementSystemAtt() {
		return qualityManagementSystemAtt;
	}
	public void setQualityManagementSystemAtt(String qualityManagementSystemAtt) {
		this.qualityManagementSystemAtt = qualityManagementSystemAtt;
	}
	public String getReportFileLocation() {
		return reportFileLocation;
	}
	public void setReportFileLocation(String reportFileLocation) {
		this.reportFileLocation = reportFileLocation;
	}
	public Long getTestingLabId() {
		return testingLabId;
	}
	public void setTestingLabId(Long testingLabId) {
		this.testingLabId = testingLabId;
	}
	
}
