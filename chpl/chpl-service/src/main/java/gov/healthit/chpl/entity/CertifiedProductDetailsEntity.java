package gov.healthit.chpl.entity;


import java.awt.Transparency;
import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Type;


@Entity
@Table(name = "certified_product_details")
public class CertifiedProductDetailsEntity {

	/** Serial Version UID. */
	private static final long serialVersionUID = -2928065796550377879L;
	
    @Id 
	@Basic( optional = false )
	@Column( name = "certified_product_id", nullable = false  )
	private Long id;
    
    @Column(name = "testing_lab_code")
    private String testingLabCode;
    
    @Column(name = "product_code")
    private String productCode;
    
    @Column(name = "version_code")
    private String versionCode;
    
    @Column(name = "ics_code")
    private String icsCode;
    
    @Column(name = "additional_software_code")
    private String additionalSoftwareCode;
    
    @Column(name = "certified_date_code")
    private String certifiedDateCode;
    
    @Column(name = "testing_lab_id")
    private Long testingLabId;
    
    @Column(name = "testing_lab_name")
    private String testingLabName;
    
    @Column(name = "chpl_product_number")
    private String chplProductNumber;
    
    @Column(name = "report_file_location")
    private String reportFileLocation;
    
    @Column(name = "sed_report_file_location")
    private String sedReportFileLocation;
    
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
    
    @Column(name = "certification_body_code")
    private String certificationBodyCode;
    
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
    private Long developerId;
    
    @Column(name = "vendor_name")
    private String developerName;
    
    @Column(name = "vendor_code")
    private String developerCode;

    @Column(name = "vendor_website")
    private String developerWebsite;
    
    @Column(name = "certification_date")
    private Date certificationDate;
    
    @Column(name = "count_certifications")
    private Integer countCertifications;
    
    @Column(name = "count_cqms")
    private Integer countCqms;
    
    @Column(name = "count_corrective_action_plans")
    private Integer countCorrectiveActionPlans;

    @Column(name = "count_current_corrective_action_plans")
    private Integer countCurrentCorrectiveActionPlans;
    
    @Column(name = "count_closed_corrective_action_plans")
    private Integer countClosedCorrectiveActionPlans;
    
	@Column(name = "visible_on_chpl")
    private Boolean visibleOnChpl;
    
    @Column(name = "last_modified_date")
    private Date lastModifiedDate;
	 
	@Column(name = "terms_of_use_url")
	private String termsOfUse;
	
	@Column(name = "api_documentation_url")
	private String apiDocumentation;
	
	@Column(name = "ics")
	private Boolean ics;
	
	@Column(name = "sed")
	private Boolean sedTesting;
	
	@Column(name = "qms")
	private Boolean qmsTesting;
	
	@Column(name = "product_additional_software")
	private String productAdditionalSoftware;
	
	@Column(name = "transparency_attestation")
	@Type(type = "gov.healthit.chpl.entity.PostgresEnumType" , parameters ={@org.hibernate.annotations.Parameter(name = "enumClassName",value = "gov.healthit.chpl.entity.AttestationType")} )
	private AttestationType transparencyAttestation;
	
	@Column(name = "transparency_attestation_url")
	private String transparencyAttestationUrl;
	
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

	public Long getDeveloperId() {
		return developerId;
	}

	public void setDeveloperId(Long developerId) {
		this.developerId = developerId;
	}

	public String getDeveloperName() {
		return developerName;
	}

	public void setDeveloperName(String developerName) {
		this.developerName = developerName;
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

//	public Set<CertificationResultDetailsEntity> getCertResults() {
//		return certResults;
//	}
//
//	public void setCertResults(Set<CertificationResultDetailsEntity> certResults) {
//		this.certResults = certResults;
//	}
//
//	public Set<CQMResultDetailsEntity> getCqmResults() {
//		return cqmResults;
//	}
//
//	public void setCqmResults(Set<CQMResultDetailsEntity> cqmResults) {
//		this.cqmResults = cqmResults;
//	}
//
//	public Set<AdditionalSoftwareEntity> getAdditionalSoftware() {
//		return additionalSoftware;
//	}
//
//	public void setAdditionalSoftware(Set<AdditionalSoftwareEntity> additionalSoftware) {
//		this.additionalSoftware = additionalSoftware;
//	}

	public String getProductCode() {
		return productCode;
	}

	public void setProductCode(String productCode) {
		this.productCode = productCode;
	}

	public String getVersionCode() {
		return versionCode;
	}

	public void setVersionCode(String versionCode) {
		this.versionCode = versionCode;
	}

	public String getAdditionalSoftwareCode() {
		return additionalSoftwareCode;
	}

	public void setAdditionalSoftwareCode(String additionalSoftwareCode) {
		this.additionalSoftwareCode = additionalSoftwareCode;
	}

	public String getCertifiedDateCode() {
		return certifiedDateCode;
	}

	public void setCertifiedDateCode(String certifiedDateCode) {
		this.certifiedDateCode = certifiedDateCode;
	}

	public String getCertificationBodyCode() {
		return certificationBodyCode;
	}

	public void setCertificationBodyCode(String certificationBodyCode) {
		this.certificationBodyCode = certificationBodyCode;
	}

	public String getDeveloperCode() {
		return developerCode;
	}

	public void setDeveloperCode(String developerCode) {
		this.developerCode = developerCode;
	}

	public String getIcsCode() {
		return icsCode;
	}

	public void setIcsCode(String icsCode) {
		this.icsCode = icsCode;
	}

	public String getTermsOfUse() {
		return termsOfUse;
	}

	public void setTermsOfUse(String termsOfUse) {
		this.termsOfUse = termsOfUse;
	}

	public String getApiDocumentation() {
		return apiDocumentation;
	}

	public void setApiDocumentation(String apiDocumentation) {
		this.apiDocumentation = apiDocumentation;
	}

	public AttestationType getTransparencyAttestation() {
		return transparencyAttestation;
	}

	public void setTransparencyAttestation(AttestationType transparencyAttestation) {
		this.transparencyAttestation = transparencyAttestation;
	}

	public String getTestingLabCode() {
		return testingLabCode;
	}

	public void setTestingLabCode(String testingLabCode) {
		this.testingLabCode = testingLabCode;
	}

	public String getTestingLabName() {
		return testingLabName;
	}

	public void setTestingLabName(String testingLabName) {
		this.testingLabName = testingLabName;
	}

	public Boolean getIcs() {
		return ics;
	}

	public void setIcs(Boolean ics) {
		this.ics = ics;
	}

	public Boolean getSedTesting() {
		return sedTesting;
	}

	public void setSedTesting(Boolean sedTesting) {
		this.sedTesting = sedTesting;
	}

	public Boolean getQmsTesting() {
		return qmsTesting;
	}

	public void setQmsTesting(Boolean qmsTesting) {
		this.qmsTesting = qmsTesting;
	}

	public String getDeveloperWebsite() {
		return developerWebsite;
	}

	public void setDeveloperWebsite(String developerWebsite) {
		this.developerWebsite = developerWebsite;
	}

	public String getSedReportFileLocation() {
		return sedReportFileLocation;
	}

	public void setSedReportFileLocation(String sedReportFileLocation) {
		this.sedReportFileLocation = sedReportFileLocation;
	}

	public String getProductAdditionalSoftware() {
		return productAdditionalSoftware;
	}

	public void setProductAdditionalSoftware(String productAdditionalSoftware) {
		this.productAdditionalSoftware = productAdditionalSoftware;
	}

	public String getTransparencyAttestationUrl() {
		return transparencyAttestationUrl;
	}

	public void setTransparencyAttestationUrl(String transparencyAttestationUrl) {
		this.transparencyAttestationUrl = transparencyAttestationUrl;
	}
	public Integer getCountCurrentCorrectiveActionPlans() {
		return countCurrentCorrectiveActionPlans;
	}

	public void setCountCurrentCorrectiveActionPlans(Integer countCurrentCorrectiveActionPlans) {
		this.countCurrentCorrectiveActionPlans = countCurrentCorrectiveActionPlans;
	}

	public Integer getCountClosedCorrectiveActionPlans() {
		return countClosedCorrectiveActionPlans;
	}

	public void setCountClosedCorrectiveActionPlans(Integer countClosedCorrectiveActionPlans) {
		this.countClosedCorrectiveActionPlans = countClosedCorrectiveActionPlans;
	}	
}
