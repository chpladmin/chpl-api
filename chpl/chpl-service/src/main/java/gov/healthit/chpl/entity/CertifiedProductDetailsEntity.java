package gov.healthit.chpl.entity;

import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
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
    
	@Basic(optional = true) 
    @Column(name = "sed_intended_user_description")
    private String sedIntendedUserDescription;
	
	@Basic(optional = true)
	@Column(name ="meaningful_use_users")
	private Long meaningfulUseUsers;

	@Basic(optional = true) 
    @Column(name = "sed_testing_end")
    private Date sedTestingEnd;
	
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
    
    @Column(name = "acb_is_deleted")
    private Boolean acbIsDeleted;
    
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
    
    @Basic( optional = true )
	@OneToOne(optional = true, fetch = FetchType.LAZY)
	@JoinColumn(name = "product_id", unique=true, nullable = true, insertable=false, updatable=false)
	private ProductEntity product;
 
    @Column(name = "vendor_id")
    private Long developerId;
    
    @Column(name = "vendor_name")
    private String developerName;
    
    @Column(name = "vendor_code")
    private String developerCode;

    @Column(name = "vendor_website")
    private String developerWebsite;
    
    @Column(name = "vendor_status_id")
    private Long developerStatusId;
    
    @Column(name = "vendor_status_name")
    private String developerStatusName;
    
    @Column(name = "address_id")
    private Long addressId;
    
    @Column(name = "street_line_1")
    private String streetLine1;
    
    @Column(name = "street_line_2")
    private String streetLine2;
    
    @Column(name = "city")
    private String city;
    
    @Column(name = "state")
    private String state;
    
    @Column(name = "zipcode")
    private String zipcode;
    
    @Column(name = "country")
    private String country;
    
    @Column(name = "contact_id")
    private Long contactId;
    
    @Column(name = "first_name")
    private String firstName;
    
    @Column(name = "last_name")
    private String lastName;
    
    @Column(name = "email")
    private String email;
    
    @Column(name = "phone_number")
    private String phoneNumber;
    
    @Column(name = "title")
    private String title;
    
    @Column(name = "creation_date")
    private Date creationDate;
    
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
    
    @Column(name = "last_modified_date")
    private Date lastModifiedDate;

	@Column(name = "ics")
	private Boolean ics;
	
	@Column(name = "sed")
	private Boolean sedTesting;
	
	@Column(name = "qms")
	private Boolean qmsTesting;
	
	@Column(name = "accessibility_certified")
	private Boolean accessibilityCertified;
	
	@Column(name = "product_additional_software")
	private String productAdditionalSoftware;
	
	@Column(name = "transparency_attestation")
	@Type(type = "gov.healthit.chpl.entity.PostgresAttestationType" , parameters ={@org.hibernate.annotations.Parameter(name = "enumClassName",value = "gov.healthit.chpl.entity.AttestationType")} )
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
	
	public Boolean getAcbIsDeleted() {
		return this.acbIsDeleted;
	}
	
	public void setAcbIsDeleted(Boolean acbIsDeleted) {
		this.acbIsDeleted = acbIsDeleted;
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
	
	public Date getCreationDate(){
		return creationDate;
	}
	
	public void setCreationDate(Date creationDate){
		this.creationDate = creationDate;
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
	
	public Long getMeaningfulUseUsers(){
		return meaningfulUseUsers;
	}
	
	public void setMeaningfulUseUsers(Long meaningfulUseUsers){
		this.meaningfulUseUsers = meaningfulUseUsers;
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

	public Boolean getAccessibilityCertified() {
		return accessibilityCertified;
	}

	public void setAccessibilityCertified(Boolean accessibilityCertified) {
		this.accessibilityCertified = accessibilityCertified;
	}

	public String getSedIntendedUserDescription() {
		return sedIntendedUserDescription;
	}

	public void setSedIntendedUserDescription(String sedIntendedUserDescription) {
		this.sedIntendedUserDescription = sedIntendedUserDescription;
	}

	public Date getSedTestingEnd() {
		return sedTestingEnd;
	}

	public void setSedTestingEnd(Date sedTestingEnd) {
		this.sedTestingEnd = sedTestingEnd;
	}

	public Long getAddressId() {
		return addressId;
	}

	public void setAddressId(Long addressId) {
		this.addressId = addressId;
	}

	public String getStreetLine1() {
		return streetLine1;
	}

	public void setStreetLine1(String streetLine1) {
		this.streetLine1 = streetLine1;
	}

	public String getStreetLine2() {
		return streetLine2;
	}

	public void setStreetLine2(String streetLine2) {
		this.streetLine2 = streetLine2;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getZipcode() {
		return zipcode;
	}

	public void setZipcode(String zipcode) {
		this.zipcode = zipcode;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public Long getContactId() {
		return contactId;
	}

	public void setContactId(Long contactId) {
		this.contactId = contactId;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Long getDeveloperStatusId() {
		return developerStatusId;
	}

	public void setDeveloperStatusId(Long developerStatusId) {
		this.developerStatusId = developerStatusId;
	}

	public String getDeveloperStatusName() {
		return developerStatusName;
	}

	public void setDeveloperStatusName(String developerStatusName) {
		this.developerStatusName = developerStatusName;
	}

	public ProductEntity getProduct() {
		return product;
	}

	public void setProduct(ProductEntity product) {
		this.product = product;
	}	
}
