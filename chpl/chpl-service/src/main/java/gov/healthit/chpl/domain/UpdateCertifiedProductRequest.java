package gov.healthit.chpl.domain;

public class UpdateCertifiedProductRequest {
	
	private Long id;
	
	//foreign key references that are changeable
	private Long testingLabId; //no create
	private Long certificationBodyId; //no create
	private Long practiceTypeId; //no create
	private Long productClassificationTypeId; //no create
	private Long certificationStatusId; //no create
	
	//direct properties of a certified product that are changeable
	private String chplProductNumber;
	private String reportFileLocation;
	private String qualityManagementSystemAtt;
	private String acbCertificationId;
	private String otherAcb;
	private Boolean isChplVisible;
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
	public Long getCertificationBodyId() {
		return certificationBodyId;
	}
	public void setCertificationBodyId(Long certificationBodyId) {
		this.certificationBodyId = certificationBodyId;
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
	public Long getCertificationStatusId() {
		return certificationStatusId;
	}
	public void setCertificationStatusId(Long certificationStatusId) {
		this.certificationStatusId = certificationStatusId;
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
	public String getOtherAcb() {
		return otherAcb;
	}
	public void setOtherAcb(String otherAcb) {
		this.otherAcb = otherAcb;
	}
	public Boolean getIsChplVisible() {
		return isChplVisible;
	}
	public void setIsChplVisible(Boolean isChplVisible) {
		this.isChplVisible = isChplVisible;
	}
}
