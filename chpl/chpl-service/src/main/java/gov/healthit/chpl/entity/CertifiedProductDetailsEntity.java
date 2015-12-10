package gov.healthit.chpl.entity;


import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;


@Entity
@Table(name = "certified_product_details")
public class CertifiedProductDetailsEntity {

	/** Serial Version UID. */
	private static final long serialVersionUID = -2928065796550377879L;
	
    @Id 
	@Basic( optional = false )
	@Column( name = "certified_product_id", nullable = false  )
	private Long id;
    
    @Column(name = "testing_lab_id")
    private Long testingLabId;
    
    @Column(name = "chpl_product_number")
    private String chplProductNumber;
    
    @Column(name = "report_file_location")
    private String reportFileLocation;
    
    @Column(name = "quality_management_system_att")
    private String qualityManagementSystemAtt;
    
    @Column(name = "acb_certification_id")
    private String acbCertificationId;
    
    @Column(name = "practice_type_id")
    private Long practiceTypeId;
    
    @Column(name = "practice_type_name")
    private String practiceTypeName;
    
    @Column(name = "product_classification_type_id")
    private Long productClassificationTypeId;
    
    @Column(name = "other_acb")
    private String otherAcb;
    
    @Column(name = "certification_status_id")
    private Long certificationStatusId;
    
    @Column(name = "certification_status_name")
    private String certificationStatusName;
    
    @Column(name = "certification_edition_id")
    private Long certificationEditionId;
    
    @Column(name = "year")
    private String year;
    
    @Column(name = "certification_body_id")
    private Long certificationBodyId;
    
    @Column(name = "certification_body_name")
    private String certificationBodyName;
    
    @Column(name = "product_classification_name")
    private String productClassificationName;
    
    @Column(name = "product_version_id")
    private Long productVersionId;
    
    @Column(name = "product_version")
    private String productVersion;
    
    @Column(name = "product_id")
    private Long productId;
    
    @Column(name = "product_name")
    private String productName;
    
    @Column(name = "vendor_id")
    private Long vendorId;
    
    @Column(name = "vendor_name")
    private String vendorName;

    @Column(name = "certification_date")
    private Date certificationDate;
    
    @Column(name = "count_certifications")
    private Integer countCertifications;
    
    @Column(name = "count_cqms")
    private Integer countCqms;
    
    @Column(name = "count_corrective_action_plans")
    private Integer countCorrectiveActionPlans;

	@Column(name = "visible_on_chpl")
    private Boolean visibleOnChpl;
    
    @Column(name = "last_modified_date")
    private Date lastModifiedDate;

    @Column(name = "privacy_attestation")
	private Boolean privacyAttestation;
    
	@Basic( optional = true )
	@OneToMany(fetch = FetchType.LAZY)
	@JoinColumn(name = "certified_product_id", nullable = true)
	private Set<AdditionalSoftwareEntity> additionalSoftware;
	
	@Basic( optional = true )
	@OneToMany(fetch = FetchType.LAZY)
	@JoinColumn(name = "certified_product_id", nullable = true)
	private Set<CertificationResultDetailsEntity> certResults;
	
	@Basic( optional = true )
	@OneToMany(fetch = FetchType.LAZY)
	@JoinColumn(name = "certified_product_id", nullable = true)
	private Set<CQMResultDetailsEntity> cqmResults;
	
//	 public boolean equals(CertifiedProductDetailsEntity other) {
//	    	if(other == null) {
//	    		return false;
//	    	}
//	    	if(other.getId() == null) {
//	    		return false;
//	    	}
//	    	if(this.getId() == null) {
//	    		return false;
//	    	}
//	    	if(other.getId().longValue() == this.getId().longValue()) {
//	    		return true;
//	    	}
//	    	return false;
//	    }
	 
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

	public String getProductClassificationName() {
		return productClassificationName;
	}

	public void setProductClassificationName(String productClassificationName) {
		this.productClassificationName = productClassificationName;
	}
	
	public String getPracticeTypeName() {
		return practiceTypeName;
	}

	public void setPracticeTypeName(String practiceTypeName) {
		this.practiceTypeName = practiceTypeName;
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
    
	public Date getCertificationDate() {
		return certificationDate;
	}

	public void setCertificationDate(Date certificationDate) {
		this.certificationDate = certificationDate;
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
	
    public Integer getCountCorrectiveActionPlans() {
		return countCorrectiveActionPlans;
	}

	public void setCountCorrectiveActionPlans(Integer countCorrectiveActionPlans) {
		this.countCorrectiveActionPlans = countCorrectiveActionPlans;
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

	public Set<CertificationResultDetailsEntity> getCertResults() {
		return certResults;
	}

	public void setCertResults(Set<CertificationResultDetailsEntity> certResults) {
		this.certResults = certResults;
	}

	public Set<CQMResultDetailsEntity> getCqmResults() {
		return cqmResults;
	}

	public void setCqmResults(Set<CQMResultDetailsEntity> cqmResults) {
		this.cqmResults = cqmResults;
	}

	public Set<AdditionalSoftwareEntity> getAdditionalSoftware() {
		return additionalSoftware;
	}

	public void setAdditionalSoftware(Set<AdditionalSoftwareEntity> additionalSoftware) {
		this.additionalSoftware = additionalSoftware;
	}	
}
