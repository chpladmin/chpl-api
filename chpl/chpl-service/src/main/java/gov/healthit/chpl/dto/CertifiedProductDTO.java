package gov.healthit.chpl.dto;


import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class CertifiedProductDTO {
	
	private Long id;
	private Set<AdditionalSoftwareDTO> additionalSoftwares = new HashSet<AdditionalSoftwareDTO>();
	private String atcbCertificationId;
	private Set<CertificationEventDTO> certificationEvents = new HashSet<CertificationEventDTO>();
	private Set<CertificationResultDTO> certificationResults = new HashSet<CertificationResultDTO>();
	private CertificationBodyDTO certificationBody;
	private CertificationEditionDTO certificationEdition;
	private Set<CertifiedProductCqmEditionMapDTO> certifiedProductCqmEditionMaps = new HashSet<CertifiedProductCqmEditionMapDTO>();
	private String chplProductNumber;
	private Date creationDate;
	private Boolean deleted;
	private Date lastModifiedDate;
	private Long lastModifiedUser;
	private Set<NewerStandardsMetDTO> newerStandardsMets = new HashSet<NewerStandardsMetDTO>();
	private PracticeTypeDTO practiceType;
	private ProductClassificationTypeDTO productClassificationType;
	private ProductVersionDTO productVersion;
	private String qualityManagementSystemAtt;
	private String reportFileLocation;
	private Set<TestDataAlterationDTO> testDataAlterations = new HashSet<TestDataAlterationDTO>();
	private TestingLabDTO testingLab;
	private Set<TestResultSummaryVersionDTO> testResultSummaryVersions = new HashSet<TestResultSummaryVersionDTO>();
	private Set<UtilizedTestToolDTO> utilizedTestTools = new HashSet<UtilizedTestToolDTO>();
	
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Set<AdditionalSoftwareDTO> getAdditionalSoftwares() {
		return additionalSoftwares;
	}
	public void setAdditionalSoftwares(
			Set<AdditionalSoftwareDTO> additionalSoftwares) {
		this.additionalSoftwares = additionalSoftwares;
	}
	public String getAtcbCertificationId() {
		return atcbCertificationId;
	}
	public void setAtcbCertificationId(String atcbCertificationId) {
		this.atcbCertificationId = atcbCertificationId;
	}
	public Set<CertificationEventDTO> getCertificationEvents() {
		return certificationEvents;
	}
	public void setCertificationEvents(
			Set<CertificationEventDTO> certificationEvents) {
		this.certificationEvents = certificationEvents;
	}
	public Set<CertificationResultDTO> getCertificationResults() {
		return certificationResults;
	}
	public void setCertificationResults(
			Set<CertificationResultDTO> certificationResults) {
		this.certificationResults = certificationResults;
	}
	public CertificationBodyDTO getCertificationBody() {
		return certificationBody;
	}
	public void setCertificationBody(CertificationBodyDTO certificationBody) {
		this.certificationBody = certificationBody;
	}
	public CertificationEditionDTO getCertificationEdition() {
		return certificationEdition;
	}
	public void setCertificationEdition(CertificationEditionDTO certificationEdition) {
		this.certificationEdition = certificationEdition;
	}
	public Set<CertifiedProductCqmEditionMapDTO> getCertifiedProductCqmEditionMaps() {
		return certifiedProductCqmEditionMaps;
	}
	public void setCertifiedProductCqmEditionMaps(
			Set<CertifiedProductCqmEditionMapDTO> certifiedProductCqmEditionMaps) {
		this.certifiedProductCqmEditionMaps = certifiedProductCqmEditionMaps;
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
	public Set<NewerStandardsMetDTO> getNewerStandardsMets() {
		return newerStandardsMets;
	}
	public void setNewerStandardsMets(Set<NewerStandardsMetDTO> newerStandardsMets) {
		this.newerStandardsMets = newerStandardsMets;
	}
	public PracticeTypeDTO getPracticeType() {
		return practiceType;
	}
	public void setPracticeType(PracticeTypeDTO practiceType) {
		this.practiceType = practiceType;
	}
	public ProductClassificationTypeDTO getProductClassificationType() {
		return productClassificationType;
	}
	public void setProductClassificationType(
			ProductClassificationTypeDTO productClassificationType) {
		this.productClassificationType = productClassificationType;
	}
	public ProductVersionDTO getProductVersion() {
		return productVersion;
	}
	public void setProductVersion(ProductVersionDTO productVersion) {
		this.productVersion = productVersion;
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
	public Set<TestDataAlterationDTO> getTestDataAlterations() {
		return testDataAlterations;
	}
	public void setTestDataAlterations(
			Set<TestDataAlterationDTO> testDataAlterations) {
		this.testDataAlterations = testDataAlterations;
	}
	public TestingLabDTO getTestingLab() {
		return testingLab;
	}
	public void setTestingLab(TestingLabDTO testingLab) {
		this.testingLab = testingLab;
	}
	public Set<TestResultSummaryVersionDTO> getTestResultSummaryVersions() {
		return testResultSummaryVersions;
	}
	public void setTestResultSummaryVersions(
			Set<TestResultSummaryVersionDTO> testResultSummaryVersions) {
		this.testResultSummaryVersions = testResultSummaryVersions;
	}
	public Set<UtilizedTestToolDTO> getUtilizedTestTools() {
		return utilizedTestTools;
	}
	public void setUtilizedTestTools(Set<UtilizedTestToolDTO> utilizedTestTools) {
		this.utilizedTestTools = utilizedTestTools;
	}
	
	
	
	
	
}
